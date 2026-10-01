<template>
  <div class="historiales" :class="{ 'has-selected': !!idMascota }">
    <ToastContainer />

    <!-- ═══ BREADCRUMB ═══ -->
    <nav v-if="idMascota" class="breadcrumb" aria-label="Migas de pan">
      <button class="bc-back" type="button" aria-label="Volver al selector" @click="volverAlSelector">
        <ArrowLeft :size="15" />
      </button>
      <button class="bc-item bc-link" type="button" @click="volverAlSelector">
        Historial Clínico
      </button>
      <ChevronRight :size="14" class="bc-sep" />
      <span class="bc-item bc-current">{{ infoMascota?.nombre || `Paciente #${idMascota}` }}</span>
    </nav>

    <!-- ═══ SELECTOR (sin mascota seleccionada) ═══ -->
    <template v-if="!idMascota">
      <header class="page-header">
        <div>
          <span class="page-header-eyebrow">
            <Stethoscope :size="12" /> Historial clínico
          </span>
          <h1>Historial Clínico</h1>
          <p class="page-header-sub">
            Expediente cronológico de atenciones de tus mascotas, de la más reciente a la más antigua.
          </p>
        </div>
      </header>

      <AppCard>
        <template #header>
          <div>
            <h3>Selecciona una de tus mascotas</h3>
            <p class="card-header-sub">Verás su expediente clínico completo.</p>
          </div>
        </template>

        <div v-if="cargandoCatalogo" class="loading-state">
          <span class="spinner spinner-lg" />
          <p>Cargando tus mascotas…</p>
        </div>

        <AppEmptyState
          v-else-if="!misMascotas.length"
          :icon="PawPrint"
          title="No tienes mascotas registradas"
          description="Registra tu primera mascota para ver su historial."
        >
          <template #action>
            <AppButton variant="primary" @click="router.push('/cliente/mascotas')">
              <template #icon-left><Plus :size="16" /></template>
              Registrar mascota
            </AppButton>
          </template>
        </AppEmptyState>

        <div v-else class="selector-mascotas">
          <button
            v-for="m in misMascotas"
            :key="m.idMascota"
            type="button"
            class="selector-card"
            @click="irAlHistorial(m)"
          >
            <PetAvatar :nombre-especie="especieNombre(m.idEspecie)" size="lg" />
            <div class="selector-info">
              <p class="selector-nombre">{{ m.nombre }}</p>
              <p class="selector-sub">{{ resumenMascota(m) }}</p>
            </div>
            <span class="selector-cta" aria-hidden="true">
              <ChevronRight :size="16" />
            </span>
          </button>
        </div>
      </AppCard>
    </template>

    <!-- ═══ VISTA DE PACIENTE ═══ -->
    <template v-else>
      <!-- Hero del paciente -->
      <section class="paciente-hero">
        <PetAvatar :nombre-especie="especieNombre(infoMascota?.idEspecie)" size="lg" />

        <div class="paciente-datos">
          <h1 class="paciente-nombre">
            {{ infoMascota?.nombre || `Paciente #${idMascota}` }}
          </h1>

          <div v-if="metadatosMascota.length" class="paciente-badges">
            <span v-for="meta in metadatosMascota" :key="meta" class="paciente-badge">
              {{ meta }}
            </span>
          </div>

          <div class="paciente-meta">
            <span class="meta-item">
              <User :size="13" />
              <strong>Dueño:</strong> {{ authStore.userName }}
            </span>
            <span v-if="infoMascota?.fechaNacimiento" class="meta-item">
              <Calendar :size="13" />
              <strong>Nacimiento:</strong> {{ formatFechaNac(infoMascota.fechaNacimiento) }}
            </span>
          </div>
        </div>

        <div class="paciente-acciones">
          <AppButton variant="primary" @click="router.push('/cliente/solicitar-cita')">
            <template #icon-left><CalendarPlus :size="16" /></template>
            Agendar cita
          </AppButton>
        </div>
      </section>

      <!-- KPIs -->
      <section v-if="historial.length" class="kpis">
        <article v-for="kpi in kpis" :key="kpi.etiqueta" class="kpi">
          <div class="kpi-icon" :style="{ '--kpi-color': kpi.color, '--kpi-bg': kpi.fondo }">
            <component :is="kpi.icono" :size="18" />
          </div>
          <div>
            <p class="kpi-value">{{ kpi.valor }}</p>
            <p class="kpi-label">{{ kpi.etiqueta }}</p>
          </div>
        </article>
      </section>

      <!-- Loading historial -->
      <div v-if="cargandoHistorial" class="card">
        <div class="card-body">
          <div class="loading-state">
            <span class="spinner spinner-lg" />
            <p>Cargando el historial clínico…</p>
          </div>
        </div>
      </div>

      <!-- Sin atenciones -->
      <AppCard v-else-if="!historial.length">
        <AppEmptyState
          :icon="Inbox"
          title="Sin atenciones registradas"
          description="Aún no hay atenciones registradas para esta mascota."
        />
      </AppCard>

      <!-- Timeline -->
      <template v-else>
        <div class="timeline-header">
          <h2 class="timeline-title">Historial de atenciones</h2>
          <span class="timeline-count">
            {{ historial.length }} {{ historial.length === 1 ? 'registro' : 'registros' }}
          </span>
        </div>

        <div class="timeline">
          <article
            v-for="atencion in historial"
            :key="atencion.idAtencion"
            class="timeline-item"
          >
            <div class="timeline-dot" />

            <div class="atencion-card" :class="{ 'is-open': estaExpandida(atencion.idAtencion) }">
              <button
                type="button"
                class="atencion-head"
                :aria-expanded="estaExpandida(atencion.idAtencion)"
                @click="toggleExpandida(atencion.idAtencion)"
              >
                <div class="atencion-head-main">
                  <p class="atencion-fecha">{{ fechaHoraCorta(atencion.fechaHoraInicio) }}</p>
                  <p class="atencion-vet">
                    <Stethoscope :size="13" />
                    {{ atencion.veterinarioNombre }}
                  </p>
                  <p v-if="atencion.diagnosticoPrincipal" class="atencion-dx">
                    <strong>Dx:</strong> {{ atencion.diagnosticoPrincipal }}
                  </p>
                </div>
                <div class="atencion-head-side">
                  <span class="badge badge-brand">
                    {{ estadoLabel(atencion.estadoAtencion) }}
                  </span>
                  <span class="atencion-toggle" aria-hidden="true">
                    <ChevronDown :size="16" />
                  </span>
                </div>
              </button>

              <Transition name="expand">
                <div v-show="estaExpandida(atencion.idAtencion)" class="atencion-body">
                  <!-- Signos vitales -->
                  <div class="vitales-grid">
                    <div v-for="vitalCfg in SIGNOS_VITALES" :key="vitalCfg.campo" class="vital">
                      <component :is="vitalCfg.icono" :size="14" class="vital-icon" />
                      <span class="vital-label">{{ vitalCfg.etiqueta }}</span>
                      <strong class="vital-value">
                        {{ vital(atencion[vitalCfg.campo], vitalCfg.unidad) }}
                      </strong>
                    </div>
                  </div>

                  <!-- Bloques clínicos -->
                  <div class="detalle-textos">
                    <div
                      v-for="bloque in bloquesDe(atencion)"
                      :key="bloque.campo"
                      class="detalle-bloque"
                      :class="bloque.clase"
                    >
                      <h5>{{ bloque.titulo }}</h5>
                      <p>{{ atencion[bloque.campo] || '—' }}</p>
                    </div>
                  </div>

                  <!-- Insumos -->
                  <div v-if="atencion.insumos?.length" class="bloque-interno">
                    <h5><Package :size="13" /> Insumos aplicados</h5>
                    <div class="table-wrap">
                      <table class="data-table">
                        <thead>
                          <tr>
                            <th>Insumo</th>
                            <th class="der">Cant.</th>
                            <th class="der">P. unitario</th>
                            <th class="der">Subtotal</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="insumo in atencion.insumos" :key="insumo.idProducto">
                            <td>
                              {{ insumo.nombre }}
                              <span class="sku">({{ insumo.codigoSku }})</span>
                            </td>
                            <td class="der">{{ insumo.cantidad }} {{ insumo.unidadMedida }}</td>
                            <td class="der">{{ formatoUSD(insumo.precioUnitarioUsd) }}</td>
                            <td class="der amount">{{ formatoUSD(subtotalInsumo(insumo)) }}</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                    <p class="total-linea">
                      <span>Total insumos</span>
                      <span class="amount">{{ formatoUSD(totalInsumosAtencion(atencion)) }}</span>
                    </p>
                  </div>

                  <!-- Récipe -->
                  <div v-if="atencion.receta" class="bloque-interno bloque-receta">
                    <div class="receta-cabecera">
                      <h5><Pill :size="13" /> Récipe {{ atencion.receta.codigoReceta }}</h5>
                    </div>
                    <p v-if="atencion.receta.indicacionesGenerales" class="receta-indicaciones">
                      <strong>Indicaciones generales:</strong> {{ atencion.receta.indicacionesGenerales }}
                    </p>
                    <div class="table-wrap">
                      <table class="data-table">
                        <thead>
                          <tr>
                            <th v-for="col in COLUMNAS_RECETA" :key="col.campo">{{ col.titulo }}</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="(item, i) in atencion.receta.items" :key="i">
                            <td v-for="col in COLUMNAS_RECETA" :key="col.campo">
                              {{ item[col.campo] || col.fallback || '—' }}
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              </Transition>
            </div>
          </article>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ChevronRight, ChevronDown, Inbox, Package, PawPrint, Pill, Stethoscope,
  User, Calendar, FileText, Activity, Weight, Thermometer, Heart, Wind,
  ArrowLeft, Plus, CalendarPlus,
} from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth.store'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import PetAvatar from '@/components/ui/PetAvatar.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import { getHistorialMascota } from '@/api/atenciones.api'
import { getMisMascotas, getEspecies } from '@/api/mascotas.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { fechaHoraCorta } from '@/utils/fecha'

