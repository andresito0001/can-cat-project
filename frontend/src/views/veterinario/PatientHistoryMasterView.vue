<template>
  <div class="historiales-view" :class="{ 'has-selected': !!idMascota }">
    <ToastContainer />

    <div class="master-detail">
      <!-- ═══════════════════════════════════════════════
           SIDEBAR
           ═══════════════════════════════════════════════ -->
      <aside class="sidebar">
        <header class="sidebar-header">
          <h1 class="sidebar-title">
            <PawPrint :size="18" />
            Pacientes
          </h1>
          <p class="sidebar-subtitle">
            Busca por nombre de la mascota o por dueño y documento.
          </p>
        </header>

        <div class="search-box">
          <Search :size="15" class="search-icon" />
          <input
            v-model="filtro"
            type="text"
            class="search-input"
            placeholder="Ej: Pepe, o nombre o documento del dueño…"
            autocomplete="off"
          />
          <Loader2 v-if="buscando" :size="14" class="search-spinner spin" />
          <button
            v-else-if="filtro"
            type="button"
            class="search-clear"
            aria-label="Limpiar búsqueda"
            @click="filtro = ''"
          >
            <X :size="13" />
          </button>
        </div>

        <div v-if="filtroAplicado && !buscando" class="results-count">
          <span v-if="resultados.length">
            {{ resultados.length }}
            {{ resultados.length === 1 ? 'paciente encontrado' : 'pacientes encontrados' }}
          </span>
          <span v-else class="results-count-empty">Sin resultados</span>
        </div>

        <div class="sidebar-body">
          <div v-if="buscando" class="state-inline">
            <div class="spin"></div>
            <p>Buscando pacientes…</p>
          </div>

          <div v-else-if="!filtroAplicado" class="state-inline">
            <div class="state-icon">
              <Search :size="22" />
            </div>
            <p class="state-title">Comienza a escribir</p>
            <p class="state-text">
              Escribe al menos una letra para buscar por mascota o dueño.
            </p>
          </div>

          <div v-else-if="!resultados.length" class="state-inline">
            <div class="state-icon">
              <PawPrint :size="22" />
            </div>
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
              :class="{ 'is-selected': String(m.idMascota) === String(idMascota) }"
            >
              <button
                type="button"
                class="patient-btn"
                :aria-pressed="String(m.idMascota) === String(idMascota)"
                @click="seleccionarPaciente(m)"
              >
                <PetAvatar :nombre-especie="m.especie" size="md" :muted="!m.activo || m.fallecido" />
                <div class="patient-info">
                  <p class="patient-name">
                    <span class="patient-name-text">{{ m.nombre }}</span>
                    <span v-if="m.fallecido" class="pill-status is-muted">Fallecido</span>
                    <span v-else-if="!m.activo" class="pill-status is-muted">Inactivo</span>
                  </p>
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
           PANEL DETALLE
           ═══════════════════════════════════════════════ -->
      <main class="detail-panel">
        <button
          v-if="idMascota"
          class="btn-back-mobile"
          type="button"
          @click="volverALaLista"
        >
          <ArrowLeft :size="15" /> Volver a la lista
        </button>

        <section v-if="!idMascota" class="empty-detail">
          <div class="empty-detail-icon">
            <FileText :size="32" />
          </div>
          <h2 class="empty-detail-title">Selecciona un paciente</h2>
          <p class="empty-detail-text">
            Elige un paciente del listado para consultar su expediente clínico
            completo con atenciones, insumos aplicados y récipes emitidos.
          </p>
        </section>

        <template v-else>
          <!-- Cabecera del paciente -->
          <section class="paciente-hero">
            <PetAvatar
              :nombre-especie="pacienteActivo?.especie"
              size="lg"
              :muted="!pacienteActivo?.activo || pacienteActivo?.fallecido"
            />

            <div class="paciente-datos">
              <h1 class="paciente-nombre">{{ nombrePaciente }}</h1>

              <div v-if="pacienteActivo" class="paciente-badges">
                <span v-if="pacienteActivo.especie" class="paciente-badge">{{ pacienteActivo.especie }}</span>
                <span v-if="pacienteActivo.raza" class="paciente-badge">{{ pacienteActivo.raza }}</span>
                <span v-if="pacienteActivo.sexo" class="paciente-badge">{{ pacienteActivo.sexo }}</span>
                <span v-if="pacienteActivo.edad" class="paciente-badge">{{ pacienteActivo.edad }}</span>
                <span v-if="pacienteActivo.fallecido" class="paciente-badge is-warn">Fallecido</span>
                <span v-else-if="pacienteActivo.activo === false" class="paciente-badge is-warn">Inactivo</span>
              </div>
              <p v-else-if="!cargandoHistorial" class="paciente-id">Paciente #{{ idMascota }}</p>

              <div v-if="pacienteActivo" class="paciente-owner">
                <User :size="13" />
                <strong>{{ pacienteActivo.clienteNombre }}</strong>
                <span class="owner-sep">·</span>
                <span class="owner-doc">{{ pacienteActivo.clienteDocumento }}</span>
                <template v-if="pacienteActivo.clienteTelefono">
                  <span class="owner-sep">·</span>
                  <span class="owner-phone">
                    <Phone :size="12" /> {{ pacienteActivo.clienteTelefono }}
                  </span>
                </template>
              </div>
            </div>

            <div class="paciente-actions">
              <button
                class="btn-secondary"
                type="button"
                :disabled="cargandoHistorial"
                @click="cargarHistorial"
              >
                <RefreshCw :size="14" :class="{ spin: cargandoHistorial }" />
                Actualizar
              </button>
            </div>
          </section>

          <!-- KPIs -->
          <section v-if="!cargandoHistorial && historial.length" class="kpis">
            <article class="kpi">
              <div class="kpi-icon" style="--kpi-color: #0F766E; --kpi-bg: #F0FDFA;">
                <FileText :size="16" />
              </div>
              <div class="kpi-texto">
                <p class="kpi-value">{{ historial.length }}</p>
                <p class="kpi-label">{{ historial.length === 1 ? 'Atención' : 'Atenciones' }}</p>
              </div>
            </article>
            <article class="kpi">
              <div class="kpi-icon" style="--kpi-color: #3B82F6; --kpi-bg: #EFF6FF;">
                <CalendarDays :size="16" />
              </div>
              <div class="kpi-texto">
                <p class="kpi-value">{{ ultimaVisita }}</p>
                <p class="kpi-label">Última visita</p>
              </div>
            </article>
            <article class="kpi">
              <div class="kpi-icon" style="--kpi-color: #8B5CF6; --kpi-bg: #F5F3FF;">
                <Pill :size="16" />
              </div>
              <div class="kpi-texto">
                <p class="kpi-value">{{ totalRecetas }}</p>
                <p class="kpi-label">{{ totalRecetas === 1 ? 'Récipe' : 'Récipes' }}</p>
              </div>
            </article>
            <article class="kpi">
              <div class="kpi-icon" style="--kpi-color: #F59E0B; --kpi-bg: #FFFBEB;">
                <Package :size="16" />
              </div>
              <div class="kpi-texto">
                <p class="kpi-value">{{ totalInsumos }}</p>
                <p class="kpi-label">{{ totalInsumos === 1 ? 'Insumo' : 'Insumos' }}</p>
              </div>
            </article>
          </section>

          <!-- Loading -->
          <div v-if="cargandoHistorial" class="loading-state">
            <div class="spin"></div>
            <p>Cargando el historial clínico…</p>
          </div>

          <!-- Sin atenciones -->
          <section v-else-if="!historial.length" class="card empty-card">
            <div class="card-body">
              <div class="empty-state slim">
                <div class="empty-icon">
                  <Inbox :size="28" />
                </div>
                <h3>Sin atenciones registradas</h3>
                <p>
                  Este paciente aún no tiene atenciones en su expediente.
                  Cuando registres una consulta aparecerá aquí automáticamente.
                </p>
              </div>
            </div>
          </section>

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
                      <span v-if="atencion.receta" class="pill-receta">
                        <Pill :size="11" /> Récipe
                      </span>
                      <span class="pill-estado">
                        {{ String(atencion.estadoAtencion || '').replaceAll('_', ' ') }}
                      </span>
                      <span class="atencion-toggle" aria-hidden="true">
                        <ChevronDown :size="16" />
                      </span>
                    </div>
                  </button>

                  <Transition name="expand">
                    <div v-show="estaExpandida(atencion.idAtencion)" class="atencion-body">
                      <div class="vitales-grid">
                        <div class="vital">
                          <Weight :size="14" class="vital-icon" />
                          <span class="vital-label">Peso</span>
                          <strong class="vital-value">{{ vital(atencion.pesoKg, 'kg') }}</strong>
                        </div>
                        <div class="vital">
                          <Thermometer :size="14" class="vital-icon" />
                          <span class="vital-label">Temp.</span>
                          <strong class="vital-value">{{ vital(atencion.temperaturaC, '°C') }}</strong>
                        </div>
                        <div class="vital">
                          <Heart :size="14" class="vital-icon" />
                          <span class="vital-label">FC</span>
                          <strong class="vital-value">{{ vital(atencion.frecCardiaca, 'lpm') }}</strong>
                        </div>
                        <div class="vital">
                          <Wind :size="14" class="vital-icon" />
                          <span class="vital-label">FR</span>
                          <strong class="vital-value">{{ vital(atencion.frecRespiratoria, 'rpm') }}</strong>
                        </div>
                      </div>

                      <div class="detalle-textos">
                        <div class="detalle-bloque">
                          <h5>Anamnesis</h5>
                          <p>{{ atencion.anamnesis || '—' }}</p>
                        </div>
                        <div v-if="atencion.sintomasObservados" class="detalle-bloque">
                          <h5>Hallazgos</h5>
                          <p>{{ atencion.sintomasObservados }}</p>
                        </div>
                        <div class="detalle-bloque dx">
                          <h5>Diagnóstico</h5>
                          <p>{{ atencion.diagnosticoPrincipal || '—' }}</p>
                        </div>
                        <div v-if="atencion.diagnosticosDiferenciales" class="detalle-bloque">
                          <h5>Dx. diferenciales</h5>
                          <p>{{ atencion.diagnosticosDiferenciales }}</p>
                        </div>
                        <div v-if="atencion.observacionesGenerales" class="detalle-bloque">
                          <h5>Pronóstico</h5>
                          <p>{{ atencion.observacionesGenerales }}</p>
                        </div>
                        <div v-if="atencion.indicacionesDueno" class="detalle-bloque">
                          <h5>Indicaciones al dueño</h5>
                          <p>{{ atencion.indicacionesDueno }}</p>
                        </div>
                        <div v-if="atencion.proximaCitaRecomendada" class="detalle-bloque">
                          <h5>Próxima cita</h5>
                          <p>{{ atencion.proximaCitaRecomendada }}</p>
                        </div>
                      </div>

                      <div v-if="atencion.insumos?.length" class="bloque-interno">
                        <h5><Package :size="13" /> Insumos aplicados</h5>
                        <div class="tabla-wrap">
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
                              <tr v-for="i in atencion.insumos" :key="i.idProducto">
                                <td>
                                  {{ i.nombre }}
                                  <span class="sku">({{ i.codigoSku }})</span>
                                </td>
                                <td class="der">{{ i.cantidad }} {{ i.unidadMedida }}</td>
                                <td class="der">{{ formatoUSD(i.precioUnitarioUsd) }}</td>
                                <td class="der amount">{{ formatoUSD(subtotalInsumo(i)) }}</td>
                              </tr>
                            </tbody>
                          </table>
                        </div>
                        <p class="total-linea">
                          <span>Total insumos</span>
                          <span class="amount">{{ formatoUSD(totalInsumosAtencion(atencion)) }}</span>
                        </p>
                      </div>

                      <div v-if="atencion.receta" class="bloque-interno bloque-receta">
                        <div class="receta-cabecera">
                          <h5><Pill :size="13" /> Récipe {{ atencion.receta.codigoReceta }}</h5>
                          <button
                            class="btn-pdf"
                            type="button"
                            :disabled="descargandoReceta === atencion.receta.idReceta"
                            @click.stop="descargarReceta(atencion.receta)"
                          >
                            <Loader2
                              v-if="descargandoReceta === atencion.receta.idReceta"
                              :size="14"
                              class="spin"
                            />
                            <Download v-else :size="14" />
                            {{ descargandoReceta === atencion.receta.idReceta ? 'Descargando…' : 'Descargar PDF' }}
                          </button>
                        </div>
                        <p v-if="atencion.receta.indicacionesGenerales" class="receta-indicaciones">
                          <strong>Indicaciones generales:</strong> {{ atencion.receta.indicacionesGenerales }}
                        </p>
                        <div class="tabla-wrap">
                          <table class="data-table">
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
                                <td>{{ item.medicamento }}</td>
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
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  ArrowLeft, CalendarDays, ChevronDown, ChevronRight, Download,
  FileText, Heart, Inbox, Loader2, Package, PawPrint, Phone, Pill,
  RefreshCw, Search, Stethoscope, Thermometer, User, Weight, Wind, X
} from 'lucide-vue-next';
import { useToast } from '@/composables/useToast';
import ToastContainer from '@/components/ui/ToastContainer.vue';
import PetAvatar from '@/components/ui/PetAvatar.vue';
import { getHistorialMascota, descargarRecetaPdf } from '@/api/atenciones.api';
import { buscarMascotas } from '@/api/mascotas.api';
import { getApiErrorMessage } from '@/utils/apiError';
import { descargarBlob } from '@/utils/descargas';
import { fechaHoraCorta } from '@/utils/fecha';

