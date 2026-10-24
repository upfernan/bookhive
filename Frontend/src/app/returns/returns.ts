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
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { messages } from '../messages';
import { fillTemplate } from '../shared/fill-template';
import { RequestProblem, describeRequestProblem } from '../shared/request-errors';
import { ReturnDesk, ReturnedBook } from './return-desk';

const DATE_FORMAT = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'long', year: 'numeric' });
const MONEY_FORMAT = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

@Component({
  selector: 'app-returns',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './returns.html',
  styleUrls: ['../shell/section-page.css', '../shared/feedback.css', './returns.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Returns {
  private readonly desk = inject(ReturnDesk);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly barcodeInput = viewChild<ElementRef<HTMLInputElement>>('barcodeInput');

  protected readonly messages = messages.returns;

  protected readonly form = this.formBuilder.group({
    barcode: ['', Validators.required],
  });

  protected readonly registering = signal(false);
  protected readonly returned = signal<ReturnedBook | null>(null);
  protected readonly problem = signal<RequestProblem | null>(null);

  protected readonly summary = computed(() => {
    const book = this.returned();
    if (!book) {
      return '';
    }
    if (book.overdueDays === 0) {
      return fillTemplate(this.messages.onTime, { title: book.title });
    }
    return fillTemplate(book.overdueDays === 1 ? this.messages.lateOne : this.messages.lateMany, {
      title: book.title,
      days: String(book.overdueDays),
    });
  });

  protected readonly dueDate = computed(() => formatDate(this.returned()?.dueDate ?? ''));
  protected readonly fineAmount = computed(() => MONEY_FORMAT.format(this.returned()?.fineAmount ?? 0));
  protected readonly dailyRate = computed(() => MONEY_FORMAT.format(this.returned()?.dailyRate ?? 0));

  protected readonly reservedText = computed(() =>
    fillTemplate(this.messages.reservedText, { name: this.returned()?.reservedFor ?? '' }),
  );

  protected readonly traceText = computed(() => {
    const traceId = this.problem()?.traceId ?? '';
    return traceId === '' ? '' : fillTemplate(this.messages.traceId, { traceId });
  });

  constructor() {
    afterNextRender(() => this.focusBarcode());
  }

  protected async registerReturn(): Promise<void> {
    if (this.registering()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.registering.set(true);
    this.problem.set(null);
    this.returned.set(null);

    try {
      this.returned.set(await this.desk.returnBook(this.form.controls.barcode.value));
      this.form.reset();
    } catch (error) {
      this.problem.set(describeRequestProblem(error, this.messages.failed));
    } finally {
      this.registering.set(false);
      setTimeout(() => this.focusBarcode());
    }
  }

  private focusBarcode(): void {
    const input = this.barcodeInput()?.nativeElement;
    input?.focus();
    input?.select();
  }
}

function formatDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-').map(Number);

  if (!year || !month || !day) {
    return isoDate;
  }

  return DATE_FORMAT.format(new Date(year, month - 1, day));
}
