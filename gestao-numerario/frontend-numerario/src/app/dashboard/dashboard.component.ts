import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import {
  ArrowLeftRight,
  ArrowUpRight,
  BookOpen,
  Building2,
  CalendarDays,
  CircleAlert,
  ClipboardList,
  Clock3,
  LucideAngularModule,
  RefreshCw,
  WalletCards
} from 'lucide-angular';
import { AuthService } from '../core/auth.service';
import { PageBackComponent } from '../shared/page-back/page-back.component';

interface DashboardResponse {
  dataReferencia: string;
  numerarioTotal: number;
  quantidadeAgenciasEmAlerta: number;
  quantidadeSolicitacoesPendentes: number;
  quantidadeAbastecimentosHoje: number;
  valorAbastecidoHoje: number;
}

@Component({
  selector: 'app-dashboard',
  imports: [
    CurrencyPipe,
    DatePipe,
    LucideAngularModule,
    RouterLink,
    PageBackComponent
  ],
  template: `
    <div class="page-navigation">
      <p class="crumb">
        <a routerLink="/menu">COIN Home</a> /
        <a routerLink="/tesouraria">Tesouraria</a> /
        Dashboard
      </p>
      <app-page-back to="/tesouraria" />
    </div>

    <header class="dashboard-hero">
      <div>
        <span class="dashboard-eyebrow">PAINEL OPERACIONAL</span>
        <h1>Visão geral do numerário</h1>
        <p>Acompanhe a posição da rede e identifique o que precisa de atenção.</p>
      </div>

      @if (resumo) {
        <div class="reference-date" aria-label="Data de referência do dashboard">
          <span class="reference-date__icon">
            <lucide-icon [img]="CalendarDays" [size]="20" aria-hidden="true" />
          </span>
          <span>
            <small>DATA DE REFERÊNCIA</small>
            <strong>{{ resumo.dataReferencia | date:'dd/MM/yyyy':'UTC' }}</strong>
          </span>
          <button
            class="refresh-button"
            type="button"
            title="Atualizar indicadores"
            aria-label="Atualizar indicadores"
            [disabled]="carregando"
            (click)="carregar()">
            <lucide-icon [img]="RefreshCw" [size]="18" aria-hidden="true" />
          </button>
        </div>
      }
    </header>

    @if (erro) {
      <section class="dashboard-error" role="alert">
        <lucide-icon [img]="CircleAlert" [size]="22" aria-hidden="true" />
        <div>
          <strong>Não foi possível carregar os indicadores</strong>
          <p>{{ erro }}</p>
        </div>
        <button type="button" class="outline" (click)="carregar()">Tentar novamente</button>
      </section>
    } @else if (resumo) {
      <section class="metric-grid" aria-label="Indicadores operacionais">
        <article class="metric-card metric-card--primary">
          <div class="metric-card__top">
            <span class="metric-icon">
              <lucide-icon [img]="WalletCards" [size]="22" aria-hidden="true" />
            </span>
            <span class="metric-label">Numerário total</span>
          </div>
          <strong class="metric-value">{{ resumo.numerarioTotal | currency:'BRL' }}</strong>
          <p>Saldo consolidado das agências ativas.</p>
        </article>

        <article class="metric-card metric-card--warning">
          <div class="metric-card__top">
            <span class="metric-icon">
              <lucide-icon [img]="Building2" [size]="22" aria-hidden="true" />
            </span>
            <span class="metric-label">Agências em alerta</span>
          </div>
          <strong class="metric-value">{{ resumo.quantidadeAgenciasEmAlerta }}</strong>
          <p>Com saldo abaixo do limite mínimo.</p>
          @if (isGestor) {
            <a class="metric-link" routerLink="/agencias" [queryParams]="{ alerta: true }">
              Consultar agências
              <lucide-icon [img]="ArrowUpRight" [size]="16" aria-hidden="true" />
            </a>
          }
        </article>

        <article class="metric-card metric-card--attention">
          <div class="metric-card__top">
            <span class="metric-icon">
              <lucide-icon [img]="ClipboardList" [size]="22" aria-hidden="true" />
            </span>
            <span class="metric-label">Solicitações pendentes</span>
          </div>
          <strong class="metric-value">{{ resumo.quantidadeSolicitacoesPendentes }}</strong>
          <p>Aguardando análise e decisão.</p>
          <a class="metric-link" routerLink="/solicitacoes" [queryParams]="{ status: 'PENDENTE' }">
            Ver solicitações
            <lucide-icon [img]="ArrowUpRight" [size]="16" aria-hidden="true" />
          </a>
        </article>

        <article class="metric-card metric-card--today">
          <div class="metric-card__top">
            <span class="metric-icon">
              <lucide-icon [img]="Clock3" [size]="22" aria-hidden="true" />
            </span>
            <span class="metric-label">Abastecimentos hoje</span>
          </div>
          <div class="daily-supply">
            <strong class="metric-value">{{ resumo.quantidadeAbastecimentosHoje }}</strong>
            <span>
              <small>VALOR MOVIMENTADO</small>
              <b>{{ resumo.valorAbastecidoHoje | currency:'BRL' }}</b>
            </span>
          </div>
          <a class="metric-link" routerLink="/movimentacoes" [queryParams]="{ tipo: 'ABASTECIMENTO' }">
            Ver movimentações
            <lucide-icon [img]="ArrowUpRight" [size]="16" aria-hidden="true" />
          </a>
        </article>
      </section>

      <section class="dashboard-lower-grid">
        <article class="priority-panel">
          <div class="section-heading">
            <div>
              <span class="section-kicker">ATENÇÃO OPERACIONAL</span>
              <h2>Prioridades da rede</h2>
            </div>
            <span class="priority-total">
              {{ resumo.quantidadeAgenciasEmAlerta + resumo.quantidadeSolicitacoesPendentes }}
            </span>
          </div>

          <div class="priority-list">
            <div class="priority-item">
              <span class="priority-marker priority-marker--warning"></span>
              <div>
                <strong>Reposição de numerário</strong>
                <p>
                  {{ resumo.quantidadeAgenciasEmAlerta }}
                  {{ resumo.quantidadeAgenciasEmAlerta === 1 ? 'agência está' : 'agências estão' }}
                  abaixo do limite mínimo.
                </p>
              </div>
              @if (isGestor) {
                <a routerLink="/agencias" [queryParams]="{ alerta: true }" aria-label="Ver agências em alerta">
                  <lucide-icon [img]="ArrowUpRight" [size]="18" aria-hidden="true" />
                </a>
              }
            </div>

            <div class="priority-item">
              <span class="priority-marker priority-marker--attention"></span>
              <div>
                <strong>Fila de solicitações</strong>
                <p>
                  {{ resumo.quantidadeSolicitacoesPendentes }}
                  {{ resumo.quantidadeSolicitacoesPendentes === 1 ? 'solicitação aguarda' : 'solicitações aguardam' }}
                  análise.
                </p>
              </div>
              <a routerLink="/solicitacoes" [queryParams]="{ status: 'PENDENTE' }" aria-label="Ver solicitações pendentes">
                <lucide-icon [img]="ArrowUpRight" [size]="18" aria-hidden="true" />
              </a>
            </div>
          </div>
        </article>

        <article class="quick-actions">
          <div class="section-heading">
            <div>
              <span class="section-kicker">NAVEGAÇÃO RÁPIDA</span>
              <h2>Acessar operações</h2>
            </div>
          </div>

          <div class="quick-actions__grid">
            <a routerLink="/solicitacoes">
              <span><lucide-icon [img]="ClipboardList" [size]="19" aria-hidden="true" /></span>
              Solicitações
              <lucide-icon [img]="ArrowUpRight" [size]="16" aria-hidden="true" />
            </a>
            <a routerLink="/movimentacoes">
              <span><lucide-icon [img]="ArrowLeftRight" [size]="19" aria-hidden="true" /></span>
              Movimentações
              <lucide-icon [img]="ArrowUpRight" [size]="16" aria-hidden="true" />
            </a>
            @if (isGestor) {
              <a routerLink="/agencias">
                <span><lucide-icon [img]="Building2" [size]="19" aria-hidden="true" /></span>
                Agências
                <lucide-icon [img]="ArrowUpRight" [size]="16" aria-hidden="true" />
              </a>
              <a routerLink="/livro-caixa">
                <span><lucide-icon [img]="BookOpen" [size]="19" aria-hidden="true" /></span>
                Livro Caixa
                <lucide-icon [img]="ArrowUpRight" [size]="16" aria-hidden="true" />
              </a>
            }
          </div>
        </article>
      </section>
    } @else {
      <section class="dashboard-skeleton" aria-label="Carregando indicadores">
        <span></span>
        <span></span>
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

  readonly ArrowLeftRight = ArrowLeftRight;
  readonly ArrowUpRight = ArrowUpRight;
  readonly BookOpen = BookOpen;
  readonly Building2 = Building2;
  readonly CalendarDays = CalendarDays;
  readonly CircleAlert = CircleAlert;
  readonly ClipboardList = ClipboardList;
  readonly Clock3 = Clock3;
  readonly RefreshCw = RefreshCw;
  readonly WalletCards = WalletCards;

  constructor(private http: HttpClient, auth: AuthService) {
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