const route = useRoute();
const router = useRouter();
const { toastError } = useToast();

const formatoUSD = (valor) => `$${Number(valor || 0).toFixed(2)}`;

const idMascota = computed(() => route.query.mascota);
const nombreQuery = computed(() => route.query.nombre);

const filtro = ref('');
const filtroAplicado = ref('');
const resultados = ref([]);
const buscando = ref(false);
let temporizador = null;

const historial = ref([]);
const cargandoHistorial = ref(false);
const descargandoReceta = ref(null);
const pacienteActivo = ref(null);

const cachePacientes = ref({});

const nombrePaciente = computed(() =>
  pacienteActivo.value?.nombre ||
  nombreQuery.value ||
  `Paciente #${idMascota.value}`
);

const expandidas = ref({});
function estaExpandida(id) { return !!expandidas.value[id]; }
function toggleExpandida(id) {
  expandidas.value = { ...expandidas.value, [id]: !expandidas.value[id] };
}

watch(filtro, (valor) => {
  clearTimeout(temporizador);
  temporizador = setTimeout(() => {
    filtroAplicado.value = valor.trim();
  }, 350);
});

watch(filtroAplicado, async (valor) => {
  if (!valor) {
    resultados.value = [];
    return;
  }
  buscando.value = true;
  try {
    resultados.value = await buscarMascotas(valor);
    for (const m of resultados.value) {
      cachePacientes.value[m.idMascota] = m;
    }
  } catch (error) {
    toastError(getApiErrorMessage(error));
    resultados.value = [];
  } finally {
    buscando.value = false;
  }
});

