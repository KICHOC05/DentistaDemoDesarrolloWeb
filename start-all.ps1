param(
    [switch]$NoWait,
    [int]$TimeoutSeconds = 120
)

$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
$LogsDir = Join-Path $ProjectRoot "logs"
$StartTime = Get-Date

function Write-Step  { Write-Host ("[$(Get-Date -Format 'HH:mm:ss')] >> $args") -ForegroundColor Magenta }
function Write-Info  { Write-Host ("[$(Get-Date -Format 'HH:mm:ss')]    $args") -ForegroundColor Cyan }
function Write-OK    { Write-Host ("[$(Get-Date -Format 'HH:mm:ss')]  OK $args") -ForegroundColor Green }
function Write-Warn  { Write-Host ("[$(Get-Date -Format 'HH:mm:ss')] WARN $args") -ForegroundColor Yellow }
function Write-Err   { Write-Host ("[$(Get-Date -Format 'HH:mm:ss')] ERR  $args") -ForegroundColor Red }

Clear-Host

Write-Step "=== CLINICA DENTAL SAAS - INICIANDO MICROSERVICIOS ==="
Write-Info ("Directorio: " + $ProjectRoot)
Write-Info ("Inicio:     " + $StartTime.ToString('yyyy-MM-dd HH:mm:ss'))
Write-Info ""

Write-Step "--- DIAGNOSTICO DEL SISTEMA ---"

try {
    $oldEAP = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $jv = (& java --version 2>&1 | Out-String).Trim()
    if (-not $jv) { $jv = (& java -version 2>&1 | Out-String).Trim() }
    $ErrorActionPreference = $oldEAP
    if ($jv -match '"(.*?)"') { $jv = $Matches[1] }
    Write-Info (" Java: " + $jv)
} catch {
    Write-Err "Java no detectado. Necesitas JDK 21+"
    exit 1
}

if (-not (Test-Path (Join-Path $ProjectRoot "mvnw.cmd"))) {
    Write-Err "mvnw.cmd no encontrado en " + $ProjectRoot
    exit 1
}
Write-Info " Maven: mvnw.cmd encontrado"

$Services = @(
    @{ Name = "api-gateway";         Port = 8080; Dir = "api-gateway";         Desc = "API Gateway - http://localhost:8080" }
    @{ Name = "auth-service";        Port = 8081; Dir = "auth-service";        Desc = "Autenticacion / Usuarios" }
    @{ Name = "appointment-service"; Port = 8082; Dir = "appointment-service"; Desc = "Gestion de Citas" }
    @{ Name = "clinical-service";    Port = 8083; Dir = "clinical-service";    Desc = "Expediente Clinico" }
)

Write-Info ""
Write-Info "Verificando estructura..."
foreach ($svc in $Services) {
    $pom = Join-Path (Join-Path $ProjectRoot $svc.Dir) "pom.xml"
    if (Test-Path $pom) {
        Write-Info ("  [" + $svc.Port + "] " + $svc.Name + " - " + $svc.Desc)
    } else {
        Write-Warn ("  [" + $svc.Port + "] " + $svc.Name + " - FALTA pom.xml en " + $svc.Dir)
    }
}

Write-Info ""
Write-Info "Verificando puertos..."
foreach ($svc in $Services) {
    $inUse = netstat -ano | Select-String (":" + $svc.Port + "\s") 2>$null
    if ($inUse) {
        Write-Warn ("  Puerto " + $svc.Port + " ya esta en uso por otro proceso")
    } else {
        Write-Info ("  Puerto " + $svc.Port + ": disponible")
    }
}

Remove-Item -Path $LogsDir -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $LogsDir -Force | Out-Null
Write-Info ""
Write-Info ("Logs: " + $LogsDir)

Write-Step ""
Write-Step "--- INICIANDO SERVICIOS ---"
Write-Info "Cada servicio se abrira en su propia ventana de consola"
Write-Info ("Revisa los logs en " + $LogsDir + " para detalles")
Write-Info ""

$processes = @()

