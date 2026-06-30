import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PageResponse } from '../models/PageResponse';
import { ProdutoAlterarDTO } from '../models/ProdutoAlterarDTO';
import { ProdutoRequestDTO } from '../models/ProdutoRequestDTO';
import { ProdutoResponseDTO } from '../models/ProdutoResponseDTO';

@Injectable({
  providedIn: 'root'
})
export class ProdutoService {
  private readonly apiUrl = 'http://localhost:8080/produtos';

  constructor(private http: HttpClient) {}

  cadastrar(produto: ProdutoRequestDTO): Observable<ProdutoResponseDTO> {
    return this.http.post<ProdutoResponseDTO>(this.apiUrl, produto);
  }

  alterar(id: number, produto: ProdutoAlterarDTO): Observable<ProdutoResponseDTO> {
    return this.http.put<ProdutoResponseDTO>(`${this.apiUrl}/${id}`, produto);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorId(id: number): Observable<ProdutoResponseDTO> {
    return this.http.get<ProdutoResponseDTO>(`${this.apiUrl}/${id}`);
  }

  listar(filtros: {
    page: number;
    size: number;
    busca?: string;
    status?: string;
    precoMinimo?: number | null;
    precoMaximo?: number | null;
    sort?: string;
  }): Observable<PageResponse<ProdutoResponseDTO>> {
    let params = new HttpParams()
      .set('page', filtros.page)
      .set('size', filtros.size)
      .set('sort', filtros.sort || 'nome,asc');

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

    return this.http.get<PageResponse<ProdutoResponseDTO>>(this.apiUrl, { params });
  }
}
