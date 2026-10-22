# =====================================================================
# Skill Gap Analyzer - PowerShell MySQL Setup Script
# =====================================================================

Write-Host "====================================================================" -ForegroundColor Cyan
Write-Host "Skill Gap Analyzer - MySQL Database Setup Script" -ForegroundColor Cyan
Write-Host "====================================================================" -ForegroundColor Cyan

$password = Read-Host "Enter MySQL root password (press Enter if blank)"

$mysqlPath = "mysql"
if (-not (Get-Command mysql -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe") {
        $mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    } elseif (Test-Path "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe") {
        $mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
    } else {
        Write-Host "[Error] MySQL executable not found. Please run database/schema.sql in MySQL Workbench." -ForegroundColor Red
        exit 1
    }
}

$passArg = if ($password) { "-p$password" } else { "" }

Write-Host "[Database] Creating schema..." -ForegroundColor Green
Get-Content "database\schema.sql" | & $mysqlPath -u root $passArg

Write-Host "[Database] Seeding industry benchmarks and roles..." -ForegroundColor Green
Get-Content "database\seed_data.sql" | & $mysqlPath -u root $passArg

if ($LASTEXITCODE -eq 0) {
    Write-Host "[Database] Database setup completed successfully!" -ForegroundColor Green
} else {
    Write-Host "[Database] Setup failed. Please check root password." -ForegroundColor Red
}
