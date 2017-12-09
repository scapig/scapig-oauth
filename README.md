## scapig-oauth

## Building
``
sbt clean test it:test component:test
``

## Packaging
``
sbt universal:package-zip-tarball
docker build -t scapig-oauth .
``

## Running
``
docker run -p7040:7040 -i -a stdin -a stdout -a stderr scapig-oauth sh start-docker.sh
``