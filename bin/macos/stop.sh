#!/bin/bash

PROJECT=$1
PROJECT_PID_FILE=$TAPI_REPOSITORY/$PROJECT/target/universal/stage/RUNNING_PID
if [ -e $PROJECT_PID_FILE ]
then
  echo stopping $PROJECT
  kill $(cat $PROJECT_PID_FILE)
else
  echo $PROJECT already stopped
fi
