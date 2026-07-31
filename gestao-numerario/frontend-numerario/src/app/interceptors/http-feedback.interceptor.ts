import {
  HttpContextToken,
  HttpErrorResponse,
  HttpEvent,
  HttpEventType,
  HttpInterceptorFn
} from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, finalize, tap, throwError } from 'rxjs';
import { LoadingService } from '../services/loading.service';
import { NotificationService } from '../services/notification.service';

export const SILENCIAR_NOTIFICACOES_HTTP = new HttpContextToken<boolean>(() => false);

export const httpFeedbackInterceptor: HttpInterceptorFn = (request, next) => {
  const loading = inject(LoadingService);
  const notification = inject(NotificationService);
  const router = inject(Router);
  const silenciarNotificacoes = request.context.get(SILENCIAR_NOTIFICACOES_HTTP);

  loading.iniciar();

  return next(request).pipe(
    tap((event: HttpEvent<unknown>) => {
      if (
        !silenciarNotificacoes
        && event.type === HttpEventType.Response
        && request.method.toUpperCase() !== 'GET'
      ) {
        notification.success(mensagemSucesso(request.method));
      }
    }),
    catchError((error: HttpErrorResponse) => {
      if (!silenciarNotificacoes) {
        notification.error(mensagemErro(error));
      }

      const tipoErro = tipoPaginaErro(error);
      if (tipoErro && !router.url.startsWith('/erro')) {
        void router.navigate(['/erro'], { queryParams: { tipo: tipoErro } });
      }

      return throwError(() => error);
    }),
    finalize(() => loading.finalizar())
  );
};

function mensagemSucesso(method: string): string {
  switch (method.toUpperCase()) {
    case 'POST':
      return 'Registro realizado com sucesso.';
    case 'PUT':
    case 'PATCH':
      return 'Alteração realizada com sucesso.';
    case 'DELETE':
      return 'Registro removido com sucesso.';
    default:
      return 'Operação concluída com sucesso.';
  }
}

function mensagemErro(error: HttpErrorResponse): string {
  return error.error?.msgError
    || error.error?.message
    || (error.status === 0
      ? 'Não foi possível conectar aos serviços do COIN.'
      : 'Não foi possível concluir a operação.');
}

function tipoPaginaErro(error: HttpErrorResponse): 'acesso' | 'url' | 'infra' | null {
  if (error.status === 403) {
    return 'acesso';
  }
  if (error.status === 404) {
    return 'url';
  }
  if (error.status === 0 || error.status >= 500) {
    return 'infra';
  }
  return null;
}
