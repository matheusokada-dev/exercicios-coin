import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const router = inject(Router);
  const auth = inject(AuthService);
  const endpointPublico = /\/api\/v1\/auth\/(login|refresh|logout)$/.test(request.url);
  const token = auth.accessToken();
  const requisicaoAutenticada = token && !endpointPublico
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(requisicaoAutenticada).pipe(catchError((error: HttpErrorResponse) => {
    if (error.status !== 401 || endpointPublico) {
      return throwError(() => error);
    }

    return auth.renovar().pipe(
      switchMap(() => {
        const novoToken = auth.accessToken();
        return next(novoToken
          ? request.clone({ setHeaders: { Authorization: `Bearer ${novoToken}` } })
          : request);
      }),
      catchError(refreshError => {
        auth.limparSessao();
        router.navigateByUrl('/login');
        return throwError(() => refreshError);
      })
    );
  }));
};
