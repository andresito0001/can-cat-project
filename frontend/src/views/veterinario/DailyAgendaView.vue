<template>
  <div class="agenda-view">
    <ToastContainer />

    <!-- ═══ HEADER ═══ -->
    <header class="agenda-header">
      <div class="header-info">
        <p class="header-eyebrow">
          <CalendarCheck2 :size="12" />
          Veterinario · Agenda
        </p>
        <h1>{{ tituloRango }}</h1>
        <p class="header-sub">{{ subtituloRango }}</p>
      </div>
      <div class="header-actions">
        <button class="btn-ghost" type="button" :disabled="cargando" @click="irAHoy">
          <Calendar :size="14" /> Hoy
        </button>
        <button class="btn-secondary" type="button" :disabled="cargando" @click="recargar">
          <Loader2 v-if="cargando" :size="14" class="spin" />
          <Clock v-else :size="14" />
          Actualizar
        </button>
      </div>
    </header>

    <!-- ═══ CONTROLES: navegación + vista ═══ -->
    <section class="agenda-controls">
      <div class="nav-group">
        <button class="nav-btn" type="button" :disabled="cargando" @click="navegar(-1)" aria-label="Anterior">
          <ChevronLeft :size="16" />
        </button>
        <button class="nav-btn nav-today" type="button" :disabled="cargando" @click="irAHoy">
          Hoy
        </button>
        <button class="nav-btn" type="button" :disabled="cargando" @click="navegar(1)" aria-label="Siguiente">
          <ChevronRight :size="16" />
        </button>
      </div>

      <div class="view-toggle" role="tablist">
        <button
          type="button" role="tab"
          class="view-btn" :class="{ active: vista === 'dia' }"
          @click="cambiarVista('dia')"
        >
          <CalendarDays :size="14" /> Día
        </button>
        <button
          type="button" role="tab"
          class="view-btn" :class="{ active: vista === 'semana' }"
          @click="cambiarVista('semana')"
        >
          <CalendarRange :size="14" /> Semana
        </button>
        <button
          type="button" role="tab"
          class="view-btn" :class="{ active: vista === 'mes' }"
          @click="cambiarVista('mes')"
        >
          <Calendar :size="14" /> Mes
        </button>
      </div>
    </section>

    <!-- ═══ FILTROS ═══ -->
    <section class="agenda-filters">
      <div class="search-box">
        <Search :size="16" class="search-icon" />
        <input
          v-model="buscando"
          type="text"
          placeholder="Buscar por mascota, dueño, cédula o servicio…"
          class="search-input"
        />
        <button v-if="buscando" class="search-clear" type="button" @click="buscando = ''" aria-label="Limpiar">
          <X :size="14" />
        </button>
      </div>

      <div class="filter-chips">
        <button
          v-for="f in FILTROS" :key="f.value"
          type="button"
          class="chip" :class="{ 'chip-active': filtroEstado === f.value }"
          @click="filtroEstado = f.value"
        >
          {{ f.label }}
          <span v-if="f.value === 'todas'" class="chip-count">{{ citasFiltradas.length }}</span>
          <span v-else class="chip-count">{{ contarPorFiltro(f.value) }}</span>
        </button>
      </div>
    </section>

    <!-- ═══ KPIs ═══ -->
    <section class="agenda-kpis">
      <article class="kpi">
        <div class="kpi-icon" style="--c: #0F766E; --b: #F0FDFA;"><CalendarDays :size="16" /></div>
        <div>
          <p class="kpi-value">{{ citasFiltradas.length }}</p>
          <p class="kpi-label">{{ vista === 'dia' ? 'Citas del día' : `Citas del período` }}</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon" style="--c: #D97706; --b: #FFFBEB;"><Clock :size="16" /></div>
        <div>
          <p class="kpi-value">{{ resumen.porAtender }}</p>
          <p class="kpi-label">Por atender</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon" style="--c: #059669; --b: #ECFDF5;"><CheckCircle2 :size="16" /></div>
        <div>
          <p class="kpi-value">{{ resumen.atendidas }}</p>
          <p class="kpi-label">Atendidas</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon" style="--c: #64748B; --b: #F1F5F9;"><AlertCircle :size="16" /></div>
        <div>
          <p class="kpi-value">{{ resumen.canceladas }}</p>
          <p class="kpi-label">Canceladas</p>
        </div>
      </article>
    </section>

    <!-- ═══ CONTENIDO ═══ -->
    <div v-if="cargando" class="loading-state">
      <div class="spin-lg"></div>
      <p>Cargando agenda…</p>
    </div>

    <div v-else-if="citasFiltradas.length === 0" class="empty-state">
      <Inbox :size="44" />
      <h3>{{ citas.length === 0 ? 'Sin citas en este período' : 'Sin resultados con esos filtros' }}</h3>
      <p v-if="citas.length === 0">
        Cuando se agenden citas contigo aparecerán en esta vista.
      </p>
      <p v-else>
        Prueba con otro término de búsqueda o cambia el filtro de estado.
      </p>
      <button v-if="citas.length > 0" class="btn-secondary" type="button" @click="limpiarFiltros">
        <X :size="14" /> Limpiar filtros
      </button>
    </div>

    <!-- Vista DÍA: timeline -->
    <template v-else-if="vista === 'dia'">
      <div class="timeline">
        <div
          v-for="cita in citasFiltradas"
          :key="cita.idCita"
          class="timeline-item"
          :class="{ 'is-done': cita.atendida }"
        >
          <div class="timeline-hora">
            <span class="hora-inicio">{{ fmtHora(cita.horaInicio) }}</span>
            <span class="hora-fin">{{ fmtHora(cita.horaFin) }}</span>
          </div>

          <div class="timeline-dot" :style="{ background: colorEstado(cita) }"></div>

          <article class="cita-card">
            <header class="cita-head">
              <div class="cita-title">
                <h3>
                  <PawPrint :size="15" />
                  {{ cita.mascotaNombre }}
                </h3>
                <span class="cita-species">{{ cita.especie }}<template v-if="cita.raza"> · {{ cita.raza }}</template></span>
              </div>
              <div class="cita-pills">
                <span class="status-pill" :style="estiloEstado(cita.estado)">
                  {{ formatEstado(cita.estado) }}
                </span>
                <span v-if="cita.atendida" class="pill-done">
                  <CheckCircle2 :size="11" /> Atendida
                </span>
              </div>
            </header>

            <div class="cita-body">
              <div class="cita-field">
                <span class="field-label"><User :size="11" /> Dueño</span>
                <span class="field-value">{{ cita.clienteNombre }}</span>
              </div>
              <div class="cita-field">
                <span class="field-label"><FileText :size="11" /> Motivo</span>
                <span class="field-value">{{ cita.motivoConsulta }}</span>
              </div>
              <div class="cita-field">
                <span class="field-label"><Stethoscope :size="11" /> Servicio</span>
                <span class="field-value">{{ cita.servicioNombre }}</span>
              </div>
              <div class="cita-field">
                <span class="field-label"><Phone :size="11" /> Contacto</span>
                <span class="field-value">{{ cita.clienteTelefono || '—' }}</span>
              </div>
            </div>

            <footer class="cita-actions">
              <button
                v-if="!cita.atendida"
                class="btn-atender"
                type="button"
                @click="irAAtender(cita)"
              >
                <Stethoscope :size="14" /> Atender
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
                {{ resolviendoHistorial === cita.idCita ? 'Buscando…' : 'Historial' }}
              </button>
            </footer>
          </article>
        </div>
      </div>
    </template>

    <!-- Vista SEMANA / MES: agrupado por día -->
    <template v-else>
      <div class="grupos-dias">
        <section
          v-for="grupo in gruposPorDia"
          :key="grupo.fecha"
          class="grupo-dia"
          :class="{ 'grupo-hoy': grupo.fecha === hoyISO() }"
        >
          <header class="grupo-header">
            <div class="grupo-fecha">
              <span class="grupo-dia-nombre">{{ nombreDiaLargo(grupo.fecha) }}</span>
              <span class="grupo-dia-fecha">{{ fmtFechaCorta(grupo.fecha) }}</span>
            </div>
            <div class="grupo-meta">
              <span class="grupo-count">{{ grupo.citas.length }} {{ grupo.citas.length === 1 ? 'cita' : 'citas' }}</span>
              <span v-if="grupo.porAtender > 0" class="grupo-badge-warn">
                {{ grupo.porAtender }} por atender
              </span>
            </div>
          </header>

          <div class="grupo-citas">
            <article
              v-for="cita in grupo.citas"
              :key="cita.idCita"
              class="cita-row"
              :class="{ 'is-done': cita.atendida }"
            >
              <div class="row-hora">
                <span>{{ fmtHora(cita.horaInicio) }}</span>
              </div>

              <div class="row-status" :style="{ background: colorEstado(cita) }"></div>

              <div class="row-info">
                <div class="row-title">
                  <PawPrint :size="13" />
                  <strong>{{ cita.mascotaNombre }}</strong>
                  <span class="row-especie">{{ cita.especie }}</span>
                </div>
                <div class="row-meta">
                  <span><User :size="11" /> {{ cita.clienteNombre }}</span>
                  <span><FileText :size="11" /> {{ cita.motivoConsulta }}</span>
                </div>
              </div>

              <div class="row-pills">
                <span class="status-pill-sm" :style="estiloEstado(cita.estado)">
                  {{ formatEstado(cita.estado) }}
                </span>
              </div>

              <div class="row-actions">
                <button
                  v-if="!cita.atendida"
                  class="btn-atender-sm"
                  type="button"
                  @click="irAAtender(cita)"
                >
                  Atender
                </button>
                <button
                  class="btn-icon"
                  type="button"
                  :disabled="resolviendoHistorial === cita.idCita"
                  title="Ver historial"
                  @click="verHistorial(cita)"
                >
                  <History :size="14" />
                </button>
              </div>
            </article>
          </div>
        </section>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ChevronLeft, ChevronRight, Calendar, CalendarDays, CalendarRange,
  CalendarCheck2, CheckCircle2, Clock, FileText, History, Inbox,
  Loader2, PawPrint, Phone, Search, Stethoscope, User, X, AlertCircle,
} from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import { getAgendaVet, getCitaContexto } from '@/api/atenciones.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { ESTADO_COLOR } from '@/utils/constants/estadosCita'
import { hoyISO } from '@/utils/fecha'

