import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
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

  alterar(id: number, produto: ProdutoRequestDTO): Observable<ProdutoResponseDTO> {
    return this.http.put<ProdutoResponseDTO>(`${this.apiUrl}/${id}`, produto);
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  buscarPorId(id: number): Observable<ProdutoResponseDTO> {
    return this.http.get<ProdutoResponseDTO>(`${this.apiUrl}/${id}`);
  }

  listar(): Observable<ProdutoResponseDTO[]> {
    return this.http.get<ProdutoResponseDTO[]>(this.apiUrl);
  }

  listarInativos(): Observable<ProdutoResponseDTO[]>{
    return this.http.get<ProdutoResponseDTO[]>(`${this.apiUrl}/inativos`)
  }
}