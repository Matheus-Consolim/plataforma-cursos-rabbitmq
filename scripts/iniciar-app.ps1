# Inicia UMA aplicacao (produtor ou um consumidor) de forma independente.
#   .\scripts\iniciar-app.ps1 certificacao
#   .\scripts\iniciar-app.ps1 indicadores -Porta 8086        (segunda instancia de Indicadores)
#   .\scripts\iniciar-app.ps1 comunicacao -Janela            (abre uma janela mostrando o log ao vivo)
# Sem -Janela, o log vai para logs\<app>-<porta>.log
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet("produtor", "certificacao", "comunicacao", "pedagogico", "auditoria", "indicadores")]
    [string]$App,
    [int]$Porta,
    [switch]$Janela
)

$raiz = Split-Path $PSScriptRoot -Parent
. (Join-Path $PSScriptRoot "ambiente.ps1") | Out-Null

$portas = @{ produtor = 8080; certificacao = 8081; comunicacao = 8082; pedagogico = 8083; auditoria = 8084; indicadores = 8085 }
if (-not $Porta) { $Porta = $portas[$App] }

$modulo = if ($App -eq "produtor") { "produtor" } else { "consumidor-$App" }
$jar = Join-Path $raiz "$modulo\target\$modulo-1.0.0.jar"
if (-not (Test-Path $jar)) {
    Write-Error "Jar nao encontrado: $jar. Rode antes: mvn -DskipTests package"
    exit 1
}

$java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME "bin\java.exe" } else { "java" }
$nome = "$App-$Porta"
$logs = Join-Path $raiz "logs"
New-Item -ItemType Directory -Force $logs | Out-Null
$argumentos = @("-jar", "`"$jar`"", "--server.port=$Porta")

if ($Janela) {
    $cmd = "title $nome && `"$java`" $($argumentos -join ' ')"
    $p = Start-Process cmd.exe -ArgumentList "/k", $cmd -WorkingDirectory $raiz -PassThru
} else {
    $p = Start-Process $java -ArgumentList $argumentos -WorkingDirectory $raiz -PassThru -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $logs "$nome.log") -RedirectStandardError (Join-Path $logs "$nome.err.log")
}
$p.Id | Set-Content (Join-Path $logs "$nome.pid")

Write-Host -NoNewline "Iniciando $nome (pid $($p.Id))"
for ($i = 0; $i -lt 60; $i++) {
    Start-Sleep -Seconds 1
    Write-Host -NoNewline "."
    try {
        $url = if ($App -eq "produtor") { "http://localhost:$Porta/eventos" } else { "http://localhost:$Porta/info" }
        Invoke-RestMethod $url -TimeoutSec 2 | Out-Null
        Write-Host " pronto em http://localhost:$Porta"
        exit 0
    } catch { }
}
Write-Warning " $nome nao respondeu em 60s; veja logs\$nome.log"
