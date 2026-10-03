<template>
  <div class="dashboard">
    <ToastContainer />

    <!-- ═══ HERO ═══ -->
    <header class="page-header">
      <div>
        <p class="hero-date">{{ fechaLarga }}</p>
        <h1>Hola, {{ nombreCliente }}</h1>
        <p class="page-header-sub">
          Gestiona tus citas, mascotas y pagos desde un solo lugar.
        </p>
      </div>
      <div class="hero-badge">
        <PawPrint :size="14" />
        {{ stats.mascotas }} {{ stats.mascotas === 1 ? 'mascota' : 'mascotas' }} registradas
      </div>
    </header>

    <!-- ═══ LOADING ═══ -->
    <div v-if="cargando" class="card">
      <div class="card-body">
        <div class="loading-state">
          <span class="spinner spinner-lg" />
          <p>Cargando tu información…</p>
        </div>
      </div>
    </div>

    <template v-else>
      <!-- ═══ KPIs ═══ -->
      <section class="stats-grid">
        <article class="stat-card">
          <div class="stat-icon stat-icon--info">
            <CalendarCheck :size="22" />
          </div>
          <div class="stat-texto">
            <p class="stat-value">{{ stats.citasPendientes }}</p>
            <p class="stat-label">Citas pendientes</p>
          </div>
        </article>
        <article class="stat-card">
          <div class="stat-icon stat-icon--brand">
            <PawPrint :size="22" />
          </div>
          <div class="stat-texto">
            <p class="stat-value">{{ stats.mascotas }}</p>
            <p class="stat-label">Mis mascotas</p>
          </div>
        </article>
        <article class="stat-card">
          <div class="stat-icon stat-icon--warning">
            <CreditCard :size="22" />
          </div>
          <div class="stat-texto">
            <p class="stat-value">{{ stats.pagosPendientes }}</p>
            <p class="stat-label">Pagos pendientes</p>
          </div>
        </article>
        <article class="stat-card">
          <div class="stat-icon stat-icon--purple">
            <Stethoscope :size="22" />
          </div>
          <div class="stat-texto">
            <p class="stat-value">{{ stats.consultasEsteMes }}</p>
            <p class="stat-label">Consultas este mes</p>
          </div>
        </article>
      </section>

      <!-- ═══ PRÓXIMA CITA (destacada) ═══ -->
      <section v-if="citaDestacada" class="proximo">
        <div class="proximo-info">
          <span class="proximo-tag">
            <Clock :size="12" />
            {{ textoCuentaRegresiva(citaDestacada) }}
          </span>
          <h2 class="proximo-nombre">{{ citaDestacada.motivo }}</h2>
          <p class="proximo-sub">
            <PawPrint :size="13" /> {{ citaDestacada.mascota }}
          </p>
          <div class="proximo-meta">
            <span><Calendar :size="13" /> {{ citaDestacada.dia }} {{ citaDestacada.mes }}</span>
            <span><Clock :size="13" /> {{ citaDestacada.hora }}</span>
            <span class="proximo-estado" :class="citaDestacada.estado">
              {{ citaDestacada.estadoTexto }}
            </span>
          </div>
        </div>
        <div class="proximo-accion">
          <AppButton variant="secondary" @click="router.push('/cliente/mis-citas')">
            Ver detalle
            <template #icon-right><ArrowRight :size="16" /></template>
          </AppButton>
        </div>
      </section>

      <!-- ═══ GRID PRINCIPAL ═══ -->
      <section class="content-grid">
        <!-- Próximas citas -->
        <AppCard
          title="Próximas citas"
          :subtitle="proximasCitas.length
            ? `${proximasCitas.length} programada${proximasCitas.length === 1 ? '' : 's'}`
            : 'Sin citas programadas'"
        >
          <template #header-actions>
            <RouterLink to="/cliente/solicitar-cita" class="link-action">
              + Nueva cita
            </RouterLink>
          </template>

          <AppEmptyState
            v-if="!proximasCitas.length"
            :icon="CalendarX"
            title="No tienes citas programadas"
            description="Agenda tu primera cita para el cuidado de tu mascota."
          >
            <template #action>
              <AppButton variant="primary" @click="router.push('/cliente/solicitar-cita')">
                <template #icon-left><Plus :size="16" /></template>
                Agendar cita
              </AppButton>
            </template>
          </AppEmptyState>

          <div v-else class="citas-lista">
            <article
              v-for="cita in proximasCitas"
              :key="cita.id"
              class="cita-item"
            >
              <div class="cita-fecha">
                <span class="fecha-dia">{{ cita.dia }}</span>
                <span class="fecha-mes">{{ cita.mes }}</span>
              </div>

              <div class="cita-info">
                <p class="cita-motivo">{{ cita.motivo }}</p>
                <p class="cita-mascota"><PawPrint :size="12" /> {{ cita.mascota }}</p>
                <p class="cita-hora"><Clock :size="12" /> {{ cita.hora }}</p>
              </div>

              <div class="cita-accion">
                <span class="status-pill" :class="cita.estado">
                  {{ cita.estadoTexto }}
                </span>
              </div>
            </article>
          </div>
        </AppCard>

        <!-- Mis mascotas -->
        <AppCard
          title="Mis mascotas"
          :subtitle="misMascotas.length
            ? `${misMascotas.length} registrada${misMascotas.length === 1 ? '' : 's'}`
            : 'Sin mascotas registradas'"
        >
          <template #header-actions>
            <RouterLink to="/cliente/mascotas" class="link-action">
              Ver todas
            </RouterLink>
          </template>

          <AppEmptyState
            v-if="!misMascotas.length"
            :icon="PawPrint"
            title="Aún no has registrado mascotas"
            description="Registra tu primera mascota para gestionar su historial."
          >
            <template #action>
              <AppButton variant="primary" @click="router.push('/cliente/mascotas')">
                <template #icon-left><Plus :size="16" /></template>
                Registrar mascota
              </AppButton>
            </template>
          </AppEmptyState>

          <div v-else class="pet-list">
            <button
              v-for="mascota in misMascotas"
              :key="mascota.idMascota"
              type="button"
              class="pet-card"
              @click="verHistorial(mascota.idMascota)"
            >
              <PetAvatar :nombre-especie="mascota.especie" size="md" />
              <div class="pet-info">
                <p class="pet-nombre">{{ mascota.nombre }}</p>
                <p class="pet-meta">{{ mascota.especie }} · {{ mascota.edad }}</p>
              </div>
              <span class="pet-cta" aria-hidden="true">
                <FileText :size="15" />
              </span>
            </button>
          </div>
        </AppCard>
      </section>

      <!-- ═══ PAGOS PENDIENTES (banner) ═══ -->
      <section v-if="pagosPendientes.length > 0" class="pagos-banner">
        <div class="pagos-banner-icon">
          <CreditCard :size="22" />
        </div>
        <div class="pagos-banner-texto">
          <p class="pagos-banner-titulo">
            Tienes {{ pagosPendientes.length }}
            {{ pagosPendientes.length === 1 ? 'cita pendiente' : 'citas pendientes' }} de pago
          </p>
          <p class="pagos-banner-sub">
            Total a pagar: <strong>{{ fmtUsd(totalPendienteUsd) }} USD</strong>
            <template v-if="totalPendienteBs > 0">
              · <strong>Bs. {{ fmtBs(totalPendienteBs) }}</strong>
            </template>
          </p>
        </div>
        <AppButton variant="primary" @click="router.push('/cliente/mis-citas')">
          Ver y pagar
          <template #icon-right><ArrowRight :size="15" /></template>
        </AppButton>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight, Calendar, CalendarCheck, CalendarX, Clock,
  CreditCard, FileText, PawPrint, Plus, Stethoscope,
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth.store'
import { useToast } from '@/composables/useToast'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import PetAvatar from '@/components/ui/PetAvatar.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import { getMisCitas } from '@/api/citas.api'
import { getMisMascotas, getEspecies } from '@/api/mascotas.api'
import { getHistorialMascota } from '@/api/atenciones.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { fechaCompleta, hoyISO, horaCorta, partesBadgeFecha } from '@/utils/fecha'

