<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight, CalendarCheck, CalendarDays, CheckCircle2, ChevronRight,
  Clock, Clock3, FileText, Inbox, Search, Stethoscope, User,
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth.store'
import { useToast } from '@/composables/useToast'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import { getAgendaVet } from '@/api/atenciones.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { ESTADO_COLOR } from '@/utils/constants/estadosCita'
import { fechaCompleta, hoyISO, rangoHora } from '@/utils/fecha'

const router = useRouter()
const authStore = useAuthStore()
const { toastError } = useToast()

const hoy = hoyISO()
const cargando = ref(true)
const agendaHoy = ref([])
const atencionesMes = ref(null)

const nombreVet = computed(() => authStore.user?.nombreCompleto || 'Doctor(a)')

const stats = computed(() => [
  {
    etiqueta: 'Citas de hoy',
    valor: agendaHoy.value.length,
    icono: CalendarDays,
    color: 'brand',
  },
  {
    etiqueta: 'Por atender',
    valor: agendaHoy.value.filter((c) => !c.atendida).length,
    icono: Clock3,
    color: 'warning',
  },
  {
    etiqueta: 'Atendidas hoy',
    valor: agendaHoy.value.filter((c) => c.atendida).length,
    icono: CheckCircle2,
    color: 'success',
  },
  {
    etiqueta: 'Atenciones este mes',
    valor: atencionesMes.value ?? '…',
    icono: Stethoscope,
    color: 'purple',
  },
])

const proximaCita = computed(() => agendaHoy.value.find((c) => !c.atendida) || null)

const citasAgenda = computed(() =>
  proximaCita.value
    ? agendaHoy.value.filter((c) => c.idCita !== proximaCita.value.idCita)
    : agendaHoy.value
)
const citasVisibles = computed(() => citasAgenda.value.slice(0, 5))
const citasOcultas = computed(() => Math.max(0, citasAgenda.value.length - 5))

const accesos = [
  {
    titulo: 'Mi Agenda Hoy',
    descripcion: 'Citas confirmadas del día',
    icono: CalendarCheck,
    ruta: '/veterinario/agenda',
  },
  {
    titulo: 'Atención Clínica',
    descripcion: 'Iniciar una consulta médica',
    icono: Stethoscope,
    ruta: '/veterinario/atencion',
  },
  {
    titulo: 'Historiales',
    descripcion: 'Expedientes clínicos',
    icono: FileText,
    ruta: '/veterinario/historiales',
  },
]

function etiquetaEstado(estadoNombre) {
  return String(estadoNombre || '').replaceAll('_', ' ')
}
function estiloEstado(estadoNombre, estadoColor) {
  const color = estadoColor || ESTADO_COLOR[estadoNombre] || 'var(--neutral-500)'
  return { color, borderColor: color }
}

function horaCorta(h) {
  return h ? String(h).slice(0, 5) : ''
}

function minutosHasta(horaInicio) {
  if (!horaInicio) return null
  const [h, m] = String(horaInicio).split(':').map(Number)
  const ahora = new Date()
  return h * 60 + m - (ahora.getHours() * 60 + ahora.getMinutes())
}
function textoCuentaRegresiva(horaInicio) {
  const diff = minutosHasta(horaInicio)
  if (diff === null) return ''
  if (diff < -5) return 'En curso'
  if (diff <= 5) return 'Ahora mismo'
  if (diff < 60) return `En ${diff} min`
  const h = Math.floor(diff / 60)
  const m = diff % 60
  return m === 0 ? `En ${h}h` : `En ${h}h ${m}m`
}

async function cargarAgenda() {
  cargando.value = true
  try {
    agendaHoy.value = await getAgendaVet(hoy)
  } catch (error) {
    toastError(getApiErrorMessage(error))
    agendaHoy.value = []
  } finally {
    cargando.value = false
  }
}

async function calcularAtencionesMes() {
  const ahora = new Date()
  const anio = ahora.getFullYear()
  const mes = String(ahora.getMonth() + 1).padStart(2, '0')
  const diaActual = ahora.getDate()
  const fechas = []
  for (let d = 1; d <= diaActual; d += 1) {
    fechas.push(`${anio}-${mes}-${String(d).padStart(2, '0')}`)
  }
  const resultados = await Promise.allSettled(fechas.map((f) => getAgendaVet(f)))
  atencionesMes.value = resultados.reduce(
    (total, r) =>
      r.status === 'fulfilled' ? total + r.value.filter((c) => c.atendida).length : total,
    0,
  )
}

