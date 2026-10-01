<template>
  <div class="historial-view">
    <ToastContainer />

    <!-- ═══ HERO ═══ -->
    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <ReceiptText :size="12" /> Historial de pagos
        </span>
        <h1>Historial de pagos</h1>
        <p class="page-header-sub">
          Consulta tus transacciones, sigue el estado de cada pago y descarga tus facturas.
        </p>
      </div>
      <div class="page-header-actions">
        <AppButton variant="secondary" @click="router.push('/cliente/dashboard')">
          <template #icon-left><ArrowLeft :size="16" /></template>
          Volver al panel
        </AppButton>
      </div>
    </header>

    <!-- ═══ KPIs ═══ -->
    <section v-if="!cargando && !errorCarga && pagos.length" class="kpis">
      <article class="kpi">
        <div class="kpi-icon kpi-icon--brand">
          <ReceiptText :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ stats.total }}</p>
          <p class="kpi-label">Transacciones</p>
        </div>
      </article>

      <article class="kpi">
        <div class="kpi-icon kpi-icon--info">
          <Wallet :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ fmtUsd(stats.totalPagado) }}</p>
          <p class="kpi-label">Total pagado</p>
        </div>
      </article>

      <article class="kpi" :class="{ 'is-warn': stats.porVerificar > 0 }">
        <div class="kpi-icon" :class="stats.porVerificar > 0 ? 'kpi-icon--warning' : 'kpi-icon--neutral'">
          <Clock :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value" :class="{ 'is-warn': stats.porVerificar > 0 }">
            {{ stats.porVerificar }}
          </p>
          <p class="kpi-label">Por verificar</p>
        </div>
      </article>

      <article class="kpi">
        <div class="kpi-icon kpi-icon--purple">
          <Receipt :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ stats.porCobrar }}</p>
          <p class="kpi-label">Por cobrar</p>
        </div>
      </article>
    </section>

    <!-- ═══ FILTROS ═══ -->
    <section class="filtro-card">
      <div class="filtro-modos" role="group" aria-label="Filtrar por fechas">
        <button
          type="button"
          :class="{ activo: filtroModo === 'todos' }"
          @click="filtroModo = 'todos'"
        >
          Todos
        </button>
        <button
          type="button"
          :class="{ activo: filtroModo === 'dia' }"
          @click="filtroModo = 'dia'"
        >
          Por día
        </button>
        <button
          type="button"
          :class="{ activo: filtroModo === 'rango' }"
          @click="filtroModo = 'rango'"
        >
          Por rango
        </button>
      </div>

      <div v-if="filtroModo === 'dia'" class="filtro-campos">
        <input v-model="fechaDia" type="date" aria-label="Día" class="form-input" />
      </div>

      <div v-else-if="filtroModo === 'rango'" class="filtro-campos">
        <input v-model="fechaDesde" type="date" aria-label="Desde" class="form-input" />
        <span class="filtro-sep">hasta</span>
        <input v-model="fechaHasta" type="date" aria-label="Hasta" class="form-input" />
      </div>

      <div class="filtro-side">
        <span class="filtro-conteo">
          <strong>{{ pagosFiltrados.length }}</strong> de {{ pagos.length }} transacciones
        </span>
        <button v-if="filtroActivo" type="button" class="filtro-limpiar" @click="limpiarFiltro">
          <X :size="13" /> Limpiar
        </button>
      </div>
    </section>

    <!-- ═══ LOADING ═══ -->
    <div v-if="cargando" class="skeleton-list">
      <div v-for="i in 4" :key="i" class="skeleton-card">
        <div class="skeleton sk-fecha" />
        <div class="skeleton-info">
          <div class="skeleton w-40" />
          <div class="skeleton w-70" />
          <div class="skeleton w-30" />
        </div>
        <div class="skeleton sk-monto" />
      </div>
    </div>

    <!-- ═══ ERROR ═══ -->
    <div v-else-if="errorCarga" class="alert alert-error">
      <AlertCircle :size="16" />
      <span>{{ errorCarga }}</span>
      <button type="button" class="alert-action" @click="cargarHistorial">Reintentar</button>
    </div>

    <!-- ═══ VACÍO: sin registros ═══ -->
    <AppEmptyState
      v-else-if="pagos.length === 0"
      :icon="ReceiptText"
      title="Sin registros de pagos"
      description="Aún no posee registros de pagos o facturas asociadas a su cuenta."
    >
      <template #action>
        <AppButton variant="primary" @click="router.push('/cliente/dashboard')">
          Volver al panel principal
        </AppButton>
      </template>
    </AppEmptyState>

    <!-- ═══ VACÍO: sin resultados con filtro ═══ -->
    <AppEmptyState
      v-else-if="pagosFiltrados.length === 0"
      :icon="Calendar"
      title="Sin resultados"
      description="No hay pagos para el filtro de fechas seleccionado."
    >
      <template #action>
        <AppButton variant="secondary" @click="limpiarFiltro">
          <template #icon-left><X :size="15" /></template>
          Limpiar filtros
        </AppButton>
      </template>
    </AppEmptyState>

    <!-- ═══ LISTA AGRUPADA POR MES ═══ -->
    <section v-else class="grupos">
      <div v-for="grupo in pagosAgrupados" :key="grupo.key" class="grupo">
        <header class="grupo-header">
          <h3>{{ grupo.etiqueta }}</h3>
          <span class="grupo-resumen">
            {{ grupo.items.length }} {{ grupo.items.length === 1 ? 'transacción' : 'transacciones' }}
            · {{ fmtUsd(grupo.total) }}
          </span>
        </header>

        <ul class="pagos-lista">
          <li v-for="pago in grupo.items" :key="pago.idFactura">
            <article
              class="pago-card"
              role="button"
              tabindex="0"
              @click="verDetalle(pago)"
              @keydown.enter="verDetalle(pago)"
              @keydown.space.prevent="verDetalle(pago)"
            >
              <div class="pago-fecha-bloque">
                <span class="fecha-dia">{{ partesFecha(pago.fecha).dia }}</span>
                <span class="fecha-mes">{{ partesFecha(pago.fecha).mes }}</span>
              </div>

              <div class="pago-info">
                <span class="pago-numero mono">{{ pago.numeroControl }}</span>
                <h4>{{ pago.concepto }}</h4>
                <p class="pago-meta">
                  <Wallet :size="13" /> {{ metodoPagoLabel(pago.metodoPago) }}
                  <template v-if="horaDe(pago.fecha)"> · {{ horaDe(pago.fecha) }}</template>
                </p>
              </div>

              <div class="pago-lateral">
                <span class="badge" :class="badgeClass(pago.estadoPago)">
                  {{ etiquetaPago(pago.estadoPago) }}
                </span>
                <strong class="pago-monto">{{ fmtUsd(pago.monto) }}</strong>
              </div>

              <ChevronRight :size="16" class="pago-flecha" />
            </article>
          </li>
        </ul>
      </div>
    </section>

    <!-- ═══ MODAL DETALLE ═══ -->
    <AppModal
      :model-value="!!pagoSeleccionado"
      :title="pagoSeleccionado?.numeroControl || 'Detalle'"
      subtitle="Detalle del comprobante"
      size="md"
      :loading="descargando"
      @update:model-value="cerrarModal"
    >
      <template v-if="pagoSeleccionado">
        <div class="modal-monto">
          <span class="monto-label">Monto total</span>
          <strong class="monto-valor">{{ fmtUsd(pagoSeleccionado.monto) }} USD</strong>
          <strong v-if="pagoSeleccionado.montoBs != null" class="monto-valor-bs">
            Bs. {{ fmtBs(pagoSeleccionado.montoBs) }}
          </strong>
          <span class="badge" :class="badgeClass(pagoSeleccionado.estadoPago)">
            {{ etiquetaPago(pagoSeleccionado.estadoPago) }}
          </span>
        </div>

        <div class="detalle-resumen">
          <div class="detalle-fila">
            <span class="detalle-label"><Calendar :size="14" /> Fecha</span>
            <span class="detalle-valor">{{ formatearFechaHora(pagoSeleccionado.fecha) }}</span>
          </div>
          <div class="detalle-fila">
            <span class="detalle-label"><ReceiptText :size="14" /> Concepto</span>
            <span class="detalle-valor">{{ pagoSeleccionado.concepto }}</span>
          </div>
          <div class="detalle-fila">
            <span class="detalle-label"><Wallet :size="14" /> Método de pago</span>
            <span class="detalle-valor">{{ metodoPagoLabel(pagoSeleccionado.metodoPago) }}</span>
          </div>
          <div class="detalle-fila">
            <span class="detalle-label"><Hash :size="14" /> Referencia</span>
            <span class="detalle-valor mono">{{ pagoSeleccionado.referenciaTransaccion || '—' }}</span>
          </div>
          <div v-if="pagoSeleccionado.estadoCita" class="detalle-fila">
            <span class="detalle-label"><Calendar :size="14" /> Estado de la cita</span>
            <span class="detalle-valor">
              <span class="badge" :class="badgeClassCitaVisible(pagoSeleccionado)">
                {{ estadoCitaVisible(pagoSeleccionado) }}
              </span>
            </span>
          </div>
        </div>

        <div v-if="descargaFallida" class="alert alert-warning mt-4">
          <AlertTriangle :size="16" />
          <span>
            El archivo digital del comprobante no está disponible en este momento.
            Intente más tarde o contacte a la clínica.
          </span>
          <button type="button" class="alert-action" @click="descargar">Reintentar</button>
        </div>
      </template>

      <template #footer>
        <AppButton variant="secondary" :disabled="descargando" @click="cerrarModal">
          Cerrar
        </AppButton>
        <AppButton variant="primary" :loading="descargando" @click="descargar">
          <template #icon-left><Download :size="16" /></template>
          {{ descargando ? 'Descargando…' : 'Descargar factura' }}
        </AppButton>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeft, Download, ReceiptText, AlertTriangle, AlertCircle,
  Calendar, Wallet, Hash, Clock, Receipt, ChevronRight, X,
} from 'lucide-vue-next'
import { getHistorialPagos, descargarFactura } from '@/api/pagos.api.js'
import { useToast } from '@/composables/useToast'

