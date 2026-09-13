<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHistorialPagos, descargarFactura } from '@/api/pagos.api.js'
import {
  ArrowLeft, Download, ReceiptText, AlertTriangle, AlertCircle,
  Loader2, X, Calendar, Wallet, Hash, Banknote, CheckCircle2
} from 'lucide-vue-next'

const router = useRouter()

// ─── ESTADO ───
const cargando = ref(false)
const pagos = ref([])
const errorCarga = ref('')

const pagoSeleccionado = ref(null)
const descargando = ref(false)
const descargaFallida = ref(false)

const toast = ref({ visible: false, message: '', type: 'success' })

// ─── CARGA ───
async function cargarHistorial() {
  cargando.value = true
  errorCarga.value = ''
  try {
    const { data } = await getHistorialPagos()
    // Defensivo: más reciente primero (el backend ya lo ordena)
    pagos.value = [...data].sort((a, b) => (b.fecha || '').localeCompare(a.fecha || ''))
  } catch (err) {
    errorCarga.value = 'No se pudo cargar el historial de pagos. Intenta nuevamente.'
  } finally {
    cargando.value = false
  }
}

// ─── HELPERS ───
const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

function formatearFechaHora(iso) {
  if (!iso) return '-'
  const [fecha, hora] = iso.split('T')
  const [y, m, d] = fecha.split('-')
  const base = `${d} ${MESES[Number(m) - 1]} ${y}`
  return hora ? `${base}, ${hora.slice(0, 5)}` : base
}

function fmtUsd(v) {
  return `$ ${Number(v).toFixed(2)}`
}

function badgeClass(estado) {
  switch (estado) {
    case 'Verificado': return 'verificado'
    case 'Pendiente_Verificacion': return 'pendiente'
    case 'Rechazado': return 'rechazado'
    default: return 'otro'
  }
}

function showToast(message, type = 'success') {
  toast.value = { visible: true, message, type }
  setTimeout(() => { toast.value.visible = false }, 4000)
}

// ─── MODAL ───
function verDetalle(pago) {
  pagoSeleccionado.value = pago
  descargaFallida.value = false
}

function cerrarModal() {
  if (descargando.value) return
  pagoSeleccionado.value = null
}

async function descargar() {
  if (!pagoSeleccionado.value) return
  descargando.value = true
  descargaFallida.value = false
  try {
    const res = await descargarFactura(pagoSeleccionado.value.idFactura)
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.download = `Factura-${pagoSeleccionado.value.numeroControl}.pdf`
    link.click()
    window.URL.revokeObjectURL(url)
    showToast('Factura descargada exitosamente')
  } catch (err) {
    // Flujo alterno: Documento PDF no disponible
    descargaFallida.value = true
  } finally {
    descargando.value = false
  }
}

onMounted(cargarHistorial)
</script>

