<template>
  <div class="reportes-view">
    <ToastContainer />

    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <BarChart3 :size="12" /> Administración · Reportes
        </span>
        <h1>Reportes y Métricas</h1>
        <p class="page-header-sub">
          Resumen operativo del personal y la estructura de la clínica.
        </p>
      </div>
    </header>

    <!-- Loading -->
    <div v-if="cargando" class="kpis">
      <div v-for="i in 5" :key="i" class="skeleton-kpi">
        <div class="skeleton sk-icon" />
        <div class="sk-lines">
          <div class="skeleton sk-line-lg" />
          <div class="skeleton sk-line-sm" />
        </div>
      </div>
    </div>

    <template v-else>
      <!-- KPIs -->
      <section class="kpis">
        <article v-for="k in kpis" :key="k.label" class="kpi">
          <div class="kpi-icon" :style="{ background: k.bg, color: k.fg }">
            <component :is="k.icon" :size="18" />
          </div>
          <div>
            <p class="kpi-value">{{ k.value }}</p>
            <p class="kpi-label">{{ k.label }}</p>
          </div>
        </article>
      </section>

      <!-- Charts -->
      <section class="charts-grid">
        <!-- Distribución del personal -->
        <div class="card">
          <div class="card-header">
            <div>
              <h3>Distribución del personal activo</h3>
              <p class="card-sub">Por cargo en la clínica</p>
            </div>
          </div>
          <div class="card-body">
            <div v-if="totalActivos === 0" class="empty-state slim">
              <div class="empty-icon"><Users :size="24" /></div>
              <p>No hay personal activo registrado.</p>
            </div>

            <div v-else class="bars">
              <div v-for="b in barras" :key="b.label" class="bar-item">
                <span class="bar-label">{{ b.label }}</span>
                <div class="bar-track">
                  <div
                    class="bar-fill"
                    :style="{ width: b.pct + '%', background: b.color }"
                  />
                </div>
                <span class="bar-value">{{ b.value }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Ocupación por cargo -->
        <div class="card">
          <div class="card-header">
            <div>
              <h3>Ocupación de la plantilla</h3>
              <p class="card-sub">Porcentaje de cada cargo sobre el total activo</p>
            </div>
          </div>
          <div class="card-body">
            <div v-if="totalActivos === 0" class="empty-state slim">
              <div class="empty-icon"><PieChart :size="24" /></div>
              <p>Sin datos para mostrar.</p>
            </div>

            <div v-else class="composicion">
              <div
                v-for="c in composicion"
                :key="c.label"
                class="comp-item"
              >
                <div class="comp-head">
                  <span class="comp-color" :style="{ background: c.color }" />
                  <span class="comp-label">{{ c.label }}</span>
                  <span class="comp-pct">{{ c.pct }}%</span>
                </div>
                <div class="comp-track">
                  <div class="comp-fill" :style="{ width: c.pct + '%', background: c.color }" />
                </div>
                <span class="comp-meta">{{ c.value }} de {{ totalActivos }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { BarChart3, Users, Stethoscope, Headset, Package, Shield, PieChart } from 'lucide-vue-next'
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

const totalActivos = computed(() =>
  (stats.value.veterinariosActivos || 0) +
  (stats.value.recepcionistasActivos || 0) +
  (stats.value.almacenActivos || 0) +
  (stats.value.administradoresActivos || 0)
)

const kpis = computed(() => [
  { label: 'Total personal',   value: stats.value.totalPersonal || 0,             icon: Users,       bg: 'var(--brand-50)',   fg: 'var(--brand-700)' },
  { label: 'Veterinarios',     value: stats.value.veterinariosActivos || 0,       icon: Stethoscope, bg: 'var(--info-50)',    fg: 'var(--info-600)' },
  { label: 'Recepcionistas',   value: stats.value.recepcionistasActivos || 0,     icon: Headset,     bg: 'var(--purple-50)',  fg: 'var(--purple-600)' },
  { label: 'Almacén',          value: stats.value.almacenActivos || 0,            icon: Package,     bg: 'var(--warning-50)', fg: 'var(--warning-600)' },
  { label: 'Administradores',  value: stats.value.administradoresActivos || 0,    icon: Shield,      bg: 'var(--danger-50)',  fg: 'var(--danger-600)' },
])

const distribucionData = computed(() => [
  { label: 'Veterinarios',    value: stats.value.veterinariosActivos || 0,     color: 'var(--info-500)' },
  { label: 'Recepcionistas',  value: stats.value.recepcionistasActivos || 0,   color: 'var(--purple-500)' },
  { label: 'Almacén',         value: stats.value.almacenActivos || 0,          color: 'var(--warning-500)' },
  { label: 'Administradores', value: stats.value.administradoresActivos || 0,  color: 'var(--brand-700)' },
])

const barras = computed(() => {
  const max = Math.max(1, ...distribucionData.value.map((i) => i.value))
  return distribucionData.value.map((i) => ({
    ...i,
    pct: Math.round((i.value / max) * 100),
  }))
})

const composicion = computed(() => {
  const total = totalActivos.value || 1
  return distribucionData.value.map((i) => ({
    ...i,
    pct: totalActivos.value === 0 ? 0 : Math.round((i.value / total) * 100),
  }))
})
</script>

<style scoped>
.reportes-view {
  max-width: 1500px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-7) var(--space-12);
  font-family: var(--font-sans);
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.kpis {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-4);
}
.kpi {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
  transition: all var(--duration-base) var(--ease-out);
}
.kpi:hover {
  border-color: var(--border-strong);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.kpi-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
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
  letter-spacing: var(--tracking-tight);
  font-variant-numeric: tabular-nums;
}
.kpi-label {
  margin: var(--space-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-semibold);
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-5);
  align-items: start;
}

