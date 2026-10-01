<template>
  <div class="dashboard-almacen">
    <ToastContainer />

    <!-- ═══ HERO ═══ -->
    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow">
          <Sparkles :size="12" />
          Almacén · Panel principal
        </p>
        <h1>Hola, {{ nombreUsuario }}</h1>
        <p class="hero-sub">
          {{ fechaLarga }} · Resumen del inventario y actividad reciente.
        </p>
      </div>
      <div class="hero-right">
        <router-link to="/almacen/entrada" class="btn-primary">
          <ArrowDownToLine :size="15" />
          Registrar entrada
        </router-link>
      </div>
    </header>

    <!-- ═══ LOADING (skeleton) ═══ -->
    <template v-if="cargando">
      <section class="kpis">
        <div v-for="i in 4" :key="i" class="skeleton-kpi">
          <div class="skeleton sk-icon" />
          <div class="sk-lines">
            <div class="skeleton sk-line-lg" />
            <div class="skeleton sk-line-sm" />
          </div>
        </div>
      </section>

      <section class="destacado destacado-skeleton">
        <div class="skeleton sk-dest-line-lg" />
        <div class="skeleton sk-dest-line-md" />
        <div class="skeleton sk-dest-line-sm" />
      </section>

      <section class="content-grid">
        <div class="card card-skeleton">
          <div class="skeleton sk-card-title" />
          <div v-for="i in 4" :key="i" class="skeleton sk-row" />
        </div>
        <div class="card card-skeleton">
          <div class="skeleton sk-card-title" />
          <div v-for="i in 4" :key="i" class="skeleton sk-row" />
        </div>
      </section>
    </template>

    <template v-else>
      <!-- ═══ KPIs ═══ -->
      <section class="kpis">
        <article class="kpi">
          <div class="kpi-icon kpi-icon--brand">
            <Package :size="18" />
          </div>
          <div class="kpi-texto">
            <p class="kpi-value">{{ stats.totalProductos }}</p>
            <p class="kpi-label">Productos en catálogo</p>
          </div>
        </article>

        <article class="kpi" :class="{ 'is-warn': stats.alertasCriticas > 0 }">
          <div class="kpi-icon" :class="stats.alertasCriticas > 0 ? 'kpi-icon--danger' : 'kpi-icon--warning'">
            <AlertTriangle :size="18" />
          </div>
          <div class="kpi-texto">
            <p class="kpi-value" :class="{ 'is-warn': stats.alertasCriticas > 0 }">
              {{ stats.totalAlertas }}
            </p>
            <p class="kpi-label">
              Alertas de stock
              <span v-if="stats.alertasCriticas > 0" class="kpi-badge">
                {{ stats.alertasCriticas }} crítica{{ stats.alertasCriticas === 1 ? '' : 's' }}
              </span>
            </p>
          </div>
        </article>

        <article class="kpi">
          <div class="kpi-icon kpi-icon--info">
            <DollarSign :size="18" />
          </div>
          <div class="kpi-texto">
            <p class="kpi-value">{{ fmtUsd(stats.valorInventario) }}</p>
            <p class="kpi-label">Valor del inventario</p>
          </div>
        </article>

        <article class="kpi">
          <div class="kpi-icon kpi-icon--purple">
            <TrendingUp :size="18" />
          </div>
          <div class="kpi-texto">
            <p class="kpi-value">{{ stats.movimientosHoy }}</p>
            <p class="kpi-label">
              Movimientos hoy
              <span v-if="stats.movimientosSemana > 0" class="kpi-secondary">
                · {{ stats.movimientosSemana }} esta semana
              </span>
            </p>
          </div>
        </article>
      </section>

      <!-- ═══ DESTACADO: producto más crítico ═══ -->
      <Transition name="fade-up">
        <section v-if="productoMasCritico" class="destacado">
          <div class="destacado-info">
            <span class="destacado-tag">
              <AlertTriangle :size="12" />
              Requiere reposición urgente
            </span>
            <h2 class="destacado-nombre">{{ productoMasCritico.nombre }}</h2>
            <p class="destacado-sku">
              <span class="sku-mono">{{ productoMasCritico.codigoSku }}</span>
              <span class="destacado-cat">{{ productoMasCritico.categoria }}</span>
            </p>
            <div class="destacado-meta">
              <span class="meta-item">
                <Package :size="13" />
                Stock actual: <strong>{{ productoMasCritico.stockActual }}</strong>
              </span>
              <span class="meta-item">
                <Target :size="13" />
                Mínimo requerido: <strong>{{ productoMasCritico.stockMinimo }}</strong>
              </span>
              <span class="meta-item is-critical">
                <AlertCircle :size="13" />
                Déficit: <strong>{{ productoMasCritico.stockMinimo - productoMasCritico.stockActual }}</strong>
              </span>
            </div>
          </div>
          <div class="destacado-accion">
            <router-link to="/almacen/entrada" class="btn-accion-grande">
              Reponer stock
              <ArrowRight :size="15" />
            </router-link>
          </div>
        </section>
      </Transition>

      <!-- ═══ GRID PRINCIPAL ═══ -->
      <section class="content-grid">
        <!-- Alertas de stock -->
        <div class="card">
          <div class="card-header">
            <div>
              <h3>Alertas de stock</h3>
              <p class="card-sub">
                {{ alertas.length
                  ? `${alertas.length} producto${alertas.length === 1 ? '' : 's'} por debajo del mínimo`
                  : 'Todo el inventario está abastecido' }}
              </p>
            </div>
            <router-link v-if="alertas.length > 5" to="/almacen/inventario" class="btn-link">
              Ver todas
            </router-link>
          </div>

          <div class="card-body">
            <div v-if="alertas.length === 0" class="empty-state slim">
              <div class="empty-icon empty-icon--success">
                <CheckCircle2 :size="24" />
              </div>
              <p>Sin alertas activas. Todo el inventario está por encima del stock mínimo.</p>
            </div>

            <div v-else class="alertas-list">
              <router-link
                v-for="a in alertasVisibles"
                :key="a.id"
                to="/almacen/inventario"
                class="alerta-item"
                :class="criticidadClass(a)"
              >
                <EntityAvatar
                  :nombre="a.nombre"
                  :tipo="`producto-${(a.categoria || '').toLowerCase()}`"
                  size="sm"
                />
                <div class="alerta-info">
                  <p class="alerta-nombre">{{ a.nombre }}</p>
                  <p class="alerta-meta">
                    <span class="sku-mono-sm">{{ a.codigoSku }}</span>
                    · {{ a.categoria }}
                  </p>
                </div>
                <div class="alerta-stock">
                  <span class="alerta-num" :class="criticidadClass(a)">
                    {{ a.stockActual }}
                  </span>
                  <span class="alerta-min">min {{ a.stockMinimo }}</span>
                </div>
                <span class="alerta-badge" :class="criticidadClass(a)">
                  {{ criticidadLabel(a) }}
                </span>
              </router-link>

              <router-link
                v-if="alertas.length > 5"
                to="/almacen/inventario"
                class="more-link"
              >
                + {{ alertas.length - 5 }} producto{{ alertas.length - 5 === 1 ? '' : 's' }} más con alerta
              </router-link>
            </div>
          </div>
        </div>

        <!-- Movimientos recientes -->
        <div class="card">
          <div class="card-header">
            <div>
              <h3>Movimientos recientes</h3>
              <p class="card-sub">
                {{ movimientos.length
                  ? `Últimos ${movimientosVisibles.length} movimientos registrados`
                  : 'Sin movimientos registrados' }}
              </p>
            </div>
          </div>

          <div class="card-body">
            <div v-if="movimientos.length === 0" class="empty-state slim">
              <div class="empty-icon empty-icon--brand">
                <Activity :size="24" />
              </div>
              <p>
                Aún no hay movimientos registrados.
                Cuando registres una entrada de mercancía aparecerá aquí.
              </p>
              <router-link to="/almacen/entrada" class="btn-link">
                Registrar entrada
              </router-link>
            </div>

            <div v-else class="movimientos-list">
              <article
                v-for="m in movimientosVisibles"
                :key="m.id"
                class="movimiento-item"
              >
                <div class="mov-icon" :class="movimientoClass(m.tipoMovimiento)">
                  <component :is="movimientoIcon(m.tipoMovimiento)" :size="15" />
                </div>
                <div class="mov-info">
                  <p class="mov-nombre">{{ m.nombreProducto }}</p>
                  <p class="mov-meta">
                    <span class="mov-tipo">{{ m.tipoMovimiento }}</span>
                    · {{ m.motivo }}
                    <template v-if="m.documentoReferencia">
                      · <span class="mov-doc">{{ m.documentoReferencia }}</span>
                    </template>
                  </p>
                </div>
                <div class="mov-side">
                  <span class="mov-cantidad" :class="movimientoClass(m.tipoMovimiento)">
                    {{ m.tipoMovimiento === 'Entrada' ? '+' : '' }}{{ m.cantidad }}
                  </span>
                  <span class="mov-fecha">{{ fmtFechaCorta(m.fechaMovimiento) }}</span>
                </div>
              </article>
            </div>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Sparkles, Package, AlertTriangle, DollarSign, TrendingUp,
  ArrowDownToLine, ArrowRight, AlertCircle, Target, CheckCircle2,
  Activity, ArrowUpFromLine, Settings2,
} from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import EntityAvatar from '@/components/ui/EntityAvatar.vue'
import { useAuthStore } from '@/stores/auth.store'
import { useToast } from '@/composables/useToast'
import { getProductos, getMovimientos } from '@/api/almacen.api'
import { getApiErrorMessage } from '@/utils/apiError'

