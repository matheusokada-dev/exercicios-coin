$raizProjeto = Split-Path -Parent $PSScriptRoot

function Iniciar-Terminal {
    param(
        [string]$Titulo,
        [string]$Diretorio,
        [string]$Comando
    )

    $instrucao = "`$Host.UI.RawUI.WindowTitle = '$Titulo'; Set-Location -LiteralPath '$Diretorio'; $Comando"
    Start-Process powershell.exe -ArgumentList '-NoExit', '-Command', $instrucao
}

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
