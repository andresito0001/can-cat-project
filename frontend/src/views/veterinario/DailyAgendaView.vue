<template>
  <div class="agenda-view">
    <ToastContainer />

    <!-- ═══ HEADER ═══ -->
    <header class="page-top">
      <div class="page-top-info">
        <span class="page-top-eyebrow">
          <CalendarCheck2 :size="12" />
          Mi agenda
        </span>
        <h1 class="page-top-title">{{ tituloRango }}</h1>
        <p class="page-top-sub">{{ subtituloRango }}</p>
      </div>
      <div class="page-top-actions">
        <button class="quick-action" type="button" @click="irAHoy">
          <Calendar :size="14" />
          Hoy
        </button>
        <button class="quick-action" type="button" :disabled="cargando" @click="recargar">
          <Loader2 v-if="cargando" :size="14" class="spin" />
          <RefreshCw v-else :size="14" />
          Actualizar
        </button>
      </div>
    </header>

    <!-- ═══ WEEK STRIP ═══ -->
    <nav class="week-strip" aria-label="Navegación semanal">
      <button class="week-nav" type="button" :disabled="cargando" aria-label="Semana anterior" @click="moverSemana(-1)">
        <ChevronLeft :size="16" />
      </button>

      <div class="week-days">
        <button
          v-for="d in diasSemana"
          :key="d.iso"
          type="button"
          class="week-day"
          :class="{
            'is-selected': d.iso === fechaBase,
            'is-today': d.iso === hoyISO(),
            'is-weekend': d.isWeekend,
          }"
          @click="seleccionarDia(d.iso)"
        >
          <span class="week-day-name">{{ d.diaSemana }}</span>
          <span class="week-day-num">{{ d.dia }}</span>
          <span v-if="d.isToday" class="week-day-dot" />
        </button>
      </div>

      <button class="week-nav" type="button" :disabled="cargando" aria-label="Semana siguiente" @click="moverSemana(1)">
        <ChevronRight :size="16" />
      </button>
    </nav>

    <!-- ═══ CONTROLES ═══ -->
    <section class="agenda-controls">
      <div class="agenda-search">
        <Search :size="15" class="agenda-search-icon" />
        <input
          v-model="buscando"
          type="text"
          class="agenda-search-input"
          placeholder="Buscar por mascota, dueño, cédula o servicio…"
        />
        <button v-if="buscando" class="agenda-search-clear" type="button" aria-label="Limpiar" @click="buscando = ''">
          <X :size="13" />
        </button>
      </div>

      <div class="agenda-filters">
        <button
          v-for="f in FILTROS"
          :key="f.value"
          type="button"
          class="filter-chip"
          :class="{ 'is-active': filtroEstado === f.value }"
          @click="filtroEstado = f.value"
        >
          {{ f.label }}
          <span class="filter-chip-count">{{ contarPorFiltro(f.value) }}</span>
        </button>
      </div>
    </section>

    <!-- ═══ KPIs ═══ -->
    <section class="stats-strip">
      <article class="stat stat-brand">
        <div class="stat-icon"><CalendarDays :size="18" /></div>
        <div class="stat-content">
          <p class="stat-value">{{ citasFiltradas.length }}</p>
          <p class="stat-label">{{ vista === 'dia' ? 'Citas del día' : 'Citas del período' }}</p>
        </div>
      </article>
      <article class="stat stat-warning">
        <div class="stat-icon"><Clock3 :size="18" /></div>
        <div class="stat-content">
          <p class="stat-value">{{ resumen.porAtender }}</p>
          <p class="stat-label">Por atender</p>
        </div>
      </article>
      <article class="stat stat-success">
        <div class="stat-icon"><CheckCircle2 :size="18" /></div>
        <div class="stat-content">
          <p class="stat-value">{{ resumen.atendidas }}</p>
          <p class="stat-label">Atendidas</p>
        </div>
      </article>
      <article class="stat stat-neutral">
        <div class="stat-icon"><AlertCircle :size="18" /></div>
        <div class="stat-content">
          <p class="stat-value">{{ resumen.canceladas }}</p>
          <p class="stat-label">Canceladas</p>
        </div>
      </article>
    </section>

    <!-- ═══ CONTENIDO ═══ -->
    <div v-if="cargando" class="loading-box">
      <span class="spinner spinner-lg" />
      <p>Cargando agenda…</p>
    </div>

    <AppEmptyState
      v-else-if="!citasFiltradas.length"
      :icon="citas.length === 0 ? Inbox : Search"
      :title="citas.length === 0 ? 'Sin citas en este período' : 'Sin resultados con esos filtros'"
      :description="citas.length === 0
        ? 'Cuando se agenden citas contigo aparecerán en esta vista.'
        : 'Prueba con otro término de búsqueda o cambia el filtro de estado.'"
    >
      <template #action>
        <button v-if="citas.length > 0" class="agenda-clear-btn" type="button" @click="limpiarFiltros">
          <X :size="14" /> Limpiar filtros
        </button>
      </template>
    </AppEmptyState>

    <template v-else-if="vista === 'dia'">
      <!-- ═══ TIMELINE DEL DÍA ═══ -->
      <div class="day-timeline">
        <article
          v-for="cita in citasFiltradas"
          :key="cita.idCita"
          class="timeline-item"
          :class="{ 'is-done': cita.atendida }"
        >
          <div class="timeline-time">
            <span class="time-start">{{ fmtHora(cita.horaInicio) }}</span>
            <span class="time-end">{{ fmtHora(cita.horaFin) }}</span>
          </div>

          <div class="timeline-rail">
            <span class="rail-dot" :style="{ background: colorEstado(cita) }" />
          </div>

          <div class="timeline-card">
            <header class="card-head">
              <div class="card-head-main">
                <h3 class="card-pet">
                  <PawPrint :size="15" />
                  <span>{{ cita.mascotaNombre }}</span>
                </h3>
                <p class="card-pet-meta">
                  {{ cita.especie }}
                  <template v-if="cita.raza"> · {{ cita.raza }}</template>
                </p>
              </div>

              <div class="card-head-side">
                <span class="status-pill" :style="estiloEstado(cita.estado)">
                  {{ formatEstado(cita.estado) }}
                </span>
                <span v-if="cita.atendida" class="status-done">
                  <CheckCircle2 :size="11" />
                  Atendida
                </span>
              </div>
            </header>

            <div class="card-grid">
              <div class="field">
                <span class="field-label"><User :size="11" /> Dueño</span>
                <span class="field-value">{{ cita.clienteNombre }}</span>
              </div>
              <div class="field">
                <span class="field-label"><FileText :size="11" /> Motivo</span>
                <span class="field-value">{{ cita.motivoConsulta }}</span>
              </div>
              <div class="field">
                <span class="field-label"><Stethoscope :size="11" /> Servicio</span>
                <span class="field-value">{{ cita.servicioNombre }}</span>
              </div>
              <div class="field">
                <span class="field-label"><Phone :size="11" /> Contacto</span>
                <span class="field-value">{{ cita.clienteTelefono || '—' }}</span>
              </div>
            </div>

            <footer class="card-foot">
              <button
                v-if="!cita.atendida"
                class="btn-atender"
                type="button"
                @click="irAAtender(cita)"
              >
                <Stethoscope :size="14" />
                Iniciar atención
                <ArrowRight :size="14" />
              </button>
              <span v-else class="badge-completada">
                <CheckCircle2 :size="13" /> Consulta completada
              </span>

              <button
                class="btn-link"
                type="button"
                :disabled="resolviendoHistorial === cita.idCita"
                @click="verHistorial(cita)"
              >
                <History :size="13" />
                {{ resolviendoHistorial === cita.idCita ? 'Buscando…' : 'Ver historial' }}
              </button>
            </footer>
          </div>
        </article>
      </div>
    </template>

    <!-- ═══ AGRUPADO POR DÍA (semana/mes) ═══ -->
    <template v-else>
      <div class="day-groups">
        <section
          v-for="grupo in gruposPorDia"
          :key="grupo.fecha"
          class="day-group"
          :class="{ 'is-today': grupo.fecha === hoyISO() }"
        >
          <header class="day-group-header">
            <div class="day-group-date">
              <span class="dg-name">{{ nombreDiaLargo(grupo.fecha) }}</span>
              <span class="dg-full">{{ fmtFechaCorta(grupo.fecha) }}</span>
            </div>
            <div class="day-group-meta">
              <span class="dg-count">
                {{ grupo.citas.length }} {{ grupo.citas.length === 1 ? 'cita' : 'citas' }}
              </span>
              <span v-if="grupo.porAtender > 0" class="dg-alert">
                {{ grupo.porAtender }} por atender
              </span>
            </div>
          </header>

          <ul class="day-group-list">
            <li
              v-for="cita in grupo.citas"
              :key="cita.idCita"
              class="group-row"
              :class="{ 'is-done': cita.atendida }"
            >
              <div class="gr-time">{{ fmtHora(cita.horaInicio) }}</div>
              <div class="gr-accent" :style="{ background: colorEstado(cita) }" />
              <div class="gr-main">
                <p class="gr-pet">
                  <PawPrint :size="12" />
                  <strong>{{ cita.mascotaNombre }}</strong>
                  <span class="gr-species">{{ cita.especie }}</span>
                </p>
                <p class="gr-meta">
                  <User :size="11" /> {{ cita.clienteNombre }}
                  <span class="gr-sep">·</span>
                  {{ cita.motivoConsulta }}
                </p>
              </div>
              <div class="gr-status">
                <span class="status-pill-sm" :style="estiloEstado(cita.estado)">
                  {{ formatEstado(cita.estado) }}
                </span>
              </div>
              <div class="gr-actions">
                <button
                  v-if="!cita.atendida"
                  class="btn-atender-sm"
                  type="button"
                  @click="irAAtender(cita)"
                >
                  Atender
                </button>
                <button
                  class="btn-icon-sm"
                  type="button"
                  :disabled="resolviendoHistorial === cita.idCita"
                  title="Ver historial"
                  @click="verHistorial(cita)"
                >
                  <History :size="13" />
                </button>
              </div>
            </li>
          </ul>
        </section>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ChevronLeft, ChevronRight, Calendar, CalendarDays, CalendarCheck2,
  CheckCircle2, Clock3, FileText, History, Inbox, Loader2, PawPrint,
  Phone, RefreshCw, Search, Stethoscope, User, X, AlertCircle, ArrowRight,
} from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import { getAgendaVet, getCitaContexto } from '@/api/atenciones.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { ESTADO_COLOR } from '@/utils/constants/estadosCita'
import { hoyISO } from '@/utils/fecha'

