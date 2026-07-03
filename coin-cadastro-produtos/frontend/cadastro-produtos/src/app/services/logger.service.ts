import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoggerService {
  info(evento: string, dados?: unknown): void {
    console.info(`[INFO] ${evento}`, dados ?? '');
  }

  error(evento: string, erro?: unknown): void {
    console.error(`[ERROR] ${evento}`, erro ?? '');
  }
}
