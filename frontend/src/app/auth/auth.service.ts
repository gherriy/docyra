import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';

interface Session {
  token: string;
  expiresAt: string;
}

const STORAGE_KEY = 'docyra.session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly session = signal<Session | null>(this.readSession());

  readonly isLoggedIn = computed(() => {
    const session = this.session();
    return session !== null && new Date(session.expiresAt) > new Date();
  });

  token() {
    return this.isLoggedIn() ? this.session()!.token : null;
  }

  login(email: string, password: string) {
    return this.http.post<Session>('/api/auth/login', { email, password }).pipe(
      tap((session) => {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
        this.session.set(session);
      }),
    );
  }

  logout() {
    localStorage.removeItem(STORAGE_KEY);
    this.session.set(null);
    this.router.navigate(['/connexion']);
  }

  private readSession(): Session | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }
}