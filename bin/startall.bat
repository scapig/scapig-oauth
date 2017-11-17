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
  echo starting tapi-developer
  (start.bat tapi-developer 8000)
  echo starting tapi-developer-hub
  (start.bat tapi-developer-hub 8010)
  echo starting tapi-documentation
  (start.bat tapi-documentation 8020)
  echo starting tapi-gateway
  (start.bat tapi-gateway 8030)
  echo starting tapi-publisher
  (start.bat tapi-publisher 8040)
  echo starting tapi-hello
  (start.bat tapi-hello 8080)
  endlocal
)