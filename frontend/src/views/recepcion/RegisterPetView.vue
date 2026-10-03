<template>
  <div class="registrar-mascota">
    <ToastContainer />

    <!-- Breadcrumb -->
    <nav class="breadcrumb" aria-label="Migas de pan">
      <button class="bc-back" type="button" @click="volver">
        <ArrowLeft :size="15" />
      </button>
      <button class="bc-item bc-link" type="button" @click="volver">
        Panel de Recepción
      </button>
      <ChevronRight :size="14" class="bc-sep" />
      <span class="bc-item bc-current">Registrar Mascota</span>
    </nav>

    <!-- Hero -->
    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <Sparkles :size="12" /> Recepción · Registro asistido
        </span>
        <h1>Registrar Mascota</h1>
        <p class="page-header-sub">
          Asocia un nuevo paciente a un cliente existente para agendar consultas.
        </p>
      </div>
      <div v-if="!exito" class="page-header-actions">
        <span class="progress-pill">
          <span class="progress-dot" :class="{ 'is-complete': progreso === 100 }" />
          {{ completadosObligatorios }} / {{ totalObligatorios }} campos obligatorios
        </span>
      </div>
    </header>

    <!-- ═══════════ ÉXITO ═══════════ -->
    <section v-if="exito" class="success-container">
      <div class="success-icon">
        <CheckCircle2 :size="40" />
      </div>
      <h2 class="success-title">Mascota registrada con éxito</h2>
      <p class="success-message">
        <strong>{{ exito.nombre }}</strong> ({{ exito.nombreEspecie }})
        ahora está asociada a {{ exito.nombreCliente }}.
      </p>

      <div class="success-resumen">
        <div class="resumen-fila">
          <span class="resumen-label"><PawPrint :size="13" /> Mascota</span>
          <span class="resumen-value">{{ exito.nombre }}</span>
        </div>
        <div class="resumen-fila">
          <span class="resumen-label"><Dog :size="13" /> Especie</span>
          <span class="resumen-value">{{ exito.nombreEspecie }}</span>
        </div>
        <div class="resumen-fila">
          <span class="resumen-label"><User :size="13" /> Dueño</span>
          <span class="resumen-value">{{ exito.nombreCliente }}</span>
        </div>
        <div class="resumen-fila">
          <span class="resumen-label"><CreditCard :size="13" /> Documento</span>
          <span class="resumen-value mono">
            {{ exito.documentoIdentidadCliente || clienteDocumento }}
          </span>
        </div>
      </div>

      <div class="success-actions">
        <AppButton variant="primary" size="lg" block @click="irAgendarCita">
          <template #icon-left><CalendarPlus :size="15" /></template>
          Agendar cita ahora
        </AppButton>
        <AppButton variant="secondary" size="lg" block @click="volver">
          <template #icon-left><ArrowLeft :size="15" /></template>
          Volver al panel
        </AppButton>
      </div>
    </section>

    <!-- ═══════════ FORMULARIO ═══════════ -->
    <form v-else @submit.prevent="guardar" novalidate class="form-layout">
      <div class="form-main">
        <!-- ─── Sección 1: Cliente ─── -->
        <section class="form-section">
          <header class="section-header">
            <div class="section-number">1</div>
            <div>
              <h2 class="section-title">Cliente dueño</h2>
              <p class="section-sub">
                {{ clienteDocumento
                  ? 'Cliente seleccionado'
                  : 'Busca por nombre o cédula para asociar la mascota' }}
              </p>
            </div>
          </header>

          <div class="section-body">
            <div v-if="clienteDocumento" class="cliente-chip">
              <EntityAvatar :nombre="clienteNombre" tipo="cliente" size="md" />
              <div class="cliente-chip-info">
                <p class="cliente-chip-name">{{ clienteNombre }}</p>
                <p class="cliente-chip-doc mono">{{ clienteDocumento }}</p>
              </div>
              <button type="button" class="btn-chip" @click="cambiarCliente">
                <X :size="13" /> Cambiar
              </button>
            </div>

            <template v-else>
              <div class="search-bar">
                <Search :size="15" class="search-icon" />
                <input
                  ref="buscadorClienteRef"
                  v-model="clienteFiltro"
                  type="text"
                  class="form-input search-input"
                  placeholder="Ej: María González o V-12345678…"
                  autocomplete="off"
                />
                <span v-if="buscando" class="search-spinner">
                  <span class="spinner spinner-sm" />
                </span>
                <button
                  v-else-if="clienteFiltro"
                  type="button"
                  class="search-clear"
                  aria-label="Limpiar"
                  @click="clienteFiltro = ''"
                >
                  <X :size="13" />
                </button>
              </div>

              <Transition name="slide-down">
                <div
                  v-if="clienteFiltro.length >= 2 && !buscando && !resultados.length"
                  class="empty-inline"
                >
                  <AlertTriangle :size="24" />
                  <p>
                    No se encontraron resultados. Verifica los datos o
                    <router-link to="/recepcion/clientes/nuevo" class="link-teal">
                      registra previamente al cliente
                    </router-link>.
                  </p>
                </div>
              </Transition>

              <div v-if="resultados.length" class="results-list">
                <button
                  v-for="c in resultados"
                  :key="c.id"
                  type="button"
                  class="result-row"
                  @click="seleccionarCliente(c)"
                >
                  <EntityAvatar :nombre="c.nombreCompleto" tipo="cliente" size="sm" />
                  <div class="result-info">
                    <p class="result-name">{{ c.nombreCompleto }}</p>
                    <p class="result-meta mono">{{ c.documentoIdentidad }}</p>
                  </div>
                  <ChevronRight :size="16" class="result-arrow" />
                </button>
              </div>
            </template>
          </div>
        </section>

        <!-- ─── Sección 2: Datos de la mascota ─── -->
        <section class="form-section">
          <header class="section-header">
            <div class="section-number">2</div>
            <div>
              <h2 class="section-title">Datos de la mascota</h2>
              <p class="section-sub">Información básica del nuevo paciente</p>
            </div>
          </header>

          <div class="section-body">
            <Transition name="slide-down">
              <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
            </Transition>

            <AppFormField
              label="Nombre de la mascota"
              :error="erroresCampo.nombre ? 'El nombre es obligatorio' : ''"
              required
            >
              <template #default="{ id, invalid }">
                <AppInput
                  :id="id"
                  v-model="form.nombre"
                  placeholder="Ej: Rocky"
                  maxlength="50"
                  :error="invalid"
                  autocomplete="off"
                />
              </template>
            </AppFormField>

            <!-- Sexo: segmented control limpio, sin iconos raros -->
            <AppFormField label="Sexo" required>
              <div class="segmented" role="radiogroup" aria-label="Sexo de la mascota">
                <button
                  type="button"
                  role="radio"
                  class="segment"
                  :class="{ 'is-active': form.sexo === 'M' }"
                  :aria-checked="form.sexo === 'M'"
                  @click="form.sexo = 'M'"
                >
                  Macho
                </button>
                <button
                  type="button"
                  role="radio"
                  class="segment"
                  :class="{ 'is-active': form.sexo === 'H' }"
                  :aria-checked="form.sexo === 'H'"
                  @click="form.sexo = 'H'"
                >
                  Hembra
                </button>
              </div>
            </AppFormField>

            <!-- Especie: chips con inicial en círculo (sin iconos raros) -->
            <AppFormField
              label="Especie"
              :error="erroresCampo.idEspecie ? 'Selecciona una especie' : ''"
              required
            >
              <div class="species-grid">
                <button
                  v-for="e in especies"
                  :key="e.id"
                  type="button"
                  class="species-chip"
                  :class="{ 'is-selected': form.idEspecie === e.id }"
                  @click="form.idEspecie = e.id"
                >
                  <span class="species-inicial">{{ e.nombre.charAt(0) }}</span>
                  <span class="species-name">{{ e.nombre }}</span>
                  <Check v-if="form.idEspecie === e.id" :size="13" class="species-check" />
                </button>
              </div>
            </AppFormField>

            <AppFormField label="Raza" optional>
              <template #default="{ id }">
                <AppSelect
                  :id="id"
                  v-model="form.idRaza"
                  :disabled="!form.idEspecie || cargandoRazas"
                >
                  <option value="">
                    {{ !form.idEspecie
                      ? 'Selecciona primero una especie'
                      : (cargandoRazas ? 'Cargando razas…' : 'Seleccione una raza') }}
                  </option>
                  <option v-for="r in razas" :key="r.id" :value="r.id">{{ r.nombre }}</option>
                </AppSelect>
              </template>
            </AppFormField>

            <div class="form-grid-2">
              <AppFormField label="Fecha de nacimiento" optional>
                <DatePicker
                  v-model="form.fechaNacimiento"
                  placeholder="Seleccionar fecha"
                  :max="hoy"
                />
              </AppFormField>

              <AppFormField label="Peso actual" optional hint="En kilogramos">
                <template #default="{ id }">
                  <AppInput
                    :id="id"
                    v-model="form.pesoActual"
                    type="number"
                    step="0.01"
                    min="0.01"
                    max="999.99"
                    placeholder="Ej: 12.50"
                  />
                </template>
              </AppFormField>
            </div>

            <AppFormField label="Color / señas particulares" optional>
              <template #default="{ id }">
                <AppInput
                  :id="id"
                  v-model="form.color"
                  placeholder="Ej: Marrón con manchas blancas"
                  maxlength="30"
                />
              </template>
            </AppFormField>

            <label class="check-row">
              <input v-model="form.esterilizado" type="checkbox" class="checkbox-input" />
              <span class="checkbox-box">
                <Check v-if="form.esterilizado" :size="12" />
              </span>
              <span class="checkbox-label">La mascota está esterilizada</span>
            </label>
          </div>
        </section>
      </div>

      <!-- ─── Sidebar: preview + acciones ─── -->
      <aside class="form-sidebar">
        <div class="preview-card">
          <header class="preview-header">
            <h4>Vista previa</h4>
            <span class="preview-hint">Así se verá la ficha</span>
          </header>

          <div class="preview-body">
            <PetAvatar :nombre-especie="especieSeleccionada?.nombre" size="lg" />

            <p class="preview-nombre" :class="{ 'is-empty': !form.nombre }">
              {{ form.nombre || 'Nombre de la mascota' }}
            </p>

            <div class="preview-badges">
              <span v-if="especieSeleccionada" class="preview-badge">
                {{ especieSeleccionada.nombre }}
              </span>
              <span v-if="form.sexo" class="preview-badge">
                {{ form.sexo === 'M' ? 'Macho' : 'Hembra' }}
              </span>
            </div>

            <div class="preview-meta">
              <div v-if="clienteNombre" class="preview-line">
                <User :size="13" />
                <span>{{ clienteNombre }}</span>
              </div>
              <div v-if="form.pesoActual" class="preview-line">
                <Weight :size="13" />
                <span>{{ form.pesoActual }} kg</span>
              </div>
              <div v-if="form.fechaNacimiento" class="preview-line">
                <CalendarDays :size="13" />
                <span>{{ formatFechaCorta(form.fechaNacimiento) }}</span>
              </div>
              <div v-if="form.esterilizado" class="preview-line line-success">
                <CheckCircle2 :size="13" />
                <span>Esterilizada</span>
              </div>
            </div>
          </div>

          <footer class="preview-footer">
            <div class="progress-top">
              <span class="progress-label">Campos obligatorios</span>
              <span class="progress-count">{{ completadosObligatorios }} / {{ totalObligatorios }}</span>
            </div>
            <div class="progress-bar">
              <div
                class="progress-fill"
                :class="{ 'is-complete': progreso === 100 }"
                :style="{ width: progreso + '%' }"
              />
            </div>
          </footer>
        </div>

        <div class="process-note">
          <div class="process-icon"><CalendarPlus :size="16" /></div>
          <div>
            <p class="process-title">¿Ya tienes la cita?</p>
            <p class="process-desc">
              Después de registrar la mascota podrás agendar una cita directamente.
            </p>
          </div>
        </div>
      </aside>
    </form>

    <!-- ─── Footer sticky (solo en formulario) ─── -->
    <footer v-if="!exito" class="form-actions">
      <AppButton variant="ghost" :disabled="guardando" @click="volver">
        <template #icon-left><X :size="15" /></template>
        Cancelar
      </AppButton>
      <AppButton
        type="button"
        variant="primary"
        size="lg"
        :loading="guardando"
        :disabled="!puedeGuardar"
        @click="guardar"
      >
        <template #icon-left><Save :size="15" /></template>
        {{ guardando ? 'Guardando…' : 'Guardar mascota' }}
      </AppButton>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  PawPrint, Search, AlertTriangle, CheckCircle2, Check,
  CalendarPlus, ArrowLeft, ChevronRight, X, User, CreditCard,
  Sparkles, Save, Dog, Weight, CalendarDays,
} from 'lucide-vue-next'
import PetAvatar from '@/components/ui/PetAvatar.vue'
import EntityAvatar from '@/components/ui/EntityAvatar.vue'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppFormField from '@/components/ui/AppFormField.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppSelect from '@/components/ui/AppSelect.vue'
import DatePicker from '@/components/ui/DatePicker.vue'
import * as clientesApi from '@/api/clientes.api'
import { registrarMascota, getEspecies, getRazasPorEspecie } from '@/api/mascotas.api'
import { formatearCedula } from '@/utils/cedula'

