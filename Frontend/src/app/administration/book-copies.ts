import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  afterNextRender,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import {
  FormArray,
  FormControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { messages } from '../messages';
import { CurrentSession } from '../session/current-session';
import { PermissionCode } from '../session/permission-code';
import { fillTemplate } from '../shared/fill-template';
import { RequestProblem, describeRequestProblem } from '../shared/request-errors';
import {
  AdministrationDesk,
  BookCopyLocation,
  BookCopyStatus,
  Branch,
  Level,
} from './administration-desk';

const MAX_VALUE = 40;

@Component({
  selector: 'app-book-copies',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './book-copies.html',
  styleUrls: [
    '../shell/section-page.css',
    '../shared/feedback.css',
    '../shared/admin-page.css',
    '../shared/admin-details.css',
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookCopies {
  private readonly desk = inject(AdministrationDesk);
  private readonly currentSession = inject(CurrentSession);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly barcodeInput = viewChild<ElementRef<HTMLInputElement>>('barcodeInput');

  protected readonly messages = messages.bookCopies;

  protected readonly searchForm = this.formBuilder.group({
    barcode: ['', [Validators.required, Validators.maxLength(50)]],
  });
  protected readonly branchControl = new FormControl('', { nonNullable: true });
  protected readonly valuesForm = new FormArray<FormControl<string>>([]);

  protected readonly searching = signal(false);
  protected readonly searchProblem = signal<RequestProblem | null>(null);
  protected readonly location = signal<BookCopyLocation | null>(null);

  protected readonly branches = signal<readonly Branch[]>([]);
  protected readonly levels = signal<readonly Level[]>([]);
  protected readonly placesProblem = signal(false);

  protected readonly relocating = signal(false);
  protected readonly relocateProblem = signal('');
  protected readonly relocated = signal(false);

  protected readonly confirmingWithdraw = signal(false);
  protected readonly withdrawing = signal(false);
  protected readonly withdrawProblem = signal('');
  protected readonly withdrawnNow = signal(false);

  protected readonly canRelocate = computed(() =>
    this.currentSession.allows(PermissionCode.relocateBookCopy),
  );
  protected readonly canWithdraw = computed(() =>
    this.currentSession.allows(PermissionCode.withdrawBookCopy),
  );
  protected readonly isWithdrawn = computed(
    () => this.location()?.status === BookCopyStatus.withdrawn,
  );

  protected readonly statusText = computed(() => {
    const status = this.location()?.status ?? '';
    return this.messages.statuses[status] ?? status;
  });

  protected readonly statusClass = computed(() => {
    switch (this.location()?.status) {
      case BookCopyStatus.available:
        return 'chip chip--success';
      case BookCopyStatus.withdrawn:
        return 'chip chip--error';
      default:
        return 'chip chip--neutral';
    }
  });

  protected readonly withdrawQuestion = computed(() =>
    fillTemplate(this.messages.withdrawConfirm, { barcode: this.location()?.barcode ?? '' }),
  );

  protected readonly traceText = computed(() => {
    const traceId = this.searchProblem()?.traceId ?? '';
    return traceId === '' ? '' : fillTemplate(this.messages.traceId, { traceId });
  });

  constructor() {
    afterNextRender(() => this.barcodeInput()?.nativeElement.focus());
    void this.loadPlaces();
  }

  protected async search(): Promise<void> {
    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    this.searching.set(true);
    this.searchProblem.set(null);
    this.resetActions();

    try {
      this.show(await this.desk.findLocation(this.searchForm.getRawValue().barcode));
    } catch (error) {
      this.location.set(null);
      this.searchProblem.set(describeRequestProblem(error, this.messages.searchFailed));
    } finally {
      this.searching.set(false);
    }
  }

  protected valueLabel(index: number): string {
    return this.levels()[index]?.name ?? '';
  }

  protected async relocate(): Promise<void> {
    const current = this.location();
    if (current === null || this.relocating()) {
      return;
    }

    const branch = this.branchControl.value;
    const values = this.valuesForm.getRawValue().map((value) => value.trim());
    const lastFilled = values.reduce((last, value, index) => (value !== '' ? index : last), -1);
    const filled = values.slice(0, lastFilled + 1);

    this.relocated.set(false);
    if (branch === '') {
      this.relocateProblem.set(this.messages.branchRequired);
      return;
    }
    if (filled.some((value) => value === '')) {
      this.relocateProblem.set(this.messages.valueGap);
      return;
    }
    if (filled.some((value) => value.length > MAX_VALUE)) {
      this.relocateProblem.set(this.messages.valueTooLong);
      return;
    }

    this.relocating.set(true);
    this.relocateProblem.set('');

    try {
      this.show(await this.desk.relocate(current.barcode, branch, filled));
      this.relocated.set(true);
    } catch (error) {
      this.relocateProblem.set(describeRequestProblem(error, this.messages.relocateFailed).message);
    } finally {
      this.relocating.set(false);
    }
  }

  protected askWithdraw(): void {
    this.withdrawProblem.set('');
    this.confirmingWithdraw.set(true);
  }

  protected cancelWithdraw(): void {
    this.confirmingWithdraw.set(false);
  }

  protected async withdraw(): Promise<void> {
    const current = this.location();
    if (current === null || this.withdrawing()) {
      return;
    }

    this.withdrawing.set(true);
    this.withdrawProblem.set('');

    try {
      this.show(await this.desk.withdrawBookCopy(current.barcode));
      this.confirmingWithdraw.set(false);
      this.withdrawnNow.set(true);
    } catch (error) {
      this.withdrawProblem.set(describeRequestProblem(error, this.messages.withdrawFailed).message);
    } finally {
      this.withdrawing.set(false);
    }
  }

  private async loadPlaces(): Promise<void> {
    try {
      const [branches, levels] = await Promise.all([
        this.desk.listBranches(),
        this.desk.listLevels(),
      ]);
      this.branches.set(
        [...branches].sort((first, second) => first.name.localeCompare(second.name, 'es')),
      );
      this.levels.set([...levels].sort((first, second) => first.depth - second.depth));
      this.placesProblem.set(false);
    } catch {
      this.placesProblem.set(true);
    }
  }

  private show(location: BookCopyLocation): void {
    this.location.set(location);
    this.branchControl.setValue(location.branch?.id ?? '');
    this.valuesForm.clear();
    for (const level of this.levels()) {
      const spot = location.spots.find((candidate) => candidate.depth === level.depth);
      this.valuesForm.push(new FormControl(spot?.value ?? '', { nonNullable: true }));
    }
  }

  private resetActions(): void {
    this.relocated.set(false);
    this.relocateProblem.set('');
    this.confirmingWithdraw.set(false);
    this.withdrawProblem.set('');
    this.withdrawnNow.set(false);
  }
}
