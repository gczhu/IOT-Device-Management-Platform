FROM node:latest AS build-stage

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /iot-dmp-vue-v2.0

COPY package*.json ./

RUN npm install

COPY . .

EXPOSE 9528

CMD ["npm", "run", "dev"]
