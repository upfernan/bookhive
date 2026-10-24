import { HttpClient, HttpParams, HttpXsrfTokenExtractor } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

const CSRF_TOKEN_SOURCE_PATH = '/actuator/health';
const BOOK_COPIES_PATH = '/api/v1/ejemplares';
const RETURNS_PATH = '/api/v1/devoluciones';

interface FoundBookCopy {
  readonly id: string;
  readonly barcode: string;
  readonly title: string;
}

interface RegisteredReturn {
  readonly dueDate: string;
  readonly returnDate: string;
  readonly overdueDays: number;
  readonly fineGenerated: boolean;
  readonly dailyRate: number;
  readonly fineAmount: number;
  readonly reserved: boolean;
  readonly reservedFor: string;
}

export interface ReturnedBook extends RegisteredReturn {
  readonly title: string;
  readonly barcode: string;
}

@Injectable({ providedIn: 'root' })
export class ReturnDesk {
  private readonly http = inject(HttpClient);
  private readonly csrfToken = inject(HttpXsrfTokenExtractor);

  async returnBook(barcode: string): Promise<ReturnedBook> {
    const params = new HttpParams().set('codigo', barcode.trim());
    const bookCopy = await firstValueFrom(this.http.get<FoundBookCopy>(BOOK_COPIES_PATH, { params }));

    if (this.csrfToken.getToken() === null) {
      await firstValueFrom(this.http.get(CSRF_TOKEN_SOURCE_PATH));
    }

    const registered = await firstValueFrom(
      this.http.post<RegisteredReturn>(RETURNS_PATH, { bookCopy: bookCopy.id }),
    );

    return { ...registered, title: bookCopy.title, barcode: bookCopy.barcode };
  }
}