const router = useRouter()
const route = useRoute()

/* ─── Cliente ─── */
const clienteDocumento = ref(route.query.clienteDocumento || '')
const clienteNombre = ref(route.query.clienteNombre || '')
const clienteFiltro = ref('')
const resultados = ref([])
const buscando = ref(false)
const buscadorClienteRef = ref(null)
let debounce = null

watch(clienteFiltro, (val) => {
  if (clienteDocumento.value) return
  clearTimeout(debounce)
  if (!val || val.trim().length < 2) {
    resultados.value = []
    return
  }
  debounce = setTimeout(async () => {
    buscando.value = true
    try {
      // Si el usuario escribió algo tipo "v12345678" o "12345678", normalizamos
      const limpio = val.trim()
      const esCedula = /^[VEJPGvejpg]-?\d{4,}$/.test(limpio) || /^\d{4,}$/.test(limpio)
      const query = esCedula ? formatearCedula(limpio) : limpio

      const { data } = await clientesApi.getAll(query)
      resultados.value = data
    } catch {
      resultados.value = []
    } finally {
      buscando.value = false
    }
  }, 350)
})

function seleccionarCliente(c) {
  clienteDocumento.value = c.documentoIdentidad
  clienteNombre.value = c.nombreCompleto
  clienteFiltro.value = ''
  resultados.value = []
}
function cambiarCliente() {
  clienteDocumento.value = ''
  clienteNombre.value = ''
  nextTick(() => buscadorClienteRef.value?.focus())
}

