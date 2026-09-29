<template>
  <div class="registrar-mascota">
    <!-- ═══ BREADCRUMB ═══ -->
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

    <!-- ═══ HERO ═══ -->
    <header class="hero">
      <p class="hero-eyebrow">
        <Sparkles :size="12" />
        Recepción · Registro asistido
      </p>
      <h1>Registrar Mascota</h1>
      <p class="hero-sub">
        Asocia un nuevo paciente a un cliente existente para agendar consultas.
      </p>
    </header>

    <!-- ═══ LAYOUT ═══ -->
    <div class="wizard-layout" :class="{ 'is-success': exito }">
      <main class="wizard-main">
        <!-- ══════ ESTADO ÉXITO ══════ -->
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
            <button class="btn-primary" type="button" @click="irAgendarCita">
              <CalendarPlus :size="15" /> Agendar cita ahora
            </button>
            <button class="btn-secondary" type="button" @click="volver">
              <ArrowLeft :size="15" /> Volver al panel
            </button>
          </div>
        </section>

        <!-- ══════ FORMULARIO ══════ -->
        <template v-else>
          <form @submit.prevent="guardar" novalidate>
            <!-- Card: Cliente dueño -->
            <section class="card">
              <header class="card-header">
                <div class="card-header-left">
                  <div class="card-icon"><User :size="16" /></div>
                  <div>
                    <h3>Cliente dueño</h3>
                    <p class="card-header-sub">
                      {{ clienteDocumento ? 'Cliente seleccionado' : 'Busca por nombre o documento' }}
                    </p>
                  </div>
                </div>
              </header>
              <div class="card-body">
                <!-- Cliente seleccionado -->
                <div v-if="clienteDocumento" class="cliente-chip">
                  <EntityAvatar :nombre="clienteNombre" tipo="cliente" size="md" />
                  <div class="cliente-chip-info">
                    <p class="cliente-chip-name">{{ clienteNombre }}</p>
                    <p class="cliente-chip-doc">{{ clienteDocumento }}</p>
                  </div>
                  <button type="button" class="btn-chip" @click="cambiarCliente">
                    <X :size="13" /> Cambiar
                  </button>
                </div>

                <!-- Búsqueda -->
                <template v-else>
                  <div class="search-box">
                    <Search :size="15" class="search-icon" />
                    <input
                      v-model="clienteFiltro"
                      type="text"
                      class="search-input"
                      placeholder="Ej: María González o V-12345678…"
                      autocomplete="off"
                    />
                    <Loader2 v-if="buscando" :size="14" class="search-spinner spin" />
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

                  <div
                    v-if="clienteFiltro.length >= 2 && !buscando && resultados.length === 0"
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

                  <div v-if="resultados.length > 0" class="results-list">
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
                        <p class="result-meta">{{ c.documentoIdentidad }}</p>
                      </div>
                      <ChevronRight :size="16" class="result-arrow" />
                    </button>
                  </div>
                </template>
              </div>
            </section>

            <!-- Card: Datos de la mascota -->
            <section class="card">
              <header class="card-header">
                <div class="card-header-left">
                  <div class="card-icon"><PawPrint :size="16" /></div>
                  <div>
                    <h3>Datos de la mascota</h3>
                    <p class="card-header-sub">Información básica del paciente</p>
                  </div>
                </div>
              </header>
              <div class="card-body">
                <!-- Error general -->
                <Transition name="slide-fade">
                  <div v-if="error" class="alert alert-error">
                    <AlertTriangle :size="16" />
                    <span>{{ error }}</span>
                  </div>
                </Transition>

                <!-- Nombre -->
                <div class="form-group">
                  <label class="form-label" for="nombre">
                    Nombre <span class="required">*</span>
                  </label>
                  <input
                    id="nombre"
                    v-model="form.nombre"
                    type="text"
                    maxlength="50"
                    class="form-input"
                    placeholder="Ej: Rocky"
                  />
                </div>

                <!-- Sexo: 2 pick cards -->
                <div class="form-group">
                  <label class="form-label">
                    Sexo <span class="required">*</span>
                  </label>
                  <div class="sex-picks">
                    <button
                      type="button"
                      class="sex-pick"
                      :class="{ selected: form.sexo === 'M' }"
                      @click="form.sexo = 'M'"
                    >
                      <span class="sex-icon sex-m">
                        <Mars :size="16" />
                      </span>
                      <span class="sex-info">
                        <span class="sex-name">Macho</span>
                        <span class="sex-hint">♂</span>
                      </span>
                      <span v-if="form.sexo === 'M'" class="pick-check">
                        <CheckCircle2 :size="14" />
                      </span>
                    </button>
                    <button
                      type="button"
                      class="sex-pick"
                      :class="{ selected: form.sexo === 'H' }"
                      @click="form.sexo = 'H'"
                    >
                      <span class="sex-icon sex-h">
                        <Venus :size="16" />
                      </span>
                      <span class="sex-info">
                        <span class="sex-name">Hembra</span>
                        <span class="sex-hint">♀</span>
                      </span>
                      <span v-if="form.sexo === 'H'" class="pick-check">
                        <CheckCircle2 :size="14" />
                      </span>
                    </button>
                  </div>
                </div>

                <!-- Especie: pick cards -->
                <div class="form-group">
                  <label class="form-label">
                    Especie <span class="required">*</span>
                  </label>
                  <div class="species-picks">
                    <button
                      v-for="e in especies"
                      :key="e.id"
                      type="button"
                      class="species-pick"
                      :class="{ selected: form.idEspecie === e.id }"
                      @click="form.idEspecie = e.id"
                    >
                      <span class="species-inicial">
                        {{ e.nombre.charAt(0).toUpperCase() }}
                      </span>
                      <span class="species-name">{{ e.nombre }}</span>
                      <span v-if="form.idEspecie === e.id" class="pick-check">
                        <CheckCircle2 :size="14" />
                      </span>
                    </button>
                  </div>
                </div>

                <!-- Raza -->
                <div class="form-group">
                  <label class="form-label" for="raza">
                    Raza <span class="optional">(opcional)</span>
                  </label>
                  <select
                    id="raza"
                    v-model="form.idRaza"
                    class="form-select"
                    :disabled="!form.idEspecie || cargandoRazas"
                  >
                    <option value="">
                      {{ !form.idEspecie
                        ? 'Selecciona primero una especie'
                        : (cargandoRazas ? 'Cargando razas…' : 'Seleccione una raza') }}
                    </option>
                    <option v-for="r in razas" :key="r.id" :value="r.id">{{ r.nombre }}</option>
                  </select>
                </div>

                <!-- Fecha y peso -->
                <div class="form-grid-2">
                  <div class="form-group">
                    <label class="form-label" for="nacimiento">
                      Fecha de nacimiento <span class="optional">(opcional)</span>
                    </label>
                    <input
                      id="nacimiento"
                      v-model="form.fechaNacimiento"
                      type="date"
                      :max="hoy"
                      class="form-input"
                    />
                  </div>
                  <div class="form-group">
                    <label class="form-label" for="peso">
                      Peso actual (kg) <span class="optional">(opcional)</span>
                    </label>
                    <input
                      id="peso"
                      v-model="form.pesoActual"
                      type="number"
                      step="0.01"
                      min="0.01"
                      max="999.99"
                      class="form-input"
                      placeholder="Ej: 12.50"
                    />
                  </div>
                </div>

                <!-- Color -->
                <div class="form-group">
                  <label class="form-label" for="color">
                    Color <span class="optional">(opcional)</span>
                  </label>
                  <input
                    id="color"
                    v-model="form.color"
                    type="text"
                    maxlength="30"
                    class="form-input"
                    placeholder="Ej: Marrón con manchas blancas"
                  />
                </div>

                <!-- Esterilizado -->
                <label class="check-row">
                  <input v-model="form.esterilizado" type="checkbox" class="checkbox-input" />
                  <span class="checkbox-box">
                    <Check v-if="form.esterilizado" :size="12" />
                  </span>
                  <span class="checkbox-label">
                    ¿La mascota está esterilizada?
                  </span>
                </label>
              </div>
            </section>

            <!-- Acciones -->
            <div class="form-actions">
              <button type="button" class="btn-cancel" :disabled="guardando" @click="volver">
                <X :size="15" /> Cancelar
              </button>
              <button type="submit" class="btn-next" :disabled="guardando">
                <Loader2 v-if="guardando" :size="15" class="spin" />
                <Save v-else :size="15" />
                {{ guardando ? 'Guardando…' : 'Guardar mascota' }}
              </button>
            </div>
          </form>
        </template>
      </main>

      <!-- ═══ SIDEBAR VISTA PREVIA ═══ -->
      <aside v-if="!exito" class="wizard-sidebar">
        <div class="preview-card">
          <header class="preview-header">
            <h4>Vista previa</h4>
            <span class="preview-hint">Así se verá la ficha</span>
          </header>

          <div class="preview-body">
            <PetAvatar size="lg" />

            <p class="preview-nombre" :class="{ 'is-empty': !form.nombre }">
              {{ form.nombre || 'Nombre de la mascota' }}
            </p>

            <div class="preview-badges">
              <span
                v-if="especieSeleccionada"
                class="preview-badge"
              >
                {{ especieSeleccionada.nombre }}
              </span>
              <span
                v-if="form.sexo"
                class="preview-badge"
                :class="form.sexo === 'M' ? 'badge-m' : 'badge-h'"
              >
                {{ form.sexo === 'M' ? 'Macho' : 'Hembra' }}
              </span>
            </div>

            <div class="preview-meta">
              <div
                v-if="clienteNombre"
                class="preview-line"
              >
                <User :size="13" />
                <span>{{ clienteNombre }}</span>
              </div>
              <div
                v-if="form.pesoActual"
                class="preview-line"
              >
                <Weight :size="13" />
                <span>{{ form.pesoActual }} kg</span>
              </div>
              <div
                v-if="form.fechaNacimiento"
                class="preview-line"
              >
                <CalendarDays :size="13" />
                <span>{{ fmtFechaCorta(form.fechaNacimiento) }}</span>
              </div>
              <div
                v-if="form.esterilizado"
                class="preview-line line-success"
              >
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
                :class="{ 'is-complete': progresoObligatorio === 100 }"
                :style="{ width: progresoObligatorio + '%' }"
              />
            </div>
            <p class="progress-note">
              <component :is="progresoObligatorio === 100 ? CheckCircle2 : Info" :size="12" />
              <span v-if="progresoObligatorio === 100">Listo para registrar</span>
              <span v-else>
                Faltan {{ totalObligatorios - completadosObligatorios }} campo{{
                  totalObligatorios - completadosObligatorios === 1 ? '' : 's'
                }}
              </span>
            </p>
          </footer>
        </div>

        <div class="process-note">
          <div class="process-icon"><CalendarPlus :size="16" /></div>
          <div>
            <p class="process-title">¿Ya tienes la cita?</p>
            <p class="process-desc">
              Después de registrar la mascota podrás agendar una cita directamente
              sin volver al panel.
            </p>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  PawPrint, Search, AlertTriangle, CheckCircle2, Loader2, CalendarPlus,
  ArrowLeft, ChevronRight, X, User, CreditCard, Sparkles, Info, Save,
  Dog, Weight, CalendarDays, Mars, Venus, Check
} from 'lucide-vue-next'
import PetAvatar from '@/components/ui/PetAvatar.vue'
import EntityAvatar from '@/components/ui/EntityAvatar.vue'
import * as clientesApi from '@/api/clientes.api'
import { registrarMascota, getEspecies, getRazasPorEspecie } from '@/api/mascotas.api'

