# ═══════════════════════════════════════════════════════════════════
# ETAPA 1 — BUILD (compila con Maven)
# ═══════════════════════════════════════════════════════════════════
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos solo lo necesario para resolver dependencias primero
# (cachea esta capa si no cambian las deps)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Descargamos dependencias (capa cacheada)
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y compilamos
COPY src ./src
RUN mvn clean package -DskipTests -B

# ═══════════════════════════════════════════════════════════════════
# ETAPA 2 — RUNTIME (imagen ligera solo con JRE)
# ═══════════════════════════════════════════════════════════════════
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Zona horaria Venezuela
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/America/Caracas /etc/localtime && \
    echo "America/Caracas" > /etc/timezone

# Usuario no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Copiamos el JAR generado
COPY --from=build /app/target/*.jar app.jar

RUN chown -R spring:spring /app
USER spring

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
