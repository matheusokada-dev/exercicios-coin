import { CurrencyPipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { DetalheAgencia } from '../../../../models/api.models';
import { AlertComponent } from '../../../shared/alert/alert.component';
import { BreadcrumbItem, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';
import { CurrencyInputDirective } from '../../../../directives/currency-input.directive';

interface EdicaoAgencia {
  nome: string;
  cidade: string;
  limiteMinimo: number | '';
}

@Component({
  selector: 'app-agencia-detalhe',
  imports: [AlertComponent, CurrencyPipe, FormsModule, PageHeaderComponent, ConfirmationDialogComponent, CurrencyInputDirective],
  templateUrl: './agencia-detalhe.component.html'
})
export class AgenciaDetalheComponent implements OnInit {
  @ViewChild('confirmacaoEdicao') confirmacaoEdicao!:ConfirmationDialogComponent;
  @ViewChild('confirmacaoDesativacao') confirmacaoDesativacao!:ConfirmationDialogComponent;
  readonly breadcrumbs: BreadcrumbItem[] = [
    { label: 'COIN Home', link: '/menu' },
    { label: 'Tesouraria', link: '/tesouraria' },
    { label: 'Agências', link: '/agencias' },
    { label: 'Consultar', link: '/agencias/consultar' },
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
    if(!this.edicao.nome.trim()||!this.edicao.cidade.trim()||this.edicao.limiteMinimo===''){
      this.erro='Preencha todos os campos da agência.';return;
    }
    this.confirmacaoEdicao.open();
  }

  confirmarEdicao() {
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

  confirmarDesativacao() { this.confirmacaoDesativacao.open(); }

  desativar() {
    this.http.delete(`/api/v1/agencias/${this.id}`).subscribe({
      next: () => { this.sucesso = 'Agência desativada com sucesso.'; this.carregar(); },
      error: error => this.falha(error, 'Não foi possível desativar a agência.')
    });
  }

  private falha(error: HttpErrorResponse, fallback: string) {
    this.erro = error.error?.msgError || error.error?.message || fallback;
  }
}
