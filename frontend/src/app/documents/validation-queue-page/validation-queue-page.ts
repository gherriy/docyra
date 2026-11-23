import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { LucideChevronRight, LucideDownload, LucideFileText } from '@lucide/angular';
import { EMPTY, catchError, switchMap, timer } from 'rxjs';
import { DocumentStatus, DocumentSummary } from '../document.model';
import { DocumentService } from '../document.service';

const STATUS_LABELS: Record<DocumentStatus, string> = {
  PROCESSING: 'Lecture en cours',
  MATCHED: 'Patient trouvé',
  TO_REVIEW: 'À vérifier',
  VALIDATED: 'Validé',
  DOWNLOADED: 'Téléchargé',
};

@Component({
  selector: 'app-validation-queue-page',
  imports: [DatePipe, RouterLink, LucideChevronRight, LucideFileText, LucideDownload],
  templateUrl: './validation-queue-page.html',
  styleUrl: './validation-queue-page.scss',
})
export class ValidationQueuePage implements OnInit {
  private readonly documentService = inject(DocumentService);
  private readonly destroyRef = inject(DestroyRef);

  readonly documents = signal<DocumentSummary[]>([]);
  readonly loaded = signal(false);
  readonly error = signal(false);
  readonly downloading = signal(false);
  readonly validatedCount = computed(
    () => this.documents().filter((d) => d.status === 'VALIDATED').length,
  );

  ngOnInit() {
    timer(0, 5000)
      .pipe(
        switchMap(() =>
          this.documentService.list().pipe(
            catchError(() => {
              this.error.set(true);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((documents) => {
        this.documents.set(documents);
        this.loaded.set(true);
        this.error.set(false);
      });
  }

  statusLabel(status: DocumentStatus) {
    return STATUS_LABELS[status];
  }

  title(doc: DocumentSummary) {
    if (doc.fileName) {
      return doc.fileName;
    }
    return doc.status === 'PROCESSING' ? 'Patient en cours de recherche' : 'Patient à vérifier';
  }
   

  downloadZip() {
    this.downloading.set(true);
    this.documentService.downloadZip().subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `courriers-docyra-${new Date().toISOString().slice(0, 10)}.zip`;
        link.click();
        URL.revokeObjectURL(url);
        this.documents.update((docs) => docs.filter((d) => d.status !== 'VALIDATED'));
        this.downloading.set(false);
      },
      error: () => this.downloading.set(false),
    });
  }
}