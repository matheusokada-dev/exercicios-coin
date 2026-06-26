import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProdutoResponseDTO } from '../../models/ProdutoResponseDTO';
import { ProdutoService } from '../../services/produto.service';
import { ErrorObject } from '../../models/ErrorObject';

@Component({
  selector: 'app-listar-produtos-inativos',
  imports: [RouterLink, CommonModule],
  templateUrl: './listar-produtos-inativos.component.html',
  styleUrl: './listar-produtos-inativos.component.css'
})
export class ListarProdutosInativosComponent {
    produtos: ProdutoResponseDTO[] = [];
    mensagem = '';
    tipoMensagem = '';

    constructor(private produtoService: ProdutoService) {}
    
listarInativos(): void {
    this.produtoService.listarInativos().subscribe({
      next: (resposta) => {
        this.produtos = resposta;
        this.mensagem = 'Produtos carregados com sucesso!';
        this.tipoMensagem = 'sucesso';
      }, error: (erro) => {
                    console.error(erro);
                      const errorObject = erro.error as ErrorObject<unknown>;
            
                    this.mensagem = erro.error?.msgError || 'Erro ao buscar produtos inativos.';
                    this.tipoMensagem = 'erro';
                  }
      });

}
}
