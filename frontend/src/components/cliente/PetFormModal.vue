<template>
  <AppModal
    :model-value="visible"
    :title="esEdicion ? 'Editar Mascota' : 'Registrar Nueva Mascota'"
    :subtitle="esEdicion ? 'Actualiza los datos de tu compañero' : 'Completa los datos de tu compañero'"
    size="lg"
    :loading="guardando"
    @update:model-value="$emit('close')"
  >
    <form @submit.prevent="guardar" class="pet-form" novalidate>
      <!-- Identificación -->
      <section class="form-section">
        <p class="section-eyebrow"><BadgeInfo :size="12" /> Identificación</p>

        <AppFormField
          label="Nombre"
          :error="''"
          required
        >
          <template #default="{ id }">
            <AppInput
              :id="id"
              ref="nombreInputRef"
              v-model="form.nombre"
              placeholder="Ej: Rocky"
              maxlength="50"
              :disabled="guardando"
              autocomplete="off"
            />
          </template>
        </AppFormField>

        <div class="form-grid-2">
          <AppFormField label="Especie" required>
            <template #default="{ id }">
              <AppSelect
                :id="id"
                v-model="form.idEspecie"
                :disabled="guardando"
              >
                <option value="" disabled>Seleccione...</option>
                <option v-for="esp in especiesLocal" :key="esp.id" :value="esp.id">
                  {{ esp.nombre }}
                </option>
              </AppSelect>
            </template>
          </AppFormField>

          <AppFormField label="Raza" optional>
            <template #default="{ id }">
              <AppSelect
                :id="id"
                v-model="form.idRaza"
                :disabled="!form.idEspecie || cargandoRazas || guardando"
              >
                <option value="">
                  {{ !form.idEspecie
                    ? 'Seleccione especie primero'
                    : (cargandoRazas ? 'Cargando razas…' : 'Seleccione...') }}
                </option>
                <option v-for="raza in razas" :key="raza.id" :value="raza.id">
                  {{ raza.nombre }}
                </option>
              </AppSelect>
            </template>
          </AppFormField>
        </div>
      </section>

      <!-- Detalles físicos -->
      <section class="form-section">
        <p class="section-eyebrow"><Ruler :size="12" /> Detalles físicos</p>

        <div class="form-grid-2">
          <AppFormField label="Sexo" required>
            <div class="segmented">
              <label class="segment" :class="{ active: form.sexo === 'M' }">
                <input type="radio" value="M" v-model="form.sexo" hidden />
                <Mars :size="15" /> Macho
              </label>
              <label class="segment" :class="{ active: form.sexo === 'H' }">
                <input type="radio" value="H" v-model="form.sexo" hidden />
                <Venus :size="15" /> Hembra
              </label>
            </div>
          </AppFormField>

          <AppFormField label="Fecha de nacimiento" optional>
            <template #default="{ id }">
              <DatePicker
                :id="id"
                v-model="form.fechaNacimiento"
                placeholder="Seleccionar fecha"
                :max="hoy"
                :disabled="guardando"
              />
            </template>
          </AppFormField>
        </div>

        <div class="form-grid-2">
          <AppFormField label="Color / particularidades" optional>
            <template #default="{ id }">
              <AppInput
                :id="id"
                v-model="form.color"
                placeholder="Ej: Marrón con manchas blancas"
                maxlength="30"
                :disabled="guardando"
              />
            </template>
          </AppFormField>

          <AppFormField
            label="Peso actual"
            optional
            :error="errorPeso"
            hint="En kilogramos"
          >
            <template #default="{ id, invalid }">
              <AppInput
                :id="id"
                v-model="form.pesoActual"
                type="number"
                step="0.01"
                min="0.01"
                max="999.99"
                placeholder="12.50"
                :error="invalid"
                :disabled="guardando"
              />
            </template>
          </AppFormField>
        </div>
      </section>

      <!-- Salud -->
      <section class="form-section">
        <p class="section-eyebrow"><Heart :size="12" /> Salud</p>
        <label class="toggle-row">
          <div class="toggle-text">
            <span class="toggle-title">¿Está esterilizado/a?</span>
            <span class="toggle-desc">Reduce el riesgo de ciertas enfermedades</span>
          </div>
          <span class="switch" :class="{ on: form.esterilizado }">
            <input type="checkbox" v-model="form.esterilizado" hidden />
            <span class="switch-knob" />
          </span>
        </label>
      </section>

      <Transition name="slide-down">
        <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
      </Transition>
    </form>

    <template #footer>
      <AppButton variant="secondary" :disabled="guardando" @click="$emit('close')">
        Cancelar
      </AppButton>
      <AppButton
        variant="primary"
        :loading="guardando"
        :disabled="!puedeGuardar"
        @click="guardar"
      >
        <template #icon-left><Save :size="16" /></template>
        {{ guardando ? 'Guardando…' : (esEdicion ? 'Guardar cambios' : 'Guardar Mascota') }}
      </AppButton>
    </template>
  </AppModal>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import {
  Save, BadgeInfo, Ruler, Heart, Mars, Venus,
} from 'lucide-vue-next'
import {
  registrarMascota, actualizarMascota,
  getEspecies, getRazasPorEspecie,
} from '@/api/mascotas.api.js'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

import AppModal from '@/components/ui/AppModal.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppFormField from '@/components/ui/AppFormField.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppSelect from '@/components/ui/AppSelect.vue'
import AppAlert from '@/components/ui/AppAlert.vue'

import DatePicker from '@/components/ui/DatePicker.vue'
import { hoyISO } from '@/utils/fecha'

const hoy = new Date().toISOString().split('T')[0]