const router = useRouter()
const authStore = useAuthStore()
const { toastError } = useToast()

const nombreCliente = computed(() => authStore.userName || 'Cliente')
const fechaLarga = computed(() => fechaCompleta(hoyISO()))

const cargando = ref(true)
const misMascotas = ref([])
const proximasCitas = ref([])
const pagosPendientes = ref([])
const stats = ref({
  citasPendientes: 0,
  mascotas: 0,
  pagosPendientes: 0,
  consultasEsteMes: '…',
})

const ESTADOS_ACTIVOS = ['Pendiente_Pago', 'Confirmada']
const ESTADO_PENDIENTE = 'Pendiente_Pago'
const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

function fmtUsd(v) { return `$ ${Number(v || 0).toFixed(2)}` }
function fmtBs(v) {
  return Number(v || 0).toLocaleString('es-VE', {
    minimumFractionDigits: 2, maximumFractionDigits: 2,
  })
}
function formatearFechaCorta(iso) {
  if (!iso) return ''
  const [y, m, d] = String(iso).slice(0, 10).split('-')
  return `${d} ${MESES[Number(m) - 1]} ${y}`
}
function edadMascota(iso) {
  if (!iso) return 'Sin datos'
  const nac = new Date(iso)
  if (Number.isNaN(nac.getTime())) return 'Sin datos'
  const hoy = new Date()
  let anios = hoy.getFullYear() - nac.getFullYear()
  const m = hoy.getMonth() - nac.getMonth()
  if (m < 0 || (m === 0 && hoy.getDate() < nac.getDate())) anios--
  if (anios < 1) {
    const meses = Math.max(
      0,
      (hoy.getFullYear() - nac.getFullYear()) * 12 + (hoy.getMonth() - nac.getMonth())
    )
    if (meses === 0) return 'Recién nacido'
    return `${meses} ${meses === 1 ? 'mes' : 'meses'}`
  }
  return `${anios} ${anios === 1 ? 'año' : 'años'}`
}

