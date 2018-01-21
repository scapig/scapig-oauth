#!/bin/sh
sbt universal:package-zip-tarball
docker build -t scapig-oauth .
docker tag scapig-oauth scapig/scapig-oauth:0.1
docker push scapig/scapig-oauth:0.1