const router = useRouter()
const route = useRoute()

// ─── Cliente ───
const clienteDocumento = ref(route.query.clienteDocumento || '')
const clienteNombre = ref(route.query.clienteNombre || '')
const clienteFiltro = ref('')
const resultados = ref([])
const buscando = ref(false)
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
      const { data } = await clientesApi.getAll(val.trim())
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
}

// ─── Formulario mascota ───
const especies = ref([])
const razas = ref([])
const cargandoRazas = ref(false)
const guardando = ref(false)
const error = ref('')
const exito = ref(null)

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

const hoy = new Date().toISOString().split('T')[0]

// ─── Computeds ───
const especieSeleccionada = computed(() =>
  especies.value.find(e => e.id === form.value.idEspecie) || null
)

const totalObligatorios = 3

const completadosObligatorios = computed(() => {
  let n = 0
  if (form.value.nombre.trim()) n++
  if (form.value.idEspecie) n++
  if (form.value.sexo) n++
  return n
})

const progresoObligatorio = computed(() =>
  Math.round((completadosObligatorios.value / totalObligatorios) * 100)
)

// ─── Watch especie → razas ───
watch(() => form.value.idEspecie, async (id) => {
  form.value.idRaza = ''
  razas.value = []
  if (!id) return
  cargandoRazas.value = true
  try {
    const { data } = await getRazasPorEspecie(id)
    razas.value = data
  } catch {
    /* vacío */
  } finally {
    cargandoRazas.value = false
  }
})

