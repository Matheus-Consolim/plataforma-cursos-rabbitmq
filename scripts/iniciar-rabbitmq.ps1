# Inicia o RabbitMQ instalado localmente (Windows, sem Docker), com o painel de gerenciamento
# e a topologia de rabbitmq/definitions.json carregada.
#   Uso:  .\scripts\iniciar-rabbitmq.ps1
# Painel: http://localhost:15672  (guest / guest)

$raiz = Split-Path $PSScriptRoot -Parent
. (Join-Path $PSScriptRoot "ambiente.ps1") | Out-Null

if (-not (Get-Command rabbitmq-server.bat -ErrorAction SilentlyContinue)) {
    Write-Error "RabbitMQ nao encontrado. Instale-o (ou use 'docker compose up -d')."
    exit 1
}
if (-not $env:RABBITMQ_BASE) { $env:RABBITMQ_BASE = Join-Path $env:USERPROFILE ".plataforma-rabbitmq" }
if (-not $env:RABBITMQ_CONFIG_FILE) { $env:RABBITMQ_CONFIG_FILE = Join-Path $env:RABBITMQ_BASE "rabbitmq.conf" }
New-Item -ItemType Directory -Force $env:RABBITMQ_BASE | Out-Null

$defs = (Join-Path $raiz "rabbitmq\definitions.json") -replace "\\", "/"
@"
definitions.import_backend = local_filesystem
definitions.local.path = $defs
collect_statistics_interval = 1000
"@ | Set-Content $env:RABBITMQ_CONFIG_FILE -Encoding ascii

rabbitmq-plugins.bat enable --offline rabbitmq_management | Out-Null

# O epmd (port mapper do Erlang) precisa estar ativo para o no do RabbitMQ subir.
$epmd = Get-Command epmd.exe -ErrorAction SilentlyContinue
if (-not $epmd -and $env:ERLANG_HOME) {
    $epmd = Get-ChildItem $env:ERLANG_HOME -Recurse -Filter epmd.exe -ErrorAction SilentlyContinue | Select-Object -First 1
}
if ($epmd) { & ($epmd.Source, $epmd.FullName | Where-Object { $_ } | Select-Object -First 1) -daemon }

Write-Host "Iniciando RabbitMQ (janela separada)..."
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "title RabbitMQ && rabbitmq-server.bat"

for ($i = 0; $i -lt 60; $i++) {
    Start-Sleep -Seconds 2
    try {
        Invoke-RestMethod "http://localhost:15672/api/overview" -Credential (New-Object PSCredential("guest", (ConvertTo-SecureString "guest" -AsPlainText -Force))) | Out-Null
        Write-Host "RabbitMQ pronto. Painel: http://localhost:15672 (guest/guest)"
        exit 0
    } catch { }
}
Write-Warning "RabbitMQ ainda nao respondeu; verifique a janela 'RabbitMQ'."
