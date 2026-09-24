<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  ArrowRight, CalendarCheck, CalendarDays, CheckCircle2, ChevronRight,
  Clock, Clock3, FileText, Inbox, Search, Stethoscope, User
} from 'lucide-vue-next';
import { useAuthStore } from '@/stores/auth.store';
import { useToast } from '@/composables/useToast';
import ToastContainer from '@/components/ui/ToastContainer.vue';
import { getAgendaVet } from '@/api/atenciones.api';
import { getApiErrorMessage } from '@/utils/apiError';
import { ESTADO_COLOR } from '@/utils/constants/estadosCita';
import { fechaCompleta, hoyISO, rangoHora } from '@/utils/fecha';

const router = useRouter();
const authStore = useAuthStore();
const { toastError } = useToast();

const hoy = hoyISO();
const cargando = ref(true);
const agendaHoy = ref([]);
const atencionesMes = ref(null);

const nombreVet = computed(() => authStore.user?.nombreCompleto || 'Doctor(a)');

const stats = computed(() => [
  {
    etiqueta: 'Citas de hoy',
    valor: agendaHoy.value.length,
    icono: CalendarDays,
    color: '#0F766E',
    bg: '#F0FDFA',
  },
  {
    etiqueta: 'Por atender',
    valor: agendaHoy.value.filter((c) => !c.atendida).length,
    icono: Clock3,
    color: '#3B82F6',
    bg: '#EFF6FF',
  },
  {
    etiqueta: 'Atendidas hoy',
    valor: agendaHoy.value.filter((c) => c.atendida).length,
    icono: CheckCircle2,
    color: '#F59E0B',
    bg: '#FFFBEB',
  },
  {
    etiqueta: 'Atenciones este mes',
    valor: atencionesMes.value ?? '…',
    icono: Stethoscope,
    color: '#8B5CF6',
    bg: '#F5F3FF',
  },
]);

// Próximo paciente pendiente (destacado arriba, fuera de la lista)
const proximaCita = computed(() => agendaHoy.value.find((c) => !c.atendida) || null);

// Resto de citas (excluye la del próximo paciente para no duplicar)
const citasAgenda = computed(() =>
  proximaCita.value
    ? agendaHoy.value.filter((c) => c.idCita !== proximaCita.value.idCita)
    : agendaHoy.value
);
const citasVisibles = computed(() => citasAgenda.value.slice(0, 6));
const citasOcultas = computed(() => Math.max(0, citasAgenda.value.length - 6));

const accesos = [
  {
    titulo: 'Mi Agenda Hoy',
    descripcion: 'Citas confirmadas del día',
    icono: CalendarCheck,
    color: '#0F766E',
    bg: '#F0FDFA',
    ruta: '/veterinario/agenda',
  },
  {
    titulo: 'Atención Clínica',
    descripcion: 'Iniciar una consulta médica',
    icono: Stethoscope,
    color: '#3B82F6',
    bg: '#EFF6FF',
    ruta: '/veterinario/atencion',
  },
  {
    titulo: 'Historiales',
    descripcion: 'Expedientes clínicos',
    icono: FileText,
    color: '#8B5CF6',
    bg: '#F5F3FF',
    ruta: '/veterinario/historiales',
  },
];

// ─── Helpers de estado (FIX: usar estadoNombre / estadoColor del DTO) ───
function etiquetaEstado(estadoNombre) {
  return String(estadoNombre || '').replaceAll('_', ' ');
}
function estiloEstado(estadoNombre, estadoColor) {
  const color = estadoColor || ESTADO_COLOR[estadoNombre] || '#64748B';
  return { color, borderColor: color, backgroundColor: `${color}1A` };
}

// ─── Formato de hora HH:MM ───
function horaCorta(h) {
  return h ? String(h).slice(0, 5) : '';
}

// ─── Cuenta regresiva ───
function minutosHasta(horaInicio) {
  if (!horaInicio) return null;
  const [h, m] = String(horaInicio).split(':').map(Number);
  const ahora = new Date();
  return h * 60 + m - (ahora.getHours() * 60 + ahora.getMinutes());
}
function textoCuentaRegresiva(horaInicio) {
  const diff = minutosHasta(horaInicio);
  if (diff === null) return '';
  if (diff < -5) return 'En curso';
  if (diff <= 5) return 'Ahora mismo';
  if (diff < 60) return `En ${diff} min`;
  const h = Math.floor(diff / 60);
  const m = diff % 60;
  return m === 0 ? `En ${h}h` : `En ${h}h ${m}m`;
}

