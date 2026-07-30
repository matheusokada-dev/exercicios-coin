import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpContext, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Download, LucideAngularModule } from 'lucide-angular';
import { SILENCIAR_NOTIFICACOES_HTTP } from '../../../interceptors/http-feedback.interceptor';
import { Agencia, GerarRelatorioResponse, PaginaResponse } from '../../../models/api.models';
import { LoadingService } from '../../../services/loading.service';
import { NotificationService } from '../../../services/notification.service';
import { AlertComponent } from '../../shared/alert/alert.component';
import { BreadcrumbItem, PageHeaderComponent } from '../../shared/page-header/page-header.component';

@Component({
  selector: 'app-livro-caixa',
  standalone: true,
  imports: [AlertComponent, FormsModule, LucideAngularModule, PageHeaderComponent],
  templateUrl: './livro-caixa.component.html',
  styleUrl: './livro-caixa.component.css'
})
export class LivroCaixaComponent implements OnInit {
  readonly Download = Download;
  readonly hoje = dataLocalIso(new Date());
  readonly breadcrumbs: BreadcrumbItem[] = [
    { label: 'COIN Home', link: '/menu' },
    { label: 'Tesouraria', link: '/tesouraria' },
    { label: 'Livro Caixa' }
  ];

  agencias: Agencia[] = [];
  agenciaId = '';
  dataInicio = '';
  dataFim = '';
  gerando = false;
  erro = '';

  constructor(
    private readonly http: HttpClient,
    private readonly loading: LoadingService,
    private readonly notification: NotificationService
  ) {}

  ngOnInit(): void {
    void this.carregarAgencias();
  }

  async gerar(): Promise<void> {
    this.erro = this.validar();
    if (this.erro) {
      this.notification.error(this.erro);
      return;
    }

    const agencia = this.agencias.find(item => item.id === Number(this.agenciaId));
    if (!agencia) {
      this.erro = 'Selecione uma agência válida.';
      this.notification.error(this.erro);
      return;
    }

    this.gerando = true;
    this.loading.iniciar();
    try {
      const relatorio = await firstValueFrom(this.http.post<GerarRelatorioResponse>(
        '/api/v1/relatorios/livro-caixa',
        {
          agenciaId: agencia.id,
          dataInicio: this.dataInicio,
          dataFim: this.dataFim
        },
        {
          context: new HttpContext().set(SILENCIAR_NOTIFICACOES_HTTP, true)
        }
      ));
      baixarArquivo(relatorio.conteudo, relatorio.nomeArquivo);
      this.notification.success('Livro Caixa gerado com sucesso.');
    } catch (error) {
      this.erro = mensagemErro(error);
      this.notification.error(this.erro);
    } finally {
      this.gerando = false;
      this.loading.finalizar();
    }
  }

  private validar(): string {
    if (!this.agenciaId || !this.dataInicio || !this.dataFim) {
      return 'Informe a agência, a data inicial e a data final.';
    }
    if (this.dataFim < this.dataInicio) {
      return 'A data final não pode ser anterior à data inicial.';
    }
    if (this.dataFim > this.hoje) {
      return 'A data final não pode ser maior que a data de hoje.';
    }
    return '';
  }

  private async carregarAgencias(): Promise<void> {
    try {
      const primeira = await this.buscarAgencias(0);
      const paginas = [primeira];
      for (let pagina = 1; pagina < primeira.totalPaginas; pagina++) {
        paginas.push(await this.buscarAgencias(pagina));
      }
      this.agencias = paginas
        .flatMap(item => item.itens)
        .filter(agencia => agencia.ativo)
        .sort((a, b) => a.codigo.localeCompare(b.codigo));
    } catch {
      this.erro = 'Não foi possível carregar as agências.';
    }
  }

  private buscarAgencias(pagina: number): Promise<PaginaResponse<Agencia>> {
    const params = new HttpParams()
      .set('ativo', true)
      .set('ordenarPor', 'CODIGO')
      .set('direcao', 'ASC')
      .set('pagina', pagina)
      .set('tamanho', 100);
    return firstValueFrom(this.http.get<PaginaResponse<Agencia>>('/api/v1/agencias', { params }));
  }
}

function dataLocalIso(data: Date): string {
  const ano = data.getFullYear();
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${ano}-${mes}-${dia}`;
}

function mensagemErro(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Não foi possível gerar o Livro Caixa.';
  }
  return error.error?.msgError
    || error.error?.message
    || error.error?.detail
    || 'Não foi possível gerar o Livro Caixa.';
}

function baixarArquivo(conteudoBase64: string, nome: string): void {
  if (!conteudoBase64?.trim()) {
    throw new Error('O serviço de relatórios retornou um arquivo vazio.');
  }
  const binario = atob(conteudoBase64.replace(/\s/g, ''));
  const bytes = new Uint8Array(binario.length);
  for (let indice = 0; indice < binario.length; indice++) {
    bytes[indice] = binario.charCodeAt(indice);
  }
  const blob = new Blob([bytes], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = nome.endsWith('.xlsx') ? nome : `${nome}.xlsx`;
  link.style.display = 'none';
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.setTimeout(() => URL.revokeObjectURL(url), 1_000);
}
