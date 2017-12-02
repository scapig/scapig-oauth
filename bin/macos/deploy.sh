#!/bin/bash

docker network create tapi-network
docker run --network=tapi-network --name tapi-api-definition \
 -d -p7000:7000 -i tapi-api-definition sh start-docker.sh
docker run --network=tapi-network --name tapi-api-scope \
 -d -p7010:7010 -i tapi-api-scope sh start-docker.sh
docker run --network=tapi-network --name tapi-application \
 -d -p7020:7020 -i tapi-application sh start-docker.sh
docker run --network=tapi-network --name tapi-delegated-authority \
 -d -p7030:7030 -i tapi-delegated-authority sh start-docker.sh
docker run --network=tapi-network --name tapi-oauth \
 -d -p7040:7040 -i tapi-oauth sh start-docker.sh
docker run --network=tapi-network --name tapi-oauth-login \
 -d -p7050:7050 -i tapi-oauth-login sh start-docker.sh
docker run --network=tapi-network --name tapi-requested-authority \
 -d -p7060:7060 -i tapi-requested-authority sh start-docker.sh
docker run --network=tapi-network --name tapi-developer \
 -d -p8000:8000 -i tapi-developer sh start-docker.sh
docker run --network=tapi-network --name tapi-developer-hub \
 -d -p8010:8010 -i tapi-developer-hub sh start-docker.sh
docker run --network=tapi-network --name tapi-documentation \
 -d -p8020:8020 -i tapi-documentation sh start-docker.sh
docker run --network=tapi-network --name tapi-gateway \
 -d -p8030:8030 -i tapi-gateway sh start-docker.sh
docker run --network=tapi-network --name tapi-publisher \
 -d -p8040:8040 -i tapi-publisher sh start-docker.sh
docker run --network=tapi-network --name tapi-hello \
 -d -p8080:8080 -i tapi-hello sh start-docker.sh
