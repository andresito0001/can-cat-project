<template>
  <div class="admin-dashboard">
    <ToastContainer />
    
    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow"><Sparkles :size="12" /> Administración · Panel principal</p>
        <h1>Panel de Administración</h1>
        <p class="hero-sub">Resumen general del personal y accesos rápidos de gestión.</p>
      </div>
    </header>

    <section v-if="cargando" class="loading-state"><div class="spin"></div><p>Cargando estadísticas...</p></section>

    <template v-else>
      <section class="kpis">
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color: #0F766E; --kpi-bg: #F0FDFA;"><Users :size="18" /></div>
          <div class="kpi-texto">
            <p class="kpi-value">{{ stats.totalPersonal }}</p>
            <p class="kpi-label">Total Personal</p>
          </div>
        </article>
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color: #3B82F6; --kpi-bg: #EFF6FF;"><Stethoscope :size="18" /></div>
          <div class="kpi-texto">
            <p class="kpi-value">{{ stats.veterinariosActivos }}</p>
            <p class="kpi-label">Veterinarios Activos</p>
          </div>
        </article>
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color: #8B5CF6; --kpi-bg: #F5F3FF;"><Headset :size="18" /></div>
          <div class="kpi-texto">
            <p class="kpi-value">{{ stats.recepcionistasActivos }}</p>
            <p class="kpi-label">Recepcionistas Activos</p>
          </div>
        </article>
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color: #F59E0B; --kpi-bg: #FFFBEB;"><Package :size="18" /></div>
          <div class="kpi-texto">
            <p class="kpi-value">{{ stats.almacenActivos }}</p>
            <p class="kpi-label">Encargados de Almacén</p>
          </div>
        </article>
      </section>

      <section class="card wide">
        <div class="card-header">
          <div>
            <h3>Accesos rápidos de gestión</h3>
            <p class="card-sub">Accede directamente a las tareas administrativas más frecuentes</p>
          </div>
        </div>
        <div class="card-body card-body-slim">
          <div class="quick-grid">
            <router-link to="/admin/personal" class="quick-card">
              <div class="quick-icon" style="background: #F0FDFA; color: #0F766E;"><UserPlus :size="20" /></div>
              <div class="quick-texto">
                <p class="quick-titulo">Gestionar Personal</p>
                <p class="quick-desc">Crear, editar o desactivar miembros del equipo</p>
              </div>
              <ChevronRight :size="16" class="quick-arrow" />
            </router-link>
            <router-link to="/admin/usuarios" class="quick-card">
              <div class="quick-icon" style="background: #EFF6FF; color: #3B82F6;"><Users :size="20" /></div>
              <div class="quick-texto">
                <p class="quick-titulo">Directorio de Usuarios</p>
                <p class="quick-desc">Ver cuentas, roles y estados de acceso</p>
              </div>
              <ChevronRight :size="16" class="quick-arrow" />
            </router-link>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Sparkles, Users, Stethoscope, Headset, Package, UserPlus, ChevronRight } from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import { getAdminStats } from '@/api/admin.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

const { toastError } = useToast()
const cargando = ref(true)
const stats = ref({ totalPersonal: 0, veterinariosActivos: 0, recepcionistasActivos: 0, almacenActivos: 0 })

onMounted(async () => {
  try {
    const { data } = await getAdminStats()
    stats.value = data
  } catch (err) {
    toastError(getApiErrorMessage(err))
    // Fallback para que no se vea vacío si falla
    stats.value = { totalPersonal: 0, veterinariosActivos: 0, recepcionistasActivos: 0, almacenActivos: 0 }
  } finally {
    cargando.value = false
  }
})
</script>

<style scoped>
/* Copia los estilos de .admin-dashboard, .hero, .kpis, .kpi, .card, .quick-grid de tu DashboardWarehouseView.vue aquí para mantener la consistencia */
.admin-dashboard { max-width: 1500px; margin: 0 auto; padding: 24px 28px 48px; font-family: 'Inter', sans-serif; color: #0F172A; display: flex; flex-direction: column; gap: 20px; }
.hero { padding: 24px 28px; background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%); border: 1px solid #CCFBF1; border-radius: 16px; }
.hero-eyebrow { display: inline-flex; align-items: center; gap: 6px; margin: 0 0 8px; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .7px; color: #0F766E; background: #fff; padding: 4px 10px; border-radius: 20px; border: 1px solid #CCFBF1; }
.hero h1 { margin: 0 0 4px; font-size: 26px; font-weight: 700; color: #0F172A; }
.hero-sub { margin: 0; font-size: 14px; color: #64748B; }
.kpis { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
.kpi { display: flex; align-items: center; gap: 14px; padding: 18px 20px; background: #fff; border: 1px solid #E2E8F0; border-radius: 14px; }
.kpi-icon { width: 42px; height: 42px; border-radius: 12px; display: flex; align-items: center; justify-content: center; background: var(--kpi-bg); color: var(--kpi-color); }
.kpi-value { margin: 0; font-size: 20px; font-weight: 700; color: #0F172A; }
.kpi-label { margin: 3px 0 0; font-size: 11.5px; color: #64748B; font-weight: 600; }
.card { background: #fff; border: 1px solid #E2E8F0; border-radius: 14px; }
.card.wide { grid-column: 1 / -1; }
.card-header { padding: 20px 24px; border-bottom: 1px solid #E2E8F0; }
.card-header h3 { margin: 0; font-size: 15.5px; font-weight: 700; color: #0F172A; }
.card-sub { margin: 3px 0 0; font-size: 12.5px; color: #64748B; }
.card-body-slim { padding: 14px 18px 18px; }
.quick-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.quick-card { display: grid; grid-template-columns: auto 1fr auto; gap: 14px; align-items: center; padding: 16px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 12px; text-decoration: none; color: inherit; transition: all .2s ease; }
.quick-card:hover { background: #fff; border-color: #0F766E; transform: translateY(-2px); box-shadow: 0 8px 20px -10px rgba(15, 118, 110, .25); }
.quick-icon { width: 40px; height: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.quick-titulo { margin: 0; font-size: 13.5px; font-weight: 700; color: #0F172A; }
.quick-desc { margin: 2px 0 0; font-size: 11.5px; color: #64748B; }
.quick-arrow { color: #CBD5E1; transition: all .2s ease; }
.quick-card:hover .quick-arrow { color: #0F766E; transform: translateX(2px); }
.loading-state { display: flex; flex-direction: column; align-items: center; padding: 80px; color: #64748B; }
.spin { width: 32px; height: 32px; border: 3px solid #E2E8F0; border-top-color: #0F766E; border-radius: 50%; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
@media (max-width: 1024px) { .kpis { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) { .kpis { grid-template-columns: 1fr; } .quick-grid { grid-template-columns: 1fr; } }
</style>