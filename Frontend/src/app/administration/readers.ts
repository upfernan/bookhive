import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { messages } from '../messages';
import { fillTemplate } from '../shared/fill-template';
import { FoundReader } from '../shared/reader-directory';
import { ReaderPicker } from '../shared/reader-picker';
import { describeRequestProblem } from '../shared/request-errors';
import { AccountStatus, AdministrationDesk, UserAccount } from './administration-desk';

type ReaderAction = 'deactivate' | 'reactivate' | 'purge';

interface ActionOption {
  readonly action: ReaderAction;
  readonly label: string;
  readonly text: string;
  readonly danger: boolean;
}

@Component({
  selector: 'app-readers',
  imports: [MatButtonModule, ReaderPicker],
  templateUrl: './readers.html',
  styleUrls: ['../shell/section-page.css', '../shared/feedback.css', '../shared/admin-page.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Readers {
  private readonly desk = inject(AdministrationDesk);

  protected readonly messages = messages.readers;

  protected readonly options: readonly ActionOption[] = [
    {
      action: 'deactivate',
      label: this.messages.deactivate,
      text: this.messages.deactivateText,
      danger: false,
    },
    {
      action: 'reactivate',
      label: this.messages.reactivate,
      text: this.messages.reactivateText,
      danger: false,
    },
    { action: 'purge', label: this.messages.purge, text: this.messages.purgeText, danger: true },
  ];

  protected readonly reader = signal<FoundReader | null>(null);
  protected readonly account = signal<UserAccount | null>(null);
  protected readonly confirming = signal<ActionOption | null>(null);
  protected readonly working = signal(false);
  protected readonly problem = signal('');

  protected readonly purged = computed(() => this.account()?.status === AccountStatus.purged);

  protected readonly statusText = computed(() => {
    const status = this.account()?.status ?? '';
    return this.messages.statuses[status] ?? status;
  });

  protected readonly doneText = computed(() =>
    fillTemplate(this.messages.done, {
      name: this.reader()?.name ?? '',
      status: this.statusText(),
    }),
  );

  protected readonly question = computed(() => {
    const option = this.confirming();
    return option === null
      ? ''
      : fillTemplate(this.messages.confirm, {
          action: option.label.toLocaleLowerCase('es'),
          name: this.reader()?.name ?? '',
        });
  });

  protected pick(reader: FoundReader): void {
    this.reader.set(reader);
    this.account.set(null);
    this.confirming.set(null);
    this.problem.set('');
  }

  protected ask(option: ActionOption): void {
    this.problem.set('');
    this.confirming.set(option);
  }

  protected cancel(): void {
    this.confirming.set(null);
  }

  protected async confirm(): Promise<void> {
    const reader = this.reader();
    const option = this.confirming();
    if (reader === null || option === null || this.working()) {
      return;
    }

    this.working.set(true);
    this.problem.set('');

    try {
      this.account.set(await this.run(option.action, reader.id));
      this.confirming.set(null);
    } catch (error) {
      this.problem.set(describeRequestProblem(error, this.messages.actionFailed).message);
    } finally {
      this.working.set(false);
    }
  }

  private run(action: ReaderAction, id: string): Promise<UserAccount> {
    switch (action) {
      case 'deactivate':
        return this.desk.deactivateUser(id);
      case 'reactivate':
        return this.desk.reactivateUser(id);
      default:
        return this.desk.purgeUser(id);
    }
  }
}
