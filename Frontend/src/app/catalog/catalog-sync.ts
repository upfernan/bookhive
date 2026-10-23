import { ChangeDetectionStrategy, Component } from '@angular/core';
import { messages } from '../messages';

@Component({
  selector: 'app-catalog-sync',
  template: `
    <h1 class="page__title">{{ messages.catalogSync.title }}</h1>
    <p class="page__lead">{{ messages.catalogSync.lead }}</p>
  `,
  styleUrl: '../shell/section-page.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CatalogSync {
  protected readonly messages = messages;
}
