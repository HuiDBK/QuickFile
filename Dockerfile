# 使用 OpenJDK 23 作为基础镜像
FROM openjdk:23-slim-bullseye

# 设置工作目录
WORKDIR /app

# 复制 JAR 文件到容器中
COPY target/QuickFile-0.0.1-SNAPSHOT.jar app.jar

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]
