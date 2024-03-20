#!/bin/bash
version="latest"
mvn clean install -Dmaven.test.skip=true
echo "install finished"
rm -rf tmp
mkdir -p tmp/target
mv target/*.jar tmp/target/
mv tmp/target/sms-bomb*.jar tmp/target/sms-bomb.jar
docker image rm sms-bomb:$version
docker build -f ./Dockerfile -t sms-bomb:$version ./tmp
rm -rf tmp