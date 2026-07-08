import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { LoggerService } from '../services/logger.service';
import { Router } from '@angular/router';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const logger = inject(LoggerService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((erro: HttpErrorResponse) => {
      logger.error(`Falha HTTP ${req.method} ${req.urlWithParams}`, {
        status: erro.status,
        message: erro.message
      });

if (deveRedirecionarParaErro(erro)) {
  setTimeout(() => {
    router.navigate(['/erro'], {
      queryParams: {
        tipo: 'infra'
      }
    });
  }, 2500);
}

      return throwError(() => erro);
    })
  );
};

function deveRedirecionarParaErro(erro: HttpErrorResponse): boolean {
  return erro.status === 0
    || erro.status === 503
    || erro.status === 504;
}
