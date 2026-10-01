<template>
  <div class="dashboard">
    <ToastContainer />

    <!-- Hero -->
    <header class="page-header">
      <div>
        <p class="hero-date">{{ fechaCompleta(hoy) }}</p>
        <h1>Hola, {{ nombreRecep }}</h1>
        <p class="page-header-sub">
          Este es el resumen de la operación en mostrador.
        </p>
      </div>
      <div class="hero-badge">
        <CalendarDays :size="14" />
        {{ agendaHoy.length }} {{ agendaHoy.length === 1 ? 'cita' : 'citas' }} hoy
      </div>
    </header>

    <!-- Loading -->
    <div v-if="cargando" class="card">
      <div class="card-body">
        <div class="loading-state">
          <span class="spinner spinner-lg" />
          <p>Cargando el panel…</p>
        </div>
      </div>
    </div>

    <template v-else>
      <!-- KPIs -->
      <section class="stats-grid">
        <router-link
          :to="{ path: '/recepcion/caja', query: { tab: 'verificacion' } }"
          class="stat-card stat-card-link"
          :class="{ 'is-warn': pagosPorVerificar.length > 0 }"
        >
          <div class="stat-icon stat-icon-warning">
            <ShieldCheck :size="22" />
          </div>
          <div class="stat-texto">
            <p class="stat-value" :class="{ 'is-warn': pagosPorVerificar.length > 0 }">
              {{ pagosPorVerificar.length }}
            </p>
            <p class="stat-label">Pagos por verificar</p>
          </div>
        </router-link>

        <article class="stat-card">
          <div class="stat-icon stat-icon-brand"><CalendarDays :size="22" /></div>
          <div class="stat-texto">
            <p class="stat-value">{{ agendaHoy.length }}</p>
            <p class="stat-label">Citas de hoy</p>
          </div>
        </article>

        <article class="stat-card">
          <div class="stat-icon stat-icon-warning"><CreditCard :size="22" /></div>
          <div class="stat-texto">
            <p class="stat-value">{{ citasPendientesPago.length }}</p>
            <p class="stat-label">Pendientes de pago</p>
          </div>
        </article>

        <article class="stat-card">
          <div class="stat-icon stat-icon-purple"><Banknote :size="22" /></div>
          <div class="stat-texto">
            <p class="stat-value">{{ totalCobros }}</p>
            <p class="stat-label">Por cobrar</p>
          </div>
        </article>

        <article class="stat-card">
          <div class="stat-icon stat-icon-info"><CheckCircle2 :size="22" /></div>
          <div class="stat-texto">
            <p class="stat-value">{{ completadasHoy }}</p>
            <p class="stat-label">Completadas hoy</p>
          </div>
        </article>
      </section>

      <!-- Próxima cita destacada -->
      <section v-if="proximaCita" class="proximo">
        <div class="proximo-info">
          <span class="proximo-tag">
            <Clock :size="12" />
            {{ horaCorta(proximaCita.horaInicio) }} · Próximo en atender
          </span>
          <h2 class="proximo-nombre">
            {{ proximaCita.mascotaNombre || proximaCita.mascota || 'Paciente' }}
          </h2>
          <p class="proximo-sub">
            <User :size="13" /> {{ proximaCita.clienteNombre || 'Cliente' }}
          </p>
          <div class="proximo-meta">
            <span v-if="proximaCita.servicioNombre">
              <FileText :size="13" /> {{ proximaCita.servicioNombre }}
            </span>
            <span v-if="proximaCita.horaFin">
              <Clock :size="13" /> {{ horaCorta(proximaCita.horaInicio) }} – {{ horaCorta(proximaCita.horaFin) }}
            </span>
            <span v-if="proximaCita.estadoNombre" class="proximo-estado">
              {{ etiquetaEstado(proximaCita.estadoNombre) }}
            </span>
          </div>
        </div>
        <div class="proximo-accion">
          <AppButton variant="secondary" @click="verDetalleCita(proximaCita.idCita)">
            Ver en agenda
            <template #icon-right><ArrowRight :size="16" /></template>
          </AppButton>
        </div>
      </section>

      <!-- Grid principal -->
      <section class="content-grid">
        <!-- Agenda del día -->
        <AppCard>
          <template #header>
            <div>
              <h3>Agenda de hoy</h3>
              <p class="card-header-sub">
                {{ agendaHoy.length
                  ? `${agendaHoy.length} cita${agendaHoy.length === 1 ? '' : 's'} programada${agendaHoy.length === 1 ? '' : 's'}`
                  : 'Sin citas programadas' }}
              </p>
            </div>
          </template>
          <template #header-actions>
            <button class="link-action" type="button" @click="irAAgenda">
              Ver agenda completa
            </button>
          </template>

          <AppEmptyState
            v-if="!agendaHoy.length"
            :icon="Inbox"
            title="No hay citas programadas para hoy"
            description="Las citas agendadas aparecerán aquí."
          >
            <template #action>
              <AppButton variant="secondary" @click="irAAgenda">Ir a la agenda</AppButton>
            </template>
          </AppEmptyState>

          <div v-else-if="!citasVisibles.length" class="empty-compact">
            <CheckCircle2 :size="32" />
            <p>No hay más citas pendientes hoy.</p>
          </div>

          <div v-else class="citas-lista">
            <article
              v-for="cita in citasVisibles"
              :key="cita.idCita"
              class="cita-item"
              :class="{ 'is-completada': cita.atendida || cita.estadoNombre === 'Completada' }"
            >
              <div class="cita-hora">
                <span class="hora-inicio">{{ horaCorta(cita.horaInicio) }}</span>
                <span class="hora-fin">{{ horaCorta(cita.horaFin) }}</span>
              </div>

              <div class="cita-info">
                <p class="cita-mascota">
                  {{ cita.mascotaNombre || cita.mascota || 'Paciente' }}
                </p>
                <p class="cita-detalle">
                  {{ cita.especie || cita.tipoAtencion || 'Consulta' }}
                  <template v-if="cita.raza"> · {{ cita.raza }}</template>
                </p>
                <p class="cita-dueno">
                  <User :size="12" /> {{ cita.clienteNombre || 'Cliente' }}
                </p>
              </div>

              <div class="cita-estado">
                <span
                  class="status-pill"
                  :style="estiloEstado(cita.estadoNombre, cita.estadoColor)"
                >
                  {{ etiquetaEstado(cita.estadoNombre) }}
                </span>
              </div>

              <div class="cita-accion">
                <AppButton
                  v-if="!cita.atendida && cita.estadoNombre !== 'Completada'"
                  variant="primary"
                  size="sm"
                  @click="verDetalleCita(cita.idCita)"
                >
                  Ver
                </AppButton>
                <span v-else class="cita-ok" title="Completada">
                  <CheckCircle2 :size="18" />
                </span>
              </div>
            </article>

            <p v-if="citasOcultas > 0" class="more-link">
              + {{ citasOcultas }} cita{{ citasOcultas === 1 ? '' : 's' }} más en la agenda completa
            </p>
          </div>
        </AppCard>

        <!-- Cobros pendientes -->
        <AppCard>
          <template #header>
            <div>
              <h3>Cobros pendientes</h3>
              <p class="card-header-sub">
                {{ cobrosPendientes.length
                  ? `${cobrosPendientes.length} por procesar`
                  : 'Sin cobros pendientes' }}
              </p>
            </div>
          </template>
          <template #header-actions>
            <button v-if="cobrosPendientes.length" class="link-action" type="button" @click="irACobros">
              Ir a caja
            </button>
          </template>

          <AppEmptyState
            v-if="!cobrosPendientes.length"
            :icon="CheckCircle2"
            title="No hay cobros pendientes"
            description="Todos los cobros del mostrador están al día."
          />

          <div v-else class="cobros-lista">
            <article
              v-for="cobro in cobrosVisibles"
              :key="cobro.id"
              class="cobro-item"
            >
              <div class="cobro-avatar" :class="{ 'is-factura': cobro.tipo === 'factura' }">
                <Receipt v-if="cobro.tipo === 'factura'" :size="16" />
                <User v-else :size="16" />
              </div>

              <div class="cobro-info">
                <p class="cobro-titulo">{{ cobro.titulo }}</p>
                <p class="cobro-sub">{{ cobro.detalle }}</p>
              </div>

              <div class="cobro-monto">
                <span class="cobro-tipo">
                  {{ cobro.tipo === 'factura' ? 'Factura' : 'Cita' }}
                </span>
                <strong class="cobro-valor">{{ fmtUsd(cobro.monto) }}</strong>
              </div>

              <AppButton variant="primary" size="sm" @click="irACobros">
                Cobrar
              </AppButton>
            </article>

            <p v-if="cobrosOcultos > 0" class="more-link">
              + {{ cobrosOcultos }} cobro{{ cobrosOcultos === 1 ? '' : 's' }} más
            </p>
          </div>
        </AppCard>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight, Banknote, CalendarDays, CheckCircle2, Clock,
  CreditCard, FileText, Inbox, Receipt, User, ShieldCheck,
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth.store'
import { useToast } from '@/composables/useToast'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import { getAgenda, getPendientesPago } from '@/api/citas.api'
import { getFacturasPendientes, getPagosPendientesVerificacion } from '@/api/pagos.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { fechaCompleta, hoyISO, horaCorta } from '@/utils/fecha'

