import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
import {
  normalizarPerfilCoin,
  PERFIS_COIN,
  PerfilCoin
} from './perfil-coin';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly key = 'numerario_access_token';

  constructor(private http: HttpClient) {}

  login(login: string, senha: string) {
    return this.http.post<{ accessToken: string }>('/api/v1/auth/login', { login, senha })
      .pipe(tap(r => localStorage.setItem(this.key, r.accessToken)));
  }

  token() {
    return localStorage.getItem(this.key);
  }

  perfil(): PerfilCoin | null {
    const token = this.token();
    if (!token) {
      return null;
    }

    try {
      const payload = JSON.parse(atob(this.normalizarBase64Url(token.split('.')[1])));
      const roles = payload.roles || payload.authorities || payload.perfis || [];
      const roleText = Array.isArray(roles) ? roles.join(' ') : String(roles);
      const perfil = String(payload.perfil || payload.role || roleText).toUpperCase();
      const codigo = normalizarPerfilCoin(perfil);
      return codigo ? PERFIS_COIN[codigo] : null;
    } catch {
      return null;
    }
  }

  isGestor() {
    return this.perfil()?.codigo === 'COIN0001';
  }

  sair() {
    localStorage.removeItem(this.key);
  }

  private normalizarBase64Url(valor: string) {
    const base64 = valor.replace(/-/g, '+').replace(/_/g, '/');
    return base64.padEnd(base64.length + (4 - base64.length % 4) % 4, '=');
  }
}
