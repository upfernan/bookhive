import { HttpErrorResponse } from '@angular/common/http';
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
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { messages } from '../../messages';
import { fillTemplate } from '../../shared/fill-template';
import { describeRequestProblem } from '../../shared/request-errors';
import { PublicDataPolicy, RegistrationDesk, domainOf } from '../registration-desk';
import { Session } from '../session';
import { SessionLayout } from '../session-layout/session-layout';

const DATE_FORMAT = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'long', year: 'numeric' });
const MAX_NAME_LENGTH = 200;
const LOGIN_PATH = '/ingresar';
const HOME_PATH = '/inicio';
const NOT_FOUND = 404;
const CONFLICT = 409;

@Component({
  selector: 'app-complete-registration',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    SessionLayout,
  ],
  templateUrl: './complete-registration.html',
  styleUrls: [
    '../session-page.css',
    '../data-policy-page/data-policy-page.css',
    './complete-registration.css',
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CompleteRegistration {
  private readonly session = inject(Session);
  private readonly registrationDesk = inject(RegistrationDesk);
  private readonly router = inject(Router);
  private readonly heading = viewChild<ElementRef<HTMLElement>>('heading');
  private readonly domain = domainOf(this.session.pendingAccount()?.email ?? '');

  protected readonly messages = messages.registration;
  protected readonly policy = signal<PublicDataPolicy | null>(null);
  protected readonly loading = signal(false);
  protected readonly registering = signal(false);
  protected readonly cancelling = signal(false);
  protected readonly errorMessage = signal('');

  protected readonly form = inject(NonNullableFormBuilder).group({
    name: [
      this.session.pendingAccount()?.name ?? '',
      [Validators.required, Validators.maxLength(MAX_NAME_LENGTH)],
    ],
    accepted: [false],
  });

  protected readonly paragraphs = computed(() =>
    (this.policy()?.content ?? '')
      .split('\n')
      .map((paragraph) => paragraph.trim())
      .filter((paragraph) => paragraph !== ''),
  );

  protected readonly versionText = computed(() => {
    const current = this.policy();
    return current
      ? fillTemplate(this.messages.version, { version: String(current.version), date: dateOf(current.publishedAt) })
      : '';
  });

  constructor() {
    void this.load();
    afterNextRender(() => this.heading()?.nativeElement.focus());
  }

  protected async load(): Promise<void> {
    this.loading.set(true);
    this.errorMessage.set('');

    try {
      this.policy.set(await this.registrationDesk.publicPolicy(this.domain));
    } catch (error) {
      this.errorMessage.set(
        error instanceof HttpErrorResponse && error.status === NOT_FOUND
          ? messages.signUp.domainNotAllowed
          : describeRequestProblem(error, this.messages.loadFailed).message,
      );
    } finally {
      this.loading.set(false);
    }
  }

  protected async submit(): Promise<void> {
    const current = this.policy();
    const account = this.session.pendingAccount();

    if (this.registering() || !current) {
      return;
    }

    if (!account) {
      await this.router.navigateByUrl(LOGIN_PATH);
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (!this.form.controls.accepted.value) {
      this.errorMessage.set(this.messages.acceptRequired);
      return;
    }

    this.registering.set(true);
    this.errorMessage.set('');

    try {
      await this.registrationDesk.register(
        await account.idToken(),
        this.form.controls.name.value.trim(),
        current.version,
      );
      await this.session.finishRegistration();
      await this.router.navigateByUrl(HOME_PATH);
    } catch (error) {
      await this.explain(error, current);
    } finally {
      this.registering.set(false);
    }
  }

  protected async cancel(): Promise<void> {
    if (this.cancelling()) {
      return;
    }

    this.cancelling.set(true);

    try {
      await this.session.discardPending();
    } finally {
      this.cancelling.set(false);
      await this.router.navigateByUrl(LOGIN_PATH);
    }
  }

  private async explain(error: unknown, accepted: PublicDataPolicy): Promise<void> {
    if (error instanceof HttpErrorResponse && error.status === CONFLICT) {
      const latest = await this.registrationDesk.publicPolicy(this.domain).catch(() => null);

      if (latest && latest.version !== accepted.version) {
        this.policy.set(latest);
        this.form.controls.accepted.setValue(false);
        this.errorMessage.set(this.messages.policyChanged);
        return;
      }
    }

    this.errorMessage.set(describeRequestProblem(error, this.messages.failed).message);
  }
}

function dateOf(value: string): string {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '' : DATE_FORMAT.format(date);
}
