@echo off

REM environment variable TAPI_REPOSITORY must be set

(
  setlocal EnableDelayedExpansion
  echo starting tapi-api-definition
  (start.bat tapi-api-definition 7000)
  echo starting tapi-api-scope
  (start.bat tapi-api-scope 7010)
  echo starting tapi-application
  (start.bat tapi-application 7020)
  echo starting tapi-delegated-authority
  (start.bat tapi-delegated-authority 7030)
  echo starting tapi-oauth
  (start.bat tapi-oauth 7040)
  echo starting tapi-oauth-login
  (start.bat tapi-oauth-login 7050)
  echo starting tapi-requested-authority
  (start.bat tapi-requested-authority 7060)
  endlocal
)