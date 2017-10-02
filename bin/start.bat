@echo off

REM environment variable TAPI_REPOSITORY must be set

set PROJECT_NAME=%1
set PORT=%2
set PROJECT_PATH=%TAPI_REPOSITORY%\%PROJECT_NAME%
set STAGE_PATH=%PROJECT_PATH%\target\universal\stage
set TARGET=C:\temp\stage_%PROJECT_NAME%
set BIN=%TARGET%\bin\%PROJECT_NAME%

(
  setlocal EnableDelayedExpansion
  (del %TARGET% /q)
  (cd %PROJECT_PATH% && activator stage)
  (xcopy %STAGE_PATH% %TARGET% /s /e /y /i)
  START /B %BIN% -Dplay.server.pidfile.path=%TARGET%\RUNNING_PID -Dhttp.port=%PORT%
  endlocal
)
