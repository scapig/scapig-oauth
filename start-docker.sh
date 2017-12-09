#!/bin/sh
SCRIPT=$(find . -type f -name scapig-oauth)
rm -f scapig-oauth*/RUNNING_PID
exec $SCRIPT -Dhttp.port=7040
