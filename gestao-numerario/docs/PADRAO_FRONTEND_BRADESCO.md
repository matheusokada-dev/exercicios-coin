# Padrão de frontend Bradesco

## Objetivo

Este documento é a referência de design e implementação do frontend do projeto
Gestão de Numerário. Ele transforma o material do Liquid Bradesco fornecido pelo
usuário em regras rastreáveis para o Angular, sem apresentar como oficial aquilo
que não está presente na fonte.

Aplicação inicial:

- `frontend-numerario`;
- novas telas e novos componentes;
- revisão progressiva das telas existentes;
- análise visual e revisão de código do frontend.

## Fonte e precedência

Fonte recebida em 24/07/2026:

- anexo `pasted-text.txt`, com documentação de Pagination, Chart Donut, Alert,
  Modal Dialog e Typography do Liquid Bradesco.

Precedência:

1. documentação Bradesco/Liquid fornecida pelo usuário;
2. decisões explícitas registradas neste projeto;
3. adaptações técnicas necessárias ao Angular;
4. soluções provisórias, sempre identificadas como provisórias.

Os nomes `brad-*` abaixo são preservados para manter rastreabilidade com a
documentação original. Isso não significa que a biblioteca Liquid esteja
instalada no projeto.

## Estado da referência recebida

| Área | Cobertura da fonte | Situação no projeto |
| --- | --- | --- |
| Tipografia | Família, tamanhos, pesos e alturas de linha | A adotar |
| Paginação | Estrutura, opções, evento e acessibilidade | Implementação atual deve ser substituída |
| Gráfico donut | Estrutura, dados, cores, legendas, carregamento e acessibilidade | Disponível para uso futuro |
| Alertas | Estrutura, variantes, propósito e acessibilidade | Toast e erros devem ser revisados |
| Modal dialog | Estrutura, métodos e acessibilidade | Disponível para uso futuro |
| Cards | Não documentado no anexo | Pendente de fonte oficial |
| Paleta institucional primária | Não documentada no anexo | Pendente de fonte oficial |
| Espaçamentos, bordas e sombras | Não documentados no anexo | Pendente de fonte oficial |
| Botões, campos, tabelas e navegação | Não documentados no anexo | Pendente de fonte oficial |

## Regra de adaptação para Angular

Enquanto o pacote Liquid não estiver instalado e aprovado:

- reproduzir comportamento, semântica e acessibilidade em componentes Angular;
- não copiar inicializadores globais `LiquidCorp.*` para dentro de componentes;
- encapsular cada padrão em componente reutilizável;
- expor dados por `@Input` e eventos por `@Output`;
- destruir listeners ou instâncias de bibliotecas no ciclo de destruição;
- carregar dependências pesadas de gráfico somente quando a tela precisar delas;
- manter nomes de tokens internos rastreáveis ao padrão `brad-*`;
- não criar variações visuais locais sem registro neste documento.

Se a biblioteca Liquid oficial for incorporada futuramente, revisar esta camada
de adaptação e evitar duas implementações concorrentes do mesmo componente.

## Fundamentos

### Tipografia

Família oficial informada:

```css
font-family: "Bradesco", sans-serif;
```

O arquivo da fonte Bradesco e sua licença de distribuição não foram fornecidos.
Até que o ativo oficial seja incluído no projeto, a declaração deve possuir
fallback seguro. A ausência do arquivo não autoriza substituir a fonte por uma
imitação.

#### Títulos

| Token Liquid | Tamanho | Peso | Altura de linha |
| --- | ---: | ---: | ---: |
| `brad-font-title-xl` | 22px / 1.375rem | 600 | 32px / 2rem |
| `brad-font-title-lg` | 20px / 1.25rem | 600 | 32px / 2rem |
| `brad-font-title-md` | 16px / 1rem | 600 | 20px / 1.25rem |
| `brad-font-title-sm` | 14px / 0.875rem | 600 | 20px / 1.25rem |

