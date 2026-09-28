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

    <!-- ═══ LOADING ═══ -->
    <div v-if="cargando" class="loading-state">
      <div class="spin"></div>
      <p>Cargando datos del almacén…</p>
    </div>

    <template v-else>
      <!-- ═══ KPIs ═══ -->
      <section class="kpis">
        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color: #0F766E; --kpi-bg: #F0FDFA;">
            <Package :size="18" />
          </div>
          <div class="kpi-texto">
            <p class="kpi-value">{{ stats.totalProductos }}</p>
            <p class="kpi-label">Productos en catálogo</p>
          </div>
        </article>

        <article class="kpi" :class="{ 'is-warn': stats.alertasCriticas > 0 }">
          <div
            class="kpi-icon"
            :style="stats.alertasCriticas > 0
              ? '--kpi-color: #DC2626; --kpi-bg: #FEF2F2;'
              : '--kpi-color: #F59E0B; --kpi-bg: #FFFBEB;'"
          >
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
          <div class="kpi-icon" style="--kpi-color: #3B82F6; --kpi-bg: #EFF6FF;">
            <DollarSign :size="18" />
          </div>
          <div class="kpi-texto">
            <p class="kpi-value">{{ fmtUsd(stats.valorInventario) }}</p>
            <p class="kpi-label">Valor del inventario</p>
          </div>
        </article>

        <article class="kpi">
          <div class="kpi-icon" style="--kpi-color: #8B5CF6; --kpi-bg: #F5F3FF;">
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

      <!-- ═══ CARD DESTACADA: producto más crítico ═══ -->
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
            <router-link to="/almacen/alertas" class="btn-link">
              Ver todas
            </router-link>
          </div>
          <div class="card-body">
            <div v-if="alertas.length === 0" class="empty-state slim">
              <div class="empty-icon">
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
                <div class="alerta-avatar" :data-cat="a.categoria">
                  {{ inicialNombre(a.nombre) }}
                </div>
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
                to="/almacen/alertas"
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
              <div class="empty-icon">
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
import { useAuthStore } from '@/stores/auth.store'
import { useToast } from '@/composables/useToast'
import { getProductos, getMovimientos } from '@/api/almacen.api'
import { getApiErrorMessage } from '@/utils/apiError'

const authStore = useAuthStore()
const { toastError } = useToast()

// ─── Estado ───
const cargando = ref(true)
const productos = ref([])
const movimientos = ref([])

// ─── Datos derivados ───
const nombreUsuario = computed(() => authStore.userName || 'Encargado')

