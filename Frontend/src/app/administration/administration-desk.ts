import { HttpClient, HttpXsrfTokenExtractor } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

const SETTINGS_PATH = '/api/v1/admin/parametros';
const BRANCHES_PATH = '/api/v1/sedes';
const ADMIN_BRANCHES_PATH = '/api/v1/admin/sedes';
const LEVELS_PATH = '/api/v1/ubicacion/niveles';
const ADMIN_LEVELS_PATH = '/api/v1/admin/ubicacion/niveles';
const BOOK_COPIES_PATH = '/api/v1/ejemplares';
const ADMIN_USERS_PATH = '/api/v1/admin/usuarios';
const MY_ACCOUNT_PURGE_PATH = '/api/v1/mi-cuenta/depuracion';
const CATALOG_SYNC_PATH = '/api/v1/catalogo/sincronizacion';
const CSRF_TOKEN_SOURCE_PATH = '/actuator/health';

export const AccountStatus = {
  active: 'ACTIVO',
  retired: 'RETIRADO',
  purged: 'DEPURADO',
} as const;

export const BookCopyStatus = {
  available: 'DISPONIBLE',
  lent: 'PRESTADO',
  reserved: 'RESERVADO',
  withdrawn: 'BAJA',
} as const;

export interface InstitutionSettings {
  readonly loanDays: number;
  readonly maxConcurrentLoans: number;
  readonly dailyFineRate: number;
  readonly timeZone: string;
}

export interface Branch {
  readonly id: string;
  readonly name: string;
}

export interface Level {
  readonly depth: number;
  readonly name: string;
}

export interface Spot {
  readonly depth: number;
  readonly level: string;
  readonly value: string;
}

export interface BookCopyLocation {
  readonly bookCopy: string;
  readonly barcode: string;
  readonly status: string;
  readonly branch: Branch | null;
  readonly spots: readonly Spot[];
  readonly description: string;
}

export interface UserAccount {
  readonly user: string;
  readonly status: string;
  readonly deactivatedAt: string | null;
}

export interface Retention {
  readonly months: number;
}

export interface CatalogSync {
  readonly indexed: number;
  readonly removed: number;
  readonly fullReload: boolean;
}

/**
 * Habla con los endpoints de administración del backend: parámetros de la institución,
 * sedes y ubicación de ejemplares, ciclo de vida de los lectores (módulo circulation)
 * y la sincronización manual del catálogo (módulo catalog).
 */
@Injectable({ providedIn: 'root' })
export class AdministrationDesk {
  private readonly http = inject(HttpClient);
  private readonly csrfToken = inject(HttpXsrfTokenExtractor);

  findSettings(): Promise<InstitutionSettings> {
    return firstValueFrom(this.http.get<InstitutionSettings>(SETTINGS_PATH));
  }

  async saveSettings(settings: InstitutionSettings): Promise<InstitutionSettings> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.put<InstitutionSettings>(SETTINGS_PATH, settings));
  }

  listBranches(): Promise<readonly Branch[]> {
    return firstValueFrom(this.http.get<readonly Branch[]>(BRANCHES_PATH));
  }

  async createBranch(name: string): Promise<Branch> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.post<Branch>(ADMIN_BRANCHES_PATH, { name: name.trim() }));
  }

  listLevels(): Promise<readonly Level[]> {
    return firstValueFrom(this.http.get<readonly Level[]>(LEVELS_PATH));
  }

  async saveLevels(names: readonly string[]): Promise<readonly Level[]> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.put<readonly Level[]>(ADMIN_LEVELS_PATH, { names }));
  }

  findLocation(barcode: string): Promise<BookCopyLocation> {
    return firstValueFrom(
      this.http.get<BookCopyLocation>(`${BOOK_COPIES_PATH}/${encoded(barcode)}/ubicacion`),
    );
  }

  async relocate(
    barcode: string,
    branch: string,
    values: readonly string[],
  ): Promise<BookCopyLocation> {
    await this.ensureCsrfToken();
    return firstValueFrom(
      this.http.put<BookCopyLocation>(`${BOOK_COPIES_PATH}/${encoded(barcode)}/ubicacion`, {
        barcode: barcode.trim(),
        branch,
        values,
      }),
    );
  }

  async withdrawBookCopy(barcode: string): Promise<BookCopyLocation> {
    await this.ensureCsrfToken();
    return firstValueFrom(
      this.http.post<BookCopyLocation>(`${BOOK_COPIES_PATH}/${encoded(barcode)}/baja`, {}),
    );
  }

  async deactivateUser(id: string): Promise<UserAccount> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.post<UserAccount>(`${ADMIN_USERS_PATH}/${id}/baja`, {}));
  }

  async reactivateUser(id: string): Promise<UserAccount> {
    await this.ensureCsrfToken();
    return firstValueFrom(
      this.http.post<UserAccount>(`${ADMIN_USERS_PATH}/${id}/reactivacion`, {}),
    );
  }

  async purgeUser(id: string): Promise<UserAccount> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.post<UserAccount>(`${ADMIN_USERS_PATH}/${id}/depuracion`, {}));
  }

  async purgeMyAccount(): Promise<UserAccount> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.post<UserAccount>(MY_ACCOUNT_PURGE_PATH, {}));
  }

  findRetention(): Promise<Retention> {
    return firstValueFrom(this.http.get<Retention>(`${ADMIN_USERS_PATH}/conservacion`));
  }

  async saveRetention(months: number): Promise<Retention> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.put<Retention>(`${ADMIN_USERS_PATH}/conservacion`, { months }));
  }

  async syncCatalog(): Promise<CatalogSync> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.post<CatalogSync>(CATALOG_SYNC_PATH, {}));
  }

  private async ensureCsrfToken(): Promise<void> {
    if (this.csrfToken.getToken() === null) {
      await firstValueFrom(this.http.get(CSRF_TOKEN_SOURCE_PATH));
    }
  }
}

function encoded(barcode: string): string {
  return encodeURIComponent(barcode.trim());
}