function seleccionarPaciente(mascota) {
  cachePacientes.value[mascota.idMascota] = mascota;
  pacienteActivo.value = mascota;
  router.replace({
    path: '/veterinario/historiales',
    query: { mascota: mascota.idMascota, nombre: mascota.nombre },
  });
}

function volverALaLista() {
  router.replace({ path: '/veterinario/historiales' });
  pacienteActivo.value = null;
}

async function cargarHistorial() {
  if (!idMascota.value) {
    historial.value = [];
    return;
  }
  cargandoHistorial.value = true;
  historial.value = [];
  expandidas.value = {};

  try {
    historial.value = await getHistorialMascota(idMascota.value);
    const primera = historial.value[0]?.idAtencion;
    if (primera) expandidas.value = { [primera]: true };
  } catch (error) {
    toastError(getApiErrorMessage(error));
    historial.value = [];
  } finally {
    cargandoHistorial.value = false;
  }
}

async function resolverPacienteActivo() {
  const id = idMascota.value;
  if (!id) {
    pacienteActivo.value = null;
    return;
  }
  const cached = cachePacientes.value[id];
  if (cached) {
    pacienteActivo.value = cached;
    return;
  }
  const nombre = nombreQuery.value;
  if (!nombre) {
    pacienteActivo.value = null;
    return;
  }
  try {
    const coincidencias = await buscarMascotas(nombre);
    const match =
      coincidencias.find((m) => String(m.idMascota) === String(id)) ||
      coincidencias[0] ||
      null;
    if (match) cachePacientes.value[id] = match;
    pacienteActivo.value = match;
  } catch {
    pacienteActivo.value = null;
  }
}