const fechaLarga = computed(() =>
  new Date().toLocaleDateString('es-VE', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
)

// ─── Alertas derivadas de productos ───
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

// ─── KPIs ───
const stats = computed(() => {
  const total = productos.value.length
  const alertasCriticas = productos.value.filter((p) => p.stockActual === 0).length

  const valor = productos.value.reduce(
    (sum, p) => sum + (p.stockActual * (p.costoAdquisicion || 0)),
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

// ─── Helpers de formato ───
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

  const MESES = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']
  return `${d.getDate()} ${MESES[d.getMonth()]}`
}

function inicialNombre(nombre) {
  return String(nombre || '?').trim().charAt(0).toUpperCase() || '?'
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

// ─── Carga ───
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
  padding: 24px 28px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #0F172A;
  display: flex;
  flex-direction: column;
  gap: 20px;
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
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
}
.hero-left { min-width: 0; }
.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 8px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .7px;
  color: #0F766E;
  background: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  border: 1px solid #CCFBF1;
}
.hero h1 {
  margin: 0 0 4px;
  font-size: 26px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.02em;
  line-height: 1.15;
}
.hero-sub {
  margin: 0;
  font-size: 14px;
  color: #64748B;
  max-width: 620px;
}
.hero-right { display: flex; align-items: center; }

/* ═══ KPIs ═══ */
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
  box-shadow: 0 1px 2px rgba(15, 23, 42, .03), 0 1px 3px rgba(15, 23, 42, .02);
  transition: border-color .2s ease, box-shadow .25s ease, transform .2s ease;
}
.kpi:hover {
  border-color: #CBD5E1;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px -2px rgba(15, 23, 42, .06), 0 12px 24px -4px rgba(15, 23, 42, .08);
}
.kpi.is-warn { border-color: #FECACA; }
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
.kpi-value.is-warn { color: #DC2626; }
.kpi-label {
  margin: 3px 0 0;
  font-size: 11.5px;
  color: #64748B;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.kpi-badge {
  display: inline-block;
  background: #FEF2F2;
  color: #DC2626;
  border: 1px solid #FECACA;
  padding: 1px 7px;
  border-radius: 20px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .2px;
}
.kpi-secondary {
  font-weight: 500;
  color: #94A3B8;
}

/* ═══ DESTACADO ═══ */
.destacado {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 24px;
  align-items: center;
  padding: 24px 28px;
  background: linear-gradient(135deg, #FEF2F2 0%, #FFFFFF 55%);
  border: 1px solid #FECACA;
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(220, 38, 38, .04), 0 8px 24px -12px rgba(220, 38, 38, .2);
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
  background: radial-gradient(circle, rgba(220, 38, 38, .06) 0%, transparent 70%);
  pointer-events: none;
}
.destacado-info { min-width: 0; position: relative; z-index: 1; }
.destacado-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #DC2626;
  margin-bottom: 10px;
}
.destacado-nombre {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.015em;
  line-height: 1.2;
}
.destacado-sku {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 12px;
  font-size: 12.5px;
  color: #64748B;
}
.destacado-cat {
  font-weight: 600;
}
.destacado-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  font-size: 13px;
  color: #475569;
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.meta-item strong {
  color: #0F172A;
  font-weight: 700;
}
.meta-item.is-critical {
  color: #DC2626;
  font-weight: 600;
}
.meta-item.is-critical strong {
  color: #DC2626;
}
.destacado-accion {
  position: relative;
  z-index: 1;
}
.btn-accion-grande {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 14px 24px;
  background: #DC2626;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 14.5px;
  font-weight: 700;
  cursor: pointer;
  text-decoration: none;
  transition: all .2s ease;
  white-space: nowrap;
  box-shadow: 0 4px 12px -2px rgba(220, 38, 38, .3);
}
.btn-accion-grande:hover {
  background: #B91C1C;
  transform: translateY(-2px);
  box-shadow: 0 12px 24px -8px rgba(220, 38, 38, .4);
}

/* ═══ GRID CONTENIDO ═══ */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: start;
}

/* ═══ CARDS BASE ═══ */
.card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, .03), 0 4px 12px -2px rgba(15, 23, 42, .04);
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
}
.card-header h3 {
  margin: 0;
  font-size: 15.5px;
  font-weight: 700;
  color: #0F172A;
}
.card-sub {
  margin: 3px 0 0;
  font-size: 12.5px;
  color: #64748B;
}
.card-body { padding: 18px 20px 20px; }
.btn-link {
  background: none;
  border: none;
  padding: 0;
  color: #0F766E;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  text-decoration: none;
  white-space: nowrap;
}
.btn-link:hover { text-decoration: underline; }

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
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  text-decoration: none;
  white-space: nowrap;
}
.btn-primary:hover {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}

/* ═══ ALERTAS LIST ═══ */
.alertas-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.alerta-item {
  display: grid;
  grid-template-columns: auto 1fr auto auto;
  gap: 12px;
  align-items: center;
  padding: 12px 14px;
  border-radius: 10px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  text-decoration: none;
  color: inherit;
  transition: all .2s ease;
}
.alerta-item:hover {
  background: #fff;
  border-color: #CBD5E1;
  transform: translateX(2px);
}
.alerta-item.is-critical {
  background: #FEF2F2;
  border-color: #FECACA;
}
.alerta-item.is-low {
  background: #FFFBEB;
  border-color: #FDE68A;
}

