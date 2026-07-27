[CmdletBinding()]
param(
    [string]$JdbcUrl = $env:DB_URL,
    [string]$Usuario = $env:DB_USERNAME,
    [string]$Senha = $env:DB_PASSWORD,
    [switch]$Aplicar
)

$ErrorActionPreference = 'Stop'

function Require-Value {
    param([string]$Nome, [string]$Valor)
    if ([string]::IsNullOrWhiteSpace($Valor)) {
        throw "Informe $Nome por parâmetro ou variável de ambiente."
    }
}

Require-Value -Nome 'DB_URL' -Valor $JdbcUrl
Require-Value -Nome 'DB_USERNAME' -Valor $Usuario
Require-Value -Nome 'DB_PASSWORD' -Valor $Senha

if (-not $JdbcUrl.StartsWith('jdbc:mysql://', [StringComparison]::OrdinalIgnoreCase)) {
    throw 'DB_URL deve usar o formato jdbc:mysql://host:porta/banco.'
}

$connectionUri = [Uri]$JdbcUrl.Substring(5)
$databaseName = $connectionUri.AbsolutePath.Trim('/')
if ([string]::IsNullOrWhiteSpace($databaseName)) {
    throw 'Não foi possível identificar o banco na DB_URL.'
}

$mysqlCommand = Get-Command mysql -ErrorAction SilentlyContinue
$mysqldumpCommand = Get-Command mysqldump -ErrorAction SilentlyContinue
$mysqlBin = Join-Path $env:ProgramFiles 'MySQL\MySQL Server 8.4\bin'
$mysqlPath = if ($mysqlCommand) {
    $mysqlCommand.Source
} else {
    Join-Path $mysqlBin 'mysql.exe'
}
$mysqldumpPath = if ($mysqldumpCommand) {
    $mysqldumpCommand.Source
} else {
    Join-Path $mysqlBin 'mysqldump.exe'
}
if (-not (Test-Path -LiteralPath $mysqlPath) -or
    -not (Test-Path -LiteralPath $mysqldumpPath)) {
    throw 'mysql e mysqldump precisam estar instalados e disponíveis no PATH.'
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$backupRoot = Join-Path $repoRoot 'database\backups-local'
New-Item -ItemType Directory -Path $backupRoot -Force | Out-Null

$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$backupPath = Join-Path $backupRoot "gestao_numerario_pre_v4_$timestamp.sql"
$manifestPath = Join-Path $backupRoot "gestao_numerario_pre_v4_$timestamp.txt"
$port = if ($connectionUri.Port -gt 0) { $connectionUri.Port } else { 3306 }
$mysqlArguments = @(
    "--host=$($connectionUri.Host)",
    "--port=$port",
    "--user=$Usuario",
    '--batch',
    '--skip-column-names'
)

$previousMysqlPassword = $env:MYSQL_PWD
$previousFlywayEnabled = $env:FLYWAY_ENABLED
$previousDbUrl = $env:DB_URL
$previousDbUsername = $env:DB_USERNAME
$previousDbPassword = $env:DB_PASSWORD

try {
    $env:MYSQL_PWD = $Senha

    $versionQuery = @'
SELECT COALESCE(MAX(CAST(version AS UNSIGNED)), 0)
FROM flyway_schema_history
WHERE success = 1;
'@
    $currentVersionOutput = & $mysqlPath @mysqlArguments $databaseName "--execute=$versionQuery"
    if ($LASTEXITCODE -ne 0) {
        throw 'Falha ao consultar flyway_schema_history.'
    }
    $currentVersion = ($currentVersionOutput | Select-Object -Last 1).Trim()
    if ($currentVersion -ne '3') {
        throw "A migração segura exige Flyway na versão 3; versão encontrada: $currentVersion."
    }

    $inventoryQuery = @'
SELECT 'agencia', COUNT(*) FROM agencia
UNION ALL SELECT 'movimentacao', COUNT(*) FROM movimentacao
UNION ALL SELECT 'solicitacao_abastecimento', COUNT(*) FROM solicitacao_abastecimento
UNION ALL SELECT 'usuario', COUNT(*) FROM usuario;
'@
    $inventoryBefore = & $mysqlPath @mysqlArguments $databaseName "--execute=$inventoryQuery"
    if ($LASTEXITCODE -ne 0) {
        throw 'Falha ao gerar inventário pré-migração.'
    }

    & $mysqldumpPath `
        "--host=$($connectionUri.Host)" `
        "--port=$port" `
        "--user=$Usuario" `
        '--single-transaction' `
        '--routines' `
        '--triggers' `
        '--events' `
        '--hex-blob' `
        "--result-file=$backupPath" `
        $databaseName
    if ($LASTEXITCODE -ne 0) {
        throw 'mysqldump falhou; nenhuma migration foi executada.'
    }

    $backup = Get-Item -LiteralPath $backupPath
    if ($backup.Length -lt 1024) {
        throw "Backup inválido ou muito pequeno: $backupPath"
    }

    @(
        "Gerado em: $(Get-Date -Format o)"
        "Servidor: $($connectionUri.Host):$port"
        "Banco: $databaseName"
        "Flyway antes: $currentVersion"
        "Backup: $backupPath"
        "Tamanho: $($backup.Length) bytes"
        ''
        'Inventário antes:'
        $inventoryBefore
    ) | Set-Content -LiteralPath $manifestPath -Encoding UTF8

    Write-Host "Backup validado: $backupPath"
    Write-Host "Inventário: $manifestPath"

    if (-not $Aplicar) {
        Write-Host 'Preflight concluído. Execute novamente com -Aplicar para promover V4, V5 e V6.'
        exit 0
    }

    $env:FLYWAY_ENABLED = 'true'
    $env:DB_URL = $JdbcUrl
    $env:DB_USERNAME = $Usuario
    $env:DB_PASSWORD = $Senha

    Push-Location (Join-Path $repoRoot 'api-numerario')
    try {
        & .\mvnw.cmd -q spring-boot:run `
            '-Dspring-boot.run.arguments=--spring.main.web-application-type=none'
        if ($LASTEXITCODE -ne 0) {
            throw "A aplicação não concluiu a migration. Preserve o backup: $backupPath"
        }
    }
    finally {
        Pop-Location
    }

    $finalVersionOutput = & $mysqlPath @mysqlArguments $databaseName "--execute=$versionQuery"
    if ($LASTEXITCODE -ne 0) {
        throw 'Falha ao validar a versão final do Flyway.'
    }
    $finalVersion = ($finalVersionOutput | Select-Object -Last 1).Trim()
    if ($finalVersion -ne '6') {
        throw "Migration incompleta: esperado Flyway 6, encontrado $finalVersion."
    }

    Add-Content -LiteralPath $manifestPath -Encoding UTF8 -Value @(
        ''
        "Flyway depois: $finalVersion"
        "Concluído em: $(Get-Date -Format o)"
    )
    Write-Host 'Migração concluída e validada em Flyway V6.'
}
finally {
    $env:MYSQL_PWD = $previousMysqlPassword
    $env:FLYWAY_ENABLED = $previousFlywayEnabled
    $env:DB_URL = $previousDbUrl
    $env:DB_USERNAME = $previousDbUsername
    $env:DB_PASSWORD = $previousDbPassword
}
