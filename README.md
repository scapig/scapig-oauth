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

## Publishing
``
docker tag scapig-oauth scapig/scapig-oauth:VERSION
docker login
docker push scapig/scapig-oauth:VERSION
``

## Running
``
docker run -p9015:9015 -d scapig/scapig-oauth:VERSION
``
