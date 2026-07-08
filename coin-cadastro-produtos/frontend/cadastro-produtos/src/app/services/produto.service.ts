import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/PageResponse';
import { ProdutoAlterarDtoRequest } from '../models/ProdutoAlterarDtoRequest';
import { ProdutoDtoRequest } from '../models/ProdutoDtoRequest';
import { ProdutoDtoResponse } from '../models/ProdutoDtoResponse';

@Injectable({
  providedIn: 'root'
})
export class ProdutoService {
  private readonly apiUrl = 'http://localhost:8081/api/bff/produtos';

  constructor(private http: HttpClient) {}

  cadastrar(produto: ProdutoDtoRequest): Observable<ProdutoDtoResponse> {
    return this.http.post<ProdutoDtoResponse>(this.apiUrl, produto);
  }

  alterar(id: number, produto: ProdutoAlterarDtoRequest): Observable<ProdutoDtoResponse> {
    return this.http.put<ProdutoDtoResponse>(`${this.apiUrl}/${id}`, produto);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorId(id: number): Observable<ProdutoDtoResponse> {
    return this.http.get<ProdutoDtoResponse>(`${this.apiUrl}/${id}`);
  }

  listar(filtros: {
    page: number;
    size: number;
    busca?: string;
    status?: string;
    precoMinimo?: number | null;
    precoMaximo?: number | null;
    sort?: string;
  }): Observable<PageResponse<ProdutoDtoResponse>> {
    let params = new HttpParams()
      .set('page', filtros.page)
      .set('size', filtros.size)
      .set('sort', filtros.sort || 'id,asc');

    if (filtros.busca) {
      params = params.set('busca', filtros.busca);
    }

    if (filtros.status) {
      params = params.set('status', filtros.status);
    }

    if (filtros.precoMinimo !== null && filtros.precoMinimo !== undefined) {
      params = params.set('precoMinimo', filtros.precoMinimo);
    }

    if (filtros.precoMaximo !== null && filtros.precoMaximo !== undefined) {
      params = params.set('precoMaximo', filtros.precoMaximo);
    }

    return this.http.get<PageResponse<ProdutoDtoResponse>>(this.apiUrl, { params });
  }
}
