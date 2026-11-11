<#
.SYNOPSIS
  Ejecuta las pruebas de carga con k6 (catálogo, recomendaciones y login) y genera la tabla de resultados.

.EXAMPLE
  # 1) Prueba rápida (1 usuario, 30 s) para comprobar que todo funciona
  .\tests\load\run_load_tests.ps1 -BaseUrl https://toco-backend.onrender.com -Email pruebas@correo.com -Password 'Secreta1!' -Profile smoke

  # 2) Prueba de carga real (20 usuarios virtuales, meseta de 5 min por escenario)
  .\tests\load\run_load_tests.ps1 -BaseUrl https://toco-backend.onrender.com -Email pruebas@correo.com -Password 'Secreta1!' -Profile load -Vus 20

.NOTES
  - El usuario de pruebas debe ser un USER normal (no el admin) con al menos una lista con ítems.
  - Contra Render (plan gratuito) la primera petición despierta el contenedor: el script hace antes
    una petición a /actuator/health y espera a que responda, para no medir el arranque en frío.
  - Los resúmenes JSON quedan en tests/load/results y la tabla en tests/load/results/resumen_carga.md.
#>
param(
  [string]$BaseUrl = "http://localhost:8080",
  [Parameter(Mandatory = $true)][string]$Email,
  [Parameter(Mandatory = $true)][string]$Password,
  [ValidateSet("smoke", "load", "stress")][string]$Profile = "load",
  [int]$Vus = 20,
  [int]$SpikeVus = 50,
  [string]$Hold = "5m",
  [string[]]$Scenarios = @("catalog", "recommendations", "login"),
  # Carpeta de resultados (por defecto tests/load/results). Usa otra para no mezclar, p. ej.,
  # las ejecuciones contra el backend local con las de Render: -ResultsDir tests/load/results_local
  [string]$ResultsDir = ""
)

$ErrorActionPreference = "Stop"
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$k6Dir = Join-Path $here "k6"
$results = if ($ResultsDir) { $ResultsDir } else { Join-Path $here "results" }
$results = (New-Item -ItemType Directory -Force $results).FullName  # absoluta: k6 se ejecuta desde k6/

$k6 = (Get-Command k6 -ErrorAction SilentlyContinue).Source
if (-not $k6) { $k6 = "C:\Program Files\k6\k6.exe" }
if (-not (Test-Path $k6)) { throw "No se encuentra k6. Instálalo con: winget install GrafanaLabs.k6" }

# Despertar el backend y medir el arranque en frío (si lo hay)
Write-Host "Comprobando $BaseUrl/actuator/health ..."
$sw = [System.Diagnostics.Stopwatch]::StartNew()
try {
  $health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -TimeoutSec 180
  $sw.Stop()
  Write-Host ("Backend {0} en {1:N1} s (si es >10 s, era un arranque en frío)" -f $health.status, $sw.Elapsed.TotalSeconds)
  "{0};{1};{2:N0}" -f (Get-Date -Format s), $BaseUrl, $sw.Elapsed.TotalMilliseconds | Add-Content (Join-Path $results "health_checks.csv")
} catch {
  throw "El backend no responde en $BaseUrl : $_"
}

foreach ($scenario in $Scenarios) {
  $script = Join-Path $k6Dir "$scenario.js"
  Write-Host "`n=== Escenario $scenario ($Profile) ==="
  Push-Location $k6Dir
  # k6 escribe en stderr si no se cumple un umbral (y sale con código 99): en PowerShell 5.1, con
  # "Stop", eso cortaría el script y no se ejecutarían los demás escenarios
  $previousPreference = $ErrorActionPreference
  $ErrorActionPreference = "Continue"
  try {
    & $k6 run `
      -e BASE_URL=$BaseUrl -e TEST_EMAIL=$Email -e TEST_PASSWORD=$Password `
      -e PROFILE=$Profile -e VUS=$Vus -e SPIKE_VUS=$SpikeVus -e HOLD=$Hold `
      -e RESULTS_DIR=$results `
      $script 2>&1 | ForEach-Object { "$_" }
    if ($LASTEXITCODE -eq 99) { Write-Host "Aviso: no se cumple algún umbral (p95 < 1 s o errores < 1 %); queda en el resumen." }
    elseif ($LASTEXITCODE -ne 0) { Write-Host "Aviso: k6 terminó con código $LASTEXITCODE." }
  } finally {
    $ErrorActionPreference = $previousPreference
    Pop-Location
  }
}

Write-Host "`nGenerando la tabla de resultados..."
python -I (Join-Path $here "summarize_k6.py") $results
