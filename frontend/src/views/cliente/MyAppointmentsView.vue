<template>
  <div class="mis-citas-view">
    <header class="page-header">
      <div>
        <h2>Mis Citas</h2>
        <p class="subtitle">Consulta y gestiona tus citas agendadas</p>
      </div>
      <button class="btn-primary" @click="$router.push('/cliente/solicitar-cita')">
        <CalendarPlus :size="16" /> Nueva Cita
      </button>
    </header>

    <!-- Filtros -->
    <div class="filtros">
      <button
        v-for="f in filtros"
        :key="f.value"
        class="filtro-pill"
        :class="{ active: filtroActivo === f.value }"
        @click="cambiarFiltro(f.value)"
      >
        {{ f.label }}
      </button>
    </div>

    <!-- Loading -->
    <div v-if="cargando" class="loading-state">
      <Loader2 :size="32" class="spin" />
      <p>Cargando citas...</p>
    </div>

    <!-- Empty -->
    <div v-else-if="citas.length === 0" class="empty-state">
      <CalendarX2 :size="48" />
      <h3>No tienes citas {{ filtroActivo !== 'todas' ? 'en este estado' : 'agendadas' }}</h3>
      <p>Agenda una cita para el cuidado de tu mascota.</p>
      <button class="btn-primary" @click="$router.push('/cliente/solicitar-cita')">
        <CalendarPlus :size="16" /> Solicitar Cita
      </button>
    </div>

    <!-- Lista -->
    <div v-else class="citas-grid">
      <article
        v-for="cita in citas"
        :key="cita.idCita"
        class="cita-card"
        :class="{ 'cita-pasiva': esPasiva(cita) }"
        :style="{ '--estado': cita.colorEstado || '#6C757D' }"
      >
        <!-- Cabecera: bloque de fecha + mascota + estado -->
        <header class="cita-header">
          <div class="fecha-bloque" :title="formatFecha(cita.fechaCita)">
            <span class="fb-mes">{{ getMes(cita.fechaCita) }}</span>
            <span class="fb-dia">{{ getDia(cita.fechaCita) }}</span>
            <span class="fb-semana">{{ getDiaSemana(cita.fechaCita) }}</span>
          </div>

          <div class="cita-principal">
            <span class="estado-badge">{{ formatEstadoCita(cita) }}</span>
            <h3 class="cita-mascota">
              <span class="mascota-avatar"><PawPrint :size="14" /></span>
              <span class="mascota-nombre">{{ cita.nombreMascota }}</span>
            </h3>
            <p class="cita-servicio">{{ cita.nombreServicio }}</p>
          </div>
        </header>

        <div class="cita-body">
          <!-- Metadata: veterinario + horario -->
          <div class="cita-meta">
            <span class="meta-chip">
              <Stethoscope :size="13" />
              <span>{{ cita.nombreVeterinario }}</span>
            </span>
            <span class="meta-chip">
              <Clock :size="13" />
              <span>{{ cita.horaFin ? `${cita.horaInicio} – ${cita.horaFin}` : cita.horaInicio }}</span>
            </span>
          </div>

          <!-- ═══ Zona de estado de pago: verificación o countdown ═══ -->
          <div
            v-if="esPendienteVerificacion(cita) || (esPendientePago(cita) && cita.expiraEn)"
            class="cita-estado-zona"
          >
            <!-- Caso 1: ya pagó online, esperando verificación -->
            <div v-if="esPendienteVerificacion(cita)" class="verificando-banner">
              <span class="vb-icono"><Clock :size="15" /></span>
              <div class="vb-texto">
                <strong>Pago en verificación</strong>
                <p>Estamos confirmando tu pago. Te avisaremos en cuanto sea aprobado.</p>
              </div>
            </div>

            <!-- Caso 2: pendiente de pago, aún sin pagar → countdown activo -->
            <CitaCountdown
              v-else
              :expira-en="cita.expiraEn"
              @expirado="onCitaExpirada(cita)"
            />
          </div>

          <!-- Costos -->
          <div class="cita-costos">
            <div class="costo">
              <span class="costo-label">Total USD</span>
              <span class="costo-num">${{ cita.costoUsd }}</span>
            </div>
            <span class="costo-divisor" aria-hidden="true"></span>
            <div class="costo">
              <span class="costo-label">Total Bs</span>
              <span class="costo-num">Bs. {{ formatBs(cita.costoBs) }}</span>
            </div>
          </div>
        </div>

        <footer v-if="tieneFooter(cita)" class="cita-footer">
          <button v-if="esPendientePago(cita)" class="btn-pagar" @click="irAPagar(cita)">
            <CreditCard :size="15" /> Pagar ahora
          </button>

          <!-- Ya pagó: estado informativo en lugar del botón -->
          <div v-else-if="esPendienteVerificacion(cita)" class="pay-espera">
            <span class="espera-pulse" aria-hidden="true"></span>
            Esperando verificación
          </div>

          <button v-if="esCancelable(cita)" class="btn-cancelar" @click="abrirConfirmarCancelar(cita)">
            <X :size="15" /> Cancelar
          </button>
          <span v-else-if="notaRecepcionVisible(cita)" class="cancel-info">
            <Info :size="13" /> Para cancelar, contacta a recepción
          </span>
        </footer>
      </article>
    </div>

    <!-- Modal cancelar -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="modalCancelar" class="modal-overlay" @click.self="cerrarModalCancelar">
          <div class="modal-confirm">
            <div class="confirm-icon"><AlertTriangle :size="22" /></div>
            <h3>¿Cancelar esta cita?</h3>
            <p>
              La cita de <strong>{{ citaACancelar?.nombreMascota }}</strong>
              el {{ formatFecha(citaACancelar?.fechaCita) }} a las
              {{ citaACancelar?.horaInicio }} será cancelada.
            </p>
            <p class="confirm-hint">Esta acción no se puede deshacer.</p>
            <div class="confirm-actions">
              <button class="btn-secondary" :disabled="cancelando" @click="cerrarModalCancelar">
                Volver
              </button>
              <button class="btn-danger" :disabled="cancelando" @click="confirmarCancelar">
                <Loader2 v-if="cancelando" :size="16" class="spin" />
                <X v-else :size="16" />
                {{ cancelando ? 'Cancelando...' : 'Sí, cancelar cita' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import {
  CalendarPlus, CalendarX2, Loader2,
  PawPrint, Stethoscope, Clock,
  CreditCard, X, AlertTriangle, Info,
} from 'lucide-vue-next'
import { getMisCitas, cancelarCita } from '@/api/citas.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'
import CitaCountdown from '@/components/cliente/CitaCountdown.vue'

const router = useRouter()
const { toastSuccess, toastError } = useToast()

const cargando = ref(true)
const citas = ref([])
const filtroActivo = ref('todas')

const modalCancelar = ref(false)
const citaACancelar = ref(null)
const cancelando = ref(false)

const filtros = [
  { label: 'Todas',       value: 'todas' },
  { label: 'Por pagar',   value: 'Pendiente_Pago' },
  { label: 'Confirmadas', value: 'Confirmada' },
  { label: 'Completadas', value: 'Completada' },
  { label: 'Canceladas',  value: 'Cancelada' },
]

let refreshId = null

onMounted(() => {
  cargar()
  // Auto-refresh cada 60s (por si el job canceló una cita o el estado cambió)
  refreshId = setInterval(cargarSilencioso, 60000)
})

onBeforeUnmount(() => {
  if (refreshId) clearInterval(refreshId)
})

async function cargar() {
  cargando.value = true
  try {
    const estado = filtroActivo.value === 'todas' ? null : filtroActivo.value
    const { data } = await getMisCitas(estado)
    citas.value = data || []
  } catch (err) {
    toastError(getApiErrorMessage(err) || 'Error al cargar las citas')
  } finally {
    cargando.value = false
  }
}

async function cargarSilencioso() {
  try {
    const estado = filtroActivo.value === 'todas' ? null : filtroActivo.value
    const { data } = await getMisCitas(estado)
    citas.value = data || []
  } catch {
    // Silencioso: no molestamos al usuario por errores de refresh automático
  }
}

function cambiarFiltro(v) {
  filtroActivo.value = v
  cargar()
}

// Cuando el countdown llega a 0, damos 3 segundos y recargamos
function onCitaExpirada(cita) {
  setTimeout(() => cargarSilencioso(), 3000)
}

// ─── Estados de pago ───
function esPendienteVerificacion(cita) {
  return cita.estado === 'Pendiente_Pago'
      && cita.estadoPago === 'Pendiente_Verificacion'
}

function esPendientePago(cita) {
  return cita.estado === 'Pendiente_Pago'
      && cita.estadoPago !== 'Pendiente_Verificacion'
}

function esCancelable(cita) {
  if (cita.estadoPago === 'Pendiente_Verificacion' || cita.estadoPago === 'Confirmado') {
    return false
  }
  return ['Pendiente_Pago', 'Confirmada'].includes(cita.estado)
}

// Citas históricas: se muestran atenuadas, sin interacciones destacadas
function esPasiva(cita) {
  return ['Completada', 'Cancelada'].includes(cita.estado)
}

// El footer solo se renderiza si hay botones o nota que mostrar
function tieneFooter(cita) {
  return esPendientePago(cita) || esCancelable(cita) || notaRecepcionVisible(cita)
}

function notaRecepcionVisible(cita) {
  return ['Pendiente_Verificacion', 'Confirmado'].includes(cita.estadoPago)
      && ['Pendiente_Pago', 'Confirmada'].includes(cita.estado)
}

function irAPagar(cita) {
  router.push({ name: 'PayAppointment', params: { idCita: cita.idCita } })
}

function abrirConfirmarCancelar(cita) {
  citaACancelar.value = cita
  modalCancelar.value = true
}

function cerrarModalCancelar() {
  if (cancelando.value) return
  modalCancelar.value = false
  citaACancelar.value = null
}

async function confirmarCancelar() {
  if (!citaACancelar.value) return
  cancelando.value = true
  try {
    await cancelarCita(citaACancelar.value.idCita)
    toastSuccess('Cita cancelada correctamente')
    modalCancelar.value = false
    citaACancelar.value = null
    await cargar()
  } catch (err) {
    toastError(getApiErrorMessage(err) || 'No se pudo cancelar la cita')
  } finally {
    cancelando.value = false
  }
}

// ─── Formatters ───
function formatEstadoCita(cita) {
  if (cita.estado === 'Pendiente_Pago' && cita.estadoPago === 'Pendiente_Verificacion') {
    return 'Verificando pago'
  }
  const map = {
    'Pendiente_Pago': 'Pendiente de Pago',
    'Confirmada': 'Confirmada',
    'En_Atencion': 'En Atención',
    'Completada': 'Completada',
    'Cancelada': 'Cancelada',
  }
  return map[cita.estado] || cita.estado
}

function formatFecha(iso) {
  if (!iso) return ''
  const d = new Date(iso + 'T00:00:00')
  return d.toLocaleDateString('es-VE', {
    weekday: 'short', day: '2-digit', month: 'short', year: 'numeric',
  })
}

// Partes de fecha para el bloque visual tipo calendario
function getDia(iso) {
  if (!iso) return '--'
  return String(new Date(iso + 'T00:00:00').getDate()).padStart(2, '0')
}

function getMes(iso) {
  if (!iso) return '---'
  return new Date(iso + 'T00:00:00')
    .toLocaleDateString('es-VE', { month: 'short' })
    .replace('.', '')
    .toUpperCase()
}

function getDiaSemana(iso) {
  if (!iso) return ''
  return new Date(iso + 'T00:00:00')
    .toLocaleDateString('es-VE', { weekday: 'short' })
    .replace('.', '')
}

function formatBs(v) {
  if (v == null) return '0,00'
  return Number(v).toLocaleString('es-VE', {
    minimumFractionDigits: 2, maximumFractionDigits: 2,
  })
}
</script>

<style scoped>
.mis-citas-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.page-header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px; gap: 16px; flex-wrap: wrap;
}
.page-header h2 { margin: 0; font-size: 24px; font-weight: 700; color: #1E293B; }
.subtitle { margin: 4px 0 0; font-size: 14px; color: #64748B; }

.btn-primary {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 10px 18px; background: #0F766E; color: #fff;
  border: none; border-radius: 10px; font-size: 13.5px;
  font-weight: 600; cursor: pointer; font-family: inherit;
  transition: background .15s;
}
.btn-primary:hover { background: #115E59; }

/* Filtros */
.filtros {
  display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 20px;
}
.filtro-pill {
  padding: 7px 14px; font-size: 13px; font-weight: 600;
  border-radius: 20px; border: 1px solid #E2E8F0; background: #fff;
  color: #64748B; cursor: pointer; transition: all .15s; font-family: inherit;
}
.filtro-pill:hover { border-color: #CBD5E1; color: #334155; }
.filtro-pill.active {
  background: #0F766E; border-color: #0F766E; color: #fff;
}

/* Estados */
.loading-state, .empty-state {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 80px 24px; gap: 14px;
  background: #fff; border-radius: 16px; border: 1px solid #E2E8F0;
  color: #94A3B8;
}
.empty-state h3 { font-size: 18px; font-weight: 600; color: #1E293B; margin: 0; }
.empty-state p { font-size: 14px; color: #64748B; margin: 0 0 8px; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══════════ Grid de citas ═══════════ */
.citas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 16px;
}

/* ═══════════ Card ═══════════ */
.cita-card {
  position: relative;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: border-color .2s ease, box-shadow .25s ease,
              transform .25s ease, opacity .25s ease;
}
.cita-card:hover {
  border-color: rgba(15, 118, 110, .35);
  box-shadow: 0 14px 30px -14px rgba(15, 23, 42, .18);
  transform: translateY(-2px);
}

/* Citas históricas: atenuadas y sin lift */
.cita-card.cita-pasiva { opacity: .72; }
.cita-card.cita-pasiva:hover {
  transform: none;
  box-shadow: none;
  border-color: #E2E8F0;
}

/* ── Cabecera: bloque fecha + info principal ── */
.cita-header {
  display: flex;
  gap: 14px;
  padding: 16px 18px 4px;
}

/* Bloque de fecha estilo calendario, con el color del estado */
.fecha-bloque {
  width: 56px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(15, 23, 42, .05);
}
.fb-mes {
  width: 100%;
  text-align: center;
  padding: 4px 0 3px;
  background: var(--estado);
  color: #fff;
  font-size: 10px;
  font-weight: 800;
  letter-spacing: 1px;
  text-transform: uppercase;
}
.fb-dia {
  font-size: 21px;
  font-weight: 800;
  color: #1E293B;
  line-height: 1.15;
  padding-top: 5px;
  letter-spacing: -.3px;
}
.fb-semana {
  font-size: 10px;
  font-weight: 600;
  color: #94A3B8;
  text-transform: capitalize;
  padding: 1px 0 7px;
}

.cita-principal {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 3px;
  padding-top: 2px;
}

/* Badge de estado con tinte dinámico del color del estado */
.estado-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: .3px;
  text-transform: uppercase;
  /* Fallback si el navegador no soporta color-mix */
  background: #F1F5F9;
  color: #475569;
  border: 1px solid #E2E8F0;
  /* Tintes según --estado */
  background: color-mix(in srgb, var(--estado) 10%, #fff);
  color: color-mix(in srgb, var(--estado) 75%, #0F172A);
  border-color: color-mix(in srgb, var(--estado) 25%, #fff);
}
.estado-badge::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--estado);
  flex-shrink: 0;
}

.cita-mascota {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 4px 0 0;
  max-width: 100%;
  font-size: 16px;
  font-weight: 700;
  color: #1E293B;
}
.mascota-avatar {
  width: 26px; height: 26px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #F0FDFA;
  color: #0F766E;
}
.mascota-nombre {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cita-servicio {
  margin: 0;
  max-width: 100%;
  font-size: 13px;
  font-weight: 500;
  color: #64748B;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ── Cuerpo ── */
.cita-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px 18px 16px;
}

/* Chips de metadata */
.cita-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
  padding: 5px 10px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  font-size: 11.5px;
  font-weight: 600;
  color: #475569;
}
.meta-chip svg { color: #94A3B8; flex-shrink: 0; }
.meta-chip span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Zona de estado de pago */
.cita-estado-zona { display: flex; flex-direction: column; }

.verificando-banner {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  padding: 11px 13px;
  background: #FFFBEB;
  border: 1px solid #FDE68A;
  border-radius: 11px;
}
.vb-icono {
  width: 28px; height: 28px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #FEF3C7;
  color: #D97706;
}
.vb-texto strong {
  display: block;
  margin-bottom: 2px;
  font-size: 12.5px;
  font-weight: 700;
  color: #92400E;
}
.vb-texto p {
  margin: 0;
  font-size: 12px;
  line-height: 1.45;
  color: #A16207;
}

/* Costos: caja anclada abajo para consistencia entre cards */
.cita-costos {
  margin-top: auto;
  display: flex;
  align-items: stretch;
  gap: 14px;
  padding: 10px 14px;
  background: #F8FAFC;
  border: 1px solid #F1F5F9;
  border-radius: 11px;
}
.costo {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.costo-label {
  font-size: 9.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .7px;
  color: #94A3B8;
}
.costo-num {
  font-size: 15px;
  font-weight: 800;
  color: #0F766E;
  letter-spacing: -.2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.costo-divisor { width: 1px; flex-shrink: 0; background: #E2E8F0; }

/* ── Footer de acciones ── */
.cita-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 13px 18px 15px;
  border-top: 1px solid #F1F5F9;
  background: #fff;
}
.btn-pagar {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 9px 16px;
  background: #0F766E;
  color: #fff;
  border: none;
  border-radius: 9px;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  box-shadow: 0 1px 2px rgba(15, 118, 110, .3);
  transition: background .15s, box-shadow .15s, transform .1s;
}
.btn-pagar:hover {
  background: #115E59;
  box-shadow: 0 4px 10px -2px rgba(15, 118, 110, .4);
}
.btn-pagar:active { transform: scale(.98); }
.btn-pagar:focus-visible { outline: 2px solid #0F766E; outline-offset: 2px; }

.btn-cancelar {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 13px;
  background: #fff;
  color: #DC2626;
  border: 1px solid #FECACA;
  border-radius: 9px;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  margin-left: auto;
  transition: background .15s, border-color .15s;
}
.btn-cancelar:hover { background: #FEF2F2; border-color: #FCA5A5; }
.btn-cancelar:focus-visible { outline: 2px solid #DC2626; outline-offset: 2px; }

/* Estado "esperando verificación" */
.pay-espera {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 9px 16px;
  background: #F8FAFC;
  border: 1px dashed #CBD5E1;
  border-radius: 9px;
  font-size: 12.5px;
  font-weight: 700;
  color: #64748B;
}
.espera-pulse {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: #D97706;
  animation: pulso 1.6s ease-in-out infinite;
}
@keyframes pulso {
  0%, 100% { box-shadow: 0 0 0 0 rgba(217, 119, 6, .35); opacity: 1; }
  50%      { box-shadow: 0 0 0 5px rgba(217, 119, 6, 0); opacity: .55; }
}

.cancel-info {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  font-weight: 500;
  color: #94A3B8;
}

/* Modal confirmar */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(15, 23, 42, .5);
  backdrop-filter: blur(4px); display: flex; align-items: center;
  justify-content: center; padding: 24px; z-index: 100;
}
.modal-confirm {
  background: #fff; border-radius: 18px; max-width: 440px;
  width: 100%; padding: 28px 26px 24px; text-align: center;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .25);
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}
.confirm-icon {
  width: 52px; height: 52px; border-radius: 14px;
  background: #FEF2F2; color: #DC2626; margin: 0 auto 14px;
  display: flex; align-items: center; justify-content: center;
}
.modal-confirm h3 { font-size: 17px; font-weight: 700; color: #1E293B; margin: 0 0 8px; }
.modal-confirm p { font-size: 13.5px; color: #64748B; margin: 0 0 6px; line-height: 1.55; }
.modal-confirm strong { color: #334155; }
.confirm-hint { font-size: 12px !important; color: #94A3B8 !important; margin-bottom: 20px !important; }
.confirm-actions { display: flex; gap: 10px; }
.confirm-actions button { flex: 1; justify-content: center; }
.btn-secondary {
  padding: 10px 20px; background: #F1F5F9; color: #475569;
  border: 1px solid #E2E8F0; border-radius: 10px; font-size: 14px;
  font-weight: 600; cursor: pointer; font-family: inherit;
}
.btn-secondary:hover:not(:disabled) { background: #E2E8F0; }
.btn-secondary:disabled { opacity: .55; cursor: not-allowed; }
.btn-danger {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 10px 20px; background: #DC2626; color: #fff;
  border: none; border-radius: 10px; font-size: 14px;
  font-weight: 600; cursor: pointer; font-family: inherit;
}
.btn-danger:hover:not(:disabled) { background: #B91C1C; }
.btn-danger:disabled { opacity: .55; cursor: not-allowed; }

.fade-enter-active, .fade-leave-active { transition: opacity .2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* Accesibilidad: respeta preferencias de movimiento reducido */
@media (prefers-reduced-motion: reduce) {
  .cita-card, .btn-pagar, .espera-pulse { transition: none; animation: none; }
}

@media (max-width: 640px) {
  .citas-grid { grid-template-columns: 1fr; }
  .page-header { flex-direction: column; align-items: stretch; }
}
</style>