// ─── Carga ───
async function cargarAgenda() {
  cargando.value = true;
  try {
    agendaHoy.value = await getAgendaVet(hoy);
  } catch (error) {
    toastError(getApiErrorMessage(error));
    agendaHoy.value = [];
  } finally {
    cargando.value = false;
  }
}

// Recorre los días transcurridos del mes en curso (en paralelo) contando
// citas con atendida=true. Fallidas se ignoran (Promise.allSettled).
async function calcularAtencionesMes() {
  const ahora = new Date();
  const anio = ahora.getFullYear();
  const mes = String(ahora.getMonth() + 1).padStart(2, '0');
  const diaActual = ahora.getDate();
  const fechas = [];
  for (let d = 1; d <= diaActual; d += 1) {
    fechas.push(`${anio}-${mes}-${String(d).padStart(2, '0')}`);
  }
  const resultados = await Promise.allSettled(fechas.map((f) => getAgendaVet(f)));
  atencionesMes.value = resultados.reduce(
    (total, r) =>
      r.status === 'fulfilled' ? total + r.value.filter((c) => c.atendida).length : total,
    0,
  );
}

function irAtencion(idCita) {
  if (!idCita) return;
  router.push(`/veterinario/atencion/${idCita}`);
}

onMounted(async () => {
  await cargarAgenda();
  calcularAtencionesMes();
});
</script>

<template>
  <div class="dashboard">
    <ToastContainer />

    <!-- ═══ HERO ═══ -->
    <header class="hero">
      <div class="hero-left">
        <p class="hero-date">{{ fechaCompleta(hoy) }}</p>
        <h1>Hola, {{ nombreVet }}</h1>
        <p class="hero-sub">Este es el resumen de tu jornada en la clínica.</p>
      </div>
      <div class="hero-right">
        <span class="hero-badge">
          <CalendarDays :size="14" />
          {{ agendaHoy.length }} {{ agendaHoy.length === 1 ? 'cita' : 'citas' }} hoy
        </span>
      </div>
    </header>

    <!-- ═══ LOADING ═══ -->
    <div v-if="cargando" class="loading-state">
      <div class="spin"></div>
      <p>Cargando tu agenda de hoy…</p>
    </div>

    <template v-else>
      <!-- ═══ KPIs ═══ -->
      <section class="stats-grid">
        <article v-for="s in stats" :key="s.etiqueta" class="stat-card">
          <div
            class="stat-icon"
            :style="{ backgroundColor: s.bg, color: s.color }"
          >
            <component :is="s.icono" :size="22" />
          </div>
          <div class="stat-texto">
            <p class="stat-value">{{ s.valor }}</p>
            <p class="stat-label">{{ s.etiqueta }}</p>
          </div>
        </article>
      </section>

      <!-- ═══ PRÓXIMO PACIENTE (destacado) ═══ -->
      <section v-if="proximaCita" class="proximo">
        <div class="proximo-info">
          <span class="proximo-tag">
            <Clock :size="12" />
            {{ textoCuentaRegresiva(proximaCita.horaInicio) }}
          </span>
          <h2 class="proximo-nombre">{{ proximaCita.mascotaNombre }}</h2>
          <p class="proximo-sub">
            {{ proximaCita.especie }}
            <template v-if="proximaCita.raza"> · {{ proximaCita.raza }}</template>
          </p>
          <div class="proximo-meta">
            <span><User :size="13" /> {{ proximaCita.clienteNombre }}</span>
            <span><Clock :size="13" /> {{ rangoHora(proximaCita.horaInicio, proximaCita.horaFin) }}</span>
          </div>
        </div>
        <div class="proximo-accion">
          <button class="btn-atender-grande" type="button" @click="irAtencion(proximaCita.idCita)">
            Iniciar atención
            <ArrowRight :size="16" />
          </button>
        </div>
      </section>

      <!-- ═══ GRID PRINCIPAL ═══ -->
      <section class="content-grid">
        <!-- Agenda -->
        <div class="card">
          <div class="card-header">
            <div>
              <h3>Agenda de hoy</h3>
              <p class="card-sub">
                {{ agendaHoy.length ? `${agendaHoy.length} cita${agendaHoy.length === 1 ? '' : 's'} programada${agendaHoy.length === 1 ? '' : 's'}` : 'Sin citas programadas' }}
              </p>
            </div>
            <button class="btn-link" type="button" @click="router.push('/veterinario/agenda')">
              Ver agenda completa
            </button>
          </div>
          <div class="card-body">
            <!-- Sin citas -->
            <div v-if="!agendaHoy.length" class="empty-state">
              <Inbox :size="40" />
              <p>No tienes citas programadas para hoy.</p>
              <button class="btn-link" type="button" @click="router.push('/veterinario/agenda')">
                Ir a mi agenda
              </button>
            </div>

            <!-- Solo estaba la del próximo -->
            <div v-else-if="!citasVisibles.length" class="empty-state slim">
              <CheckCircle2 :size="32" />
              <p>No hay más citas pendientes hoy.</p>
            </div>

            <!-- Lista -->
            <div v-else class="citas-lista">
              <article
                v-for="cita in citasVisibles"
                :key="cita.idCita"
                class="cita-item"
                :class="{ 'is-atendida': cita.atendida }"
              >
                <div class="cita-hora">
                  <span class="hora-inicio">{{ horaCorta(cita.horaInicio) }}</span>
                  <span class="hora-fin">{{ horaCorta(cita.horaFin) }}</span>
                </div>

                <div class="cita-info">
                  <p class="cita-mascota">{{ cita.mascotaNombre }}</p>
                  <p class="cita-detalle">
                    {{ cita.especie }}
                    <template v-if="cita.raza"> · {{ cita.raza }}</template>
                  </p>
                  <p class="cita-dueno"><User :size="12" /> {{ cita.clienteNombre }}</p>
                </div>

                <div class="cita-estado">
                  <span
                    class="status-pill"
                    :style="estiloEstado(cita.estadoNombre, cita.estadoColor)"
                  >
                    {{ etiquetaEstado(cita.estadoNombre) }}
                  </span>
                </div>

                <div class="cita-accion">
                  <button
                    v-if="!cita.atendida"
                    class="btn-atender-sm"
                    type="button"
                    @click="irAtencion(cita.idCita)"
                  >
                    Atender
                  </button>
                  <span v-else class="cita-ok" title="Atendida">
                    <CheckCircle2 :size="18" />
                  </span>
                </div>
              </article>

              <p v-if="citasOcultas > 0" class="more-link">
                + {{ citasOcultas }} cita{{ citasOcultas === 1 ? '' : 's' }} más en tu agenda completa
              </p>
            </div>
          </div>
        </div>

        <!-- Accesos rápidos -->
        <div class="card">
          <div class="card-header">
            <h3>Accesos rápidos</h3>
          </div>
          <div class="card-body card-body-slim">
            <div class="quick-list">
              <button
                v-for="a in accesos"
                :key="a.titulo"
                class="quick-card"
                type="button"
                @click="router.push(a.ruta)"
              >
                <div
                  class="quick-icon"
                  :style="{ backgroundColor: a.bg, color: a.color }"
                >
                  <component :is="a.icono" :size="20" />
                </div>
                <div class="quick-texto">
                  <p class="quick-titulo">{{ a.titulo }}</p>
                  <p class="quick-desc">{{ a.descripcion }}</p>
                </div>
                <ChevronRight :size="16" class="quick-arrow" />
              </button>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
  padding: 28px 24px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
}
button { font-family: inherit; }

