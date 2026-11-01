import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';
import { messages } from '../messages';
import { Session } from '../session/session';
import { describeRequestProblem } from '../shared/request-errors';
import { AdministrationDesk } from './administration-desk';

/** Bloque de "Mi cuenta" para que el lector pida borrar sus datos personales. */
@Component({
  selector: 'app-delete-my-data',
  imports: [MatButtonModule],
  templateUrl: './delete-my-data.html',
  styleUrls: ['../shared/admin-page.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeleteMyData {
  private readonly desk = inject(AdministrationDesk);
  private readonly session = inject(Session);
  private readonly router = inject(Router);

  protected readonly messages = messages.myData;
  protected readonly confirming = signal(false);
  protected readonly working = signal(false);
  protected readonly problem = signal('');

  protected ask(): void {
    this.problem.set('');
    this.confirming.set(true);
  }

  protected async purge(): Promise<void> {
    if (this.working()) {
      return;
    }

    this.working.set(true);
    this.problem.set('');

    try {
      await this.desk.purgeMyAccount();
    } catch (error) {
      this.problem.set(describeRequestProblem(error, this.messages.failed).message);
      this.working.set(false);
      return;
    }

    try {
      await this.session.close();
    } finally {
      await this.router.navigateByUrl('/ingresar');
    }
  }
}
