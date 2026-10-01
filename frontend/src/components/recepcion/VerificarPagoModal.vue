<template>
  <AppModal
    :model-value="modelValue"
    :title="aprobado ? 'Confirmar pago' : 'Rechazar pago'"
    subtitle="Verificación de pago online"
    size="md"
    :loading="procesando"
    @update:model-value="cerrar"
  >
    <div class="verif-content">
      <!-- Resumen del pago -->
      <div class="info-card">
        <div class="info-row">
          <span class="info-label"><User :size="13" /> Cliente</span>
          <span class="info-value">{{ pago?.clienteNombre || '—' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label"><CreditCard :size="13" /> Documento</span>
          <span class="info-value mono">{{ pago?.clienteDocumento || '—' }}</span>
        </div>
        <div v-if="pago?.mascotaNombre" class="info-row">
          <span class="info-label"><PawPrint :size="13" /> Mascota</span>
          <span class="info-value">{{ pago.mascotaNombre }}</span>
        </div>
        <div class="info-row">
          <span class="info-label"><FileText :size="13" /> Factura</span>
          <span class="info-value mono">{{ pago?.numeroControl || '—' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label"><Wallet :size="13" /> Monto</span>
          <span class="info-value amount">
            {{ fmtUsd(pago?.monto) }} USD
            <span v-if="pago?.montoBs != null" class="monto-bs">
              · Bs. {{ fmtBs(pago.montoBs) }}
            </span>
          </span>
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
          <template v-for="(val, key) in (pago?.metadataPago || {})" :key="key">
            <div v-if="key !== 'referencia'" class="tx-field">
              <span class="tx-label">{{ fmtFieldLabel(key) }}</span>
              <span class="tx-value">{{ val }}</span>
            </div>
          </template>
        </div>
      </div>

      <!-- Aviso si es rechazo -->
      <AppAlert v-if="!aprobado" variant="warning">
        Al rechazar, el pago quedará como <strong>Rechazado</strong> y la cita se
        <strong>cancelará automáticamente</strong>. El cliente deberá agendar de nuevo.
      </AppAlert>

      <!-- Observaciones -->
      <div class="form-group">
        <label class="form-label">Observaciones (opcional)</label>
        <AppTextarea
          v-model="observaciones"
          :rows="3"
          placeholder="Ej: Verificado en el banco. Transacción confirmada el 27/09 a las 10:30."
        />
      </div>

      <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    </div>

    <template #footer>
      <AppButton variant="secondary" :disabled="procesando" @click="cerrar">
        Cancelar
      </AppButton>
      <AppButton
        :variant="aprobado ? 'primary' : 'danger'"
        :loading="procesando"
        @click="confirmar"
      >
        <template #icon-left>
          <component :is="aprobado ? CheckCircle2 : XCircle" :size="15" />
        </template>
        {{ procesando
          ? 'Procesando…'
          : (aprobado ? 'Sí, confirmar pago' : 'Sí, rechazar pago') }}
      </AppButton>
    </template>
  </AppModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import {
  XCircle, User, CreditCard, PawPrint, FileText,
  Wallet, Info, CheckCircle2,
} from 'lucide-vue-next'
import { verificarPago } from '@/api/pagos.api'
import { getApiErrorMessage } from '@/utils/apiError'

import AppModal from '@/components/ui/AppModal.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppTextarea from '@/components/ui/AppTextarea.vue'

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
  if (procesando.value) return
  procesando.value = true
  error.value = ''
  try {
    await verificarPago(props.pago.idPago, {
      aprobado: props.aprobado,
      observaciones: observaciones.value.trim() || null,
    })
    procesando.value = false
    emit('verificado', { pago: props.pago, aprobado: props.aprobado })
    emit('update:modelValue', false)
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

function fmtBs(v) {
  const n = Number(v)
  if (v == null || Number.isNaN(n)) return '—'
  return n.toLocaleString('es-VE', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
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
.verif-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.info-card {
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-1) var(--space-4);
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-subtle);
  font-size: var(--text-md);
}
.info-row:last-child { border-bottom: none; }
.info-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  font-weight: var(--font-semibold);
  font-size: var(--text-sm);
  white-space: nowrap;
  flex-shrink: 0;
}
.info-label svg { color: var(--text-tertiary); }
.info-value {
  color: var(--text-primary);
  font-weight: var(--font-bold);
  text-align: right;
  word-break: break-word;
  min-width: 0;
}
.info-value.mono {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  background: var(--neutral-100);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
}
.info-value.amount { color: var(--brand-700); font-size: var(--text-lg); }
.info-value .monto-bs {
  color: var(--neutral-700);
  font-weight: var(--font-bold);
  font-size: var(--text-md);
}

.tx-card {
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-5);
}
.tx-title {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--brand-700);
}
.tx-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--space-3);
}
.tx-field {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.tx-label {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--text-tertiary);
}
.tx-value {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  word-break: break-word;
}
.tx-value.mono {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
}

.form-group { display: flex; flex-direction: column; gap: var(--space-2); }
.form-label {
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--neutral-700);
}
</style>