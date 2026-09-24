<template>
  <div class="inventory-view">
    <ToastContainer />

    <!-- ═══ HERO ═══ -->
    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow">
          <Package :size="12" />
          Almacén · Inventario
        </p>
        <h1>Gestión de Inventario</h1>
        <p class="hero-sub">
          Controla el stock de medicamentos e insumos, y registra la entrada de mercancía.
        </p>
      </div>
      <div class="hero-right">
        <button class="btn-secondary" type="button" @click="abrirModalProducto(null)">
          <Plus :size="15" /> Nuevo producto
        </button>
        <button class="btn-primary" type="button" @click="irARegistrarEntrada">
          <ArrowDownToLine :size="15" /> Registrar entrada
        </button>
      </div>
    </header>

    <!-- ═══ KPIs ═══ -->
    <section class="kpis">
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #0F766E; --kpi-bg: #F0FDFA;">
          <Package :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ stats.totalProductos }}</p>
          <p class="kpi-label">Productos activos</p>
        </div>
      </article>
      <article class="kpi" :class="{ 'is-warn': stats.alertas > 0 }">
        <div class="kpi-icon" :style="stats.alertas > 0 ? '--kpi-color: #DC2626; --kpi-bg: #FEF2F2;' : '--kpi-color: #F59E0B; --kpi-bg: #FFFBEB;'">
          <AlertTriangle :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value" :class="{ 'is-warn': stats.alertas > 0 }">{{ stats.alertas }}</p>
          <p class="kpi-label">Alertas de stock</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #3B82F6; --kpi-bg: #EFF6FF;">
          <DollarSign :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ fmtUsd(stats.valorInventario) }}</p>
          <p class="kpi-label">Valor inventario</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #8B5CF6; --kpi-bg: #F5F3FF;">
          <Layers :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ stats.categorias }}</p>
          <p class="kpi-label">Categorías</p>
        </div>
      </article>
    </section>

    <!-- ═══ TOOLBAR ═══ -->
    <section class="toolbar">
      <div class="search-box">
        <Search :size="15" class="search-icon" />
        <input
          v-model="filtro"
          type="text"
          class="search-input"
          placeholder="Buscar por nombre o SKU…"
        />
        <button v-if="filtro" type="button" class="search-clear" @click="filtro = ''">
          <X :size="13" />
        </button>
      </div>

      <div class="chips-row">
        <button
          type="button"
          class="chip"
          :class="{ active: !categoriaFiltro }"
          @click="categoriaFiltro = ''"
        >
          Todas
        </button>
        <button
          v-for="cat in categoriasDisponibles"
          :key="cat"
          type="button"
          class="chip"
          :class="{ active: categoriaFiltro === cat }"
          @click="categoriaFiltro = categoriaFiltro === cat ? '' : cat"
        >
          {{ cat }}
        </button>
      </div>

      <button
        type="button"
        class="toggle-alertas"
        :class="{ active: soloAlertas }"
        @click="soloAlertas = !soloAlertas"
      >
        <AlertTriangle :size="14" />
        Solo alertas
        <span v-if="stats.alertas > 0" class="toggle-count">{{ stats.alertas }}</span>
      </button>
    </section>

    <!-- ═══ ERROR ═══ -->
    <div v-if="error" class="alert alert-error">
      <AlertCircle :size="16" />
      <span>{{ error }}</span>
      <button type="button" class="alert-action" @click="cargar">Reintentar</button>
    </div>

    <!-- ═══ LOADING ═══ -->
    <div v-if="cargando" class="skeleton-table">
      <div v-for="i in 6" :key="i" class="skeleton-row">
        <div class="skeleton-cell w-15" />
        <div class="skeleton-cell w-30" />
        <div class="skeleton-cell w-15" />
        <div class="skeleton-cell w-15" />
        <div class="skeleton-cell w-10" />
      </div>
    </div>

    <!-- ═══ VACÍO ═══ -->
    <div v-else-if="!productosFiltrados.length" class="empty-state">
      <div class="empty-icon">
        <component :is="soloAlertas || filtro || categoriaFiltro ? Search : Package" :size="32" />
      </div>
      <h3 v-if="soloAlertas">Sin alertas de stock</h3>
      <h3 v-else-if="filtro || categoriaFiltro">Sin resultados</h3>
      <h3 v-else>Sin productos registrados</h3>
      <p v-if="soloAlertas">Todos los productos están por encima de su stock mínimo.</p>
      <p v-else-if="filtro || categoriaFiltro">Prueba con otros criterios de búsqueda.</p>
      <p v-else>Comienza registrando tu primer producto en el catálogo.</p>
      <button v-if="!soloAlertas && !filtro && !categoriaFiltro" class="btn-primary" type="button" @click="abrirModalProducto(null)">
        <Plus :size="15" /> Nuevo producto
      </button>
      <button v-else type="button" class="btn-link" @click="limpiarFiltros">Limpiar filtros</button>
    </div>

    <!-- ═══ TABLA ═══ -->
    <div v-else class="table-card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>SKU</th>
              <th>Producto</th>
              <th>Categoría</th>
              <th class="der">Stock</th>
              <th class="der">Precio</th>
              <th class="centro">Receta</th>
              <th class="der">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in productosFiltrados" :key="p.id" class="table-row">
              <td><span class="sku-pill">{{ p.codigoSku }}</span></td>
              <td>
                <div class="producto-cell">
                  <p class="producto-nombre">{{ p.nombre }}</p>
                  <p v-if="p.descripcion" class="producto-desc">{{ p.descripcion }}</p>
                </div>
              </td>
              <td>
                <span class="categoria-pill" :data-cat="p.categoria">
                  {{ p.categoria }}
                </span>
              </td>
              <td class="der">
                <div class="stock-cell">
                  <span class="stock-num" :class="stockClass(p)">{{ p.stockActual }}</span>
                  <div class="stock-bar">
                    <div
                      class="stock-fill"
                      :class="stockClass(p)"
                      :style="{ width: stockPct(p) + '%' }"
                    />
                  </div>
                  <span class="stock-min">mín {{ p.stockMinimo }}</span>
                </div>
              </td>
              <td class="der">
                <span class="precio">{{ fmtUsd(p.precioVenta) }}</span>
              </td>
              <td class="centro">
                <span v-if="p.requiereReceta" class="receta-badge" title="Requiere receta">
                  <Pill :size="12" />
                </span>
                <span v-else class="sin-receta">—</span>
              </td>
              <td class="der">
                <div class="acciones">
                  <button class="icon-action" type="button" title="Editar" @click="abrirModalProducto(p)">
                    <Edit2 :size="14" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <footer class="table-footer">
        <span>{{ productosFiltrados.length }} {{ productosFiltrados.length === 1 ? 'producto' : 'productos' }}</span>
      </footer>
    </div>

    <!-- ═══ MODAL CREAR/EDITAR PRODUCTO ═══ -->
    <ProductFormModal
      :visible="modalProductoVisible"
      :producto="productoEditando"
      :categorias="categoriasDisponibles"
      :proveedores="proveedores"
      @close="cerrarModalProducto"
      @saved="onProductoGuardado"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Package, Plus, Search, X, AlertTriangle, AlertCircle,
  ArrowDownToLine, DollarSign, Layers, Pill, Edit2
} from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import ProductFormModal from '@/components/almacen/ProductFormModal.vue'
import { getProductos, getProveedores } from '@/api/almacen.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { toastSuccess, toastError } = useToast()

