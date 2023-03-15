@echo off
SET version=latest
@call mvn clean install -Dmaven.test.skip=true
echo install finished
rmdir /s /q tmp
mkdir tmp\target
echo .original > EXCLUDE.txt
xcopy target tmp\target /exclude:EXCLUDE.txt
del EXCLUDE.txt
docker image rm sms-bomb:%version%
docker build -f ./Dockerfile -t sms-bomb:%version% ./tmp
rmdir /q /s tmp