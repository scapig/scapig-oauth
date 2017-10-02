@echo off

REM environment variable TAPI_REPOSITORY must be set

(
  setlocal EnableDelayedExpansion
  echo starting tapi-api-definition
  (cd %TAPI_REPOSITORY%/tapi-api-definition && activator "start 7000")
  echo starting tapi-api-scope
  (cd %TAPI_REPOSITORY%/tapi-api-scope && sbt activator "start 7010")
  echo starting tapi-application
  (cd %TAPI_REPOSITORY%/tapi-application && activator "start 7020")
  echo starting tapi-delegated-authority
  (cd %TAPI_REPOSITORY%/tapi-delegated-authority && activator "start 7030")
  echo starting tapi-oauth
  (cd %TAPI_REPOSITORY%/tapi-oauth && activator "start 7040")
  echo starting tapi-oauth-login
  (cd %TAPI_REPOSITORY%/tapi-oauth-login && activator "start 7050")
  echo starting tapi-requested-authority
  (cd %TAPI_REPOSITORY%/tapi-requested-authority && activator "start 7050")
  endlocal
)