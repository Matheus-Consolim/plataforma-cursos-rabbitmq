# Interrompe UMA aplicacao iniciada por iniciar-app.ps1 (os demais continuam funcionando).
#   .\scripts\parar-app.ps1 comunicacao
#   .\scripts\parar-app.ps1 indicadores -Porta 8086
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet("produtor", "certificacao", "comunicacao", "pedagogico", "auditoria", "indicadores")]
    [string]$App,
    [int]$Porta
)

$raiz = Split-Path $PSScriptRoot -Parent
$portas = @{ produtor = 8080; certificacao = 8081; comunicacao = 8082; pedagogico = 8083; auditoria = 8084; indicadores = 8085 }
if (-not $Porta) { $Porta = $portas[$App] }
$nome = "$App-$Porta"

# Procura o processo que escuta na porta (funciona tambem para apps abertas com -Janela).
$conexao = Get-NetTCPConnection -State Listen -LocalPort $Porta -ErrorAction SilentlyContinue | Select-Object -First 1
$alvo = if ($conexao) { $conexao.OwningProcess } else {
    $pidFile = Join-Path $raiz "logs\$nome.pid"
    if (Test-Path $pidFile) { [int](Get-Content $pidFile) } else { $null }
}
if ($alvo) {
    Stop-Process -Id $alvo -Force -ErrorAction SilentlyContinue
    Write-Host "$nome interrompido (pid $alvo)."
} else {
    Write-Host "$nome nao esta em execucao."
}
