import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ProdutoAlterarDTO } from '../../models/ProdutoAlterarDTO';
import { ProdutoResponseDTO } from '../../models/ProdutoResponseDTO';
import { ProdutoService } from '../../services/produto.service';

type CampoModificado = {
  campo: string;
  antes: string | number;
  depois: string | number;
};

@Component({
  selector: 'app-alterar-produto',
  imports: [RouterLink, FormsModule, CommonModule],
  templateUrl: './alterar-produto.component.html',
  styleUrl: './alterar-produto.component.css'
})
export class AlterarProdutoComponent implements OnInit {
  private readonly siglas = ['HDMI', 'USB', 'LED', 'LCD', 'SSD', 'HD', 'CPU', 'GPU', 'RAM', 'TV', 'DVD', 'CD', 'VGA', 'RGB'];

  id = 0;
  nome = '';
  preco = 0;
  precoFormatado = '';
  ativo = true;

  busca = '';
  origem = '';

  resultadosBusca: ProdutoResponseDTO[] = [];
  produtoSelecionado: ProdutoResponseDTO | null = null;
  produtoOriginal: ProdutoResponseDTO | null = null;

  modalConfirmacaoAberto = false;
  camposModificados: CampoModificado[] = [];

  mensagem = '';
  tipoMensagem = 'sucesso';

  constructor(
    private produtoService: ProdutoService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
this.route.queryParams.subscribe((params) => {
  const id = Number(params['id']);
  this.origem = params['origem'] || '';

  if (id) {
    this.produtoService.buscarPorId(id).subscribe({
      next: (produto) => this.selecionarProduto(produto),
      error: () => {
        this.mensagem = 'Produto nao encontrado.';
        this.tipoMensagem = 'erro';
      }
    });
  }
});
  }

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
      status: 'todos',
      sort: 'nome,asc'
    }).subscribe({
      next: (resposta) => {
        this.resultadosBusca = resposta.content;
      },
      error: () => {
        this.mensagem = 'Erro ao buscar produtos.';
        this.tipoMensagem = 'erro';
      }
    });
  }

  selecionarProduto(produto: ProdutoResponseDTO): void {
    this.produtoSelecionado = produto;
    this.produtoOriginal = { ...produto };
    this.id = produto.id;
    this.nome = produto.nome;
    this.preco = produto.preco;
    this.precoFormatado = this.formatarMoeda(produto.preco);
    this.ativo = produto.ativo;
    this.busca = `${produto.nome} - #${produto.id}`;
    this.resultadosBusca = [];
    this.mensagem = '';
  }

  alterar(): void {
    if (!this.produtoOriginal) {
      this.mensagem = 'Selecione um produto para alterar.';
      this.tipoMensagem = 'erro';
      return;
    }

    this.camposModificados = this.obterCamposModificados();

    if (this.camposModificados.length === 0) {
      this.mensagem = 'Nenhuma alteracao foi feita.';
      this.tipoMensagem = 'erro';
      return;
    }

    this.modalConfirmacaoAberto = true;
  }

  obterCamposModificados(): CampoModificado[] {
    const alteracoes: CampoModificado[] = [];

    if (!this.produtoOriginal) {
      return alteracoes;
    }

    if (this.produtoOriginal.nome !== this.nome) {
      alteracoes.push({ campo: 'Nome', antes: this.produtoOriginal.nome, depois: this.nome });
    }

    if (this.produtoOriginal.preco !== this.preco) {
      alteracoes.push({
        campo: 'Preco',
        antes: this.formatarMoeda(this.produtoOriginal.preco),
        depois: this.formatarMoeda(this.preco)
      });
    }

    if (this.produtoOriginal.ativo !== this.ativo) {
      alteracoes.push({
        campo: 'Status',
        antes: this.produtoOriginal.ativo ? 'Ativo' : 'Inativo',
        depois: this.ativo ? 'Ativo' : 'Inativo'
      });
    }

    return alteracoes;
  }

  confirmarAlteracao(): void {
    const produto: ProdutoAlterarDTO = {
      nome: this.padronizarNome(this.nome),
      preco: this.preco,
      ativo: this.ativo
    };

    this.produtoService.alterar(this.id, produto).subscribe({
      next: (produtoAtualizado) => {
        this.mensagem = 'Produto alterado com sucesso.';
        this.tipoMensagem = 'sucesso';
        this.modalConfirmacaoAberto = false;
        this.selecionarProduto(produtoAtualizado);
      },
      error: (erro) => {
        this.mensagem = erro.error?.msgError || 'Nao foi possivel alterar o produto.';
        this.tipoMensagem = 'erro';
        this.modalConfirmacaoAberto = false;
      }
    });
  }

  fecharModalConfirmacao(): void {
    this.modalConfirmacaoAberto = false;
  }

  formatarMoeda(valor: number): string {
    return valor.toLocaleString('pt-BR', {
      style: 'currency',
      currency: 'BRL'
    });
  }

  aoDigitarPreco(valorDigitado: string): void {
    const somenteNumeros = valorDigitado.replace(/\D/g, '');
    const valorEmCentavos = Number(somenteNumeros || 0);

    this.preco = valorEmCentavos / 100;
    this.precoFormatado = this.formatarMoeda(this.preco);
  }

  private padronizarNome(nome: string): string {
    return nome
      .trim()
      .split(/\s+/)
      .map((palavra) => this.padronizarPalavra(palavra))
      .join(' ');
  }

  private padronizarPalavra(palavra: string): string {
    const palavraMaiuscula = palavra.toUpperCase();

    if (this.siglas.includes(palavraMaiuscula)) {
      return palavraMaiuscula;
    }

    if (palavra.length > 1 && palavra === palavra.toUpperCase()) {
      return palavra;
    }

    const palavraMinuscula = palavra.toLowerCase();
    return palavraMinuscula.charAt(0).toUpperCase() + palavraMinuscula.slice(1);
  }
  
  get rotaVoltar(): string {
  return this.origem === 'listagem' ? '/listar-produtos' : '/';
}
}