import AppButton from '@/components/ui/AppButton.vue'
import AppModal from '@/components/ui/AppModal.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import ToastContainer from '@/components/ui/ToastContainer.vue'

const router = useRouter()
const { toastSuccess } = useToast()

const cargando = ref(false)
const pagos = ref([])
const errorCarga = ref('')

const filtroModo = ref('todos')
const fechaDia = ref('')
const fechaDesde = ref('')
const fechaHasta = ref('')

const filtroActivo = computed(() =>
  filtroModo.value !== 'todos' || !!fechaDia.value || !!fechaDesde.value || !!fechaHasta.value
)

function limpiarFiltro() {
  filtroModo.value = 'todos'
  fechaDia.value = ''
  fechaDesde.value = ''
  fechaHasta.value = ''
}

const pagosFiltrados = computed(() => {
  if (filtroModo.value === 'todos' || !pagos.value.length) return pagos.value
  const diaDe = (p) => String(p.fecha || '').slice(0, 10)
  if (filtroModo.value === 'dia') {
    return fechaDia.value ? pagos.value.filter((p) => diaDe(p) === fechaDia.value) : pagos.value
  }
  return pagos.value.filter((p) => {
    const f = diaDe(p)
    return (!fechaDesde.value || f >= fechaDesde.value) && (!fechaHasta.value || f <= fechaHasta.value)
  })
})

