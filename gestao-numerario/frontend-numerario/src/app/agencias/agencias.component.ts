import { CurrencyPipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Agencia, PaginaResponse } from '../core/api.models';
import { AuthService } from '../core/auth.service';
import { AlertComponent } from '../shared/alert/alert.component';
import { ConfirmationDialogComponent } from '../shared/confirmation-dialog/confirmation-dialog.component';
import { BreadcrumbItem, PageHeaderComponent } from '../shared/page-header/page-header.component';
import { PaginationComponent } from '../shared/pagination/pagination.component';
import { AgenciasService } from './agencias.service';

interface NovaAgencia {
  codigo: string;
  nome: string;
  cidade: string;
  saldoAtual: number | '';
  limiteMinimo: number | '';
}

@Component({
  selector: 'app-agencias',
  imports: [
    AlertComponent,
    ConfirmationDialogComponent,
    CurrencyPipe,
    FormsModule,
    PageHeaderComponent,
    PaginationComponent,
    RouterLink
  ],
  template: `
    <app-page-header
      title="Agências"
      description="Consulta, cadastro e acompanhamento do limite operacional."
      [breadcrumbs]="breadcrumbs" />

    @if (!isGestor) {
      <app-alert type="warning" message="A consulta e a manutenção de agências exigem perfil gestor." />
    }

    <section class="filters" aria-label="Filtros de agências">
      <label class="field field--wide">
        <span>Busca</span>
        <input [(ngModel)]="busca" placeholder="Código, nome ou cidade">
      </label>
      <label class="field">
        <span>Status</span>
        <select [(ngModel)]="alerta">
          <option value="">Todos</option>
          <option value="true">Em alerta</option>
          <option value="false">Regular</option>
        </select>
      </label>
      <label class="field">
        <span>Ordenar por</span>
        <select [(ngModel)]="ordenarPor">
          <option value="CODIGO">Código</option>
          <option value="NOME">Nome</option>
          <option value="CIDADE">Cidade</option>
        </select>
      </label>
      <label class="field">
        <span>Direção</span>
        <select [(ngModel)]="direcao">
          <option value="ASC">Crescente</option>
          <option value="DESC">Decrescente</option>
        </select>
      </label>
      <button type="button" (click)="pesquisar()">Pesquisar</button>
      <button type="button" class="outline" (click)="limpar()">Limpar</button>
    </section>

    @if (isGestor) {
      <section class="form-panel" aria-labelledby="new-agency-title">
        <h2 id="new-agency-title">Nova agência</h2>
        <div class="form">
          <label class="field"><span>Código</span><input [(ngModel)]="nova.codigo" required></label>
          <label class="field"><span>Nome</span><input [(ngModel)]="nova.nome" required></label>
          <label class="field"><span>Cidade</span><input [(ngModel)]="nova.cidade" required></label>
          <label class="field"><span>Saldo inicial</span><input [(ngModel)]="nova.saldoAtual" type="number" min="0" step="0.01"></label>
          <label class="field"><span>Limite mínimo</span><input [(ngModel)]="nova.limiteMinimo" type="number" min="0" step="0.01" required></label>
          <button type="button" (click)="criar()" [disabled]="salvando">Cadastrar agência</button>
        </div>
      </section>
    }

    @if (erro) {
      <app-alert type="error" [message]="erro" />
    }
    @if (sucesso) {
      <app-alert type="success" [message]="sucesso" />
    }

    @if (carregando) {
      <p class="empty">Carregando agências...</p>
    } @else if (resultado?.itens?.length) {
      <div class="table-wrap">
        <table>
          <thead>
            <tr><th>Código</th><th>Agência</th><th>Cidade</th><th>Saldo</th><th>Status</th><th>Ações</th></tr>
          </thead>
          <tbody>
            @for (agencia of resultado!.itens; track agencia.id) {
              <tr>
                <td><a [routerLink]="['/agencias', agencia.id]">{{ agencia.codigo }}</a></td>
                <td>{{ agencia.nome }}</td>
                <td>{{ agencia.cidade }}</td>
                <td>{{ agencia.saldoAtual | currency:'BRL' }}</td>
                <td>
                  <span class="status-badge" [class.status-badge--attention]="agencia.abaixoDoLimite">
                    {{ agencia.abaixoDoLimite ? 'Em alerta' : 'Regular' }}
                  </span>
                </td>
                <td class="actions">
                  <a class="button-link" [routerLink]="['/agencias', agencia.id]">Detalhar</a>
                  @if (isGestor && agencia.ativo !== false) {
                    <button type="button" class="outline" (click)="solicitarDesativacao(agencia)">Desativar</button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
      <app-pagination [page]="pagina" [totalPages]="totalPaginas" (pageChange)="irParaPagina($event)" />
    } @else if (resultado) {
      <p class="empty">Nenhuma agência encontrada para os filtros informados.</p>
    }

    <app-confirmation-dialog
      #deactivationDialog
      title="Desativar agência"
      [message]="mensagemDesativacao"
      confirmLabel="Desativar"
      [danger]="true"
      (confirmed)="desativar()" />
  `
})
export class AgenciasComponent implements OnInit {
  @ViewChild('deactivationDialog') private dialog!: ConfirmationDialogComponent;

