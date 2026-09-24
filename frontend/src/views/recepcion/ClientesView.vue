<template>
  <div class="gestion-clientes">
    <!-- ═══ HERO ═══ -->
    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow">
          <Users :size="12" />
          Recepción · Clientes
        </p>
        <h1>Gestión de Clientes</h1>
        <p class="hero-sub">Registro, búsqueda y administración de clientes de la clínica.</p>
      </div>
      <div class="hero-right">
        <button class="btn-primary" type="button" @click="router.push('/recepcion/clientes/nuevo')">
          <UserPlus :size="16" />
          Añadir nuevo cliente
        </button>
      </div>
    </header>

    <!-- ═══ KPIs ═══ -->
    <section v-if="!isLoading && clientes.length" class="kpis">
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #0F766E; --kpi-bg: #F0FDFA;">
          <Users :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ clientes.length }}</p>
          <p class="kpi-label">
            {{ filtro ? 'Resultados' : 'Clientes registrados' }}
          </p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #3B82F6; --kpi-bg: #EFF6FF;">
          <MapPin :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ conCiudad }}</p>
          <p class="kpi-label">Con ciudad</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #F59E0B; --kpi-bg: #FFFBEB;">
          <MapPinOff :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ sinCiudad }}</p>
          <p class="kpi-label">Sin ciudad</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #8B5CF6; --kpi-bg: #F5F3FF;">
          <CalendarPlus :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ nuevosEsteMes }}</p>
          <p class="kpi-label">Nuevos este mes</p>
        </div>
      </article>
    </section>

    <!-- ═══ TOOLBAR ═══ -->
    <section class="toolbar">
      <div class="search-box">
        <Search :size="15" class="search-icon" />
        <input
          v-model="filtro"
          type="text"
          class="search-input"
          placeholder="Buscar por nombre o documento de identidad…"
          aria-label="Buscar cliente"
        />
        <button
          v-if="filtro"
          type="button"
          class="search-clear"
          aria-label="Limpiar búsqueda"
          @click="limpiarFiltro"
        >
          <X :size="13" />
        </button>
      </div>
      <span class="counter">
        {{ clientes.length }} {{ clientes.length === 1 ? 'cliente' : 'clientes' }}
        <template v-if="filtro"> con "{{ filtro }}"</template>
      </span>
    </section>

    <!-- ═══ ALERTA ═══ -->
    <div v-if="errorMessage" class="alert alert-error">
      <AlertCircle :size="16" />
      <span>{{ errorMessage }}</span>
      <button type="button" class="alert-action" @click="cargarClientes">Reintentar</button>
    </div>

    <!-- ═══ SKELETON ═══ -->
    <div v-if="isLoading" class="table-card">
      <div class="skeleton-head">
        <div class="skeleton-col w-30" />
        <div class="skeleton-col w-20" />
        <div class="skeleton-col w-15" />
        <div class="skeleton-col w-15" />
        <div class="skeleton-col w-15" />
      </div>
      <div v-for="i in 5" :key="i" class="skeleton-row">
        <div class="skeleton-cell w-30" />
        <div class="skeleton-cell w-20" />
        <div class="skeleton-cell w-15" />
        <div class="skeleton-cell w-15" />
        <div class="skeleton-cell w-15" />
      </div>
    </div>

    <!-- ═══ VACÍO ═══ -->
    <div v-else-if="clientes.length === 0" class="empty-state">
      <div class="empty-icon">
        <component :is="filtro ? Search : Users" :size="32" />
      </div>
      <template v-if="filtro">
        <h3>Sin resultados</h3>
        <p>No hay clientes que coincidan con <strong>"{{ filtro }}"</strong>.</p>
        <button type="button" class="btn-link" @click="limpiarFiltro">
          Limpiar búsqueda
        </button>
      </template>
      <template v-else>
        <h3>Aún no hay clientes registrados</h3>
        <p>Comienza registrando al primer cliente para agendar citas y gestionar mascotas.</p>
        <button class="btn-primary" type="button" @click="router.push('/recepcion/clientes/nuevo')">
          <UserPlus :size="16" /> Añadir nuevo cliente
        </button>
      </template>
    </div>

    <!-- ═══ TABLA ═══ -->
    <div v-else class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>Cliente</th>
            <th>Documento</th>
            <th>Teléfono</th>
            <th>Ciudad</th>
            <th>Registrado</th>
            <th class="col-accion" aria-label="Acciones"></th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="cliente in clientes"
            :key="cliente.id"
            class="data-row"
            @click="verCliente(cliente)"
          >
            <td class="cell-cliente">
              <div class="cliente-cell">
                <div
                  class="cliente-avatar"
                  :style="{ backgroundColor: colorAvatar(cliente.nombreCompleto) }"
                >
                  {{ inicialesCliente(cliente.nombreCompleto) }}
                </div>
                <div class="cliente-info">
                  <p class="cliente-nombre">{{ cliente.nombreCompleto }}</p>
                  <p class="cliente-id">ID #{{ cliente.id }}</p>
                </div>
              </div>
            </td>
            <td>
              <span class="cell-mono">{{ cliente.documentoIdentidad }}</span>
            </td>
            <td>
              <span class="cell-icon">
                <Phone :size="13" />
                {{ cliente.telefonoPrincipal }}
              </span>
            </td>
            <td>
              <span v-if="cliente.ciudad" class="ciudad-pill">
                <MapPin :size="11" />
                {{ cliente.ciudad }}
              </span>
              <span v-else class="cell-empty">—</span>
            </td>
            <td>
              <span class="cell-date">{{ formatearFecha(cliente.createdAt) }}</span>
            </td>
            <td class="cell-accion">
              <ChevronRight :size="16" class="row-arrow" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import * as clientesApi from '@/api/clientes.api'
