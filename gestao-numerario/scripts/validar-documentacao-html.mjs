import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "..");
const file = path.join(root, "docs", "DOCUMENTACAO_COMPLETA.html");
const html = fs.readFileSync(file, "utf8");
const failures = [];

function check(name, condition, detail) {
  if (!condition) failures.push(`${name}: ${detail}`);
}

check("doctype", /^<!doctype html>/i.test(html), "DOCTYPE HTML5 ausente");
check("idioma", /<html lang="pt-BR">/.test(html), "idioma pt-BR ausente");
check("charset", /<meta charset="utf-8">/.test(html), "charset UTF-8 ausente");
check("viewport", /name="viewport"/.test(html), "meta viewport ausente");
check("título", /<title>COIN — Documentação completa do projeto<\/title>/.test(html), "título inesperado");
check("codificação", !/[ÃÂ][^\s<]|â€|�/.test(html), "possível texto corrompido");
check("autocontido", !/<(?:script|link|img)[^>]+(?:src|href)=["']https?:/i.test(html), "dependência externa encontrada");
check("versão de rota", !/\b(?:GET|POST|PUT|PATCH|DELETE)\s+\/api\/v2\b/i.test(html), "endpoint /api/v2 encontrado");
check("responsividade", html.includes("@media(max-width:980px)") && html.includes("@media(max-width:640px)"), "breakpoints responsivos ausentes");
check("impressão", html.includes("@media print"), "estilo de impressão ausente");

const requiredSections = ["visao-geral", "arquitetura", "telas", "regras", "estados", "api", "dados", "seguranca", "operacao", "catalogo", "glossario"];
for (const section of requiredSections) {
  check(`seção ${section}`, html.includes(`<section id="${section}">`), "seção obrigatória ausente");
  check(`navegação ${section}`, html.includes(`href="#${section}"`), "atalho de navegação ausente");
}

for (const tag of ["section", "details", "table", "script", "style"]) {
  const openings = (html.match(new RegExp(`<${tag}(?:\\s|>)`, "gi")) || []).length;
  const closings = (html.match(new RegExp(`</${tag}>`, "gi")) || []).length;
  check(`balanceamento ${tag}`, openings === closings, `${openings} aberturas e ${closings} fechamentos`);
}

const scripts = [...html.matchAll(/<script>([\s\S]*?)<\/script>/gi)].map((match) => match[1]);
check("JavaScript", scripts.length === 1, `${scripts.length} scripts inline encontrados`);
if (scripts.length === 1) {
  try {
    new Function(scripts[0]);
  } catch (error) {
    failures.push(`JavaScript: ${error.message}`);
  }
}

const stats = fs.statSync(file);
const fileEntries = (html.match(/<details class="file"/g) || []).length;
const methodRows = (html.match(/<td><strong>[^<]+<\/strong><div class="signature">/g) || []).length;
check("catálogo", fileEntries >= 390, `somente ${fileEntries} arquivos catalogados`);
check("métodos", methodRows >= 580, `somente ${methodRows} métodos catalogados`);
check("conteúdo", stats.size > 500_000, `arquivo possui somente ${stats.size} bytes`);

const result = {
  arquivo: path.relative(root, file).replaceAll("\\", "/"),
  bytes: stats.size,
  secoes: requiredSections.length,
  arquivosCatalogados: fileEntries,
  metodosCatalogados: methodRows,
  verificacoes: 20 + requiredSections.length * 2,
  falhas: failures,
};

console.log(JSON.stringify(result, null, 2));
if (failures.length) process.exitCode = 1;
