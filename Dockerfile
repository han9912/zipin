# 使用 Java 17 作为基础镜像
FROM eclipse-temurin:17-jdk-alpine

# 指定工作目录
WORKDIR /app

# 拷贝构建好的 jar 包（注意改成你实际的文件名）
COPY target/zipin-0.0.1-SNAPSHOT.jar app.jar

# 容器暴露的端口（Cloud Run 默认为 $PORT）
EXPOSE 8080

# 启动 Spring Boot 项目
ENTRYPOINT ["java", "-jar", "app.jar"]