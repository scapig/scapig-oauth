#!/bin/bash

sh build-instance.sh scapig-api-definition
sh build-instance.sh scapig-scope
sh build-instance.sh scapig-application
sh build-instance.sh scapig-delegated-authority
sh build-instance.sh scapig-oauth
sh build-instance.sh scapig-oauth-login
sh build-instance.sh scapig-requested-authority
sh build-instance.sh scapig-developer
sh build-instance.sh scapig-developer-hub
sh build-instance.sh scapig-gateway
sh build-instance.sh scapig-publisher
sh build-instance.sh scapig-hello-world-api
docker build -t scapig-nginx-documentation nginx-documentation
docker build -t scapig-nginx-gateway nginx-gateway
