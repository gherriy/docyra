import { Component, inject, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { PatientService } from '../patient.service';
import { Patient } from '../patient.model';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-add-patient-form',
  styleUrl: './add-patient-form.scss',
  templateUrl: './add-patient-form.html',
})
export class AddPatientForm {
  private readonly patientService = inject(PatientService);

  readonly created = output<Patient>();
  readonly cancelled = output<void>();

  readonly form = inject(FormBuilder).nonNullable.group({
    lastName: ['', Validators.required],
    firstName: ['', Validators.required],
  });
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    const { lastName, firstName } = this.form.getRawValue();
    this.patientService.create(lastName, firstName).subscribe({
      next: (patient) => {
        this.saving.set(false);
        this.form.reset();
        this.created.emit(patient);
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(err.error?.detail ?? "Le patient n'a pas pu être ajouté.");
      },
    });
  }
}