watch(idMascota, async () => {
  await resolverPacienteActivo();
  await cargarHistorial();
}, { immediate: false });

function vital(valor, unidad) {
  return valor !== null && valor !== undefined && valor !== ''
    ? `${valor} ${unidad}`
    : '—';
}

function subtotalInsumo(insumo) {
  return insumo.subtotal ?? insumo.subtotalUsd ?? (insumo.cantidad * insumo.precioUnitarioUsd);
}
function totalInsumosAtencion(atencion) {
  return (atencion.insumos || []).reduce((suma, i) => suma + subtotalInsumo(i), 0);
}

const ultimaVisita = computed(() => {
  const primera = historial.value[0];
  if (!primera?.fechaHoraInicio) return '—';
  return fechaHoraCorta(primera.fechaHoraInicio).split('·')[0]?.trim() || '—';
});
const totalRecetas = computed(() => historial.value.filter(a => a.receta).length);
const totalInsumos = computed(() =>
  historial.value.reduce((sum, a) => sum + (a.insumos?.length || 0), 0)
);

async function descargarReceta(receta) {
  try {
    descargandoReceta.value = receta.idReceta;
    const { blob, filename } = await descargarRecetaPdf(receta.idReceta);
    descargarBlob(blob, filename || `receta-${receta.codigoReceta}.pdf`);
  } catch (error) {
    toastError(getApiErrorMessage(error));
  } finally {
    descargandoReceta.value = null;
  }
}

onMounted(async () => {
  await resolverPacienteActivo();
  if (idMascota.value) await cargarHistorial();
});

onBeforeUnmount(() => {
  clearTimeout(temporizador);
});
</script>

<style scoped>
/* ═══════════════════════════════════════════════════════════
   BASE + TOKENS
   ═══════════════════════════════════════════════════════════ */
.historiales-view {
  max-width: 1500px;
  margin: 0 auto;
  padding: 24px 28px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #0F172A;
  -webkit-font-smoothing: antialiased;
}
button { font-family: inherit; }

/* Sistema de sombras en capas (patrón Linear/Stripe) */
:root {
  --shadow-xs: 0 1px 2px rgba(15, 23, 42, 0.04);
  --shadow-sm: 0 1px 2px rgba(15, 23, 42, 0.04), 0 1px 3px rgba(15, 23, 42, 0.06);
  --shadow-md: 0 2px 4px rgba(15, 23, 42, 0.04), 0 4px 12px -2px rgba(15, 23, 42, 0.06);
  --shadow-lg: 0 4px 8px -2px rgba(15, 23, 42, 0.06), 0 12px 24px -4px rgba(15, 23, 42, 0.08);
  --shadow-xl: 0 8px 16px -4px rgba(15, 23, 42, 0.08), 0 24px 48px -8px rgba(15, 23, 42, 0.1);
}

/* ═══════════════════════════════════════════════════════════
   MASTER-DETAIL LAYOUT
   ═══════════════════════════════════════════════════════════ */
