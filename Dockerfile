FROM eclipse-temurin:11-jdk-jammy
WORKDIR /app
COPY target/sms-bomb.jar .
ENV CONFIG_LOCATION_FILE=application-prod.properties
ENV JVM_XMS=256m
ENV JVM_XMX=512m
ENV JVM_OPTS='-Xmx512m -Xms256m'
VOLUME /opt/smsbomb
USER root
EXPOSE 8080
ENTRYPOINT java -jar -Xms${JVM_XMS} -Xmx${JVM_XMX} ${JVM_OPTS} -Dspring.profiles.active=prod sms-bomb.jar \
--spring.config.location=optional:file:/opt/smsbomb/$CONFIG_LOCATION_FILE