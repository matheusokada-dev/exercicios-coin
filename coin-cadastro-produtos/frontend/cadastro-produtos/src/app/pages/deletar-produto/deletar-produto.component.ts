import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ProdutoService } from '../../services/produto.service';
import { ErrorObject } from '../../models/ErrorObject';

@Component({
  selector: 'app-deletar-produto',
  imports: [RouterLink, FormsModule
  ],
  templateUrl: './deletar-produto.component.html',
  styleUrl: './deletar-produto.component.css'
})
export class DeletarProdutoComponent {
  id = 0;

  mensagem = '';
  tipoMensagem = '';

  constructor(private produtoService: ProdutoService) {}

  deletar(): void {
    this.produtoService.deletar(this.id).subscribe({
      next: () => {
        this.mensagem = 'Produto foi desativado';
        this.tipoMensagem = 'sucesso';

        this.id = 0;
      },error: (erro) => {
              console.error(erro);
                const errorObject = erro.error as ErrorObject<unknown>;
      
              this.mensagem = erro.error?.msgError || 'Erro ao alterar produto.';
              this.tipoMensagem = 'erro';
            }
});
}}
