import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "..");
const output = path.join(root, "docs", "DOCUMENTACAO_COMPLETA.html");
const generatedAt = "27/07/2026";

const esc = (value = "") => String(value)
  .replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
  .replaceAll('"', "&quot;").replaceAll("'", "&#39;");
const rel = (file) => path.relative(root, file).replaceAll("\\", "/");
const slug = (value) => value.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "")
  .replace(/[^a-z0-9]+/g, "-").replace(/(^-|-$)/g, "");

const ignored = new Set([".git", "node_modules", "target", "dist", "coverage", ".angular", ".idea", ".vscode", "backups-local"]);
const allowedExtensions = new Set([".java", ".ts", ".mjs", ".html", ".scss", ".css", ".sql", ".md", ".yml", ".yaml", ".json", ".xml", ".properties", ".ps1", ".cmd"]);
const roots = ["api-numerario", "bff-numerario", "frontend-numerario", "docs", "scripts"];
const rootFiles = ["README.md", "docker-compose.yml", "DOCUMENTACAO_PROJETO.md"];

function walk(dir) {
  if (!fs.existsSync(dir)) return [];
  const files = [];
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    if (ignored.has(entry.name)) continue;
    const absolute = path.join(dir, entry.name);
    if (entry.isDirectory()) files.push(...walk(absolute));
    else if (allowedExtensions.has(path.extname(entry.name).toLowerCase())) files.push(absolute);
  }
  return files;
}

const files = [
  ...roots.flatMap((item) => walk(path.join(root, item))),
  ...rootFiles.map((item) => path.join(root, item)).filter(fs.existsSync),
].filter((file) => path.resolve(file) !== path.resolve(output)).sort((a, b) => rel(a).localeCompare(rel(b)));

