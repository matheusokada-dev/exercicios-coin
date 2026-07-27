import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Agencia, PaginaResponse } from '../models/api.models';

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
}
