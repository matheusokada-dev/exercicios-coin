import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Agencia, Movimentacao, PaginaResponse, TipoMovimentacao } from '../../../../models/api.models';
import { AgenciasService } from '../../../../services/agencias.service';
import { AlertComponent } from '../../../shared/alert/alert.component';
import { BreadcrumbItem, PageHeaderComponent } from '../../../shared/page-header/page-header.component';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';

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
  templateUrl: './movimentacoes.component.html'
})
export class MovimentacoesComponent implements OnInit {
  breadcrumbs: BreadcrumbItem[] = [
    { label: 'COIN Home', link: '/menu' },
    { label: 'Tesouraria', link: '/tesouraria' },
    { label: 'Movimentações', link: '/movimentacoes' },
    { label: 'Consultar' }
  ];
  backTo = '/movimentacoes';
  agenciaId = '';
  tipo = '';
  dataInicio = '';
  dataFim = '';
  pagina = 0;
  tamanho = 20;
  resultado?: PaginaResponse<Movimentacao>;
  agencias: Agencia[] = [];
  carregando = false;
  salvando = false;
  erro = '';
  sucesso = '';
  nova: NovaMovimentacao = this.movimentacaoVazia();

  constructor(
    private readonly http: HttpClient,
    private readonly route: ActivatedRoute,
    private readonly agenciasService: AgenciasService
  ) {}

  get totalPaginas() { return Math.max(1, this.resultado?.totalPaginas ?? 1); }

  ngOnInit() {
    this.tipo = this.route.snapshot.queryParamMap.get('tipo') || '';
    if (this.route.snapshot.queryParamMap.get('origem') === 'dashboard'
        || history.state?.origem === 'dashboard') {
      this.breadcrumbs = [
        { label: 'COIN Home', link: '/menu' },
        { label: 'Tesouraria', link: '/tesouraria' },
        { label: 'Dashboard', link: '/dashboard' },
        { label: 'Movimentações', link: '/movimentacoes' },
        { label: 'Consultar' }
      ];
      this.backTo = '/dashboard';
    }
    this.carregarAgencias();
    this.listar();
  }
  carregarAgencias(){this.agenciasService.listar('','','CODIGO','ASC',0,100).subscribe({
    next:r=>this.agencias=r.itens,
    error:error=>this.falha(error,'Não foi possível carregar as agências.')
  });}
  nomeAgencia(id:number){const a=this.agencias.find(item=>item.id===id);return a?`${a.codigo} — ${a.nome}`:`Agência ${id}`;}

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