function irAtencion(idCita) {
  if (!idCita) return
  router.push(`/veterinario/atencion/${idCita}`)
}

onMounted(async () => {
  await cargarAgenda()
  calcularAtencionesMes()
})
</script>

<template>
  <div class="dashboard-vet">
    <ToastContainer />

    <!-- ═══ HEADER COMPACTO ═══ -->
    <header class="page-top">
      <div class="page-top-info">
        <p class="page-top-date">{{ fechaCompleta(hoy) }}</p>
        <h1 class="page-top-title">
          Hola, <span class="page-top-name">{{ nombreVet }}</span>
        </h1>
      </div>
      <div class="page-top-actions">
        <button class="quick-action" type="button" @click="router.push('/veterinario/agenda')">
          <CalendarDays :size="15" />
          <span>Mi agenda</span>
        </button>
        <button class="quick-action quick-action-primary" type="button" @click="router.push('/veterinario/atencion')">
          <Stethoscope :size="15" />
          <span>Iniciar atención</span>
        </button>
      </div>
    </header>

    <!-- ═══ LOADING ═══ -->
    <div v-if="cargando" class="loading-box">
      <span class="spinner spinner-lg" />
      <p>Cargando tu agenda…</p>
    </div>

    <template v-else>
      <!-- ═══ STATS STRIP (línea horizontal, no cards gigantes) ═══ -->
      <section class="stats-strip">
        <article v-for="s in stats" :key="s.etiqueta" class="stat" :class="`stat-${s.color}`">
          <div class="stat-icon">
            <component :is="s.icono" :size="18" />
          </div>
          <div class="stat-content">
            <p class="stat-value">{{ s.valor }}</p>
            <p class="stat-label">{{ s.etiqueta }}</p>
          </div>
        </article>
      </section>

      <!-- ═══ PRÓXIMO PACIENTE (card destacada horizontal) ═══ -->
      <section v-if="proximaCita" class="next-patient">
        <div class="next-accent" />
        <div class="next-content">
          <div class="next-tag">
            <Clock :size="12" />
            <span>{{ textoCuentaRegresiva(proximaCita.horaInicio) }}</span>
          </div>
          <h2 class="next-name">{{ proximaCita.mascotaNombre }}</h2>
          <p class="next-species">
            {{ proximaCita.especie }}
            <template v-if="proximaCita.raza"> · {{ proximaCita.raza }}</template>
          </p>
          <div class="next-meta">
            <span class="next-meta-item">
              <User :size="13" />
              {{ proximaCita.clienteNombre }}
            </span>
            <span class="next-meta-sep" />
            <span class="next-meta-item">
              <Clock :size="13" />
              {{ rangoHora(proximaCita.horaInicio, proximaCita.horaFin) }}
            </span>
            <template v-if="proximaCita.servicioNombre">
              <span class="next-meta-sep" />
              <span class="next-meta-item">
                <Stethoscope :size="13" />
                {{ proximaCita.servicioNombre }}
              </span>
            </template>
          </div>
        </div>
        <div class="next-action">
          <button class="btn-start" type="button" @click="irAtencion(proximaCita.idCita)">
            Iniciar atención
            <ArrowRight :size="16" />
          </button>
        </div>
      </section>

      <!-- ═══ GRID PRINCIPAL ═══ -->
      <section class="main-grid">
        <!-- Agenda de hoy -->
        <article class="panel">
          <header class="panel-header">
            <div class="panel-header-info">
              <h3 class="panel-title">Agenda de hoy</h3>
              <p class="panel-sub">
                {{ agendaHoy.length
                  ? `${agendaHoy.length} ${agendaHoy.length === 1 ? 'cita' : 'citas'} programadas`
                  : 'Sin citas programadas' }}
              </p>
            </div>
            <button class="panel-link" type="button" @click="router.push('/veterinario/agenda')">
              Ver completa
              <ChevronRight :size="14" />
            </button>
          </header>

          <div class="panel-body">
            <AppEmptyState
              v-if="!agendaHoy.length"
              :icon="Inbox"
              title="Sin citas para hoy"
              description="Cuando recibas citas aparecerán aquí automáticamente."
            />

            <div v-else-if="!citasVisibles.length" class="compact-empty">
              <CheckCircle2 :size="20" />
              <span>Solo tenías la cita destacada. Todo al día.</span>
            </div>

            <ul v-else class="agenda-list">
              <li
                v-for="cita in citasVisibles"
                :key="cita.idCita"
                class="agenda-row"
                :class="{ 'is-done': cita.atendida }"
              >
                <div class="row-time">
                  <span class="row-time-start">{{ horaCorta(cita.horaInicio) }}</span>
                  <span class="row-time-end">{{ horaCorta(cita.horaFin) }}</span>
                </div>

                <div class="row-main">
                  <p class="row-name">{{ cita.mascotaNombre }}</p>
                  <p class="row-meta">
                    {{ cita.especie }}
                    <template v-if="cita.raza"> · {{ cita.raza }}</template>
                    · {{ cita.clienteNombre }}
                  </p>
                </div>

                <div class="row-status">
                  <span
                    class="status-dot"
                    :style="{ background: cita.estadoColor || ESTADO_COLOR[cita.estadoNombre] }"
                  />
                  <span class="status-label">
                    {{ etiquetaEstado(cita.estadoNombre) }}
                  </span>
                </div>

                <div class="row-action">
                  <button
                    v-if="!cita.atendida"
                    class="row-btn"
                    type="button"
                    @click="irAtencion(cita.idCita)"
                  >
                    Atender
                  </button>
                  <CheckCircle2 v-else :size="18" class="row-done-icon" />
                </div>
              </li>
            </ul>

            <p v-if="citasOcultas > 0" class="more-hint">
              + {{ citasOcultas }} {{ citasOcultas === 1 ? 'cita más' : 'citas más' }} en tu agenda completa
            </p>
          </div>
        </article>

        <!-- Accesos rápidos -->
        <article class="panel panel-narrow">
          <header class="panel-header">
            <h3 class="panel-title">Accesos rápidos</h3>
          </header>
          <div class="panel-body panel-body-compact">
            <ul class="shortcuts">
              <li v-for="a in accesos" :key="a.titulo">
                <button class="shortcut" type="button" @click="router.push(a.ruta)">
                  <span class="shortcut-icon">
                    <component :is="a.icono" :size="18" />
                  </span>
                  <span class="shortcut-body">
                    <span class="shortcut-title">{{ a.titulo }}</span>
                    <span class="shortcut-desc">{{ a.descripcion }}</span>
                  </span>
                  <ChevronRight :size="16" class="shortcut-arrow" />
                </button>
              </li>
            </ul>
          </div>
        </article>
      </section>
    </template>
  </div>
