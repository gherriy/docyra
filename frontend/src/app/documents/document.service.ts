import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { DocumentDetail, DocumentSummary } from './document.model';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly http = inject(HttpClient);

  list() {
    return this.http.get<DocumentSummary[]>('/api/documents');
  }

    get(id: number) {
    return this.http.get<DocumentDetail>(`/api/documents/${id}`);
  }

  assignPatient(id: number, patientId: number) {
    return this.http.put<DocumentDetail>(`/api/documents/${id}/patient`, { patientId });
  }
    validate(id: number) {
    return this.http.put<DocumentDetail>(`/api/documents/${id}/validate`, {});
  }

  downloadZip() {
    return this.http.get('/api/documents/zip', { responseType: 'blob' });
  }
    image(id: number) {
    return this.http.get(`/api/documents/${id}/image`, { responseType: 'blob' });
  }
}