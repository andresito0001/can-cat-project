<template>
  <div class="gestion-citas">
    <ToastContainer />

    <!-- ═══ HERO ═══ -->
    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow">
          <CalendarDays :size="12" />
          Recepción · Agenda
        </p>
        <h1>Gestión de Citas</h1>
        <p class="hero-sub">Calendario general de especialistas y turnos programados.</p>
      </div>
      <div class="hero-right">
        <button class="btn-primary" type="button" @click="router.push('/recepcion/citas/nueva')">
          <Plus :size="16" /> Nueva Reserva
        </button>
      </div>
    </header>

    <!-- ═══ STRIP DE ESTADOS (KPI + filtro) ═══ -->
    <section v-if="!cargando && citas.length" class="estados-strip">
      <button
        type="button"
        class="estado-chip"
        :class="{ active: !filtroEstado }"
        @click="filtroEstado = null"
      >
        Todas
        <span class="chip-count">{{ citas.length }}</span>
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

    <!-- ═══ TOOLBAR ═══ -->
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
        <button class="nav-btn" type="button" aria-label="Anterior" @click="moverRango(-1)">
          <ChevronLeft :size="17" />
        </button>
        <button class="nav-btn today" type="button" @click="fechaBase = hoy; cargarAgenda()">
          Hoy
        </button>
        <button class="nav-btn" type="button" aria-label="Siguiente" @click="moverRango(1)">
          <ChevronRight :size="17" />
        </button>
      </div>

      <span class="rango-label">{{ rangoLabel }}</span>

      <div class="toolbar-right">
        <span v-if="citas.length" class="counter">
          {{ citasFiltradas.length }}<span v-if="filtroEstado"> / {{ citas.length }}</span>
          {{ citasFiltradas.length === 1 ? 'turno' : 'turnos' }}
        </span>
        <button class="btn-secondary" type="button" :disabled="cargando" @click="cargarAgenda">
          <RefreshCw :size="15" :class="{ spin: cargando }" />
          Actualizar
        </button>
      </div>
    </section>

    <!-- ═══ ALERTA ═══ -->
    <div v-if="error" class="alert alert-error">
      <AlertCircle :size="16" />
      <span>{{ error }}</span>
      <button type="button" class="alert-action" @click="cargarAgenda">Reintentar</button>
    </div>

    <!-- ═══ SKELETON ═══ -->
    <div v-if="cargando" class="skeleton-list">
      <div v-for="i in 3" :key="i" class="skeleton-day">
        <div class="skeleton-header" />
        <div class="skeleton-row" v-for="j in 2" :key="j" />
      </div>
    </div>

    <!-- ═══ VACÍO ═══ -->
    <div v-else-if="!citas.length" class="empty-state">
      <div class="empty-icon"><CalendarDays :size="32" /></div>
      <h3>No hay turnos en este rango</h3>
      <p>Usa "Nueva Reserva" para agendar y cobrar una cita en mostrador.</p>
      <button class="btn-primary" type="button" @click="router.push('/recepcion/citas/nueva')">
        <Plus :size="16" /> Nueva Reserva
      </button>
    </div>

    <!-- ═══ VACÍO POR FILTRO ═══ -->
    <div v-else-if="!citasFiltradas.length" class="empty-state">
      <div class="empty-icon"><CalendarDays :size="32" /></div>
      <h3>Sin resultados</h3>
      <p>No hay turnos con el estado "{{ ESTADO_LABEL[filtroEstado] || filtroEstado }}" en este rango.</p>
      <button class="btn-link" type="button" @click="filtroEstado = null">Ver todos los turnos</button>
    </div>

    <!-- ═══ LISTADO POR DÍAS ═══ -->
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
          <article v-for="c in grupo.citas" :key="c.idCita" class="turno">
            <div class="turno-hora">
              <span class="h-inicio">{{ fmtHora(c.horaInicio) }}</span>
              <span class="h-fin">{{ fmtHora(c.horaFin) }}</span>
            </div>

            <div class="turno-info">
              <p class="turno-mascota">
                <PawPrint :size="13" />
                {{ c.nombreMascota }}
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

            <div class="turno-side">
              <span class="estado-pill" :style="estadoPillStyle(c)">
                {{ ESTADO_LABEL[c.estado] || c.estado }}
              </span>

              <button
                v-if="tieneTransiciones(c)"
                class="btn-estado"
                type="button"
                :title="`Cambiar estado (${opcionesDe(c).length} disponibles)`"
                @click="abrirCambioEstado(c)"
              >
                <ArrowRightCircle :size="15" />
                <span class="btn-estado-text">Cambiar</span>
              </button>
            </div>
          </article>
        </div>
      </section>
    </div>

    <!-- ═══ MODAL CAMBIO DE ESTADO ═══ -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="citaSeleccionada" class="modal-overlay" @click.self="cerrarModal">
          <Transition name="slide-up">
            <div v-if="citaSeleccionada" class="modal-card">
              <header class="modal-header">
                <div class="modal-header-left">
                  <div class="modal-icon"><ArrowRightCircle :size="18" /></div>
                  <div>
                    <h3>Cambiar estado de la cita</h3>
                    <p class="modal-header-sub">Selecciona el nuevo estado al que quieres mover esta cita</p>
                  </div>
                </div>
                <button class="btn-close" type="button" @click="cerrarModal">
                  <X :size="18" />
                </button>
              </header>

              <div class="modal-body">
                <!-- Cita actual -->
                <div class="cita-resumen">
                  <div class="resumen-row">
                    <span class="resumen-label"><PawPrint :size="13" /> Mascota</span>
                    <span class="resumen-value">{{ citaSeleccionada.nombreMascota }}</span>
                  </div>
                  <div class="resumen-row">
                    <span class="resumen-label"><User :size="13" /> Cliente</span>
                    <span class="resumen-value">{{ citaSeleccionada.nombreCliente }}</span>
                  </div>
                  <div class="resumen-row">
                    <span class="resumen-label"><CalendarDays :size="13" /> Fecha y hora</span>
                    <span class="resumen-value">
                      {{ fmtFecha(citaSeleccionada.fecha) }} · {{ fmtHora(citaSeleccionada.horaInicio) }}
                    </span>
                  </div>
                  <div class="resumen-row">
                    <span class="resumen-label"><Clock :size="13" /> Estado actual</span>
                    <span class="estado-pill" :style="estadoPillStyle(citaSeleccionada)">
                      {{ ESTADO_LABEL[citaSeleccionada.estado] || citaSeleccionada.estado }}
                    </span>
                  </div>
                </div>

                <!-- Error -->
                <div v-if="cambioError" class="alert alert-error" style="margin-top: 0; margin-bottom: 16px;">
                  <AlertCircle :size="16" />
                  <span>{{ cambioError }}</span>
                </div>

                <!-- Opciones -->
                <p class="section-label">
                  Nuevo estado <span class="required">*</span>
                </p>

                <div v-if="!opcionesEstado.length" class="empty-inline">
                  <AlertCircle :size="24" />
                  <p>Esta cita no tiene transiciones disponibles.</p>
                </div>

                <div v-else class="opciones">
                  <button
                    v-for="op in opcionesEstado"
                    :key="op"
                    type="button"
                    class="opcion"
                    :class="{ selected: estadoDestino === op }"
                    :style="opcionStyle(op)"
                    @click="estadoDestino = op"
                  >
                    <span class="opcion-radio">
                      <span v-if="estadoDestino === op" class="opcion-radio-inner" />
                    </span>
                    <span class="opcion-body">
                      <span class="opcion-label">{{ ESTADO_LABEL[op] || op }}</span>
                      <span class="opcion-desc">{{ descripcionEstado(op) }}</span>
                    </span>
                  </button>
                </div>
              </div>

              <footer class="modal-footer">
                <button class="btn-secondary" type="button" :disabled="cambiandoEstado" @click="cerrarModal">
                  Cancelar
                </button>
                <button
                  class="btn-primary"
                  type="button"
                  :disabled="!estadoDestino || cambiandoEstado"
                  @click="aplicarCambioEstado"
                >
                  <Loader2 v-if="cambiandoEstado" :size="15" class="spin" />
                  <CheckCircle2 v-else :size="15" />
                  {{ cambiandoEstado ? 'Aplicando…' : 'Aplicar cambio' }}
                </button>
              </footer>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <!-- ═══ TOAST ═══ -->
    <Transition name="slide-down">
      <div v-if="toast.visible" class="toast" :class="toast.type">
        <CheckCircle2 v-if="toast.type === 'success'" :size="16" />
        <AlertCircle v-else :size="16" />
        {{ toast.message }}
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAgenda, cambiarEstadoCita } from '@/api/citas.api'
import { TRANSICIONES_ESTADOS, ESTADO_LABEL, ESTADO_COLOR } from '@/utils/constants/estadosCita'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import {
  CalendarDays, Plus, RefreshCw, ChevronLeft, ChevronRight, AlertCircle,
  Stethoscope, PawPrint, ArrowRightCircle, Loader2, CheckCircle2, X,
  User, ClipboardList, DollarSign, Clock
} from 'lucide-vue-next'

