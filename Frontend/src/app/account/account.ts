import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { messages } from '../messages';
import { CurrentSession } from '../session/current-session';
import { PermissionCode } from '../session/permission-code';
import { fillTemplate } from '../shared/fill-template';
import { RequestProblem, describeRequestProblem } from '../shared/request-errors';
import {
  AccountBook,
  AccountDesk,
  ActiveLoan,
  FinePayment,
  MyAccount,
  PendingFine,
  ReturnedLoan,
} from './account-desk';

const DATE_FORMAT = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'long', year: 'numeric' });
const MONEY_FORMAT = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});
const DEFAULT_DATE = '1900-01-01';

interface AccountRow {
  readonly title: string;
  readonly authors: string;
  readonly detail: string;
  readonly note: string;
  readonly badge: string;
  readonly badgeClass: string;
}

interface AccountGroup {
  readonly key: string;
  readonly title: string;
  readonly empty: string;
  readonly rows: readonly AccountRow[];
}

@Component({
  selector: 'app-account',
  imports: [RouterLink, MatButtonModule],
  templateUrl: './account.html',
  styleUrls: ['../shell/section-page.css', '../shared/feedback.css', './account.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Account {
  private readonly desk = inject(AccountDesk);
  private readonly currentSession = inject(CurrentSession);

  protected readonly messages = messages.account;
  protected readonly account = signal<MyAccount | null>(null);
  protected readonly loading = signal(false);
  protected readonly problem = signal<RequestProblem | null>(null);

  protected readonly canReserve = computed(() => this.currentSession.allows(PermissionCode.reserveBook));

  protected readonly finesTotal = computed(() => money(this.account()?.pendingFinesTotal));

  protected readonly overdueText = computed(() => {
    const accruing = Number(this.account()?.accruingFinesTotal ?? 0);
    return accruing > 0 ? fillTemplate(this.messages.overdueText, { amount: money(accruing) }) : '';
  });

  protected readonly groups = computed<readonly AccountGroup[]>(() => {
    const current = this.account();
    if (!current) {
      return [];
    }

    return [
      {
        key: 'loans',
        title: this.messages.loansTitle,
        empty: this.messages.loansEmpty,
        rows: current.activeLoans.map((item) => this.loanRow(item)),
      },
      {
        key: 'fines',
        title: this.messages.finesTitle,
        empty: this.messages.finesEmpty,
        rows: current.pendingFines.map((item) => this.fineRow(item)),
      },
      {
        key: 'history',
        title: this.messages.historyTitle,
        empty: this.messages.historyEmpty,
        rows: current.loanHistory.map((item) => this.returnRow(item)),
      },
      {
        key: 'payments',
        title: this.messages.paymentsTitle,
        empty: this.messages.paymentsEmpty,
        rows: current.payments.map((item) => this.paymentRow(item)),
      },
    ];
  });

  protected readonly traceText = computed(() => {
    const traceId = this.problem()?.traceId ?? '';
    return traceId === '' ? '' : fillTemplate(this.messages.traceId, { traceId });
  });

  constructor() {
    void this.load();
  }

  protected async load(): Promise<void> {
    this.loading.set(true);
    this.problem.set(null);

    try {
      this.account.set(await this.desk.viewMine());
    } catch (error) {
      this.problem.set(describeRequestProblem(error, this.messages.loadFailed));
    } finally {
      this.loading.set(false);
    }
  }

  private loanRow(item: ActiveLoan): AccountRow {
    const overdue = item.overdueDays > 0;

    return {
      ...this.bookOf(item.book),
      detail: fillTemplate(this.messages.loanDates, {
        loaned: formatDate(item.loanDate),
        due: formatDate(item.dueDate),
      }),
      note: overdue ? fillTemplate(this.messages.accruedFine, { amount: money(item.accruedFine) }) : '',
      badge: overdue ? this.daysText(item.overdueDays, this.messages.overdueOne, this.messages.overdueMany) : this.messages.onTime,
      badgeClass: overdue ? 'chip chip--error' : 'chip chip--success',
    };
  }

  private fineRow(item: PendingFine): AccountRow {
    return {
      ...this.bookOf(item.book),
      detail: fillTemplate(item.overdueDays === 1 ? this.messages.fineDetailOne : this.messages.fineDetailMany, {
        days: String(item.overdueDays),
        rate: money(item.dailyRate),
      }),
      note: dated(this.messages.fineIssued, item.issueDate),
      badge: money(item.amount),
      badgeClass: 'amount amount--due',
    };
  }

  private returnRow(item: ReturnedLoan): AccountRow {
    const late = item.overdueDays > 0;

    return {
      ...this.bookOf(item.book),
      detail: fillTemplate(this.messages.historyDates, {
        loaned: formatDate(item.loanDate),
        returned: formatDate(item.returnDate),
      }),
      note: '',
      badge: late
        ? this.daysText(item.overdueDays, this.messages.returnedLateOne, this.messages.returnedLateMany)
        : this.messages.returnedOnTime,
      badgeClass: late ? 'chip chip--neutral' : 'chip chip--success',
    };
  }

  private paymentRow(item: FinePayment): AccountRow {
    const reference = item.reference.trim();

    return {
      ...this.bookOf(item.book),
      detail: dated(this.messages.paidOn, item.paidAt),
      note: reference === '' ? '' : fillTemplate(this.messages.reference, { reference }),
      badge: money(item.amount),
      badgeClass: 'amount',
    };
  }

  private bookOf(book: AccountBook): Pick<AccountRow, 'title' | 'authors'> {
    const title = book?.title?.trim() ?? '';
    return { title: title === '' ? this.messages.untitled : title, authors: book?.authors?.trim() ?? '' };
  }

  private daysText(days: number, one: string, many: string): string {
    return days === 1 ? one : fillTemplate(many, { days: String(days) });
  }
}

function money(value: number | undefined): string {
  return MONEY_FORMAT.format(Number(value ?? 0) || 0);
}

function dated(template: string, value: string): string {
  const date = formatDate(value);
  return date === '' ? '' : fillTemplate(template, { date });
}

function formatDate(value: string): string {
  if (!value || value.startsWith(DEFAULT_DATE)) {
    return '';
  }
  const date = new Date(value.length === 10 ? `${value}T12:00:00` : value);
  return Number.isNaN(date.getTime()) ? '' : DATE_FORMAT.format(date);
}
