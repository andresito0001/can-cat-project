<template>
  <div class="dashboard">
    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon bg-blue">
          <CalendarCheck :size="24" />
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.citasPendientes }}</span>
          <span class="stat-label">Citas Pendientes</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-teal">
          <PawPrint :size="24" />
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.mascotas }}</span>
          <span class="stat-label">Mis Mascotas</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-amber">
          <CreditCard :size="24" />
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.pagosPendientes }}</span>
          <span class="stat-label">Pagos Pendientes</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-rose">
          <Stethoscope :size="24" />
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.consultasEsteMes }}</span>
          <span class="stat-label">Consultas este mes</span>
        </div>
      </div>
    </div>

    <!-- Content Grid -->
    <div class="content-grid">
      <!-- Próximas Citas -->
      <div class="card">
        <div class="card-header">
          <h3>Próximas Citas</h3>
          <router-link to="/cliente/solicitar-cita" class="btn-link">
            + Nueva Cita
          </router-link>
        </div>
        <div class="card-body">
          <div v-if="proximasCitas.length === 0" class="empty-state">
            <CalendarX :size="40" />
            <p>No tienes citas programadas</p>
          </div>
          <div v-else class="appointment-list">
            <div v-for="cita in proximasCitas" :key="cita.id" class="appointment-item">
              <div class="appointment-date">
                <span class="day">{{ cita.dia }}</span>
                <span class="month">{{ cita.mes }}</span>
              </div>
              <div class="appointment-details">
                <h4>{{ cita.motivo }}</h4>
                <p class="pet-name">
                  <PawPrint :size="14" /> {{ cita.mascota }}
                </p>
                <span class="time">
                  <Clock :size="14" /> {{ cita.hora }}
                </span>
              </div>
              <span class="status" :class="cita.estado">{{ cita.estadoTexto }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Mis Mascotas -->
      <div class="card">
        <div class="card-header">
          <h3>Mis Mascotas</h3>
          <router-link to="/cliente/mascotas" class="btn-link">Ver todas</router-link>
        </div>
        <div class="card-body">
          <div class="pet-grid">
            <div v-for="mascota in misMascotas" :key="mascota.id" class="pet-card">
              <div class="pet-avatar" :style="{ backgroundColor: mascota.color }">
                {{ mascota.nombre[0] }}
              </div>
              <div class="pet-info">
                <h4>{{ mascota.nombre }}</h4>
                <p>{{ mascota.especie }} • {{ mascota.edad }}</p>
              </div>
              <button class="pet-action" @click="verHistorial(mascota.id)">
                <FileText :size="16" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagos Pendientes -->
      <div class="card wide">
        <div class="card-header">
          <h3>Pagos Pendientes</h3>
          <span v-if="pagosPendientes.length > 0" class="badge-alert">
            {{ pagosPendientes.length }} pendiente(s)
          </span>
        </div>
        <div class="card-body">
          <div v-if="pagosPendientes.length === 0" class="empty-state">
            <CheckCircle :size="40" />
            <p>No tienes pagos pendientes</p>
          </div>
          <table v-else class="data-table">
            <thead>
              <tr>
                <th>Servicio</th>
                <th>Mascota</th>
                <th>Fecha</th>
                <th>Monto</th>
                <th>Acción</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="pago in pagosPendientes" :key="pago.id">
                <td>{{ pago.servicio }}</td>
                <td>{{ pago.mascota }}</td>
                <td>{{ pago.fecha }}</td>
                <td class="amount">{{ pago.monto }}</td>
                <td>
                  <button class="btn-pay" @click="pagar(pago.id)">Pagar</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  CalendarCheck, PawPrint, CreditCard, Stethoscope,
  CalendarX, Clock, FileText, CheckCircle
} from 'lucide-vue-next'

const router = useRouter()

// ─── DATOS MOCK ───
const stats = ref({
  citasPendientes: 2,
  mascotas: 3,
  pagosPendientes: 1,
  consultasEsteMes: 4
})

const proximasCitas = ref([
  {
    id: 1,
    dia: '25',
    mes: 'AGO',
    motivo: 'Vacunación Anual',
    mascota: 'Rocky',
    hora: '09:30 AM',
    estado: 'confirmed',
    estadoTexto: 'Confirmada'
  },
  {
    id: 2,
    dia: '28',
    mes: 'AGO',
    motivo: 'Control de Pulgas',
    mascota: 'Luna',
    hora: '02:00 PM',
    estado: 'pending',
    estadoTexto: 'Pendiente de pago'
  }
])

