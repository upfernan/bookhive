import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

const BOOKS_PATH = '/api/v1/libros';

export interface FoundBook {
  readonly id: string;
  readonly title: string;
  readonly authors: readonly string[];
  readonly category: string;
  readonly publisher: string;
  readonly format: string;
  readonly year: number;
  readonly isbn: string;
  readonly availableCopies: number;
  readonly totalCopies: number;
}

export interface FacetValue {
  readonly value: string;
  readonly count: number;
}

export interface SearchResult {
  readonly found: number;
  readonly page: number;
  readonly books: readonly FoundBook[];
  readonly authors: readonly FacetValue[];
  readonly categories: readonly FacetValue[];
  readonly publishers: readonly FacetValue[];
}

export interface SearchFilters {
  readonly text: string;
  readonly author: string;
  readonly category: string;
  readonly publisher: string;
}

@Injectable({ providedIn: 'root' })
export class CatalogSearch {
  private readonly http = inject(HttpClient);

  search(filters: SearchFilters, page: number): Promise<SearchResult> {
    const parameters: Record<string, string> = {
      texto: filters.text.trim(),
      autor: filters.author,
      categoria: filters.category,
      editorial: filters.publisher,
      pagina: String(page),
    };

    let params = new HttpParams();
    for (const [name, value] of Object.entries(parameters)) {
      if (value !== '') {
        params = params.set(name, value);
      }
    }

    return firstValueFrom(this.http.get<SearchResult>(BOOKS_PATH, { params }));
  }
}