  readonly breadcrumbs: BreadcrumbItem[] = [
    { label: 'COIN Home', link: '/menu' },
    { label: 'Tesouraria', link: '/tesouraria' },
    { label: 'Agências' }
  ];
  busca = '';
  alerta = '';
  ordenarPor = 'CODIGO';
  direcao = 'ASC';
  pagina = 0;
  tamanho = 20;
  resultado?: PaginaResponse<Agencia>;
  carregando = false;
  salvando = false;
  erro = '';
  sucesso = '';
  nova: NovaAgencia = this.novaAgenciaVazia();
  agenciaParaDesativar?: Agencia;

  constructor(
    private readonly service: AgenciasService,
    private readonly http: HttpClient,
    private readonly auth: AuthService,
    private readonly route: ActivatedRoute
  ) {}

  get isGestor() { return this.auth.isGestor(); }
  get totalPaginas() { return Math.max(1, this.resultado?.totalPaginas ?? 1); }
  get mensagemDesativacao() {
    return this.agenciaParaDesativar
      ? `A agência ${this.agenciaParaDesativar.codigo} — ${this.agenciaParaDesativar.nome} deixará de operar.`
      : '';
  }

  ngOnInit() {
    this.alerta = this.route.snapshot.queryParamMap.get('alerta') || '';
    this.pesquisar();
  }

  pesquisar(resetPagina = true) {
    if (resetPagina) this.pagina = 0;
    this.erro = '';
    this.sucesso = '';
    this.carregando = true;
    this.service.listar(this.busca, this.alerta, this.ordenarPor, this.direcao, this.pagina, this.tamanho).subscribe({
      next: resultado => {
        this.resultado = resultado;
        this.carregando = false;
      },
      error: error => this.falha(error, 'Não foi possível carregar as agências.')
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
        this.nova = this.novaAgenciaVazia();
        this.salvando = false;
        this.pesquisar();
        this.sucesso = 'Agência criada com sucesso.';
      },
      error: error => {
        this.salvando = false;
        this.falha(error, 'Não foi possível criar a agência.');
      }
    });
  }

  solicitarDesativacao(agencia: Agencia) {
    this.agenciaParaDesativar = agencia;
    this.dialog.open();
  }

  desativar() {
    if (!this.agenciaParaDesativar) return;
    this.http.delete(`/api/v1/agencias/${this.agenciaParaDesativar.id}`).subscribe({
      next: () => {
        this.agenciaParaDesativar = undefined;
        this.pesquisar(false);
        this.sucesso = 'Agência desativada com sucesso.';
      },
      error: error => this.falha(error, 'Não foi possível desativar a agência.')
    });
  }

  irParaPagina(pagina: number) {
    this.pagina = pagina;
    this.pesquisar(false);
  }

  private novaAgenciaVazia(): NovaAgencia {
    return { codigo: '', nome: '', cidade: '', saldoAtual: '', limiteMinimo: '' };
  }

  private falha(error: HttpErrorResponse, fallback: string) {
    this.erro = error.error?.msgError || error.error?.message || fallback;
    this.carregando = false;
  }
}
