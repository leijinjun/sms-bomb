@echo off
SET version=latest
SET current_dir=%CD%
echo Current dir:%current_dir%
cd ..
@call mvn clean install -Dmaven.test.skip=true
echo install finished
cd /d %current_dir%
rd /s /q target
echo .original > EXCLUDE.txt
mkdir target
xcopy ..\target\*.jar target /exclude:EXCLUDE.txt /i
del EXCLUDE.txt
ren target\sms-bomb*.jar sms-bomb.jar
docker image rm sms-bomb:%version%
docker build -f ./Dockerfile -t sms-bomb:%version% ./target
rd /s /q target