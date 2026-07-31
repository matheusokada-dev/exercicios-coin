import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {
  Agencia,
  AtualizarAgenciaRequest,
  CriarAgenciaRequest,
  DetalheAgencia,
  PaginaResponse
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AgenciasService {
  constructor(private readonly http: HttpClient) {}

  listar(busca: string, alerta: string, ordenarPor: string, direcao: string, pagina: number, tamanho: number) {
    let params = new HttpParams()
      .set('pagina', pagina)
      .set('tamanho', tamanho)
      .set('ordenarPor', ordenarPor)
      .set('direcao', direcao);

    if (busca) {
      params = params.set('busca', busca);
    }
    if (alerta) {
      params = params.set('alerta', alerta);
    }

    return this.http.get<PaginaResponse<Agencia>>('/api/v1/agencias', { params });
  }

  detalhar(id: number) {
    return this.http.get<DetalheAgencia>(`/api/v1/agencias/${id}/detalhe`);
  }

  criar(request: CriarAgenciaRequest) {
    return this.http.post<Agencia>('/api/v1/agencias', request);
  }

  atualizar(id: number, request: AtualizarAgenciaRequest) {
    return this.http.put<Agencia>(`/api/v1/agencias/${id}`, request);
  }

  desativar(id: number) {
    return this.http.delete<void>(`/api/v1/agencias/${id}`);
  }
}
