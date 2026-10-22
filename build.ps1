# =====================================================================
# Skill Gap Analyzer - PowerShell Build & Package Utility
# =====================================================================

Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "Skill Gap Analyzer - Build & Package Utility" -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan

# Check if ant is available
$antCmd = Get-Command ant -ErrorAction SilentlyContinue
if ($antCmd) {
    Write-Host "[Build] Apache Ant detected in PATH. Executing 'ant war'..." -ForegroundColor Green
    & ant war
    exit $LASTEXITCODE
}

Write-Host "[Build] Apache Ant not detected in PATH. Using JDK compiler & archiver..." -ForegroundColor Yellow

# Clean directories
if (Test-Path "build") { Remove-Item -Recurse -Force "build" }
if (Test-Path "dist") { Remove-Item -Recurse -Force "dist" }

New-Item -ItemType Directory -Force -Path "build\classes", "dist", "build\war_temp" | Out-Null

$classpath = "lib\javax.servlet-api-4.0.1.jar;lib\mysql-connector-j-8.3.0.jar;lib\gson-2.10.1.jar"
$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

Write-Host "[Build] Compiling $($javaFiles.Count) Java source files..." -ForegroundColor Green
& javac -cp $classpath -d "build\classes" -encoding UTF-8 $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[Error] Java compilation failed!" -ForegroundColor Red
    exit 1
}

Copy-Item "src\db.properties" -Destination "build\classes\" -Force

Write-Host "[Build] Assembling WAR package..." -ForegroundColor Green
Copy-Item -Path "WebContent\*" -Destination "build\war_temp" -Recurse -Force
Copy-Item -Path "build\classes" -Destination "build\war_temp\WEB-INF\" -Recurse -Force

Set-Location "build\war_temp"
& jar -cvf "..\..\dist\SkillGapAnalyzer.war" * | Out-Null
Set-Location "..\.."

Remove-Item -Recurse -Force "build\war_temp"

if (Test-Path "dist\SkillGapAnalyzer.war") {
    $warSize = (Get-Item "dist\SkillGapAnalyzer.war").Length / 1MB
    Write-Host "[Build] SUCCESS! Generated dist\SkillGapAnalyzer.war ($([Math]::Round($warSize, 2)) MB)" -ForegroundColor Green
    Write-Host ""
    Write-Host "To deploy locally to Apache Tomcat:" -ForegroundColor Cyan
    Write-Host "1. Copy dist\SkillGapAnalyzer.war to <Tomcat_Dir>\webapps\"
    Write-Host "2. Start Tomcat (<Tomcat_Dir>\bin\startup.bat)"
    Write-Host "3. Open browser: http://localhost:8080/SkillGapAnalyzer/"
} else {
    Write-Host "[Error] Failed to generate WAR file." -ForegroundColor Red
}