function estadoClase(estado) {
  if (estado === 'Pendiente_Pago') return 'pending'
  if (estado === 'Confirmada') return 'confirmed'
  if (estado === 'Cancelada') return 'cancelled'
  return 'other'
}
function estadoTexto(estado) {
  return String(estado || '').replaceAll('_', ' ')
}

function minutosHasta(horaInicio, fechaCita) {
  if (!horaInicio || !fechaCita) return null
  const [y, m, d] = String(fechaCita).slice(0, 10).split('-').map(Number)
  const [h, mi] = String(horaInicio).split(':').map(Number)
  const objetivo = new Date(y, (m || 1) - 1, d || 1, h || 0, mi || 0)
  return Math.round((objetivo - new Date()) / 60000)
}
function textoCuentaRegresiva(cita) {
  if (!cita) return ''
  if (cita.estado === 'pending') return 'Pago pendiente'
  const diff = minutosHasta(cita.horaInicio, cita.fechaCita)
  if (diff === null) return 'Próxima cita'
  if (diff < -5) return 'En curso'
  if (diff <= 5) return 'Ahora mismo'
  if (diff < 60) return `En ${diff} min`
  const h = Math.floor(diff / 60)
  const d = Math.floor(h / 24)
  if (d >= 1) return `En ${d} día${d === 1 ? '' : 's'}`
  const mm = diff % 60
  return mm === 0 ? `En ${h}h` : `En ${h}h ${mm}m`
}

