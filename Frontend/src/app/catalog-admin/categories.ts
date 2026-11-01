import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { messages } from '../messages';
import { fillTemplate } from '../shared/fill-template';
import { describeRequestProblem } from '../shared/request-errors';
import { CatalogAdminDesk, Category, CategoryStatus } from './catalog-admin-desk';

const MAX_CATEGORY = 100;

interface RowState {
  readonly working: boolean;
  readonly problem: string;
}

@Component({
  selector: 'app-categories',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './categories.html',
  styleUrls: ['../shell/section-page.css', '../shared/feedback.css', '../shared/admin-page.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Categories {
  private readonly desk = inject(CatalogAdminDesk);
  private readonly formBuilder = inject(NonNullableFormBuilder);

  protected readonly messages = messages.categories;
  protected readonly maxLength = MAX_CATEGORY;

  protected readonly createForm = this.formBuilder.group({
    name: ['', [Validators.required, Validators.maxLength(MAX_CATEGORY)]],
  });
  protected readonly renameForm = this.formBuilder.group({
    name: ['', [Validators.required, Validators.maxLength(MAX_CATEGORY)]],
  });

  protected readonly categories = signal<readonly Category[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadProblem = signal('');
  protected readonly creating = signal(false);
  protected readonly createProblem = signal('');
  protected readonly createdText = signal('');
  protected readonly renaming = signal<string | null>(null);
  protected readonly rows = signal<ReadonlyMap<string, RowState>>(new Map());

  constructor() {
    void this.load();
  }

  protected async load(): Promise<void> {
    this.loading.set(true);
    this.loadProblem.set('');

    try {
      this.categories.set(sorted(await this.desk.listCategories()));
    } catch (error) {
      this.loadProblem.set(describeRequestProblem(error, this.messages.loadFailed).message);
    } finally {
      this.loading.set(false);
    }
  }

  protected isActive(category: Category): boolean {
    return category.status === CategoryStatus.active;
  }

  protected titlesOf(category: Category): string {
    if (category.activeTitles === 0) {
      return this.messages.titlesNone;
    }
    return category.activeTitles === 1
      ? this.messages.titlesOne
      : fillTemplate(this.messages.titlesMany, { count: String(category.activeTitles) });
  }

  protected rowOf(id: string): RowState {
    return this.rows().get(id) ?? { working: false, problem: '' };
  }

  protected nameError(form: typeof this.createForm): string {
    const control = form.controls.name;
    if (!control.touched || control.valid) {
      return '';
    }
    return control.hasError('maxlength') ? this.messages.nameTooLong : this.messages.nameRequired;
  }

  protected async create(): Promise<void> {
    if (this.creating()) {
      return;
    }
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.creating.set(true);
    this.createProblem.set('');
    this.createdText.set('');

    try {
      const category = await this.desk.createCategory(this.createForm.getRawValue().name);
      this.replace(category);
      this.createdText.set(fillTemplate(this.messages.created, { name: category.name }));
      this.createForm.reset();
    } catch (error) {
      this.createProblem.set(describeRequestProblem(error, this.messages.actionFailed).message);
    } finally {
      this.creating.set(false);
    }
  }

  protected startRename(category: Category): void {
    this.renaming.set(category.id);
    this.renameForm.reset({ name: category.name });
    this.setRow(category.id, { working: false, problem: '' });
  }

  protected cancelRename(): void {
    this.renaming.set(null);
  }

  protected async rename(category: Category): Promise<void> {
    if (this.renameForm.invalid) {
      this.renameForm.markAllAsTouched();
      return;
    }
    await this.run(category.id, async () => {
      this.replace(await this.desk.renameCategory(category.id, this.renameForm.getRawValue().name));
      this.renaming.set(null);
    });
  }

  protected async toggle(category: Category): Promise<void> {
    await this.run(category.id, async () => {
      this.replace(
        this.isActive(category)
          ? await this.desk.deactivateCategory(category.id)
          : await this.desk.activateCategory(category.id),
      );
    });
  }

  private async run(id: string, action: () => Promise<void>): Promise<void> {
    if (this.rowOf(id).working) {
      return;
    }

    this.setRow(id, { working: true, problem: '' });
    try {
      await action();
      this.setRow(id, { working: false, problem: '' });
    } catch (error) {
      this.setRow(id, {
        working: false,
        problem: describeRequestProblem(error, this.messages.actionFailed).message,
      });
    }
  }

  private replace(category: Category): void {
    this.categories.update((current) =>
      sorted([...current.filter((existing) => existing.id !== category.id), category]),
    );
  }

  private setRow(id: string, state: RowState): void {
    this.rows.update((current) => new Map(current).set(id, state));
  }
}

function sorted(categories: readonly Category[]): readonly Category[] {
  return [...categories].sort((first, second) => first.name.localeCompare(second.name, 'es'));
}
