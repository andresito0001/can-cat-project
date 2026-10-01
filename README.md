# 🐾 Can+Cat — Sistema de Gestión Veterinaria

Sistema de información fullstack para la gestión integral de una clínica veterinaria: agenda de citas, historial clínico, inventario, facturación y control de usuarios.

---

## 📋 Tabla de contenido

- [Stack tecnológico](#-stack-tecnológico)
- [Arquitectura](#-arquitectura)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Inicio rápido con Docker](#-inicio-rápido-con-docker)
- [Variables de entorno](#-variables-de-entorno)
- [Desarrollo local sin Docker](#-desarrollo-local-sin-docker)
- [Módulos funcionales](#-módulos-funcionales)
- [Datos de prueba](#-datos-de-prueba)
- [Migraciones de base de datos](#-migraciones-de-base-de-datos)
- [Notas técnicas](#-notas-técnicas)
- [Troubleshooting](#-troubleshooting)
- [Licencia](#-licencia)

---

## 🧰 Stack tecnológico

### Backend
- **Java 17**
- **Spring Boot 4** (Web, Security, Data JPA, Validation, Mail)
- **Spring Security + JWT** para autenticación
- **Flyway** para migraciones versionadas
- **PostgreSQL 15**
- **OpenPDF** para generación de facturas y récipes
- **Maven Wrapper** (`mvnw` / `mvnw.cmd`)

### Frontend
- **Vue 3** (Composition API con `<script setup>`)
- **Vite** como bundler
- **Pinia** para estado global
- **Vue Router** con guards por rol
- **Axios** para llamadas HTTP
- **Lucide Vue Next** para iconografía
- **Design system propio** con tokens CSS

### Infraestructura
- **Docker + Docker Compose**
- **Nginx** para servir el frontend y proxy inverso
- **PostgreSQL en contenedor** con volumen persistente

---

## 🏛️ Arquitectura

El proyecto sigue **Clean Architecture** en el backend, separando cada módulo en capas:

```
módulo/
├── application/       ← Casos de uso, DTOs, puertos, servicios
├── domain/            ← Entidades, excepciones, interfaces de repositorio
└── infrastructure/    ← Controllers, persistencia JPA, adapters, schedulers
```

### Módulos del backend

| Módulo | Responsabilidad |
|---|---|
| `usuarios` | Autenticación, registro, clientes, personal, roles |
| `mascotas` | Registro y gestión de mascotas |
| `citas` | Agenda, disponibilidad, transiciones de estado |
| `atenciones` | Consulta clínica, recetas, insumos, historial |
| `almacen` | Productos, proveedores, movimientos de inventario |
| `facturacion` | Facturas, pagos, verificación |
| `shared` | Servicios transversales (tasa de cambio, datos bancarios) |
| `config` | Seguridad, excepciones globales, Flyway, seeder |

### Frontend

El frontend se organiza por **módulo funcional** alineado con el backend:

```
src/
├── api/              ← Clientes Axios por módulo
├── components/       ← Componentes reutilizables (UI + específicos)
├── composables/      ← Lógica reutilizable (useToast, usePolling, etc.)
├── layouts/          ← Layouts por rol
├── router/           ← Definición de rutas con guards
├── stores/           ← Estado global (Pinia)
├── styles/           ← Design system (tokens, base, components, utilities)
├── utils/            ← Helpers y constantes
└── views/            ← Vistas por módulo funcional
```

---

## 📁 Estructura del proyecto

```
can-cat-project/
├── docker-compose.yml          # Orquestación de los 3 servicios
├── .env.example                # Plantilla de variables de entorno
├── .gitattributes              # Line endings consistentes Linux/Windows
├── .gitignore
├── README.md
├── LICENSE
│
├── backend/                    # API REST — Spring Boot
│   ├── Dockerfile              # Build multi-stage (Maven + JRE Alpine)
│   ├── .dockerignore
│   ├── pom.xml
│   ├── mvnw, mvnw.cmd
│   ├── .mvn/
│   └── src/
│       ├── main/
│       │   ├── java/com/udo/can_cat/
│       │   │   ├── almacen/            # Inventario, productos, proveedores
│       │   │   ├── atenciones/         # Consulta clínica, recetas
│       │   │   ├── citas/              # Agenda, disponibilidad
│       │   │   ├── config/             # Security, Flyway, ExceptionHandler, Seeder
│       │   │   ├── facturacion/        # Facturas, pagos
│       │   │   ├── mascotas/           # Mascotas, especies, razas
│       │   │   ├── shared/             # Tasa de cambio, datos bancarios
│       │   │   ├── usuarios/           # Auth, clientes, personal, roles
│       │   │   └── CanCatApplication.java
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       ├── application-prod.yml
│       │       └── db/migration/       # Migraciones Flyway (V1 - V16)
│       └── test/
│
└── frontend/                   # SPA — Vue 3 + Vite
    ├── Dockerfile              # Build multi-stage (Node + Nginx)
    ├── .dockerignore
    ├── nginx.conf              # SPA fallback + proxy /api
    ├── package.json
    ├── vite.config.js
    ├── index.html
    ├── public/
    └── src/
        ├── api/                # Clientes por módulo
        ├── assets/
        ├── components/
        │   ├── admin/          # ClienteFormModal, PersonalFormModal, etc.
        │   ├── almacen/        # ProductFormModal, ProveedorFormModal
        │   ├── cliente/        # PetFormModal, CitaCountdown, etc.
        │   ├── layout/         # Sidebar, TopNavbar
        │   ├── recepcion/      # ClienteDetalleModal, CobroCitaModal, etc.
        │   └── ui/             # Design system (AppButton, AppCard, etc.)
        ├── composables/        # useToast, useCountdown, usePolling
        ├── layouts/            # AppLayout + variantes por rol
        ├── router/             # Rutas con guards
        ├── stores/             # Pinia
        ├── styles/             # Design system (tokens, base, components, utilities)
        ├── utils/              # Helpers y constantes
        └── views/              # Vistas por módulo
            ├── admin/
            ├── almacen/
            ├── auth/
            ├── cliente/
            ├── recepcion/
            ├── shared/         # Vistas transversales (Mi Perfil)
            └── veterinario/
```

---

## 🚀 Inicio rápido con Docker

**Esta es la forma recomendada de levantar el proyecto.** Funciona igual en Linux, macOS y Windows.

### Requisitos

- **Docker Desktop** (Windows/macOS) o **Docker Engine + Compose plugin** (Linux)
  - Windows/macOS: https://www.docker.com/products/docker-desktop
  - Linux: `curl -fsSL https://get.docker.com | sh` y luego `sudo apt install docker-compose-plugin`
- **Git**

**Verifica:**

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

# 5. Verificar que los 3 servicios estén arriba
docker compose ps
```

Cuando los 3 servicios estén en estado `healthy`, abre:

- **Frontend:** http://localhost
- **Backend health:** http://localhost:8080/actuator/health

**Login de prueba:**
- Correo: `maria.gonzalez@email.com`
- Contraseña: `secreto123`

### Comandos útiles

```bash
# Levantar todo
docker compose up -d

# Ver estado de los servicios
docker compose ps

# Ver logs en vivo
docker compose logs -f
docker compose logs -f backend
docker compose logs --tail=50 backend

# Detener (conserva la BD)
docker compose down

# Reconstruir tras cambios de código
docker compose up -d --build backend
docker compose up -d --build frontend

# ⚠ Reset total (borra la BD y el volumen)
docker compose down -v
docker compose up -d

# Entrar a un contenedor
docker exec -it cancat-backend sh
docker exec -it cancat-postgres psql -U can_cat_user -d can-cat-system
```

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
| `JWT_SECRET` | Clave para firmar JWT (64+ caracteres) | (generar con `openssl rand -base64 64`) |
| `JWT_EXPIRATION_MS` | Duración del token en ms | `86400000` (24 h) |
| `MAIL_USERNAME` | Correo de Gmail | `tu-correo@gmail.com` |
| `MAIL_PASSWORD` | **App Password** de Gmail | `xxxx xxxx xxxx xxxx` |
| `APP_FRONTEND_URL` | URL pública para links de correo | `http://localhost` |
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring | `dev` |

### Generar un `JWT_SECRET` seguro

```bash
openssl rand -base64 64 | tr -d '\n'
```

### Generar una App Password de Gmail

1. Ve a https://myaccount.google.com/apppasswords
2. Genera una para "Correo"
3. Copia los 16 caracteres (con espacios)

> **Nota:** `MAIL_PASSWORD` **no es** la contraseña normal de tu Gmail. Debe ser una "App Password".

---

## 💻 Desarrollo local sin Docker

Para desarrollo activo con hot-reload, corre backend y frontend por separado.

### Requisitos

- Java 17+
- Node.js 20.19+ o 22.12+
- PostgreSQL 15+

### Backend

```bash
# 1. Crear la base de datos
psql -U postgres -c "CREATE DATABASE \"can-cat-system\";"
psql -U postgres -c "CREATE USER can_cat_user WITH PASSWORD 'tu_password';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE \"can-cat-system\" TO can_cat_user;"

# 2. Configurar variables de entorno
export DB_URL="jdbc:postgresql://localhost:5432/can-cat-system"
export DB_USERNAME="can_cat_user"
export DB_PASSWORD="tu_password"
export JWT_SECRET="tu_clave_jwt_larga"
export MAIL_USERNAME="tu_correo@gmail.com"
export MAIL_PASSWORD="tu_app_password"

# 3. Arrancar
cd backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend queda en: http://localhost:5173

---

## 🎯 Módulos funcionales

### 👤 Cliente
- Registro e inicio de sesión
- Gestión de mascotas (crear, editar, archivar)
- Solicitud de citas con selección de veterinario y horario
- Pago de citas online (Pago Móvil / Transferencia)
- Historial clínico de sus mascotas
- Historial de pagos con descarga de facturas
- Edición de perfil

### 🩺 Recepcionista
- Agenda de citas del día / semana / rango
- Agendar cita en mostrador con cobro presencial
- Verificación de pagos online
- Cobro de facturas pendientes
- Gestión de clientes (registro asistido)
- Registro de mascotas para clientes existentes

### 🐾 Veterinario
- Agenda personal del día
- Atención clínica completa:
  - Registro de anamnesis, signos vitales, diagnóstico
  - Aplicación de insumos (descuenta stock)
  - Generación de récipes (con PDF descargable)
- Historial clínico de pacientes

### 📦 Encargado de Almacén
- Catálogo de productos con alertas de stock
- Registro de entradas de mercancía con múltiples líneas
- Gestión de proveedores
- Dashboard con KPIs y movimientos recientes

### ⚙️ Administrador
- Dashboard con gráficas y estado del sistema
- Gestión de personal (crear, editar, activar/desactivar)
- Directorio de usuarios con filtros por rol y estado
- Consulta de roles y permisos
- Reportes y métricas
- Configuración del sistema

### 🔐 Mi Perfil (transversal)
- Editable para Clientes (teléfono, dirección, ciudad)
- Solo lectura para Personal (info profesional)
- Cambio de contraseña con verificación de la actual

---

## 🧪 Datos de prueba

En perfil `dev`, el `DevDataSeeder` crea automáticamente:

### Veterinarios (contraseña: `secreto123`)

| Correo | Nombre | Especialidad | Horario |
|---|---|---|---|
| `ana.perez@cancat.com` | Dra. Ana Pérez | Consulta | L-V 08-12, 14-18 |
| `carlos.mendez@cancat.com` | Dr. Carlos Méndez | Cirugía | L-V 09-13, 15-19 |
| `lucia.fernandez@cancat.com` | Dra. Lucía Fernández | Vacunación | M-S 10-14, 16-20 |
| `roberto.silva@cancat.com` | Dr. Roberto Silva | Estética | L-V 08-12, 13-17 |

### Clientes (contraseña: `secreto123`)

| Correo | Nombre | Mascotas |
|---|---|---|
| `maria.gonzalez@email.com` | María González | Rocky, Luna |
| `pedro.ramirez@email.com` | Pedro Ramírez | Max |
| `carmen.silva@email.com` | Carmen Silva | Bella |
| `jose.martinez@email.com` | José Martínez | Toby |
| `luisa.torres@email.com` | Luisa Torres | Michi |

### Administrador (del seed inicial)

- `admin@cancat.com`

---

## 🗄️ Migraciones de base de datos

Flyway aplica migraciones automáticamente al arrancar el backend. Los archivos están en:

```
backend/src/main/resources/db/migration/
```

### Migraciones actuales

| Versión | Descripción |
|---|---|
| `V1` | Schema inicial completo |
| `V2` | Tokens de recuperación de contraseña |
| `V3` | Servicios y alter de citas |
| `V4` | Nombre en personal |
| `V5` | `created_at` en estado_cita |
| `V6` | Check de especialidad |
| `V7` | Atención clínica, recetas, insumos |
| `V8` | Seed de admin |
| `V9` | Expiración de reservas |
| `V10` | Índice único parcial de slots |
| `V11` | Simplificar estados de cita |
| `V12` | Factura de proveedor |
| `V13` | Seed de proveedores |
| `V14` | Completar costos de productos |
| `V15` | `updated_at` en proveedor |
| `V16` | Personal admin |

> **Regla de oro:** no editar migraciones ya aplicadas. Crear una nueva `V17__descripcion.sql`.

---

## 🛠️ Notas técnicas

### Line endings (Windows ↔ Linux)

El archivo `.gitattributes` fuerza `LF` en todos los archivos que van a contenedores Linux. En Windows, configura antes de clonar:

```bash
git config --global core.autocrlf false
git config --global core.eol lf
```

### Antivirus en Windows

Docker Desktop escribe muchos archivos temporales. Sin exclusiones, el build puede tardar 10-15 min en lugar de 3.

**Excluir de Windows Defender:**
- `C:\Users\<TuUsuario>\can-cat-project`
- `C:\Users\<TuUsuario>\AppData\Local\Docker`
- `C:\Program Files\Docker`

### Zona horaria

El backend fuerza `America/Caracas` vía `TimeZoneConfig` para que `LocalDate.now()` y `LocalTime.now()` coincidan con la hora real del usuario, sin importar dónde corra el contenedor.

### Frontend "unhealthy"

Si el frontend aparece `unhealthy` pero la app carga bien en el navegador, es un falso positivo del healthcheck. **No bloquea nada.**

### Expiración de reservas

Las citas en estado `Pendiente_Pago` expiran a los **15 minutos**. Hay dos mecanismos:
- **Frontend:** el contador dispara `POST /api/citas/{id}/cancelar-expirada` al llegar a cero
- **Backend:** un `@Scheduled` job corre cada minuto y cancela las expiradas huérfanas

### Tasa de cambio

El backend consulta `ve.dolarapi.com` para obtener la tasa oficial USD→VES, con caché de 60 minutos. Si la API falla, usa la caché previa o un fallback.

---

## 🚨 Troubleshooting

### Puerto ocupado (`address already in use`)

Edita `.env` y cambia el puerto:

```
DB_PORT_EXPOSED=5433       # PostgreSQL (si tienes uno local en 5432)
BACKEND_PORT_EXPOSED=8081
FRONTEND_PORT_EXPOSED=3000
```

### `password authentication failed for user "can_cat_user"`

Cambiaste `DB_PASSWORD` en `.env` pero el volumen de Postgres tiene la contraseña vieja:

```bash
docker compose down -v   # ⚠ borra la BD
docker compose up -d
```

### Backend se reinicia en loop

```bash
docker compose logs --tail=50 backend
```

Busca `ERROR`, `Caused by`, `FATAL`. Causas típicas:
- Variables faltantes en `.env`
- `JWT_SECRET` muy corto (mínimo 64 caracteres)
- Contraseña de BD incorrecta

### `Connection refused` al backend desde el frontend

Verifica que nginx está haciendo el proxy:

```bash
docker compose exec frontend wget -qO- http://backend:8080/actuator/health
```

Debe responder `{"status":"UP"}`.

### Flyway checksum mismatch

Ocurre cuando se edita una migración ya aplicada. **No lo hagas.** Crea una nueva migración.

### No llegan correos

Gmail requiere **App Password**, no la contraseña normal. Verifica `MAIL_USERNAME` y `MAIL_PASSWORD` en `.env`.

### Node.js versión incorrecta con Vite

Vite 8 requiere Node.js **20.19+** o **22.12+**. Node 18 falla.

---

## 📄 Licencia

MIT — ver [LICENSE](LICENSE).