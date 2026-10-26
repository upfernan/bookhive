import { HttpClient, HttpXsrfTokenExtractor } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

const DATA_POLICY_PATH = '/api/v1/politica-datos';
const ACCEPTANCE_PATH = `${DATA_POLICY_PATH}/aceptacion`;
const CSRF_TOKEN_SOURCE_PATH = '/actuator/health';

export type DataPolicyStatus = 'unknown' | 'accepted' | 'pending' | 'unavailable';

export interface DataPolicyDocument {
  readonly version: number;
  readonly content: string;
  readonly publishedAt: string;
  readonly accepted: boolean;
  readonly acceptedAt: string | null;
}

@Injectable({ providedIn: 'root' })
export class DataPolicy {
  private readonly http = inject(HttpClient);
  private readonly csrfToken = inject(HttpXsrfTokenExtractor);
  private pending: Promise<DataPolicyStatus> | null = null;

  readonly status = signal<DataPolicyStatus>('unknown');

  find(): Promise<DataPolicyDocument> {
    return firstValueFrom(this.http.get<DataPolicyDocument>(DATA_POLICY_PATH));
  }

  check(): Promise<DataPolicyStatus> {
    if (this.status() === 'accepted') {
      return Promise.resolve('accepted');
    }

    this.pending ??= this.fetchStatus().finally(() => {
      this.pending = null;
    });

    return this.pending;
  }

  async accept(version: number): Promise<DataPolicyDocument> {
    if (this.csrfToken.getToken() === null) {
      await firstValueFrom(this.http.get(CSRF_TOKEN_SOURCE_PATH));
    }

    const policy = await firstValueFrom(this.http.post<DataPolicyDocument>(ACCEPTANCE_PATH, { version }));
    this.status.set(policy.accepted ? 'accepted' : 'pending');
    return policy;
  }

  forget(): void {
    this.status.set('unknown');
  }

  private async fetchStatus(): Promise<DataPolicyStatus> {
    try {
      const policy = await this.find();
      return this.settle(policy.accepted ? 'accepted' : 'pending');
    } catch {
      return this.settle('unavailable');
    }
  }

  private settle(status: DataPolicyStatus): DataPolicyStatus {
    this.status.set(status);
    return status;
  }
}
