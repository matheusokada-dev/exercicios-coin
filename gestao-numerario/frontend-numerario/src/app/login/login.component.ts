import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  readonly formulario = new FormGroup({
    login: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/)]
    }),
    senha: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    })
  });

  erro = '';
  carregando = false;

  constructor(private auth: AuthService, private router: Router) {}

  entrar() {
    if (this.carregando) {
      return;
    }

    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      this.erro = '';
      return;
    }

    this.erro = '';
    this.carregando = true;
    const { login, senha } = this.formulario.getRawValue();

    this.auth.login(login.trim(), senha).subscribe({
      next: () => this.router.navigateByUrl('/menu'),
      error: (error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.erro = this.mensagemCredenciaisInvalidas(error);
        } else if (error.status === 400) {
          this.erro = 'Revise os campos obrigatórios e tente novamente.';
        } else if (error.status === 0) {
          this.erro = 'Não foi possível conectar ao BFF. Verifique se os serviços estão ativos.';
        } else {
          this.erro = 'Não foi possível entrar agora. Verifique a API e o BFF.';
        }
        this.carregando = false;
      }
    });
  }

  private mensagemCredenciaisInvalidas(error: HttpErrorResponse): string {
    const detalhe = error.error?.value as {
      tentativasRestantes?: number;
      bloqueadoAte?: string;
    } | undefined;

    if (detalhe?.tentativasRestantes === 0) {
      const horario = detalhe.bloqueadoAte
        ? new Date(detalhe.bloqueadoAte).toLocaleTimeString('pt-BR', {
            hour: '2-digit',
            minute: '2-digit'
          })
        : null;

      return horario
        ? `Acesso temporariamente bloqueado. Tente novamente após ${horario}.`
        : 'Acesso temporariamente bloqueado. Tente novamente mais tarde.';
    }

    if (typeof detalhe?.tentativasRestantes === 'number') {
      const quantidade = detalhe.tentativasRestantes;
      const sufixo = quantidade === 1 ? 'tentativa' : 'tentativas';
      return `Login ou senha inválidos. Restam ${quantidade} ${sufixo} antes do bloqueio.`;
    }

    return 'Login ou senha inválidos.';
  }
}
