# Can+Cat Project

Sistema de información con arquitectura fullstack: Spring Boot (REST API), Vue.js 3 (SPA) y PostgreSQL.

## Arquitectura

Frontend:  Vue 3 + Vite + Axios
Backend: Java 17 + Spring Boot 3
Base de datos: PostgreSQL 15
Build Tool: Maven

## Requisitos previos

- Java 17+
- Node.js 18+
- PostgreSQL 15+
- Maven (o usar el wrapper `./mvnw`)

## Configuración

### 1. Base de datos

```sql
CREATE DATABASE sistema_db;
CREATE USER sistema_user WITH PASSWORD 'Sistema123!';
GRANT ALL PRIVILEGES ON DATABASE sistema_db TO sistema_user;