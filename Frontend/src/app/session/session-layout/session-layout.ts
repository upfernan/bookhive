import { ChangeDetectionStrategy, Component } from '@angular/core';
import { messages } from '../../messages';

@Component({
  selector: 'app-session-layout',
  templateUrl: './session-layout.html',
  styleUrl: './session-layout.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SessionLayout {
  protected readonly messages = messages.brand;
}