const router = useRouter()

// ─── Rango de fechas ───
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
  cargarAgenda()
}

function cambiarModo(id) {
  modo.value = id
  fechaBase.value = hoy
  cargarAgenda()
}

// ─── Datos ───
const citas = ref([])
const cargando = ref(false)
const error = ref('')
const toast = ref({ visible: false, message: '', type: 'success' })

// ─── Filtro por estado ───
const filtroEstado = ref(null)

async function cargarAgenda() {
  cargando.value = true
  error.value = ''
  try {
    const { data } = await getAgenda(rango.value)
    citas.value = data
    // Si el filtro actual ya no tiene citas, lo limpiamos
    if (filtroEstado.value && !data.some(c => c.estado === filtroEstado.value)) {
      filtroEstado.value = null
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'No se pudo cargar la agenda.'
  } finally {
    cargando.value = false
  }
}

// ─── Conteos por estado ───
const estadosDisponibles = computed(() => {
  const counts = {}
  for (const c of citas.value) {
    const e = c.estado
    if (!e) continue
    counts[e] = (counts[e] || 0) + 1
  }
  return Object.entries(counts)
    .map(([estado, count]) => ({ estado, count }))
    .sort((a, b) => b.count - a.count)
})

// ─── Citas filtradas ───
const citasFiltradas = computed(() => {
  if (!filtroEstado.value) return citas.value
  return citas.value.filter(c => c.estado === filtroEstado.value)
})

// ─── Agrupación por fecha ───
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

// ─── Helpers de fecha ───
const DIAS_SEMANA = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo']
const MESES_CORTOS = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']
const MESES_LARGOS = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre']

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
function numeroDia(iso) {
  return new Date(iso + 'T12:00:00').getDate()
}
function mesCorto(iso) {
  return MESES_CORTOS[new Date(iso + 'T12:00:00').getMonth()]
}
function nombreDia(iso) {
  const d = new Date(iso + 'T12:00:00')
  return DIAS_SEMANA[(d.getDay() + 6) % 7]
}
function yearDia(iso) {
  return new Date(iso + 'T12:00:00').getFullYear()
}
function fmtHora(t) {
  if (!t) return ''
  const [h, m] = t.split(':')
  const hh = Number(h) % 12 || 12
  return `${hh}:${m} ${Number(h) >= 12 ? 'PM' : 'AM'}`
}
function esVencida(iso) {
  return iso < hoy
}
function fmtUsd(v) {
  if (v == null) return '—'
  return `$${Number(v).toFixed(2)}`
}

// ─── Colores / contraste ───
function isLightColor(hex) {
  if (!hex || typeof hex !== 'string') return false
  const c = hex.replace('#', '')
  if (c.length !== 6) return false
  const r = parseInt(c.slice(0, 2), 16)
  const g = parseInt(c.slice(2, 4), 16)
  const b = parseInt(c.slice(4, 6), 16)
  if ([r, g, b].some(Number.isNaN)) return false
  const lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255
  return lum > 0.65
}

function colorDeEstado(cita) {
  return cita.colorUi || ESTADO_COLOR[cita.estado] || '#64748B'
}

function estadoPillStyle(cita) {
  const color = colorDeEstado(cita)
  const textoClaro = isLightColor(color)
  return {
    backgroundColor: color,
    color: textoClaro ? '#0F172A' : '#fff',
    borderColor: color,
  }
}

function chipEstadoStyle({ estado }) {
  const color = ESTADO_COLOR[estado] || '#64748B'
  return {
    '--chip-color': color,
    '--chip-bg': `${color}14`,
  }
}

function opcionStyle(estado) {
  const color = ESTADO_COLOR[estado] || '#64748B'
  return {
    '--op-color': color,
    '--op-bg': `${color}12`,
  }
}

// ─── Cambio de estado ───
const citaSeleccionada = ref(null)
const estadoDestino = ref(null)
const cambiandoEstado = ref(false)
const cambioError = ref('')

const opcionesEstado = computed(() =>
  citaSeleccionada.value ? (TRANSICIONES_ESTADOS[citaSeleccionada.value.estado] || []) : []
)

function opcionesDe(cita) {
  return TRANSICIONES_ESTADOS[cita.estado] || []
}
function tieneTransiciones(cita) {
  return opcionesDe(cita).length > 0
}

function abrirCambioEstado(cita) {
  citaSeleccionada.value = cita
  estadoDestino.value = null
  cambioError.value = ''
}

function cerrarModal() {
  if (cambiandoEstado.value) return
  citaSeleccionada.value = null
  estadoDestino.value = null
  cambioError.value = ''
}

async function aplicarCambioEstado() {
  if (!estadoDestino.value) return
  cambiandoEstado.value = true
  cambioError.value = ''
  try {
    await cambiarEstadoCita(citaSeleccionada.value.idCita, estadoDestino.value)
    citaSeleccionada.value = null
    estadoDestino.value = null
    showToast('Estado actualizado correctamente')
    await cargarAgenda()
  } catch (err) {
    cambioError.value = err.response?.data?.message || 'No se pudo cambiar el estado.'
  } finally {
    cambiandoEstado.value = false
  }
}

// Descripciones humanas de cada estado para las opciones del modal
function descripcionEstado(estado) {
  const descripciones = {
    Pendiente_Pago: 'La cita aún no ha sido pagada',
    Pagada: 'El pago fue confirmado',
    Confirmada: 'La cita está agendada y confirmada',
    En_Atencion: 'El veterinario está atendiendo al paciente',
    Completada: 'La cita se realizó y quedó cerrada',
    Cancelada: 'La cita se cancela y libera el horario',
  }
  return descripciones[estado] || ''
}

function showToast(message, type = 'success') {
  toast.value = { visible: true, message, type }
  setTimeout(() => { toast.value.visible = false }, 4000)
}

onMounted(cargarAgenda)
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

/* ═══ STRIP DE ESTADOS ═══ */
.estados-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.estado-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 12px 6px 14px;
  border: 1px solid #E2E8F0;
  background: #fff;
  border-radius: 20px;
  font-size: 12.5px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
}
.estado-chip:hover {
  background: #F8FAFC;
  border-color: #CBD5E1;
}
.estado-chip.active {
  background: var(--chip-bg, #F0FDFA);
  border-color: var(--chip-color, #0F766E);
  color: var(--chip-color, #0F766E);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--chip-color, #0F766E) 12%, transparent);
}
.chip-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 18px;
  padding: 0 6px;
  border-radius: 10px;
  background: #F1F5F9;
  color: #64748B;
  font-size: 10.5px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  transition: background-color .2s ease, color .2s ease;
}
.estado-chip.active .chip-count {
  background: color-mix(in srgb, var(--chip-color, #0F766E) 18%, transparent);
  color: var(--chip-color, #0F766E);
}

/* ═══ TOOLBAR ═══ */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03);
}
.modo-chips {
  display: flex;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  overflow: hidden;
  padding: 2px;
  gap: 2px;
}
.chip {
  padding: 7px 14px;
  border: none;
  background: transparent;
  font-size: 13px;
  font-weight: 600;
  color: #64748B;
  cursor: pointer;
  font-family: inherit;
  transition: all .15s;
  border-radius: 8px;
}
.chip:hover { color: #0F766E; }
.chip.active {
  background: #0F766E;
  color: #fff;
  box-shadow: 0 2px 6px rgba(15, 118, 110, .25);
}

.nav-rango { display: flex; gap: 4px; }
.nav-btn {
  height: 36px;
  min-width: 36px;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  background: #fff;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-family: inherit;
  transition: all .15s;
  padding: 0 12px;
  font-size: 13px;
  font-weight: 600;
}
.nav-btn:hover {
  border-color: #99F6E4;
  color: #0F766E;
  background: #F0FDFA;
}
.nav-btn.today { font-weight: 700; }

.rango-label {
  font-size: 13px;
  color: #475569;
  font-weight: 700;
  padding: 0 4px;
  letter-spacing: -0.01em;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}
.counter {
  color: #64748B;
  font-size: 12.5px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}
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
.btn-primary:disabled { opacity: .5; cursor: not-allowed; }
.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 14px;
  background: #fff;
  color: #475569;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
}
.btn-secondary:hover:not(:disabled) {
  background: #F8FAFC;
  border-color: #CBD5E1;
  color: #0F766E;
}
.btn-secondary:disabled { opacity: .5; cursor: not-allowed; }
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

.spin { animation: spin .9s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══ ALERTA ═══ */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
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
.skeleton-list { display: flex; flex-direction: column; gap: 14px; }
.skeleton-day {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  overflow: hidden;
}
.skeleton-header {
  height: 64px;
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
.skeleton-row {
  height: 74px;
  border-top: 1px solid #F1F5F9;
  background: linear-gradient(90deg, #FAFBFC 25%, #F1F5F9 50%, #FAFBFC 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
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
  padding: 64px 24px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
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

/* ═══ LISTADO ═══ */
.dias { display: flex; flex-direction: column; gap: 14px; }
.dia-card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03);
  transition: border-color .2s ease, box-shadow .2s ease;
}
.dia-card:hover {
  border-color: #CBD5E1;
  box-shadow: 0 10px 24px -12px rgba(15, 23, 42, .08);
}
.dia-card.is-today {
  border-color: #99F6E4;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .06);
}
.dia-card.is-past { opacity: .92; }

.dia-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 20px;
  background: linear-gradient(180deg, #FAFBFC 0%, #F8FAFC 100%);
  border-bottom: 1px solid #E2E8F0;
  flex-wrap: wrap;
}
.dia-card.is-today .dia-header {
  background: linear-gradient(180deg, #F0FDFA 0%, #F8FAFC 100%);
  border-bottom-color: #CCFBF1;
}
.dia-card.is-past .dia-header {
  background: linear-gradient(180deg, #FFFBEB 0%, #FEF3C7 100%);
  border-bottom-color: #FDE68A;
}

.dia-header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.dia-fecha {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 11px;
  flex-shrink: 0;
}
.dia-card.is-today .dia-fecha {
  background: #0F766E;
  border-color: #0F766E;
}
.dia-card.is-past .dia-fecha {
  background: #FFFBEB;
  border-color: #FDE68A;
}
.dia-numero {
  font-size: 18px;
  font-weight: 700;
  color: #0F766E;
  line-height: 1;
  letter-spacing: -0.02em;
}
.dia-card.is-today .dia-numero { color: #fff; }
.dia-card.is-past .dia-numero { color: #D97706; }
.dia-mes {
  margin-top: 2px;
  font-size: 9.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #94A3B8;
  line-height: 1;
}
.dia-card.is-today .dia-mes { color: rgba(255, 255, 255, .85); }
.dia-card.is-past .dia-mes { color: #B45309; }

.dia-texto { min-width: 0; }
.dia-nombre {
  margin: 0;
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
  line-height: 1.2;
}
.dia-year {
  margin: 2px 0 0;
  font-size: 11.5px;
  color: #64748B;
  font-weight: 500;
}
.dia-card.is-past .dia-nombre { color: #78350F; }
.dia-card.is-past .dia-year { color: #B45309; }

.dia-header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.dia-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
}
.hoy-badge {
  background: #0F766E;
  color: #fff;
}
.past-badge {
  background: #FEF3C7;
  color: #B45309;
  border: 1px solid #FDE68A;
}
.dia-count {
  font-size: 12px;
  font-weight: 700;
  color: #64748B;
  background: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  border: 1px solid #E2E8F0;
  white-space: nowrap;
}

/* ═══ TURNOS ═══ */
.turnos { display: flex; flex-direction: column; }
.turno {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 16px;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid #F1F5F9;
  transition: background-color .2s ease;
}
.turno:last-child { border-bottom: none; }
.turno:hover { background: #FAFBFC; }

.turno-hora {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 68px;
  padding: 8px 10px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  flex-shrink: 0;
}
.h-inicio {
  font-size: 13.5px;
  font-weight: 700;
  color: #0F766E;
  line-height: 1.1;
  letter-spacing: -0.01em;
}
.h-fin {
  margin-top: 3px;
  font-size: 10.5px;
  font-weight: 600;
  color: #94A3B8;
  line-height: 1;
}

.turno-info { min-width: 0; }
.turno-mascota {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
.turno-cliente {
  margin: 3px 0 0;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12.5px;
  color: #64748B;
}
.turno-meta {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11.5px;
  font-weight: 600;
  color: #64748B;
  background: #F8FAFC;
  padding: 3px 9px;
  border-radius: 20px;
  border: 1px solid #E2E8F0;
}
.meta-price { color: #0F766E; border-color: #CCFBF1; background: #F0FDFA; }

.turno-side {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.estado-pill {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
  border: 1px solid;
  white-space: nowrap;
  letter-spacing: .1px;
}
.btn-estado {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 12px;
  background: #fff;
  color: #64748B;
  border: 1px solid #E2E8F0;
  border-radius: 9px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
}
.btn-estado:hover {
  border-color: #0F766E;
  color: #0F766E;
  background: #F0FDFA;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px -4px rgba(15, 118, 110, .25);
}
.btn-estado-text { display: inline; }

/* ═══ MODAL ═══ */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, .5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 100;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.modal-card {
  background: #fff;
  border-radius: 16px;
  max-width: 520px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .25);
}
.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.modal-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.modal-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #F0FDFA;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.modal-header h3 {
  margin: 0;
  font-size: 15.5px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.2;
}
.modal-header-sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748B;
}
.btn-close {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: #F1F5F9;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .2s ease;
  flex-shrink: 0;
}
.btn-close:hover { background: #E2E8F0; color: #1E293B; }

.modal-body { padding: 20px 24px 24px; }
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px 20px;
  border-top: 1px solid #E2E8F0;
}

.cita-resumen {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 6px 14px;
  margin-bottom: 18px;
}
.resumen-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 9px 0;
  border-bottom: 1px solid #E2E8F0;
}
.resumen-row:last-child { border-bottom: none; }
.resumen-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #64748B;
  white-space: nowrap;
}
.resumen-value {
  font-size: 13px;
  font-weight: 700;
  color: #1E293B;
  text-align: right;
  word-break: break-word;
}

.section-label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
  margin: 0 0 10px;
}
.required { color: #EF4444; }

.opciones {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.opcion {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: all .2s ease;
}
.opcion:hover {
  border-color: var(--op-color, #0F766E);
  background: var(--op-bg, #F0FDFA);
}
.opcion.selected {
  border-color: var(--op-color, #0F766E);
  background: var(--op-bg, #F0FDFA);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--op-color, #0F766E) 12%, transparent);
}
.opcion-radio {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #CBD5E1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: border-color .2s ease;
}
.opcion.selected .opcion-radio { border-color: var(--op-color, #0F766E); }
.opcion-radio-inner {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--op-color, #0F766E);
  animation: radioPop .2s ease;
}
@keyframes radioPop {
  0% { transform: scale(0); }
  100% { transform: scale(1); }
}
.opcion-body {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.opcion-label {
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.2;
}
.opcion.selected .opcion-label { color: var(--op-color, #0F766E); }
.opcion-desc {
  font-size: 11.5px;
  color: #64748B;
  line-height: 1.4;
}

.empty-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px;
  color: #94A3B8;
  text-align: center;
  font-size: 13px;
  border: 1px dashed #E2E8F0;
  border-radius: 10px;
}
.empty-inline p { margin: 0; }

/* ═══ TOAST ═══ */
.toast {
  position: fixed;
  top: 24px;
  right: 24px;
  padding: 12px 18px;
  border-radius: 12px;
  font-size: 13.5px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, .1);
  z-index: 200;
  font-family: inherit;
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
.slide-up-enter-active,
.slide-up-leave-active {
  transition: opacity .3s cubic-bezier(0.16, 1, 0.3, 1),
              transform .3s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(.98);
}
.slide-down-enter-active,
.slide-down-leave-active { transition: all .3s ease; }
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .estados-strip { overflow-x: auto; flex-wrap: nowrap; padding-bottom: 4px; }
  .estado-chip { flex-shrink: 0; }
}
@media (max-width: 768px) {
  .gestion-citas { padding: 16px 16px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero h1 { font-size: 22px; }

  .toolbar { padding: 12px; gap: 10px; }
  .toolbar-right { margin-left: 0; width: 100%; justify-content: space-between; }
  .rango-label { width: 100%; text-align: center; padding: 4px 0; }

  .turno {
    grid-template-columns: auto 1fr;
    gap: 12px;
    padding: 14px 16px;
  }
  .turno-side {
    grid-column: 2;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
  .btn-estado-text { display: none; }
  .btn-estado { padding: 7px 9px; }

  .dia-header { padding: 12px 16px; }
  .dia-fecha { width: 42px; height: 42px; }
  .dia-numero { font-size: 16px; }

  .modal-card { max-width: 100%; }
  .modal-footer { flex-direction: column-reverse; }
  .modal-footer .btn-primary,
  .modal-footer .btn-secondary { width: 100%; justify-content: center; }
}

@media (max-width: 480px) {
  .dia-header-right { width: 100%; justify-content: space-between; }
  .dia-count { font-size: 11px; padding: 3px 9px; }
  .resumen-row { flex-direction: column; align-items: flex-start; gap: 4px; padding: 8px 0; }
  .resumen-value { text-align: left; }
  .estado-chip { padding: 5px 10px 5px 12px; font-size: 12px; }
  .chip-count { min-width: 18px; height: 16px; font-size: 10px; }
}
</style>