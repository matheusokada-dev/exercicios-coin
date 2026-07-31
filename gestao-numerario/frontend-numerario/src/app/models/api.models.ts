export interface PaginaResponse<T> {
  itens: T[];
  pagina: number;
  tamanho: number;
  totalItens: number;
  totalPaginas: number;
}

export interface Agencia {
  id: number;
  codigo: string;
  nome: string;
  cidade: string;
  saldoAtual: number;
  limiteMinimo: number;
  ativo: boolean;
  abaixoDoLimite: boolean;
  sugestaoAbastecimento: number;
  versao: number;
}

export interface DetalheAgencia {
  agencia: Agencia;
  dataReferencia: string;
  valorEntradasHoje: number;
  valorSaidasHoje: number;
  valorAbastecimentoAprovado: number;
  saldoPrevistoAposAbastecimentoAprovado: number;
}

export interface GerarRelatorioResponse {
  conteudo: string;
  nomeArquivo: string;
  formato: string;
  dataGeracao: string;
}

export interface CriarAgenciaRequest {
  codigo: string;
  nome: string;
  cidade: string;
  saldoAtual: number | string;
  limiteMinimo: number | string;
}

export interface AtualizarAgenciaRequest {
  nome: string;
  cidade: string;
  limiteMinimo: number;
}

export type StatusSolicitacao = 'PENDENTE' | 'APROVADA' | 'REJEITADA' | 'ATENDIDA';

export interface Solicitacao {
  id: number;
  agenciaId: number;
  valor: number;
  motivo: string;
  dataDesejada: string;
  status: StatusSolicitacao;
  solicitanteId: number;
  decisorId?: number;
  justificativaDecisao?: string;
  justificativaEspecial?: string;
  dataCriacao: string;
  dataDecisao?: string;
  dataAtendimento?: string;
  versao: number;
}

export type TipoMovimentacao =
  | 'ABASTECIMENTO'
  | 'DEPOSITO'
  | 'RECOLHIMENTO'
  | 'SAQUE'
  | 'AJUSTE';

export interface Movimentacao {
  id: number;
  agenciaId: number;
  solicitacaoId: number | null;
  tipo: TipoMovimentacao;
  entrada: boolean;
  valor: number;
  saldoAnterior: number;
  saldoPosterior: number;
  descricao: string | null;
  dataMovimento: string;
  usuarioId: number;
}

export interface DashboardResponse {
  dataReferencia: string;
  numerarioTotal: number;
  quantidadeAgenciasEmAlerta: number;
  quantidadeSolicitacoesPendentes: number;
  quantidadeAbastecimentosHoje: number;
  valorAbastecidoHoje: number;
}

export type TipoOperacaoNumerario = 'SUPRIMENTO' | 'RECOLHIMENTO';
export type StatusSolicitacaoNumerario =
  | 'PENDENTE' | 'APROVADA' | 'REJEITADA' | 'CANCELADA'
  | 'EM_EXECUCAO' | 'COM_DIVERGENCIA' | 'CONCLUIDA';
export type StatusOperacaoNumerario =
  | 'PROGRAMADA' | 'EM_SEPARACAO' | 'EM_TRANSITO'
  | 'RECEBIDA' | 'COM_DIVERGENCIA' | 'CONCILIADA';

export interface SolicitacaoNumerario {
  id: number;
  tipoOperacao: TipoOperacaoNumerario;
  agenciaId: number;
  origemId?: number;
  destinoId?: number;
  valorSolicitado: number;
  motivo: string;
  dataDesejada: string;
  status: StatusSolicitacaoNumerario;
  solicitanteId: number;
  aprovadorId?: number;
  justificativaDecisao?: string;
  dataCriacao: string;
  dataDecisao?: string;
  dataConclusao?: string;
  versao: number;
}

export interface OperacaoNumerario {
  id: number;
  status: StatusOperacaoNumerario;
  origemId: number;
  destinoId: number;
  valorProgramado: number;
  valorExpedido?: number;
  valorRecebido?: number;
  valorDivergencia?: number;
  versao: number;
}

export interface HistoricoNumerario {
  id: number;
  evento: string;
  statusAnterior?: string;
  statusNovo?: string;
  usuarioId: number;
  dataEvento: string;
  justificativa?: string;
}

export interface DetalheSolicitacaoNumerario {
  solicitacao: SolicitacaoNumerario;
  operacao?: OperacaoNumerario;
  historico: HistoricoNumerario[];
}

export interface UnidadeOperacional {
  id: number;
  tipo: string;
  codigo: string;
  nome: string;
  saldoAtual: number;
  versao: number;
  atualizadoEm: string;
}
