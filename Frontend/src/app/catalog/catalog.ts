import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  afterNextRender,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { messages } from '../messages';
import { ReservationDesk } from '../reservations/reservation-desk';
import { CurrentSession } from '../session/current-session';
import { PermissionCode } from '../session/permission-code';
import { fillTemplate } from '../shared/fill-template';
import { RequestProblem, describeRequestProblem } from '../shared/request-errors';
import { CatalogSearch, FacetValue, FoundBook, SearchFilters, SearchResult } from './catalog-search';

const PAGE_SIZE = 20;
const MAX_PAGE = 100;
const DIGITAL_FORMAT = 'DIGITAL';
const NUMBER_FORMAT = new Intl.NumberFormat('es-CO');
const NO_FILTERS: SearchFilters = { text: '', author: '', category: '', publisher: '' };

interface FilterOption {
  readonly value: string;
  readonly label: string;
}

interface CardReservation {
  readonly status: 'reserving' | 'reserved' | 'failed';
  readonly message: string;
}

interface BookCard {
  readonly id: string;
  readonly title: string;
  readonly authors: string;
  readonly details: string;
  readonly isbn: string;
  readonly availability: string;
  readonly availabilityClass: string;
  readonly reservable: boolean;
  readonly reservation: CardReservation | null;
}

@Component({
  selector: 'app-catalog',
  imports: [ReactiveFormsModule, RouterLink, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './catalog.html',
  styleUrls: ['../shell/section-page.css', '../shared/feedback.css', './catalog.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Catalog {
  private readonly catalog = inject(CatalogSearch);
  private readonly reservationDesk = inject(ReservationDesk);
  private readonly currentSession = inject(CurrentSession);
  private readonly formBuilder = inject(NonNullableFormBuilder);
  private readonly reservations = signal<ReadonlyMap<string, CardReservation>>(new Map());
  private readonly textInput = viewChild<ElementRef<HTMLInputElement>>('textInput');
  private readonly resultsTop = viewChild<ElementRef<HTMLElement>>('resultsTop');
  private latestRequest = 0;

  protected readonly messages = messages.catalog;

  protected readonly form = this.formBuilder.group({
    text: [''],
    author: [''],
    category: [''],
    publisher: [''],
  });

  protected readonly result = signal<SearchResult | null>(null);
  protected readonly applied = signal<SearchFilters>(NO_FILTERS);
  protected readonly searching = signal(false);
  protected readonly problem = signal<RequestProblem | null>(null);

  protected readonly hasFilters = computed(() => {
    const filters = this.applied();
    return filters.author !== '' || filters.category !== '' || filters.publisher !== '';
  });

  protected readonly authorOptions = computed(() =>
    optionsOf(this.result()?.authors ?? [], this.applied().author),
  );

  protected readonly categoryOptions = computed(() =>
    optionsOf(this.result()?.categories ?? [], this.applied().category),
  );

  protected readonly publisherOptions = computed(() =>
    optionsOf(this.result()?.publishers ?? [], this.applied().publisher),
  );

  protected readonly canReserve = computed(() => this.currentSession.allows(PermissionCode.reserveBook));

  protected readonly cards = computed(() => {
    const reservations = this.reservations();
    return (this.result()?.books ?? []).map((book) => this.cardOf(book, reservations.get(book.id) ?? null));
  });

  protected readonly pages = computed(() =>
    Math.min(Math.ceil((this.result()?.found ?? 0) / PAGE_SIZE), MAX_PAGE),
  );

  protected readonly summary = computed(() => {
    const found = this.result()?.found ?? 0;
    return found === 1
      ? this.messages.foundOne
      : fillTemplate(this.messages.foundMany, { count: NUMBER_FORMAT.format(found) });
  });

  protected readonly pageStatus = computed(() =>
    fillTemplate(this.messages.pageOf, {
      page: String(this.result()?.page ?? 1),
      pages: String(this.pages()),
    }),
  );

  protected readonly traceText = computed(() => {
    const traceId = this.problem()?.traceId ?? '';
    return traceId === '' ? '' : fillTemplate(this.messages.traceId, { traceId });
  });

  constructor() {
    afterNextRender(() => this.textInput()?.nativeElement.focus());
    void this.search();
  }

  protected async search(page = 1): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const request = ++this.latestRequest;
    const filters = this.form.getRawValue();
    this.applied.set(filters);
    this.searching.set(true);
    this.problem.set(null);

    try {
      const result = await this.catalog.search(filters, page);
      if (request === this.latestRequest) {
        this.result.set(result);
      }
    } catch (error) {
      if (request === this.latestRequest) {
        this.result.set(null);
        this.problem.set(describeRequestProblem(error, this.messages.searchFailed));
      }
    } finally {
      if (request === this.latestRequest) {
        this.searching.set(false);
      }
    }
  }

  protected clearFilters(): void {
    this.form.patchValue({ author: '', category: '', publisher: '' });
    void this.search();
  }

  protected async goToPage(page: number): Promise<void> {
    await this.search(page);
    this.resultsTop()?.nativeElement.scrollIntoView({ block: 'start' });
  }

  protected async reserve(book: string): Promise<void> {
    const current = this.reservations().get(book);
    if (current?.status === 'reserving' || current?.status === 'reserved') {
      return;
    }

    this.setReservation(book, { status: 'reserving', message: '' });

    try {
      const reserved = await this.reservationDesk.reserve(book);
      this.setReservation(book, {
        status: 'reserved',
        message: fillTemplate(this.messages.reserved, { position: String(reserved.position) }),
      });
    } catch (error) {
      this.setReservation(book, {
        status: 'failed',
        message: describeRequestProblem(error, this.messages.reserveFailed).message,
      });
    }
  }

  private setReservation(book: string, reservation: CardReservation): void {
    this.reservations.update((current) => new Map(current).set(book, reservation));
  }

  private cardOf(book: FoundBook, reservation: CardReservation | null): BookCard {
    const format = book.format === DIGITAL_FORMAT ? this.messages.digital : this.messages.physical;
    const year = book.year > 0 ? String(book.year) : '';
    const tone =
      book.totalCopies === 0 ? 'neutral' : book.availableCopies > 0 ? 'success' : 'error';

    return {
      id: book.id,
      title: book.title,
      authors: book.authors.join(', '),
      details: [book.category, book.publisher, year, format].filter((part) => part !== '').join(' · '),
      isbn: book.isbn,
      availability:
        book.totalCopies === 0
          ? this.messages.noCopies
          : fillTemplate(this.messages.availability, {
              available: String(book.availableCopies),
              total: String(book.totalCopies),
            }),
      availabilityClass: `chip book__availability chip--${tone}`,
      reservable: this.canReserve() && book.totalCopies > 0 && book.availableCopies === 0,
      reservation,
    };
  }
}

function optionsOf(facet: readonly FacetValue[], selected: string): readonly FilterOption[] {
  const options = facet
    .filter((entry) => entry.value !== '')
    .map((entry) => ({ value: entry.value, label: `${entry.value} (${NUMBER_FORMAT.format(entry.count)})` }));

  return selected === '' || options.some((option) => option.value === selected)
    ? options
    : [{ value: selected, label: selected }, ...options];
}
