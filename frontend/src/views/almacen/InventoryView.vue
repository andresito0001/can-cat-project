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
        <h1>Inventario</h1>
        <p class="hero-sub">
          Catálogo completo de productos, alertas de stock y control de existencias.
        </p>
      </div>
      <div class="hero-actions">
        <button class="btn-secondary" type="button" @click="abrirModalProducto(null)">
          <PackagePlus :size="16" />
          Nuevo producto
        </button>
        <button class="btn-primary" type="button" @click="irARegistrarEntrada">
          <ArrowDownToLine :size="16" />
          Registrar entrada
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
        <div
          class="kpi-icon"
          :style="stats.alertas > 0
            ? '--kpi-color: #DC2626; --kpi-bg: #FEF2F2;'
            : '--kpi-color: #94A3B8; --kpi-bg: #F1F5F9;'"
        >
          <AlertTriangle :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value" :class="{ 'is-warn': stats.alertas > 0 }">{{ stats.alertas }}</p>
          <p class="kpi-label">{{ stats.alertas > 0 ? 'Requieren reposición' : 'Alertas de stock' }}</p>
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

    <!-- ═══ PANEL DE CONTROL: TABS + BÚSQUEDA + FILTROS ═══ -->
    <section class="control-panel">
      <nav class="tabs" role="tablist">
        <button
          v-for="t in tabsConfig"
          :key="t.id"
          type="button"
          role="tab"
          class="tab"
          :class="{ active: tab === t.id, 'is-warn': t.id === 'alertas' && stats.alertas > 0 }"
          :aria-selected="tab === t.id"
          @click="tab = t.id"
        >
          <component :is="t.icono" :size="14" />
          {{ t.label }}
          <span
            v-if="t.contador > 0 || t.id === 'todos'"
            class="tab-count"
            :class="{ 'is-warn': t.id === 'alertas' }"
          >
            {{ t.contador }}
          </span>
        </button>
      </nav>

      <div class="toolbar">
        <div class="search-box">
          <Search :size="15" class="search-icon" />
          <input
            ref="searchInputRef"
            v-model="filtro"
            type="text"
            class="search-input"
            placeholder="Buscar por nombre o SKU…"
          />
          <button
            v-if="filtro"
            type="button"
            class="search-clear"
            aria-label="Limpiar búsqueda"
            @click="filtro = ''"
          >
            <X :size="13" />
          </button>
          <kbd v-else class="search-kbd">/</kbd>
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
            v-for="cat in categoriasFiltro"
            :key="cat.nombre"
            type="button"
            class="chip"
            :class="{ active: categoriaFiltro === cat.nombre }"
            @click="categoriaFiltro = categoriaFiltro === cat.nombre ? '' : cat.nombre"
          >
            {{ cat.nombre }}
            <span class="chip-count">{{ cat.total }}</span>
          </button>
        </div>
      </div>
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
    <div v-else-if="!error && !productosFiltrados.length" class="empty-state">
      <div class="empty-icon">
        <component :is="iconoVacio" :size="32" />
      </div>
      <h3>{{ tituloVacio }}</h3>
      <p>{{ descripcionVacio }}</p>
      <div class="empty-actions">
        <button
          v-if="tieneFiltros"
          type="button"
          class="btn-secondary"
          @click="limpiarFiltros"
        >
          <X :size="15" /> Limpiar filtros
        </button>
        <button
          v-else-if="tab === 'todos'"
          class="btn-primary"
          type="button"
          @click="abrirModalProducto(null)"
        >
          <PackagePlus :size="15" /> Nuevo producto
        </button>
      </div>
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
            <tr
              v-for="p in productosFiltrados"
              :key="p.id"
              class="table-row"
              :class="{ 'is-inactivo': !p.activo }"
            >
              <td>
                <span class="sku-pill">{{ p.codigoSku }}</span>
              </td>
              <td>
                <div class="producto-cell">
                  <p class="producto-nombre">
                    <span class="estado-dot" :class="p.activo ? 'is-active' : 'is-inactive'" />
                    {{ p.nombre }}
                    <span v-if="!p.activo" class="inactivo-tag">Inactivo</span>
                  </p>
                  <p v-if="p.descripcion" class="producto-desc">{{ p.descripcion }}</p>
                </div>
              </td>
              <td>
                <span class="categoria-pill" :data-cat="p.tipoCategoria">
                  {{ p.tipoCategoria }}
                </span>
              </td>
              <td class="der">
                <div class="stock-cell">
                  <div class="stock-top">
                    <span class="stock-num" :class="stockClass(p)">{{ p.stockActual }}</span>
                    <span class="stock-min">/ mín {{ p.stockMinimo }}</span>
                  </div>
                  <div class="stock-bar">
                    <div
                      class="stock-fill"
                      :class="stockClass(p)"
                      :style="{ width: stockPct(p) + '%' }"
                    />
                  </div>
                </div>
              </td>
              <td class="der">
                <div class="precio-cell">
                  <span class="precio-principal">{{ fmtUsd(p.precioUsd) }}</span>
                  <span v-if="p.costoAdquisicion" class="precio-costo">
                    costo {{ fmtUsd(p.costoAdquisicion) }}
                  </span>
                </div>
              </td>
              <td class="centro">
                <span v-if="p.requiereReceta" class="receta-badge" title="Requiere receta">
                  <Pill :size="12" />
                </span>
                <span v-else class="sin-receta">—</span>
              </td>
              <td class="der">
                <div class="row-actions">
                  <button
                    class="action-btn"
                    type="button"
                    title="Editar producto"
                    @click="abrirModalProducto(p)"
                  >
                    <Pencil :size="15" />
                  </button>
                  <button
                    class="action-btn"
                    :class="p.activo ? 'is-danger' : 'is-success'"
                    type="button"
                    :title="p.activo ? 'Desactivar producto' : 'Activar producto'"
                    @click="pedirConfirmacionCambioEstado(p)"
                  >
                    <component :is="p.activo ? Archive : RotateCcw" :size="15" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <footer class="table-footer">
        <div class="footer-info">
          <span class="footer-count">
            {{ productosFiltrados.length }}
            {{ productosFiltrados.length === 1 ? 'producto' : 'productos' }}
          </span>
          <span v-if="alertasEnLista > 0" class="footer-alert">
            <AlertTriangle :size="12" />
            {{ alertasEnLista }} {{ alertasEnLista === 1 ? 'producto necesita' : 'productos necesitan' }} reposición
          </span>
        </div>
        <button
          v-if="tieneFiltros"
          type="button"
          class="footer-clear"
          @click="limpiarFiltros"
        >
          <X :size="12" /> Limpiar filtros
        </button>
      </footer>
    </div>

    <!-- ═══ MODAL PRODUCTO ═══ -->
    <ProductFormModal
      :visible="modalProductoVisible"
      :producto="productoEditando"
      :categorias="categoriasDisponibles"
      :proveedores="proveedores"
      @close="cerrarModalProducto"
      @saved="onProductoGuardado"
    />

    <!-- ═══ MODAL CONFIRMACIÓN DE ESTADO ═══ -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="confirmacion.visible"
          class="modal-overlay"
          @click.self="cerrarConfirmacion"
        >
          <Transition name="pop">
            <div
              v-if="confirmacion.visible"
              class="confirm-modal"
              role="dialog"
              aria-modal="true"
              :aria-label="confirmacion.activar ? 'Activar producto' : 'Desactivar producto'"
            >
              <div class="confirm-icon" :class="confirmacion.activar ? 'is-success' : 'is-danger'">
                <RotateCcw v-if="confirmacion.activar" :size="22" />
                <Archive v-else :size="22" />
              </div>

              <h3 class="confirm-title">
                {{ confirmacion.activar ? 'Activar producto' : 'Desactivar producto' }}
              </h3>

              <p class="confirm-texto">
                <template v-if="confirmacion.activar">
                  El producto <strong>«{{ confirmacion.producto?.nombre }}»</strong> volverá a
                  estar disponible en el catálogo y podrá usarse en nuevas entradas y atenciones.
                </template>
                <template v-else>
                  El producto <strong>«{{ confirmacion.producto?.nombre }}»</strong> dejará de
                  estar disponible para nuevos registros. Su historial y stock actual se conservan.
                </template>
              </p>

              <div v-if="confirmacion.producto" class="confirm-meta">
                <div class="meta-item">
                  <span class="meta-label">SKU</span>
                  <span class="meta-valor mono">{{ confirmacion.producto.codigoSku }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">Stock actual</span>
                  <span class="meta-valor">{{ confirmacion.producto.stockActual }}</span>
                </div>
                <div class="meta-item">
                  <span class="meta-label">Estado</span>
                  <span class="meta-valor">{{ confirmacion.producto.activo ? 'Activo' : 'Inactivo' }}</span>
                </div>
              </div>

              <div class="confirm-actions">
                <button
                  class="btn-secondary"
                  type="button"
                  :disabled="confirmacion.procesando"
                  @click="cerrarConfirmacion"
                >
                  Cancelar
                </button>
                <button
                  class="btn-confirm"
                  :class="confirmacion.activar ? 'is-success' : 'is-danger'"
                  type="button"
                  :disabled="confirmacion.procesando"
                  @click="confirmarCambioEstado"
                >
                  <Loader2 v-if="confirmacion.procesando" :size="15" class="spin" />
                  <component
                    :is="confirmacion.activar ? RotateCcw : Archive"
                    v-else
                    :size="15"
                  />
                  {{ confirmacion.procesando
                    ? 'Procesando…'
                    : (confirmacion.activar ? 'Sí, activar' : 'Sí, desactivar') }}
                </button>
              </div>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Package, PackagePlus, Search, X, AlertTriangle, AlertCircle,
  ArrowDownToLine, DollarSign, Layers, Pill, Pencil, Archive, RotateCcw,
  CheckCircle2, Loader2,
} from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import ProductFormModal from '@/components/almacen/ProductFormModal.vue'
import {
  getProductos, getProveedores, cambiarEstadoProducto,
} from '@/api/almacen.api'
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
const tab = ref('todos')
const searchInputRef = ref(null)

