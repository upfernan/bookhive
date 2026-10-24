import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Signal,
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
import { BookCopy, Borrower, LoanDesk } from './loan-desk';

interface AttentionLoan {
  readonly loan: string;
  readonly title: string;
  readonly barcode: string;
  readonly dueDate: string;
}

interface Confirmation {
  readonly title: string;
  readonly name: string;
  readonly dueDate: string;
}

const DUE_DATE_FORMAT = new Intl.DateTimeFormat('es-CO', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  year: 'numeric',
});

@Component({
  selector: 'app-loans',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  templateUrl: './loans.html',
  styleUrls: ['../shell/section-page.css', '../shared/feedback.css', './loans.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Loans {
  private readonly desk = inject(LoanDesk);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly borrowerInput = viewChild<ElementRef<HTMLInputElement>>('borrowerInput');
  private readonly copyInput = viewChild<ElementRef<HTMLInputElement>>('copyInput');

  protected readonly messages = messages.loans;

  protected readonly borrowerForm = this.formBuilder.group({
    email: ['', [Validators.required, Validators.email]],
  });

  protected readonly copyForm = this.formBuilder.group({
    barcode: [{ value: '', disabled: true }, Validators.required],
  });

  protected readonly borrower = signal<Borrower | null>(null);
  protected readonly bookCopy = signal<BookCopy | null>(null);
  protected readonly searchingBorrower = signal(false);
  protected readonly searchingCopy = signal(false);
  protected readonly registering = signal(false);
  protected readonly borrowerProblem = signal('');
  protected readonly copyProblem = signal('');
  protected readonly actionHint = signal('');
  protected readonly registerProblem = signal<RequestProblem | null>(null);
  protected readonly confirmation = signal<Confirmation | null>(null);
  protected readonly attention = signal<readonly AttentionLoan[]>([]);

  protected readonly initials = computed(() => initialsOf(this.borrower()?.name ?? ''));

  protected readonly loansSummary = computed(() => {
    const borrower = this.borrower();
    return borrower
      ? fillTemplate(this.messages.activeLoansValue, {
          active: String(borrower.activeLoans),
          max: String(borrower.maxLoans),
        })
      : '';
  });

  protected readonly confirmationText = computed(() => {
    const confirmation = this.confirmation();
    return confirmation
      ? fillTemplate(this.messages.registeredText, {
          title: confirmation.title,
          name: confirmation.name,
          date: confirmation.dueDate,
        })
      : '';
  });

  protected readonly traceText = computed(() => {
    const traceId = this.registerProblem()?.traceId ?? '';
    return traceId === '' ? '' : fillTemplate(this.messages.traceId, { traceId });
  });

  constructor() {
    afterNextRender(() => this.borrowerInput()?.nativeElement.focus());
  }

  protected async searchBorrower(): Promise<void> {
    if (this.searchingBorrower()) {
      return;
    }

    if (this.borrowerForm.invalid) {
      this.borrowerForm.markAllAsTouched();
      return;
    }

    this.searchingBorrower.set(true);
    this.borrowerProblem.set('');
    this.startAttention();

    try {
      this.borrower.set(await this.desk.findBorrower(this.borrowerForm.controls.email.value));
      this.copyForm.enable();
      this.focus(this.copyInput);
    } catch (error) {
      this.borrowerProblem.set(describeRequestProblem(error, this.messages.borrowerNotFound).message);
    } finally {
      this.searchingBorrower.set(false);
    }
  }

  protected async searchBookCopy(): Promise<void> {
    if (this.searchingCopy()) {
      return;
    }

    if (this.copyForm.invalid) {
      this.copyForm.markAllAsTouched();
      return;
    }

    this.searchingCopy.set(true);
    this.copyProblem.set('');
    this.clearOutcome();
    this.bookCopy.set(null);

    try {
      this.bookCopy.set(await this.desk.findBookCopy(this.copyForm.controls.barcode.value));
    } catch (error) {
      this.copyProblem.set(describeRequestProblem(error, this.messages.copyNotFound).message);
    } finally {
      this.searchingCopy.set(false);
    }
  }

  protected async register(): Promise<void> {
    if (this.registering()) {
      return;
    }

    const borrower = this.borrower();
    const bookCopy = this.bookCopy();
    this.clearOutcome();

    if (!borrower) {
      this.actionHint.set(this.messages.missingBorrower);
      this.focus(this.borrowerInput);
      return;
    }

    if (!bookCopy) {
      this.actionHint.set(this.messages.missingCopy);
      this.focus(this.copyInput);
      return;
    }

    if (borrower.blocks.length > 0 || bookCopy.blocks.length > 0) {
      this.actionHint.set(this.messages.blocked);
      return;
    }

    this.registering.set(true);

    try {
      const registered = await this.desk.lend(borrower, bookCopy);
      const dueDate = formatDueDate(registered.dueDate);
      this.confirmation.set({ title: bookCopy.title, name: borrower.name, dueDate });
      this.attention.update((loans) => [
        { loan: registered.loan, title: bookCopy.title, barcode: bookCopy.barcode, dueDate },
        ...loans,
      ]);
      void this.refreshBorrower(borrower.email);
    } catch (error) {
      this.registerProblem.set(describeRequestProblem(error, this.messages.registerFailed));
    } finally {
      this.registering.set(false);
    }
  }

  protected lendAnother(): void {
    this.clearOutcome();
    this.bookCopy.set(null);
    this.copyProblem.set('');
    this.copyForm.reset();
    this.focus(this.copyInput);
  }

  protected endAttention(): void {
    this.startAttention();
    this.borrower.set(null);
    this.borrowerProblem.set('');
    this.borrowerForm.reset();
    this.focus(this.borrowerInput);
  }

  private startAttention(): void {
    this.clearOutcome();
    this.attention.set([]);
    this.bookCopy.set(null);
    this.copyProblem.set('');
    this.copyForm.reset();
    this.copyForm.disable();
  }

  private clearOutcome(): void {
    this.actionHint.set('');
    this.registerProblem.set(null);
    this.confirmation.set(null);
  }

  private async refreshBorrower(email: string): Promise<void> {
    try {
      this.borrower.set(await this.desk.findBorrower(email));
    } catch {
      return;
    }
  }

  private focus(input: Signal<ElementRef<HTMLInputElement> | undefined>): void {
    setTimeout(() => input()?.nativeElement.focus());
  }
}

function initialsOf(name: string): string {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word.charAt(0).toLocaleUpperCase('es'))
    .join('');
}

function formatDueDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-').map(Number);

  if (!year || !month || !day) {
    return isoDate;
  }

  return DUE_DATE_FORMAT.format(new Date(year, month - 1, day));
}
