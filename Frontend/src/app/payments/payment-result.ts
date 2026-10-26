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
import { MatButtonModule } from '@angular/material/button';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { messages } from '../messages';
import { fillTemplate } from '../shared/fill-template';
import { RequestProblem, describeRequestProblem } from '../shared/request-errors';
import { FinePaymentResult, PaymentDesk, PaymentStatus } from './payment-desk';

const MONEY_FORMAT = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
});

type ResultTone = 'success' | 'pending' | 'failed';

interface ResultView {
  readonly tone: ResultTone;
  readonly title: string;
  readonly text: string;
  readonly canCheckAgain: boolean;
}

@Component({
  selector: 'app-payment-result',
  imports: [RouterLink, MatButtonModule],
  templateUrl: './payment-result.html',
  styleUrls: ['../shell/section-page.css', '../shared/feedback.css', './payment-result.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentResult {
  private readonly desk = inject(PaymentDesk);
  private readonly route = inject(ActivatedRoute);
  private readonly heading = viewChild<ElementRef<HTMLElement>>('heading');

  protected readonly messages = messages.paymentResult;
  protected readonly reference = (this.route.snapshot.paramMap.get('referencia') ?? '').trim();
  protected readonly transaction = (this.route.snapshot.queryParamMap.get('id') ?? '').trim();

  protected readonly result = signal<FinePaymentResult | null>(null);
  protected readonly checking = signal(false);
  protected readonly problem = signal<RequestProblem | null>(null);

  protected readonly view = computed<ResultView | null>(() => {
    if (this.reference === '' || this.transaction === '') {
      return this.viewOf('pending', this.messages.missingTitle, this.messages.missingText, false);
    }
    const current = this.result();
    return current ? this.statusView(current) : null;
  });

  protected readonly amount = computed(() => MONEY_FORMAT.format((this.result()?.amountInCents ?? 0) / 100));

  protected readonly traceText = computed(() => {
    const traceId = this.problem()?.traceId ?? '';
    return traceId === '' ? '' : fillTemplate(this.messages.traceId, { traceId });
  });

  constructor() {
    if (this.reference !== '' && this.transaction !== '') {
      void this.check();
    }
    afterNextRender(() => this.heading()?.nativeElement.focus());
  }

  protected async check(): Promise<void> {
    if (this.checking()) {
      return;
    }

    this.checking.set(true);
    this.problem.set(null);

    try {
      this.result.set(await this.desk.confirm(this.reference, this.transaction));
    } catch (error) {
      this.problem.set(describeRequestProblem(error, this.messages.checkFailed));
    } finally {
      this.checking.set(false);
    }
  }

  private statusView(current: FinePaymentResult): ResultView {
    switch (current.status.trim().toUpperCase()) {
      case PaymentStatus.approved:
        return this.viewOf(
          'success',
          this.messages.approvedTitle,
          current.finePaid ? this.messages.approvedText : this.messages.approvedRecordingText,
          false,
        );
      case PaymentStatus.pending:
        return this.viewOf('pending', this.messages.pendingTitle, this.messages.pendingText, true);
      case PaymentStatus.declined:
        return this.viewOf('failed', this.messages.declinedTitle, this.messages.declinedText, false);
      case PaymentStatus.voided:
        return this.viewOf('failed', this.messages.voidedTitle, this.messages.voidedText, false);
      default:
        return this.viewOf('failed', this.messages.errorTitle, this.messages.errorText, false);
    }
  }

  private viewOf(tone: ResultTone, title: string, text: string, canCheckAgain: boolean): ResultView {
    return { tone, title, text, canCheckAgain };
  }
}
