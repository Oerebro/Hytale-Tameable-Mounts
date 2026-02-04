@echo off
setlocal EnableExtensions EnableDelayedExpansion

echo === Chocobo Variant Generator Launcher ===
echo.

:: Ask for the JSON file name
set /p INPUT=Enter JSON file name (example: Chocobo.json): 

if "%INPUT%"=="" (
    echo ERROR: No file name entered.
    pause
    exit /b 1
)

if not exist "%INPUT%" (
    echo ERROR: File "%INPUT%" not found in this directory.
    pause
    exit /b 1
)

:: Define base names
set "BASEDIR=%CD%"
for %%F in ("%INPUT%") do set "BASENAME=%%~nF"
set "OUTPUTDIR=%BASEDIR%\%BASENAME%"

echo Input file:  %INPUT%
echo Output folder:  %OUTPUTDIR%
echo.

:: Run the PowerShell script
powershell -NoProfile -ExecutionPolicy Bypass -File ".\GenColorVariants.ps1" -InputFile "%INPUT%" -OutputDir "%OUTPUTDIR%"

:: Keep the window open for user to see success/errors
echo.
echo Finished. Output folder: %OUTPUTDIR%
pause
