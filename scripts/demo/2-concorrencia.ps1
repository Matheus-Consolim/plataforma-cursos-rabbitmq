# Roteiro 3: duas instancias de Indicadores dividindo o trabalho da mesma fila.
# Pre-requisito: indicadores rodando nas portas 8085 e 8086.
param([int]$Quantidade = 10)
. (Join-Path $PSScriptRoot "_comum.ps1")

Write-Titulo "Instancias de Indicadores conectadas"
foreach ($p in 8085, 8086) {
    try { $i = Invoke-RestMethod "http://localhost:$p/info"; Write-Host "  porta $p -> $($i.instancia) (eventos ja processados: $($i.eventosProcessados))" }
    catch { Write-Host "  porta $p -> OFFLINE" -ForegroundColor Red }
}
Show-Filas

Write-Titulo "Publicando $Quantidade conclusoes distintas"
$antes = @(Get-Consumidor indicadores "/processados").Count
$eventos = Invoke-RestMethod -Method Post "$Produtor/eventos/curso-concluido/lote?quantidade=$Quantidade"
Write-Host "Publicados $(@($eventos).Count) eventos. Aguardando processamento..."
Wait-Processamento ([Math]::Max(4, $Quantidade * 0.3))

$ids = @($eventos | ForEach-Object { $_.idEvento })
$proc = @(Get-Consumidor indicadores "/processados") | Where-Object { $ids -contains $_.id_evento }
Write-Titulo "Qual instancia processou cada evento"
$proc | Sort-Object processado_em | Format-Table id_evento, instancia, processado_em -AutoSize | Out-Host
$proc | Group-Object instancia | Format-Table @{n = "instancia"; e = { $_.Name } }, @{n = "eventos"; e = { $_.Count } } -AutoSize | Out-Host

Write-Host "Cada evento processado exatamente uma vez? " -NoNewline
$unicos = ($proc | Select-Object -ExpandProperty id_evento -Unique).Count
if ($unicos -eq $ids.Count -and @($proc).Count -eq $ids.Count) { Write-Host "SIM ($unicos de $($ids.Count))" -ForegroundColor Green }
else { Write-Host "NAO ($(@($proc).Count) registros para $($ids.Count) eventos)" -ForegroundColor Red }

Write-Titulo "Efeitos nas demais responsabilidades interessadas"
foreach ($n in "certificacao", "comunicacao", "auditoria") {
    $p = @(Get-Consumidor $n "/processados") | Where-Object { $ids -contains $_.id_evento }
    Write-Host ("  {0,-13} processou {1} de {2}" -f $n, @($p).Count, $ids.Count)
}
Write-Host "Total de conclusoes por curso:"; Get-Consumidor indicadores "/indicadores" | Format-Table -AutoSize | Out-Host
