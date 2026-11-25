# Roteiro 4: para o consumidor de Comunicacao, publica um evento que ele e outros devem receber,
# mostra a pendencia na fila e a retomada depois de religa-lo.
. (Join-Path $PSScriptRoot "_comum.ps1")
$scripts = Split-Path $PSScriptRoot -Parent
$pausar = -not $env:DEMO_SEM_PAUSA

Write-Titulo "1) Parando o consumidor de Comunicacao"
& "$scripts\parar-app.ps1" comunicacao
Start-Sleep -Seconds 6   # as estatisticas do RabbitMQ chegam ao painel/API com alguns segundos de atraso
Show-Filas

Write-Titulo "2) Publicando curso.abandonado (deve ir para comunicacao e auditoria)"
$evento = Send-Evento "curso-abandonado"
Write-Host "idEvento = $($evento.idEvento)"
Wait-Processamento
Get-QuemProcessou $evento.idEvento | Format-Table -AutoSize | Out-Host
Wait-Processamento 4
Write-Host "Fila plataforma.comunicacao deve ter 1 mensagem pronta (pendente):"
Show-Filas
if ($pausar) { Read-Host "ENTER para religar Comunicacao" | Out-Null }

Write-Titulo "3) Religando Comunicacao"
& "$scripts\iniciar-app.ps1" comunicacao
Wait-Processamento 7
Get-QuemProcessou $evento.idEvento | Format-Table -AutoSize | Out-Host
Show-Filas
$aviso = @(Get-Consumidor comunicacao "/avisos") | Where-Object { $_.id_evento -eq $evento.idEvento }
if ($aviso) {
    Write-Host "Aviso registrado apos a retomada: $($aviso.mensagem)" -ForegroundColor Green
} else {
    Write-Host "Aviso ainda nao registrado" -ForegroundColor Red; exit 1
}
