# IOT-Device-Management-Platform

2023秋冬-浙江大学-BS体系软件设计-课程大作业

## 前端代码

**前端采用 Vue 框架开发，使用了 element-ui 组件库，文件目录结构如下，网页代码放在 /src 目录下**

**前端代码的 dockerfile 文件名为 web.dockerfile**

```
iot-dmp-vue-v2.0
├── dist
├── node_modules
├── public
├── src
├── .editorconfig
├── .env.development
├── .env.production
├── .env.staging
├── .eslintignore
├── .eslintrc.js
├── .gitignore
├── .travis.yml
├── babel.config.js
├── jest.config.js
├── jsconfig.json
├── LICENSE
├── package.json
├── package-lock.json
├── postcss.config.js
├── README.md
├── README-zh.md
├── vue.config.js
└── web.dockerfile
```

## 后端代码

**采用 Springboot 框架开发，使用了 myBatisPlus 来对数据库进行操作，JWT进行身份验证，文件目录结构如下，源代码放在 /src 目录下**

**resources 目录下存放着配置文件，其中 appliction.properties 用于本地运行项目，appliction-pro.properties 用于生成在 docker 中运行的 jar 包**

**target 目录下存放着 docker 运行所需的 jar 包 springboot-0.0.1-SNAPSHOT.jar，以及建表文件 init.sql，mysql 数据库的 dockerfile（名为 mysql.dockerfile）和后端代码的 dockerfile（名为 app.dockerfile）**

**pom.xml 包含着运行该项目所需的依赖**

```
springboot/
├── .idea
├── .mvn
├── src
├── target
│   ├── classes
│   ├── generated-sources
│   ├── maven-archiver
│   ├── maven-status
│   ├── init.sql
│   ├── mysql.dockerfile
│   ├── springboot-0.0.1-SNAPSHOT.jar
│   └── springboot-0.0.1-SNAPSHOT.jar.original
├── .gitignore
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
└── springboot.iml
```

## MQTT 客户端代码

**文件目录结构如下，源代码放在 /src 目录下**

**target 目录下存放着 docker 运行所需的 jar 包 iotclient-1.0.0.jar，以及 MQTT 客户端端代码的 dockerfile（名为 iotclient.dockerfile）**

**pom.xml 包含着运行该项目所需的依赖**

```
iotclient/
├── .idea
├── src
├── target
│   ├── classes
│   ├── generated-sources
│   ├── lib
│   ├── maven-archiver
│   ├── maven-status
│   ├── iotclient.dockerfile
│   └── iotclient-1.0.0.jar
├── .DS_Store
├── iotclient.iml
├── pom.xml
└── README.md
```

## MQTT 服务端代码

**文件目录结构如下，源代码放在 /src 目录下**

**target 目录下存放着 docker 运行所需的 jar 包 iotserver-1.0-SNAPSHOT.jar，以及 MQTT 客户端端代码的 dockerfile（名为 iotserver.dockerfile）**

**pom.xml 包含着运行该项目所需的依赖**

```
iotserver/
├── .idea
├── src
├── target
│   ├── classes
│   ├── generated-sources
│   ├── lib
│   ├── maven-archiver
│   ├── maven-status
│   ├── iotserver.dockerfile
│   └── iotserver-1.0-SNAPSHOT.jar
└── pom.xml
```