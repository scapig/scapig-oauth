#!/bin/bash

docker rm tapi-api-definition.docker
docker rm tapi-api-scope.docker
docker rm tapi-application.docker
docker rm tapi-delegated-authority.docker
docker rm tapi-oauth.docker
docker rm tapi-oauth-login.docker
docker rm tapi-requested-authority.docker
docker rm tapi-developer.docker
docker rm tapi-developer-hub.docker
docker rm tapi-documentation.docker
docker rm tapi-gateway.docker
docker rm tapi-publisher.docker
docker rm tapi-hello.docker
docker network rm tapi-network
