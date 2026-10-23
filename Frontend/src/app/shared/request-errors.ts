import { HttpErrorResponse } from '@angular/common/http';
import { messages } from '../messages';

export interface RequestProblem {
  readonly message: string;
  readonly traceId: string;
}

export function describeRequestProblem(error: unknown, fallback: string): RequestProblem {
  if (!(error instanceof HttpErrorResponse)) {
    return { message: fallback, traceId: '' };
  }

  if (error.status === 0) {
    return { message: messages.session.networkUnavailable, traceId: '' };
  }

  const body: unknown = error.error;
  const message = textField(body, 'message');
  return { message: message === '' ? fallback : message, traceId: textField(body, 'traceId') };
}

function textField(body: unknown, field: string): string {
  if (typeof body === 'object' && body !== null && field in body) {
    const value = (body as Record<string, unknown>)[field];
    return typeof value === 'string' ? value.trim() : '';
  }

  return '';
}