const authStore = useAuthStore()
const { toastError } = useToast()

/* ─── Estado ─── */
const cargando = ref(true)
const productos = ref([])
const movimientos = ref([])

/* ─── Datos derivados ─── */
const nombreUsuario = computed(() => authStore.userName || 'Encargado')

const fechaLarga = computed(() =>
  new Date().toLocaleDateString('es-VE', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
)

const alertas = computed(() =>
  productos.value
    .filter((p) => p.stockActual <= p.stockMinimo && p.activo !== false)
    .sort((a, b) => {
      const da = a.stockMinimo - a.stockActual
      const db = b.stockMinimo - b.stockActual
      return db - da
    })
)

const alertasVisibles = computed(() => alertas.value.slice(0, 5))

const productoMasCritico = computed(() => {
  const criticos = alertas.value.filter((a) => a.stockActual === 0)
  if (criticos.length > 0) return criticos[0]
  return alertas.value[0] || null
})

const movimientosVisibles = computed(() => movimientos.value.slice(0, 6))

const stats = computed(() => {
  const total = productos.value.length
  const alertasCriticas = productos.value.filter((p) => p.stockActual === 0).length

  const valor = productos.value.reduce(
    (sum, p) => sum + p.stockActual * (p.costoAdquisicion || 0),
    0
  )

  const hoy = new Date().toISOString().slice(0, 10)
  const hace7Dias = new Date(Date.now() - 7 * 86400000).toISOString().slice(0, 10)

  const movimientosHoy = movimientos.value.filter(
    (m) => String(m.fechaMovimiento || '').slice(0, 10) === hoy
  ).length

  const movimientosSemana = movimientos.value.filter(
    (m) => String(m.fechaMovimiento || '').slice(0, 10) >= hace7Dias
  ).length

  return {
    totalProductos: total,
    totalAlertas: alertas.value.length,
    alertasCriticas,
    valorInventario: valor,
    movimientosHoy,
    movimientosSemana,
  }
})

/* ─── Helpers ─── */
function fmtUsd(v) {
  if (v == null) return '$0.00'
  return `$${Number(v).toFixed(2)}`
}

function fmtFechaCorta(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const hoy = new Date()
  const ayer = new Date(Date.now() - 86400000)

  const mismoDia = (a, b) =>
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()

  if (mismoDia(d, hoy)) return 'Hoy'
  if (mismoDia(d, ayer)) return 'Ayer'

  const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']
  return `${d.getDate()} ${MESES[d.getMonth()]}`
}

function criticidadClass(p) {
  if (p.stockActual === 0) return 'is-critical'
  if (p.stockActual <= p.stockMinimo * 0.5) return 'is-low'
  return 'is-warning'
}

function criticidadLabel(p) {
  if (p.stockActual === 0) return 'Agotado'
  if (p.stockActual <= p.stockMinimo * 0.5) return 'Crítico'
  return 'Bajo'
}

function movimientoClass(tipo) {
  switch (tipo) {
    case 'Entrada': return 'is-entrada'
    case 'Salida': return 'is-salida'
    case 'Ajuste': return 'is-ajuste'
    case 'Vencimiento': return 'is-vencimiento'
    default: return 'is-default'
  }
}

function movimientoIcon(tipo) {
  switch (tipo) {
    case 'Entrada': return ArrowDownToLine
    case 'Salida': return ArrowUpFromLine
    case 'Ajuste': return Settings2
    case 'Vencimiento': return AlertTriangle
    default: return Activity
  }
}

/* ─── Carga ─── */
async function cargar() {
  cargando.value = true
  try {
    const [prodRes, movRes] = await Promise.allSettled([
      getProductos(),
      getMovimientos(),
    ])

    if (prodRes.status === 'fulfilled') {
      productos.value = prodRes.value.data || []
    } else {
      toastError(getApiErrorMessage(prodRes.reason))
    }

    if (movRes.status === 'fulfilled') {
      movimientos.value = movRes.value.data || []
    }
  } finally {
    cargando.value = false
  }
}

onMounted(cargar)
</script>

<style scoped>
.dashboard-almacen {
  max-width: 1500px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-7) var(--space-12);
  font-family: var(--font-sans);
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}
button { font-family: inherit; }

