import fs from "node:fs";
import path from "node:path";

const services = [
  {
    name: "API",
    url: process.env.API_OPENAPI_URL ?? "http://localhost:8081/v3/api-docs",
    expectedOperations: 34,
    unavailableResponses: [],
    requiresBearer: true,
  },
  {
    name: "BFF",
    url: process.env.BFF_OPENAPI_URL ?? "http://localhost:8080/v3/api-docs",
    expectedOperations: 32,
    unavailableResponses: ["503", "504"],
    requiresBearer: true,
  },
  {
    name: "Relatórios",
    url: process.env.RELATORIOS_OPENAPI_URL ?? "http://localhost:8082/v3/api-docs",
    expectedOperations: 1,
    unavailableResponses: [],
    requiresBearer: false,
  },
];

const methods = new Set(["get", "post", "put", "patch", "delete"]);
const failures = [];
const results = [];

validarArquiteturaDosContratos();

for (const service of services) {
  let document;
  try {
    const response = await fetch(service.url, {
      headers: { Accept: "application/json" },
      signal: AbortSignal.timeout(60_000),
    });
    if (!response.ok) {
      failures.push(`${service.name}: ${service.url} respondeu HTTP ${response.status}`);
      continue;
    }
    document = await response.json();
  } catch (error) {
    failures.push(`${service.name}: não foi possível consultar ${service.url}: ${error.message}`);
    continue;
  }

  check(service, "info.title", Boolean(document.info?.title));
  check(service, "info.version", Boolean(document.info?.version));
  if (service.requiresBearer) {
    check(
      service,
      "bearerAuth",
      document.components?.securitySchemes?.bearerAuth?.type === "http"
        && document.components?.securitySchemes?.bearerAuth?.scheme === "bearer",
    );
    check(service, "segurança global", Array.isArray(document.security) && document.security.length > 0);
  } else {
    check(
      service,
      "sem segurança global fictícia",
      !Array.isArray(document.security) || document.security.length === 0,
    );
  }

  const operations = [];
  for (const [path, pathItem] of Object.entries(document.paths ?? {})) {
    for (const [method, operation] of Object.entries(pathItem)) {
      if (!methods.has(method)) continue;
      operations.push({ path, method: method.toUpperCase(), operation });
    }
  }

  check(
    service,
    "quantidade de operações",
    operations.length === service.expectedOperations,
    `esperado ${service.expectedOperations}, encontrado ${operations.length}`,
  );

  for (const { path, method, operation } of operations) {
    const label = `${method} ${path}`;
    check(service, `${label} summary`, Boolean(operation.summary));
    check(service, `${label} description`, Boolean(operation.description));
    check(service, `${label} operationId`, Boolean(operation.operationId));
    check(service, `${label} tags`, Array.isArray(operation.tags) && operation.tags.length > 0);
    check(service, `${label} response 400`, Boolean(operation.responses?.["400"]));
    check(service, `${label} response 500`, Boolean(operation.responses?.["500"]));

    if (service.requiresBearer) {
      const publicOperation = path === "/api/v1/auth/login";
      if (publicOperation) {
        check(
          service,
          `${label} público`,
          Array.isArray(operation.security) && operation.security.length === 0,
        );
      } else {
        check(service, `${label} response 401`, Boolean(operation.responses?.["401"]));
        check(service, `${label} response 403`, Boolean(operation.responses?.["403"]));
      }
    }

    for (const code of service.unavailableResponses) {
      check(service, `${label} response ${code}`, Boolean(operation.responses?.[code]));
    }

    for (const [code, response] of Object.entries(operation.responses ?? {})) {
      check(service, `${label} response ${code} description`, Boolean(response.description));
    }
  }

  results.push({
    servico: service.name,
    url: service.url,
    operacoes: operations.length,
    tags: Object.keys(document.tags ?? {}).length || new Set(
      operations.flatMap(({ operation }) => operation.tags ?? []),
    ).size,
    schemas: Object.keys(document.components?.schemas ?? {}).length,
  });
}

console.log(JSON.stringify({
  resultados: results,
  arquitetura: "interface *Api -> Controller implements *Api",
  verificacoes: results.reduce((total, result) => total + result.operacoes, 0),
  falhas: failures,
}, null, 2));

if (failures.length) process.exitCode = 1;

function check(service, item, condition, detail = "ausente ou inválido") {
  if (!condition) failures.push(`${service.name} — ${item}: ${detail}`);
}

function validarArquiteturaDosContratos() {
  const roots = [
    {
      name: "API",
      controllers: "api-numerario/src/main/java/br/com/gestaonumerario/api/adapter/input/controller",
      contracts: "api-numerario/src/main/java/br/com/gestaonumerario/api/adapter/input/contract",
    },
    {
      name: "BFF",
      controllers: "bff-numerario/src/main/java/br/com/gestaonumerario/bff/controller",
      contracts: "bff-numerario/src/main/java/br/com/gestaonumerario/bff/contract",
    },
  ];

  for (const root of roots) {
    const controllerFiles = fs.readdirSync(root.controllers)
      .filter((file) => file.endsWith("Controller.java"));

    for (const file of controllerFiles) {
      const controllerName = file.replace(/\.java$/, "");
      const contractName = controllerName.replace(/Controller$/, "Api");
      const controllerSource = fs.readFileSync(path.join(root.controllers, file), "utf8");
      const contractFile = path.join(root.contracts, `${contractName}.java`);

      check(
        root,
        `${controllerName} implementa ${contractName}`,
        controllerSource.includes(`implements ${contractName}`),
      );
      check(
        root,
        `${controllerName} sem anotações OpenAPI funcionais`,
        !/@(?:Tag|Operation|SecurityRequirements)\b/.test(controllerSource),
      );
      check(root, `contrato ${contractName}`, fs.existsSync(contractFile));

      if (!fs.existsSync(contractFile)) continue;

      const contractSource = fs.readFileSync(contractFile, "utf8");
      const mappedOperations = [...controllerSource.matchAll(
        /@(?:org\.springframework\.web\.bind\.annotation\.)?(?:Get|Post|Put|Patch|Delete)Mapping\b/g,
      )].length;
      const documentedOperations = [...contractSource.matchAll(/@Operation\b/g)].length;

      check(root, `${contractName} possui @Tag`, /@Tag\b/.test(contractSource));
      check(
        root,
        `${contractName} documenta todos os métodos`,
        documentedOperations === mappedOperations,
        `esperado ${mappedOperations}, encontrado ${documentedOperations}`,
      );
    }
  }
}
