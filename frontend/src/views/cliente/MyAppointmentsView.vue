<template>
  <div class="mis-citas-view">
    <ToastContainer />

    <!-- ═══ HERO ═══ -->
    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <CalendarDays :size="12" /> Mis Citas
        </span>
        <h1>Mis Citas</h1>
        <p class="page-header-sub">
          Consulta y gestiona tus citas agendadas.
        </p>
      </div>
      <div class="page-header-actions">
        <AppButton variant="primary" @click="router.push('/cliente/solicitar-cita')">
          <template #icon-left><CalendarPlus :size="16" /></template>
          Nueva Cita
        </AppButton>
      </div>
    </header>

    <!-- ═══ FILTROS ═══ -->
    <nav class="filtros" aria-label="Filtrar citas por estado">
      <button
        v-for="f in filtros"
        :key="f.value"
        type="button"
        class="filtro-pill"
        :class="{ active: filtroActivo === f.value }"
        @click="cambiarFiltro(f.value)"
      >
        {{ f.label }}
      </button>
    </nav>

    <!-- ═══ LOADING ═══ -->
    <div v-if="cargando" class="card">
      <div class="card-body">
        <div class="loading-state">
          <span class="spinner spinner-lg" />
          <p>Cargando citas…</p>
        </div>
      </div>
    </div>

    <!-- ═══ EMPTY ═══ -->
    <AppEmptyState
      v-else-if="!citas.length"
      :icon="CalendarX2"
      :title="filtroActivo !== 'todas'
        ? 'Sin citas en este estado'
        : 'No tienes citas agendadas'"
      description="Agenda una cita para el cuidado de tu mascota."
    >
      <template #action>
        <AppButton variant="primary" @click="router.push('/cliente/solicitar-cita')">
          <template #icon-left><CalendarPlus :size="16" /></template>
          Solicitar Cita
        </AppButton>
      </template>
    </AppEmptyState>

    <!-- ═══ GRID DE CITAS ═══ -->
    <div v-else class="citas-grid">
      <article
        v-for="cita in citas"
        :key="cita.idCita"
        class="cita-card"
        :class="{ 'cita-pasiva': esPasiva(cita) }"
        :style="{ '--estado': cita.colorEstado || 'var(--neutral-500)' }"
      >
        <!-- Cabecera -->
        <header class="cita-header">
          <div class="fecha-bloque" :title="formatFecha(cita.fechaCita)">
            <span class="fb-mes">{{ getMes(cita.fechaCita) }}</span>
            <span class="fb-dia">{{ getDia(cita.fechaCita) }}</span>
            <span class="fb-semana">{{ getDiaSemana(cita.fechaCita) }}</span>
          </div>

          <div class="cita-principal">
            <span class="estado-badge">{{ formatEstadoCita(cita) }}</span>
            <h3 class="cita-mascota">
              <PetAvatar size="sm" />
              <span class="mascota-nombre">{{ cita.nombreMascota }}</span>
            </h3>
            <p class="cita-servicio">{{ cita.nombreServicio }}</p>
          </div>
        </header>

        <div class="cita-body">
          <!-- Metadata -->
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

          <!-- Zona de pago: verificación o countdown -->
          <div
            v-if="esPendienteVerificacion(cita) || (esPendientePago(cita) && cita.expiraEn)"
            class="cita-estado-zona"
          >
            <div v-if="esPendienteVerificacion(cita)" class="verificando-banner">
              <span class="vb-icono"><Clock :size="15" /></span>
              <div class="vb-texto">
                <strong>Pago en verificación</strong>
                <p>Estamos confirmando tu pago. Te avisaremos en cuanto sea aprobado.</p>
              </div>
            </div>

            <CitaCountdown
              v-else
              :expira-en="cita.expiraEn"
              @expirado="onCitaExpirada(cita)"
            />
          </div>

          <!-- Costos -->
          <div class="cita-costos">
            <div class="costo">
              <span class="costo-label">Subtotal</span>
              <span class="costo-num">${{ cita.subtotalUsd }}</span>
            </div>
            <span class="costo-divisor" aria-hidden="true" />
            <div class="costo">
              <span class="costo-label">IVA {{ cita.porcentajeIva }}%</span>
              <span class="costo-num">${{ cita.ivaUsd }}</span>
            </div>
            <span class="costo-divisor" aria-hidden="true" />
            <div class="costo">
              <span class="costo-label">Total</span>
              <span class="costo-num costo-total">${{ cita.costoUsd }}</span>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <footer v-if="tieneFooter(cita)" class="cita-footer">
          <AppButton
            v-if="esPendientePago(cita)"
            variant="primary"
            block
            @click="irAPagar(cita)"
          >
            <template #icon-left><CreditCard :size="15" /></template>
            Pagar ahora
          </AppButton>

          <div v-else-if="esPendienteVerificacion(cita)" class="pay-espera">
            <span class="espera-pulse" aria-hidden="true" />
            Esperando verificación
          </div>

          <AppButton
            v-if="esCancelable(cita)"
            variant="danger-soft"
            @click="abrirConfirmarCancelar(cita)"
          >
            <template #icon-left><X :size="15" /></template>
            Cancelar
          </AppButton>
          <span v-else-if="notaRecepcionVisible(cita)" class="cancel-info">
            <Info :size="13" /> Para cancelar, contacta a recepción
          </span>
        </footer>
      </article>
    </div>

    <!-- ═══ MODAL CANCELAR ═══ -->
    <AppModal
      :model-value="modalCancelar"
      title="¿Cancelar esta cita?"
      size="sm"
      :loading="cancelando"
      @update:model-value="cerrarModalCancelar"
    >
      <div class="cancel-content">
        <div class="confirm-icon"><AlertTriangle :size="22" /></div>
        <p class="cancel-texto">
          La cita de <strong>{{ citaACancelar?.nombreMascota }}</strong>
          el {{ formatFecha(citaACancelar?.fechaCita) }} a las
          {{ citaACancelar?.horaInicio }} será cancelada.
        </p>
        <p class="cancel-hint">Esta acción no se puede deshacer.</p>
      </div>

      <template #footer>
        <AppButton variant="secondary" :disabled="cancelando" @click="cerrarModalCancelar">
          Volver
        </AppButton>
        <AppButton variant="danger" :loading="cancelando" @click="confirmarCancelar">
          <template #icon-left><X :size="16" /></template>
          {{ cancelando ? 'Cancelando…' : 'Sí, cancelar cita' }}
        </AppButton>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import {
  CalendarPlus, CalendarX2, CalendarDays,
  Stethoscope, Clock,
  CreditCard, X, AlertTriangle, Info,
} from 'lucide-vue-next'
import { getMisCitas, cancelarCita } from '@/api/citas.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

