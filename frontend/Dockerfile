# ═══════════════════════════════════════════════════════════════════
# ETAPA 1 — BUILD (Vite compila el bundle estático)
# ═══════════════════════════════════════════════════════════════════
FROM node:20-alpine AS build

WORKDIR /app

# Copiamos package.json e instalamos deps (capa cacheada)
COPY package*.json ./
RUN npm ci

# Copiamos el resto del código
COPY . .

# Argumento de build: la URL del backend que usará la app
ARG VITE_API_URL=/api
ENV VITE_API_URL=$VITE_API_URL

# Compilamos para producción
RUN npm run build

# ═══════════════════════════════════════════════════════════════════
# ETAPA 2 — SERVE (Nginx sirve los archivos estáticos)
# ═══════════════════════════════════════════════════════════════════
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=15s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -q -O /dev/null http://127.0.0.1/ || exit 1

CMD ["nginx", "-g", "daemon off;"]