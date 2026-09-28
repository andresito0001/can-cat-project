<template>
  <div class="gestion-citas">
    <ToastContainer />

    <!-- HERO -->
    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow">
          <CalendarDays :size="12" /> Recepción · Agenda
        </p>
        <h1>Agenda</h1>
        <p class="hero-sub">Programa, cobra y verifica las citas de los clientes.</p>
      </div>
      <div class="hero-right">
        <button class="btn-primary" type="button" @click="router.push('/recepcion/citas/nueva')">
          <Plus :size="16" /> Nueva Reserva
        </button>
      </div>
    </header>

    <!-- STRIP DE ESTADOS -->
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
        :style="chipEstadoStyle(e)"
        @click="filtroEstado = filtroEstado === e.estado ? null : e.estado"
      >
        {{ ESTADO_LABEL[e.estado] || e.estado }}
        <span class="chip-count">{{ e.count }}</span>
      </button>
    </section>

    <!-- TOOLBAR -->
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
        <button class="nav-btn" type="button" @click="moverRango(-1)"><ChevronLeft :size="17" /></button>
        <button class="nav-btn today" type="button" @click="fechaBase = hoy; cargarAgenda()">Hoy</button>
        <button class="nav-btn" type="button" @click="moverRango(1)"><ChevronRight :size="17" /></button>
      </div>
      <span class="rango-label">{{ rangoLabel }}</span>
      <div class="toolbar-right">
        <span v-if="citas.length" class="counter">
          {{ citasFiltradas.length }}<span v-if="filtroEstado"> / {{ citas.length }}</span>
          {{ citasFiltradas.length === 1 ? 'turno' : 'turnos' }}
        </span>
        <button class="btn-secondary" type="button" :disabled="cargando" @click="cargarTodo">
          <RefreshCw :size="15" :class="{ spin: cargando }" /> Actualizar
        </button>
      </div>
    </section>

    <!-- ALERTA -->
    <div v-if="error" class="alert alert-error">
      <AlertCircle :size="16" />
      <span>{{ error }}</span>
      <button type="button" class="alert-action" @click="cargarTodo">Reintentar</button>
    </div>

    <!-- SKELETON -->
    <div v-if="cargando" class="skeleton-list">
      <div v-for="i in 3" :key="i" class="skeleton-day">
        <div class="skeleton-header" />
        <div class="skeleton-row" v-for="j in 2" :key="j" />
      </div>
    </div>

    <!-- EMPTY -->
    <div v-else-if="!citas.length" class="empty-state">
      <div class="empty-icon"><CalendarDays :size="32" /></div>
      <h3>No hay turnos en este rango</h3>
      <p>Usa "Nueva Reserva" para agendar y cobrar una cita en mostrador.</p>
      <button class="btn-primary" type="button" @click="router.push('/recepcion/citas/nueva')">
        <Plus :size="16" /> Nueva Reserva
      </button>
    </div>

    <div v-else-if="!citasFiltradas.length" class="empty-state">
      <div class="empty-icon"><CalendarDays :size="32" /></div>
      <h3>Sin resultados</h3>
      <p>No hay turnos con el estado "{{ ESTADO_LABEL[filtroEstado] || filtroEstado }}".</p>
      <button class="btn-link" type="button" @click="filtroEstado = null">Ver todos los turnos</button>
    </div>

    <!-- LISTADO -->
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
            :class="{ 'turno-clickable': true }"
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
                <span class="meta-item"><Stethoscope :size="12" /> {{ c.nombreVeterinario }}</span>
                <span v-if="c.nombreServicio" class="meta-item"><ClipboardList :size="12" /> {{ c.nombreServicio }}</span>
                <span v-if="c.costoUsd != null" class="meta-item meta-price">
                  <DollarSign :size="12" /> {{ fmtUsd(c.costoUsd) }}
                </span>
              </div>
            </div>

            <div class="turno-side" @click.stop>
              <span class="estado-pill" :style="estadoPillStyle(c)">
                {{ labelEstadoDinamico(c) }}
              </span>

              <!-- Acciones contextuales según estado -->
              <template v-if="pagoPendienteDe(c)">
                <!-- Cita con pago online en verificación -->
                <button
                  v-if="pagoPendienteDe(c).aprobado !== null"
                  class="btn-accion btn-verificar"
                  type="button"
                  title="Verificar pago online"
                  @click="abrirVerificarPago(pagoPendienteDe(c).pago, true)"
                >
                  <ShieldCheck :size="14" />
                  <span class="btn-accion-text">Verificar</span>
                </button>
              </template>
              <template v-else-if="c.estado === 'Pendiente_Pago'">
                <!-- Cita sin pago online → cobrar en mostrador -->
                <button
                  class="btn-accion btn-cobrar"
                  type="button"
                  title="Cobrar en mostrador"
                  @click="abrirCobro(c)"
                >
                  <Banknote :size="14" />
                  <span class="btn-accion-text">Cobrar</span>
                </button>
              </template>
              <template v-else-if="c.estado === 'Confirmada'">
                <!-- Cita confirmada → cancelar -->
                <button
                  class="btn-accion btn-cancelar"
                  type="button"
                  title="Cancelar cita"
                  @click="abrirCancelar(c)"
                >
                  <XCircle :size="14" />
                  <span class="btn-accion-text">Cancelar</span>
                </button>
              </template>
              <!-- En_Atencion / Completada / Cancelada → sin acciones -->
              <button
                class="btn-detalle"
                type="button"
                title="Ver detalles"
                @click="abrirDetalle(c)"
              >
                <Eye :size="14" />
              </button>
            </div>
          </article>
        </div>
      </section>
    </div>

    <!-- MODAL: COBRO -->
    <CobroCitaModal
      v-model="cobroModalVisible"
      :cita="citaSeleccionada || {}"
      @cobrado="onCobrado"
    />

    <!-- MODAL: VERIFICAR PAGO -->
    <VerificarPagoModal
      v-model="verificarModalVisible"
      :pago="pagoSeleccionado || {}"
      :aprobado="verificarAprobado"
      @verificado="onVerificado"
    />

    <!-- MODAL: DETALLE -->
    <DetalleCitaModal
      v-model="detalleModalVisible"
      :cita="citaSeleccionada"
    />

    <!-- MODAL: CANCELAR -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="cancelarModalVisible" class="modal-overlay" @click.self="!cancelando && cerrarCancelar()">
          <Transition name="slide-up" appear>
            <div v-if="cancelarModalVisible" class="modal-cancel">
              <header class="modal-head">
                <div class="modal-head-left">
                  <div class="modal-icon modal-icon--danger"><XCircle :size="18" /></div>
                  <div>
                    <span class="modal-eyebrow">Cancelar cita</span>
                    <h3>{{ citaSeleccionada?.nombreMascota }} · {{ citaSeleccionada?.nombreCliente }}</h3>
                  </div>
                </div>
                <button class="modal-close" type="button" :disabled="cancelando" @click="cerrarCancelar">
                  <X :size="17" />
                </button>
              </header>

              <div class="modal-body">
                <p class="cancel-texto">
                  La cita quedará <strong>cancelada</strong> y el horario quedará libre.
                  Esta acción no se puede deshacer.
                </p>
                <div v-if="cancelarError" class="alert alert-error">
                  <AlertTriangle :size="16" />
                  <span>{{ cancelarError }}</span>
                </div>
              </div>

              <footer class="modal-foot">
                <button class="btn btn-ghost" type="button" :disabled="cancelando" @click="cerrarCancelar">
                  Volver
                </button>
                <button class="btn btn-danger" type="button" :disabled="cancelando" @click="confirmarCancelar">
                  <Loader2 v-if="cancelando" :size="15" class="spin" />
                  <XCircle v-else :size="15" />
                  {{ cancelando ? 'Cancelando…' : 'Sí, cancelar cita' }}
                </button>
              </footer>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAgenda, cambiarEstadoCita } from '@/api/citas.api'
