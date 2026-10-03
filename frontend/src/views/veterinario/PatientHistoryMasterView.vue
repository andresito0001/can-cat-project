<template>
  <div class="history-view" :class="{ 'has-selection': !!idMascota }">
    <ToastContainer />

    <div class="master-detail">
      <!-- ═══════════════════════════════════════════════
           SIDEBAR DE PACIENTES
           ═══════════════════════════════════════════════ -->
      <aside class="sidebar">
        <header class="sidebar-head">
          <span class="sidebar-eyebrow">
            <ClipboardList :size="12" />
            Pacientes
          </span>
          <h2 class="sidebar-title">Expedientes clínicos</h2>
        </header>

        <div class="sidebar-search">
          <Search :size="15" class="sidebar-search-icon" />
          <input
            v-model="filtro"
            type="text"
            class="sidebar-search-input"
            placeholder="Buscar por mascota, dueño o documento"
            autocomplete="off"
          />
          <span v-if="buscando" class="sidebar-search-spinner">
            <span class="spinner spinner-sm" />
          </span>
          <button
            v-else-if="filtro"
            type="button"
            class="sidebar-search-clear"
            aria-label="Limpiar"
            @click="filtro = ''"
          >
            <X :size="13" />
          </button>
        </div>

        <div v-if="filtroAplicado && !buscando" class="results-meta">
          <span v-if="resultados.length">
            {{ resultados.length }} {{ resultados.length === 1 ? 'paciente' : 'pacientes' }}
          </span>
          <span v-else class="results-empty">Sin coincidencias</span>
        </div>

        <div class="sidebar-body">
          <div v-if="buscando" class="state-inline">
            <span class="spinner spinner-lg" />
            <p>Buscando pacientes…</p>
          </div>

          <div v-else-if="!filtroAplicado" class="state-inline">
            <div class="state-icon"><Search :size="22" /></div>
            <p class="state-title">Comienza a escribir</p>
            <p class="state-text">
              Busca por nombre de la mascota, del dueño o por su documento de identidad.
            </p>
          </div>

          <div v-else-if="!resultados.length" class="state-inline">
            <div class="state-icon"><PawPrint :size="22" /></div>
            <p class="state-title">Sin resultados</p>
            <p class="state-text">
              No encontramos pacientes que coincidan con
              <strong>«{{ filtroAplicado }}»</strong>.
            </p>
          </div>

          <ul v-else class="patient-list">
            <li
              v-for="m in resultados"
              :key="m.idMascota"
              class="patient-item"
              :class="{ 'is-active': String(m.idMascota) === String(idMascota) }"
            >
              <button
                type="button"
                class="patient-btn"
                @click="seleccionarPaciente(m)"
              >
                <PetAvatar :nombre-especie="m.especie" size="md" :muted="!m.activo || m.fallecido" />
                <div class="patient-info">
                  <div class="patient-line">
                    <span class="patient-name">{{ m.nombre }}</span>
                    <span v-if="m.fallecido" class="patient-tag is-danger">Fallecido</span>
                    <span v-else-if="!m.activo" class="patient-tag is-muted">Inactivo</span>
                  </div>
                  <p class="patient-meta">
                    {{ [m.especie, m.raza].filter(Boolean).join(' · ') || 'Paciente' }}
                  </p>
                  <p class="patient-owner">
                    <User :size="11" />
                    {{ m.clienteNombre }}
                  </p>
                </div>
                <ChevronRight :size="15" class="patient-chevron" />
              </button>
            </li>
          </ul>
        </div>
      </aside>

      <!-- ═══════════════════════════════════════════════
           PANEL DE DETALLE
           ═══════════════════════════════════════════════ -->
      <main class="detail-panel">
        <button
          v-if="idMascota"
          class="back-mobile"
          type="button"
          @click="volverALista"
        >
          <ArrowLeft :size="15" /> Volver a la lista
        </button>

        <section v-if="!idMascota" class="detail-empty">
          <div class="detail-empty-icon">
            <FileText :size="32" />
          </div>
          <h2 class="detail-empty-title">Selecciona un paciente</h2>
          <p class="detail-empty-text">
            Elige un paciente del listado para consultar su expediente clínico
            completo con atenciones, insumos aplicados y récipes emitidos.
          </p>
        </section>

        <template v-else>
          <!-- ═══ CABECERA DEL PACIENTE ═══ -->
          <section class="patient-hero">
            <div class="patient-hero-avatar">
              <PetAvatar
                :nombre-especie="pacienteActivo?.especie"
                size="lg"
                :muted="!pacienteActivo?.activo || pacienteActivo?.fallecido"
              />
            </div>

            <div class="patient-hero-info">
              <h1 class="patient-hero-name">{{ nombrePaciente }}</h1>

              <div v-if="pacienteActivo" class="patient-hero-badges">
                <span v-if="pacienteActivo.especie" class="p-badge">{{ pacienteActivo.especie }}</span>
                <span v-if="pacienteActivo.raza" class="p-badge">{{ pacienteActivo.raza }}</span>
                <span v-if="pacienteActivo.sexo" class="p-badge">{{ pacienteActivo.sexo }}</span>
                <span v-if="pacienteActivo.edad" class="p-badge">{{ pacienteActivo.edad }}</span>
                <span v-if="pacienteActivo.fallecido" class="p-badge is-danger">Fallecido</span>
                <span v-else-if="pacienteActivo.activo === false" class="p-badge is-warn">Inactivo</span>
              </div>

              <div v-if="pacienteActivo" class="patient-hero-meta">
                <span class="meta-pill">
                  <User :size="13" />
                  <strong>{{ pacienteActivo.clienteNombre }}</strong>
                </span>
                <span class="meta-pill">
                  <CreditCard :size="13" />
                  <span class="mono">{{ pacienteActivo.clienteDocumento }}</span>
                </span>
                <span v-if="pacienteActivo.clienteTelefono" class="meta-pill">
                  <Phone :size="13" />
                  {{ pacienteActivo.clienteTelefono }}
                </span>
              </div>
            </div>

            <div class="patient-hero-actions">
              <button class="icon-btn" type="button" :disabled="cargandoHistorial" @click="cargarHistorial">
                <Loader2 v-if="cargandoHistorial" :size="15" class="spin" />
                <RefreshCw v-else :size="15" />
                <span class="hidden-sm">Actualizar</span>
              </button>
            </div>
          </section>

          <!-- ═══ KPIs DEL PACIENTE ═══ -->
          <section v-if="!cargandoHistorial && historial.length" class="patient-kpis">
            <article class="p-kpi">
              <span class="p-kpi-value">{{ historial.length }}</span>
              <span class="p-kpi-label">{{ historial.length === 1 ? 'Atención' : 'Atenciones' }}</span>
            </article>
            <article class="p-kpi">
              <span class="p-kpi-value">{{ ultimaVisita }}</span>
              <span class="p-kpi-label">Última visita</span>
            </article>
            <article class="p-kpi">
              <span class="p-kpi-value">{{ totalRecetas }}</span>
              <span class="p-kpi-label">{{ totalRecetas === 1 ? 'Récipe' : 'Récipes' }}</span>
            </article>
            <article class="p-kpi">
              <span class="p-kpi-value">{{ totalInsumos }}</span>
              <span class="p-kpi-label">{{ totalInsumos === 1 ? 'Insumo' : 'Insumos' }}</span>
            </article>
          </section>

          <!-- ═══ TIMELINE ═══ -->
          <div v-if="cargandoHistorial" class="loading-box">
            <span class="spinner spinner-lg" />
            <p>Cargando historial…</p>
          </div>

          <section v-else-if="!historial.length" class="card">
            <div class="card-body">
              <div class="empty-detail">
                <div class="empty-icon"><Inbox :size="28" /></div>
                <h3>Sin atenciones registradas</h3>
                <p>
                  Este paciente aún no tiene atenciones en su expediente.
                  Cuando registres una consulta aparecerá aquí automáticamente.
                </p>
              </div>
            </div>
          </section>

          <template v-else>
            <!-- ═══ Barra de filtros (sticky) ═══ -->
            <div class="historial-toolbar">
              <div class="toolbar-row">
                <div class="toolbar-shortcuts" role="group" aria-label="Atajos de fecha">
                  <button
                    type="button"
                    class="shortcut"
                    :class="{ 'is-active': atajoFecha === 'hoy' }"
                    @click="aplicarAtajo('hoy')"
                  >
                    Hoy
                  </button>
                  <button
                    type="button"
                    class="shortcut"
                    :class="{ 'is-active': atajoFecha === 'semana' }"
                    @click="aplicarAtajo('semana')"
                  >
                    Esta semana
                  </button>
                  <button
                    type="button"
                    class="shortcut"
                    :class="{ 'is-active': atajoFecha === 'mes' }"
                    @click="aplicarAtajo('mes')"
                  >
                    Este mes
                  </button>
                  <button
                    type="button"
                    class="shortcut"
                    :class="{ 'is-active': atajoFecha === 'custom' }"
                    @click="aplicarAtajo('custom')"
                  >
                    Personalizado
                  </button>
                </div>

                <div class="toolbar-dates" :class="{ 'is-disabled': atajoFecha !== 'custom' && atajoFecha !== '' }">
                <DatePicker
                  v-model="rangoDesde"
                  placeholder="Desde"
                  @update:model-value="atajoFecha = 'custom'"
                />
                  <span class="date-sep">—</span>
                  <DatePicker
                    v-model="rangoHasta"
                    placeholder="Hasta"
                    :min="rangoDesde"
                    @update:model-value="atajoFecha = 'custom'"
                  />
                </div>

                <button
                  v-if="hayFiltrosActivos"
                  type="button"
                  class="btn-limpiar"
                  title="Limpiar filtros"
                  @click="limpiarFiltros"
                >
                  <X :size="14" />
                  <span>Limpiar</span>
                </button>
              </div>

              <div class="toolbar-row toolbar-row-estados">
                <span class="estados-label">
                  <SlidersHorizontal :size="13" />
                  Estado
                </span>
                <div class="estados-chips" role="group" aria-label="Filtrar por estado">
                  <button
                    type="button"
                    class="chip"
                    :class="{ 'is-active': filtroEstado === 'todas' }"
                    @click="filtroEstado = 'todas'"
                  >
                    Todas
                    <span class="chip-count">{{ conteosPorEstado.todas }}</span>
                  </button>
                  <button
                    v-for="e in ESTADOS_ATENCION"
                    :key="e.value"
                    type="button"
                    class="chip"
                    :class="{ 'is-active': filtroEstado === e.value, 'is-empty': conteosPorEstado[e.value] === 0 }"
                    :disabled="conteosPorEstado[e.value] === 0"
                    @click="filtroEstado = e.value"
                  >
                    {{ e.label }}
                    <span class="chip-count">{{ conteosPorEstado[e.value] }}</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- ═══ Header del timeline ═══ -->
            <div class="timeline-head">
              <h2 class="timeline-head-title">Historial de atenciones</h2>
              <span class="timeline-head-count">
                {{ conteoTimeline }}
                {{ historialFiltrado.length === 1 ? 'registro' : 'registros' }}
              </span>
            </div>

            <!-- ═══ Empty por filtros ═══ -->
            <div v-if="!historialFiltrado.length" class="empty-filtered">
              <div class="empty-icon"><CalendarRange :size="26" /></div>
              <h3>Sin resultados</h3>
              <p>No hay atenciones que coincidan con los filtros aplicados.</p>
              <button type="button" class="btn-link" @click="limpiarFiltros">
                <X :size="13" /> Limpiar filtros
              </button>
            </div>

            <!-- ═══ Timeline filtrado ═══ -->
            <div v-else class="timeline">
              <article
                v-for="atencion in historialFiltrado"
                :key="atencion.idAtencion"
                class="timeline-entry"
              >
                <div class="entry-rail">
                  <span class="entry-dot" />
                </div>

                <div class="entry-card" :class="{ 'is-open': estaExpandida(atencion.idAtencion) }">
                  <button
                    type="button"
                    class="entry-head"
                    @click="toggleExpandida(atencion.idAtencion)"
                  >
                    <div class="entry-head-main">
                      <p class="entry-date">{{ fechaHoraCorta(atencion.fechaHoraInicio) }}</p>
                      <p class="entry-vet">
                        <Stethoscope :size="13" />
                        {{ atencion.veterinarioNombre }}
                      </p>
                      <p v-if="atencion.diagnosticoPrincipal" class="entry-dx">
                        <span class="entry-dx-label">Dx</span>
                        {{ atencion.diagnosticoPrincipal }}
                      </p>
                    </div>
                    <div class="entry-head-side">
                      <span v-if="atencion.receta" class="entry-pill is-receta">
                        <Pill :size="11" /> Récipe
                      </span>
                      <span class="entry-pill">
                        {{ String(atencion.estadoAtencion || '').replaceAll('_', ' ') }}
                      </span>
                      <span class="entry-toggle">
                        <ChevronDown :size="16" />
                      </span>
                    </div>
                  </button>

                  <!-- ═══ ENTRY BODY — contenido restaurado ═══ -->
                  <Transition name="expand">
                    <div v-show="estaExpandida(atencion.idAtencion)" class="entry-body">
                      <!-- Signos vitales -->
                      <div class="vitals-row">
                        <div class="vital">
                          <span class="vital-label">Peso</span>
                          <span class="vital-value">{{ vital(atencion.pesoKg, 'kg') }}</span>
                        </div>
                        <div class="vital">
                          <span class="vital-label">Temp.</span>
                          <span class="vital-value">{{ vital(atencion.temperaturaC, '°C') }}</span>
                        </div>
                        <div class="vital">
                          <span class="vital-label">FC</span>
                          <span class="vital-value">{{ vital(atencion.frecCardiaca, 'lpm') }}</span>
                        </div>
                        <div class="vital">
                          <span class="vital-label">FR</span>
                          <span class="vital-value">{{ vital(atencion.frecRespiratoria, 'rpm') }}</span>
                        </div>
                      </div>

                      <!-- Bloques clínicos -->
                      <div class="clinical-grid">
                        <div class="clinical-block">
                          <h5>Anamnesis</h5>
                          <p>{{ atencion.anamnesis || '—' }}</p>
                        </div>
                        <div v-if="atencion.sintomasObservados" class="clinical-block">
                          <h5>Hallazgos</h5>
                          <p>{{ atencion.sintomasObservados }}</p>
                        </div>
                        <div class="clinical-block clinical-block-highlight">
                          <h5>Diagnóstico</h5>
                          <p>{{ atencion.diagnosticoPrincipal || '—' }}</p>
                        </div>
                        <div v-if="atencion.diagnosticosDiferenciales" class="clinical-block">
                          <h5>Dx. diferenciales</h5>
                          <p>{{ atencion.diagnosticosDiferenciales }}</p>
                        </div>
                        <div v-if="atencion.observacionesGenerales" class="clinical-block">
                          <h5>Pronóstico</h5>
                          <p>{{ atencion.observacionesGenerales }}</p>
                        </div>
                        <div v-if="atencion.indicacionesDueno" class="clinical-block">
                          <h5>Indicaciones al dueño</h5>
                          <p>{{ atencion.indicacionesDueno }}</p>
                        </div>
                        <div v-if="atencion.proximaCitaRecomendada" class="clinical-block">
                          <h5>Próxima cita</h5>
                          <p>{{ atencion.proximaCitaRecomendada }}</p>
                        </div>
                      </div>

                      <!-- Insumos -->
                      <div v-if="atencion.insumos?.length" class="inner-block">
                        <h5 class="inner-block-title">
                          <Package :size="13" /> Insumos aplicados
                        </h5>
                        <div class="table-wrap">
                          <table class="mini-table">
                            <thead>
                              <tr>
                                <th>Insumo</th>
                                <th class="der">Cant.</th>
                                <th class="der">P. unit.</th>
                                <th class="der">Subtotal</th>
                              </tr>
                            </thead>
                            <tbody>
                              <tr v-for="i in atencion.insumos" :key="i.idProducto">
                                <td>
                                  <span class="cell-main">{{ i.nombre }}</span>
                                  <span class="cell-sub">{{ i.codigoSku }}</span>
                                </td>
                                <td class="der">{{ i.cantidad }} {{ i.unidadMedida }}</td>
                                <td class="der">{{ formatoUSD(i.precioUnitarioUsd) }}</td>
                                <td class="der amount">{{ formatoUSD(subtotalInsumo(i)) }}</td>
                              </tr>
                            </tbody>
                          </table>
                        </div>
                        <p class="inner-block-total">
                          <span>Total insumos</span>
                          <span class="amount">{{ formatoUSD(totalInsumosAtencion(atencion)) }}</span>
                        </p>
                      </div>

                      <!-- Récipe -->
                      <div v-if="atencion.receta" class="inner-block inner-block-receta">
                        <header class="inner-block-head">
                          <h5 class="inner-block-title">
                            <Pill :size="13" /> Récipe {{ atencion.receta.codigoReceta }}
                          </h5>
                          <button
                            class="btn-pdf"
                            type="button"
                            :disabled="descargandoReceta === atencion.receta.idReceta"
                            @click.stop="descargarReceta(atencion.receta)"
                          >
                            <Loader2
                              v-if="descargandoReceta === atencion.receta.idReceta"
                              :size="13"
                              class="spin"
                            />
                            <Download v-else :size="13" />
                            Descargar PDF
                          </button>
                        </header>
                        <p v-if="atencion.receta.indicacionesGenerales" class="recipe-notes">
                          <strong>Indicaciones generales:</strong>
                          {{ atencion.receta.indicacionesGenerales }}
                        </p>
                        <div class="table-wrap">
                          <table class="mini-table">
                            <thead>
                              <tr>
                                <th>Medicamento</th>
                                <th>Concentración</th>
                                <th>Dosis</th>
                                <th>Vía</th>
                                <th>Frecuencia</th>
                                <th>Duración</th>
                              </tr>
                            </thead>
                            <tbody>
                              <tr v-for="(item, i) in atencion.receta.items" :key="i">
                                <td class="cell-main">{{ item.medicamento }}</td>
                                <td>{{ item.concentracion || '—' }}</td>
                                <td>{{ item.dosis }}</td>
                                <td>{{ item.viaAdministracion }}</td>
                                <td>{{ item.frecuencia }}</td>
                                <td>{{ item.duracion }}</td>
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
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, CalendarRange, ChevronDown, ChevronRight, ClipboardList,
  CreditCard, Download, FileText, Inbox, Loader2, Package, PawPrint,
  Phone, Pill, RefreshCw, Search, SlidersHorizontal, Stethoscope, User, X,
} from 'lucide-vue-next'

