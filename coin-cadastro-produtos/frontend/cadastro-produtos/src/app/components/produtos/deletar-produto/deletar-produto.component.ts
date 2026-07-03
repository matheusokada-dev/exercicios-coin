import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { delay, dematerialize, finalize, materialize } from 'rxjs';
import { LoadingComponent } from '../../loading/loading.component';
import { ProdutoResponseDTO } from '../../../models/ProdutoResponseDTO';
import { ApiErrorService } from '../../../services/api-error.service';
import { NotificationService } from '../../../services/notification.service';
import { ProdutoService } from '../../../services/produto.service';

@Component({
  selector: 'app-deletar-produto',
  standalone: true,
  imports: [RouterLink, FormsModule, CommonModule, LoadingComponent],
  templateUrl: './deletar-produto.component.html',
  styleUrl: './deletar-produto.component.css'
})
export class DeletarProdutoComponent {
  private readonly tempoLoadingMs = 1000;

  id = 0;
  nome = '';
  preco = 0;
  ativo = true;

  busca = '';
  resultadosBusca: ProdutoResponseDTO[] = [];
  produtoSelecionado: ProdutoResponseDTO | null = null;

  modalConfirmacaoAberto = false;
  processando = false;

  mensagem = '';
  tipoMensagem = '';

  constructor(
    private produtoService: ProdutoService,
    private notificationService: NotificationService,
    private apiErrorService: ApiErrorService
  ) {}

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
      sort: 'id,asc'
    }).subscribe({
      next: (resposta) => {
        this.resultadosBusca = resposta.content;
      },
      error: (erro) => {
        console.error(erro);
        this.mensagem = this.apiErrorService.obterMensagem(erro, 'Erro ao buscar produtos.');
        this.tipoMensagem = 'erro';
        this.notificationService.error(this.mensagem);
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
      this.mensagem = 'Selecione um produto antes de excluir.';
      this.tipoMensagem = 'erro';
      this.notificationService.error(this.mensagem);
      return;
    }

    if (!this.produtoSelecionado.ativo) {
      this.mensagem = 'Produto inativo não pode ser excluído novamente.';
      this.tipoMensagem = 'erro';
      this.notificationService.error(this.mensagem);
      return;
    }

    this.modalConfirmacaoAberto = true;
  }

  fecharModalConfirmacao(): void {
    if (this.processando) {
      return;
    }

    this.modalConfirmacaoAberto = false;
  }

  confirmarDelecao(): void {
    if (this.processando) {
      return;
    }

    this.processando = true;
    this.produtoService.deletar(this.id).pipe(
      materialize(),
      delay(this.tempoLoadingMs),
      dematerialize(),
      finalize(() => this.processando = false)
    ).subscribe({
      next: () => {
        this.mensagem = 'Produto foi desativado.';
        this.tipoMensagem = 'sucesso';
        this.notificationService.success(this.mensagem);
        this.modalConfirmacaoAberto = false;
        this.limparFormulario();
      },
      error: (erro) => {
        console.error(erro);
        this.mensagem = this.apiErrorService.obterMensagem(erro, 'Erro ao excluir produto.');
        this.tipoMensagem = 'erro';
        this.notificationService.error(this.mensagem);
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