const citaDestacada = computed(() => {
  const activas = proximasCitas.value
  return activas.find((c) => c.estado === 'confirmed')
      || activas.find((c) => c.estado === 'in-progress')
      || activas.find((c) => c.estado === 'verifying')
      || activas.find((c) => c.estado === 'pending')
      || null
})

const totalPendienteUsd = computed(() =>
  pagosPendientes.value.reduce((s, p) => s + Number(p.costoUsd || 0), 0)
)
const totalPendienteBs = computed(() =>
  pagosPendientes.value.reduce((s, p) => s + Number(p.costoBs || 0), 0)
)

async function cargarDatos() {
  cargando.value = true
  try {
    const [mascotasRes, citasRes, especiesRes] = await Promise.allSettled([
      getMisMascotas(),
      getMisCitas(),
      getEspecies(),
    ])

    const mapaEspecies = {}
    if (especiesRes.status === 'fulfilled') {
      for (const e of especiesRes.value.data) mapaEspecies[e.id] = e.nombre
    }

    if (mascotasRes.status === 'fulfilled') {
      misMascotas.value = mascotasRes.value.data.map((m) => ({
        idMascota: m.idMascota,
        nombre: m.nombre,
        especie: m.nombreEspecie || mapaEspecies[m.idEspecie] || 'Mascota',
        edad: edadMascota(m.fechaNacimiento),
        fechaNacimiento: m.fechaNacimiento,
        pesoActual: m.pesoActual,
      }))
    }

    const todasLasCitas = citasRes.status === 'fulfilled' ? citasRes.value.data : []
    const hoyStr = hoyISO()

    const pendientesPago = todasLasCitas.filter(
      (c) => c.estado === ESTADO_PENDIENTE && c.estadoPago !== 'Pendiente_Verificacion'
    )
    pagosPendientes.value = pendientesPago.map((c) => ({
      id: c.idCita,
      idCita: c.idCita,
      servicio: c.nombreServicio,
      mascota: c.nombreMascota,
      fecha: formatearFechaCorta(c.fechaCita),
      monto: fmtUsd(c.costoUsd),
      costoUsd: c.costoUsd,
      costoBs: c.costoBs,
    }))

    proximasCitas.value = todasLasCitas
      .filter((c) => ESTADOS_ACTIVOS.includes(c.estado) && c.fechaCita >= hoyStr)
      .sort((a, b) => {
        if (a.fechaCita !== b.fechaCita) return a.fechaCita.localeCompare(b.fechaCita)
        return (a.horaInicio || '').localeCompare(b.horaInicio || '')
      })
      .slice(0, 4)
      .map((c) => {
        const badge = partesBadgeFecha(c.fechaCita)

        // ── Determinar el estado REAL considerando estadoPago ──
        let estadoClase, estadoTexto
        if (c.estado === 'Pendiente_Pago') {
          if (c.estadoPago === 'Pendiente_Verificacion') {
            estadoClase = 'verifying'
            estadoTexto = 'Verificando pago'
          } else if (c.estadoPago === 'Rechazado') {
            estadoClase = 'rejected'
            estadoTexto = 'Pago rechazado'
          } else {
            estadoClase = 'pending'
            estadoTexto = 'Pendiente de pago'
          }
        } else if (c.estado === 'Confirmada') {
          estadoClase = 'confirmed'
          estadoTexto = 'Confirmada'
        } else if (c.estado === 'En_Atencion') {
          estadoClase = 'in-progress'
          estadoTexto = 'En atención'
        } else {
          estadoClase = 'other'
          estadoTexto = String(c.estado || '').replaceAll('_', ' ')
        }

        return {
          id: c.idCita,
          idCita: c.idCita,
          fechaCita: c.fechaCita,
          horaInicio: c.horaInicio,
          dia: badge.dia,
          mes: badge.mes,
          motivo: c.nombreServicio,
          mascota: c.nombreMascota,
          hora: horaCorta(c.horaInicio),
          estado: estadoClase,
          estadoTexto,
        }
      })

    stats.value.mascotas = misMascotas.value.length
    stats.value.pagosPendientes = pendientesPago.length
    stats.value.citasPendientes = todasLasCitas.filter(
      (c) => ESTADOS_ACTIVOS.includes(c.estado) && c.fechaCita >= hoyStr
    ).length
  } catch (error) {
    toastError(getApiErrorMessage(error))
  } finally {
    cargando.value = false
  }

  calcularConsultasMes()
}

