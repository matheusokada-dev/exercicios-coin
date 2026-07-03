import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { delay, dematerialize, finalize, materialize } from 'rxjs';
import { LoadingComponent } from '../../loading/loading.component';
import { ProdutoResponseDTO } from '../../../models/ProdutoResponseDTO';
import { ApiErrorService } from '../../../services/api-error.service';
import { NotificationService } from '../../../services/notification.service';
import { ProdutoService } from '../../../services/produto.service';

@Component({
  selector: 'app-listar-produtos',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule, LoadingComponent],
  templateUrl: './listar-produtos.component.html',
  styleUrl: './listar-produtos.component.css'
})
export class ListarProdutosComponent implements OnInit {
  private readonly tempoLoadingMs = 1500;
  private buscaAtual = 0;
  private primeiraBusca = true;

  produtos: ProdutoResponseDTO[] = [];

  paginaAtual = 0;
  tamanhoPagina = 5;
  opcoesTamanhoPagina = [5, 10, 20, 50];

  busca = '';
  filtroStatus = 'todos';
  ordenacao = 'id,asc';
  precoMinimo: number | null = null;
  precoMaximo: number | null = null;
  precoMinimoFormatado = '';
  precoMaximoFormatado = '';

  totalPaginas = 0;
  totalElementos = 0;

  mensagem = '';
  tipoMensagem = '';
  mensagemResultado = '';
  carregando = false;
  processando = false;

  produtoParaExcluir: ProdutoResponseDTO | null = null;
  modalExclusaoAberto = false;

  constructor(
    private produtoService: ProdutoService,
    private router: Router,
    private notificationService: NotificationService,
    private apiErrorService: ApiErrorService
  ) {}

  ngOnInit(): void {
    this.listar(true);
    this.primeiraBusca = false;
  }

  listar(simularLoading = false): void {
    if (!this.validarFiltrosDePreco()) {
      return;
    }

    const deveSimularLoading = simularLoading || this.primeiraBusca;
    this.primeiraBusca = false;
    this.carregando = deveSimularLoading;
    this.mensagemResultado = '';
    const buscaId = ++this.buscaAtual;

    this.produtoService.listar({
      page: this.paginaAtual,
      size: this.tamanhoPagina,
      busca: this.busca,
      status: this.filtroStatus,
      precoMinimo: this.precoMinimo,
      precoMaximo: this.precoMaximo,
      sort: this.ordenacao
    }).pipe(
      materialize(),
      delay(deveSimularLoading ? this.tempoLoadingMs : 0),
      dematerialize(),
      finalize(() => {
        if (buscaId === this.buscaAtual) {
          this.carregando = false;
        }
      })
    ).subscribe({
      next: (resposta) => {
        if (buscaId !== this.buscaAtual) {
          return;
        }

        this.produtos = resposta.content;
        this.totalPaginas = resposta.totalPages;
        this.totalElementos = resposta.totalElements;
        this.mensagemResultado = resposta.content.length === 0
          ? 'Nenhum produto encontrado para os filtros informados.'
          : '';
        this.mensagem = '';
        this.tipoMensagem = '';
      },
      error: (erro) => {
        if (buscaId !== this.buscaAtual) {
          return;
        }

        console.error(erro);
        this.produtos = [];
        this.totalPaginas = 0;
        this.totalElementos = 0;
        this.mensagemResultado = '';
        this.mensagem = this.apiErrorService.obterMensagem(erro, 'Erro ao buscar produtos.');
        this.tipoMensagem = 'erro';
        this.notificationService.error(this.mensagem);
      }
    });
  }

  aplicarFiltros(): void {
    this.paginaAtual = 0;
    this.listar(true);
  }

  irParaPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas) {
      return;
    }

    this.paginaAtual = pagina;
    this.listar(false);
  }

  limparFiltros(): void {
    this.busca = '';
    this.filtroStatus = 'todos';
    this.precoMinimo = null;
    this.precoMaximo = null;
    this.precoMinimoFormatado = '';
    this.precoMaximoFormatado = '';
    this.ordenacao = 'id,asc';
    this.mensagemResultado = '';
    this.tamanhoPagina = 5;
    this.aplicarFiltros();
  }

  aoDigitarPrecoMinimo(valorDigitado: string): void {
    this.precoMinimo = this.converterTextoParaPreco(valorDigitado);
    this.precoMinimoFormatado = this.precoMinimo === null ? '' : this.formatarMoeda(this.precoMinimo);
  }

  aoDigitarPrecoMaximo(valorDigitado: string): void {
    this.precoMaximo = this.converterTextoParaPreco(valorDigitado);
    this.precoMaximoFormatado = this.precoMaximo === null ? '' : this.formatarMoeda(this.precoMaximo);
  }

  alterarProduto(produto: ProdutoResponseDTO): void {
    this.router.navigate(['/alterar-produto'], {
      queryParams: {
        id: produto.id,
        origem: 'listagem'
      }
    });
  }

  abrirModalExclusao(produto: ProdutoResponseDTO): void {
    if (!produto.ativo) {
      return;
    }

    this.produtoParaExcluir = produto;
    this.modalExclusaoAberto = true;
  }

  fecharModalExclusao(): void {
    if (this.processando) {
      return;
    }

    this.produtoParaExcluir = null;
    this.modalExclusaoAberto = false;
  }

  confirmarExclusao(): void {
    if (this.processando) {
      return;
    }

    if (!this.produtoParaExcluir) {
      return;
    }

    this.processando = true;
    this.produtoService.deletar(this.produtoParaExcluir.id).pipe(
      materialize(),
      delay(this.tempoLoadingMs),
      dematerialize(),
      finalize(() => this.processando = false)
    ).subscribe({
      next: () => {
        this.mensagem = 'Produto excluído com sucesso.';
        this.tipoMensagem = 'sucesso';
        this.notificationService.success(this.mensagem);
        this.produtoParaExcluir = null;
        this.modalExclusaoAberto = false;
        this.listar(false);
      },
      error: (erro) => {
        this.mensagem = this.apiErrorService.obterMensagem(erro, 'Não foi possível excluir o produto.');
        this.tipoMensagem = 'erro';
        this.notificationService.error(this.mensagem);
        this.produtoParaExcluir = null;
        this.modalExclusaoAberto = false;
      }
    });
  }

  private converterTextoParaPreco(valorDigitado: string): number | null {
    const somenteNumeros = valorDigitado.replace(/\D/g, '');

    if (!somenteNumeros) {
      return null;
    }

    return Number(somenteNumeros) / 100;
  }

  private formatarMoeda(valor: number): string {
    return valor.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    });
  }

  private validarFiltrosDePreco(): boolean {
    if ((this.precoMinimo !== null && this.precoMinimo < 0) || (this.precoMaximo !== null && this.precoMaximo < 0)) {
      this.mensagem = 'Os preços não podem ser negativos.';
      this.tipoMensagem = 'erro';
      this.notificationService.error(this.mensagem);
      return false;
    }

    if (this.precoMinimo !== null && this.precoMaximo !== null && this.precoMinimo > this.precoMaximo) {
      this.mensagem = 'O preço mínimo não pode ser maior que o preço máximo.';
      this.tipoMensagem = 'erro';
      this.notificationService.error(this.mensagem);
      return false;
    }

    return true;
  }
}
