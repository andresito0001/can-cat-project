<template>
  <div class="dashboard">
    <ToastContainer />

    <!-- ═══ HERO ═══ -->
    <header class="hero">
      <div class="hero-left">
        <p class="hero-date">{{ fechaLarga }}</p>
        <h1>Hola, {{ nombreCliente }}</h1>
        <p class="hero-sub">Gestiona tus citas, mascotas y pagos desde un solo lugar.</p>
      </div>
      <div class="hero-right">
        <span class="hero-badge">
          <PawPrint :size="14" />
          {{ stats.mascotas }} {{ stats.mascotas === 1 ? 'mascota' : 'mascotas' }} registradas
        </span>
      </div>
    </header>

    <!-- ═══ LOADING ═══ -->
    <div v-if="cargando" class="loading-state">
      <div class="spin"></div>
      <p>Cargando tu información…</p>
    </div>

    <template v-else>
      <!-- ═══ KPIs ═══ -->
      <section class="stats-grid">
        <article class="stat-card">
          <div class="stat-icon" style="--bg: #EFF6FF; --fg: #3B82F6;">
            <CalendarCheck :size="22" />
          </div>
          <div class="stat-texto">
            <p class="stat-value">{{ stats.citasPendientes }}</p>
            <p class="stat-label">Citas pendientes</p>
          </div>
        </article>
        <article class="stat-card">
          <div class="stat-icon" style="--bg: #F0FDFA; --fg: #0F766E;">
            <PawPrint :size="22" />
          </div>
          <div class="stat-texto">
            <p class="stat-value">{{ stats.mascotas }}</p>
            <p class="stat-label">Mis mascotas</p>
          </div>
        </article>
        <article class="stat-card">
          <div class="stat-icon" style="--bg: #FFFBEB; --fg: #F59E0B;">
            <CreditCard :size="22" />
          </div>
          <div class="stat-texto">
            <p class="stat-value">{{ stats.pagosPendientes }}</p>
            <p class="stat-label">Pagos pendientes</p>
          </div>
        </article>
        <article class="stat-card">
          <div class="stat-icon" style="--bg: #F5F3FF; --fg: #8B5CF6;">
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
            <PawPrint :size="13" class="inline-icon" /> {{ citaDestacada.mascota }}
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
          <button
            class="btn-accion-grande"
            type="button"
            @click="router.push('/cliente/mis-citas')"
          >
            Ver detalle
            <ArrowRight :size="16" />
          </button>
        </div>
      </section>

      <!-- ═══ GRID PRINCIPAL ═══ -->
      <section class="content-grid">
        <!-- Próximas citas -->
        <div class="card">
          <div class="card-header">
            <div>
              <h3>Próximas citas</h3>
              <p class="card-sub">
                {{ proximasCitas.length
                  ? `${proximasCitas.length} programada${proximasCitas.length === 1 ? '' : 's'}`
                  : 'Sin citas programadas' }}
              </p>
            </div>
            <router-link to="/cliente/solicitar-cita" class="btn-link">
              + Nueva cita
            </router-link>
          </div>
          <div class="card-body">
            <div v-if="proximasCitas.length === 0" class="empty-state">
              <CalendarX :size="40" />
              <p>No tienes citas programadas</p>
              <router-link to="/cliente/solicitar-cita" class="btn-link">
                Agendar una cita
              </router-link>
            </div>

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
                  <p class="cita-mascota">
                    <PawPrint :size="12" /> {{ cita.mascota }}
                  </p>
                  <p class="cita-hora">
                    <Clock :size="12" /> {{ cita.hora }}
                  </p>
                </div>

                <div class="cita-accion">
                  <span class="status-pill" :class="cita.estado">
                    {{ cita.estadoTexto }}
                  </span>
                </div>
              </article>
            </div>
          </div>
        </div>

        <!-- Mis mascotas -->
        <div class="card">
          <div class="card-header">
            <div>
              <h3>Mis mascotas</h3>
              <p class="card-sub">
                {{ misMascotas.length
                  ? `${misMascotas.length} registrada${misMascotas.length === 1 ? '' : 's'}`
                  : 'Sin mascotas registradas' }}
              </p>
            </div>
            <router-link to="/cliente/mascotas" class="btn-link">
              Ver todas
            </router-link>
          </div>
          <div class="card-body card-body-slim">
            <div v-if="misMascotas.length === 0" class="empty-state slim">
              <PawPrint :size="32" />
              <p>Aún no has registrado mascotas</p>
              <router-link to="/cliente/mascotas" class="btn-link">
                Registrar mascota
              </router-link>
            </div>

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
          </div>
        </div>
      </section>

            <!-- ═══ PAGOS PENDIENTES (banner compacto) ═══ -->
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
        <router-link to="/cliente/mis-citas" class="pagos-banner-btn">
          Ver y pagar <ArrowRight :size="15" />
        </router-link>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight, Calendar, CalendarCheck, CalendarX, CheckCircle, Clock,
  CreditCard, FileText, PawPrint, Stethoscope
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth.store'
import { useToast } from '@/composables/useToast'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import PetAvatar from '@/components/ui/PetAvatar.vue'
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

