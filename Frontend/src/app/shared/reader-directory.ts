import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';

const READER_SEARCH_PATH = '/api/v1/usuarios/busqueda';

export interface FoundReader {
  readonly id: string;
  readonly name: string;
  readonly email: string;
}

@Injectable({ providedIn: 'root' })
export class ReaderDirectory {
  private readonly http = inject(HttpClient);

  search(text: string): Promise<readonly FoundReader[]> {
    const params = new HttpParams().set('texto', text.trim());
    return firstValueFrom(this.http.get<readonly FoundReader[]>(READER_SEARCH_PATH, { params }));
  }
}
