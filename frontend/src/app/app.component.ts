import { Component, DestroyRef, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize } from 'rxjs';
import { Priority, RequestApi, RequestStatus, ServiceRequest } from './request-api.service';

@Component({
  selector: 'app-root',
  imports: [DatePipe, FormsModule, ReactiveFormsModule],
  templateUrl: './app.component.html'
})
export class AppComponent {
  private readonly api = inject(RequestApi);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(NonNullableFormBuilder);
  readonly requests = signal<ServiceRequest[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly updating = signal<string | null>(null);
  readonly error = signal('');
  readonly notice = signal('');
  readonly total = signal(0);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly selected = signal<ServiceRequest | null>(null);
  search = '';
  status: RequestStatus | '' = '';
  readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(120), Validators.pattern(/\S/)]],
    description: ['', [Validators.required, Validators.maxLength(4000), Validators.pattern(/\S/)]],
    priority: this.fb.control<Priority>('MEDIUM', Validators.required)
  });

  constructor() { this.load(); }

  load(page = 0) {
    if (this.loading()) return;
    this.loading.set(true);
    this.error.set('');
    this.api.list(this.status, this.search, page).pipe(
      takeUntilDestroyed(this.destroyRef), finalize(() => this.loading.set(false))
    ).subscribe({
      next: result => {
        this.requests.set(result.content);
        this.total.set(result.totalElements);
        this.page.set(result.page);
        this.totalPages.set(result.totalPages);
      },
      error: () => this.error.set('Could not load requests. Check the connection and try again.')
    });
  }

  create() {
    if (this.saving()) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    this.error.set('');
    this.notice.set('');
    this.api.create(this.form.getRawValue()).pipe(
      takeUntilDestroyed(this.destroyRef), finalize(() => this.saving.set(false))
    ).subscribe({
      next: () => {
        this.form.reset({ title: '', description: '', priority: 'MEDIUM' });
        this.notice.set('Request created.');
        this.search = '';
        this.status = '';
        this.load();
      },
      error: () => this.error.set('Could not create the request. Your draft has been kept; try again.')
    });
  }

  changeStatus(request: ServiceRequest, event: Event) {
    const select = event.target as HTMLSelectElement;
    const status = select.value as RequestStatus;
    if (this.updating()) { select.value = request.status; return; }
    this.updating.set(request.id);
    this.error.set('');
    this.api.updateStatus(request.id, status).pipe(
      takeUntilDestroyed(this.destroyRef), finalize(() => this.updating.set(null))
    ).subscribe({
      next: updated => {
        if (this.selected()?.id === updated.id) this.selected.set(updated);
        this.notice.set('Status updated.');
        this.load(this.page());
      },
      error: () => {
        select.value = request.status;
        this.error.set('Could not update the status. Refresh and try again.');
      }
    });
  }

  label(value: string) { return value.toLowerCase().replaceAll('_', ' '); }
}
