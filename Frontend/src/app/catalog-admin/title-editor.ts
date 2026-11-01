import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { messages } from '../messages';
import { fillTemplate } from '../shared/fill-template';
import { RequestProblem, describeRequestProblem } from '../shared/request-errors';
import {
  Branch,
  CatalogAdminDesk,
  Category,
  CategoryStatus,
  Title,
  TitleDraft,
  TitleFormat,
  TitleStatus,
} from './catalog-admin-desk';

const MAX_TITLE = 300;
const MAX_NAME = 150;
const MAX_BARCODE = 50;
const FIRST_YEAR = 1000;
const LAST_YEAR = 2100;
const ISBN_PATTERN = /^(\d{9}[\dX]|\d{13})$/;

interface CategoryOption {
  readonly id: string;
  readonly label: string;
}

@Component({
  selector: 'app-title-editor',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './title-editor.html',
  styleUrls: ['../shell/section-page.css', '../shared/feedback.css', '../shared/admin-page.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TitleEditor {
  private readonly desk = inject(CatalogAdminDesk);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly titleId = (inject(ActivatedRoute).snapshot.paramMap.get('id') ?? '').trim();

  protected readonly messages = messages.titleEditor;
  protected readonly formats = TitleFormat;
  protected readonly editing = this.titleId !== '';

  protected readonly form = this.formBuilder.group({
    title: ['', [Validators.required, Validators.maxLength(MAX_TITLE)]],
    authors: ['', [authorsValidator]],
    publisher: ['', [Validators.required, Validators.maxLength(MAX_NAME)]],
    category: ['', [Validators.required]],
    isbn: ['', [isbnValidator]],
    year: ['', [yearValidator]],
    format: [TitleFormat.physical as TitleFormat],
    branch: [''],
    barcodes: ['', [barcodesValidator]],
  });

  protected readonly loading = signal(true);
  protected readonly loadProblem = signal<RequestProblem | null>(null);
  protected readonly categories = signal<readonly Category[]>([]);
  protected readonly branches = signal<readonly Branch[] | null>([]);
  protected readonly current = signal<Title | null>(null);
  protected readonly format = signal<TitleFormat>(TitleFormat.physical);
  protected readonly barcodeCount = signal(0);
  protected readonly branchMissing = signal(false);

  protected readonly saving = signal(false);
  protected readonly saveProblem = signal<RequestProblem | null>(null);
  protected readonly saved = signal<Title | null>(null);

  protected readonly confirmingWithdraw = signal(false);
  protected readonly withdrawing = signal(false);
  protected readonly withdrawProblem = signal('');

  protected readonly withdrawn = computed(() => this.current()?.status === TitleStatus.withdrawn);

  protected readonly categoryOptions = computed<readonly CategoryOption[]>(() => {
    const selected = this.current()?.category ?? '';
    return this.categories()
      .filter((category) => category.status === CategoryStatus.active || category.id === selected)
      .map((category) => ({
        id: category.id,
        label:
          category.status === CategoryStatus.active
            ? category.name
            : fillTemplate(this.messages.inactiveCategory, { name: category.name }),
      }));
  });

  protected readonly copiesText = computed(() => {
    const count = this.barcodeCount();
    return count === 1
      ? this.messages.oneCopy
      : fillTemplate(this.messages.copiesCount, { count: String(count) });
  });

  protected readonly savedText = computed(() =>
    fillTemplate(this.messages.savedText, { title: this.saved()?.title ?? '' }),
  );

  protected readonly withdrawQuestion = computed(() =>
    fillTemplate(this.messages.withdrawConfirm, { title: this.current()?.title ?? '' }),
  );

  protected readonly saveTrace = computed(() => traceOf(this.saveProblem(), this.messages.traceId));
  protected readonly loadTrace = computed(() => traceOf(this.loadProblem(), this.messages.traceId));

  constructor() {
    this.form.controls.format.valueChanges.subscribe((format) => this.format.set(format));
    this.form.controls.barcodes.valueChanges.subscribe((text) => {
      this.barcodeCount.set(linesOf(text).length);
      this.branchMissing.set(false);
    });
    this.form.controls.branch.valueChanges.subscribe(() => this.branchMissing.set(false));
    void this.load();
  }

  protected async load(): Promise<void> {
    this.loading.set(true);
    this.loadProblem.set(null);

    try {
      const [categories, title] = await Promise.all([
        this.desk.listCategories(),
        this.editing ? this.desk.findTitle(this.titleId) : Promise.resolve(null),
      ]);
      this.categories.set(categories);
      this.branches.set(await this.desk.listBranches().catch(() => null));

      if (title !== null) {
        this.show(title);
      }
    } catch (error) {
      this.loadProblem.set(describeRequestProblem(error, this.messages.loadFailed));
    } finally {
      this.loading.set(false);
    }
  }

  protected errorOf(field: keyof typeof this.form.controls): string {
    const control = this.form.controls[field];
    if (!control.touched || control.valid) {
      return '';
    }

    const errors = control.errors ?? {};
    const texts: Record<string, string> = {
      'title.required': this.messages.titleRequired,
      'title.maxlength': this.messages.titleTooLong,
      'authors.required': this.messages.authorsRequired,
      'authors.repeated': this.messages.authorsRepeated,
      'authors.tooLong': this.messages.authorsTooLong,
      'publisher.required': this.messages.publisherRequired,
      'publisher.maxlength': this.messages.publisherTooLong,
      'category.required': this.messages.categoryRequired,
      'isbn.isbn': this.messages.isbnInvalid,
      'year.year': this.messages.yearInvalid,
      'barcodes.repeated': this.messages.barcodesRepeated,
      'barcodes.tooLong': this.messages.barcodesTooLong,
    };

    const key = Object.keys(errors)[0] ?? '';
    return texts[`${field}.${key}`] ?? '';
  }

  protected async save(): Promise<void> {
    if (this.saving() || this.withdrawn()) {
      return;
    }

    const value = this.form.getRawValue();
    const barcodes = value.format === TitleFormat.physical ? linesOf(value.barcodes) : [];
    const needsBranch = barcodes.length > 0 && value.branch === '';
    this.branchMissing.set(needsBranch);

    if (this.form.invalid || needsBranch) {
      this.form.markAllAsTouched();
      return;
    }

    const draft: TitleDraft = {
      title: value.title,
      isbn: normalizedIsbn(value.isbn),
      year: value.year.trim() === '' ? 0 : Number(value.year),
      format: value.format,
      category: value.category,
      publisher: value.publisher,
      authors: authorsOf(value.authors),
      branch: value.branch === '' ? null : value.branch,
      barcodes,
    };

    this.saving.set(true);
    this.saveProblem.set(null);
    this.saved.set(null);

    try {
      const title = this.editing
        ? await this.desk.updateTitle(this.titleId, draft)
        : await this.desk.registerTitle(draft);
      this.saved.set(title);
      if (this.editing) {
        this.show(title);
      } else {
        this.form.disable();
      }
    } catch (error) {
      this.saveProblem.set(describeRequestProblem(error, this.messages.saveFailed));
    } finally {
      this.saving.set(false);
    }
  }

  protected registerAnother(): void {
    this.saved.set(null);
    this.saveProblem.set(null);
    this.form.enable();
    this.form.reset({ format: TitleFormat.physical, branch: this.form.controls.branch.value });
  }

  protected askWithdraw(): void {
    this.withdrawProblem.set('');
    this.confirmingWithdraw.set(true);
  }

  protected cancelWithdraw(): void {
    this.confirmingWithdraw.set(false);
  }

  protected async withdraw(): Promise<void> {
    if (this.withdrawing()) {
      return;
    }

    this.withdrawing.set(true);
    this.withdrawProblem.set('');

    try {
      const title = await this.desk.withdrawTitle(this.titleId);
      this.saved.set(null);
      this.confirmingWithdraw.set(false);
      this.show(title);
    } catch (error) {
      this.withdrawProblem.set(describeRequestProblem(error, this.messages.withdrawFailed).message);
    } finally {
      this.withdrawing.set(false);
    }
  }

  private show(title: Title): void {
    this.current.set(title);
    this.form.reset({
      title: title.title,
      authors: title.authors.join(', '),
      publisher: title.publisher,
      category: title.category,
      isbn: title.isbn,
      year: title.year > 0 ? String(title.year) : '',
      format: title.format === TitleFormat.digital ? TitleFormat.digital : TitleFormat.physical,
      branch: this.form.controls.branch.value,
      barcodes: '',
    });
    this.form.controls.format.disable();

    if (title.status === TitleStatus.withdrawn) {
      this.form.disable();
    }
  }
}

function traceOf(problem: RequestProblem | null, template: string): string {
  const traceId = problem?.traceId ?? '';
  return traceId === '' ? '' : fillTemplate(template, { traceId });
}

function authorsOf(text: string): string[] {
  return text
    .split(',')
    .map((author) => author.trim())
    .filter((author) => author !== '');
}

function linesOf(text: string): string[] {
  return text
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line !== '');
}

function normalizedIsbn(text: string): string {
  return text.replace(/[\s-]/g, '').toUpperCase();
}

function authorsValidator(control: AbstractControl<string>): ValidationErrors | null {
  const authors = authorsOf(control.value ?? '');
  if (authors.length === 0) {
    return { required: true };
  }
  if (authors.some((author) => author.length > MAX_NAME)) {
    return { tooLong: true };
  }
  const unique = new Set(authors.map((author) => author.toLocaleLowerCase('es')));
  return unique.size === authors.length ? null : { repeated: true };
}

function barcodesValidator(control: AbstractControl<string>): ValidationErrors | null {
  const barcodes = linesOf(control.value ?? '');
  if (barcodes.some((barcode) => barcode.length > MAX_BARCODE)) {
    return { tooLong: true };
  }
  return new Set(barcodes).size === barcodes.length ? null : { repeated: true };
}

function isbnValidator(control: AbstractControl<string>): ValidationErrors | null {
  const isbn = normalizedIsbn(control.value ?? '');
  return isbn === '' || ISBN_PATTERN.test(isbn) ? null : { isbn: true };
}

function yearValidator(control: AbstractControl<string>): ValidationErrors | null {
  const text = (control.value ?? '').trim();
  if (text === '') {
    return null;
  }
  const year = Number(text);
  return Number.isInteger(year) && year >= FIRST_YEAR && year <= LAST_YEAR ? null : { year: true };
}