import { getPagosPendientesVerificacion } from '@/api/pagos.api'
import { ESTADO_LABEL, ESTADO_COLOR } from '@/utils/constants/estadosCita'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import CobroCitaModal from '@/components/recepcion/CobroCitaModal.vue'
import VerificarPagoModal from '@/components/recepcion/VerificarPagoModal.vue'
import DetalleCitaModal from '@/components/recepcion/DetalleCitaModal.vue'
import { useToast } from '@/composables/useToast'
import {
  CalendarDays, Plus, RefreshCw, ChevronLeft, ChevronRight, AlertCircle,
  Stethoscope, PawPrint, Loader2, X, XCircle, Eye,
  User, ClipboardList, DollarSign, Banknote, ShieldCheck, AlertTriangle,
} from 'lucide-vue-next'

const router = useRouter()
const { toastSuccess, toastError } = useToast()

// ─── Rango ───
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
  const m = MODOS.find(x => x.id === modo.value)
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

// ─── Datos ───
const citas = ref([])
const pagosPendientes = ref([])
const cargando = ref(false)
const error = ref('')
const filtroEstado = ref(null)

async function cargarAgenda() {
  const { data } = await getAgenda(rango.value)
  citas.value = data
  if (filtroEstado.value && !data.some(c => c.estado === filtroEstado.value)) {
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

/** Devuelve el pago pendiente de verificación asociado a la cita (si existe). */
function pagoPendienteDe(cita) {
  const p = pagosPendientes.value.find(x => x.idCita === cita.idCita)
  return p ? { pago: p, aprobado: true } : null
}

/** Label dinámico del badge según estado + pago. */
function labelEstadoDinamico(cita) {
  if (cita.estado === 'Pendiente_Pago' && pagoPendienteDe(cita)) {
    return 'Verificando pago'
  }
  return ESTADO_LABEL[cita.estado] || cita.estado
}

// ─── Estados ───
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
  return citas.value.filter(c => labelEstadoDinamico(c) === filtroEstado.value)
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
      citas: [...lista].sort((a, b) => String(a.horaInicio || '').localeCompare(String(b.horaInicio || ''))),
    }))
})