import { useToast } from '@/composables/useToast'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import PetAvatar from '@/components/ui/PetAvatar.vue'
import DatePicker from '@/components/ui/DatePicker.vue'
import { getHistorialMascota, descargarRecetaPdf } from '@/api/atenciones.api'
import { buscarMascotas } from '@/api/mascotas.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { descargarBlob } from '@/utils/descargas'
import { fechaHoraCorta, hoyISO } from '@/utils/fecha'

const route = useRoute()
const router = useRouter()
const { toastError } = useToast()

const formatoUSD = (valor) => `$${Number(valor || 0).toFixed(2)}`

/* ═══════════════════════════════════════════════════════════════
   RUTA / QUERY
   ═══════════════════════════════════════════════════════════════ */
const idMascota = computed(() => route.query.mascota)
const nombreQuery = computed(() => route.query.nombre)

/* ═══════════════════════════════════════════════════════════════
   ESTADO PRINCIPAL  ← AHORA VA ARRIBA
   ═══════════════════════════════════════════════════════════════ */
const filtro = ref('')
const filtroAplicado = ref('')
const resultados = ref([])
const buscando = ref(false)
let temporizador = null

const historial = ref([])
const cargandoHistorial = ref(false)
const descargandoReceta = ref(null)
const pacienteActivo = ref(null)

