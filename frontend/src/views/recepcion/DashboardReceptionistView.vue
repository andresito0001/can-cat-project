<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight, Banknote, CalendarDays, CheckCircle2, Clock,
  CreditCard, FileText, Inbox, Receipt, User,
  ShieldCheck,
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth.store'
import { useToast } from '@/composables/useToast'
import ToastContainer from '@/components/ui/ToastContainer.vue'
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

const stats = computed(() => {
  const totalHoy = agendaHoy.value.length
  const completadasHoy = agendaHoy.value.filter(c => {
    const estado = String(c.estadoNombre || '').trim()
    return estado === 'Completada' || c.atendida === true
  }).length
  const totalCobros = citasPendientesPago.value.length + facturasPendientes.value.length

  return [
    {
      etiqueta: 'Citas de hoy',
      valor: totalHoy,
      icono: CalendarDays,
      color: '#0F766E',
      bg: '#F0FDFA',
    },
    {
      etiqueta: 'Pagos por verificar',
      valor: pagosPorVerificar.value.length,
      icono: ShieldCheck,
      color: '#D97706',
      bg: '#FFFBEB',
      resaltar: pagosPorVerificar.value.length > 0,
    },
    {
      etiqueta: 'Pendientes de pago',
      valor: citasPendientesPago.value.length,
      icono: CreditCard,
      color: '#F59E0B',
      bg: '#FFFBEB',
    },
    {
      etiqueta: 'Cobros en mostrador',
      valor: totalCobros,
      icono: Banknote,
      color: '#8B5CF6',
      bg: '#F5F3FF',
    },
    {
      etiqueta: 'Completadas hoy',
      valor: completadasHoy,
      icono: CheckCircle2,
      color: '#3B82F6',
      bg: '#EFF6FF',
    },
  ]
})

const proximaCita = computed(() => {
  return agendaHoy.value.find(c => {
    const estado = String(c.estadoNombre || '').trim()
    return c.atendida !== true && !['Completada', 'Cancelada'].includes(estado)
  }) || null
})

const citasAgenda = computed(() => {
  if (!proximaCita.value) return agendaHoy.value
  return agendaHoy.value.filter(c => c.idCita !== proximaCita.value.idCita)
})
const citasVisibles = computed(() => citasAgenda.value.slice(0, 6))
const citasOcultas = computed(() => Math.max(0, citasAgenda.value.length - 6))