#### Subtítulos

| Token Liquid | Tamanho | Peso | Altura de linha |
| --- | ---: | ---: | ---: |
| `brad-font-subtitle-sm` | 14px / 0.875rem | 600 | 20px / 1.25rem |
| `brad-font-subtitle-xs` | 12px / 0.75rem | 600 | 16px / 1rem |
| `brad-font-subtitle-xxs` | 10px / 0.625rem | 600 | 16px / 1rem |

#### Parágrafos

| Token Liquid | Tamanho | Peso | Altura de linha |
| --- | ---: | ---: | ---: |
| `brad-font-paragraph-md` | 16px / 1rem | 500 | 20px / 1.25rem |
| `brad-font-paragraph-sm` | 14px / 0.875rem | 500 | 16px / 1rem |

#### Links

| Token Liquid | Tamanho | Peso | Altura de linha |
| --- | ---: | ---: | ---: |
| `brad-font-link-md` | 16px / 1rem | 600 | 20px / 1.25rem |
| `brad-font-link-sm` | 14px / 0.875rem | 600 | 16px / 1rem |

#### Pesos

| Token Liquid | Valor |
| --- | ---: |
| `brad-font-weight-regular` | 400 |
| `brad-font-weight-medium` | 500 |
| `brad-font-weight-semibold` | 600 |
| `brad-font-weight-bold` | 700 |

#### Regras do projeto

- Não criar tamanhos intermediários sem uma necessidade registrada.
- Títulos de página devem usar um token de título, não valores fluidos
  arbitrários.
- Rótulos não devem depender de caixa alta e espaçamento de letras para criar
  hierarquia.
- Valores monetários no Angular usam `CurrencyPipe`, locale `pt-BR` e moeda
  `BRL`.
- Textos operacionais devem ser diretos; evitar slogans e explicações
  decorativas.

### Cores

A fonte recebida documenta apenas cores disponíveis para setores de gráficos:

| Token | RGBA |
| --- | --- |
| `brad-color-extended-blue` | `rgba(59, 105, 255, 1)` |
| `brad-color-extended-purple` | `rgba(115, 48, 139, 1)` |
| `brad-color-extended-green` | `rgba(9, 171, 72, 1)` |
| `brad-color-extended-violet` | `rgba(180, 26, 131, 1)` |
| `brad-color-extended-salmon` | `rgba(243, 98, 121, 1)` |
| `brad-color-neutral-40` | `rgba(109, 110, 113, 1)` |
| `brad-color-extended-red` | `rgba(225, 23, 63, 1)` |

Essas cores não constituem a paleta institucional completa e não devem ser
promovidas automaticamente a cor primária de botões, headers, cards ou links.

Até o recebimento da paleta oficial:

- usar superfícies neutras e contraste legível;
- restringir cores estendidas a visualização de dados;
- usar cor semântica apenas quando existir significado;
- não usar gradientes decorativos;
- não atribuir uma cor diferente a cada card apenas para ornamentação;
- não comunicar estado somente por cor.

### Cards

O anexo não contém especificação oficial de cards. Portanto, qualquer card atual
é uma adaptação provisória.

Direção provisória aprovada para evitar aparência genérica:

- superfície neutra;
- borda e sombra discretas;
- hierarquia derivada da tipografia oficial;
- ícone somente quando melhora reconhecimento da ação;
- sem gradiente decorativo;
- sem faixa colorida sem significado;
- sem repetir em outro card a mesma informação;
- priorizar conteúdo operacional e caminhos de ação.

Esta seção deve ser substituída quando a documentação oficial de Card for
fornecida.

## Componentes

### Paginação

Propósito: organizar dados em páginas sequenciais ou em exibição gradativa,
melhorando o consumo de grandes volumes.

Estrutura original:

```html
<div id="brad-pagination" class="brad-pagination">
  <ul class="brad-pagination__pages brad-flex-justify-content-center"></ul>
</div>
```

Contrato documentado:

| Opção | Tipo | Regra |
| --- | --- | --- |
| `targetSelector` | `string` | ID ou classe do container |
| `currentPage` | `number` | Página inicialmente ativa |
| `totalPages` | `number` | Quantidade total de páginas |
| `countNumbersStart` | `number` | Quantidade de botões numéricos apresentados |
| `isIndeterminate` | `boolean` | Ativa versão indeterminada; padrão `false` |

Evento `pageChanges`:

```typescript
interface PageChanges {
  previousAccessedPage: number;
  currentPage: number;
  elementClicked: HTMLElement;
}
```

Regras para o Angular:

- criar um único componente compartilhado de paginação;
- receber página atual, total, quantidade de números e modo indeterminado;
- emitir mudança de página sem acoplar o componente à chamada HTTP;
- oferecer versão compacta com rótulo “Página X de Y”;
- suportar atualização dinâmica do total de páginas;
- desabilitar corretamente anterior/próxima nos limites;
- não adicionar `tabindex="0"` indiscriminadamente à estrutura inicial;
- preservar ordem de foco e leitura criadas pelos elementos interativos.

### Chart Donut

Propósito: representar partes de um todo. Não usar donut para tendência no tempo,
comparação precisa entre muitas categorias ou decoração.

Estrutura:

```html
<div class="brad-chart">
  <div class="chart-container">
    <div class="inside-legend">
      <p class="brad-font-paragraph-sm">Total</p>
      <p class="brad-font-title-sm">R$ 30.000,00</p>
    </div>
    <canvas></canvas>
  </div>
</div>
```

`inside-legend` é opcional.

Estrutura de dados:

```typescript
interface BradDonutItem {
  valueLabel: string;
  value: number;
  color: string;
  supportingText?: string;
}
```

Opções documentadas:

| Opção | Tipo | Regra |
| --- | --- | --- |
| `targetSelector` | `string` | Container associado |
| `itens` | `BradDonutItem[]` | Setores do gráfico |
| `type` | `string` | Obrigatoriamente `donut` |
| `border` | `boolean` | Espaçamento entre setores; padrão `true` |
| `legendType` | `string` | Posição da legenda |
| `showLegendPercentage` | `boolean` | Exibição de porcentagem; padrão `true` |
| `moneyInTooltip` | `boolean` | Formatação monetária no tooltip; padrão `false` |

Posições de legenda aceitas:

- `horizontal-top`, `vertical-top`;
- `horizontal-right`, `vertical-right`;
- `horizontal-bottom`, `vertical-bottom`;
- `horizontal-left`, `vertical-left`.

Regras técnicas:

- o Chart.js foi removido do bundle principal da Liquid;
- inicialização é assíncrona por `Promise` ou `async/await`;
- carregar o gráfico por lazy loading/code splitting;
- usar `getInstances` para múltiplos gráficos;
- chamar `destroy` quando a instância não for mais usada;
- limpar listeners globais quando aplicável;
- legenda interna pode apresentar total, valor e data de atualização;
- leitor de tela deve acessar cada setor e cada item da legenda;
- valores monetários visíveis no Angular continuam usando `CurrencyPipe`.

### Alert

Propósito documentado: notificar informações urgentes que exigem atenção imediata
ou apoiar ações irreversíveis. Alert não deve substituir texto informativo comum.

Variantes:

| Tipo | Classe |
| --- | --- |
| Informação | `brad-alert--info` |
| Sucesso | `brad-alert--success` |
| Atenção | `brad-alert--warning` |
| Erro | `brad-alert--error` |

Estrutura semântica:

- ícone;
- conteúdo;
- título opcional;
- corpo opcional;
- ação/link opcional.

Regras:

