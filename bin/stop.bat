@echo off

REM environment variable TAPI_REPOSITORY must be set

set PROJECT_NAME=%1
set PID_PATH=C:\temp\stage_%PROJECT_NAME%\RUNNING_PID

if exist %PID_PATH% (
  setlocal EnableDelayedExpansion
  set /p PLAY_PID=<%PID_PATH%
  echo killing pid !PLAY_PID!
  taskkill /F /PID !PLAY_PID!
  echo deleting file !PID_PATH!
  del %PID_PATH%
  endlocal
)