/* ═══════════════════════════════════════════════════════════════
   HERO
   ═══════════════════════════════════════════════════════════════ */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-5);
  flex-wrap: wrap;
  padding: var(--space-6) var(--space-7);
  background: linear-gradient(135deg, var(--brand-50) 0%, var(--bg-surface) 55%);
  border: 1px solid var(--brand-100);
  border-radius: var(--radius-3xl);
}
.hero-left { min-width: 0; }
.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin: 0 0 var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--brand-700);
  background: var(--bg-surface);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid var(--brand-100);
}
.hero h1 {
  margin: 0 0 var(--space-1);
  font-size: var(--text-5xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
  line-height: 1.15;
}
.hero-sub {
  margin: 0;
  font-size: var(--text-base);
  color: var(--text-secondary);
  max-width: 620px;
  line-height: var(--leading-normal);
}
.hero-right { display: flex; align-items: center; }

/* ═══════════════════════════════════════════════════════════════
   KPIs
   ═══════════════════════════════════════════════════════════════ */
.kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}
.kpi {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
  transition: border-color var(--duration-base) var(--ease-out),
              box-shadow var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
}
.kpi:hover {
  border-color: var(--border-strong);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.kpi.is-warn { border-color: var(--danger-200); }

.kpi-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.kpi-icon--brand   { background: var(--brand-50);   color: var(--brand-700); }
.kpi-icon--info    { background: var(--info-50);    color: var(--info-600); }
.kpi-icon--warning { background: var(--warning-50); color: var(--warning-600); }
.kpi-icon--danger  { background: var(--danger-50);  color: var(--danger-600); }
.kpi-icon--purple  { background: var(--purple-50);  color: var(--purple-600); }

.kpi-texto { min-width: 0; }
.kpi-value {
  margin: 0;
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.1;
  letter-spacing: var(--tracking-tight);
  font-variant-numeric: tabular-nums;
}
.kpi-value.is-warn { color: var(--danger-600); }
.kpi-label {
  margin: var(--space-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-semibold);
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}
.kpi-badge {
  display: inline-flex;
  align-items: center;
  padding: 1px var(--space-2);
  border-radius: var(--radius-full);
  background: var(--danger-50);
  color: var(--danger-700);
  border: 1px solid var(--danger-200);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.02em;
}
.kpi-secondary {
  font-weight: var(--font-medium);
  color: var(--text-tertiary);
}

/* ═══════════════════════════════════════════════════════════════
   DESTACADO
   ═══════════════════════════════════════════════════════════════ */
.destacado {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: var(--space-6);
  align-items: center;
  padding: var(--space-6) var(--space-7);
  background: linear-gradient(135deg, var(--danger-50) 0%, var(--bg-surface) 55%);
  border: 1px solid var(--danger-200);
  border-radius: var(--radius-3xl);
  box-shadow: 0 1px 2px rgba(220, 38, 38, 0.04),
              0 8px 24px -12px rgba(220, 38, 38, 0.20);
  position: relative;
  overflow: hidden;
}
.destacado::after {
  content: '';
  position: absolute;
  right: -40px;
  top: -40px;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(220, 38, 38, 0.06) 0%, transparent 70%);
  pointer-events: none;
}
.destacado-info { min-width: 0; position: relative; z-index: 1; }
.destacado-tag {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-3);
  background: var(--danger-50);
  border: 1px solid var(--danger-200);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--danger-700);
  margin-bottom: var(--space-3);
}
.destacado-nombre {
  margin: 0 0 var(--space-1);
  font-size: var(--text-5xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
  line-height: 1.15;
}
.destacado-sku {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin: 0 0 var(--space-3);
  font-size: var(--text-md);
  color: var(--text-secondary);
}
.sku-mono {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  background: var(--bg-surface);
  color: var(--neutral-700);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-subtle);
}
.destacado-cat {
  font-weight: var(--font-semibold);
}
.destacado-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-5);
  font-size: var(--text-md);
  color: var(--neutral-700);
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
}
.meta-item strong { color: var(--text-primary); font-weight: var(--font-bold); }
.meta-item.is-critical { color: var(--danger-600); font-weight: var(--font-semibold); }
.meta-item.is-critical strong { color: var(--danger-600); }
.destacado-accion { position: relative; z-index: 1; }

