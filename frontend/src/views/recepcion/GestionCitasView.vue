<template>
  <div class="gestion-citas">
    <ToastContainer />

    <!-- Hero -->
    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <CalendarDays :size="12" /> Recepción · Agenda
        </span>
        <h1>Agenda</h1>
        <p class="page-header-sub">
          Programa, cobra y verifica las citas de los clientes.
        </p>
      </div>
      <div class="page-header-actions">
        <AppButton variant="primary" @click="router.push('/recepcion/citas/nueva')">
          <template #icon-left><Plus :size="16" /></template>
          Nueva Reserva
        </AppButton>
      </div>
    </header>

    <!-- Chips de estados -->
    <section v-if="!cargando && citas.length" class="estados-strip">
      <button
        type="button"
        class="estado-chip"
        :class="{ active: !filtroEstado }"
        @click="filtroEstado = null"
      >
        Todas <span class="chip-count">{{ citas.length }}</span>
      </button>
      <button
        v-for="e in estadosDisponibles"
        :key="e.estado"
        type="button"
        class="estado-chip"
        :class="{ active: filtroEstado === e.estado }"
        @click="filtroEstado = filtroEstado === e.estado ? null : e.estado"
      >
        {{ e.estado }}
        <span class="chip-count">{{ e.count }}</span>
      </button>
    </section>

    <!-- Toolbar -->
    <section class="toolbar">
      <div class="modo-chips" role="tablist">
        <button
          v-for="m in MODOS"
          :key="m.id"
          type="button"
          class="chip"
          :class="{ active: modo === m.id }"
          @click="cambiarModo(m.id)"
        >
          {{ m.label }}
        </button>
      </div>
      <div class="nav-rango">
        <button class="nav-btn" type="button" @click="moverRango(-1)" aria-label="Anterior">
          <ChevronLeft :size="17" />
        </button>
        <button class="nav-btn today" type="button" @click="irAHoy">Hoy</button>
        <button class="nav-btn" type="button" @click="moverRango(1)" aria-label="Siguiente">
          <ChevronRight :size="17" />
        </button>
      </div>
      <span class="rango-label">{{ rangoLabel }}</span>
      <div class="toolbar-right">
        <span v-if="citas.length" class="counter">
          {{ citasFiltradas.length }}<span v-if="filtroEstado"> / {{ citas.length }}</span>
          {{ citasFiltradas.length === 1 ? 'turno' : 'turnos' }}
        </span>
        <AppButton variant="secondary" :loading="cargando" @click="cargarTodo">
          <template #icon-left><RefreshCw :size="15" /></template>
          Actualizar
        </AppButton>
      </div>
    </section>

    <!-- Error -->
    <AppAlert
      v-if="error"
      variant="error"
      :action="'Reintentar'"
      @action="cargarTodo"
    >
      {{ error }}
    </AppAlert>

    <!-- Loading -->
    <div v-if="cargando" class="skeleton-list">
      <div v-for="i in 3" :key="i" class="skeleton-day">
        <div class="skeleton skeleton-header" />
        <div v-for="j in 2" :key="j" class="skeleton skeleton-row-s" />
      </div>
    </div>

    <!-- Empty -->
    <AppEmptyState
      v-else-if="!citas.length"
      :icon="CalendarDays"
      title="No hay turnos en este rango"
      description='Usa "Nueva Reserva" para agendar y cobrar una cita en mostrador.'
    >
      <template #action>
        <AppButton variant="primary" @click="router.push('/recepcion/citas/nueva')">
          <template #icon-left><Plus :size="16" /></template>
          Nueva Reserva
        </AppButton>
      </template>
    </AppEmptyState>

    <AppEmptyState
      v-else-if="!citasFiltradas.length"
      :icon="CalendarDays"
      title="Sin resultados"
      :description="`No hay turnos con el estado &quot;${filtroEstado}&quot;.`"
    >
      <template #action>
        <AppButton variant="secondary" @click="filtroEstado = null">
          Ver todos los turnos
        </AppButton>
      </template>
    </AppEmptyState>

    <!-- Listado por día -->
    <div v-else class="dias">
      <section
        v-for="grupo in gruposPorFecha"
        :key="grupo.fecha"
        class="dia-card"
        :class="{ 'is-past': esVencida(grupo.fecha), 'is-today': grupo.fecha === hoy }"
      >
        <header class="dia-header">
          <div class="dia-header-left">
            <div class="dia-fecha">
              <span class="dia-numero">{{ numeroDia(grupo.fecha) }}</span>
              <span class="dia-mes">{{ mesCorto(grupo.fecha) }}</span>
            </div>
            <div class="dia-texto">
              <p class="dia-nombre">{{ nombreDia(grupo.fecha) }}</p>
              <p class="dia-year">{{ yearDia(grupo.fecha) }}</p>
            </div>
          </div>
          <div class="dia-header-right">
            <span v-if="grupo.fecha === hoy" class="dia-badge hoy-badge">Hoy</span>
            <span v-else-if="esVencida(grupo.fecha)" class="dia-badge past-badge">Día pasado</span>
            <span class="dia-count">
              {{ grupo.citas.length }} {{ grupo.citas.length === 1 ? 'turno' : 'turnos' }}
            </span>
          </div>
        </header>

        <div class="turnos">
          <article
            v-for="c in grupo.citas"
            :key="c.idCita"
            class="turno"
            @click="abrirDetalle(c)"
          >
            <div class="turno-hora">
              <span class="h-inicio">{{ fmtHora(c.horaInicio) }}</span>
              <span class="h-fin">{{ fmtHora(c.horaFin) }}</span>
            </div>

            <div class="turno-info">
              <p class="turno-mascota">
                <PawPrint :size="13" /> {{ c.nombreMascota }}
              </p>
              <p class="turno-cliente">
                <User :size="12" /> {{ c.nombreCliente }}
              </p>
              <div class="turno-meta">
                <span class="meta-item">
                  <Stethoscope :size="12" /> {{ c.nombreVeterinario }}
                </span>
                <span v-if="c.nombreServicio" class="meta-item">
                  <ClipboardList :size="12" /> {{ c.nombreServicio }}
                </span>
                <span v-if="c.costoUsd != null" class="meta-item meta-price">
                  <DollarSign :size="12" /> {{ fmtUsd(c.costoUsd) }}
                </span>
              </div>
            </div>

            <div class="turno-side" @click.stop>
              <span class="estado-pill" :style="estadoPillStyle(c)">
                {{ labelEstadoDinamico(c) }}
              </span>

              <AppButton
                v-if="pagoPendienteDe(c)"
                variant="outline"
                size="sm"
                @click="irAVerificarEnCaja(pagoPendienteDe(c).pago)"
              >
                <template #icon-left><ShieldCheck :size="14" /></template>
                Ver pago
              </AppButton>
              <AppButton
                v-else-if="c.estado === 'Pendiente_Pago'"
                variant="primary"
                size="sm"
                @click="abrirCobro(c)"
              >
                <template #icon-left><Banknote :size="14" /></template>
                Cobrar
              </AppButton>
              <AppButton
                v-else-if="c.estado === 'Confirmada'"
                variant="danger-soft"
                size="sm"
                @click="abrirCancelar(c)"
              >
                <template #icon-left><XCircle :size="14" /></template>
                Cancelar
              </AppButton>
              <AppButton variant="ghost" size="sm" icon @click="abrirDetalle(c)">
                <Eye :size="14" />
              </AppButton>
            </div>
          </article>
        </div>
      </section>
    </div>

    <!-- Modales -->
    <CobroCitaModal
      v-model="cobroModalVisible"
      :cita="citaSeleccionada || {}"
      @cobrado="onCobrado"
    />

    <DetalleCitaModal
      v-model="detalleModalVisible"
      :cita="citaSeleccionada"
    />

    <AppModal
      :model-value="cancelarModalVisible"
      title="¿Cancelar esta cita?"
      size="sm"
      :loading="cancelando"
      @update:model-value="cerrarCancelar"
    >
      <div class="cancel-content">
        <div class="modal-icon-danger">
          <XCircle :size="22" />
        </div>
        <p class="cancel-texto">
          La cita de <strong>{{ citaSeleccionada?.nombreMascota }}</strong>
          quedará <strong>cancelada</strong> y el horario quedará libre.
          Esta acción no se puede deshacer.
        </p>
        <AppAlert v-if="cancelarError" variant="error">{{ cancelarError }}</AppAlert>
      </div>

      <template #footer>
        <AppButton variant="secondary" :disabled="cancelando" @click="cerrarCancelar">
          Volver
        </AppButton>
        <AppButton variant="danger" :loading="cancelando" @click="confirmarCancelar">
          <template #icon-left><XCircle :size="15" /></template>
          {{ cancelando ? 'Cancelando…' : 'Sí, cancelar cita' }}
        </AppButton>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAgenda, cambiarEstadoCita } from '@/api/citas.api'
