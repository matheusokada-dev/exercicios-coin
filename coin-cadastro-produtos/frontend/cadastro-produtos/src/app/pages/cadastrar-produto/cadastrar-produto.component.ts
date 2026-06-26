import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ProdutoService } from '../../services/produto.service';
import { ProdutoRequestDTO } from '../../models/ProdutoRequestDTO';

@Component({
  selector: 'app-cadastrar-produto',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './cadastrar-produto.component.html',
  styleUrl: './cadastrar-produto.component.css'
})
export class CadastrarProdutoComponent {

  nome = '';
  preco = 0;

  mensagem = '';
  tipoMensagem = 'sucesso';

  constructor(private produtoService: ProdutoService) {}

  cadastrar(): void {
    const produto: ProdutoRequestDTO = {
      nome: this.nome,
      preco: this.preco,
    };

    this.produtoService.cadastrar(produto).subscribe({
      next: (resposta) => {
        this.mensagem = `Produto cadastrado com sucesso! ID: ${resposta.id}`;
        this.tipoMensagem = 'sucesso';

        this.nome = '';
        this.preco = 0;
      },
      error: (erro) => {
        console.error(erro);
        this.tipoMensagem = 'erro';
        this.mensagem = 'Erro ao cadastrar produto.';
      }
    });
  }
}