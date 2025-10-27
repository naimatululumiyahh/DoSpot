# Setup helper for DoSpot (PowerShell)
# Usage:
# 1) Open PowerShell with appropriate privileges
# 2) Edit the $jdkPath variable below if needed, or pass your JDK path
# 3) Run: .\scripts\setup.ps1

param(
    [string]$jdkPath = 'C:\\Program Files\\Java\\jdk-17'
)

Write-Host "Using JDK path: $jdkPath"

if (-Not (Test-Path "$jdkPath\bin\keytool.exe")) {
    Write-Warning "keytool.exe not found at $jdkPath\bin. Please install JDK 17 and update the path or run keytool from PATH."
}

# Set JAVA_HOME for this session
$env:JAVA_HOME = $jdkPath
$env:PATH = "$env:JAVA_HOME\\bin;" + $env:PATH
Write-Host "JAVA_HOME set to: $env:JAVA_HOME"

Write-Host "\n== Generate signing report (SHA-1). This uses Gradle signingReport. If you prefer keytool, run the keytool command provided in README. =="
Push-Location "$(Resolve-Path ..)"
try {
    & .\gradlew signingReport -q
} catch {
    Write-Warning "Failed to run signingReport: $_"
}
Pop-Location

Write-Host "\n== Build debug APK =="
Push-Location "$(Resolve-Path ..)"
try {
    & .\gradlew clean assembleDebug --console=plain
} catch {
    Write-Warning "Gradle build failed: $_"
}
Pop-Location