import { getPagosPendientesVerificacion } from '@/api/pagos.api'
import { ESTADO_COLOR } from '@/utils/constants/estadosCita'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppModal from '@/components/ui/AppModal.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import CobroCitaModal from '@/components/recepcion/CobroCitaModal.vue'
import DetalleCitaModal from '@/components/recepcion/DetalleCitaModal.vue'
import { useToast } from '@/composables/useToast'
import {
  CalendarDays, Plus, RefreshCw, ChevronLeft, ChevronRight,
  Stethoscope, PawPrint, X, XCircle, Eye,
  User, ClipboardList, DollarSign, Banknote, ShieldCheck,
} from 'lucide-vue-next'

const router = useRouter()
const { toastSuccess, toastError } = useToast()

/* ─── Rango ─── */
const modo = ref('dia')
const fechaBase = ref(new Date().toISOString().split('T')[0])
const hoy = new Date().toISOString().split('T')[0]

const MODOS = [
  { id: 'dia', label: 'Día', offset: 1 },
  { id: 'semana', label: 'Semana', offset: 7 },
  { id: 'rango30', label: '30 días', offset: 30 },
]

function toISO(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const rango = computed(() => {
  const base = new Date(fechaBase.value + 'T12:00:00')
  const inicio = new Date(base)
  const fin = new Date(base)
  if (modo.value === 'semana') {
    const lunesOffset = (base.getDay() + 6) % 7
    inicio.setDate(base.getDate() - lunesOffset)
    fin.setDate(inicio.getDate() + 6)
  } else if (modo.value === 'rango30') {
    fin.setDate(inicio.getDate() + 29)
  }
  return { fechaInicio: toISO(inicio), fechaFin: toISO(fin) }
})

const rangoLabel = computed(() => {
  const { fechaInicio, fechaFin } = rango.value
  if (fechaInicio === fechaFin) return fmtFecha(fechaInicio)
  return `${fmtFechaCorta(fechaInicio)} → ${fmtFechaCorta(fechaFin)}`
})

function moverRango(direccion) {
  const m = MODOS.find((x) => x.id === modo.value)
  const base = new Date(fechaBase.value + 'T12:00:00')
  base.setDate(base.getDate() + direccion * m.offset)
  fechaBase.value = toISO(base)
  cargarTodo()
}

function cambiarModo(id) {
  modo.value = id
  fechaBase.value = hoy
  cargarTodo()
}

function irAHoy() {
  fechaBase.value = hoy
  cargarTodo()
}

/* ─── Datos ─── */
const citas = ref([])
const pagosPendientes = ref([])
const cargando = ref(false)
const error = ref('')
const filtroEstado = ref(null)

async function cargarAgenda() {
  const { data } = await getAgenda(rango.value)
  citas.value = data
  if (filtroEstado.value && !data.some((c) => labelEstadoDinamico(c) === filtroEstado.value)) {
    filtroEstado.value = null
  }
}

async function cargarPagosPendientes() {
  try {
    const { data } = await getPagosPendientesVerificacion()
    pagosPendientes.value = data || []
  } catch {
    pagosPendientes.value = []
  }
}

async function cargarTodo() {
  cargando.value = true
  error.value = ''
  try {
    await Promise.all([cargarAgenda(), cargarPagosPendientes()])
  } catch (err) {
    error.value = err.response?.data?.message || 'No se pudo cargar la agenda.'
  } finally {
    cargando.value = false
  }
}

function pagoPendienteDe(cita) {
  const p = pagosPendientes.value.find((x) => x.idCita === cita.idCita)
  return p ? { pago: p, aprobado: true } : null
}

function labelEstadoDinamico(cita) {
  if (cita.estado === 'Pendiente_Pago' && pagoPendienteDe(cita)) {
    return 'Verificando pago'
  }
  return String(cita.estado || '').replaceAll('_', ' ')
}

/* ─── Estados ─── */
const estadosDisponibles = computed(() => {
  const counts = {}
  for (const c of citas.value) {
    const e = labelEstadoDinamico(c)
    counts[e] = (counts[e] || 0) + 1
  }
  return Object.entries(counts)
    .map(([estado, count]) => ({ estado, count }))
    .sort((a, b) => b.count - a.count)
})

const citasFiltradas = computed(() => {
  if (!filtroEstado.value) return citas.value
  return citas.value.filter((c) => labelEstadoDinamico(c) === filtroEstado.value)
})

const gruposPorFecha = computed(() => {
  const map = new Map()
  for (const c of citasFiltradas.value) {
    if (!map.has(c.fecha)) map.set(c.fecha, [])
    map.get(c.fecha).push(c)
  }
  return [...map.entries()]
    .sort((a, b) => a[0].localeCompare(b[0]))
    .map(([fecha, lista]) => ({
      fecha,
      citas: [...lista].sort((a, b) =>
        String(a.horaInicio || '').localeCompare(String(b.horaInicio || ''))
      ),
    }))
})

/* ─── Helpers ─── */
const DIAS_SEMANA = ['Lunes','Martes','Miércoles','Jueves','Viernes','Sábado','Domingo']
const MESES_CORTOS = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']

function fmtFecha(iso) {
  if (!iso) return ''
  const d = new Date(iso + 'T12:00:00')
  return `${d.getDate()} ${MESES_CORTOS[d.getMonth()]} ${d.getFullYear()} · ${DIAS_SEMANA[(d.getDay() + 6) % 7]}`
}
function fmtFechaCorta(iso) {
  if (!iso) return ''
  const d = new Date(iso + 'T12:00:00')
  return `${d.getDate()} ${MESES_CORTOS[d.getMonth()]}`
}
function numeroDia(iso) { return new Date(iso + 'T12:00:00').getDate() }
function mesCorto(iso) { return MESES_CORTOS[new Date(iso + 'T12:00:00').getMonth()] }
function nombreDia(iso) {
  const d = new Date(iso + 'T12:00:00')
  return DIAS_SEMANA[(d.getDay() + 6) % 7]
}
function yearDia(iso) { return new Date(iso + 'T12:00:00').getFullYear() }
function fmtHora(t) {
  if (!t) return ''
  const [h, m] = t.split(':')
  const hh = Number(h) % 12 || 12
  return `${hh}:${m} ${Number(h) >= 12 ? 'PM' : 'AM'}`
}
function esVencida(iso) { return iso < hoy }
function fmtUsd(v) { return v == null ? '—' : `$${Number(v).toFixed(2)}` }

function colorDeEstado(cita) {
  return cita.colorUi || ESTADO_COLOR[cita.estado] || 'var(--neutral-500)'
}
function estadoPillStyle(cita) {
  const color = colorDeEstado(cita)
  return {
    color,
    borderColor: color,
    backgroundColor: 'transparent',
  }
}

/* ─── Modales ─── */
const citaSeleccionada = ref(null)
const cobroModalVisible = ref(false)
const detalleModalVisible = ref(false)
const cancelarModalVisible = ref(false)
const cancelando = ref(false)
const cancelarError = ref('')

function abrirCobro(cita) {
  citaSeleccionada.value = cita
  cobroModalVisible.value = true
}

function abrirDetalle(cita) {
  citaSeleccionada.value = cita
  detalleModalVisible.value = true
}

function abrirCancelar(cita) {
  citaSeleccionada.value = cita
  cancelarError.value = ''
  cancelarModalVisible.value = true
}

function cerrarCancelar() {
  if (cancelando.value) return
  cancelarModalVisible.value = false
}

async function onCobrado(cita) {
  toastSuccess(`Cita de ${cita.nombreMascota} cobrada correctamente`)
  await cargarTodo()
}

async function confirmarCancelar() {
  if (!citaSeleccionada.value) return
  cancelando.value = true
  cancelarError.value = ''
  try {
    await cambiarEstadoCita(citaSeleccionada.value.idCita, 'Cancelada')
    toastSuccess('Cita cancelada')
    cancelarModalVisible.value = false
    await cargarTodo()
  } catch (err) {
    cancelarError.value = err.response?.data?.message || 'No se pudo cancelar la cita.'
  } finally {
    cancelando.value = false
  }
}

function irAVerificarEnCaja(pago) {
  router.push({
    path: '/recepcion/caja',
    query: { tab: 'verificacion', focus: pago.idPago },
  })
}

onMounted(cargarTodo)
</script>

<style scoped>
.gestion-citas {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

/* ESTADOS STRIP */
.estados-strip {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
.estado-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-3) var(--space-1) var(--space-4);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--neutral-600);
  cursor: pointer;
  transition: all var(--duration-base) var(--ease-out);
  font-family: inherit;
}
.estado-chip:hover { background: var(--bg-surface-alt); border-color: var(--border-strong); }
.estado-chip.active {
  background: var(--brand-700);
  border-color: var(--brand-700);
  color: var(--text-inverse);
  box-shadow: 0 2px 8px rgba(15, 118, 110, 0.25);
}
.chip-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 18px;
  padding: 0 var(--space-2);
  border-radius: var(--radius-full);
  background: var(--neutral-100);
  color: var(--text-secondary);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
}
.estado-chip.active .chip-count {
  background: rgba(255, 255, 255, 0.22);
  color: var(--text-inverse);
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
}
.modo-chips {
  display: flex;
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 2px;
  gap: 2px;
}
.chip {
  padding: var(--space-2) var(--space-4);
  border: none;
  background: transparent;
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--duration-fast) var(--ease-out);
  border-radius: var(--radius-md);
}
.chip.active { background: var(--brand-700); color: var(--text-inverse); }

