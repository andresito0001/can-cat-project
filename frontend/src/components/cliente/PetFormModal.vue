<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="modal-overlay" @click.self="cerrar">
        <Transition name="slide-up" appear>
          <div v-if="visible" class="modal-container" role="dialog" aria-modal="true">
            <header class="modal-header">
              <div class="modal-header-left">
                <div class="modal-icon"><PawPrint :size="20" /></div>
                <div>
                  <h3>{{ esEdicion ? 'Editar Mascota' : 'Registrar Nueva Mascota' }}</h3>
                  <p class="modal-sub">
                    {{ esEdicion ? 'Actualiza los datos de tu compañero' : 'Completa los datos de tu compañero' }}
                  </p>
                </div>
              </div>
              <button class="btn-close" type="button" :disabled="guardando" @click="cerrar">
                <X :size="20" />
              </button>
            </header>

            <form @submit.prevent="guardar" class="modal-body" novalidate>
              <!-- Identificación -->
              <section class="form-section">
                <header class="section-header"><BadgeInfo :size="14" /><span>Identificación</span></header>

                <div class="form-group">
                  <label for="nombre">
                    Nombre <span class="required">*</span>
                    <span class="char-count">{{ form.nombre.length }}/50</span>
                  </label>
                  <input
                    id="nombre"
                    ref="nombreInput"
                    v-model="form.nombre"
                    type="text"
                    maxlength="50"
                    placeholder="Ej: Rocky"
                    autocomplete="off"
                    required
                  />
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label for="especie">Especie <span class="required">*</span></label>
                    <select id="especie" v-model="form.idEspecie" required>
                      <option value="" disabled>Seleccione...</option>
                      <option v-for="esp in especies" :key="esp.id" :value="esp.id">
                        {{ esp.nombre }}
                      </option>
                    </select>
                  </div>
                  <div class="form-group">
                    <label for="raza">Raza <span class="optional">(opcional)</span></label>
                    <select id="raza" v-model="form.idRaza" :disabled="!form.idEspecie || cargandoRazas">
                      <option value="">Seleccione...</option>
                      <option v-for="raza in razas" :key="raza.id" :value="raza.id">
                        {{ raza.nombre }}
                      </option>
                    </select>
                    <small v-if="form.idEspecie && razas.length === 0 && !cargandoRazas" class="hint hint-warn">
                      No hay razas para esta especie
                    </small>
                  </div>
                </div>
              </section>

              <!-- Detalles físicos -->
              <section class="form-section">
                <header class="section-header"><Ruler :size="14" /><span>Detalles físicos</span></header>

                <div class="form-row">
                  <div class="form-group">
                    <label>Sexo <span class="required">*</span></label>
                    <div class="segmented">
                      <label class="segment" :class="{ active: form.sexo === 'M' }">
                        <input type="radio" value="M" v-model="form.sexo" required hidden />
                        <Mars :size="15" /> Macho
                      </label>
                      <label class="segment" :class="{ active: form.sexo === 'H' }">
                        <input type="radio" value="H" v-model="form.sexo" required hidden />
                        <Venus :size="15" /> Hembra
                      </label>
                    </div>
                  </div>
                  <div class="form-group">
                    <label for="fechaNacimiento">Fecha de nacimiento</label>
                    <input id="fechaNacimiento" v-model="form.fechaNacimiento" type="date" :max="hoy" />
                  </div>
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label for="color">Color / particularidades</label>
                    <input id="color" v-model="form.color" type="text" maxlength="30"
                           placeholder="Ej: Marrón con manchas blancas" />
                  </div>
                  <div class="form-group">
                    <label for="peso">Peso actual <span class="optional">(kg)</span></label>
                    <div class="input-suffix">
                      <input
                        id="peso"
                        v-model="form.pesoActual"
                        type="number"
                        step="0.01"
                        min="0.01"
                        max="999.99"
                        placeholder="12.50"
                        :class="{ 'is-invalid': errorPeso }"
                      />
                      <span class="suffix">kg</span>
                    </div>
                    <small v-if="errorPeso" class="form-error">{{ errorPeso }}</small>
                  </div>
                </div>
              </section>

              <!-- Salud -->
              <section class="form-section">
                <header class="section-header"><Heart :size="14" /><span>Salud</span></header>
                <label class="toggle-row">
                  <div class="toggle-text">
                    <span class="toggle-title">¿Está esterilizado/a?</span>
                    <span class="toggle-desc">Reduce el riesgo de ciertas enfermedades</span>
                  </div>
                  <span class="switch" :class="{ on: form.esterilizado }">
                    <input type="checkbox" v-model="form.esterilizado" hidden />
                    <span class="switch-knob"></span>
                  </span>
                </label>
              </section>

              <Transition name="slide-down">
                <div v-if="error" class="alert-error">
                  <AlertCircle :size="16" /><span>{{ error }}</span>
                </div>
              </Transition>

              <div class="modal-footer">
                <button type="button" class="btn-secondary" :disabled="guardando" @click="cerrar">
                  Cancelar
                </button>
                <button type="submit" class="btn-primary" :disabled="guardando || !puedeGuardar">
                  <Loader2 v-if="guardando" :size="18" class="spin" />
                  <Save v-else :size="18" />
                  {{ guardando ? 'Guardando...' : (esEdicion ? 'Guardar cambios' : 'Guardar Mascota') }}
                </button>
              </div>
            </form>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import {
  PawPrint, X, Save, Loader2, AlertCircle,
  BadgeInfo, Ruler, Heart, Mars, Venus,
} from 'lucide-vue-next'
import {
  registrarMascota, actualizarMascota,
  getEspecies, getRazasPorEspecie,
} from '@/api/mascotas.api.js'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

