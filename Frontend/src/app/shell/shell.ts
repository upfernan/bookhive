import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatMenuModule } from '@angular/material/menu';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { messages } from '../messages';
import { CurrentSession } from '../session/current-session';
import { Session } from '../session/session';
import { describeSessionError } from '../session/session-errors';
import { sections } from './navigation';

@Component({
  selector: 'app-shell',
  imports: [MatMenuModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './shell.html',
  styleUrl: './shell.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Shell {
  private readonly session = inject(Session);
  private readonly router = inject(Router);

  protected readonly currentSession = inject(CurrentSession);
  protected readonly messages = messages;
  protected readonly sections = computed(() =>
    sections.filter((section) => this.currentSession.allows(section.permission)),
  );
  protected readonly profile = computed(() =>
    [this.currentSession.role(), this.currentSession.institution()].filter(Boolean).join(' · '),
  );
  protected readonly menuOpen = signal(false);
  protected readonly closing = signal(false);
  protected readonly retrying = signal(false);
  protected readonly errorMessage = signal('');

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  protected closeMenu(): void {
    this.menuOpen.set(false);
  }

  protected async closeSession(): Promise<void> {
    if (this.closing()) {
      return;
    }

    this.closing.set(true);
    this.errorMessage.set('');

    try {
      await this.session.close();
      await this.router.navigateByUrl('/ingresar');
    } catch (error) {
      this.errorMessage.set(describeSessionError(error, messages.session.closeFailed));
    } finally {
      this.closing.set(false);
    }
  }

  protected async retry(): Promise<void> {
    if (this.retrying()) {
      return;
    }

    this.retrying.set(true);

    try {
      await this.currentSession.load();
    } finally {
      this.retrying.set(false);
    }
  }
}
