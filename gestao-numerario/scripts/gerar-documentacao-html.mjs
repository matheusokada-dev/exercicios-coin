import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "..");
const docsDir = path.join(root, "docs");
const generatedAt = new Intl.DateTimeFormat("pt-BR", {
  dateStyle: "long",
  timeStyle: "short",
}).format(new Date());

const ignored = new Set([
  ".git", "node_modules", "target", "dist", "coverage", ".angular", ".idea", ".vscode",
]);
const extensions = new Set([
  ".java", ".ts", ".html", ".scss", ".css", ".xml", ".properties", ".json", ".md", ".yml", ".yaml",
]);
const esc = (value = "") => String(value)
  .replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
  .replaceAll('"', "&quot;").replaceAll("'", "&#39;");
const rel = (file) => path.relative(root, file).replaceAll("\\", "/");

const applications = [
  {
    key: "frontend",
    name: "Frontend Angular",
    port: 4200,
    directory: "frontend-numerario",
    output: "DOCUMENTACAO_FRONTEND_4200.html",
    stack: "Angular · TypeScript · HTML · CSS",
    role: "Interface utilizada pelo operador. Apresenta telas, valida formulários, controla navegação e consome exclusivamente o BFF.",
    boundary: "Não acessa banco, API de domínio ou Apache POI diretamente. Toda comunicação de negócio sai por HTTP para a porta 8080.",
    entries: [
      ["Navegação", "Rotas Angular", "Carrega telas públicas e autenticadas; guards impedem navegação incompatível com a sessão."],
      ["Integração", "HttpClient → :8080/api/v1", "Serviços tipados enviam filtros e comandos ao BFF."],
      ["Relatório", "POST /api/v1/relatorios/livro-caixa", "Recebe Base64 do BFF, converte para Blob e inicia o download do XLSX."],
    ],
    flow: [
      ["1. Bootstrap", "main.ts inicia o Angular com app.config.ts, roteamento, interceptors e providers globais."],
      ["2. Navegação", "app.routes.ts resolve a URL; guards consultam AuthService antes de liberar telas protegidas."],
      ["3. Interação", "O componente mantém estado visual, coleta filtros ou formulário e chama um serviço Angular."],
      ["4. Requisição", "AuthInterceptor inclui Authorization: Bearer quando existe sessão; HttpFeedbackInterceptor controla loading e erros."],
      ["5. BFF", "O navegador envia a requisição somente para localhost:8080, preservando o contrato /api/v1."],
      ["6. Resposta", "O serviço converte o JSON em modelos TypeScript e o componente atualiza tabela, cards, modal ou paginação."],
      ["7. Excel", "No Livro Caixa, o Base64 devolvido pelo BFF vira um Blob XLSX; o navegador dispara o download sem biblioteca JS de planilha."],
    ],
  },
  {
    key: "bff",
    name: "BFF Numerário",
    port: 8080,
    directory: "bff-numerario",
    output: "DOCUMENTACAO_BFF_8080.html",
    stack: "Java 21 · Spring Boot · REST Client · OpenAPI",
    role: "Backend dedicado ao Angular. Expõe contratos adequados às telas, encaminha autenticação e orquestra API e relatórios.",
    boundary: "Não persiste o domínio e não gera XLSX. Consulta a API na porta 8081 e delega a montagem do arquivo à porta 8082.",
    entries: [
      ["Frontend", "/api/v1/**", "Recebe todas as consultas e comandos do Angular."],
      ["OpenAPI", "/swagger-ui.html e /v3/api-docs", "Publica 32 operações documentadas por interfaces *Api."],
      ["Relatório", "POST /api/v1/relatorios/livro-caixa", "Consulta movimentos na API, monta o contrato tabular e chama o microsserviço 8082."],
    ],
    flow: [
      ["1. Entrada HTTP", "O controller Spring recebe JSON, query params, Authorization e, quando aplicável, Idempotency-Key."],
      ["2. Contrato", "A interface *Api concentra @Tag e @Operation; o controller implementa o contrato e conserva os mappings."],
      ["3. Validação", "Bean Validation rejeita campos inválidos antes da orquestração."],
      ["4. Serviço", "A implementação delega ao service correspondente, que define a sequência necessária para atender a tela."],
      ["5. Cliente API", "Clients chamam localhost:8081, encaminham Bearer e preservam status e payloads de erro."],
      ["6. Relatórios", "RelatorioLivroCaixaService pagina todas as movimentações, totaliza os dados e chama localhost:8082."],
      ["7. Saída", "O BFF devolve DTOs próprios ao Angular; falhas de dependência são traduzidas para respostas padronizadas."],
    ],
  },
  {
    key: "api",
    name: "API Numerário",
    port: 8081,
    directory: "api-numerario",
    output: "DOCUMENTACAO_API_8081.html",
    stack: "Java 21 · Spring Boot · Arquitetura hexagonal · JPA · MySQL",
    role: "Autoridade do domínio. Aplica autenticação, regras, transações, idempotência, persistência e auditoria financeira.",
    boundary: "Não conhece telas Angular nem formata arquivos Excel. Expõe dados e comandos de negócio para o BFF.",
    entries: [
      ["BFF", "/api/v1/**", "Recebe consultas e comandos encaminhados pela porta 8080."],
      ["OpenAPI", "/swagger-ui.html e /v3/api-docs", "Publica 34 operações documentadas por interfaces *Api."],
      ["Persistência", "MySQL + Flyway", "Mantém agências, usuários, solicitações, operações, movimentos, histórico e idempotência."],
    ],
    flow: [
      ["1. Filtros", "CorrelationIdFilter identifica a chamada; JwtAuthenticationFilter valida o Bearer e cria UsuarioAutenticado."],
      ["2. Controller", "O controller recebe o contrato HTTP, aplica validação e converte DTOs por meio dos mappers REST."],
      ["3. Porta de entrada", "O controller chama uma interface InputPort, evitando dependência direta da implementação de negócio."],
      ["4. Caso de uso", "O use case valida perfil, estado, versão, valor, saldo e demais invariantes do domínio."],
      ["5. Transação", "Comandos financeiros persistem estado, movimentação e histórico de forma atômica; chaves impedem duplicidade."],
      ["6. Porta de saída", "O núcleo chama OutputPorts; adapters JPA convertem domínio em entidades e executam repositories."],
      ["7. Resposta", "O resultado retorna pelo mapper REST; handlers padronizam erros 400, 401, 403, 404, 409, 422 e 500."],
    ],
  },
  {
    key: "report",
    name: "Relatório Numerário",
    port: 8082,
    directory: "relatorio-numerario",
    output: "DOCUMENTACAO_RELATORIO_8082.html",
    stack: "Java 21 · Spring Boot · Apache POI",
    role: "Microsserviço especializado em transformar dados tabulares já preparados em arquivos Excel XLSX.",
    boundary: "Não consulta banco nem conhece regras de saldo. Recebe título, colunas, linhas, totais e metadados do BFF.",
    entries: [
      ["BFF", "POST /v1/relatorios/gerar", "Recebe o modelo tabular e devolve nome, MIME type e conteúdo Base64."],
      ["Formato", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Produz XLSX com Apache POI."],
      ["Identidade", "spring.application.name=relatorio-numerario", "Executa de forma independente na porta 8082."],
    ],
    flow: [
      ["1. Recepção", "RelatorioController recebe GerarRelatorioRequest em POST /v1/relatorios/gerar."],
      ["2. Validação", "O contrato e o service verificam título, nome do arquivo, colunas, linhas e consistência tabular."],
      ["3. Workbook", "GerarRelatorioService cria XSSFWorkbook e configura estilos reutilizáveis para título, cabeçalho, dados e totais."],
      ["4. Conteúdo", "Metadados, colunas e linhas são escritos em ordem; valores são convertidos para células compatíveis."],
      ["5. Acabamento", "Larguras, filtros, congelamento e formatação tornam a planilha adequada para consulta operacional."],
      ["6. Serialização", "O workbook é gravado em memória e convertido em Base64, sem criar arquivo temporário persistente."],
      ["7. Retorno", "GerarRelatorioResponse volta ao BFF com nome do arquivo, contentType e conteúdo; o BFF repassa ao Angular."],
    ],
  },
];

function walk(directory) {
  if (!fs.existsSync(directory)) return [];
  const files = [];
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    if (ignored.has(entry.name)) continue;
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) files.push(...walk(absolute));
    else if (extensions.has(path.extname(entry.name).toLowerCase())) files.push(absolute);
  }
  return files;
}