const router = useRouter()
const { toastError } = useToast()

/* ═══════════════════════════════════════════════════════════════
   CONSTANTES
   ═══════════════════════════════════════════════════════════════ */
const DIAS_CORTOS = ['Dom','Lun','Mar','Mié','Jue','Vie','Sáb']
const DIAS_LARGOS = ['Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado']
const MESES = ['enero','febrero','marzo','abril','mayo','junio','julio','agosto','septiembre','octubre','noviembre','diciembre']
const MESES_CORTOS = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']

const FILTROS = [
  { value: 'todas',       label: 'Todas' },
  { value: 'por_atender', label: 'Por atender' },
  { value: 'atendidas',   label: 'Atendidas' },
  { value: 'canceladas',  label: 'Canceladas' },
]

/* ═══════════════════════════════════════════════════════════════
   ESTADO
   ═══════════════════════════════════════════════════════════════ */
const vista = ref('dia')
const fechaBase = ref(hoyISO())
const citas = ref([])
const cargando = ref(false)
const buscando = ref('')
const filtroEstado = ref('todas')
const resolviendoHistorial = ref(null)

const cacheFecha = new Map()

/* ═══════════════════════════════════════════════════════════════
   FECHAS — helpers puros
   ═══════════════════════════════════════════════════════════════ */
