# 第一阶段：构建环境
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN mvn package -DskipTests

# 第二阶段：运行环境
FROM eclipse-temurin:21-jre-alpine

# 设置非 root 用户以增强安全性
RUN addgroup -S vertx && adduser -S vertx -G vertx
USER vertx

WORKDIR /opt/vertx

# 从构建阶段复制生成的 fat jar
# 注意：请根据 pom.xml 中的 artifactId 和 version 修改文件名
COPY --from=build --chown=vertx:vertx /app/target/*-fat.jar ./app.jar

# 环境变量设置
ENV JAVA_OPTS="-Xms256m -Xmx512m" \
    VERTICLE_HOME="/opt/vertx"

# 暴露 Vert.x 默认端口（根据你的应用修改）
EXPOSE 8080

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]