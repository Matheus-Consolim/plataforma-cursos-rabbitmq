# Ajusta JAVA_HOME / PATH na sessao atual do PowerShell.
# Se Java, Maven e RabbitMQ ja estiverem instalados no PATH, este script nao e necessario.
# Ele procura as ferramentas portateis em $env:USERPROFILE\tools (ou em $env:PLATAFORMA_TOOLS).
#   Uso:  . .\scripts\ambiente.ps1      (repare no ponto inicial: "dot-source")

$tools = if ($env:PLATAFORMA_TOOLS) { $env:PLATAFORMA_TOOLS } else { Join-Path $env:USERPROFILE "tools" }

function Find-Dir($pattern) {
    $d = Get-ChildItem -Path $pattern -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($d) { $d.FullName } else { $null }
}

$jdk = Find-Dir (Join-Path $tools "jdk\jdk-*")
$mvn = Find-Dir (Join-Path $tools "maven\apache-maven-*")
$git = Join-Path $tools "git\cmd"
$erl = Join-Path $tools "erlang"
$rmq = Find-Dir (Join-Path $tools "rabbitmq\rabbitmq_server-*")

if ($jdk) { $env:JAVA_HOME = $jdk; $env:Path = "$jdk\bin;$env:Path" }
if ($mvn) { $env:Path = "$mvn\bin;$env:Path" }
if (Test-Path $git) { $env:Path = "$git;$env:Path" }
if (Test-Path "$erl\bin\erl.exe") { $env:ERLANG_HOME = $erl; $env:Path = "$erl\bin;$env:Path" }
if ($rmq) {
    $env:RABBITMQ_HOME = $rmq
    $env:Path = "$rmq\sbin;$env:Path"
    $base = Join-Path $env:USERPROFILE ".plataforma-rabbitmq"
    $env:RABBITMQ_BASE = $base
    $env:RABBITMQ_CONFIG_FILE = Join-Path $base "rabbitmq.conf"
}
