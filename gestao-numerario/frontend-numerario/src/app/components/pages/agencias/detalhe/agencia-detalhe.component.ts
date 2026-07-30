import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CurrencyInputDirective } from '../../../../directives/currency-input.directive';
import { DetalheAgencia } from '../../../../models/api.models';
import { AgenciasService } from '../../../../services/agencias.service';
import { AlertComponent } from '../../../shared/alert/alert.component';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';
import { BreadcrumbItem, PageHeaderComponent } from '../../../shared/page-header/page-header.component';

interface EdicaoAgencia {
  nome: string;
  cidade: string;
  limiteMinimo: number | '';
}

@Component({
  selector: 'app-agencia-detalhe',
  imports: [
    AlertComponent,
    ConfirmationDialogComponent,
    CurrencyInputDirective,
    CurrencyPipe,
    DatePipe,
    FormsModule,
    PageHeaderComponent
  ],
  templateUrl: './agencia-detalhe.component.html',
  styleUrl: './agencia-detalhe.component.css'
})
export class AgenciaDetalheComponent implements OnInit {
  @ViewChild('confirmacaoEdicao') confirmacaoEdicao!: ConfirmationDialogComponent;
  @ViewChild('confirmacaoDesativacao') confirmacaoDesativacao!: ConfirmationDialogComponent;

  readonly breadcrumbs: BreadcrumbItem[] = [
    { label: 'Início', link: '/menu' },
    { label: 'Tesouraria', link: '/tesouraria' },
    { label: 'Agências', link: '/agencias/consultar' },
    { label: 'Detalhe da agência' }
  ];

  detalhe?: DetalheAgencia;
  id = 0;
  edicao: EdicaoAgencia = { nome: '', cidade: '', limiteMinimo: '' };
  erro = '';
  erroLimite = '';
  sucesso = '';
  salvando = false;
  desativando = false;
  emEdicao = false;
  consultadoEm?: Date;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly agenciasService: AgenciasService
  ) {}

  get descricao() {
    return this.detalhe
      ? `Código ${this.detalhe.agencia.codigo} · ${this.detalhe.agencia.cidade}`
      : 'Posição financeira e dados cadastrais.';
  }

  get rotuloStatus() {
    if (!this.detalhe?.agencia.ativo) return 'Inativa';
    if (this.detalhe.agencia.abaixoDoLimite) return 'Abaixo do limite';
    return 'Ativa';
  }

  get classeStatus() {
    if (!this.detalhe?.agencia.ativo) return 'agency-status--inactive';
    if (this.detalhe.agencia.abaixoDoLimite) return 'agency-status--attention';
    return 'agency-status--active';
  }

  get possuiAlteracoes() {
    if (!this.emEdicao || !this.detalhe) return false;
    const agencia = this.detalhe.agencia;
    return this.edicao.nome.trim() !== agencia.nome
      || this.edicao.cidade.trim() !== agencia.cidade
      || Number(this.edicao.limiteMinimo) !== agencia.limiteMinimo;
  }

  ngOnInit() {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.carregar();
  }

  carregar() {
    this.erro = '';
    this.agenciasService.detalhar(this.id).subscribe({
      next: detalhe => {
        this.detalhe = detalhe;
        this.consultadoEm = new Date();
        this.restaurarEdicao();
      },
      error: error => this.falha(error, 'Não foi possível carregar o detalhe da agência.')
    });
  }

  iniciarEdicao() {
    this.restaurarEdicao();
    this.erro = '';
    this.sucesso = '';
    this.emEdicao = true;
  }

  cancelarEdicao() {
    this.restaurarEdicao();
    this.emEdicao = false;
    this.erro = '';
  }

  salvar() {
    this.erro = '';
    this.erroLimite = '';

    if (!this.edicao.nome.trim() || !this.edicao.cidade.trim() || this.edicao.limiteMinimo === '') {
      this.erro = 'Preencha todos os campos obrigatórios da agência.';
      return;
    }
    if (Number(this.edicao.limiteMinimo) <= 0) {
      this.erroLimite = 'Informe um limite mínimo maior que zero.';
      return;
    }
    if (!this.possuiAlteracoes) {
      this.erro = 'Nenhuma alteração foi feita.';
      return;
    }
    this.confirmacaoEdicao.open();
  }

  confirmarEdicao() {
    this.erro = '';
    this.sucesso = '';
    this.salvando = true;
    const body = {
      nome: this.edicao.nome.trim(),
      cidade: this.edicao.cidade.trim(),
      limiteMinimo: Number(this.edicao.limiteMinimo)
    };

    this.agenciasService.atualizar(this.id, body).subscribe({
      next: agencia => {
        if (this.detalhe) {
          this.detalhe = { ...this.detalhe, agencia };
        }
        this.salvando = false;
        this.emEdicao = false;
        this.consultadoEm = new Date();
        this.restaurarEdicao();
        this.sucesso = 'Agência atualizada com sucesso.';
      },
      error: error => {
        this.salvando = false;
        this.falha(error, 'Não foi possível salvar as alterações.');
      }
    });
  }

  confirmarDesativacao() {
    this.confirmacaoDesativacao.open();
  }

  desativar() {
    this.desativando = true;
    this.erro = '';
    this.sucesso = '';
    this.agenciasService.desativar(this.id).subscribe({
      next: () => {
        this.desativando = false;
        this.sucesso = 'Agência desativada com sucesso. Os dados e o histórico foram preservados.';
        this.carregar();
      },
      error: error => {
        this.desativando = false;
        this.falha(error, 'Não foi possível desativar a agência.');
      }
    });
  }

  canDeactivate() {
    return !this.possuiAlteracoes
      || window.confirm('Existem alterações não salvas. Deseja sair sem salvar?');
  }

  @HostListener('window:beforeunload', ['$event'])
  protegerRecarregamento(event: BeforeUnloadEvent) {
    if (this.possuiAlteracoes) event.preventDefault();
  }

  private restaurarEdicao() {
    if (!this.detalhe) return;
    this.edicao = {
      nome: this.detalhe.agencia.nome,
      cidade: this.detalhe.agencia.cidade,
      limiteMinimo: this.detalhe.agencia.limiteMinimo
    };
    this.erroLimite = '';
  }

  private falha(error: HttpErrorResponse, fallback: string) {
    this.erro = error.error?.msgError || error.error?.message || fallback;
  }
}
