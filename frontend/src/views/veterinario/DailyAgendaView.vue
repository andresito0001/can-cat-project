<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Inbox, Phone, User } from 'lucide-vue-next';
import { useToast } from '@/composables/useToast';
import ToastContainer from '@/components/ui/ToastContainer.vue';
import { getAgendaVet, getCitaContexto } from '@/api/atenciones.api';
import { getApiErrorMessage } from '@/utils/apiError';
import { ESTADO_COLOR } from '@/utils/constants/estadosCita';
import { fechaCompleta, hoyISO, partesBadgeFecha, rangoHora } from '@/utils/fecha';

const router = useRouter();
const { toastError } = useToast();

const fechaSeleccionada = ref(hoyISO());
const citas = ref([]);
const cargando = ref(false);
const resolviendoHistorial = ref(null); // idCita mientras se resuelve el idMascota

const resumen = computed(() => ({
  total: citas.value.length,
  porAtender: citas.value.filter((c) => !c.atendida).length,
  atendidas: citas.value.filter((c) => c.atendida).length,
}));

function infoEstado(estado) {
  return { color: ESTADO_COLOR[estado] || '#64748B', etiqueta: String(estado || '').replaceAll('_', ' ') };
}

function estiloEstado(estado) {
  const { color } = infoEstado(estado);
  return { color, borderColor: color, backgroundColor: `${color}1A` };
}

async function cargarAgenda() {
  cargando.value = true;
  try {
    citas.value = await getAgendaVet(fechaSeleccionada.value);
  } catch (error) {
    toastError(getApiErrorMessage(error));
    citas.value = [];
  } finally {
    cargando.value = false;
  }
}

function irAHoy() {
  fechaSeleccionada.value = hoyISO();
}

// "Historial": la agenda no expone idMascota → se resuelve vía el contexto
// de la cita y se navega al expediente con el nombre para la cabecera.
async function verHistorial(cita) {
  try {
    resolviendoHistorial.value = cita.idCita;
    const ctx = await getCitaContexto(cita.idCita);
    router.push({ path: '/veterinario/historiales', query: { mascota: ctx.mascota.idMascota, nombre: ctx.mascota.nombre } });
  } catch (error) {
    toastError(getApiErrorMessage(error));
  } finally {
    resolviendoHistorial.value = null;
  }
}

watch(fechaSeleccionada, cargarAgenda);
onMounted(cargarAgenda);
</script>

