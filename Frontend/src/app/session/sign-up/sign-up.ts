import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { messages } from '../../messages';
import { RecaptchaNotice } from '../recaptcha-notice/recaptcha-notice';
import { RegistrationDesk, domainOf } from '../registration-desk';
import { Session } from '../session';
import { describeSignUpError } from '../session-errors';
import { SessionLayout } from '../session-layout/session-layout';

const MIN_PASSWORD_LENGTH = 8;
const MAX_NAME_LENGTH = 200;
const NOT_FOUND = 404;

@Component({
  selector: 'app-sign-up',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    RecaptchaNotice,
    SessionLayout,
  ],
  templateUrl: './sign-up.html',
  styleUrls: ['../session-page.css', './sign-up.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SignUp {
  private readonly session = inject(Session);
  private readonly registrationDesk = inject(RegistrationDesk);

  protected readonly messages = messages;
  protected readonly submitting = signal(false);
  protected readonly sent = signal(false);
  protected readonly errorMessage = signal('');
  protected readonly passwordVisible = signal(false);

  protected readonly form = inject(NonNullableFormBuilder).group({
    name: ['', [Validators.required, Validators.maxLength(MAX_NAME_LENGTH)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(MIN_PASSWORD_LENGTH)]],
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

    const { name, email, password } = this.form.getRawValue();
    const cleanEmail = email.trim();

    try {
      await this.registrationDesk.publicPolicy(domainOf(cleanEmail));
      await this.session.signUp(name.trim(), cleanEmail, password);
      this.sent.set(true);
    } catch (error) {
      this.form.controls.password.reset();
      this.passwordVisible.set(false);
      this.errorMessage.set(
        error instanceof HttpErrorResponse && error.status === NOT_FOUND
          ? messages.signUp.domainNotAllowed
          : describeSignUpError(error),
      );
    } finally {
      this.submitting.set(false);
    }
  }
}