const cachePacientes = ref({})
const expandidas = ref({})

/* ═══════════════════════════════════════════════════════════════
   FILTROS DEL HISTORIAL
   ═══════════════════════════════════════════════════════════════ */
const rangoDesde = ref('')
const rangoHasta = ref('')
const atajoFecha = ref('')
const filtroEstado = ref('todas')
const hoy = computed(() => hoyISO())

const ESTADOS_ATENCION = [
  { value: 'En_Proceso', label: 'En proceso' },
  { value: 'Finalizada', label: 'Finalizada' },
  { value: 'Derivada', label: 'Derivada' },
]

/* ═══════════════════════════════════════════════════════════════
   COMPUTED — derivados de `historial` (ya declarado arriba)
   ═══════════════════════════════════════════════════════════════ */
const historialFiltrado = computed(() => {
  return historial.value.filter((a) => {
    const fecha = String(a.fechaHoraInicio || '').slice(0, 10)
    if (rangoDesde.value && fecha < rangoDesde.value) return false
    if (rangoHasta.value && fecha > rangoHasta.value) return false

    if (filtroEstado.value === 'todas') return true
    return a.estadoAtencion === filtroEstado.value
  })
})

const conteosPorEstado = computed(() => {
  const acc = { todas: historial.value.length, En_Proceso: 0, Finalizada: 0, Derivada: 0 }
  for (const a of historial.value) {
    if (a.estadoAtencion && acc[a.estadoAtencion] !== undefined) acc[a.estadoAtencion]++
  }
  return acc
})

