import { HttpClient, HttpErrorResponse, HttpXsrfTokenExtractor } from '@angular/common/http';
import { DOCUMENT, Injectable, inject, signal } from '@angular/core';
import {
  ActionCodeSettings,
  User,
  createUserWithEmailAndPassword,
  initializeRecaptchaConfig,
  sendEmailVerification,
  sendPasswordResetEmail,
  signInWithEmailAndPassword,
  signOut,
  updateProfile,
} from '@firebase/auth';
import { firstValueFrom } from 'rxjs';
import { CurrentSession } from './current-session';
import { DataPolicy } from './data-policy';
import { IDENTITY_PROVIDER } from './identity-provider';
import { identityProviderCodeOf } from './session-errors';

const SESSION_PATH = '/api/v1/sesiones';
const CSRF_TOKEN_SOURCE_PATH = '/actuator/health';
const LOGIN_PATH = '/ingresar';
const UNKNOWN_ACCOUNT = 'auth/user-not-found';
const EMAIL_IN_USE = 'auth/email-already-in-use';
const NOT_REGISTERED = 409;

export type SessionOutcome = 'opened' | 'unverified' | 'unregistered';

export interface PendingAccount {
  readonly email: string;
  readonly name: string;
  idToken(): Promise<string>;
}

@Injectable({ providedIn: 'root' })
export class Session {
  private readonly http = inject(HttpClient);
  private readonly identityProvider = inject(IDENTITY_PROVIDER);
  private readonly csrfToken = inject(HttpXsrfTokenExtractor);
  private readonly currentSession = inject(CurrentSession);
  private readonly dataPolicy = inject(DataPolicy);
  private readonly document = inject(DOCUMENT);

  readonly pendingAccount = signal<PendingAccount | null>(null);

  async preloadBotProtection(): Promise<void> {
    try {
      await initializeRecaptchaConfig(this.identityProvider);
    } catch {
      return;
    }
  }

  async open(email: string, password: string): Promise<SessionOutcome> {
    this.pendingAccount.set(null);
    const credential = await signInWithEmailAndPassword(this.identityProvider, email, password);
    const user = credential.user;
    let keepSignedIn = false;

    try {
      if (!user.emailVerified) {
        keepSignedIn = true;
        return 'unverified';
      }

      await this.ensureCsrfToken();

      try {
        await firstValueFrom(this.http.post<void>(SESSION_PATH, { idToken: await user.getIdToken() }));
      } catch (error) {
        if (error instanceof HttpErrorResponse && error.status === NOT_REGISTERED) {
          keepSignedIn = true;
          this.pendingAccount.set(pendingAccountOf(user));
          return 'unregistered';
        }
        throw error;
      }

      this.forgetSessionState();
      return 'opened';
    } finally {
      if (!keepSignedIn) {
        await signOut(this.identityProvider);
      }
    }
  }

  async signUp(name: string, email: string, password: string): Promise<void> {
    try {
      const credential = await createUserWithEmailAndPassword(this.identityProvider, email, password);
      await updateProfile(credential.user, { displayName: name });
      await sendEmailVerification(credential.user, this.verificationSettings());
    } catch (error) {
      if (identityProviderCodeOf(error) !== EMAIL_IN_USE) {
        throw error;
      }
    } finally {
      await signOut(this.identityProvider);
    }
  }

  async resendVerification(): Promise<void> {
    const user = this.identityProvider.currentUser;

    if (!user) {
      return;
    }

    try {
      await sendEmailVerification(user, this.verificationSettings());
    } finally {
      await signOut(this.identityProvider);
    }
  }

  async finishRegistration(): Promise<void> {
    this.pendingAccount.set(null);
    this.forgetSessionState();
    await signOut(this.identityProvider);
  }

  async discardPending(): Promise<void> {
    this.pendingAccount.set(null);
    await signOut(this.identityProvider);
  }

  async close(): Promise<void> {
    await this.ensureCsrfToken();
    await firstValueFrom(this.http.delete<void>(SESSION_PATH));
    this.forgetSessionState();
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

  private forgetSessionState(): void {
    this.currentSession.forget();
    this.dataPolicy.forget();
  }

  private verificationSettings(): ActionCodeSettings {
    return { url: `${this.document.location.origin}${LOGIN_PATH}` };
  }

  private async ensureCsrfToken(): Promise<void> {
    if (this.csrfToken.getToken() === null) {
      await firstValueFrom(this.http.get(CSRF_TOKEN_SOURCE_PATH));
    }
  }
}

function pendingAccountOf(user: User): PendingAccount {
  return {
    email: user.email ?? '',
    name: user.displayName ?? '',
    idToken: () => user.getIdToken(),
  };
}
