# 使用 OpenJDK 23 作为基础镜像
FROM openjdk:23-bullseye

# 设置工作目录
WORKDIR /app

# 安装中文字体
RUN echo 'deb https://mirrors.cloud.tencent.com/debian/ bullseye main contrib non-free' > /etc/apt/sources.list && \
    echo 'deb https://mirrors.cloud.tencent.com/debian/ bullseye-updates main contrib non-free' >> /etc/apt/sources.list && \
    echo 'deb https://mirrors.cloud.tencent.com/debian-security bullseye-security main contrib non-free' >> /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
    fonts-wqy-zenhei \
    fonts-wqy-microhei \
    fonts-noto-cjk \
    fontconfig \
    && rm -rf /var/lib/apt/lists/* \

# 刷新字体缓存
RUN fc-cache -fv

# 复制 JAR 文件到容器中
COPY target/QuickFile-0.0.1-SNAPSHOT.jar app.jar

# 运行应用
ENTRYPOINT ["java", "-jar", "app.jar"]
