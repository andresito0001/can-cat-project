<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAgenda, getVeterinarios, cambiarEstadoCita } from '@/api/citas.api'
import { TRANSICIONES_ESTADOS, ESTADO_LABEL, ESTADO_COLOR } from '@/utils/constants/estadosCita'
import {
  CalendarDays, Plus, RefreshCw, ChevronLeft, ChevronRight, AlertCircle,
  Stethoscope, PawPrint, ArrowRightCircle, Loader2, CheckCircle2, X
} from 'lucide-vue-next'

const router = useRouter()

// ── Rango de fechas ──
const modo = ref('dia') // 'dia' | 'semana' | 'rango30'
const fechaBase = ref(new Date().toISOString().split('T')[0])
const hoy = new Date().toISOString().split('T')[0]

const MODOS = [
  { id: 'dia', label: 'Día', offset: 1 },
  { id: 'semana', label: 'Semana', offset: 7 },
  { id: 'rango30', label: '30 días', offset: 30 }
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

// ── Datos ──
const citas = ref([])
const cargando = ref(false)
const error = ref('')
const toast = ref({ visible: false, message: '', type: 'success' })

async function cargarAgenda() {
  cargando.value = true
  error.value = ''
  try {
    const { data } = await getAgenda(rango.value)
    citas.value = data
  } catch (err) {
    error.value = err.response?.data?.message || 'No se pudo cargar la agenda.'
  } finally {
    cargando.value = false
  }
}

// ── Agrupación por fecha (soporta día y rangos) ──
const gruposPorFecha = computed(() => {
  const map = new Map()
  for (const c of citas.value) {
    if (!map.has(c.fecha)) map.set(c.fecha, [])
    map.get(c.fecha).push(c)
  }
  return [...map.entries()]
    .sort((a, b) => a[0].localeCompare(b[0]))
    .map(([fecha, lista]) => ({ fecha, citas: lista }))
})

const DIAS_SEMANA = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo']
const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

function fmtFecha(iso) {
  if (!iso) return ''
  const d = new Date(iso + 'T12:00:00')
  return `${d.getDate()} ${MESES[d.getMonth()]} ${d.getFullYear()} · ${DIAS_SEMANA[(d.getDay() + 6) % 7]}`
}
function fmtHora(t) {
  if (!t) return ''
  const [h, m] = t.split(':')
  const hh = Number(h) % 12 || 12
  return `${hh}:${m} ${Number(h) >= 12 ? 'PM' : 'AM'}`
}
function esVencida(iso) { return iso < hoy }

// ── Cambio de estado ──
const citaSeleccionada = ref(null)
const estadoDestino = ref(null)
const cambiandoEstado = ref(false)
const cambioError = ref('')

const opcionesEstado = computed(() =>
  citaSeleccionada.value ? (TRANSICIONES_ESTADOS[citaSeleccionada.value.estado] || []) : []
)

function abrirCambioEstado(cita) {
  citaSeleccionada.value = cita
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
    showToast('Estado actualizado correctamente')
    await cargarAgenda()
  } catch (err) {
    cambioError.value = err.response?.data?.message || 'No se pudo cambiar el estado.'
  } finally {
    cambiandoEstado.value = false
  }
}

function showToast(message, type = 'success') {
  toast.value = { visible: true, message, type }
  setTimeout(() => { toast.value.visible = false }, 4000)
}