.master-detail {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

/* ═══════════════════════════════════════════════════════════
   SIDEBAR
   ═══════════════════════════════════════════════════════════ */
.sidebar {
  position: sticky;
  top: 24px;
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.04),
    0 4px 12px -2px rgba(15, 23, 42, 0.06);
  overflow: hidden;
}

.sidebar-header {
  padding: 22px 22px 4px;
}
.sidebar-title {
  display: flex;
  align-items: center;
  gap: 9px;
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.015em;
}
.sidebar-title svg { color: #0F766E; }
.sidebar-subtitle {
  margin: 0;
  font-size: 12.5px;
  color: #64748B;
  line-height: 1.5;
}

/* ─── Buscador ─── */
.search-box {
  position: relative;
  margin: 0 22px;
}
.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #94A3B8;
  pointer-events: none;
  transition: color .2s ease;
}
.search-input {
  width: 100%;
  padding: 12px 40px 12px 42px;
  border: 1.5px solid #E2E8F0;
  border-radius: 11px;
  font-size: 13.5px;
  color: #0F172A;
  background: #F8FAFC;
  font-family: inherit;
  outline: none;
  box-sizing: border-box;
  transition: border-color .2s, box-shadow .2s, background-color .2s;
}
.search-input::placeholder { color: #94A3B8; }
.search-input:focus {
  border-color: #0F766E;
  background: #FFFFFF;
  box-shadow: 0 0 0 4px rgba(15, 118, 110, .1);
}
.search-box:focus-within .search-icon { color: #0F766E; }
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

/* ─── Contador ─── */
.results-count {
  padding: 0 22px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .7px;
  color: #64748B;
}
.results-count-empty { color: #94A3B8; }

/* ─── Cuerpo con scroll ─── */
.sidebar-body {
  flex: 1;
  overflow-y: auto;
  padding: 4px 14px 20px;
  scrollbar-width: thin;
  scrollbar-color: #CBD5E1 transparent;
}
.sidebar-body::-webkit-scrollbar { width: 6px; }
.sidebar-body::-webkit-scrollbar-track { background: transparent; }
.sidebar-body::-webkit-scrollbar-thumb {
  background: #CBD5E1;
  border-radius: 3px;
}
.sidebar-body::-webkit-scrollbar-thumb:hover { background: #94A3B8; }

/* ─── Estados inline ─── */
.state-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
  padding: 40px 20px;
  color: #94A3B8;
}
.state-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #F0FDFA 0%, #ECFDF5 100%);
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;
  box-shadow: 0 4px 12px -4px rgba(15, 118, 110, .2);
}
.state-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
}
.state-text {
  margin: 0;
  font-size: 12.5px;
  color: #64748B;
  line-height: 1.55;
  max-width: 250px;
}
.state-text strong { color: #0F172A; font-weight: 700; }

/* ═══════════════════════════════════════════════════════════
   LISTA DE PACIENTES — el corazón visual del sidebar
   ═══════════════════════════════════════════════════════════ */
.patient-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.patient-item { margin: 0; }

.patient-btn {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 14px;
  align-items: center;
  width: 100%;
  padding: 14px 16px;
  background: #FFFFFF;
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  position: relative;
  overflow: hidden;
  transition:
    border-color .2s ease,
    background-color .2s ease,
    box-shadow .25s ease,
    transform .2s ease;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    0 1px 3px rgba(15, 23, 42, 0.02);
}

/* Acento lateral que aparece al hover/selección */
.patient-btn::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: #0F766E;
  transform: scaleY(0);
  transform-origin: center;
  transition: transform .25s cubic-bezier(0.16, 1, 0.3, 1);
}

.patient-btn:hover {
  border-color: #CBD5E1;
  background: #FFFFFF;
  transform: translateY(-1px);
  box-shadow:
    0 4px 8px -2px rgba(15, 23, 42, 0.06),
    0 12px 24px -4px rgba(15, 23, 42, 0.08);
}
.patient-btn:hover::before { transform: scaleY(1); }

.patient-item.is-selected .patient-btn {
  border-color: #0F766E;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 100%);
  box-shadow:
    0 0 0 4px rgba(15, 118, 110, .08),
    0 4px 8px -2px rgba(15, 118, 110, 0.08);
}
.patient-item.is-selected .patient-btn::before { transform: scaleY(1); }

.patient-btn:focus-visible {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 4px rgba(15, 118, 110, .15);
}

.patient-info { min-width: 0; }
.patient-name {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.patient-name-text {
  font-size: 14px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.25;
  letter-spacing: -0.01em;
}
.pill-status {
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 9.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .4px;
  white-space: nowrap;
}
.pill-status.is-muted {
  background: #F1F5F9;
  color: #64748B;
  border: 1px solid #E2E8F0;
}
.patient-meta {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}
.patient-owner {
  margin: 5px 0 0;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  color: #94A3B8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  font-weight: 500;
}
.patient-chevron {
  color: #CBD5E1;
  flex-shrink: 0;
  transition: color .2s ease, transform .25s cubic-bezier(0.16, 1, 0.3, 1);
}
.patient-btn:hover .patient-chevron {
  color: #0F766E;
  transform: translateX(2px);
}
.patient-item.is-selected .patient-chevron {
  color: #0F766E;
  transform: translateX(2px);
}

/* ═══════════════════════════════════════════════════════════
   PANEL DETALLE
   ═══════════════════════════════════════════════════════════ */
.detail-panel {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.btn-back-mobile {
  display: none;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  align-self: flex-start;
  transition: all .2s ease;
}
.btn-back-mobile:hover {
  background: #F0FDFA;
  border-color: #99F6E4;
  color: #0F766E;
}

/* ─── Empty detail ─── */
.empty-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 14px;
  padding: 100px 32px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    0 4px 12px -2px rgba(15, 23, 42, 0.04);
}
.empty-detail-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #F0FDFA 0%, #ECFDF5 100%);
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  box-shadow: 0 8px 24px -8px rgba(15, 118, 110, .25);
}
.empty-detail-title {
  margin: 0;
  font-size: 19px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.015em;
}
.empty-detail-text {
  margin: 0;
  font-size: 13.5px;
  color: #64748B;
  line-height: 1.65;
  max-width: 460px;
}

/* ═══════════════════════════════════════════════════════════
   CABECERA DEL PACIENTE
   ═══════════════════════════════════════════════════════════ */
.paciente-hero {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 22px;
  align-items: center;
  padding: 24px 28px;
  background:
    linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
  box-shadow:
    0 1px 2px rgba(15, 118, 110, 0.04),
    0 8px 24px -12px rgba(15, 118, 110, 0.15);
  position: relative;
  overflow: hidden;
}
.paciente-hero::after {
  content: '';
  position: absolute;
  top: -40px;
  right: -40px;
  width: 160px;
  height: 160px;
  background: radial-gradient(circle, rgba(15, 118, 110, 0.06) 0%, transparent 70%);
  pointer-events: none;
}

.paciente-datos { min-width: 0; position: relative; z-index: 1; }
.paciente-nombre {
  margin: 0 0 10px;
  font-size: 24px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.02em;
  line-height: 1.15;
}
.paciente-id {
  margin: 4px 0 0;
  font-size: 13px;
  color: #64748B;
  font-weight: 500;
}
.paciente-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-bottom: 12px;
}
.paciente-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid #CCFBF1;
  color: #0F766E;
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: .1px;
  box-shadow: 0 1px 2px rgba(15, 118, 110, 0.05);
}
.paciente-badge.is-warn {
  background: #FEF2F2;
  border-color: #FECACA;
  color: #DC2626;
  box-shadow: 0 1px 2px rgba(220, 38, 38, 0.05);
}
.paciente-owner {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 7px;
  font-size: 12.5px;
  color: #64748B;
}
.paciente-owner svg { color: #94A3B8; flex-shrink: 0; }
.paciente-owner strong { color: #334155; font-weight: 700; }
.owner-doc {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 11.5px;
  background: #fff;
  padding: 3px 9px;
  border-radius: 6px;
  color: #475569;
  border: 1px solid #E2E8F0;
  font-weight: 600;
}
.owner-sep { color: #CBD5E1; }
.owner-phone {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.paciente-actions {
  display: flex;
  align-items: center;
  position: relative;
  z-index: 1;
}

/* ═══════════════════════════════════════════════════════════
   KPIs
   ═══════════════════════════════════════════════════════════ */
.kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.kpi {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    0 1px 3px rgba(15, 23, 42, 0.02);
  transition:
    border-color .2s ease,
    box-shadow .25s ease,
    transform .2s ease;
}
.kpi:hover {
  border-color: #CBD5E1;
  transform: translateY(-2px);
  box-shadow:
    0 4px 8px -2px rgba(15, 23, 42, 0.06),
    0 12px 24px -4px rgba(15, 23, 42, 0.08);
}
.kpi-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--kpi-bg);
  color: var(--kpi-color);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, .5);
}
.kpi-texto { min-width: 0; }
.kpi-value {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.1;
  letter-spacing: -0.02em;
}
.kpi-label {
  margin: 3px 0 0;
  font-size: 11.5px;
  color: #64748B;
  font-weight: 600;
  letter-spacing: .1px;
}

/* ═══════════════════════════════════════════════════════════
   BOTONES
   ═══════════════════════════════════════════════════════════ */
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 18px;
  background: #fff;
  color: #475569;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
}
.btn-secondary:hover:not(:disabled) {
  background: #F8FAFC;
  border-color: #CBD5E1;
  color: #0F766E;
  box-shadow:
    0 4px 8px -2px rgba(15, 23, 42, 0.06),
    0 2px 4px rgba(15, 23, 42, 0.04);
}
.btn-secondary:disabled { opacity: .55; cursor: not-allowed; }