.btn-accion-grande {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-6);
  background: var(--danger-600);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-xl);
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  cursor: pointer;
  text-decoration: none;
  transition: all var(--duration-base) var(--ease-out);
  white-space: nowrap;
  box-shadow: 0 4px 12px -2px rgba(220, 38, 38, 0.30);
}
.btn-accion-grande:hover {
  background: var(--danger-700);
  transform: translateY(-2px);
  box-shadow: 0 12px 24px -8px rgba(220, 38, 38, 0.40);
}

/* ═══════════════════════════════════════════════════════════════
   CONTENT GRID
   ═══════════════════════════════════════════════════════════════ */
.content-grid {
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
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-subtle);
  flex-wrap: wrap;
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
.card-body { padding: var(--space-4) var(--space-5) var(--space-5); }

.btn-link {
  background: none;
  border: none;
  padding: 0;
  color: var(--brand-700);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  cursor: pointer;
  text-decoration: none;
  white-space: nowrap;
  transition: color var(--duration-fast) var(--ease-out);
}
.btn-link:hover { color: var(--brand-800); text-decoration: underline; }

/* ═══════════════════════════════════════════════════════════════
   BOTÓN PRIMARIO (hero)
   ═══════════════════════════════════════════════════════════════ */
.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  background: var(--brand-700);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-lg);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  cursor: pointer;
  transition: all var(--duration-base) var(--ease-out);
  text-decoration: none;
  white-space: nowrap;
  box-shadow: var(--shadow-xs);
}
.btn-primary:hover {
  background: var(--brand-800);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, 0.40);
}

