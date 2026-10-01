# 🐾 Can+Cat — Sistema de Gestión Veterinaria

Sistema fullstack para la gestión integral de una clínica veterinaria: agenda de citas, historial clínico, inventario, facturación y control de usuarios.

---

## 🧰 Tecnologías

### Backend
- **Java 17**
- **Spring Boot 4** — Web, Security, Data JPA, Validation, Mail
- **Spring Security + JWT** — autenticación
- **Flyway** — migraciones versionadas
- **PostgreSQL 15**
- **OpenPDF** — generación de PDFs (facturas, récipes)
- **Maven Wrapper**

### Frontend
- **Vue 3** (Composition API)
- **Vite** — bundler
- **Pinia** — estado global
- **Vue Router** — ruteo con guards por rol
- **Axios** — cliente HTTP
- **Lucide Vue Next** — iconografía

### Infraestructura
- **Docker + Docker Compose**
- **Nginx** — sirve el frontend y hace de proxy inverso

---

## 🏛️ Cómo está construido

### Backend — Clean Architecture

Cada módulo funcional se divide en 3 capas:

- **`application/`** — Casos de uso, DTOs, puertos y servicios
- **`domain/`** — Entidades, excepciones e interfaces de repositorio
- **`infrastructure/`** — Controllers, persistencia JPA, adapters y schedulers

**Módulos:** `usuarios`, `mascotas`, `citas`, `atenciones`, `almacen`, `facturacion`, `shared`, `config`.

### Frontend — Vue 3 moderno

- **Composición con `<script setup>`** en todos los componentes
- **Design system propio** basado en tokens CSS (`tokens.css`, `base.css`, `components.css`, `utilities.css`)
- **Componentes UI reutilizables** (`AppButton`, `AppCard`, `AppModal`, `AppInput`, etc.)
- **Layouts por rol** que comparten `AppLayout` con sidebar colapsable y tema cromático según el rol
- **Guards de ruta** que validan autenticación y rol

---

## 🚀 Compilación y despliegue con Docker

**Forma recomendada.** Funciona igual en Linux, macOS y Windows.

### Requisitos

- **Docker Desktop** (Windows/macOS) o **Docker Engine + Compose plugin** (Linux)
- **Git**

```bash
docker --version          # 24.x o superior
docker compose version    # v2.x o superior
```

### Pasos

```bash
# 1. Clonar el repositorio
git clone <url-del-repo>
cd can-cat-project

# 2. Crear el archivo de configuración
cp .env.example .env

# 3. Editar .env con tus valores (ver sección "Variables de entorno")
nano .env

# 4. Levantar todo
docker compose up -d

# 5. Verificar
docker compose ps
```

Cuando los 3 servicios estén `healthy`:

- **Frontend:** http://localhost
- **Backend:** http://localhost:8080/actuator/health

**Login de prueba:**
- Correo: `maria.gonzalez@email.com`
- Contraseña: `secreto123`

### Comandos útiles

```bash
docker compose up -d                     # Levantar
docker compose ps                        # Ver estado
docker compose logs -f backend           # Logs en vivo
docker compose down                      # Detener (conserva BD)
docker compose up -d --build backend     # Reconstruir backend
docker compose down -v                   # ⚠ Reset total (borra BD)
```

---

## 💻 Compilación local (sin Docker)

### Backend

**Requisitos:** Java 17+, PostgreSQL 15+

```bash
# 1. Crear la base de datos
psql -U postgres -c "CREATE DATABASE \"can-cat-system\";"
psql -U postgres -c "CREATE USER can_cat_user WITH PASSWORD 'tu_password';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE \"can-cat-system\" TO can_cat_user;"

# 2. Variables de entorno
export DB_URL="jdbc:postgresql://localhost:5432/can-cat-system"
export DB_USERNAME="can_cat_user"
export DB_PASSWORD="tu_password"
export JWT_SECRET="$(openssl rand -base64 64 | tr -d '\n')"
export MAIL_USERNAME="tu_correo@gmail.com"
export MAIL_PASSWORD="tu_app_password"

# 3. Arrancar
cd backend
./mvnw spring-boot:run
```

