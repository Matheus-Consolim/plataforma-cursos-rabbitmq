# Roteiro 2 da apresentacao: publica os quatro eventos, um por vez, comparando a previsao
# (matriz evento x consumidor) com quem realmente processou.
. (Join-Path $PSScriptRoot "_comum.ps1")

$casos = @(
    @{ rota = "curso-concluido";    tipo = "curso.concluido";    esperado = @("certificacao", "comunicacao", "auditoria", "indicadores") },
    @{ rota = "curso-abandonado";   tipo = "curso.abandonado";   esperado = @("comunicacao", "auditoria") },
    @{ rota = "avaliacao-enviada";  tipo = "avaliacao.enviada";  esperado = @("pedagogico", "auditoria") },
    @{ rota = "avaliacao-revisada"; tipo = "avaliacao.revisada"; esperado = @("comunicacao", "pedagogico", "auditoria") }
)

$falhas = 0
foreach ($c in $casos) {
    Write-Titulo "Evento $($c.tipo)"
    Write-Host "Previsao - DEVE receber : $($c.esperado -join ', ')"
    $naoRecebe = $Consumidores.Keys | Where-Object { $c.esperado -notcontains $_ }
    Write-Host "Previsao - NAO recebe   : $($naoRecebe -join ', ')"
    if ($Host.UI.RawUI -and -not $env:DEMO_SEM_PAUSA) { Read-Host "ENTER para publicar" | Out-Null }

    $evento = Send-Evento $c.rota
    Write-Host "Publicado:" -ForegroundColor Yellow
    $evento | ConvertTo-Json -Depth 5 | Out-Host
    Wait-Processamento

    $real = Get-QuemProcessou $evento.idEvento
    foreach ($nome in $real.Keys) {
        $deve = $c.esperado -contains $nome
        $recebeu = $real[$nome] -like "RECEBEU*"
        $ok = $deve -eq $recebeu
        if (-not $ok) { $falhas++ }
        $cor = if ($ok) { "Green" } else { "Red" }
        Write-Host ("  {0,-13} previsto={1,-9} real={2,-30} {3}" -f $nome, ($(if ($deve) { "recebe" } else { "nao" })),
            $real[$nome], $(if ($ok) { "OK" } else { "DIVERGENTE" })) -ForegroundColor $cor
    }
}

Write-Titulo "Efeitos"
Write-Host "Certificados:"; Get-Consumidor certificacao "/certificados" | Select-Object -Last 3 | Format-Table codigo, aluno_nome, curso_nome, carga_horaria -AutoSize | Out-Host
Write-Host "Avisos:"; Get-Consumidor comunicacao "/avisos" | Select-Object -Last 3 | Format-Table tipo_aviso, destinatario, mensagem -AutoSize -Wrap | Out-Host
Write-Host "Acompanhamento pedagogico:"; Get-Consumidor pedagogico "/avaliacoes" | Select-Object -Last 3 | Format-Table etapa, avaliacao_id, aluno_nome, nota -AutoSize | Out-Host
Write-Host "Indicadores:"; Get-Consumidor indicadores "/indicadores" | Format-Table -AutoSize | Out-Host
Write-Host "Auditoria (ultimos 4):"; Get-Consumidor auditoria "/auditoria" | Select-Object -Last 4 | Format-Table id_evento, tipo, ocorrido_em -AutoSize | Out-Host

if ($falhas -eq 0) { Write-Host "RESULTADO: roteamento conforme a matriz." -ForegroundColor Green }
else { Write-Host "RESULTADO: $falhas divergencia(s)." -ForegroundColor Red; exit 1 }