.nav-rango { display: flex; gap: var(--space-1); }
.nav-btn {
  height: 36px;
  min-width: 36px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  background: var(--bg-surface);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-family: inherit;
  padding: 0 var(--space-3);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  transition: all var(--duration-fast) var(--ease-out);
}
.nav-btn:hover { border-color: var(--brand-200); color: var(--brand-700); background: var(--brand-50); }

.rango-label { font-size: var(--text-md); color: var(--neutral-700); font-weight: var(--font-bold); }
.toolbar-right { display: flex; align-items: center; gap: var(--space-3); margin-left: auto; }
.counter { color: var(--text-secondary); font-size: var(--text-sm); font-weight: var(--font-semibold); }

/* SKELETON */
.skeleton-list { display: flex; flex-direction: column; gap: var(--space-4); }
.skeleton-day {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
}
.skeleton-header { height: 64px; }
.skeleton-row-s { height: 74px; border-top: 1px solid var(--neutral-100); }

/* DÍAS */
.dias { display: flex; flex-direction: column; gap: var(--space-4); }
.dia-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.dia-card.is-today {
  border-color: var(--brand-200);
  box-shadow: 0 0 0 3px var(--brand-100);
}
.dia-card.is-past { opacity: 0.92; }

.dia-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  background: linear-gradient(180deg, var(--neutral-50) 0%, var(--bg-surface-alt) 100%);
  border-bottom: 1px solid var(--border-subtle);
  flex-wrap: wrap;
}
.dia-card.is-today .dia-header {
  background: linear-gradient(180deg, var(--brand-50) 0%, var(--bg-surface-alt) 100%);
  border-bottom-color: var(--brand-100);
}
.dia-header-left { display: flex; align-items: center; gap: var(--space-4); }
.dia-fecha {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}
.dia-card.is-today .dia-fecha { background: var(--brand-700); border-color: var(--brand-700); }
.dia-numero { font-size: var(--text-2xl); font-weight: var(--font-bold); color: var(--brand-700); line-height: 1; }
.dia-card.is-today .dia-numero { color: var(--text-inverse); }
.dia-mes {
  margin-top: 2px;
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
}
.dia-card.is-today .dia-mes { color: rgba(255,255,255,0.85); }
.dia-nombre { margin: 0; font-size: var(--text-lg); font-weight: var(--font-bold); color: var(--text-primary); }
.dia-year { margin: 2px 0 0; font-size: var(--text-sm); color: var(--text-secondary); }