// ─── Helpers ───
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

function isLightColor(hex) {
  if (!hex || typeof hex !== 'string') return false
  const c = hex.replace('#', '')
  if (c.length !== 6) return false
  const r = parseInt(c.slice(0, 2), 16)
  const g = parseInt(c.slice(2, 4), 16)
  const b = parseInt(c.slice(4, 6), 16)
  if ([r, g, b].some(Number.isNaN)) return false
  return (0.299 * r + 0.587 * g + 0.114 * b) / 255 > 0.65
}
function colorDeEstado(cita) {
  return cita.colorUi || ESTADO_COLOR[cita.estado] || '#64748B'
}
function estadoPillStyle(cita) {
  const color = colorDeEstado(cita)
  return {
    backgroundColor: color,
    color: isLightColor(color) ? '#0F172A' : '#fff',
    borderColor: color,
  }
}
function chipEstadoStyle({ estado }) {
  const color = ESTADO_COLOR[estado] || '#0F766E'
  return { '--chip-color': color, '--chip-bg': `${color}14` }
}

// ─── Modales ───
const citaSeleccionada = ref(null)
const cobroModalVisible = ref(false)
const verificarModalVisible = ref(false)
const verificarAprobado = ref(true)
const pagoSeleccionado = ref(null)
const detalleModalVisible = ref(false)
const cancelarModalVisible = ref(false)
const cancelando = ref(false)
const cancelarError = ref('')