const hayFiltrosActivos = computed(() =>
  Boolean(rangoDesde.value || rangoHasta.value || filtroEstado.value !== 'todas')
)

const conteoTimeline = computed(() => {
  const filtrados = historialFiltrado.value.length
  const total = historial.value.length
  return hayFiltrosActivos.value ? `${filtrados} de ${total}` : String(total)
})

const nombrePaciente = computed(() =>
  pacienteActivo.value?.nombre
  || nombreQuery.value
  || `Paciente #${idMascota.value}`
)

const ultimaVisita = computed(() => {
  const primera = historial.value[0]
  if (!primera?.fechaHoraInicio) return '—'
  return fechaHoraCorta(primera.fechaHoraInicio).split('·')[0]?.trim() || '—'
})

const totalRecetas = computed(() => historial.value.filter((a) => a.receta).length)

const totalInsumos = computed(() =>
  historial.value.reduce((sum, a) => sum + (a.insumos?.length || 0), 0)
)

/* ═══════════════════════════════════════════════════════════════
   WATCHERS
   ═══════════════════════════════════════════════════════════════ */
watch(filtro, (valor) => {
  clearTimeout(temporizador)
  temporizador = setTimeout(() => {
    filtroAplicado.value = valor.trim()
  }, 350)
})

watch(filtroAplicado, async (valor) => {
  if (!valor) {
    resultados.value = []
    return
  }
  buscando.value = true
  try {
    resultados.value = await buscarMascotas(valor)
    for (const m of resultados.value) {
      cachePacientes.value[m.idMascota] = m
    }
  } catch (error) {
    toastError(getApiErrorMessage(error))
    resultados.value = []
  } finally {
    buscando.value = false
  }
})

watch([rangoDesde, rangoHasta], ([desde, hasta]) => {
  if (atajoFecha.value && atajoFecha.value !== 'custom') {
    const esperado = calcularRangoAtajo(atajoFecha.value)
    if (esperado && (desde !== esperado.desde || hasta !== esperado.hasta)) {
      atajoFecha.value = 'custom'
    }
  }
})

/* ═══════════════════════════════════════════════════════════════
   ACCIONES DE PACIENTE
   ═══════════════════════════════════════════════════════════════ */
function seleccionarPaciente(mascota) {
  cachePacientes.value[mascota.idMascota] = mascota
  pacienteActivo.value = mascota
  router.replace({
    path: '/veterinario/historiales',
    query: { mascota: mascota.idMascota, nombre: mascota.nombre },
  })
}

