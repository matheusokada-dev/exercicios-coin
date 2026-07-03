import { Routes } from '@angular/router';

import { MenuPrincipalComponent } from './components/menu-principal';
import { CadastrarProdutoComponent } from './components/produtos/cadastrar-produto';
import { AlterarProdutoComponent } from './components/produtos/alterar-produto';
import { DeletarProdutoComponent } from './components/produtos/deletar-produto';
import { ListarProdutosComponent } from './components/produtos/listar-produtos';
import { PaginaErroComponent } from './components/pagina-erro';

export const routes: Routes = [
  {
    path: '',
    component: MenuPrincipalComponent
  },
  {
    path: 'cadastrar-produto',
    component: CadastrarProdutoComponent
  },
  {
    path: 'alterar-produto',
    component: AlterarProdutoComponent
  },
  {
    path: 'deletar-produto',
    component: DeletarProdutoComponent
  },
  {
    path: 'listar-produtos',
    component: ListarProdutosComponent
  },
  {
    path: '**',
    component: PaginaErroComponent
  }
];
