import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const read = path => readFileSync(resolve(path), 'utf8');
const routes = read('src/app/app.routes.ts');
const component = read('src/app/components/pages/solicitacoes/consulta/solicitacoes.component.ts');
const template = read('src/app/components/pages/solicitacoes/consulta/solicitacoes.component.html');
const pagination = read('src/app/components/shared/pagination/pagination.component.html');

const expectedRoutes = [
  "path: 'solicitacoes'",
  "path: 'solicitacoes/consultar'",
  "path: 'solicitacoes/nova'"
];
const expectedEndpoints = [
  "aprovar:'aprovar'",
  "rejeitar:'rejeitar'",
  "cancelar:'cancelar'",
  "programar:'programar'",
  "separar:'iniciar-separacao'",
  "expedir:'expedir'",
  "ocorrencia:'registrar-ocorrencia'",
  "receber:'receber'",
  "conciliar:'conciliar'",
  "ajustar:'ajustes-divergencia'"
];

for (const route of expectedRoutes) {
  if (!routes.includes(route) || !routes.match(new RegExp(`${route.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}[^\\n]+gestorGuard`))) {
    throw new Error(`Rota ausente ou sem gestorGuard: ${route}`);
  }
}
for (const endpoint of expectedEndpoints) {
  if (!component.includes(endpoint)) throw new Error(`Mapeamento ausente: ${endpoint}`);
}
for (const fragment of ['<th>Agência origem</th>', '<th>Agência destino</th>', 'Divergência prevista']) {
  if (!template.includes(fragment)) throw new Error(`Elemento do fluxo ausente: ${fragment}`);
}
if (!component.includes("if(!id)return '-'")) {
  throw new Error('Unidade ainda não definida deve ser exibida como “-”.');
}
if (pagination.includes('@if (totalPages > 1)') || !pagination.includes('>Anterior</span>')) {
  throw new Error('Navegação da paginação deve permanecer visível quando existe apenas uma página.');
}

console.log('Fluxo de solicitações verificado: rotas, códigos, paginação, endpoints e recebimento.');