.dia-header-right { display: flex; align-items: center; gap: var(--space-3); }
.dia-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.hoy-badge { background: var(--brand-700); color: var(--text-inverse); }
.past-badge { background: var(--warning-50); color: var(--warning-700); border: 1px solid var(--warning-200); }
.dia-count {
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
  background: var(--bg-surface);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-subtle);
}

.turnos { display: flex; flex-direction: column; }
.turno {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--neutral-100);
  transition: background-color var(--duration-fast) var(--ease-out);
  cursor: pointer;
}
.turno:last-child { border-bottom: none; }
.turno:hover { background: var(--bg-surface-alt); }

.turno-hora {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 68px;
  padding: var(--space-2) var(--space-3);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}
.h-inicio { font-size: var(--text-md); font-weight: var(--font-bold); color: var(--brand-700); }
.h-fin { margin-top: 3px; font-size: var(--text-2xs); font-weight: var(--font-semibold); color: var(--text-tertiary); }

.turno-info { min-width: 0; }
.turno-mascota {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
.turno-cliente {
  margin: 3px 0 0;
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.turno-meta { margin-top: var(--space-2); display: flex; flex-wrap: wrap; gap: var(--space-2); }
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
  background: var(--bg-surface-alt);
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-subtle);
}
.meta-price { color: var(--brand-700); border-color: var(--brand-200); background: var(--brand-50); }

.turno-side { display: flex; align-items: center; gap: var(--space-2); flex-shrink: 0; flex-wrap: wrap; }
.estado-pill {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  border: 1px solid;
  white-space: nowrap;
}

/* MODAL CANCELAR */
.cancel-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
}
.modal-icon-danger {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-2xl);
  background: var(--danger-50);
  color: var(--danger-600);
  display: flex;
  align-items: center;
  justify-content: center;
}
.cancel-texto {
  margin: 0;
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  max-width: 380px;
}
.cancel-texto strong { color: var(--text-primary); font-weight: var(--font-bold); }

/* RESPONSIVE */
@media (max-width: 1024px) {
  .toolbar-right { margin-left: 0; width: 100%; justify-content: space-between; }
  .rango-label { width: 100%; text-align: center; }
}
@media (max-width: 768px) {
  .gestion-citas { padding: var(--space-4); }
  .turno { grid-template-columns: auto 1fr; gap: var(--space-3); padding: var(--space-4); }
  .turno-side {
    grid-column: 1 / -1;
    justify-content: flex-start;
    padding-top: var(--space-3);
    border-top: 1px dashed var(--border-subtle);
  }
}
</style>