// ─── Helpers visuales ───
function fmtFechaCorta(iso) {
  if (!iso) return ''
  const MESES = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']
  const d = new Date(iso + 'T12:00:00')
  return `${d.getDate()} ${MESES[d.getMonth()]} ${d.getFullYear()}`
}

// ─── Acciones ───
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

async function guardar() {
  error.value = ''
  if (!clienteDocumento.value) {
    error.value = 'Debe seleccionar el cliente dueño de la mascota.'
    return
  }
  if (!form.value.nombre.trim() || !form.value.idEspecie || !form.value.sexo) {
    error.value = 'Debe completar todos los campos obligatorios.'
    return
  }
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

onMounted(async () => {
  try {
    const { data } = await getEspecies()
    especies.value = data
  } catch {
    /* vacío */
  }
})
</script>

<style scoped>
.registrar-mascota {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 24px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
}
button { font-family: inherit; }

/* ═══ BREADCRUMB ═══ */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 13px;
  color: #64748B;
}
.bc-back {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: 1px solid #E2E8F0;
  background: #fff;
  color: #475569;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .15s ease;
  margin-right: 2px;
}
.bc-back:hover { background: #F1F5F9; color: #0F766E; border-color: #CBD5E1; }
.bc-item { font-weight: 500; }
.bc-link {
  background: none;
  border: none;
  color: #64748B;
  cursor: pointer;
  padding: 0;
  font-size: 13px;
}
.bc-link:hover { color: #0F766E; text-decoration: underline; }
.bc-current { color: #1E293B; font-weight: 700; }
.bc-sep { color: #CBD5E1; }

/* ═══ HERO ═══ */
.hero {
  padding: 24px 28px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
}
.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 8px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .7px;
  color: #0F766E;
  background: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  border: 1px solid #CCFBF1;
}
.hero h1 {
  margin: 0 0 4px;
  font-size: 26px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.02em;
  line-height: 1.15;
}
.hero-sub {
  margin: 0;
  font-size: 14px;
  color: #64748B;
  max-width: 620px;
  line-height: 1.5;
}

/* ═══ LAYOUT ═══ */
.wizard-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 20px;
  align-items: start;
}
.wizard-layout.is-success { grid-template-columns: 1fr; }
.wizard-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.wizard-sidebar {
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: sticky;
  top: 24px;
}

/* ═══ CARDS ═══ */
.card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  overflow: hidden;
}
.card-header {
  padding: 16px 22px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.card-header-left { display: flex; align-items: center; gap: 12px; min-width: 0; }
.card-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: #F0FDFA;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.card-header h3 {
  margin: 0;
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.2;
}
.card-header-sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748B;
}
.card-body { padding: 20px 22px 22px; }

