import { ChangeDetectionStrategy, Component, afterNextRender, computed, inject, signal, viewChild } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { messages } from '../messages';
import { fillTemplate } from '../shared/fill-template';
import { FoundReader } from '../shared/reader-directory';
import { ReaderPicker } from '../shared/reader-picker';
import { RequestProblem, describeRequestProblem } from '../shared/request-errors';
import { BookCopy, Borrower, LoanDesk } from './loan-desk';

interface ReadyCard {
  readonly reservation: string;
  readonly bookCopy: string;
  readonly barcode: string;
  readonly title: string;
  readonly authors: string;
  readonly details: string;
  readonly blocks: readonly string[];
  readonly registerLabel: string;
}

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
  imports: [MatButtonModule, ReaderPicker],
  templateUrl: './loans.html',
  styleUrls: ['../shell/section-page.css', '../shared/feedback.css', './loans.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Loans {
  private readonly desk = inject(LoanDesk);
  private readonly picker = viewChild.required(ReaderPicker);
  private readonly readyCopies = signal<ReadonlyMap<string, BookCopy>>(new Map());

  protected readonly messages = messages.loans;

  protected readonly borrower = signal<Borrower | null>(null);
  protected readonly loadingBorrower = signal(false);
  protected readonly copiesLoaded = signal(false);
  protected readonly lending = signal('');
  protected readonly borrowerProblem = signal('');
  protected readonly registerProblem = signal<RequestProblem | null>(null);
  protected readonly confirmation = signal<Confirmation | null>(null);
  protected readonly attention = signal<readonly AttentionLoan[]>([]);

  protected readonly initials = computed(() => initialsOf(this.borrower()?.name ?? ''));

  protected readonly blocked = computed(() => (this.borrower()?.blocks.length ?? 0) > 0);

  protected readonly readyCards = computed<readonly ReadyCard[]>(() => {
    const copies = this.readyCopies();
    const loaded = this.copiesLoaded();
    return (this.borrower()?.readyReservations ?? []).map((ready) => {
      const copy = copies.get(ready.bookCopy) ?? null;
      const title = copy?.title || (loaded ? this.messages.readyUntitled : this.messages.readyLoading);
      return {
        reservation: ready.reservation,
        bookCopy: ready.bookCopy,
        barcode: ready.barcode,
        title,
        authors: copy?.authors ?? '',
        details: [
          ready.barcode ? `${this.messages.barcode}: ${ready.barcode}` : '',
          copy?.branch ? `${this.messages.branch}: ${copy.branch}` : '',
          fillTemplate(this.messages.readyPickup, { date: formatDueDate(ready.pickupDeadline) }),
        ]
          .filter((part) => part !== '')
          .join(' · '),
        blocks: copy?.blocks ?? [],
        registerLabel: fillTemplate(this.messages.registerLabel, { title }),
      };
    });
  });

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
    afterNextRender(() => this.picker().focus());
  }

  protected async pick(reader: FoundReader): Promise<void> {
    this.startAttention();
    this.borrower.set(null);
    this.borrowerProblem.set('');
    this.loadingBorrower.set(true);

    try {
      const borrower = await this.desk.findBorrower(reader.email);
      this.borrower.set(borrower);
      void this.loadReadyCopies(borrower);
    } catch (error) {
      this.borrowerProblem.set(describeRequestProblem(error, this.messages.borrowerNotFound).message);
    } finally {
      this.loadingBorrower.set(false);
    }
  }

  protected canRegister(card: ReadyCard): boolean {
    return !this.blocked() && this.copiesLoaded() && card.blocks.length === 0 && this.lending() === '';
  }

  protected async register(card: ReadyCard): Promise<void> {
    const borrower = this.borrower();

    if (!borrower || !this.canRegister(card)) {
      return;
    }

    this.clearOutcome();
    this.lending.set(card.bookCopy);

    try {
      const registered = await this.desk.lend(borrower.id, card.bookCopy);
      const dueDate = formatDueDate(registered.dueDate);
      this.confirmation.set({ title: card.title, name: borrower.name, dueDate });
      this.attention.update((loans) => [
        { loan: registered.loan, title: card.title, barcode: card.barcode, dueDate },
        ...loans,
      ]);
      await this.refreshBorrower(borrower.email);
    } catch (error) {
      this.registerProblem.set(describeRequestProblem(error, this.messages.registerFailed));
    } finally {
      this.lending.set('');
    }
  }

  protected endAttention(): void {
    this.startAttention();
    this.borrower.set(null);
    this.borrowerProblem.set('');
    this.picker().clear();
  }

  private startAttention(): void {
    this.clearOutcome();
    this.attention.set([]);
    this.readyCopies.set(new Map());
    this.copiesLoaded.set(false);
  }

  private clearOutcome(): void {
    this.registerProblem.set(null);
    this.confirmation.set(null);
  }

  private async refreshBorrower(email: string): Promise<void> {
    try {
      const borrower = await this.desk.findBorrower(email);
      this.borrower.set(borrower);
      await this.loadReadyCopies(borrower);
    } catch {
      return;
    }
  }

  private async loadReadyCopies(borrower: Borrower): Promise<void> {
    const found = await Promise.allSettled(
      (borrower.readyReservations ?? [])
        .filter((ready) => ready.barcode !== '')
        .map((ready) => this.desk.findBookCopy(ready.barcode, borrower.id)),
    );

    if (this.borrower()?.id !== borrower.id) {
      return;
    }

    this.readyCopies.set(
      new Map(
        found
          .filter((result): result is PromiseFulfilledResult<BookCopy> => result.status === 'fulfilled')
          .map((result) => [result.value.id, result.value]),
      ),
    );
    this.copiesLoaded.set(true);
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
