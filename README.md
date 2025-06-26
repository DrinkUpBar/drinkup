# 开喝APP (DrinkUp)

开喝后端APP代码

## 项目介绍

开喝是一款社交饮品分享应用的后端服务，基于Java Spring Boot 3.4开发。

## 环境要求

- JDK 21
- Maven 3.9+
- Docker 24+ 和 Docker Compose
- Git

## 技术栈

- **Framework**: Spring Boot 3.4
- **Database**: MariaDB
- **Cache**: Redis
- **ORM**: JPA, MyBatis
- **AI Integration**: Spring AI (OpenAI, Anthropic, ZhipuAI)
- **Vector Store**: Milvus
- **Object Storage**: MinIO
- **Documentation**: SpringDoc OpenAPI
- **Authentication**: Spring Security
- **Scheduling**: Quartz
- **SMS Service**: 阿里云SMS

## 快速开始

### 1. 克隆项目

```bash
git clone https://your-repository-url/drinkup.git
cd drinkup
```

### 2. 环境配置

复制环境变量示例文件并修改配置：

```bash
cp .env.example .env
```

编辑.env文件，填写必要的配置信息：

``` txt
ALIYUN_SMS_ACCESS_KEY_ID=your_access_key_id
ALIYUN_SMS_ACCESS_KEY_SECRET=your_access_key_secret
ALIYUN_SMS_SIGN_NAME=your_sign_name
ALIYUN_SMS_TEMPLATE_CODE=your_template_code
```

### 3. 启动依赖服务

使用Docker Compose启动所需的基础设施服务：

```bash
docker compose up -d
```

这将启动以下服务：

- MariaDB (数据库)
- Redis (缓存和会话存储)
- MinIO (对象存储)
- Milvus (向量数据库)
- ETCD (Milvus依赖)

### 4. 编译运行

使用Maven编译项目：

```bash
./mvnw clean package
```

运行应用：

```bash
./mvnw spring-boot:run
```

## 开发指南

### 数据库访问

项目同时支持JPA和MyBatis进行数据库操作，根据需要选择合适的方式。

### API文档

启动应用后，可以通过以下地址访问API文档：

``` txt
http://localhost:8080/swagger-ui.html
```

### 环境变量说明

- `ALIYUN_SMS_*`: 阿里云短信服务配置
- 其他环境变量请参考.env.example文件

## 项目结构

``` txt
drinkup/
├── src/                  # 源代码
│   ├── main/             # 主要代码
│   │   ├── java/         # Java代码
│   │   └── resources/    # 资源文件
│   └── test/             # 测试代码
├── docs/                 # 文档
├── .data/                # 本地开发数据存储
├── compose.yaml          # Docker Compose配置
├── pom.xml               # Maven配置
└── READEME.md            # 项目说明文档
```

## 依赖服务说明

### MariaDB

- 端口: 3306
- 用户名: test
- 密码: test
- 数据库: drinkup

### Redis

- 端口: 6380 (主机) -> 6379 (容器)
- 无密码认证

### MinIO

- API端口: 9000
- Web端口: 9001
- 访问密钥: minioadmin
- 密钥: minioadmin

### Milvus

- 端口: 19530 (gRPC), 9091 (HTTP)

## 故障排除

### 数据库连接问题

确保MariaDB容器正常运行，并且数据库用户权限配置正确：

```bash
docker compose ps
docker compose logs mariadb
```

### 依赖服务健康检查

所有服务都配置了健康检查，可以通过以下命令查看服务状态：

```bash
docker compose ps
```

## 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送分支 (`git push origin feature/amazing-feature`)
5. 创建Pull Request
