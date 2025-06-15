# Kanban API

## Overview

`kanban-api` is the backend service for a Kanban board application, built with Spring Boot, PostgreSQL, Redis, Kafka, and secured with JWT authentication.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** (set `JAVA_HOME` accordingly)
- **Maven 3.8+**
- **Docker & Docker Compose**

## Environment Variables

The application reads configuration values from environment variables. Create a file named `.env` in the project root with the following entries:

```env
# PostgreSQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=kanban_db
DB_USERNAME=kanban_user
DB_PASSWORD=kanban_pass

# JWT Settings
JWT_SECRET=your_secret_key_here
# Expiration in milliseconds
JWT_EXPIRATION=1800000
```

> **Note:** For production, ensure secrets are managed securely (e.g., Vault, AWS Secrets Manager).

## Setup & Running

### 1. Clone the Repository

```bash
git clone https://github.com/armaansandhu/kanban-api.git
cd kanban-api
```

### 2. Start Dependent Services

Use Docker Compose to launch PostgreSQL and any other services:

```bash
docker-compose up -d
```

This will start:
- PostgreSQL (configured by `docker-compose.yaml`)

### 3. Build & Run the Application

You can run the app in two ways:

- **Via Maven** (development mode with DevTools):

  ```bash
  mvn spring-boot:run
  ```

- **As a Jar** (production mode):

  ```bash
  mvn clean package
  java -jar target/kanban-api-0.0.1-SNAPSHOT.jar
  ```

The service will start on `http://localhost:8080` by default.

## Logging

- **Console:** Logs at `INFO` level by default; debug enabled for application packages.
- **File:** All logs are written to `logs/application.log`. Adjust paths in `application.properties`.