/* ═══ HERO ═══ */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  padding: 24px 28px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
}
.hero-date {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #0F766E;
}
.hero-left h1 {
  margin: 0 0 4px;
  font-size: 26px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.02em;
  line-height: 1.2;
}
.hero-sub {
  margin: 0;
  font-size: 14px;
  color: #64748B;
}
.hero-right { display: flex; align-items: center; }
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #CCFBF1;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #0F766E;
  white-space: nowrap;
}

/* ═══ KPIs ═══ */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 18px;
  transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease;
}
.stat-card:hover {
  border-color: #CBD5E1;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -10px rgba(15, 23, 42, .08);
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-texto { min-width: 0; }
.stat-value {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.1;
  letter-spacing: -0.01em;
}
.stat-label {
  margin: 3px 0 0;
  font-size: 12.5px;
  color: #64748B;
  font-weight: 500;
}

/* ═══ PRÓXIMO PACIENTE ═══ */
.proximo {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 24px;
  align-items: center;
  padding: 24px 28px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #0F766E 0%, #0D9488 100%);
  color: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 32px -12px rgba(15, 118, 110, .5);
  position: relative;
  overflow: hidden;
}
.proximo::after {
  content: '';
  position: absolute;
  right: -60px;
  top: -60px;
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(255, 255, 255, .12) 0%, transparent 70%);
  pointer-events: none;
}
.proximo-info { min-width: 0; position: relative; z-index: 1; }
.proximo-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: rgba(255, 255, 255, .2);
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  margin-bottom: 10px;
  backdrop-filter: blur(4px);
}
.proximo-nombre {
  margin: 0 0 4px;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.01em;
  line-height: 1.2;
}
.proximo-sub {
  margin: 0 0 12px;
  font-size: 13.5px;
  color: rgba(255, 255, 255, .85);
}
.proximo-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  font-size: 13px;
  color: rgba(255, 255, 255, .9);
}
.proximo-meta span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.proximo-accion { position: relative; z-index: 1; }
.btn-atender-grande {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  background: #fff;
  color: #0F766E;
  border: none;
  border-radius: 12px;
  font-size: 14.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  white-space: nowrap;
  font-family: inherit;
}
.btn-atender-grande:hover {
  background: #F0FDFA;
  transform: translateY(-2px);
  box-shadow: 0 12px 24px -8px rgba(0, 0, 0, .25);
}

