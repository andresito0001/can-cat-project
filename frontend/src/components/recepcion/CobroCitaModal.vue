<template>
  <AppModal
    :model-value="modelValue"
    :title="cita?.nombreMascota || 'Cobro de cita'"
    :subtitle="cita?.nombreCliente ? `${cita.nombreCliente} · ${cita.nombreServicio || ''}` : 'Cobro presencial'"
    size="lg"
    :loading="procesando"
    @update:model-value="cerrar"
  >
    <div class="cobro-content">
      <!-- Info cita -->
      <div class="info-card">
        <div class="info-row">
          <span class="info-label"><PawPrint :size="13" /> Mascota</span>
          <span class="info-value">{{ cita?.nombreMascota || '—' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label"><Stethoscope :size="13" /> Servicio</span>
          <span class="info-value">{{ cita?.nombreServicio || '—' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label"><CalendarDays :size="13" /> Turno</span>
          <span class="info-value">
            {{ fmtFecha(cita?.fecha) }} · {{ fmtHora(cita?.horaInicio) }}
          </span>
        </div>
      </div>

      <!-- Total -->
      <div class="total-card">
        <div class="total-left">
          <span class="total-label">Resumen del cobro</span>
          <span class="total-hint">Bs. se calcula con la tasa oficial</span>
        </div>
        <div class="total-right">
          <div class="total-row">
            <span class="total-row-label">Subtotal</span>
            <span class="total-row-value">${{ fmtUsd(cita.subtotalUsd) }}</span>
          </div>
          <div class="total-row">
            <span class="total-row-label">IVA ({{ cita.porcentajeIva }}%)</span>
            <span class="total-row-value">${{ fmtUsd(cita.ivaUsd) }}</span>
          </div>
          <div class="total-row total-row-final">
            <span class="total-row-label">Total</span>
            <span class="total-row-total">{{ fmtUsd(cita.costoUsd) }} USD</span>
          </div>
        </div>
      </div>

      <!-- Error -->
      <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>

      <!-- Método de pago -->
      <p class="section-label">Método de pago <span class="required">*</span></p>
      <div class="methods-list">
        <button
          v-for="m in metodos"
          :key="m.id"
          type="button"
          class="method-option"
          :class="{ active: selectedMetodo === m.id }"
          @click="seleccionarMetodo(m)"
        >
          <div class="method-radio">
            <div class="radio-outer" :class="{ checked: selectedMetodo === m.id }">
              <div v-if="selectedMetodo === m.id" class="radio-inner" />
            </div>
          </div>
          <div class="method-info">
            <span class="method-name">{{ METODO_LABEL[m.nombre] || m.nombre }}</span>
            <span class="method-desc">{{ m.descripcion }}</span>
          </div>
        </button>
      </div>

      <!-- Campos dinámicos -->
      <Transition name="expand">
        <div v-if="metodoSeleccionado" class="dynamic-fields">
          <p class="section-label">Datos del pago</p>

          <div v-if="'referencia' in camposActuales" class="form-group">
            <label class="form-label">
              Número de referencia <span class="required">*</span>
            </label>
            <AppInput
              v-model="referencia"
              placeholder="Ej: 0000123456789"
              :error="errors.referencia"
            />
            <span v-if="errors.referencia" class="form-error">{{ errors.referencia }}</span>
          </div>

          <div v-for="campo in camposDinamicos" :key="campo.key" class="form-group">
            <label class="form-label">
              {{ fmtFieldLabel(campo.key) }} <span class="required">*</span>
            </label>
            <BancoSelector
              v-if="campo.key === 'banco'"
              v-model="datosPago[campo.key]"
              :error="errors[campo.key]"
              placeholder="Selecciona el banco emisor"
            />
            <AppInput
              v-else
              v-model="datosPago[campo.key]"
              :error="errors[campo.key]"
            />
            <span v-if="errors[campo.key]" class="form-error">{{ errors[campo.key] }}</span>
          </div>
        </div>
      </Transition>

      <!-- Confirmar fondos -->
      <label class="check-row" :class="{ 'is-invalid': errors.fondos }">
        <input v-model="fondosConfirmados" type="checkbox" class="checkbox-input" />
        <span class="checkbox-box">
          <Check v-if="fondosConfirmados" :size="12" />
        </span>
        <span class="checkbox-label">Confirmo la recepción de los fondos</span>
      </label>
      <span v-if="errors.fondos" class="form-error">{{ errors.fondos }}</span>
    </div>

    <template #footer>
      <AppButton variant="secondary" :disabled="procesando" @click="cerrar">
        Cancelar
      </AppButton>
      <AppButton
        variant="primary"
        :loading="procesando"
        :disabled="!puedeConfirmar"
        @click="confirmar"
      >
        <template #icon-left><Banknote :size="15" /></template>
        {{ procesando ? 'Procesando…' : 'Cobrar y confirmar' }}
      </AppButton>
    </template>
  </AppModal>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import {
  Banknote, PawPrint, Stethoscope, CalendarDays, Check,
} from 'lucide-vue-next'
import { getMetodosPresenciales } from '@/api/pagos.api'
import { cobrarCitaMostrador } from '@/api/citas.api'
import { getApiErrorMessage } from '@/utils/apiError'

import AppModal from '@/components/ui/AppModal.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppInput from '@/components/ui/AppInput.vue'
import BancoSelector from '@/components/ui/BancoSelector.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  cita: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue', 'cobrado'])

const METODO_LABEL = {
  Efectivo: 'Efectivo',
  Tarjeta: 'Tarjeta (Punto de Venta)',
  Pago_Movil: 'Pago Móvil',
}

const metodos = ref([])
const selectedMetodo = ref(null)
const datosPago = ref({})
const referencia = ref('')
const fondosConfirmados = ref(false)
const procesando = ref(false)
const error = ref('')
const errors = ref({})

const metodoSeleccionado = computed(() =>
  metodos.value.find((m) => m.id === selectedMetodo.value) || null
)

const camposActuales = computed(() =>
  metodoSeleccionado.value?.camposRequeridos
  ?? metodoSeleccionado.value?.datosRequeridos
  ?? {}
)

const camposDinamicos = computed(() =>
  Object.entries(camposActuales.value)
    .filter(([key]) => key !== 'referencia')
    .map(([key]) => ({ key }))
)

const puedeConfirmar = computed(() => {
  if (!selectedMetodo.value) return false
  if (!fondosConfirmados.value) return false
  const campos = camposActuales.value
  if ('referencia' in campos && !referencia.value.trim()) return false
  for (const { key } of camposDinamicos.value) {
    if (!datosPago.value[key]?.trim()) return false
  }
  return true
})

onMounted(async () => {
  try {
    const { data } = await getMetodosPresenciales()
    metodos.value = data || []
  } catch { /* sin métodos */ }
})

watch(() => props.modelValue, (abierto) => {
  if (abierto) reset()
})

function reset() {
  selectedMetodo.value = null
  datosPago.value = {}
  referencia.value = ''
  fondosConfirmados.value = false
  error.value = ''
  errors.value = {}
}

function seleccionarMetodo(m) {
  selectedMetodo.value = m.id
  datosPago.value = {}
  referencia.value = ''
  errors.value = {}
  error.value = ''
}

function cerrar() {
  if (procesando.value) return
  emit('update:modelValue', false)
}

async function confirmar() {
  const campos = camposActuales.value
  const validationErrors = {}
  if (!selectedMetodo.value) validationErrors.metodo = 'Selecciona un método'
  if ('referencia' in campos && !referencia.value.trim()) {
    validationErrors.referencia = 'La referencia es obligatoria'
  }
  for (const { key } of camposDinamicos.value) {
    if (!datosPago.value[key]?.trim()) validationErrors[key] = 'Este campo es obligatorio'
  }
  if (!fondosConfirmados.value) validationErrors.fondos = 'Debes confirmar la recepción'
  errors.value = validationErrors
  if (Object.keys(validationErrors).length > 0) {
    error.value = 'Verifica los campos marcados antes de continuar.'
    return
  }

  procesando.value = true
  error.value = ''
  try {
    const datosCompletos = { ...datosPago.value }
    if ('referencia' in campos) datosCompletos.referencia = referencia.value.trim()

    const payload = {
      idMetodoPago: selectedMetodo.value,
      referenciaTransaccion: referencia.value.trim() || undefined,
      datosPago: datosCompletos,
    }
    await cobrarCitaMostrador(props.cita.idCita, payload)
    emit('cobrado', props.cita)
    emit('update:modelValue', false)
  } catch (err) {
    error.value = getApiErrorMessage(err) || 'No se pudo procesar el cobro.'
  } finally {
    procesando.value = false
  }
}

function fmtUsd(v) {
  if (v == null) return '$0.00'
  return `$${Number(v).toFixed(2)}`
}

function fmtHora(t) {
  if (!t) return ''
  const [h, m] = t.split(':')
  const hh = Number(h) % 12 || 12
  return `${hh}:${m} ${Number(h) >= 12 ? 'PM' : 'AM'}`
}

function fmtFecha(iso) {
  if (!iso) return ''
  const [y, m, d] = String(iso).slice(0, 10).split('-')
  const MESES = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']
  return `${d} ${MESES[Number(m)-1]} ${y}`
}

function fmtFieldLabel(key) {
  const labels = {
    banco: 'Banco emisor',
    telefono: 'Teléfono asociado',
    lote: 'Número de lote',
    ultimos_digitos: 'Últimos 4 dígitos',
  }
  return labels[key] || key.replace(/_/g, ' ')
}
</script>

<style scoped>
.cobro-content {
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

.total-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  background: linear-gradient(135deg, var(--brand-50) 0%, var(--bg-surface) 100%);
  border: 1px solid var(--brand-200);
  border-radius: var(--radius-xl);
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  gap: var(--space-3);
  font-size: var(--text-sm);
}
.total-row-label {
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}
.total-row-value {
  color: var(--neutral-700);
  font-weight: var(--font-semibold);
  font-variant-numeric: tabular-nums;
}

.total-row-final {
  padding-top: var(--space-3);
  margin-top: var(--space-1);
  border-top: 1px solid var(--brand-200);
}
.total-row-final .total-row-label {
  font-size: var(--text-xs);
  font-weight: var(--font-extrabold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
}
.total-row-total {
  font-size: 22px;
  font-weight: var(--font-bold);
  color: var(--brand-700);
  letter-spacing: -0.02em;
  line-height: 1;
}

.total-left { display: flex; flex-direction: column; gap: 2px; }
.total-label {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--brand-700);
}
.total-hint { font-size: var(--text-xs); color: var(--text-secondary); }
.total-right { display: flex; align-items: baseline; gap: var(--space-1); }
.total-monto {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  letter-spacing: var(--tracking-tight);
  line-height: 1;
}
.total-currency {
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  letter-spacing: 0.05em;
}

.section-label {
  margin: var(--space-2) 0 0;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}
.required { color: var(--danger-500); }

.methods-list { display: flex; flex-direction: column; gap: var(--space-3); }
.method-option {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  border: 1.5px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--duration-base) var(--ease-out);
  background: var(--bg-surface);
  font-family: inherit;
  text-align: left;
  width: 100%;
}
.method-option:hover { border-color: var(--brand-200); background: var(--brand-50); }
.method-option.active {
  border-color: var(--brand-700);
  background: var(--brand-50);
  box-shadow: 0 0 0 3px var(--brand-100);
}
.method-radio { flex-shrink: 0; }
.radio-outer {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid var(--neutral-300);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--duration-base);
}
.radio-outer.checked { border-color: var(--brand-700); }
.radio-inner {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--brand-700);
  animation: radioPop 0.2s var(--ease-out);
}
@keyframes radioPop { 0% { transform: scale(0); } 100% { transform: scale(1); } }
.method-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.method-name { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); }
.method-desc { font-size: var(--text-sm); color: var(--text-secondary); }

