import { Routes } from '@angular/router';

import { MenuPrincipalComponent } from './pages/menu-principal/menu-principal.component';
import { CadastrarProdutoComponent } from './pages/cadastrar-produto/cadastrar-produto.component';
import { AlterarProdutoComponent } from './pages/alterar-produto/alterar-produto.component';
import { DeletarProdutoComponent } from './pages/deletar-produto/deletar-produto.component';
import { ListarProdutosComponent } from './pages/listar-produtos/listar-produtos.component';

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
    redirectTo: ''
  }
];