const props = defineProps({
  visible: { type: Boolean, default: false },
  mascota: { type: Object, default: null },
  especies: { type: Array, default: () => [] },
})
const emit = defineEmits(['close', 'saved'])

const { toastSuccess } = useToast()

const nombreInputRef = ref(null)
const guardando = ref(false)
const error = ref('')
const errorPeso = ref('')
const razas = ref([])
const cargandoRazas = ref(false)
const especiesLocal = ref(props.especies || [])

const esEdicion = computed(() => !!props.mascota?.idMascota)

const form = ref(formVacio())
const puedeGuardar = computed(
  () => form.value.nombre.trim().length > 0
    && form.value.idEspecie !== ''
    && form.value.sexo !== ''
)

function formVacio() {
  return {
    nombre: '',
    idEspecie: '',
    idRaza: '',
    fechaNacimiento: '',
    sexo: '',
    color: '',
    pesoActual: '',
    esterilizado: false,
  }
}

watch(() => props.visible, async (v) => {
  if (!v) return
  error.value = ''
  errorPeso.value = ''

  if (especiesLocal.value.length === 0) {
    try {
      const { data } = await getEspecies()
      especiesLocal.value = data
    } catch { /* ignore */ }
  }

  if (esEdicion.value) {
    const m = props.mascota
    form.value = {
      nombre: m.nombre || '',
      idEspecie: m.idEspecie ?? '',
      idRaza: m.idRaza ?? '',
      fechaNacimiento: m.fechaNacimiento || '',
      sexo: m.sexo || '',
      color: m.color || '',
      pesoActual: m.pesoActual ?? '',
      esterilizado: !!m.esterilizado,
    }
    if (form.value.idEspecie) await cargarRazas(form.value.idEspecie)
  } else {
    form.value = formVacio()
    razas.value = []
    nextTick(() => nombreInputRef.value?.focus?.())
  }
})

watch(() => form.value.idEspecie, async (nuevoId, viejoId) => {
  if (nuevoId === viejoId) return
  form.value.idRaza = ''
  await cargarRazas(nuevoId)
})

async function cargarRazas(idEspecie) {
  razas.value = []
  if (!idEspecie) return
  cargandoRazas.value = true
  try {
    const { data } = await getRazasPorEspecie(idEspecie)
    razas.value = data
  } catch { /* ignore */ }
  finally { cargandoRazas.value = false }
}

function validarPeso() {
  errorPeso.value = ''
  const raw = form.value.pesoActual
  if (raw === '' || raw == null) return true
  const n = Number(raw)
  if (Number.isNaN(n)) { errorPeso.value = 'Ingresa un número válido'; return false }
  if (n <= 0) { errorPeso.value = 'El peso debe ser mayor a 0'; return false }
  if (n > 999.99) { errorPeso.value = 'El peso no puede superar 999.99 kg'; return false }
  return true
}

async function guardar() {
  error.value = ''
  if (!validarPeso()) return
  if (!puedeGuardar.value) return

  guardando.value = true
  try {
    const payload = {
      idEspecie: parseInt(form.value.idEspecie),
      idRaza: form.value.idRaza ? parseInt(form.value.idRaza) : null,
      nombre: form.value.nombre.trim(),
      fechaNacimiento: form.value.fechaNacimiento || null,
      sexo: form.value.sexo,
      color: form.value.color.trim() || null,
      pesoActual: form.value.pesoActual ? parseFloat(form.value.pesoActual) : null,
      esterilizado: form.value.esterilizado,
    }

    if (esEdicion.value) {
      const { data } = await actualizarMascota(props.mascota.idMascota, payload)
      toastSuccess('¡Mascota actualizada!')
      emit('saved', data)
    } else {
      const { data } = await registrarMascota(payload)
      toastSuccess('¡Mascota registrada exitosamente!')
      emit('saved', data)
    }
    emit('close')
  } catch (err) {
    error.value = getApiErrorMessage(err) || 'No se pudo guardar la mascota'
  } finally {
    guardando.value = false
  }
}
</script>

<style scoped>
.pet-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding-bottom: var(--space-5);
  border-bottom: 1px solid var(--border-subtle);
}
.form-section:last-of-type { border-bottom: none; padding-bottom: 0; }

.section-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin: 0;
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--brand-700);
}

.form-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}
.form-grid-2 :deep(.form-group) { margin-bottom: 0; }
.pet-form :deep(.form-group) { margin-bottom: 0; }

/* Segmented (sexo) */
.segmented {
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: var(--neutral-100);
  border-radius: var(--radius-lg);
  padding: var(--space-1);
  gap: var(--space-1);
}
.segment {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-md);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  user-select: none;
}
.segment.active {
  background: var(--bg-surface);
  color: var(--brand-700);
  box-shadow: var(--shadow-xs);
}

/* Toggle (esterilizado) */
.toggle-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  cursor: pointer;
}
.toggle-text { display: flex; flex-direction: column; gap: 2px; }
.toggle-title { font-size: var(--text-base); font-weight: var(--font-semibold); color: var(--text-primary); }
.toggle-desc { font-size: var(--text-sm); color: var(--text-secondary); }

.switch {
  position: relative;
  width: 40px;
  height: 22px;
  flex-shrink: 0;
  background: var(--neutral-300);
  border-radius: var(--radius-full);
  transition: background-color var(--duration-base) var(--ease-out);
}
.switch.on { background: var(--brand-700); }
.switch-knob {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 16px;
  height: 16px;
  background: var(--bg-surface);
  border-radius: 50%;
  transition: transform var(--duration-base) var(--ease-out);
}
.switch.on .switch-knob { transform: translateX(18px); }

@media (max-width: 640px) {
  .form-grid-2 { grid-template-columns: 1fr; }
}
</style>