/* ═══════════════════════════════════════════════════════════════
   CONFIGURACIÓN DECLARATIVA
   ═══════════════════════════════════════════════════════════════ */
const SIGNOS_VITALES = [
  { campo: 'pesoKg', etiqueta: 'Peso', unidad: 'kg', icono: Weight },
  { campo: 'temperaturaC', etiqueta: 'Temp.', unidad: '°C', icono: Thermometer },
  { campo: 'frecCardiaca', etiqueta: 'FC', unidad: 'lpm', icono: Heart },
  { campo: 'frecRespiratoria', etiqueta: 'FR', unidad: 'rpm', icono: Wind },
]

const BLOQUES_CLINICOS = [
  { campo: 'anamnesis', titulo: 'Anamnesis', siempreVisible: true },
  { campo: 'sintomasObservados', titulo: 'Hallazgos' },
  { campo: 'diagnosticoPrincipal', titulo: 'Diagnóstico', clase: 'dx', siempreVisible: true },
  { campo: 'diagnosticosDiferenciales', titulo: 'Dx. diferenciales' },
  { campo: 'observacionesGenerales', titulo: 'Pronóstico' },
  { campo: 'indicacionesDueno', titulo: 'Indicaciones al dueño' },
  { campo: 'proximaCitaRecomendada', titulo: 'Próxima cita' },
]

