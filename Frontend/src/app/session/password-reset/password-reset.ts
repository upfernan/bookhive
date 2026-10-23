import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { messages } from '../../messages';
import { RecaptchaNotice } from '../recaptcha-notice/recaptcha-notice';
import { Session } from '../session';
import { describePasswordResetError } from '../session-errors';
import { SessionLayout } from '../session-layout/session-layout';

@Component({
  selector: 'app-password-reset',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    RecaptchaNotice,
    SessionLayout,
  ],
  templateUrl: './password-reset.html',
  styleUrl: '../session-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PasswordReset {
  private readonly session = inject(Session);

  protected readonly messages = messages;
  protected readonly sending = signal(false);
  protected readonly sent = signal(false);
  protected readonly errorMessage = signal('');

  protected readonly form = inject(NonNullableFormBuilder).group({
    email: [emailFromNavigation(), [Validators.required, Validators.email]],
  });

  constructor() {
    void this.session.preloadBotProtection();
  }

  protected async submit(): Promise<void> {
    if (this.sending()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.sending.set(true);
    this.errorMessage.set('');

    try {
      await this.session.requestPasswordReset(this.form.getRawValue().email.trim());
      this.sent.set(true);
    } catch (error) {
      this.errorMessage.set(describePasswordResetError(error));
    } finally {
      this.sending.set(false);
    }
  }
}

function emailFromNavigation(): string {
  const state: unknown = history.state;

  if (typeof state === 'object' && state !== null && 'email' in state) {
    return typeof state.email === 'string' ? state.email : '';
  }

  return '';
}