onMounted(cargarAgenda)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Gestión de Citas</h1>
        <p class="page-subtitle">Calendario general de especialistas y turnos programados.</p>
      </div>
      <button class="btn-primary" @click="router.push('/recepcion/citas/nueva')">
        <Plus :size="18" /> Nueva Reserva
      </button>
    </div>

    <!-- Toolbar: rango + navegación -->
    <div class="toolbar">
      <div class="modo-chips">
        <button v-for="m in MODOS" :key="m.id" class="chip" :class="{ active: modo === m.id }"
          @click="cambiarModo(m.id)">{{ m.label }}</button>
      </div>
      <div class="nav-rango">
        <button class="nav-btn" @click="moverRango(-1)"><ChevronLeft :size="18" /></button>
        <button class="nav-btn today" @click="fechaBase = hoy; cargarAgenda()">Hoy</button>
        <button class="nav-btn" @click="moverRango(1)"><ChevronRight :size="18" /></button>
      </div>
      <span class="rango-label">
        {{ rango.fechaInicio === rango.fechaFin
            ? fmtFecha(rango.fechaInicio)
            : `${rango.fechaInicio} → ${rango.fechaFin}` }}
      </span>
      <button class="btn-secondary" :disabled="cargando" @click="cargarAgenda">
        <RefreshCw :size="16" :class="{ spin: cargando }" /> Actualizar
      </button>
      <span class="counter">{{ citas.length }} cita(s)</span>
    </div>

    <div v-if="error" class="alert error"><AlertCircle :size="18" /> {{ error }}</div>

    <div v-if="cargando" class="state"><span class="spinner" /> Cargando agenda...</div>

    <div v-else-if="citas.length === 0" class="state empty">
      <CalendarDays :size="48" />
      <h3>No hay turnos en este rango</h3>
      <p>Usa "Nueva Reserva" para agendar y cobrar una cita en mostrador.</p>
    </div>

    <div v-else class="dias">
      <div v-for="grupo in gruposPorFecha" :key="grupo.fecha" class="dia-card">
        <div class="dia-header" :class="{ vencida: esVencida(grupo.fecha) }">
          <CalendarDays :size="16" />
          <span>{{ fmtFecha(grupo.fecha) }}</span>
          <span class="dia-count">{{ grupo.citas.length }} turno(s)</span>
        </div>
        <div v-for="c in grupo.citas" :key="c.idCita" class="turno">
          <div class="turno-hora">
            <span class="h-inicio">{{ fmtHora(c.horaInicio) }}</span>
            <span class="h-fin">{{ fmtHora(c.horaFin) }}</span>
          </div>
          <div class="turno-info">
            <strong><PawPrint :size="13" /> {{ c.nombreMascota }}</strong> · {{ c.nombreCliente }}
            <div class="turno-meta">
              <Stethoscope :size="12" /> {{ c.nombreVeterinario }} · {{ c.nombreServicio }} · ${{ Number(c.costoUsd).toFixed(2) }}
            </div>
          </div>
          <span class="estado" :style="{ background: c.colorUi || ESTADO_COLOR[c.estado] }">
            {{ (ESTADO_LABEL[c.estado] || c.estado) }}
          </span>
          <button v-if="opcionesDe(c)" class="estado-btn" title="Cambiar estado"
            @click="abrirCambioEstado(c)">
            <ArrowRightCircle :size="16" />
          </button>
        </div>
      </div>
    </div>

    <!-- Modal cambio de estado -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="citaSeleccionada" class="modal-overlay" @click.self="citaSeleccionada = null">
          <div class="modal-card">
            <div class="modal-header">
              <h3>Cambiar estado de la cita</h3>
              <button class="btn-close" @click="citaSeleccionada = null"><X :size="18" /></button>
            </div>
            <div class="modal-body">
              <div class="cita-actual">
                <span>{{ citaSeleccionada.nombreCliente }} · {{ citaSeleccionada.nombreMascota }}</span>
                <span>{{ fmtFecha(citaSeleccionada.fecha) }} · {{ fmtHora(citaSeleccionada.horaInicio) }}</span>
                <span class="estado-actual">Estado actual:
                  <strong>{{ ESTADO_LABEL[citaSeleccionada.estado] || citaSeleccionada.estado }}</strong>
                </span>
              </div>

              <label class="section-label">Nuevo estado</label>
              <div v-if="cambioError" class="alert error small">{{ cambioError }}</div>
              <div class="opciones">
                <button v-for="op in opcionesEstado" :key="op" class="opcion"
                  :class="{ selected: estadoDestino === op, [op]: true }"
                  @click="estadoDestino = op">
                  {{ ESTADO_LABEL[op] || op }}
                </button>
              </div>
            </div>
            <div class="modal-footer">
              <button class="btn-secondary" :disabled="cambiandoEstado" @click="citaSeleccionada = null">Cancelar</button>
              <button class="btn-primary" :disabled="!estadoDestino || cambiandoEstado" @click="aplicarCambioEstado">
                <Loader2 v-if="cambiandoEstado" :size="16" class="spin" />
                <CheckCircle2 v-else :size="16" /> Aplicar
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- Toast -->
    <Transition name="slide-down">
      <div v-if="toast.visible" class="toast" :class="toast.type">
        <CheckCircle2 :size="18" /> {{ toast.message }}
      </div>
    </Transition>
  </div>