function volverALista() {
  router.replace({ path: '/veterinario/historiales' })
  pacienteActivo.value = null
}

/* ═══════════════════════════════════════════════════════════════
   CARGA DE HISTORIAL
   ═══════════════════════════════════════════════════════════════ */
async function cargarHistorial() {
  if (!idMascota.value) {
    historial.value = []
    return
  }
  cargandoHistorial.value = true
  historial.value = []
  expandidas.value = {}

  try {
    historial.value = await getHistorialMascota(idMascota.value)
    const primera = historial.value[0]?.idAtencion
    if (primera) expandidas.value = { [primera]: true }
  } catch (error) {
    toastError(getApiErrorMessage(error))
    historial.value = []
  } finally {
    cargandoHistorial.value = false
  }
}

async function resolverPacienteActivo() {
  const id = idMascota.value
  if (!id) {
    pacienteActivo.value = null
    return
  }
  const cached = cachePacientes.value[id]
  if (cached) {
    pacienteActivo.value = cached
    return
  }
  const nombre = nombreQuery.value
  if (!nombre) {
    pacienteActivo.value = null
    return
  }
  try {
    const coincidencias = await buscarMascotas(nombre)
    const match =
      coincidencias.find((m) => String(m.idMascota) === String(id))
      || coincidencias[0]
      || null
    if (match) cachePacientes.value[id] = match
    pacienteActivo.value = match
  } catch {
    pacienteActivo.value = null
  }
}

/* ═══════════════════════════════════════════════════════════════
   EXPANDIR / COLAPSAR
   ═══════════════════════════════════════════════════════════════ */
function estaExpandida(id) { return !!expandidas.value[id] }
function toggleExpandida(id) {
  expandidas.value = { ...expandidas.value, [id]: !expandidas.value[id] }
}

/* ═══════════════════════════════════════════════════════════════
   HELPERS
   ═══════════════════════════════════════════════════════════════ */
function vital(valor, unidad) {
  return valor !== null && valor !== undefined && valor !== ''
    ? `${valor} ${unidad}`
    : '—'
}
function subtotalInsumo(insumo) {
  return insumo.subtotal ?? insumo.subtotalUsd ?? (insumo.cantidad * insumo.precioUnitarioUsd)
}
function totalInsumosAtencion(atencion) {
  return (atencion.insumos || []).reduce((suma, i) => suma + subtotalInsumo(i), 0)
}

async function descargarReceta(receta) {
  try {
    descargandoReceta.value = receta.idReceta
    const { blob, filename } = await descargarRecetaPdf(receta.idReceta)
    descargarBlob(blob, filename || `receta-${receta.codigoReceta}.pdf`)
  } catch (error) {
    toastError(getApiErrorMessage(error))
  } finally {
    descargandoReceta.value = null
  }
}

/* ═══════════════════════════════════════════════════════════════
   ATAJOS DE FECHA
   ═══════════════════════════════════════════════════════════════ */
function isoDe(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function aplicarAtajo(atajo) {
  atajoFecha.value = atajo
  const hoyDate = new Date()

  if (atajo === 'hoy') {
    rangoDesde.value = hoyISO()
    rangoHasta.value = hoyISO()
    return
  }
  if (atajo === 'semana') {
    const inicio = new Date(hoyDate)
    const dia = inicio.getDay() || 7
    inicio.setDate(inicio.getDate() - (dia - 1))
    rangoDesde.value = isoDe(inicio)
    rangoHasta.value = hoyISO()
    return
  }
  if (atajo === 'mes') {
    const inicio = new Date(hoyDate.getFullYear(), hoyDate.getMonth(), 1)
    rangoDesde.value = isoDe(inicio)
    rangoHasta.value = hoyISO()
    return
  }
  // 'custom' → no tocamos nada
}

function limpiarFiltros() {
  rangoDesde.value = ''
  rangoHasta.value = ''
  atajoFecha.value = ''
  filtroEstado.value = 'todas'
}

function calcularRangoAtajo(atajo) {
  const hoyDate = new Date()
  if (atajo === 'hoy') return { desde: hoyISO(), hasta: hoyISO() }
  if (atajo === 'semana') {
    const inicio = new Date(hoyDate)
    const dia = inicio.getDay() || 7
    inicio.setDate(inicio.getDate() - (dia - 1))
    return { desde: isoDe(inicio), hasta: hoyISO() }
  }
  if (atajo === 'mes') {
    const inicio = new Date(hoyDate.getFullYear(), hoyDate.getMonth(), 1)
    return { desde: isoDe(inicio), hasta: hoyISO() }
  }
  return null
}

/* ═══════════════════════════════════════════════════════════════
   CICLO DE VIDA
   ═══════════════════════════════════════════════════════════════ */
watch(idMascota, async () => {
  limpiarFiltros()
  await resolverPacienteActivo()
  await cargarHistorial()
})

onMounted(async () => {
  await resolverPacienteActivo()
  if (idMascota.value) await cargarHistorial()
})

onBeforeUnmount(() => {
  clearTimeout(temporizador)
})
</script>


<style scoped>
.history-view {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-6) var(--space-12);
}

/* ═══ MASTER-DETAIL LAYOUT ═══ */
.master-detail {
  display: grid;
  grid-template-columns: 340px minmax(0, 1fr);
  gap: var(--space-5);
  align-items: start;
}

/* ═══════════════════════════════════════════════════════════
   SIDEBAR DE PACIENTES
   ═══════════════════════════════════════════════════════════ */
