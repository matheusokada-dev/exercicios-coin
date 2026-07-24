import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Movimentacao, PaginaResponse, TipoMovimentacao } from '../core/api.models';
import { AlertComponent } from '../shared/alert/alert.component';
import { BreadcrumbItem, PageHeaderComponent } from '../shared/page-header/page-header.component';
import { PaginationComponent } from '../shared/pagination/pagination.component';

interface NovaMovimentacao {
  agenciaId: number | '';
  tipo: Exclude<TipoMovimentacao, 'ABASTECIMENTO'>;
  entradaAjuste: boolean | null;
  valor: number | '';
  descricao: string;
}

@Component({
  selector: 'app-movimentacoes',
  imports: [
    AlertComponent,
    CurrencyPipe,
    DatePipe,
    FormsModule,
    PageHeaderComponent,
    PaginationComponent
  ],
  template: `
    <app-page-header
      title="Movimentações"
      description="Consulta e registro de entradas e saídas de numerário."
      [breadcrumbs]="breadcrumbs" />

    <section class="filters" aria-label="Filtros de movimentações">
      <label class="field"><span>Agência</span><input [(ngModel)]="agenciaId" type="number" min="1" placeholder="ID da agência"></label>
      <label class="field">
        <span>Tipo</span>
        <select [(ngModel)]="tipo">
          <option value="">Todos</option>
          <option value="SAQUE">Saque</option>
          <option value="DEPOSITO">Depósito</option>
          <option value="RECOLHIMENTO">Recolhimento</option>
          <option value="AJUSTE">Ajuste</option>
          <option value="ABASTECIMENTO">Abastecimento</option>
        </select>
      </label>
      <label class="field"><span>Data inicial</span><input [(ngModel)]="dataInicio" type="date"></label>
      <label class="field"><span>Data final</span><input [(ngModel)]="dataFim" type="date"></label>
      <button type="button" (click)="listar()">Pesquisar</button>
      <button type="button" class="outline" (click)="limpar()">Limpar</button>
    </section>

    <section class="form-panel" aria-labelledby="new-movement-title">
      <h2 id="new-movement-title">Registrar movimentação</h2>
      <div class="form">
        <label class="field"><span>Agência</span><input [(ngModel)]="nova.agenciaId" type="number" min="1" required></label>
        <label class="field">
          <span>Tipo</span>
          <select [(ngModel)]="nova.tipo">
            <option value="SAQUE">Saque</option>
            <option value="DEPOSITO">Depósito</option>
            <option value="RECOLHIMENTO">Recolhimento</option>
            <option value="AJUSTE">Ajuste</option>
          </select>
        </label>
        @if (nova.tipo === 'AJUSTE') {
          <label class="field">
            <span>Direção do ajuste</span>
            <select [(ngModel)]="nova.entradaAjuste">
              <option [ngValue]="true">Entrada</option>
              <option [ngValue]="false">Saída</option>
            </select>
          </label>
        }
        <label class="field"><span>Valor</span><input [(ngModel)]="nova.valor" type="number" min="0.01" step="0.01" required></label>
        <label class="field field--wide"><span>Descrição</span><input [(ngModel)]="nova.descricao"></label>
        <button type="button" (click)="criar()" [disabled]="salvando">Registrar</button>
      </div>
    </section>

    @if (erro) { <app-alert type="error" [message]="erro" /> }
    @if (sucesso) { <app-alert type="success" [message]="sucesso" /> }

    @if (carregando) {
      <p class="empty">Carregando movimentações...</p>
    } @else if (resultado?.itens?.length) {
      <div class="table-wrap">
        <table>
          <thead><tr><th>Agência</th><th>Tipo</th><th>Valor</th><th>Saldo posterior</th><th>Data e hora</th></tr></thead>
          <tbody>
            @for (movimento of resultado!.itens; track movimento.id) {
              <tr>
                <td>{{ movimento.agenciaId }}</td>
                <td>{{ movimento.tipo }}</td>
                <td>{{ movimento.valor | currency:'BRL' }}</td>
                <td>{{ movimento.saldoPosterior | currency:'BRL' }}</td>
                <td>{{ movimento.dataMovimento | date:'dd/MM/yyyy HH:mm' }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
      <app-pagination [page]="pagina" [totalPages]="totalPaginas" (pageChange)="irParaPagina($event)" />
    } @else if (resultado) {
      <p class="empty">Nenhuma movimentação encontrada para os filtros informados.</p>
    }
  `
})
export class MovimentacoesComponent implements OnInit {
  readonly breadcrumbs: BreadcrumbItem[] = [
    { label: 'COIN Home', link: '/menu' },
    { label: 'Tesouraria', link: '/tesouraria' },
    { label: 'Movimentações' }
  ];
  agenciaId = '';
  tipo = '';
  dataInicio = '';
  dataFim = '';
  pagina = 0;
  tamanho = 20;
  resultado?: PaginaResponse<Movimentacao>;
  carregando = false;
  salvando = false;
  erro = '';
  sucesso = '';
  nova: NovaMovimentacao = this.movimentacaoVazia();

  constructor(
    private readonly http: HttpClient,
    private readonly route: ActivatedRoute
  ) {}

  get totalPaginas() { return Math.max(1, this.resultado?.totalPaginas ?? 1); }

  ngOnInit() {
    this.tipo = this.route.snapshot.queryParamMap.get('tipo') || '';
    this.listar();
  }

  listar(resetPagina = true) {
    if (resetPagina) this.pagina = 0;
    this.erro = '';
    this.sucesso = '';
    this.carregando = true;
    let params = new HttpParams().set('pagina', this.pagina).set('tamanho', this.tamanho);
    if (this.agenciaId) params = params.set('agenciaId', this.agenciaId);
    if (this.tipo) params = params.set('tipo', this.tipo);
    if (this.dataInicio) params = params.set('dataInicio', this.dataInicio);
    if (this.dataFim) params = params.set('dataFim', this.dataFim);

    this.http.get<PaginaResponse<Movimentacao>>('/api/v1/movimentacoes', { params }).subscribe({
      next: resultado => {
        this.resultado = resultado;
        this.carregando = false;
      },
      error: error => this.falha(error, 'Não foi possível carregar as movimentações.')
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
    const body = {
      ...this.nova,
      agenciaId: Number(this.nova.agenciaId),
      valor: Number(this.nova.valor),
      entradaAjuste: this.nova.tipo === 'AJUSTE' ? this.nova.entradaAjuste : null,
      idempotencyKey: crypto.randomUUID()
    };

    this.http.post('/api/v1/movimentacoes', body).subscribe({
      next: () => {
        this.nova = this.movimentacaoVazia();
        this.salvando = false;
        this.listar();
        this.sucesso = 'Movimentação registrada com sucesso.';
      },
      error: error => {
        this.salvando = false;
        this.falha(error, 'Não foi possível registrar a movimentação.');
      }
    });
  }

  irParaPagina(pagina: number) {
    this.pagina = pagina;
    this.listar(false);
  }

  private movimentacaoVazia(): NovaMovimentacao {
    return { agenciaId: '', tipo: 'SAQUE', entradaAjuste: null, valor: '', descricao: '' };
  }

  private falha(error: HttpErrorResponse, fallback: string) {
    this.erro = error.error?.msgError || error.error?.message || fallback;
    this.carregando = false;
  }
}