// ─── Estado ───
const cargando = ref(false)
const error = ref('')
const productos = ref([])
const proveedores = ref([])
const filtro = ref('')
const categoriaFiltro = ref('')
const soloAlertas = ref(false)

// ─── Modal ───
const modalProductoVisible = ref(false)
const productoEditando = ref(null)

// ─── Computeds ───
const categoriasDisponibles = computed(() => {
  const set = new Set()
  for (const p of productos.value) if (p.categoria) set.add(p.categoria)
  return Array.from(set).sort()
})

const productosFiltrados = computed(() => {
  let items = productos.value
  const q = filtro.value.trim().toLowerCase()

  if (q) {
    items = items.filter((p) =>
      p.nombre.toLowerCase().includes(q) ||
      p.codigoSku.toLowerCase().includes(q)
    )
  }
  if (categoriaFiltro.value) {
    items = items.filter((p) => p.categoria === categoriaFiltro.value)
  }
  if (soloAlertas.value) {
    items = items.filter((p) => p.stockActual <= p.stockMinimo)
  }

  return items.sort((a, b) => a.nombre.localeCompare(b.nombre))
})

const stats = computed(() => {
  const total = productos.value.length
  const alertas = productos.value.filter((p) => p.stockActual <= p.stockMinimo).length
  const valor = productos.value.reduce(
    (sum, p) => sum + (p.stockActual * p.costoAdquisicion || 0),
    0
  )
  const cats = new Set(productos.value.map((p) => p.categoria).filter(Boolean)).size
  return { totalProductos: total, alertas, valorInventario: valor, categorias: cats }
})

// ─── Helpers ───
function fmtUsd(v) {
  if (v == null) return '—'
  return `$${Number(v).toFixed(2)}`
}