/* ─── Catálogos ─── */
const especies = ref([])
const razas = ref([])
const cargandoRazas = ref(false)

/* ─── Formulario ─── */
const form = ref({
  nombre: '',
  idEspecie: '',
  idRaza: '',
  sexo: '',
  fechaNacimiento: '',
  color: '',
  pesoActual: '',
  esterilizado: false,
})
const erroresCampo = ref({})
const guardando = ref(false)
const error = ref('')
const exito = ref(null)

const hoy = new Date().toISOString().split('T')[0]

const especieSeleccionada = computed(() =>
  especies.value.find((e) => e.id === form.value.idEspecie) || null
)

/* ─── Progreso ─── */
const totalObligatorios = 3
const completadosObligatorios = computed(() => {
  let n = 0
  if (form.value.nombre.trim()) n++
  if (form.value.idEspecie) n++
  if (form.value.sexo) n++
  return n
})
const progreso = computed(() =>
  Math.round((completadosObligatorios.value / totalObligatorios) * 100)
)
const puedeGuardar = computed(() =>
  Boolean(clienteDocumento.value) && completadosObligatorios.value === totalObligatorios
)

/* ─── Carga de razas al cambiar especie ─── */
watch(() => form.value.idEspecie, async (id) => {
  form.value.idRaza = ''
  razas.value = []
  if (!id) return
  cargandoRazas.value = true
  try {
    const { data } = await getRazasPorEspecie(id)
    razas.value = data
  } catch { /* vacío */ }
  finally { cargandoRazas.value = false }
})

