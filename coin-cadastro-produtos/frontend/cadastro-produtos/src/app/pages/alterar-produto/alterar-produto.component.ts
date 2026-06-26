import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProdutoService } from '../../services/produto.service';
import { ProdutoAlterarDTO } from '../../models/ProdutoAlterarDTO';
import { ErrorObject } from '../../models/ErrorObject';

@Component({
  selector: 'app-alterar-produto',
  imports: [RouterLink, FormsModule],
  templateUrl: './alterar-produto.component.html',
  styleUrl: './alterar-produto.component.css'
})
export class AlterarProdutoComponent {

  id = 0;
  nome = '';
  preco = 0;
  ativo = true;

  mensagem = '';
  tipoMensagem = 'sucesso';

  constructor(private produtoService: ProdutoService) {}

  alterar(): void {
    const produto: ProdutoAlterarDTO = {
      nome: this.nome,
      preco: this.preco,
      ativo: this.ativo
    };

    this.produtoService.alterar(this.id, produto).subscribe({
      next: () => {
        this.mensagem = 'Produto alterado com sucesso!';
            this.tipoMensagem = 'sucesso';

      },
      error: (erro) => {
        console.error(erro);
          const errorObject = erro.error as ErrorObject<unknown>;

        this.mensagem = erro.error?.msgError || 'Erro ao alterar produto.';
        this.tipoMensagem = 'erro';
      }
    });
  }
}