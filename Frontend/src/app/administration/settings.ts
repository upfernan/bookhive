import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormArray,
  FormControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { messages } from '../messages';
import { fillTemplate } from '../shared/fill-template';
import { describeRequestProblem } from '../shared/request-errors';
import { AdministrationDesk, Branch } from './administration-desk';

const MAX_LEVELS = 6;
const MAX_LEVEL_NAME = 40;
const MAX_BRANCH_NAME = 100;
const MAX_FINE_RATE = 1_000_000;
const FINE_RATE_PATTERN = /^\d{1,7}([.,]\d{1,2})?$/;
const COMMON_TIME_ZONES = [
  'America/Bogota',
  'America/Lima',
  'America/Guayaquil',
  'America/Panama',
  'America/Mexico_City',
  'America/Caracas',
  'America/La_Paz',
  'America/Santiago',
  'America/Argentina/Buenos_Aires',
  'America/Sao_Paulo',
  'America/New_York',
  'Europe/Madrid',
];

/** Estado de cada bloque de la pantalla: se carga y se guarda por separado. */
class Block {
  readonly loading = signal(true);
  readonly loadProblem = signal('');
  readonly saving = signal(false);
  readonly saved = signal('');
  readonly saveProblem = signal('');

  async load(action: () => Promise<void>, fallback: string): Promise<void> {
    this.loading.set(true);
    this.loadProblem.set('');
    try {
      await action();
    } catch (error) {
      this.loadProblem.set(describeRequestProblem(error, fallback).message);
    } finally {
      this.loading.set(false);
    }
  }

  async save(action: () => Promise<string>, fallback: string): Promise<void> {
    if (this.saving()) {
      return;
    }
    this.saving.set(true);
    this.saved.set('');
    this.saveProblem.set('');
    try {
      this.saved.set(await action());
    } catch (error) {
      this.saveProblem.set(describeRequestProblem(error, fallback).message);
    } finally {
      this.saving.set(false);
    }
  }
}