/* ─── Formato de fecha corta para preview ─── */
const MESES_CORTOS = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']
function formatFechaCorta(iso) {
  if (!iso) return ''
  const d = new Date(iso + 'T12:00:00')
  return `${d.getDate()} ${MESES_CORTOS[d.getMonth()]} ${d.getFullYear()}`
}

/* ─── Navegación ─── */
function volver() {
  router.push('/recepcion/dashboard')
}
function irAgendarCita() {
  router.push({
    path: '/recepcion/citas/nueva',
    query: {
      clienteDocumento: exito.value.documentoIdentidadCliente || clienteDocumento.value,
      clienteNombre: exito.value.nombreCliente,
    },
  })
}

/* ─── Validación + submit ─── */
function validar() {
  erroresCampo.value = {}
  if (!clienteDocumento.value) {
    error.value = 'Debe seleccionar el cliente dueño de la mascota.'
    return false
  }
  if (!form.value.nombre.trim()) erroresCampo.value.nombre = true
  if (!form.value.idEspecie) erroresCampo.value.idEspecie = true

  if (Object.keys(erroresCampo.value).length > 0) {
    error.value = 'Complete los campos obligatorios marcados en rojo.'
    return false
  }
  return true
}

async function guardar() {
  error.value = ''
  if (!validar()) return
  guardando.value = true
  try {
    const { data } = await registrarMascota({
      documentoIdentidadCliente: clienteDocumento.value,
      idEspecie: parseInt(form.value.idEspecie),
      idRaza: form.value.idRaza ? parseInt(form.value.idRaza) : null,
      nombre: form.value.nombre.trim(),
      fechaNacimiento: form.value.fechaNacimiento || null,
      sexo: form.value.sexo,
      color: form.value.color.trim() || null,
      pesoActual: form.value.pesoActual ? parseFloat(form.value.pesoActual) : null,
      esterilizado: form.value.esterilizado,
    })
    exito.value = data
  } catch (err) {
    error.value = err.response?.data?.message || 'Error al registrar la mascota.'
  } finally {
    guardando.value = false
  }
}