const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']
const MESES_LARGOS = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre']

function etiquetaMes(key) {
  const match = /^(\d{4})-(\d{2})$/.exec(key)
  if (!match) return 'Sin fecha'
  const [, y, m] = match
  return `${MESES_LARGOS[Number(m) - 1]} ${y}`
}

const pagosAgrupados = computed(() => {
  const grupos = []
  const map = new Map()
  for (const p of pagosFiltrados.value) {
    const key = String(p.fecha || '').slice(0, 7)
    if (!map.has(key)) {
      const grupo = { key, etiqueta: etiquetaMes(key), items: [], total: 0 }
      map.set(key, grupo)
      grupos.push(grupo)
    }
    const g = map.get(key)
    g.items.push(p)
    g.total += Number(p.monto || 0)
  }
  return grupos
})

const ESTADOS_PAGADOS = ['Verificado', 'Confirmado']

const stats = computed(() => {
  const total = pagos.value.length
  const pagados = pagos.value.filter((p) => ESTADOS_PAGADOS.includes(p.estadoPago))
  const totalPagado = pagados.reduce((s, p) => s + Number(p.monto || 0), 0)
  const porVerificar = pagos.value.filter((p) => p.estadoPago === 'Pendiente_Verificacion').length
  const porCobrar = pagos.value.filter((p) => p.estadoPago === 'Emitida').length
  return { total, totalPagado, porVerificar, porCobrar }
})

