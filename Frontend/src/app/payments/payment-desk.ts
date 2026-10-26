import { HttpClient, HttpXsrfTokenExtractor } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

const CSRF_TOKEN_SOURCE_PATH = '/actuator/health';
const FINE_PAYMENTS_PATH = '/api/v1/pagos/multas';
const PAYMENT_RESULT_PATH = '/pagos/resultado';
const ALLOWED_PROTOCOLS = new Set(['https:', 'http:']);

export const PaymentStatus = {
  pending: 'PENDING',
  approved: 'APPROVED',
  declined: 'DECLINED',
  voided: 'VOIDED',
} as const;

export interface FineCheckout {
  readonly checkoutUrl: string;
  readonly publicKey: string;
  readonly currency: string;
  readonly amountInCents: number;
  readonly reference: string;
  readonly signature: string;
  readonly expirationTime: string;
  readonly redirectUrl: string;
}

export interface FinePaymentResult {
  readonly reference: string;
  readonly status: string;
  readonly amountInCents: number;
  readonly finePaid: boolean;
}

@Injectable({ providedIn: 'root' })
export class PaymentDesk {
  private readonly http = inject(HttpClient);
  private readonly csrfToken = inject(HttpXsrfTokenExtractor);

  async start(fine: string): Promise<FineCheckout> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.post<FineCheckout>(FINE_PAYMENTS_PATH, { fine }));
  }

  async confirm(reference: string, transaction: string): Promise<FinePaymentResult> {
    await this.ensureCsrfToken();
    return firstValueFrom(
      this.http.post<FinePaymentResult>(`${FINE_PAYMENTS_PATH}/${encodeURIComponent(reference)}/confirmacion`, {
        transaction,
      }),
    );
  }

  private async ensureCsrfToken(): Promise<void> {
    if (this.csrfToken.getToken() === null) {
      await firstValueFrom(this.http.get(CSRF_TOKEN_SOURCE_PATH));
    }
  }
}

export function checkoutAddress(checkout: FineCheckout, origin: string): string {
  const address = new URL(checkout.checkoutUrl);
  if (!ALLOWED_PROTOCOLS.has(address.protocol) || checkout.reference.trim() === '') {
    throw new TypeError('La pasarela devolvió datos de pago incompletos.');
  }

  const parameters = address.searchParams;
  parameters.set('public-key', checkout.publicKey);
  parameters.set('currency', checkout.currency);
  parameters.set('amount-in-cents', String(checkout.amountInCents));
  parameters.set('reference', checkout.reference);
  parameters.set('signature:integrity', checkout.signature);
  if (checkout.expirationTime.trim() !== '') {
    parameters.set('expiration-time', checkout.expirationTime);
  }
  parameters.set('redirect-url', resultAddress(checkout, origin));

  return address.toString();
}

function resultAddress(checkout: FineCheckout, origin: string): string {
  const configured = checkout.redirectUrl.trim();
  const base = configured === '' ? `${origin}${PAYMENT_RESULT_PATH}` : configured;
  return `${base.replace(/\/+$/, '')}/${encodeURIComponent(checkout.reference)}`;
}