/* ─── Init ─── */
onMounted(async () => {
  try {
    const { data } = await getEspecies()
    especies.value = data
  } catch { /* vacío */ }
  // Si venimos con query, precargamos al cliente sin esperar al watcher
  if (clienteDocumento.value) {
    // nada más, ya está seteado
  }
})
</script>

<style scoped>
.registrar-mascota {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-6) var(--space-16);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* ═══ BREADCRUMB ═══ */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-md);
  color: var(--text-secondary);
}
.bc-back {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--neutral-600);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  margin-right: 2px;
}
.bc-back:hover {
  background: var(--neutral-100);
  color: var(--brand-700);
  border-color: var(--border-strong);
}
.bc-item { font-weight: var(--font-medium); }
.bc-link {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
  font-size: var(--text-md);
  font-family: inherit;
}
.bc-link:hover { color: var(--brand-700); text-decoration: underline; }
.bc-current { color: var(--text-primary); font-weight: var(--font-bold); }
.bc-sep { color: var(--neutral-300); }

/* ═══ Progress pill en el hero ═══ */
.progress-pill {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
  white-space: nowrap;
}
.progress-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--warning-500);
  transition: background-color var(--duration-base) var(--ease-out);
}
.progress-dot.is-complete { background: var(--success-500); }

/* ═══ Layout principal ═══ */
.form-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: var(--space-5);
  align-items: start;
}
.form-main {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  min-width: 0;
}

