#!/bin/sh
SCRIPT=$(find . -type f -name tapi-oauth)
exec $SCRIPT $HMRC_CONFIG -Dhttp.port=7040