// ─── ESTADO ───
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

// ─── CONSTANTES ───
const ESTADOS_ACTIVOS = ['Pendiente_Pago', 'Confirmada']
const ESTADO_PENDIENTE = 'Pendiente_Pago'
const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

// ─── HELPERS ───
function fmtUsd(v) {
  return `$ ${Number(v || 0).toFixed(2)}`
}

function fmtBs(v) {
  return Number(v || 0).toLocaleString('es-VE', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
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

// Convierte un estado de la BD a la clase visual del pill
function estadoClase(estado) {
  if (estado === 'Pendiente_Pago') return 'pending'
  if (estado === 'Confirmada') return 'confirmed'
  if (estado === 'Cancelada') return 'cancelled'
  return 'other'
}
function estadoTexto(estado) {
  return String(estado || '').replaceAll('_', ' ')
}

// Calcula el "En X min" / "En curso" comparando con ahora
function minutosHasta(horaInicio, fechaCita) {
  if (!horaInicio || !fechaCita) return null
  const [y, m, d] = String(fechaCita).slice(0, 10).split('-').map(Number)
  const [h, mi] = String(horaInicio).split(':').map(Number)
  const objetivo = new Date(y, (m || 1) - 1, d || 1, h || 0, mi || 0)
  const ahora = new Date()
  return Math.round((objetivo - ahora) / 60000)
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

const citaDestacada = computed(() => proximasCitas.value[0] || null)

const totalPendienteUsd = computed(() =>
  pagosPendientes.value.reduce((s, p) => s + Number(p.costoUsd || 0), 0)
)

const totalPendienteBs = computed(() =>
  pagosPendientes.value.reduce((s, p) => s + Number(p.costoBs || 0), 0)
)

// ─── CARGA DE DATOS ───
async function cargarDatos() {
  cargando.value = true
  try {
    const [mascotasRes, citasRes, especiesRes] = await Promise.allSettled([
      getMisMascotas(),
      getMisCitas(),
      getEspecies(),
    ])

    // 1. Mapa de especies para el nombre
    const mapaEspecies = {}
    if (especiesRes.status === 'fulfilled') {
      for (const e of especiesRes.value.data) mapaEspecies[e.id] = e.nombre
    }

    // 2. Mascotas → adaptar al shape del template
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

    // 3. Citas
    const todasLasCitas = citasRes.status === 'fulfilled' ? citasRes.value.data : []
    const hoyStr = hoyISO()

    // 3a. Pagos pendientes = citas Pendiente_Pago
    const pendientesPago = todasLasCitas.filter(
      (c) => c.estado === ESTADO_PENDIENTE
          && c.estadoPago !== 'Pendiente_Verificacion'
    )
    pagosPendientes.value = pendientesPago.map((c) => ({
      id: c.idCita,
      idCita: c.idCita,
      servicio: c.nombreServicio,         // ← corregido
      mascota: c.nombreMascota,            // ← corregido
      fecha: formatearFechaCorta(c.fechaCita),
      monto: fmtUsd(c.costoUsd),
      costoUsd: c.costoUsd,
      costoBs: c.costoBs,
    }))

    // 3b. Próximas citas = activas + futuras, ordenadas
    proximasCitas.value = todasLasCitas
      .filter((c) => ESTADOS_ACTIVOS.includes(c.estado) && c.fechaCita >= hoyStr)
      .sort((a, b) => {
        if (a.fechaCita !== b.fechaCita) return a.fechaCita.localeCompare(b.fechaCita)
        return (a.horaInicio || '').localeCompare(b.horaInicio || '')
      })
      .slice(0, 4)
      .map((c) => {
        const badge = partesBadgeFecha(c.fechaCita)
        return {
          id: c.idCita,
          idCita: c.idCita,
          fechaCita: c.fechaCita,
          horaInicio: c.horaInicio,
          dia: badge.dia,
          mes: badge.mes,
          motivo: c.servicio,
          mascota: c.mascota,
          hora: horaCorta(c.horaInicio),
          estado: estadoClase(c.estado),
          estadoTexto: estadoTexto(c.estado),
        }
      })

    // 4. Stats inmediatos
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

  // 5. Consultas del mes (async, no bloquea el render)
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

// ─── ACCIONES ───
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
  padding: 28px 24px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
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
.hero-sub { margin: 0; font-size: 14px; color: #64748B; }
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

/* ═══ KPIs ═══ */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
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
.stat-card:hover {
  border-color: #CBD5E1;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -10px rgba(15, 23, 42, .08);
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--bg);
  color: var(--fg);
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
.stat-label {
  margin: 3px 0 0;
  font-size: 12.5px;
  color: #64748B;
  font-weight: 500;
}

/* ═══ PRÓXIMA CITA ═══ */
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
.proximo-estado.pending {
  background: rgba(251, 191, 36, .25);
  border-color: rgba(251, 191, 36, .55);
  color: #FEF3C7;
}
.proximo-estado.confirmed {
  background: rgba(34, 197, 94, .25);
  border-color: rgba(34, 197, 94, .5);
  color: #DCFCE7;
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

/* ═══ GRID CONTENIDO ═══ */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: start;
}

/* ═══ CARDS ═══ */
.card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  overflow: hidden;
}
.card.wide {
  grid-column: 1 / -1;
  margin-top: 20px;
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
.card-body-slim { padding: 14px 16px; }

/* ═══ LISTA DE CITAS ═══ */
.citas-lista { display: flex; flex-direction: column; gap: 10px; }
.cita-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  transition: border-color .2s ease, background-color .2s ease;
}
.cita-item:hover { border-color: #CBD5E1; background: #fff; }
.cita-fecha {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 56px;
  padding: 8px 6px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  flex-shrink: 0;
}
.fecha-dia {
  font-size: 20px;
  font-weight: 700;
  color: #0F766E;
  line-height: 1;
  letter-spacing: -0.01em;
}
.fecha-mes {
  margin-top: 3px;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  color: #64748B;
  letter-spacing: .5px;
  line-height: 1;
}
.cita-info { min-width: 0; }
.cita-motivo {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cita-mascota,
.cita-hora {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin: 4px 12px 0 0;
  font-size: 12.5px;
  color: #64748B;
}
.cita-hora { margin-right: 0; }
.cita-accion { flex-shrink: 0; }
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
.status-pill.confirmed {
  background: #ECFDF5;
  color: #059669;
  border-color: #A7F3D0;
}
.status-pill.pending {
  background: #FFFBEB;
  color: #D97706;
  border-color: #FDE68A;
}
.status-pill.cancelled {
  background: #FEF2F2;
  color: #DC2626;
  border-color: #FECACA;
}
.status-pill.other {
  background: #F1F5F9;
  color: #64748B;
  border-color: #E2E8F0;
}

/* ═══ MASCOTAS ═══ */
.pet-list { display: flex; flex-direction: column; gap: 8px; }
.pet-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  overflow: hidden;
  transition: all .2s ease;
}
.pet-card::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 3px;
  background: var(--pet-color, #0F766E);
  transform: scaleY(.35);
  opacity: 0;
  transition: opacity .25s ease, transform .25s ease;
}
.pet-card:hover {
  background: #fff;
  border-color: rgba(15, 118, 110, .35);
  transform: translateX(2px);
  box-shadow: 0 6px 16px -8px rgba(15, 118, 110, .2);
}
.pet-card:hover::before { opacity: 1; transform: scaleY(1); }
.pet-card:focus-visible {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .15);
}
.pet-info { flex: 1; min-width: 0; }
.pet-nombre {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pet-meta {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pet-cta {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94A3B8;
  background: #F1F5F9;
  flex-shrink: 0;
  transition: background-color .2s ease, color .2s ease, transform .2s ease;
}
.pet-card:hover .pet-cta {
  background: #0F766E;
  color: #fff;
  transform: translateX(2px);
}

/* ═══ BANNER PAGOS PENDIENTES ═══ */
.pagos-banner {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 16px;
  align-items: center;
  padding: 16px 20px;
  margin-top: 20px;
  background: linear-gradient(135deg, #FFFBEB 0%, #FFFFFF 60%);
  border: 1px solid #FDE68A;
  border-radius: 14px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03);
}

.pagos-banner-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #FEF3C7;
  color: #B45309;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.pagos-banner-texto { min-width: 0; }
.pagos-banner-titulo {
  margin: 0;
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.3;
}
.pagos-banner-sub {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748B;
}
.pagos-banner-sub strong {
  color: #B45309;
  font-weight: 700;
}

.pagos-banner-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 10px 20px;
  background: #B45309;
  color: #fff;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
  transition: all .15s ease;
  flex-shrink: 0;
}
.pagos-banner-btn:hover {
  background: #92400E;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(180, 83, 9, .4);
}

@media (max-width: 640px) {
  .pagos-banner {
    grid-template-columns: auto 1fr;
    gap: 12px;
  }
  .pagos-banner-btn {
    grid-column: 1 / -1;
    justify-content: center;
  }
}

/* ═══ LINKS / BADGES ═══ */
.btn-link {
  color: #0F766E;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  white-space: nowrap;
  transition: color .2s;
}
.btn-link:hover { color: #115E59; text-decoration: underline; }
.badge-alert {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  background: #FEF2F2;
  color: #EF4444;
  border: 1px solid #FECACA;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: .1px;
  white-space: nowrap;
}

/* ═══ EMPTY / LOADING ═══ */
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

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1100px) {
  .content-grid { grid-template-columns: 1fr; }
}
@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .proximo { grid-template-columns: 1fr; gap: 18px; }
  .proximo-accion { width: 100%; }
  .btn-accion-grande { width: 100%; justify-content: center; }
}
@media (max-width: 640px) {
  .dashboard { padding: 16px 16px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero-left h1 { font-size: 22px; }
  .stats-grid { grid-template-columns: 1fr; }
  .proximo { padding: 20px; border-radius: 14px; }
  .proximo-nombre { font-size: 20px; }
  .cita-item { grid-template-columns: auto 1fr; gap: 12px; }
  .cita-accion { grid-column: 2; justify-self: start; }
  .status-pill { margin-top: 4px; }
}
</style>