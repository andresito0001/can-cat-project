<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHistorialPagos, descargarFactura } from '@/api/pagos.api.js'
import {
  Sparkles, ArrowLeft, Download, ReceiptText, AlertTriangle, AlertCircle,
  Loader2, X, Calendar, Wallet, Hash, Banknote, CheckCircle2,
  ChevronRight, Clock, Receipt,
} from 'lucide-vue-next'

const router = useRouter()

// ─── ESTADO ───
const cargando = ref(false)
const pagos = ref([])
const errorCarga = ref('')

// ── Filtro por fechas ──
const filtroModo = ref('todos') // 'todos' | 'dia' | 'rango'
const fechaDia = ref('')
const fechaDesde = ref('')
const fechaHasta = ref('')

const filtroActivo = computed(() =>
  filtroModo.value !== 'todos'
  || !!fechaDia.value
  || !!fechaDesde.value
  || !!fechaHasta.value
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

// ── Agrupación por mes para la lista ──
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

// ── KPIs ──
const ESTADOS_PAGADOS = ['Verificado', 'Confirmado']

const stats = computed(() => {
  const total = pagos.value.length
  const pagados = pagos.value.filter((p) => ESTADOS_PAGADOS.includes(p.estadoPago))
  const totalPagado = pagados.reduce((s, p) => s + Number(p.monto || 0), 0)
  const porVerificar = pagos.value.filter((p) => p.estadoPago === 'Pendiente_Verificacion').length
  const porCobrar = pagos.value.filter((p) => p.estadoPago === 'Emitida').length
  return { total, totalPagado, porVerificar, porCobrar }
})

// ── Modal / descarga ──
const pagoSeleccionado = ref(null)
const descargando = ref(false)
const descargaFallida = ref(false)

// ── Toast (con timer seguro) ──
const toast = ref({ visible: false, message: '', type: 'success' })
let toastTimer = null

function showToast(message, type = 'success') {
  if (toastTimer) clearTimeout(toastTimer)
  toast.value = { visible: true, message, type }
  toastTimer = setTimeout(() => { toast.value.visible = false }, 4000)
}

// ─── CARGA ───
async function cargarHistorial() {
  cargando.value = true
  errorCarga.value = ''
  try {
    const { data } = await getHistorialPagos()
    pagos.value = [...(data || [])].sort((a, b) =>
      String(b.fecha || '').localeCompare(String(a.fecha || ''))
    )
  } catch (err) {
    errorCarga.value = 'No se pudo cargar el historial de pagos. Intenta nuevamente.'
  } finally {
    cargando.value = false
  }
}

// ─── HELPERS ───
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
      return 'verificado'
    case 'Pendiente_Verificacion':
    case 'Emitida':
      return 'pendiente'
    case 'Rechazado':
    case 'Anulada':
      return 'rechazado'
    default:
      return 'otro'
  }
}

function badgeClassCita(estado) {
  switch (estado) {
    case 'Confirmada': case 'Completada': return 'verificado'
    case 'Pendiente_Pago': return 'pendiente'
    case 'Cancelada': return 'rechazado'
    default: return 'otro'
  }
}

// ─── MODAL ───
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
    showToast('Factura descargada exitosamente')
  } catch (err) {
    descargaFallida.value = true
  } finally {
    descargando.value = false
  }
}

// ─── Teclado: Escape cierra el modal ───
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
  if (toastTimer) clearTimeout(toastTimer)
})
</script>