function cleanSignature(text) {
  return text.replace(/\s+/g, " ").replace(/\s*\{\s*$/, "").trim();
}

function javaMethods(source) {
  const methods = [];
  const lines = source.split(/\r?\n/);
  const interfaceFile = /\binterface\s+\w+/.test(source);
  let annotation = "";
  for (let i = 0; i < lines.length; i += 1) {
    const line = lines[i].trim();
    if (line.startsWith("@")) annotation = `${annotation} ${line}`.trim();
    const isMethod = /^(?:(?:public|protected|private|static|default|synchronized|final|abstract|native|strictfp)\s+)+(?:<[^>]+>\s*)?[\w<>\[\], ?.@]+\s+\w+\s*\(/.test(line);
    const isConstructor = /^(?:(?:public|protected|private)\s+)+\w+\s*\(/.test(line);
    const isInterfaceMethod = /^[\w<>\[\], ?.@]+\s+\w+\s*\([^)]*\)\s*;$/.test(line);
    const isInterfaceStart = interfaceFile && /^[\w<>\[\], ?.@]+\s+\w+\s*\(/.test(line);
    const excluded = /^(if|for|while|switch|catch|return|new)\b/.test(line) || /\b(class|interface|record|enum)\b/.test(line);
    if ((isMethod || isConstructor || isInterfaceMethod || isInterfaceStart) && !excluded) {
      const name = line.match(/(\w+)\s*\(/)?.[1];
      if (name && !["if", "for", "while", "switch", "catch"].includes(name)) {
        methods.push({ name, signature: cleanSignature(line), line: i + 1, annotation });
      }
      annotation = "";
    } else if (line && !line.startsWith("@") && !line.startsWith("//")) {
      annotation = "";
    }
  }
  return methods;
}

function tsMethods(source) {
  const methods = [];
  const lines = source.split(/\r?\n/);
  for (let i = 0; i < lines.length; i += 1) {
    const line = lines[i].trim();
    const match = line.match(/^(?:(?:public|private|protected|static|readonly|async|override)\s+)*([A-Za-z_$][\w$]*)\s*(?:<[^>]+>)?\s*\([^)]*\)\s*(?::\s*[^={]+)?\s*\{/);
    if (match && !["if", "for", "while", "switch", "catch", "constructor"].includes(match[1])) {
      methods.push({ name: match[1], signature: cleanSignature(line), line: i + 1 });
    }
  }
  return methods;
}

function classify(file, source) {
  const relative = rel(file);
  const name = path.basename(file, path.extname(file));
  const extension = path.extname(file).toLowerCase();
  const isTest = /\/src\/test\/|\.spec\.ts$/.test(relative);
  let type = "Arquivo de suporte";
  let purpose = "Registra configuração, conteúdo ou suporte operacional necessário ao projeto.";
  let rationale = "Centraliza essa responsabilidade para manter o comportamento reproduzível e fácil de localizar.";
  let contribution = "Apoia a execução, manutenção ou entendimento do sistema.";

  if (isTest) {
    type = "Teste automatizado";
    purpose = `Valida o comportamento de ${name.replace(/Test$|Spec$/, "")}, incluindo cenários felizes e de erro.`;
    rationale = "Isola a verificação automatizada e protege contratos contra regressões.";
    contribution = "Permite evoluir a aplicação com evidência executável de compatibilidade.";
  } else if (extension === ".java") {
    if (/Controller$/.test(name)) [type, purpose, rationale, contribution] = ["Controller REST", "Expõe operações HTTP na rota pública /api/v1 e traduz entrada/saída.", "Mantém protocolo HTTP fora do núcleo de negócio.", "Conecta clientes aos casos de uso preservando validação e códigos de resposta."];
    else if (/Api$/.test(name)) [type, purpose, rationale, contribution] = ["Contrato OpenAPI", "Declara o contrato, respostas e documentação do endpoint.", "Separa a descrição pública da implementação do controller.", "Mantém a API navegável e consistente no Swagger/OpenAPI."];
    else if (/UseCase$/.test(name)) [type, purpose, rationale, contribution] = ["Caso de uso", "Orquestra regras de negócio e transações da capacidade indicada pelo nome.", "Concentra decisões no núcleo sem dependência de HTTP ou JPA.", "Implementa o fluxo funcional por meio das portas de saída."];
    else if (/InputPort$/.test(name)) [type, purpose, rationale, contribution] = ["Porta de entrada", "Define as operações de negócio oferecidas pela aplicação.", "Aplica inversão de dependência entre adaptadores e núcleo.", "Estabiliza o contrato usado pelos controllers."];
    else if (/OutputPort$/.test(name)) [type, purpose, rationale, contribution] = ["Porta de saída", "Define a necessidade externa do núcleo, como persistência, relógio, token ou transação.", "Impede que o domínio dependa diretamente de infraestrutura.", "Permite trocar implementações e testar casos de uso isoladamente."];
    else if (/PersistenceAdapter$|Adapter$/.test(name)) [type, purpose, rationale, contribution] = ["Adaptador de saída", "Implementa uma porta do núcleo usando tecnologia de infraestrutura.", "Encapsula detalhes técnicos atrás de um contrato estável.", "Liga casos de uso a persistência, segurança, tempo ou transação."];
    else if (/JpaRepository$/.test(name)) [type, purpose, rationale, contribution] = ["Repositório Spring Data", "Declara consultas e operações de persistência JPA.", "Delega infraestrutura repetitiva ao Spring Data.", "Fornece acesso eficiente às tabelas do MySQL."];
    else if (/Entity$/.test(name)) [type, purpose, rationale, contribution] = ["Entidade JPA", "Mapeia o estado persistido e relacionamentos de uma tabela.", "Mantém anotações JPA fora do modelo de domínio.", "Materializa dados no banco sem contaminar as regras centrais."];
    else if (/Mapper$/.test(name)) [type, purpose, rationale, contribution] = ["Mapper", "Converte representações entre domínio, persistência e contratos REST.", "Evita acoplamento entre modelos de camadas diferentes.", "Preserva fronteiras arquiteturais e reduz conversões duplicadas."];
    else if (/Config$/.test(name)) [type, purpose, rationale, contribution] = ["Configuração", "Compõe dependências e políticas técnicas do Spring.", "Mantém o wiring fora das classes de negócio.", "Inicializa segurança, documentação e casos de uso."];
    else if (/Exception|Error|Handler/.test(name)) [type, purpose, rationale, contribution] = ["Tratamento de erro", "Representa ou traduz uma condição de falha de forma padronizada.", "Evita respostas inconsistentes e vazamento de detalhes internos.", "Garante códigos HTTP e mensagens previsíveis."];
    else if (/Request$|Response$|Dto$|PageResponse$/.test(name) || relative.includes("/dto/")) [type, purpose, rationale, contribution] = ["DTO/contrato", "Transporta dados validados na fronteira da aplicação.", "Distingue o contrato público do modelo de domínio.", "Define dados aceitos ou retornados sem expor entidades JPA."];
    else if (/\b(enum)\b/.test(source)) [type, purpose, rationale, contribution] = ["Enumeração de domínio", "Restringe valores válidos de estado, tipo, perfil ou ordenação.", "Substitui valores livres por um vocabulário controlado.", "Torna regras condicionais explícitas e seguras."];
    else if (relative.includes("/core/domain/model/")) [type, purpose, rationale, contribution] = ["Modelo de domínio", "Representa um conceito de negócio e protege suas invariantes.", "Mantém comportamento e dados essenciais no núcleo puro.", "É a linguagem comum usada pelos casos de uso."];
    else [type, purpose, rationale, contribution] = ["Componente Java", `Implementa a responsabilidade técnica de ${name}.`, "Mantém a responsabilidade coesa em uma unidade identificável.", "Participa da composição do backend ou BFF."];
  } else if (extension === ".ts") {
    if (/\.spec\.ts$/.test(relative)) {/* já classificado */}
    else if (/\.component\.ts$/.test(relative)) [type, purpose, rationale, contribution] = ["Componente de tela", "Controla apresentação, estado e ações da respectiva tela Angular.", "Mantém comportamento visual encapsulado e testável.", "Conecta templates aos serviços e ao roteamento."];
    else if (/\.service\.ts$/.test(relative)) [type, purpose, rationale, contribution] = ["Serviço Angular", "Centraliza comunicação HTTP ou estado compartilhado.", "Evita chamadas e regras de integração duplicadas nos componentes.", "Liga a interface aos contratos /api/v1 do BFF."];
    else if (/\.guard\.ts$/.test(relative)) [type, purpose, rationale, contribution] = ["Guard de rota", "Decide se uma navegação é autorizada.", "Bloqueia acesso antes da criação da tela.", "Aplica autenticação ou perfil GESTOR no frontend."];
    else if (/\.interceptor\.ts$/.test(relative)) [type, purpose, rationale, contribution] = ["Interceptor HTTP", "Aplica políticas transversais às requisições e respostas.", "Centraliza o header Bearer, renovação e tratamento de falhas.", "Mantém serviços focados no contrato funcional."];
    else if (/models?|types?/.test(relative)) [type, purpose, rationale, contribution] = ["Modelo TypeScript", "Tipa dados, filtros e estados usados pela interface.", "Detecta incompatibilidades em compilação.", "Alinha telas aos contratos do BFF."];
    else [type, purpose, rationale, contribution] = ["Componente TypeScript", `Implementa a responsabilidade de ${name} no frontend.`, "Separa comportamento por capacidade técnica ou funcional.", "Sustenta navegação, estado, utilitários ou bootstrap Angular."];
  } else if (extension === ".html") {
    [type, purpose, rationale, contribution] = ["Template Angular", "Define a estrutura semântica, campos, ações e apresentação de uma tela.", "Separa marcação do comportamento TypeScript e do estilo.", "Materializa a interação descrita pelo componente associado."];
  } else if (extension === ".scss" || extension === ".css") {
    [type, purpose, rationale, contribution] = ["Estilo visual", "Define layout, responsividade e estados visuais.", "Mantém apresentação separada da lógica.", "Garante consistência visual e usabilidade."];
  } else if (extension === ".sql") {
    [type, purpose, rationale, contribution] = ["Migração Flyway", "Aplica uma evolução versionada e auditável ao esquema ou aos dados.", "Evita alterações manuais e mantém ambientes reproduzíveis.", "Leva o banco de forma ordenada até o modelo V6."];
  } else if (extension === ".md") {
    [type, purpose, rationale, contribution] = ["Documento de apoio", "Registra decisões, requisitos ou instruções específicas do projeto.", "Preserva contexto fora do código sem sobrecarregar a documentação principal.", "Apoia desenvolvimento, operação e auditoria."];
  } else if (extension === ".ps1" || extension === ".cmd" || extension === ".mjs") {
    [type, purpose, rationale, contribution] = ["Script operacional", "Automatiza uma rotina de execução, validação ou migração.", "Reduz erro humano e torna a operação repetível.", "Apoia inicialização e manutenção segura do ambiente."];
  }
  return { type, purpose, rationale, contribution };
}

function inspect(file) {
  const source = fs.readFileSync(file, "utf8");
  const relative = rel(file);
  const extension = path.extname(file).toLowerCase();
  const meta = classify(file, source);
  const imports = extension === ".java"
    ? [...source.matchAll(/^import\s+(br\.com\.gestaonumerario\.[^;]+);/gm)].map((m) => m[1])
    : extension === ".ts"
      ? [...source.matchAll(/from\s+['"]([^'"]+)['"]/g)].map((m) => m[1]).filter((item) => item.startsWith(".") || item.startsWith("@app"))
      : [];
  const methods = extension === ".java" ? javaMethods(source) : [".ts", ".mjs"].includes(extension) ? tsMethods(source) : [];
  const declaration = extension === ".java"
    ? source.match(/\b(class|interface|record|enum)\s+(\w+)/)?.slice(1).join(" ")
    : source.match(/\b(class|interface|type|enum)\s+(\w+)/)?.slice(1).join(" ");
  return { file, relative, extension, source, imports, methods, declaration, ...meta };
}

const catalog = files.map(inspect);
const count = (predicate) => catalog.filter(predicate).length;
const methodCount = catalog.reduce((sum, item) => sum + item.methods.length, 0);

const screens = [
  ["Login", "/login", "Autenticar o usuário e iniciar uma sessão.", "Informa credenciais, envia o formulário e recebe feedback de validação ou bloqueio.", "Usuário; a senha nunca é exibida nem persistida no navegador.", "BFF autentica na API; o Angular persiste os tokens no localStorage e mantém o contexto da sessão.", "Público"],
  ["Menu principal", "/menu", "Apresentar atalhos compatíveis com o perfil autenticado.", "Escolhe Tesouraria ou encerra a sessão.", "Nome, perfil e opções autorizadas.", "É o ponto de entrada após login e direciona aos módulos protegidos.", "Autenticado"],
  ["Tesouraria", "/tesouraria", "Centralizar as capacidades operacionais do numerário.", "Navega para dashboard, agências, solicitações, movimentações e livro-caixa.", "Cards de acesso e contexto do usuário.", "Organiza o fluxo principal; capacidades sensíveis dependem do perfil GESTOR.", "Autenticado"],
  ["Dashboard", "/dashboard", "Exibir uma síntese operacional para decisão rápida.", "Consulta indicadores, alertas de saldo e atividade recente.", "Saldos, totais, pendências, alertas e resumos de movimentação.", "Consolida dados de agências, solicitações e movimentações.", "Autenticado"],
  ["Menu de agências", "/agencias", "Agrupar consulta e cadastro de agências.", "Escolhe consultar ou cadastrar.", "Ações disponíveis para gestão cadastral.", "Conecta a manutenção de unidades aos fluxos financeiros.", "GESTOR"],
  ["Consultar agências", "/agencias/consultar", "Pesquisar e administrar unidades operacionais.", "Filtra, ordena, pagina, abre detalhe e solicita desativação.", "Código, nome, tipo, status, saldo, mínimo e alertas.", "A agência selecionada alimenta solicitações, movimentos e dashboard.", "GESTOR"],
  ["Nova agência", "/agencias/nova", "Cadastrar uma unidade com parâmetros financeiros iniciais.", "Preenche código, nome, tipo, saldo e saldo mínimo; confirma o envio.", "Dados cadastrais e valores monetários validados.", "Cria a unidade usada como origem ou destino dos fluxos de numerário.", "GESTOR"],
  ["Detalhe da agência", "/agencias/:id", "Consultar e atualizar uma unidade específica.", "Edita dados permitidos, acompanha saldo e desativa quando aplicável.", "Cadastro, versão otimista, saldo, mínimo e situação.", "Fornece contexto para auditoria e operações associadas.", "GESTOR"],
  ["Menu de solicitações", "/solicitacoes", "Organizar criação e acompanhamento de solicitações.", "Escolhe nova solicitação ou consulta.", "Atalhos e orientações do processo.", "Inicia o ciclo SUPRIMENTO ou RECOLHIMENTO.", "Autenticado"],
  ["Consultar solicitações", "/solicitacoes/consultar", "Acompanhar o ciclo de vida e executar transições autorizadas.", "Filtra, pagina, abre detalhes/histórico e aciona aprovar, rejeitar, programar, expedir, receber, cancelar ou conciliar.", "Tipo, unidade, valores, status, versão, responsáveis, datas, justificativas e histórico.", "Orquestra operações de numerário e produz movimentações somente nos marcos financeiros.", "Autenticado; comandos críticos GESTOR"],
  ["Nova solicitação", "/solicitacoes/nova", "Registrar pedido de suprimento ou recolhimento.", "Escolhe tipo e unidade, informa valor e observação e confirma.", "Valor solicitado, agência de origem/destino e contexto do solicitante.", "Cria solicitação PENDENTE; aprovação ainda não altera saldo.", "Autenticado conforme regra da API"],
  ["Menu de movimentações", "/movimentacoes", "Agrupar consulta e lançamento de movimentos.", "Escolhe consultar ou criar.", "Atalhos do módulo financeiro.", "Conecta operações manuais e automáticas ao razão imutável.", "Autenticado"],
  ["Consultar movimentações", "/movimentacoes/consultar", "Pesquisar o livro de eventos financeiros.", "Filtra por agência, tipo e período; pagina resultados.", "Valor, tipo, data, descrição, saldo anterior/posterior e referência.", "Explica alterações de saldo e alimenta auditoria/dashboard.", "Autenticado"],
  ["Nova movimentação", "/movimentacoes/nova", "Lançar depósito, retirada, coleta ou ajuste autorizado.", "Seleciona agência/tipo, informa valor, descrição e chave idempotente.", "Dados do movimento e resultado com saldos.", "Atualiza saldo atomicamente e grava movimento imutável.", "Autenticado; regras financeiras na API"],
  ["Livro-caixa", "/livro-caixa", "Apresentar visão consolidada do razão por período.", "Seleciona filtros, revisa entradas/saídas e totais.", "Movimentos, saldos anterior/posterior e totalizadores.", "É a trilha contábil derivada das movimentações persistidas.", "GESTOR"],
  ["Erro", "/erro e rota desconhecida", "Comunicar falha de navegação ou condição não recuperável.", "Lê a orientação e retorna a um ponto seguro.", "Mensagem contextual sem detalhes sensíveis.", "Recebe rotas inválidas e falhas encaminhadas pelos fluxos.", "Público"],
];

const endpointGroups = [
  ["Autenticação", ["POST /api/v1/auth/login", "POST /api/v1/auth/refresh", "POST /api/v1/auth/logout", "GET /api/v1/auth/me (BFF)"]],
  ["Agências", ["POST /api/v1/agencias", "GET /api/v1/agencias", "GET /api/v1/agencias/{id}", "GET /api/v1/agencias/{id}/detalhe", "PUT /api/v1/agencias/{id}", "DELETE /api/v1/agencias/{id}"]],
  ["Dashboard e razão", ["GET /api/v1/dashboard", "POST /api/v1/movimentacoes", "GET /api/v1/movimentacoes", "GET /api/v1/operacoes-numerario", "POST /api/v1/tesouraria/carga-inicial"]],
  ["Solicitações evoluídas", ["POST /api/v1/solicitacoes-numerario", "GET /api/v1/solicitacoes-numerario", "GET /api/v1/solicitacoes-numerario/{id}", "GET /api/v1/solicitacoes-numerario/{id}/historico", "PUT .../{id}/aprovar", "PUT .../{id}/rejeitar", "PUT .../{id}/cancelar", "PUT .../{id}/programar", "PUT .../{id}/iniciar-separacao", "PUT .../{id}/expedir", "PUT .../{id}/registrar-ocorrencia", "PUT .../{id}/receber", "PUT .../{id}/conciliar", "POST .../{id}/ajustes-divergencia"]],
  ["Compatibilidade existente", ["POST /api/v1/solicitacoes", "GET /api/v1/solicitacoes", "PUT /api/v1/solicitacoes/{id}/aprovar", "PUT .../{id}/rejeitar", "PUT .../{id}/atender", "POST /api/v1/usuarios", "GET /api/v1/usuarios/{id}", "GET /api/v1/unidades-operacionais"]],
];

const businessRules = [
  ["Autorização e sessão", [
    "Toda rota funcional exige autenticação; o Angular mantém access e refresh tokens no localStorage e envia o access token como Bearer.",
    "O access token expira em 15 minutos; o refresh expira em 8 horas, é aleatório, armazenado apenas como hash SHA-256 e rotacionado a cada uso.",
    "Logout envia o refresh token para revogação e remove todos os dados locais da sessão.",
    "Funções de tesouraria sensíveis, gestão de agências e livro-caixa exigem perfil GESTOR; a API é a autoridade final, mesmo que o frontend esconda opções.",
    "Após tentativas inválidas consecutivas, o controle de login aplica bloqueio conforme dados de tentativas persistidos.",
  ]],
  ["Agências e unidades", [
    "O código da agência é obrigatório e único; nome, tipo e estado cadastral são validados.",
    "Saldo inicial e saldo mínimo não podem ser negativos.",
    "Uma agência é desativada logicamente para preservar referências históricas; não há exclusão física de movimentos ou operações.",
    "Saldo abaixo do mínimo gera alerta; a necessidade sugerida de aporte corresponde à diferença positiva até o mínimo.",
    "Atualizações usam versão otimista; uma versão desatualizada é rejeitada com HTTP 409.",
  ]],
  ["Movimentação financeira", [
    "O valor deve ser maior que zero. DEPÓSITO soma; RETIRADA/COLETA subtrai; AJUSTE exige direção explícita.",
    "Saídas não podem deixar saldo negativo. A alteração do saldo e a gravação do movimento ocorrem na mesma transação.",
    "Cada comando mutável usa chave de idempotência; repetir a mesma chave não pode duplicar efeito financeiro.",
    "Movimentos são imutáveis e registram saldo anterior, saldo posterior, ator, instante e referência operacional.",
    "Aprovar ou programar uma solicitação não altera saldo; o débito ocorre na expedição e o crédito no recebimento.",
  ]],
  ["Solicitação de numerário", [
    "Existem SUPRIMENTO e RECOLHIMENTO; não existe transferência direta informal entre agências.",
    "Toda solicitação nasce PENDENTE e registra solicitante, valor total e unidade relevante, sem composição por denominações.",
    "Aprovação ou rejeição exige decisão explícita; a rejeição exige justificativa. Autoaprovação é permitida pelo modelo atual.",
    "Após aprovação, a tesouraria define origem/destino e programa a operação; somente então a separação e a expedição podem avançar.",
    "Cancelamento é permitido antes da expedição; após aprovação requer confirmação. Depois da expedição é proibido.",
    "Recebimento registra usuário e instante. Se o valor recebido divergir do expedido, justificativa é obrigatória e a solicitação fica pendente de conciliação.",
    "A conciliação encerra a divergência preservando valores solicitado, expedido, recebido e diferença.",
    "Toda transição valida o status anterior e a versão; transição fora de ordem é rejeitada.",
  ]],
  ["Auditoria, API e dados", [
    "O histórico de solicitação é append-only e registra evento, status anterior/novo, ator, instante, justificativa e metadados.",
    "Datas trafegam em ISO-8601/UTC e valores monetários usam decimal, nunca ponto flutuante de domínio.",
    "Listagens são paginadas e filtros/ordenação aceitam somente campos conhecidos.",
    "Erros seguem contrato padronizado, sem stack trace ou segredo; validação retorna campos inválidos e conflitos retornam 409.",
    "Todas as rotas públicas permanecem sob /api/v1; a migração não criou /v2.",
    "Flyway automático fica desabilitado por padrão. A evolução V3→V6 deve passar por backup, validações prévias, migração controlada e conferência pós-migração.",
  ]],
];

const migrations = [
  ["V1", "Esquema inicial", "Cria usuários, agências, solicitações de abastecimento e movimentações, com chaves e índices básicos."],
  ["V2", "Direção da movimentação", "Acrescenta a direção necessária para ajustes e interpretação consistente de entradas/saídas."],
  ["V3", "Tentativas de login", "Persiste tentativas inválidas e informações de bloqueio da autenticação."],
  ["V4", "Evolução operacional", "Introduz unidades operacionais, solicitações/ operações de numerário, histórico, comandos idempotentes, versões e migra dados legados."],
  ["V5", "Sessões de refresh", "Cria persistência de refresh tokens revogáveis e expirados para renovação segura."],
  ["V6", "Hash do refresh token", "Alinha o tipo/tamanho da coluna de hash ao armazenamento SHA-256 usado pela autenticação."],
];

function methodPurpose(method, item) {
  const lower = method.name.toLowerCase();
  if (lower.startsWith("get") || lower.startsWith("find") || lower.startsWith("buscar") || lower.startsWith("consult")) return "Consulta e devolve dados sem alterar o estado principal.";
  if (lower.startsWith("set") || lower.startsWith("update") || lower.startsWith("atualiz")) return "Atualiza o estado validado deste componente.";
  if (lower.startsWith("create") || lower.startsWith("criar") || lower.startsWith("cadastrar") || lower.startsWith("salvar")) return "Cria ou persiste um novo resultado conforme as invariantes.";
  if (lower.includes("map") || lower.startsWith("to")) return "Converte dados entre representações mantendo o contrato das camadas.";
  if (lower.includes("valid")) return "Verifica pré-condições e rejeita dados incompatíveis com a regra.";
  if (lower.includes("aprovar") || lower.includes("rejeitar") || lower.includes("expedir") || lower.includes("receber") || lower.includes("conciliar") || lower.includes("cancelar")) return "Executa uma transição explícita do fluxo de solicitação.";
  if (lower.includes("login") || lower.includes("autent") || lower.includes("refresh") || lower.includes("logout")) return "Participa do ciclo seguro de autenticação e sessão.";
  if (item.type === "Teste automatizado") return "Executa um cenário automatizado e comprova o resultado esperado.";
  return `Executa a operação “${method.name}” dentro da responsabilidade de ${path.basename(item.file, item.extension)}.`;
}

function catalogGroup(item) {
  if (item.relative.includes("/src/test/") || item.relative.endsWith(".spec.ts")) return "Testes automatizados";
  if (item.relative.startsWith("api-numerario/")) return "API — produção";
  if (item.relative.startsWith("bff-numerario/")) return "BFF — produção";
  if (item.relative.startsWith("frontend-numerario/")) return "Frontend Angular";
  if (item.extension === ".sql") return "Banco de dados";
  if (item.relative.startsWith("docs/")) return "Documentação de apoio";
  return "Operação e configuração";
}

const grouped = Map.groupBy(catalog, catalogGroup);

const nav = [
  ["visao-geral", "Visão geral"], ["arquitetura", "Arquitetura e fluxo"], ["telas", "Fluxo das telas"],
  ["regras", "Regras de negócio"], ["estados", "Estados e transições"], ["api", "Contratos /api/v1"],
  ["dados", "Banco e migrações"], ["seguranca", "Segurança"], ["operacao", "Operação e testes"],
  ["catalogo", "Catálogo técnico"], ["glossario", "Glossário"],
];

const html = `<!doctype html>
<html lang="pt-BR">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="color-scheme" content="light">
  <title>COIN — Documentação completa do projeto</title>
  <style>
    :root{--red:#b4102b;--red2:#7c0c22;--ink:#18212f;--muted:#667085;--line:#e4e7ec;--soft:#f7f8fa;--ok:#147d64;--blue:#175cd3;--amber:#b54708;--white:#fff;--shadow:0 10px 30px rgba(16,24,40,.08)}
    *{box-sizing:border-box}html{scroll-behavior:smooth}body{margin:0;font:15px/1.62 Inter,Segoe UI,Arial,sans-serif;color:var(--ink);background:#f2f4f7}a{color:var(--red2)}code,.mono{font-family:Consolas,SFMono-Regular,monospace;font-size:.92em}.app{display:grid;grid-template-columns:280px minmax(0,1fr);min-height:100vh}
    aside{position:sticky;top:0;height:100vh;overflow:auto;background:#111827;color:#d0d5dd;padding:25px 18px}.brand{display:flex;gap:12px;align-items:center;margin:0 5px 24px}.logo{display:grid;place-items:center;width:42px;height:42px;border-radius:12px;background:linear-gradient(145deg,#df183d,var(--red2));font-weight:800;color:white}.brand strong{display:block;color:white;font-size:18px}.brand small{color:#98a2b3}.nav a{display:block;color:#cbd5e1;text-decoration:none;padding:9px 11px;border-radius:8px;margin:2px 0}.nav a:hover,.nav a.active{color:white;background:#273244}.side-note{border-top:1px solid #344054;margin-top:24px;padding:18px 8px 0;font-size:12px;color:#98a2b3}
    main{min-width:0}.hero{background:linear-gradient(125deg,#751024 0%,#b4102b 58%,#df3150 100%);color:white;padding:58px clamp(25px,5vw,76px)}.hero-inner,.content{max-width:1280px;margin:auto}.eyebrow{font-size:12px;font-weight:800;letter-spacing:.14em;text-transform:uppercase;opacity:.85}.hero h1{font-size:clamp(34px,5vw,62px);line-height:1.04;margin:12px 0 18px;max-width:900px}.hero p{font-size:18px;max-width:850px;color:#ffe8ed}.badges{display:flex;flex-wrap:wrap;gap:9px;margin-top:25px}.badge{border:1px solid rgba(255,255,255,.35);background:rgba(255,255,255,.12);padding:6px 10px;border-radius:999px;font-size:13px}
    .content{padding:40px clamp(20px,4vw,64px) 80px}section{scroll-margin-top:18px;margin:0 0 54px}.section-head{display:flex;align-items:end;justify-content:space-between;gap:20px;border-bottom:2px solid var(--line);padding-bottom:13px;margin-bottom:22px}.section-head h2{font-size:29px;line-height:1.2;margin:0}.section-head p{margin:0;color:var(--muted);max-width:650px}.grid{display:grid;gap:16px}.g2{grid-template-columns:repeat(2,minmax(0,1fr))}.g3{grid-template-columns:repeat(3,minmax(0,1fr))}.g4{grid-template-columns:repeat(4,minmax(0,1fr))}.card{background:white;border:1px solid var(--line);border-radius:13px;padding:20px;box-shadow:0 1px 2px rgba(16,24,40,.03)}.card h3{margin:0 0 9px;font-size:18px}.card p{margin:6px 0;color:#475467}.metric strong{display:block;font-size:29px;color:var(--red2)}.metric span{color:var(--muted)}
    .callout{padding:18px 20px;border-radius:10px;border-left:4px solid var(--blue);background:#eff6ff}.callout.warn{border-color:var(--amber);background:#fff7ed}.callout.ok{border-color:var(--ok);background:#ecfdf3}.callout strong{display:block;margin-bottom:4px}.tag{display:inline-block;padding:3px 8px;border-radius:99px;background:#f2f4f7;color:#344054;font-size:12px}.tag.manager{background:#ffeaee;color:#8b1026}.tag.public{background:#ecfdf3;color:#067647}
    .flow{display:flex;align-items:stretch;gap:10px;overflow:auto;padding:8px 2px 18px}.node{flex:1;min-width:155px;background:white;border:1px solid var(--line);border-top:4px solid var(--red);border-radius:10px;padding:15px}.node b{display:block}.node small{color:var(--muted)}.arrow{display:grid;place-items:center;font-size:25px;color:var(--red2)}
    .screen{display:grid;grid-template-columns:210px 1fr;gap:20px}.screen .route{font-weight:700;color:var(--red2)}.screen dl{display:grid;grid-template-columns:135px 1fr;gap:5px 13px;margin:0}.screen dt{font-weight:700;color:#344054}.screen dd{margin:0;color:#475467}.screen-card{border-left:4px solid var(--red)}
    .rules h3{color:var(--red2)}ul{padding-left:21px}li+li{margin-top:6px}.state-line{display:flex;align-items:center;gap:7px;flex-wrap:wrap}.state{padding:7px 10px;border:1px solid #d0d5dd;background:white;border-radius:7px;font-weight:700;font-size:13px}.state.alt{border-color:#fda29b;background:#fff1f3}.mini-arrow{color:var(--red2);font-weight:800}
    table{width:100%;border-collapse:collapse;background:white;border:1px solid var(--line)}th,td{text-align:left;vertical-align:top;padding:11px 12px;border-bottom:1px solid var(--line)}th{background:#f8fafc;font-size:12px;text-transform:uppercase;letter-spacing:.04em;color:#475467}tr:last-child td{border-bottom:0}.endpoints code{display:block;margin:5px 0;padding:5px 8px;background:#f7f8fa;border-radius:5px;color:#344054}
    .toolbar{display:flex;flex-wrap:wrap;gap:10px;margin-bottom:18px;position:sticky;top:8px;z-index:4;padding:10px;background:rgba(242,244,247,.92);backdrop-filter:blur(8px);border-radius:10px}.toolbar input{flex:1;min-width:240px;border:1px solid #98a2b3;border-radius:8px;padding:10px 12px;background:white}.btn{border:1px solid #98a2b3;background:white;color:#344054;border-radius:8px;padding:9px 12px;cursor:pointer}.btn.primary{background:var(--red);border-color:var(--red);color:white}.catalog-group{margin:28px 0}.catalog-group h3{display:flex;justify-content:space-between}.file{background:white;border:1px solid var(--line);border-radius:9px;margin:8px 0;overflow:hidden}.file>summary{cursor:pointer;padding:13px 15px;font-weight:700;display:flex;justify-content:space-between;gap:12px}.file>summary:hover{background:#fafafa}.file-body{border-top:1px solid var(--line);padding:16px}.file-path{font-family:Consolas,monospace;overflow-wrap:anywhere}.file-meta{display:grid;grid-template-columns:150px 1fr;gap:7px 12px}.file-meta dt{font-weight:700}.file-meta dd{margin:0;color:#475467}.methods{margin-top:14px}.methods td:first-child{white-space:nowrap}.signature{overflow-wrap:anywhere}.empty{color:var(--muted);font-style:italic}.footer{text-align:center;color:var(--muted);padding:25px}
    @media(max-width:980px){.app{display:block}aside{position:relative;height:auto}.nav{display:flex;overflow:auto}.nav a{white-space:nowrap}.side-note{display:none}.g3,.g4{grid-template-columns:repeat(2,minmax(0,1fr))}.screen{grid-template-columns:1fr}}
    @media(max-width:640px){.g2,.g3,.g4{grid-template-columns:1fr}.hero{padding:38px 22px}.content{padding:28px 15px}.section-head{display:block}.section-head p{margin-top:8px}.screen dl,.file-meta{grid-template-columns:1fr}.screen dt{margin-top:6px}.flow{display:grid}.arrow{transform:rotate(90deg)}}
    @media print{aside,.toolbar,.btn{display:none!important}.app{display:block}.hero{background:white!important;color:#111;padding:20px 0;border-bottom:3px solid #b4102b}.hero p{color:#444}.badge{border-color:#999}.content{padding:20px 0}.card,.file{box-shadow:none;break-inside:avoid}details.file:not([open]) .file-body{display:block}section{break-before:auto}}
  </style>
</head>
<body>
<div class="app">
  <aside>
    <div class="brand"><div class="logo">C</div><div><strong>COIN</strong><small>Gestão de numerário</small></div></div>
    <nav class="nav">${nav.map(([id, label]) => `<a href="#${id}">${esc(label)}</a>`).join("")}</nav>
    <div class="side-note">Documento autocontido<br>Baseline de dados: Flyway V6<br>Rotas preservadas: <code>/api/v1</code><br>Gerado em ${generatedAt}</div>
  </aside>
  <main>
    <header class="hero"><div class="hero-inner">
      <div class="eyebrow">Documentação técnica e funcional completa</div>
      <h1>COIN — Gestão de numerário</h1>
      <p>Arquitetura, jornadas de tela, regras de negócio, contratos, modelo de dados e catálogo justificativo de cada componente do código no estado migrado para V6.</p>
      <div class="badges"><span class="badge">Angular + BFF + API hexagonal</span><span class="badge">Somente /api/v1</span><span class="badge">MySQL / Flyway V6</span><span class="badge">JWT Bearer + localStorage</span><span class="badge">${catalog.length} arquivos catalogados</span><span class="badge">${methodCount} métodos identificados</span></div>
    </div></header>
    <div class="content">
      <section id="visao-geral">
        <div class="section-head"><div><div class="eyebrow" style="color:var(--red)">01</div><h2>Visão geral</h2></div><p>O sistema controla unidades, solicitações e movimentos de numerário com rastreabilidade financeira e separação clara entre interface, segurança e domínio.</p></div>
        <div class="grid g4">
          <div class="card metric"><strong>${count((x)=>x.relative.startsWith("api-numerario/"))}</strong><span>arquivos da API</span></div>
          <div class="card metric"><strong>${count((x)=>x.relative.startsWith("bff-numerario/"))}</strong><span>arquivos do BFF</span></div>
          <div class="card metric"><strong>${count((x)=>x.relative.startsWith("frontend-numerario/"))}</strong><span>arquivos do frontend</span></div>
          <div class="card metric"><strong>V6</strong><span>versão atual do banco</span></div>
        </div>
        <div class="grid g3" style="margin-top:16px">
          <div class="card"><h3>Frontend Angular</h3><p>SPA responsável por navegação, formulários, feedback visual e restrição antecipada de rotas. Consome apenas o BFF.</p></div>
          <div class="card"><h3>BFF Spring Boot</h3><p>Proxy tipado para a API. Encaminha o header Bearer e preserva os contratos sem criar uma nova versão.</p></div>
          <div class="card"><h3>API Spring Boot</h3><p>Núcleo hexagonal que valida regras, autorizações, idempotência, transações, persistência e auditoria no MySQL.</p></div>
        </div>
        <div class="callout ok" style="margin-top:16px"><strong>Estado validado da migração local</strong>34 agências, 36 unidades operacionais, 182 solicitações, 92 operações, 437 eventos de histórico e 545 movimentações; nenhuma anomalia de integridade identificada após V6.</div>
      </section>

      <section id="arquitetura">
        <div class="section-head"><div><div class="eyebrow" style="color:var(--red)">02</div><h2>Arquitetura e fluxo principal</h2></div><p>Fluxo síncrono em camadas, com o navegador isolado dos tokens e o domínio isolado das tecnologias de transporte e persistência.</p></div>
        <div class="flow" aria-label="Fluxo arquitetural">
          <div class="node"><b>Usuário / navegador</b><small>Interage com telas e formulários</small></div><div class="arrow">→</div>
          <div class="node"><b>Angular</b><small>Componentes, guards, serviços e interceptors</small></div><div class="arrow">→</div>
          <div class="node"><b>BFF</b><small>Proxy tipado e encaminhamento Bearer em /api/v1</small></div><div class="arrow">→</div>
          <div class="node"><b>Controllers API</b><small>Validação REST, identidade e códigos HTTP</small></div><div class="arrow">→</div>
          <div class="node"><b>Casos de uso</b><small>Regras, estados, idempotência e transações</small></div><div class="arrow">→</div>
          <div class="node"><b>Adapters / MySQL</b><small>JPA, Flyway V6, histórico e razão</small></div>
        </div>
        <div class="grid g2">
          <div class="card"><h3>Entrada de uma consulta</h3><ol><li>Guard confirma a sessão local antes de ativar a rota.</li><li>Interceptor lê o access token e adiciona o header Bearer.</li><li>Serviço Angular chama o mesmo caminho <code>/api/v1</code> no BFF.</li><li>BFF encaminha o header para a API.</li><li>Caso de uso consulta por porta de saída; adapter mapeia JPA para domínio.</li><li>A resposta retorna tipada e paginada até a tela.</li></ol></div>
          <div class="card"><h3>Entrada de um comando</h3><ol><li>Frontend valida o formulário e envia Bearer, versão e chave idempotente.</li><li>BFF preserva o header de autenticação e encaminha o contrato.</li><li>API autentica, autoriza e valida o contrato.</li><li>Caso de uso verifica estado, versão, saldo e invariantes.</li><li>Em uma transação, persiste estado, movimento e histórico.</li><li>Repetição idempotente devolve resultado sem duplicar efeito.</li></ol></div>
        </div>
        <div class="callout warn" style="margin-top:16px"><strong>Fronteira arquitetural</strong>O núcleo (<code>core/domain</code> e <code>core/usecase</code>) não importa Spring, JPA ou HTTP. Controllers chamam portas de entrada; adapters implementam portas de saída; configurações apenas compõem dependências.</div>
      </section>

      <section id="telas">
        <div class="section-head"><div><div class="eyebrow" style="color:var(--red)">03</div><h2>Fluxo de cada tela</h2></div><p>Cobertura das rotas declaradas em <code>app.routes.ts</code>, incluindo propósito, interação, dados, conexão e acesso.</p></div>
        <div class="flow"><div class="node"><b>/login</b><small>Autenticação</small></div><div class="arrow">→</div><div class="node"><b>/menu</b><small>Escolha de contexto</small></div><div class="arrow">→</div><div class="node"><b>/tesouraria</b><small>Hub funcional</small></div><div class="arrow">→</div><div class="node"><b>Módulos</b><small>Dashboard · Agências · Solicitações · Movimentos · Livro-caixa</small></div></div>
        <div class="grid">
          ${screens.map(([name, route, purpose, interaction, data, connection, access]) => `<article class="card screen-card screen"><div><h3>${esc(name)}</h3><div class="route"><code>${esc(route)}</code></div><span class="tag ${access==="GESTOR"?"manager":access==="Público"?"public":""}">${esc(access)}</span></div><dl><dt>Propósito</dt><dd>${esc(purpose)}</dd><dt>Interação</dt><dd>${esc(interaction)}</dd><dt>Dados</dt><dd>${esc(data)}</dd><dt>Conexão</dt><dd>${esc(connection)}</dd></dl></article>`).join("")}
        </div>
      </section>

      <section id="regras">
        <div class="section-head"><div><div class="eyebrow" style="color:var(--red)">04</div><h2>Regras de negócio</h2></div><p>Regras consolidadas do domínio, contratos atuais, segurança e procedimento seguro de banco.</p></div>
        <div class="grid g2 rules">${businessRules.map(([title, rules]) => `<div class="card"><h3>${esc(title)}</h3><ul>${rules.map((rule)=>`<li>${esc(rule)}</li>`).join("")}</ul></div>`).join("")}</div>
      </section>

      <section id="estados">
        <div class="section-head"><div><div class="eyebrow" style="color:var(--red)">05</div><h2>Estados e transições</h2></div><p>O estado é uma máquina controlada: cada comando conhece os estados de origem aceitos e registra a mudança no histórico.</p></div>
        <div class="card"><h3>Fluxo principal da solicitação</h3><div class="state-line"><span class="state">PENDENTE</span><span class="mini-arrow">→</span><span class="state">APROVADA</span><span class="mini-arrow">→</span><span class="state">PROGRAMADA</span><span class="mini-arrow">→</span><span class="state">EM_SEPARACAO</span><span class="mini-arrow">→</span><span class="state">EM_TRANSITO</span><span class="mini-arrow">→</span><span class="state">RECEBIDA</span><span class="mini-arrow">→</span><span class="state">CONCLUIDA</span></div><p>Na expedição nasce o débito da origem. No recebimento nasce o crédito do destino. Uma diferença mantém o processo aberto para conciliação.</p></div>
        <div class="grid g3" style="margin-top:16px"><div class="card"><h3>Saídas alternativas</h3><div class="state-line"><span class="state alt">REJEITADA</span><span class="state alt">CANCELADA</span></div><p>Rejeição exige motivo; cancelamento respeita o marco de expedição.</p></div><div class="card"><h3>Ocorrência</h3><p>Uma ocorrência durante o fluxo registra contexto sem apagar a trilha anterior e pode exigir tratamento antes do avanço.</p></div><div class="card"><h3>Divergência</h3><p>Valor recebido diferente do expedido exige justificativa e ajuste/decisão de conciliação, preservando todos os valores.</p></div></div>
      </section>

      <section id="api">
        <div class="section-head"><div><div class="eyebrow" style="color:var(--red)">06</div><h2>Contratos HTTP preservados</h2></div><p>O BFF e a API mantêm os caminhos atuais. Não existe namespace <code>/api/v2</code>.</p></div>
        <div class="grid g2 endpoints">${endpointGroups.map(([title, endpoints])=>`<div class="card"><h3>${esc(title)}</h3>${endpoints.map((endpoint)=>`<code>${esc(endpoint)}</code>`).join("")}</div>`).join("")}</div>
        <div class="callout" style="margin-top:16px"><strong>Convenções</strong>JSON para entrada/saída; paginação com conteúdo e metadados; datas ISO-8601 em UTC; validações retornam detalhes por campo; 401 para ausência/expiração, 403 para permissão, 404 para recurso ausente e 409 para versão, estado ou idempotência conflitante.</div>
      </section>

      <section id="dados">
        <div class="section-head"><div><div class="eyebrow" style="color:var(--red)">07</div><h2>Banco de dados e migrações</h2></div><p>O esquema é evoluído exclusivamente por scripts Flyway ordenados. A execução automática fica desativada por padrão para proteger bancos existentes.</p></div>
        <table><thead><tr><th>Versão</th><th>Objetivo</th><th>Contribuição</th></tr></thead><tbody>${migrations.map((row)=>`<tr>${row.map((cell)=>`<td>${esc(cell)}</td>`).join("")}</tr>`).join("")}</tbody></table>
        <div class="grid g3" style="margin-top:16px"><div class="card"><h3>Cadastros</h3><p>Usuários, agências e unidades operacionais dão identidade e contexto às operações.</p></div><div class="card"><h3>Fluxo operacional</h3><p>Solicitações, operações e histórico representam estado, logística, decisões e responsáveis.</p></div><div class="card"><h3>Financeiro e segurança</h3><p>Movimentações formam o razão; comandos idempotentes impedem duplicidade; refresh tokens sustentam sessões revogáveis.</p></div></div>
        <div class="callout warn" style="margin-top:16px"><strong>Procedimento obrigatório de evolução</strong>Validar versão e integridade → gerar backup verificável → habilitar Flyway apenas na execução controlada → conferir contagens, órfãos, saldos e versão → manter caminho de restauração. Consulte <code>docs/MIGRACAO_BANCO_SEGURA.md</code> e <code>scripts/migrar-banco-seguro.ps1</code>.</div>
      </section>

      <section id="seguranca">
        <div class="section-head"><div><div class="eyebrow" style="color:var(--red)">08</div><h2>Segurança</h2></div><p>Defesa em profundidade: o frontend melhora a experiência, o BFF protege a sessão e a API aplica autenticação e autorização de forma definitiva.</p></div>
        <div class="grid g3"><div class="card"><h3>Navegador</h3><ul><li>Access e refresh tokens no localStorage.</li><li>Authorization Bearer em chamadas protegidas.</li><li>Renovação automática após 401.</li><li>Mitigação de XSS é obrigatória, pois JavaScript acessa os tokens.</li></ul></div><div class="card"><h3>BFF</h3><ul><li>Login, refresh rotativo e logout em JSON.</li><li>Proxy somente para destinos configurados.</li><li>Preserva o Authorization recebido.</li><li>Não registra credenciais ou tokens.</li></ul></div><div class="card"><h3>API</h3><ul><li>JWT assinado e expiração verificada.</li><li>Perfis aplicados nos endpoints/casos de uso.</li><li>Senhas BCrypt.</li><li>Refresh armazenado como hash e revogável.</li></ul></div></div>
      </section>

      <section id="operacao">
        <div class="section-head"><div><div class="eyebrow" style="color:var(--red)">09</div><h2>Operação, build e qualidade</h2></div><p>Critérios usados para entregar a migração e manter a execução reproduzível.</p></div>
        <div class="grid g3"><div class="card metric"><strong>67/67</strong><span>testes seguros da API (persistência externa excluída)</span></div><div class="card metric"><strong>30/30</strong><span>testes do BFF após simplificação JWT</span></div><div class="card metric"><strong>Build OK</strong><span>frontend sem suíte/specs locais</span></div></div>
        <div class="grid g2" style="margin-top:16px"><div class="card"><h3>Inicialização</h3><ol><li>Subir MySQL conforme configuração local.</li><li>Confirmar que o esquema já está em V6.</li><li>Iniciar API e BFF com perfis/segredos externos.</li><li>Iniciar Angular e acessar a rota de login.</li></ol><p>O script <code>scripts/iniciar-tudo.ps1</code> automatiza o ambiente local.</p></div><div class="card"><h3>Critérios de regressão</h3><ul><li>Build de produção dos três módulos.</li><li>Suites unitárias/integração segura.</li><li>Busca por rotas fora de <code>/api/v1</code>.</li><li>Validação visual e funcional das jornadas.</li><li>Conferência de versão e integridade do banco.</li></ul></div></div>
      </section>

      <section id="catalogo">
        <div class="section-head"><div><div class="eyebrow" style="color:var(--red)">10</div><h2>Catálogo técnico por arquivo e método</h2></div><p>Inventário gerado diretamente do workspace. Cada item explica responsabilidade, decisão de desenho, contribuição, relações internas e métodos detectados.</p></div>
        <div class="toolbar"><input id="search" type="search" placeholder="Filtrar por arquivo, classe, tipo, método ou dependência…" aria-label="Filtrar catálogo"><button class="btn" id="expand">Expandir visíveis</button><button class="btn" id="collapse">Recolher</button><button class="btn primary" onclick="window.print()">Imprimir / PDF</button></div>
        <div id="catalog-results">
        ${[...grouped.entries()].map(([group, items])=>`<div class="catalog-group"><h3>${esc(group)} <span class="tag">${items.length} arquivos</span></h3>${items.map((item)=>{
          const searchText = [item.relative,item.type,item.declaration,...item.imports,...item.methods.map((m)=>m.name)].join(" ").toLowerCase();
          return `<details class="file" data-search="${esc(searchText)}"><summary><span class="file-path">${esc(item.relative)}</span><span class="tag">${esc(item.type)}</span></summary><div class="file-body"><dl class="file-meta"><dt>Declaração</dt><dd>${esc(item.declaration || "Arquivo declarativo/configuracional")}</dd><dt>Responsabilidade</dt><dd>${esc(item.purpose)}</dd><dt>Por que existe assim</dt><dd>${esc(item.rationale)}</dd><dt>Contribuição</dt><dd>${esc(item.contribution)}</dd><dt>Dependências internas</dt><dd>${item.imports.length ? item.imports.map((dep)=>`<code>${esc(dep)}</code>`).join(" · ") : "Nenhuma dependência interna explícita; atua como folha, contrato ou configuração."}</dd></dl><div class="methods"><h4>Métodos identificados (${item.methods.length})</h4>${item.methods.length ? `<table><thead><tr><th>Linha</th><th>Método / assinatura</th><th>Justificativa funcional</th></tr></thead><tbody>${item.methods.map((method)=>`<tr><td>${method.line}</td><td><strong>${esc(method.name)}</strong><div class="signature"><code>${esc(method.signature)}</code></div></td><td>${esc(methodPurpose(method,item))}</td></tr>`).join("")}</tbody></table>` : `<p class="empty">Sem método executável próprio: o arquivo declara dados, marcação, estilo, migração, configuração ou contrato.</p>`}</div></div></details>`;
        }).join("")}</div>`).join("")}
        </div>
      </section>

      <section id="glossario">
        <div class="section-head"><div><div class="eyebrow" style="color:var(--red)">11</div><h2>Glossário</h2></div><p>Termos usados de forma consistente no código e nesta documentação.</p></div>
        <table><thead><tr><th>Termo</th><th>Definição</th></tr></thead><tbody>
          <tr><td>BFF</td><td>Backend for Frontend: camada dedicada ao navegador que protege tokens e adapta a sessão.</td></tr>
          <tr><td>Porta</td><td>Interface do núcleo que define uma entrada de negócio ou necessidade de infraestrutura.</td></tr>
          <tr><td>Adapter</td><td>Implementação técnica que conecta HTTP, JPA, segurança ou tempo às portas.</td></tr>
          <tr><td>Idempotência</td><td>Propriedade que impede um comando repetido de produzir efeito financeiro duplicado.</td></tr>
          <tr><td>Versão otimista</td><td>Número enviado em atualizações para detectar concorrência e evitar sobrescrita silenciosa.</td></tr>
          <tr><td>Razão</td><td>Conjunto imutável de movimentações que explica cada alteração de saldo.</td></tr>
          <tr><td>Flyway</td><td>Mecanismo que aplica scripts SQL versionados e registra a versão do esquema.</td></tr>
        </tbody></table>
      </section>
      <div class="footer">COIN · Documentação gerada a partir do código local em ${generatedAt} · HTML autocontido, sem dependências externas</div>
    </div>
  </main>
</div>
<script>
  const search = document.querySelector("#search");
  const files = [...document.querySelectorAll("details.file")];
  const groups = [...document.querySelectorAll(".catalog-group")];
  function applyFilter(){
    const q = search.value.trim().toLowerCase();
    files.forEach(file => { file.hidden = q && !file.dataset.search.includes(q); });
    groups.forEach(group => { group.hidden = ![...group.querySelectorAll("details.file")].some(file => !file.hidden); });
  }
  search.addEventListener("input", applyFilter);
  document.querySelector("#expand").addEventListener("click",()=>files.filter(file=>!file.hidden).forEach(file=>file.open=true));
  document.querySelector("#collapse").addEventListener("click",()=>files.forEach(file=>file.open=false));
  const links=[...document.querySelectorAll(".nav a")];
  const observer=new IntersectionObserver(entries=>entries.forEach(entry=>{if(entry.isIntersecting){links.forEach(a=>a.classList.toggle("active",a.hash==="#"+entry.target.id));}}),{rootMargin:"-15% 0px -75% 0px"});
  document.querySelectorAll("main section[id]").forEach(section=>observer.observe(section));
</script>
</body>
</html>`;

fs.writeFileSync(output, html, "utf8");
console.log(JSON.stringify({
  output: rel(output),
  bytes: Buffer.byteLength(html),
  files: catalog.length,
  methods: methodCount,
  java: count((item) => item.extension === ".java"),
  typescript: count((item) => item.extension === ".ts"),
  migrations: count((item) => item.extension === ".sql"),
}, null, 2));
