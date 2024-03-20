## 短信炸弹
该程序使用手机号往各个网站渠道发送短信验证码，从而对手机进行短信轰炸。
轰炸机仅供学习使用。
## 轰炸原理
搜集各个网站页面上发送短信验证码使用的api接口，利用这些api接口来发送短信验证码。
## 描述
该程序会内置少量的资源以供使用。但不负责提供网站发送验证码资源。网站短信发送接口资源可以通过后台页面
添加。一般地，新添加网站短信资源需要编写扩展脚本，需要具有Groovy知识。
## 快速开始
1. 配置JDK
2. 下载源码安装
     ```
     git clone https://github.com/leijinjun/sms-bomb.git
     cd sms-bomb
     mvn clean install -Dmaven.test.skip=true
     mv target/sms-bomb.jar ./
     Linux、MacOS平台下执行
     java -jar -Dspring.profiles.active=dev -DDB_FILE_PATH=sqlite3DB文件路径 sms-bomb.jar
     Windows平台下执行
     java -jar "-Dspring.profiles.active=dev" "-DDB_FILE_PATH=sqlite3DB文件路径" sms-bomb.jar
     ```
3. 下载安装包安装

    [Release](https://github.com/leijinjun/sms-bomb/releases) 页面下载最新jar包。
    执行命令
    ```
    Linux、MacOS平台下执行
     java -jar -Dspring.profiles.active=dev -DDB_FILE_PATH=sqlite3DB文件路径 sms-bomb.jar
    Windows平台下执行
     java -jar "-Dspring.profiles.active=dev" "-DDB_FILE_PATH=sqlite3DB文件路径" sms-bomb.jar
    ```
   其中sqlite3DB文件在[sqllite3DB样例文件](https://github.com/leijinjun/sms-bomb/blob/develop/src/main/resources/db/sms_bomb.db) ，该文件为样例文件，仅用于测试。
4. docker安装
    在路径/opt/smsBomb下添加sms_bomb.db文件。
    ```
    git clone https://github.com/leijinjun/sms-bomb.git
    cd sms-bomb
    chmod +x deploy.sh
    sh deploy.sh
    docker run --name sms-bomb -p 8080:8080 -v /opt/smsBomb:/opt/smsBomb -d sms-bomb:[deploy.sh脚本中指定的version]
    ```
5. 安装ocr服务
    参考https://github.com/sml2h3/ddddocr