const router = useRouter()
const authStore = useAuthStore()
const { toastError } = useToast()

const hoy = hoyISO()
const cargando = ref(true)
const agendaHoy = ref([])
const citasPendientesPago = ref([])
const facturasPendientes = ref([])
const pagosPorVerificar = ref([])

const nombreRecep = computed(() => authStore.userName || 'Recepción')

const completadasHoy = computed(() =>
  agendaHoy.value.filter((c) => {
    const estado = String(c.estadoNombre || '').trim()
    return estado === 'Completada' || c.atendida === true
  }).length
)

const totalCobros = computed(() =>
  citasPendientesPago.value.length + facturasPendientes.value.length
)

const proximaCita = computed(() =>
  agendaHoy.value.find((c) => {
    const estado = String(c.estadoNombre || '').trim()
    return c.atendida !== true && !['Completada', 'Cancelada'].includes(estado)
  }) || null
)

const citasAgenda = computed(() => {
  if (!proximaCita.value) return agendaHoy.value
  return agendaHoy.value.filter((c) => c.idCita !== proximaCita.value.idCita)
})
const citasVisibles = computed(() => citasAgenda.value.slice(0, 6))
const citasOcultas = computed(() => Math.max(0, citasAgenda.value.length - 6))

const cobrosPendientes = computed(() => {
  const citas = citasPendientesPago.value.map((c) => ({
    id: `cita-${c.idCita}`,
    tipo: 'cita',
    idOriginal: c.idCita,
    titulo: c.mascotaNombre || c.mascota || 'Cita',
    sub: c.clienteNombre || '',
    detalle: c.servicioNombre || c.tipoAtencion || 'Cita pendiente',
    monto: c.costoEstimado || c.costoUsd || null,
    hora: horaCorta(c.horaInicio),
    fechaISO: c.fechaCita,
  }))
  const facturas = facturasPendientes.value.map((f) => ({
    id: `factura-${f.idFactura}`,
    tipo: 'factura',
    idOriginal: f.idFactura,
    titulo: f.cliente?.nombre || 'Cliente',
    sub: f.cliente?.documento || '',
    detalle: f.detalles?.length
      ? `${f.detalles.length} ${f.detalles.length === 1 ? 'ítem' : 'ítems'}`
      : `Factura ${f.numeroControl}`,
    monto: f.totalNeto,
    hora: '',
    fechaISO: f.fechaEmision,
  }))
  return [...citas, ...facturas]
})

