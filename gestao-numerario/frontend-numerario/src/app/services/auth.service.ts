import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, of, tap, throwError } from 'rxjs';
import { normalizarPerfilCoin, PERFIS_COIN, PerfilCoin } from '../models/perfil-coin';

export interface SessaoUsuario {
  usuarioId: number;
  nome: string;
  perfil: string;
  expiraEm?: string;
}

interface RespostaAutenticacao extends SessaoUsuario {
  accessToken: string;
  tokenType: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private static readonly ACCESS_TOKEN = 'coin.accessToken';
  private static readonly SESSAO = 'coin.sessao';

  private readonly sessaoAtual = signal<SessaoUsuario | null>(this.lerSessao());

  constructor(private readonly http: HttpClient) {}

  login(login: string, senha: string): Observable<SessaoUsuario> {
    return this.http.post<RespostaAutenticacao>('/api/v1/auth/login', { login, senha }).pipe(
      tap(resposta => this.persistir(resposta))
    );
  }

  carregarSessao(): Observable<SessaoUsuario> {
    if (!this.accessToken()) {
      return throwError(() => new Error('Sessão ausente.'));
    }
    const sessao = this.sessaoAtual();
    return sessao ? of(sessao) : this.http.get<SessaoUsuario>('/api/v1/auth/me')
      .pipe(tap(resposta => this.persistirSessao(resposta)));
  }

  sair(): void {
    this.limparSessao();
  }

  autenticado(): boolean {
    return this.sessaoAtual() !== null && this.accessToken() !== null;
  }

  perfil(): PerfilCoin | null {
    const codigo = normalizarPerfilCoin(this.sessaoAtual()?.perfil ?? '');
    return codigo ? PERFIS_COIN[codigo] : null;
  }

  isGestor(): boolean {
    return this.perfil()?.codigo === 'COIN0001';
  }

  accessToken(): string | null {
    return localStorage.getItem(AuthService.ACCESS_TOKEN);
  }

  limparSessao(): void {
    this.sessaoAtual.set(null);
    localStorage.removeItem(AuthService.ACCESS_TOKEN);
    localStorage.removeItem(AuthService.SESSAO);
  }

  private persistir(resposta: RespostaAutenticacao): void {
    localStorage.setItem(AuthService.ACCESS_TOKEN, resposta.accessToken);
    this.persistirSessao({
      usuarioId: resposta.usuarioId,
      nome: resposta.nome,
      perfil: resposta.perfil,
      expiraEm: resposta.expiraEm
    });
  }

  private persistirSessao(sessao: SessaoUsuario): void {
    this.sessaoAtual.set(sessao);
    localStorage.setItem(AuthService.SESSAO, JSON.stringify(sessao));
  }

  private lerSessao(): SessaoUsuario | null {
    const sessao = localStorage.getItem(AuthService.SESSAO);
    if (!sessao) {
      return null;
    }
    try {
      return JSON.parse(sessao) as SessaoUsuario;
    } catch {
      localStorage.removeItem(AuthService.SESSAO);
      return null;
    }
  }
}
