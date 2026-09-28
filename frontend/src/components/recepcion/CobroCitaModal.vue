<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="modelValue" class="modal-overlay" @click.self="!procesando && cerrar()">
        <Transition name="slide-up" appear>
          <div v-if="modelValue" class="cobro-modal">
            <header class="modal-head">
              <div class="modal-head-left">
                <div class="modal-icon"><Banknote :size="18" /></div>
                <div>
                  <span class="modal-eyebrow">Cobro de cita</span>
                  <h3>{{ cita?.nombreMascota }} · {{ cita?.nombreCliente }}</h3>
                </div>
              </div>
              <button class="modal-close" type="button" :disabled="procesando" @click="cerrar">
                <X :size="17" />
              </button>
            </header>

            <div class="modal-body">
              <!-- Info de la cita -->
              <div class="info-card">
                <div class="info-row">
                  <span class="info-label"><PawPrint :size="13" /> Mascota</span>
                  <span class="info-value">{{ cita?.nombreMascota }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label"><Stethoscope :size="13" /> Servicio</span>
                  <span class="info-value">{{ cita?.nombreServicio }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label"><CalendarDays :size="13" /> Turno</span>
                  <span class="info-value">{{ fmtFecha(cita?.fecha) }} · {{ fmtHora(cita?.horaInicio) }}</span>
                </div>
              </div>

              <!-- Total -->
              <div class="total-card">
                <div class="total-left">
                  <span class="total-label">Total a cobrar</span>
                  <span class="total-hint">Bs. se calcula con la tasa oficial</span>
                </div>
                <div class="total-right">
                  <span class="total-monto">{{ fmtUsd(cita?.costoUsd) }}</span>
                  <span class="total-currency">USD</span>
                </div>
              </div>

              <!-- Error -->
              <div v-if="error" class="alert alert-error">
                <AlertTriangle :size="16" />
                <span>{{ error }}</span>
              </div>

              <!-- Método de pago -->
              <p class="section-label">Método de pago <span class="req">*</span></p>
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
                    <label class="form-label">Número de referencia <span class="req">*</span></label>
                    <input
                      v-model="referencia"
                      type="text"
                      class="form-input"
                      :class="{ 'is-invalid': errors.referencia }"
                      placeholder="Ej: 0000123456789"
                    />
                    <span v-if="errors.referencia" class="form-error">{{ errors.referencia }}</span>
                  </div>

                  <div v-for="campo in camposDinamicos" :key="campo.key" class="form-group">
                    <label class="form-label">
                      {{ fmtFieldLabel(campo.key) }} <span class="req">*</span>
                    </label>
                    <select
                      v-if="campo.key === 'banco'"
                      v-model="datosPago[campo.key]"
                      class="form-select"
                      :class="{ 'is-invalid': errors[campo.key] }"
                    >
                      <option value="" disabled>Selecciona el banco emisor</option>
                      <option v-for="b in BANCOS_VENEZUELA" :key="b.codigo" :value="b.nombre">
                        {{ b.codigo }} - {{ b.nombre }}
                      </option>
                    </select>
                    <input
                      v-else
                      v-model="datosPago[campo.key]"
                      type="text"
                      class="form-input"
                      :class="{ 'is-invalid': errors[campo.key] }"
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

            <footer class="modal-foot">
              <button class="btn btn-ghost" type="button" :disabled="procesando" @click="cerrar">
                Cancelar
              </button>
              <button
                class="btn btn-primary"
                type="button"
                :disabled="!puedeConfirmar || procesando"
                @click="confirmar"
              >
                <Loader2 v-if="procesando" :size="15" class="spin" />
                <Banknote v-else :size="15" />
                {{ procesando ? 'Procesando…' : 'Cobrar y confirmar' }}
              </button>
            </footer>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import {
  Banknote, X, PawPrint, Stethoscope, CalendarDays,
  AlertTriangle, Loader2, Check,
} from 'lucide-vue-next'
import { getMetodosPresenciales } from '@/api/pagos.api'
import { cobrarCitaMostrador } from '@/api/citas.api'
import { getApiErrorMessage } from '@/utils/apiError'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  cita: { type: Object, required: true },
})
const emit = defineEmits(['update:modelValue', 'cobrado'])

const BANCOS_VENEZUELA = [
  { codigo: '0102', nombre: 'Banco de Venezuela, S.A.C.A.' },
  { codigo: '0104', nombre: 'Venezolano de Crédito, S.A.' },
  { codigo: '0105', nombre: 'Mercantil Banco, C.A.' },
  { codigo: '0108', nombre: 'Banco Provincial, S.A.' },
  { codigo: '0114', nombre: 'Banco del Caribe, C.A.' },
  { codigo: '0134', nombre: 'Banesco Banco Universal, C.A.' },
  { codigo: '0151', nombre: 'Banco Fondo Común, C.A.' },
  { codigo: '0163', nombre: 'Banco del Tesoro, C.A.' },
  { codigo: '0177', nombre: 'BANFANB' },
  { codigo: '0190', nombre: 'Banco Nacional de Crédito, C.A.' },
]
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
  metodos.value.find(m => m.id === selectedMetodo.value) || null
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
    cerrar()
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
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, .55);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  padding: 24px; z-index: 1100;
  font-family: 'Inter', 'Segoe UI', Roboto, sans-serif;
}
.cobro-modal {
  background: #fff; border-radius: 18px;
  width: 100%; max-width: 620px;
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
.modal-eyebrow {
  display: inline-block;
  font-size: 10.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .6px;
  color: #0F766E; margin-bottom: 4px;
}
.modal-head h3 {
  margin: 0; font-size: 16px; font-weight: 700;
  color: #0F172A; letter-spacing: -0.01em;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
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
  scrollbar-width: thin; scrollbar-color: #CBD5E1 transparent;
}
.modal-body::-webkit-scrollbar { width: 6px; }
.modal-body::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 3px; }