const props = defineProps({
  visible: { type: Boolean, default: false },
  mascota: { type: Object, default: null },  // si viene → modo edición
  especies: { type: Array, default: () => [] },  // catálogo cacheado del padre
})
const emit = defineEmits(['close', 'saved'])

const { toastSuccess, toastError } = useToast()

const hoy = new Date().toISOString().split('T')[0]
const nombreInput = ref(null)
const guardando = ref(false)
const error = ref('')
const errorPeso = ref('')
const razas = ref([])
const cargandoRazas = ref(false)
const especiesLocal = ref(props.especies || [])

const esEdicion = computed(() => !!props.mascota?.idMascota)

const form = ref(formVacio())
const puedeGuardar = computed(() =>
  form.value.nombre.trim().length > 0 &&
  form.value.idEspecie !== '' &&
  form.value.sexo !== ''
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

// Cargar al abrir
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
    if (form.value.idEspecie) {
      await cargarRazas(form.value.idEspecie)
    }
  } else {
    form.value = formVacio()
    razas.value = []
    nextTick(() => nombreInput.value?.focus())
  }
})

// Cambio de especie → cargar razas
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
  finally {
    cargandoRazas.value = false
  }
}

function cerrar() {
  if (guardando.value) return
  emit('close')
}

function validarPeso() {
  errorPeso.value = ''
  const raw = form.value.pesoActual
  if (raw === '' || raw == null) return true
  const n = Number(raw)
  if (Number.isNaN(n)) {
    errorPeso.value = 'Ingresa un número válido'
    return false
  }
  if (n <= 0) {
    errorPeso.value = 'El peso debe ser mayor a 0'
    return false
  }
  if (n > 999.99) {
    errorPeso.value = 'El peso no puede superar 999.99 kg'
    return false
  }
  return true
}

async function guardar() {
  error.value = ''
  if (!validarPeso()) return

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
    cerrar()
  } catch (err) {
    error.value = getApiErrorMessage(err) || 'No se pudo guardar la mascota'
  } finally {
    guardando.value = false
  }
}
</script>