const COLUMNAS_RECETA = [
  { campo: 'medicamento', titulo: 'Medicamento' },
  { campo: 'concentracion', titulo: 'Concentración', fallback: '—' },
  { campo: 'dosis', titulo: 'Dosis' },
  { campo: 'viaAdministracion', titulo: 'Vía' },
  { campo: 'frecuencia', titulo: 'Frecuencia' },
  { campo: 'duracion', titulo: 'Duración' },
]

/* ═══════════════════════════════════════════════════════════════
   ESTADO
   ═══════════════════════════════════════════════════════════════ */
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { toastError } = useToast()

const idMascota = computed(() => route.query.mascota)

const misMascotas = ref([])
const especies = ref([])
const historial = ref([])

const cargandoCatalogo = ref(true)
const cargandoHistorial = ref(false)

const expandidas = ref({})

/* ═══════════════════════════════════════════════════════════════
   DERIVADOS
   ═══════════════════════════════════════════════════════════════ */
const mapaEspecies = computed(() =>
  new Map(especies.value.map((e) => [e.id, e.nombre]))
)

const infoMascota = computed(() =>
  misMascotas.value.find((m) => String(m.idMascota) === String(idMascota.value)) ?? null
)

const metadatosMascota = computed(() => metadatosDe(infoMascota.value))