import CitaCountdown from '@/components/cliente/CitaCountdown.vue'
import PetAvatar from '@/components/ui/PetAvatar.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppModal from '@/components/ui/AppModal.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import ToastContainer from '@/components/ui/ToastContainer.vue'

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
  } catch { /* silencioso */ }
}

function cambiarFiltro(v) {
  filtroActivo.value = v
  cargar()
}

function onCitaExpirada() {
  setTimeout(() => cargarSilencioso(), 3000)
}

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

function esPasiva(cita) {
  return ['Completada', 'Cancelada'].includes(cita.estado)
}

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

function formatEstadoCita(cita) {
  if (cita.estado === 'Pendiente_Pago' && cita.estadoPago === 'Pendiente_Verificacion') {
    return 'Verificando pago'
  }
  const map = {
    'Pendiente_Pago': 'Pendiente de Pago',
    'Confirmada':     'Confirmada',
    'En_Atencion':    'En Atención',
    'Completada':     'Completada',
    'Cancelada':      'Cancelada',
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
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* ═══ FILTROS ═══ */
.filtros {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}
.filtro-pill {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--text-secondary);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--duration-fast) var(--ease-out);
}
.filtro-pill:hover { border-color: var(--border-strong); color: var(--neutral-700); }
.filtro-pill.active {
  background: var(--brand-700);
  border-color: var(--brand-700);
  color: var(--text-inverse);
  box-shadow: 0 2px 8px rgba(15, 118, 110, 0.25);
}

/* ═══ GRID ═══ */
.citas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: var(--space-4);
}

/* ═══ CARD ═══ */
.cita-card {
  position: relative;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-3xl);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-xs);
  transition: border-color var(--duration-base) var(--ease-out),
              box-shadow var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out),
              opacity var(--duration-base) var(--ease-out);
}
.cita-card:hover {
  border-color: var(--brand-200);
  box-shadow: var(--shadow-lg);
  transform: translateY(-2px);
}
.cita-card.cita-pasiva { opacity: 0.72; }
.cita-card.cita-pasiva:hover {
  transform: none;
  box-shadow: var(--shadow-xs);
  border-color: var(--border-subtle);
}