function stockClass(p) {
  if (p.stockActual === 0) return 'is-empty'
  if (p.stockActual <= p.stockMinimo) return 'is-low'
  if (p.stockMinimo && p.stockActual <= p.stockMinimo * 1.5) return 'is-warning'
  return 'is-ok'
}

function stockPct(p) {
  const max = Math.max(p.stockMaximo || p.stockMinimo * 3, p.stockActual)
  return Math.min(100, (p.stockActual / max) * 100)
}

// ─── Carga ───
async function cargar() {
  cargando.value = true
  error.value = ''
  try {
    const [prodRes, provRes] = await Promise.all([
      getProductos(),
      getProveedores(),
    ])
    productos.value = prodRes.data || []
    proveedores.value = provRes.data || []
  } catch (err) {
    error.value = getApiErrorMessage(err)
    toastError(error.value)
  } finally {
    cargando.value = false
  }
}

// ─── Acciones ───
function limpiarFiltros() {
  filtro.value = ''
  categoriaFiltro.value = ''
  soloAlertas.value = false
}

function irARegistrarEntrada() {
  router.push('/almacen/entrada')
}

function abrirModalProducto(producto) {
  productoEditando.value = producto
  modalProductoVisible.value = true
}

function cerrarModalProducto() {
  modalProductoVisible.value = false
  productoEditando.value = null
}

async function onProductoGuardado(producto) {
  toastSuccess(
    productoEditando.value
      ? 'Producto actualizado correctamente'
      : 'Producto registrado correctamente'
  )
  cerrarModalProducto()
  await cargar()
}

onMounted(cargar)
</script>

<style scoped>
.inventory-view {
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
.hero-right { display: flex; gap: 10px; flex-wrap: wrap; }

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
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03), 0 1px 3px rgba(15, 23, 42, 0.02);
  transition: border-color .2s, box-shadow .25s, transform .2s;
}
.kpi:hover {
  border-color: #CBD5E1;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px -2px rgba(15, 23, 42, 0.06), 0 12px 24px -4px rgba(15, 23, 42, 0.08);
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
}

