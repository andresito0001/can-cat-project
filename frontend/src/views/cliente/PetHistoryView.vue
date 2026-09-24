<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  ChevronRight, ChevronDown, Inbox, Package, PawPrint, Pill, Stethoscope,
  User, Calendar, FileText, Activity, Weight, Thermometer, Heart, Wind,
  ArrowLeft, Plus
} from 'lucide-vue-next';
import { useToast } from '@/composables/useToast';
import ToastContainer from '@/components/ui/ToastContainer.vue';
import { getHistorialMascota } from '@/api/atenciones.api';
import { getMisMascotas, getEspecies } from '@/api/mascotas.api';
import { getApiErrorMessage } from '@/utils/apiError';
import { useAuthStore } from '@/stores/auth.store';
import { fechaHoraCorta } from '@/utils/fecha';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const { toastError } = useToast();

const formatoUSD = (valor) => `$${Number(valor || 0).toFixed(2)}`;

// Devuelve "valor unidad" o "—" si no hay dato (evita "null kg" / "undefined °C")
function vital(valor, unidad) {
  return valor !== null && valor !== undefined && valor !== ''
    ? `${valor} ${unidad}`
    : '—';
}

const idMascota = computed(() => route.query.mascota);
const misMascotas = ref([]);
const especies = ref([]);
const cargando = ref(false);
const historial = ref([]);
const infoMascota = ref(null);

// Estado de atenciones expandidas: { [idAtencion]: true }
const expandidas = ref({});
function estaExpandida(id) { return !!expandidas.value[id]; }
function toggleExpandida(id) {
  expandidas.value = { ...expandidas.value, [id]: !expandidas.value[id] };
}

// ─── HELPERS ───
function especieNombre(idEspecie) {
  const e = especies.value.find((esp) => esp.id === idEspecie);
  return e ? e.nombre : null;
}
function sexoLabel(sexo) {
  return sexo === 'M' ? 'Macho' : sexo === 'H' ? 'Hembra' : null;
}

const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];
function formatFechaNac(iso) {
  if (!iso) return null;
  const [y, m, d] = String(iso).slice(0, 10).split('-');
  return `${d} ${MESES[Number(m) - 1]} ${y}`;
}

// Edad legible: "3 años", "8 meses", "Recién nacido"
function edadMascota(iso) {
  if (!iso) return null;
  const nac = new Date(iso);
  if (Number.isNaN(nac.getTime())) return null;
  const hoy = new Date();
  let anios = hoy.getFullYear() - nac.getFullYear();
  const m = hoy.getMonth() - nac.getMonth();
  if (m < 0 || (m === 0 && hoy.getDate() < nac.getDate())) anios--;
  if (anios < 1) {
    const meses = Math.max(
      0,
      (hoy.getFullYear() - nac.getFullYear()) * 12 + (hoy.getMonth() - nac.getMonth())
    );
    if (meses === 0) return 'Recién nacido';
    return `${meses} ${meses === 1 ? 'mes' : 'meses'}`;
  }
  return `${anios} ${anios === 1 ? 'año' : 'años'}`;
}

const metadatosMascota = computed(() => {
  const m = infoMascota.value;
  if (!m) return [];
  return [
    especieNombre(m.idEspecie),
    sexoLabel(m.sexo),
    edadMascota(m.fechaNacimiento),
  ].filter(Boolean);
});

const subtotalInsumo = (insumo) =>
  insumo.subtotal ?? insumo.subtotalUsd ?? insumo.cantidad * insumo.precioUnitarioUsd;

const totalInsumosAtencion = (atencion) =>
  (atencion.insumos || []).reduce((suma, i) => suma + subtotalInsumo(i), 0);

// ─── STATS KPI ───
const stats = computed(() => {
  const total = historial.value.length;
  const ultima = historial.value[0]?.fechaHoraInicio;
  const m = infoMascota.value;
  return {
    total,
    ultimaVisita: ultima ? fechaHoraCorta(ultima).split('·')[0]?.trim() : '—',
    edad: edadMascota(m?.fechaNacimiento) || '—',
    peso: m?.pesoActual ? `${m.pesoActual} kg` : '—',
  };
});