const stats = computed(() => ({
  total: historial.value.length,
  ultimaVisita: ultimaVisita(),
  edad: edadMascota(infoMascota.value?.fechaNacimiento) || '—',
  peso: infoMascota.value?.pesoActual ? `${infoMascota.value.pesoActual} kg` : '—',
}))

const kpis = computed(() => [
  { icono: FileText, valor: stats.value.total, etiqueta: 'Atenciones', color: 'var(--brand-700)', fondo: 'var(--brand-50)' },
  { icono: Calendar, valor: stats.value.ultimaVisita, etiqueta: 'Última visita', color: 'var(--info-600)', fondo: 'var(--info-50)' },
  { icono: Activity, valor: stats.value.edad, etiqueta: 'Edad', color: 'var(--warning-600)', fondo: 'var(--warning-50)' },
  { icono: Weight, valor: stats.value.peso, etiqueta: 'Peso actual', color: 'var(--purple-600)', fondo: 'var(--purple-50)' },
])

/* ═══════════════════════════════════════════════════════════════
   HELPERS
   ═══════════════════════════════════════════════════════════════ */
const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

function vital(valor, unidad) {
  return valor !== null && valor !== undefined && valor !== ''
    ? `${valor} ${unidad}`
    : '—'
}

function formatoUSD(valor) {
  return `$${Number(valor || 0).toFixed(2)}`
}

function estadoLabel(estado) {
  return String(estado || '').replaceAll('_', ' ')
}

const especieNombre = (idEspecie) => mapaEspecies.value.get(idEspecie) ?? null

const sexoLabel = (sexo) =>
  sexo === 'M' ? 'Macho' : sexo === 'H' ? 'Hembra' : null

function formatFechaNac(iso) {
  if (!iso) return null
  const [y, m, d] = String(iso).slice(0, 10).split('-')
  return `${d} ${MESES[Number(m) - 1]} ${y}`
}

function edadMascota(iso) {
  if (!iso) return null
  const nac = new Date(iso)
  if (Number.isNaN(nac.getTime())) return null
  const hoy = new Date()
  let anios = hoy.getFullYear() - nac.getFullYear()
  const m = hoy.getMonth() - nac.getMonth()
  if (m < 0 || (m === 0 && hoy.getDate() < nac.getDate())) anios--
  if (anios < 1) {
    const meses = Math.max(
      0,
      (hoy.getFullYear() - nac.getFullYear()) * 12 + (hoy.getMonth() - nac.getMonth())
    )
    if (meses === 0) return 'Recién nacido'
    return `${meses} ${meses === 1 ? 'mes' : 'meses'}`
  }
  return `${anios} ${anios === 1 ? 'año' : 'años'}`
}

function metadatosDe(m) {
  return [
    especieNombre(m?.idEspecie),
    sexoLabel(m?.sexo),
    edadMascota(m?.fechaNacimiento),
  ].filter(Boolean)
}

function resumenMascota(m) {
  return metadatosDe(m).join(' · ') || 'Sin datos'
}

function ultimaVisita() {
  const fecha = historial.value[0]?.fechaHoraInicio
  return fecha ? fechaHoraCorta(fecha).split('·')[0]?.trim() : '—'
}

