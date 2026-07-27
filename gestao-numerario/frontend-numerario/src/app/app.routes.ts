import { Routes } from '@angular/router';
import { LoginComponent } from './components/pages/login/login.component';
import { DashboardComponent } from './components/pages/dashboard/dashboard.component';
import { LayoutComponent } from './components/layout/layout.component';
import { AgenciasComponent } from './components/pages/agencias/consulta/agencias.component';
import { SolicitacoesComponent } from './components/pages/solicitacoes/consulta/solicitacoes.component';
import { SolicitacoesMenuComponent } from './components/pages/solicitacoes/menu/solicitacoes-menu.component';
import { NovaSolicitacaoComponent } from './components/pages/solicitacoes/cadastro/nova-solicitacao.component';
import { MovimentacoesComponent } from './components/pages/movimentacoes/consulta/movimentacoes.component';
import { AgenciaDetalheComponent } from './components/pages/agencias/detalhe/agencia-detalhe.component';
import { AgenciasMenuComponent } from './components/pages/agencias/menu/agencias-menu.component';
import { NovaAgenciaComponent } from './components/pages/agencias/cadastro/nova-agencia.component';
import { MovimentacoesMenuComponent } from './components/pages/movimentacoes/menu/movimentacoes-menu.component';
import { NovaMovimentacaoComponent } from './components/pages/movimentacoes/cadastro/nova-movimentacao.component';
import { authGuard } from './guards/auth.guard';
import { gestorGuard } from './guards/gestor.guard';
import { MenuComponent } from './components/pages/menu/menu.component';
import { ErrorComponent } from './components/pages/error/error.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginComponent },
  { path: 'erro', component: ErrorComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'menu', component: MenuComponent },
      {
        path: 'tesouraria',
        loadComponent: () => import('./components/pages/tesouraria/tesouraria-menu.component')
          .then(modulo => modulo.TesourariaMenuComponent)
      },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'agencias', component: AgenciasMenuComponent, canActivate: [gestorGuard] },
      { path: 'agencias/consultar', component: AgenciasComponent, canActivate: [gestorGuard] },
      { path: 'agencias/nova', component: NovaAgenciaComponent, canActivate: [gestorGuard] },
      { path: 'agencias/:id', component: AgenciaDetalheComponent, canActivate: [gestorGuard] },
      { path: 'solicitacoes', component: SolicitacoesMenuComponent },
      { path: 'solicitacoes/consultar', component: SolicitacoesComponent },
      { path: 'solicitacoes/nova', component: NovaSolicitacaoComponent },
      { path: 'movimentacoes', component: MovimentacoesMenuComponent },
      { path: 'movimentacoes/consultar', component: MovimentacoesComponent },
      { path: 'movimentacoes/nova', component: NovaMovimentacaoComponent },
      {
        path: 'livro-caixa',
        loadComponent: () => import('./components/pages/livro-caixa/livro-caixa.component')
          .then(modulo => modulo.LivroCaixaComponent),
        canActivate: [gestorGuard]
      }
    ]
  },
  { path: '**', component: ErrorComponent }
];
