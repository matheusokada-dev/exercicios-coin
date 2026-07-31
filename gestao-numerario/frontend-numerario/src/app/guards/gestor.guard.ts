import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, map, of } from 'rxjs';

export const gestorGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.carregarSessao().pipe(
    map(() => auth.isGestor()
      ? true
      : router.createUrlTree(['/erro'], { queryParams: { tipo: 'acesso' } })),
    catchError(() => of(router.createUrlTree(['/login'])))
  );
};