<style scoped>
/* Reusa los estilos del antiguo MyPetsView modal — se copian aquí */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  padding: 24px; z-index: 100;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}
.modal-container {
  background: #fff; border-radius: 18px; width: 100%; max-width: 640px;
  max-height: 92vh; overflow-y: auto;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  display: flex; flex-direction: column;
}
.modal-header {
  padding: 20px 24px; border-bottom: 1px solid #E2E8F0;
  display: flex; align-items: flex-start; justify-content: space-between;
  position: sticky; top: 0; background: #fff; z-index: 2;
  border-radius: 18px 18px 0 0;
}
.modal-header-left { display: flex; gap: 14px; align-items: center; }
.modal-icon {
  width: 42px; height: 42px; border-radius: 12px;
  background: #F0FDFA; color: #0F766E;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.modal-header h3 { font-size: 17px; font-weight: 700; color: #1E293B; margin: 0; }
.modal-sub { font-size: 12.5px; color: #64748B; margin: 2px 0 0 0; }
.btn-close {
  width: 36px; height: 36px; border-radius: 10px; border: none;
  background: #F1F5F9; color: #64748B;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.2s; flex-shrink: 0;
}
.btn-close:hover:not(:disabled) { background: #E2E8F0; }
.btn-close:disabled { opacity: 0.5; cursor: not-allowed; }

.modal-body { padding: 20px 24px 24px; display: flex; flex-direction: column; gap: 18px; }
.form-section { display: flex; flex-direction: column; gap: 14px; }
.section-header {
  display: flex; align-items: center; gap: 6px;
  font-size: 11px; font-weight: 700; text-transform: uppercase;
  letter-spacing: 0.6px; color: #64748B;
  padding-bottom: 6px; border-bottom: 1px dashed #E2E8F0;
}
.section-header svg { color: #94A3B8; }

.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label {
  display: flex; align-items: center; justify-content: space-between;
  font-size: 12.5px; font-weight: 600; color: #475569;
}
.required { color: #EF4444; margin-left: 2px; }
.optional { font-weight: 400; color: #94A3B8; font-size: 11px; }
.char-count { font-size: 10.5px; color: #94A3B8; font-family: monospace; }

.form-group input[type="text"],
.form-group input[type="date"],
.form-group input[type="number"],
.form-group select {
  width: 100%; padding: 10px 14px;
  border: 1px solid #E2E8F0; border-radius: 10px;
  font-size: 14px; color: #1E293B; background: #fff;
  transition: border-color .2s, box-shadow .2s;
  font-family: inherit; box-sizing: border-box; line-height: 1.5;
}
.form-group input:focus, .form-group select:focus {
  outline: none; border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.1);
}
.form-group select:disabled { background: #F8FAFC; color: #94A3B8; }
.form-group input[type="number"]::-webkit-outer-spin-button,
.form-group input[type="number"]::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }
.form-group input[type="number"] { -moz-appearance: textfield; }
.is-invalid { border-color: #EF4444 !important; background: #FEF2F2 !important; }

.hint { font-size: 11px; color: #94A3B8; margin-top: 2px; }
.hint-warn { color: #B45309; }
.form-error { font-size: 11.5px; color: #EF4444; font-weight: 600; margin-top: 2px; }

.input-suffix { position: relative; display: flex; align-items: center; }
.input-suffix input { padding-right: 44px; }
.input-suffix .suffix {
  position: absolute; right: 12px; font-size: 12px; font-weight: 700;
  color: #94A3B8; pointer-events: none;
}

.segmented {
  display: grid; grid-template-columns: 1fr 1fr;
  background: #F1F5F9; border-radius: 10px; padding: 4px; gap: 4px;
}
.segment {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 8px 12px; border-radius: 7px;
  font-size: 13px; font-weight: 600; color: #64748B;
  cursor: pointer; transition: all .15s; user-select: none;
}
.segment.active { background: #fff; color: #0F766E; box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08); }

.toggle-row {
  display: flex; align-items: center; justify-content: space-between;
  gap: 14px; padding: 12px 14px;
  background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 12px;
  cursor: pointer;
}
.toggle-text { display: flex; flex-direction: column; gap: 2px; }
.toggle-title { font-size: 13.5px; font-weight: 600; color: #1E293B; }
.toggle-desc { font-size: 11.5px; color: #64748B; }
.switch {
  position: relative; width: 40px; height: 22px; flex-shrink: 0;
  background: #CBD5E1; border-radius: 22px; transition: background-color .2s;
}
.switch.on { background: #0F766E; }
.switch-knob {
  position: absolute; top: 3px; left: 3px;
  width: 16px; height: 16px; background: #fff; border-radius: 50%;
  transition: transform .2s;
}
.switch.on .switch-knob { transform: translateX(18px); }

.alert-error {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 12px 14px; background: #FEF2F2; color: #B91C1C;
  border-radius: 10px; font-size: 13px; font-weight: 500;
  border: 1px solid #FECACA; line-height: 1.45;
}

.modal-footer {
  display: flex; justify-content: flex-end; gap: 12px;
  margin-top: 4px; padding-top: 18px; border-top: 1px solid #E2E8F0;
  position: sticky; bottom: 0; background: #fff; padding-bottom: 4px;
}
.btn-primary {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 10px 20px; background: #0F766E; color: #fff;
  border: none; border-radius: 10px; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all 0.2s; font-family: inherit;
}
.btn-primary:hover:not(:disabled) { background: #115E59; }
.btn-primary:disabled { opacity: 0.55; cursor: not-allowed; }
.btn-secondary {
  padding: 10px 20px; background: #F1F5F9; color: #475569;
  border: 1px solid #E2E8F0; border-radius: 10px; font-size: 14px;
  font-weight: 600; cursor: pointer; font-family: inherit;
}
.btn-secondary:hover:not(:disabled) { background: #E2E8F0; }
.btn-secondary:disabled { opacity: 0.55; cursor: not-allowed; }

.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.fade-enter-active, .fade-leave-active { transition: opacity .25s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-up-enter-active, .slide-up-leave-active { transition: all .3s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateY(24px) scale(.97); }
.slide-down-enter-active, .slide-down-leave-active { transition: all .2s ease; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-6px); max-height: 0; }

@media (max-width: 640px) {
  .form-row { grid-template-columns: 1fr; }
  .modal-footer { flex-direction: column-reverse; }
  .modal-footer .btn-primary, .modal-footer .btn-secondary { width: 100%; justify-content: center; }
}
</style>