// ─── Modal producto ───
const modalProductoVisible = ref(false)
const productoEditando = ref(null)

// ─── Modal confirmación de estado ───
const confirmacion = ref({
  visible: false,
  producto: null,
  activar: false,
  procesando: false,
})

// ─── Categorías ───
const categoriasDisponibles = computed(() => {
  const set = new Set()
  for (const p of productos.value) {
    if (p.tipoCategoria) set.add(p.tipoCategoria)
  }
  return Array.from(set).sort()
})

const categoriasFiltro = computed(() => {
  const map = new Map()
  for (const p of productos.value) {
    if (p.activo && p.tipoCategoria) {
      map.set(p.tipoCategoria, (map.get(p.tipoCategoria) || 0) + 1)
    }
  }
  return Array.from(map.entries())
    .map(([nombre, total]) => ({ nombre, total }))
    .sort((a, b) => a.nombre.localeCompare(b.nombre))
})

// ─── Filtro principal ───
const productosFiltrados = computed(() => {
  let items = productos.value
  const q = filtro.value.trim().toLowerCase()

  switch (tab.value) {
    case 'todos':
    default:
      // sin filtro de activo
      break
    case 'alertas':
      items = items.filter((p) => p.activo && p.stockActual <= p.stockMinimo)
      break
    case 'receta':
      items = items.filter((p) => p.activo && p.requiereReceta)
      break
    case 'inactivos':
      items = items.filter((p) => !p.activo)
      break
  }

  if (q) {
    items = items.filter((p) =>
      (p.nombre || '').toLowerCase().includes(q) ||
      (p.codigoSku || '').toLowerCase().includes(q)
    )
  }
  if (categoriaFiltro.value) {
    items = items.filter((p) => p.tipoCategoria === categoriaFiltro.value)
  }

  return items.sort((a, b) => (a.nombre || '').localeCompare(b.nombre || ''))
})

