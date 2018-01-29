#!/bin/sh
sbt universal:package-zip-tarball
docker build -t scapig-oauth .
docker tag scapig-oauth scapig/scapig-oauth
docker push scapig/scapig-oauth