<template>
  <div class="agenda">
    <ToastContainer />

    <header class="pagina-header">
      <div>
        <h1>Mi Agenda Hoy</h1>
        <p>{{ fechaCompleta(fechaSeleccionada) }}</p>
      </div>
    </header>

    <div class="controles-agenda">
      <input v-model="fechaSeleccionada" type="date" class="form-input fecha-input" />
      <button class="btn-outline" type="button" @click="irAHoy">Hoy</button>
    </div>

    <div v-if="cargando" class="loading-state">
      <div class="spin"></div>
      <p>Cargando la agenda…</p>
    </div>

    <div v-else class="card">
      <div class="card-header">
        <h3>Citas del día</h3>
        <div class="resumen-fila">
          <span class="chip"><strong>{{ resumen.total }}</strong> citas</span>
          <span class="chip"><strong>{{ resumen.porAtender }}</strong> por atender</span>
          <span class="chip"><strong>{{ resumen.atendidas }}</strong> atendidas</span>
        </div>
      </div>
      <div class="card-body">
        <div v-if="!citas.length" class="empty-state">
          <Inbox :size="40" />
          <p>No hay citas para la fecha seleccionada.</p>
        </div>

        <div v-else class="appointment-list">
          <article v-for="cita in citas" :key="cita.idCita" class="appointment-item">
            <div class="date-badge">
              <span class="day">{{ partesBadgeFecha(cita.fechaCita).dia }}</span>
              <span class="month">{{ partesBadgeFecha(cita.fechaCita).mes }}</span>
            </div>

            <div class="appointment-info">
              <p class="appointment-title">{{ cita.mascotaNombre }}</p>
              <p class="appointment-sub">{{ cita.especie }} · {{ cita.raza }}</p>
              <p class="appointment-sub">{{ cita.motivoConsulta }}</p>
              <p class="appointment-sub"><strong>Servicio:</strong> {{ cita.servicioNombre }}</p>
            </div>

            <div class="appointment-dueno">
              <p class="dueno-nombre">{{ cita.clienteNombre }}</p>
              <p class="dueno-meta"><User :size="12" /> {{ cita.clienteDocumento }}</p>
              <p class="dueno-meta"><Phone :size="12" /> {{ cita.clienteTelefono }}</p>
            </div>

            <div class="appointment-side">
              <span class="appointment-time">{{ rangoHora(cita.horaInicio, cita.horaFin) }}</span>
              <div class="pills">
                <span class="status-pill" :style="estiloEstado(cita.estado)">{{ infoEstado(cita.estado).etiqueta }}</span>
                <span v-if="cita.atendida" class="pill-ok">Atendida</span>
              </div>
              <div class="appointment-acciones">
                <button v-if="!cita.atendida" class="btn-pay" type="button" @click="router.push(`/veterinario/atencion/${cita.idCita}`)">
                  Atender
                </button>
                <button class="btn-link" type="button" :disabled="resolviendoHistorial === cita.idCita" @click="verHistorial(cita)">
                  {{ resolviendoHistorial === cita.idCita ? 'Buscando…' : 'Historial' }}
                </button>
              </div>
            </div>
          </article>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.agenda {
  max-width: 1400px; margin: 0 auto; padding: 28px 24px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #1E293B;
}
.pagina-header { margin-bottom: 20px; }
.pagina-header h1 { margin: 0 0 4px; font-size: 22px; font-weight: 700; color: #1E293B; }
.pagina-header p { margin: 0; font-size: 14px; color: #64748B; }

.controles-agenda { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }
.fecha-input { width: auto; }
.form-input {
  background: #F8FAFC; border: 1.5px solid #E2E8F0; border-radius: 8px;
  padding: 9px 12px; font-size: 14px; color: #1E293B; font-family: inherit; transition: all .2s ease;
}
.form-input:focus { outline: none; border-color: #0F766E; background: #fff; box-shadow: 0 0 0 3px rgba(15,118,110,.1); }

.card { background: #fff; border: 1px solid #E2E8F0; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,.03), 0 10px 15px -3px rgba(0,0,0,.05); }
.card-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 20px 24px; border-bottom: 1px solid #E2E8F0; flex-wrap: wrap; }
.card-header h3 { margin: 0; font-size: 16px; font-weight: 600; color: #1E293B; }
.card-body { padding: 24px; }

.resumen-fila { display: flex; gap: 10px; flex-wrap: wrap; }
.chip { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 20px; padding: 5px 12px; font-size: 12px; font-weight: 600; color: #64748B; }
.chip strong { color: #1E293B; }

.appointment-list { display: flex; flex-direction: column; gap: 12px; }
.appointment-item {
  display: flex; align-items: center; gap: 18px; background: #F8FAFC;
  border: 1px solid #E2E8F0; border-radius: 10px; padding: 14px 16px;
  transition: all .2s ease; flex-wrap: wrap;
}
.appointment-item:hover { border-color: #CBD5E1; }
.date-badge {
  width: 52px; height: 52px; background: #fff; border: 1px solid #E2E8F0;
  border-radius: 10px; display: flex; flex-direction: column;
  align-items: center; justify-content: center; flex-shrink: 0;
}
.date-badge .day { font-size: 20px; font-weight: 700; color: #0F766E; line-height: 1; }
.date-badge .month { font-size: 10px; font-weight: 700; text-transform: uppercase; color: #64748B; letter-spacing: .5px; margin-top: 2px; }
.appointment-info { flex: 1; min-width: 210px; }
.appointment-title { margin: 0; font-size: 15px; font-weight: 700; color: #1E293B; }
.appointment-sub { margin: 3px 0 0; font-size: 12.5px; color: #64748B; }
.appointment-dueno { min-width: 180px; }
.dueno-nombre { margin: 0; font-size: 13.5px; font-weight: 700; color: #334155; }
.dueno-meta { margin: 3px 0 0; font-size: 12.5px; color: #64748B; display: flex; align-items: center; gap: 6px; }
.appointment-side { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; margin-left: auto; }
.appointment-time { font-size: 13px; font-weight: 700; color: #0F766E; white-space: nowrap; }
.pills { display: flex; gap: 8px; flex-wrap: wrap; justify-content: flex-end; }
.status-pill { display: inline-flex; align-items: center; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; border: 1px solid; white-space: nowrap; }
.pill-ok { display: inline-flex; align-items: center; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; background: #ECFDF5; color: #059669; border: 1px solid #A7F3D0; white-space: nowrap; }
.appointment-acciones { display: flex; align-items: center; gap: 14px; }

button { font-family: inherit; }
.btn-pay {
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  background: #0F766E; color: #fff; border: none; border-radius: 8px;
  padding: 8px 16px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all .2s ease;
}
.btn-pay:hover { background: #115E59; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(15,118,110,.25); }
.btn-outline {
  display: inline-flex; align-items: center; gap: 8px; background: #fff; color: #334155;
  border: 1px solid #E2E8F0; border-radius: 8px; padding: 8px 14px;
  font-size: 13px; font-weight: 600; cursor: pointer; transition: all .2s ease;
}
.btn-outline:hover { border-color: #0F766E; color: #0F766E; }
.btn-link { background: none; border: none; padding: 0; color: #0F766E; font-size: 13px; font-weight: 600; cursor: pointer; font-family: inherit; }
.btn-link:hover:not(:disabled) { text-decoration: underline; }
.btn-link:disabled { color: #94A3B8; cursor: default; }

.loading-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 64px 24px; color: #64748B; font-size: 14px; }
.spin { width: 32px; height: 32px; border: 3px solid #E2E8F0; border-top-color: #0F766E; border-radius: 50%; animation: girar .8s linear infinite; }
@keyframes girar { to { transform: rotate(360deg); } }
.empty-state { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 48px 24px; color: #94A3B8; text-align: center; font-size: 14px; }
.empty-state p { margin: 0; }

@media (max-width: 1024px) {
  .appointment-item { flex-wrap: wrap; }
  .appointment-side { align-items: flex-start; width: 100%; flex-direction: row; justify-content: space-between; }
}
@media (max-width: 640px) {
  .appointment-dueno { min-width: 100%; }
}
</style>