function signature(lines, start) {
  let text = lines[start].trim();
  for (let index = start + 1; index < Math.min(lines.length, start + 12); index += 1) {
    if (/[{;]\s*$/.test(text)) break;
    text += ` ${lines[index].trim()}`;
  }
  return text.replace(/\s+/g, " ").replace(/\s*\{\s*$/, "").trim();
}

function javaMethods(source) {
  const methods = [];
  const lines = source.split(/\r?\n/);
  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index].trim();
    if (!line || line.startsWith("@") || line.startsWith("//")) continue;
    const candidate = signature(lines, index);
    const match = candidate.match(
      /^(?:(?:public|protected|private|static|default|synchronized|final|abstract)\s+)*(?:<[^>]+>\s*)?[\w<>\[\], ?.@]+\s+(\w+)\s*\([^)]*\)\s*(?:throws\s+[^{;]+)?[{;]?$/,
    );
    const constructor = candidate.match(/^(?:public|protected|private)\s+(\w+)\s*\([^)]*\)\s*\{?$/);
    const name = match?.[1] ?? constructor?.[1];
    if (name && !["if", "for", "while", "switch", "catch"].includes(name)
      && !/\b(class|interface|record|enum)\b/.test(candidate)) {
      methods.push({ name, signature: candidate, line: index + 1 });
    }
  }
  return uniqueMethods(methods);
}

function tsMethods(source) {
  const methods = [];
  const lines = source.split(/\r?\n/);
  for (let index = 0; index < lines.length; index += 1) {
    const candidate = signature(lines, index);
    const match = candidate.match(
      /^(?:(?:public|private|protected|static|readonly|async|override)\s+)*([A-Za-z_$][\w$]*)\s*(?:<[^>]+>)?\s*\([^)]*\)\s*(?::\s*[^={]+)?\s*\{/,
    );
    if (match && !["if", "for", "while", "switch", "catch", "constructor"].includes(match[1])) {
      methods.push({ name: match[1], signature: candidate, line: index + 1 });
    }
  }
  return uniqueMethods(methods);
}

function uniqueMethods(methods) {
  const seen = new Set();
  return methods.filter((method) => {
    const key = `${method.line}:${method.name}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

function typeOf(file, source) {
  const name = path.basename(file, path.extname(file));
  const relative = rel(file);
  const extension = path.extname(file).toLowerCase();
  if (/\/src\/test\/|\.spec\.ts$/.test(relative)) return "Teste automatizado";
  if (extension === ".java") {
    if (/Api$/.test(name)) return "Contrato OpenAPI";
    if (/Controller$/.test(name)) return "Controller REST";
    if (/UseCase$/.test(name)) return "Caso de uso";
    if (/InputPort$/.test(name)) return "Porta de entrada";
    if (/OutputPort$/.test(name)) return "Porta de saída";
    if (/PersistenceAdapter$|Adapter$/.test(name)) return "Adapter";
    if (/JpaRepository$/.test(name)) return "Repository JPA";
    if (/Entity$/.test(name)) return "Entidade JPA";
    if (/Mapper$/.test(name)) return "Mapper";
    if (/Config$/.test(name)) return "Configuração Spring";
    if (/Filter$/.test(name)) return "Filtro HTTP";
    if (/Exception|Handler|Error/.test(name)) return "Tratamento de erro";
    if (/Request$|Response$|Dto$/.test(name) || relative.includes("/dto/")) return "DTO";
    if (/\benum\s+\w+/.test(source)) return "Enumeração";
    if (relative.includes("/domain/")) return "Modelo de domínio";
    if (/Service$/.test(name)) return "Serviço de aplicação";
    return "Componente Java";
  }
  if (extension === ".ts") {
    if (/\.component\.ts$/.test(relative)) return "Componente Angular";
    if (/\.service\.ts$/.test(relative)) return "Serviço Angular";
    if (/\.guard\.ts$/.test(relative)) return "Guard de rota";
    if (/\.interceptor\.ts$/.test(relative)) return "Interceptor HTTP";
    if (/models?|types?/.test(relative)) return "Modelo TypeScript";
    return "Componente TypeScript";
  }
  if (extension === ".html") return "Template Angular";
  if (extension === ".css" || extension === ".scss") return "Estilo visual";
  return "Configuração ou apoio";
}

function declarationOf(source, extension) {
  if (extension === ".java") {
    const match = source.match(/\b(public\s+)?(class|interface|record|enum)\s+(\w+)/);
    return match ? `${match[2]} ${match[3]}` : "arquivo Java declarativo";
  }
  if (extension === ".ts") {
    const match = source.match(/\b(export\s+)?(class|interface|type|enum)\s+(\w+)/);
    return match ? `${match[2]} ${match[3]}` : "módulo TypeScript";
  }
  return "arquivo declarativo";
}

function inspect(file, app) {
  const source = fs.readFileSync(file, "utf8");
  const extension = path.extname(file).toLowerCase();
  const type = typeOf(file, source);
  const declaration = declarationOf(source, extension);
  const methods = extension === ".java" ? javaMethods(source) : extension === ".ts" ? tsMethods(source) : [];
  const imports = extension === ".java"
    ? [...source.matchAll(/^import\s+(br\.com\.gestaonumerario\.[^;]+);/gm)].map((match) => match[1])
    : extension === ".ts"
      ? [...source.matchAll(/from\s+['"]([^'"]+)['"]/g)].map((match) => match[1]).filter((item) => item.startsWith("."))
      : [];
  const baseRoute = source.match(/@RequestMapping\("([^"]+)"\)/)?.[1] ?? "";
  const mappings = [...source.matchAll(/@(Get|Post|Put|Patch|Delete)Mapping(?:\(([^)]*)\))?/g)]
    .map((match) => {
      const methodRoute = match[2]?.match(/"([^"]*)"/)?.[1] ?? "";
      return `${match[1].toUpperCase()} ${baseRoute}${methodRoute || (baseRoute ? "" : "(rota da classe)")}`;
    });
  return {
    file,
    relative: rel(file),
    extension,
    source,
    type,
    declaration,
    methods,
    imports,
    mappings,
    responsibility: responsibility(type, path.basename(file, extension), app),
  };
}

function responsibility(type, name, app) {
  const descriptions = {
    "Contrato OpenAPI": `Define a documentação pública de ${name.replace(/Api$/, "")}: tag, operações, parâmetros e respostas esperadas, separando contrato de implementação.`,
    "Controller REST": `Recebe chamadas HTTP de ${app.name}, aplica o contrato de entrada e delega o processamento sem concentrar regra de negócio.`,
    "Caso de uso": `Orquestra a capacidade ${name.replace(/UseCase$/, "")}, aplicando regras, autorizações, transações e portas de saída.`,
    "Porta de entrada": `Declara as operações de negócio que os adapters de entrada podem solicitar ao núcleo.`,
    "Porta de saída": `Declara uma necessidade externa do núcleo e impede dependência direta de banco, relógio, segurança ou transação.`,
    "Adapter": `Implementa uma fronteira técnica e converte o contrato do núcleo para a tecnologia utilizada.`,
    "Repository JPA": `Declara consultas e operações de persistência executadas pelo Spring Data no MySQL.`,
    "Entidade JPA": `Mapeia uma tabela e seus campos persistidos sem expor essa representação como modelo público.`,
    "Mapper": `Converte dados entre DTO, domínio e persistência, evitando acoplamento entre camadas.`,
    "Configuração Spring": `Compõe beans e políticas técnicas necessárias ao funcionamento da porta ${app.port}.`,
    "Filtro HTTP": `Intercepta a chamada antes do controller para aplicar identificação, autenticação ou outra política transversal.`,
    "Tratamento de erro": `Representa ou converte falhas em uma resposta previsível, sem vazar detalhes internos.`,
    "DTO": `Transporta dados na fronteira da aplicação com campos e tipos explícitos.`,
    "Enumeração": `Restringe um conjunto de valores válidos utilizado pelas regras e contratos.`,
    "Modelo de domínio": `Representa um conceito de negócio e mantém seu estado e invariantes no núcleo.`,
    "Serviço de aplicação": `Executa uma sequência coesa da aplicação e coordena seus colaboradores técnicos.`,
    "Componente Angular": `Controla estado, eventos e dados da tela ${name.replace(".component", "")}.`,
    "Serviço Angular": `Centraliza integração HTTP ou estado compartilhado para evitar lógica duplicada nas telas.`,
    "Guard de rota": `Decide se uma rota pode ser ativada a partir da sessão e do perfil atual.`,
    "Interceptor HTTP": `Aplica comportamento transversal às chamadas, como Bearer, loading e tratamento de falhas.`,
    "Modelo TypeScript": `Define tipos usados pelo frontend para manter compatibilidade com o BFF em compilação.`,
    "Teste automatizado": `Comprova cenários de ${name.replace(/Test$|Spec$/, "")} e protege o comportamento contra regressões.`,
    "Template Angular": `Declara a estrutura visual e os bindings da tela associada.`,
    "Estilo visual": `Define apresentação, responsividade e estados visuais do componente ou da aplicação.`,
    "Configuração ou apoio": `Configura ou documenta uma parte necessária para construir e executar ${app.name}.`,
    "Componente Java": `Implementa a responsabilidade indicada por ${name} dentro da porta ${app.port}.`,
    "Componente TypeScript": `Implementa a responsabilidade indicada por ${name} no frontend.`,
  };
  return descriptions[type] ?? descriptions["Configuração ou apoio"];
}

function methodExplanation(method, item) {
  const name = method.name;
  const lower = name.toLowerCase();
  const context = item.declaration.replace(/^(class|interface|record|enum)\s+/, "");
  if (name === context) {
    return `Constrói ${context} e recebe suas dependências obrigatórias; deixa a instância pronta para atender as operações públicas sem criar colaboradores internamente.`;
  }
  if (lower === "main") return `Inicializa ${context}, entrega a configuração ao Spring Boot e mantém a porta da aplicação disponível.`;
  if (item.type === "Contrato OpenAPI") {
    return `Declara a operação pública “${name}” de ${context}; sua assinatura define entrada e saída, enquanto as anotações associadas alimentam o Swagger/OpenAPI implementado pelo controller.`;
  }
  if (item.type === "Controller REST") {
    return `Atende a operação HTTP “${name}”, recebe e valida os parâmetros da assinatura, delega ao serviço ou porta de entrada e converte o resultado para a resposta HTTP esperada.`;
  }
  if (item.type === "Repository JPA") {
    return `Executa a operação de persistência “${name}” por meio do Spring Data, usando os filtros da assinatura e devolvendo entidades ou projeções para o adapter.`;
  }
  if (item.type === "Serviço Angular") {
    return `Centraliza a operação “${name}” para as telas Angular; monta a chamada ao BFF ou trata o estado compartilhado e devolve um resultado tipado ao componente.`;
  }
  if (item.type === "Componente Angular") {
    return `Executa a ação de tela “${name}”, coordena estado de carregamento, dados visíveis e feedback ao usuário e chama o serviço Angular quando a ação exige integração.`;
  }
  if (item.type === "Porta de entrada" || item.type === "Porta de saída") {
    return `Define “${name}” como operação obrigatória desta fronteira; implementações devem respeitar exatamente os parâmetros e o retorno declarados.`;
  }
  if (lower.startsWith("get") || lower.startsWith("buscar") || lower.startsWith("consult")
    || lower.startsWith("listar") || lower.startsWith("detalh") || lower.startsWith("historico")) {
    return `Consulta os dados de ${context} sem comandar uma alteração principal; aplica os filtros presentes na assinatura e devolve o tipo declarado.`;
  }
  if (lower.startsWith("criar") || lower.startsWith("cadastrar") || lower.startsWith("salvar")
    || lower.startsWith("solicitar") || lower.startsWith("gerar")) {
    return `Cria o resultado associado a ${context}, validando a entrada e delegando às dependências da classe antes de devolver o objeto produzido.`;
  }
  if (lower.startsWith("atualiz") || lower.startsWith("ajust") || lower.startsWith("alter")) {
    return `Altera o estado tratado por ${context}; usa os identificadores e versões da assinatura para preservar consistência e concorrência.`;
  }
  if (["aprovar", "rejeitar", "cancelar", "programar", "separar", "expedir", "ocorrencia", "receber", "conciliar", "atender"].some((action) => lower.includes(action))) {
    return `Executa a transição “${name}” no fluxo de numerário, valida o estado anterior e registra o resultado necessário para auditoria.`;
  }
  if (lower.startsWith("to") || lower.includes("map") || lower.includes("converter") || lower.includes("from")) {
    return `Converte a representação recebida por ${context} para o tipo de retorno, copiando e normalizando os campos entre camadas.`;
  }
  if (lower.includes("valid") || lower.includes("normaliz") || lower.includes("format")) {
    return `Normaliza ou valida os dados utilizados por ${context}; rejeita ou ajusta valores antes da etapa seguinte do fluxo.`;
  }
  if (lower.includes("auth") || lower.includes("login") || lower.includes("sessao") || lower.includes("token")) {
    return `Participa da autenticação de ${context}, tratando credenciais, sessão ou token conforme os parâmetros declarados.`;
  }
  if (lower.startsWith("on") || lower === "ngoninit" || lower.includes("click") || lower.includes("submit")) {
    return `Responde ao evento da interface “${name}”, atualiza o estado do componente e aciona o serviço necessário quando aplicável.`;
  }
  if (item.type === "Teste automatizado") {
    return `Executa o cenário automatizado “${name}”, prepara as condições, chama o comportamento sob teste e verifica o resultado esperado.`;
  }
  return `Executa “${name}” como parte de ${context}; recebe os parâmetros da assinatura, utiliza as dependências da classe e devolve ou aplica o resultado declarado.`;
}

function layer(type) {
  if (["Controller REST", "Contrato OpenAPI", "Filtro HTTP", "Componente Angular", "Template Angular", "Guard de rota"].includes(type)) return "Entrada e apresentação";
  if (["Caso de uso", "Serviço de aplicação", "Serviço Angular", "Porta de entrada"].includes(type)) return "Aplicação";
  if (["Modelo de domínio", "Enumeração", "DTO", "Modelo TypeScript"].includes(type)) return "Modelo e contratos";
  if (["Porta de saída", "Adapter", "Repository JPA", "Entidade JPA", "Mapper", "Interceptor HTTP"].includes(type)) return "Infraestrutura e integração";
  if (type === "Teste automatizado") return "Testes";
  return "Configuração e apoio";
}

function renderFile(item) {
  const search = [
    item.relative, item.type, item.declaration, item.responsibility,
    ...item.imports, ...item.methods.map((method) => method.name),
  ].join(" ").toLowerCase();
  return `<details class="file" data-search="${esc(search)}">
    <summary><span><span class="file-path">${esc(item.relative)}</span><small>${esc(item.declaration)}</small></span><span class="tag">${esc(item.type)}</span></summary>
    <div class="file-body">
      <div class="facts">
        <div><b>Responsabilidade</b><p>${esc(item.responsibility)}</p></div>
        <div><b>Papel no fluxo</b><p>${esc(flowRole(item))}</p></div>
        <div><b>Dependências internas</b><p>${item.imports.length ? item.imports.map((value) => `<code>${esc(value)}</code>`).join("<br>") : "Nenhuma importação interna explícita."}</p></div>
        <div><b>Entradas HTTP</b><p>${item.mappings.length ? item.mappings.map((value) => `<code>${esc(value)}</code>`).join("<br>") : "Não expõe mapping HTTP diretamente."}</p></div>
      </div>
      <h4>Métodos identificados (${item.methods.length})</h4>
      ${item.methods.length ? `<div class="method-list">${item.methods.map((method) => `
        <article class="method">
          <div class="method-head"><strong>${esc(method.name)}</strong><span>Linha ${method.line}</span></div>
          <code class="signature">${esc(method.signature)}</code>
          <p>${esc(methodExplanation(method, item))}</p>
        </article>`).join("")}</div>` : `<p class="empty">Este arquivo não declara método executável próprio; sua contribuição ocorre por configuração, marcação, tipos ou estrutura visual.</p>`}
    </div>
  </details>`;
}

function flowRole(item) {
  const roles = {
    "Entrada e apresentação": "É acionado no início da interação desta porta ou define como a entrada é apresentada e aceita.",
    "Aplicação": "Coordena a sequência depois da entrada e antes dos detalhes de infraestrutura.",
    "Modelo e contratos": "Define os dados e valores compartilhados entre etapas, tornando o fluxo explícito e tipado.",
    "Infraestrutura e integração": "Conecta a aplicação a HTTP, persistência ou outra tecnologia externa.",
    "Testes": "Executa o fluxo de forma controlada e verifica seus resultados.",
    "Configuração e apoio": "Prepara build, runtime, aparência ou documentação necessária para o fluxo funcionar.",
  };
  return roles[layer(item.type)];
}

function styles(accent) {
  return `
    :root{--accent:${accent};--accent2:#7b0d24;--ink:#17202e;--muted:#667085;--line:#dfe3e8;--soft:#f5f6f8;--paper:#fff;--nav:#161d2a}
    *{box-sizing:border-box}html{scroll-behavior:smooth}body{margin:0;background:var(--soft);color:var(--ink);font:15px/1.65 "Segoe UI",Arial,sans-serif}
    a{color:var(--accent2)}code{font:13px/1.55 Consolas,monospace}.layout{display:grid;grid-template-columns:270px minmax(0,1fr);min-height:100vh}
    aside{position:sticky;top:0;height:100vh;overflow:auto;background:var(--nav);color:#d0d5dd;padding:24px 18px}.brand{padding:8px 10px 22px;border-bottom:1px solid #344054}.brand b{display:block;color:#fff;font-size:21px}.brand span{font-size:13px;color:#98a2b3}
    nav{padding-top:18px}nav a{display:block;color:#d0d5dd;text-decoration:none;padding:9px 11px;border-radius:8px}nav a:hover,nav a.active{background:#293446;color:#fff}.aside-meta{margin:22px 10px;font-size:12px;color:#98a2b3}
    main{min-width:0}.hero{padding:52px clamp(24px,5vw,74px);background:linear-gradient(125deg,var(--accent2),var(--accent));color:#fff}.hero-inner,.content{max-width:1260px;margin:auto}.kicker{text-transform:uppercase;letter-spacing:.13em;font-size:12px;font-weight:800;opacity:.84}
    h1{font-size:clamp(34px,5vw,58px);line-height:1.05;margin:10px 0 15px}.hero p{max-width:880px;font-size:18px;color:#fff1f4}.badges{display:flex;gap:8px;flex-wrap:wrap;margin-top:20px}.badge{padding:5px 10px;border:1px solid #ffffff66;border-radius:99px;background:#ffffff16}
    .content{padding:38px clamp(18px,4vw,58px) 80px}section{margin-bottom:50px;scroll-margin-top:16px}.section-head{display:flex;justify-content:space-between;gap:25px;align-items:end;border-bottom:2px solid var(--line);padding-bottom:12px;margin-bottom:20px}.section-head h2{font-size:28px;margin:0}.section-head p{max-width:650px;color:var(--muted);margin:0}
    .grid{display:grid;gap:15px}.g2{grid-template-columns:repeat(2,minmax(0,1fr))}.g3{grid-template-columns:repeat(3,minmax(0,1fr))}.card{background:var(--paper);border:1px solid var(--line);border-radius:12px;padding:19px;box-shadow:0 1px 2px #10182808}.card h3{margin:0 0 8px}.card p{margin:5px 0;color:#475467}.metric strong{display:block;font-size:30px;color:var(--accent2)}
    .flow{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:10px}.step{background:#fff;border:1px solid var(--line);border-top:4px solid var(--accent);border-radius:10px;padding:15px}.step b{display:block;margin-bottom:5px}.step p{margin:0;color:#475467}.callout{border-left:4px solid var(--accent);background:#fff;padding:17px 19px;border-radius:8px}
    table{width:100%;border-collapse:collapse;background:#fff}th,td{padding:11px;border:1px solid var(--line);text-align:left;vertical-align:top}th{background:#f8fafc;color:#475467;font-size:12px;text-transform:uppercase}
    .toolbar{position:sticky;top:8px;z-index:3;display:flex;gap:9px;padding:10px;background:#f5f6f8eF;border:1px solid var(--line);border-radius:10px;backdrop-filter:blur(8px)}.toolbar input{flex:1;min-width:220px;padding:10px;border:1px solid #98a2b3;border-radius:7px}.button{padding:9px 12px;border:1px solid #98a2b3;border-radius:7px;background:#fff;cursor:pointer}
    .catalog-group{margin:26px 0}.catalog-group>h3{display:flex;justify-content:space-between}.tag{font-size:12px;padding:3px 8px;border-radius:99px;background:#eef0f3;color:#344054}.file{background:#fff;border:1px solid var(--line);border-radius:9px;margin:8px 0;overflow:hidden}.file>summary{display:flex;justify-content:space-between;gap:12px;align-items:center;padding:13px 15px;cursor:pointer}.file>summary:hover{background:#fafafa}.file>summary small{display:block;color:var(--muted);font-weight:400}.file-path{display:block;font:600 13px/1.45 Consolas,monospace;overflow-wrap:anywhere}.file-body{border-top:1px solid var(--line);padding:16px}
    .facts{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}.facts>div{padding:12px;background:#f8fafc;border-radius:8px}.facts b{color:#344054}.facts p{margin:5px 0;color:#475467;overflow-wrap:anywhere}.method-list{display:grid;gap:9px}.method{border:1px solid var(--line);border-left:3px solid var(--accent);border-radius:8px;padding:12px}.method-head{display:flex;justify-content:space-between}.method-head span{font-size:12px;color:var(--muted)}.signature{display:block;margin:7px 0;padding:7px 9px;background:#f7f8fa;border-radius:5px;overflow-wrap:anywhere}.method p{margin:5px 0;color:#475467}.empty{color:var(--muted);font-style:italic}.footer{text-align:center;color:var(--muted);padding-top:25px}
    @media(max-width:980px){.layout{display:block}aside{position:relative;height:auto}nav{display:flex;overflow:auto}nav a{white-space:nowrap}.aside-meta{display:none}.flow{grid-template-columns:repeat(2,minmax(0,1fr))}}
    @media(max-width:640px){.g2,.g3,.flow,.facts{grid-template-columns:1fr}.hero{padding:36px 20px}.content{padding:25px 14px}.section-head{display:block}.section-head p{margin-top:7px}.toolbar{position:relative;flex-wrap:wrap}.toolbar input{width:100%}}
    @media print{aside,.toolbar{display:none!important}.layout{display:block}.hero{background:#fff!important;color:#111;padding:20px 0;border-bottom:3px solid var(--accent)}.hero p{color:#444}.content{padding:20px 0}.file{break-inside:avoid}details.file:not([open]) .file-body{display:block}}
  `;
}

function renderApplication(app, catalog) {
  const grouped = Map.groupBy(catalog, (item) => layer(item.type));
  const methodCount = catalog.reduce((total, item) => total + item.methods.length, 0);
  const classCount = catalog.filter((item) => /^(class|interface|record|enum)\s/.test(item.declaration)).length;
  const accent = app.key === "frontend" ? "#c11435" : app.key === "bff" ? "#a80f2d" : app.key === "api" ? "#8e1028" : "#6f1630";
  const nav = [
    ["visao", "Visão da porta"], ["fluxo", "Fluxo minucioso"], ["entradas", "Entradas e saídas"],
    ["arquitetura", "Camadas"], ["catalogo", "Classes e métodos"], ["execucao", "Execução"],
  ];
  return `<!doctype html>
<html lang="pt-BR">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <meta name="color-scheme" content="light">
  <title>COIN — ${esc(app.name)} — Porta ${app.port}</title>
  <style>${styles(accent)}</style>
</head>
<body data-files="${catalog.length}" data-methods="${methodCount}">
<div class="layout">
  <aside>
    <div class="brand"><b>COIN · ${esc(app.port)}</b><span>${esc(app.name)}</span></div>
    <nav>${nav.map(([id, label]) => `<a href="#${id}">${esc(label)}</a>`).join("")}</nav>
    <div class="aside-meta">Documento autocontido<br>${esc(app.stack)}<br>Gerado em ${esc(generatedAt)}</div>
  </aside>
  <main>
    <header class="hero"><div class="hero-inner">
      <div class="kicker">Documentação técnica por aplicação</div>
      <h1>${esc(app.name)} · porta ${app.port}</h1>
      <p>${esc(app.role)}</p>
      <div class="badges"><span class="badge">${esc(app.stack)}</span><span class="badge">${catalog.length} arquivos</span><span class="badge">${classCount} tipos/classes</span><span class="badge">${methodCount} métodos</span></div>
    </div></header>
    <div class="content">
      <section id="visao">
        <div class="section-head"><div><div class="kicker" style="color:${accent}">01</div><h2>Visão da porta ${app.port}</h2></div><p>Responsabilidade e limite arquitetural desta aplicação.</p></div>
        <div class="grid g2"><div class="card"><h3>O que faz</h3><p>${esc(app.role)}</p></div><div class="card"><h3>O que não faz</h3><p>${esc(app.boundary)}</p></div></div>
        <div class="grid g3" style="margin-top:15px"><div class="card metric"><strong>${catalog.length}</strong><span>arquivos documentados</span></div><div class="card metric"><strong>${classCount}</strong><span>classes, interfaces, records e enums</span></div><div class="card metric"><strong>${methodCount}</strong><span>métodos explicados</span></div></div>
      </section>
      <section id="fluxo">
        <div class="section-head"><div><div class="kicker" style="color:${accent}">02</div><h2>Fluxo minucioso</h2></div><p>Ordem real das responsabilidades desde a entrada até a resposta.</p></div>
        <div class="flow">${app.flow.map(([title, text]) => `<article class="step"><b>${esc(title)}</b><p>${esc(text)}</p></article>`).join("")}</div>
        <div class="callout" style="margin-top:15px"><b>Encadeamento entre portas</b><br>4200 Angular → 8080 BFF → 8081 API/MySQL. Para Excel: 8080 BFF → 8082 Relatório Numerário → 8080 BFF → 4200 Angular.</div>
      </section>
      <section id="entradas">
        <div class="section-head"><div><div class="kicker" style="color:${accent}">03</div><h2>Entradas, saídas e integrações</h2></div><p>Pontos pelos quais esta aplicação participa do sistema.</p></div>
        <table><thead><tr><th>Origem/capacidade</th><th>Entrada ou destino</th><th>Comportamento</th></tr></thead><tbody>${app.entries.map((row) => `<tr>${row.map((cell) => `<td>${esc(cell)}</td>`).join("")}</tr>`).join("")}</tbody></table>
      </section>
      <section id="arquitetura">
        <div class="section-head"><div><div class="kicker" style="color:${accent}">04</div><h2>Distribuição por camada</h2></div><p>Como os arquivos se distribuem dentro da responsabilidade da porta.</p></div>
        <div class="grid g3">${[...grouped.entries()].map(([name, items]) => `<div class="card metric"><strong>${items.length}</strong><span>${esc(name)}</span></div>`).join("")}</div>
      </section>
      <section id="catalogo">
        <div class="section-head"><div><div class="kicker" style="color:${accent}">05</div><h2>Cada classe, arquivo e método</h2></div><p>Inventário gerado diretamente de ${esc(app.directory)}, com responsabilidade, posição no fluxo, dependências, mappings e explicação de cada método detectado.</p></div>
        <div class="toolbar"><input id="search" type="search" placeholder="Filtrar classe, arquivo, método ou dependência…" aria-label="Filtrar catálogo"><button class="button" id="expand">Expandir visíveis</button><button class="button" id="collapse">Recolher</button><button class="button" onclick="window.print()">Imprimir / PDF</button></div>
        <div id="catalog">${[...grouped.entries()].map(([name, items]) => `<div class="catalog-group"><h3>${esc(name)} <span class="tag">${items.length} arquivos</span></h3>${items.map(renderFile).join("")}</div>`).join("")}</div>
      </section>
      <section id="execucao">
        <div class="section-head"><div><div class="kicker" style="color:${accent}">06</div><h2>Execução local</h2></div><p>Identidade operacional preservada pelo script central.</p></div>
        <div class="card"><h3>${esc(app.name)}</h3><p>Diretório: <code>${esc(app.directory)}</code></p><p>Porta: <code>${app.port}</code></p><p>Inicialização conjunta: <code>powershell -ExecutionPolicy Bypass -File .\\scripts\\iniciar-tudo.ps1</code></p></div>
      </section>
      <div class="footer">COIN · ${esc(app.name)} · porta ${app.port} · gerado em ${esc(generatedAt)}</div>
    </div>
  </main>
</div>
<script>
  const search=document.querySelector("#search");
  const files=[...document.querySelectorAll("details.file")];
  const groups=[...document.querySelectorAll(".catalog-group")];
  function filter(){const value=search.value.trim().toLowerCase();files.forEach(file=>file.hidden=Boolean(value)&&!file.dataset.search.includes(value));groups.forEach(group=>group.hidden=![...group.querySelectorAll("details.file")].some(file=>!file.hidden));}
  search.addEventListener("input",filter);
  document.querySelector("#expand").addEventListener("click",()=>files.filter(file=>!file.hidden).forEach(file=>file.open=true));
  document.querySelector("#collapse").addEventListener("click",()=>files.forEach(file=>file.open=false));
  const links=[...document.querySelectorAll("nav a")];
  const observer=new IntersectionObserver(entries=>entries.forEach(entry=>{if(entry.isIntersecting)links.forEach(link=>link.classList.toggle("active",link.hash==="#"+entry.target.id));}),{rootMargin:"-15% 0px -75% 0px"});
  document.querySelectorAll("main section[id]").forEach(section=>observer.observe(section));
</script>
</body>
</html>`;
}

function renderIndex(results) {
  return `<!doctype html>
<html lang="pt-BR">
<head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>COIN — Documentação por porta</title>
<style>
  :root{--red:#b4102b;--ink:#17202e;--muted:#667085;--line:#dfe3e8}*{box-sizing:border-box}body{margin:0;background:#f4f5f7;color:var(--ink);font:15px/1.6 "Segoe UI",Arial,sans-serif}.hero{padding:55px 24px;background:linear-gradient(125deg,#741024,var(--red));color:#fff}.wrap{max-width:1120px;margin:auto}.hero h1{font-size:clamp(34px,6vw,60px);line-height:1.05;margin:9px 0}.hero p{font-size:18px;max-width:800px}.content{padding:38px 24px 70px}.flow{display:flex;align-items:center;gap:10px;overflow:auto;margin-bottom:28px}.node{min-width:175px;flex:1;background:#fff;border:1px solid var(--line);border-top:4px solid var(--red);border-radius:10px;padding:15px}.arrow{font-size:25px;color:var(--red)}.grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px}.card{display:block;background:#fff;border:1px solid var(--line);border-radius:12px;padding:22px;text-decoration:none;color:inherit;box-shadow:0 2px 8px #1018280a}.card:hover{border-color:var(--red);transform:translateY(-1px)}.port{font-size:12px;color:var(--red);font-weight:800;text-transform:uppercase;letter-spacing:.1em}.card h2{margin:5px 0}.card p{color:var(--muted)}.meta{display:flex;gap:8px;flex-wrap:wrap}.meta span{background:#f0f2f5;border-radius:99px;padding:4px 8px;font-size:12px}@media(max-width:700px){.grid{grid-template-columns:1fr}.flow{display:grid}.arrow{transform:rotate(90deg);text-align:center}}
</style></head>
<body><header class="hero"><div class="wrap"><div class="port">Documentação técnica e funcional</div><h1>COIN por porta</h1><p>A documentação foi separada por aplicação para mostrar com clareza o fluxo, os limites arquiteturais e cada classe e método do código correspondente.</p></div></header>
<main class="wrap content"><div class="flow"><div class="node"><b>4200 · Angular</b><br>Interface</div><div class="arrow">→</div><div class="node"><b>8080 · BFF</b><br>Orquestração</div><div class="arrow">→</div><div class="node"><b>8081 · API</b><br>Domínio e dados</div><div class="arrow">+</div><div class="node"><b>8082 · Relatórios</b><br>Excel Apache POI</div></div>
<div class="grid">${results.map(({ app, catalog, methods }) => `<a class="card" href="${esc(app.output)}"><div class="port">Porta ${app.port}</div><h2>${esc(app.name)}</h2><p>${esc(app.role)}</p><div class="meta"><span>${catalog.length} arquivos</span><span>${methods} métodos</span><span>${esc(app.stack)}</span></div></a>`).join("")}</div></main></body></html>`;
}

const results = [];
for (const app of applications) {
  const catalog = walk(path.join(root, app.directory))
    .sort((a, b) => rel(a).localeCompare(rel(b)))
    .map((file) => inspect(file, app));
  const html = renderApplication(app, catalog);
  fs.writeFileSync(path.join(docsDir, app.output), html, "utf8");
  results.push({
    app,
    catalog,
    methods: catalog.reduce((total, item) => total + item.methods.length, 0),
    bytes: Buffer.byteLength(html),
  });
}

const index = renderIndex(results);
fs.writeFileSync(path.join(docsDir, "DOCUMENTACAO_COMPLETA.html"), index, "utf8");

console.log(JSON.stringify({
  indice: "docs/DOCUMENTACAO_COMPLETA.html",
  documentos: results.map(({ app, catalog, methods, bytes }) => ({
    porta: app.port,
    arquivo: `docs/${app.output}`,
    arquivosCatalogados: catalog.length,
    metodosDocumentados: methods,
    bytes,
  })),
}, null, 2));
