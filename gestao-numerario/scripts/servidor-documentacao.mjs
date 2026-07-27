import http from "node:http";
import fs from "node:fs";
import path from "node:path";

const port = Number(process.argv[2] || 4317);
const docs = path.resolve(import.meta.dirname, "..", "docs");

http.createServer((request, response) => {
  const requested = decodeURIComponent(new URL(request.url, `http://127.0.0.1:${port}`).pathname);
  const relative = requested === "/" ? "DOCUMENTACAO_COMPLETA.html" : requested.replace(/^\/+/, "");
  const file = path.resolve(docs, relative);
  if (!file.startsWith(`${docs}${path.sep}`) || !fs.existsSync(file) || !fs.statSync(file).isFile()) {
    response.writeHead(404, { "content-type": "text/plain; charset=utf-8" });
    response.end("Arquivo não encontrado.");
    return;
  }
  const type = path.extname(file) === ".html" ? "text/html; charset=utf-8" : "application/octet-stream";
  response.writeHead(200, { "content-type": type, "cache-control": "no-store" });
  fs.createReadStream(file).pipe(response);
}).listen(port, "127.0.0.1", () => {
  console.log(`Documentação disponível em http://127.0.0.1:${port}/`);
});
