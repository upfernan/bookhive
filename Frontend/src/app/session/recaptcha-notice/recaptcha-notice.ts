import { ChangeDetectionStrategy, Component } from '@angular/core';
import { messages } from '../../messages';

@Component({
  selector: 'app-recaptcha-notice',
  template: `
    <p class="recaptcha-notice">
      {{ text.before }}<a [href]="privacyUrl" target="_blank" rel="noopener noreferrer">{{
        text.privacy
      }}</a
      >{{ text.between }}<a [href]="termsUrl" target="_blank" rel="noopener noreferrer">{{
        text.terms
      }}</a
      >{{ text.after }}
    </p>
  `,
  styles: `
    .recaptcha-notice {
      margin: 16px 0 0;
      font-size: 13px;
      line-height: 1.5;
      color: var(--mat-sys-on-surface-variant);
      text-align: center;
    }

    a {
      color: var(--bh-walnut);
      text-underline-offset: 2px;
    }

    a:focus-visible {
      outline: 2px solid var(--bh-honey);
      outline-offset: 2px;
      border-radius: 2px;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RecaptchaNotice {
  protected readonly text = messages.session.recaptcha;
  protected readonly privacyUrl = 'https://policies.google.com/privacy';
  protected readonly termsUrl = 'https://policies.google.com/terms';
}