function isoDe(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}
function parseISO(iso) {
  const [y, m, d] = iso.split('-').map(Number)
  return new Date(y, m - 1, d)
}
function addDays(iso, n) {
  const d = parseISO(iso)
  d.setDate(d.getDate() + n)
  return isoDe(d)
}
function inicioSemana(iso) {
  const d = parseISO(iso)
  const day = d.getDay()
  const offset = day === 0 ? -6 : 1 - day
  return addDays(iso, offset)
}
function finSemana(iso) {
  return addDays(inicioSemana(iso), 6)
}

/* ═══════════════════════════════════════════════════════════════
   WEEK STRIP
   ═══════════════════════════════════════════════════════════════ */
const diasSemana = computed(() => {
  const inicio = inicioSemana(fechaBase.value)
  return Array.from({ length: 7 }, (_, i) => {
    const iso = addDays(inicio, i)
    const d = parseISO(iso)
    return {
      iso,
      dia: d.getDate(),
      diaSemana: DIAS_CORTOS[d.getDay()],
      isToday: iso === hoyISO(),
      isWeekend: d.getDay() === 0 || d.getDay() === 6,
    }
  })
})

function seleccionarDia(iso) {
  fechaBase.value = iso
  if (vista.value !== 'dia') vista.value = 'dia'
}

