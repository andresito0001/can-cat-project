<template>
  <div class="gestion-clientes">
    <ToastContainer />

    <!-- Hero -->
    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <Users :size="12" /> Recepción · Clientes
        </span>
        <h1>Gestión de Clientes</h1>
        <p class="page-header-sub">
          Registro, búsqueda y administración de clientes de la clínica.
        </p>
      </div>
      <div class="page-header-actions">
        <AppButton variant="primary" @click="router.push('/recepcion/clientes/nuevo')">
          <template #icon-left><UserPlus :size="16" /></template>
          Añadir nuevo cliente
        </AppButton>
      </div>
    </header>

    <!-- KPIs -->
    <section v-if="!isLoading && clientes.length" class="kpis">
      <article class="kpi">
        <div class="kpi-icon kpi-icon-brand"><Users :size="18" /></div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ clientes.length }}</p>
          <p class="kpi-label">
            {{ filtro ? 'Resultados' : 'Clientes registrados' }}
          </p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon kpi-icon-info"><MapPin :size="18" /></div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ conCiudad }}</p>
          <p class="kpi-label">Con ciudad</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon kpi-icon-warning"><MapPinOff :size="18" /></div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ sinCiudad }}</p>
          <p class="kpi-label">Sin ciudad</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon kpi-icon-purple"><CalendarPlus :size="18" /></div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ nuevosEsteMes }}</p>
          <p class="kpi-label">Nuevos este mes</p>
        </div>
      </article>
    </section>

    <!-- Toolbar -->
    <section class="toolbar">
      <div class="search-box">
        <Search :size="15" class="search-icon" />
        <input
          v-model="filtro"
          type="text"
          class="form-input search-input"
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

    <!-- Alerta -->
    <AppAlert
      v-if="errorMessage"
      variant="error"
      :action="'Reintentar'"
      @action="cargarClientes"
    >
      {{ errorMessage }}
    </AppAlert>

    <!-- Loading skeleton -->
    <div v-if="isLoading" class="table-card">
      <div class="skeleton-head">
        <div class="skeleton skeleton-col" />
        <div class="skeleton skeleton-col skeleton-col-sm" />
        <div class="skeleton skeleton-col skeleton-col-sm" />
        <div class="skeleton skeleton-col skeleton-col-sm" />
      </div>
      <div v-for="i in 5" :key="i" class="skeleton-row">
        <div class="skeleton skeleton-cell" />
        <div class="skeleton skeleton-cell skeleton-cell-sm" />
        <div class="skeleton skeleton-cell skeleton-cell-sm" />
        <div class="skeleton skeleton-cell skeleton-cell-sm" />
      </div>
    </div>

    <!-- Empty -->
    <AppEmptyState
      v-else-if="clientes.length === 0"
      :icon="filtro ? Search : Users"
      :title="filtro ? 'Sin resultados' : 'Aún no hay clientes registrados'"
      :description="filtro
        ? `No hay clientes que coincidan con &quot;${filtro}&quot;.`
        : 'Comienza registrando al primer cliente para agendar citas y gestionar mascotas.'"
    >
      <template #action>
        <AppButton v-if="filtro" variant="secondary" @click="limpiarFiltro">
          Limpiar búsqueda
        </AppButton>
        <AppButton v-else variant="primary" @click="router.push('/recepcion/clientes/nuevo')">
          <template #icon-left><UserPlus :size="16" /></template>
          Añadir nuevo cliente
        </AppButton>
      </template>
    </AppEmptyState>

    <!-- Tabla -->
    <div v-else class="table-card">
      <div class="table-wrap">
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
                  <EntityAvatar :nombre="cliente.nombreCompleto" tipo="cliente" size="md" />
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

    <ClienteDetalleModal
      v-model="detalleModalVisible"
      :cliente="clienteSeleccionado"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import * as clientesApi from '@/api/clientes.api'
import {
  UserPlus, Users, Search, X, ChevronRight,
  Phone, MapPin, MapPinOff, CalendarPlus,
} from 'lucide-vue-next'
import ClienteDetalleModal from '@/components/recepcion/ClienteDetalleModal.vue'
import EntityAvatar from '@/components/ui/EntityAvatar.vue'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'

const router = useRouter()

const clientes = ref([])
const filtro = ref('')
const isLoading = ref(false)
const errorMessage = ref(null)
const detalleModalVisible = ref(false)
const clienteSeleccionado = ref(null)

let debounceTimer = null

const conCiudad = computed(() => clientes.value.filter((c) => c.ciudad?.trim()).length)
const sinCiudad = computed(() => clientes.value.length - conCiudad.value)
const nuevosEsteMes = computed(() => {
  const ahora = new Date()
  const y = ahora.getFullYear()
  const m = ahora.getMonth()
  return clientes.value.filter((c) => {
    if (!c.createdAt) return false
    const d = new Date(c.createdAt)
    return d.getFullYear() === y && d.getMonth() === m
  }).length
})

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
  clienteSeleccionado.value = cliente
  detalleModalVisible.value = true
}

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
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

