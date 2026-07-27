import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  ArrowLeft,
  LockKeyhole,
  LucideAngularModule,
  ServerOff,
  TriangleAlert
} from 'lucide-angular';
import { AuthService } from '../../../services/auth.service';

type ErrorType = 'url' | 'acesso' | 'infra' | 'cadastros';

interface ErrorContent {
  code: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-error',
  standalone: true,
  imports: [RouterLink, LucideAngularModule],
  templateUrl: './error.component.html',
  styleUrl: './error.component.css'
})
export class ErrorComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  readonly ArrowLeft = ArrowLeft;
  readonly LockKeyhole = LockKeyhole;
  readonly ServerOff = ServerOff;
  readonly TriangleAlert = TriangleAlert;

  type: ErrorType = 'url';
  content: ErrorContent = this.contentFor('url');

  constructor(
    private readonly route: ActivatedRoute,
    private readonly auth: AuthService
  ) {}

  ngOnInit() {
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
      const type = params.get('tipo');
      this.type = type === 'acesso' || type === 'infra' || type === 'cadastros' ? type : 'url';
      this.content = this.contentFor(this.type);
    });
  }

  get backLink() {
    return this.auth.autenticado() ? '/menu' : '/login';
  }

  get icon() {
    if (this.type === 'acesso') {
      return this.LockKeyhole;
    }
    if (this.type === 'infra') {
      return this.ServerOff;
    }
    return this.TriangleAlert;
  }

  private contentFor(type: ErrorType): ErrorContent {
    switch (type) {
      case 'acesso':
        return {
          code: 'Erro 403',
          title: 'Acesso não autorizado',
          description: 'Seu perfil não possui permissão para acessar esta funcionalidade.'
        };
      case 'infra':
        return {
          code: 'Serviço indisponível',
          title: 'Não foi possível carregar o COIN',
          description: 'A API ou o BFF não respondeu. Aguarde alguns instantes e tente novamente.'
        };
      case 'cadastros':
        return {
          code: 'Módulo indisponível',
          title: 'Cadastros ainda não foi implementado',
          description: 'Esta área será disponibilizada em uma próxima etapa do COIN.'
        };
      default:
        return {
          code: 'Erro 404',
          title: 'Página não encontrada',
          description: 'O endereço informado não existe ou foi removido.'
        };
    }
  }
}
