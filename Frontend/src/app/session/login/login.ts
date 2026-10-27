import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';
import { messages } from '../../messages';
import { Session } from '../session';
import { RecaptchaNotice } from '../recaptcha-notice/recaptcha-notice';
import { describeSessionError } from '../session-errors';
import { SessionLayout } from '../session-layout/session-layout';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    RecaptchaNotice,
    SessionLayout,
  ],
  templateUrl: './login.html',
  styleUrl: '../session-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Login {
  private readonly session = inject(Session);
  private readonly router = inject(Router);

  protected readonly messages = messages.session;
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly passwordVisible = signal(false);
  protected readonly unverified = signal(false);
  protected readonly resending = signal(false);
  protected readonly resendNotice = signal('');

  protected readonly form = inject(NonNullableFormBuilder).group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  constructor() {
    void this.session.preloadBotProtection();
  }

  protected togglePasswordVisibility(): void {
    this.passwordVisible.update((visible) => !visible);
  }

  protected async submit(): Promise<void> {
    if (this.submitting()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set('');
    this.unverified.set(false);
    this.resendNotice.set('');

    const { email, password } = this.form.getRawValue();

    try {
      const outcome = await this.session.open(email.trim(), password);

      if (outcome === 'unverified') {
        this.form.controls.password.reset();
        this.passwordVisible.set(false);
        this.unverified.set(true);
        return;
      }

      await this.router.navigateByUrl(outcome === 'unregistered' ? '/completar-registro' : '/inicio');
    } catch (error) {
      this.form.controls.password.reset();
      this.passwordVisible.set(false);
      this.errorMessage.set(describeSessionError(error, this.messages.openFailed));
    } finally {
      this.submitting.set(false);
    }
  }

  protected async resendVerification(): Promise<void> {
    if (this.resending()) {
      return;
    }

    this.resending.set(true);

    try {
      await this.session.resendVerification();
      this.resendNotice.set(this.messages.resent);
    } catch {
      this.resendNotice.set(this.messages.resendFailed);
    } finally {
      this.resending.set(false);
    }
  }
}
