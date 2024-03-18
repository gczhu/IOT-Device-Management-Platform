# README

**该文件夹为“浙江大学-2023年秋冬-BS体系软件设计-课程大作业-物联网设备管理平台”项目文件夹**

**源代码、Dockerfile、数据库建表文件等均包含在 code 文件夹下，相关代码目录结构请见 code 文件夹下的 README.md 文件**

**document 文件夹下存放着各个项目文档，包括测试报告、个人小结、开发体会、设计报告和用户手册等**

**此外，该文件夹下还提供了项目的演示视频，方便老师直观地了解操作流程。由于我完成了 bonus 样式适配手机端，因此这里分别提供了 PC 端和手机端的演示视频**

```
3210105954_祝广程
├── PC端演示视频.mp4
├── 手机端演示视频.mp4
├── code
│   ├── .idea
│   ├── iotclient
│   ├── iot-dmp-vue-v2.0
│   ├── iotserver
│   ├── springboot
│   └── README.md
├── document
│    ├── 测试报告.pdf
│    ├── 个人小结.pdf
│    ├── 开发体会.pdf
│    ├── 设计报告.pdf
│    └── 用户手册.pdf
└── README.md
```

## 构建 Docker 容器并运行

**构建 docker 时需要在各 dockerfile 所在的目录下构建**
**构建 docker 时需要在各 dockerfile 所在的目录下构建**

### 构建自定义的网络（用于各端口的通信）
```
docker network create my_network
```

### 构建数据库的 Docker 容器并运行

**注意：可能 3306 端口已经被本机数据库软件占用了，请关闭占用程序**

**建议开启 VPN 后再进行 build， 不然可能发生 timeout 的错误！**

```
// cd 到 3210105954_祝广程\code\springboot\target 目录下
docker build -t mysql -f mysql.dockerfile .
docker run --name mysql --network my_network -e MYSQL_ROOT_PASSWORD=zgc112912 -p 3306:3306 -d mysql:latest
```

### 构建后端的 Docker 容器并运行

**注意：请在数据库的 Docker 容器构建并运行后，再进行该操作**

```
// cd 到 3210105954_祝广程\code\springboot\target 目录下
docker pull openjdk:8
docker build -t app -f app.dockerfile .
docker run --name app --network my_network -p 8081:8081 app
```

### 构建前端的 Docker 容器并运行

**注意：构建过程可能较长，请耐心等待**

```
// cd 到 3210105954_祝广程\code\iot-dmp-vue-v2.0 目录下
docker build -t web -f web.dockerfile .
docker run --network my_network -p 9528:9528 web
```

### 构建 MQTT 服务器的 Docker 容器并运行

**注意：可能 1883 端口已经被本机其他软件占用了，请关闭占用程序**

```
// cd 到 3210105954_祝广程\code\iotserver\target 目录下
docker build -t iotserver -f iotserver.dockerfile .
docker run --name iotserver --network my_network -p 1883:1883 iotserver
```

### 构建 MQTT 客户端的 Docker 容器并运行

**注意：请在 MQTT 服务器的 Docker 容器构建并运行后，再进行该操作**

```
// cd 到 3210105954_祝广程\code\iotclient\target 目录下
docker build -t iotclient -f iotclient.dockerfile .
docker run --name iotclient --network my_network iotclient
```

## 浏览器访问 url

**上述步骤均运行成功后，即可通过网址 http://localhost:9528/ 访问平台**

```url
http://localhost:9528/
```

测试用户帐号密码

账号：**zhangsan**

密码：**123456**