// ─── Stats ───
const stats = computed(() => {
  const activos = productos.value.filter((p) => p.activo)
  const total = activos.length
  const alertas = activos.filter((p) => p.stockActual <= p.stockMinimo).length
  const valor = activos.reduce(
    (sum, p) => sum + p.stockActual * Number(p.costoAdquisicion || 0),
    0
  )
  const cats = new Set(activos.map((p) => p.tipoCategoria).filter(Boolean)).size
  const conReceta = activos.filter((p) => p.requiereReceta).length
  const inactivos = productos.value.filter((p) => !p.activo).length

  return { totalProductos: total, alertas, valorInventario: valor, categorias: cats, conReceta, inactivos }
})

// ─── Config de tabs ───
const tabsConfig = computed(() => [
  { id: 'todos', label: 'Todos', icono: Package, contador: stats.value.totalProductos },
  { id: 'alertas', label: 'Alertas', icono: AlertTriangle, contador: stats.value.alertas },
  { id: 'receta', label: 'Con receta', icono: Pill, contador: stats.value.conReceta },
  { id: 'inactivos', label: 'Inactivos', icono: Archive, contador: stats.value.inactivos },
])

const alertasEnLista = computed(() =>
  productosFiltrados.value.filter((p) => p.activo && p.stockActual <= p.stockMinimo).length
)

