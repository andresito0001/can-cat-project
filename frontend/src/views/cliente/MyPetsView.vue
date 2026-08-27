<template>
  <div class="mascotas-view">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2>Mis Mascotas</h2>
        <p class="subtitle">Gestiona y registra las mascotas asociadas a tu cuenta</p>
      </div>
      <button class="btn-primary" @click="abrirModal">
        <Plus :size="18" />
        Nueva Mascota
      </button>
    </div>

    <!-- Lista de Mascotas -->
    <div v-if="cargando" class="loading-state">
      <Loader2 :size="32" class="spin" />
      <p>Cargando mascotas...</p>
    </div>

    <div v-else-if="mascotas.length === 0" class="empty-state">
      <PawPrint :size="48" />
      <h3>No tienes mascotas registradas</h3>
      <p>Registra tu primera mascota para comenzar a gestionar su historial clínico.</p>
      <button class="btn-primary" @click="abrirModal">
        <Plus :size="18" />
        Registrar Mascota
      </button>
    </div>

    <div v-else class="mascotas-grid">
      <div v-for="mascota in mascotas" :key="mascota.id" class="mascota-card">
        <div class="mascota-avatar" :style="{ backgroundColor: getAvatarColor(mascota.nombre) }">
          {{ mascota.nombre[0].toUpperCase() }}
        </div>
        <div class="mascota-info">
          <h4>{{ mascota.nombre }}</h4>
          <p class="mascota-meta">
            <span class="badge-especie">{{ getEspecieLabel(mascota.idEspecie) }}</span>
            <span v-if="mascota.sexo" class="badge-sexo">{{ mascota.sexo === 'M' ? 'Macho' : 'Hembra' }}</span>
          </p>
          <p v-if="mascota.fechaNacimiento" class="mascota-detail">
            <Calendar :size="14" />
            {{ formatFecha(mascota.fechaNacimiento) }}
          </p>
          <p v-if="mascota.pesoActual" class="mascota-detail">
            <Weight :size="14" />
            {{ mascota.pesoActual }} kg
          </p>
          <p v-if="mascota.esterilizado" class="mascota-detail esterilizado">
            <CheckCircle2 :size="14" />
            Esterilizado/a
          </p>
        </div>
        <button class="btn-icon" @click="verHistorial(mascota.id)" title="Ver historial clínico">
          <FileText :size="18" />
        </button>
      </div>
    </div>

    <!-- Modal Registrar Mascota -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="mostrarModal" class="modal-overlay" @click.self="cerrarModal">
          <Transition name="slide-up">
            <div v-if="mostrarModal" class="modal-container">
              <div class="modal-header">
                <h3>Registrar Nueva Mascota</h3>
                <button class="btn-close" @click="cerrarModal">
                  <X :size="20" />
                </button>
              </div>

              <form @submit.prevent="guardarMascota" class="modal-body">
                <!-- Nombre -->
                <div class="form-group">
                  <label for="nombre">Nombre <span class="required">*</span></label>
                  <input
                    id="nombre"
                    v-model="form.nombre"
                    type="text"
                    maxlength="50"
                    placeholder="Ej: Rocky"
                    required
                  />
                </div>

                <!-- Especie y Raza -->
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
                    <label for="raza">Raza</label>
                    <select id="raza" v-model="form.idRaza" :disabled="!form.idEspecie || cargandoRazas">
                      <option value="">Seleccione...</option>
                      <option v-for="raza in razas" :key="raza.id" :value="raza.id">
                        {{ raza.nombre }}
                      </option>
                    </select>
                    <small v-if="form.idEspecie && razas.length === 0 && !cargandoRazas" class="hint">
                      No hay razas registradas para esta especie
                    </small>
                    <small v-else class="hint">Opcional</small>
                  </div>
                </div>

                <!-- Sexo y Fecha Nacimiento -->
                <div class="form-row">
                  <div class="form-group">
                    <label for="sexo">Sexo <span class="required">*</span></label>
                    <select id="sexo" v-model="form.sexo" required>
                      <option value="" disabled>Seleccione...</option>
                      <option value="M">Macho</option>
                      <option value="H">Hembra</option>
                    </select>
                  </div>
                  <div class="form-group">
                    <label for="fechaNacimiento">Fecha de Nacimiento</label>
                    <input
                      id="fechaNacimiento"
                      v-model="form.fechaNacimiento"
                      type="date"
                      :max="hoy"
                    />
                  </div>
                </div>

                <!-- Color y Peso -->
                <div class="form-row">
                  <div class="form-group">
                    <label for="color">Color</label>
                    <input
                      id="color"
                      v-model="form.color"
                      type="text"
                      maxlength="30"
                      placeholder="Ej: Marrón con manchas blancas"
                    />
                  </div>
                  <div class="form-group">
                    <label for="peso">Peso Actual (kg)</label>
                    <input
                      id="peso"
                      v-model="form.pesoActual"
                      type="number"
                      step="0.01"
                      min="0.01"
                      max="999.99"
                      placeholder="Ej: 12.50"
                    />
                  </div>
                </div>

                <!-- Esterilizado -->
                <div class="form-group checkbox-group">
                  <label class="checkbox-label">
                    <input v-model="form.esterilizado" type="checkbox" />
                    <span class="checkmark"></span>
                    ¿Está esterilizado/a?
                  </label>
                </div>

                <!-- Alerta de error -->
                <div v-if="error" class="alert-error">
                  <AlertCircle :size="16" />
                  {{ error }}
                </div>

                <!-- Botones -->
                <div class="modal-footer">
                  <button type="button" class="btn-secondary" @click="cerrarModal" :disabled="guardando">
                    Cancelar
                  </button>
                  <button type="submit" class="btn-primary" :disabled="guardando">
                    <Loader2 v-if="guardando" :size="18" class="spin" />
                    <Save v-else :size="18" />
                    {{ guardando ? 'Guardando...' : 'Guardar Mascota' }}
                  </button>
                </div>
              </form>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <!-- Toast de éxito -->
    <Transition name="slide-down">
      <div v-if="toast.visible" class="toast" :class="toast.type">
        <CheckCircle2 v-if="toast.type === 'success'" :size="18" />
        <AlertCircle v-else :size="18" />
        {{ toast.message }}
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  Plus, PawPrint, Calendar, Weight, CheckCircle2,
  FileText, Loader2, X, Save, AlertCircle
} from 'lucide-vue-next'
import { registrarMascota, getMisMascotas, getEspecies, getRazasPorEspecie } from '@/api/mascotas.api.js'

