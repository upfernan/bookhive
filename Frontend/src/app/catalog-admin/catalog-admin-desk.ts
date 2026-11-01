import { HttpClient, HttpXsrfTokenExtractor } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

const CATEGORIES_PATH = '/api/v1/categorias';
const ADMIN_CATEGORIES_PATH = '/api/v1/admin/categorias';
const TITLES_PATH = '/api/v1/catalogo/titulos';
const BRANCHES_PATH = '/api/v1/sedes';
const CSRF_TOKEN_SOURCE_PATH = '/actuator/health';

export const TitleFormat = {
  physical: 'FISICO',
  digital: 'DIGITAL',
} as const;

export type TitleFormat = (typeof TitleFormat)[keyof typeof TitleFormat];

export const CategoryStatus = {
  active: 'ACTIVA',
  inactive: 'INACTIVA',
} as const;

export const TitleStatus = {
  active: 'ACTIVO',
  withdrawn: 'BAJA',
} as const;

export interface Category {
  readonly id: string;
  readonly name: string;
  readonly status: string;
  readonly activeTitles: number;
}

export interface Branch {
  readonly id: string;
  readonly name: string;
}

export interface Title {
  readonly id: string;
  readonly title: string;
  readonly isbn: string;
  readonly year: number;
  readonly format: string;
  readonly status: string;
  readonly category: string;
  readonly publisher: string;
  readonly authors: readonly string[];
}

export interface TitleDraft {
  readonly title: string;
  readonly isbn: string;
  readonly year: number;
  readonly format: TitleFormat;
  readonly category: string;
  readonly publisher: string;
  readonly authors: readonly string[];
  readonly branch: string | null;
  readonly barcodes: readonly string[];
}

/**
 * Habla con los endpoints de administración del catálogo (módulo catalog del backend)
 * y con la lista de sedes (módulo circulation), que se usa para registrar ejemplares.
 */
@Injectable({ providedIn: 'root' })
export class CatalogAdminDesk {
  private readonly http = inject(HttpClient);
  private readonly csrfToken = inject(HttpXsrfTokenExtractor);

  listCategories(): Promise<readonly Category[]> {
    return firstValueFrom(this.http.get<readonly Category[]>(CATEGORIES_PATH));
  }

  async createCategory(name: string): Promise<Category> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.post<Category>(ADMIN_CATEGORIES_PATH, { name: name.trim() }));
  }

  async renameCategory(id: string, name: string): Promise<Category> {
    await this.ensureCsrfToken();
    return firstValueFrom(
      this.http.put<Category>(`${ADMIN_CATEGORIES_PATH}/${id}`, { name: name.trim() }),
    );
  }

  async activateCategory(id: string): Promise<Category> {
    await this.ensureCsrfToken();
    return firstValueFrom(
      this.http.post<Category>(`${ADMIN_CATEGORIES_PATH}/${id}/activacion`, {}),
    );
  }

  async deactivateCategory(id: string): Promise<Category> {
    await this.ensureCsrfToken();
    return firstValueFrom(
      this.http.post<Category>(`${ADMIN_CATEGORIES_PATH}/${id}/desactivacion`, {}),
    );
  }

  listBranches(): Promise<readonly Branch[]> {
    return firstValueFrom(this.http.get<readonly Branch[]>(BRANCHES_PATH));
  }

  findTitle(id: string): Promise<Title> {
    return firstValueFrom(this.http.get<Title>(`${TITLES_PATH}/${id}`));
  }

  async registerTitle(draft: TitleDraft): Promise<Title> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.post<Title>(TITLES_PATH, bodyOf(draft)));
  }

  async updateTitle(id: string, draft: TitleDraft): Promise<Title> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.put<Title>(`${TITLES_PATH}/${id}`, bodyOf(draft)));
  }

  async withdrawTitle(id: string): Promise<Title> {
    await this.ensureCsrfToken();
    return firstValueFrom(this.http.post<Title>(`${TITLES_PATH}/${id}/baja`, {}));
  }

  private async ensureCsrfToken(): Promise<void> {
    if (this.csrfToken.getToken() === null) {
      await firstValueFrom(this.http.get(CSRF_TOKEN_SOURCE_PATH));
    }
  }
}

function bodyOf(draft: TitleDraft): Record<string, unknown> {
  const withCopies = draft.format === TitleFormat.physical && draft.barcodes.length > 0;

  return {
    title: draft.title.trim(),
    isbn: draft.isbn.trim(),
    year: draft.year,
    format: draft.format,
    category: draft.category,
    publisher: draft.publisher.trim(),
    authors: draft.authors,
    branch: withCopies ? draft.branch : null,
    barcodes: withCopies ? draft.barcodes : [],
  };
}
