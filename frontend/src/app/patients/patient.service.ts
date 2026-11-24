import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ImportResult, Patient } from './patient.model';

@Injectable({ providedIn: 'root' })
export class PatientService {
  private readonly http = inject(HttpClient);

  list() {
    return this.http.get<Patient[]>('/api/patients');
  }

  import(file: File) {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<ImportResult>('/api/patients/import', form);
  }
    create(lastName: string, firstName: string) {
    return this.http.post<Patient>('/api/patients', { lastName, firstName });
  }
}