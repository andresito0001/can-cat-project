<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="modal-overlay" @click.self="cerrar">
        <Transition name="slide-up" appear>
          <div v-if="visible" class="modal-card">
            <header class="modal-header">
              <div class="modal-header-left">
                <div class="modal-icon"><Truck :size="18" /></div>
                <div>
                  <h3>{{ esEdicion ? 'Editar proveedor' : 'Nuevo proveedor' }}</h3>
                  <p class="modal-header-sub">
                    {{ esEdicion ? 'Actualiza los datos del proveedor' : 'Registra un nuevo proveedor' }}
                  </p>
                </div>
              </div>
              <button class="btn-close" type="button" :disabled="guardando" @click="cerrar">
                <X :size="18" />
              </button>
            </header>

            <div class="modal-body">
              <div v-if="error" class="alert alert-error">
                <AlertCircle :size="16" />
                <span>{{ error }}</span>
              </div>

              <div class="form-grid-2">
                <div class="form-group">
                  <label class="form-label" for="rif">
                    RIF <span class="required">*</span>
                  </label>
                  <input
                    id="rif"
                    v-model="form.rif"
                    type="text"
                    class="form-input"
                    :class="{ 'is-invalid': errors.rif }"
                    placeholder="J-12345678-9"
                    :disabled="esEdicion || guardando"
                  />
                  <span v-if="esEdicion" class="field-hint">El RIF no es editable</span>
                  <span v-else class="field-hint">Formato: J-12345678-9</span>
                  <span v-if="errors.rif" class="form-error">{{ errors.rif }}</span>
                </div>

                <div class="form-group">
                  <label class="form-label" for="tipo">
                    Tipo de suministro <span class="required">*</span>
                  </label>
                  <select
                    id="tipo"
                    v-model="form.tipoSuministro"
                    class="form-select"
                    :class="{ 'is-invalid': errors.tipoSuministro }"
                    :disabled="guardando"
                  >
                    <option value="" disabled>Seleccione…</option>
                    <option value="Medicamentos">Medicamentos</option>
                    <option value="Alimentos">Alimentos</option>
                    <option value="Mixto">Mixto</option>
                  </select>
                  <span v-if="errors.tipoSuministro" class="form-error">{{ errors.tipoSuministro }}</span>
                </div>
              </div>

              <div class="form-group">
                <label class="form-label" for="empresa">
                  Nombre de la empresa <span class="required">*</span>
                </label>
                <input
                  id="empresa"
                  v-model="form.nombreEmpresa"
                  type="text"
                  class="form-input"
                  :class="{ 'is-invalid': errors.nombreEmpresa }"
                  placeholder="Ej: Distribuidora VetMed C.A."
                  maxlength="150"
                  :disabled="guardando"
                />
                <span v-if="errors.nombreEmpresa" class="form-error">{{ errors.nombreEmpresa }}</span>
              </div>

              <div class="form-grid-2">
                <div class="form-group">
                  <label class="form-label" for="contacto">Persona de contacto</label>
                  <input
                    id="contacto"
                    v-model="form.nombreContacto"
                    type="text"
                    class="form-input"
                    placeholder="Ej: María López"
                    maxlength="100"
                    :disabled="guardando"
                  />
                </div>

                <div class="form-group">
                  <label class="form-label" for="telefono">Teléfono</label>
                  <input
                    id="telefono"
                    v-model="form.telefono"
                    type="text"
                    class="form-input"
                    placeholder="Ej: 0212-5551234"
                    maxlength="20"
                    :disabled="guardando"
                  />
                </div>
              </div>

              <div class="form-group">
                <label class="form-label" for="correo">Correo electrónico</label>
                <input
                  id="correo"
                  v-model="form.correo"
                  type="email"
                  class="form-input"
                  :class="{ 'is-invalid': errors.correo }"
                  placeholder="Ej: ventas@vetmed.com"
                  maxlength="100"
                  :disabled="guardando"
                />
                <span v-if="errors.correo" class="form-error">{{ errors.correo }}</span>
              </div>

              <div class="form-group">
                <label class="form-label" for="direccion">Dirección</label>
                <textarea
                  id="direccion"
                  v-model="form.direccion"
                  class="form-textarea"
                  rows="2"
                  placeholder="Dirección fiscal o de despacho…"
                  :disabled="guardando"
                ></textarea>
              </div>
            </div>

            <footer class="modal-footer">
              <button class="btn-secondary" type="button" :disabled="guardando" @click="cerrar">
                Cancelar
              </button>
              <button class="btn-primary" type="button" :disabled="guardando" @click="guardar">
                <Loader2 v-if="guardando" :size="15" class="spin" />
                <Save v-else :size="15" />
                {{ guardando ? 'Guardando…' : (esEdicion ? 'Guardar cambios' : 'Crear proveedor') }}
              </button>
            </footer>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { X, Truck, AlertCircle, Loader2, Save } from 'lucide-vue-next'
