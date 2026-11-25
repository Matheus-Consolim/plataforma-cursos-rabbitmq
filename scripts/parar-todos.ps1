# Interrompe todas as aplicacoes Java do projeto (o RabbitMQ continua rodando).
$s = $PSScriptRoot
& "$s\parar-app.ps1" produtor
& "$s\parar-app.ps1" certificacao
& "$s\parar-app.ps1" comunicacao
& "$s\parar-app.ps1" pedagogico
& "$s\parar-app.ps1" auditoria
& "$s\parar-app.ps1" indicadores -Porta 8085
& "$s\parar-app.ps1" indicadores -Porta 8086
