# 使用 OpenJDK 8 作为基础镜像
FROM openjdk:8

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 设置工作目录
WORKDIR /usr/src/app

# 复制项目的 JAR 文件到镜像中
COPY iotclient-1.0.0.jar .

# 复制项目依赖到镜像中
COPY lib ./lib

# 使用ENTRYPOINT执行Java应用程序
ENTRYPOINT ["java", "-cp", "/usr/src/app/iotclient-1.0.0.jar:/usr/src/app/lib/*", "cn.edu.zju.cs.bs.IOTClient"]