.card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
  overflow: hidden;
}
.card-header {
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-subtle);
}
.card-header h3 {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
.card-sub {
  margin: 3px 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.card-body { padding: var(--space-5) var(--space-6); }

/* Bars */
.bars { display: flex; flex-direction: column; gap: var(--space-4); }
.bar-item {
  display: grid;
  grid-template-columns: 130px 1fr 40px;
  gap: var(--space-3);
  align-items: center;
}
.bar-label {
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--neutral-700);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.bar-track {
  height: 12px;
  background: var(--neutral-100);
  border-radius: var(--radius-full);
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: var(--radius-full);
  transition: width var(--duration-slower) var(--ease-out);
}
.bar-value {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  text-align: right;
  font-variant-numeric: tabular-nums;
}

/* Composicion */
.composicion {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.comp-item { display: flex; flex-direction: column; gap: var(--space-1); }
.comp-head {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.comp-color {
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}
.comp-label {
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--neutral-700);
  flex: 1;
}
.comp-pct {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}
.comp-track {
  height: 8px;
  background: var(--neutral-100);
  border-radius: var(--radius-full);
  overflow: hidden;
  margin-left: calc(10px + var(--space-2));
}
.comp-fill {
  height: 100%;
  border-radius: var(--radius-full);
  transition: width var(--duration-slower) var(--ease-out);
}
.comp-meta {
  margin-left: calc(10px + var(--space-2));
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: var(--font-medium);
}

/* Empty */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-8) var(--space-4);
  color: var(--text-tertiary);
  text-align: center;
  font-size: var(--text-md);
}
.empty-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-full);
  background: var(--brand-50);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-1);
}
.empty-state p { margin: 0; }

/* Skeleton */
.skeleton-kpi {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
}
.skeleton {
  background: linear-gradient(90deg, var(--neutral-100) 25%, var(--neutral-200) 50%, var(--neutral-100) 75%);
  background-size: 200% 100%;
  border-radius: var(--radius-md);
  animation: shimmer 1.4s infinite;
}
.sk-icon { width: 40px; height: 40px; border-radius: var(--radius-lg); flex-shrink: 0; }
.sk-lines { flex: 1; display: flex; flex-direction: column; gap: var(--space-2); }
.sk-line-lg { height: 16px; width: 55%; }
.sk-line-sm { height: 11px; width: 75%; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Responsive */
@media (max-width: 1100px) {
  .kpis { grid-template-columns: repeat(3, 1fr); }
  .charts-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .reportes-view { padding: var(--space-5) var(--space-4) var(--space-10); }
  .kpis { grid-template-columns: repeat(2, 1fr); gap: var(--space-3); }
  .bar-item { grid-template-columns: 100px 1fr 32px; }
}
@media (max-width: 480px) {
  .reportes-view { padding: var(--space-4) var(--space-3) var(--space-8); }
  .kpis { grid-template-columns: 1fr; }
  .kpi { padding: var(--space-3) var(--space-4); }
  .kpi-value { font-size: var(--text-2xl); }
  .bar-item { grid-template-columns: 90px 1fr 30px; }
  .bar-label { font-size: var(--text-sm); }
}
</style>