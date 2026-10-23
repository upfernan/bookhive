import { InjectionToken } from '@angular/core';
import { initializeApp } from '@firebase/app';
import { Auth, initializeAuth, inMemoryPersistence } from '@firebase/auth';
import { identityProviderSettings } from './identity-provider.settings';

const LANGUAGE = 'es';

export const IDENTITY_PROVIDER = new InjectionToken<Auth>('IDENTITY_PROVIDER', {
  providedIn: 'root',
  factory: () => {
    const identityProvider = initializeAuth(initializeApp(identityProviderSettings), {
      persistence: inMemoryPersistence,
    });
    identityProvider.languageCode = LANGUAGE;
    return identityProvider;
  },
});
