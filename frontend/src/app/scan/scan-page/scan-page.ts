import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LucideCamera } from '@lucide/angular';
import { DocumentService } from '../../documents/document.service';

@Component({
  selector: 'app-scan-page',
  imports: [LucideCamera],
  templateUrl: './scan-page.html',
  styleUrl: './scan-page.scss',
})
export class ScanPage {
  private readonly documentService = inject(DocumentService);

  readonly token = inject(ActivatedRoute).snapshot.queryParamMap.get('token');
  readonly sending = signal(false);
  readonly sentCount = signal(0);
  readonly expired = signal(false);
  readonly error = signal<string | null>(null);

  onPhoto(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file || !this.token) {
      return;
    }
    this.sending.set(true);
    this.error.set(null);
    this.documentService.upload(file, this.token).subscribe({
      next: () => {
        this.sentCount.update((n) => n + 1);
        this.sending.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.sending.set(false);
        if (err.status === 401) {
          this.expired.set(true);
        } else {
          this.error.set(err.error?.detail ?? "La photo n'a pas pu être envoyée. Réessayez.");
        }
      },
    });
  }
}