# Zipin - Boss自聘平台

Zipin 是一个基于 Spring Boot 构建的求职招聘平台，灵感来源于 Boss 直聘，旨在为求职者与招聘者之间提供一个高效、实时、安全的沟通平台。

## ✨ 功能特性

- 求职者与招聘者双端注册与登录
- 实时消息沟通（基于 WebSocket）
- 职位发布与申请
- 简历管理
- 权限校验与安全认证（JWS）
- 文件上传与管理（基于 MinIO）
- 数据缓存（基于 Redis）

## 🛠 技术栈

- Java 17
- Spring Boot
- Spring Security + JWS
- JPA (Hibernate)
- MinIO
- Redis
- WebSocket
- Maven

## 🚀 快速开始

### 环境依赖

- JDK 17+
- Maven 3.8+
- Redis 服务
- MinIO 服务

### 构建与运行

```bash
# 克隆项目
git clone https://github.com/han9912/zipin.git
cd zipin

# 构建项目
mvn clean install

# 运行应用
mvn spring-boot:run
```

### 默认端口

- Web 服务: `http://localhost:8080`

## 📁 项目结构简介

```
zipin/
├── src/
│   ├── main/
│   │   ├── java/com/zipin/    # Java 代码
│   │   └── resources/         # 配置文件
├── pom.xml                    # Maven 项目定义
```

## 📌 TODO

- 管理后台 UI 支持
- 职位推荐算法
- 多语言支持

## 📄 License

MIT License - see the [LICENSE](LICENSE) file for details.