/* ═══════════════════════════════════════════════════════════════
   ALERTAS LIST
   ═══════════════════════════════════════════════════════════════ */
.alertas-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.alerta-item {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  gap: var(--space-3);
  align-items: center;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  text-decoration: none;
  color: inherit;
  transition: all var(--duration-base) var(--ease-out);
}
.alerta-item:hover {
  background: var(--bg-surface);
  border-color: var(--border-strong);
  transform: translateX(2px);
}
.alerta-item.is-critical {
  background: var(--danger-50);
  border-color: var(--danger-200);
}
.alerta-item.is-low {
  background: var(--warning-50);
  border-color: var(--warning-200);
}

.alerta-info { min-width: 0; }
.alerta-nombre {
  margin: 0;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  letter-spacing: -0.01em;
}
.alerta-meta {
  margin: 2px 0 0;
  font-size: var(--text-xs);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sku-mono-sm {
  font-family: var(--font-mono);
  color: var(--text-tertiary);
  font-size: var(--text-xs);
}

.alerta-stock {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 1px;
  flex-shrink: 0;
}
.alerta-num {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  line-height: 1;
  font-variant-numeric: tabular-nums;
  color: var(--brand-700);
}
.alerta-num.is-warning { color: var(--warning-600); }
.alerta-num.is-low { color: var(--danger-600); }
.alerta-num.is-critical { color: var(--danger-700); }
.alerta-min {
  font-size: var(--text-2xs);
  color: var(--text-tertiary);
  font-weight: var(--font-semibold);
}

.alerta-badge {
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  white-space: nowrap;
  flex-shrink: 0;
  border: 1px solid;
}
.alerta-badge.is-warning {
  background: var(--warning-50);
  color: var(--warning-700);
  border-color: var(--warning-200);
}
.alerta-badge.is-low {
  background: var(--danger-50);
  color: var(--danger-700);
  border-color: var(--danger-200);
}
.alerta-badge.is-critical {
  background: var(--danger-700);
  color: var(--text-inverse);
  border-color: var(--danger-700);
}

.more-link {
  display: block;
  margin-top: var(--space-2);
  padding: var(--space-2);
  text-align: center;
  font-size: var(--text-sm);
  color: var(--brand-700);
  font-weight: var(--font-bold);
  text-decoration: none;
  border-radius: var(--radius-lg);
  transition: background-color var(--duration-fast) var(--ease-out);
}
.more-link:hover { background: var(--brand-50); }

/* ═══════════════════════════════════════════════════════════════
   MOVIMIENTOS
   ═══════════════════════════════════════════════════════════════ */
.movimientos-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}
.movimiento-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-3);
  align-items: center;
  padding: var(--space-3);
  border-radius: var(--radius-lg);
  transition: background-color var(--duration-fast) var(--ease-out);
}
.movimiento-item:hover { background: var(--bg-surface-alt); }