</template>

<style scoped>
.dashboard-vet {
  max-width: 1280px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6) var(--space-12);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

/* ═══════════════════════════════════════════════════════════
   HEADER COMPACTO
   ═══════════════════════════════════════════════════════════ */
.page-top {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-6);
  flex-wrap: wrap;
  padding-bottom: var(--space-6);
  border-bottom: 1px solid var(--border-subtle);
}
.page-top-info { min-width: 0; }
.page-top-date {
  margin: 0 0 var(--space-1);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--brand-700);
}
.page-top-title {
  margin: 0;
  font-size: var(--text-5xl);
  font-weight: var(--font-bold);
  letter-spacing: var(--tracking-tight);
  line-height: 1.1;
  color: var(--text-primary);
}
.page-top-name { color: var(--brand-700); }

.page-top-actions {
  display: flex;
  gap: var(--space-2);
  align-items: center;
  flex-wrap: wrap;
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
.quick-action:hover {
  border-color: var(--brand-200);
  background: var(--brand-50);
  color: var(--brand-700);
}
.quick-action-primary {
  background: var(--brand-700);
  border-color: var(--brand-700);
  color: var(--text-inverse);
}
.quick-action-primary:hover {
  background: var(--brand-800);
  border-color: var(--brand-800);
  color: var(--text-inverse);
}

/* ═══════════════════════════════════════════════════════════
   STATS STRIP (compacto, horizontal)
   ═══════════════════════════════════════════════════════════ */
.stats-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0;
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
.stat-purple .stat-icon  { background: var(--purple-50);  color: var(--purple-600); }

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
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ═══════════════════════════════════════════════════════════
   PRÓXIMO PACIENTE — card destacada con acento lateral
   ═══════════════════════════════════════════════════════════ */
.next-patient {
  position: relative;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: var(--space-6);
  align-items: center;
  padding: var(--space-6) var(--space-7);
  background: linear-gradient(120deg, var(--brand-700) 0%, var(--brand-600) 60%, var(--brand-500) 100%);
  color: var(--text-inverse);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow:
    0 1px 3px rgba(15, 118, 110, 0.2),
    0 12px 32px -12px rgba(15, 118, 110, 0.45);
}
.next-accent {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: rgba(255, 255, 255, 0.5);
}
.next-patient::after {
  content: '';
  position: absolute;
  right: -80px;
  top: -80px;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.10) 0%, transparent 70%);
  pointer-events: none;
}
.next-content { min-width: 0; position: relative; z-index: 1; }
.next-tag {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: var(--space-3);
  backdrop-filter: blur(4px);
}
.next-name {
  margin: 0 0 var(--space-1);
  font-size: var(--text-5xl);
  font-weight: var(--font-bold);
  letter-spacing: var(--tracking-tight);
  line-height: 1;
}
.next-species {
  margin: 0 0 var(--space-4);
  font-size: var(--text-md);
  color: rgba(255, 255, 255, 0.85);
}
.next-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-md);
}
.next-meta-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: rgba(255, 255, 255, 0.95);
}
.next-meta-sep {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
}
.next-action { position: relative; z-index: 1; }
.btn-start {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-6);
  background: var(--bg-surface);
  color: var(--brand-700);
  border: none;
  border-radius: var(--radius-xl);
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
  transition: all var(--duration-base) var(--ease-out);
  white-space: nowrap;
}
.btn-start:hover {
  background: var(--brand-50);
  transform: translateY(-1px);
  box-shadow: 0 12px 24px -8px rgba(0, 0, 0, 0.25);
}

