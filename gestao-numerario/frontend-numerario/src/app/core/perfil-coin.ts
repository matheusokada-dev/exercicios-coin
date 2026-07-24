export type PerfilCoinCodigo =
  | 'COIN0001'
  | 'COIN0002'
  | 'COIN0003'
  | 'COIN0004'
  | 'COIN0005'
  | 'COIN0006';

export interface PerfilCoin {
  codigo: PerfilCoinCodigo;
  nome: string;
}

export const PERFIS_COIN: Record<PerfilCoinCodigo, PerfilCoin> = {
  COIN0001: { codigo: 'COIN0001', nome: 'Master' },
  COIN0002: { codigo: 'COIN0002', nome: 'Analista' },
  COIN0003: { codigo: 'COIN0003', nome: 'Consulta' },
  COIN0004: { codigo: 'COIN0004', nome: 'Planejamento de Tesouraria' },
  COIN0005: { codigo: 'COIN0005', nome: 'Contabilidade' },
  COIN0006: { codigo: 'COIN0006', nome: 'Cadastro' }
};

export function normalizarPerfilCoin(valor: string): PerfilCoinCodigo | null {
  const perfil = valor.toUpperCase();
  const codigo = (Object.keys(PERFIS_COIN) as PerfilCoinCodigo[])
    .find(item => perfil.includes(item));

  if (codigo) {
    return codigo;
  }

  // Compatibilidade temporaria com os JWTs emitidos pela API local.
  if (perfil.includes('GESTOR')) {
    return 'COIN0001';
  }
  if (perfil.includes('OPERADOR')) {
    return 'COIN0003';
  }
  return null;
}
