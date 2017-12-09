@echo off

(
  setlocal EnableDelayedExpansion
  echo stopping scapig-api-definition
  (stop.bat scapig-api-definition)
  echo stopping scapig-scope
  (stop.bat scapig-scope)
  echo stopping scapig-application
  (stop.bat scapig-application)
  echo stopping scapig-delegated-authority
  (stop.bat scapig-delegated-authority)
  echo stopping scapig-oauth
  (stop.bat scapig-oauth)
  echo stopping scapig-oauth-login
  (stop.bat scapig-oauth-login)
  echo stopping scapig-requested-authority
  (stop.bat scapig-requested-authority)
  echo stopping scapig-developer
  (stop.bat scapig-developer)
  echo stopping scapig-developer-hub
  (stop.bat scapig-developer-hub)
  echo stopping scapig-gateway
  (stop.bat scapig-gateway)
  echo stopping scapig-publisher
  (stop.bat scapig-publisher)
  echo stopping scapig-hello-world-api
  (stop.bat scapig-hello-world-api)
  endlocal
)
