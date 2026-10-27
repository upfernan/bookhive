import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { messages } from '../messages';
import { fillTemplate } from '../shared/fill-template';
import { RequestProblem, describeRequestProblem } from '../shared/request-errors';
import { MyReservation, ReservationDesk, ReservationStatus } from './reservation-desk';

const DATE_FORMAT = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'long', year: 'numeric' });
const DEFAULT_DATE = '1900-01-01';

interface ReservationCard {
  readonly id: string;
  readonly title: string;
  readonly authors: string;
  readonly status: string;
  readonly statusClass: string;
  readonly note: string;
  readonly cancellable: boolean;
  readonly confirmText: string;
}

@Component({
  selector: 'app-reservations',
  imports: [RouterLink, MatButtonModule],
  templateUrl: './reservations.html',
  styleUrls: ['../shell/section-page.css', '../shared/feedback.css', './reservations.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Reservations {
  private readonly desk = inject(ReservationDesk);

  protected readonly messages = messages.reservations;
  protected readonly reservations = signal<readonly MyReservation[] | null>(null);
  protected readonly loading = signal(false);
  protected readonly problem = signal<RequestProblem | null>(null);
  protected readonly cancelling = signal('');
  protected readonly confirming = signal('');
  protected readonly notice = signal('');
  protected readonly cancelError = signal('');

  protected readonly cards = computed(() => (this.reservations() ?? []).map((item) => this.cardOf(item)));

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
      this.reservations.set(await this.desk.listMine());
    } catch (error) {
      this.problem.set(describeRequestProblem(error, this.messages.loadFailed));
    } finally {
      this.loading.set(false);
    }
  }

  protected ask(card: ReservationCard): void {
    if (this.cancelling() !== '') {
      return;
    }

    this.notice.set('');
    this.cancelError.set('');
    this.confirming.set(card.id);
  }

  protected keep(): void {
    this.confirming.set('');
  }

  protected async cancel(card: ReservationCard): Promise<void> {
    if (this.cancelling() !== '' || this.confirming() !== card.id) {
      return;
    }

    this.confirming.set('');
    this.cancelling.set(card.id);
    this.notice.set('');
    this.cancelError.set('');

    try {
      await this.desk.cancel(card.id);
      this.notice.set(fillTemplate(this.messages.cancelled, { title: card.title }));
      this.reservations.update((current) => (current ?? []).filter((item) => item.reservation !== card.id));
    } catch (error) {
      this.cancelError.set(describeRequestProblem(error, this.messages.cancelFailed).message);
    } finally {
      this.cancelling.set('');
    }
  }

  private cardOf(item: MyReservation): ReservationCard {
    const waiting = item.status === ReservationStatus.waiting;
    const assigned = item.status === ReservationStatus.assigned;
    const requested = dateOf(item.requestedAt);
    const deadline = dateOf(item.pickupDeadline);

    return {
      id: item.reservation,
      title: item.title === '' ? this.messages.untitled : item.title,
      authors: item.authors,
      status: waiting
        ? fillTemplate(this.messages.waiting, { position: String(item.position) })
        : this.messages.assigned,
      statusClass: `chip chip--${assigned ? 'success' : 'neutral'}`,
      note:
        assigned && deadline !== ''
          ? fillTemplate(this.messages.pickupUntil, { date: deadline })
          : requested === ''
            ? ''
            : fillTemplate(this.messages.requestedOn, { date: requested }),
      cancellable: waiting || assigned,
      confirmText: fillTemplate(waiting ? this.messages.confirmWaiting : this.messages.confirmAssigned, {
        title: item.title === '' ? this.messages.untitled : item.title,
        position: String(item.position),
      }),
    };
  }
}

function dateOf(value: string): string {
  if (!value || value.startsWith(DEFAULT_DATE)) {
    return '';
  }
  const date = new Date(value.length === 10 ? `${value}T12:00:00` : value);
  return Number.isNaN(date.getTime()) ? '' : DATE_FORMAT.format(date);
}
