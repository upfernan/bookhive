import { HttpClient, HttpParams, HttpXsrfTokenExtractor } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

const CSRF_TOKEN_SOURCE_PATH = '/actuator/health';
const USERS_PATH = '/api/v1/usuarios';
const BOOK_COPIES_PATH = '/api/v1/ejemplares';
const LOANS_PATH = '/api/v1/prestamos';

export interface ReadyReservation {
  readonly reservation: string;
  readonly book: string;
  readonly bookCopy: string;
  readonly barcode: string;
  readonly pickupDeadline: string;
}

export interface OpenLoan {
  readonly loan: string;
  readonly bookCopy: string;
  readonly barcode: string;
  readonly book: string;
  readonly loanDate: string;
  readonly dueDate: string;
  readonly overdueDays: number;
  readonly accruedFine: number;
}

export interface Borrower {
  readonly id: string;
  readonly name: string;
  readonly email: string;
  readonly activeLoans: number;
  readonly maxLoans: number;
  readonly pendingFines: number;
  readonly blocks: readonly string[];
  readonly readyReservations: readonly ReadyReservation[];
  readonly openLoans: readonly OpenLoan[];
}

export interface BookCopy {
  readonly id: string;
  readonly barcode: string;
  readonly title: string;
  readonly authors: string;
  readonly branch: string;
  readonly available: boolean;
  readonly lendable: boolean;
  readonly blocks: readonly string[];
}

export interface RegisteredLoan {
  readonly loan: string;
  readonly dueDate: string;
}

@Injectable({ providedIn: 'root' })
export class LoanDesk {
  private readonly http = inject(HttpClient);
  private readonly csrfToken = inject(HttpXsrfTokenExtractor);

  findBorrower(email: string): Promise<Borrower> {
    const params = new HttpParams().set('correo', email.trim());
    return firstValueFrom(this.http.get<Borrower>(USERS_PATH, { params }));
  }

  findBookCopy(barcode: string, borrower = ''): Promise<BookCopy> {
    let params = new HttpParams().set('codigo', barcode.trim());
    if (borrower !== '') {
      params = params.set('usuario', borrower);
    }
    return firstValueFrom(this.http.get<BookCopy>(BOOK_COPIES_PATH, { params }));
  }

  async lend(borrower: string, bookCopy: string): Promise<RegisteredLoan> {
    if (this.csrfToken.getToken() === null) {
      await firstValueFrom(this.http.get(CSRF_TOKEN_SOURCE_PATH));
    }

    return firstValueFrom(this.http.post<RegisteredLoan>(LOANS_PATH, { user: borrower, bookCopy }));
  }
}
