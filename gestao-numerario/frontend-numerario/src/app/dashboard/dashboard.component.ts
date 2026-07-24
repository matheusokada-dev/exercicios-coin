import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, RefreshCw } from 'lucide-angular';
import { DashboardResponse } from '../core/api.models';
import { AuthService } from '../core/auth.service';
import { AlertComponent } from '../shared/alert/alert.component';
import {
  BreadcrumbItem,
  PageHeaderComponent
} from '../shared/page-header/page-header.component';

@Component({
  selector: 'app-dashboard',
  imports: [
    AlertComponent,
    CurrencyPipe,
    DatePipe,
    LucideAngularModule,
    PageHeaderComponent,
    RouterLink
  ],
  template: `
    <app-page-header
      title="Dashboard"
      description="Posição consolidada do numerário e pendências da operação."
      backTo="/tesouraria"
      [breadcrumbs]="breadcrumbs">
      @if (resumo) {
        <div class="reference">
          <span>
            <small>Data de referência</small>
            <strong>{{ resumo.dataReferencia | date:'dd/MM/yyyy':'UTC' }}</strong>
          </span>
          <button
            type="button"
            class="icon-button"
            title="Atualizar indicadores"
            aria-label="Atualizar indicadores"
            [disabled]="carregando"
            (click)="carregar()">
            <lucide-icon [img]="RefreshCw" [size]="18" aria-hidden="true" />
          </button>
        </div>
      }
    </app-page-header>

    @if (erro) {
      <app-alert
        type="error"
        title="Não foi possível carregar os indicadores"
        [message]="erro">
        <button type="button" class="outline" (click)="carregar()">Tentar novamente</button>
      </app-alert>
    } @else if (resumo) {
      <section class="summary-grid" aria-label="Resumo financeiro">
        <article class="summary-card summary-card--featured">
          <span class="summary-card__label">Numerário total</span>
          <strong>{{ resumo.numerarioTotal | currency:'BRL' }}</strong>
          <p>Saldo consolidado das agências ativas.</p>
        </article>

        <article class="summary-card">
          <span class="summary-card__label">Abastecimentos realizados hoje</span>
          <div class="supply-summary">
            <span>
              <strong>{{ resumo.quantidadeAbastecimentosHoje }}</strong>
              <small>operações</small>
            </span>
            <span>
              <strong>{{ resumo.valorAbastecidoHoje | currency:'BRL' }}</strong>
              <small>valor movimentado</small>
            </span>
          </div>
          <a routerLink="/movimentacoes" [queryParams]="{ tipo: 'ABASTECIMENTO' }">
            Consultar movimentações
          </a>
        </article>
      </section>

      <section class="pending-panel" aria-labelledby="pending-title">
        <header>
          <div>
            <h2 id="pending-title">Pendências operacionais</h2>
            <p>Itens que exigem acompanhamento da equipe.</p>
          </div>
          <strong class="pending-total">
            {{ resumo.quantidadeAgenciasEmAlerta + resumo.quantidadeSolicitacoesPendentes }}
          </strong>
        </header>

        <div class="pending-list">
          <div class="pending-item">
            <span>
              <strong>Agências abaixo do limite mínimo</strong>
              <small>Requerem análise para reposição de numerário.</small>
            </span>
            <b>{{ resumo.quantidadeAgenciasEmAlerta }}</b>
            @if (isGestor) {
              <a routerLink="/agencias" [queryParams]="{ alerta: true }">Consultar</a>
            }
          </div>

          <div class="pending-item">
            <span>
              <strong>Solicitações aguardando decisão</strong>
              <small>Pedidos de abastecimento ainda pendentes.</small>
            </span>
            <b>{{ resumo.quantidadeSolicitacoesPendentes }}</b>
            <a routerLink="/solicitacoes" [queryParams]="{ status: 'PENDENTE' }">Consultar</a>
          </div>
        </div>
      </section>
    } @else {
      <section class="dashboard-skeleton" aria-label="Carregando indicadores">
        <span></span>
        <span></span>
      </section>
    }
  `,
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  resumo?: DashboardResponse;
  erro = '';
  carregando = false;
  readonly isGestor: boolean;
  readonly RefreshCw = RefreshCw;
  readonly breadcrumbs: BreadcrumbItem[] = [
    { label: 'COIN Home', link: '/menu' },
    { label: 'Tesouraria', link: '/tesouraria' },
    { label: 'Dashboard' }
  ];

  constructor(private readonly http: HttpClient, auth: AuthService) {
    this.isGestor = auth.isGestor();
  }

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.erro = '';
    this.carregando = true;

    this.http.get<DashboardResponse>('/api/v1/dashboard').subscribe({
      next: resumo => {
        this.resumo = resumo;
        this.carregando = false;
      },
      error: (error: HttpErrorResponse) => {
        this.erro = error.error?.msgError
          || error.error?.message
          || 'Não foi possível carregar os indicadores.';
        this.carregando = false;
      }
    });
  }
}
