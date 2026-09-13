#!/usr/bin/env bash
# ═══════════════════════════════════════════════════════════════════════════
#  seed_pruebas.sh
#  Semillas de datos + smoke test del módulo "Gestión de Clientes" (CU 4.6.1.10)
#
#  USO:
#    chmod +x seed_pruebas.sh
#    ./seed_pruebas.sh                  # crea datos de prueba + corre smoke test
#    DB_PASSWORD=mipass ./seed_pruebas.sh   # override de credenciales de BD
#    ./seed_pruebas.sh --clean          # elimina TODOS los datos de prueba
#
#  REQUISITOS: curl (obligatorio) · psql (fase recepcionista) · jq (opcional)
# ═══════════════════════════════════════════════════════════════════════════
set -uo pipefail

# ─── CONFIGURACIÓN (ajusta a tu entorno) ────────────────────────────────
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-can-cat-system}"
DB_USER="${DB_USER:-dante}"
DB_PASSWORD="${DB_PASSWORD:-Minina.25}"
CONTRASENA_PRUEBA="secreto123"   # se propaga sola a todos los JSON (helper abajo)
# NOTA: si cambias la contraseña, evita los caracteres / y & (interfieren con sed)

# ─── COLORES / CONTADORES ────────────────────────────────────────────────
GREEN=$'\033[32m'; RED=$'\033[31m'; BLUE=$'\033[34m'; YELL=$'\033[33m'
BOLD=$'\033[1m';  NC=$'\033[0m'
PASS=0; FAIL=0
BODY_FILE="$(mktemp)"; trap 'rm -f "$BODY_FILE"' EXIT

section() { printf '\n%s\n' "${BOLD}═════════ $* ═════════${NC}"; }
info()    { printf '%s\n' "${BLUE}[INFO]${NC} $*"; }
ok()      { printf '%s\n' "${GREEN}[ OK ]${NC} $*"; PASS=$((PASS+1)); }
bad()     { printf '%s\n' "${RED}[FAIL]${NC} $*"; FAIL=$((FAIL+1)); }
aviso()   { printf '%s\n' "${YELL}[AVISO]${NC} $*"; }

# ─── HELPERS ─────────────────────────────────────────────────────────────
# http_request METODO RUTA [JSON_BODY] [TOKEN]
#   → deja el status HTTP en $HTTP_STATUS y el cuerpo en $HTTP_BODY
http_request() {
  local metodo="$1" ruta="$2" datos="${3:-}" token="${4:-}"
  local args=(-s -o "$BODY_FILE" -w '%{http_code}' --max-time 20
              -X "$metodo" -H 'Content-Type: application/json'
              "$BACKEND_URL$ruta")
  [[ -n "$token" ]] && args+=(-H "Authorization: Bearer $token")
  [[ -n "$datos" ]] && args+=(-d "$datos")
  HTTP_STATUS="$(curl "${args[@]}" 2>/dev/null || echo 000)"
  HTTP_BODY="$(cat "$BODY_FILE" 2>/dev/null || true)"
}

# Reemplaza el placeholder __PASS__ de los JSON por la contraseña real
con_pass() { sed "s/__PASS__/${CONTRASENA_PRUEBA}/" <<<"$1"; }

extraer_token() {
  if command -v jq >/dev/null 2>&1; then
    jq -r '.accessToken // empty' <<<"$HTTP_BODY" 2>/dev/null
  else
    sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p' <<<"$HTTP_BODY"
  fi
}

run_sql() {  # run_sql "SQL..." → código de salida de psql
  PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" \
    -d "$DB_NAME" -q -c "$1" >/dev/null 2>&1
}

# ─── MODO LIMPIEZA ───────────────────────────────────────────────────────
if [[ "${1:-}" == "--clean" ]]; then
  section "LIMPIANDO DATOS DE PRUEBA (*@test.cog)"
  if run_sql "DELETE FROM usuario WHERE correo_electronico LIKE '%@test.cog';"; then
    ok "Eliminados (sus clientes se borran solos por ON DELETE CASCADE)"
  else
    bad "No se pudo conectar a PostgreSQL. Ejecuta a mano:"
    echo "  DELETE FROM usuario WHERE correo_electronico LIKE '%@test.cog';"
  fi
  exit 0