const router = useRouter()

// ─── ESTADO ───
const cargando = ref(false)
const guardando = ref(false)
const mostrarModal = ref(false)
const error = ref('')
const mascotas = ref([])
const especies = ref([])
const razas = ref([])
const cargandoRazas = ref(false)

const toast = ref({ visible: false, message: '', type: 'success' })

const hoy = new Date().toISOString().split('T')[0]

const form = ref({
  nombre: '',
  idEspecie: '',
  idRaza: '',
  fechaNacimiento: '',
  sexo: '',
  color: '',
  pesoActual: '',
  esterilizado: false
})

// ─── WATCH: Cuando cambia especie, cargar razas ───
watch(() => form.value.idEspecie, async (nuevoId) => {
  // Limpiar raza seleccionada
  form.value.idRaza = ''
  razas.value = []

  if (!nuevoId) return

  cargandoRazas.value = true
  try {
    const { data } = await getRazasPorEspecie(nuevoId)
    razas.value = data
  } catch (err) {
    console.error('Error al cargar razas:', err)
  } finally {
    cargandoRazas.value = false
  }
})

// ─── HELPERS ───

function getAvatarColor(str) {
  const colors = ['#0F766E', '#3B82F6', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899']
  let hash = 0
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}

function getEspecieLabel(id) {
  const especie = especies.value.find(e => e.id === id)
  return especie ? especie.nombre : 'Mascota'
}

function formatFecha(fecha) {
  if (!fecha) return ''
  const d = new Date(fecha)
  return d.toLocaleDateString('es-VE', { day: '2-digit', month: 'short', year: 'numeric' })
}

function showToast(message, type = 'success') {
  toast.value = { visible: true, message, type }
  setTimeout(() => { toast.value.visible = false }, 4000)
}

// ─── MÉTODOS ───

async function cargarEspecies() {
  try {
    const { data } = await getEspecies()
    especies.value = data
  } catch (err) {
    console.error('Error al cargar especies:', err)
  }
}

function abrirModal() {
  error.value = ''
  form.value = {
    nombre: '',
    idEspecie: '',
    idRaza: '',
    fechaNacimiento: '',
    sexo: '',
    color: '',
    pesoActual: '',
    esterilizado: false
  }
  razas.value = []
  mostrarModal.value = true
}

function cerrarModal() {
  if (guardando.value) return
  mostrarModal.value = false
}

function verHistorial(mascotaId) {
  router.push(`/cliente/historial-clinico?mascota=${mascotaId}`)
}

async function cargarMascotas() {
  cargando.value = true
  try {
    const { data } = await getMisMascotas()
    mascotas.value = data
  } catch (err) {
    showToast('Error al cargar las mascotas', 'error')
  } finally {
    cargando.value = false
  }
}

async function guardarMascota() {
  error.value = ''
  guardando.value = true

  // El cliente NO envía documentoIdentidadCliente
  // El backend lo obtiene automáticamente del JWT
  const payload = {
    idEspecie: parseInt(form.value.idEspecie),
    idRaza: form.value.idRaza ? parseInt(form.value.idRaza) : null,
    nombre: form.value.nombre.trim(),
    fechaNacimiento: form.value.fechaNacimiento || null,
    sexo: form.value.sexo,
    color: form.value.color.trim() || null,
    pesoActual: form.value.pesoActual ? parseFloat(form.value.pesoActual) : null,
    esterilizado: form.value.esterilizado
  }

  try {
    await registrarMascota(payload)
    showToast('¡Mascota registrada exitosamente!')
    cerrarModal()
    await cargarMascotas()
  } catch (err) {
    const msg = err.response?.data?.message || 'Error al registrar la mascota'
    error.value = Array.isArray(msg) ? msg.join(', ') : msg
  } finally {
    guardando.value = false
  }
}

onMounted(() => {
  cargarEspecies()
  cargarMascotas()
})
</script>

<style scoped>
.mascotas-view {
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 24px;
}

/* Header */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  margin: 0;
}