const pagoSeleccionado = ref(null)
const descargando = ref(false)
const descargaFallida = ref(false)

async function cargarHistorial() {
  cargando.value = true
  errorCarga.value = ''
  try {
    const { data } = await getHistorialPagos()
    pagos.value = [...(data || [])].sort((a, b) =>
      String(b.fecha || '').localeCompare(String(a.fecha || ''))
    )
  } catch {
    errorCarga.value = 'No se pudo cargar el historial de pagos. Intenta nuevamente.'
  } finally {
    cargando.value = false
  }
}

function formatearFechaHora(iso) {
  if (!iso) return '—'
  const [fecha, hora] = String(iso).split('T')
  const [y, m, d] = fecha.split('-')
  const base = `${d} ${MESES[Number(m) - 1]} ${y}`
  return hora ? `${base}, ${hora.slice(0, 5)}` : base
}

function partesFecha(iso) {
  if (!iso) return { dia: '--', mes: '—' }
  const [, m, d] = String(iso).slice(0, 10).split('-')
  return { dia: d, mes: MESES[Number(m) - 1]?.toUpperCase() || '' }
}

function horaDe(iso) {
  if (!iso || !iso.includes('T')) return ''
  return iso.slice(11, 16)
}

function fmtUsd(v) {
  const n = Number(v)
  if (v == null || Number.isNaN(n)) return '—'
  return `$${n.toFixed(2)}`
}

