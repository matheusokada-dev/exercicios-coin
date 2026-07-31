import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const router = inject(Router);
  const auth = inject(AuthService);
  const endpointPublico = request.url.endsWith('/api/v1/auth/login');
  const token = auth.accessToken();
  const requisicaoAutenticada = token && !endpointPublico
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(requisicaoAutenticada).pipe(catchError((error: HttpErrorResponse) => {
    if (error.status !== 401 || endpointPublico) {
      return throwError(() => error);
    }

    auth.limparSessao();
    router.navigateByUrl('/login');
    return throwError(() => error);
  }));
};
