# Perguntas da defesa: (a) evento novo de uma familia ja acompanhada; (b) evento sem regra especifica.
. (Join-Path $PSScriptRoot "_comum.ps1")

Write-Titulo "a) avaliacao.contestada - fato NOVO da familia 'avaliacao'"
Write-Host "Previsao: pedagogico (avaliacao.#) e auditoria (#) recebem, sem mudar codigo nem broker."
$e1 = Send-Evento "personalizado" @{ tipo = "avaliacao.contestada"; dados = @{ alunoId = "A-1001"; alunoNome = "Ana Souza"; avaliacaoId = "AV-2024"; motivo = "Questao 3 com gabarito errado" } }
Wait-Processamento
Get-QuemProcessou $e1.idEvento | Format-Table -AutoSize | Out-Host
@(Get-Consumidor pedagogico "/avaliacoes?avaliacaoId=AV-2024") | Format-Table etapa, tipo_evento, avaliacao_id, aluno_nome, nota -AutoSize | Out-Host

Write-Titulo "b) matricula.realizada - nao atende regra especifica de nenhum consumidor"
Write-Host "Previsao: somente auditoria (#) recebe; nenhuma fila especifica e afetada."
$e2 = Send-Evento "personalizado" @{ tipo = "matricula.realizada"; dados = @{ alunoId = "A-1002"; cursoId = "C-RMQ" } }
Wait-Processamento
Get-QuemProcessou $e2.idEvento | Format-Table -AutoSize | Out-Host