import { crearProveedor, actualizarProveedor } from '@/api/almacen.api'
import { getApiErrorMessage } from '@/utils/apiError'

const props = defineProps({
  visible: { type: Boolean, default: false },
  proveedor: { type: Object, default: null },
})
const emit = defineEmits(['close', 'saved'])

const guardando = ref(false)
const error = ref('')

const form = reactive({
  rif: '',
  nombreEmpresa: '',
  nombreContacto: '',
  telefono: '',
  correo: '',
  direccion: '',
  tipoSuministro: '',
})

const errors = reactive({
  rif: '',
  nombreEmpresa: '',
  correo: '',
  tipoSuministro: '',
})

const esEdicion = computed(() => !!props.proveedor?.id)

watch(() => props.visible, (v) => {
  if (v) resetForm()
})

function resetForm() {
  Object.keys(errors).forEach((k) => (errors[k] = ''))
  error.value = ''

  if (props.proveedor) {
    form.rif = props.proveedor.rif || ''
    form.nombreEmpresa = props.proveedor.nombreEmpresa || ''
    form.nombreContacto = props.proveedor.nombreContacto || ''
    form.telefono = props.proveedor.telefono || ''
    form.correo = props.proveedor.correo || ''
    form.direccion = props.proveedor.direccion || ''
    form.tipoSuministro = props.proveedor.tipoSuministro || ''
  } else {
    form.rif = ''
    form.nombreEmpresa = ''
    form.nombreContacto = ''
    form.telefono = ''
    form.correo = ''
    form.direccion = ''
    form.tipoSuministro = ''
  }
}

function cerrar() {
  if (guardando.value) return
  emit('close')
}

function validar() {
  Object.keys(errors).forEach((k) => (errors[k] = ''))
  let ok = true

  if (!esEdicion.value) {
    if (!form.rif.trim()) {
      errors.rif = 'El RIF es obligatorio'
      ok = false
    } else if (!/^[JGVEP]-\d{8}-\d$/i.test(form.rif.trim())) {
      errors.rif = 'Formato inválido. Ejemplo: J-12345678-9'
      ok = false
    }
  }
  if (!form.nombreEmpresa.trim()) {
    errors.nombreEmpresa = 'El nombre de la empresa es obligatorio'
    ok = false
  }
  if (form.correo && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.correo)) {
    errors.correo = 'Correo inválido'
    ok = false
  }
  if (!form.tipoSuministro) {
    errors.tipoSuministro = 'Selecciona un tipo de suministro'
    ok = false
  }
  return ok
}

async function guardar() {
  error.value = ''
  if (!validar()) return

  guardando.value = true
  try {
    const payload = {
      nombreEmpresa: form.nombreEmpresa.trim(),
      nombreContacto: form.nombreContacto.trim() || null,
      telefono: form.telefono.trim() || null,
      correo: form.correo.trim() || null,
      direccion: form.direccion.trim() || null,
      tipoSuministro: form.tipoSuministro,
    }

    if (esEdicion.value) {
      const { data } = await actualizarProveedor(props.proveedor.id, payload)
      emit('saved', data)
    } else {
      const { data } = await crearProveedor({
        ...payload,
        rif: form.rif.trim().toUpperCase(),
      })
      emit('saved', data)
    }
  } catch (err) {
    error.value = getApiErrorMessage(err)
  } finally {
    guardando.value = false
  }
}
</script>