- sem variante, o alerta fica transparente e espera um background explícito;
- cor nunca é a única forma de transmitir o tipo;
- título e corpo devem explicar o estado em texto;
- ações internas devem ser acessíveis por teclado;
- toasts continuam adequados para feedback transitório;
- alertas persistentes são usados quando a informação precisa permanecer na tela.

### Modal Dialog

O Modal Dialog possui estrutura pré-definida:

- topo;
- cabeçalho com título;
- parágrafo descritivo opcional;
- conteúdo principal;
- rodapé com ações secundária e primária.

Métodos documentados:

- `open`;
- `close`;
- `toggle`;
- `destroy`;
- `getInstance`;
- `getInstances`.

Regras de acessibilidade:

- container com `role="dialog"`;
- `aria-modal="true"`;
- nome acessível por `aria-label` ou associação com o título;
- container inicialmente focável com `tabindex="-1"`;
- conteúdo atrás do modal indisponível para leitores de tela enquanto aberto;
- modal deve ficar fora de um ancestral marcado com `aria-hidden`;
- foco deve entrar no diálogo quando aberto;
- foco deve retornar ao elemento acionador ao fechar;
- listeners e referências devem ser removidos ao destruir o componente.

No Angular, preferir um componente/serviço compartilhado que controle foco,
backdrop, fechamento por teclado e restauração de foco de forma centralizada.

## Acessibilidade transversal

- Não depender apenas de cor, posição ou ícone.
- Todo controle deve possuir nome acessível.
- Ordem de tabulação deve seguir a ordem visual e semântica.
- Não adicionar `tabindex="0"` a elementos não interativos.
- Componentes dinâmicos devem gerenciar entrada e retorno de foco.
- Gráficos precisam de alternativa textual com rótulo, valor e relação com o
  total.
- Estados de carregamento e erro devem ser compreensíveis sem animação.
- Respeitar `prefers-reduced-motion`.

## Aplicação no frontend atual

### Prioridade 1 — fundamentos

1. Disponibilizar o arquivo oficial da fonte Bradesco, com licença de uso.
2. Criar tokens CSS de tipografia com os valores deste documento.
3. Receber a paleta institucional oficial antes de redefinir a cor primária.
4. Remover gradientes e cores ornamentais que não expressem estado.

### Prioridade 2 — componentes compartilhados

1. Criar componente de paginação e substituir paginações duplicadas.
2. Criar componente de alert persistente.
3. Criar modal dialog acessível antes de novos fluxos de confirmação.
4. Criar wrapper lazy de donut somente quando houver dado parte/todo aprovado.

### Prioridade 3 — revisão das telas

1. Dashboard.
2. Agências e detalhe de agência.
3. Solicitações.
4. Movimentações.
5. Livro Caixa.
6. Login, COIN Home e menus.

## Checklist de revisão

- A tipografia corresponde a um token documentado?
- A fonte oficial está disponível ou o fallback está explícito?
- A cor usada possui significado e origem documentada?
- O mesmo dado está sendo repetido sem acrescentar ação ou contexto?
- O componente já existe na camada compartilhada?
- O componente funciona por teclado?
- O significado continua claro sem cor?
- O foco é preservado em modais e elementos dinâmicos?
- Valores monetários usam `CurrencyPipe` com `BRL`?
- Gráficos representam uma relação adequada e possuem alternativa textual?
- O layout funciona em 320px sem rolagem horizontal?

## Pendências documentais

Ainda são necessárias fontes Bradesco/Liquid para:

- Card;
- paleta institucional completa;
- Grid e espaçamento;
- Button;
- Input, Select, Checkbox e Datepicker;
- Table;
- Breadcrumb;
- Header e navegação;
- Toast;
- Loading;
- Empty state;
- Tooltip;
- ícones e regras de licenciamento;
- breakpoints responsivos.

Até receber essas referências, qualquer regra nessas áreas permanece uma
adaptação provisória do projeto.