function moverSemana(delta) {
  fechaBase.value = addDays(fechaBase.value, delta * 7)
  cargar()
}

/* ═══════════════════════════════════════════════════════════════
   RANGO
   ═══════════════════════════════════════════════════════════════ */
const rango = computed(() => {
  const base = fechaBase.value
  return { dias: [base] }
})

const tituloRango = computed(() => {
  const d = parseISO(fechaBase.value)
  return `${DIAS_LARGOS[d.getDay()]}, ${d.getDate()} de ${MESES[d.getMonth()]}`
})

const subtituloRango = computed(() => {
  const total = citas.value.length
  const atendidas = citas.value.filter((c) => c.atendida).length
  if (total === 0) return 'Sin citas agendadas'
  return `${total} ${total === 1 ? 'cita' : 'citas'} · ${atendidas} ${atendidas === 1 ? 'atendida' : 'atendidas'}`
})

/* ═══════════════════════════════════════════════════════════════
   CARGA
   ═══════════════════════════════════════════════════════════════ */
async function cargarFecha(iso, forzar = false) {
  if (!forzar && cacheFecha.has(iso)) return cacheFecha.get(iso)
  const data = await getAgendaVet(iso)
  cacheFecha.set(iso, data || [])
  return data || []
}

async function cargar() {
  cargando.value = true
  try {
    const resultados = await Promise.all(rango.value.dias.map((d) => cargarFecha(d)))
    citas.value = resultados.flat()
  } catch (err) {
    toastError(getApiErrorMessage(err) || 'No se pudo cargar la agenda')
    citas.value = []
  } finally {
    cargando.value = false
  }
}

async function recargar() {
  cacheFecha.clear()
  await cargar()
}

function irAHoy() {
  fechaBase.value = hoyISO()
  cargar()
}

/* ═══════════════════════════════════════════════════════════════
   FILTROS
   ═══════════════════════════════════════════════════════════════ */
function normalizar(s) {
  return String(s || '').trim().toLowerCase()
    .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
}

const citasFiltradas = computed(() => {
  const q = normalizar(buscando.value)
  return citas.value
    .filter((c) => {
      if (filtroEstado.value === 'todas') return true
      if (filtroEstado.value === 'por_atender') {
        return !c.atendida && !['Cancelada', 'Completada'].includes(c.estado)
      }
      if (filtroEstado.value === 'atendidas') {
        return c.atendida || c.estado === 'Completada'
      }
      if (filtroEstado.value === 'canceladas') return c.estado === 'Cancelada'
      return true
    })
    .filter((c) => {
      if (!q) return true
      return [c.mascotaNombre, c.clienteNombre, c.clienteDocumento, c.servicioNombre, c.motivoConsulta]
        .some((v) => normalizar(v).includes(q))
    })
    .sort((a, b) => {
      const k = `${a.fechaCita} ${a.horaInicio}`
      const k2 = `${b.fechaCita} ${b.horaInicio}`
      return k.localeCompare(k2)
    })
})

