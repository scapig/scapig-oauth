#!/bin/bash

docker network create tapi-network
docker run --network=tapi-network --name mongo  \
 -d mongo
docker run --network=tapi-network --name tapi-api-definition.docker \
 -d -p7000:7000 -i tapi-api-definition sh start-docker.sh
docker run --network=tapi-network --name tapi-api-scope.docker \
 -d -p7010:7010 -i tapi-api-scope sh start-docker.sh
docker run --network=tapi-network --name tapi-application.docker \
 -d -p7020:7020 -i tapi-application sh start-docker.sh
docker run --network=tapi-network --name tapi-delegated-authority.docker \
 -d -p7030:7030 -i tapi-delegated-authority sh start-docker.sh
docker run --network=tapi-network --name tapi-oauth.docker \
 -d -p7040:7040 -i tapi-oauth sh start-docker.sh
docker run --network=tapi-network --name tapi-oauth-login.docker \
 -d -p7050:7050 -i tapi-oauth-login sh start-docker.sh
docker run --network=tapi-network --name tapi-requested-authority.docker \
 -d -p7060:7060 -i tapi-requested-authority sh start-docker.sh
docker run --network=tapi-network --name tapi-developer.docker \
 -d -p8000:8000 -i tapi-developer sh start-docker.sh
docker run --network=tapi-network --name tapi-developer-hub.docker \
 -d -p8010:8010 -i tapi-developer-hub sh start-docker.sh
docker run --network=tapi-network --name tapi-documentation.docker \
 -d -p8020:8020 -i tapi-documentation sh start-docker.sh
docker run --network=tapi-network --name tapi-gateway.docker \
 -d -p8030:8030 -i tapi-gateway sh start-docker.sh
docker run --network=tapi-network --name tapi-publisher.docker \
 -d -p8040:8040 -i tapi-publisher sh start-docker.sh
docker run --network=tapi-network --name tapi-hello.docker \
 -d -p8080:8080 -i tapi-hello sh start-docker.sh