const tieneFiltros = computed(() => !!filtro.value || !!categoriaFiltro.value)

// ─── Estados vacíos ───
const iconoVacio = computed(() => {
  if (tieneFiltros.value) return Search
  switch (tab.value) {
    case 'alertas': return CheckCircle2
    case 'receta': return Pill
    case 'inactivos': return Archive
    default: return Package
  }
})

const tituloVacio = computed(() => {
  if (tieneFiltros.value) return 'Sin resultados'
  switch (tab.value) {
    case 'alertas': return 'Sin alertas de stock'
    case 'receta': return 'Sin productos que requieran receta'
    case 'inactivos': return 'Sin productos inactivos'
    default: return 'Sin productos en el catálogo'
  }
})

const descripcionVacio = computed(() => {
  if (tieneFiltros.value) return 'Prueba con otros criterios de búsqueda o cambia de categoría.'
  switch (tab.value) {
    case 'alertas': return 'Todos los productos están por encima de su stock mínimo.'
    case 'receta': return 'Ningún producto está marcado como "requiere receta".'
    case 'inactivos': return 'Todos los productos están activos.'
    default: return 'Comienza registrando tu primer producto en el catálogo.'
  }
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
  const max = Math.max(p.stockMaximo || p.stockMinimo * 3, p.stockActual, 1)
  return Math.min(100, (p.stockActual / max) * 100)
}

// ─── Carga ───
async function cargar() {
  cargando.value = true
  error.value = ''
  try {
    const [prodRes, provRes] = await Promise.all([
      getProductos({ incluirInactivos: true }),
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

async function onProductoGuardado() {
  toastSuccess(
    productoEditando.value
      ? 'Producto actualizado correctamente'
      : 'Producto registrado correctamente'
  )
  cerrarModalProducto()
  await cargar()
}

// ─── Confirmación de cambio de estado ───
function pedirConfirmacionCambioEstado(producto) {
  confirmacion.value = {
    visible: true,
    producto,
    activar: !producto.activo,
    procesando: false,
  }
}

function cerrarConfirmacion() {
  if (confirmacion.value.procesando) return
  confirmacion.value.visible = false
  confirmacion.value.producto = null
}

async function confirmarCambioEstado() {
  const { producto, activar } = confirmacion.value
  confirmacion.value.procesando = true
  try {
    await cambiarEstadoProducto(producto.id, activar)
    toastSuccess(`"${producto.nombre}" ${activar ? 'activado' : 'desactivado'} correctamente`)
    confirmacion.value.visible = false
    confirmacion.value.producto = null
    await cargar()
  } catch (err) {
    toastError(getApiErrorMessage(err))
    confirmacion.value.procesando = false
  }
}

// ─── Atajos de teclado ───
function onKeydown(e) {
  if (e.key === 'Escape' && confirmacion.value.visible && !confirmacion.value.procesando) {
    confirmacion.value.visible = false
    confirmacion.value.producto = null
    return
  }
  if (e.key === '/' && !modalProductoVisible.value && !confirmacion.value.visible) {
    const tag = document.activeElement?.tagName
    if (!['INPUT', 'TEXTAREA', 'SELECT'].includes(tag)) {
      e.preventDefault()
      searchInputRef.value?.focus()
    }
  }
}

onMounted(() => {
  cargar()
  window.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
})
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
  gap: 18px;
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
.hero-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

/* ═══ KPIs ═══ */
.kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.kpi {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  transition: border-color .2s, box-shadow .25s, transform .2s;
}
.kpi:hover {
  border-color: #CBD5E1;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -10px rgba(15, 23, 42, .08);
}
.kpi.is-warn {
  border-color: #FECACA;
  background: linear-gradient(135deg, #FFFBFB 0%, #FFFFFF 60%);
}
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

/* ═══ PANEL DE CONTROL ═══ */
.control-panel {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  overflow: hidden;
}
.tabs {
  display: flex;
  gap: 2px;
  overflow-x: auto;
  padding: 6px 12px 0;
  border-bottom: 1px solid #E2E8F0;
  scrollbar-width: none;
}
.tabs::-webkit-scrollbar { display: none; }
.tab {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 11px 15px;
  border: none;
  background: transparent;
  color: #64748B;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all .2s;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  white-space: nowrap;
}
.tab:hover { color: #0F766E; }
.tab.active {
  color: #0F766E;
  border-bottom-color: #0F766E;
}
.tab.active.is-warn {
  color: #DC2626;
  border-bottom-color: #DC2626;
}
.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 18px;
  padding: 0 6px;
  border-radius: 10px;
  background: #F1F5F9;
  color: #64748B;
  font-size: 10.5px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}
.tab.active .tab-count {
  background: #F0FDFA;
  color: #0F766E;
}
.tab-count.is-warn {
  background: #FEF2F2;
  color: #DC2626;
}
.tab.active.is-warn .tab-count {
  background: #FEF2F2;
  color: #DC2626;
}

/* ═══ TOOLBAR ═══ */
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding: 14px 16px;
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
  padding: 10px 40px;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  font-size: 13.5px;
  color: #0F172A;
  background: #F8FAFC;
  font-family: inherit;
  outline: none;
  box-sizing: border-box;
  transition: all .2s;
}
.search-input::placeholder { color: #94A3B8; }
.search-input:focus {
  border-color: #0F766E;
  background: #fff;
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
.search-kbd {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 10.5px;
  padding: 2px 7px;
  border-radius: 5px;
  background: #fff;
  border: 1px solid #E2E8F0;
  color: #94A3B8;
  font-weight: 600;
  line-height: 1.4;
  pointer-events: none;
}

.chips-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
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
.chip:hover {
  background: #F0FDFA;
  border-color: #99F6E4;
  color: #0F766E;
}
.chip.active {
  background: #0F766E;
  color: #fff;
  border-color: #0F766E;
  box-shadow: 0 2px 8px rgba(15, 118, 110, .25);
}
.chip.active .chip-count {
  background: rgba(255, 255, 255, .22);
  color: #fff;
}
.chip-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 17px;
  height: 17px;
  padding: 0 4px;
  border-radius: 9px;
  background: #F1F5F9;
  color: #64748B;
  font-size: 10px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
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
.btn-primary:active { transform: translateY(0); }
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

/* ═══ ALERTA ═══ */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
}
.alert-error {
  background: #FEF2F2;
  color: #991B1B;
  border: 1px solid #FECACA;
}
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
  padding: 72px 24px;
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
  margin: 0;
  font-size: 13.5px;
  color: #64748B;
  max-width: 420px;
  line-height: 1.55;
}
.empty-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
}

/* ═══ TABLA ═══ */
.table-card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  overflow: hidden;
}
.table-wrap { overflow-x: auto; }
.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 900px;
}
.data-table thead { background: #F8FAFC; }
.data-table th {
  text-align: left;
  padding: 12px 18px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #64748B;
  border-bottom: 1px solid #E2E8F0;
  white-space: nowrap;
}
.data-table td {
  padding: 14px 18px;
  font-size: 13.5px;
  color: #0F172A;
  border-bottom: 1px solid #F1F5F9;
  vertical-align: middle;
}
.table-row { transition: background-color .15s; }
.table-row:hover { background: #FAFDFC; }
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

.producto-cell { min-width: 220px; }
.producto-nombre {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
  display: flex;
  align-items: center;
  gap: 8px;
}
.estado-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}
.estado-dot.is-active {
  background: #10B981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, .15);
}
.estado-dot.is-inactive { background: #CBD5E1; }
.table-row.is-inactivo .producto-nombre { color: #64748B; }
.inactivo-tag {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .4px;
  color: #94A3B8;
  background: #F1F5F9;
  padding: 2px 7px;
  border-radius: 5px;
  flex-shrink: 0;
}
.producto-desc {
  margin: 3px 0 0 15px;
  font-size: 11.5px;
  color: #94A3B8;
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
  white-space: nowrap;
}
.categoria-pill[data-cat="Medicamento"] {
  background: #EFF6FF; color: #2563EB; border-color: #BFDBFE;
}
.categoria-pill[data-cat="Alimento"] {
  background: #FFFBEB; color: #D97706; border-color: #FDE68A;
}
.categoria-pill[data-cat="Accesorio"] {
  background: #F5F3FF; color: #7C3AED; border-color: #DDD6FE;
}

.stock-cell {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
  min-width: 110px;
}
.stock-top {
  display: flex;
  align-items: baseline;
  gap: 5px;
}
.stock-num {
  font-size: 14.5px;
  font-weight: 700;
  line-height: 1;
}
.stock-num.is-ok { color: #0F766E; }
.stock-num.is-warning { color: #D97706; }
.stock-num.is-low { color: #DC2626; }
.stock-num.is-empty { color: #991B1B; }
.stock-min {
  font-size: 10.5px;
  color: #94A3B8;
  font-weight: 600;
}
.stock-bar {
  width: 80px;
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

.precio-cell {
  display: inline-flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 1px;
}
.precio-principal {
  font-size: 13.5px;
  font-weight: 700;
  color: #0F766E;
}
.precio-costo {
  font-size: 10.5px;
  color: #94A3B8;
  font-weight: 500;
}

.receta-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 8px;
  background: #FFFBEB;
  color: #92400E;
  border: 1px solid #FDE68A;
}
.sin-receta { color: #CBD5E1; }

/* ═══ ACCIONES POR FILA ═══ */
.row-actions {
  display: inline-flex;
  gap: 4px;
  padding: 4px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
}
.table-row.is-inactivo { opacity: .75; }
.action-btn {
  width: 30px;
  height: 30px;
  border-radius: 7px;
  border: none;
  background: transparent;
  color: #64748B;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .15s;
}
.action-btn:hover {
  background: #fff;
  color: #0F766E;
  box-shadow: 0 1px 4px rgba(15, 23, 42, .1);
}
.action-btn.is-danger:hover {
  background: #FEF2F2;
  color: #DC2626;
}
.action-btn.is-success:hover {
  background: #ECFDF5;
  color: #059669;
}

/* ═══ FOOTER TABLA ═══ */
.table-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 20px;
  border-top: 1px solid #E2E8F0;
  background: #FAFBFC;
  flex-wrap: wrap;
}
.footer-info {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}
.footer-count {
  font-size: 12px;
  font-weight: 700;
  color: #475569;
}
.footer-alert {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  color: #B45309;
  background: #FFFBEB;
  border: 1px solid #FDE68A;
  padding: 3px 10px;
  border-radius: 20px;
}
.footer-clear {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: none;
  border: none;
  color: #0F766E;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  padding: 4px 8px;
  border-radius: 6px;
}
.footer-clear:hover { background: #F0FDFA; }

/* ═══ MODAL CONFIRMACIÓN ═══ */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, .45);
  backdrop-filter: blur(3px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 60;
  padding: 20px;
}
.confirm-modal {
  width: 100%;
  max-width: 440px;
  background: #fff;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 24px 48px -12px rgba(15, 23, 42, .3);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.confirm-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}
.confirm-icon.is-danger {
  background: #FEF2F2;
  color: #DC2626;
  border: 1px solid #FECACA;
}
.confirm-icon.is-success {
  background: #ECFDF5;
  color: #059669;
  border: 1px solid #A7F3D0;
}
.confirm-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
}
.confirm-texto {
  margin: 0 0 16px;
  font-size: 13.5px;
  color: #64748B;
  line-height: 1.6;
}
.confirm-texto strong { color: #0F172A; }
.confirm-meta {
  display: flex;
  gap: 0;
  width: 100%;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 20px;
}
.meta-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.meta-item + .meta-item {
  border-left: 1px solid #E2E8F0;
  padding-left: 16px;
}
.meta-label {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #94A3B8;
}
.meta-valor {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}
.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
}
.confirm-actions {
  display: flex;
  gap: 10px;
  width: 100%;
}
.confirm-actions .btn-secondary,
.btn-confirm {
  flex: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 11px 18px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s;
  font-family: inherit;
  white-space: nowrap;
}
.btn-confirm.is-danger {
  background: #DC2626;
  color: #fff;
  border: none;
}
.btn-confirm.is-danger:hover:not(:disabled) {
  background: #B91C1C;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(220, 38, 38, .4);
}
.btn-confirm.is-success {
  background: #0F766E;
  color: #fff;
  border: none;
}
.btn-confirm.is-success:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-confirm:disabled { opacity: .7; cursor: not-allowed; }

.spin {
  animation: girar .8s linear infinite;
}
@keyframes girar { to { transform: rotate(360deg); } }

/* ═══ TRANSICIONES MODAL ═══ */
.fade-enter-active,
.fade-leave-active { transition: opacity .2s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

.pop-enter-active { transition: all .25s cubic-bezier(.16, 1, .3, 1); }
.pop-leave-active { transition: all .15s ease; }
.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(.96);
}

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .inventory-view { padding: 16px 14px 40px; gap: 14px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero h1 { font-size: 22px; }
  .hero-actions { width: 100%; flex-direction: column; align-items: stretch; }
  .hero-actions .btn-primary,
  .hero-actions .btn-secondary { width: 100%; }
  .kpis { grid-template-columns: 1fr; gap: 10px; }
  .toolbar { padding: 12px; gap: 10px; }
  .search-box { max-width: none; }
  .confirm-actions { flex-direction: column-reverse; }
  .confirm-meta { flex-direction: column; gap: 10px; }
  .meta-item + .meta-item {
    border-left: none;
    padding-left: 0;
    border-top: 1px solid #E2E8F0;
    padding-top: 10px;
  }
}
</style>