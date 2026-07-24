$ProjectRoot = $PSScriptRoot
$LogsDir = Join-Path $ProjectRoot "logs"

Write-Host "Deteniendo servicios..." -ForegroundColor Cyan

$stopped = 0
$total = 0

$pidFiles = Get-ChildItem "$LogsDir\*.pid" -ErrorAction SilentlyContinue
if ($pidFiles) {
    $pidFiles | ForEach-Object {
        $total++
        $svcName = $_.BaseName
        $pid = (Get-Content $_.FullName -Raw).Trim()
        if ($pid -match '^\d+$') {
            try {
                $proc = Get-Process -Id $pid -ErrorAction SilentlyContinue
                if ($proc) {
                    Write-Host "  Deteniendo $svcName (PID: $pid)..." -ForegroundColor Yellow
                    $proc.Kill()
                    Write-Host "  OK $svcName detenido" -ForegroundColor Green
                    $stopped++
                } else {
                    Write-Host "  $svcName ya no esta corriendo" -ForegroundColor Gray
                    $stopped++
                }
            } catch {
                Write-Host "  ERROR al detener $svcName (PID: $pid): $_" -ForegroundColor Red
            }
        }
        Remove-Item $_.FullName -Force -ErrorAction SilentlyContinue
    }
}

if ($total -eq 0) {
    Write-Host "  Buscando procesos java/mvn de forma manual..." -ForegroundColor Cyan
    $procNames = @("java", "mvnw")
    foreach ($name in $procNames) {
        $procs = Get-Process -Name $name -ErrorAction SilentlyContinue
        foreach ($p in $procs) {
            try {
                Write-Host "  Deteniendo $name (PID: $($p.Id))..." -ForegroundColor Yellow
                $p.Kill()
                $stopped++
            } catch {
                Write-Host "  Error al detener PID $($p.Id): $_" -ForegroundColor Red
            }
        }
    }
}

Write-Host "`nServicios detenidos: $stopped" -ForegroundColor Cyan
Start-Sleep -Seconds 1
