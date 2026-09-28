<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="modelValue" class="modal-overlay" @click.self="!procesando && cerrar()">
        <Transition name="slide-up" appear>
          <div v-if="modelValue" class="verif-modal">
            <header class="modal-head">
              <div class="modal-head-left">
                <div class="modal-icon" :class="{ 'modal-icon--warn': !aprobado }">
                  <component :is="aprobado ? ShieldCheck : XCircle" :size="18" />
                </div>
                <div>
                  <span class="modal-eyebrow">Verificación de pago</span>
                  <h3>{{ aprobado ? 'Confirmar pago' : 'Rechazar pago' }}</h3>
                </div>
              </div>
              <button class="modal-close" type="button" :disabled="procesando" @click="cerrar">
                <X :size="17" />
              </button>
            </header>

            <div class="modal-body">
              <!-- Resumen del pago -->
              <div class="info-card">
                <div class="info-row">
                  <span class="info-label"><User :size="13" /> Cliente</span>
                  <span class="info-value">{{ pago?.clienteNombre }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label"><CreditCard :size="13" /> Documento</span>
                  <span class="info-value mono">{{ pago?.clienteDocumento }}</span>
                </div>
                <div v-if="pago?.mascotaNombre" class="info-row">
                  <span class="info-label"><PawPrint :size="13" /> Mascota</span>
                  <span class="info-value">{{ pago.mascotaNombre }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label"><FileText :size="13" /> Factura</span>
                  <span class="info-value mono">{{ pago?.numeroControl }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label"><Wallet :size="13" /> Monto</span>
                  <span class="info-value amount">{{ fmtUsd(pago?.monto) }}</span>
                </div>
              </div>

              <!-- Datos de la transacción -->
              <div class="tx-card">
                <p class="tx-title"><Info :size="13" /> Datos de la transacción</p>
                <div class="tx-grid">
                  <div v-if="pago?.referenciaTransaccion" class="tx-field">
                    <span class="tx-label">Referencia</span>
                    <span class="tx-value mono">{{ pago.referenciaTransaccion }}</span>
                  </div>
                  <div v-for="(val, key) in (pago?.metadataPago || {})" :key="key" class="tx-field">
                    <span class="tx-label">{{ fmtFieldLabel(key) }}</span>
                    <span class="tx-value">{{ val }}</span>
                  </div>
                </div>
              </div>

              <!-- Aviso si es rechazo -->
              <div v-if="!aprobado" class="alert alert-warn">
                <AlertTriangle :size="16" />
                <span>
                  Al rechazar, el pago quedará como <strong>Rechazado</strong> y la cita se
                  <strong>cancelará automáticamente</strong>. El cliente deberá agendar de nuevo.
                </span>
              </div>

              <!-- Observaciones -->
              <div class="form-group">
                <label class="form-label">Observaciones (opcional)</label>
                <textarea
                  v-model="observaciones"
                  rows="3"
                  class="form-input form-textarea"
                  placeholder="Ej: Verificado en el banco. Transacción confirmada el 27/09 a las 10:30."
                ></textarea>
              </div>

              <div v-if="error" class="alert alert-error">
                <AlertTriangle :size="16" />
                <span>{{ error }}</span>
              </div>
            </div>

            <footer class="modal-foot">
              <button class="btn btn-ghost" type="button" :disabled="procesando" @click="cerrar">
                Cancelar
              </button>
              <button
                class="btn"
                :class="aprobado ? 'btn-primary' : 'btn-danger'"
                type="button"
                :disabled="procesando"
                @click="confirmar"
              >
                <Loader2 v-if="procesando" :size="15" class="spin" />
                <component v-else :is="aprobado ? CheckCircle2 : XCircle" :size="15" />
                {{ procesando ? 'Procesando…' : (aprobado ? 'Sí, confirmar pago' : 'Sí, rechazar pago') }}
              </button>
            </footer>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import {
  ShieldCheck, XCircle, X, User, CreditCard, PawPrint, FileText,
  Wallet, Info, AlertTriangle, Loader2, CheckCircle2,
} from 'lucide-vue-next'
import { verificarPago } from '@/api/pagos.api'
import { getApiErrorMessage } from '@/utils/apiError'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  pago: { type: Object, required: true },
  aprobado: { type: Boolean, default: true },
})
const emit = defineEmits(['update:modelValue', 'verificado'])

const observaciones = ref('')
const procesando = ref(false)
const error = ref('')

watch(() => props.modelValue, (abierto) => {
  if (abierto) {
    observaciones.value = ''
    error.value = ''
  }
})

function cerrar() {
  if (procesando.value) return
  emit('update:modelValue', false)
}

async function confirmar() {
  procesando.value = true
  error.value = ''
  try {
    await verificarPago(props.pago.idPago, {
      aprobado: props.aprobado,
      observaciones: observaciones.value.trim() || null,
    })
    emit('verificado', { pago: props.pago, aprobado: props.aprobado })
    cerrar()
  } catch (err) {
    error.value = getApiErrorMessage(err) || 'No se pudo procesar la verificación.'
  } finally {
    procesando.value = false
  }
}

function fmtUsd(v) {
  if (v == null) return '$0.00'
  return `$${Number(v).toFixed(2)}`
}

function fmtFieldLabel(key) {
  const labels = {
    banco: 'Banco',
    telefono: 'Teléfono',
    referencia: 'Referencia',
    lote: 'Lote',
    ultimos_digitos: 'Últimos 4 dígitos',
    numero_cuenta: 'Número de cuenta',
  }
  return labels[key] || String(key).replace(/_/g, ' ')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, .55);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  padding: 24px; z-index: 1100;
  font-family: 'Inter', 'Segoe UI', Roboto, sans-serif;
}
.verif-modal {
  background: #fff; border-radius: 18px;
  width: 100%; max-width: 580px;
  max-height: 90vh; display: flex; flex-direction: column;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .3);
}
.modal-head {
  display: flex; justify-content: space-between; align-items: flex-start;
  gap: 16px; padding: 20px 24px 18px;
  border-bottom: 1px solid #E2E8F0;
}
.modal-head-left { display: flex; gap: 14px; align-items: center; min-width: 0; }
.modal-icon {
  width: 40px; height: 40px; border-radius: 11px;
  background: #F0FDFA; color: #0F766E;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.modal-icon--warn {
  background: #FEF2F2; color: #DC2626;
}
.modal-eyebrow {
  display: inline-block;
  font-size: 10.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .6px;
  color: #0F766E; margin-bottom: 4px;
}
.modal-head h3 {
  margin: 0; font-size: 16px; font-weight: 700;
  color: #0F172A; letter-spacing: -0.01em;
}
.modal-close {
  width: 34px; height: 34px; border-radius: 9px;
  border: 1px solid #E2E8F0; background: #fff; color: #64748B;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; flex-shrink: 0;
}
.modal-close:hover:not(:disabled) { background: #F8FAFC; color: #1E293B; }
.modal-close:disabled { opacity: .5; cursor: not-allowed; }

.modal-body {
  flex: 1; overflow-y: auto;
  padding: 20px 24px;
  display: flex; flex-direction: column; gap: 16px;
}
.info-card {
  background: #F8FAFC; border: 1px solid #E2E8F0;
  border-radius: 12px; padding: 4px 16px;
}
.info-row {
  display: flex; justify-content: space-between; align-items: center;
  gap: 14px; padding: 9px 0;
  border-bottom: 1px solid #E2E8F0; font-size: 13px;
}
.info-row:last-child { border-bottom: none; }
.info-label {
  display: inline-flex; align-items: center; gap: 6px;
  color: #64748B; font-weight: 600; font-size: 12.5px;
  white-space: nowrap; flex-shrink: 0;
}
.info-value {
  color: #0F172A; font-weight: 700; text-align: right;
  word-break: break-word; min-width: 0;
}
.info-value.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12.5px; background: #F1F5F9;
  padding: 2px 8px; border-radius: 5px;
}
.info-value.amount { color: #0F766E; font-size: 14px; }

.tx-card {
  background: #F8FAFC; border: 1px solid #E2E8F0;
  border-radius: 12px; padding: 14px 16px;
}
.tx-title {
  display: inline-flex; align-items: center; gap: 6px;
  margin: 0 0 12px;
  font-size: 11px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .5px; color: #0F766E;
}
.tx-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
}
.tx-field {
  background: #fff; border: 1px solid #E2E8F0;
  border-radius: 8px; padding: 8px 12px;
  display: flex; flex-direction: column; gap: 2px;
  min-width: 0;
}
.tx-label {
  font-size: 10.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .4px; color: #94A3B8;
}
.tx-value {
  font-size: 13px; font-weight: 700; color: #1E293B;
  word-break: break-word;
}
.tx-value.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12.5px;
}

