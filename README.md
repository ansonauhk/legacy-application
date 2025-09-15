# Legacy Spring Boot Application - Modernization Demo

A Spring Boot 2.7.18 application intentionally built with legacy patterns to demonstrate cloud modernization and migration strategies to Spring Boot 3.x and Java 11+.

## 🎯 Purpose

This application serves as a hands-on example for:
- Understanding common legacy patterns in Spring Boot 2.x applications
- Learning migration strategies to Spring Boot 3.x
- Practicing application modernization for cloud deployment
- Demonstrating the transition from Java 8 to Java 11/17

## 🚀 Quick Start

### Prerequisites

- **Java 8 (JDK 1.8)** - Required for running the legacy application
- **Maven 3.6+** - For building and running the application
- **Git** - For version control

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/app-mod-demo.git
cd app-mod-demo
```

2. Build the application:
```bash
mvn clean compile
```

3. Run the application:
```bash
mvn spring-boot:run
```

4. Access the application:
- Main application: http://localhost:8080/legacy
- H2 Database Console: http://localhost:8080/legacy/h2-console
- REST API: http://localhost:8080/legacy/api

### Default Credentials
- Username: `admin`
- Password: `admin123`

## 📁 Project Structure

```
app-mod-demo/
├── src/
│   ├── main/
│   │   ├── java/com/example/legacyapp/
│   │   │   ├── config/          # Configuration classes (KeystoreConfig)
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── model/          # Entity models
│   │   │   ├── repository/     # Data repositories
│   │   │   ├── service/        # Business logic
│   │   │   ├── util/           # Utility classes
│   │   │   └── LegacyApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── log4j2.xml
│   └── test/
├── data/                # Application data directory
│   ├── users.txt       # User data storage
│   ├── backups/        # Automatic backups
│   └── uploads/        # File uploads
├── keystore/           # Security certificates
├── logs/              # Application logs
└── pom.xml
```

## 🏗️ Architecture

### Technology Stack
- **Framework**: Spring Boot 2.7.18
- **Java Version**: Java 8
- **Database**: H2 (file-based)
- **Security**: Spring Security with JKS Keystore
- **Logging**: Log4j2
- **Build Tool**: Maven

### Key Components

1. **REST API Layer**
   - CRUD operations for User management
   - Basic authentication with Spring Security

2. **Service Layer**
   - In-memory user database (ConcurrentHashMap)
   - File-based persistence
   - Integration with legacy Java 8 features

3. **Data Persistence**
   - H2 embedded database
   - File-based storage in `data/` directory
   - Automatic backup system

4. **Security**
   - Local JKS keystore for certificates
   - Self-signed certificate generation
   - Basic HTTP authentication

## 🔄 Legacy Patterns & Migration Targets

This application intentionally includes legacy patterns that require modernization:

### 1. javax to jakarta Migration
- **Current**: Uses `javax.annotation.*` and `javax.validation.*`
- **Target**: Migrate to `jakarta.*` namespace for Spring Boot 3.x

### 2. Deprecated Security Configuration
- **Current**: `WebSecurityConfigurerAdapter` (deprecated)
- **Target**: Modern `SecurityFilterChain` bean configuration

### 3. Java 8 Internal APIs
- **Current**: Uses `sun.misc.BASE64Encoder`, `sun.security.x509.*`
- **Target**: Java 11+ standard APIs

### 4. Local File Storage
- **Current**: Direct file system operations
- **Target**: Cloud storage abstractions (Azure Blob, AWS S3)

### 5. Local Secrets Management
- **Current**: JKS keystore for secrets
- **Target**: Cloud key management (Azure Key Vault, AWS Secrets Manager)

## 📊 API Endpoints

### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create new user |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |
| GET | `/api/users/search?username={query}` | Search users |

### Example Request

```bash
# Create a new user
curl -X POST http://localhost:8080/legacy/api/users \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '{
    "username": "john.doe",
    "email": "john@example.com"
  }'
```

## 🧪 Testing

Run all tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=UserServiceTest
```

## 📝 Configuration

Key configuration properties in `application.properties`:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/legacy

# Database Configuration
spring.datasource.url=jdbc:h2:file:./data/app-db
spring.h2.console.enabled=true

# Security
spring.security.user.name=admin
spring.security.user.password=admin123

# Keystore
keystore.path=keystore/app-keystore.jks
keystore.password=changeit
```

## 🔧 Development

### Building JAR
```bash
mvn clean package
java -jar target/legacy-app-1.0.0-SNAPSHOT.jar
```

### Clean Build
```bash
mvn clean
```

### Skip Tests
```bash
mvn package -DskipTests
```

## 🎓 Learning Resources

This project is ideal for learning:
- Spring Boot 2 to 3 migration strategies
- Java 8 to 11/17 upgrade paths
- Cloud-native application patterns
- Dependency modernization techniques
- Security best practices migration

## 🤝 Contributing

This is a demonstration project for learning purposes. Feel free to:
1. Fork the repository
2. Create feature branches for different modernization approaches
3. Experiment with migration strategies
4. Share your learnings

## 📄 License

This project is created for educational purposes and is provided as-is without any warranty.

## 🆘 Troubleshooting

### Common Issues

1. **Java Version Error**
   - Ensure Java 8 is installed and JAVA_HOME is set correctly
   - Check version: `java -version`

2. **Port Already in Use**
   - Change port in `application.properties`
   - Or kill process using port 8080

3. **Keystore Issues**
   - Delete `keystore/` directory to regenerate certificates
   - Check keystore password in `application.properties`

4. **Database Connection Issues**
   - Ensure `data/` directory has write permissions
   - Delete `data/app-db.*` files to reset database

## 📚 Additional Documentation

- [Spring Boot 2.7 Documentation](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/)
- [Spring Boot 3 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Java 8 to 11 Migration](https://docs.oracle.com/en/java/javase/11/migrate/)