<template>
  <AppModal
    :model-value="visible"
    :title="modoEdicion ? 'Editar Personal' : 'Registrar Nuevo Personal'"
    subtitle="Los datos marcados con * son obligatorios"
    size="lg"
    :loading="guardando"
    @update:model-value="cerrar"
  >
    <form class="personal-form" @submit.prevent="submit">
      <!-- Correo / Contraseña (solo creación) -->
      <template v-if="!modoEdicion">
        <AppFormField label="Correo electrónico" required>
          <template #default="{ id }">
            <AppInput
              :id="id"
              v-model="form.correoElectronico"
              type="email"
              placeholder="usuario@cancat.com"
              autocomplete="off"
            />
          </template>
        </AppFormField>

        <AppFormField label="Contraseña temporal" required hint="Mínimo 6 caracteres">
          <template #default="{ id }">
            <AppPasswordField
              :id="id"
              v-model="form.contrasena"
              placeholder="Mínimo 6 caracteres"
              autocomplete="new-password"
            />
          </template>
        </AppFormField>
      </template>

      <!-- Nombre completo -->
      <AppFormField label="Nombre completo" required>
        <template #default="{ id }">
          <AppInput
            :id="id"
            v-model="form.nombreCompleto"
            maxlength="150"
            :autofocus="modoEdicion"
          />
        </template>
      </AppFormField>

      <!-- Código + Cargo -->
      <div class="form-grid-2">
        <AppFormField
          label="Código de empleado"
          :hint="modoEdicion ? 'Editable al modificar' : 'Se asignará un código único según el cargo'"
        >
          <template #default="{ id }">
            <AppInput
              :id="id"
              v-model="form.codigoEmpleado"
              maxlength="20"
              placeholder="Se generará automáticamente"
              :disabled="!modoEdicion"
            />
          </template>
        </AppFormField>

        <AppFormField label="Cargo" required>
          <template #default="{ id }">
            <AppSelect :id="id" v-model="form.cargo" required>
              <option value="" disabled>Seleccione…</option>
              <option value="VETERINARIO">Veterinario</option>
              <option value="RECEPCIONISTA">Recepcionista</option>
              <option value="ENCARGADO_ALMACEN">Encargado de Almacén</option>
              <option value="ADMINISTRADOR">Administrador</option>
            </AppSelect>
          </template>
        </AppFormField>
      </div>

      <!-- Fecha + Especialidad -->
      <div class="form-grid-2">
        <AppFormField label="Fecha de contratación" required>
          <template #default="{ id }">
            <DatePicker
              v-model="form.fechaContratacion"
              placeholder="Seleccionar fecha"
              :max="hoy"
            />
          </template>
        </AppFormField>

        <AppFormField label="Especialidad (solo veterinarios)" optional>
          <template #default="{ id }">
            <AppSelect
              :id="id"
              v-model="form.especialidad"
              :disabled="form.cargo !== 'VETERINARIO'"
            >
              <option value="">— Ninguna —</option>
              <option value="Consulta">Consulta</option>
              <option value="Vacunacion">Vacunación</option>
              <option value="Cirugia">Cirugía</option>
              <option value="Estetica">Estética</option>
            </AppSelect>
          </template>
        </AppFormField>
      </div>

      <!-- Licencia -->
      <AppFormField label="Licencia profesional" optional>
        <template #default="{ id }">
          <AppInput :id="id" v-model="form.licenciaProfesional" maxlength="50" />
        </template>
      </AppFormField>

      <!-- Horario -->
      <AppFormField label="Horario de atención" optional>
        <HorarioAtencionEditor v-model="form.horarioAtencion" />
      </AppFormField>

      <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
    </form>

    <template #footer>
      <AppButton variant="secondary" :disabled="guardando" @click="cerrar">
        Cancelar
      </AppButton>
      <AppButton
        variant="primary"
        :loading="guardando"
        @click="submit"
      >
        {{ guardando
          ? 'Guardando…'
          : (modoEdicion ? 'Guardar cambios' : 'Crear personal') }}
      </AppButton>
    </template>
  </AppModal>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import HorarioAtencionEditor from './HorarioAtencionEditor.vue'
import { crearPersonal, actualizarPersonal } from '@/api/admin.api'
import { getApiErrorMessage, getValidationFieldErrors } from '@/utils/apiError'

import AppModal from '@/components/ui/AppModal.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppFormField from '@/components/ui/AppFormField.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppSelect from '@/components/ui/AppSelect.vue'
import AppPasswordField from '@/components/ui/AppPasswordField.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import DatePicker from '@/components/ui/DatePicker.vue'
import { hoyISO } from '@/utils/fecha'

const hoy = hoyISO()

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  personal:   { type: Object, default: null },
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
  form.value = props.personal
    ? {
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
    : formInicial()
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
.personal-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.form-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}
.form-grid-2 :deep(.form-group) { margin-bottom: 0; }
.personal-form :deep(.form-group) { margin-bottom: 0; }

@media (max-width: 640px) {
  .form-grid-2 { grid-template-columns: 1fr; }
}
</style>