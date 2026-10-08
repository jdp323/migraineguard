@echo off
setlocal
for %%I in ("%~dp0..") do set "PROJECT_ROOT=%%~fI"
set "PROJECT_GIT="
for /f "delims=" %%G in ('where git 2^>nul') do if not defined PROJECT_GIT set "PROJECT_GIT=%%G"
if not defined PROJECT_GIT set "PROJECT_GIT=%USERPROFILE%\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\git\cmd\git.exe"
if not exist "%PROJECT_GIT%" (
    echo Git was not found. Open this project in Codex or install Git for Windows.
    exit /b 1
)
if "%~1"=="" goto Status
if /i "%~1"=="Status" goto Status
if /i "%~1"=="CheckAccess" goto CheckAccess
if /i "%~1"=="Login" goto Login
if /i "%~1"=="Fetch" goto Fetch
if /i "%~1"=="Snapshot" goto Snapshot
if /i "%~1"=="Backup" goto Backup
echo Usage: git-manage.cmd [Status^|CheckAccess^|Login^|Fetch^|Snapshot^|Backup]
exit /b 1
:Status
"%PROJECT_GIT%" -c "safe.directory=%PROJECT_ROOT%" -C "%PROJECT_ROOT%" status --short --branch
exit /b %errorlevel%
:CheckAccess
"%PROJECT_GIT%" -c "safe.directory=%PROJECT_ROOT%" -C "%PROJECT_ROOT%" -c credential.interactive=false ls-remote --symref origin HEAD
if errorlevel 1 exit /b 1
echo Repository read access succeeded. Push permission is not established.
exit /b 0
:Login
"%PROJECT_GIT%" -c "safe.directory=%PROJECT_ROOT%" -C "%PROJECT_ROOT%" credential-manager github login --device --username jdp323
exit /b %errorlevel%
:Fetch
"%PROJECT_GIT%" -c "safe.directory=%PROJECT_ROOT%" -C "%PROJECT_ROOT%" fetch origin
exit /b %errorlevel%
:Snapshot
"%PROJECT_GIT%" -c "safe.directory=%PROJECT_ROOT%" -C "%PROJECT_ROOT%" add --all
if errorlevel 1 exit /b 1
"%PROJECT_GIT%" -c "safe.directory=%PROJECT_ROOT%" -C "%PROJECT_ROOT%" diff --cached --stat
if errorlevel 1 exit /b 1
"%PROJECT_GIT%" -c "safe.directory=%PROJECT_ROOT%" -C "%PROJECT_ROOT%" diff --cached --quiet
if errorlevel 2 exit /b 1
if not errorlevel 1 (
    echo No staged changes to save.
    exit /b 0
)
"%PROJECT_GIT%" -c "safe.directory=%PROJECT_ROOT%" -C "%PROJECT_ROOT%" commit -m "Save local development progress"
exit /b %errorlevel%
:Backup
if not exist "%PROJECT_ROOT%\dist" mkdir "%PROJECT_ROOT%\dist"
if errorlevel 1 exit /b 1
set "BUNDLE_PATH=%PROJECT_ROOT%\dist\MigraineGuard-git-%RANDOM%-%RANDOM%.bundle"
if exist "%BUNDLE_PATH%" (
    echo Backup already exists: %BUNDLE_PATH%
    exit /b 0
)
"%PROJECT_GIT%" -c "safe.directory=%PROJECT_ROOT%" -C "%PROJECT_ROOT%" bundle create "%BUNDLE_PATH%" --all
if errorlevel 1 exit /b 1
"%PROJECT_GIT%" -c "safe.directory=%PROJECT_ROOT%" -C "%PROJECT_ROOT%" bundle verify "%BUNDLE_PATH%"
if errorlevel 1 exit /b 1
echo History backup: %BUNDLE_PATH%
echo This includes committed files only. Run Snapshot first to save pending changes.
exit /b 0
