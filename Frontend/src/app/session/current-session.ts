import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { PermissionCode } from './permission-code';

const CURRENT_SESSION_PATH = '/api/v1/sesiones/actual';
const UNAUTHORIZED = 401;

export type CurrentSessionStatus = 'unknown' | 'signed-in' | 'signed-out' | 'unavailable';

interface CurrentSessionResponse {
  readonly email?: unknown;
  readonly institution?: unknown;
  readonly roles?: unknown;
  readonly permissions?: unknown;
}

@Injectable({ providedIn: 'root' })
export class CurrentSession {
  private readonly http = inject(HttpClient);
  private readonly grantedPermissions = signal<ReadonlySet<string>>(new Set());
  private pending: Promise<CurrentSessionStatus> | null = null;

  readonly status = signal<CurrentSessionStatus>('unknown');
  readonly email = signal('');
  readonly institution = signal('');
  readonly roles = signal<readonly string[]>([]);
  readonly role = computed(() => readableRole(this.roles()[0] ?? ''));
  readonly initial = computed(() => this.email().charAt(0).toLocaleUpperCase('es'));

  allows(permission: PermissionCode): boolean {
    return this.status() === 'unavailable' || this.grantedPermissions().has(permission);
  }

  load(): Promise<CurrentSessionStatus> {
    if (this.status() === 'signed-in') {
      return Promise.resolve('signed-in');
    }

    this.pending ??= this.fetch().finally(() => {
      this.pending = null;
    });

    return this.pending;
  }

  forget(): void {
    this.status.set('unknown');
    this.email.set('');
    this.institution.set('');
    this.roles.set([]);
    this.grantedPermissions.set(new Set());
  }

  private async fetch(): Promise<CurrentSessionStatus> {
    try {
      const response = await firstValueFrom(
        this.http.get<CurrentSessionResponse | null>(CURRENT_SESSION_PATH),
      );
      const permissions = textsOf(response?.permissions);

      if (permissions === null) {
        this.forget();
        return this.settle('unavailable');
      }

      this.email.set(textOf(response?.email));
      this.institution.set(textOf(response?.institution));
      this.roles.set(textsOf(response?.roles) ?? []);
      this.grantedPermissions.set(new Set(permissions));
      return this.settle('signed-in');
    } catch (error) {
      const signedOut = error instanceof HttpErrorResponse && error.status === UNAUTHORIZED;
      this.forget();
      return this.settle(signedOut ? 'signed-out' : 'unavailable');
    }
  }

  private settle(status: CurrentSessionStatus): CurrentSessionStatus {
    this.status.set(status);
    return status;
  }
}

function textOf(value: unknown): string {
  return typeof value === 'string' ? value.trim() : '';
}

function textsOf(value: unknown): string[] | null {
  return Array.isArray(value)
    ? value.filter((item): item is string => typeof item === 'string' && item.trim() !== '')
    : null;
}

function readableRole(role: string): string {
  const lower = role.trim().toLocaleLowerCase('es');
  return lower.charAt(0).toLocaleUpperCase('es') + lower.slice(1);
}