const subtotalInsumo = (insumo) =>
  insumo.subtotal ?? insumo.subtotalUsd ?? insumo.cantidad * insumo.precioUnitarioUsd

const totalInsumosAtencion = (atencion) =>
  (atencion.insumos || []).reduce((suma, i) => suma + subtotalInsumo(i), 0)

function bloquesDe(atencion) {
  return BLOQUES_CLINICOS.filter((b) => b.siempreVisible || atencion[b.campo])
}

/* ═══════════════════════════════════════════════════════════════
   INTERACCIÓN
   ═══════════════════════════════════════════════════════════════ */
function estaExpandida(id) {
  return !!expandidas.value[id]
}

function toggleExpandida(id) {
  expandidas.value = { ...expandidas.value, [id]: !expandidas.value[id] }
}

/* ═══════════════════════════════════════════════════════════════
   CARGA DE DATOS
   ═══════════════════════════════════════════════════════════════ */
async function cargarCatalogo() {
  const [mascotasRes, especiesRes] = await Promise.allSettled([
    getMisMascotas(),
    getEspecies(),
  ])

  if (mascotasRes.status === 'fulfilled') {
    misMascotas.value = mascotasRes.value.data ?? []
  } else {
    toastError(getApiErrorMessage(mascotasRes.reason))
  }

  if (especiesRes.status === 'fulfilled') {
    especies.value = especiesRes.value.data ?? []
  }

  cargandoCatalogo.value = false
}

let idSolicitud = 0

async function cargarHistorial(id) {
  if (!id) {
    historial.value = []
    return
  }
  const solicitud = ++idSolicitud
  cargandoHistorial.value = true
  try {
    const data = await getHistorialMascota(id)
    if (solicitud !== idSolicitud) return
    historial.value = data
    expandirPrimera(data)
  } catch (error) {
    if (solicitud !== idSolicitud) return
    toastError(getApiErrorMessage(error))
    historial.value = []
  } finally {
    if (solicitud === idSolicitud) cargandoHistorial.value = false
  }
}

function expandirPrimera(atenciones) {
  const primera = atenciones[0]?.idAtencion
  expandidas.value = primera ? { [primera]: true } : {}
}

/* ═══════════════════════════════════════════════════════════════
   ORQUESTACIÓN
   ═══════════════════════════════════════════════════════════════ */
onMounted(async () => {
  await cargarCatalogo()
  await cargarHistorial(idMascota.value)
})

watch(idMascota, async (id) => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
  await cargarHistorial(id)
})

/* ═══════════════════════════════════════════════════════════════
   NAVEGACIÓN
   ═══════════════════════════════════════════════════════════════ */
function volverAlSelector() {
  router.replace({ path: '/cliente/historial-clinico' })
}

function irAlHistorial(mascota) {
  const id = mascota?.idMascota
  if (!id) return
  router.push({ path: '/cliente/historial-clinico', query: { mascota: id } })
}
</script>

<style scoped>
.historiales {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-6) var(--space-12);
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
  min-height: 24px;
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
.bc-back:hover { background: var(--neutral-100); color: var(--brand-700); border-color: var(--border-strong); }
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

