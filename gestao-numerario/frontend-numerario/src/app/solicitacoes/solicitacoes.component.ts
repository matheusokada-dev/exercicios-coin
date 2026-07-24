import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { PaginaResponse, Solicitacao } from '../core/api.models';
import { AuthService } from '../core/auth.service';
import { AlertComponent } from '../shared/alert/alert.component';
import { BreadcrumbItem, PageHeaderComponent } from '../shared/page-header/page-header.component';
import { PaginationComponent } from '../shared/pagination/pagination.component';

interface NovaSolicitacao {
  agenciaId: number | '';
  valor: number | '';
  motivo: string;
  dataDesejada: string;
}

interface Decisao {
  solicitacao?: Solicitacao;
  acao: 'aprovar' | 'rejeitar' | '';
  justificativaDecisao: string;
  justificativaEspecial: string;
}

@Component({
  selector: 'app-solicitacoes',
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
      title="Solicitações de abastecimento"
      description="Registro e acompanhamento dos pedidos de numerário."
      [breadcrumbs]="breadcrumbs" />

    <section class="filters" aria-label="Filtros de solicitações">
      <label class="field"><span>Agência</span><input [(ngModel)]="agenciaId" type="number" min="1" placeholder="ID da agência"></label>
      <label class="field">
        <span>Status</span>
        <select [(ngModel)]="status">
          <option value="">Todos</option>
          <option value="PENDENTE">Pendente</option>
          <option value="APROVADA">Aprovada</option>
          <option value="REJEITADA">Rejeitada</option>
          <option value="ATENDIDA">Atendida</option>
        </select>
      </label>
      <label class="field"><span>Data inicial</span><input [(ngModel)]="dataInicio" type="date"></label>
      <label class="field"><span>Data final</span><input [(ngModel)]="dataFim" type="date"></label>
      <button type="button" (click)="listar()">Pesquisar</button>
      <button type="button" class="outline" (click)="limpar()">Limpar</button>
    </section>

    <section class="form-panel" aria-labelledby="new-request-title">
      <h2 id="new-request-title">Nova solicitação</h2>
      <div class="form">
        <label class="field"><span>Agência</span><input [(ngModel)]="nova.agenciaId" type="number" min="1" required></label>
        <label class="field"><span>Valor</span><input [(ngModel)]="nova.valor" type="number" min="0.01" step="0.01" required></label>
        <label class="field field--wide"><span>Motivo</span><input [(ngModel)]="nova.motivo" required></label>
        <label class="field"><span>Data desejada</span><input [(ngModel)]="nova.dataDesejada" type="date"></label>
        <button type="button" (click)="criar()" [disabled]="salvando">Cadastrar solicitação</button>
      </div>
    </section>

    @if (decisao.solicitacao) {
      <section class="form-panel decision-panel" aria-labelledby="decision-title">
        <h2 id="decision-title">
          {{ decisao.acao === 'aprovar' ? 'Aprovar' : 'Rejeitar' }} solicitação #{{ decisao.solicitacao.id }}
        </h2>
        <div class="form">
          <label class="field field--wide"><span>Justificativa da decisão</span><input [(ngModel)]="decisao.justificativaDecisao" required></label>
          @if (decisao.acao === 'aprovar' && decisao.solicitacao.valor > 500000) {
            <label class="field field--wide">
              <span>Justificativa especial para valor acima de {{ 500000 | currency:'BRL' }}</span>
              <input [(ngModel)]="decisao.justificativaEspecial" required>
            </label>
          }
          <button type="button" (click)="confirmarDecisao()" [disabled]="salvando">Confirmar</button>
          <button type="button" class="outline" (click)="cancelarDecisao()">Cancelar</button>
        </div>
      </section>
    }

    @if (!isGestor) {
      <app-alert type="info" message="A aprovação e a rejeição são exclusivas do perfil gestor." />
    }
    @if (erro) { <app-alert type="error" [message]="erro" /> }
    @if (sucesso) { <app-alert type="success" [message]="sucesso" /> }

    @if (carregando) {
      <p class="empty">Carregando solicitações...</p>
    } @else if (resultado?.itens?.length) {
      <div class="table-wrap">
        <table>
          <thead><tr><th>Agência</th><th>Valor</th><th>Data desejada</th><th>Status</th><th>Ações</th></tr></thead>
          <tbody>
            @for (solicitacao of resultado!.itens; track solicitacao.id) {
              <tr>
                <td>{{ solicitacao.agenciaId }}</td>
                <td>{{ solicitacao.valor | currency:'BRL' }}</td>
                <td>{{ solicitacao.dataDesejada | date:'dd/MM/yyyy':'UTC' }}</td>
                <td>
                  <span class="status-badge" [class.status-badge--attention]="solicitacao.status === 'PENDENTE'">
                    {{ rotuloStatus(solicitacao.status) }}
                  </span>
                </td>
                <td class="actions">
                  @if (solicitacao.status === 'PENDENTE' && isGestor) {
                    <button type="button" (click)="abrirDecisao(solicitacao, 'aprovar')">Aprovar</button>
                    <button type="button" class="outline" (click)="abrirDecisao(solicitacao, 'rejeitar')">Rejeitar</button>
                  }
                  @if (solicitacao.status === 'APROVADA') {
                    <button type="button" (click)="atender(solicitacao)">Atender</button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
      <app-pagination [page]="pagina" [totalPages]="totalPaginas" (pageChange)="irParaPagina($event)" />
    } @else if (resultado) {
      <p class="empty">Nenhuma solicitação encontrada para os filtros informados.</p>
    }
  `
})
export class SolicitacoesComponent implements OnInit {
  readonly breadcrumbs: BreadcrumbItem[] = [
    { label: 'COIN Home', link: '/menu' },
    { label: 'Tesouraria', link: '/tesouraria' },
    { label: 'Solicitações' }
  ];
  agenciaId = '';
  status = '';
  dataInicio = '';
  dataFim = '';
  pagina = 0;
  tamanho = 20;
  resultado?: PaginaResponse<Solicitacao>;
  carregando = false;
  salvando = false;
  erro = '';
  sucesso = '';
  nova: NovaSolicitacao = this.solicitacaoVazia();
  decisao: Decisao = this.decisaoVazia();

  constructor(
    private readonly http: HttpClient,
    private readonly auth: AuthService,
    private readonly route: ActivatedRoute
  ) {}

  get isGestor() { return this.auth.isGestor(); }
  get totalPaginas() { return Math.max(1, this.resultado?.totalPaginas ?? 1); }

  ngOnInit() {
    this.status = this.route.snapshot.queryParamMap.get('status') || '';
    this.listar();
  }

  listar(resetPagina = true) {
    if (resetPagina) this.pagina = 0;
    this.erro = '';
    this.sucesso = '';
    this.carregando = true;
    let params = new HttpParams().set('pagina', this.pagina).set('tamanho', this.tamanho);
    if (this.agenciaId) params = params.set('agenciaId', this.agenciaId);
    if (this.status) params = params.set('status', this.status);
    if (this.dataInicio) params = params.set('dataInicio', this.dataInicio);
    if (this.dataFim) params = params.set('dataFim', this.dataFim);
    this.http.get<PaginaResponse<Solicitacao>>('/api/v1/solicitacoes', { params }).subscribe({
      next: resultado => {
        this.resultado = resultado;
        this.carregando = false;
      },
      error: error => this.falha(error, 'Não foi possível carregar as solicitações.')
    });
  }

  limpar() {
    this.agenciaId = '';
    this.status = '';
    this.dataInicio = '';
    this.dataFim = '';
    this.listar();
  }

  criar() {
    this.erro = '';
    this.sucesso = '';
    this.salvando = true;
    const body = { ...this.nova, agenciaId: Number(this.nova.agenciaId), valor: Number(this.nova.valor) };
    this.http.post('/api/v1/solicitacoes', body).subscribe({
      next: () => {
        this.nova = this.solicitacaoVazia();
        this.salvando = false;
        this.listar();
        this.sucesso = 'Solicitação criada com sucesso.';
      },
      error: error => {
        this.salvando = false;
        this.falha(error, 'Não foi possível criar a solicitação.');
      }
    });
  }

  abrirDecisao(solicitacao: Solicitacao, acao: 'aprovar' | 'rejeitar') {
    this.erro = '';
    this.sucesso = '';
    this.decisao = { solicitacao, acao, justificativaDecisao: '', justificativaEspecial: '' };
  }

  cancelarDecisao() { this.decisao = this.decisaoVazia(); }

  confirmarDecisao() {
    const { solicitacao, acao, justificativaDecisao, justificativaEspecial } = this.decisao;
    if (!solicitacao || !acao) return;
    this.salvando = true;
    const body = acao === 'aprovar'
      ? { justificativaDecisao, justificativaEspecial }
      : { justificativaDecisao };
    this.http.put(`/api/v1/solicitacoes/${solicitacao.id}/${acao}`, body).subscribe({
      next: () => {
        this.salvando = false;
        this.cancelarDecisao();
        this.listar(false);
        this.sucesso = `Solicitação ${acao === 'aprovar' ? 'aprovada' : 'rejeitada'} com sucesso.`;
      },
      error: error => {
        this.salvando = false;
        this.falha(error, 'Não foi possível concluir a decisão.');
      }
    });
  }

  atender(solicitacao: Solicitacao) {
    this.http.put(`/api/v1/solicitacoes/${solicitacao.id}/atender`, {
      idempotencyKey: crypto.randomUUID()
    }).subscribe({
      next: () => {
        this.listar(false);
        this.sucesso = 'Solicitação atendida com sucesso.';
      },
      error: error => this.falha(error, 'Não foi possível atender a solicitação.')
    });
  }

  irParaPagina(pagina: number) {
    this.pagina = pagina;
    this.listar(false);
  }

  rotuloStatus(status: Solicitacao['status']) {
    return { PENDENTE: 'Pendente', APROVADA: 'Aprovada', REJEITADA: 'Rejeitada', ATENDIDA: 'Atendida' }[status];
  }

  private solicitacaoVazia(): NovaSolicitacao {
    return { agenciaId: '', valor: '', motivo: '', dataDesejada: '' };
  }

  private decisaoVazia(): Decisao {
    return { acao: '', justificativaDecisao: '', justificativaEspecial: '' };
  }

  private falha(error: HttpErrorResponse, fallback: string) {
    this.erro = error.error?.msgError || error.error?.message || fallback;
    this.carregando = false;
  }
}