function fmtBs(v) {
  const n = Number(v)
  if (v == null || Number.isNaN(n)) return '—'
  return n.toLocaleString('es-VE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function estadoCitaVisible(pago) {
  if (!pago) return '—'
  const cita = pago.estadoCita
  const pagoEstado = pago.estadoPago

  if (cita === 'Pendiente_Pago') {
    if (pagoEstado === 'Pendiente_Verificacion') return 'Verificando pago'
    if (pagoEstado === 'Rechazado') return 'Pago rechazado'
    return 'Pendiente de pago'
  }
  const map = {
    'Confirmada': 'Confirmada',
    'En_Atencion': 'En atención',
    'Completada': 'Completada',
    'Cancelada': 'Cancelada',
  }
  return map[cita] || cita.replaceAll('_', ' ')
}

function badgeClassCitaVisible(pago) {
  const estado = estadoCitaVisible(pago)
  switch (estado) {
    case 'Confirmada':
    case 'Completada':
      return 'badge-success'
    case 'Verificando pago':
    case 'Pendiente de pago':
      return 'badge-warning'
    case 'En atención':
      return 'badge-info'
    case 'Cancelada':
    case 'Pago rechazado':
      return 'badge-danger'
    default:
      return 'badge-neutral'
  }
}

function metodoPagoLabel(m) {
  return m ? String(m).replaceAll('_', ' ') : '—'
}

const ETIQUETAS_PAGO = {
  Confirmado: 'Confirmado',
  Emitida: 'Por cobrar',
  Pendiente_Verificacion: 'Por verificar',
  Verificado: 'Verificado',
  Rechazado: 'Rechazado',
  Anulada: 'Anulada',
}

function etiquetaPago(estado) {
  return ETIQUETAS_PAGO[estado] || String(estado ?? '—').replaceAll('_', ' ')
}

function badgeClass(estado) {
  switch (estado) {
    case 'Verificado':
    case 'Confirmado':
      return 'badge-success'
    case 'Pendiente_Verificacion':
    case 'Emitida':
      return 'badge-warning'
    case 'Rechazado':
    case 'Anulada':
      return 'badge-danger'
    default:
      return 'badge-neutral'
  }
}

function verDetalle(pago) {
  pagoSeleccionado.value = pago
  descargaFallida.value = false
}

function cerrarModal() {
  if (descargando.value) return
  pagoSeleccionado.value = null
}

async function descargar() {
  if (!pagoSeleccionado.value || descargando.value) return
  descargando.value = true
  descargaFallida.value = false
  try {
    const res = await descargarFactura(pagoSeleccionado.value.idFactura)
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.download = `Factura-${pagoSeleccionado.value.numeroControl}.pdf`
    link.click()
    window.URL.revokeObjectURL(url)
    toastSuccess('Factura descargada exitosamente')
  } catch {
    descargaFallida.value = true
  } finally {
    descargando.value = false
  }
}

function onKeydown(e) {
  if (e.key === 'Escape' && pagoSeleccionado.value && !descargando.value) {
    cerrarModal()
  }
}

onMounted(() => {
  cargarHistorial()
  window.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
})
</script>

<style scoped>
.historial-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-6) var(--space-12);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* ═══ KPIs ═══ */
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
  border-radius: var(--radius-2xl);
  transition: border-color var(--duration-base) var(--ease-out),
              box-shadow var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
}
.kpi:hover {
  border-color: var(--border-strong);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.kpi.is-warn { border-color: var(--warning-200); }
.kpi-icon {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.kpi-icon--brand   { background: var(--brand-50);   color: var(--brand-700); }
.kpi-icon--info    { background: var(--info-50);    color: var(--info-600); }
.kpi-icon--warning { background: var(--warning-50); color: var(--warning-600); }
.kpi-icon--neutral { background: var(--neutral-100); color: var(--text-secondary); }
.kpi-icon--purple  { background: var(--purple-50);  color: var(--purple-600); }

.kpi-texto { min-width: 0; }
.kpi-value {
  margin: 0;
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.1;
  letter-spacing: var(--tracking-tight);
  font-variant-numeric: tabular-nums;
}
.kpi-value.is-warn { color: var(--warning-600); }
.kpi-label {
  margin: var(--space-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-semibold);
}

/* ═══ FILTROS ═══ */
.filtro-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  flex-wrap: wrap;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
}
.filtro-modos { display: flex; gap: var(--space-2); }
.filtro-modos button {
  padding: var(--space-2) var(--space-4);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--neutral-600);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--duration-fast) var(--ease-out);
}
.filtro-modos button:hover { border-color: var(--brand-200); color: var(--brand-700); }
.filtro-modos button.activo {
  background: var(--brand-700);
  border-color: var(--brand-700);
  color: var(--text-inverse);
  box-shadow: 0 2px 8px rgba(15, 118, 110, 0.25);
}
.filtro-campos { display: flex; align-items: center; gap: var(--space-3); }
.filtro-campos input[type="date"] {
  width: auto;
  height: var(--input-h-sm);
  padding: 0 var(--space-3);
  font-size: var(--text-md);
  background: var(--bg-surface-alt);
}
.filtro-sep { font-size: var(--text-md); color: var(--text-secondary); }
.filtro-side {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-left: auto;
}
.filtro-conteo { font-size: var(--text-md); color: var(--text-secondary); font-weight: var(--font-medium); white-space: nowrap; }
.filtro-conteo strong { color: var(--brand-700); font-weight: var(--font-bold); }
.filtro-limpiar {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  background: none;
  border: none;
  color: var(--brand-700);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  cursor: pointer;
  font-family: inherit;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-md);
  transition: background-color var(--duration-fast) var(--ease-out);
}
.filtro-limpiar:hover { background: var(--brand-50); }

/* ═══ SKELETON ═══ */
.skeleton-list { display: flex; flex-direction: column; gap: var(--space-3); }
.skeleton-card {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
}
.sk-fecha { width: 48px; height: 48px; border-radius: var(--radius-xl); }
.sk-monto { width: 90px; height: 22px; }
.skeleton-info { display: flex; flex-direction: column; gap: var(--space-2); }
.w-30 { width: 30%; height: 11px; }
.w-40 { width: 40%; height: 12px; }
.w-70 { width: 70%; height: 14px; }

