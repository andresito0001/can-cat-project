<template>
  <div class="mascotas-view">
    <!-- Header -->
    <header class="page-header">
      <div>
        <h2>Mis Mascotas</h2>
        <p class="subtitle">Gestiona y registra las mascotas asociadas a tu cuenta</p>
      </div>
      <button class="btn-primary" @click="abrirModal">
        <Plus :size="18" />
        Nueva Mascota
      </button>
    </header>

    <!-- Loading -->
    <div v-if="cargando" class="loading-state">
      <Loader2 :size="32" class="spin" />
      <p>Cargando mascotas...</p>
    </div>

    <!-- Empty -->
    <div v-else-if="mascotas.length === 0" class="empty-state">
      <PawPrint :size="48" />
      <h3>No tienes mascotas registradas</h3>
      <p>Registra tu primera mascota para comenzar a gestionar su historial clínico.</p>
      <button class="btn-primary" @click="abrirModal">
        <Plus :size="18" />
        Registrar Mascota
      </button>
    </div>

    <!-- Grid -->
    <div v-else class="mascotas-grid">
      <div
        v-for="mascota in mascotas"
        :key="mascota.idMascota"
        class="mascota-card-wrapper"
        :style="{ '--pet-color': getAvatarColor(mascota.nombre) }"
      >
        <button
          type="button"
          class="mascota-card"
          :aria-label="`Ver historial clínico de ${mascota.nombre}`"
          @click="verHistorial(mascota.idMascota)"
        >
          <div
            class="mascota-avatar"
            :style="{ backgroundColor: getAvatarColor(mascota.nombre) }"
          >
            {{ inicialNombre(mascota.nombre) }}
          </div>

          <div class="mascota-info">
            <h4 class="mascota-nombre">{{ mascota.nombre }}</h4>

            <p class="mascota-meta">
              <span class="badge-especie">{{ getEspecieLabel(mascota.idEspecie) }}</span>
              <span v-if="mascota.sexo" class="badge-sexo">
                {{ mascota.sexo === 'M' ? 'Macho' : 'Hembra' }}
              </span>
            </p>

            <div class="mascota-datos">
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
          </div>

          <span class="mascota-cta" aria-hidden="true">
            <FileText :size="16" />
          </span>
        </button>

        <!-- Botón eliminar flotante -->
        <button
          type="button"
          class="mascota-delete"
          :aria-label="`Eliminar ${mascota.nombre}`"
          :title="`Eliminar ${mascota.nombre}`"
          @click.stop="confirmarEliminar(mascota)"
        >
          <Trash2 :size="14" />
        </button>
      </div>
    </div>

    <!-- ═══════════ MODAL REGISTRAR MASCOTA ═══════════ -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="mostrarModal" class="modal-overlay" @click.self="cerrarModal">
          <Transition name="slide-up" appear>
            <div class="modal-container" role="dialog" aria-modal="true">
              <!-- Header con icono -->
              <div class="modal-header">
                <div class="modal-header-left">
                  <div class="modal-icon"><PawPrint :size="20" /></div>
                  <div>
                    <h3>Registrar Nueva Mascota</h3>
                    <p class="modal-sub">Completa los datos de tu compañero</p>
                  </div>
                </div>
                <button
                  class="btn-close"
                  type="button"
                  aria-label="Cerrar"
                  :disabled="guardando"
                  @click="cerrarModal"
                >
                  <X :size="20" />
                </button>
              </div>

              <form @submit.prevent="guardarMascota" class="modal-body" novalidate>
                <!-- Sección: Identificación -->
                <section class="form-section">
                  <header class="section-header">
                    <BadgeInfo :size="14" />
                    <span>Identificación</span>
                  </header>

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
                      <select
                        id="raza"
                        v-model="form.idRaza"
                        :disabled="!form.idEspecie || cargandoRazas"
                      >
                        <option value="">Seleccione...</option>
                        <option v-for="raza in razas" :key="raza.id" :value="raza.id">
                          {{ raza.nombre }}
                        </option>
                      </select>
                      <small
                        v-if="form.idEspecie && razas.length === 0 && !cargandoRazas"
                        class="hint hint-warn"
                      >
                        No hay razas para esta especie
                      </small>
                    </div>
                  </div>
                </section>

                <!-- Sección: Detalles físicos -->
                <section class="form-section">
                  <header class="section-header">
                    <Ruler :size="14" />
                    <span>Detalles físicos</span>
                  </header>

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
                      <input
                        id="fechaNacimiento"
                        v-model="form.fechaNacimiento"
                        type="date"
                        :max="hoy"
                      />
                    </div>
                  </div>

                  <div class="form-row">
                    <div class="form-group">
                      <label for="color">Color / particularidades</label>
                      <input
                        id="color"
                        v-model="form.color"
                        type="text"
                        maxlength="30"
                        placeholder="Ej: Marrón con manchas blancas"
                      />
                    </div>
                    <div class="form-group">
                      <label for="peso">Peso actual</label>
                      <div class="input-suffix">
                        <input
                          id="peso"
                          v-model="form.pesoActual"
                          type="number"
                          step="0.01"
                          min="0.01"
                          max="999.99"
                          placeholder="12.50"
                        />
                        <span class="suffix">kg</span>
                      </div>
                    </div>
                  </div>
                </section>

                <!-- Sección: Salud -->
                <section class="form-section">
                  <header class="section-header">
                    <Heart :size="14" />
                    <span>Salud</span>
                  </header>

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

                <!-- Alerta de error -->
                <Transition name="slide-down">
                  <div v-if="error" class="alert-error">
                    <AlertCircle :size="16" />
                    <span>{{ error }}</span>
                  </div>
                </Transition>

                <!-- Footer -->
                <div class="modal-footer">
                  <button
                    type="button"
                    class="btn-secondary"
                    :disabled="guardando"
                    @click="cerrarModal"
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    class="btn-primary"
                    :disabled="guardando || !puedeGuardar"
                  >
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

    <!-- ═══════════ MODAL CONFIRMAR ELIMINACIÓN ═══════════ -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="modalEliminar" class="modal-overlay" @click.self="cancelarEliminar">
          <Transition name="slide-up" appear>
            <div class="modal-container modal-confirm" role="dialog" aria-modal="true">
              <div class="confirm-icon-wrap">
                <div class="confirm-icon">
                  <AlertTriangle :size="22" />
                </div>
              </div>

              <h3 class="confirm-title">
                ¿Eliminar a {{ mascotaAEliminar?.nombre }}?
              </h3>
              <p class="confirm-text">
                Esta acción quitará a
                <strong>{{ mascotaAEliminar?.nombre }}</strong>
                de tu lista de mascotas. Su historial clínico se conservará en el sistema
                por razones médicas.
              </p>

              <Transition name="slide-down">
                <div v-if="errorEliminar" class="alert-error">
                  <AlertCircle :size="16" />
                  <span>{{ errorEliminar }}</span>
                </div>
              </Transition>

              <div class="confirm-actions">
                <button
                  type="button"
                  class="btn-secondary"
                  :disabled="eliminando"
                  @click="cancelarEliminar"
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  class="btn-danger"
                  :disabled="eliminando"
                  @click="eliminarMascota"
                >
                  <Loader2 v-if="eliminando" :size="18" class="spin" />
                  <Trash2 v-else :size="18" />
                  {{ eliminando ? 'Eliminando...' : 'Sí, eliminar' }}
                </button>
              </div>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import {
  Plus, PawPrint, Calendar, Weight, CheckCircle2,
  FileText, Loader2, X, Save, AlertCircle,
  BadgeInfo, Ruler, Heart, Mars, Venus,
  Trash2, AlertTriangle,
} from 'lucide-vue-next'
import {
  registrarMascota,
  getMisMascotas,
  getEspecies,
  getRazasPorEspecie,
  eliminarMascota as apiEliminarMascota,
} from '@/api/mascotas.api.js'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { toastSuccess, toastError } = useToast()