foreach ($svc in $Services) {
    $logFile = Join-Path $LogsDir ($svc.Name + ".log")
    $pidFile = Join-Path $LogsDir ($svc.Name + ".pid")
    $workDir = Join-Path $ProjectRoot $svc.Dir
    $windowTitle = $svc.Name + " [:" + $svc.Port + "]"

    Write-Info ("Arrancando " + $windowTitle + " ...")

    $mvnCmd = "..\mvnw.cmd"
    $mvnArgs = "spring-boot:run"

    try {
        $proc = Start-Process -FilePath "cmd.exe" `
            -ArgumentList "/c `"$mvnCmd $mvnArgs`" 2>&1" `
            -WorkingDirectory $workDir `
            -NoNewWindow `
            -RedirectStandardOutput $logFile `
            -PassThru
        $procId = $proc.Id
        $procId | Out-File -FilePath $pidFile -Encoding ascii

        $processes += [PSCustomObject]@{
            Name = $svc.Name
            Port = $svc.Port
            Process = $proc
            Pid = $procId
            LogFile = $logFile
            PidFile = $pidFile
        }

        Write-Info ("  PID: " + $procId + " | Log: " + $svc.Name + ".log")
        Start-Sleep -Seconds 2
    } catch {
        Write-Err ("Error al iniciar " + $svc.Name + ": " + $_)
    }
}

Write-Step ""
Write-Step ("--- MONITOREANDO INICIO (timeout: " + $TimeoutSeconds + "s) ---")
Write-Info "Verificando que los servicios arranquen correctamente..."
Write-Info "Paciencia, la primera vez Maven descarga dependencias."
Write-Info ""

$allReady = $false
$elapsed = 0
$checkInterval = 5
$maxChecks = [Math]::Floor($TimeoutSeconds / $checkInterval)

$readyPatterns = @(
    'Started\s+\w+\s+in\s+[\d\.]+\s+seconds',
    'Tomcat (started|initialized) on port',
    'Completed initialization in'
)

for ($i = 0; $i -lt $maxChecks; $i++) {
    Start-Sleep -Seconds $checkInterval
    $elapsed += $checkInterval

    $allReady = $true
    foreach ($p in $processes) {
        if (-not (Test-Path $p.LogFile)) { $allReady = $false; continue }

        $content = Get-Content -Path $p.LogFile -Tail 100 -ErrorAction SilentlyContinue
        $isReady = $false
        foreach ($pattern in $readyPatterns) {
            if ($content -match $pattern) { $isReady = $true; break }
        }

        $alive = try { -not $p.Process.HasExited } catch { $false }
        if (-not $alive) {
            Write-Err ($p.Name + " - proceso terminado inesperadamente. Revisa " + $p.LogFile)
            $allReady = $false
            continue
        }

        if (-not $isReady) {
            $allReady = $false
            $lastLine = $content | Where-Object { $_ -match 'ERROR|WARN|Started|Tomcat|FAIL' } | Select-Object -Last 1
            if (-not $lastLine) { $lastLine = $content | Select-Object -Last 1 }
            Write-Info ("  Esperando " + $p.Name + "... (" + $elapsed + "s)")
        }
    }

    if ($allReady) { break }
}

Write-Step ""
Write-Step "--- VERIFICACION DE SERVICIOS ---"

$readyCount = 0

foreach ($p in $processes) {
    $alive = try { -not $p.Process.HasExited } catch { $false }

    if (-not $alive) {
        $exitCode = try { $p.Process.ExitCode } catch { -1 }
        Write-Err ($p.Name + " [:" + $p.Port + "] - CAIDO (exit code: " + $exitCode + ")")
        Write-Info ("  Revisa el log: Get-Content -Path """ + $p.LogFile + """ -Tail 50")
        continue
    }

    try {
        $response = Invoke-WebRequest -Uri ("http://localhost:" + $p.Port) -TimeoutSec 5 -UseBasicParsing -ErrorAction SilentlyContinue
        Write-OK ($p.Name + " [:" + $p.Port + "] - HTTP " + $response.StatusCode)
        $readyCount++
    } catch {
        Write-OK ($p.Name + " [:" + $p.Port + "] - proceso vivo (endpoint protegido, requiere login)")
        $readyCount++
    }
}

$EndTime = Get-Date
$Duration = $EndTime - $StartTime

Write-Step ""
Write-Step "========================================"
Write-Step "             R  E  S  U  M  E  N"
Write-Step "========================================"
Write-Info ("Tiempo:      " + $Duration.Minutes + "m " + $Duration.Seconds + "s")
Write-Info ("Servicios:   " + $readyCount + " de " + $Services.Length + " activos")
Write-Info ("Logs:        " + $LogsDir)
Write-Info ""

if ($readyCount -eq $Services.Length) {
    Write-OK "  TODOS LOS SERVICIOS ESTAN CORRIENDO"
    Write-Info ""
    Write-Info "  ACCEDE A LA APLICACION:"
    Write-Info "  http://localhost:8080  - API Gateway (entrada principal)"
    Write-Info ""
    Write-Info "  Acceso directo por servicio:"
    Write-Info "    Auth Service:  http://localhost:8081"
    Write-Info "    Citas:         http://localhost:8082"
    Write-Info "    Clinica:       http://localhost:8083"
    Write-Info "    H2 Console:    http://localhost:8082/h2-console"
    Write-Info ""
    Write-Info "  Credenciales por defecto:"
    Write-Info "    admin / admin123"
    Write-Info "    recepcion / recepcion123"
    Write-Info "    doctor1 / doctor123"
    Write-Info ""
    Write-Info "  Comandos utiles:"
    Write-Info "    Ver logs en vivo: Get-Content .\logs\*.log -Tail 20 -Wait"
    Write-Info "    Detener todo:     .\stop-all.ps1"
} else {
    Write-Warn "  ALGUNOS SERVICIOS FALLARON. Revisa los logs:"
    Write-Info ""
    foreach ($p in $processes) {
        $alive = try { -not $p.Process.HasExited } catch { $false }
        $icon = if ($alive) { "ACTIVO" } else { "CAIDO" }
        Write-Warn ("  [" + $icon + "] " + $p.Name + " -> " + $p.LogFile)
    }
    Write-Info ""
    Write-Info ("  Para ver un log: notepad """ + $processes[0].LogFile + """")
}

Write-Step "========================================"
Write-Info ""
Write-Info "Puedes cerrar esta ventana minimizandola."
Write-Info "Los servicios siguen corriendo en sus ventanas individuales."

if (-not $NoWait) {
    Write-Host "`nPresiona cualquier tecla para cerrar el panel de control..." -ForegroundColor Gray
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}