function contarPorFiltro(tipo) {
  return citas.value.filter((c) => {
    if (tipo === 'todas') return true
    if (tipo === 'por_atender') {
      return !c.atendida && !['Cancelada', 'Completada'].includes(c.estado)
    }
    if (tipo === 'atendidas') return c.atendida || c.estado === 'Completada'
    if (tipo === 'canceladas') return c.estado === 'Cancelada'
    return true
  }).length
}

const resumen = computed(() => ({
  porAtender: citas.value.filter((c) => !c.atendida && !['Cancelada', 'Completada'].includes(c.estado)).length,
  atendidas: citas.value.filter((c) => c.atendida || c.estado === 'Completada').length,
  canceladas: citas.value.filter((c) => c.estado === 'Cancelada').length,
}))

function limpiarFiltros() {
  buscando.value = ''
  filtroEstado.value = 'todas'
}

/* ═══════════════════════════════════════════════════════════════
   AGRUPACIÓN POR DÍA (semana/mes)
   ═══════════════════════════════════════════════════════════════ */
const gruposPorDia = computed(() => {
  const map = new Map()
  for (const c of citasFiltradas.value) {
    const f = String(c.fechaCita).slice(0, 10)
    if (!map.has(f)) map.set(f, [])
    map.get(f).push(c)
  }
  return [...map.entries()]
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([fecha, lista]) => ({
      fecha,
      citas: lista.sort((a, b) => a.horaInicio.localeCompare(b.horaInicio)),
      porAtender: lista.filter((c) => !c.atendida && c.estado !== 'Cancelada').length,
    }))
})

/* ═══════════════════════════════════════════════════════════════
   ACCIONES
   ═══════════════════════════════════════════════════════════════ */
function irAAtender(cita) {
  router.push(`/veterinario/atencion/${cita.idCita}`)
}

async function verHistorial(cita) {
  try {
    resolviendoHistorial.value = cita.idCita
    const ctx = await getCitaContexto(cita.idCita)
    router.push({
      path: '/veterinario/historiales',
      query: { mascota: ctx.mascota.idMascota, nombre: ctx.mascota.nombre },
    })
  } catch (err) {
    toastError(getApiErrorMessage(err))
  } finally {
    resolviendoHistorial.value = null
  }
}

/* ═══════════════════════════════════════════════════════════════
   HELPERS DE FORMATO
   ═══════════════════════════════════════════════════════════════ */
function fmtHora(t) {
  if (!t) return ''
  const [h, m] = String(t).split(':')
  const hh = Number(h) % 12 || 12
  return `${hh}:${m} ${Number(h) >= 12 ? 'PM' : 'AM'}`
}

function fmtFechaCorta(iso) {
  const d = parseISO(iso)
  return `${d.getDate()} ${MESES_CORTOS[d.getMonth()]}`
}

function nombreDiaLargo(iso) {
  return DIAS_LARGOS[parseISO(iso).getDay()]
}

function formatEstado(estado) {
  return String(estado || '').replaceAll('_', ' ')
}

function colorEstado(cita) {
  return cita.estadoColor || ESTADO_COLOR[cita.estado] || 'var(--neutral-500)'
}

function estiloEstado(estado) {
  const color = ESTADO_COLOR[estado] || 'var(--neutral-500)'
  return { color, borderColor: color }
}

/* ═══════════════════════════════════════════════════════════════
   CICLO DE VIDA
   ═══════════════════════════════════════════════════════════════ */
watch(fechaBase, cargar)
onMounted(cargar)
</script>

<style scoped>
.agenda-view {
  max-width: 1280px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6) var(--space-12);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* ═══ HEADER ═══ */
.page-top {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-6);
  flex-wrap: wrap;
}
.page-top-info { min-width: 0; }
.page-top-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin: 0 0 var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--brand-700);
}
.page-top-title {
  margin: 0 0 var(--space-1);
  font-size: var(--text-5xl);
  font-weight: var(--font-bold);
  letter-spacing: var(--tracking-tight);
  line-height: 1.1;
  color: var(--text-primary);
  text-transform: capitalize;
}
.page-top-sub {
  margin: 0;
  font-size: var(--text-md);
  color: var(--text-secondary);
}
.page-top-actions {
  display: flex;
  gap: var(--space-2);
  align-items: center;
}
.quick-action {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--neutral-700);
  border-radius: var(--radius-lg);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  font-family: inherit;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.quick-action:hover:not(:disabled) {
  border-color: var(--brand-200);
  background: var(--brand-50);
  color: var(--brand-700);
}
.quick-action:disabled { opacity: 0.5; cursor: not-allowed; }