fi

# ═════════════════════════════════════════════════════════════════════════
#  FASE 0 · Backend en línea
# ═════════════════════════════════════════════════════════════════════════
section "FASE 0 · Verificando backend en $BACKEND_URL"
http_request POST "/api/auth/login" '{}'
if [[ "$HTTP_STATUS" == "000" ]]; then
  bad "El backend no responde. Levántalo primero (./mvnw spring-boot:run) y re-ejecuta."
  exit 1
fi
ok "Backend en línea (health-check respondió HTTP $HTTP_STATUS)"

# ═════════════════════════════════════════════════════════════════════════
#  FASE 1 · Clientes de prueba (vía registro público)
# ═════════════════════════════════════════════════════════════════════════
section "FASE 1 · Creando clientes de prueba"

CLIENTES=(
'{"nombreCompleto":"María Alejandra González","documentoIdentidad":"V-12345678","correoElectronico":"maria.gonzalez@test.cog","contrasena":"__PASS__","telefonoPrincipal":"0412-1234567","telefonoSecundario":"0212-5550134","direccion":"Av. Principal, Casa 5, Altavista","ciudad":"Barcelona","fechaNacimiento":"1990-05-14"}'
'{"nombreCompleto":"José Manuel Rodríguez","documentoIdentidad":"V-23456789","correoElectronico":"jose.rodriguez@test.cog","contrasena":"__PASS__","telefonoPrincipal":"0414-2345678","direccion":"Calle 5 de Julio, Edif. 3","ciudad":"Puerto La Cruz","fechaNacimiento":"1985-11-02"}'
'{"nombreCompleto":"Ana Belén Martínez","documentoIdentidad":"V-34567890","correoElectronico":"ana.martinez@test.cog","contrasena":"__PASS__","telefonoPrincipal":"0424-3456789","ciudad":"Barcelona","fechaNacimiento":"1992-01-25"}'
'{"nombreCompleto":"Carlos Enrique Pérez","documentoIdentidad":"V-45678901","correoElectronico":"carlos.perez@test.cog","contrasena":"__PASS__","telefonoPrincipal":"0412-4567890","direccion":"Av. Bolívar, Torre B, Piso 4","ciudad":"Lechería"}'
'{"nombreCompleto":"Luisa Fernanda Bolívar","documentoIdentidad":"E-56789012","correoElectronico":"luisa.bolivar@test.cog","contrasena":"__PASS__","telefonoPrincipal":"0416-5678901","ciudad":"Barcelona","fechaNacimiento":"1978-07-09"}'
'{"nombreCompleto":"Pedro Alejandro Sánchez","documentoIdentidad":"V-67890123","correoElectronico":"pedro.sanchez@test.cog","contrasena":"__PASS__","telefonoPrincipal":"0426-6789012","direccion":"Sector El Mangle, Calle 8","ciudad":"Puerto La Cruz"}'
)

for i in "${!CLIENTES[@]}"; do
  http_request POST "/api/auth/registro" "$(con_pass "${CLIENTES[$i]}")"
  case "$HTTP_STATUS" in
    201) ok "Cliente $((i+1))/${#CLIENTES[@]} creado" ;;
    409) info "Cliente $((i+1)) ya existía (re-run, idempotente)" ;;
    *)   bad "Cliente $((i+1)) → $HTTP_STATUS: $HTTP_BODY" ;;
  esac
done

# ═════════════════════════════════════════════════════════════════════════
#  FASE 2 · Recepcionista (registro + rol por SQL)
# ═════════════════════════════════════════════════════════════════════════
section "FASE 2 · Creando RECEPCIONISTA"

