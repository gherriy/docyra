import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { LucideCamera, LucideSend, LucideX } from '@lucide/angular';
import { DocumentService } from '../../documents/document.service';

const MAX_PAGES = 20;

@Component({
  selector: 'app-scan-page',
  imports: [LucideCamera, LucideSend, LucideX],
  templateUrl: './scan-page.html',
  styleUrl: './scan-page.scss',
})
export class ScanPage implements OnDestroy {
  private readonly documentService = inject(DocumentService);

  readonly token = inject(ActivatedRoute).snapshot.queryParamMap.get('token');
  readonly maxPages = MAX_PAGES;
  readonly pages = signal<File[]>([]);
  readonly previews = signal<string[]>([]);
  readonly sending = signal(false);
  readonly sentCount = signal(0);
  readonly expired = signal(false);
  readonly error = signal<string | null>(null);

  onPhoto(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file || this.pages().length >= MAX_PAGES) {
      return;
    }
    this.error.set(null);
    this.pages.update((pages) => [...pages, file]);
    this.previews.update((urls) => [...urls, URL.createObjectURL(file)]);
  }

  removePage(index: number) {
    URL.revokeObjectURL(this.previews()[index]);
    this.pages.update((pages) => pages.filter((_, i) => i !== index));
    this.previews.update((urls) => urls.filter((_, i) => i !== index));
  }

  ngOnDestroy() {
    this.clearPages();
  }

  private clearPages() {
    this.previews().forEach((url) => URL.revokeObjectURL(url));
    this.previews.set([]);
    this.pages.set([]);
  }

  send() {
    const pages = this.pages();
    if (pages.length === 0 || !this.token) {
      return;
    }
    this.sending.set(true);
    this.error.set(null);
    this.documentService.upload(pages, this.token).subscribe({
      next: () => {
        this.clearPages();
        this.sentCount.update((n) => n + 1);
        this.sending.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.sending.set(false);
        if (err.status === 401) {
          this.expired.set(true);
        } else {
          this.error.set(err.error?.detail ?? "Le courrier n'a pas pu être envoyé. Réessayez.");
        }
      },
    });
  }
}