const router = useRouter()
const { toastError } = useToast()

// ═════════════════════════════════════════════════════════════
// ESTADO
// ═════════════════════════════════════════════════════════════
const vista = ref('dia')                 // 'dia' | 'semana' | 'mes'
const fechaBase = ref(hoyISO())          // ISO YYYY-MM-DD
const citas = ref([])
const cargando = ref(false)
const buscando = ref('')
const filtroEstado = ref('todas')
const resolviendoHistorial = ref(null)

// Cache por fecha para no re-consultar días ya cargados
const cacheFecha = new Map()

const FILTROS = [
  { value: 'todas',      label: 'Todas' },
  { value: 'por_atender', label: 'Por atender' },
  { value: 'atendidas',  label: 'Atendidas' },
  { value: 'canceladas', label: 'Canceladas' },
]

// ═════════════════════════════════════════════════════════════
// FECHAS
// ═════════════════════════════════════════════════════════════
const DIAS_CORTOS = ['Dom','Lun','Mar','Mié','Jue','Vie','Sáb']
const DIAS_LARGOS = ['Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado']
const MESES = ['enero','febrero','marzo','abril','mayo','junio','julio','agosto','septiembre','octubre','noviembre','diciembre']
const MESES_CORTOS = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']

function isoDe(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth()+1).padStart(2,'0')
  const dd = String(d.getDate()).padStart(2,'0')
  return `${y}-${m}-${dd}`
}
function parseISO(iso) {
  const [y,m,d] = iso.split('-').map(Number)
  return new Date(y, m-1, d)
}
function addDays(iso, n) {
  const d = parseISO(iso)
  d.setDate(d.getDate() + n)
  return isoDe(d)
}
function addMonths(iso, n) {
  const d = parseISO(iso)
  d.setMonth(d.getMonth() + n)
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
function inicioMes(iso) {
  const d = parseISO(iso)
  return isoDe(new Date(d.getFullYear(), d.getMonth(), 1))
}
function finMes(iso) {
  const d = parseISO(iso)
  return isoDe(new Date(d.getFullYear(), d.getMonth()+1, 0))
}

// Rango actual según la vista
const rango = computed(() => {
  const base = fechaBase.value
  if (vista.value === 'dia') {
    return { dias: [base] }
  }
  if (vista.value === 'semana') {
    const ini = inicioSemana(base)
    const dias = Array.from({ length: 7 }, (_, i) => addDays(ini, i))
    return { dias, inicio: dias[0], fin: dias[6] }
  }
  const ini = inicioMes(base)
  const fin = finMes(base)
  const dias = []
  let cur = ini
  while (cur <= fin) {
    dias.push(cur)
    cur = addDays(cur, 1)
  }
  return { dias, mes: parseISO(base).getMonth(), anio: parseISO(base).getFullYear() }
})

const tituloRango = computed(() => {
  if (vista.value === 'dia') {
    const d = parseISO(fechaBase.value)
    return `${DIAS_LARGOS[d.getDay()]}, ${d.getDate()} de ${MESES[d.getMonth()]}`
  }
  if (vista.value === 'semana') {
    const ini = parseISO(inicioSemana(fechaBase.value))
    const fin = parseISO(finSemana(fechaBase.value))
    if (ini.getMonth() === fin.getMonth()) {
      return `${ini.getDate()} – ${fin.getDate()} de ${MESES[ini.getMonth()]} ${ini.getFullYear()}`
    }
    return `${ini.getDate()} ${MESES_CORTOS[ini.getMonth()]} – ${fin.getDate()} ${MESES_CORTOS[fin.getMonth()]} ${fin.getFullYear()}`
  }
  const d = parseISO(fechaBase.value)
  return `${MESES[d.getMonth()][0].toUpperCase()}${MESES[d.getMonth()].slice(1)} ${d.getFullYear()}`
})

const subtituloRango = computed(() => {
  if (vista.value === 'dia') return fechaBase.value === hoyISO() ? 'Hoy' : ''
  return `${rango.value.dias.length} días`
})

// ═════════════════════════════════════════════════════════════
// CARGA DE DATOS (cache + paralelo)
// ═════════════════════════════════════════════════════════════
async function cargarFecha(iso, forzar = false) {
  if (!forzar && cacheFecha.has(iso)) return cacheFecha.get(iso)
  const data = await getAgendaVet(iso)
  cacheFecha.set(iso, data || [])
  return data || []
}

async function cargar() {
  cargando.value = true
  try {
    const dias = rango.value.dias
    const resultados = await Promise.all(dias.map(d => cargarFecha(d)))
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

// ═════════════════════════════════════════════════════════════
// NAVEGACIÓN
// ═════════════════════════════════════════════════════════════
function navegar(delta) {
  if (vista.value === 'dia') fechaBase.value = addDays(fechaBase.value, delta)
  else if (vista.value === 'semana') fechaBase.value = addDays(fechaBase.value, delta * 7)
  else fechaBase.value = addMonths(fechaBase.value, delta)
}

function irAHoy() {
  fechaBase.value = hoyISO()
}

function cambiarVista(v) {
  vista.value = v
}

// ═════════════════════════════════════════════════════════════
// FILTROS + AGRUPACIÓN
// ═════════════════════════════════════════════════════════════
function normalizar(s) {
  return String(s || '').trim().toLowerCase()
    .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
}

const citasFiltradas = computed(() => {
  const q = normalizar(buscando.value)
  return citas.value
    .filter(c => {
      if (filtroEstado.value === 'todas') return true
      if (filtroEstado.value === 'por_atender') return !c.atendida && !['Cancelada','Completada'].includes(c.estado)
      if (filtroEstado.value === 'atendidas') return c.atendida || c.estado === 'Completada'
      if (filtroEstado.value === 'canceladas') return c.estado === 'Cancelada'
      return true
    })
    .filter(c => {
      if (!q) return true
      return [c.mascotaNombre, c.clienteNombre, c.clienteDocumento, c.servicioNombre, c.motivoConsulta]
        .some(v => normalizar(v).includes(q))
    })
    .sort((a,b) => {
      const k = `${a.fechaCita} ${a.horaInicio}`
      const k2 = `${b.fechaCita} ${b.horaInicio}`
      return k.localeCompare(k2)
    })
})

const gruposPorDia = computed(() => {
  const map = new Map()
  for (const c of citasFiltradas.value) {
    const f = String(c.fechaCita).slice(0,10)
    if (!map.has(f)) map.set(f, [])
    map.get(f).push(c)
  }
  return [...map.entries()]
    .sort(([a],[b]) => a.localeCompare(b))
    .map(([fecha, lista]) => ({
      fecha,
      citas: lista.sort((a,b) => a.horaInicio.localeCompare(b.horaInicio)),
      porAtender: lista.filter(c => !c.atendida && c.estado !== 'Cancelada').length,
    }))
})

function contarPorFiltro(tipo) {
  return citas.value.filter(c => {
    if (tipo === 'por_atender') return !c.atendida && !['Cancelada','Completada'].includes(c.estado)
    if (tipo === 'atendidas') return c.atendida || c.estado === 'Completada'
    if (tipo === 'canceladas') return c.estado === 'Cancelada'
    return true
  }).length
}

const resumen = computed(() => ({
  porAtender: citasFiltradas.value.filter(c => !c.atendida && !['Cancelada','Completada'].includes(c.estado)).length,
  atendidas: citasFiltradas.value.filter(c => c.atendida || c.estado === 'Completada').length,
  canceladas: citasFiltradas.value.filter(c => c.estado === 'Cancelada').length,
}))

function limpiarFiltros() {
  buscando.value = ''
  filtroEstado.value = 'todas'
}

// ═════════════════════════════════════════════════════════════
// ACCIONES
// ═════════════════════════════════════════════════════════════
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

// ═════════════════════════════════════════════════════════════
// HELPERS DE FORMATO
// ═════════════════════════════════════════════════════════════
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
  const d = parseISO(iso)
  return DIAS_LARGOS[d.getDay()]
}

function formatEstado(estado) {
  return String(estado || '').replaceAll('_', ' ')
}

function colorEstado(cita) {
  return cita.estadoColor || ESTADO_COLOR[cita.estado] || '#64748B'
}

function estiloEstado(estado) {
  const color = ESTADO_COLOR[estado] || '#64748B'
  return { color, borderColor: color, backgroundColor: `${color}1A` }
}

// ═════════════════════════════════════════════════════════════
// CICLO DE VIDA
// ═════════════════════════════════════════════════════════════
watch([vista, fechaBase], cargar)
onMounted(cargar)
</script>

<style scoped>
.agenda-view {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 24px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
button { font-family: inherit; }

/* ═══ HEADER ═══ */
.agenda-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 22px 26px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
  flex-wrap: wrap;
}
.header-info { min-width: 0; }
.header-eyebrow {
  display: inline-flex; align-items: center; gap: 6px;
  margin: 0 0 8px;
  font-size: 11px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .7px;
  color: #0F766E;
  background: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  border: 1px solid #CCFBF1;
}
.agenda-header h1 {
  margin: 0 0 4px;
  font-size: 24px; font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.02em;
  text-transform: capitalize;
  line-height: 1.2;
}
.header-sub {
  margin: 0;
  font-size: 13.5px; color: #64748B;
}
.header-actions {
  display: flex; gap: 8px;
  align-items: center;
}

/* ═══ CONTROLES ═══ */
.agenda-controls {
  display: flex; align-items: center; justify-content: space-between;
  gap: 16px; flex-wrap: wrap;
}
.nav-group {
  display: inline-flex;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
}
.nav-btn {
  border: none; background: #fff;
  padding: 8px 12px;
  color: #475569;
  cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600;
  font-family: inherit;
  transition: background .15s, color .15s;
  border-right: 1px solid #E2E8F0;
}
.nav-btn:last-child { border-right: none; }
.nav-btn:hover:not(:disabled) { background: #F0FDFA; color: #0F766E; }
.nav-btn:disabled { opacity: .5; cursor: not-allowed; }
.nav-today { padding: 8px 16px; }

.view-toggle {
  display: inline-flex;
  background: #F1F5F9;
  border-radius: 10px;
  padding: 4px;
  gap: 4px;
}
.view-btn {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 7px 14px;
  border-radius: 7px;
  border: none;
  background: transparent;
  color: #64748B;
  font-size: 12.5px; font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: all .15s;
}
.view-btn:hover { color: #0F766E; }
.view-btn.active {
  background: #fff;
  color: #0F766E;
  box-shadow: 0 1px 3px rgba(15, 23, 42, .08);
}

/* ═══ FILTROS ═══ */
.agenda-filters {
  display: flex; flex-direction: column; gap: 12px;
}
.search-box {
  position: relative;
  max-width: 100%;
}
.search-icon {
  position: absolute;
  left: 14px; top: 50%; transform: translateY(-50%);
  color: #94A3B8;
  pointer-events: none;
}
.search-input {
  width: 100%;
  padding: 11px 42px;
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  font-size: 14px; color: #1E293B;
  background: #fff;
  font-family: inherit;
  transition: border-color .15s, box-shadow .15s;
  box-sizing: border-box;
}
.search-input::placeholder { color: #94A3B8; }
.search-input:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.search-clear {
  position: absolute;
  right: 12px; top: 50%; transform: translateY(-50%);
  background: none; border: none;
  color: #94A3B8;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  display: flex; align-items: center;
}
.search-clear:hover { color: #475569; background: #F1F5F9; }

.filter-chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.chip {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 7px 14px;
  border-radius: 20px;
  border: 1.5px solid #E2E8F0;
  background: #fff;
  color: #64748B;
  font-size: 12.5px; font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all .15s;
}
.chip:hover { border-color: #CBD5E1; color: #334155; }
.chip-active {
  background: #0F766E;
  border-color: #0F766E;
  color: #fff;
}
.chip-count {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 18px; height: 18px;
  padding: 0 5px;
  border-radius: 20px;
  background: #F1F5F9;
  color: #64748B;
  font-size: 10.5px; font-weight: 700;
  font-variant-numeric: tabular-nums;
}
.chip-active .chip-count {
  background: rgba(255,255,255,.25);
  color: #fff;
}

/* ═══ KPIs ═══ */
.agenda-kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.kpi {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
}
.kpi-icon {
  width: 38px; height: 38px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  background: var(--b); color: var(--c);
  flex-shrink: 0;
}
.kpi-value {
  margin: 0;
  font-size: 20px; font-weight: 700;
  color: #0F172A;
  line-height: 1.1;
  letter-spacing: -0.01em;
}
.kpi-label {
  margin: 3px 0 0;
  font-size: 11.5px; color: #64748B; font-weight: 500;
}

/* ═══ ESTADOS ═══ */
.loading-state, .empty-state {
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 12px;
  padding: 72px 24px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  color: #94A3B8;
  text-align: center;
}
.empty-state h3 {
  margin: 0;
  font-size: 17px; font-weight: 700; color: #1E293B;
}
.empty-state p {
  margin: 0 0 6px;
  font-size: 13.5px; color: #64748B;
  max-width: 420px; line-height: 1.55;
}
.spin { animation: spin 1s linear infinite; }
.spin-lg {
  width: 34px; height: 34px;
  border: 3px solid #E2E8F0; border-top-color: #0F766E;
  border-radius: 50%;
  animation: spin .8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══ VISTA DÍA — timeline ═══ */
.timeline {
  display: flex; flex-direction: column;
  gap: 12px;
  position: relative;
}
.timeline-item {
  display: grid;
  grid-template-columns: 76px 20px 1fr;
  gap: 14px;
  align-items: stretch;
}
.timeline-hora {
  display: flex; flex-direction: column;
  align-items: flex-end;
  padding-top: 14px;
  gap: 2px;
}
.hora-inicio {
  font-size: 14px; font-weight: 700; color: #0F766E;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.hora-fin {
  font-size: 11px; font-weight: 600; color: #94A3B8;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.timeline-dot {
  position: relative;
  width: 12px; height: 12px;
  border-radius: 50%;
  margin-top: 18px;
  justify-self: center;
  box-shadow: 0 0 0 3px #fff, 0 0 0 4px #E2E8F0;
}
.timeline-dot::before,
.timeline-dot::after {
  content: '';
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  width: 2px;
  background: #E2E8F0;
}
.timeline-dot::before { bottom: 100%; height: 30px; margin-bottom: 6px; }
.timeline-dot::after { top: 100%; height: 200px; margin-top: 6px; }
.timeline-item:first-child .timeline-dot::before { display: none; }
.timeline-item:last-child .timeline-dot::after { display: none; }

/* ═══ CARD de cita (día) ═══ */
.cita-card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 14px 16px;
  display: flex; flex-direction: column; gap: 12px;
  transition: border-color .2s, box-shadow .2s, transform .2s;
}
.cita-card:hover {
  border-color: #CBD5E1;
  transform: translateY(-1px);
  box-shadow: 0 8px 20px -10px rgba(15, 23, 42, .12);
}
.timeline-item.is-done .cita-card {
  background: #F8FAFC;
  opacity: .85;
}
.cita-head {
  display: flex; align-items: flex-start; justify-content: space-between;
  gap: 12px; flex-wrap: wrap;
}
.cita-title h3 {
  margin: 0 0 2px;
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 15.5px; font-weight: 700; color: #0F172A;
  letter-spacing: -0.01em;
}
.cita-title h3 svg { color: #0F766E; }
.cita-species {
  font-size: 12px; color: #64748B; font-weight: 500;
}
.cita-pills { display: flex; gap: 6px; flex-wrap: wrap; }
.status-pill {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11.5px; font-weight: 700;
  border: 1px solid;
  white-space: nowrap;
}
.pill-done {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11.5px; font-weight: 700;
  background: #ECFDF5; color: #059669; border: 1px solid #A7F3D0;
}

.cita-body {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px 20px;
}
.cita-field {
  display: flex; flex-direction: column; gap: 2px;
  min-width: 0;
}
.field-label {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 10.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .4px;
  color: #94A3B8;
}
.field-value {
  font-size: 13px; font-weight: 600; color: #334155;
  overflow: hidden; text-overflow: ellipsis;
}

.cita-actions {
  display: flex; justify-content: space-between; align-items: center;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px dashed #E2E8F0;
}
.btn-atender {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px;
  background: #0F766E; color: #fff;
  border: none; border-radius: 9px;
  font-size: 13px; font-weight: 700;
  font-family: inherit; cursor: pointer;
  transition: all .15s;
}
.btn-atender:hover {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15,118,110,.4);
}
.badge-completada {
  display: inline-flex; align-items: center; gap: 6px;
  font-size: 12.5px; font-weight: 700;
  color: #059669;
}
.btn-link {
  display: inline-flex; align-items: center; gap: 6px;
  background: none; border: none;
  color: #0F766E;
  font-size: 12.5px; font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  padding: 6px 8px;
  border-radius: 6px;
  transition: background .15s;
}
.btn-link:hover:not(:disabled) { background: #F0FDFA; }
.btn-link:disabled { color: #94A3B8; cursor: not-allowed; }

/* ═══ VISTA SEMANA/MES — grupos por día ═══ */
.grupos-dias { display: flex; flex-direction: column; gap: 16px; }
.grupo-dia {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  overflow: hidden;
}
.grupo-dia.grupo-hoy {
  border-color: #99F6E4;
  box-shadow: 0 0 0 3px rgba(15,118,110,.06);
}
.grupo-header {
  display: flex; align-items: center; justify-content: space-between;
  gap: 12px; flex-wrap: wrap;
  padding: 12px 18px;
  background: #F8FAFC;
  border-bottom: 1px solid #E2E8F0;
}
.grupo-hoy .grupo-header {
  background: linear-gradient(90deg, #F0FDFA 0%, #F8FAFC 100%);
}
.grupo-fecha { display: flex; align-items: baseline; gap: 10px; }
.grupo-dia-nombre {
  font-size: 14px; font-weight: 700; color: #0F172A;
  text-transform: capitalize;
}
.grupo-dia-fecha {
  font-size: 12px; font-weight: 600; color: #64748B;
}
.grupo-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.grupo-count {
  font-size: 12px; font-weight: 600; color: #64748B;
}
.grupo-badge-warn {
  display: inline-flex; align-items: center;
  padding: 3px 10px;
  border-radius: 20px;
  background: #FFFBEB; color: #B45309;
  border: 1px solid #FDE68A;
  font-size: 11px; font-weight: 700;
}

.grupo-citas {
  display: flex; flex-direction: column;
}
.cita-row {
  display: grid;
  grid-template-columns: 76px 4px 1fr auto auto;
  gap: 14px;
  align-items: center;
  padding: 12px 18px;
  border-bottom: 1px solid #F1F5F9;
  transition: background .15s;
}
.cita-row:last-child { border-bottom: none; }
.cita-row:hover { background: #FAFBFC; }
.cita-row.is-done { opacity: .72; }

.row-hora {
  font-size: 13px; font-weight: 700; color: #0F766E;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.row-status {
  width: 4px; height: 34px;
  border-radius: 4px;
}
.row-info { min-width: 0; }
.row-title {
  display: flex; align-items: center; gap: 6px;
  font-size: 13.5px;
  color: #0F172A;
  margin-bottom: 2px;
}
.row-title svg { color: #0F766E; flex-shrink: 0; }
.row-title strong { font-weight: 700; }
.row-especie {
  font-size: 11.5px; color: #64748B; font-weight: 500;
}
.row-meta {
  display: flex; flex-wrap: wrap; gap: 12px;
  font-size: 12px; color: #64748B;
}
.row-meta span {
  display: inline-flex; align-items: center; gap: 4px;
  min-width: 0;
}
.row-meta svg { color: #94A3B8; }

.row-pills { display: flex; }
.status-pill-sm {
  display: inline-flex; align-items: center;
  padding: 3px 9px;
  border-radius: 20px;
  font-size: 11px; font-weight: 700;
  border: 1px solid;
  white-space: nowrap;
}
.row-actions { display: flex; align-items: center; gap: 6px; }
.btn-atender-sm {
  display: inline-flex; align-items: center;
  padding: 6px 12px;
  background: #0F766E; color: #fff;
  border: none; border-radius: 7px;
  font-size: 12px; font-weight: 700;
  font-family: inherit; cursor: pointer;
  transition: background .15s;
}
.btn-atender-sm:hover { background: #115E59; }
.btn-icon {
  width: 30px; height: 30px;
  border-radius: 7px;
  border: 1px solid #E2E8F0;
  background: #fff;
  color: #64748B;
  display: inline-flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: all .15s;
}
.btn-icon:hover:not(:disabled) {
  border-color: #0F766E; color: #0F766E; background: #F0FDFA;
}
.btn-icon:disabled { opacity: .5; cursor: not-allowed; }

/* ═══ BOTONES GENERALES ═══ */
.btn-primary, .btn-secondary, .btn-ghost {
  display: inline-flex; align-items: center; justify-content: center; gap: 6px;
  padding: 9px 16px;
  border-radius: 10px;
  font-size: 13px; font-weight: 700;
  font-family: inherit; cursor: pointer;
  transition: all .15s;
  border: 1px solid #E2E8F0;
  white-space: nowrap;
}
.btn-secondary {
  background: #fff; color: #475569;
}
.btn-secondary:hover:not(:disabled) {
  border-color: #CBD5E1; color: #0F766E; background: #F8FAFC;
}
.btn-secondary:disabled { opacity: .55; cursor: not-allowed; }
.btn-ghost {
  background: #fff; color: #0F766E;
  border-color: #99F6E4;
}
.btn-ghost:hover { background: #F0FDFA; }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .agenda-kpis { grid-template-columns: repeat(2, 1fr); }
  .cita-body { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .agenda-view { padding: 16px 14px 40px; }
  .agenda-header { padding: 18px 20px; border-radius: 14px; }
  .agenda-header h1 { font-size: 20px; }
  .header-actions { width: 100%; }
  .header-actions button { flex: 1; }

  .timeline-item { grid-template-columns: 60px 16px 1fr; gap: 10px; }
  .timeline-dot::after { height: 300px; }
  .hora-inicio, .hora-fin { font-size: 12px; }

  .cita-row {
    grid-template-columns: 1fr auto;
    gap: 8px 12px;
    padding: 12px 14px;
  }
  .row-hora { grid-column: 1; grid-row: 1; }
  .row-status { display: none; }
  .row-info { grid-column: 1 / -1; }
  .row-pills { grid-column: 1; grid-row: 3; }
  .row-actions { grid-column: 2; grid-row: 1 / span 3; align-self: start; }
}
@media (max-width: 480px) {
  .agenda-kpis { grid-template-columns: 1fr 1fr; gap: 8px; }
  .kpi { padding: 12px; }
  .kpi-value { font-size: 18px; }
  .filter-chips { gap: 6px; }
  .chip { padding: 6px 10px; font-size: 11.5px; }
  .view-btn { padding: 6px 10px; font-size: 11.5px; }
}
</style>