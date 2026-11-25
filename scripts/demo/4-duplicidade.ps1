# Roteiro 5: republica um evento ja processado com o MESMO idEvento e comprova o efeito unico.
# Uso: .\4-duplicidade.ps1               (publica uma conclusao nova e depois a republica)
#      .\4-duplicidade.ps1 -IdEvento <id> (republica um evento ja publicado pelo produtor)
param([string]$IdEvento)
. (Join-Path $PSScriptRoot "_comum.ps1")

if (-not $IdEvento) {
    Write-Titulo "1) Publicando uma conclusao de curso"
    $evento = Send-Evento "curso-concluido"
    $IdEvento = $evento.idEvento
    $evento | ConvertTo-Json -Depth 5 | Out-Host
    Wait-Processamento
}

function Show-Estado {
    $c = @(Get-Consumidor certificacao "/certificados?idEvento=$IdEvento")
    Write-Host "  Certificados para este evento: $($c.Count)" -ForegroundColor Yellow
    $c | Format-Table codigo, aluno_nome, curso_nome, carga_horaria, emitido_em -AutoSize | Out-Host
    foreach ($n in $Consumidores.Keys) {
        $p = @(Get-Consumidor $n "/processados") | Where-Object { $_.id_evento -eq $IdEvento }
        if ($p) { Write-Host ("  {0,-13} processado_em={1}  duplicatas_ignoradas={2}" -f $n, $p.processado_em, $p.duplicatas_ignoradas) }
    }
    return $c.Count
}

Write-Titulo "Estado antes da republicacao"
Show-Estado | Out-Null

Write-Titulo "2) Republicando o MESMO evento (idEvento=$IdEvento)"
Invoke-RestMethod -Method Post "$Produtor/eventos/$IdEvento/republicar" | ConvertTo-Json -Depth 5 | Out-Host
Wait-Processamento

Write-Titulo "Estado depois da republicacao"
$qtd = Show-Estado
if ($qtd -eq 1) { Write-Host "RESULTADO: um unico certificado - efeito ocorreu apenas uma vez." -ForegroundColor Green }
else { Write-Host "RESULTADO: $qtd certificados!" -ForegroundColor Red; exit 1 }
