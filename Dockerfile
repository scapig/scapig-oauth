FROM openjdk:8

COPY target/universal/tapi-oauth-*.tgz .
COPY start-docker.sh .
RUN chmod +x start-docker.sh
RUN tar xvf tapi-oauth-*.tgz

EXPOSE 7040