</template>

<script>
function opcionesDe(cita) {
  return (TRANSICIONES_ESTADOS[cita.estado] || []).length > 0
}
</script>


<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; font-family: 'Inter','Segoe UI',Roboto,sans-serif; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; flex-wrap: wrap; }
.page-title { font-size: 24px; font-weight: 700; color: #1E293B; margin: 0; letter-spacing: -0.03em; }
.page-subtitle { color: #64748B; font-size: 14px; margin: 4px 0 0; }
.btn-primary, .btn-secondary { display: inline-flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit; transition: all .2s; }
.btn-primary { background: #0F766E; color: #fff; border: none; }
.btn-primary:hover { background: #115E59; transform: translateY(-1px); }
.btn-secondary { background: #fff; color: #475569; border: 1.5px solid #E2E8F0; }
.toolbar { display: flex; align-items: center; gap: 14px; flex-wrap: wrap; }
.modo-chips { display: flex; background: #fff; border: 1.5px solid #E2E8F0; border-radius: 10px; overflow: hidden; }
.chip { padding: 8px 16px; border: none; background: #fff; font-size: 13px; font-weight: 600; color: #64748B; cursor: pointer; font-family: inherit; transition: all .15s; }
.chip.active { background: #0F766E; color: #fff; }
.nav-rango { display: flex; gap: 6px; }
.nav-btn { width: 36px; height: 36px; border: 1.5px solid #E2E8F0; border-radius: 10px; background: #fff; color: #64748B; display: flex; align-items: center; justify-content: center; cursor: pointer; font-family: inherit; }
.nav-btn:hover { border-color: #0F766E; color: #0F766E; }
.nav-btn.today { width: auto; padding: 0 14px; font-size: 13px; font-weight: 600; }
.rango-label { font-size: 13px; color: #475569; font-weight: 600; }
.counter { color: #64748B; font-size: 13px; margin-left: auto; }
.alert { display: flex; align-items: center; gap: 8px; padding: 12px 16px; border-radius: 8px; font-size: 13px; font-weight: 500; }
.alert.error { color: #EF4444; background: #FEF2F2; border: 1px solid #FECACA; }
.alert.error.small { margin-bottom: 12px; }
.state { padding: 64px 24px; text-align: center; color: #64748B; background: #fff; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,.03), 0 20px 40px -4px rgba(15,118,110,.08); }
.state.empty { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.state.empty h3 { font-size: 18px; color: #1E293B; margin: 0; } .state.empty p { font-size: 14px; margin: 0; }
.spinner { width: 18px; height: 18px; border: 2px solid rgba(15,118,110,.2); border-top-color: #0F766E; border-radius: 50%; animation: spin .6s linear infinite; display: inline-block; vertical-align: middle; margin-right: 8px; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.dias { display: flex; flex-direction: column; gap: 16px; }
.dia-card { background: #fff; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,.03), 0 20px 40px -4px rgba(15,118,110,.08); overflow: hidden; }
.dia-header { padding: 14px 20px; background: #F8FAFC; border-bottom: 1px solid #E2E8F0; display: flex; align-items: center; gap: 8px; font-weight: 700; color: #1E293B; font-size: 14px; }
.dia-header.vencida { color: #B45309; background: #FFFBEB; }
.dia-count { margin-left: auto; font-size: 12px; font-weight: 500; color: #64748B; }
.turno { display: flex; align-items: center; gap: 14px; padding: 14px 20px; border-bottom: 1px solid #F1F5F9; }
.turno:last-child { border-bottom: none; }
.turno-hora { display: flex; flex-direction: column; align-items: center; min-width: 70px; }
.h-inicio { font-weight: 700; color: #0F766E; font-size: 14px; }
.h-fin { color: #94A3B8; font-size: 11px; }
.turno-info { flex: 1; font-size: 13px; color: #475569; display: flex; align-items: center; gap: 4px; flex-wrap: wrap; }
.turno-info strong { display: inline-flex; align-items: center; gap: 4px; color: #1E293B; }
.turno-meta { width: 100%; color: #94A3B8; font-size: 12px; display: flex; align-items: center; gap: 4px; }
.estado { font-size: 11px; font-weight: 600; color: #fff; padding: 3px 10px; border-radius: 20px; white-space: nowrap; }
.estado-btn { width: 34px; height: 34px; border: 1.5px solid #E2E8F0; border-radius: 10px; background: #fff; color: #64748B; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all .2s; }
.estado-btn:hover { border-color: #0F766E; color: #0F766E; background: #F0FDFA; }
.modal-overlay { position: fixed; inset: 0; background: rgba(15,23,42,.5); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; padding: 24px; z-index: 100; }
.modal-card { background: #fff; border-radius: 16px; max-width: 460px; width: 100%; box-shadow: 0 25px 50px -12px rgba(0,0,0,.25); }
.modal-header { padding: 18px 24px; border-bottom: 1px solid #E2E8F0; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { font-size: 16px; font-weight: 700; color: #1E293B; margin: 0; }
.btn-close { width: 32px; height: 32px; border-radius: 8px; border: none; background: #F1F5F9; color: #64748B; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.modal-body { padding: 24px; }
.cita-actual { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 12px 16px; display: flex; flex-direction: column; gap: 4px; font-size: 13px; color: #475569; margin-bottom: 16px; }
.estado-actual strong { color: #1E293B; }
.section-label { display: block; font-size: 13px; font-weight: 600; color: #475569; margin-bottom: 10px; }
.opciones { display: flex; flex-direction: column; gap: 10px; }
.opcion { padding: 12px 16px; border: 1.5px solid #E2E8F0; border-radius: 10px; background: #fff; font-size: 14px; font-weight: 600; color: #1E293B; cursor: pointer; font-family: inherit; text-align: left; transition: all .15s; }
.opcion:hover { border-color: #0F766E; background: #F0FDFA; }
.opcion.selected { border-color: #0F766E; background: #F0FDFA; box-shadow: 0 0 0 3px rgba(15,118,110,.1); }
.opcion.Cancelada { border-left: 4px solid #DC3545; }
.opcion.Confirmada, .opcion.Pagada { border-left: 4px solid #28A745; }
.opcion.En_Atencion { border-left: 4px solid #FD7E14; }
.opcion.Completada { border-left: 4px solid #6C757D; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 16px 24px 20px; border-top: 1px solid #E2E8F0; }
.toast { position: fixed; top: 24px; right: 24px; padding: 14px 20px; border-radius: 12px; font-size: 14px; font-weight: 500; display: flex; align-items: center; gap: 10px; z-index: 200; background: #ECFDF5; color: #059669; border: 1px solid #A7F3D0; }
.fade-enter-active, .fade-leave-active { transition: opacity .25s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-down-enter-active, .slide-down-leave-active { transition: all .3s; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-20px); }
</style>