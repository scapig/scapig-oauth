FROM openjdk:8

COPY target/universal/scapig-oauth-*.tgz .
COPY start-docker.sh .
RUN chmod +x start-docker.sh
RUN tar xvf scapig-oauth-*.tgz

EXPOSE 7040