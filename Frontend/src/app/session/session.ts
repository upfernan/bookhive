import { HttpClient, HttpXsrfTokenExtractor } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import {
  initializeRecaptchaConfig,
  sendPasswordResetEmail,
  signInWithEmailAndPassword,
  signOut,
} from '@firebase/auth';
import { firstValueFrom } from 'rxjs';
import { CurrentSession } from './current-session';
import { IDENTITY_PROVIDER } from './identity-provider';
import { identityProviderCodeOf } from './session-errors';

const SESSION_PATH = '/api/v1/sesiones';
const CSRF_TOKEN_SOURCE_PATH = '/actuator/health';
const UNKNOWN_ACCOUNT = 'auth/user-not-found';

@Injectable({ providedIn: 'root' })
export class Session {
  private readonly http = inject(HttpClient);
  private readonly identityProvider = inject(IDENTITY_PROVIDER);
  private readonly csrfToken = inject(HttpXsrfTokenExtractor);
  private readonly currentSession = inject(CurrentSession);

  async preloadBotProtection(): Promise<void> {
    try {
      await initializeRecaptchaConfig(this.identityProvider);
    } catch {
      return;
    }
  }

  async open(email: string, password: string): Promise<void> {
    const credential = await signInWithEmailAndPassword(this.identityProvider, email, password);

    try {
      const idToken = await credential.user.getIdToken();
      await this.ensureCsrfToken();
      await firstValueFrom(this.http.post<void>(SESSION_PATH, { idToken }));
      this.currentSession.forget();
    } finally {
      await signOut(this.identityProvider);
    }
  }

  async close(): Promise<void> {
    await this.ensureCsrfToken();
    await firstValueFrom(this.http.delete<void>(SESSION_PATH));
    this.currentSession.forget();
  }

  async requestPasswordReset(email: string): Promise<void> {
    try {
      await sendPasswordResetEmail(this.identityProvider, email);
    } catch (error) {
      if (identityProviderCodeOf(error) !== UNKNOWN_ACCOUNT) {
        throw error;
      }
    }
  }

  private async ensureCsrfToken(): Promise<void> {
    if (this.csrfToken.getToken() === null) {
      await firstValueFrom(this.http.get(CSRF_TOKEN_SOURCE_PATH));
    }
  }
}
