@echo off
title Sleep Health Big Data Platform - One-Click Launcher

echo ============================================================
echo      Sleep Health Big Data Platform - One-Click Launcher
echo ============================================================
echo.
echo Launching start_project.ps1 in PowerShell...
echo.

:: Launch start_project.ps1 and bypass ExecutionPolicy limits
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start_project.ps1"

echo.
echo ============================================================
echo Startup script finished. Please check logs if there are errors.
echo ============================================================
pause
