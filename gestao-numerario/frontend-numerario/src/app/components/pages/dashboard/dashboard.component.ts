import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, RefreshCw } from 'lucide-angular';
import { DashboardResponse } from '../../../models/api.models';
import { AuthService } from '../../../services/auth.service';
import { AlertComponent } from '../../shared/alert/alert.component';
import {
  BreadcrumbItem,
  PageHeaderComponent
} from '../../shared/page-header/page-header.component';

@Component({
  selector: 'app-dashboard',
  imports: [
    AlertComponent,
    CurrencyPipe,
    DatePipe,
    LucideAngularModule,
    PageHeaderComponent,
    RouterLink
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  resumo?: DashboardResponse;
  erro = '';
  carregando = false;
  readonly isGestor: boolean;
  readonly RefreshCw = RefreshCw;
  readonly breadcrumbs: BreadcrumbItem[] = [
    { label: 'COIN Home', link: '/menu' },
    { label: 'Tesouraria', link: '/tesouraria' },
    { label: 'Dashboard' }
  ];

  constructor(private readonly http: HttpClient, auth: AuthService) {
    this.isGestor = auth.isGestor();
  }

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.erro = '';
    this.carregando = true;

    this.http.get<DashboardResponse>('/api/v1/dashboard').subscribe({
      next: resumo => {
        this.resumo = resumo;
        this.carregando = false;
      },
      error: (error: HttpErrorResponse) => {
        this.erro = error.error?.msgError
          || error.error?.message
          || 'Não foi possível carregar os indicadores.';
        this.carregando = false;
      }
    });
  }
}
