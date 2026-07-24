import { Component, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PageBackComponent } from '../shared/page-back/page-back.component';

@Component({
  selector: 'app-movimentacoes',
  imports: [CurrencyPipe, FormsModule, RouterLink, PageBackComponent],
  template: `
    <div class="page-navigation">
      <p class="crumb"><a routerLink="/menu">COIN Home</a> / <a routerLink="/tesouraria">Tesouraria</a> / Movimentações</p>
      <app-page-back to="/tesouraria" />
    </div>
    <h1>Movimentações</h1>

    <section class="filters">
      <input [(ngModel)]="agenciaId" placeholder="ID da agencia">
      <select [(ngModel)]="tipo">
        <option value="">Tipo</option>
        <option value="SAQUE">Saque</option>
        <option value="DEPOSITO">Deposito</option>
        <option value="RECOLHIMENTO">Recolhimento</option>
        <option value="AJUSTE">Ajuste</option>
        <option value="ABASTECIMENTO">Abastecimento</option>
      </select>
      <input [(ngModel)]="dataInicio" type="date" aria-label="Data inicial">
      <input [(ngModel)]="dataFim" type="date" aria-label="Data final">
      <button (click)="listar()">Pesquisar</button>
      <button class="outline" (click)="limpar()">Limpar</button>
    </section>

    <section class="form">
      <input [(ngModel)]="nova.agenciaId" type="number" min="1" placeholder="Agencia *">
      <select [(ngModel)]="nova.tipo">
        <option value="SAQUE">Saque</option>
        <option value="DEPOSITO">Deposito</option>
        <option value="RECOLHIMENTO">Recolhimento</option>
        <option value="AJUSTE">Ajuste</option>
      </select>
      @if (nova.tipo === 'AJUSTE') {
        <select [(ngModel)]="nova.entradaAjuste">
          <option [ngValue]="true">Entrada</option>
          <option [ngValue]="false">Saida</option>
        </select>
      }
      <input [(ngModel)]="nova.valor" type="number" min="0.01" step="0.01" placeholder="Valor *">
      <input [(ngModel)]="nova.descricao" placeholder="Descricao">
      <button (click)="criar()" [disabled]="salvando">Registrar +</button>
    </section>

    @if (erro) {
      <p class="form-error">{{ erro }}</p>
    }
    @if (sucesso) {
      <p class="form-success">{{ sucesso }}</p>
    }

    @if (carregando) {
      <p class="empty">Carregando movimentacoes...</p>
    } @else if (resultado && resultado.itens?.length) {
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Agencia</th>
              <th>Tipo</th>
              <th>Valor</th>
              <th>Saldo posterior</th>
              <th>Data</th>
            </tr>
          </thead>
          <tbody>
            @for (m of resultado.itens; track m.id) {
              <tr>
                <td>{{ m.agenciaId }}</td>
                <td>{{ m.tipo }}</td>
                <td>{{ m.valor | currency:'BRL' }}</td>
                <td>{{ m.saldoPosterior | currency:'BRL' }}</td>
                <td>{{ m.dataMovimento }}</td>
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
      <p class="empty">Nenhuma movimentacao encontrada para os filtros informados.</p>
    }
  `
})
export class MovimentacoesComponent implements OnInit {
  agenciaId = '';
  tipo = '';
  dataInicio = '';
  dataFim = '';
  pagina = 0;
  tamanho = 20;
  resultado: any;
  carregando = false;
  salvando = false;
  erro = '';
  sucesso = '';
  nova: any = { agenciaId: '', tipo: 'SAQUE', entradaAjuste: null, valor: '', descricao: '' };

  constructor(private http: HttpClient, private route: ActivatedRoute) {}

  get totalPaginas() {
    return Math.max(1, this.resultado?.totalPaginas || 1);
  }

  ngOnInit() {
    this.tipo = this.route.snapshot.queryParamMap.get('tipo') || '';
    this.listar();
  }

  listar(resetPagina = true) {
    if (resetPagina) {
      this.pagina = 0;
    }

    this.erro = '';
    this.sucesso = '';
    this.carregando = true;
    let params = new HttpParams().set('pagina', this.pagina).set('tamanho', this.tamanho);
    if (this.agenciaId) params = params.set('agenciaId', this.agenciaId);
    if (this.tipo) params = params.set('tipo', this.tipo);
    if (this.dataInicio) params = params.set('dataInicio', this.dataInicio);
    if (this.dataFim) params = params.set('dataFim', this.dataFim);

    this.http.get<any>('/api/v1/movimentacoes', { params }).subscribe({
      next: r => {
        this.resultado = r;
        this.carregando = false;
      },
      error: e => this.falha(e, 'Nao foi possivel carregar movimentacoes.')
    });
  }

  limpar() {
    this.agenciaId = '';
    this.tipo = '';
    this.dataInicio = '';
    this.dataFim = '';
    this.listar();
  }

  criar() {
    this.erro = '';
    this.sucesso = '';
    this.salvando = true;
    const body: any = {
      ...this.nova,
      agenciaId: Number(this.nova.agenciaId),
      valor: Number(this.nova.valor),
      idempotencyKey: crypto.randomUUID()
    };
    if (body.tipo !== 'AJUSTE') {
      body.entradaAjuste = null;
    }

    this.http.post('/api/v1/movimentacoes', body).subscribe({
      next: () => {
        this.nova = { agenciaId: '', tipo: 'SAQUE', entradaAjuste: null, valor: '', descricao: '' };
        this.salvando = false;
        this.sucesso = 'Movimentacao registrada com sucesso.';
        this.listar();
      },
      error: e => {
        this.salvando = false;
        this.falha(e, 'Nao foi possivel registrar a movimentacao.');
      }
    });
  }

  paginaAnterior() {
    if (this.pagina > 0) {
      this.pagina -= 1;
      this.listar(false);
    }
  }

  proximaPagina() {
    if (this.pagina + 1 < this.totalPaginas) {
      this.pagina += 1;
      this.listar(false);
    }
  }

  private falha(error: HttpErrorResponse, fallback: string) {
    this.erro = error.error?.msgError || error.error?.message || fallback;
    this.carregando = false;
  }
}
