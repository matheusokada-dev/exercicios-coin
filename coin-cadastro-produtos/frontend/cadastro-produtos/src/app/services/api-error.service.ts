import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ApiErrorService {
  obterMensagem(erro: unknown, mensagemPadrao: string): string {
    if (erro instanceof HttpErrorResponse && erro.status === 0) {
      return 'Não foi possível conectar ao servidor.';
    }

    if (erro instanceof HttpErrorResponse && erro.error?.msgError) {
      return erro.error.msgError;
    }

    return mensagemPadrao;
  }
}