/* ═══════════════════════════════════════════════════════════
   GRID PRINCIPAL
   ═══════════════════════════════════════════════════════════ */
.main-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(280px, 1fr);
  gap: var(--space-5);
  align-items: start;
}

.panel {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-xs);
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-subtle);
  flex-wrap: wrap;
}
.panel-header-info { min-width: 0; }
.panel-title {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
.panel-sub {
  margin: 2px 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.panel-link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  background: none;
  border: none;
  padding: 0;
  color: var(--brand-700);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.panel-link:hover {
  color: var(--brand-800);
  gap: var(--space-2);
}
.panel-body { padding: var(--space-4) var(--space-5) var(--space-5); }
.panel-body-compact { padding: var(--space-3); }

/* ═══════════════════════════════════════════════════════════
   AGENDA — filas compactas, info densa
   ═══════════════════════════════════════════════════════════ */
.agenda-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
}
.agenda-row {
  display: grid;
  grid-template-columns: 60px 1fr auto auto;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-4) var(--space-2);
  border-bottom: 1px solid var(--neutral-100);
  transition: background-color var(--duration-fast) var(--ease-out);
  border-radius: var(--radius-md);
}
.agenda-row:last-child { border-bottom: none; }
.agenda-row:hover { background: var(--bg-surface-alt); }
.agenda-row.is-done { opacity: 0.6; }

.row-time {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: var(--space-2) var(--space-1);
  background: var(--bg-surface-alt);
  border-radius: var(--radius-md);
  border-left: 3px solid var(--brand-700);
}
.agenda-row.is-done .row-time { border-left-color: var(--neutral-300); }
.row-time-start {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  font-variant-numeric: tabular-nums;
  line-height: 1;
}
.agenda-row.is-done .row-time-start { color: var(--text-secondary); }
.row-time-end {
  font-size: var(--text-2xs);
  font-weight: var(--font-semibold);
  color: var(--text-tertiary);
  font-variant-numeric: tabular-nums;
  line-height: 1;
}

