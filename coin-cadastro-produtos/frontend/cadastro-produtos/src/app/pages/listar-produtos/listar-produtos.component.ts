import { Component } from '@angular/core';
import { ProdutoResponseDTO } from '../../models/ProdutoResponseDTO';
import { ProdutoService } from '../../services/produto.service';
import { ErrorObject } from '../../models/ErrorObject';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-listar-produtos',
  imports: [RouterLink, CommonModule],
  templateUrl: './listar-produtos.component.html',
  styleUrl: './listar-produtos.component.css'
})
export class ListarProdutosComponent {
  
  produtos: ProdutoResponseDTO[] = [];

  mensagem = '';
  tipoMensagem = '';

  constructor(private produtoService: ProdutoService) {}

  listar(): void {
    this.produtoService.listar().subscribe({
      next: (resposta) => {
        this.produtos = resposta;
        this.mensagem = 'Produtos carregados com sucesso!';
        this.tipoMensagem = 'sucesso';
      }, error: (erro) => {
                    console.error(erro);
                      const errorObject = erro.error as ErrorObject<unknown>;
            
                    this.mensagem = erro.error?.msgError || 'Erro ao buscar produtos.';
                    this.tipoMensagem = 'erro';
                  }
      });

}
}