.dynamic-fields {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-subtle);
}
.form-group { display: flex; flex-direction: column; gap: var(--space-2); }
.form-label {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--neutral-700);
}
.form-error {
  font-size: var(--text-sm);
  color: var(--danger-600);
  font-weight: var(--font-semibold);
}

.check-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1.5px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--duration-fast) var(--ease-out);
  margin: 0;
}
.check-row:hover { border-color: var(--brand-200); background: var(--brand-50); }
.check-row.is-invalid { border-color: var(--danger-500); background: var(--danger-50); }
.checkbox-input { display: none; }
.checkbox-box {
  width: 20px;
  height: 20px;
  border-radius: var(--radius-sm);
  border: 2px solid var(--neutral-300);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-surface);
  flex-shrink: 0;
  transition: all var(--duration-fast);
  color: var(--text-inverse);
}
.check-row .checkbox-input:checked + .checkbox-box {
  background: var(--brand-700);
  border-color: var(--brand-700);
}
.checkbox-label {
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.expand-enter-active,
.expand-leave-active {
  transition: opacity var(--duration-slow) var(--ease-out),
              transform var(--duration-slow) var(--ease-out);
  overflow: hidden;
}
.expand-enter-from,
.expand-leave-to { opacity: 0; transform: translateY(-6px); }
</style>