<template>
  <Transition name="fade">
    <div v-if="visible" class="modal-overlay" @click.self="cerrar">
      <div class="modal-card">
        <header class="modal-header">
          <div>
            <h3>{{ modoEdicion ? 'Editar Personal' : 'Registrar Nuevo Personal' }}</h3>
            <p class="modal-sub">Los datos marcados con * son obligatorios</p>
          </div>
          <button class="close-btn" type="button" @click="cerrar"><X :size="18" /></button>
        </header>

        <form class="modal-body" @submit.prevent="submit">
          <div v-if="!modoEdicion" class="field">
            <label>Correo electrónico *</label>
            <input v-model="form.correoElectronico" type="email" required
                   placeholder="usuario@cancat.com" />
          </div>

          <div v-if="!modoEdicion" class="field">
            <label>Contraseña temporal *</label>
            <input v-model="form.contrasena" type="password" required minlength="6"
                   placeholder="Mínimo 6 caracteres" />
          </div>

          <div class="field">
            <label>Nombre completo *</label>
            <input v-model="form.nombreCompleto" type="text" required maxlength="150" />
          </div>

          <div class="row">
            <div class="field">
              <label>Código de empleado</label>
              <input v-model="form.codigoEmpleado" type="text" maxlength="20"
                     placeholder="Se generará automáticamente"
                     :disabled="!modoEdicion" />
              <small class="hint">
                {{ modoEdicion
                    ? 'Editable al modificar'
                    : 'Se asignará un código único según el cargo (ej. VET-001)' }}
              </small>
            </div>

            <div class="field">
              <label>Cargo *</label>
              <select v-model="form.cargo" required>
                <option value="" disabled>Seleccione…</option>
                <option value="VETERINARIO">Veterinario</option>
                <option value="RECEPCIONISTA">Recepcionista</option>
                <option value="ENCARGADO_ALMACEN">Encargado de Almacén</option>
                <option value="ADMINISTRADOR">Administrador</option>
              </select>
            </div>
          </div>

          <div class="row">
            <div class="field">
              <label>Fecha de contratación *</label>
              <input v-model="form.fechaContratacion" type="date" required />
            </div>

            <div class="field">
              <label>Especialidad (solo veterinarios)</label>
              <select v-model="form.especialidad"
                      :disabled="form.cargo !== 'VETERINARIO'">
                <option value="">— Ninguna —</option>
                <option value="Consulta">Consulta</option>
                <option value="Vacunacion">Vacunación</option>
                <option value="Cirugia">Cirugía</option>
                <option value="Estetica">Estética</option>
              </select>
            </div>
          </div>

          <div class="field">
            <label>Licencia profesional</label>
            <input v-model="form.licenciaProfesional" type="text" maxlength="50" />
          </div>

          <!-- ─── Horario de atención ─── -->
          <HorarioAtencionEditor v-model="form.horarioAtencion" />

          <p v-if="error" class="error-msg">{{ error }}</p>

          <footer class="modal-footer">
            <button type="button" class="btn-ghost" @click="cerrar">Cancelar</button>
            <button type="submit" class="btn-primary" :disabled="guardando">
              {{ guardando ? 'Guardando…' : (modoEdicion ? 'Guardar cambios' : 'Crear personal') }}
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
import HorarioAtencionEditor from './HorarioAtencionEditor.vue'
import { crearPersonal, actualizarPersonal } from '@/api/admin.api'
import { getApiErrorMessage, getValidationFieldErrors } from '@/utils/apiError'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  personal: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue', 'guardado'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const modoEdicion = computed(() => !!props.personal?.personalId)

const form = ref(formInicial())
const guardando = ref(false)
const error = ref('')

function formInicial() {
  return {
    correoElectronico: '',
    contrasena: '',
    nombreCompleto: '',
    codigoEmpleado: '',
    cargo: '',
    especialidad: '',
    fechaContratacion: '',
    licenciaProfesional: '',
    horarioAtencion: horarioVacio(),
  }
}

function horarioVacio() {
  const h = {}
  for (let d = 1; d <= 7; d++) h[String(d)] = []
  return h
}

