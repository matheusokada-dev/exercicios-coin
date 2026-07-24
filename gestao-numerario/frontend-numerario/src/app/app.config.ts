import { ApplicationConfig, LOCALE_ID, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './core/auth.interceptor';
import { httpFeedbackInterceptor } from './core/http-feedback.interceptor';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    { provide: LOCALE_ID, useValue: 'pt-BR' },
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor, httpFeedbackInterceptor]))
  ]
};