<template>
  <div class="historial-view">
    <!-- ═══ HERO ═══ -->
    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow">
          <Sparkles :size="12" />
          Cliente · Pagos
        </p>
        <h1>Historial de pagos</h1>
        <p class="hero-sub">
          Consulta tus transacciones, sigue el estado de cada pago y descarga tus facturas.
        </p>
      </div>
      <div class="hero-right">
        <button class="btn-secondary" type="button" @click="router.push('/cliente/dashboard')">
          <ArrowLeft :size="16" />
          Volver al panel
        </button>
      </div>
    </header>

    <!-- ═══ KPIs ═══ -->
    <section v-if="!cargando && !errorCarga && pagos.length" class="kpis">
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #0F766E; --kpi-bg: #F0FDFA;">
          <ReceiptText :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ stats.total }}</p>
          <p class="kpi-label">Transacciones</p>
        </div>
      </article>

      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #3B82F6; --kpi-bg: #EFF6FF;">
          <Wallet :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ fmtUsd(stats.totalPagado) }}</p>
          <p class="kpi-label">Total pagado</p>
        </div>
      </article>

      <article class="kpi" :class="{ 'is-warn': stats.porVerificar > 0 }">
        <div
          class="kpi-icon"
          :style="stats.porVerificar > 0
            ? '--kpi-color: #D97706; --kpi-bg: #FFFBEB;'
            : '--kpi-color: #94A3B8; --kpi-bg: #F1F5F9;'"
        >
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
        <div class="kpi-icon" style="--kpi-color: #8B5CF6; --kpi-bg: #F5F3FF;">
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
          :aria-pressed="filtroModo === 'todos'"
          @click="filtroModo = 'todos'"
        >
          Todos
        </button>
        <button
          type="button"
          :class="{ activo: filtroModo === 'dia' }"
          :aria-pressed="filtroModo === 'dia'"
          @click="filtroModo = 'dia'"
        >
          Por día
        </button>
        <button
          type="button"
          :class="{ activo: filtroModo === 'rango' }"
          :aria-pressed="filtroModo === 'rango'"
          @click="filtroModo = 'rango'"
        >
          Por rango
        </button>
      </div>

      <div v-if="filtroModo === 'dia'" class="filtro-campos">
        <input v-model="fechaDia" type="date" aria-label="Día" />
      </div>

      <div v-else-if="filtroModo === 'rango'" class="filtro-campos">
        <input v-model="fechaDesde" type="date" aria-label="Desde" />
        <span class="filtro-sep">hasta</span>
        <input v-model="fechaHasta" type="date" aria-label="Hasta" />
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
        <div class="skeleton-block sk-fecha" />
        <div class="skeleton-info">
          <div class="skeleton-block w-40" />
          <div class="skeleton-block w-70" />
          <div class="skeleton-block w-30" />
        </div>
        <div class="skeleton-block sk-monto" />
      </div>
    </div>

    <!-- ═══ ERROR ═══ -->
    <div v-else-if="errorCarga" class="alert-error">
      <AlertCircle :size="16" />
      <span>{{ errorCarga }}</span>
      <button type="button" class="alert-action" @click="cargarHistorial">Reintentar</button>
    </div>

    <!-- ═══ VACÍO: sin registros ═══ -->
    <div v-else-if="pagos.length === 0" class="empty-state">
      <div class="empty-icon"><ReceiptText :size="32" /></div>
      <h3>Sin registros de pagos</h3>
      <p>Aún no posee registros de pagos o facturas asociadas a su cuenta.</p>
      <button class="btn-primary" type="button" @click="router.push('/cliente/dashboard')">
        Volver al panel principal
      </button>
    </div>

    <!-- ═══ VACÍO: sin resultados con filtro ═══ -->
    <div v-else-if="pagosFiltrados.length === 0" class="empty-state">
      <div class="empty-icon"><Calendar :size="32" /></div>
      <h3>Sin resultados</h3>
      <p>No hay pagos para el filtro de fechas seleccionado.</p>
      <button class="btn-secondary" type="button" @click="limpiarFiltro">
        <X :size="15" /> Limpiar filtros
      </button>
    </div>

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
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="pagoSeleccionado"
          class="modal-overlay"
          @click.self="cerrarModal"
        >
          <Transition name="slide-up">
            <div
              v-if="pagoSeleccionado"
              class="modal-container"
              role="dialog"
              aria-modal="true"
              aria-label="Detalle de la transacción"
            >
              <header class="modal-header">
                <div class="modal-titles">
                  <span class="modal-eyebrow">
                    <ReceiptText :size="12" />
                    Comprobante
                  </span>
                  <h3 class="mono">{{ pagoSeleccionado.numeroControl }}</h3>
                </div>
                <button
                  class="btn-close"
                  type="button"
                  :disabled="descargando"
                  aria-label="Cerrar"
                  @click="cerrarModal"
                >
                  <X :size="18" />
                </button>
              </header>

              <div class="modal-body">
                <div class="modal-monto">
                  <span class="monto-label">Monto total</span>
                  <strong class="monto-valor">{{ fmtUsd(pagoSeleccionado.monto) }}</strong>
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
                      <span class="badge" :class="badgeClassCita(pagoSeleccionado.estadoCita)">
                        {{ pagoSeleccionado.estadoCita.replaceAll('_', ' ') }}
                      </span>
                    </span>
                  </div>
                </div>

                <!-- Flujo alterno: documento no disponible -->
                <div v-if="descargaFallida" class="alert-warn">
                  <AlertTriangle :size="16" />
                  <span>
                    El archivo digital del comprobante no está disponible en este momento.
                    Intente más tarde o contacte a la clínica.
                  </span>
                  <button type="button" class="warn-retry" @click="descargar">Reintentar</button>
                </div>
              </div>

              <footer class="modal-footer">
                <button class="btn-secondary" type="button" :disabled="descargando" @click="cerrarModal">
                  Cerrar
                </button>
                <button class="btn-primary" type="button" :disabled="descargando" @click="descargar">
                  <Loader2 v-if="descargando" :size="16" class="spin" />
                  <Download v-else :size="16" />
                  {{ descargando ? 'Descargando…' : 'Descargar factura' }}
                </button>
              </footer>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <!-- ═══ TOAST ═══ -->
    <Transition name="slide-down">
      <div v-if="toast.visible" class="toast" :class="toast.type" role="status">
        <CheckCircle2 v-if="toast.type === 'success'" :size="18" />
        <AlertCircle v-else :size="18" />
        {{ toast.message }}
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.historial-view {
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #0F172A;
  padding: 24px 24px 48px;
  display: flex;
  flex-direction: column;
  gap: 20px;
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
  max-width: 560px;
  line-height: 1.5;
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
  border-radius: 14px;
  transition: border-color .2s, box-shadow .25s, transform .2s;
}
.kpi:hover {
  border-color: #CBD5E1;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -10px rgba(15, 23, 42, .08);
}
.kpi.is-warn { border-color: #FDE68A; }
.kpi-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
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
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}
.kpi-value.is-warn { color: #D97706; }
.kpi-label {
  margin: 3px 0 0;
  font-size: 11.5px;
  color: #64748B;
  font-weight: 600;
}

/* ═══ FILTROS ═══ */
.filtro-card {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  padding: 12px 16px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
}
.filtro-modos { display: flex; gap: 6px; }
.filtro-modos button {
  padding: 8px 16px;
  border: 1px solid #E2E8F0;
  background: #fff;
  border-radius: 20px;
  font-size: 12.5px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  font-family: inherit;
  transition: all .2s;
}
.filtro-modos button:hover { border-color: #99F6E4; color: #0F766E; }
.filtro-modos button.activo {
  background: #0F766E;
  border-color: #0F766E;
  color: #fff;
  box-shadow: 0 2px 8px rgba(15, 118, 110, .25);
}
.filtro-campos { display: flex; align-items: center; gap: 10px; }
.filtro-campos input[type="date"] {
  padding: 8px 12px;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  font-size: 13px;
  color: #1E293B;
  background: #F8FAFC;
  font-family: inherit;
  outline: none;
  transition: all .2s;
}
.filtro-campos input[type="date"]:focus {
  border-color: #0F766E;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.filtro-sep { font-size: 13px; color: #64748B; }
.filtro-side {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}
.filtro-conteo { font-size: 13px; color: #64748B; font-weight: 500; white-space: nowrap; }
.filtro-conteo strong { color: #0F766E; font-weight: 700; }
.filtro-limpiar {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: none;
  border: none;
  color: #0F766E;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background-color .15s;
}
.filtro-limpiar:hover { background: #F0FDFA; }

/* ═══ SKELETON ═══ */
.skeleton-list { display: flex; flex-direction: column; gap: 12px; }
.skeleton-card {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 18px;
  align-items: center;
  padding: 18px 20px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
}
.skeleton-block {
  border-radius: 6px;
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
.sk-fecha { width: 48px; height: 48px; border-radius: 12px; }
.sk-monto { width: 90px; height: 22px; }
.skeleton-info { display: flex; flex-direction: column; gap: 8px; }
.w-30 { width: 30%; height: 11px; }
.w-40 { width: 40%; height: 12px; }
.w-70 { width: 70%; height: 14px; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ═══ ESTADOS ═══ */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 72px 24px;
  color: #94A3B8;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #E2E8F0;
  text-align: center;
}
.empty-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #F0FDFA 0%, #ECFDF5 100%);
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}
.empty-state h3 {
  font-size: 17px;
  font-weight: 700;
  color: #0F172A;
  margin: 0;
}
.empty-state p {
  font-size: 13.5px;
  color: #64748B;
  margin: 0 0 12px;
  max-width: 420px;
  line-height: 1.55;
}

.alert-error {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #FEF2F2;
  color: #991B1B;
  border-radius: 12px;
  border: 1px solid #FECACA;
  font-size: 13px;
  font-weight: 500;
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
  font-family: inherit;
  white-space: nowrap;
}

.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══ GRUPOS ═══ */
.grupos { display: flex; flex-direction: column; gap: 24px; }
.grupo-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 0 4px 10px;
  border-bottom: 1px solid #E2E8F0;
}
.grupo-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
  text-transform: capitalize;
}
.grupo-resumen {
  font-size: 12px;
  color: #94A3B8;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

/* ═══ LISTA DE PAGOS ═══ */
.pagos-lista {
  list-style: none;
  padding: 0;
  margin: 12px 0 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pago-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto auto;
  gap: 16px;
  align-items: center;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 1px 2px rgba(15, 23, 42, .03);
  padding: 16px 18px;
  cursor: pointer;
  transition: border-color .2s, box-shadow .25s, transform .2s;
}
.pago-card:hover {
  border-color: #99F6E4;
  box-shadow: 0 12px 24px -12px rgba(15, 118, 110, .18);
  transform: translateY(-2px);
}
.pago-card:focus-visible {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .15);
}

.pago-fecha-bloque {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 52px;
  padding: 8px 6px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  flex-shrink: 0;
}
.fecha-dia {
  font-size: 16px;
  font-weight: 700;
  color: #0F766E;
  line-height: 1;
  letter-spacing: -0.01em;
  font-variant-numeric: tabular-nums;
}
.fecha-mes {
  margin-top: 3px;
  font-size: 10px;
  font-weight: 700;
  color: #94A3B8;
  line-height: 1;
  text-transform: uppercase;
  letter-spacing: .5px;
}

.pago-info { min-width: 0; }
.pago-numero {
  font-size: 11px;
  color: #94A3B8;
  font-weight: 600;
  letter-spacing: .3px;
}
.pago-info h4 {
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
  margin: 2px 0 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pago-meta {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #64748B;
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
  gap: 5px;
  flex-shrink: 0;
}
.pago-monto {
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.01em;
}

.pago-flecha {
  color: #CBD5E1;
  flex-shrink: 0;
  transition: color .2s, transform .2s;
}
.pago-card:hover .pago-flecha {
  color: #0F766E;
  transform: translateX(2px);
}

/* ═══ BADGES ═══ */
.badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
  border: 1px solid transparent;
  letter-spacing: .2px;
}
.badge.verificado {
  background: #ECFDF5;
  color: #059669;
  border-color: #A7F3D0;
}
.badge.pendiente {
  background: #FFFBEB;
  color: #D97706;
  border-color: #FDE68A;
}
.badge.rechazado {
  background: #FEF2F2;
  color: #DC2626;
  border-color: #FECACA;
}
.badge.otro {
  background: #F1F5F9;
  color: #64748B;
  border-color: #E2E8F0;
}

/* ═══ BOTONES ═══ */
.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  background: #0F766E;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s;
  font-family: inherit;
  white-space: nowrap;
}
.btn-primary:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-primary:disabled { opacity: .65; cursor: not-allowed; }

.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  background: #fff;
  color: #475569;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s;
  font-family: inherit;
  white-space: nowrap;
}
.btn-secondary:hover:not(:disabled) {
  background: #F8FAFC;
  border-color: #CBD5E1;
  color: #0F766E;
}
.btn-secondary:disabled { opacity: .65; cursor: not-allowed; }

/* ═══ MODAL ═══ */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, .45);
  backdrop-filter: blur(3px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 100;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}
.modal-container {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-width: 520px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .25);
}
.modal-container::-webkit-scrollbar { width: 8px; }
.modal-container::-webkit-scrollbar-track { background: transparent; }
.modal-container::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 4px; }
.modal-container::-webkit-scrollbar-thumb:hover { background: #94A3B8; }

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.modal-titles { min-width: 0; }
.modal-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #0F766E;
  margin-bottom: 4px;
}
.modal-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: #0F172A;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.btn-close {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: #F1F5F9;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .2s;
  flex-shrink: 0;
}
.btn-close:hover:not(:disabled) { background: #E2E8F0; color: #1E293B; }
.btn-close:disabled { opacity: .6; cursor: not-allowed; }

.modal-body { padding: 24px; }

.modal-monto {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 22px 20px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 70%);
  border: 1px solid #CCFBF1;
  border-radius: 14px;
}
.monto-label {
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #64748B;
}
.monto-valor {
  font-size: 30px;
  font-weight: 700;
  color: #0F766E;
  letter-spacing: -0.02em;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.detalle-resumen {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 6px 16px;
}
.detalle-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 11px 0;
}
.detalle-fila + .detalle-fila { border-top: 1px solid #E2E8F0; }
.detalle-label {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  font-weight: 500;
  color: #64748B;
  flex-shrink: 0;
}
.detalle-valor {
  font-size: 13.5px;
  font-weight: 600;
  color: #1E293B;
  text-align: right;
  word-break: break-word;
  min-width: 0;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-weight: 500;
}

/* ═══ ALERTA WARN ═══ */
.alert-warn {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  background: #FFFBEB;
  color: #92400E;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #FDE68A;
  margin-top: 16px;
  line-height: 1.5;
}
.alert-warn > svg { flex-shrink: 0; margin-top: 1px; }
.warn-retry {
  background: none;
  border: none;
  color: #B45309;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  text-decoration: underline;
  white-space: nowrap;
  margin-left: auto;
  flex-shrink: 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px 24px;
  border-top: 1px solid #E2E8F0;
}

/* ═══ TOAST ═══ */
.toast {
  position: fixed;
  top: 24px;
  right: 24px;
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 12px 24px -8px rgba(0, 0, 0, .18);
  z-index: 200;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}
.toast.success {
  background: #ECFDF5;
  color: #059669;
  border: 1px solid #A7F3D0;
}
.toast.error {
  background: #FEF2F2;
  color: #DC2626;
  border: 1px solid #FECACA;
}

/* ═══ TRANSICIONES ═══ */
.fade-enter-active,
.fade-leave-active { transition: opacity .25s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

.slide-up-enter-active { transition: all .3s cubic-bezier(.16, 1, .3, 1); }
.slide-up-leave-active { transition: all .15s ease; }
.slide-up-enter-from,
.slide-up-leave-to { opacity: 0; transform: translateY(20px) scale(.98); }

.slide-down-enter-active,
.slide-down-leave-active { transition: all .3s ease; }
.slide-down-enter-from,
.slide-down-leave-to { opacity: 0; transform: translateY(-20px); }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 640px) {
  .historial-view { padding: 16px 14px 40px; gap: 14px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero h1 { font-size: 22px; }
  .hero-right { width: 100%; }
  .hero-right .btn-secondary { width: 100%; justify-content: center; }
  .kpis { grid-template-columns: 1fr; gap: 10px; }

  .filtro-side { margin-left: 0; width: 100%; justify-content: space-between; }

  .pago-card {
    grid-template-columns: auto minmax(0, 1fr);
    gap: 14px;
  }
  .pago-lateral {
    grid-column: 1 / -1;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding-top: 4px;
  }
  .pago-flecha { display: none; }

  .modal-footer { flex-direction: column-reverse; }
  .modal-footer .btn-primary,
  .modal-footer .btn-secondary { width: 100%; justify-content: center; }
}
</style>