#!/bin/sh
SCRIPT=$(find . -type f -name scapig-oauth)
rm -f scapig-oauth*/RUNNING_PID
exec $SCRIPT -Dhttp.port=9015 -J-Xms16M -J-Xmx64m
