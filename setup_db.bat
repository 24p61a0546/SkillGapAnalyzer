@echo off
setlocal enabledelayedexpansion

echo ====================================================================
echo Skill Gap Analyzer - MySQL Database Setup Script
echo ====================================================================
echo.

set /p MYSQL_PASS="Enter MySQL 'root' password (press Enter if blank): "

set MYSQL_BIN=mysql
where mysql >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
        set MYSQL_BIN="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    ) else if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" (
        set MYSQL_BIN="C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
    ) else (
        echo [Error] MySQL CLI binary not found in PATH or Program Files.
        echo Please import database\schema.sql and database\seed_data.sql manually in MySQL Workbench.
        pause
        exit /b 1
    )
)

echo.
echo [Database] Initializing schema (creating tables in 'skill_gap_db')...
if "%MYSQL_PASS%"=="" (
    %MYSQL_BIN% -u root < database\schema.sql
    if !ERRORLEVEL! neq 0 goto failure
    echo [Database] Populating industry benchmark seed data...
    %MYSQL_BIN% -u root < database\seed_data.sql
    if !ERRORLEVEL! neq 0 goto failure
) else (
    %MYSQL_BIN% -u root -p"%MYSQL_PASS%" < database\schema.sql
    if !ERRORLEVEL! neq 0 goto failure
    echo [Database] Populating industry benchmark seed data...
    %MYSQL_BIN% -u root -p"%MYSQL_PASS%" < database\seed_data.sql
    if !ERRORLEVEL! neq 0 goto failure
)

echo.
echo ====================================================================
echo [Database] SUCCESS! 'skill_gap_db' is now initialized with seed data!
echo ====================================================================
echo.

:: Automatically offer to configure Tomcat conf\db.properties if Tomcat exists
if exist "C:\apache-tomcat-9\conf" (
    echo [Tomcat] Detected Tomcat at C:\apache-tomcat-9
    echo Writing configured credentials to C:\apache-tomcat-9\conf\db.properties...
    (
        echo # Configured by setup_db.bat
        echo db.host=localhost
        echo db.port=3306
        echo db.name=skill_gap_db
        echo db.user=root
        echo db.pass=%MYSQL_PASS%
    ) > "C:\apache-tomcat-9\conf\db.properties"
    echo [Tomcat] Credentials saved to C:\apache-tomcat-9\conf\db.properties!
    echo Tomcat will now automatically connect using this password.
)

goto finish

:failure
echo.
echo ====================================================================
echo [Database] Error: Failed to connect or execute SQL scripts.
echo Please verify your MySQL password and ensure the MySQL service is running.
echo ====================================================================

:finish
echo.
pause