.subtitle {
  font-size: 14px;
  color: #64748B;
  margin: 4px 0 0 0;
}

/* Botones */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #0F766E;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.btn-primary:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.25);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-secondary {
  padding: 10px 20px;
  background: #F1F5F9;
  color: #475569;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.btn-secondary:hover:not(:disabled) {
  background: #E2E8F0;
}

.btn-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid #E2E8F0;
  background: white;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-icon:hover {
  background: #0F766E;
  color: white;
  border-color: #0F766E;
}

/* Loading & Empty */
.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
  color: #94A3B8;
  gap: 16px;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03);
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.empty-state p {
  font-size: 14px;
  color: #64748B;
  margin: 0 0 8px 0;
  text-align: center;
  max-width: 400px;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Grid de mascotas */
.mascotas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.mascota-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  transition: all 0.2s;
}

.mascota-card:hover {
  border-color: #0F766E;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 20px 25px -5px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.mascota-avatar {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 20px;
  flex-shrink: 0;
}

.mascota-info {
  flex: 1;
  min-width: 0;
}

.mascota-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 6px 0;
}

.mascota-meta {
  display: flex;
  gap: 8px;
  margin: 0 0 10px 0;
  flex-wrap: wrap;
}

.badge-especie {
  background: #ECFDF5;
  color: #059669;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  border: 1px solid #A7F3D0;
}

