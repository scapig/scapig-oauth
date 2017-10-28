#!/bin/bash

PROJECT=$1
PORT=$2
(cd $TAPI_REPOSITORY/$PROJECT; sbt "runProd $PORT -Dapplication.home=$PROJECT" &)