.mov-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.mov-icon.is-entrada     { background: var(--success-50); color: var(--success-600); }
.mov-icon.is-salida      { background: var(--danger-50);  color: var(--danger-600); }
.mov-icon.is-ajuste      { background: var(--info-50);    color: var(--info-600); }
.mov-icon.is-vencimiento { background: var(--warning-50); color: var(--warning-600); }
.mov-icon.is-default     { background: var(--neutral-100); color: var(--text-secondary); }

.mov-info { min-width: 0; }
.mov-nombre {
  margin: 0;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.mov-meta {
  margin: 2px 0 0;
  font-size: var(--text-xs);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.mov-tipo { font-weight: var(--font-bold); color: var(--neutral-700); }
.mov-doc {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

.mov-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}
.mov-cantidad {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.mov-cantidad.is-entrada     { color: var(--success-600); }
.mov-cantidad.is-salida      { color: var(--danger-600); }
.mov-cantidad.is-ajuste      { color: var(--info-600); }
.mov-cantidad.is-vencimiento { color: var(--warning-600); }
.mov-fecha {
  font-size: var(--text-2xs);
  color: var(--text-tertiary);
  font-weight: var(--font-semibold);
}

/* ═══════════════════════════════════════════════════════════════
   EMPTY STATE
   ═══════════════════════════════════════════════════════════════ */
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
.empty-state.slim { padding: var(--space-6) var(--space-4); }
.empty-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-1);
}
.empty-icon--brand   { background: var(--brand-50);   color: var(--brand-700); }
.empty-icon--success { background: var(--success-50); color: var(--success-600); }
.empty-state p {
  margin: 0;
  max-width: 380px;
  line-height: var(--leading-normal);
  color: var(--text-secondary);
}
.empty-state .btn-link {
  margin-top: var(--space-2);
  font-size: var(--text-md);
}

/* ═══════════════════════════════════════════════════════════════
   SKELETON
   ═══════════════════════════════════════════════════════════════ */
.skeleton {
  background: linear-gradient(
    90deg,
    var(--neutral-100) 25%,
    var(--neutral-200) 50%,
    var(--neutral-100) 75%
  );
  background-size: 200% 100%;
  border-radius: var(--radius-md);
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.skeleton-kpi {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
}
.sk-icon { width: 44px; height: 44px; border-radius: var(--radius-xl); flex-shrink: 0; }
.sk-lines { flex: 1; display: flex; flex-direction: column; gap: var(--space-2); min-width: 0; }
.sk-line-lg { height: 16px; width: 55%; }
.sk-line-sm { height: 11px; width: 75%; }

.destacado-skeleton {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  align-items: flex-start;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  padding: var(--space-7);
}
.sk-dest-line-lg { height: 20px; width: 40%; }
.sk-dest-line-md { height: 16px; width: 65%; }
.sk-dest-line-sm { height: 14px; width: 30%; }

.card-skeleton {
  padding: var(--space-5) var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.sk-card-title { height: 18px; width: 45%; margin-bottom: var(--space-2); }
.sk-row { height: 44px; width: 100%; border-radius: var(--radius-lg); }

/* ═══════════════════════════════════════════════════════════════
   TRANSICIONES
   ═══════════════════════════════════════════════════════════════ */
.fade-up-enter-active { transition: opacity var(--duration-slow) var(--ease-out),
                                    transform var(--duration-slow) var(--ease-out); }
.fade-up-leave-active { transition: opacity var(--duration-base) var(--ease-out); }
.fade-up-enter-from   { opacity: 0; transform: translateY(8px); }
.fade-up-leave-to     { opacity: 0; }

/* ═══════════════════════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════════════════════ */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
  .content-grid { grid-template-columns: 1fr; }
  .destacado { grid-template-columns: 1fr; gap: var(--space-5); }
  .destacado-accion { width: 100%; }
  .btn-accion-grande { width: 100%; justify-content: center; }
}

@media (max-width: 768px) {
  .dashboard-almacen { padding: var(--space-5) var(--space-4) var(--space-10); gap: var(--space-4); }
  .hero { padding: var(--space-5); border-radius: var(--radius-2xl); }
  .hero h1 { font-size: var(--text-4xl); }
  .hero-right { width: 100%; }
  .hero-right .btn-primary { width: 100%; justify-content: center; }

  .kpis { grid-template-columns: 1fr; gap: var(--space-3); }
  .kpi { padding: var(--space-4); }

  .destacado { padding: var(--space-5); border-radius: var(--radius-2xl); }
  .destacado-nombre { font-size: var(--text-4xl); }

  .card-header { padding: var(--space-4) var(--space-5); }
  .card-body { padding: var(--space-3) var(--space-4) var(--space-4); }

  .alerta-item {
    grid-template-columns: auto 1fr auto;
    gap: var(--space-3);
  }
  .alerta-badge { grid-column: 2 / -1; justify-self: flex-start; margin-top: 2px; }
}

@media (max-width: 480px) {
  .dashboard-almacen { padding: var(--space-4) var(--space-3) var(--space-8); }
  .hero { padding: var(--space-4); }
  .hero h1 { font-size: var(--text-3xl); }
  .hero-eyebrow { font-size: var(--text-2xs); }

  .kpi { gap: var(--space-3); padding: var(--space-3) var(--space-4); }
  .kpi-value { font-size: var(--text-3xl); }

  .destacado { padding: var(--space-4); }
  .destacado-nombre { font-size: var(--text-3xl); }
  .destacado-meta { gap: var(--space-3); font-size: var(--text-sm); }

  .movimiento-item { grid-template-columns: auto 1fr; gap: var(--space-3); }
  .mov-side {
    grid-column: 2;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    padding-top: var(--space-1);
  }
  .mov-cantidad { font-size: var(--text-base); }
}
</style>