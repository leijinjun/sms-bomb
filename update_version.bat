@echo off
REM 提示用户输入新版本号
set /p new_version=please new version：

REM 执行 Maven 命令k
mvn versions:set -DnewVersion="%new_version%" -DgenerateBackupPoms=false