/* ═══════════════════════════════════════════════════════════
   LOADING
   ═══════════════════════════════════════════════════════════ */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  padding: 80px 24px;
  color: #64748B;
  font-size: 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    0 4px 12px -2px rgba(15, 23, 42, 0.04);
}
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══════════════════════════════════════════════════════════
   CARDS BASE
   ═══════════════════════════════════════════════════════════ */
.card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    0 4px 12px -2px rgba(15, 23, 42, 0.04);
}
.card-body { padding: 28px; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 24px;
  color: #94A3B8;
  text-align: center;
  font-size: 13.5px;
}
.empty-state.slim { padding: 28px; }
.empty-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #F0FDFA 0%, #ECFDF5 100%);
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 6px;
  box-shadow: 0 6px 16px -6px rgba(15, 118, 110, .2);
}
.empty-state h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
}
.empty-state p {
  margin: 0;
  max-width: 440px;
  line-height: 1.6;
  color: #64748B;
}

/* ═══════════════════════════════════════════════════════════
   TIMELINE HEADER
   ═══════════════════════════════════════════════════════════ */
.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 4px;
}
.timeline-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
}
.timeline-count {
  font-size: 12px;
  font-weight: 700;
  color: #64748B;
  background: #F1F5F9;
  padding: 5px 14px;
  border-radius: 20px;
  border: 1px solid #E2E8F0;
}