/* ═══ TOOLBAR ═══ */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding: 14px 18px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03);
}
.search-box {
  position: relative;
  flex: 1 1 280px;
  max-width: 420px;
}
.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #94A3B8;
  pointer-events: none;
}
.search-input {
  width: 100%;
  padding: 10px 40px 10px 40px;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  font-size: 13.5px;
  color: #0F172A;
  background: #fff;
  font-family: inherit;
  outline: none;
  box-sizing: border-box;
  transition: all .2s;
}
.search-input:focus {
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
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
  padding: 5px;
  border-radius: 6px;
}
.search-clear:hover { color: #475569; background: #F1F5F9; }

.chips-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.chip {
  padding: 6px 13px;
  border: 1px solid #E2E8F0;
  background: #fff;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition: all .2s;
}
.chip:hover { background: #F0FDFA; border-color: #99F6E4; color: #0F766E; }
.chip.active {
  background: #0F766E;
  color: #fff;
  border-color: #0F766E;
  box-shadow: 0 2px 8px rgba(15, 118, 110, .25);
}

.toggle-alertas {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: 1.5px solid #E2E8F0;
  background: #fff;
  border-radius: 10px;
  font-size: 12.5px;
  font-weight: 700;
  color: #64748B;
  cursor: pointer;
  transition: all .2s;
  margin-left: auto;
}
.toggle-alertas:hover { border-color: #FDE68A; color: #D97706; }
.toggle-alertas.active {
  background: #FFFBEB;
  border-color: #F59E0B;
  color: #D97706;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, .1);
}
.toggle-count {
  background: #DC2626;
  color: #fff;
  font-size: 10.5px;
  padding: 1px 7px;
  border-radius: 20px;
  font-weight: 700;
}

/* ═══ BOTONES ═══ */
.btn-primary,
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s;
  font-family: inherit;
  white-space: nowrap;
}
.btn-primary {
  background: #0F766E;
  color: #fff;
  border: none;
}
.btn-primary:hover {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-secondary {
  background: #fff;
  color: #475569;
  border: 1px solid #E2E8F0;
}
.btn-secondary:hover {
  background: #F8FAFC;
  border-color: #CBD5E1;
  color: #0F766E;
}
.btn-link {
  background: none;
  border: none;
  color: #0F766E;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}
.btn-link:hover { text-decoration: underline; }

/* ═══ ALERTA ═══ */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  font-weight: 500;
}
.alert-error { background: #FEF2F2; color: #991B1B; border: 1px solid #FECACA; }
.alert-action {
  margin-left: auto;
  background: none;
  border: none;
  color: inherit;
  font-weight: 700;
  font-size: 12px;
  cursor: pointer;
  text-decoration: underline;
}

/* ═══ SKELETON ═══ */
.skeleton-table {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  overflow: hidden;
}
.skeleton-row {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr 1fr 1fr;
  gap: 20px;
  padding: 18px 20px;
  border-bottom: 1px solid #F1F5F9;
}
.skeleton-row:last-child { border-bottom: none; }
.skeleton-cell {
  height: 14px;
  border-radius: 6px;
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
.w-10 { width: 40%; }
.w-15 { width: 60%; }
.w-30 { width: 90%; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ═══ EMPTY ═══ */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 80px 24px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  text-align: center;
}
.empty-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #F0FDFA 0%, #ECFDF5 100%);
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}
.empty-state h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0F172A;
}
.empty-state p {
  margin: 0 0 8px;
  font-size: 13.5px;
  color: #64748B;
  max-width: 420px;
  line-height: 1.55;
}

/* ═══ TABLA ═══ */
.table-card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.03), 0 4px 12px -2px rgba(15, 23, 42, 0.04);
}
.table-wrap { overflow-x: auto; }
.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}
.data-table thead { background: #F8FAFC; }
.data-table th {
  text-align: left;
  padding: 14px 18px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #64748B;
  border-bottom: 1px solid #E2E8F0;
  white-space: nowrap;
}
.data-table td {
  padding: 16px 18px;
  font-size: 13.5px;
  color: #0F172A;
  border-bottom: 1px solid #F1F5F9;
  vertical-align: middle;
}
.table-row { transition: background-color .15s; }
.table-row:hover { background: #FAFBFC; }
.table-row:last-child td { border-bottom: none; }
.der { text-align: right; }
.centro { text-align: center; }

.sku-pill {
  display: inline-block;
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 11.5px;
  font-weight: 700;
  color: #334155;
  background: #F1F5F9;
  padding: 3px 10px;
  border-radius: 6px;
  border: 1px solid #E2E8F0;
}

.producto-cell { min-width: 200px; }
.producto-nombre {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.3;
}
.producto-desc {
  margin: 3px 0 0;
  font-size: 11.5px;
  color: #94A3B8;
  line-height: 1.4;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.categoria-pill {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
  background: #F1F5F9;
  color: #475569;
  border: 1px solid #E2E8F0;
}
.categoria-pill[data-cat="Medicamento"] {
  background: #EFF6FF;
  color: #2563EB;
  border-color: #BFDBFE;
}
.categoria-pill[data-cat="Alimento"] {
  background: #FFFBEB;
  color: #D97706;
  border-color: #FDE68A;
}
.categoria-pill[data-cat="Accesorio"] {
  background: #F5F3FF;
  color: #7C3AED;
  border-color: #DDD6FE;
}

.stock-cell {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  min-width: 100px;
}
.stock-num {
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
}
.stock-num.is-ok { color: #0F766E; }
.stock-num.is-warning { color: #D97706; }
.stock-num.is-low { color: #DC2626; }
.stock-num.is-empty { color: #991B1B; }

.stock-bar {
  width: 70px;
  height: 4px;
  background: #E2E8F0;
  border-radius: 2px;
  overflow: hidden;
}
.stock-fill {
  height: 100%;
  border-radius: 2px;
  transition: width .3s ease;
}
.stock-fill.is-ok { background: #10B981; }
.stock-fill.is-warning { background: #F59E0B; }
.stock-fill.is-low { background: #EF4444; }
.stock-fill.is-empty { background: #991B1B; }

.stock-min {
  font-size: 10.5px;
  color: #94A3B8;
  font-weight: 600;
}

.precio {
  font-size: 13.5px;
  font-weight: 700;
  color: #0F766E;
}

.receta-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 8px;
  background: #FFFBEB;
  color: #92400E;
  border: 1px solid #FDE68A;
}
.sin-receta { color: #CBD5E1; }

.acciones {
  display: flex;
  gap: 6px;
  justify-content: flex-end;
}
.icon-action {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid #E2E8F0;
  background: #fff;
  color: #64748B;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .2s;
}
.icon-action:hover {
  background: #F0FDFA;
  border-color: #99F6E4;
  color: #0F766E;
}

.table-footer {
  padding: 12px 20px;
  border-top: 1px solid #E2E8F0;
  background: #FAFBFC;
  font-size: 12px;
  font-weight: 600;
  color: #64748B;
}

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .inventory-view { padding: 16px 16px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero h1 { font-size: 22px; }
  .hero-right { width: 100%; flex-direction: column; }
  .hero-right .btn-primary,
  .hero-right .btn-secondary { width: 100%; }
  .kpis { grid-template-columns: 1fr; gap: 10px; }
  .toolbar { padding: 12px; }
  .toggle-alertas { margin-left: 0; width: 100%; justify-content: center; }
}
</style>