.info-card {
  background: #F8FAFC; border: 1px solid #E2E8F0;
  border-radius: 12px; padding: 4px 16px;
}
.info-row {
  display: flex; justify-content: space-between; align-items: center;
  gap: 14px; padding: 9px 0; border-bottom: 1px solid #E2E8F0;
  font-size: 13px;
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
.total-card {
  display: flex; justify-content: space-between; align-items: center;
  gap: 16px; padding: 16px 18px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 100%);
  border: 1px solid #99F6E4; border-radius: 12px;
}
.total-left { display: flex; flex-direction: column; gap: 2px; }
.total-label {
  font-size: 12px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .5px; color: #0F766E;
}
.total-hint { font-size: 11px; color: #64748B; }
.total-right { display: flex; align-items: baseline; gap: 5px; }
.total-monto {
  font-size: 24px; font-weight: 700; color: #0F766E;
  letter-spacing: -0.02em; line-height: 1;
}
.total-currency {
  font-size: 12px; font-weight: 700; color: #0F766E; letter-spacing: .5px;
}

.section-label {
  margin: 0 0 10px;
  font-size: 12px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .5px; color: #64748B;
}
.req { color: #EF4444; }

.methods-list { display: flex; flex-direction: column; gap: 8px; }
.method-option {
  display: flex; align-items: center; gap: 14px;
  padding: 12px 14px;
  border: 1.5px solid #E2E8F0; border-radius: 12px;
  cursor: pointer; transition: all .2s; background: #fff;
  font-family: inherit; text-align: left; width: 100%;
}
.method-option:hover { border-color: #99F6E4; background: #F0FDFA; }
.method-option.active {
  border-color: #0F766E; background: #F0FDFA;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.method-radio { flex-shrink: 0; }
.radio-outer {
  width: 20px; height: 20px; border-radius: 50%;
  border: 2px solid #CBD5E1;
  display: flex; align-items: center; justify-content: center;
  transition: all .2s;
}
.radio-outer.checked { border-color: #0F766E; }
.radio-inner {
  width: 10px; height: 10px; border-radius: 50%;
  background: #0F766E; animation: radioPop .2s ease;
}
@keyframes radioPop { 0% { transform: scale(0); } 100% { transform: scale(1); } }
.method-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.method-name { font-size: 13.5px; font-weight: 700; color: #0F172A; }
.method-desc { font-size: 12px; color: #64748B; }

.dynamic-fields {
  display: flex; flex-direction: column; gap: 14px;
  padding-top: 16px; border-top: 1px solid #F1F5F9;
}
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-label {
  font-size: 12.5px; font-weight: 600; color: #374151;
}
.form-input, .form-select {
  width: 100%; padding: 10px 14px;
  border: 1px solid #D1D5DB; border-radius: 10px;
  font-size: 13.5px; color: #1E293B; background: #fff;
  font-family: inherit; box-sizing: border-box;
  transition: border-color .15s, box-shadow .15s;
  appearance: none; -webkit-appearance: none;
}
.form-select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  padding-right: 40px; cursor: pointer;
}
.form-input:focus, .form-select:focus {
  outline: none; border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.is-invalid { border-color: #EF4444 !important; background: #FEF2F2 !important; }
.form-error { font-size: 12px; color: #EF4444; font-weight: 600; }

.check-row {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 14px;
  background: #F8FAFC; border: 1.5px solid #E2E8F0;
  border-radius: 10px; cursor: pointer;
  font-family: inherit; transition: all .2s;
  margin: 0;
}
.check-row:hover { border-color: #99F6E4; background: #F0FDFA; }
.check-row.is-invalid { border-color: #EF4444; background: #FEF2F2; }
.checkbox-input { display: none; }
.checkbox-box {
  width: 20px; height: 20px; border-radius: 6px;
  border: 2px solid #CBD5E1; background: #fff;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; color: #fff; transition: all .2s;
}
.check-row .checkbox-input:checked + .checkbox-box {
  background: #0F766E; border-color: #0F766E;
}
.checkbox-label { font-size: 13px; font-weight: 600; color: #1E293B; }

.alert {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 12px 16px; border-radius: 10px;
  font-size: 13px; font-weight: 500; line-height: 1.5;
}
.alert-error {
  background: #FEF2F2; color: #991B1B; border: 1px solid #FECACA;
}

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
.btn-primary {
  background: #0F766E; color: #fff; border-color: #0F766E;
}
.btn-primary:hover:not(:disabled) {
  background: #115E59; border-color: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-ghost {
  background: #fff; color: #475569; border-color: #E2E8F0;
}
.btn-ghost:hover:not(:disabled) {
  background: #F8FAFC; border-color: #CBD5E1; color: #0F766E;
}

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
.expand-enter-active, .expand-leave-active {
  transition: all .2s ease; overflow: hidden;
}
.expand-enter-from, .expand-leave-to {
  opacity: 0; transform: translateY(-6px);
}
</style>