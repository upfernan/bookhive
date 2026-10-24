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
import { messages } from '../messages';
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

interface BookCard {
  readonly id: string;
  readonly title: string;
  readonly authors: string;
  readonly details: string;
  readonly isbn: string;
  readonly availability: string;
  readonly availabilityClass: string;
}

@Component({
  selector: 'app-catalog',
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './catalog.html',
  styleUrls: ['../shell/section-page.css', '../shared/feedback.css', './catalog.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Catalog {
  private readonly catalog = inject(CatalogSearch);
  private readonly formBuilder = inject(NonNullableFormBuilder);
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

  protected readonly cards = computed(() => (this.result()?.books ?? []).map((book) => this.cardOf(book)));

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

  private cardOf(book: FoundBook): BookCard {
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
