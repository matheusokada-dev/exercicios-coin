import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiErrorService {
  ehErroInfra(erro: unknown): boolean {
    return erro instanceof HttpErrorResponse
      && (erro.status === 0 || erro.status === 503 || erro.status === 504);
  }

  obterMensagem(erro: unknown, mensagemPadrao: string): string {
    if (this.ehErroInfra(erro)) {
      return 'Nao foi possivel conectar ao servidor.';
    }

    if (erro instanceof HttpErrorResponse && erro.error?.msgError) {
      return erro.error.msgError;
    }

    return mensagemPadrao;
  }
}