import {
  UserPlus, Users, Search, X, AlertCircle, ChevronRight,
  Phone, MapPin, MapPinOff, CalendarPlus
} from 'lucide-vue-next'

const router = useRouter()

const clientes = ref([])
const filtro = ref('')
const isLoading = ref(false)
const errorMessage = ref(null)

const PALETA_AVATARES = ['#0F766E', '#3B82F6', '#F59E0B', '#F43F5E', '#8B5CF6', '#0EA5E9']

let debounceTimer = null

// ─── KPIs ───
const conCiudad = computed(() => clientes.value.filter(c => c.ciudad?.trim()).length)
const sinCiudad = computed(() => clientes.value.length - conCiudad.value)
const nuevosEsteMes = computed(() => {
  const ahora = new Date()
  const y = ahora.getFullYear()
  const m = ahora.getMonth()
  return clientes.value.filter(c => {
    if (!c.createdAt) return false
    const d = new Date(c.createdAt)
    return d.getFullYear() === y && d.getMonth() === m
  }).length
})

// ─── Helpers ───
function colorAvatar(nombre) {
  let hash = 0
  for (const ch of String(nombre || '')) hash = (hash * 31 + ch.charCodeAt(0)) % 997
  return PALETA_AVATARES[hash % PALETA_AVATARES.length]
}
function inicialesCliente(nombre) {
  const words = String(nombre || '?').trim().split(/\s+/).filter(Boolean)
  if (!words.length) return '?'
  if (words.length === 1) return words[0].charAt(0).toUpperCase()
  return (words[0].charAt(0) + words[words.length - 1].charAt(0)).toUpperCase()
}
function formatearFecha(fecha) {
  if (!fecha) return '—'
  return new Date(fecha).toLocaleDateString('es-VE', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  })
}
function limpiarFiltro() {
  filtro.value = ''
}
function verCliente(cliente) {
  // Ajusta la ruta cuando exista el detalle
  router.push(`/recepcion/clientes/${cliente.id}`)
}

// ─── Carga ───
async function cargarClientes() {
  isLoading.value = true
  errorMessage.value = null
  try {
    const { data } = await clientesApi.getAll(filtro.value)
    clientes.value = data
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'No se pudo cargar el listado de clientes.'
  } finally {
    isLoading.value = false
  }
}

onMounted(cargarClientes)

