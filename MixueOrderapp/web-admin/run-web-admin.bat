@echo off
setlocal

REM Mixue Web Admin runner (UI-only)
REM - Starts a local HTTP server on http://localhost:5173/
REM - Opens the default browser

set PORT=5173
set URL=http://localhost:%PORT%/web-admin/

REM Move to repository root so the server can also serve /app/google-services.json
cd /d "%~dp0\.."

echo ==============================================
echo Mixue Web Admin (UI-only) - Local Server
echo Folder: %CD%
echo URL   : %URL%
echo ==============================================
echo.
echo Starting server... (close this window to stop)
echo.

REM Start Python static server (supports ES modules).
where python >nul 2>nul
if %errorlevel%==0 (
  echo Using: python -m http.server %PORT%
  start "" "%URL%"
  python -m http.server %PORT%
  goto :eof
)

REM Fallback: try PHP built-in server
where php >nul 2>nul
if %errorlevel%==0 (
  echo Using: php -S localhost:%PORT%
  start "" "%URL%"
  php -S localhost:%PORT%
  goto :eof
)

echo ERROR: Cannot start a local server.
echo - Install Python (recommended) OR PHP.
echo - Then re-run this file.
echo.
echo Quick install notes:
echo - Python: https://www.python.org/downloads/  (enable "Add python.exe to PATH")
echo - PHP   : https://windows.php.net/download/
echo.
pause
exit /b 1

