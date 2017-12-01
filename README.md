## tapi-oauth

## Building
``
sbt clean test it:test component:test
``

## Packaging
``
sbt universal:package-zip-tarball
docker build -t tapi-oauth .
``

## Running
``
docker run -p7040:7040 -i -a stdin -a stdout -a stderr tapi-oauth sh start-docker.sh
``