@Component({
  selector: 'app-settings',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './settings.html',
  styleUrls: [
    '../shell/section-page.css',
    '../shared/feedback.css',
    '../shared/admin-page.css',
    '../shared/admin-details.css',
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Settings {
  private readonly desk = inject(AdministrationDesk);
  private readonly formBuilder = inject(NonNullableFormBuilder);

  protected readonly messages = messages.settings;
  protected readonly maxLevels = MAX_LEVELS;

  protected readonly rules = new Block();
  protected readonly branchesBlock = new Block();
  protected readonly levelsBlock = new Block();
  protected readonly retention = new Block();

  protected readonly rulesForm = this.formBuilder.group({
    loanDays: ['', [rangeValidator(1, 90)]],
    maxConcurrentLoans: ['', [rangeValidator(1, 20)]],
    dailyFineRate: ['', [fineRateValidator]],
    timeZone: ['America/Bogota', [Validators.required]],
  });

  protected readonly branchForm = this.formBuilder.group({
    name: ['', [Validators.required, Validators.maxLength(MAX_BRANCH_NAME)]],
  });

  protected readonly levelsForm = new FormArray<FormControl<string>>([]);
  protected readonly levelsInvalid = signal(false);

  protected readonly retentionForm = this.formBuilder.group({
    months: ['', [rangeValidator(1, 120)]],
  });

  protected readonly branches = signal<readonly Branch[]>([]);
  protected readonly timeZones = signal<readonly string[]>(COMMON_TIME_ZONES);
  protected readonly levelCount = signal(0);
  protected readonly canAddLevel = computed(() => this.levelCount() < MAX_LEVELS);

  constructor() {
    void this.loadRules();
    void this.loadBranches();
    void this.loadLevels();
    void this.loadRetention();
  }

  protected loadRules(): Promise<void> {
    return this.rules.load(async () => {
      const settings = await this.desk.findSettings();
      this.includeTimeZone(settings.timeZone);
      this.rulesForm.reset({
        loanDays: positive(settings.loanDays),
        maxConcurrentLoans: positive(settings.maxConcurrentLoans),
        dailyFineRate: String(settings.dailyFineRate ?? 0),
        timeZone: settings.timeZone || 'America/Bogota',
      });
    }, this.messages.loadFailed);
  }

  protected loadBranches(): Promise<void> {
    return this.branchesBlock.load(async () => {
      this.branches.set(sortedBranches(await this.desk.listBranches()));
    }, this.messages.loadFailed);
  }

  protected loadLevels(): Promise<void> {
    return this.levelsBlock.load(async () => {
      const levels = await this.desk.listLevels();
      this.setLevels(
        [...levels].sort((first, second) => first.depth - second.depth).map((level) => level.name),
      );
    }, this.messages.loadFailed);
  }

  protected loadRetention(): Promise<void> {
    return this.retention.load(async () => {
      const retention = await this.desk.findRetention();
      this.retentionForm.reset({ months: positive(retention.months) });
    }, this.messages.loadFailed);
  }

  protected errorOf(control: AbstractControl, text: string): string {
    return control.touched && control.invalid ? text : '';
  }

  protected levelLabel(index: number): string {
    return fillTemplate(this.messages.levelLabel, { depth: String(index + 1) });
  }

  protected saveRules(): Promise<void> {
    if (this.rulesForm.invalid) {
      this.rulesForm.markAllAsTouched();
      return Promise.resolve();
    }
    const value = this.rulesForm.getRawValue();
    return this.rules.save(async () => {
      const saved = await this.desk.saveSettings({
        loanDays: Number(value.loanDays),
        maxConcurrentLoans: Number(value.maxConcurrentLoans),
        dailyFineRate: Number(value.dailyFineRate.replace(',', '.')),
        timeZone: value.timeZone,
      });
      this.rulesForm.reset({
        loanDays: positive(saved.loanDays),
        maxConcurrentLoans: positive(saved.maxConcurrentLoans),
        dailyFineRate: String(saved.dailyFineRate ?? 0),
        timeZone: saved.timeZone,
      });
      return this.messages.saved;
    }, this.messages.saveFailed);
  }

  protected createBranch(): Promise<void> {
    if (this.branchForm.invalid) {
      this.branchForm.markAllAsTouched();
      return Promise.resolve();
    }
    return this.branchesBlock.save(async () => {
      const branch = await this.desk.createBranch(this.branchForm.getRawValue().name);
      this.branches.update((current) => sortedBranches([...current, branch]));
      this.branchForm.reset();
      return fillTemplate(this.messages.branchCreated, { name: branch.name });
    }, this.messages.saveFailed);
  }

  protected branchNameError(): string {
    const control = this.branchForm.controls.name;
    if (!control.touched || control.valid) {
      return '';
    }
    return control.hasError('maxlength')
      ? this.messages.branchNameTooLong
      : this.messages.branchNameRequired;
  }

  protected addLevel(): void {
    if (this.canAddLevel()) {
      this.levelsForm.push(new FormControl('', { nonNullable: true }));
      this.levelCount.set(this.levelsForm.length);
    }
  }

  protected removeLevel(index: number): void {
    this.levelsForm.removeAt(index);
    this.levelCount.set(this.levelsForm.length);
    this.levelsInvalid.set(false);
  }

  protected saveLevels(): Promise<void> {
    const names = this.levelsForm.getRawValue().map((name) => name.trim());
    const unique = new Set(names.map((name) => name.toLocaleLowerCase('es')));
    const valid =
      names.length > 0 &&
      names.length <= MAX_LEVELS &&
      names.every((name) => name !== '' && name.length <= MAX_LEVEL_NAME) &&
      unique.size === names.length;
    this.levelsInvalid.set(!valid);

    if (!valid) {
      return Promise.resolve();
    }
    return this.levelsBlock.save(async () => {
      const levels = await this.desk.saveLevels(names);
      this.setLevels(
        [...levels].sort((first, second) => first.depth - second.depth).map((level) => level.name),
      );
      return this.messages.saved;
    }, this.messages.saveFailed);
  }

  protected saveRetention(): Promise<void> {
    if (this.retentionForm.invalid) {
      this.retentionForm.markAllAsTouched();
      return Promise.resolve();
    }
    return this.retention.save(async () => {
      const saved = await this.desk.saveRetention(Number(this.retentionForm.getRawValue().months));
      this.retentionForm.reset({ months: positive(saved.months) });
      return this.messages.saved;
    }, this.messages.saveFailed);
  }

  private setLevels(names: readonly string[]): void {
    this.levelsForm.clear();
    for (const name of names.length > 0 ? names : ['']) {
      this.levelsForm.push(new FormControl(name, { nonNullable: true }));
    }
    this.levelCount.set(this.levelsForm.length);
    this.levelsInvalid.set(false);
  }

  private includeTimeZone(zone: string): void {
    if (zone && !this.timeZones().includes(zone)) {
      this.timeZones.update((current) => [zone, ...current]);
    }
  }
}

function positive(value: number): string {
  return value > 0 ? String(value) : '';
}

function sortedBranches(branches: readonly Branch[]): readonly Branch[] {
  return [...branches].sort((first, second) => first.name.localeCompare(second.name, 'es'));
}

function rangeValidator(min: number, max: number) {
  return (control: AbstractControl<string>): ValidationErrors | null => {
    const text = (control.value ?? '').trim();
    const value = Number(text);
    return text !== '' && Number.isInteger(value) && value >= min && value <= max
      ? null
      : { range: true };
  };
}

function fineRateValidator(control: AbstractControl<string>): ValidationErrors | null {
  const text = (control.value ?? '').trim();
  if (!FINE_RATE_PATTERN.test(text)) {
    return { fineRate: true };
  }
  return Number(text.replace(',', '.')) <= MAX_FINE_RATE ? null : { fineRate: true };
}