/* ═══ SELECTOR DE MASCOTAS ═══ */
.selector-mascotas {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-4);
}
.selector-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  overflow: hidden;
  transition: border-color var(--duration-base) var(--ease-out),
              box-shadow var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
  box-shadow: var(--shadow-xs);
}
.selector-card:hover {
  border-color: var(--brand-200);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.selector-info { flex: 1; min-width: 0; }
.selector-nombre {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.selector-sub {
  margin: var(--space-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.selector-cta {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
  background: var(--neutral-100);
  flex-shrink: 0;
  transition: all var(--duration-base) var(--ease-out);
}
.selector-card:hover .selector-cta {
  background: var(--brand-700);
  color: var(--text-inverse);
  transform: translateX(2px);
}

/* ═══ HERO DEL PACIENTE ═══ */
.paciente-hero {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-5);
  align-items: center;
  padding: var(--space-5) var(--space-6);
  background: linear-gradient(135deg, var(--brand-50) 0%, var(--bg-surface) 60%);
  border: 1px solid var(--brand-100);
  border-radius: var(--radius-3xl);
  box-shadow: var(--shadow-xs);
}
.paciente-datos { min-width: 0; }
.paciente-nombre {
  margin: 0 0 var(--space-2);
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.2;
  letter-spacing: var(--tracking-tight);
}
.paciente-badges {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}
.paciente-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  background: var(--bg-surface);
  border: 1px solid var(--brand-100);
  color: var(--brand-700);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
}
.paciente-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  font-size: var(--text-md);
  color: var(--text-secondary);
}
.meta-item { display: inline-flex; align-items: center; gap: var(--space-2); }
.meta-item strong { font-weight: var(--font-semibold); color: var(--neutral-700); }
.paciente-acciones { display: flex; gap: var(--space-3); align-items: center; }

/* ═══ KPIs ═══ */
.kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}
.kpi {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  transition: all var(--duration-base) var(--ease-out);
}
.kpi:hover {
  border-color: var(--border-strong);
  box-shadow: var(--shadow-sm);
}
.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  background: var(--kpi-bg);
  color: var(--kpi-color);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.kpi-value {
  margin: 0;
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.kpi-label {
  margin: var(--space-1) 0 0;
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}

/* ═══ TIMELINE ═══ */
.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}
.timeline-title {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}
.timeline-count {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
  background: var(--neutral-100);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
}

.timeline {
  position: relative;
  padding-left: var(--space-7);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.timeline::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 10px;
  bottom: 10px;
  width: 2px;
  background: linear-gradient(to bottom, var(--brand-200) 0%, var(--border-subtle) 60%);
  border-radius: 2px;
}
.timeline-item { position: relative; }
.timeline-dot {
  position: absolute;
  left: calc(var(--space-7) * -1);
  top: 20px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--brand-700);
  border: 3px solid var(--bg-surface);
  box-shadow: 0 0 0 2px var(--brand-200);
  z-index: 1;
}

/* ═══ ATENCIÓN CARD ═══ */
.atencion-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  transition: border-color var(--duration-base) var(--ease-out),
              box-shadow var(--duration-base) var(--ease-out);
}
.atencion-card:hover { border-color: var(--border-strong); }
.atencion-card.is-open {
  border-color: var(--brand-200);
  box-shadow: 0 8px 20px -10px rgba(15, 118, 110, 0.18);
}
.atencion-head {
  width: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
  background: transparent;
  border: none;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
  transition: background-color var(--duration-fast) var(--ease-out);
}
.atencion-head:hover { background: var(--bg-surface-alt); }
.atencion-head-main { min-width: 0; flex: 1; }
.atencion-fecha {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  letter-spacing: -0.01em;
}
.atencion-vet {
  margin: var(--space-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.atencion-dx {
  margin: var(--space-2) 0 0;
  font-size: var(--text-md);
  color: var(--neutral-700);
  line-height: var(--leading-normal);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.atencion-dx strong { color: var(--text-secondary); font-weight: var(--font-semibold); }

.atencion-head-side { display: flex; align-items: center; gap: var(--space-3); flex-shrink: 0; }
.atencion-toggle {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-md);
  background: var(--neutral-100);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform var(--duration-base) var(--ease-out),
              background-color var(--duration-base) var(--ease-out),
              color var(--duration-base) var(--ease-out);
}
.atencion-card.is-open .atencion-toggle {
  background: var(--brand-50);
  color: var(--brand-700);
  transform: rotate(180deg);
}

/* ═══ CUERPO ATENCIÓN ═══ */
.atencion-body {
  padding: var(--space-1) var(--space-5) var(--space-5);
  border-top: 1px solid var(--border-subtle);
}

/* Vitales */
.vitales-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-3);
  margin: var(--space-4) 0;
}
.vital {
  display: grid;
  grid-template-columns: auto 1fr;
  grid-template-rows: auto auto;
  align-items: center;
  column-gap: var(--space-3);
  row-gap: 2px;
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: var(--space-3);
}
.vital-icon { grid-row: 1 / span 2; color: var(--text-tertiary); }
.vital-label {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}
.vital-value {
  font-size: var(--text-lg);
  color: var(--text-primary);
  font-weight: var(--font-bold);
  letter-spacing: -0.01em;
}

