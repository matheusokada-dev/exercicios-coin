import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProdutoResponseDTO } from '../../models/ProdutoResponseDTO';
import { ProdutoService } from '../../services/produto.service';

@Component({
  selector: 'app-deletar-produto',
  imports: [RouterLink, FormsModule, CommonModule],
  templateUrl: './deletar-produto.component.html',
  styleUrl: './deletar-produto.component.css'
})
export class DeletarProdutoComponent {
  id = 0;
  nome = '';
  preco = 0;
  ativo = true;

  busca = '';
  resultadosBusca: ProdutoResponseDTO[] = [];
  produtoSelecionado: ProdutoResponseDTO | null = null;

  modalConfirmacaoAberto = false;

  mensagem = '';
  tipoMensagem = '';

  constructor(private produtoService: ProdutoService) {}

  buscarProdutos(): void {
    const termo = this.busca.trim();

    if (!termo) {
      this.resultadosBusca = [];
      return;
    }

    this.produtoService.listar({
      page: 0,
      size: 5,
      busca: termo,
      status: 'ativos',
      precoMinimo: null,
      precoMaximo: null,
      sort: 'nome,asc'
    }).subscribe({
      next: (resposta) => {
        this.resultadosBusca = resposta.content;
      },
      error: (erro) => {
        console.error(erro);
        this.mensagem = 'Erro ao buscar produtos.';
        this.tipoMensagem = 'erro';
      }
    });
  }

  selecionarProduto(produto: ProdutoResponseDTO): void {
    this.produtoSelecionado = produto;
    this.id = produto.id;
    this.nome = produto.nome;
    this.preco = produto.preco;
    this.ativo = produto.ativo;
    this.busca = `${produto.nome} - #${produto.id}`;
    this.resultadosBusca = [];
    this.mensagem = '';
    this.tipoMensagem = '';
  }

  abrirModalConfirmacao(): void {
    if (!this.produtoSelecionado) {
      this.mensagem = 'Selecione um produto antes de deletar.';
      this.tipoMensagem = 'erro';
      return;
    }

    if (!this.produtoSelecionado.ativo) {
      this.mensagem = 'Produto inativo nao pode ser deletado novamente.';
      this.tipoMensagem = 'erro';
      return;
    }

    this.modalConfirmacaoAberto = true;
  }

  fecharModalConfirmacao(): void {
    this.modalConfirmacaoAberto = false;
  }

  confirmarDelecao(): void {
    this.produtoService.deletar(this.id).subscribe({
      next: () => {
        this.mensagem = 'Produto foi desativado.';
        this.tipoMensagem = 'sucesso';
        this.modalConfirmacaoAberto = false;
        this.limparFormulario();
      },
      error: (erro) => {
        console.error(erro);
        this.mensagem = erro.error?.msgError || 'Erro ao deletar produto.';
        this.tipoMensagem = 'erro';
      }
    });
  }

  limparFormulario(): void {
    this.id = 0;
    this.nome = '';
    this.preco = 0;
    this.ativo = true;
    this.busca = '';
    this.resultadosBusca = [];
    this.produtoSelecionado = null;
  }
}
