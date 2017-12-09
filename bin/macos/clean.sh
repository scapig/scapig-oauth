#!/bin/bash

docker rm scapig-nginx-documentation.docker
docker rm scapig-nginx-gateway.docker
docker rm scapig-api-definition.docker
docker rm scapig-scope.docker
docker rm scapig-application.docker
docker rm scapig-delegated-authority.docker
docker rm scapig-oauth.docker
docker rm scapig-oauth-login.docker
docker rm scapig-requested-authority.docker
docker rm scapig-developer.docker
docker rm scapig-developer-hub.docker
docker rm scapig-gateway.docker
docker rm scapig-publisher.docker
docker rm scapig-hello-world-api.docker
docker network rm scapig-network