.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 12.5px; font-weight: 600; color: #374151; }
.form-input {
  width: 100%; padding: 10px 14px;
  border: 1px solid #D1D5DB; border-radius: 10px;
  font-size: 13.5px; color: #1E293B; background: #fff;
  font-family: inherit; box-sizing: border-box;
  transition: border-color .15s, box-shadow .15s;
}
.form-textarea { resize: vertical; min-height: 72px; line-height: 1.5; }
.form-input:focus {
  outline: none; border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}

.alert {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 12px 14px; border-radius: 10px;
  font-size: 13px; font-weight: 500; line-height: 1.5;
}
.alert-error { background: #FEF2F2; color: #991B1B; border: 1px solid #FECACA; }
.alert-warn { background: #FFFBEB; color: #92400E; border: 1px solid #FDE68A; }
.alert strong { color: inherit; font-weight: 700; }

.modal-foot {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 16px 24px; border-top: 1px solid #E2E8F0;
  background: #FAFBFC;
}

.btn {
  display: inline-flex; align-items: center; justify-content: center;
  gap: 7px; padding: 10px 20px; border-radius: 10px;
  font-size: 13.5px; font-weight: 700; font-family: inherit;
  cursor: pointer; transition: all .2s; border: 1px solid transparent;
  white-space: nowrap;
}
.btn:disabled { opacity: .55; cursor: not-allowed; }
.btn-primary { background: #0F766E; color: #fff; border-color: #0F766E; }
.btn-primary:hover:not(:disabled) {
  background: #115E59; border-color: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-danger { background: #DC2626; color: #fff; border-color: #DC2626; }
.btn-danger:hover:not(:disabled) { background: #B91C1C; border-color: #B91C1C; }
.btn-ghost { background: #fff; color: #475569; border-color: #E2E8F0; }
.btn-ghost:hover:not(:disabled) { background: #F8FAFC; border-color: #CBD5E1; color: #0F766E; }

.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.fade-enter-active, .fade-leave-active { transition: opacity .2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-up-enter-active, .slide-up-leave-active {
  transition: all .3s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from, .slide-up-leave-to {
  opacity: 0; transform: translateY(20px) scale(.98);
}
</style>