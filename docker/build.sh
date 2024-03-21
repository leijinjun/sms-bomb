#!/bin/bash
version="latest"
current_dir=$(pwd)
cd ..
mvn clean install -Dmaven.test.skip=true
echo "install finished"
cd "$current_dir"
rm -rf target
mkdir -p target
mv target/*.jar target/
docker image rm sms-bomb:$version
docker build -f ./Dockerfile -t sms-bomb:$version ./target
rm -rf target