Backend queda en http://localhost:8080.

### Frontend

**Requisitos:** Node.js 20.19+ o 22.12+

```bash
cd frontend
npm install
npm run dev
```

Frontend queda en http://localhost:5173.

---

## 🔧 Variables de entorno

Todas las variables se configuran en `.env` (copia de `.env.example`):

| Variable | Descripción | Ejemplo |
|---|---|---|
| `DB_NAME` | Nombre de la base de datos | `can-cat-system` |
| `DB_USERNAME` | Usuario de Postgres | `can_cat_user` |
| `DB_PASSWORD` | Contraseña de Postgres | (elegir una segura) |
| `DB_PORT_EXPOSED` | Puerto del host para Postgres | `5432` o `5433` si está ocupado |
| `BACKEND_PORT_EXPOSED` | Puerto del backend | `8080` |
| `FRONTEND_PORT_EXPOSED` | Puerto del frontend | `80` |
| `JWT_SECRET` | Clave para firmar JWT (64+ caracteres) | `openssl rand -base64 64` |
| `JWT_EXPIRATION_MS` | Duración del token en ms | `86400000` (24 h) |
| `MAIL_USERNAME` | Correo de Gmail | `tu-correo@gmail.com` |
| `MAIL_PASSWORD` | **App Password** de Gmail | `xxxx xxxx xxxx xxxx` |
| `APP_FRONTEND_URL` | URL pública para links de correo | `http://localhost` |

> **Nota:** `MAIL_PASSWORD` **no es** la contraseña de tu Gmail. Debe ser una **App Password** generada en https://myaccount.google.com/apppasswords.

---

## 🧪 Datos de prueba

Al arrancar en perfil `dev`, se crean automáticamente:

**Veterinarios** (contraseña: `secreto123`)
- `ana.perez@cancat.com` — Consulta
- `carlos.mendez@cancat.com` — Cirugía
- `lucia.fernandez@cancat.com` — Vacunación
- `roberto.silva@cancat.com` — Estética

**Clientes** (contraseña: `secreto123`)
- `maria.gonzalez@email.com` — Rocky, Luna
- `pedro.ramirez@email.com` — Max
- `carmen.silva@email.com` — Bella
- `jose.martinez@email.com` — Toby
- `luisa.torres@email.com` — Michi

**Administrador**
- `admin@cancat.com`

---

## 🛠️ Notas técnicas

- **Line endings (Windows):** el archivo `.gitattributes` fuerza `LF` en todos los archivos. Antes de clonar en Windows: `git config --global core.autocrlf false`.
- **Antivirus Windows:** excluir `can-cat-project`, `AppData\Local\Docker` y `Program Files\Docker` para que el build no tarde 10 minutos.
- **Zona horaria:** el backend fuerza `America/Caracas` vía `TimeZoneConfig`.
- **Migraciones:** gestionadas por Flyway en `backend/src/main/resources/db/migration/`. **Nunca editar una migración ya aplicada**, crear una nueva.
- **Expiración de reservas:** citas en `Pendiente_Pago` expiran a los 15 minutos (frontend + job de backend).

---

## 🚨 Troubleshooting

**Puerto ocupado (`address already in use`)** → editar `.env` y cambiar `DB_PORT_EXPOSED`, `BACKEND_PORT_EXPOSED` o `FRONTEND_PORT_EXPOSED`.

**`password authentication failed for user "can_cat_user"`** → cambiaste `DB_PASSWORD` pero el volumen tiene la vieja:
```bash
docker compose down -v && docker compose up -d
```

**Backend se reinicia en loop** → `docker compose logs --tail=50 backend` y buscar `ERROR` o `Caused by`.

**Flyway checksum mismatch** → no editar migraciones aplicadas. Crear una nueva.

**No llegan correos** → verificar que `MAIL_PASSWORD` sea una App Password de Gmail, no la contraseña normal.

**Node.js versión incorrecta** → Vite 8 requiere Node 20.19+ o 22.12+.

---

## 📄 Licencia

MIT — ver [LICENSE](LICENSE).