# Can+Cat Project

Sistema de información fullstack: API REST con Spring Boot, SPA con Vue 3 + Vite y base de datos PostgreSQL.

## Stack

- **Backend:** Java 17+, Spring Boot, Spring Security + JWT, Spring Data JPA, Flyway, PostgreSQL, Maven Wrapper.
- **Frontend:** Vue 3, Vite, Pinia, Vue Router, Axios.
- **Base de datos:** PostgreSQL 15+.
- **Build tool:** Maven (`./mvnw` / `mvnw.cmd`).

## Requisitos previos

- Java 17 o superior.
- Node.js 20.19+ o 22.12+ (Vite 8 lo requiere; Node 18 puede dar error).
- npm.
- PostgreSQL 15 o superior.
- Git.
- Maven no es obligatorio: se usa el wrapper `./mvnw` o `mvnw.cmd`.

## 1. Configurar la base de datos

Crea la base de datos y un usuario. Usa tus propios valores locales.

```sql
CREATE DATABASE "can-cat-system";
CREATE USER can_cat_user WITH PASSWORD 'tu_password_segura';
GRANT ALL PRIVILEGES ON DATABASE "can-cat-system" TO can_cat_user;
```

> Nota: si el nombre lleva guion, como `can-cat-system`, debes escribirlo entre comillas dobles en SQL.


## 2. Configurar variables de entorno

### Linux / macOS

```bash
export DB_URL="jdbc:postgresql://localhost:5432/can-cat-system"
export DB_USERNAME="can_cat_user"
export DB_PASSWORD="tu_password_segura"
export MAIL_USERNAME="tu_correo@gmail.com"
export MAIL_PASSWORD="tu_app_password"
export JWT_SECRET="genera_una_clave_larga_y_segura_de_al_menos_256_bits"
```

### Windows PowerShell

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/can-cat-system"
$env:DB_USERNAME="can_cat_user"
$env:DB_PASSWORD="tu_password_segura"
$env:MAIL_USERNAME="tu_correo@gmail.com"
$env:MAIL_PASSWORD="tu_app_password"
$env:JWT_SECRET="genera_una_clave_larga_y_segura_de_al_menos_256_bits"
```

> `MAIL_PASSWORD` debe ser una **contraseña de aplicación** de Gmail, no la contraseña normal de la cuenta.

## 3. Configurar el backend

El archivo `backend/src/main/resources/application.yml` activa el perfil `dev`:

```yaml
spring:
  application:
    name: can-cat-system
  profiles:
    active: dev
```

El archivo `backend/src/main/resources/application-dev.yml` debe verse así, usando variables de entorno:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true

jwt:
  secret: ${JWT_SECRET}
  expiration-ms: ${JWT_EXPIRATION_MS:86400000}
  password-reset-expiration-ms: ${JWT_PASSWORD_RESET_EXPIRATION_MS:86400000}

logging:
  level:
    com.cancat: DEBUG
    org.springframework.security: DEBUG
```

## 4. Configurar el frontend

El archivo `frontend/vite.config.js` no tiene proxy hacia el backend. Por eso, el frontend debe saber la URL del backend mediante una variable de entorno.

Crea `frontend/.env.development`:

```env
VITE_API_URL=http://localhost:8080
```

Si tus endpoints están bajo `/api`, usa:

```env
VITE_API_URL=http://localhost:8080/api
```

En tu cliente Axios, usa esa variable:

```js
import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL
})

export default api
```

Si no usas variables de entorno, revisa dónde esté configurada la `baseURL` de Axios y ajusta la URL manualmente.

## 5. Ejecutar en dev

### Backend

En una terminal:

```bash
cd backend
./mvnw spring-boot:run
```

En Windows:

```bash
cd backend
mvnw.cmd spring-boot:run
```

El backend queda disponible en:

```txt
http://localhost:8080
```

Flyway ejecutará las migraciones automáticamente al iniciar.

### Frontend

En otra terminal:

```bash
cd frontend
npm install
npm run dev
```

El frontend queda disponible normalmente en:

```txt
http://localhost:5173
```

Abre esa URL en el navegador.

## 6. Migraciones Flyway

Las migraciones están en:

```txt
backend/src/main/resources/db/migration/
```

## 8. Troubleshooting

### El backend no inicia porque el puerto 8080 está ocupado

```bash
lsof -i :8080
```

Puedes matar el proceso o cambiar el puerto en `application-dev.yml`.

### El frontend no inicia porque el puerto 5173 está ocupado

Vite te ofrecerá otro puerto automáticamente, o puedes detener el proceso que lo usa.

### Error de conexión a PostgreSQL

Verifica:

- Que PostgreSQL esté corriendo.
- Que la base de datos exista.
- Que el usuario y contraseña sean correctos.
- Que la URL JDBC coincida con el puerto y nombre de la base.
- Que las variables `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` estén exportadas.

### Error de CORS

El backend debe permitir peticiones desde `http://localhost:5173` en desarrollo. Revisa la configuración de Spring Security.

### Flyway checksum mismatch

Ocurre cuando modificas una migración ya aplicada. Solución:

- No editar migraciones aplicadas.
- Crear una nueva migración.
- O limpiar la base de datos en desarrollo y volver a iniciar.

### Error de versión de Node con Vite

Vite 8 requiere Node.js 20.19+ o 22.12+. Actualiza Node si ves errores raros al ejecutar `npm run dev`.

### No llegan correos

Gmail requiere una **contraseña de aplicación**, no la contraseña normal de la cuenta. Verifica `MAIL_USERNAME` y `MAIL_PASSWORD`.

## 9. Licencia

Este proyecto está bajo la licencia incluida en el archivo `LICENSE`.