.badge-sexo {
  background: #EFF6FF;
  color: #3B82F6;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  border: 1px solid #BFDBFE;
}

.mascota-detail {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748B;
  margin: 4px 0 0 0;
}

.mascota-detail.esterilizado {
  color: #059669;
}

/* ─── MODAL Y FORMULARIO CORREGIDO ─── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 100;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.modal-container {
  background: #ffffff;
  border-radius: 16px;
  width: 100%;
  max-width: 520px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  font-family: inherit;
}

/* Scrollbar del modal */
.modal-container::-webkit-scrollbar {
  width: 8px;
}

.modal-container::-webkit-scrollbar-track {
  background: transparent;
}

.modal-container::-webkit-scrollbar-thumb {
  background: #CBD5E1;
  border-radius: 4px;
}

.modal-container::-webkit-scrollbar-thumb:hover {
  background: #94A3B8;
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.btn-close {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: #F1F5F9;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #E2E8F0;
  color: #1E293B;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #E2E8F0;
}

/* ─── FORMULARIO CON TIPOGRAFÍA Y ESTILOS CORREGIDOS ─── */
.form-group {
  margin-bottom: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #64748B;
  margin-bottom: 6px;
  font-family: inherit;
}

.required {
  color: #EF4444;
}

/* Inputs y selects con fuente forzada y bordes consistentes */
.form-group input,
.form-group select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 400;
  color: #1E293B;
  background: #ffffff;
  transition: all 0.2s;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  box-sizing: border-box;
  line-height: 1.5;
}

.form-group input::placeholder {
  color: #94A3B8;
  font-family: inherit;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.08);
}

/* Select disabled */
.form-group select:disabled {
  background: #F8FAFC;
  color: #94A3B8;
  cursor: not-allowed;
  opacity: 0.7;
}

/* Input number: ocultar flechas */
.form-group input[type="number"]::-webkit-outer-spin-button,
.form-group input[type="number"]::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.form-group input[type="number"] {
  -moz-appearance: textfield;
}

/* Input date: icono de calendenda consistente */
.form-group input[type="date"]::-webkit-calendar-picker-indicator {
  opacity: 0.5;
  cursor: pointer;
  filter: invert(0.4);
}

.form-group input[type="date"]::-webkit-calendar-picker-indicator:hover {
  opacity: 0.8;
}

.hint {
  font-size: 11px;
  color: #94A3B8;
  margin-top: 4px;
  display: block;
  font-family: inherit;
}

/* Checkbox corregido */
.checkbox-group {
  margin: 12px 0;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #64748B;
  cursor: pointer;
  font-weight: 500;
  font-family: inherit;
}

.checkbox-label input {
  display: none;
}

.checkmark {
  width: 20px;
  height: 20px;
  border: 1.5px solid #E2E8F0;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
  background: #ffffff;
}

.checkbox-label input:checked + .checkmark {
  background: #0F766E;
  border-color: #0F766E;
}

.checkbox-label input:checked + .checkmark::after {
  content: '';
  width: 5px;
  height: 9px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
  margin-bottom: 1px;
}

/* Alerta */
.alert-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #FEF2F2;
  color: #DC2626;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #FECACA;
  margin-top: 8px;
  font-family: inherit;
}

/* Toast */
.toast {
  position: fixed;
  top: 24px;
  right: 24px;
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  z-index: 200;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.toast.success {
  background: #ECFDF5;
  color: #059669;
  border: 1px solid #A7F3D0;
}

.toast.error {
  background: #FEF2F2;
  color: #DC2626;
  border: 1px solid #FECACA;
}

/* Transiciones */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* Responsive */
@media (max-width: 640px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .form-row {
    grid-template-columns: 1fr;
  }

  .mascotas-grid {
    grid-template-columns: 1fr;
  }

  .modal-footer {
    flex-direction: column-reverse;
  }

  .modal-footer .btn-primary,
  .modal-footer .btn-secondary {
    width: 100%;
    justify-content: center;
  }
}
</style>