http_request POST "/api/auth/registro" "$(con_pass '{"nombreCompleto":"Personal Recepción","documentoIdentidad":"V-99900001","correoElectronico":"recepcion@test.cog","contrasena":"__PASS__","telefonoPrincipal":"0412-9990001","direccion":"Mostrador principal"}')"
case "$HTTP_STATUS" in
  201) ok "Usuario base del recepcionista creado" ;;
  409) info "Usuario base ya existía (re-run)" ;;
  *)   bad "No se pudo crear el usuario base → $HTTP_STATUS: $HTTP_BODY" ;;
esac

SQL_ROL="INSERT INTO rol (nombre_rol, descripcion)
SELECT 'Recepcionista', 'Personal de recepción (mostrador y caja)'
WHERE NOT EXISTS (SELECT 1 FROM rol WHERE nombre_rol = 'Recepcionista');

UPDATE usuario
SET id_rol = (SELECT id_rol FROM rol WHERE nombre_rol = 'Recepcionista')
WHERE correo_electronico = 'recepcion@test.cog';"

if ! command -v psql >/dev/null 2>&1; then
  bad "psql no está instalado. Instala postgresql-client, o ejecuta este SQL a mano y re-corre el script:"
  printf '%s\n' "$SQL_ROL"
  exit 1
fi

if run_sql "$SQL_ROL"; then
  ok "Rol 'Recepcionista' asegurado y asignado a recepcion@test.cog"
  aviso "El recepcionista conserva su perfil de cliente (verás 'Personal Recepción' en el listado). Es intencional: el login solo usa el ROL para permisos, y así el navbar muestra su nombre."
else
  bad "psql falló — revisa DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD. SQL manual:"
  printf '%s\n' "$SQL_ROL"
  exit 1
fi

# ═════════════════════════════════════════════════════════════════════════
#  FASE 3 · Login del recepcionista
# ═════════════════════════════════════════════════════════════════════════
section "FASE 3 · Login del recepcionista"

http_request POST "/api/auth/login" "$(con_pass '{"correoElectronico":"recepcion@test.cog","contrasena":"__PASS__"}')"
TOKEN_RECEP="$(extraer_token)"

if [[ "$HTTP_STATUS" == "200" && -n "$TOKEN_RECEP" ]] && grep -q 'Recepcionista' <<<"$HTTP_BODY"; then
  ok "Login OK — el JWT llega con rol 'Recepcionista'"
else
  bad "Login falló ($HTTP_STATUS): $HTTP_BODY"
  info "Si cambiaste CONTRASENA_PRUEBA entre ejecuciones, corre ./seed_pruebas.sh --clean y re-intenta."
  exit 1
fi

# ═════════════════════════════════════════════════════════════════════════
#  FASE 4 · Smoke test del módulo (CU 4.6.1.10)
# ═════════════════════════════════════════════════════════════════════════
section "FASE 4 · Smoke test — Gestión de Clientes"

# 4.1 Listado completo
http_request GET "/api/clientes" "" "$TOKEN_RECEP"
if [[ "$HTTP_STATUS" == "200" ]]; then
  TOTAL=$(grep -o '"id":' <<<"$HTTP_BODY" | wc -l | tr -d ' ')
  ok "GET /api/clientes → 200 (listado con $TOTAL clientes)"
else
  bad "GET /api/clientes → $HTTP_STATUS: $HTTP_BODY"
fi

# 4.2 Búsqueda por filtro
http_request GET "/api/clientes?filtro=gonz" "" "$TOKEN_RECEP"
if [[ "$HTTP_STATUS" == "200" ]] && grep -q 'González' <<<"$HTTP_BODY"; then
  ok "GET /api/clientes?filtro=gonz → 200 y encuentra a María González"
else
  bad "Búsqueda por filtro → $HTTP_STATUS: $HTTP_BODY"
fi

# 4.3 CU 4.6.1.10 · Flujo normal: registro asistido
E2E_JSON='{"nombreCompleto":"Prueba E2E Script","documentoIdentidad":"V-77700001","correoElectronico":"prueba.e2e@test.cog","telefonoPrincipal":"0414-7770001","direccion":"Calle Falsa 123","ciudad":"Barcelona"}'
http_request POST "/api/clientes/registro-asistido" "$E2E_JSON" "$TOKEN_RECEP"
if [[ "$HTTP_STATUS" == "201" ]]; then
  ok "POST /clientes/registro-asistido → 201 (flujo normal del CU)"
  info "Body: $(head -c 300 <<<"$HTTP_BODY")"