/* ═══ Sección con número ═══ */
.form-section {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-xs);
}
.section-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-subtle);
  background: linear-gradient(90deg, var(--neutral-50) 0%, var(--bg-surface) 60%);
}
.section-number {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  background: var(--brand-50);
  color: var(--brand-700);
  border: 1px solid var(--brand-200);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  flex-shrink: 0;
}
.section-title {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
.section-sub {
  margin: 3px 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.section-body {
  padding: var(--space-5) var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.section-body :deep(.form-group) { margin-bottom: 0; }

/* ═══ Cliente chip ═══ */
.cliente-chip {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--brand-50);
  border: 1px solid var(--brand-200);
  border-radius: var(--radius-xl);
}
.cliente-chip-info { flex: 1; min-width: 0; }
.cliente-chip-name {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cliente-chip-doc {
  margin: 2px 0 0;
  font-size: var(--text-sm);
  color: var(--brand-700);
  font-weight: var(--font-bold);
}
.mono { font-family: var(--font-mono); }
.btn-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  background: var(--bg-surface);
  border: 1px solid var(--brand-200);
  color: var(--brand-700);
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--duration-fast) var(--ease-out);
  flex-shrink: 0;
}
.btn-chip:hover { background: var(--brand-100); border-color: var(--brand-700); }

/* ═══ Search bar ═══ */
.search-bar { position: relative; }
.search-icon {
  position: absolute;
  left: var(--space-4);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  pointer-events: none;
}
.search-input { padding-left: 40px; padding-right: 40px; }
.search-spinner,
.search-clear {
  position: absolute;
  right: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  color: var(--brand-700);
}
.search-clear {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
}
.search-clear:hover { color: var(--neutral-600); background: var(--neutral-100); }

.results-list {
  margin-top: var(--space-3);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  max-height: 300px;
  overflow-y: auto;
}
.result-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-3);
  align-items: center;
  width: 100%;
  text-align: left;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface);
  border: none;
  border-bottom: 1px solid var(--neutral-100);
  cursor: pointer;
  font-family: inherit;
  transition: background-color var(--duration-fast) var(--ease-out);
}
.result-row:last-child { border-bottom: none; }
.result-row:hover { background: var(--brand-50); }
.result-info { min-width: 0; }
.result-name {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.result-meta {
  margin: 2px 0 0;
  font-size: var(--text-sm);
  color: var(--brand-700);
  font-weight: var(--font-semibold);
}
.result-arrow { color: var(--neutral-300); flex-shrink: 0; }
.result-row:hover .result-arrow { color: var(--brand-700); }

/* ═══ Segmented control (Sexo) ═══ */
.segmented {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-1);
  padding: var(--space-1);
  background: var(--neutral-100);
  border-radius: var(--radius-lg);
}
.segment {
  padding: var(--space-3);
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  font-family: inherit;
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.segment:hover { color: var(--brand-700); }
.segment.is-active {
  background: var(--bg-surface);
  color: var(--brand-700);
  font-weight: var(--font-bold);
  box-shadow: var(--shadow-xs);
}

/* ═══ Especies (chips con inicial) ═══ */
.species-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: var(--space-2);
}
.species-chip {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface);
  border: 1.5px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: all var(--duration-base) var(--ease-out);
}
.species-chip:hover {
  border-color: var(--brand-200);
  background: var(--brand-50);
}
.species-chip.is-selected {
  border-color: var(--brand-700);
  background: var(--brand-50);
  box-shadow: 0 0 0 3px var(--brand-100);
}
.species-inicial {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-lg);
  background: var(--brand-50);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  flex-shrink: 0;
  transition: background-color var(--duration-base) var(--ease-out),
              color var(--duration-base) var(--ease-out);
}
.species-chip.is-selected .species-inicial {
  background: var(--brand-700);
  color: var(--text-inverse);
}
.species-name {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.species-check { color: var(--brand-700); flex-shrink: 0; }

/* ═══ Grid 2 columnas ═══ */
.form-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}
.form-grid-2 :deep(.form-group) { margin-bottom: 0; }

/* ═══ Checkbox ═══ */
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
}
.check-row:hover { border-color: var(--brand-200); background: var(--brand-50); }
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
  transition: all var(--duration-fast) var(--ease-out);
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

/* ═══ Sidebar ═══ */
.form-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  position: sticky;
  top: var(--space-6);
}
.preview-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.preview-header {
  padding: var(--space-4) var(--space-5) var(--space-3);
  border-bottom: 1px solid var(--border-subtle);
}
.preview-header h4 {
  margin: 0;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.preview-hint {
  display: block;
  margin-top: 2px;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}
.preview-body {
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
}
.preview-nombre {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: var(--leading-snug);
  word-break: break-word;
}
.preview-nombre.is-empty {
  color: var(--text-tertiary);
  font-weight: var(--font-medium);
  font-style: italic;
}
.preview-badges {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-1);
  justify-content: center;
  min-height: 22px;
}
.preview-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  background: var(--brand-50);
  border: 1px solid var(--brand-200);
  color: var(--brand-700);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
}
.preview-meta {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  margin-top: var(--space-1);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-subtle);
}
.preview-line {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--neutral-700);
  text-align: left;
  line-height: var(--leading-snug);
}
.preview-line svg { color: var(--text-tertiary); flex-shrink: 0; }
.preview-line span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.preview-line.line-success { color: var(--success-600); }
.preview-line.line-success svg { color: var(--success-500); }