async function calcularConsultasMes() {
  if (!misMascotas.value.length) {
    stats.value.consultasEsteMes = 0
    return
  }
  const ahora = new Date()
  const anioActual = ahora.getFullYear()
  const mesActual = ahora.getMonth() + 1

  const resultados = await Promise.allSettled(
    misMascotas.value.map((m) => getHistorialMascota(m.idMascota))
  )

  let total = 0
  for (const r of resultados) {
    if (r.status !== 'fulfilled') continue
    for (const atencion of r.value) {
      const fecha = String(atencion.fechaHoraInicio || '').slice(0, 10)
      const [y, m] = fecha.split('-').map(Number)
      if (y === anioActual && m === mesActual) total++
    }
  }
  stats.value.consultasEsteMes = total
}

function verHistorial(mascotaId) {
  if (!mascotaId) return
  router.push(`/cliente/historial-clinico?mascota=${mascotaId}`)
}

onMounted(cargarDatos)
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-7) var(--space-6) var(--space-12);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* ═══ HERO ═══ */
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

/* ═══ KPIs ═══ */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}
.stat-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  box-shadow: var(--shadow-xs);
  transition: border-color var(--duration-base) var(--ease-out),
              box-shadow var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
}
.stat-card:hover {
  border-color: var(--border-strong);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-icon--brand   { background: var(--brand-50);   color: var(--brand-700); }
.stat-icon--info    { background: var(--info-50);    color: var(--info-600); }
.stat-icon--warning { background: var(--warning-50); color: var(--warning-600); }
.stat-icon--purple  { background: var(--purple-50);  color: var(--purple-600); }

.stat-texto { min-width: 0; }
.stat-value {
  margin: 0;
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.1;
  letter-spacing: var(--tracking-tight);
}
.stat-label {
  margin: var(--space-1) 0 0;
  font-size: var(--text-md);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}

/* ═══ PRÓXIMA CITA ═══ */
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
  color: var(--text-inverse);
}
.proximo-nombre {
  margin: 0 0 var(--space-1);
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  letter-spacing: var(--tracking-tight);
  line-height: var(--leading-tight);
  color: var(--text-inverse);
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
.proximo-estado.pending   { background: rgba(251, 191, 36, 0.25); border-color: rgba(251, 191, 36, 0.55); color: #FEF3C7; }
.proximo-estado.confirmed { background: rgba(34, 197, 94, 0.25);  border-color: rgba(34, 197, 94, 0.50);  color: #DCFCE7; }
.proximo-accion { position: relative; z-index: 1; }

/* ═══ GRID CONTENIDO ═══ */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-5);
  align-items: start;
}

.link-action {
  color: var(--brand-700);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  white-space: nowrap;
}
.link-action:hover { color: var(--brand-800); text-decoration: underline; }

/* ═══ LISTA DE CITAS ═══ */
.citas-lista { display: flex; flex-direction: column; gap: var(--space-3); }
.cita-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  transition: border-color var(--duration-base) var(--ease-out),
              background-color var(--duration-base) var(--ease-out);
}
.cita-item:hover { border-color: var(--border-strong); background: var(--bg-surface); }
.cita-fecha {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 56px;
  padding: var(--space-2) var(--space-1);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}