watch(filtro, () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(cargarClientes, 350)
})

onBeforeUnmount(() => clearTimeout(debounceTimer))
</script>

<style scoped>
.gestion-clientes {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 24px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
button { font-family: inherit; }

/* ═══ HERO ═══ */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  padding: 24px 28px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
}
.hero-left { min-width: 0; }
.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 8px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .7px;
  color: #0F766E;
  background: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  border: 1px solid #CCFBF1;
}
.hero h1 {
  margin: 0 0 4px;
  font-size: 26px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.02em;
  line-height: 1.15;
}
.hero-sub {
  margin: 0;
  font-size: 14px;
  color: #64748B;
  max-width: 520px;
}
.hero-right { display: flex; align-items: center; }

/* ═══ KPIs ═══ */
.kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.kpi {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease;
}
.kpi:hover {
  border-color: #CBD5E1;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -10px rgba(15, 23, 42, .08);
}
.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--kpi-bg);
  color: var(--kpi-color);
}
.kpi-texto { min-width: 0; }
.kpi-value {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.1;
  letter-spacing: -0.01em;
}
.kpi-label {
  margin: 3px 0 0;
  font-size: 12px;
  color: #64748B;
  font-weight: 500;
}

/* ═══ TOOLBAR ═══ */
.toolbar {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  padding: 14px 18px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03);
}
.search-box {
  position: relative;
  flex: 1 1 320px;
  max-width: 460px;
}
.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #94A3B8;
  pointer-events: none;
}
.search-input {
  width: 100%;
  padding: 10px 40px 10px 40px;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  font-size: 13.5px;
  color: #1E293B;
  background: #fff;
  font-family: inherit;
  outline: none;
  box-sizing: border-box;
  transition: all .2s;
}
.search-input::placeholder { color: #94A3B8; }
.search-input:focus {
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.search-clear {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #94A3B8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 5px;
  border-radius: 6px;
  transition: all .15s;
}
.search-clear:hover { color: #475569; background: #F1F5F9; }
.counter {
  color: #64748B;
  font-size: 12.5px;
  font-weight: 600;
  margin-left: auto;
  font-variant-numeric: tabular-nums;
}

/* ═══ ALERTA ═══ */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  font-weight: 500;
}
.alert-error {
  background: #FEF2F2;
  color: #991B1B;
  border: 1px solid #FECACA;
}
.alert-action {
  margin-left: auto;
  background: none;
  border: none;
  color: inherit;
  font-weight: 700;
  font-size: 12px;
  cursor: pointer;
  text-decoration: underline;
  white-space: nowrap;
}

/* ═══ SKELETON ═══ */
.table-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  overflow: hidden;
}
.skeleton-head {
  display: grid;
  grid-template-columns: 2fr 1.2fr 1fr 1fr 1fr auto;
  gap: 20px;
  padding: 16px 20px;
  background: #F8FAFC;
  border-bottom: 1px solid #E2E8F0;
}
.skeleton-row {
  display: grid;
  grid-template-columns: 2fr 1.2fr 1fr 1fr 1fr auto;
  gap: 20px;
  padding: 16px 20px;
  border-bottom: 1px solid #F1F5F9;
}
.skeleton-row:last-child { border-bottom: none; }
.skeleton-col,
.skeleton-cell {
  height: 12px;
  border-radius: 6px;
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
.skeleton-cell { height: 16px; }
.w-30 { width: 70%; }
.w-20 { width: 55%; }
.w-15 { width: 40%; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ═══ VACÍO ═══ */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 72px 24px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03);
  text-align: center;
}
.empty-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #F0FDFA;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}
.empty-state h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0F172A;
}
.empty-state p {
  margin: 0 0 8px;
  font-size: 13.5px;
  color: #64748B;
  max-width: 420px;
  line-height: 1.5;
}
.empty-state strong {
  color: #1E293B;
  font-weight: 700;
}