.sidebar {
  position: sticky;
  top: var(--space-6);
  max-height: calc(100vh - var(--space-12));
  display: flex;
  flex-direction: column;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.sidebar-head {
  padding: var(--space-5) var(--space-5) var(--space-2);
}
.sidebar-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin: 0 0 var(--space-2);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--brand-700);
}
.sidebar-title {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.sidebar-search {
  position: relative;
  margin: var(--space-3) var(--space-5);
}
.sidebar-search-icon {
  position: absolute;
  left: var(--space-4);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  pointer-events: none;
  transition: color var(--duration-fast);
}
.sidebar-search-input {
  width: 100%;
  padding: var(--space-3) var(--space-10) var(--space-3) 40px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface-alt);
  border-radius: var(--radius-lg);
  font-size: var(--text-md);
  font-family: inherit;
  color: var(--text-primary);
  outline: none;
  box-sizing: border-box;
  transition: all var(--duration-fast) var(--ease-out);
}
.sidebar-search-input::placeholder { color: var(--text-tertiary); }
.sidebar-search-input:focus {
  border-color: var(--brand-700);
  background: var(--bg-surface);
  box-shadow: 0 0 0 4px var(--brand-100);
}
.sidebar-search:focus-within .sidebar-search-icon { color: var(--brand-700); }
.sidebar-search-spinner,
.sidebar-search-clear {
  position: absolute;
  right: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  color: var(--brand-700);
}
.sidebar-search-clear {
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
}
.sidebar-search-clear:hover { color: var(--neutral-600); background: var(--neutral-100); }

.results-meta {
  padding: 0 var(--space-5) var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}
.results-empty { color: var(--text-tertiary); }

.sidebar-body {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-2) var(--space-3) var(--space-4);
  scrollbar-width: thin;
  scrollbar-color: var(--neutral-300) transparent;
}
.sidebar-body::-webkit-scrollbar { width: 6px; }
.sidebar-body::-webkit-scrollbar-thumb { background: var(--neutral-300); border-radius: 3px; }

/* ═══ ESTADOS INLINE ═══ */
.state-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-2);
  padding: var(--space-10) var(--space-5);
  color: var(--text-tertiary);
}
.state-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-full);
  background: var(--brand-50);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
}
.state-title {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}
.state-text {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-normal);
  max-width: 260px;
}
.state-text strong { color: var(--text-primary); font-weight: var(--font-bold); }

/* ═══ LISTA DE PACIENTES ═══ */
.patient-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: var(--space-1); }
.patient-item { margin: 0; }
.patient-btn {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-3);
  align-items: center;
  width: 100%;
  padding: var(--space-3);
  background: transparent;
  border: 1px solid transparent;
  border-radius: var(--radius-lg);
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: all var(--duration-fast) var(--ease-out);
}
.patient-btn:hover {
  background: var(--bg-surface-alt);
  border-color: var(--border-subtle);
}
.patient-item.is-active .patient-btn {
  background: var(--brand-50);
  border-color: var(--brand-200);
}
.patient-item.is-active .patient-chevron { color: var(--brand-700); transform: translateX(2px); }

.patient-info { min-width: 0; }
.patient-line {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 2px;
  flex-wrap: wrap;
}
.patient-name {
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.2;
}
.patient-tag {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  padding: 1px var(--space-2);
  border-radius: var(--radius-full);
  white-space: nowrap;
}
.patient-tag.is-muted { background: var(--neutral-100); color: var(--text-secondary); }
.patient-tag.is-danger { background: var(--danger-50); color: var(--danger-700); border: 1px solid var(--danger-200); }
.patient-meta {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.patient-owner {
  margin: 2px 0 0;
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
.patient-chevron {
  color: var(--neutral-300);
  flex-shrink: 0;
  transition: all var(--duration-fast) var(--ease-out);
}

/* ═══════════════════════════════════════════════════════════
   DETAIL PANEL
   ═══════════════════════════════════════════════════════════ */
.detail-panel {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.back-mobile {
  display: none;
  align-items: center;
  gap: var(--space-2);
  align-self: flex-start;
  padding: var(--space-2) var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  color: var(--neutral-600);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
}
.back-mobile:hover { border-color: var(--brand-200); color: var(--brand-700); }

.detail-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
  padding: var(--space-16) var(--space-6);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
}
.detail-empty-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--brand-50);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
}
.detail-empty-title {
  margin: 0;
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.015em;
}
.detail-empty-text {
  margin: 0;
  font-size: var(--text-md);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  max-width: 460px;
}

/* ═══ HERO DEL PACIENTE ═══ */
.patient-hero {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-5);
  align-items: center;
  padding: var(--space-5) var(--space-6);
  background: linear-gradient(135deg, var(--brand-50) 0%, var(--bg-surface) 65%);
  border: 1px solid var(--brand-100);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
}
.patient-hero-info { min-width: 0; }
.patient-hero-name {
  margin: 0 0 var(--space-2);
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
  line-height: 1.1;
}
.patient-hero-badges {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-1);
  margin-bottom: var(--space-3);
}
.p-badge {
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
.p-badge.is-danger { background: var(--danger-50); border-color: var(--danger-200); color: var(--danger-700); }
.p-badge.is-warn { background: var(--warning-50); border-color: var(--warning-200); color: var(--warning-700); }

.patient-hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2) var(--space-4);
}
.meta-pill {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.meta-pill svg { color: var(--text-tertiary); flex-shrink: 0; }
.meta-pill strong { color: var(--text-primary); font-weight: var(--font-bold); }
.mono {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  background: var(--bg-surface);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-subtle);
  color: var(--neutral-700);
}

.patient-hero-actions { display: flex; gap: var(--space-2); }
.icon-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  color: var(--neutral-600);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.icon-btn:hover:not(:disabled) {
  border-color: var(--brand-200);
  color: var(--brand-700);
  background: var(--brand-50);
}
.icon-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ═══ KPIs DEL PACIENTE ═══ */
.patient-kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
}
.p-kpi {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  padding: var(--space-4) var(--space-5);
  border-right: 1px solid var(--border-subtle);
}
.p-kpi:last-child { border-right: none; }
.p-kpi-value {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  line-height: 1;
  letter-spacing: var(--tracking-tight);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.p-kpi-label {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}

/* ═══ TIMELINE ═══ */
.timeline-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: 0 var(--space-1);
}
.timeline-head-title {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
.timeline-head-count {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
  background: var(--bg-surface);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid var(--border-subtle);
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.timeline-entry {
  display: grid;
  grid-template-columns: 24px 1fr;
  gap: var(--space-4);
}
.entry-rail {
  position: relative;
  display: flex;
  justify-content: center;
}
.entry-rail::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--border-subtle);
  transform: translateX(-50%);
}
.timeline-entry:first-child .entry-rail::before { top: 24px; }
.timeline-entry:last-child .entry-rail::before { bottom: calc(100% - 24px); }
.entry-dot {
  position: relative;
  z-index: 1;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--brand-700);
  margin-top: 18px;
  box-shadow: 0 0 0 4px var(--bg-page);
}