.alerta-avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #0F766E;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 6px -2px rgba(15, 23, 42, .15);
}
.alerta-avatar[data-cat="Medicamento"] { background: #3B82F6; }
.alerta-avatar[data-cat="Alimento"] { background: #F59E0B; }
.alerta-avatar[data-cat="Accesorio"] { background: #8B5CF6; }

.alerta-info { min-width: 0; }
.alerta-nombre {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.alerta-meta {
  margin: 2px 0 0;
  font-size: 11.5px;
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sku-mono-sm {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  color: #94A3B8;
  font-size: 11px;
}

.alerta-stock {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 1px;
  flex-shrink: 0;
}
.alerta-num {
  font-size: 15px;
  font-weight: 700;
  line-height: 1;
  color: #0F766E;
}
.alerta-num.is-warning { color: #D97706; }
.alerta-num.is-low { color: #DC2626; }
.alerta-num.is-critical { color: #991B1B; }
.alerta-min {
  font-size: 10.5px;
  color: #94A3B8;
  font-weight: 600;
}

.alerta-badge {
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .3px;
  white-space: nowrap;
  flex-shrink: 0;
  border: 1px solid;
}
.alerta-badge.is-warning {
  background: #FFFBEB;
  color: #D97706;
  border-color: #FDE68A;
}
.alerta-badge.is-low {
  background: #FEF2F2;
  color: #DC2626;
  border-color: #FECACA;
}
.alerta-badge.is-critical {
  background: #991B1B;
  color: #fff;
  border-color: #991B1B;
}

.more-link {
  display: block;
  margin-top: 6px;
  padding: 8px;
  text-align: center;
  font-size: 12.5px;
  color: #0F766E;
  font-weight: 600;
  text-decoration: none;
  border-radius: 8px;
  transition: background-color .15s;
}
.more-link:hover { background: #F0FDFA; }

/* ═══ MOVIMIENTOS ═══ */
.movimientos-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.movimiento-item {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  padding: 10px 12px;
  border-radius: 10px;
  transition: background-color .15s;
}
.movimiento-item:hover { background: #F8FAFC; }
.mov-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.mov-icon.is-entrada {
  background: #ECFDF5;
  color: #059669;
}
.mov-icon.is-salida {
  background: #FEF2F2;
  color: #DC2626;
}
.mov-icon.is-ajuste {
  background: #EFF6FF;
  color: #2563EB;
}
.mov-icon.is-vencimiento {
  background: #FFFBEB;
  color: #D97706;
}
.mov-icon.is-default {
  background: #F1F5F9;
  color: #64748B;
}

.mov-info { min-width: 0; }
.mov-nombre {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.mov-meta {
  margin: 2px 0 0;
  font-size: 11.5px;
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.mov-tipo { font-weight: 700; color: #475569; }
.mov-doc {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 10.5px;
  color: #94A3B8;
}

.mov-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}
.mov-cantidad {
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
}
.mov-cantidad.is-entrada { color: #059669; }
.mov-cantidad.is-salida { color: #DC2626; }
.mov-cantidad.is-ajuste { color: #2563EB; }
.mov-cantidad.is-vencimiento { color: #D97706; }
.mov-fecha {
  font-size: 10.5px;
  color: #94A3B8;
  font-weight: 600;
}

/* ═══ EMPTY STATE ═══ */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 40px 20px;
  color: #94A3B8;
  text-align: center;
  font-size: 13px;
}
.empty-state.slim { padding: 24px 16px; }
.empty-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #F0FDFA;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}
.empty-state p {
  margin: 0;
  max-width: 380px;
  line-height: 1.55;
  color: #64748B;
}

/* ═══ LOADING ═══ */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 80px 24px;
  color: #64748B;
  font-size: 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
}
.spin {
  width: 32px;
  height: 32px;
  border: 3px solid #E2E8F0;
  border-top-color: #0F766E;
  border-radius: 50%;
  animation: spin .8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
  .content-grid { grid-template-columns: 1fr; }
  .destacado { grid-template-columns: 1fr; gap: 18px; }
  .destacado-accion { width: 100%; }
  .btn-accion-grande { width: 100%; justify-content: center; }
}

@media (max-width: 768px) {
  .dashboard-almacen { padding: 16px 16px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero h1 { font-size: 22px; }
  .hero-right { width: 100%; }
  .hero-right .btn-primary { width: 100%; justify-content: center; }

  .kpis { grid-template-columns: 1fr; gap: 10px; }
  .kpi { padding: 14px 16px; }

  .destacado { padding: 20px; border-radius: 14px; }
  .destacado-nombre { font-size: 18px; }

  .card-header { padding: 16px 18px; }
  .card-body { padding: 14px 16px 16px; }

  .alerta-item {
    grid-template-columns: auto 1fr auto;
    gap: 10px;
  }
  .alerta-badge { grid-column: 2 / -1; justify-self: flex-start; margin-top: 2px; }
}
</style>