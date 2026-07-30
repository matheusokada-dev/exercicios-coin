import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "..");
const docs = path.join(root, "docs");
const expected = [
  [4200, "DOCUMENTACAO_FRONTEND_4200.html", "frontend-numerario"],
  [8080, "DOCUMENTACAO_BFF_8080.html", "bff-numerario"],
  [8081, "DOCUMENTACAO_API_8081.html", "api-numerario"],
  [8082, "DOCUMENTACAO_RELATORIO_8082.html", "relatorio-numerario"],
];
const ignored = new Set([".git", "node_modules", "target", "dist", "coverage", ".angular", ".idea", ".vscode"]);
const extensions = new Set([".java", ".ts", ".html", ".scss", ".css", ".xml", ".properties", ".json", ".md", ".yml", ".yaml"]);
const failures = [];
const results = [];

function sourceCount(directory) {
  let total = 0;
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    if (ignored.has(entry.name)) continue;
    const absolute = path.join(directory, entry.name);
    if (entry.isDirectory()) total += sourceCount(absolute);
    else if (extensions.has(path.extname(entry.name).toLowerCase())) total += 1;
  }
  return total;
}

function check(label, condition, detail) {
  if (!condition) failures.push(`${label}: ${detail}`);
}

const indexFile = path.join(docs, "DOCUMENTACAO_COMPLETA.html");
check("índice", fs.existsSync(indexFile), "arquivo ausente");
const index = fs.existsSync(indexFile) ? fs.readFileSync(indexFile, "utf8") : "";
check("índice doctype", /^<!doctype html>/i.test(index), "DOCTYPE ausente");

for (const [port, filename, sourceDirectory] of expected) {
  const file = path.join(docs, filename);
  check(`${port} arquivo`, fs.existsSync(file), `${filename} ausente`);
  if (!fs.existsSync(file)) continue;
  const html = fs.readFileSync(file, "utf8");
  const sources = sourceCount(path.join(root, sourceDirectory));
  const entries = (html.match(/<details class="file"/g) || []).length;
  const methods = Number(html.match(/data-methods="(\d+)"/)?.[1] ?? 0);
  const declaredFiles = Number(html.match(/data-files="(\d+)"/)?.[1] ?? 0);
  const scripts = [...html.matchAll(/<script>([\s\S]*?)<\/script>/gi)].map((match) => match[1]);

  check(`${port} doctype`, /^<!doctype html>/i.test(html), "DOCTYPE ausente");
  check(`${port} idioma`, html.includes('<html lang="pt-BR">'), "lang pt-BR ausente");
  check(`${port} charset`, html.includes('<meta charset="utf-8">'), "charset ausente");
  check(`${port} título`, html.includes(`Porta ${port}</title>`), "porta ausente no título");
  check(`${port} índice`, index.includes(filename), "link ausente no índice");
  check(`${port} fontes`, entries === sources && declaredFiles === sources, `fontes ${sources}, catálogo ${entries}, declarado ${declaredFiles}`);
  check(`${port} métodos`, methods > 0, "nenhum método documentado");
  check(`${port} fluxo`, html.includes('id="fluxo"') && (html.match(/class="step"/g) || []).length >= 7, "fluxo incompleto");
  check(`${port} catálogo`, html.includes('id="catalogo"') && html.includes("Cada classe, arquivo e método"), "catálogo incompleto");
  check(`${port} responsividade`, html.includes("@media(max-width:980px)") && html.includes("@media(max-width:640px)"), "breakpoints ausentes");
  check(`${port} impressão`, html.includes("@media print"), "estilo de impressão ausente");
  check(`${port} autocontido`, !/<(?:script|link|img)[^>]+(?:src|href)=["']https?:/i.test(html), "dependência externa");
  check(`${port} codificação`, !/[ÃÂ][^\s<]|â€|�/.test(html), "texto possivelmente corrompido");
  check(`${port} JavaScript`, scripts.length === 1, `${scripts.length} scripts inline`);
  if (scripts.length === 1) {
    try {
      new Function(scripts[0]);
    } catch (error) {
      failures.push(`${port} JavaScript inválido: ${error.message}`);
    }
  }

  results.push({
    porta: port,
    arquivo: `docs/${filename}`,
    bytes: fs.statSync(file).size,
    arquivosCatalogados: entries,
    metodosDocumentados: methods,
  });
}

console.log(JSON.stringify({
  indice: "docs/DOCUMENTACAO_COMPLETA.html",
  documentos: results,
  verificacoes: expected.length * 14 + 2,
  falhas: failures,
}, null, 2));
if (failures.length) process.exitCode = 1;