// ─── AVATAR ───
const PALETA_AVATARES = ['#0F766E', '#3B82F6', '#F59E0B', '#F43F5E', '#8B5CF6', '#0EA5E9'];
function colorAvatar(nombre) {
  let hash = 0;
  for (const caracter of String(nombre || '')) hash = (hash * 31 + caracter.charCodeAt(0)) % 997;
  return PALETA_AVATARES[hash % PALETA_AVATARES.length];
}
function inicialNombre(nombre) {
  return String(nombre || '?').trim().charAt(0).toUpperCase() || '?';
}

// ─── CARGA ───
async function cargar() {
  const [mascotasRes, especiesRes] = await Promise.allSettled([getMisMascotas(), getEspecies()]);
  if (mascotasRes.status === 'fulfilled') misMascotas.value = mascotasRes.value.data;
  if (especiesRes.status === 'fulfilled') especies.value = especiesRes.value.data;

  infoMascota.value =
    misMascotas.value.find((m) => String(m.idMascota) === String(idMascota.value)) || null;

  if (!idMascota.value) {
    historial.value = [];
    cargando.value = false;
    return;
  }

  cargando.value = true;
  historial.value = [];
  expandidas.value = {};
  try {
    historial.value = await getHistorialMascota(idMascota.value);
    // La más reciente se expande por defecto
    const primera = historial.value[0]?.idAtencion;
    if (primera) expandidas.value = { [primera]: true };
  } catch (error) {
    toastError(getApiErrorMessage(error));
    historial.value = [];
  } finally {
    cargando.value = false;
  }
}

onMounted(cargar);
watch(() => route.query.mascota, cargar);

// ─── NAVEGACIÓN ───
function volverAlSelector() {
  router.push({ path: '/cliente/historial-clinico' });
}
function irAlHistorial(m) {
  const id = m?.idMascota;
  if (!id) return;
  router.push({ path: '/cliente/historial-clinico', query: { mascota: id } });
}
</script>

