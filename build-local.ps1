# Lokaler Build der Halal-Scanner APK
# Aufruf in normalem PowerShell:
#   cd C:\temp\projects\halal-scanner
#   .\build-local.ps1
#
# Voraussetzung: Android Studio (JBR-JDK17 + Android SDK) installiert.

$ErrorActionPreference = "Stop"

# JDK 17 aus Android Studio
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\hozan.shaker\AppData\Local\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Java:" -ForegroundColor Cyan
& "$env:JAVA_HOME\bin\java.exe" -version
Write-Host "Android SDK: $env:ANDROID_HOME" -ForegroundColor Cyan

Set-Location "$PSScriptRoot\android"

# local.properties anlegen falls fehlt
$localProps = "$PSScriptRoot\android\local.properties"
if (-not (Test-Path $localProps)) {
    "sdk.dir=$($env:ANDROID_HOME -replace '\\','\\')" | Out-File -FilePath $localProps -Encoding ASCII
    Write-Host "local.properties erstellt" -ForegroundColor Green
}

Write-Host ""
Write-Host "==> Building Release APK..." -ForegroundColor Cyan
& .\gradlew.bat :app:assembleRelease

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

$apk = "$PSScriptRoot\android\app\build\outputs\apk\release\app-release.apk"
if (Test-Path $apk) {
    $dest = "$PSScriptRoot\..\..\halal-scanner.apk"
    Copy-Item $apk $dest -Force
    $sizeMB = [math]::Round((Get-Item $dest).Length / 1MB, 2)
    Write-Host ""
    Write-Host "==> Fertig!" -ForegroundColor Green
    Write-Host "    APK: $dest"
    Write-Host "    Größe: $sizeMB MB"
    Write-Host ""
    Write-Host "Installation:"
    Write-Host "    adb install -r `"$dest`""
}