watch(() => props.modelValue, (abierto) => {
  if (!abierto) return
  error.value = ''
  if (props.personal) {
    form.value = {
      correoElectronico: props.personal.correoElectronico || '',
      contrasena: '',
      nombreCompleto: props.personal.nombreCompleto || '',
      codigoEmpleado: props.personal.codigoEmpleado || '',
      cargo: props.personal.cargo || '',
      especialidad: props.personal.especialidad || '',
      fechaContratacion: props.personal.fechaContratacion || '',
      licenciaProfesional: props.personal.licenciaProfesional || '',
      horarioAtencion: props.personal.horarioAtencion || horarioVacio(),
    }
  } else {
    form.value = formInicial()
  }
})

function cerrar() { visible.value = false }

async function submit() {
  error.value = ''
  guardando.value = true
  try {
    if (modoEdicion.value) {
      await actualizarPersonal(props.personal.personalId, {
        nombreCompleto: form.value.nombreCompleto,
        codigoEmpleado: form.value.codigoEmpleado,
        cargo: form.value.cargo,
        especialidad: form.value.especialidad || null,
        fechaContratacion: form.value.fechaContratacion,
        licenciaProfesional: form.value.licenciaProfesional || null,
        horarioAtencion: form.value.horarioAtencion,
      })
    } else {
      await crearPersonal({
        correoElectronico: form.value.correoElectronico,
        contrasena: form.value.contrasena,
        nombreCompleto: form.value.nombreCompleto,
        cargo: form.value.cargo,
        especialidad: form.value.especialidad || null,
        fechaContratacion: form.value.fechaContratacion,
        horarioAtencion: form.value.horarioAtencion,
      })
    }
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
.modal-card { background: #fff; border-radius: 16px; width: 100%; max-width: 720px; max-height: 90vh; overflow-y: auto; box-shadow: 0 20px 50px -12px rgba(15,23,42,.3); }
.modal-header { display: flex; justify-content: space-between; align-items: flex-start; padding: 22px 26px; border-bottom: 1px solid #E2E8F0; position: sticky; top: 0; background: #fff; z-index: 1; }
.modal-header h3 { margin: 0; font-size: 17px; font-weight: 700; color: #0F172A; }
.modal-sub { margin: 4px 0 0; font-size: 12.5px; color: #64748B; }
.close-btn { width: 34px; height: 34px; border-radius: 8px; border: 1px solid #E2E8F0; background: #fff; color: #64748B; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.close-btn:hover { background: #F1F5F9; }
.modal-body { padding: 22px 26px 8px; display: flex; flex-direction: column; gap: 16px; }
.row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 12.5px; font-weight: 600; color: #334155; }
.field input, .field select { padding: 10px 12px; border: 1px solid #CBD5E1; border-radius: 9px; font-size: 13.5px; font-family: inherit; color: #0F172A; background: #fff; }
.field input:focus, .field select:focus { outline: none; border-color: #0F766E; box-shadow: 0 0 0 3px rgba(15,118,110,.12); }
.field select:disabled, .field input:disabled { background: #F1F5F9; color: #94A3B8; cursor: not-allowed; }
.hint { font-size: 11.5px; color: #94A3B8; }
.error-msg { margin: 0; padding: 10px 14px; background: #FEF2F2; color: #B91C1C; border-radius: 8px; font-size: 12.5px; border: 1px solid #FECACA; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 16px 0 18px; position: sticky; bottom: 0; background: #fff; }
.btn-ghost { padding: 10px 18px; background: #fff; border: 1px solid #CBD5E1; border-radius: 9px; font-size: 13.5px; font-weight: 600; color: #475569; cursor: pointer; font-family: inherit; }
.btn-ghost:hover { background: #F8FAFC; }
.btn-primary { padding: 10px 20px; background: #0F766E; border: none; border-radius: 9px; font-size: 13.5px; font-weight: 700; color: #fff; cursor: pointer; font-family: inherit; }
.btn-primary:hover { background: #0E6862; }
.btn-primary:disabled { opacity: .6; cursor: not-allowed; }
.fade-enter-active, .fade-leave-active { transition: opacity .18s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
@media (max-width: 560px) { .row { grid-template-columns: 1fr; } }
</style>