const cobrosPendientes = computed(() => {
  const citas = citasPendientesPago.value.map(c => ({
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
  const facturas = facturasPendientes.value.map(f => ({
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
    Pendiente_Pago: '#F59E0B',
    Confirmada: '#10B981',
    En_Atencion: '#F97316',
    Completada: '#64748B',
    Cancelada: '#EF4444'
  }

  const color = estadoColor || mapaColores[estadoNombre] || '#64748B'
  return { color, borderColor: color, backgroundColor: `${color}1A` }
}

function irACobros() {
  router.push('/recepcion/cobrar')
}
function irAVerificacion() {
  router.push('/recepcion/caja?tab=verificacion')
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
  pagosPorVerificar.value = verificacionRes.status === 'fulfilled'
    ? verificacionRes.value.data || []
    : []

  const todosFallaron = [agendaRes, pendientesRes, facturasRes, verificacionRes]
    .every(r => r.status === 'rejected')
  if (todosFallaron) {
    toastError(getApiErrorMessage(agendaRes.reason) || 'No se pudieron cargar los datos del panel')
  }
  cargando.value = false
}

onMounted(cargarDatos)
</script>

<template>
  <div class="dashboard">
    <ToastContainer />

    <header class="hero">
      <div class="hero-left">
        <p class="hero-date">{{ fechaCompleta(hoy) }}</p>
        <h1>Hola, {{ nombreRecep }}</h1>
        <p class="hero-sub">Este es el resumen de la operación en mostrador.</p>
      </div>
      <div class="hero-right">
        <span class="hero-badge">
          <CalendarDays :size="14" />
          {{ agendaHoy.length }} {{ agendaHoy.length === 1 ? 'cita' : 'citas' }} hoy
        </span>
      </div>
    </header>

    <div v-if="cargando" class="loading-state">
      <div class="spin"></div>
      <p>Cargando el panel…</p>
    </div>

    <template v-else>
      <section class="stats-grid">
        <template v-for="s in stats" :key="s.etiqueta">
          <router-link
            v-if="s.etiqueta === 'Pagos por verificar'"
            :to="{ path: '/recepcion/caja', query: { tab: 'verificacion' } }"
            class="stat-card stat-card--link"
            :class="{ 'is-warn': s.resaltar }"
          >
            <div class="stat-icon" :style="{ backgroundColor: s.bg, color: s.color }">
              <component :is="s.icono" :size="22" />
            </div>
            <div class="stat-texto">
              <p class="stat-value" :class="{ 'is-warn': s.resaltar }">{{ s.valor }}</p>
              <p class="stat-label">{{ s.etiqueta }}</p>
            </div>
          </router-link>

          <article
            v-else
            class="stat-card"
            :class="{ 'is-warn': s.resaltar }"
          >
            <div class="stat-icon" :style="{ backgroundColor: s.bg, color: s.color }">
              <component :is="s.icono" :size="22" />
            </div>
            <div class="stat-texto">
              <p class="stat-value" :class="{ 'is-warn': s.resaltar }">{{ s.valor }}</p>
              <p class="stat-label">{{ s.etiqueta }}</p>
            </div>
          </article>
        </template>
      </section>

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
            <User :size="13" class="inline-icon" />
            {{ proximaCita.clienteNombre || 'Cliente' }}
          </p>
          <div class="proximo-meta">
            <span v-if="proximaCita.servicioNombre">
              <FileText :size="13" /> {{ proximaCita.servicioNombre }}
            </span>
            <span v-if="proximaCita.horaFin">
              <Clock :size="13" /> {{ horaCorta(proximaCita.horaInicio) }} – {{ horaCorta(proximaCita.horaFin) }}
            </span>
            <span
              v-if="proximaCita.estadoNombre"
              class="proximo-estado"
            >
              {{ etiquetaEstado(proximaCita.estadoNombre) }}
            </span>
          </div>
        </div>
        <div class="proximo-accion">
          <button
            class="btn-accion-grande"
            type="button"
            @click="verDetalleCita(proximaCita.idCita)"
          >
            Ver en agenda
            <ArrowRight :size="16" />
          </button>
        </div>
      </section>

      <section class="content-grid">
        <div class="card">
          <div class="card-header">
            <div>
              <h3>Agenda de hoy</h3>
              <p class="card-sub">
                {{ agendaHoy.length
                  ? `${agendaHoy.length} cita${agendaHoy.length === 1 ? '' : 's'} programada${agendaHoy.length === 1 ? '' : 's'}`
                  : 'Sin citas programadas' }}
              </p>
            </div>
            <button class="btn-link" type="button" @click="irAAgenda">
              Ver agenda completa
            </button>
          </div>
          <div class="card-body">
            <div v-if="!agendaHoy.length" class="empty-state">
              <Inbox :size="40" />
              <p>No hay citas programadas para hoy.</p>
              <button class="btn-link" type="button" @click="irAAgenda">
                Ir a la agenda
              </button>
            </div>

            <div v-else-if="!citasVisibles.length" class="empty-state slim">
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
                  <button
                    v-if="!cita.atendida && cita.estadoNombre !== 'Completada'"
                    class="btn-ver"
                    type="button"
                    @click="verDetalleCita(cita.idCita)"
                  >
                    Ver
                  </button>
                  <span v-else class="cita-ok" title="Completada">
                    <CheckCircle2 :size="18" />
                  </span>
                </div>
              </article>

              <p v-if="citasOcultas > 0" class="more-link">
                + {{ citasOcultas }} cita{{ citasOcultas === 1 ? '' : 's' }} más en la agenda completa
              </p>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <div>
              <h3>Cobros pendientes</h3>
              <p class="card-sub">
                {{ cobrosPendientes.length
                  ? `${cobrosPendientes.length} por procesar`
                  : 'Sin cobros pendientes' }}
              </p>
            </div>
            <button
              v-if="cobrosPendientes.length"
              class="btn-link"
              type="button"
              @click="irACobros"
            >
              Ir a caja
            </button>
          </div>
          <div class="card-body">
            <div v-if="!cobrosPendientes.length" class="empty-state">
              <CheckCircle2 :size="40" />
              <p>No hay cobros pendientes en mostrador.</p>
            </div>

            <div v-else class="cobros-lista">
              <article
                v-for="cobro in cobrosVisibles"
                :key="cobro.id"
                class="cobro-item"
              >
                <div
                  class="cobro-avatar"
                  :class="{ 'is-factura': cobro.tipo === 'factura' }"
                >
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

                <button class="btn-cobrar" type="button" @click="irACobros">
                  Cobrar
                </button>
              </article>

              <p v-if="cobrosOcultos > 0" class="more-link">
                + {{ cobrosOcultos }} cobro{{ cobrosOcultos === 1 ? '' : 's' }} más
              </p>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: 28px 24px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
}
button { font-family: inherit; }

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  padding: 24px 28px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
}
.hero-date {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #0F766E;
}
.hero-left h1 {
  margin: 0 0 4px;
  font-size: 26px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.02em;
  line-height: 1.2;
}
.hero-sub {
  margin: 0;
  font-size: 14px;
  color: #64748B;
}
.hero-right { display: flex; align-items: center; }
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #CCFBF1;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #0F766E;
  white-space: nowrap;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
  margin-bottom: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 18px;
  transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease;
}
.stat-card--link {
  text-decoration: none;
  color: inherit;
  cursor: pointer;
}
.stat-card:hover {
  border-color: #CBD5E1;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -10px rgba(15, 23, 42, .08);
}
.stat-card.is-warn {
  border-color: #FDE68A;
  background: linear-gradient(135deg, #FFFBEB 0%, #FFFFFF 60%);
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-texto { min-width: 0; }
.stat-value {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.1;
  letter-spacing: -0.01em;
}
.stat-card .stat-value.is-warn {
  color: #B45309;
}
.stat-label {
  margin: 3px 0 0;
  font-size: 12.5px;
  color: #64748B;
  font-weight: 500;
}

.proximo {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 24px;
  align-items: center;
  padding: 24px 28px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #0F766E 0%, #0D9488 100%);
  color: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 32px -12px rgba(15, 118, 110, .5);
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
  background: radial-gradient(circle, rgba(255, 255, 255, .12) 0%, transparent 70%);
  pointer-events: none;
}
.proximo-info { min-width: 0; position: relative; z-index: 1; }
.proximo-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: rgba(255, 255, 255, .2);
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  margin-bottom: 10px;
  backdrop-filter: blur(4px);
}
.proximo-nombre {
  margin: 0 0 4px;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.01em;
  line-height: 1.2;
}
.proximo-sub {
  margin: 0 0 12px;
  font-size: 13.5px;
  color: rgba(255, 255, 255, .85);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.inline-icon { display: inline-block; vertical-align: middle; }
.proximo-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  font-size: 13px;
  color: rgba(255, 255, 255, .9);
  align-items: center;
}
.proximo-meta span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.proximo-estado {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .2px;
  background: rgba(255, 255, 255, .22);
  border: 1px solid rgba(255, 255, 255, .35);
}
.proximo-accion { position: relative; z-index: 1; }
.btn-accion-grande {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  background: #fff;
  color: #0F766E;
  border: none;
  border-radius: 12px;
  font-size: 14.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  white-space: nowrap;
  font-family: inherit;
}
.btn-accion-grande:hover {
  background: #F0FDFA;
  transform: translateY(-2px);
  box-shadow: 0 12px 24px -8px rgba(0, 0, 0, .25);
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
  gap: 20px;
  align-items: start;
  margin-bottom: 20px;
}

.card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
}
.card-header h3 { margin: 0; font-size: 16px; font-weight: 700; color: #0F172A; }
.card-sub { margin: 3px 0 0; font-size: 12.5px; color: #64748B; }
.card-body { padding: 20px 24px 24px; }

.citas-lista { display: flex; flex-direction: column; gap: 10px; }
.cita-item {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  transition: border-color .2s ease, background-color .2s ease;
}
.cita-item:hover { border-color: #CBD5E1; background: #fff; }
.cita-item.is-completada { opacity: .72; }

.cita-hora {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 58px;
  padding: 8px 6px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  flex-shrink: 0;
}
.hora-inicio {
  font-size: 15px;
  font-weight: 700;
  color: #0F766E;
  line-height: 1;
  letter-spacing: -0.01em;
}
.hora-fin {
  margin-top: 3px;
  font-size: 10.5px;
  font-weight: 600;
  color: #94A3B8;
  line-height: 1;
}
.cita-item.is-completada .hora-inicio { color: #64748B; }

.cita-info { min-width: 0; }
.cita-mascota {
  margin: 0;
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cita-detalle {
  margin: 3px 0 0;
  font-size: 12.5px;
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cita-dueno {
  margin: 4px 0 0;
  font-size: 12px;
  color: #94A3B8;
  display: flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cita-estado { flex-shrink: 0; }
.status-pill {
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

.cita-accion { flex-shrink: 0; }
.btn-ver {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  background: #0F766E;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
}
.btn-ver:hover {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, .25);
}
.cita-ok {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #ECFDF5;
  color: #059669;
}

.cobros-lista { display: flex; flex-direction: column; gap: 8px; }
.cobro-item {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  gap: 12px;
  align-items: center;
  padding: 12px 14px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  transition: border-color .2s ease, background-color .2s ease;
}
.cobro-item:hover { border-color: #CBD5E1; background: #fff; }

.cobro-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F0FDFA;
  color: #0F766E;
  flex-shrink: 0;
}
.cobro-avatar.is-factura {
  background: #F5F3FF;
  color: #8B5CF6;
}

.cobro-info { min-width: 0; }
.cobro-titulo {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cobro-sub {
  margin: 2px 0 0;
  font-size: 11.5px;
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cobro-monto {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}
.cobro-tipo {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #94A3B8;
}
.cobro-valor {
  font-size: 14px;
  font-weight: 700;
  color: #0F766E;
}

.btn-cobrar {
  padding: 7px 14px;
  background: #0F766E;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
  flex-shrink: 0;
}
.btn-cobrar:hover {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, .25);
}

.btn-link {
  background: none;
  border: none;
  padding: 0;
  color: #0F766E;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
}
.btn-link:hover { text-decoration: underline; }

.more-link {
  margin: 14px 0 0;
  font-size: 12.5px;
  color: #94A3B8;
  text-align: center;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 80px 24px;
  color: #64748B;
  font-size: 14px;
}
.spin {
  width: 32px;
  height: 32px;
  border: 3px solid #E2E8F0;
  border-top-color: #0F766E;
  border-radius: 50%;
  animation: girar .8s linear infinite;
}
@keyframes girar { to { transform: rotate(360deg); } }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 24px;
  color: #94A3B8;
  text-align: center;
  font-size: 14px;
}
.empty-state.slim { padding: 24px; }
.empty-state p { margin: 0; }

@media (max-width: 1100px) {
  .stats-grid { grid-template-columns: repeat(3, 1fr); }
  .content-grid { grid-template-columns: 1fr; }
}
@media (max-width: 1024px) {
  .proximo { grid-template-columns: 1fr; gap: 18px; }
  .proximo-accion { width: 100%; }
  .btn-accion-grande { width: 100%; justify-content: center; }
}
@media (max-width: 768px) {
  .dashboard { padding: 16px 16px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero-left h1 { font-size: 22px; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .proximo { padding: 20px; border-radius: 14px; }
  .proximo-nombre { font-size: 20px; }
  .cita-item {
    grid-template-columns: auto 1fr;
    gap: 12px;
  }
  .cita-estado,
  .cita-accion { grid-column: 2; }
  .cita-accion { justify-self: start; }
  .cita-estado { margin-top: 6px; }

  .cobro-item {
    grid-template-columns: auto 1fr auto;
    gap: 10px;
  }
  .btn-cobrar { grid-column: 2 / -1; justify-self: end; }
}
</style>