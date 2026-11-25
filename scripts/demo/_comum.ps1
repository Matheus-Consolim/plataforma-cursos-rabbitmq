# Funcoes auxiliares usadas pelos roteiros de demonstracao.
$ErrorActionPreference = "Stop"
$Produtor = "http://localhost:8080"
$Consumidores = [ordered]@{
    certificacao = "http://localhost:8081"
    comunicacao  = "http://localhost:8082"
    pedagogico   = "http://localhost:8083"
    auditoria    = "http://localhost:8084"
    indicadores  = "http://localhost:8085"
}
$CredRabbit = New-Object PSCredential("guest", (ConvertTo-SecureString "guest" -AsPlainText -Force))

function Write-Titulo($texto) {
    Write-Host ""
    Write-Host ("=" * 78) -ForegroundColor DarkCyan
    Write-Host " $texto" -ForegroundColor Cyan
    Write-Host ("=" * 78) -ForegroundColor DarkCyan
}

function Send-Evento([string]$rota, $corpo = $null) {
    $json = if ($corpo) { $corpo | ConvertTo-Json -Depth 10 } else { "{}" }
    Invoke-RestMethod -Method Post "$Produtor/eventos/$rota" -ContentType "application/json; charset=utf-8" `
        -Body ([Text.Encoding]::UTF8.GetBytes($json))
}

function Get-Consumidor([string]$nome, [string]$caminho) {
    # No Windows PowerShell 5.1 um array JSON chega como um unico objeto; o pipe o "desenrola".
    try { $r = Invoke-RestMethod "$($Consumidores[$nome])$caminho" -TimeoutSec 3; $r | ForEach-Object { $_ } } catch { $null }
}

# Retorna, para cada consumidor, se ele processou o idEvento (ou "OFFLINE").
function Get-QuemProcessou([string]$idEvento) {
    $r = [ordered]@{}
    foreach ($nome in $Consumidores.Keys) {
        $lista = Get-Consumidor $nome "/processados"
        if ($null -eq $lista) { $r[$nome] = "OFFLINE"; continue }
        $hit = @($lista) | Where-Object { $_.id_evento -eq $idEvento }
        $r[$nome] = if ($hit) { "RECEBEU ($($hit.instancia))" } else { "-" }
    }
    $r
}

function Show-Filas {
    $filas = Invoke-RestMethod "http://localhost:15672/api/queues/%2F" -Credential $CredRabbit
    $filas | Where-Object { $_.name -like "plataforma.*" } | Sort-Object name |
        Select-Object @{n = "fila"; e = { $_.name } }, @{n = "prontas"; e = { $_.messages_ready } },
        @{n = "sem_ack"; e = { $_.messages_unacknowledged } }, @{n = "consumidores"; e = { $_.consumers } } |
        Format-Table -AutoSize | Out-Host
}

function Wait-Processamento([int]$segundos = 3) { Start-Sleep -Seconds $segundos }