/* ═══ CLIENTE CHIP ═══ */
.cliente-chip {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #F0FDFA;
  border: 1px solid #99F6E4;
  border-radius: 12px;
}
.cliente-chip-info { flex: 1; min-width: 0; }
.cliente-chip-name {
  margin: 0;
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cliente-chip-doc {
  margin: 2px 0 0;
  font-size: 12.5px;
  color: #0F766E;
  font-weight: 600;
}
.btn-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: #fff;
  border: 1px solid #99F6E4;
  color: #0F766E;
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: all .2s ease;
  flex-shrink: 0;
}
.btn-chip:hover { background: #CCFBF1; border-color: #0F766E; }

/* ═══ SEARCH BOX ═══ */
.search-box { position: relative; }
.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #94A3B8;
  pointer-events: none;
}
.search-input {
  width: 100%;
  padding: 11px 40px 11px 40px;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  font-size: 13.5px;
  color: #1E293B;
  background: #fff;
  font-family: inherit;
  outline: none;
  box-sizing: border-box;
  transition: all .2s;
}
.search-input::placeholder { color: #94A3B8; }
.search-input:focus {
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.search-spinner {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #0F766E;
}
.search-clear {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #94A3B8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 5px;
  border-radius: 6px;
  transition: all .15s;
}
.search-clear:hover { color: #475569; background: #F1F5F9; }

/* ═══ RESULTS LIST ═══ */
.results-list {
  margin-top: 10px;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  overflow: hidden;
  max-height: 300px;
  overflow-y: auto;
}
.result-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  width: 100%;
  text-align: left;
  padding: 12px 14px;
  background: #fff;
  border: none;
  border-bottom: 1px solid #F1F5F9;
  cursor: pointer;
  font-family: inherit;
  transition: background-color .15s;
}
.result-row:last-child { border-bottom: none; }
.result-row:hover { background: #F0FDFA; }
.result-info { min-width: 0; }
.result-name {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.result-meta {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748B;
}
.result-arrow { color: #CBD5E1; flex-shrink: 0; transition: color .2s ease; }
.result-row:hover .result-arrow { color: #0F766E; }

/* ═══ FORM ═══ */
.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 18px; }
.form-group:last-child { margin-bottom: 0; }
.form-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 600;
  color: #374151;
}
.required { color: #EF4444; }
.optional {
  color: #94A3B8;
  font-weight: 500;
  font-size: 11.5px;
  margin-left: 2px;
}
.form-input,
.form-select {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  font-size: 14px;
  color: #1E293B;
  background: #fff;
  font-family: inherit;
  transition: border-color .2s, box-shadow .2s;
  box-sizing: border-box;
  appearance: none;
  -webkit-appearance: none;
}
.form-select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  padding-right: 40px;
  cursor: pointer;
}
.form-select:disabled {
  background: #F8FAFC;
  color: #94A3B8;
  cursor: not-allowed;
}
.form-input::placeholder { color: #94A3B8; }
.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}

.form-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

/* ═══ SEX PICKS ═══ */
.sex-picks {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.sex-pick {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #fff;
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: all .2s ease;
}
.sex-pick:hover {
  border-color: #99F6E4;
  background: #F0FDFA;
}
.sex-pick.selected {
  border-color: #0F766E;
  background: #F0FDFA;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.sex-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
}
.sex-m { background: #3B82F6; }
.sex-h { background: #EC4899; }
.sex-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  flex: 1;
  min-width: 0;
}
.sex-name {
  font-size: 14px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.2;
}
.sex-hint {
  font-size: 11.5px;
  color: #64748B;
  font-weight: 500;
}
.pick-check {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #0F766E;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  animation: checkPop .25s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes checkPop {
  0% { transform: scale(0); }
  100% { transform: scale(1); }
}

/* ═══ SPECIES PICKS ═══ */
.species-picks {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 8px;
}
.species-pick {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background: #fff;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: all .2s ease;
}
.species-pick:hover {
  border-color: #99F6E4;
  background: #F0FDFA;
}
.species-pick.selected {
  border-color: #0F766E;
  background: #F0FDFA;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.species-inicial {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #F0FDFA;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
  transition: background-color .2s ease, color .2s ease;
}
.species-pick.selected .species-inicial {
  background: #0F766E;
  color: #fff;
}
.species-name {
  font-size: 13px;
  font-weight: 700;
  color: #0F172A;
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ═══ CHECKBOX ═══ */
.check-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  margin-top: 18px;
  background: #F8FAFC;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  cursor: pointer;
  font-family: inherit;
  transition: all .2s ease;
}
.check-row:hover { border-color: #99F6E4; background: #F0FDFA; }
.checkbox-input { display: none; }
.checkbox-box {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  border: 2px solid #CBD5E1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  flex-shrink: 0;
  transition: all .2s ease;
  color: #fff;
}
.check-row .checkbox-input:checked + .checkbox-box {
  background: #0F766E;
  border-color: #0F766E;
}
.checkbox-label {
  font-size: 13.5px;
  font-weight: 600;
  color: #1E293B;
}

/* ═══ ALERTA ═══ */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  font-weight: 500;
  margin-bottom: 16px;
}
.alert-error { background: #FEF2F2; color: #991B1B; border: 1px solid #FECACA; }

/* ═══ EMPTY INLINE ═══ */
.empty-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  margin-top: 10px;
  color: #94A3B8;
  text-align: center;
  font-size: 13px;
  background: #F8FAFC;
  border: 1px dashed #E2E8F0;
  border-radius: 10px;
}
.empty-inline p { margin: 0; max-width: 380px; line-height: 1.5; }
.link-teal {
  color: #0F766E;
  font-weight: 700;
  text-decoration: none;
}
.link-teal:hover { text-decoration: underline; }

/* ═══ FORM ACTIONS ═══ */
.form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 22px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03);
}
.btn-cancel,
.btn-next {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 11px 22px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
}
.btn-cancel {
  background: none;
  border: 1px solid #E2E8F0;
  color: #64748B;
}
.btn-cancel:hover:not(:disabled) {
  background: #FEF2F2;
  border-color: #FECACA;
  color: #EF4444;
}
.btn-next {
  background: #0F766E;
  border: none;
  color: #fff;
}
.btn-next:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-next:disabled,
.btn-cancel:disabled { opacity: .55; cursor: not-allowed; }

/* ═══ PREVIEW CARD ═══ */
.preview-card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  overflow: hidden;
}
.preview-header {
  padding: 14px 18px 10px;
  border-bottom: 1px solid #F1F5F9;
}
.preview-header h4 {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: #0F172A;
  text-transform: uppercase;
  letter-spacing: .5px;
}
.preview-hint {
  display: block;
  margin-top: 2px;
  font-size: 11px;
  color: #94A3B8;
}
.preview-body {
  padding: 20px 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
}
.preview-nombre {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
  line-height: 1.3;
  word-break: break-word;
}
.preview-nombre.is-empty {
  color: #94A3B8;
  font-weight: 500;
  font-style: italic;
}

.preview-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
  min-height: 22px;
}
.preview-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 20px;
  background: #F0FDFA;
  border: 1px solid #CCFBF1;
  color: #0F766E;
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: .1px;
}
.preview-badge.badge-m { background: #EFF6FF; border-color: #BFDBFE; color: #2563EB; }
.preview-badge.badge-h { background: #FDF2F8; border-color: #FBCFE8; color: #DB2777; }

.preview-meta {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 4px;
  padding-top: 14px;
  border-top: 1px solid #F1F5F9;
}
.preview-line {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: #334155;
  text-align: left;
  line-height: 1.4;
}
.preview-line svg { color: #94A3B8; flex-shrink: 0; }
.preview-line span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.preview-line.line-success { color: #059669; }
.preview-line.line-success svg { color: #059669; }

.preview-footer {
  padding: 14px 18px 18px;
  background: linear-gradient(180deg, #F8FAFC 0%, #F0FDFA 100%);
  border-top: 1px solid #E2E8F0;
}
.progress-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.progress-label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
}
.progress-count {
  font-size: 12px;
  font-weight: 700;
  color: #0F766E;
  font-variant-numeric: tabular-nums;
}
.progress-bar {
  height: 6px;
  background: #E2E8F0;
  border-radius: 999px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #0F766E, #14B8A6);
  border-radius: 999px;
  transition: width .35s cubic-bezier(0.16, 1, 0.3, 1);
}
.progress-fill.is-complete {
  background: linear-gradient(90deg, #059669, #10B981);
}
.progress-note {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 10px 0 0;
  font-size: 11.5px;
  color: #64748B;
  line-height: 1.4;
}
.progress-note svg { color: #0F766E; flex-shrink: 0; }

/* ═══ PROCESS NOTE ═══ */
.process-note {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  background: #F0FDFA;
  border: 1px solid #CCFBF1;
  border-radius: 12px;
}
.process-icon {
  width: 32px;
  height: 32px;
  border-radius: 9px;
  background: #fff;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.process-title {
  margin: 0 0 2px;
  font-size: 12.5px;
  font-weight: 700;
  color: #0F766E;
}
.process-desc {
  margin: 0;
  font-size: 11.5px;
  color: #334155;
  line-height: 1.5;
}

/* ═══ ÉXITO ═══ */
.success-container {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  padding: 44px 32px;
  max-width: 640px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
}
.success-icon {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDFA 100%);
  border: 3px solid #10B981;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #059669;
  margin-bottom: 6px;
  animation: successPop .5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}
.success-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
}
.success-message {
  margin: 0 0 8px;
  font-size: 13.5px;
  color: #64748B;
  line-height: 1.6;
  max-width: 460px;
}
.success-message strong { color: #0F172A; font-weight: 700; }

.success-resumen {
  width: 100%;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 6px 16px;
  margin: 8px 0;
}
.resumen-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #E2E8F0;
  font-size: 13px;
}
.resumen-fila:last-child { border-bottom: none; }
.resumen-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #64748B;
  font-weight: 600;
  white-space: nowrap;
  font-size: 12px;
}
.resumen-value {
  color: #0F172A;
  font-weight: 700;
  text-align: right;
  word-break: break-word;
}
.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12.5px;
  background: #F1F5F9;
  padding: 2px 8px;
  border-radius: 5px;
}

.success-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
  justify-content: center;
  width: 100%;
}
.btn-primary,
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 11px 20px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
  flex: 1;
  min-width: 180px;
}
.btn-primary {
  background: #0F766E;
  color: #fff;
  border: none;
}
.btn-primary:hover {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-secondary {
  background: #fff;
  color: #475569;
  border: 1px solid #E2E8F0;
}
.btn-secondary:hover {
  background: #F8FAFC;
  border-color: #CBD5E1;
  color: #0F766E;
}

/* ═══ SPIN ═══ */
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══ TRANSICIONES ═══ */
.slide-fade-enter-active,
.slide-fade-leave-active { transition: all .3s ease; }
.slide-fade-enter-from,
.slide-fade-leave-to { opacity: 0; transform: translateY(-6px); }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .wizard-layout { grid-template-columns: 1fr; }
  .wizard-sidebar { position: static; }
}

@media (max-width: 640px) {
  .registrar-mascota { padding: 16px 16px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero h1 { font-size: 22px; }
  .card-header { padding: 14px 18px; }
  .card-body { padding: 16px 18px 20px; }

  .sex-picks { grid-template-columns: 1fr; }
  .form-grid-2 { grid-template-columns: 1fr; gap: 14px; }
  .species-picks { grid-template-columns: repeat(2, 1fr); }

  .form-actions {
    flex-direction: column-reverse;
    padding: 14px 16px;
  }
  .btn-cancel,
  .btn-next { width: 100%; }

  .success-container { padding: 32px 20px; }
  .success-icon { width: 72px; height: 72px; }
  .success-title { font-size: 19px; }
  .success-actions { flex-direction: column; }
  .btn-primary,
  .btn-secondary { width: 100%; }
  .resumen-fila {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  .resumen-value { text-align: left; }
}
</style>