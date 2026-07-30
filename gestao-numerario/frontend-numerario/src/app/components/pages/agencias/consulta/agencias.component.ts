import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Agencia, PaginaResponse } from '../../../../models/api.models';
import { AuthService } from '../../../../services/auth.service';
import { AlertComponent } from '../../../shared/alert/alert.component';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';
import { BreadcrumbItem, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';
import { AgenciasService } from '../../../../services/agencias.service';

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
  templateUrl: './agencias.component.html'
})
export class AgenciasComponent implements OnInit {
  @ViewChild('deactivationDialog') private dialog!: ConfirmationDialogComponent;

  breadcrumbs: BreadcrumbItem[] = [
    { label: 'COIN Home', link: '/menu' },
    { label: 'Tesouraria', link: '/tesouraria' },
    { label: 'Agências', link: '/agencias' },
    { label: 'Consultar' }
  ];
  backTo = '/agencias';
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
    if (this.route.snapshot.queryParamMap.get('origem') === 'dashboard'
        || history.state?.origem === 'dashboard') {
      this.breadcrumbs = [
        { label: 'COIN Home', link: '/menu' },
        { label: 'Tesouraria', link: '/tesouraria' },
        { label: 'Dashboard', link: '/dashboard' },
        { label: 'Agências', link: '/agencias' },
        { label: 'Consultar' }
      ];
      this.backTo = '/dashboard';
    }
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
    this.service.criar(body).subscribe({
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
    this.service.desativar(this.agenciaParaDesativar.id).subscribe({
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

  alterarTamanho(tamanho: number) {
    this.tamanho = tamanho;
    this.pagina = 0;
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