<template>
  <div class="historial-view">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2>Historial de Pagos</h2>
        <p class="subtitle">Consulta tus transacciones y descarga tus facturas</p>
      </div>
      <button class="btn-secondary" @click="router.push('/cliente/dashboard')">
        <ArrowLeft :size="18" />
        Volver
      </button>
    </div>

    <!-- Loading -->
    <div v-if="cargando" class="loading-state">
      <Loader2 :size="32" class="spin" />
      <p>Cargando historial...</p>
    </div>

    <!-- Error de carga -->
    <div v-else-if="errorCarga" class="alert-error">
      <AlertCircle :size="16" />
      {{ errorCarga }}
    </div>

    <!-- Flujo alterno: historial vacío -->
    <div v-else-if="pagos.length === 0" class="empty-state">
      <ReceiptText :size="48" />
      <h3>Sin registros de pagos</h3>
      <p>Aún no posee registros de pagos o facturas asociadas a su cuenta.</p>
      <button class="btn-primary" @click="router.push('/cliente/dashboard')">
        Volver al panel principal
      </button>
    </div>

    <!-- Lista de transacciones -->
    <ul v-else class="pagos-lista">
      <li v-for="pago in pagos" :key="pago.idFactura">
        <div class="pago-card" @click="verDetalle(pago)">
          <div class="pago-icono">
            <ReceiptText :size="22" />
          </div>
          <div class="pago-info">
            <span class="pago-numero">{{ pago.numeroControl }}</span>
            <h4>{{ pago.concepto }}</h4>
            <p class="pago-fecha">
              <Calendar :size="14" />
              {{ formatearFechaHora(pago.fecha) }}
            </p>
          </div>
          <div class="pago-lateral">
            <strong class="pago-monto">{{ fmtUsd(pago.monto) }}</strong>
            <span class="badge" :class="badgeClass(pago.estadoPago)">
              {{ pago.estadoPago.replaceAll('_', ' ') }}
            </span>
          </div>
        </div>
      </li>
    </ul>

    <!-- Modal Detalle de la transacción -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="pagoSeleccionado" class="modal-overlay" @click.self="cerrarModal">
          <Transition name="slide-up">
            <div v-if="pagoSeleccionado" class="modal-container">
              <div class="modal-header">
                <h3>Detalle de la transacción</h3>
                <button class="btn-close" @click="cerrarModal">
                  <X :size="20" />
                </button>
              </div>

              <div class="modal-body">
                <div class="detalle-resumen">
                  <div class="detalle-fila">
                    <span class="detalle-label"><Hash :size="14" /> Comprobante</span>
                    <span class="detalle-valor mono">{{ pagoSeleccionado.numeroControl }}</span>
                  </div>
                  <div class="detalle-fila">
                    <span class="detalle-label"><Calendar :size="14" /> Fecha</span>
                    <span class="detalle-valor">{{ formatearFechaHora(pagoSeleccionado.fecha) }}</span>
                  </div>
                  <div class="detalle-fila">
                    <span class="detalle-label"><ReceiptText :size="14" /> Concepto</span>
                    <span class="detalle-valor">{{ pagoSeleccionado.concepto }}</span>
                  </div>
                  <div class="detalle-fila">
                    <span class="detalle-label"><Wallet :size="14" /> Método de pago</span>
                    <span class="detalle-valor">{{ pagoSeleccionado.metodoPago?.replaceAll('_', ' ') || '-' }}</span>
                  </div>
                  <div class="detalle-fila">
                    <span class="detalle-label"><Hash :size="14" /> Referencia</span>
                    <span class="detalle-valor mono">{{ pagoSeleccionado.referenciaTransaccion || '-' }}</span>
                  </div>
                  <div class="detalle-fila">
                    <span class="detalle-label"><AlertCircle :size="14" /> Estado</span>
                    <span class="badge" :class="badgeClass(pagoSeleccionado.estadoPago)">
                      {{ pagoSeleccionado.estadoPago.replaceAll('_', ' ') }}
                    </span>
                  </div>
                  <div class="detalle-fila total">
                    <span class="detalle-label"><Banknote :size="14" /> Monto total</span>
                    <span class="detalle-valor monto-total">{{ fmtUsd(pagoSeleccionado.monto) }}</span>
                  </div>
                </div>

                <!-- Flujo alterno: Documento PDF no disponible -->
                <div v-if="descargaFallida" class="alert-warn">
                  <AlertTriangle :size="16" />
                  El archivo digital del comprobante no está disponible en este momento. Por favor, intente más tarde o contacte a la clínica.
                </div>
              </div>

              <div class="modal-footer">
                <button class="btn-secondary" @click="cerrarModal">Cerrar</button>
                <button
                  class="btn-primary"
                  :disabled="descargando || descargaFallida"
                  @click="descargar"
                >
                  <Loader2 v-if="descargando" :size="18" class="spin" />
                  <Download v-else :size="18" />
                  {{ descargando ? 'Descargando...' : 'Descargar Factura' }}
                </button>
              </div>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <!-- Toast -->
    <Transition name="slide-down">
      <div v-if="toast.visible" class="toast" :class="toast.type">
        <CheckCircle2 v-if="toast.type === 'success'" :size="18" />
        <AlertCircle v-else :size="18" />
        {{ toast.message }}
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.historial-view {
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 24px;
}

/* Header */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  margin: 0;
}

