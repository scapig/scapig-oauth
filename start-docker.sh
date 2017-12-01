#!/bin/sh
SCRIPT=$(find . -type f -name tapi-oauth)
exec $SCRIPT -Dhttp.port=7040