.preview-footer {
  padding: var(--space-4) var(--space-5) var(--space-5);
  background: linear-gradient(180deg, var(--neutral-50) 0%, var(--brand-50) 100%);
  border-top: 1px solid var(--border-subtle);
}
.progress-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}
.progress-label {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}
.progress-count {
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  font-variant-numeric: tabular-nums;
}
.progress-bar {
  height: 6px;
  background: var(--neutral-200);
  border-radius: var(--radius-full);
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--brand-700), var(--brand-500));
  border-radius: var(--radius-full);
  transition: width var(--duration-slow) var(--ease-out);
}
.progress-fill.is-complete {
  background: linear-gradient(90deg, var(--success-600), var(--success-500));
}

/* ═══ Nota de proceso ═══ */
.process-note {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--brand-50);
  border: 1px solid var(--brand-200);
  border-radius: var(--radius-xl);
}
.process-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: var(--bg-surface);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.process-title {
  margin: 0 0 2px;
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  color: var(--brand-700);
}
.process-desc {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--neutral-700);
  line-height: var(--leading-snug);
}

/* ═══ Footer sticky ═══ */
.form-actions {
  position: sticky;
  bottom: var(--space-4);
  z-index: var(--z-sticky);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-5);
  border-radius: var(--radius-2xl);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-lg);
  flex-wrap: wrap;
}
.form-actions :deep(.btn) { min-width: 140px; }

/* ═══ Empty inline ═══ */
.empty-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-5);
  margin-top: var(--space-3);
  color: var(--text-tertiary);
  text-align: center;
  font-size: var(--text-md);
  background: var(--bg-surface-alt);
  border: 1px dashed var(--border-subtle);
  border-radius: var(--radius-lg);
}
.empty-inline p {
  margin: 0;
  max-width: 380px;
  line-height: var(--leading-normal);
}
.link-teal {
  color: var(--brand-700);
  font-weight: var(--font-bold);
  text-decoration: none;
}
.link-teal:hover { text-decoration: underline; }

/* ═══ Éxito ═══ */
.success-container {
  background: var(--bg-surface);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  padding: var(--space-12) var(--space-8);
  max-width: 640px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
}
.success-icon {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--success-50) 0%, var(--brand-50) 100%);
  border: 3px solid var(--success-500);
  color: var(--success-600);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
  animation: successPop 0.5s var(--ease-spring);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}
.success-title {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0;
  letter-spacing: var(--tracking-tight);
}
.success-message {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0 0 var(--space-2);
  line-height: var(--leading-relaxed);
  max-width: 460px;
}
.success-message strong { color: var(--text-primary); font-weight: var(--font-bold); }
.success-resumen {
  width: 100%;
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-1) var(--space-4);
  margin: var(--space-2) 0;
}
.resumen-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-subtle);
  font-size: var(--text-md);
}
.resumen-fila:last-child { border-bottom: none; }
.resumen-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  font-weight: var(--font-semibold);
  white-space: nowrap;
  font-size: var(--text-sm);
}
.resumen-value {
  color: var(--text-primary);
  font-weight: var(--font-bold);
  text-align: right;
  word-break: break-word;
}
.success-actions {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-3);
  flex-wrap: wrap;
  justify-content: center;
  width: 100%;
  max-width: 460px;
}

/* ═══ Transiciones ═══ */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: opacity var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ═══ Responsive ═══ */
@media (max-width: 1024px) {
  .form-layout { grid-template-columns: 1fr; }
  .form-sidebar { position: static; }
}
@media (max-width: 640px) {
  .registrar-mascota { padding: var(--space-4); }
  .form-grid-2 { grid-template-columns: 1fr; }
  .section-header { padding: var(--space-4) var(--space-5); }
  .section-body { padding: var(--space-4) var(--space-5); }
  .form-actions { flex-direction: column-reverse; padding: var(--space-3); }
  .form-actions :deep(.btn) { width: 100%; }
  .success-container { padding: var(--space-8) var(--space-5); }
  .success-icon { width: 72px; height: 72px; }
  .success-title { font-size: var(--text-3xl); }
  .success-actions { flex-direction: column; }
  .resumen-fila { flex-direction: column; align-items: flex-start; gap: var(--space-1); }
  .resumen-value { text-align: left; }
}
</style>