elif [[ "$HTTP_STATUS" == "409" ]]; then
  aviso "El cliente E2E ya existía (re-run). El test de duplicado (4.4) sigue siendo válido."
else
  bad "Registro asistido → $HTTP_STATUS: $HTTP_BODY"
fi

# 4.4 CU 4.6.1.10 · Flujo alterno 2: documento duplicado
http_request POST "/api/clientes/registro-asistido" "$E2E_JSON" "$TOKEN_RECEP"
if [[ "$HTTP_STATUS" == "409" ]] && grep -q 'DOCUMENTO_DUPLICADO' <<<"$HTTP_BODY"; then
  ok "Documento duplicado → 409 + DOCUMENTO_DUPLICADO (flujo alterno 2)"
else
  bad "Esperaba 409 DOCUMENTO_DUPLICADO → llegó $HTTP_STATUS: $HTTP_BODY"
fi

# 4.5 Correo duplicado
http_request POST "/api/clientes/registro-asistido" '{"nombreCompleto":"Otro Nombre","documentoIdentidad":"V-77700002","correoElectronico":"prueba.e2e@test.cog","telefonoPrincipal":"0414-7770002","direccion":"Calle 456"}' "$TOKEN_RECEP"
if [[ "$HTTP_STATUS" == "409" ]] && grep -q 'CORREO_DUPLICADO' <<<"$HTTP_BODY"; then
  ok "Correo duplicado → 409 + CORREO_DUPLICADO"
else
  bad "Esperaba 409 CORREO_DUPLICADO → llegó $HTTP_STATUS: $HTTP_BODY"
fi

# 4.6 CU 4.6.1.10 · Flujo alterno 3: datos inválidos
http_request POST "/api/clientes/registro-asistido" '{"nombreCompleto":"Mal Formado","documentoIdentidad":"V-1","correoElectronico":"malformado","telefonoPrincipal":"123","direccion":""}' "$TOKEN_RECEP"
if [[ "$HTTP_STATUS" == "400" ]]; then
  ok "Datos inválidos/omitidos → 400 (Jakarta Validation, flujo alterno 3)"
else
  bad "Esperaba 400 con datos inválidos → llegó $HTTP_STATUS: $HTTP_BODY"
fi

# 4.7 Seguridad por roles: un CLIENTE no puede listar
http_request POST "/api/auth/login" "$(con_pass '{"correoElectronico":"maria.gonzalez@test.cog","contrasena":"__PASS__"}')"
TOKEN_CLIENTE="$(extraer_token)"
if [[ -n "$TOKEN_CLIENTE" ]]; then
  http_request GET "/api/clientes" "" "$TOKEN_CLIENTE"
  if [[ "$HTTP_STATUS" == "403" ]]; then
    ok "Seguridad por roles: un CLIENTE recibe 403 al listar clientes"
  else
    bad "Se esperaba 403 para rol Cliente → llegó $HTTP_STATUS (¿falta @EnableMethodSecurity o @PreAuthorize?)"
  fi
else
  bad "No se pudo loguear el cliente de prueba para el test de seguridad"
fi

# ═════════════════════════════════════════════════════════════════════════
#  RESUMEN
# ═════════════════════════════════════════════════════════════════════════
section "RESUMEN"
printf '%s\n' "  Pasadas: ${GREEN}${PASS}${NC}   |   Fallidas: ${RED}${FAIL}${NC}"
cat <<EOF

 ${BOLD}CREDENCIALES PARA PROBAR EN EL FRONTEND${NC}
  Recepcionista →  recepcion@test.cog       /  ${CONTRASENA_PRUEBA}
  Cliente        →  maria.gonzalez@test.cog /  ${CONTRASENA_PRUEBA}

  Datos marcados con *@test.cog → bórralos con:  ./seed_pruebas.sh --clean
EOF

[[ $FAIL -gt 0 ]] && exit 1
exit 0