import { ChangeDetectionStrategy, Component } from '@angular/core';
import { messages } from '../messages';

@Component({
  selector: 'app-catalog',
  template: `
    <h1 class="page__title">{{ messages.catalog.title }}</h1>
    <p class="page__lead">{{ messages.catalog.lead }}</p>
  `,
  styleUrl: '../shell/section-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Catalog {
  protected readonly messages = messages;
}
