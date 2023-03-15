FROM openjdk:11-jdk-slim-bullseye
WORKDIR /usr/local/smsBomb
COPY target/sms-bomb.jar .
ENV CONFIG_LOCATION_FILE=application-prod.properties
VOLUME /opt/smsBomb
USER root
EXPOSE 8080
CMD java -jar -Dspring.profiles.active=prod sms-bomb.jar \
--spring.config.location=optional:file:/opt/smsBomb/$CONFIG_LOCATION_FILE