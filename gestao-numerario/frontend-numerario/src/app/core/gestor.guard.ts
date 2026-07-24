import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const gestorGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.isGestor()
    ? true
    : router.createUrlTree(['/erro'], { queryParams: { tipo: 'acesso' } });
};