.entry-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  transition: all var(--duration-base) var(--ease-out);
  box-shadow: var(--shadow-xs);
}
.entry-card:hover { border-color: var(--border-strong); }
.entry-card.is-open {
  border-color: var(--brand-200);
  box-shadow: var(--shadow-md);
}

.entry-head {
  width: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: transparent;
  border: none;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
  transition: background-color var(--duration-fast) var(--ease-out);
}
.entry-head:hover { background: var(--bg-surface-alt); }
.entry-head-main { min-width: 0; flex: 1; }
.entry-date {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  letter-spacing: -0.01em;
}
.entry-vet {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: var(--space-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.entry-vet svg { color: var(--text-tertiary); }
.entry-dx {
  margin: var(--space-2) 0 0;
  font-size: var(--text-md);
  color: var(--neutral-700);
  line-height: var(--leading-normal);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.entry-dx-label {
  display: inline-block;
  padding: 1px var(--space-2);
  margin-right: var(--space-2);
  background: var(--brand-100);
  color: var(--brand-700);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  border-radius: var(--radius-sm);
}

.entry-head-side {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-shrink: 0;
  flex-wrap: wrap;
}
.entry-pill {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  background: var(--brand-50);
  color: var(--brand-700);
  border: 1px solid var(--brand-200);
  white-space: nowrap;
}
.entry-pill.is-receta {
  background: var(--warning-50);
  color: var(--warning-700);
  border-color: var(--warning-200);
}
.entry-toggle {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: var(--neutral-100);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all var(--duration-base) var(--ease-out);
}
.entry-card.is-open .entry-toggle {
  background: var(--brand-50);
  color: var(--brand-700);
  transform: rotate(180deg);
}

/* ═══ ENTRY BODY ═══ */
.entry-body {
  padding: var(--space-1) var(--space-5) var(--space-5);
  border-top: 1px solid var(--border-subtle);
}

.vitals-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-3);
  margin: var(--space-4) 0;
}
.vital {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}
.vital-label {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
}
.vital-value {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.clinical-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}
.clinical-block {
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}
.clinical-block-highlight {
  background: var(--brand-50);
  border-color: var(--brand-200);
}
.clinical-block h5 {
  margin: 0 0 var(--space-2);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-secondary);
}
.clinical-block-highlight h5 { color: var(--brand-700); }
.clinical-block p {
  margin: 0;
  font-size: var(--text-md);
  color: var(--neutral-700);
  line-height: var(--leading-relaxed);
}

.inner-block {
  margin-top: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
}
.inner-block-receta {
  background: var(--warning-50);
  border-color: var(--warning-200);
}
.inner-block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  flex-wrap: wrap;
  margin-bottom: var(--space-3);
}
.inner-block-title {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}
.inner-block-receta .inner-block-title { color: var(--warning-700); }

.recipe-notes {
  margin: 0 0 var(--space-3);
  font-size: var(--text-md);
  color: var(--neutral-700);
  line-height: var(--leading-relaxed);
}
.recipe-notes strong { font-weight: var(--font-bold); color: var(--text-primary); }

.btn-pdf {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  background: var(--bg-surface);
  border: 1px solid var(--warning-200);
  border-radius: var(--radius-md);
  color: var(--warning-700);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.btn-pdf:hover:not(:disabled) {
  background: var(--warning-100);
  border-color: var(--warning-500);
}
.btn-pdf:disabled { opacity: 0.55; cursor: not-allowed; }

.table-wrap { overflow-x: auto; border-radius: var(--radius-lg); }
.mini-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 480px;
}
.mini-table th {
  text-align: left;
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
  border-bottom: 2px solid var(--border-subtle);
  background: transparent;
}
.mini-table td {
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-primary);
  border-bottom: 1px solid var(--neutral-100);
  vertical-align: middle;
}
.mini-table tbody tr:last-child td { border-bottom: none; }
.mini-table tbody tr:hover td { background: rgba(15, 118, 110, 0.04); }
.der { text-align: right; }
.amount { font-weight: var(--font-bold); color: var(--brand-700); }
.cell-main { font-weight: var(--font-semibold); color: var(--text-primary); display: block; }
.cell-sub {
  display: block;
  font-size: var(--text-2xs);
  color: var(--text-tertiary);
  font-family: var(--font-mono);
  margin-top: 1px;
}
.inner-block-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: var(--space-3) 0 0;
  padding-top: var(--space-3);
  border-top: 1px dashed var(--neutral-300);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--neutral-700);
}

/* ═══ EMPTY / LOADING ═══ */
.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  color: var(--text-secondary);
  font-size: var(--text-md);
}
.card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
}
.card-body { padding: var(--space-6); }
.empty-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
  padding: var(--space-6);
}
.empty-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--brand-50);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
}
.empty-detail h3 {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}
.empty-detail p {
  margin: 0;
  max-width: 440px;
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  font-size: var(--text-md);
}

.spin { animation: spin 0.9s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══ TRANSICIONES ═══ */
.expand-enter-active,
.expand-leave-active {
  transition: opacity var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
}
.expand-enter-from,
.expand-leave-to { opacity: 0; transform: translateY(-4px); }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1100px) {
  .master-detail { grid-template-columns: 300px minmax(0, 1fr); }
}

