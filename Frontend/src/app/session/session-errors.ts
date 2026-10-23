import { HttpErrorResponse } from '@angular/common/http';
import { messages } from '../messages';

const INVALID_CREDENTIALS = new Set([
  'auth/invalid-credential',
  'auth/invalid-login-credentials',
  'auth/wrong-password',
  'auth/user-not-found',
  'auth/invalid-email',
  'auth/user-disabled',
]);

export function describeSessionError(error: unknown, fallback: string): string {
  if (error instanceof HttpErrorResponse) {
    return describeBackendError(error, fallback);
  }

  const code = identityProviderCodeOf(error);

  if (INVALID_CREDENTIALS.has(code)) {
    return messages.session.invalidCredentials;
  }

  return describeIdentityProviderError(code, fallback);
}

export function describePasswordResetError(error: unknown): string {
  const code = identityProviderCodeOf(error);

  if (code === 'auth/invalid-email') {
    return messages.session.emailInvalid;
  }

  return describeIdentityProviderError(code, messages.passwordReset.failed);
}

export function identityProviderCodeOf(error: unknown): string {
  if (typeof error === 'object' && error !== null && 'code' in error) {
    return typeof error.code === 'string' ? error.code : '';
  }

  return '';
}

function describeBackendError(error: HttpErrorResponse, fallback: string): string {
  if (error.status === 0) {
    return messages.session.networkUnavailable;
  }

  const backendMessage = backendMessageOf(error.error);
  return backendMessage === '' ? fallback : backendMessage;
}

function describeIdentityProviderError(code: string, fallback: string): string {
  if (code === 'auth/too-many-requests') {
    return messages.session.tooManyAttempts;
  }

  if (code === 'auth/network-request-failed') {
    return messages.session.networkUnavailable;
  }

  if (code.includes('captcha')) {
    return messages.session.botCheckFailed;
  }

  return fallback;
}

function backendMessageOf(body: unknown): string {
  if (typeof body === 'object' && body !== null && 'message' in body) {
    return typeof body.message === 'string' ? body.message.trim() : '';
  }

  return '';
}
