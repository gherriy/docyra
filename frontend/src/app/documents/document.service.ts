import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { DocumentSummary } from './document.model';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly http = inject(HttpClient);

  list() {
    return this.http.get<DocumentSummary[]>('/api/documents');
  }
}