/* KPIs */
.kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}
.kpi {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  transition: all var(--duration-base) var(--ease-out);
}
.kpi:hover {
  border-color: var(--border-strong);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.kpi-icon-brand   { background: var(--brand-50);   color: var(--brand-700); }
.kpi-icon-info    { background: var(--info-50);    color: var(--info-600); }
.kpi-icon-warning { background: var(--warning-50); color: var(--warning-600); }
.kpi-icon-purple  { background: var(--purple-50);  color: var(--purple-600); }
.kpi-texto { min-width: 0; }
.kpi-value {
  margin: 0;
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.1;
  letter-spacing: var(--tracking-tight);
}
.kpi-label {
  margin: var(--space-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}

/* TOOLBAR */
.toolbar {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  flex-wrap: wrap;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
}
.search-box {
  position: relative;
  flex: 1 1 320px;
  max-width: 460px;
}
.search-icon {
  position: absolute;
  left: var(--space-4);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  pointer-events: none;
}
.search-input {
  padding-left: 40px;
  padding-right: 40px;
  background: var(--bg-surface);
}
.search-clear {
  position: absolute;
  right: var(--space-2);
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  transition: all var(--duration-fast) var(--ease-out);
}
.search-clear:hover { color: var(--neutral-600); background: var(--neutral-100); }
.counter {
  color: var(--text-secondary);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  margin-left: auto;
  font-variant-numeric: tabular-nums;
}

/* SKELETON */
.table-card {
  background: var(--bg-surface);
  border-radius: var(--radius-2xl);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}
.skeleton-head {
  display: grid;
  grid-template-columns: 2fr 1.2fr 1fr 1fr 1fr auto;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface-alt);
  border-bottom: 1px solid var(--border-subtle);
}
.skeleton-row {
  display: grid;
  grid-template-columns: 2fr 1.2fr 1fr 1fr 1fr auto;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--neutral-100);
}
.skeleton-row:last-child { border-bottom: none; }
.skeleton-col,
.skeleton-cell {
  height: 12px;
  width: 60%;
}
.skeleton-col-sm,
.skeleton-cell-sm { width: 50%; }
.skeleton-cell { height: 16px; }

/* TABLE */
.table-wrap { overflow-x: auto; }
.data-table {
  width: 100%;
  border-collapse: collapse;
}
.data-table thead { background: var(--bg-surface-alt); }
.data-table th {
  text-align: left;
  padding: var(--space-3) var(--space-5);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  border-bottom: 1px solid var(--border-subtle);
  white-space: nowrap;
}
.data-table td {
  padding: var(--space-4) var(--space-5);
  font-size: var(--text-md);
  color: var(--text-primary);
  border-bottom: 1px solid var(--neutral-100);
  vertical-align: middle;
}
.data-table tr:last-child td { border-bottom: none; }

.data-row { cursor: pointer; transition: background-color var(--duration-fast) var(--ease-out); }
.data-row:hover { background: var(--bg-surface-alt); }
.data-row:hover .row-arrow { color: var(--brand-700); transform: translateX(2px); }

.cell-cliente { min-width: 220px; }
.cliente-cell { display: flex; align-items: center; gap: var(--space-3); }
.cliente-info { min-width: 0; }
.cliente-nombre {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cliente-id {
  margin: 2px 0 0;
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--text-tertiary);
  letter-spacing: 0.03em;
}

.cell-mono {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--neutral-700);
  background: var(--neutral-100);
  padding: 3px var(--space-3);
  border-radius: var(--radius-sm);
  display: inline-block;
}
.cell-icon {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-md);
  color: var(--neutral-700);
  font-weight: var(--font-medium);
}
.cell-icon svg { color: var(--text-tertiary); }
.cell-date {
  font-size: var(--text-md);
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
}
.cell-empty { color: var(--neutral-300); }

.ciudad-pill {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--brand-700);
  background: var(--brand-50);
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid var(--brand-200);
  white-space: nowrap;
}

.cell-accion { width: 40px; text-align: right; padding-right: var(--space-4); }
.row-arrow {
  color: var(--neutral-300);
  transition: color var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
  vertical-align: middle;
}

/* RESPONSIVE */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .gestion-clientes { padding: var(--space-4); }
  .kpis { grid-template-columns: 1fr; gap: var(--space-3); }
  .toolbar { padding: var(--space-3); gap: var(--space-3); }
  .search-box { max-width: 100%; }
  .counter { margin-left: 0; width: 100%; text-align: right; }
}
@media (max-width: 640px) {
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
    padding: var(--space-4);
    border-bottom: 1px solid var(--neutral-100);
    position: relative;
  }
  .data-table td {
    padding: 0;
    border-bottom: none;
    display: flex;
    align-items: center;
    gap: var(--space-2);
    font-size: var(--text-md);
    margin-top: var(--space-2);
  }
  .data-table td:first-child { margin-top: 0; }
  .cell-cliente { margin-bottom: var(--space-1); }
  .cell-accion {
    position: absolute;
    top: var(--space-4);
    right: var(--space-3);
    width: auto;
    padding-right: 0;
    margin: 0 !important;
  }
  .row-arrow { color: var(--brand-700); }
}
</style>