.fecha-dia {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  line-height: 1;
  letter-spacing: var(--tracking-tight);
}
.fecha-mes {
  margin-top: 3px;
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  color: var(--text-secondary);
  letter-spacing: 0.05em;
  line-height: 1;
}
.cita-info { min-width: 0; }
.cita-motivo {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cita-mascota,
.cita-hora {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin: var(--space-1) var(--space-3) 0 0;
  font-size: var(--text-md);
  color: var(--text-secondary);
}
.cita-hora { margin-right: 0; }
.cita-accion { flex-shrink: 0; }

.status-pill {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  border: 1px solid;
  white-space: nowrap;
  letter-spacing: 0.02em;
}
.status-pill.confirmed { background: var(--success-50); color: var(--success-700); border-color: var(--success-200); }
.status-pill.pending   { background: var(--warning-50); color: var(--warning-700); border-color: var(--warning-200); }
.status-pill.cancelled { background: var(--danger-50);  color: var(--danger-700);  border-color: var(--danger-200); }
.status-pill.other     { background: var(--neutral-100); color: var(--neutral-600); border-color: var(--border-subtle); }

.status-pill.verifying {
  background: var(--info-50);
  color: var(--info-700);
  border-color: var(--info-200);
}
.status-pill.in-progress {
  background: var(--warning-50);
  color: var(--warning-700);
  border-color: var(--warning-200);
}
.status-pill.rejected {
  background: var(--danger-50);
  color: var(--danger-700);
  border-color: var(--danger-200);
}

/* ═══ MASCOTAS ═══ */
.pet-list { display: flex; flex-direction: column; gap: var(--space-2); }
.pet-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  overflow: hidden;
  transition: all var(--duration-base) var(--ease-out);
}
.pet-card:hover {
  background: var(--bg-surface);
  border-color: var(--brand-200);
  transform: translateX(2px);
  box-shadow: 0 6px 16px -8px rgba(15, 118, 110, 0.20);
}
.pet-info { flex: 1; min-width: 0; }
.pet-nombre {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pet-meta {
  margin: 2px 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pet-cta {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
  background: var(--neutral-100);
  flex-shrink: 0;
  transition: all var(--duration-fast) var(--ease-out);
}
.pet-card:hover .pet-cta {
  background: var(--brand-700);
  color: var(--text-inverse);
  transform: translateX(2px);
}

/* ═══ BANNER PAGOS ═══ */
.pagos-banner {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-4) var(--space-5);
  background: linear-gradient(135deg, var(--warning-50) 0%, var(--bg-surface) 60%);
  border: 1px solid var(--warning-200);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
}
.pagos-banner-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-xl);
  background: var(--warning-100);
  color: var(--warning-700);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.pagos-banner-texto { min-width: 0; }
.pagos-banner-titulo {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: var(--leading-snug);
}
.pagos-banner-sub {
  margin: var(--space-1) 0 0;
  font-size: var(--text-md);
  color: var(--text-secondary);
}
.pagos-banner-sub strong { color: var(--warning-700); font-weight: var(--font-bold); }

/* ═══ LOADING ═══ */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12) var(--space-6);
  color: var(--text-secondary);
  font-size: var(--text-base);
}

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1100px) {
  .content-grid { grid-template-columns: 1fr; }
}
@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .proximo { grid-template-columns: 1fr; gap: var(--space-4); }
  .proximo-accion { width: 100%; }
}
@media (max-width: 640px) {
  .dashboard { padding: var(--space-4); gap: var(--space-4); }
  .stats-grid { grid-template-columns: 1fr; }
  .proximo { padding: var(--space-5); border-radius: var(--radius-2xl); }
  .proximo-nombre { font-size: var(--text-3xl); }
  .cita-item { grid-template-columns: auto 1fr; gap: var(--space-3); }
  .cita-accion { grid-column: 2; justify-self: start; }

  .pagos-banner { grid-template-columns: auto 1fr; gap: var(--space-3); }
  .pagos-banner :deep(.btn) { grid-column: 1 / -1; }
}
</style>