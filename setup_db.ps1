# =====================================================================
# Skill Gap Analyzer - PowerShell MySQL Setup Script
# =====================================================================

Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "Skill Gap Analyzer - MySQL Database Setup Script" -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host ""

$password = Read-Host "Enter MySQL root password (press Enter if blank)"

$mysqlPath = "mysql"
if (-not (Get-Command mysql -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe") {
        $mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    } elseif (Test-Path "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe") {
        $mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
    } else {
        Write-Host "[Error] MySQL executable not found in PATH or Program Files." -ForegroundColor Red
        exit 1
    }
}

$passArg = if ($password) { "-p$password" } else { "" }

Write-Host "[Database] Creating schema (tables in skill_gap_db)..." -ForegroundColor Green
Get-Content "database\schema.sql" | & $mysqlPath -u root $passArg

if ($LASTEXITCODE -ne 0) {
    Write-Host "[Database] Failed to execute schema.sql. Please verify your root password." -ForegroundColor Red
    exit 1
}

Write-Host "[Database] Seeding industry benchmarks and roles..." -ForegroundColor Green
Get-Content "database\seed_data.sql" | & $mysqlPath -u root $passArg

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "====================================================================" -ForegroundColor Green
    Write-Host "[Database] Database setup completed successfully!" -ForegroundColor Green
    Write-Host "====================================================================" -ForegroundColor Green
    Write-Host ""

    # Synchronize credentials to Tomcat conf/db.properties if Tomcat exists
    if (Test-Path "C:\apache-tomcat-9\conf") {
        $confPath = "C:\apache-tomcat-9\conf\db.properties"
        $content = @"
# Auto-configured by setup_db.ps1
db.host=localhost
db.port=3306
db.name=skill_gap_db
db.user=root
db.pass=$password
"@
        Set-Content -Path $confPath -Value $content -Encoding UTF8
        Write-Host "[Tomcat] Credentials saved to $confPath!" -ForegroundColor Cyan
        Write-Host "Tomcat will now automatically connect using this password." -ForegroundColor Cyan
    }
} else {
    Write-Host "[Database] Setup failed on seed_data.sql. Please check root password." -ForegroundColor Red
}
