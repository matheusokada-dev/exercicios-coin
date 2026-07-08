import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { delay, dematerialize, finalize, materialize } from 'rxjs';
import { LoadingComponent } from '../../loading/loading.component';
import { ProdutoDtoRequest } from '../../../models/ProdutoDtoRequest';
import { ApiErrorService } from '../../../services/api-error.service';
import { NotificationService } from '../../../services/notification.service';
import { ProdutoService } from '../../../services/produto.service';

@Component({
  selector: 'app-cadastrar-produto',
  standalone: true,
  imports: [RouterLink, FormsModule, CommonModule, LoadingComponent],
  templateUrl: './cadastrar-produto.component.html',
  styleUrl: './cadastrar-produto.component.css'
})
export class CadastrarProdutoComponent {
  private readonly tempoLoadingMs = 1000;
  private readonly siglas = ['HDMI', 'USB', 'LED', 'LCD', 'SSD', 'HD', 'CPU', 'GPU', 'RAM', 'TV', 'DVD', 'CD', 'VGA', 'RGB'];

  nome = '';
  preco = 0;
  precoFormatado = '';

  modalConfirmacaoAberto = false;
  processando = false;

  mensagem = '';
  tipoMensagem = 'sucesso';

  constructor(
    private produtoService: ProdutoService,
    private notificationService: NotificationService,
    private apiErrorService: ApiErrorService
  ) {}

  cadastrar(): void {
    if (!this.nome.trim() || this.preco <= 0) {
      this.mensagem = 'Preencha nome e preço antes de cadastrar.';
      this.tipoMensagem = 'erro';
      this.notificationService.error(this.mensagem);
      return;
    }

    this.mensagem = '';
    this.modalConfirmacaoAberto = true;
  }

  confirmarCadastro(): void {
    if (this.processando) {
      return;
    }

    const produto: ProdutoDtoRequest = {
      nome: this.nomePadronizado,
      preco: this.preco,
    };

    this.processando = true;
    this.produtoService.cadastrar(produto).pipe(
      materialize(),
      delay(this.tempoLoadingMs),
      dematerialize(),
      finalize(() => this.processando = false)
    ).subscribe({
      next: () => {
        this.mensagem = 'Produto cadastrado com sucesso.';
        this.tipoMensagem = 'sucesso';
        this.notificationService.success(this.mensagem);
        this.modalConfirmacaoAberto = false;
        this.limparFormulario();
      },
      error: (erro) => {
        console.error(erro);
        this.tipoMensagem = 'erro';
        this.mensagem = this.apiErrorService.obterMensagem(erro, 'Erro ao cadastrar produto.');
        this.notificationService.error(this.mensagem);
        this.modalConfirmacaoAberto = false;
      }
    });
  }

  fecharModalConfirmacao(): void {
    if (this.processando) {
      return;
    }

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
    this.precoFormatado = this.preco === 0 ? '' : this.formatarMoeda(this.preco);
  }

  get nomePadronizado(): string {
    return this.padronizarNome(this.nome);
  }

  private limparFormulario(): void {
    this.nome = '';
    this.preco = 0;
    this.precoFormatado = '';
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
}
