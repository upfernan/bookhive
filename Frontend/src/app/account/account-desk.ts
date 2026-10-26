import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { MyReservation } from '../reservations/reservation-desk';

const MY_ACCOUNT_PATH = '/api/v1/mi-cuenta';

export interface AccountBook {
  readonly id: string;
  readonly title: string;
  readonly authors: string;
}

export interface ActiveLoan {
  readonly loan: string;
  readonly book: AccountBook;
  readonly loanDate: string;
  readonly dueDate: string;
  readonly overdueDays: number;
  readonly accruedFine: number;
}

export interface PendingFine {
  readonly fine: string;
  readonly book: AccountBook;
  readonly issueDate: string;
  readonly overdueDays: number;
  readonly dailyRate: number;
  readonly amount: number;
}

export interface ReturnedLoan {
  readonly loan: string;
  readonly book: AccountBook;
  readonly loanDate: string;
  readonly dueDate: string;
  readonly returnDate: string;
  readonly overdueDays: number;
}

export interface FinePayment {
  readonly fine: string;
  readonly book: AccountBook;
  readonly reference: string;
  readonly amount: number;
  readonly paidAt: string;
}

export interface MyAccount {
  readonly activeLoans: readonly ActiveLoan[];
  readonly reservations: readonly MyReservation[];
  readonly pendingFines: readonly PendingFine[];
  readonly pendingFinesTotal: number;
  readonly accruingFinesTotal: number;
  readonly loanHistory: readonly ReturnedLoan[];
  readonly payments: readonly FinePayment[];
}

@Injectable({ providedIn: 'root' })
export class AccountDesk {
  private readonly http = inject(HttpClient);

  viewMine(): Promise<MyAccount> {
    return firstValueFrom(this.http.get<MyAccount>(MY_ACCOUNT_PATH));
  }
}