/* ═══ TABLA ═══ */
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table thead {
  background: #F8FAFC;
}
.data-table th {
  text-align: left;
  padding: 12px 20px;
  font-size: 11px;
  font-weight: 700;
  color: #64748B;
  text-transform: uppercase;
  letter-spacing: .6px;
  border-bottom: 1px solid #E2E8F0;
  white-space: nowrap;
}
.data-table td {
  padding: 14px 20px;
  font-size: 13.5px;
  color: #1E293B;
  border-bottom: 1px solid #F1F5F9;
  vertical-align: middle;
}
.data-table tr:last-child td { border-bottom: none; }

.data-row {
  cursor: pointer;
  transition: background-color .15s ease;
}
.data-row:hover {
  background: #FAFBFC;
}
.data-row:hover .row-arrow {
  color: #0F766E;
  transform: translateX(2px);
}

/* ═══ CELDA CLIENTE ═══ */
.cell-cliente { min-width: 220px; }
.cliente-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}
.cliente-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 4px 10px -3px rgba(15, 23, 42, .12);
  letter-spacing: .3px;
}
.cliente-info { min-width: 0; }
.cliente-nombre {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cliente-id {
  margin: 2px 0 0;
  font-size: 11px;
  font-weight: 600;
  color: #94A3B8;
  letter-spacing: .3px;
}

/* ═══ CELDAS SECUNDARIAS ═══ */
.cell-mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12.5px;
  font-weight: 600;
  color: #334155;
  background: #F1F5F9;
  padding: 3px 10px;
  border-radius: 6px;
  display: inline-block;
}
.cell-icon {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #475569;
  font-weight: 500;
}
.cell-icon svg { color: #94A3B8; }
.cell-date {
  font-size: 13px;
  color: #64748B;
  font-variant-numeric: tabular-nums;
}
.cell-empty { color: #CBD5E1; }

.ciudad-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #0F766E;
  background: #F0FDFA;
  padding: 3px 10px;
  border-radius: 20px;
  border: 1px solid #CCFBF1;
  white-space: nowrap;
}

.cell-accion {
  width: 40px;
  text-align: right;
  padding-right: 16px;
}
.row-arrow {
  color: #CBD5E1;
  transition: color .2s ease, transform .2s ease;
  vertical-align: middle;
}

/* ═══ BOTONES ═══ */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #0F766E;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
}
.btn-primary:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, .25);
}
.btn-link {
  background: none;
  border: none;
  padding: 0;
  color: #0F766E;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
}
.btn-link:hover { text-decoration: underline; }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .gestion-clientes { padding: 16px 16px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero h1 { font-size: 22px; }
  .kpis { grid-template-columns: 1fr; gap: 10px; }
  .kpi { padding: 14px 16px; }

  .toolbar { padding: 12px; gap: 10px; }
  .search-box { max-width: 100%; }
  .counter { margin-left: 0; width: 100%; text-align: right; }

  /* Tabla → tarjetas */
  .data-table thead { display: none; }
  .data-table,
  .data-table tbody,
  .data-table tr,
  .data-table td {
    display: block;
    width: 100%;
  }
  .data-table tr {
    padding: 14px 16px;
    border-bottom: 1px solid #F1F5F9;
    position: relative;
  }
  .data-table tr:last-child { border-bottom: none; }
  .data-table td {
    padding: 0;
    border-bottom: none;
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 13px;
    margin-top: 6px;
  }
  .data-table td:first-child { margin-top: 0; }

  .cell-cliente { margin-bottom: 4px; }
  .cliente-avatar { width: 36px; height: 36px; font-size: 12px; }
  .cliente-nombre { font-size: 14px; }

  /* Etiquetas antes de cada celda en móvil */
  .data-table td::before {
    content: attr(data-label);
    font-size: 10.5px;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: .5px;
    color: #94A3B8;
    min-width: 70px;
    flex-shrink: 0;
  }
  .data-table td.cell-cliente::before,
  .data-table td.cell-accion::before { content: none; }

  .cell-accion {
    position: absolute;
    top: 16px;
    right: 12px;
    width: auto;
    padding-right: 0;
    margin: 0 !important;
  }

  .row-arrow { color: #0F766E; }

  .skeleton-head,
  .skeleton-row {
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }
}
</style>