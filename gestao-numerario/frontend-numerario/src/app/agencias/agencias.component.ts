import { Component, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { AgenciasService } from './agencias.service';
import { PageBackComponent } from '../shared/page-back/page-back.component';

@Component({
  selector: 'app-agencias',
  imports: [CurrencyPipe, FormsModule, RouterLink, PageBackComponent],
  template: `
    <div class="page-navigation">
      <p class="crumb"><a routerLink="/menu">COIN Home</a> / <a routerLink="/tesouraria">Tesouraria</a> / Agências</p>
      <app-page-back to="/tesouraria" />
    </div>
    <h1>Agências</h1>

    @if (!isGestor) {
      <p class="form-error">A consulta e manutencao de agencias exige perfil gestor.</p>
    }

    <section class="filters">
      <input [(ngModel)]="busca" placeholder="Codigo, nome ou cidade">
      <select [(ngModel)]="alerta">
        <option value="">Status</option>
        <option value="true">Em alerta</option>
        <option value="false">OK</option>
      </select>
      <select [(ngModel)]="ordenarPor">
        <option value="CODIGO">Codigo</option>
        <option value="NOME">Nome</option>
        <option value="CIDADE">Cidade</option>
        <option value="SALDO_ATUAL">Saldo</option>
      </select>
      <select [(ngModel)]="direcao">
        <option value="ASC">Ascendente</option>
        <option value="DESC">Descendente</option>
      </select>
      <button (click)="pesquisar()">Pesquisar</button>
      <button class="outline" (click)="limpar()">Limpar</button>
    </section>

    @if (isGestor) {
      <section class="form">
        <input [(ngModel)]="nova.codigo" placeholder="Codigo *">
        <input [(ngModel)]="nova.nome" placeholder="Nome *">
        <input [(ngModel)]="nova.cidade" placeholder="Cidade *">
        <input [(ngModel)]="nova.saldoAtual" type="number" min="0" step="0.01" placeholder="Saldo inicial *">
        <input [(ngModel)]="nova.limiteMinimo" type="number" min="0" step="0.01" placeholder="Limite minimo *">
        <button (click)="criar()" [disabled]="salvando">Nova agencia +</button>
      </section>
    }

    @if (erro) {
      <p class="form-error">{{ erro }}</p>
    }
    @if (sucesso) {
      <p class="form-success">{{ sucesso }}</p>
    }

    @if (carregando) {
      <p class="empty">Carregando agencias...</p>
    } @else if (resultado && resultado.itens?.length) {
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Codigo</th>
              <th>Agencia</th>
              <th>Cidade</th>
              <th>Saldo</th>
              <th>Status</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            @for (a of resultado.itens; track a.id) {
              <tr>
                <td><a [routerLink]="['/agencias', a.id]">{{ a.codigo }}</a></td>
                <td>{{ a.nome }}</td>
                <td>{{ a.cidade }}</td>
                <td>{{ a.saldoAtual | currency:'BRL' }}</td>
                <td [class.alerta]="a.abaixoDoLimite">{{ a.abaixoDoLimite ? 'Alerta' : 'OK' }}</td>
                <td class="actions">
                  <a class="button-link" [routerLink]="['/agencias', a.id]">Detalhar</a>
                  @if (isGestor && a.ativo !== false) {
                    <button class="outline" (click)="desativar(a.id)">Desativar</button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
      <section class="pagination">
        <button class="outline" (click)="paginaAnterior()" [disabled]="pagina === 0">Anterior</button>
        <span>Pagina {{ pagina + 1 }} de {{ totalPaginas }}</span>
        <button class="outline" (click)="proximaPagina()" [disabled]="pagina + 1 >= totalPaginas">Proxima</button>
      </section>
    } @else if (resultado) {
      <p class="empty">Nenhuma agencia encontrada para os filtros informados.</p>
    }
  `
})
export class AgenciasComponent implements OnInit {
  busca = '';
  alerta = '';
  ordenarPor = 'CODIGO';
  direcao = 'ASC';
  pagina = 0;
  tamanho = 20;
  resultado: any;
  carregando = false;
  salvando = false;
  erro = '';
  sucesso = '';
  nova: any = { codigo: '', nome: '', cidade: '', saldoAtual: '', limiteMinimo: '' };

  constructor(
    private service: AgenciasService,
    private http: HttpClient,
    private auth: AuthService,
    private route: ActivatedRoute
  ) {}

  get isGestor() {
    return this.auth.isGestor();
  }

  get totalPaginas() {
    return Math.max(1, this.resultado?.totalPaginas || 1);
  }

  ngOnInit() {
    this.alerta = this.route.snapshot.queryParamMap.get('alerta') || '';
    this.pesquisar();
  }

  pesquisar(resetPagina = true) {
    if (resetPagina) {
      this.pagina = 0;
    }

    this.erro = '';
    this.sucesso = '';
    this.carregando = true;
    this.service.listar(this.busca, this.alerta, this.ordenarPor, this.direcao, this.pagina, this.tamanho).subscribe({
      next: r => {
        this.resultado = r;
        this.carregando = false;
      },
      error: e => this.falha(e, 'Nao foi possivel carregar agencias.')
    });
  }

  limpar() {
    this.busca = '';
    this.alerta = '';
    this.ordenarPor = 'CODIGO';
    this.direcao = 'ASC';
    this.pesquisar();
  }

  criar() {
    this.erro = '';
    this.sucesso = '';
    this.salvando = true;
    const body = {
      ...this.nova,
      saldoAtual: Number(this.nova.saldoAtual),
      limiteMinimo: Number(this.nova.limiteMinimo)
    };

    this.http.post('/api/v1/agencias', body).subscribe({
      next: () => {
        this.nova = { codigo: '', nome: '', cidade: '', saldoAtual: '', limiteMinimo: '' };
        this.salvando = false;
        this.sucesso = 'Agencia criada com sucesso.';
        this.pesquisar();
      },
      error: e => {
        this.salvando = false;
        this.falha(e, 'Nao foi possivel criar a agencia.');
      }
    });
  }

  desativar(id: number) {
    if (!confirm('Desativar esta agencia?')) {
      return;
    }

    this.http.delete(`/api/v1/agencias/${id}`).subscribe({
      next: () => {
        this.sucesso = 'Agencia desativada com sucesso.';
        this.pesquisar(false);
      },
      error: e => this.falha(e, 'Nao foi possivel desativar a agencia.')
    });
  }

  paginaAnterior() {
    if (this.pagina > 0) {
      this.pagina -= 1;
      this.pesquisar(false);
    }
  }

  proximaPagina() {
    if (this.pagina + 1 < this.totalPaginas) {
      this.pagina += 1;
      this.pesquisar(false);
    }
  }

  private falha(error: HttpErrorResponse, fallback: string) {
    this.erro = error.error?.msgError || error.error?.message || fallback;
    this.carregando = false;
  }
}
