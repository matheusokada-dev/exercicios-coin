import { Component, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { AuthService } from '../core/auth.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PageBackComponent } from '../shared/page-back/page-back.component';

@Component({
  selector: 'app-solicitacoes',
  imports: [CurrencyPipe, FormsModule, RouterLink, PageBackComponent],
  template: `
    <div class="page-navigation">
      <p class="crumb"><a routerLink="/menu">COIN Home</a> / <a routerLink="/tesouraria">Tesouraria</a> / Solicitações</p>
      <app-page-back to="/tesouraria" />
    </div>
    <h1>Solicitações de abastecimento</h1>

    <section class="filters">
      <input [(ngModel)]="agenciaId" placeholder="ID da agencia">
      <select [(ngModel)]="status">
        <option value="">Status</option>
        <option value="PENDENTE">Pendente</option>
        <option value="APROVADA">Aprovada</option>
        <option value="REJEITADA">Rejeitada</option>
        <option value="ATENDIDA">Atendida</option>
      </select>
      <input [(ngModel)]="dataInicio" type="date" aria-label="Data inicial">
      <input [(ngModel)]="dataFim" type="date" aria-label="Data final">
      <button (click)="listar()">Pesquisar</button>
      <button class="outline" (click)="limpar()">Limpar</button>
    </section>

    <section class="form">
      <input [(ngModel)]="nova.agenciaId" type="number" min="1" placeholder="Agencia *">
      <input [(ngModel)]="nova.valor" type="number" min="0.01" step="0.01" placeholder="Valor *">
      <input [(ngModel)]="nova.motivo" placeholder="Motivo *">
      <input [(ngModel)]="nova.dataDesejada" type="date">
      <button (click)="criar()" [disabled]="salvando">Nova solicitacao +</button>
    </section>

    @if (decisao.solicitacao) {
      <section class="form decision-panel">
        <strong>{{ decisao.acao === 'aprovar' ? 'Aprovar' : 'Rejeitar' }} solicitacao #{{ decisao.solicitacao.id }}</strong>
        <input [(ngModel)]="decisao.justificativaDecisao" placeholder="Justificativa da decisao *">
        @if (decisao.acao === 'aprovar' && decisao.solicitacao.valor > 500000) {
          <input [(ngModel)]="decisao.justificativaEspecial" placeholder="Justificativa especial para valor acima de R$ 500.000 *">
        }
        <button (click)="confirmarDecisao()" [disabled]="salvando">Confirmar</button>
        <button class="outline" (click)="cancelarDecisao()">Cancelar</button>
      </section>
    }

    @if (!isGestor) {
      <p class="empty">Aprovacao e rejeicao sao exclusivas do perfil gestor.</p>
    }
    @if (erro) {
      <p class="form-error">{{ erro }}</p>
    }
    @if (sucesso) {
      <p class="form-success">{{ sucesso }}</p>
    }

    @if (carregando) {
      <p class="empty">Carregando solicitacoes...</p>
    } @else if (resultado && resultado.itens?.length) {
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Agencia</th>
              <th>Valor</th>
              <th>Data desejada</th>
              <th>Status</th>
              <th>Acoes</th>
            </tr>
          </thead>
          <tbody>
            @for (s of resultado.itens; track s.id) {
              <tr>
                <td>{{ s.agenciaId }}</td>
                <td>{{ s.valor | currency:'BRL' }}</td>
                <td>{{ s.dataDesejada }}</td>
                <td>{{ s.status }}</td>
                <td class="actions">
                  @if (s.status === 'PENDENTE' && isGestor) {
                    <button (click)="abrirDecisao(s, 'aprovar')">Aprovar</button>
                    <button class="outline" (click)="abrirDecisao(s, 'rejeitar')">Rejeitar</button>
                  }
                  @if (s.status === 'APROVADA') {
                    <button (click)="atender(s)">Atender</button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
      <section class="pagination">
        <button class="outline" (click)="paginaAnterior()" [disabled]="pagina === 0">Anterior</button>
        <span>Pagina {{ pagina + 1 }} de {{ totalPaginas }}</span>
        <button class="outline" (click)="proximaPagina()" [disabled]="pagina + 1 >= totalPaginas">Proxima</button>
      </section>
    } @else if (resultado) {
      <p class="empty">Nenhuma solicitacao encontrada para os filtros informados.</p>
    }
  `
})
export class SolicitacoesComponent implements OnInit {
  agenciaId = '';
  status = '';
  dataInicio = '';
  dataFim = '';
  pagina = 0;
  tamanho = 20;
  resultado: any;
  carregando = false;
  salvando = false;
  erro = '';
  sucesso = '';
  nova: any = { agenciaId: '', valor: '', motivo: '', dataDesejada: '' };
  decisao: any = { solicitacao: null, acao: '', justificativaDecisao: '', justificativaEspecial: '' };

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private route: ActivatedRoute
  ) {}

  get isGestor() {
    return this.auth.isGestor();
  }

  get totalPaginas() {
    return Math.max(1, this.resultado?.totalPaginas || 1);
  }

  ngOnInit() {
    this.status = this.route.snapshot.queryParamMap.get('status') || '';
    this.listar();
  }

  listar(resetPagina = true) {
    if (resetPagina) {
      this.pagina = 0;
    }

    this.erro = '';
    this.sucesso = '';
    this.carregando = true;
    let params = new HttpParams().set('pagina', this.pagina).set('tamanho', this.tamanho);
    if (this.agenciaId) params = params.set('agenciaId', this.agenciaId);
    if (this.status) params = params.set('status', this.status);
    if (this.dataInicio) params = params.set('dataInicio', this.dataInicio);
    if (this.dataFim) params = params.set('dataFim', this.dataFim);

    this.http.get<any>('/api/v1/solicitacoes', { params }).subscribe({
      next: r => {
        this.resultado = r;
        this.carregando = false;
      },
      error: e => this.falha(e, 'Nao foi possivel carregar solicitacoes.')
    });
  }

  limpar() {
    this.agenciaId = '';
    this.status = '';
    this.dataInicio = '';
    this.dataFim = '';
    this.listar();
  }

  criar() {
    this.erro = '';
    this.sucesso = '';
    this.salvando = true;
    const body = {
      ...this.nova,
      agenciaId: Number(this.nova.agenciaId),
      valor: Number(this.nova.valor)
    };

    this.http.post('/api/v1/solicitacoes', body).subscribe({
      next: () => {
        this.nova = { agenciaId: '', valor: '', motivo: '', dataDesejada: '' };
        this.salvando = false;
        this.sucesso = 'Solicitacao criada com sucesso.';
        this.listar();
      },
      error: e => {
        this.salvando = false;
        this.falha(e, 'Nao foi possivel criar a solicitacao.');
      }
    });
  }

  abrirDecisao(solicitacao: any, acao: 'aprovar' | 'rejeitar') {
    this.erro = '';
    this.sucesso = '';
    this.decisao = { solicitacao, acao, justificativaDecisao: '', justificativaEspecial: '' };
  }

  cancelarDecisao() {
    this.decisao = { solicitacao: null, acao: '', justificativaDecisao: '', justificativaEspecial: '' };
  }

  confirmarDecisao() {
    const { solicitacao, acao, justificativaDecisao, justificativaEspecial } = this.decisao;
    if (!solicitacao) {
      return;
    }

    this.salvando = true;
    const body: any = { justificativaDecisao };
    if (acao === 'aprovar') {
      body.justificativaEspecial = justificativaEspecial;
    }

    this.http.put(`/api/v1/solicitacoes/${solicitacao.id}/${acao}`, body).subscribe({
      next: () => {
        this.salvando = false;
        this.sucesso = `Solicitacao ${acao === 'aprovar' ? 'aprovada' : 'rejeitada'} com sucesso.`;
        this.cancelarDecisao();
        this.listar(false);
      },
      error: e => {
        this.salvando = false;
        this.falha(e, 'Nao foi possivel concluir a decisao.');
      }
    });
  }

  atender(s: any) {
    this.http.put(`/api/v1/solicitacoes/${s.id}/atender`, { idempotencyKey: crypto.randomUUID() }).subscribe({
      next: () => {
        this.sucesso = 'Solicitacao atendida com sucesso.';
        this.listar(false);
      },
      error: e => this.falha(e, 'Nao foi possivel atender a solicitacao.')
    });
  }

  paginaAnterior() {
    if (this.pagina > 0) {
      this.pagina -= 1;
      this.listar(false);
    }
  }

  proximaPagina() {
    if (this.pagina + 1 < this.totalPaginas) {
      this.pagina += 1;
      this.listar(false);
    }
  }

  private falha(error: HttpErrorResponse, fallback: string) {
    this.erro = error.error?.msgError || error.error?.message || fallback;
    this.carregando = false;
  }
}
