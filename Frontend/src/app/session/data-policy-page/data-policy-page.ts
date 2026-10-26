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
import { ActivatedRoute, Router } from '@angular/router';
import { messages } from '../../messages';
import { fillTemplate } from '../../shared/fill-template';
import { describeRequestProblem } from '../../shared/request-errors';
import { DataPolicy, DataPolicyDocument } from '../data-policy';
import { Session } from '../session';
import { describeSessionError } from '../session-errors';
import { SessionLayout } from '../session-layout/session-layout';
import { safeReturnPath } from '../session-guards';

const DATE_FORMAT = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'long', year: 'numeric' });
const LOGIN_PATH = '/ingresar';

@Component({
  selector: 'app-data-policy-page',
  imports: [MatButtonModule, SessionLayout],
  templateUrl: './data-policy-page.html',
  styleUrls: ['../session-page.css', './data-policy-page.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DataPolicyPage {
  private readonly dataPolicy = inject(DataPolicy);
  private readonly session = inject(Session);
  private readonly router = inject(Router);
  private readonly returnPath = safeReturnPath(inject(ActivatedRoute).snapshot.queryParamMap.get('volver'));
  private readonly heading = viewChild<ElementRef<HTMLElement>>('heading');

  protected readonly messages = messages.dataPolicy;
  protected readonly policy = signal<DataPolicyDocument | null>(null);
  protected readonly loading = signal(false);
  protected readonly accepting = signal(false);
  protected readonly closing = signal(false);
  protected readonly errorMessage = signal('');

  protected readonly paragraphs = computed(() =>
    (this.policy()?.content ?? '')
      .split('\n')
      .map((line) => line.trim())
      .filter((line) => line !== ''),
  );

  protected readonly versionText = computed(() => {
    const policy = this.policy();
    return policy
      ? fillTemplate(this.messages.version, { version: String(policy.version), date: formatDate(policy.publishedAt) })
      : '';
  });

  protected readonly acceptedText = computed(() => {
    const acceptedAt = this.policy()?.acceptedAt ?? '';
    return acceptedAt === '' ? '' : fillTemplate(this.messages.acceptedOn, { date: formatDate(acceptedAt) });
  });

  constructor() {
    afterNextRender(() => this.heading()?.nativeElement.focus());
    void this.load();
  }

  protected async load(): Promise<void> {
    this.loading.set(true);
    this.errorMessage.set('');

    try {
      this.policy.set(await this.dataPolicy.find());
    } catch (error) {
      this.errorMessage.set(describeRequestProblem(error, this.messages.loadFailed).message);
    } finally {
      this.loading.set(false);
    }
  }

  protected async accept(): Promise<void> {
    const policy = this.policy();
    if (!policy || this.accepting()) {
      return;
    }

    this.accepting.set(true);
    this.errorMessage.set('');

    try {
      this.policy.set(await this.dataPolicy.accept(policy.version));
      await this.router.navigateByUrl(this.returnPath);
    } catch (error) {
      this.errorMessage.set(describeRequestProblem(error, this.messages.acceptFailed).message);
    } finally {
      this.accepting.set(false);
    }
  }

  protected async continue(): Promise<void> {
    await this.router.navigateByUrl(this.returnPath);
  }

  protected async closeSession(): Promise<void> {
    if (this.closing()) {
      return;
    }

    this.closing.set(true);
    this.errorMessage.set('');

    try {
      await this.session.close();
      await this.router.navigateByUrl(LOGIN_PATH);
    } catch (error) {
      this.errorMessage.set(describeSessionError(error, messages.session.closeFailed));
    } finally {
      this.closing.set(false);
    }
  }
}

function formatDate(value: string): string {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? '' : DATE_FORMAT.format(date);
}