/* ═══ GRID CONTENIDO ═══ */
.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr);
  gap: 20px;
  align-items: start;
}

/* ═══ CARDS ═══ */
.card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
}
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
}
.card-header h3 { margin: 0; font-size: 16px; font-weight: 700; color: #0F172A; }
.card-sub { margin: 3px 0 0; font-size: 12.5px; color: #64748B; }
.card-body { padding: 20px 24px 24px; }
.card-body-slim { padding: 14px 16px; }

/* ═══ LISTA DE CITAS ═══ */
.citas-lista { display: flex; flex-direction: column; gap: 10px; }
.cita-item {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  transition: border-color .2s ease, background-color .2s ease;
}
.cita-item:hover { border-color: #CBD5E1; background: #fff; }
.cita-item.is-atendida { opacity: .72; }

.cita-hora {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 58px;
  padding: 8px 6px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  flex-shrink: 0;
}
.hora-inicio {
  font-size: 15px;
  font-weight: 700;
  color: #0F766E;
  line-height: 1;
  letter-spacing: -0.01em;
}
.hora-fin {
  margin-top: 3px;
  font-size: 10.5px;
  font-weight: 600;
  color: #94A3B8;
  line-height: 1;
}
.cita-item.is-atendida .hora-inicio { color: #64748B; }

.cita-info { min-width: 0; }
.cita-mascota {
  margin: 0;
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cita-detalle {
  margin: 3px 0 0;
  font-size: 12.5px;
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cita-dueno {
  margin: 4px 0 0;
  font-size: 12px;
  color: #94A3B8;
  display: flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cita-estado { flex-shrink: 0; }
.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
  border: 1px solid;
  white-space: nowrap;
  letter-spacing: .1px;
}

.cita-accion { flex-shrink: 0; }
.btn-atender-sm {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  background: #0F766E;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
}
.btn-atender-sm:hover {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, .25);
}
.cita-ok {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #ECFDF5;
  color: #059669;
}

.more-link {
  margin: 14px 0 0;
  font-size: 12.5px;
  color: #94A3B8;
  text-align: center;
}

/* ═══ ACCESOS RÁPIDOS ═══ */
.quick-list { display: flex; flex-direction: column; gap: 8px; }
.quick-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: all .2s ease;
}
.quick-card:hover {
  background: #fff;
  border-color: #0F766E;
  transform: translateX(2px);
  box-shadow: 0 4px 12px -4px rgba(15, 118, 110, .15);
}
.quick-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.quick-texto { flex: 1; min-width: 0; }
.quick-titulo {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.quick-desc {
  margin: 2px 0 0;
  font-size: 11.5px;
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.quick-arrow { color: #CBD5E1; flex-shrink: 0; transition: color .2s ease; }
.quick-card:hover .quick-arrow { color: #0F766E; }

/* ═══ BOTONES LINK ═══ */
.btn-link {
  background: none;
  border: none;
  padding: 0;
  color: #0F766E;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
}
.btn-link:hover { text-decoration: underline; }

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
  padding: 40px 24px;
  color: #94A3B8;
  text-align: center;
  font-size: 14px;
}
.empty-state.slim { padding: 24px; }
.empty-state p { margin: 0; }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1100px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1024px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .proximo {
    grid-template-columns: 1fr;
    gap: 18px;
  }
  .proximo-accion { width: 100%; }
  .btn-atender-grande { width: 100%; justify-content: center; }
}

@media (max-width: 640px) {
  .dashboard { padding: 16px 16px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero-left h1 { font-size: 22px; }
  .stats-grid { grid-template-columns: 1fr; }
  .proximo { padding: 20px; border-radius: 14px; }
  .proximo-nombre { font-size: 20px; }
  .cita-item {
    grid-template-columns: auto 1fr;
    gap: 12px;
  }
  .cita-estado,
  .cita-accion {
    grid-column: 2;
  }
  .cita-accion { justify-self: start; }
  .cita-estado { margin-top: 6px; }
}
</style>