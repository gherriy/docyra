import { DatePipe } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { LucideChevronRight, LucideFileText } from '@lucide/angular';
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
  imports: [DatePipe, RouterLink, LucideChevronRight, LucideFileText],
  templateUrl: './validation-queue-page.html',
  styleUrl: './validation-queue-page.scss',
})
export class ValidationQueuePage implements OnInit {
  private readonly documentService = inject(DocumentService);
  private readonly destroyRef = inject(DestroyRef);

  readonly documents = signal<DocumentSummary[]>([]);
  readonly loaded = signal(false);
  readonly error = signal(false);

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

  title(document: DocumentSummary) {
    if (document.fileName) {
      return document.fileName;
    }
    return document.status === 'PROCESSING' ? 'Patient en cours de recherche' : 'Patient à vérifier';
  }
}