/* ═══ GRUPOS ═══ */
.grupos { display: flex; flex-direction: column; gap: var(--space-6); }
.grupo-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-3);
  padding: 0 var(--space-1) var(--space-3);
  border-bottom: 1px solid var(--border-subtle);
}
.grupo-header h3 {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
  text-transform: capitalize;
}
.grupo-resumen {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  font-weight: var(--font-semibold);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

/* ═══ LISTA DE PAGOS ═══ */
.pagos-lista {
  list-style: none;
  padding: 0;
  margin: var(--space-3) 0 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.pago-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto auto;
  gap: var(--space-4);
  align-items: center;
  background: var(--bg-surface);
  border-radius: var(--radius-2xl);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-xs);
  padding: var(--space-4) var(--space-5);
  cursor: pointer;
  transition: border-color var(--duration-base) var(--ease-out),
              box-shadow var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
}
.pago-card:hover {
  border-color: var(--brand-200);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.pago-card:focus-visible {
  outline: none;
  border-color: var(--brand-700);
  box-shadow: var(--shadow-focus);
}

.pago-fecha-bloque {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 52px;
  padding: var(--space-2) var(--space-1);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  flex-shrink: 0;
}
.fecha-dia {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  line-height: 1;
  letter-spacing: var(--tracking-tight);
  font-variant-numeric: tabular-nums;
}
.fecha-mes {
  margin-top: 3px;
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  color: var(--text-tertiary);
  line-height: 1;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.pago-info { min-width: 0; }
.pago-numero {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: var(--font-semibold);
  letter-spacing: 0.03em;
}
.pago-info h4 {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: var(--space-1) 0 var(--space-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pago-meta {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-transform: capitalize;
}

.pago-lateral {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--space-1);
  flex-shrink: 0;
}
.pago-monto {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
  letter-spacing: var(--tracking-tight);
}

.pago-flecha {
  color: var(--neutral-300);
  flex-shrink: 0;
  transition: color var(--duration-fast) var(--ease-out),
              transform var(--duration-fast) var(--ease-out);
}
.pago-card:hover .pago-flecha {
  color: var(--brand-700);
  transform: translateX(2px);
}

/* ═══ MODAL DETALLE ═══ */
.modal-monto {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-5) var(--space-4);
  margin-bottom: var(--space-5);
  background: linear-gradient(135deg, var(--brand-50) 0%, var(--bg-surface) 70%);
  border: 1px solid var(--brand-100);
  border-radius: var(--radius-2xl);
}
.monto-label {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-secondary);
}
.monto-valor {
  font-size: 30px;
  font-weight: var(--font-bold);
  color: var(--brand-700);
  letter-spacing: var(--tracking-tight);
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.monto-valor-bs {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--neutral-700);
  letter-spacing: -0.01em;
  line-height: 1;
  font-variant-numeric: tabular-nums;
  margin-top: 2px;
}

.detalle-resumen {
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-1) var(--space-4);
}
.detalle-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
}
.detalle-fila + .detalle-fila { border-top: 1px solid var(--border-subtle); }
.detalle-label {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-md);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  flex-shrink: 0;
}
.detalle-valor {
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  text-align: right;
  word-break: break-word;
  min-width: 0;
}

.mt-4 { margin-top: var(--space-4); }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .filtro-side { margin-left: 0; width: 100%; justify-content: space-between; }
}
@media (max-width: 640px) {
  .historial-view { padding: var(--space-4); gap: var(--space-4); }
  .kpis { grid-template-columns: 1fr; gap: var(--space-3); }
  .filtro-modos { width: 100%; overflow-x: auto; }
  .filtro-campos { width: 100%; flex-wrap: wrap; }
  .filtro-campos input[type="date"] { flex: 1; min-width: 0; }

  .pago-card {
    grid-template-columns: auto minmax(0, 1fr);
    gap: var(--space-3);
    padding: var(--space-3) var(--space-4);
  }
  .pago-lateral {
    grid-column: 1 / -1;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding-top: var(--space-1);
  }
  .pago-flecha { display: none; }

  .detalle-fila {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-1);
  }
  .detalle-valor { text-align: left; }
}
</style>