/* ═══ WEEK STRIP ═══ */
.week-strip {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-3);
  align-items: center;
  padding: var(--space-3);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
}
.week-nav {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.week-nav:hover:not(:disabled) {
  border-color: var(--brand-200);
  background: var(--brand-50);
  color: var(--brand-700);
}
.week-nav:disabled { opacity: 0.4; cursor: not-allowed; }

.week-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: var(--space-1);
}
.week-day {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: var(--space-3) var(--space-2);
  border: none;
  background: transparent;
  border-radius: var(--radius-lg);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--duration-fast) var(--ease-out);
}
.week-day:hover { background: var(--bg-surface-alt); }
.week-day.is-selected {
  background: var(--brand-700);
  color: var(--text-inverse);
  box-shadow: 0 4px 12px -4px rgba(15, 118, 110, 0.4);
}
.week-day.is-selected .week-day-name,
.week-day.is-selected .week-day-num { color: var(--text-inverse); }
.week-day.is-weekend:not(.is-selected) .week-day-name { color: var(--text-tertiary); }

.week-day-name {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
  transition: color var(--duration-fast);
}
.week-day-num {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1;
  transition: color var(--duration-fast);
}
.week-day.is-today:not(.is-selected) .week-day-num { color: var(--brand-700); }
.week-day-dot {
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--brand-500);
}
.week-day.is-selected .week-day-dot { background: rgba(255, 255, 255, 0.8); }

/* ═══ CONTROLES ═══ */
.agenda-controls {
  display: flex;
  gap: var(--space-4);
  align-items: center;
  flex-wrap: wrap;
}
.agenda-search {
  position: relative;
  flex: 1 1 320px;
  max-width: 480px;
}
.agenda-search-icon {
  position: absolute;
  left: var(--space-4);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  pointer-events: none;
}
.agenda-search-input {
  width: 100%;
  padding: var(--space-3) var(--space-10) var(--space-3) 42px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  border-radius: var(--radius-xl);
  font-size: var(--text-md);
  font-family: inherit;
  color: var(--text-primary);
  outline: none;
  box-sizing: border-box;
  transition: all var(--duration-fast) var(--ease-out);
}
.agenda-search-input::placeholder { color: var(--text-tertiary); }
.agenda-search-input:focus {
  border-color: var(--brand-700);
  box-shadow: 0 0 0 4px var(--brand-100);
}
.agenda-search-clear {
  position: absolute;
  right: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
}
.agenda-search-clear:hover { color: var(--neutral-600); background: var(--neutral-100); }

