$raizProjeto = Split-Path -Parent $PSScriptRoot

function Importar-AmbienteLocal {
    $arquivoAmbiente = Join-Path $raizProjeto '.env'
    if (-not (Test-Path -LiteralPath $arquivoAmbiente)) {
        throw 'Arquivo .env ausente. Copie .env.example para .env e preencha os valores locais.'
    }

    Get-Content -LiteralPath $arquivoAmbiente | ForEach-Object {
        $linha = $_.Trim()
        if ($linha -and -not $linha.StartsWith('#')) {
            $partes = $linha.Split('=', 2)
            if ($partes.Count -eq 2) {
                [Environment]::SetEnvironmentVariable(
                    $partes[0].Trim(),
                    $partes[1].Trim(),
                    [EnvironmentVariableTarget]::Process
                )
            }
        }
    }

    @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET') | ForEach-Object {
        if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($_))) {
            throw "Variavel obrigatoria ausente no .env: $_"
        }
    }
}

function Iniciar-Terminal {
    param(
        [string]$Titulo,
        [string]$Diretorio,
        [string]$Comando
    )

    $instrucao = "`$Host.UI.RawUI.WindowTitle = '$Titulo'; Set-Location -LiteralPath '$Diretorio'; $Comando"
    Start-Process powershell.exe -ArgumentList '-NoProfile', '-NoExit', '-Command', $instrucao
}

function Confirmar-PortaLivre {
    param(
        [int]$Porta,
        [string]$Componente
    )

    $ocupada = Get-NetTCPConnection -State Listen -LocalPort $Porta -ErrorAction SilentlyContinue
    if ($ocupada) {
        throw "A porta $Porta de $Componente ja esta em uso. Encerre o processo existente antes de iniciar novamente."
    }
}

function Confirmar-Arquivo {
    param(
        [string]$Caminho,
        [string]$Descricao
    )

    if (-not (Test-Path -LiteralPath $Caminho)) {
        throw "$Descricao nao encontrado: $Caminho"
    }
}

Importar-AmbienteLocal

Confirmar-Arquivo `
    -Caminho (Join-Path $raizProjeto 'api-numerario\mvnw.cmd') `
    -Descricao 'Maven Wrapper'
Confirmar-Arquivo `
    -Caminho (Join-Path $raizProjeto 'frontend-numerario\node_modules\@angular\cli\bin\ng.js') `
    -Descricao 'Angular CLI local. Execute npm install no frontend'

Confirmar-PortaLivre -Porta 8081 -Componente 'API'
Confirmar-PortaLivre -Porta 8082 -Componente 'servico de relatorios'
Confirmar-PortaLivre -Porta 8080 -Componente 'BFF'
Confirmar-PortaLivre -Porta 4200 -Componente 'frontend'

Iniciar-Terminal `
    -Titulo 'API Numerario - 8081' `
    -Diretorio (Join-Path $raizProjeto 'api-numerario') `
    -Comando '.\mvnw.cmd spring-boot:run'

Iniciar-Terminal `
    -Titulo 'Servico Relatorios - 8082' `
    -Diretorio (Join-Path $raizProjeto 'relatorio-numerario') `
    -Comando '..\api-numerario\mvnw.cmd -f .\pom.xml spring-boot:run'

Iniciar-Terminal `
    -Titulo 'BFF Numerario - 8080' `
    -Diretorio (Join-Path $raizProjeto 'bff-numerario') `
    -Comando '..\api-numerario\mvnw.cmd -f .\pom.xml spring-boot:run'

Iniciar-Terminal `
    -Titulo 'Frontend Numerario - 4200' `
    -Diretorio (Join-Path $raizProjeto 'frontend-numerario') `
    -Comando 'npm start'

Write-Host 'API, BFF, servico de relatorios e frontend foram iniciados em terminais separados.' -ForegroundColor Green
Write-Host 'Frontend:     http://localhost:4200' -ForegroundColor Cyan
Write-Host 'Swagger BFF:  http://localhost:8080/swagger-ui.html' -ForegroundColor Cyan
Write-Host 'Swagger API:  http://localhost:8081/swagger-ui.html' -ForegroundColor Cyan
Write-Host 'Relatorios:   http://localhost:8082/v1/relatorios/gerar' -ForegroundColor Cyan
Write-Host 'Validacao:    node scripts/validar-openapi.mjs' -ForegroundColor Cyan
