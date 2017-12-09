@echo off

REM environment variable TAPI_REPOSITORY must be set

(
  setlocal EnableDelayedExpansion
  echo starting scapig-api-definition
  (start.bat scapig-api-definition 7000)
  echo starting scapig-scope
  (start.bat scapig-scope 7010)
  echo starting scapig-application
  (start.bat scapig-application 7020)
  echo starting scapig-delegated-authority
  (start.bat scapig-delegated-authority 7030)
  echo starting scapig-oauth
  (start.bat scapig-oauth 7040)
  echo starting scapig-oauth-login
  (start.bat scapig-oauth-login 7050)
  echo starting scapig-requested-authority
  (start.bat scapig-requested-authority 7060)
  echo starting scapig-developer
  (start.bat scapig-developer 8000)
  echo starting scapig-developer-hub
  (start.bat scapig-developer-hub 8010)
  echo starting scapig-gateway
  (start.bat scapig-gateway 8030)
  echo starting scapig-publisher
  (start.bat scapig-publisher 8040)
  echo starting scapig-hello-world-api
  (start.bat scapig-hello-world-api 8080)
  endlocal
)