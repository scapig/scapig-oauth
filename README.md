## scapig-oauth

This is the microservice which coordinates the actions required for the Oauth 2.0 specs for the API Gateway http://scapig.com.


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
docker tag scapig-oauth scapig/scapig-oauth
docker login
docker push scapig/scapig-oauth
``

## Running
``
docker run -p9015:9015 -d scapig/scapig-oauth
``