const cobrosVisibles = computed(() => cobrosPendientes.value.slice(0, 5))
const cobrosOcultos = computed(() => Math.max(0, cobrosPendientes.value.length - 5))

function fmtUsd(v) {
  if (v == null) return '—'
  return `$${Number(v).toFixed(2)}`
}

function etiquetaEstado(estadoNombre) {
  return String(estadoNombre || '').replaceAll('_', ' ')
}

function estiloEstado(estadoNombre, estadoColor) {
  const mapaColores = {
    Pendiente_Pago: 'var(--warning-500)',
    Confirmada: 'var(--success-500)',
    En_Atencion: 'var(--warning-600)',
    Completada: 'var(--neutral-500)',
    Cancelada: 'var(--danger-500)',
  }
  const color = estadoColor || mapaColores[estadoNombre] || 'var(--neutral-500)'
  return { color, borderColor: color, backgroundColor: 'transparent' }
}

function irACobros() {
  router.push('/recepcion/caja')
}
function irAAgenda() {
  router.push('/recepcion/citas')
}
function verDetalleCita(idCita) {
  if (!idCita) return
  router.push(`/recepcion/citas?cita=${idCita}`)
}

async function cargarDatos() {
  cargando.value = true
  const [agendaRes, pendientesRes, facturasRes, verificacionRes] = await Promise.allSettled([
    getAgenda({ fecha: hoy }),
    getPendientesPago(),
    getFacturasPendientes(),
    getPagosPendientesVerificacion(),
  ])

  agendaHoy.value = agendaRes.status === 'fulfilled' ? agendaRes.value.data || [] : []
  citasPendientesPago.value = pendientesRes.status === 'fulfilled' ? pendientesRes.value.data || [] : []
  facturasPendientes.value = facturasRes.status === 'fulfilled' ? facturasRes.value.data || [] : []
  pagosPorVerificar.value = verificacionRes.status === 'fulfilled' ? verificacionRes.value.data || [] : []

  const todosFallaron = [agendaRes, pendientesRes, facturasRes, verificacionRes]
    .every((r) => r.status === 'rejected')
  if (todosFallaron) {
    toastError(getApiErrorMessage(agendaRes.reason) || 'No se pudieron cargar los datos del panel')
  }
  cargando.value = false
}

