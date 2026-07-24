import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LayoutComponent } from './layout/layout.component';
import { AgenciasComponent } from './agencias/agencias.component';
import { SolicitacoesComponent } from './solicitacoes/solicitacoes.component';
import { MovimentacoesComponent } from './movimentacoes/movimentacoes.component';
import { AgenciaDetalheComponent } from './agencias/agencia-detalhe.component';
import { authGuard } from './core/auth.guard';
import { gestorGuard } from './core/gestor.guard';
import { MenuComponent } from './menu/menu.component';
import { ErrorComponent } from './error/error.component';

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
        loadComponent: () => import('./tesouraria-menu/tesouraria-menu.component')
          .then(modulo => modulo.TesourariaMenuComponent)
      },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'agencias', component: AgenciasComponent, canActivate: [gestorGuard] },
      { path: 'agencias/:id', component: AgenciaDetalheComponent, canActivate: [gestorGuard] },
      { path: 'solicitacoes', component: SolicitacoesComponent },
      { path: 'movimentacoes', component: MovimentacoesComponent },
      {
        path: 'livro-caixa',
        loadComponent: () => import('./livro-caixa/livro-caixa.component')
          .then(modulo => modulo.LivroCaixaComponent),
        canActivate: [gestorGuard]
      }
    ]
  },
  { path: '**', component: ErrorComponent }
];
