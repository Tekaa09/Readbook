@echo off
cd /d "%~dp0"
where javaw >nul 2>nul
if errorlevel 1 (
  echo Can cai Java 8 tro len: https://www.java.com/download/
  pause
  exit /b 1
)
start "" javaw -jar RedbookUser.jar