@media (max-width: 900px) {
  .history-view { padding: var(--space-5) var(--space-4) var(--space-10); }
  .master-detail { grid-template-columns: 1fr; }
  .sidebar { position: static; max-height: none; }
  .sidebar-body { max-height: 60vh; }

  .history-view.has-selection .sidebar { display: none; }
  .history-view:not(.has-selection) .detail-panel { display: none; }
  .back-mobile { display: inline-flex; }

  .patient-hero { grid-template-columns: auto 1fr; gap: var(--space-4); }
  .patient-hero-actions { grid-column: 1 / -1; }

  .patient-kpis { grid-template-columns: repeat(2, 1fr); }
  .p-kpi:nth-child(odd) { border-right: 1px solid var(--border-subtle); }
  .p-kpi:nth-child(-n+2) { border-bottom: 1px solid var(--border-subtle); }

  .vitals-row { grid-template-columns: repeat(2, 1fr); }
  .clinical-grid { grid-template-columns: 1fr; }
}

@media (max-width: 640px) {
  .history-view { padding: var(--space-4) var(--space-3) var(--space-8); }
  .patient-hero-name { font-size: var(--text-3xl); }
  .patient-kpis { grid-template-columns: 1fr; }
  .p-kpi { border-right: none !important; border-bottom: 1px solid var(--border-subtle); }
  .p-kpi:last-child { border-bottom: none; }

  .timeline-entry { grid-template-columns: 16px 1fr; gap: var(--space-3); }
  .entry-dot { width: 10px; height: 10px; }
  .entry-head { padding: var(--space-3) var(--space-4); }
  .entry-head-side { flex-direction: column; align-items: flex-end; }
  .entry-body { padding: var(--space-1) var(--space-4) var(--space-4); }

  .vitals-row { gap: var(--space-2); }
  .vital { padding: var(--space-2) var(--space-3); }
  .vital-value { font-size: var(--text-base); }
}

/* ═══════════════════════════════════════════════════════════════
   BARRA DE FILTROS (sticky)
   ═══════════════════════════════════════════════════════════════ */
.historial-toolbar {
  position: sticky;
  top: var(--space-4);
  z-index: var(--z-sticky, 40);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-sm);
  margin-bottom: var(--space-2);
}

.toolbar-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

/* ─── Fila 1: atajos + fechas ─── */
.toolbar-shortcuts {
  display: inline-flex;
  gap: var(--space-1);
  padding: var(--space-1);
  background: var(--neutral-100);
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}
.shortcut {
  padding: var(--space-2) var(--space-4);
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  font-family: inherit;
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  white-space: nowrap;
}
.shortcut:hover:not(.is-active) {
  color: var(--brand-700);
  background: var(--bg-surface);
}
.shortcut.is-active {
  background: var(--bg-surface);
  color: var(--brand-700);
  box-shadow: var(--shadow-xs);
}

.toolbar-dates {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex: 1 1 320px;
  min-width: 0;
  transition: opacity var(--duration-base) var(--ease-out);
}
.toolbar-dates.is-disabled { opacity: 0.75; }
.toolbar-dates :deep(.date-picker) { flex: 1; min-width: 140px; }

.date-sep {
  color: var(--neutral-300);
  font-weight: var(--font-bold);
  flex-shrink: 0;
  user-select: none;
}

.btn-limpiar {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-3);
  background: none;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  font-family: inherit;
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  flex-shrink: 0;
}
.btn-limpiar:hover {
  background: var(--danger-50);
  border-color: var(--danger-200);
  color: var(--danger-600);
}

/* ─── Fila 2: chips de estado ─── */
.toolbar-row-estados { justify-content: flex-start; }

.estados-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-extrabold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-secondary);
  flex-shrink: 0;
}
.estados-label svg { color: var(--text-tertiary); }

.estados-chips {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-3) var(--space-1) var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-full);
  font-family: inherit;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--neutral-600);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  white-space: nowrap;
}
.chip:hover:not(:disabled):not(.is-active) {
  background: var(--brand-50);
  border-color: var(--brand-200);
  color: var(--brand-700);
}
.chip.is-active {
  background: var(--brand-700);
  border-color: var(--brand-700);
  color: var(--text-inverse);
  box-shadow: 0 2px 8px rgba(15, 118, 110, 0.25);
}
.chip.is-empty {
  opacity: 0.45;
  cursor: not-allowed;
  border-style: dashed;
}
.chip-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 18px;
  padding: 0 var(--space-2);
  border-radius: var(--radius-full);
  background: var(--neutral-100);
  color: var(--text-secondary);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  font-variant-numeric: tabular-nums;
}
.chip.is-active .chip-count {
  background: rgba(255, 255, 255, 0.22);
  color: var(--text-inverse);
}

/* ═══════════════════════════════════════════════════════════════
   EMPTY FILTRADO
   ═══════════════════════════════════════════════════════════════ */
.empty-filtered {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12) var(--space-6);
  text-align: center;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
}
.empty-filtered .empty-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: var(--brand-50);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
}
.empty-filtered h3 {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}
.empty-filtered p {
  margin: 0;
  max-width: 380px;
  color: var(--text-secondary);
  line-height: var(--leading-normal);
}
.empty-filtered .btn-link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  background: none;
  border: none;
  padding: var(--space-2) var(--space-4);
  color: var(--brand-700);
  font-family: inherit;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: background-color var(--duration-fast) var(--ease-out);
  margin-top: var(--space-2);
}
.empty-filtered .btn-link:hover { background: var(--brand-50); }

/* ═══════════════════════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════════════════════ */
@media (max-width: 900px) {
  .historial-toolbar { padding: var(--space-3); }
  .toolbar-shortcuts { overflow-x: auto; max-width: 100%; }
  .toolbar-dates { flex: 1 1 100%; }
  .toolbar-dates :deep(.date-picker) { min-width: 0; }
}

@media (max-width: 640px) {
  .historial-toolbar {
    top: var(--space-2);
    padding: var(--space-3);
    gap: var(--space-2);
  }
  .shortcut { padding: var(--space-2) var(--space-3); font-size: var(--text-xs); }
  .estados-chips { gap: var(--space-1); }
  .chip { padding: var(--space-1) var(--space-2) var(--space-1) var(--space-3); font-size: var(--text-xs); }
  .chip-count { min-width: 18px; height: 16px; padding: 0 var(--space-1); font-size: var(--text-2xs); }
  .toolbar-row-estados { flex-direction: column; align-items: flex-start; }
}
</style>