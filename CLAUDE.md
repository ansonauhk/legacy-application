# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 2.7.18 legacy application specifically designed to demonstrate modernization patterns. It intentionally uses deprecated/legacy patterns that will require migration when upgrading to Spring Boot 3.x and Java 11+.

## Build and Run Commands

### Prerequisites
- Java 8 (JDK 1.8) - Required, located at: `C:\Users\anson\.jdks\corretto-1.8.0_462`
- Maven (if not available, use IntelliJ IDEA's embedded Maven)

### Common Commands
```bash
# Build the application
mvn clean compile

# Run the application
mvn spring-boot:run

# Package as JAR
mvn clean package

# Run tests
mvn test

# Run a specific test class
mvn test -Dtest=UserServiceTest

# Clean build artifacts
mvn clean
```

### Application Access Points
- Main application: `http://localhost:8080/legacy`
- H2 Console: `http://localhost:8080/legacy/h2-console`
- REST API base: `http://localhost:8080/legacy/api`
- Default credentials: `admin` / `admin123`

## Architecture and Migration Points

### Legacy Patterns Requiring Migration

The application deliberately uses these patterns that need updating for Spring Boot 3.x:

1. **javax → jakarta namespace migration**
   - Uses `javax.annotation.*` and `javax.validation.*` annotations
   - Files: All entity classes, controllers, and services

2. **WebSecurityConfigurerAdapter (deprecated)**
   - Location: `LegacyApplication.java:34-44`
   - Needs refactoring to SecurityFilterChain bean pattern

3. **Java 8 specific code requiring Java 11+ migration**
   - `Java8Features.java`: Uses sun.misc.BASE64Encoder/Decoder (removed in Java 11)
   - `Java8Features.java`: Uses sun.misc.Unsafe
   - `Java8Features.java`: Contains finalize() method (deprecated in Java 9)
   - `KeystoreConfig.java`: Uses sun.security.x509.* internal APIs

4. **Local file I/O (non-cloud ready)**
   - `FileStorageService.java`: Direct file system operations to `data/` directory
   - Should migrate to cloud storage abstractions

5. **Keystore-based secrets management**
   - `KeystoreConfig.java`: Uses local JKS keystore for secrets
   - Target: Migrate to Azure Key Vault or similar cloud service

### Data Flow Architecture

1. **REST API Layer** (`controller/`)
   - Exposes CRUD operations for User entities
   - Uses `@Autowired` field injection (consider constructor injection)

2. **Service Layer** (`service/`)
   - `UserService`: In-memory ConcurrentHashMap database
   - Integrates with FileStorageService for persistence
   - Uses Guava Optional (can migrate to Java 11 Optional enhancements)

3. **Persistence**
   - H2 file-based database at `./data/app-db`
   - File-based user data storage at `data/users.txt`
   - Automatic backup creation in `data/backups/`

4. **Security**
   - Local JKS keystore at `keystore/app-keystore.jks`
   - Stores TLS certificates and application secrets
   - Self-signed certificate generation on first run

5. **Logging**
   - Log4j2 configuration with file appenders
   - Logs written to `./logs/` directory
   - Separate files for application, error, and audit logs

## Key Migration Targets

When modernizing this application, focus on:
1. Spring Boot 2.7.18 → 3.x upgrade
2. Java 8 → Java 11/17 migration
3. javax.* → jakarta.* namespace changes
4. Replace deprecated Spring Security configuration
5. Remove usage of internal JDK APIs (sun.*)
6. Migrate from local file storage to cloud abstractions
7. Replace JKS keystore with cloud key management service