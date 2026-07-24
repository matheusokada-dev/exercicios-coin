import { Injectable, computed, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly requisicoesAtivas = signal(0);

  readonly visivel = computed(() => this.requisicoesAtivas() > 0);

  iniciar() {
    this.requisicoesAtivas.update(total => total + 1);
  }

  finalizar() {
    this.requisicoesAtivas.update(total => Math.max(0, total - 1));
  }
}