// ─── ESTADO ───
const cargando = ref(false)
const guardando = ref(false)
const mostrarModal = ref(false)
const error = ref('')
const mascotas = ref([])
const especies = ref([])
const razas = ref([])
const cargandoRazas = ref(false)
const nombreInput = ref(null)

// Estado de eliminación
const modalEliminar = ref(false)
const mascotaAEliminar = ref(null)
const eliminando = ref(false)
const errorEliminar = ref('')

const hoy = new Date().toISOString().split('T')[0]

const form = ref(formVacio())

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

const puedeGuardar = computed(() =>
  form.value.nombre.trim().length > 0 &&
  form.value.idEspecie !== '' &&
  form.value.sexo !== ''
)

// ─── WATCH: al cambiar especie, cargar razas ───
watch(() => form.value.idEspecie, async (nuevoId) => {
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
const PALETA_AVATARES = ['#0F766E', '#3B82F6', '#F59E0B', '#F43F5E', '#8B5CF6', '#0EA5E9']

function getAvatarColor(str) {
  let hash = 0
  const s = String(str || '')
  for (let i = 0; i < s.length; i++) hash = s.charCodeAt(i) + ((hash << 5) - hash)
  return PALETA_AVATARES[Math.abs(hash) % PALETA_AVATARES.length]
}

function inicialNombre(nombre) {
  return String(nombre || '?').trim().charAt(0).toUpperCase() || '?'
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

/**
 * Normaliza un nombre para comparar:
 *  - trim
 *  - minúsculas
 *  - quita tildes ("Lúna" === "Luna")
 *  - colapsa espacios múltiples
 */
function normalizarNombre(str) {
  return String(str || '')
    .trim()
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/\s+/g, ' ')
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
  form.value = formVacio()
  razas.value = []
  mostrarModal.value = true
  nextTick(() => nombreInput.value?.focus())
}

function cerrarModal() {
  if (guardando.value) return
  mostrarModal.value = false
}

function verHistorial(mascotaId) {
  if (!mascotaId) return
  router.push({ path: '/cliente/historial-clinico', query: { mascota: mascotaId } })
}

async function cargarMascotas() {
  cargando.value = true
  try {
    const { data } = await getMisMascotas()
    mascotas.value = data
  } catch (err) {
    toastError(getApiErrorMessage(err) || 'Error al cargar las mascotas')
  } finally {
    cargando.value = false
  }
}

// ═══════════════════════════════════════════════════════════
//  VALIDACIÓN DE DUPLICADO — solo por nombre
//  Compara trim + lowercase + sin acentos
// ═══════════════════════════════════════════════════════════
function existeMascotaDuplicada() {
  const nombreNorm = normalizarNombre(form.value.nombre)
  return mascotas.value.some(m =>
    normalizarNombre(m.nombre) === nombreNorm
  )
}

async function guardarMascota() {
  error.value = ''

  // Validación previa: duplicado por nombre
  if (existeMascotaDuplicada()) {
    error.value = `Ya tienes una mascota llamada "${form.value.nombre.trim()}". Usa un nombre distinto o revisa si ya está registrada.`
    return
  }

  guardando.value = true

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

  try {
    await registrarMascota(payload)

    // ✅ éxito: cerrar primero, luego toast, luego recargar
    guardando.value = false
    mostrarModal.value = false
    toastSuccess('¡Mascota registrada exitosamente!')
    await cargarMascotas()
  } catch (err) {
    if (err?.response?.status === 409) {
      error.value = getApiErrorMessage(err) ||
        'Ya tienes una mascota con ese nombre. Usa uno distinto.'
    } else {
      error.value = getApiErrorMessage(err) || 'Error al registrar la mascota'
    }
  } finally {
    guardando.value = false
  }
}

// ═══════════════════════════════════════════════════════════
//  ELIMINAR MASCOTA
// ═══════════════════════════════════════════════════════════
function confirmarEliminar(mascota) {
  mascotaAEliminar.value = mascota
  errorEliminar.value = ''
  modalEliminar.value = true
}

function cancelarEliminar() {
  if (eliminando.value) return
  modalEliminar.value = false
  mascotaAEliminar.value = null
  errorEliminar.value = ''
}

async function eliminarMascota() {
  if (!mascotaAEliminar.value) return
  errorEliminar.value = ''
  eliminando.value = true
  try {
    await apiEliminarMascota(mascotaAEliminar.value.idMascota)
    eliminando.value = false
    modalEliminar.value = false
    const nombre = mascotaAEliminar.value.nombre
    mascotaAEliminar.value = null
    toastSuccess(`"${nombre}" se eliminó correctamente`)
    await cargarMascotas()
  } catch (err) {
    errorEliminar.value = getApiErrorMessage(err) ||
      'No se pudo eliminar la mascota. Intenta de nuevo.'
  } finally {
    eliminando.value = false
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

button { font-family: inherit; }

/* ── Header ── */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
  gap: 16px;
}
.page-header h2 { font-size: 24px; font-weight: 700; color: #1E293B; margin: 0; }
.subtitle { font-size: 14px; color: #64748B; margin: 4px 0 0 0; }

/* ── Botones ── */
.btn-primary {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 10px 20px; background: #0F766E; color: white;
  border: none; border-radius: 10px; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all 0.2s; font-family: inherit; white-space: nowrap;
}
.btn-primary:hover:not(:disabled) {
  background: #115E59; transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.25);
}
.btn-primary:disabled { opacity: 0.55; cursor: not-allowed; }

.btn-secondary {
  padding: 10px 20px; background: #F1F5F9; color: #475569;
  border: 1px solid #E2E8F0; border-radius: 10px; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all 0.2s; font-family: inherit;
}
.btn-secondary:hover:not(:disabled) { background: #E2E8F0; }
.btn-secondary:disabled { opacity: 0.55; cursor: not-allowed; }

.btn-danger {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 10px 20px; background: #DC2626; color: #fff;
  border: none; border-radius: 10px; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all 0.2s; font-family: inherit;
}
.btn-danger:hover:not(:disabled) {
  background: #B91C1C;
  box-shadow: 0 4px 12px rgba(220, 38, 38, 0.25);
}
.btn-danger:disabled { opacity: 0.55; cursor: not-allowed; }

/* ── Loading & Empty ── */
.loading-state, .empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 80px 24px; color: #94A3B8; gap: 16px;
  background: #fff; border-radius: 16px; border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03);
}
.empty-state h3 { font-size: 18px; font-weight: 600; color: #1E293B; margin: 0; }
.empty-state p { font-size: 14px; color: #64748B; margin: 0 0 8px 0; text-align: center; max-width: 400px; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Grid de mascotas ── */
.mascotas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.mascota-card-wrapper {
  position: relative;
}

.mascota-card {
  position: relative; display: flex; align-items: flex-start; gap: 16px;
  padding: 18px; background: #fff; border: 1px solid #E2E8F0;
  border-radius: 12px; text-align: left; cursor: pointer; overflow: hidden;
  font-family: inherit; color: inherit; appearance: none; width: 100%;
  transition: border-color .2s, box-shadow .2s, transform .2s;
}
.mascota-card::before {
  content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 3px;
  background: var(--pet-color, #0F766E);
  transform: scaleY(.35); transform-origin: center; opacity: 0;
  transition: opacity .25s, transform .25s;
}
.mascota-card:hover {
  border-color: rgba(15, 118, 110, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 10px 24px -8px rgba(15, 118, 110, 0.18),
              0 4px 8px -4px rgba(15, 23, 42, 0.06);
}
.mascota-card:hover::before { opacity: 1; transform: scaleY(1); }
.mascota-card:focus-visible {
  outline: none; border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.15);
}

.mascota-avatar {
  width: 52px; height: 52px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: white; font-weight: 700; font-size: 20px; flex-shrink: 0;
  box-shadow: 0 4px 10px -3px rgba(15, 23, 42, 0.12);
}

.mascota-info { flex: 1; min-width: 0; }
.mascota-nombre {
  font-size: 16px; font-weight: 700; color: #1E293B; margin: 0 0 6px 0;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  padding-right: 22px;
}
.mascota-meta { display: flex; gap: 6px; margin: 0 0 10px 0; flex-wrap: wrap; }
.badge-especie {
  background: #ECFDF5; color: #059669; padding: 2px 10px; border-radius: 20px;
  font-size: 11px; font-weight: 600; border: 1px solid #A7F3D0; white-space: nowrap;
}
.badge-sexo {
  background: #EFF6FF; color: #3B82F6; padding: 2px 10px; border-radius: 20px;
  font-size: 11px; font-weight: 600; border: 1px solid #BFDBFE; white-space: nowrap;
}
.mascota-datos { display: flex; flex-direction: column; gap: 4px; }
.mascota-detail {
  display: flex; align-items: center; gap: 6px; font-size: 13px;
  color: #64748B; margin: 0;
}
.mascota-detail.esterilizado { color: #059669; font-weight: 600; }

.mascota-cta {
  width: 30px; height: 30px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #94A3B8; background: #F1F5F9; flex-shrink: 0; align-self: center;
  transition: background-color .2s, color .2s, transform .2s;
}
.mascota-card:hover .mascota-cta {
  background: #0F766E; color: #fff; transform: translateX(2px);
}

/* ── Botón eliminar flotante ── */
.mascota-delete {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 30px;
  height: 30px;
  border-radius: 8px;
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(4px);
  color: #94A3B8;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transform: scale(0.9);
  transition: all 0.18s ease;
  z-index: 2;
}
.mascota-card-wrapper:hover .mascota-delete {
  opacity: 1;
  transform: scale(1);
}
.mascota-delete:hover {
  background: #FEF2F2;
  border-color: #FECACA;
  color: #DC2626;
}
.mascota-delete:focus-visible {
  opacity: 1;
  outline: none;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.25);
}

/* En pantallas táctiles, siempre visible */
@media (hover: none) {
  .mascota-delete { opacity: 1; transform: scale(1); }
}

/* ══════════════ MODAL ══════════════ */
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
.modal-container::-webkit-scrollbar { width: 8px; }
.modal-container::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 4px; }
.modal-container::-webkit-scrollbar-thumb:hover { background: #94A3B8; }

/* Header */
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
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.modal-header h3 { font-size: 17px; font-weight: 700; color: #1E293B; margin: 0; }
.modal-sub { font-size: 12.5px; color: #64748B; margin: 2px 0 0 0; }
.btn-close {
  width: 36px; height: 36px; border-radius: 10px; border: none;
  background: #F1F5F9; color: #64748B;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all 0.2s; flex-shrink: 0;
}
.btn-close:hover:not(:disabled) { background: #E2E8F0; color: #1E293B; }
.btn-close:disabled { opacity: 0.5; cursor: not-allowed; }

/* Body */
.modal-body { padding: 20px 24px 24px; display: flex; flex-direction: column; gap: 18px; }

/* Secciones */
.form-section { display: flex; flex-direction: column; gap: 14px; }
.section-header {
  display: flex; align-items: center; gap: 6px;
  font-size: 11px; font-weight: 700; text-transform: uppercase;
  letter-spacing: 0.6px; color: #64748B;
  padding-bottom: 6px; border-bottom: 1px dashed #E2E8F0;
}
.section-header svg { color: #94A3B8; }

/* Form groups */
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-group label {
  display: flex; align-items: center; justify-content: space-between;
  font-size: 12.5px; font-weight: 600; color: #475569; font-family: inherit;
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
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  box-sizing: border-box; line-height: 1.5;
}
.form-group input::placeholder { color: #94A3B8; }
.form-group input:focus, .form-group select:focus {
  outline: none; border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.1);
}
.form-group select:disabled {
  background: #F8FAFC; color: #94A3B8; cursor: not-allowed;
}
.form-group input[type="number"]::-webkit-outer-spin-button,
.form-group input[type="number"]::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }
.form-group input[type="number"] { -moz-appearance: textfield; }
.form-group input[type="date"]::-webkit-calendar-picker-indicator {
  opacity: 0.5; cursor: pointer; filter: invert(0.4);
}

.hint { font-size: 11px; color: #94A3B8; margin-top: 2px; }
.hint-warn { color: #B45309; }

/* Input con sufijo (kg) */
.input-suffix { position: relative; display: flex; align-items: center; }
.input-suffix input { padding-right: 44px; }
.input-suffix .suffix {
  position: absolute; right: 12px; font-size: 12px; font-weight: 700;
  color: #94A3B8; pointer-events: none;
}

/* Segmented control para sexo */
.segmented {
  display: grid; grid-template-columns: 1fr 1fr;
  background: #F1F5F9; border-radius: 10px; padding: 4px; gap: 4px;
}
.segment {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 8px 12px; border-radius: 7px;
  font-size: 13px; font-weight: 600; color: #64748B;
  cursor: pointer; transition: all .15s;
  user-select: none;
}
.segment:hover { color: #334155; }
.segment.active {
  background: #fff; color: #0F766E;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
}

/* Toggle switch */
.toggle-row {
  display: flex; align-items: center; justify-content: space-between;
  gap: 14px; padding: 12px 14px;
  background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 12px;
  cursor: pointer; user-select: none;
}
.toggle-text { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.toggle-title { font-size: 13.5px; font-weight: 600; color: #1E293B; }
.toggle-desc { font-size: 11.5px; color: #64748B; }

.switch {
  position: relative; width: 40px; height: 22px; flex-shrink: 0;
  background: #CBD5E1; border-radius: 22px;
  transition: background-color .2s; cursor: pointer;
}
.switch.on { background: #0F766E; }
.switch-knob {
  position: absolute; top: 3px; left: 3px;
  width: 16px; height: 16px; background: #fff; border-radius: 50%;
  transition: transform .2s; box-shadow: 0 1px 3px rgba(0,0,0,.15);
}
.switch.on .switch-knob { transform: translateX(18px); }

/* Error alert */
.alert-error {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 12px 14px; background: #FEF2F2; color: #B91C1C;
  border-radius: 10px; font-size: 13px; font-weight: 500;
  border: 1px solid #FECACA; line-height: 1.45;
}
.alert-error svg { flex-shrink: 0; margin-top: 1px; }

/* Footer */
.modal-footer {
  display: flex; justify-content: flex-end; gap: 12px;
  margin-top: 4px; padding-top: 18px; border-top: 1px solid #E2E8F0;
  position: sticky; bottom: 0; background: #fff;
  padding-bottom: 4px;
}

/* ── Modal confirmación ── */
.modal-confirm {
  max-width: 440px;
  padding: 28px 26px 24px;
  text-align: center;
  border-radius: 18px;
}
.confirm-icon-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 14px;
}
.confirm-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: #FEF2F2;
  color: #DC2626;
  display: flex;
  align-items: center;
  justify-content: center;
}
.confirm-title {
  font-size: 17px;
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 8px;
}
.confirm-text {
  font-size: 13.5px;
  color: #64748B;
  margin: 0 0 20px;
  line-height: 1.55;
}
.confirm-text strong { color: #334155; font-weight: 600; }

.confirm-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-top: 4px;
}
.confirm-actions .btn-secondary,
.confirm-actions .btn-danger {
  flex: 1;
  justify-content: center;
}

/* Transiciones */
.fade-enter-active, .fade-leave-active { transition: opacity 0.25s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from, .slide-up-leave-to {
  opacity: 0; transform: translateY(24px) scale(0.97);
}

.slide-down-enter-active, .slide-down-leave-active { transition: all 0.2s ease; }
.slide-down-enter-from, .slide-down-leave-to {
  opacity: 0; transform: translateY(-6px); max-height: 0;
}

/* Responsive */
@media (max-width: 640px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 16px; }
  .form-row { grid-template-columns: 1fr; }
  .mascotas-grid { grid-template-columns: 1fr; }
  .modal-footer { flex-direction: column-reverse; }
  .modal-footer .btn-primary, .modal-footer .btn-secondary {
    width: 100%; justify-content: center;
  }
}
</style>