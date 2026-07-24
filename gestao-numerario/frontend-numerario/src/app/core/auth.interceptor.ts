import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const router = inject(Router);
  const token = localStorage.getItem('numerario_access_token');
  return next(token ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request)
    .pipe(catchError(error => {
      if (error.status === 401) {
        localStorage.removeItem('numerario_access_token');
        router.navigateByUrl('/login');
      }
      return throwError(() => error);
    }));
};