<template>
  <div class="historiales">
    <ToastContainer />

    <!-- ═══════ BREADCRUMB ═══════ -->
    <nav class="breadcrumb" aria-label="Migas de pan">
      <template v-if="!idMascota">
        <span class="bc-item bc-current">Historial Clínico</span>
      </template>
      <template v-else>
        <button class="bc-back" type="button" @click="volverAlSelector">
          <ArrowLeft :size="15" />
        </button>
        <button class="bc-item bc-link" type="button" @click="volverAlSelector">
          Historial Clínico
        </button>
        <ChevronRight :size="14" class="bc-sep" />
        <span class="bc-item bc-current">{{ infoMascota?.nombre || `Paciente #${idMascota}` }}</span>
      </template>
    </nav>

    <!-- ═══════ SELECTOR (sin ?mascota=) ═══════ -->
    <template v-if="!idMascota">
      <header class="pagina-header">
        <div>
          <h1>Historial Clínico</h1>
          <p>Expediente cronológico de atenciones de tus mascotas, de la más reciente a la más antigua.</p>
        </div>
      </header>

      <div class="card">
        <div class="card-header">
          <div>
            <h3>Selecciona una de tus mascotas</h3>
            <p class="card-header-sub">Verás su expediente clínico completo.</p>
          </div>
        </div>
        <div class="card-body">
          <div v-if="!misMascotas.length" class="empty-state">
            <PawPrint :size="40" />
            <p>No tienes mascotas registradas todavía.</p>
            <button class="btn-primary" type="button" @click="router.push('/cliente/mascotas')">
              <Plus :size="16" /> Registrar mascota
            </button>
          </div>
          <div v-else class="selector-mascotas">
            <button
              v-for="m in misMascotas"
              :key="m.idMascota"
              type="button"
              class="selector-card"
              :style="{ '--pet-color': colorAvatar(m.nombre) }"
              @click="irAlHistorial(m)"
            >
              <div class="pet-avatar" :style="{ backgroundColor: colorAvatar(m.nombre) }">
                {{ inicialNombre(m.nombre) }}
              </div>
              <div class="selector-info">
                <p class="selector-nombre">{{ m.nombre }}</p>
                <p class="selector-sub">
                  {{ [especieNombre(m.idEspecie), sexoLabel(m.sexo), edadMascota(m.fechaNacimiento)]
                    .filter(Boolean).join(' · ') || 'Sin datos' }}
                </p>
              </div>
              <span class="selector-cta" aria-hidden="true">
                <ChevronRight :size="16" />
              </span>
            </button>
          </div>
        </div>
      </div>
    </template>

    <!-- ═══════ VISTA DE PACIENTE ═══════ -->
    <template v-else>
      <!-- Hero del paciente -->
      <section class="paciente-hero">
        <div
          class="paciente-avatar"
          :style="{ backgroundColor: colorAvatar(infoMascota?.nombre) }"
        >
          {{ inicialNombre(infoMascota?.nombre) }}
        </div>

        <div class="paciente-datos">
          <h1 class="paciente-nombre">
            {{ infoMascota?.nombre || `Paciente #${idMascota}` }}
          </h1>

          <div v-if="metadatosMascota.length" class="paciente-badges">
            <span v-for="(meta, i) in metadatosMascota" :key="i" class="paciente-badge">
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
          <button class="btn-secondary" type="button" @click="router.push('/cliente/solicitar-cita')">
            <Calendar :size="16" /> Agendar cita
          </button>
        </div>
      </section>

      <!-- KPIs -->
      <section class="kpis">
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color: #0F766E; --kpi-bg: #F0FDFA;">
            <FileText :size="18" />
          </div>
          <div>
            <p class="kpi-value">{{ stats.total }}</p>
            <p class="kpi-label">Atenciones</p>
          </div>
        </article>
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color: #3B82F6; --kpi-bg: #EFF6FF;">
            <Calendar :size="18" />
          </div>
          <div>
            <p class="kpi-value">{{ stats.ultimaVisita }}</p>
            <p class="kpi-label">Última visita</p>
          </div>
        </article>
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color: #F59E0B; --kpi-bg: #FFFBEB;">
            <Activity :size="18" />
          </div>
          <div>
            <p class="kpi-value">{{ stats.edad }}</p>
            <p class="kpi-label">Edad</p>
          </div>
        </article>
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color: #8B5CF6; --kpi-bg: #F5F3FF;">
            <Weight :size="18" />
          </div>
          <div>
            <p class="kpi-value">{{ stats.peso }}</p>
            <p class="kpi-label">Peso actual</p>
          </div>
        </article>
      </section>

      <!-- Estado: cargando -->
      <div v-if="cargando" class="loading-state">
        <div class="spin"></div>
        <p>Cargando el historial clínico…</p>
      </div>

      <!-- Estado: sin atenciones -->
      <div v-else-if="!historial.length" class="card">
        <div class="card-body">
          <div class="empty-state">
            <Inbox :size="40" />
            <p>Aún no hay atenciones registradas para esta mascota.</p>
          </div>
        </div>
      </div>

      <!-- Timeline de atenciones -->
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
            <div class="timeline-dot"></div>

            <div class="atencion-card" :class="{ 'is-open': estaExpandida(atencion.idAtencion) }">
              <!-- Cabecera clickeable -->
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
                  <span class="pill-estado">
                    {{ String(atencion.estadoAtencion || '').replaceAll('_', ' ') }}
                  </span>
                  <span class="atencion-toggle" aria-hidden="true">
                    <ChevronDown :size="16" />
                  </span>
                </div>
              </button>

              <!-- Cuerpo expandible -->
              <Transition name="expand">
                <div v-show="estaExpandida(atencion.idAtencion)" class="atencion-body">
                  <!-- Signos vitales -->
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

                  <!-- Bloques clínicos -->
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

                  <!-- Insumos -->
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

                  <!-- Récipe -->
                  <div v-if="atencion.receta" class="bloque-interno bloque-receta">
                    <div class="receta-cabecera">
                      <h5><Pill :size="13" /> Récipe {{ atencion.receta.codigoReceta }}</h5>
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
  </div>
