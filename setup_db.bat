@echo off
setlocal

echo ====================================================================
echo Skill Gap Analyzer - MySQL Database Setup Script
echo ====================================================================

set /p MYSQL_PASS="Enter MySQL 'root' password (press Enter if no password): "

set MYSQL_BIN=mysql
where mysql >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
        set MYSQL_BIN="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    ) else if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" (
        set MYSQL_BIN="C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
    ) else (
        echo [Error] MySQL CLI binary not found in PATH or standard Program Files location.
        echo Please import database\schema.sql and database\seed_data.sql manually in MySQL Workbench.
        pause
        exit /b 1
    )
)

echo [Database] Creating database schema (tables)...
if "%MYSQL_PASS%"=="" (
    %MYSQL_BIN% -u root < database\schema.sql
    echo [Database] Populating benchmark seed data...
    %MYSQL_BIN% -u root < database\seed_data.sql
) else (
    %MYSQL_BIN% -u root -p%MYSQL_PASS% < database\schema.sql
    echo [Database] Populating benchmark seed data...
    %MYSQL_BIN% -u root -p%MYSQL_PASS% < database\seed_data.sql
)

if %ERRORLEVEL% equ 0 (
    echo ====================================================================
    echo [Database] SUCCESS! 'skill_gap_db' has been created and populated!
    echo ====================================================================
) else (
    echo [Error] Database setup failed. Please check MySQL password or credentials.
)

pause
