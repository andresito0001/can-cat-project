<template>
  <Transition name="fade">
    <div v-if="visible" class="modal-overlay" @click.self="cerrar">
      <div class="modal-card">
        <header class="modal-header">
          <div>
            <h3>Editar Cliente</h3>
            <p class="modal-sub">Actualiza los datos del cliente</p>
          </div>
          <button class="close-btn" type="button" @click="cerrar"><X :size="18" /></button>
        </header>

        <form class="modal-body" @submit.prevent="submit">
          <div class="field">
            <label>Nombre completo *</label>
            <input v-model="form.nombreCompleto" type="text" required maxlength="150" />
          </div>

          <div class="row">
            <div class="field">
              <label>Documento de identidad *</label>
              <input v-model="form.documentoIdentidad" type="text" required maxlength="20" />
            </div>
            <div class="field">
              <label>Fecha de nacimiento</label>
              <input v-model="form.fechaNacimiento" type="date" />
            </div>
          </div>

          <div class="row">
            <div class="field">
              <label>Teléfono principal *</label>
              <input v-model="form.telefonoPrincipal" type="text" required maxlength="20" />
            </div>
            <div class="field">
              <label>Teléfono secundario</label>
              <input v-model="form.telefonoSecundario" type="text" maxlength="20" />
            </div>
          </div>

          <div class="field">
            <label>Dirección</label>
            <input v-model="form.direccion" type="text" maxlength="255" />
          </div>

          <div class="field">
            <label>Ciudad</label>
            <input v-model="form.ciudad" type="text" maxlength="50" />
          </div>

          <p v-if="error" class="error-msg">{{ error }}</p>

          <footer class="modal-footer">
            <button type="button" class="btn-ghost" @click="cerrar">Cancelar</button>
            <button type="submit" class="btn-primary" :disabled="guardando">
              {{ guardando ? 'Guardando…' : 'Guardar cambios' }}
            </button>
          </footer>
        </form>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { X } from 'lucide-vue-next'
import { actualizarCliente } from '@/api/admin.api'
import { getApiErrorMessage, getValidationFieldErrors } from '@/utils/apiError'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  cliente: { type: Object, required: true }, // ClienteDTO
})
const emit = defineEmits(['update:modelValue', 'guardado'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const form = ref({})
const guardando = ref(false)
const error = ref('')

watch(() => props.modelValue, (abierto) => {
  if (!abierto || !props.cliente) return
  error.value = ''
  form.value = {
    nombreCompleto: props.cliente.nombreCompleto || '',
    documentoIdentidad: props.cliente.documentoIdentidad || '',
    telefonoPrincipal: props.cliente.telefonoPrincipal || '',
    telefonoSecundario: props.cliente.telefonoSecundario || '',
    direccion: props.cliente.direccion || '',
    ciudad: props.cliente.ciudad || '',
    fechaNacimiento: props.cliente.fechaNacimiento || '',
  }
})

function cerrar() { visible.value = false }

async function submit() {
  error.value = ''
  guardando.value = true
  try {
    await actualizarCliente(props.cliente.id, {
      ...form.value,
      telefonoSecundario: form.value.telefonoSecundario || null,
      direccion: form.value.direccion || null,
      ciudad: form.value.ciudad || null,
      fechaNacimiento: form.value.fechaNacimiento || null,
    })
    emit('guardado')
    cerrar()
  } catch (err) {
    const fieldErrors = getValidationFieldErrors(err)
    error.value = fieldErrors
      ? Object.entries(fieldErrors).map(([k, v]) => `${k}: ${v}`).join(' · ')
      : getApiErrorMessage(err)
  } finally {
    guardando.value = false
  }
}
</script>

<style scoped>
.modal-overlay { position: fixed; inset: 0; background: rgba(15,23,42,.5); display: flex; align-items: center; justify-content: center; z-index: 100; padding: 20px; }
.modal-card { background: #fff; border-radius: 16px; width: 100%; max-width: 600px; max-height: 90vh; overflow-y: auto; box-shadow: 0 20px 50px -12px rgba(15,23,42,.3); }
.modal-header { display: flex; justify-content: space-between; align-items: flex-start; padding: 22px 26px; border-bottom: 1px solid #E2E8F0; }
.modal-header h3 { margin: 0; font-size: 17px; font-weight: 700; }
.modal-sub { margin: 4px 0 0; font-size: 12.5px; color: #64748B; }
.close-btn { width: 34px; height: 34px; border-radius: 8px; border: 1px solid #E2E8F0; background: #fff; color: #64748B; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.close-btn:hover { background: #F1F5F9; }
.modal-body { padding: 22px 26px 8px; display: flex; flex-direction: column; gap: 16px; }
.row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 12.5px; font-weight: 600; color: #334155; }
.field input { padding: 10px 12px; border: 1px solid #CBD5E1; border-radius: 9px; font-size: 13.5px; font-family: inherit; }
.field input:focus { outline: none; border-color: #0F766E; box-shadow: 0 0 0 3px rgba(15,118,110,.12); }
.error-msg { margin: 0; padding: 10px 14px; background: #FEF2F2; color: #B91C1C; border-radius: 8px; font-size: 12.5px; border: 1px solid #FECACA; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 16px 0 18px; }
.btn-ghost { padding: 10px 18px; background: #fff; border: 1px solid #CBD5E1; border-radius: 9px; font-size: 13.5px; font-weight: 600; color: #475569; cursor: pointer; font-family: inherit; }
.btn-ghost:hover { background: #F8FAFC; }
.btn-primary { padding: 10px 20px; background: #0F766E; border: none; border-radius: 9px; font-size: 13.5px; font-weight: 700; color: #fff; cursor: pointer; font-family: inherit; }
.btn-primary:hover { background: #0E6862; }
.btn-primary:disabled { opacity: .6; cursor: not-allowed; }
.fade-enter-active, .fade-leave-active { transition: opacity .18s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>