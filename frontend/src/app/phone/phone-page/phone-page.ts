import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { toDataURL } from 'qrcode';
import { DocumentService } from '../../documents/document.service';
import { ScanSession, ScanSessionService } from '../scan-session.service';

@Component({
  selector: 'app-phone-page',
  imports: [DatePipe, RouterLink],
  templateUrl: './phone-page.html',
  styleUrl: './phone-page.scss',
})
export class PhonePage implements OnInit {
  private readonly scanSessionService = inject(ScanSessionService);
  private readonly documentService = inject(DocumentService);

  readonly session = signal<ScanSession | null>(null);
  readonly qrCode = signal<string | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly uploadMessage = signal<string | null>(null);

  ngOnInit() {
    const current = this.scanSessionService.current();
    if (current) {
      this.show(current);
    } else {
      this.generate();
    }
  }

  generate() {
    this.loading.set(true);
    this.error.set(null);
    this.scanSessionService.create().subscribe({
      next: (session) => this.show(session),
      error: () => {
        this.error.set("Le QR code n'a pas pu être créé. Réessayez.");
        this.loading.set(false);
      },
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) {
      return;
    }
    this.uploadMessage.set('Envoi en cours…');
    this.documentService.upload(file).subscribe({
      next: () => this.uploadMessage.set(`${file.name} a été ajouté à « À valider ».`),
      error: (err) => this.uploadMessage.set(err.error?.detail ?? "L'image n'a pas pu être envoyée."),
    });
  }

  private async show(session: ScanSession) {
    this.session.set(session);
    this.qrCode.set(await toDataURL(session.scanUrl, { width: 320, margin: 1, color: { dark: '#1d1033', light: '#ffffff' } }));
    this.loading.set(false);
  }
}