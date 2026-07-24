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
  solicitacaoId?: number;
  tipo: TipoMovimentacao;
  entrada: boolean;
  valor: number;
  saldoAnterior: number;
  saldoPosterior: number;
  descricao?: string;
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