const misMascotas = ref([
  { id: 1, nombre: 'Rocky', especie: 'Perro', edad: '3 años', color: '#0F766E' },
  { id: 2, nombre: 'Luna', especie: 'Gato', edad: '2 años', color: '#F59E0B' },
  { id: 3, nombre: 'Max', especie: 'Perro', edad: '5 años', color: '#EF4444' }
])

const pagosPendientes = ref([
  {
    id: 101,
    servicio: 'Consulta General + Desparasitación',
    mascota: 'Luna',
    fecha: '28/08/2026',
    monto: '$45.00'
  }
])

function verHistorial(mascotaId) {
  router.push(`/cliente/historial-clinico?mascota=${mascotaId}`)
}

function pagar(pagoId) {
  router.push(`/cliente/solicitar-cita?pago=${pagoId}`)
  // o navegar a pasarela de pago cuando exista
}
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  /* Aquí está la solución al problema de la tipografía */
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

/* Stats Grid */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px; 
  margin-bottom: 24px;
}

.stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  border: 1px solid #E2E8F0;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 20px 25px -5px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.bg-teal { background: #0F766E; }
.bg-blue { background: #3B82F6; }
.bg-amber { background: #F59E0B; }
.bg-rose { background: #F43F5E; }

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  display: block;
  line-height: 1;
}

.stat-label {
  font-size: 13px;
  color: #64748B;
  margin-top: 4px;
  display: block;
}

/* Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.card.wide {
  grid-column: 1 / -1;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
}

.btn-link {
  color: #0F766E;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s;
}

.btn-link:hover {
  color: #115E59;
  text-decoration: underline;
}

.card-body {
  padding: 24px; 
}

/* Appointments */
.appointment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.appointment-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #F8FAFC;
  border-radius: 10px;
  border: 1px solid #E2E8F0;
  transition: border-color 0.2s;
}

.appointment-item:hover {
  border-color: #CBD5E1; 
}

.appointment-date {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 52px; 
  height: 52px;
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #E2E8F0;
}

.appointment-date .day {
  font-size: 20px;
  font-weight: 700;
  color: #0F766E;
  line-height: 1;
}

.appointment-date .month {
  font-size: 10px; 
  font-weight: 700;
  color: #64748B;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.appointment-details {
  flex: 1;
}

.appointment-details h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 4px 0; 
}

.pet-name, .time {
  display: inline-flex; 
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748B;
  margin-right: 12px;
}

.status {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap; 
}

.status.confirmed {
  background: #ECFDF5;
  color: #059669;
  border: 1px solid #A7F3D0;
}

.status.pending {
  background: #FFFBEB;
  color: #D97706;
  border: 1px solid #FDE68A;
}

/* Pets */
.pet-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pet-card {
  display: flex;
  align-items: center;
  gap: 16px; 
  padding: 16px; 
  background: #F8FAFC;
  border-radius: 10px;
  border: 1px solid #E2E8F0;
  transition: all 0.2s;
}

.pet-card:hover {
  border-color: #0F766E;
  box-shadow: 0 2px 8px rgba(15, 118, 110, 0.08);
}

.pet-avatar {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
}

.pet-info {
  flex: 1;
  min-width: 0; 
}

.pet-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.pet-info p {
  font-size: 12px;
  color: #64748B;
  margin: 2px 0 0 0;
}

.pet-action {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid #E2E8F0;
  background: white;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.pet-action:hover {
  background: #0F766E;
  color: white;
  border-color: #0F766E;
}

/* Table */
.data-table {
  width: 100%;
  border-collapse: collapse;
  /* Forzar a que la tabla herede la fuente correcta */
  font-family: inherit; 
}

.data-table th {
  text-align: left;
  padding: 12px 16px;
  font-size: 12px;
  font-weight: 600;
  color: #64748B;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #E2E8F0; 
}

.data-table td {
  padding: 16px;
  font-size: 14px;
  color: #1E293B;
  border-bottom: 1px solid #F1F5F9;
}

.amount {
  font-weight: 700;
  color: #0F766E;
}

.btn-pay {
  padding: 8px 16px;
  background: #0F766E;
  color: white;
  border: none;
  border-radius: 8px; 
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.btn-pay:hover {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.25);
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px; 
  color: #94A3B8;
  gap: 12px;
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

.badge-alert {
  background: #FEF2F2;
  color: #EF4444;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  border: 1px solid #FECACA; 
}

/* Responsive */
@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  .appointment-item {
    flex-wrap: wrap; 
  }
  .status {
    margin-left: auto; 
  }
}
</style>