/* ═══════════════════════════════════════════════════════════
   TIMELINE
   ═══════════════════════════════════════════════════════════ */
.timeline {
  position: relative;
  padding-left: 32px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.timeline::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 16px;
  bottom: 16px;
  width: 2px;
  background: linear-gradient(
    to bottom,
    #99F6E4 0%,
    #E2E8F0 50%,
    #E2E8F0 100%
  );
  border-radius: 2px;
}
.timeline-item { position: relative; }
.timeline-dot {
  position: absolute;
  left: -32px;
  top: 24px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0F766E 0%, #14B8A6 100%);
  border: 3px solid #fff;
  box-shadow:
    0 0 0 3px #99F6E4,
    0 4px 8px -2px rgba(15, 118, 110, .3);
  z-index: 1;
}

/* ═══════════════════════════════════════════════════════════
   ATENCIÓN CARD
   ═══════════════════════════════════════════════════════════ */
.atencion-card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  overflow: hidden;
  transition:
    border-color .2s ease,
    box-shadow .25s ease,
    transform .2s ease;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    0 1px 3px rgba(15, 23, 42, 0.02);
}
.atencion-card:hover {
  border-color: #CBD5E1;
  transform: translateY(-1px);
  box-shadow:
    0 4px 8px -2px rgba(15, 23, 42, 0.06),
    0 12px 24px -4px rgba(15, 23, 42, 0.08);
}
.atencion-card.is-open {
  border-color: #99F6E4;
  box-shadow:
    0 0 0 4px rgba(15, 118, 110, 0.06),
    0 8px 20px -8px rgba(15, 118, 110, 0.15);
}
.atencion-head {
  width: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 20px 22px;
  background: transparent;
  border: none;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
  transition: background-color .15s ease;
}
.atencion-head:hover { background: #FAFBFC; }
.atencion-head:focus-visible { outline: none; background: #F0FDFA; }
.atencion-head-main { min-width: 0; flex: 1; }
.atencion-fecha {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #0F766E;
  letter-spacing: -0.01em;
}
.atencion-vet {
  margin: 5px 0 0;
  font-size: 12.5px;
  color: #64748B;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}
.atencion-dx {
  margin: 10px 0 0;
  font-size: 13px;
  color: #334155;
  line-height: 1.55;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.atencion-dx strong {
  color: #64748B;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 11px;
  letter-spacing: .5px;
  margin-right: 4px;
}

.atencion-head-side {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.pill-receta {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 11px;
  border-radius: 20px;
  background: #FFFBEB;
  color: #92400E;
  border: 1px solid #FDE68A;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
  box-shadow: 0 1px 2px rgba(217, 119, 6, 0.08);
}
.pill-estado {
  padding: 5px 13px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
  background: #F0FDFA;
  color: #0F766E;
  border: 1px solid #99F6E4;
  white-space: nowrap;
  box-shadow: 0 1px 2px rgba(15, 118, 110, 0.08);
}
.atencion-toggle {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  background: #F1F5F9;
  color: #64748B;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition:
    transform .3s cubic-bezier(0.16, 1, 0.3, 1),
    background-color .2s ease,
    color .2s ease;
}
.atencion-card.is-open .atencion-toggle {
  background: #F0FDFA;
  color: #0F766E;
  transform: rotate(180deg);
}

/* ═══════════════════════════════════════════════════════════
   CUERPO ATENCIÓN
   ═══════════════════════════════════════════════════════════ */
.atencion-body {
  padding: 8px 22px 24px;
  border-top: 1px solid #F1F5F9;
}
.vitales-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin: 20px 0;
}
.vital {
  display: grid;
  grid-template-columns: auto 1fr;
  grid-template-rows: auto auto;
  align-items: center;
  column-gap: 10px;
  row-gap: 3px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 12px 14px;
  transition: border-color .2s ease, background-color .2s ease;
}
.vital:hover {
  border-color: #CBD5E1;
  background: #FFFFFF;
}
.vital-icon {
  grid-row: 1 / span 2;
  color: #94A3B8;
}
.vital-label {
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #64748B;
}
.vital-value {
  font-size: 14.5px;
  color: #0F172A;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.detalle-textos {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 6px;
}
.detalle-bloque {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 14px 16px;
  transition: border-color .2s ease;
}
.detalle-bloque:hover { border-color: #CBD5E1; }
.detalle-bloque.dx {
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 100%);
  border-color: #99F6E4;
}
.detalle-bloque h5 {
  margin: 0 0 8px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .7px;
  color: #64748B;
}
.detalle-bloque.dx h5 { color: #0F766E; }
.detalle-bloque p {
  margin: 0;
  font-size: 13.5px;
  color: #334155;
  line-height: 1.6;
}

/* ─── Bloques internos ─── */
.bloque-interno {
  margin-top: 18px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 16px 18px;
}
.bloque-interno h5 {
  margin: 0 0 12px;
  font-size: 11.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #64748B;
  display: flex;
  align-items: center;
  gap: 7px;
}
.bloque-receta {
  background: linear-gradient(135deg, #FFFBEB 0%, #FFFFFF 100%);
  border-color: #FDE68A;
}
.bloque-receta h5 { color: #92400E; }
.receta-cabecera {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.receta-cabecera h5 { margin: 0; color: #92400E; font-size: 12.5px; }
.receta-indicaciones {
  margin: 0 0 14px;
  font-size: 13px;
  color: #334155;
  line-height: 1.6;
}

/* ─── Botón PDF ─── */
.btn-pdf {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 15px;
  background: #0F766E;
  color: #fff;
  border: none;
  border-radius: 9px;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
  box-shadow: 0 1px 2px rgba(15, 118, 110, 0.15);
}
.btn-pdf:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow:
    0 4px 8px -2px rgba(15, 118, 110, 0.2),
    0 8px 16px -4px rgba(15, 118, 110, 0.25);
}
.btn-pdf:disabled { opacity: .65; cursor: not-allowed; }

/* ─── Tablas ─── */
.tabla-wrap { overflow-x: auto; border-radius: 10px; }
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-family: inherit;
  min-width: 480px;
}
.data-table th {
  text-align: left;
  padding: 10px 12px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #64748B;
  border-bottom: 1.5px solid #E2E8F0;
  background: transparent;
}
.data-table td {
  padding: 10px 12px;
  font-size: 13px;
  color: #0F172A;
  border-bottom: 1px solid #F1F5F9;
  vertical-align: middle;
}
.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr { transition: background-color .15s ease; }
.data-table tbody tr:hover td { background: rgba(15, 118, 110, .03); }
.amount { font-weight: 700; color: #0F766E; }
.der { text-align: right; }
.sku { color: #94A3B8; font-size: 12px; }
.total-linea {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 14px 0 0;
  padding-top: 12px;
  border-top: 1.5px dashed #CBD5E1;
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}

/* ═══════════════════════════════════════════════════════════
   TRANSICIONES
   ═══════════════════════════════════════════════════════════ */
.expand-enter-active,
.expand-leave-active {
  transition: opacity .25s ease, transform .25s ease;
}
.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ═══════════════════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════════════════ */
@media (max-width: 1200px) {
  .master-detail { grid-template-columns: 320px minmax(0, 1fr); }
}

@media (max-width: 900px) {
  .historiales-view { padding: 16px 16px 32px; }

  .master-detail {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .sidebar {
    position: static;
    max-height: none;
  }
  .sidebar-body {
    max-height: 60vh;
  }

  .historiales-view.has-selected .sidebar { display: none; }
  .historiales-view:not(.has-selected) .detail-panel { display: none; }

  .btn-back-mobile { display: inline-flex; }

  .paciente-hero {
    grid-template-columns: auto 1fr;
    gap: 18px;
    padding: 20px;
    border-radius: 14px;
  }
  .paciente-nombre { font-size: 20px; }
  .paciente-actions {
    grid-column: 1 / -1;
    justify-content: flex-start;
  }

  .kpis { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .vitales-grid { grid-template-columns: repeat(2, 1fr); }
  .detalle-textos { grid-template-columns: 1fr; }

  .timeline { padding-left: 24px; }
  .timeline-dot { left: -24px; width: 14px; height: 14px; }
  .timeline::before { left: 6px; }
  .atencion-head { padding: 16px 18px; }
  .atencion-head-side {
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
  }
  .atencion-body { padding: 6px 18px 20px; }

  .empty-detail { padding: 60px 24px; }
}

@media (max-width: 480px) {
  .historiales-view { padding: 12px 14px 28px; }
  .sidebar-header { padding: 18px 18px 4px; }
  .search-box { margin: 0 18px; }
  .results-count { padding: 0 18px; }

  .kpis { grid-template-columns: 1fr; gap: 10px; }
  .paciente-hero {
    grid-template-columns: 1fr;
    text-align: center;
    justify-items: center;
  }
  .paciente-owner { justify-content: center; }
  .receta-cabecera { flex-direction: column; align-items: stretch; }
  .btn-pdf { width: 100%; }

  .patient-btn { padding: 12px 14px; gap: 12px; }
}
</style>