@echo off

(
  setlocal EnableDelayedExpansion
  echo stopping tapi-api-definition
  (stop.bat tapi-api-definition)
  echo stopping tapi-api-scope
  (stop.bat tapi-api-scope)
  echo stopping tapi-application
  (stop.bat tapi-application)
  echo stopping tapi-delegated-authority
  (stop.bat tapi-delegated-authority)
  echo stopping tapi-oauth
  (stop.bat tapi-oauth)
  echo stopping tapi-oauth-login
  (stop.bat tapi-oauth-login)
  echo stopping tapi-requested-authority
  (stop.bat tapi-requested-authority)
  echo stopping tapi-developer
  (stop.bat tapi-developer)
  echo stopping tapi-developer-hub
  (stop.bat tapi-developer-hub)
  echo stopping tapi-documentation
  (stop.bat tapi-documentation)
  echo stopping tapi-gateway
  (stop.bat tapi-gateway)
  echo stopping tapi-publisher
  (stop.bat tapi-publisher)
  echo stopping tapi-hello
  (stop.bat tapi-hello)
  endlocal
)
