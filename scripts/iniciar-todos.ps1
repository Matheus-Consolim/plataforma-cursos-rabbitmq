# Inicia o produtor, os cinco consumidores e a segunda instancia de Indicadores.
#   .\scripts\iniciar-todos.ps1            (logs em logs\*.log)
#   .\scripts\iniciar-todos.ps1 -Janela    (uma janela de log por aplicacao - bom para a apresentacao)
param([switch]$Janela)

$s = $PSScriptRoot
& "$s\iniciar-app.ps1" produtor -Janela:$Janela
& "$s\iniciar-app.ps1" certificacao -Janela:$Janela
& "$s\iniciar-app.ps1" comunicacao -Janela:$Janela
& "$s\iniciar-app.ps1" pedagogico -Janela:$Janela
& "$s\iniciar-app.ps1" auditoria -Janela:$Janela
& "$s\iniciar-app.ps1" indicadores -Porta 8085 -Janela:$Janela
& "$s\iniciar-app.ps1" indicadores -Porta 8086 -Janela:$Janela
