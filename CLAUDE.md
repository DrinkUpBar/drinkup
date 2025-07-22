# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Quick Commands

### Development Setup
```bash
# Start infrastructure services
docker compose up -d

# Build and run application
./mvnw clean package
./mvnw spring-boot:run

# Access API documentation
http://localhost:8080/swagger-ui.html
```

### Common Development Commands
```bash
# Run tests
./mvnw test

# Run single test class
./mvnw test -Dtest=UserServiceTest

# Run specific test method
./mvnw test -Dtest=UserServiceTest#testCreateUser

# Format code (Spotless)
./mvnw spotless:apply

# Build Docker image
./mvnw spring-boot:build-image

# Package application
./mvnw clean package -DskipTests
```

### Database Operations
```bash
# Access MariaDB
docker compose exec mariadb mysql -u test -p drinkup

# Access Redis CLI
docker compose exec redis redis-cli -p 6379

# Access Milvus
http://localhost:9091 (Milvus web interface)
```

## Architecture Overview

### Technology Stack
- **Framework**: Spring Boot 3.4.4, Java 21
- **Architecture**: Spring Modulith (modular monolith)
- **Database**: MariaDB, Redis, Milvus (vector database)
- **AI Integration**: Spring AI (OpenAI, DeepSeek, ZhipuAI)
- **Storage**: AWS S3 (via Spring Cloud AWS)
- **Security**: Spring Security with JWT, OAuth2 (Apple, Google)
- **Monitoring**: OpenTelemetry, Micrometer, Prometheus

### Module Structure

The application uses Spring Modulith with domain-driven design:

```
cool.drinkup.drinkup/
├── user/           # User management & authentication
├── wine/           # Wine management with RAG AI features
├── favorite/       # Favorite items functionality
├── workflow/       # AI workflow engine
├── record/         # Tasting records
├── infrastructure/ # SMS, image processing
├── shared/         # Common DTOs and interfaces
└── common/         # Shared utilities
```

### Key Patterns

1. **Module Isolation**: Each module uses `@ApplicationModule` annotation
2. **SPI Design**: `spi/` packages expose public APIs, `internal/` contains implementation
3. **Layer Architecture**: Controller → Service → Repository → Model
4. **AI Integration**: Vector search with Milvus, RAG implementation in wine module

### Configuration

#### Core Configuration Files
- `config/application.yaml` - Main application configuration
- `config/logback-spring.xml` - Logging configuration
- `compose.yaml` - Docker services configuration
- `pom.xml` - Maven dependencies and build configuration

#### Environment Variables
Required environment variables (copy from `.env.example`):
- `ALIYUN_SMS_ACCESS_KEY_ID` - Alibaba Cloud SMS
- `ALIYUN_SMS_ACCESS_KEY_SECRET` - Alibaba Cloud SMS
- AI service API keys are in application.yaml (development only)

### Development Services

| Service      | Port | Description |
|--------------|------|-------------|
| MariaDB      | 3306 | Primary database |
| Redis        | 6380 | Cache & sessions |
| MinIO        | 9000 | Object storage |
| Milvus       | 19530 | Vector database |
| Elasticsearch| 9200 | Search & logging |

### AI Features

- **Wine RAG**: Vector search for wine recommendations using Milvus
- **Multiple AI Models**: OpenAI, DeepSeek, ZhipuAI integration
- **Image Generation**: Fal.ai and Glif integration for cocktail images
- **Workflow Engine**: AI-powered bartender recommendations

### Security

- **Authentication**: JWT tokens with configurable expiration
- **OAuth2**: Apple and Google OAuth integration
- **SMS Verification**: Alibaba Cloud SMS for phone verification
- **Token Expiry**: Configurable in `application.yaml`

### Testing

- **Unit Tests**: Standard Spring Boot test framework
- **Integration Tests**: Uses Testcontainers for database tests
- **Security Tests**: Spring Security test utilities
- **API Documentation**: Spring REST Docs for API testing