onMounted(cargarDatos)
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.hero-date {
  margin: 0 0 var(--space-2);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--brand-700);
}
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--brand-100);
  border-radius: var(--radius-full);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--brand-700);
  white-space: nowrap;
}

/* KPIs */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-4);
}
.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-5);
  transition: all var(--duration-base) var(--ease-out);
  text-decoration: none;
  color: inherit;
}
.stat-card:hover {
  border-color: var(--border-strong);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.stat-card.is-warn {
  border-color: var(--warning-200);
  background: linear-gradient(135deg, var(--warning-50) 0%, var(--bg-surface) 60%);
}
.stat-card-link { cursor: pointer; }
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-icon-brand   { background: var(--brand-50);   color: var(--brand-700); }
.stat-icon-info    { background: var(--info-50);    color: var(--info-600); }
.stat-icon-warning { background: var(--warning-50); color: var(--warning-600); }
.stat-icon-purple  { background: var(--purple-50);  color: var(--purple-600); }

.stat-texto { min-width: 0; }
.stat-value {
  margin: 0;
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.1;
  letter-spacing: var(--tracking-tight);
}
.stat-value.is-warn { color: var(--warning-700); }
.stat-label {
  margin: var(--space-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}

/* PRÓXIMA CITA */
.proximo {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: var(--space-6);
  align-items: center;
  padding: var(--space-6) var(--space-7);
  background: linear-gradient(135deg, var(--brand-700) 0%, var(--brand-600) 100%);
  color: var(--text-inverse);
  border-radius: var(--radius-3xl);
  box-shadow: 0 12px 32px -12px rgba(15, 118, 110, 0.5);
  position: relative;
  overflow: hidden;
}
.proximo::after {
  content: '';
  position: absolute;
  right: -60px;
  top: -60px;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.12) 0%, transparent 70%);
  pointer-events: none;
}
.proximo-info { min-width: 0; position: relative; z-index: 1; }
.proximo-tag {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  background: rgba(255, 255, 255, 0.20);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: var(--space-3);
  backdrop-filter: blur(4px);
}
.proximo-nombre {
  margin: 0 0 var(--space-1);
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  letter-spacing: var(--tracking-tight);
  line-height: var(--leading-tight);
}
.proximo-sub {
  margin: 0 0 var(--space-3);
  font-size: var(--text-md);
  color: rgba(255, 255, 255, 0.85);
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
}
.proximo-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  font-size: var(--text-md);
  color: rgba(255, 255, 255, 0.90);
  align-items: center;
}
.proximo-meta span { display: inline-flex; align-items: center; gap: var(--space-2); }
.proximo-estado {
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  background: rgba(255, 255, 255, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.35);
}

