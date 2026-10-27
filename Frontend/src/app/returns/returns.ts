import { ChangeDetectionStrategy, Component, afterNextRender, computed, inject, signal, viewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { BookCopy, Borrower, LoanDesk } from '../loans/loan-desk';
import { messages } from '../messages';
import { fillTemplate } from '../shared/fill-template';
import { FoundReader } from '../shared/reader-directory';
import { ReaderPicker } from '../shared/reader-picker';
import { RequestProblem, describeRequestProblem } from '../shared/request-errors';
import { RegisteredReturn, ReturnDesk } from './return-desk';

interface LoanCard {
  readonly loan: string;
  readonly bookCopy: string;
  readonly barcode: string;
  readonly title: string;
  readonly authors: string;
  readonly details: string;
  readonly overdue: string;
  readonly fineToday: string;
  readonly registerLabel: string;
}

interface ReturnedBook extends RegisteredReturn {
  readonly title: string;
  readonly barcode: string;
}

const DATE_FORMAT = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'long', year: 'numeric' });
const MONEY_FORMAT = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

@Component({
  selector: 'app-returns',
  imports: [MatButtonModule, ReaderPicker],
  templateUrl: './returns.html',
  styleUrls: ['../shell/section-page.css', '../shared/feedback.css', './returns.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Returns {
  private readonly loanDesk = inject(LoanDesk);
  private readonly returnDesk = inject(ReturnDesk);
  private readonly picker = viewChild.required(ReaderPicker);
  private readonly copies = signal<ReadonlyMap<string, BookCopy>>(new Map());

  protected readonly messages = messages.returns;

  protected readonly reader = signal<Borrower | null>(null);
  protected readonly loadingReader = signal(false);
  protected readonly copiesLoaded = signal(false);
  protected readonly returning = signal('');
  protected readonly readerProblem = signal('');
  protected readonly returned = signal<ReturnedBook | null>(null);
  protected readonly problem = signal<RequestProblem | null>(null);

  protected readonly loanCards = computed<readonly LoanCard[]>(() => {
    const copies = this.copies();
    const loaded = this.copiesLoaded();
    return (this.reader()?.openLoans ?? []).map((open) => {
      const copy = copies.get(open.bookCopy) ?? null;
      const title = copy?.title || (loaded ? this.messages.untitled : this.messages.loadingTitle);
      const overdue = open.overdueDays > 0;
      return {
        loan: open.loan,
        bookCopy: open.bookCopy,
        barcode: open.barcode,
        title,
        authors: copy?.authors ?? '',
        details: [
          open.barcode ? `${this.messages.barcode}: ${open.barcode}` : '',
          fillTemplate(overdue ? this.messages.wasDue : this.messages.isDue, { date: formatDate(open.dueDate) }),
        ]
          .filter((part) => part !== '')
          .join(' · '),
        overdue: overdue
          ? fillTemplate(open.overdueDays === 1 ? this.messages.overdueOne : this.messages.overdueMany, {
              days: String(open.overdueDays),
            })
          : '',
        fineToday:
          overdue && open.accruedFine > 0
            ? fillTemplate(this.messages.fineToday, { amount: MONEY_FORMAT.format(open.accruedFine) })
            : '',
        registerLabel: fillTemplate(this.messages.registerLabel, { title }),
      };
    });
  });

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
    afterNextRender(() => this.picker().focus());
  }

  protected async pick(reader: FoundReader): Promise<void> {
    this.clearOutcome();
    this.reader.set(null);
    this.readerProblem.set('');
    this.loadingReader.set(true);

    try {
      await this.loadReader(reader.email);
    } catch (error) {
      this.readerProblem.set(describeRequestProblem(error, this.messages.readerNotFound).message);
    } finally {
      this.loadingReader.set(false);
    }
  }

  protected async registerReturn(card: LoanCard): Promise<void> {
    const reader = this.reader();

    if (!reader || this.returning() !== '') {
      return;
    }

    this.clearOutcome();
    this.returning.set(card.bookCopy);

    try {
      const registered = await this.returnDesk.returnBook(card.bookCopy);
      this.returned.set({ ...registered, title: card.title, barcode: card.barcode });
      await this.loadReader(reader.email).catch(() => undefined);
    } catch (error) {
      this.problem.set(describeRequestProblem(error, this.messages.failed));
    } finally {
      this.returning.set('');
    }
  }

  protected endAttention(): void {
    this.clearOutcome();
    this.reader.set(null);
    this.readerProblem.set('');
    this.copies.set(new Map());
    this.copiesLoaded.set(false);
    this.picker().clear();
  }

  private clearOutcome(): void {
    this.problem.set(null);
    this.returned.set(null);
  }

  private async loadReader(email: string): Promise<void> {
    const reader = await this.loanDesk.findBorrower(email);
    this.reader.set(reader);
    this.copiesLoaded.set(false);

    const found = await Promise.allSettled(
      (reader.openLoans ?? [])
        .filter((open) => open.barcode !== '')
        .map((open) => this.loanDesk.findBookCopy(open.barcode)),
    );

    if (this.reader()?.id !== reader.id) {
      return;
    }

    this.copies.set(
      new Map(
        found
          .filter((result): result is PromiseFulfilledResult<BookCopy> => result.status === 'fulfilled')
          .map((result) => [result.value.id, result.value]),
      ),
    );
    this.copiesLoaded.set(true);
  }
}

function formatDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-').map(Number);

  if (!year || !month || !day) {
    return isoDate;
  }

  return DATE_FORMAT.format(new Date(year, month - 1, day));
}
