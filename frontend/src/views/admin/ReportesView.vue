<template>
  <div class="reportes-view">
    <ToastContainer />

    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow"><BarChart3 :size="12" /> Administración · Reportes</p>
        <h1>Reportes y Métricas</h1>
        <p class="hero-sub">Resumen operativo del personal y la clínica.</p>
      </div>
    </header>

    <div v-if="cargando" class="loading-state"><div class="spin"></div></div>

    <template v-else>
      <section class="kpis">
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color:#0F766E; --kpi-bg:#F0FDFA;"><Users :size="18" /></div>
          <div><p class="kpi-value">{{ stats.totalPersonal || 0 }}</p><p class="kpi-label">Total Personal</p></div>
        </article>
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color:#3B82F6; --kpi-bg:#EFF6FF;"><Stethoscope :size="18" /></div>
          <div><p class="kpi-value">{{ stats.veterinariosActivos || 0 }}</p><p class="kpi-label">Veterinarios</p></div>
        </article>
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color:#8B5CF6; --kpi-bg:#F5F3FF;"><Headset :size="18" /></div>
          <div><p class="kpi-value">{{ stats.recepcionistasActivos || 0 }}</p><p class="kpi-label">Recepcionistas</p></div>
        </article>
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color:#F59E0B; --kpi-bg:#FFFBEB;"><Package :size="18" /></div>
          <div><p class="kpi-value">{{ stats.almacenActivos || 0 }}</p><p class="kpi-label">Almacén</p></div>
        </article>
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color:#EF4444; --kpi-bg:#FEF2F2;"><Shield :size="18" /></div>
          <div><p class="kpi-value">{{ stats.administradoresActivos || 0 }}</p><p class="kpi-label">Administradores</p></div>
        </article>
      </section>

      <section class="card">
        <header class="card-header">
          <h3>Distribución del personal activo</h3>
        </header>
        <div class="card-body">
          <div class="bar-item" v-for="bar in barras" :key="bar.label">
            <span class="bar-label">{{ bar.label }}</span>
            <div class="bar-track">
              <div class="bar-fill" :style="{ width: bar.pct + '%', background: bar.color }"></div>
            </div>
            <span class="bar-value">{{ bar.value }}</span>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { BarChart3, Users, Stethoscope, Headset, Package, Shield } from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import { getAdminStats } from '@/api/admin.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

const { toastError } = useToast()
const cargando = ref(true)
const stats = ref({})

onMounted(async () => {
  try {
    const { data } = await getAdminStats()
    stats.value = data || {}
  } catch (err) {
    toastError(getApiErrorMessage(err))
  } finally {
    cargando.value = false
  }
})

const barras = computed(() => {
  const items = [
    { label: 'Veterinarios', value: stats.value.veterinariosActivos || 0, color: '#3B82F6' },
    { label: 'Recepcionistas', value: stats.value.recepcionistasActivos || 0, color: '#8B5CF6' },
    { label: 'Almacén', value: stats.value.almacenActivos || 0, color: '#F59E0B' },
    { label: 'Administradores', value: stats.value.administradoresActivos || 0, color: '#EF4444' },
  ]
  const max = Math.max(1, ...items.map(i => i.value))
  return items.map(i => ({ ...i, pct: Math.round((i.value / max) * 100) }))
})
</script>

<style scoped>
.reportes-view { max-width: 1500px; margin: 0 auto; padding: 24px 28px 48px; font-family: 'Inter', sans-serif; color: #0F172A; display: flex; flex-direction: column; gap: 20px; }
.hero { padding: 24px 28px; background: linear-gradient(135deg, #FFFBEB 0%, #FFFFFF 55%); border: 1px solid #FEF3C7; border-radius: 16px; }
.hero-eyebrow { display: inline-flex; align-items: center; gap: 6px; margin: 0 0 8px; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .7px; color: #B45309; background: #fff; padding: 4px 10px; border-radius: 20px; border: 1px solid #FEF3C7; }
.hero h1 { margin: 0 0 4px; font-size: 26px; font-weight: 700; }
.hero-sub { margin: 0; font-size: 14px; color: #64748B; }
.kpis { display: grid; grid-template-columns: repeat(5, 1fr); gap: 16px; }
.kpi { display: flex; align-items: center; gap: 14px; padding: 18px 20px; background: #fff; border: 1px solid #E2E8F0; border-radius: 14px; }
.kpi-icon { width: 42px; height: 42px; border-radius: 12px; display: flex; align-items: center; justify-content: center; background: var(--kpi-bg); color: var(--kpi-color); }
.kpi-value { margin: 0; font-size: 20px; font-weight: 700; }
.kpi-label { margin: 3px 0 0; font-size: 11.5px; color: #64748B; font-weight: 600; }
.card { background: #fff; border: 1px solid #E2E8F0; border-radius: 14px; }
.card-header { padding: 20px 24px; border-bottom: 1px solid #E2E8F0; }
.card-header h3 { margin: 0; font-size: 15.5px; font-weight: 700; }
.card-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 14px; }
.bar-item { display: grid; grid-template-columns: 160px 1fr 50px; gap: 14px; align-items: center; }
.bar-label { font-size: 13px; color: #334155; font-weight: 600; }
.bar-track { height: 12px; background: #F1F5F9; border-radius: 6px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 6px; transition: width .4s ease; }
.bar-value { font-size: 13.5px; font-weight: 700; text-align: right; }
.loading-state { display: flex; justify-content: center; padding: 80px; }
.spin { width: 32px; height: 32px; border: 3px solid #E2E8F0; border-top-color: #F59E0B; border-radius: 50%; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
@media (max-width: 1100px) { .kpis { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 640px) { .kpis { grid-template-columns: 1fr; } .bar-item { grid-template-columns: 100px 1fr 40px; } }
</style>