.agenda-filters {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}
.filter-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3) var(--space-2) var(--space-4);
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
.filter-chip:hover { border-color: var(--brand-200); color: var(--brand-700); }
.filter-chip.is-active {
  background: var(--brand-700);
  color: var(--text-inverse);
  border-color: var(--brand-700);
  box-shadow: 0 2px 8px rgba(15, 118, 110, 0.25);
}
.filter-chip-count {
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
.filter-chip.is-active .filter-chip-count {
  background: rgba(255, 255, 255, 0.22);
  color: var(--text-inverse);
}

/* ═══ STATS STRIP ═══ */
.stats-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}
.stat {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5) var(--space-6);
  border-right: 1px solid var(--border-subtle);
  transition: background-color var(--duration-fast) var(--ease-out);
}
.stat:last-child { border-right: none; }
.stat:hover { background: var(--bg-surface-alt); }
.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-brand .stat-icon   { background: var(--brand-50);   color: var(--brand-700); }
.stat-warning .stat-icon { background: var(--warning-50); color: var(--warning-600); }
.stat-success .stat-icon { background: var(--success-50); color: var(--success-600); }
.stat-neutral .stat-icon { background: var(--neutral-100); color: var(--text-secondary); }
.stat-content { min-width: 0; }
.stat-value {
  margin: 0;
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1;
  letter-spacing: var(--tracking-tight);
  font-variant-numeric: tabular-nums;
}
.stat-label {
  margin: var(--space-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}

/* ═══ TIMELINE DEL DÍA ═══ */
.day-timeline {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.timeline-item {
  display: grid;
  grid-template-columns: 80px 24px 1fr;
  gap: var(--space-3);
  align-items: stretch;
}
.timeline-time {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding-top: var(--space-4);
  gap: 3px;
}
.time-start {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
  line-height: 1;
}
.time-end {
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--text-tertiary);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
  line-height: 1;
}
.timeline-rail {
  position: relative;
  display: flex;
  justify-content: center;
}
.timeline-rail::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--border-subtle);
  transform: translateX(-50%);
}
.timeline-item:first-child .timeline-rail::before { top: 20px; }
.timeline-item:last-child .timeline-rail::before { bottom: calc(100% - 20px); }
.rail-dot {
  position: relative;
  z-index: 1;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-top: 20px;
  box-shadow: 0 0 0 3px var(--bg-page);
}

.timeline-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  transition: all var(--duration-base) var(--ease-out);
  box-shadow: var(--shadow-xs);
}
.timeline-card:hover {
  border-color: var(--brand-200);
  box-shadow: var(--shadow-md);
}
.timeline-item.is-done .timeline-card {
  background: var(--bg-surface-alt);
  opacity: 0.85;
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  flex-wrap: wrap;
}
.card-head-main { min-width: 0; }
.card-pet {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-1);
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
  line-height: 1.2;
}
.card-pet svg { color: var(--brand-700); }
.card-pet-meta {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}

.card-head-side {
  display: flex;
  gap: var(--space-2);
  align-items: center;
  flex-wrap: wrap;
  flex-shrink: 0;
}
.status-pill {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  border: 1px solid;
  white-space: nowrap;
  background: transparent;
}
.status-done {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  background: var(--success-50);
  color: var(--success-700);
  border: 1px solid var(--success-200);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-3) var(--space-5);
}
.field { display: flex; flex-direction: column; gap: 3px; min-width: 0; }
.field-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
}
.field-value {
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--neutral-700);
  line-height: var(--leading-snug);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-subtle);
}
.btn-atender {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  background: var(--brand-700);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-lg);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.btn-atender:hover {
  background: var(--brand-800);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, 0.4);
}
.badge-completada {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--success-600);
}
.btn-link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  background: none;
  border: none;
  color: var(--brand-700);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  transition: background-color var(--duration-fast) var(--ease-out);
  margin-left: auto;
}
.btn-link:hover:not(:disabled) { background: var(--brand-50); }
.btn-link:disabled { color: var(--text-tertiary); cursor: not-allowed; }

/* ═══ GRUPOS POR DÍA ═══ */
.day-groups { display: flex; flex-direction: column; gap: var(--space-5); }
.day-group {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-xs);
}
.day-group.is-today {
  border-color: var(--brand-200);
  box-shadow: 0 0 0 3px var(--brand-100);
}
.day-group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-5);
  background: var(--bg-surface-alt);
  border-bottom: 1px solid var(--border-subtle);
  flex-wrap: wrap;
}
.day-group.is-today .day-group-header {
  background: linear-gradient(90deg, var(--brand-50) 0%, var(--bg-surface-alt) 100%);
}
.day-group-date { display: flex; align-items: baseline; gap: var(--space-3); }
.dg-name {
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  text-transform: capitalize;
}
.dg-full {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
}
.day-group-meta { display: flex; gap: var(--space-2); align-items: center; flex-wrap: wrap; }
.dg-count { font-size: var(--text-sm); color: var(--text-secondary); font-weight: var(--font-semibold); }
.dg-alert {
  display: inline-flex;
  align-items: center;
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  background: var(--warning-50);
  color: var(--warning-700);
  border: 1px solid var(--warning-200);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
}