/* Bloques texto */
.detalle-textos {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}
.detalle-bloque {
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: var(--space-3) var(--space-4);
}
.detalle-bloque.dx {
  background: var(--brand-50);
  border-color: var(--brand-200);
}
.detalle-bloque h5 {
  margin: 0 0 var(--space-2);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-secondary);
}
.detalle-bloque.dx h5 { color: var(--brand-700); }
.detalle-bloque p {
  margin: 0;
  font-size: var(--text-md);
  color: var(--neutral-700);
  line-height: var(--leading-relaxed);
}

/* Bloques internos */
.bloque-interno {
  margin-top: var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
}
.bloque-interno h5 {
  margin: 0 0 var(--space-3);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.bloque-receta {
  background: var(--warning-50);
  border-color: var(--warning-200);
}
.bloque-receta h5 { color: var(--warning-700); }
.receta-cabecera {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  flex-wrap: wrap;
  margin-bottom: var(--space-2);
}
.receta-cabecera h5 { margin: 0; color: var(--warning-700); font-size: var(--text-md); }
.receta-indicaciones {
  margin: 0 0 var(--space-3);
  font-size: var(--text-md);
  color: var(--neutral-700);
  line-height: var(--leading-relaxed);
}

/* Tablas */
.table-wrap { overflow-x: auto; border-radius: var(--radius-md); }
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-family: inherit;
  min-width: 420px;
}
.data-table th {
  text-align: left;
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
  border-bottom: 2px solid var(--border-subtle);
  background: transparent;
}
.data-table td {
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-md);
  color: var(--text-primary);
  border-bottom: 1px solid var(--neutral-100);
  vertical-align: middle;
}
.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover td { background: rgba(15, 118, 110, 0.03); }
.amount { font-weight: var(--font-bold); color: var(--brand-700); }
.der { text-align: right; }
.sku { color: var(--text-tertiary); font-size: var(--text-sm); }
.total-linea {
  display: flex;
  justify-content: space-between;
  margin: var(--space-3) 0 0;
  padding-top: var(--space-3);
  border-top: 1px dashed var(--neutral-300);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--neutral-700);
}

/* ═══ LOADING ═══ */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12);
  color: var(--text-secondary);
  font-size: var(--text-base);
}

/* ═══ TRANSICIÓN EXPAND ═══ */
.expand-enter-active,
.expand-leave-active {
  transition: opacity var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
}
.expand-enter-from,
.expand-leave-to { opacity: 0; transform: translateY(-4px); }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
  .vitales-grid { grid-template-columns: repeat(2, 1fr); }
  .detalle-textos { grid-template-columns: 1fr; }
  .paciente-hero { grid-template-columns: auto 1fr; gap: var(--space-4); }
  .paciente-acciones { grid-column: 1 / -1; padding-top: var(--space-1); }
}
@media (max-width: 640px) {
  .historiales { padding: var(--space-4); }
  .paciente-hero { padding: var(--space-4); border-radius: var(--radius-2xl); }
  .paciente-nombre { font-size: var(--text-3xl); }
  .timeline { padding-left: var(--space-5); }
  .timeline-dot { left: calc(var(--space-5) * -1); width: 12px; height: 12px; }
  .timeline::before { left: 5px; }
  .atencion-head { padding: var(--space-3); }
  .atencion-head-side { flex-direction: column; align-items: flex-end; gap: var(--space-2); }
  .atencion-body { padding: var(--space-1) var(--space-4) var(--space-4); }
  .vitales-grid { gap: var(--space-2); }
  .selector-mascotas { grid-template-columns: 1fr; }
  .kpis { grid-template-columns: 1fr; }
}
</style>