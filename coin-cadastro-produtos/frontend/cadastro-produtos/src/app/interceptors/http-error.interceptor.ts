import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { LoggerService } from '../services/logger.service';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const logger = inject(LoggerService);

  return next(req).pipe(
    catchError((erro: HttpErrorResponse) => {
      logger.error(`Falha HTTP ${req.method} ${req.urlWithParams}`, {
        status: erro.status,
        message: erro.message
      });

      return throwError(() => erro);
    })
  );
};
