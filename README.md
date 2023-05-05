## 短信炸弹
该程序使用手机号往各个网站渠道发送短信验证码，从而对手机进行短信轰炸。
## 描述
该程序会内置少量的资源以供使用。但不负责提供网站发送验证码资源。网站短信发送接口资源可以通过后台页面
添加。
一般地，新添加网站短信资源需要编写扩展脚本，需要具有Groovy知识。
## 快速开始
1. 配置文件
在/opt/smsBomb下，新建application-prod.properties文件。格式如该工程中application-dev.properties文件中所示内容。
2. 下载源码安装
     ```
     git clone https://github.com/leijinjun/sms-bomb.git
     cd sms-bomb
     mvn clean install -Dmaven.test.skip=true
     mv target/sms-bomb.jar ./
     java -jar -Dspring.profiles.active=prod sms-bomb.jar \
     --spring.config.location=optional:file:/opt/smsBomb/application-prod.properties
     ```
3. 下载安装包安装

    [Release](https://github.com/leijinjun/sms-bomb/releases) 页面下载最新jar包。
    执行命令
    ```
    java -jar -Dspring.profiles.active=prod sms-bomb.jar \
    --spring.config.location=optional:file:/opt/smsBomb/application-prod.properties
    ```
4. docker安装
    ```
    git clone https://github.com/leijinjun/sms-bomb.git
    cd sms-bomb
    chmod +x deploy.sh
    sh deploy.sh
    docker run --name sms-bomb -p 8080:8080 -v /opt/smsBomb:/opt/smsBomb -d sms-bomb:[deploy.sh脚本中指定的version]
    ```
5. 安装ocr服务
    参考https://github.com/sml2h3/ddddocr
## 网站短信API资源
若需要短信API资源进行测试，可以通过邮箱联系我
