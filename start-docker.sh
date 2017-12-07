#!/bin/sh
SCRIPT=$(find . -type f -name tapi-oauth)
rm -f tapi-oauth*/RUNNING_PID
exec $SCRIPT -Dhttp.port=7040
