import { HttpClient, HttpXsrfTokenExtractor } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

const CSRF_TOKEN_SOURCE_PATH = '/actuator/health';
const PUBLIC_POLICY_PATH = '/api/v1/politica-datos/publica';
const REGISTRATION_PATH = '/api/v1/registro';

export interface PublicDataPolicy {
  readonly version: number;
  readonly content: string;
  readonly publishedAt: string;
}

@Injectable({ providedIn: 'root' })
export class RegistrationDesk {
  private readonly http = inject(HttpClient);
  private readonly csrfToken = inject(HttpXsrfTokenExtractor);

  publicPolicy(domain: string): Promise<PublicDataPolicy> {
    return firstValueFrom(this.http.get<PublicDataPolicy>(PUBLIC_POLICY_PATH, { params: { dominio: domain } }));
  }

  async register(idToken: string, name: string, policyVersion: number): Promise<void> {
    await this.ensureCsrfToken();
    await firstValueFrom(this.http.post<void>(REGISTRATION_PATH, { idToken, name, policyVersion }));
  }

  private async ensureCsrfToken(): Promise<void> {
    if (this.csrfToken.getToken() === null) {
      await firstValueFrom(this.http.get(CSRF_TOKEN_SOURCE_PATH));
    }
  }
}

export function domainOf(email: string): string {
  const at = email.lastIndexOf('@');
  return at < 0 ? '' : email.slice(at + 1).trim().toLowerCase();
}
