import { Component, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { PageBackComponent } from '../shared/page-back/page-back.component';

@Component({
  selector: 'app-agencia-detalhe',
  imports: [CurrencyPipe, FormsModule, RouterLink, PageBackComponent],
  template: `
    <div class="page-navigation">
      <p class="crumb">
        <a routerLink="/menu">COIN Home</a> /
        <a routerLink="/tesouraria">Tesouraria</a> /
        <a routerLink="/agencias">Agências</a> /
        Detalhe
      </p>
      <app-page-back to="/agencias" />
    </div>

    @if (erro) {
      <p class="form-error">{{ erro }}</p>
    }
    @if (sucesso) {
      <p class="form-success">{{ sucesso }}</p>
    }

    @if (detalhe) {
      <h1>{{ detalhe.agencia.nome }}</h1>
      <p class="subtitle">Codigo {{ detalhe.agencia.codigo }} - {{ detalhe.agencia.cidade }}</p>

      <section class="cards">
        <article><small>SALDO ATUAL</small><strong>{{ detalhe.agencia.saldoAtual | currency:'BRL' }}</strong></article>
        <article><small>ENTRADAS HOJE</small><strong>{{ detalhe.valorEntradasHoje | currency:'BRL' }}</strong></article>
        <article><small>SAIDAS HOJE</small><strong>{{ detalhe.valorSaidasHoje | currency:'BRL' }}</strong></article>
        <article><small>SALDO PREVISTO</small><strong>{{ detalhe.saldoPrevistoAposAbastecimentoAprovado | currency:'BRL' }}</strong></article>
      </section>

      <h2>Editar agencia</h2>
      <section class="form">
        <input [(ngModel)]="edicao.nome" placeholder="Nome *">
        <input [(ngModel)]="edicao.cidade" placeholder="Cidade *">
        <input [(ngModel)]="edicao.limiteMinimo" type="number" min="0" step="0.01" placeholder="Limite minimo *">
        <button (click)="salvar()" [disabled]="salvando">Salvar alteracoes</button>
      </section>
    } @else if (!erro) {
      <p class="empty">Carregando detalhe da agencia...</p>
    }
  `
})
export class AgenciaDetalheComponent implements OnInit {
  detalhe: any;
  id = 0;
  edicao: any = {};
  erro = '';
  sucesso = '';
  salvando = false;

  constructor(private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit() {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.carregar();
  }

  carregar() {
    this.erro = '';
    this.http.get<any>(`/api/v1/agencias/${this.id}/detalhe`).subscribe({
      next: r => {
        this.detalhe = r;
        this.edicao = {
          nome: r.agencia.nome,
          cidade: r.agencia.cidade,
          limiteMinimo: r.agencia.limiteMinimo
        };
      },
      error: e => this.falha(e, 'Nao foi possivel carregar o detalhe da agencia.')
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
        this.sucesso = 'Agencia atualizada com sucesso.';
        this.carregar();
      },
      error: e => {
        this.salvando = false;
        this.falha(e, 'Nao foi possivel salvar as alteracoes.');
      }
    });
  }

  private falha(error: HttpErrorResponse, fallback: string) {
    this.erro = error.error?.msgError || error.error?.message || fallback;
  }
}
