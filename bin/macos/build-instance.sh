#!/bin/bash

PROJECT=$1
(cd $TAPI_REPOSITORY/$PROJECT; sbt "universal:package-zip-tarball"; docker build -t $PROJECT .)
