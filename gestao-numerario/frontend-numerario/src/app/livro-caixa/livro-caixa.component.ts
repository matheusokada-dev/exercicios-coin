import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Workbook, Worksheet } from 'exceljs';
import { Download, LucideAngularModule } from 'lucide-angular';
import { LoadingService } from '../core/loading.service';
import { NotificationService } from '../core/notification.service';
import { AlertComponent } from '../shared/alert/alert.component';
import { BreadcrumbItem, PageHeaderComponent } from '../shared/page-header/page-header.component';

interface Agencia {
  id: number;
  codigo: string;
  nome: string;
  cidade: string;
  ativo: boolean;
}

interface Movimentacao {
  id: number;
  agenciaId: number;
  solicitacaoId: number | null;
  tipo: string;
  entrada: boolean;
  valor: number;
  saldoAnterior: number;
  saldoPosterior: number;
  descricao: string | null;
  dataMovimento: string;
  usuarioId: number;
}

interface Pagina<T> {
  itens: T[];
  pagina: number;
  tamanho: number;
  totalItens: number;
  totalPaginas: number;
}

@Component({
  selector: 'app-livro-caixa',
  standalone: true,
  imports: [AlertComponent, FormsModule, LucideAngularModule, PageHeaderComponent],
  templateUrl: './livro-caixa.component.html',
  styleUrl: './livro-caixa.component.css'
})
export class LivroCaixaComponent implements OnInit {
  readonly Download = Download;
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
      const movimentacoes = await this.carregarMovimentacoes(agencia.id);
      await this.criarArquivo(agencia, movimentacoes);
      this.notification.success(movimentacoes.length
        ? 'Livro Caixa gerado com sucesso.'
        : 'Livro Caixa gerado sem movimentações no período.');
    } catch {
      this.erro = 'Não foi possível gerar o Livro Caixa.';
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

  private buscarAgencias(pagina: number): Promise<Pagina<Agencia>> {
    const params = new HttpParams()
      .set('ativo', true)
      .set('ordenarPor', 'CODIGO')
      .set('direcao', 'ASC')
      .set('pagina', pagina)
      .set('tamanho', 100);
    return firstValueFrom(this.http.get<Pagina<Agencia>>('/api/v1/agencias', { params }));
  }

  private async carregarMovimentacoes(agenciaId: number): Promise<Movimentacao[]> {
    const primeira = await this.buscarMovimentacoes(agenciaId, 0);
    const paginas = [primeira];
    for (let pagina = 1; pagina < primeira.totalPaginas; pagina++) {
      paginas.push(await this.buscarMovimentacoes(agenciaId, pagina));
    }
    return paginas
      .flatMap(item => item.itens)
      .sort((a, b) => a.dataMovimento.localeCompare(b.dataMovimento));
  }

  private buscarMovimentacoes(agenciaId: number, pagina: number): Promise<Pagina<Movimentacao>> {
    const params = new HttpParams()
      .set('agenciaId', agenciaId)
      .set('dataInicio', this.dataInicio)
      .set('dataFim', this.dataFim)
      .set('pagina', pagina)
      .set('tamanho', 100);
    return firstValueFrom(this.http.get<Pagina<Movimentacao>>('/api/v1/movimentacoes', { params }));
  }

  private async criarArquivo(agencia: Agencia, movimentacoes: Movimentacao[]): Promise<void> {
    const workbook = new Workbook();
    workbook.creator = 'COIN Tesouraria';
    workbook.created = new Date();
    const worksheet = workbook.addWorksheet('Livro Caixa');

    worksheet.mergeCells('A1:J1');
    worksheet.getCell('A1').value = 'LIVRO CAIXA';
    worksheet.getCell('A1').font = { bold: true, size: 16, color: { argb: 'FF901B2E' } };
    worksheet.getCell('A1').alignment = { horizontal: 'center' };
    worksheet.getCell('A2').value = `Agência: ${agencia.codigo} - ${agencia.nome}`;
    worksheet.getCell('A3').value = `Período: ${formatarData(this.dataInicio)} a ${formatarData(this.dataFim)}`;

    worksheet.addRow([]);
    worksheet.addRow([
      'Data e hora',
      'Tipo',
      'Direção',
      'Descrição',
      'Solicitação',
      'Saldo anterior',
      'Entrada',
      'Saída',
      'Saldo posterior',
      'Usuário'
    ]);

    for (const movimento of movimentacoes) {
      worksheet.addRow([
        new Date(movimento.dataMovimento),
        movimento.tipo,
        movimento.entrada ? 'Entrada' : 'Saída',
        movimento.descricao ?? '',
        movimento.solicitacaoId ?? '',
        Number(movimento.saldoAnterior),
        movimento.entrada ? Number(movimento.valor) : null,
        movimento.entrada ? null : Number(movimento.valor),
        Number(movimento.saldoPosterior),
        movimento.usuarioId
      ]);
    }

    const totalRow = worksheet.addRow([
      '', '', '', 'TOTAIS', '', '',
      { formula: `SUM(G6:G${Math.max(6, worksheet.rowCount)})` },
      { formula: `SUM(H6:H${Math.max(6, worksheet.rowCount)})` },
      '', ''
    ]);
    totalRow.font = { bold: true };

    this.estilizarPlanilha(worksheet);
    const buffer = await workbook.xlsx.writeBuffer();
    baixarArquivo(
      buffer,
      `livro-caixa-${agencia.codigo}-${this.dataInicio}-${this.dataFim}.xlsx`
    );
  }

  private estilizarPlanilha(worksheet: Worksheet): void {
    const header = worksheet.getRow(5);
    header.font = { bold: true, color: { argb: 'FFFFFFFF' } };
    header.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF901B2E' } };
    header.alignment = { vertical: 'middle' };
    header.height = 24;

    worksheet.columns = [
      { width: 21 }, { width: 20 }, { width: 12 }, { width: 36 }, { width: 14 },
      { width: 18 }, { width: 16 }, { width: 16 }, { width: 18 }, { width: 12 }
    ];
    worksheet.getColumn(1).numFmt = 'dd/mm/yyyy hh:mm:ss';
    for (const coluna of [6, 7, 8, 9]) {
      worksheet.getColumn(coluna).numFmt = '"R$" #,##0.00';
    }
    worksheet.views = [{ state: 'frozen', ySplit: 5 }];
    worksheet.autoFilter = { from: 'A5', to: 'J5' };
  }
}

function formatarData(data: string): string {
  const [ano, mes, dia] = data.split('-');
  return `${dia}/${mes}/${ano}`;
}

function baixarArquivo(buffer: import('exceljs').Buffer, nome: string): void {
  const blob = new Blob([buffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = nome;
  link.click();
  URL.revokeObjectURL(url);
}
