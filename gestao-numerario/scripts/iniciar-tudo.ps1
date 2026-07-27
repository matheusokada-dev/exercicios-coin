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
    Start-Process powershell.exe -ArgumentList '-NoExit', '-Command', $instrucao
}

Importar-AmbienteLocal

Iniciar-Terminal `
    -Titulo 'API Numerario - 8081' `
    -Diretorio (Join-Path $raizProjeto 'api-numerario') `
    -Comando '.\mvnw.cmd spring-boot:run'

Iniciar-Terminal `
    -Titulo 'BFF Numerario - 8080' `
    -Diretorio (Join-Path $raizProjeto 'bff-numerario') `
    -Comando '..\api-numerario\mvnw.cmd -f .\pom.xml spring-boot:run'

Iniciar-Terminal `
    -Titulo 'Frontend Numerario - 4200' `
    -Diretorio (Join-Path $raizProjeto 'frontend-numerario') `
    -Comando 'npm start'

Write-Host 'API, BFF e frontend foram iniciados em terminais separados.' -ForegroundColor Green