.day-group-list { list-style: none; margin: 0; padding: 0; }
.group-row {
  display: grid;
  grid-template-columns: 80px 4px 1fr auto auto;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-3) var(--space-5);
  border-bottom: 1px solid var(--neutral-100);
  transition: background-color var(--duration-fast) var(--ease-out);
}
.group-row:last-child { border-bottom: none; }
.group-row:hover { background: var(--bg-surface-alt); }
.group-row.is-done { opacity: 0.72; }
.gr-time {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.gr-accent { width: 4px; height: 36px; border-radius: var(--radius-sm); }
.gr-main { min-width: 0; }
.gr-pet {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-md);
  color: var(--text-primary);
  margin: 0 0 2px;
}
.gr-pet svg { color: var(--brand-700); flex-shrink: 0; }
.gr-pet strong { font-weight: var(--font-bold); }
.gr-species { font-size: var(--text-sm); color: var(--text-secondary); font-weight: var(--font-medium); }
.gr-meta {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.gr-sep { color: var(--text-tertiary); margin: 0 var(--space-1); }
.gr-status { display: flex; }
.status-pill-sm {
  display: inline-flex;
  align-items: center;
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  border: 1px solid;
  background: transparent;
  white-space: nowrap;
}
.gr-actions { display: flex; gap: var(--space-2); align-items: center; }
.btn-atender-sm {
  padding: var(--space-2) var(--space-4);
  background: var(--brand-700);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
  transition: background-color var(--duration-fast) var(--ease-out);
}
.btn-atender-sm:hover { background: var(--brand-800); }
.btn-icon-sm {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.btn-icon-sm:hover:not(:disabled) { border-color: var(--brand-700); color: var(--brand-700); background: var(--brand-50); }
.btn-icon-sm:disabled { opacity: 0.5; cursor: not-allowed; }

/* ═══ EMPTY / LOADING ═══ */
.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  color: var(--text-secondary);
  font-size: var(--text-md);
}
.spin { animation: spin 0.9s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.agenda-clear-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  background: var(--bg-surface);
  color: var(--neutral-600);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.agenda-clear-btn:hover { border-color: var(--brand-700); color: var(--brand-700); background: var(--brand-50); }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .stats-strip { grid-template-columns: repeat(2, 1fr); }
  .stat { border-right: none; border-bottom: 1px solid var(--border-subtle); }
  .stat:nth-child(odd) { border-right: 1px solid var(--border-subtle); }
  .stat:nth-last-child(-n+2) { border-bottom: none; }
}
@media (max-width: 768px) {
  .agenda-view { padding: var(--space-5) var(--space-4) var(--space-10); }
  .page-top-title { font-size: var(--text-4xl); }
  .page-top-actions { width: 100%; }
  .quick-action { flex: 1; justify-content: center; }

  .week-strip { padding: var(--space-2); gap: var(--space-2); }
  .week-day { padding: var(--space-2) var(--space-1); }
  .week-day-num { font-size: var(--text-lg); }

  .stats-strip { grid-template-columns: 1fr; }
  .stat { border-right: none; border-bottom: 1px solid var(--border-subtle); padding: var(--space-4); }
  .stat:nth-child(odd) { border-right: none; }
  .stat:last-child { border-bottom: none; }
  .stat-value { font-size: var(--text-3xl); }

  .timeline-item { grid-template-columns: 60px 20px 1fr; gap: var(--space-2); }
  .time-start { font-size: var(--text-sm); }
  .card-grid { grid-template-columns: 1fr; }
  .card-foot { flex-direction: column; align-items: stretch; }
  .btn-atender { justify-content: center; }
  .btn-link { margin-left: 0; justify-content: center; }

  .group-row {
    grid-template-columns: 1fr auto;
    gap: var(--space-2) var(--space-3);
    padding: var(--space-3) var(--space-4);
  }
  .gr-time { grid-row: 1; grid-column: 1; }
  .gr-accent { display: none; }
  .gr-main { grid-column: 1 / -1; }
  .gr-status { grid-column: 1; }
  .gr-actions { grid-column: 2; grid-row: 1; }
}
</style>