import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ProdutoResponseDTO } from '../../models/ProdutoResponseDTO';
import { ProdutoService } from '../../services/produto.service';

@Component({
  selector: 'app-listar-produtos',
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './listar-produtos.component.html',
  styleUrl: './listar-produtos.component.css'
})
export class ListarProdutosComponent implements OnInit {
  produtos: ProdutoResponseDTO[] = [];

  paginaAtual = 0;
  tamanhoPagina = 5;
  opcoesTamanhoPagina = [5, 10, 20, 50];

  busca = '';
  filtroStatus = 'todos';
  ordenacao = 'nome,asc';
  precoMinimo: number | null = null;
  precoMaximo: number | null = null;
  precoMinimoFormatado = '';
  precoMaximoFormatado = '';

  totalPaginas = 0;
  totalElementos = 0;

  mensagem = '';
  tipoMensagem = '';

  produtoParaExcluir: ProdutoResponseDTO | null = null;
  modalExclusaoAberto = false;

  constructor(
    private produtoService: ProdutoService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.listar();
  }

  listar(): void {
    if (!this.validarFiltrosDePreco()) {
      return;
    }

    this.produtoService.listar({
      page: this.paginaAtual,
      size: this.tamanhoPagina,
      busca: this.busca,
      status: this.filtroStatus,
      precoMinimo: this.precoMinimo,
      precoMaximo: this.precoMaximo,
      sort: this.ordenacao
    }).subscribe({
      next: (resposta) => {
        this.produtos = resposta.content;
        this.totalPaginas = resposta.totalPages;
        this.totalElementos = resposta.totalElements;
        this.mensagem = '';
      },
      error: (erro) => {
        console.error(erro);
        this.mensagem = erro.error?.msgError || 'Erro ao buscar produtos.';
        this.tipoMensagem = 'erro';
      }
    });
  }

  aplicarFiltros(): void {
    this.paginaAtual = 0;
    this.listar();
  }

  irParaPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas) {
      return;
    }

    this.paginaAtual = pagina;
    this.listar();
  }

  limparFiltros(): void {
    this.busca = '';
    this.filtroStatus = 'todos';
    this.precoMinimo = null;
    this.precoMaximo = null;
    this.precoMinimoFormatado = '';
    this.precoMaximoFormatado = '';
    this.ordenacao = 'nome,asc';
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
      this.mensagem = 'Os precos nao podem ser negativos.';
      this.tipoMensagem = 'erro';
      return false;
    }

    if (this.precoMinimo !== null && this.precoMaximo !== null && this.precoMinimo > this.precoMaximo) {
      this.mensagem = 'O preco minimo nao pode ser maior que o preco maximo.';
      this.tipoMensagem = 'erro';
      return false;
    }

    return true;
  }

  alterarProduto(produto: ProdutoResponseDTO): void {
    this.router.navigate(['/alterar-produto'], {
      queryParams: { id: produto.id }
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
    this.produtoParaExcluir = null;
    this.modalExclusaoAberto = false;
  }

  confirmarExclusao(): void {
    if (!this.produtoParaExcluir) {
      return;
    }

    this.produtoService.deletar(this.produtoParaExcluir.id).subscribe({
      next: () => {
        this.mensagem = 'Produto excluido com sucesso.';
        this.tipoMensagem = 'sucesso';
        this.fecharModalExclusao();
        this.listar();
      },
      error: (erro) => {
        this.mensagem = erro.error?.msgError || 'Nao foi possivel excluir o produto.';
        this.tipoMensagem = 'erro';
        this.fecharModalExclusao();
      }
    });
  }
}