</template>

<style scoped>
.historiales {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 24px 64px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
}
button { font-family: inherit; }

/* ═══ BREADCRUMB ═══ */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 18px;
  font-size: 13px;
  color: #64748B;
  min-height: 24px;
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
.bc-current { color: #1E293B; font-weight: 600; }
.bc-sep { color: #CBD5E1; }

/* ═══ HEADERS ═══ */
.pagina-header { margin-bottom: 22px; }
.pagina-header h1 { margin: 0 0 4px; font-size: 22px; font-weight: 700; }
.pagina-header p { margin: 0; font-size: 14px; color: #64748B; }

/* ═══ CARDS ═══ */
.card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
}
.card-header { padding: 20px 24px; border-bottom: 1px solid #E2E8F0; }
.card-header h3 { margin: 0 0 2px; font-size: 16px; font-weight: 600; }
.card-header-sub { margin: 0; font-size: 13px; color: #64748B; }
.card-body { padding: 22px 24px; }

/* ═══ SELECTOR DE MASCOTAS ═══ */
.selector-mascotas {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 14px;
}
.selector-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  overflow: hidden;
  transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease;
}
.selector-card::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 3px;
  background: var(--pet-color, #0F766E);
  transform: scaleY(.35);
  opacity: 0;
  transition: opacity .25s ease, transform .25s ease;
}
.selector-card:hover {
  border-color: rgba(15, 118, 110, .35);
  transform: translateY(-2px);
  box-shadow: 0 10px 24px -8px rgba(15, 118, 110, .18),
              0 4px 8px -4px rgba(15, 23, 42, .06);
}
.selector-card:hover::before { opacity: 1; transform: scaleY(1); }
.selector-card:focus-visible {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .15);
}
.selector-info { flex: 1; min-width: 0; }
.selector-nombre {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.selector-sub {
  margin: 3px 0 0;
  font-size: 12.5px;
  color: #64748B;
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
  color: #94A3B8;
  background: #F1F5F9;
  flex-shrink: 0;
  transition: background-color .2s ease, color .2s ease, transform .2s ease;
}
.selector-card:hover .selector-cta {
  background: #0F766E;
  color: #fff;
  transform: translateX(2px);
}

/* ═══ AVATAR ═══ */
.pet-avatar {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 21px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 4px 10px -3px rgba(15, 23, 42, .12);
}

/* ═══ HERO DEL PACIENTE ═══ */
.paciente-hero {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 20px;
  align-items: center;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 60%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
  padding: 22px 24px;
  margin-bottom: 18px;
  box-shadow: 0 4px 6px -1px rgba(15, 118, 110, .04);
}
.paciente-avatar {
  width: 76px;
  height: 76px;
  border-radius: 16px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 8px 20px -6px rgba(15, 23, 42, .2);
}
.paciente-datos { min-width: 0; }
.paciente-nombre {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.2;
  letter-spacing: -0.01em;
}
.paciente-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}
.paciente-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 11px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid #CCFBF1;
  color: #0F766E;
  font-size: 11.5px;
  font-weight: 600;
  letter-spacing: .1px;
}
.paciente-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 12.5px;
  color: #64748B;
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.meta-item strong { font-weight: 600; color: #334155; }
.paciente-acciones {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* ═══ KPIs ═══ */
.kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 28px;
}
.kpi {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 16px 18px;
  transition: all .2s ease;
}
.kpi:hover {
  border-color: #CBD5E1;
  box-shadow: 0 4px 12px -4px rgba(15, 23, 42, .06);
}
.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--kpi-bg);
  color: var(--kpi-color);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.kpi-value {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.15;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.kpi-label {
  margin: 2px 0 0;
  font-size: 11.5px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
}

/* ═══ TIMELINE HEADER ═══ */
.timeline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.timeline-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
}
.timeline-count {
  font-size: 12px;
  font-weight: 600;
  color: #64748B;
  background: #F1F5F9;
  padding: 4px 12px;
  border-radius: 20px;
}

/* ═══ TIMELINE ═══ */
.timeline {
  position: relative;
  padding-left: 28px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.timeline::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 10px;
  bottom: 10px;
  width: 2px;
  background: linear-gradient(to bottom, #99F6E4 0%, #E2E8F0 60%);
  border-radius: 2px;
}
.timeline-item { position: relative; }
.timeline-dot {
  position: absolute;
  left: -28px;
  top: 20px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #0F766E;
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px #99F6E4;
  z-index: 1;
}

/* ═══ ATENCIÓN CARD (colapsable) ═══ */
.atencion-card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  overflow: hidden;
  transition: border-color .2s ease, box-shadow .2s ease;
}
.atencion-card:hover {
  border-color: #CBD5E1;
}
.atencion-card.is-open {
  border-color: #99F6E4;
  box-shadow: 0 8px 20px -10px rgba(15, 118, 110, .18);
}

.atencion-head {
  width: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  background: transparent;
  border: none;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
  transition: background-color .15s ease;
}
.atencion-head:hover { background: #FAFBFC; }
.atencion-head:focus-visible {
  outline: none;
  background: #F0FDFA;
}
.atencion-head-main { min-width: 0; flex: 1; }
.atencion-fecha {
  margin: 0;
  font-size: 14.5px;
  font-weight: 700;
  color: #0F766E;
  letter-spacing: -.01em;
}
.atencion-vet {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: #64748B;
  display: flex;
  align-items: center;
  gap: 6px;
}
.atencion-dx {
  margin: 8px 0 0;
  font-size: 13px;
  color: #334155;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.atencion-dx strong { color: #64748B; font-weight: 600; }

.atencion-head-side {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.atencion-toggle {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #F1F5F9;
  color: #64748B;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform .25s ease, background-color .2s ease, color .2s ease;
}
.atencion-card.is-open .atencion-toggle {
  background: #F0FDFA;
  color: #0F766E;
  transform: rotate(180deg);
}

.pill-estado {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 600;
  background: #F0FDFA;
  color: #0F766E;
  border: 1px solid #99F6E4;
  white-space: nowrap;
}

/* ═══ CUERPO DE LA ATENCIÓN ═══ */
.atencion-body {
  padding: 4px 18px 20px;
  border-top: 1px solid #F1F5F9;
}

/* ═══ VITALES ═══ */
.vitales-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin: 16px 0;
}
.vital {
  display: grid;
  grid-template-columns: auto 1fr;
  grid-template-rows: auto auto;
  align-items: center;
  column-gap: 8px;
  row-gap: 2px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  padding: 10px 12px;
}
.vital-icon {
  grid-row: 1 / span 2;
  color: #94A3B8;
}
.vital-label {
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
}
.vital-value {
  font-size: 14px;
  color: #1E293B;
  font-weight: 700;
  letter-spacing: -.01em;
}

/* ═══ BLOQUES DE TEXTO ═══ */
.detalle-textos {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 4px;
}
.detalle-bloque {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  padding: 12px 14px;
}
.detalle-bloque.dx {
  background: #F0FDFA;
  border-color: #99F6E4;
}
.detalle-bloque h5 {
  margin: 0 0 6px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #64748B;
}
.detalle-bloque.dx h5 { color: #0F766E; }
.detalle-bloque p {
  margin: 0;
  font-size: 13.5px;
  color: #334155;
  line-height: 1.55;
}

/* ═══ BLOQUES INTERNOS (insumos / récipe) ═══ */
.bloque-interno {
  margin-top: 14px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  padding: 14px 16px;
}
.bloque-interno h5 {
  margin: 0 0 10px;
  font-size: 11.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
  display: flex;
  align-items: center;
  gap: 6px;
}
.bloque-receta { background: #FFFBEB; border-color: #FDE68A; }
.bloque-receta h5 { color: #92400E; }
.receta-cabecera {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.receta-cabecera h5 { margin: 0; color: #92400E; font-size: 12.5px; }
.receta-indicaciones {
  margin: 0 0 10px;
  font-size: 13px;
  color: #334155;
  line-height: 1.55;
}

/* ═══ TABLAS ═══ */
.tabla-wrap { overflow-x: auto; border-radius: 8px; }
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-family: inherit;
  min-width: 420px;
}
.data-table th {
  text-align: left;
  padding: 8px 10px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
  border-bottom: 2px solid #E2E8F0;
  background: transparent;
}
.data-table td {
  padding: 8px 10px;
  font-size: 13px;
  color: #1E293B;
  border-bottom: 1px solid #F1F5F9;
  vertical-align: middle;
}
.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover td { background: rgba(15, 118, 110, .03); }
.amount { font-weight: 700; color: #0F766E; }
.der { text-align: right; }
.sku { color: #94A3B8; font-size: 12px; }
.total-linea {
  display: flex;
  justify-content: space-between;
  margin: 12px 0 0;
  padding-top: 10px;
  border-top: 1px dashed #CBD5E1;
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

/* ═══ BOTONES ═══ */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #0F766E;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all .2s;
  font-family: inherit;
  white-space: nowrap;
}
.btn-primary:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, .25);
}
.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 16px;
  background: #fff;
  color: #0F766E;
  border: 1px solid #99F6E4;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  transition: all .2s;
  font-family: inherit;
  white-space: nowrap;
}
.btn-secondary:hover {
  background: #F0FDFA;
  border-color: #0F766E;
}

/* ═══ LOADING / EMPTY ═══ */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 64px 24px;
  color: #64748B;
  font-size: 14px;
}
.spin {
  width: 32px;
  height: 32px;
  border: 3px solid #E2E8F0;
  border-top-color: #0F766E;
  border-radius: 50%;
  animation: girar .8s linear infinite;
}
@keyframes girar { to { transform: rotate(360deg); } }
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 24px;
  color: #94A3B8;
  text-align: center;
  font-size: 14px;
}
.empty-state p { margin: 0; }

/* ═══ TRANSICIÓN EXPAND ═══ */
.expand-enter-active,
.expand-leave-active {
  transition: opacity .2s ease, transform .2s ease;
}
.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
  .vitales-grid { grid-template-columns: repeat(2, 1fr); }
  .detalle-textos { grid-template-columns: 1fr; }
  .paciente-hero {
    grid-template-columns: auto 1fr;
    gap: 16px;
  }
  .paciente-acciones {
    grid-column: 1 / -1;
    padding-top: 4px;
  }
}

@media (max-width: 640px) {
  .historiales { padding: 16px 16px 48px; }
  .paciente-hero {
    padding: 18px;
    border-radius: 14px;
  }
  .paciente-avatar {
    width: 60px;
    height: 60px;
    font-size: 26px;
    border-radius: 14px;
  }
  .paciente-nombre { font-size: 20px; }
  .timeline { padding-left: 20px; }
  .timeline-dot { left: -20px; width: 12px; height: 12px; }
  .timeline::before { left: 5px; }
  .atencion-head { padding: 14px; }
  .atencion-head-side {
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
  }
  .atencion-body { padding: 4px 14px 16px; }
  .vitales-grid { gap: 8px; }
  .selector-mascotas { grid-template-columns: 1fr; }
  .kpis { grid-template-columns: 1fr; }
}
</style>