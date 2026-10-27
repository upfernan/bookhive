import { HttpClient, HttpXsrfTokenExtractor } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

const CSRF_TOKEN_SOURCE_PATH = '/actuator/health';
const RETURNS_PATH = '/api/v1/devoluciones';

export interface RegisteredReturn {
  readonly dueDate: string;
  readonly returnDate: string;
  readonly overdueDays: number;
  readonly fineGenerated: boolean;
  readonly dailyRate: number;
  readonly fineAmount: number;
  readonly reserved: boolean;
  readonly reservedFor: string;
}

@Injectable({ providedIn: 'root' })
export class ReturnDesk {
  private readonly http = inject(HttpClient);
  private readonly csrfToken = inject(HttpXsrfTokenExtractor);

  async returnBook(bookCopy: string): Promise<RegisteredReturn> {
    if (this.csrfToken.getToken() === null) {
      await firstValueFrom(this.http.get(CSRF_TOKEN_SOURCE_PATH));
    }

    return firstValueFrom(this.http.post<RegisteredReturn>(RETURNS_PATH, { bookCopy }));
  }
}