/* ── Cabecera ── */
.cita-header {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5) var(--space-1);
}
.fecha-bloque {
  width: 56px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-xs);
}
.fb-mes {
  width: 100%;
  text-align: center;
  padding: var(--space-1) 0;
  background: var(--estado);
  color: var(--text-inverse);
  font-size: var(--text-2xs);
  font-weight: var(--font-extrabold);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}
.fb-dia {
  font-size: var(--text-4xl);
  font-weight: var(--font-extrabold);
  color: var(--text-primary);
  line-height: 1.15;
  padding-top: var(--space-1);
  letter-spacing: -0.02em;
}
.fb-semana {
  font-size: var(--text-2xs);
  font-weight: var(--font-semibold);
  color: var(--text-tertiary);
  text-transform: capitalize;
  padding: 1px 0 var(--space-2);
}

.cita-principal {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-1);
  padding-top: 2px;
}

.estado-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.03em;
  text-transform: uppercase;
  background: #F1F5F9;
  color: var(--neutral-600);
  border: 1px solid var(--border-subtle);
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
  gap: var(--space-2);
  margin: var(--space-1) 0 0;
  max-width: 100%;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
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
  font-size: var(--text-md);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ── Cuerpo ── */
.cita-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-5) var(--space-4);
}

.cita-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  max-width: 100%;
  padding: var(--space-1) var(--space-3);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--neutral-600);
}
.meta-chip svg { color: var(--text-tertiary); flex-shrink: 0; }
.meta-chip span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cita-estado-zona { display: flex; flex-direction: column; }

.verificando-banner {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3);
  background: var(--warning-50);
  border: 1px solid var(--warning-200);
  border-radius: var(--radius-lg);
}
.vb-icono {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  background: var(--warning-100);
  color: var(--warning-600);
}
.vb-texto strong {
  display: block;
  margin-bottom: 2px;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--warning-700);
}
.vb-texto p {
  margin: 0;
  font-size: var(--text-sm);
  line-height: var(--leading-snug);
  color: var(--warning-700);
}

.cita-costos {
  margin-top: auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-3);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}

/* Reemplaza los .costo en fila por filas apiladas */
.cita-costos .costo {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: var(--space-3);
}
.cita-costos .costo-label {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-tertiary);
}
.cita-costos .costo-num {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--neutral-700);
  font-variant-numeric: tabular-nums;
}

.cita-costos .costo.costo-total {
  padding-top: var(--space-2);
  border-top: 1px solid var(--border-subtle);
}

.cita-costos .costo-total .costo-num {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  letter-spacing: -0.01em;
}

.costo {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.costo-label {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-tertiary);
}
.costo-num {
  font-size: var(--text-lg);
  font-weight: var(--font-extrabold);
  color: var(--brand-700);
  letter-spacing: -0.02em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.costo-divisor { width: 1px; flex-shrink: 0; background: var(--border-subtle); }

/* ── Footer ── */
.cita-footer {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5) var(--space-4);
  border-top: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  flex-wrap: wrap;
}
.cita-footer :deep(.btn) { flex: 1; min-width: 120px; }

.pay-espera {
  flex: 1;
  min-width: 120px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px dashed var(--neutral-300);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
}
.espera-pulse {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--warning-600);
  animation: pulso 1.6s ease-in-out infinite;
}
@keyframes pulso {
  0%, 100% { box-shadow: 0 0 0 0 rgba(217, 119, 6, 0.35); opacity: 1; }
  50%      { box-shadow: 0 0 0 5px rgba(217, 119, 6, 0);    opacity: 0.55; }
}

.cancel-info {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-tertiary);
}

/* ═══ MODAL CANCELAR ═══ */
.cancel-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
  padding-top: var(--space-2);
}
.confirm-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-2xl);
  background: var(--danger-50);
  color: var(--danger-600);
  display: flex;
  align-items: center;
  justify-content: center;
}
.cancel-texto {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0;
  line-height: var(--leading-normal);
  max-width: 340px;
}
.cancel-texto strong { color: var(--neutral-700); font-weight: var(--font-bold); }
.cancel-hint {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
}

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
@media (max-width: 640px) {
  .mis-citas-view { padding: var(--space-4); }
  .citas-grid { grid-template-columns: 1fr; }
  .cita-header { padding: var(--space-3) var(--space-4) var(--space-1); }
  .cita-body { padding: var(--space-3) var(--space-4) var(--space-4); }
  .cita-footer { padding: var(--space-3) var(--space-4); }
  .cita-footer :deep(.btn) { min-width: 100%; }
}
</style>