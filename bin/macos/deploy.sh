#!/bin/bash

docker network create scapig-network
docker run --network=scapig-network --name scapig-nginx-documentation.docker  \
 -d -p 80:80 -i scapig-nginx-documentation
docker run --network=scapig-network --name scapig-nginx-gateway.docker  \
 -d -p 8080:8080 -i scapig-nginx-gateway
docker run --network=scapig-network --name mongo  \
 -d mongo
docker run --network=scapig-network --name scapig-api-definition.docker \
 -d -p7000:7000 -i scapig-api-definition sh start-docker.sh
docker run --network=scapig-network --name scapig-scope.docker \
 -d -p7010:7010 -i scapig-scope sh start-docker.sh
docker run --network=scapig-network --name scapig-application.docker \
 -d -p7020:7020 -i scapig-application sh start-docker.sh
docker run --network=scapig-network --name scapig-delegated-authority.docker \
 -d -p7030:7030 -i scapig-delegated-authority sh start-docker.sh
docker run --network=scapig-network --name scapig-oauth.docker \
 -d -p7040:7040 -i scapig-oauth sh start-docker.sh
docker run --network=scapig-network --name scapig-oauth-login.docker \
 -d -p7050:7050 -i scapig-oauth-login sh start-docker.sh
docker run --network=scapig-network --name scapig-requested-authority.docker \
 -d -p7060:7060 -i scapig-requested-authority sh start-docker.sh
docker run --network=scapig-network --name scapig-developer.docker \
 -d -p8000:8000 -i scapig-developer sh start-docker.sh
docker run --network=scapig-network --name scapig-developer-hub.docker \
 -d -p8010:8010 -i scapig-developer-hub sh start-docker.sh
docker run --network=scapig-network --name scapig-gateway.docker \
 -d -p8030:8030 -i scapig-gateway sh start-docker.sh
docker run --network=scapig-network --name scapig-publisher.docker \
 -d -p8040:8040 -i scapig-publisher sh start-docker.sh
docker run --network=scapig-network --name scapig-hello-world-api.docker \
 -d -p9000:9000 -i scapig-hello-world-api sh start-docker.sh
