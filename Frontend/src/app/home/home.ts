import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { messages } from '../messages';
import { CurrentSession } from '../session/current-session';
import { tasks } from '../shell/navigation';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Home {
  protected readonly currentSession = inject(CurrentSession);
  protected readonly messages = messages;
  protected readonly tasks = computed(() =>
    tasks.filter((task) => this.currentSession.allows(task.permission)),
  );
  protected readonly profile = computed(() =>
    [this.currentSession.role(), this.currentSession.institution()].filter(Boolean).join(' · '),
  );
}
