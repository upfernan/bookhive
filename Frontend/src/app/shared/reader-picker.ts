import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  computed,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule, MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { messages } from '../messages';
import { FoundReader, ReaderDirectory } from './reader-directory';
import { describeRequestProblem } from './request-errors';

const MIN_LENGTH = 3;
const MAX_LENGTH = 100;
const TYPING_PAUSE_MS = 250;

type SearchState = 'idle' | 'searching' | 'found' | 'picked' | 'failed';

@Component({
  selector: 'app-reader-picker',
  imports: [ReactiveFormsModule, MatAutocompleteModule, MatFormFieldModule, MatInputModule],
  templateUrl: './reader-picker.html',
  styleUrl: './reader-picker.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReaderPicker {
  private readonly directory = inject(ReaderDirectory);
  private readonly readerInput = viewChild<ElementRef<HTMLInputElement>>('readerInput');
  private latestRequest = 0;
  private pause: ReturnType<typeof setTimeout> | undefined;

  readonly label = input.required<string>();
  readonly picked = output<FoundReader>();

  protected readonly messages = messages.readerPicker;
  protected readonly maxLength = MAX_LENGTH;
  protected readonly control = new FormControl<string | FoundReader>('', { nonNullable: true });
  protected readonly readers = signal<readonly FoundReader[]>([]);
  protected readonly state = signal<SearchState>('idle');
  protected readonly problem = signal('');

  protected readonly hint = computed(() => {
    switch (this.state()) {
      case 'searching':
        return this.messages.searching;
      case 'found':
        return this.readers().length === 0 ? this.messages.noResults : this.messages.pick;
      case 'picked':
      case 'failed':
        return '';
      default:
        return this.messages.hint;
    }
  });

  protected readonly showName = (value: string | FoundReader | null): string =>
    typeof value === 'string' ? value : (value?.name ?? '');

  constructor() {
    this.control.valueChanges.pipe(takeUntilDestroyed()).subscribe((value) => this.typed(value));
    inject(DestroyRef).onDestroy(() => clearTimeout(this.pause));
  }

  clear(): void {
    clearTimeout(this.pause);
    this.latestRequest++;
    this.control.setValue('', { emitEvent: false });
    this.readers.set([]);
    this.problem.set('');
    this.state.set('idle');
    this.focus();
  }

  focus(): void {
    setTimeout(() => this.readerInput()?.nativeElement.focus());
  }

  protected choose(event: MatAutocompleteSelectedEvent): void {
    const reader = event.option.value as FoundReader;
    this.state.set('picked');
    this.picked.emit(reader);
  }

  private typed(value: string | FoundReader): void {
    clearTimeout(this.pause);

    if (typeof value !== 'string') {
      return;
    }

    const text = value.trim();
    const request = ++this.latestRequest;
    this.problem.set('');

    if (text.length < MIN_LENGTH) {
      this.readers.set([]);
      this.state.set('idle');
      return;
    }

    this.state.set('searching');
    this.pause = setTimeout(() => void this.search(text, request), TYPING_PAUSE_MS);
  }

  private async search(text: string, request: number): Promise<void> {
    try {
      const found = await this.directory.search(text);
      if (request === this.latestRequest) {
        this.readers.set(found);
        this.state.set('found');
      }
    } catch (error) {
      if (request === this.latestRequest) {
        this.readers.set([]);
        this.problem.set(describeRequestProblem(error, this.messages.failed).message);
        this.state.set('failed');
      }
    }
  }
}
