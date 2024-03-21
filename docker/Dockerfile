FROM eclipse-temurin:11-jdk-jammy
WORKDIR /app
COPY target/sms-bomb*.jar sms-bomb.jar
#ENV CONFIG_LOCATION_FILE=application-prod.properties
ENV SQLITE3DB=sms_bomb.db
ENV JVM_XMS=256m
ENV JVM_XMX=512m
ENV DDDD_OCR_URL=http://192.168.100.1:9898/ocr/file
ENV DDDD_OCR_BASE64_URL=http://192.168.100.1:9898/ocr/b64/text
VOLUME /opt/smsbomb
USER root
EXPOSE 8080
ENTRYPOINT java -jar -Xms${JVM_XMS} -Xmx${JVM_XMX} -Dspring.profiles.active=dev \
-DDB_FILE_PATH=/opt/smsbomb/${SQLITE3DB} -Docr.dddd.url=${DDDD_OCR_URL} -Docr.dddd.base64.url=${DDDD_OCR_BASE64_URL} sms-bomb.jar