.row-main { min-width: 0; }
.row-name {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.row-meta {
  margin: 3px 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.row-status {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  flex-shrink: 0;
}
.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.status-label {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
  white-space: nowrap;
}

.row-action { flex-shrink: 0; }
.row-btn {
  padding: var(--space-2) var(--space-4);
  background: var(--brand-700);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.row-btn:hover {
  background: var(--brand-800);
  transform: translateY(-1px);
}
.row-done-icon {
  color: var(--success-500);
  display: block;
}

.compact-empty {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-5);
  color: var(--text-secondary);
  font-size: var(--text-md);
  background: var(--bg-surface-alt);
  border-radius: var(--radius-lg);
  justify-content: center;
}
.compact-empty svg { color: var(--success-500); flex-shrink: 0; }

.more-hint {
  margin: var(--space-4) 0 0;
  text-align: center;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  font-weight: var(--font-medium);
}

/* ═══════════════════════════════════════════════════════════
   SHORTCUTS
   ═══════════════════════════════════════════════════════════ */
.shortcuts {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.shortcut {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-4);
  align-items: center;
  width: 100%;
  padding: var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: all var(--duration-base) var(--ease-out);
}
.shortcut:hover {
  border-color: var(--brand-200);
  background: var(--brand-50);
  transform: translateX(2px);
}
.shortcut-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  background: var(--brand-50);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all var(--duration-base) var(--ease-out);
}
.shortcut:hover .shortcut-icon {
  background: var(--brand-700);
  color: var(--text-inverse);
}
.shortcut-body { min-width: 0; display: flex; flex-direction: column; gap: 2px; }
.shortcut-title {
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}
.shortcut-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.shortcut-arrow {
  color: var(--neutral-300);
  flex-shrink: 0;
  transition: all var(--duration-base) var(--ease-out);
}
.shortcut:hover .shortcut-arrow {
  color: var(--brand-700);
  transform: translateX(2px);
}

/* ═══════════════════════════════════════════════════════════
   LOADING
   ═══════════════════════════════════════════════════════════ */
.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12);
  color: var(--text-secondary);
  font-size: var(--text-md);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
}

/* ═══════════════════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════════════════ */
@media (max-width: 1024px) {
  .stats-strip { grid-template-columns: repeat(2, 1fr); }
  .stat { border-right: none; border-bottom: 1px solid var(--border-subtle); }
  .stat:nth-child(odd) { border-right: 1px solid var(--border-subtle); }
  .stat:nth-last-child(-n+2) { border-bottom: none; }

  .main-grid { grid-template-columns: 1fr; }
  .next-patient { grid-template-columns: 1fr; gap: var(--space-5); }
  .next-action { width: 100%; }
  .btn-start { width: 100%; justify-content: center; }
}

@media (max-width: 640px) {
  .dashboard-vet { padding: var(--space-5) var(--space-4) var(--space-10); gap: var(--space-5); }
  .page-top { flex-direction: column; align-items: flex-start; gap: var(--space-4); padding-bottom: var(--space-5); }
  .page-top-title { font-size: var(--text-4xl); }
  .page-top-actions { width: 100%; }
  .quick-action { flex: 1; justify-content: center; }

  .stats-strip { grid-template-columns: 1fr; }
  .stat { border-right: none; border-bottom: 1px solid var(--border-subtle); padding: var(--space-4); }
  .stat:nth-child(odd) { border-right: none; }
  .stat:last-child { border-bottom: none; }
  .stat-value { font-size: var(--text-3xl); }

  .next-patient { padding: var(--space-5); border-radius: var(--radius-xl); }
  .next-name { font-size: var(--text-4xl); }
  .next-meta { gap: var(--space-2); font-size: var(--text-sm); }
  .next-meta-sep { display: none; }
  .next-meta-item { flex-wrap: wrap; }

  .panel-header { padding: var(--space-3) var(--space-4); }
  .panel-body { padding: var(--space-3) var(--space-4) var(--space-4); }

  .agenda-row {
    grid-template-columns: 1fr auto;
    gap: var(--space-3);
    padding: var(--space-3);
  }
  .row-time { grid-row: 1 / 3; grid-column: 1; justify-self: start; }
  .row-main { grid-column: 2; grid-row: 1; }
  .row-status { grid-column: 1 / -1; grid-row: 2; }
  .row-action { grid-column: 1 / -1; grid-row: 3; justify-self: end; }
  .row-btn { width: 100%; }
}
</style>