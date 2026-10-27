import { HttpClient, HttpXsrfTokenExtractor } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

const CSRF_TOKEN_SOURCE_PATH = '/actuator/health';
const RESERVATIONS_PATH = '/api/v1/reservas';

export const ReservationStatus = {
  waiting: 'EN_ESPERA',
  assigned: 'ASIGNADA',
} as const;

export interface ReservedBook {
  readonly reservation: string;
  readonly book: string;
  readonly status: string;
  readonly position: number;
  readonly pickupDeadline: string;
}

export interface MyReservation {
  readonly reservation: string;
  readonly book: string;
  readonly title: string;
  readonly authors: string;
  readonly status: string;
  readonly position: number;
  readonly requestedAt: string;
  readonly pickupDeadline: string;
}

@Injectable({ providedIn: 'root' })
export class ReservationDesk {
  private readonly http = inject(HttpClient);
  private readonly csrfToken = inject(HttpXsrfTokenExtractor);

  listMine(): Promise<readonly MyReservation[]> {
    return firstValueFrom(this.http.get<readonly MyReservation[]>(RESERVATIONS_PATH));
  }

  async reserve(book: string): Promise<ReservedBook> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.post<ReservedBook>(RESERVATIONS_PATH, { book }));
  }

  async cancel(reservation: string): Promise<void> {
    await this.ensureCsrfToken();
    await firstValueFrom(this.http.delete(`${RESERVATIONS_PATH}/${encodeURIComponent(reservation)}`));
  }

  private async ensureCsrfToken(): Promise<void> {
    if (this.csrfToken.getToken() === null) {
      await firstValueFrom(this.http.get(CSRF_TOKEN_SOURCE_PATH));
    }
  }
}
