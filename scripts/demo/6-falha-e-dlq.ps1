# Extra: confirmacao vinculada ao sucesso. Uma conclusao com falha simulada SO na Certificacao:
# 1a entrega -> nack com requeue; reentrega -> nack sem requeue -> plataforma.certificacao.dlq.
# Os outros consumidores processam normalmente.
. (Join-Path $PSScriptRoot "_comum.ps1")

Write-Titulo "Publicando curso.concluido com dados.simularFalhaEm = certificacao"
$e = Send-Evento "curso-concluido" @{ simularFalhaEm = "certificacao" }
Write-Host "idEvento = $($e.idEvento)"
Wait-Processamento 4
Get-QuemProcessou $e.idEvento | Format-Table -AutoSize | Out-Host
Write-Host "plataforma.certificacao.dlq deve ter recebido a mensagem:"
Show-Filas
Write-Host "Veja no log da Certificacao as linhas [NACK] requeue=true e depois requeue=false."