<style scoped>
* { box-sizing: border-box; }
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, .5);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  padding: 24px;
  z-index: var(--z-modal);
  font-family: 'Inter', 'Segoe UI', Roboto, sans-serif;
}
.modal-card {
  background: #fff; border-radius: 16px;
  width: 100%; max-width: 600px; max-height: 90vh;
  display: flex; flex-direction: column; overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .3);
}
.modal-header {
  display: flex; align-items: flex-start; justify-content: space-between;
  gap: 16px; padding: 22px 24px 18px; border-bottom: 1px solid #E2E8F0;
}
.modal-header-left { display: flex; align-items: center; gap: 12px; min-width: 0; }
.modal-icon {
  width: 40px; height: 40px; border-radius: 11px;
  background: #F0FDFA; color: #0F766E;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.modal-header h3 { margin: 0 0 2px; font-size: 15.5px; font-weight: 700; color: #0F172A; }
.modal-header-sub { margin: 0; font-size: 12.5px; color: #64748B; }
.btn-close {
  width: 34px; height: 34px; border-radius: 9px;
  border: 1px solid #E2E8F0; background: #fff; color: #64748B;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; flex-shrink: 0;
}
.btn-close:hover:not(:disabled) { background: #F1F5F9; color: #0F172A; }
.btn-close:disabled { opacity: .5; cursor: not-allowed; }

.modal-body {
  flex: 1; overflow-y: auto; padding: 22px 24px;
  display: flex; flex-direction: column; gap: 16px;
}
.form-grid-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-label { font-size: 12.5px; font-weight: 600; color: #374151; }
.required { color: #EF4444; }
.field-hint { font-size: 11.5px; color: #94A3B8; }
.form-input, .form-select, .form-textarea {
  width: 100%; padding: 10px 14px;
  border: 1px solid #D1D5DB; border-radius: 10px;
  font-size: 13.5px; color: #0F172A; background: #fff;
  font-family: inherit;
  transition: border-color .2s, box-shadow .2s;
  box-sizing: border-box; appearance: none;
}
.form-select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat; background-position: right 12px center;
  padding-right: 36px; cursor: pointer;
}
.form-textarea { resize: vertical; min-height: 60px; line-height: 1.5; }
.form-input:focus, .form-select:focus, .form-textarea:focus {
  outline: none; border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.form-input:disabled, .form-select:disabled, .form-textarea:disabled {
  background: #F8FAFC; color: #94A3B8; cursor: not-allowed;
}
.is-invalid { border-color: #EF4444 !important; background: #FEF2F2 !important; }
.form-error { font-size: 11.5px; color: #EF4444; font-weight: 600; }

.alert { display: flex; align-items: flex-start; gap: 10px; padding: 12px 14px; border-radius: 10px; font-size: 13px; font-weight: 500; }
.alert-error { background: #FEF2F2; color: #991B1B; border: 1px solid #FECACA; }

.modal-footer {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 16px 24px; border-top: 1px solid #E2E8F0; background: #FAFBFC;
}
.btn-primary, .btn-secondary {
  display: inline-flex; align-items: center; justify-content: center;
  gap: 7px; padding: 10px 20px; border-radius: 10px;
  font-size: 13.5px; font-weight: 700; font-family: inherit;
  cursor: pointer; transition: all .2s; white-space: nowrap;
}
.btn-primary { background: #0F766E; color: #fff; border: none; }
.btn-primary:hover:not(:disabled) {
  background: #115E59; transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-primary:disabled { opacity: .55; cursor: not-allowed; }
.btn-secondary { background: #fff; color: #475569; border: 1px solid #E2E8F0; }
.btn-secondary:hover:not(:disabled) { background: #F8FAFC; }
.btn-secondary:disabled { opacity: .55; cursor: not-allowed; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.fade-enter-active, .fade-leave-active { transition: opacity .25s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-up-enter-active, .slide-up-leave-active { transition: all .3s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateY(20px) scale(.98); }
@media (max-width: 640px) {
  .form-grid-2 { grid-template-columns: 1fr; }
  .modal-footer { flex-direction: column-reverse; }
  .modal-footer .btn-primary, .modal-footer .btn-secondary { width: 100%; }
}
</style>