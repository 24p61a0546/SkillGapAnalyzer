@echo off
setlocal enabledelayedexpansion

echo ====================================================================
echo Skill Gap Analyzer - Build & Package Utility
echo ====================================================================

:: 1. Check if Apache Ant is installed in PATH
where ant >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [Build] Apache Ant detected in PATH. Running 'ant war'...
    call ant war
    goto finish
)

echo [Build] Apache Ant not found in PATH.
echo [Build] Falling back to standard JDK compiler ^(javac + jar^)...

:: 2. Check javac and jar
where javac >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [Error] 'javac' is not recognized. Please install JDK 8+ and add it to your PATH.
    pause
    exit /b 1
)

:: 3. Clean and prepare build folders
echo [Build] Cleaning old build artifacts...
if exist "build" rmdir /s /q "build"
if exist "dist" rmdir /s /q "dist"

mkdir "build\classes"
mkdir "dist"

:: 4. Compile Java sources
echo [Build] Compiling Java Servlets, DAOs, and Services...
set CP=lib\javax.servlet-api-4.0.1.jar;lib\mysql-connector-j-8.3.0.jar;lib\gson-2.10.1.jar

dir /s /b src\*.java > sources.txt
javac -cp "%CP%" -d "build\classes" -encoding UTF-8 @sources.txt
if %ERRORLEVEL% neq 0 (
    echo [Error] Compilation failed!
    del sources.txt
    pause
    exit /b 1
)
del sources.txt

:: Copy properties
copy "src\db.properties" "build\classes\" >nul

:: 5. Assemble and package WAR
echo [Build] Assembling WAR package...
mkdir "build\war_temp"
xcopy /E /I /Q "WebContent" "build\war_temp" >nul
xcopy /E /I /Q "build\classes" "build\war_temp\WEB-INF\classes" >nul

cd "build\war_temp"
jar -cvf "..\..\dist\SkillGapAnalyzer.war" * >nul
cd "..\.."
rmdir /s /q "build\war_temp"

echo [Build] SUCCESS! WAR generated at: dist\SkillGapAnalyzer.war

:finish
echo ====================================================================
echo Build finished. You can now copy dist\SkillGapAnalyzer.war to:
echo   ^<Tomcat_Home^>\webapps\
echo And visit: http://localhost:8080/SkillGapAnalyzer/
echo ====================================================================
pause
