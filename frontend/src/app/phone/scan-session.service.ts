import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { tap } from 'rxjs';

export interface ScanSession {
  token: string;
  scanUrl: string;
  expiresAt: string;
}

export const SCAN_SESSION_KEY = 'docyra.scan-session';

@Injectable({ providedIn: 'root' })
export class ScanSessionService {
  private readonly http = inject(HttpClient);

  current(): ScanSession | null {
    try {
      const raw = localStorage.getItem(SCAN_SESSION_KEY);
      const session: ScanSession | null = raw ? JSON.parse(raw) : null;
      return session && new Date(session.expiresAt) > new Date() ? session : null;
    } catch {
      return null;
    }
  }

  create() {
    return this.http
      .post<ScanSession>('/api/scan-sessions', {})
      .pipe(tap((session) => localStorage.setItem(SCAN_SESSION_KEY, JSON.stringify(session))));
  }
}