.subtitle {
  font-size: 14px;
  color: #64748B;
  margin: 4px 0 0 0;
}

/* Botones */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #0F766E;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.btn-primary:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.25);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #F1F5F9;
  color: #475569;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.btn-secondary:hover:not(:disabled) {
  background: #E2E8F0;
}

/* Loading & Empty */
.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
  color: #94A3B8;
  gap: 16px;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03);
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.empty-state p {
  font-size: 14px;
  color: #64748B;
  margin: 0 0 8px 0;
  text-align: center;
  max-width: 400px;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Lista de pagos */
.pagos-lista {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pago-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.pago-card:hover {
  border-color: #0F766E;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 20px 25px -5px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.pago-icono {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F0FDFA;
  color: #0F766E;
  border: 1px solid #CCFBF1;
  flex-shrink: 0;
}

.pago-info {
  flex: 1;
  min-width: 0;
}

.pago-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  margin: 2px 0 6px 0;
}

.pago-numero {
  font-size: 12px;
  color: #94A3B8;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

.pago-fecha {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748B;
  margin: 0;
}

.pago-lateral {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.pago-monto {
  font-size: 16px;
  font-weight: 700;
  color: #1E293B;
}

/* Badges (estilo pill suave, como badge-especie) */
.badge {
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
  border: 1px solid transparent;
}

.badge.verificado {
  background: #ECFDF5;
  color: #059669;
  border-color: #A7F3D0;
}

.badge.pendiente {
  background: #FFFBEB;
  color: #D97706;
  border-color: #FDE68A;
}

.badge.rechazado {
  background: #FEF2F2;
  color: #DC2626;
  border-color: #FECACA;
}

.badge.otro {
  background: #F1F5F9;
  color: #64748B;
  border-color: #E2E8F0;
}

/* Alertas */
.alert-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #FEF2F2;
  color: #DC2626;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #FECACA;
}

.alert-warn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #FFFBEB;
  color: #D97706;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #FDE68A;
  margin-top: 16px;
  font-family: inherit;
}

/* ─── MODAL ─── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 100;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.modal-container {
  background: #ffffff;
  border-radius: 16px;
  width: 100%;
  max-width: 520px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  font-family: inherit;
}

.modal-container::-webkit-scrollbar {
  width: 8px;
}

.modal-container::-webkit-scrollbar-track {
  background: transparent;
}

.modal-container::-webkit-scrollbar-thumb {
  background: #CBD5E1;
  border-radius: 4px;
}

.modal-container::-webkit-scrollbar-thumb:hover {
  background: #94A3B8;
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.btn-close {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: #F1F5F9;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #E2E8F0;
  color: #1E293B;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding: 20px 24px 24px;
  border-top: 1px solid #E2E8F0;
}

/* Detalle de la transacción */
.detalle-resumen {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 8px 16px;
}

.detalle-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 11px 0;
}

.detalle-fila + .detalle-fila {
  border-top: 1px solid #E2E8F0;
}

.detalle-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #64748B;
}

.detalle-valor {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  text-align: right;
  word-break: break-word;
}

.detalle-valor.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-weight: 500;
}

.detalle-fila.total {
  border-top: 2px solid #E2E8F0;
}

.detalle-valor.monto-total {
  font-size: 16px;
  font-weight: 700;
  color: #0F766E;
}

/* Toast */
.toast {
  position: fixed;
  top: 24px;
  right: 24px;
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  z-index: 200;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.toast.success {
  background: #ECFDF5;
  color: #059669;
  border: 1px solid #A7F3D0;
}

.toast.error {
  background: #FEF2F2;
  color: #DC2626;
  border: 1px solid #FECACA;
}

/* Transiciones */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* Responsive */
@media (max-width: 640px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .pago-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .pago-lateral {
    flex-direction: row;
    align-items: center;
    width: 100%;
    justify-content: space-between;
  }

  .modal-footer {
    flex-direction: column-reverse;
  }

  .modal-footer .btn-primary,
  .modal-footer .btn-secondary {
    width: 100%;
    justify-content: center;
  }
}
</style>