function abrirCobro(cita) {
  citaSeleccionada.value = cita
  cobroModalVisible.value = true
}
function abrirVerificarPago(pago, aprobado) {
  pagoSeleccionado.value = pago
  verificarAprobado.value = aprobado
  verificarModalVisible.value = true
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

async function onVerificado({ pago, aprobado }) {
  toastSuccess(aprobado ? 'Pago confirmado' : 'Pago rechazado y cita cancelada')
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

onMounted(cargarTodo)
</script>

<style scoped>
.gestion-citas {
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

.hero {
  display: flex; align-items: center; justify-content: space-between;
  gap: 20px; flex-wrap: wrap;
  padding: 24px 28px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1; border-radius: 16px;
}
.hero-eyebrow {
  display: inline-flex; align-items: center; gap: 6px;
  margin: 0 0 8px;
  font-size: 11px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .7px; color: #0F766E;
  background: #fff; padding: 4px 10px;
  border-radius: 20px; border: 1px solid #CCFBF1;
}
.hero h1 { margin: 0 0 4px; font-size: 26px; font-weight: 700; color: #0F172A; letter-spacing: -0.02em; }
.hero-sub { margin: 0; font-size: 14px; color: #64748B; }

.estados-strip { display: flex; flex-wrap: wrap; gap: 8px; }
.estado-chip {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 6px 12px 6px 14px;
  border: 1px solid #E2E8F0; background: #fff;
  border-radius: 20px; font-size: 12.5px; font-weight: 600;
  color: #475569; cursor: pointer; transition: all .2s; font-family: inherit;
}
.estado-chip:hover { background: #F8FAFC; border-color: #CBD5E1; }
.estado-chip.active {
  background: var(--chip-bg, #F0FDFA);
  border-color: var(--chip-color, #0F766E);
  color: var(--chip-color, #0F766E);
}
.chip-count {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 20px; height: 18px; padding: 0 6px;
  border-radius: 10px; background: #F1F5F9; color: #64748B;
  font-size: 10.5px; font-weight: 700;
}

.toolbar {
  display: flex; align-items: center; gap: 12px; flex-wrap: wrap;
  padding: 14px 16px; background: #fff;
  border: 1px solid #E2E8F0; border-radius: 14px;
}
.modo-chips {
  display: flex; background: #F8FAFC;
  border: 1px solid #E2E8F0; border-radius: 10px;
  padding: 2px; gap: 2px;
}
.chip {
  padding: 7px 14px; border: none; background: transparent;
  font-size: 13px; font-weight: 600; color: #64748B;
  cursor: pointer; font-family: inherit;
  transition: all .15s; border-radius: 8px;
}
.chip.active { background: #0F766E; color: #fff; }

.nav-rango { display: flex; gap: 4px; }
.nav-btn {
  height: 36px; min-width: 36px;
  border: 1px solid #E2E8F0; border-radius: 10px;
  background: #fff; color: #64748B;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; font-family: inherit;
  padding: 0 12px; font-size: 13px; font-weight: 600;
}
.nav-btn:hover { border-color: #99F6E4; color: #0F766E; background: #F0FDFA; }

.rango-label { font-size: 13px; color: #475569; font-weight: 700; }
.toolbar-right { display: flex; align-items: center; gap: 12px; margin-left: auto; }
.counter { color: #64748B; font-size: 12.5px; font-weight: 600; }

.btn-primary {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 10px 20px; background: #0F766E; color: #fff;
  border: none; border-radius: 10px;
  font-size: 13.5px; font-weight: 700; cursor: pointer;
  font-family: inherit; white-space: nowrap;
}
.btn-primary:hover:not(:disabled) {
  background: #115E59; transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, .25);
}
.btn-secondary {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 9px 14px; background: #fff; color: #475569;
  border: 1px solid #E2E8F0; border-radius: 10px;
  font-size: 13px; font-weight: 600; cursor: pointer;
  font-family: inherit;
}
.btn-secondary:hover:not(:disabled) { background: #F8FAFC; color: #0F766E; }
.btn-link {
  background: none; border: none; padding: 0; color: #0F766E;
  font-size: 13px; font-weight: 700; cursor: pointer; font-family: inherit;
}
.spin { animation: spin .9s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.alert {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 12px 16px; border-radius: 10px;
  font-size: 13px; line-height: 1.5;
}
.alert-error { background: #FEF2F2; color: #991B1B; border: 1px solid #FECACA; }
.alert-action {
  margin-left: auto; background: none; border: none;
  color: inherit; font-weight: 700; font-size: 12px;
  cursor: pointer; text-decoration: underline;
}

.skeleton-list { display: flex; flex-direction: column; gap: 14px; }
.skeleton-day { background: #fff; border: 1px solid #E2E8F0; border-radius: 14px; overflow: hidden; }
.skeleton-header {
  height: 64px;
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%; animation: shimmer 1.4s infinite;
}
.skeleton-row {
  height: 74px; border-top: 1px solid #F1F5F9;
  background: linear-gradient(90deg, #FAFBFC 25%, #F1F5F9 50%, #FAFBFC 75%);
  background-size: 200% 100%; animation: shimmer 1.4s infinite;
}
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  gap: 12px; padding: 64px 24px;
  background: #fff; border: 1px solid #E2E8F0;
  border-radius: 14px; text-align: center;
}
.empty-icon {
  width: 64px; height: 64px; border-radius: 50%;
  background: #F0FDFA; color: #0F766E;
  display: flex; align-items: center; justify-content: center;
}
.empty-state h3 { margin: 0; font-size: 17px; font-weight: 700; color: #0F172A; }
.empty-state p { margin: 0 0 8px; font-size: 13.5px; color: #64748B; max-width: 420px; }

.dias { display: flex; flex-direction: column; gap: 14px; }
.dia-card {
  background: #fff; border: 1px solid #E2E8F0;
  border-radius: 14px; overflow: hidden;
}
.dia-card.is-today { border-color: #99F6E4; box-shadow: 0 0 0 3px rgba(15, 118, 110, .06); }
.dia-card.is-past { opacity: .92; }

.dia-header {
  display: flex; align-items: center; justify-content: space-between;
  gap: 12px; padding: 14px 20px;
  background: linear-gradient(180deg, #FAFBFC 0%, #F8FAFC 100%);
  border-bottom: 1px solid #E2E8F0; flex-wrap: wrap;
}
.dia-card.is-today .dia-header {
  background: linear-gradient(180deg, #F0FDFA 0%, #F8FAFC 100%);
  border-bottom-color: #CCFBF1;
}
.dia-header-left { display: flex; align-items: center; gap: 14px; }
.dia-fecha {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  width: 46px; height: 46px;
  background: #fff; border: 1px solid #E2E8F0; border-radius: 11px;
}
.dia-card.is-today .dia-fecha { background: #0F766E; border-color: #0F766E; }
.dia-numero { font-size: 18px; font-weight: 700; color: #0F766E; line-height: 1; }
.dia-card.is-today .dia-numero { color: #fff; }
.dia-mes {
  margin-top: 2px; font-size: 9.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .6px; color: #94A3B8;
}
.dia-card.is-today .dia-mes { color: rgba(255,255,255,.85); }
.dia-nombre { margin: 0; font-size: 14.5px; font-weight: 700; color: #0F172A; }
.dia-year { margin: 2px 0 0; font-size: 11.5px; color: #64748B; }
.dia-header-right { display: flex; align-items: center; gap: 10px; }
.dia-badge {
  display: inline-flex; align-items: center; padding: 3px 10px;
  border-radius: 20px; font-size: 10.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .5px;
}
.hoy-badge { background: #0F766E; color: #fff; }
.past-badge { background: #FEF3C7; color: #B45309; border: 1px solid #FDE68A; }
.dia-count {
  font-size: 12px; font-weight: 700; color: #64748B;
  background: #fff; padding: 4px 12px; border-radius: 20px;
  border: 1px solid #E2E8F0;
}

.turnos { display: flex; flex-direction: column; }
.turno {
  display: grid; grid-template-columns: auto 1fr auto;
  gap: 16px; align-items: center; padding: 14px 20px;
  border-bottom: 1px solid #F1F5F9;
  transition: background-color .15s;
}
.turno:last-child { border-bottom: none; }
.turno-clickable { cursor: pointer; }
.turno-clickable:hover { background: #FAFBFC; }

.turno-hora {
  display: flex; flex-direction: column; align-items: center;
  min-width: 68px; padding: 8px 10px;
  background: #F8FAFC; border: 1px solid #E2E8F0;
  border-radius: 10px; flex-shrink: 0;
}
.h-inicio { font-size: 13.5px; font-weight: 700; color: #0F766E; }
.h-fin { margin-top: 3px; font-size: 10.5px; font-weight: 600; color: #94A3B8; }

.turno-info { min-width: 0; }
.turno-mascota {
  margin: 0; display: inline-flex; align-items: center; gap: 6px;
  font-size: 14.5px; font-weight: 700; color: #0F172A;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 100%;
}
.turno-cliente {
  margin: 3px 0 0;
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 12.5px; color: #64748B;
}
.turno-meta { margin-top: 6px; display: flex; flex-wrap: wrap; gap: 8px; }
.meta-item {
  display: inline-flex; align-items: center; gap: 4px;
  font-size: 11.5px; font-weight: 600; color: #64748B;
  background: #F8FAFC; padding: 3px 9px; border-radius: 20px;
  border: 1px solid #E2E8F0;
}
.meta-price { color: #0F766E; border-color: #CCFBF1; background: #F0FDFA; }

.turno-side { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.estado-pill {
  display: inline-flex; align-items: center; padding: 5px 12px;
  border-radius: 20px; font-size: 11.5px; font-weight: 700;
  border: 1px solid; white-space: nowrap;
}

.btn-accion {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 7px 12px; border-radius: 9px;
  font-size: 12px; font-weight: 700; cursor: pointer;
  font-family: inherit; transition: all .15s;
  border: 1px solid; white-space: nowrap;
}
.btn-cobrar {
  background: #0F766E; color: #fff; border-color: #0F766E;
}
.btn-cobrar:hover {
  background: #115E59; border-color: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px -3px rgba(15, 118, 110, .4);
}
.btn-verificar {
  background: #FFFBEB; color: #B45309; border-color: #FDE68A;
}
.btn-verificar:hover {
  background: #FEF3C7; border-color: #FCD34D;
}
.btn-cancelar {
  background: #fff; color: #DC2626; border-color: #FECACA;
}
.btn-cancelar:hover {
  background: #FEF2F2; border-color: #FCA5A5;
}
.btn-detalle {
  width: 32px; height: 32px;
  border-radius: 8px; border: 1px solid #E2E8F0;
  background: #fff; color: #64748B;
  display: inline-flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all .15s;
}
.btn-detalle:hover { border-color: #0F766E; color: #0F766E; background: #F0FDFA; }

/* Modal cancelar */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, .55);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  padding: 24px; z-index: 1100;
  font-family: 'Inter', 'Segoe UI', Roboto, sans-serif;
}
.modal-cancel {
  background: #fff; border-radius: 18px;
  width: 100%; max-width: 480px;
  max-height: 90vh; display: flex; flex-direction: column;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .3);
}
.modal-head {
  display: flex; justify-content: space-between; align-items: flex-start;
  gap: 16px; padding: 20px 24px 18px;
  border-bottom: 1px solid #E2E8F0;
}
.modal-head-left { display: flex; gap: 14px; align-items: center; min-width: 0; }
.modal-icon {
  width: 40px; height: 40px; border-radius: 11px;
  background: #F0FDFA; color: #0F766E;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.modal-icon--danger { background: #FEF2F2; color: #DC2626; }
.modal-eyebrow {
  display: inline-block; font-size: 10.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .6px;
  color: #0F766E; margin-bottom: 4px;
}
.modal-head h3 { margin: 0; font-size: 16px; font-weight: 700; color: #0F172A; }
.modal-close {
  width: 34px; height: 34px; border-radius: 9px;
  border: 1px solid #E2E8F0; background: #fff; color: #64748B;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; flex-shrink: 0;
}
.modal-close:hover:not(:disabled) { background: #F8FAFC; color: #1E293B; }
.modal-close:disabled { opacity: .5; cursor: not-allowed; }
.modal-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 14px; }
.cancel-texto { margin: 0; font-size: 13.5px; color: #475569; line-height: 1.55; }
.cancel-texto strong { color: #DC2626; font-weight: 700; }
.modal-foot {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 16px 24px; border-top: 1px solid #E2E8F0;
  background: #FAFBFC;
}
.btn {
  display: inline-flex; align-items: center; justify-content: center;
  gap: 7px; padding: 10px 20px; border-radius: 10px;
  font-size: 13.5px; font-weight: 700; font-family: inherit;
  cursor: pointer; transition: all .2s; border: 1px solid transparent;
}
.btn:disabled { opacity: .55; cursor: not-allowed; }
.btn-ghost { background: #fff; color: #475569; border-color: #E2E8F0; }
.btn-ghost:hover:not(:disabled) { background: #F8FAFC; color: #0F766E; }
.btn-danger { background: #DC2626; color: #fff; border-color: #DC2626; }
.btn-danger:hover:not(:disabled) { background: #B91C1C; border-color: #B91C1C; }

.fade-enter-active, .fade-leave-active { transition: opacity .2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-up-enter-active, .slide-up-leave-active { transition: all .3s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateY(20px) scale(.98); }

@media (max-width: 768px) {
  .gestion-citas { padding: 16px 14px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero h1 { font-size: 22px; }
  .toolbar-right { margin-left: 0; width: 100%; justify-content: space-between; }
  .rango-label { width: 100%; text-align: center; }
  .turno { grid-template-columns: auto 1fr; gap: 12px; padding: 14px 16px; }
  .turno-side {
    grid-column: 1 / -1;
    justify-content: flex-start;
    flex-wrap: wrap;
    padding-top: 8px;
    border-top: 1px dashed #E2E8F0;
  }
}
@media (max-width: 480px) {
  .btn-accion-text { display: none; }
  .btn-accion { padding: 8px 10px; }
}
</style>