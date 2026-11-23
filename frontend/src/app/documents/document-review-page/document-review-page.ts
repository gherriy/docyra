import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LucideCheck, LucideSearch, LucideTriangleAlert } from '@lucide/angular';
import { Patient } from '../../patients/patient.model';
import { PatientService } from '../../patients/patient.service';
import { DocumentDetail, DocumentSummary } from '../document.model';
import { DocumentService } from '../document.service';
import { fileNameFor } from '../file-name';
import { of, switchMap } from 'rxjs';

@Component({
  selector: 'app-document-review-page',
  imports: [RouterLink, LucideCheck, LucideSearch, LucideTriangleAlert],
  templateUrl: './document-review-page.html',
  styleUrl: './document-review-page.scss',
})
export class DocumentReviewPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly documentService = inject(DocumentService);
  private readonly patientService = inject(PatientService);

  readonly document = signal<DocumentDetail | null>(null);
  readonly queue = signal<DocumentSummary[]>([]);
  readonly patients = signal<Patient[]>([]);
  readonly selectedId = signal<number | null>(null);
  readonly changing = signal(false);
  readonly search = signal('');
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly imageUrl = computed(() => `/api/documents/${this.document()?.id}/image`);

  readonly found = computed(() => this.document()?.status === 'MATCHED' && !this.changing());

  readonly selectedPatient = computed(() => {
    const id = this.selectedId();
    const all = [...(this.document()?.candidates ?? []), ...this.patients()];
    return all.find((p) => p.id === id) ?? null;
  });

  readonly fileName = computed(() => {
    const patient = this.selectedPatient();
    return patient ? fileNameFor(patient) : null;
  });

  readonly searchResults = computed(() => {
    const term = this.search().trim().toLowerCase();
    if (term.length < 2) {
      return [];
    }
    const candidateIds = new Set(this.document()?.candidates.map((p) => p.id));
    return this.patients()
      .filter((p) => !candidateIds.has(p.id))
      .filter((p) => `${p.lastName} ${p.firstName}`.toLowerCase().includes(term))
      .slice(0, 6);
  });

  readonly position = computed(() => {
    const index = this.queue().findIndex((d) => d.id === this.document()?.id);
    return { current: index + 1, total: this.queue().length };
  });

  ngOnInit() {
    this.patientService.list().subscribe((patients) => this.patients.set(patients));
    this.route.paramMap.subscribe((params) => this.load(Number(params.get('id'))));
  }

  select(patient: Patient) {
    this.selectedId.set(patient.id);
  }

  onSearch(event: Event) {
    this.search.set((event.target as HTMLInputElement).value);
  }

  validateAndNext() {
    const document = this.document();
    const patientId = this.selectedId();
    if (!document || !patientId) {
      return;
    }
    this.saving.set(true);
    const assign$ = patientId === document.patient?.id
      ? of(document)
      : this.documentService.assignPatient(document.id, patientId);

    assign$
      .pipe(switchMap(() => this.documentService.validate(document.id)))
      .subscribe({
        next: () => this.goToNext(),
        error: (err) => {
          this.error.set(err.error?.detail ?? "Le courrier n'a pas pu être validé");
          this.saving.set(false);
        },
      });
  }

  private load(id: number) {
    this.changing.set(false);
    this.search.set('');
    this.error.set(null);
    this.saving.set(false);

    this.documentService.list().subscribe((queue) => this.queue.set(queue));
    this.documentService.get(id).subscribe({
      next: (document) => {
        this.document.set(document);
        this.selectedId.set(document.patient?.id ?? document.candidates[0]?.id ?? null);
      },
      error: () => this.error.set('Courrier introuvable'),
    });
  }

  private goToNext() {
    const queue = this.queue();
    const index = queue.findIndex((d) => d.id === this.document()?.id);
    const next = queue.slice(index + 1).find((d) => d.status !== 'VALIDATED');
    this.router.navigate(next ? ['/a-valider', next.id] : ['/a-valider']);
  }
}