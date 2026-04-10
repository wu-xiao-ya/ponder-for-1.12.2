$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$candidates = @(
    "C:\Program Files\Java\jdk-25.0.2",
    "C:\Program Files\Java\jdk-25",
    "C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot"
)

$javaHome = $candidates | Where-Object { Test-Path (Join-Path $_ "bin\java.exe") } | Select-Object -First 1

if (-not $javaHome) {
    throw "No supported Java 25 installation was found. Update scripts/gradle.ps1 or install JDK 25."
}

$env:JAVA_HOME = $javaHome

Push-Location $repoRoot
try {
    & .\gradlew.bat @args
} finally {
    Pop-Location
}
