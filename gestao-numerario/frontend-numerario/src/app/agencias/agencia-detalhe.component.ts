import { CurrencyPipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DetalheAgencia } from '../core/api.models';
import { AlertComponent } from '../shared/alert/alert.component';
import { BreadcrumbItem, PageHeaderComponent } from '../shared/page-header/page-header.component';

interface EdicaoAgencia {
  nome: string;
  cidade: string;
  limiteMinimo: number | '';
}

@Component({
  selector: 'app-agencia-detalhe',
  imports: [AlertComponent, CurrencyPipe, FormsModule, PageHeaderComponent],
  template: `
    <app-page-header
      [title]="detalhe?.agencia?.nome || 'Detalhe da agência'"
      [description]="descricao"
      backTo="/agencias"
      [breadcrumbs]="breadcrumbs" />

    @if (erro) { <app-alert type="error" [message]="erro" /> }
    @if (sucesso) { <app-alert type="success" [message]="sucesso" /> }

    @if (detalhe) {
      <section class="cards" aria-label="Posição financeira da agência">
        <article><small>Saldo atual</small><strong>{{ detalhe.agencia.saldoAtual | currency:'BRL' }}</strong></article>
        <article><small>Entradas hoje</small><strong>{{ detalhe.valorEntradasHoje | currency:'BRL' }}</strong></article>
        <article><small>Saídas hoje</small><strong>{{ detalhe.valorSaidasHoje | currency:'BRL' }}</strong></article>
        <article><small>Saldo previsto</small><strong>{{ detalhe.saldoPrevistoAposAbastecimentoAprovado | currency:'BRL' }}</strong></article>
      </section>

      <section class="form-panel" aria-labelledby="edit-agency-title">
        <h2 id="edit-agency-title">Editar agência</h2>
        <div class="form">
          <label class="field"><span>Nome</span><input [(ngModel)]="edicao.nome" required></label>
          <label class="field"><span>Cidade</span><input [(ngModel)]="edicao.cidade" required></label>
          <label class="field"><span>Limite mínimo</span><input [(ngModel)]="edicao.limiteMinimo" type="number" min="0" step="0.01" required></label>
          <button type="button" (click)="salvar()" [disabled]="salvando">Salvar alterações</button>
        </div>
      </section>
    } @else if (!erro) {
      <p class="empty">Carregando detalhe da agência...</p>
    }
  `
})
export class AgenciaDetalheComponent implements OnInit {
  readonly breadcrumbs: BreadcrumbItem[] = [
    { label: 'COIN Home', link: '/menu' },
    { label: 'Tesouraria', link: '/tesouraria' },
    { label: 'Agências', link: '/agencias' },
    { label: 'Detalhe' }
  ];
  detalhe?: DetalheAgencia;
  id = 0;
  edicao: EdicaoAgencia = { nome: '', cidade: '', limiteMinimo: '' };
  erro = '';
  sucesso = '';
  salvando = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly http: HttpClient
  ) {}

  get descricao() {
    return this.detalhe
      ? `Código ${this.detalhe.agencia.codigo} — ${this.detalhe.agencia.cidade}`
      : 'Posição financeira e dados cadastrais.';
  }

  ngOnInit() {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.carregar();
  }

  carregar() {
    this.erro = '';
    this.http.get<DetalheAgencia>(`/api/v1/agencias/${this.id}/detalhe`).subscribe({
      next: detalhe => {
        this.detalhe = detalhe;
        this.edicao = {
          nome: detalhe.agencia.nome,
          cidade: detalhe.agencia.cidade,
          limiteMinimo: detalhe.agencia.limiteMinimo
        };
      },
      error: error => this.falha(error, 'Não foi possível carregar o detalhe da agência.')
    });
  }

  salvar() {
    this.erro = '';
    this.sucesso = '';
    this.salvando = true;
    const body = { ...this.edicao, limiteMinimo: Number(this.edicao.limiteMinimo) };
    this.http.put(`/api/v1/agencias/${this.id}`, body).subscribe({
      next: () => {
        this.salvando = false;
        this.carregar();
        this.sucesso = 'Agência atualizada com sucesso.';
      },
      error: error => {
        this.salvando = false;
        this.falha(error, 'Não foi possível salvar as alterações.');
      }
    });
  }

  private falha(error: HttpErrorResponse, fallback: string) {
    this.erro = error.error?.msgError || error.error?.message || fallback;
  }
}
