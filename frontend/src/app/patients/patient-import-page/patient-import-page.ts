import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { LucideCheck, LucideSearch, LucideUpload } from '@lucide/angular';
import { ImportResult, Patient } from '../patient.model';
import { PatientService } from '../patient.service';
import { AddPatientForm } from '../add-patient-form/add-patient-form';
@Component({
  selector: 'app-patient-import-page',
imports: [LucideCheck, LucideSearch, LucideUpload, AddPatientForm],
  templateUrl: './patient-import-page.html',
  styleUrl: './patient-import-page.scss',
})
export class PatientImportPage implements OnInit {
  private readonly patientService = inject(PatientService);

  readonly patients = signal<Patient[]>([]);
  readonly search = signal('');
  readonly result = signal<ImportResult | null>(null);
  readonly importedFileName = signal('');
  readonly error = signal<string | null>(null);
  readonly loading = signal(false);
  readonly adding = signal(false);
  readonly addedName = signal<string | null>(null);

  readonly filteredPatients = computed(() => {
    const term = this.search().trim().toLowerCase();
    return this.patients().filter((p) =>
      `${p.lastName} ${p.firstName}`.toLowerCase().includes(term),
    );
  });

  ngOnInit() {
    this.loadPatients();
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) {
      return;
    }

    this.result.set(null);
    this.error.set(null);
    this.importedFileName.set(file.name);
    this.loading.set(true);

    this.patientService.import(file).subscribe({
      next: (result) => {
        this.result.set(result);
        this.loading.set(false);
        this.loadPatients();
      },
      error: (err) => {
        this.error.set(err.error?.detail ?? "L'import a échoué");
        this.loading.set(false);
      },
    });
  }

  onSearch(event: Event) {
    this.search.set((event.target as HTMLInputElement).value);
  }

  private loadPatients() {
    this.patientService.list().subscribe((patients) => this.patients.set(patients));
  }
  onPatientAdded(patient: Patient) {
    this.adding.set(false);
    this.addedName.set(`${patient.lastName} ${patient.firstName}`);
    this.loadPatients();
  }
}