/* GRID */
.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
  gap: var(--space-5);
  align-items: start;
}
.link-action {
  background: none;
  border: none;
  color: var(--brand-700);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
}
.link-action:hover { text-decoration: underline; }

/* CITAS */
.citas-lista { display: flex; flex-direction: column; gap: var(--space-3); }
.cita-item {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  transition: all var(--duration-base) var(--ease-out);
}
.cita-item:hover { border-color: var(--border-strong); background: var(--bg-surface); }
.cita-item.is-completada { opacity: 0.72; }

.cita-hora {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 58px;
  padding: var(--space-2) var(--space-1);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}
.hora-inicio {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  line-height: 1;
}
.hora-fin {
  margin-top: 3px;
  font-size: var(--text-2xs);
  font-weight: var(--font-semibold);
  color: var(--text-tertiary);
  line-height: 1;
}
.cita-item.is-completada .hora-inicio { color: var(--text-secondary); }

.cita-info { min-width: 0; }
.cita-mascota {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cita-detalle {
  margin: 3px 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cita-dueno {
  margin: var(--space-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: var(--space-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cita-estado { flex-shrink: 0; }
.status-pill {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  border: 1px solid;
  white-space: nowrap;
}
.cita-accion { flex-shrink: 0; }
.cita-ok {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--success-50);
  color: var(--success-600);
}

.empty-compact {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-8) var(--space-4);
  color: var(--text-tertiary);
  text-align: center;
  font-size: var(--text-md);
}
.empty-compact p { margin: 0; }

/* COBROS */
.cobros-lista { display: flex; flex-direction: column; gap: var(--space-3); }
.cobro-item {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  gap: var(--space-3);
  align-items: center;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  transition: all var(--duration-base) var(--ease-out);
}
.cobro-item:hover { border-color: var(--border-strong); background: var(--bg-surface); }

.cobro-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--brand-50);
  color: var(--brand-700);
  flex-shrink: 0;
}
.cobro-avatar.is-factura { background: var(--purple-50); color: var(--purple-600); }

.cobro-info { min-width: 0; }
.cobro-titulo {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cobro-sub {
  margin: 2px 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cobro-monto {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 1px;
  flex-shrink: 0;
}
.cobro-tipo {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
}
.cobro-valor {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--brand-700);
}

.more-link {
  margin: var(--space-3) 0 0;
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  text-align: center;
}

/* LOADING */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12) var(--space-6);
  color: var(--text-secondary);
  font-size: var(--text-base);
}

/* RESPONSIVE */
@media (max-width: 1100px) {
  .stats-grid { grid-template-columns: repeat(3, 1fr); }
  .content-grid { grid-template-columns: 1fr; }
}
@media (max-width: 1024px) {
  .proximo { grid-template-columns: 1fr; gap: var(--space-4); }
}
@media (max-width: 640px) {
  .dashboard { padding: var(--space-4); gap: var(--space-4); }
  .stats-grid { grid-template-columns: 1fr; }
  .proximo { padding: var(--space-5); border-radius: var(--radius-2xl); }
  .proximo-nombre { font-size: var(--text-3xl); }

  .cita-item { grid-template-columns: auto 1fr; gap: var(--space-3); }
  .cita-estado,
  .cita-accion { grid-column: 2; }
  .cita-accion { justify-self: start; }
  .cita-estado { margin-top: var(--space-1); }

  .cobro-item { grid-template-columns: auto 1fr auto; }
  .cobro-item :deep(.btn) { grid-column: 2 / -1; justify-self: end; }
}
</style>