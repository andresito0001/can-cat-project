<template>
  <div class="stock-entry">
    <ToastContainer />

    <!-- ═══════════════════════════════════════════════════
         ESTADO DE ÉXITO
         ═══════════════════════════════════════════════════ -->
    <section v-if="resultado" class="success-container">
      <div class="success-icon">
        <CheckCircle2 :size="40" />
      </div>
      <h2 class="success-title">Entrada registrada correctamente</h2>
      <p class="success-message">
        El inventario ha sido actualizado con las cantidades recibidas.
      </p>

      <div class="success-resumen">
        <div class="resumen-fila">
          <span class="resumen-label"><FileText :size="13" /> Orden</span>
          <span class="resumen-value mono">{{ resultado.numeroOrden }}</span>
        </div>
        <div class="resumen-fila">
          <span class="resumen-label"><Package :size="13" /> Productos</span>
          <span class="resumen-value">{{ resultado.totalProductos }}</span>
        </div>
        <div class="resumen-fila">
          <span class="resumen-label"><Layers :size="13" /> Unidades recibidas</span>
          <span class="resumen-value">{{ resultado.totalUnidades }}</span>
        </div>
        <div class="resumen-fila total">
          <span class="resumen-label"><DollarSign :size="13" /> Monto total</span>
          <span class="resumen-value total-value">{{ fmtUsd(resultado.montoTotal) }}</span>
        </div>
      </div>

      <div class="success-actions">
        <button class="btn-primary" type="button" @click="irAlInventario">
          <ArrowLeft :size="15" /> Volver al inventario
        </button>
        <button class="btn-secondary" type="button" @click="nuevaEntrada">
          <Plus :size="15" /> Registrar otra entrada
        </button>
      </div>
    </section>

    <!-- ═══════════════════════════════════════════════════
         FORMULARIO DE ENTRADA
         ═══════════════════════════════════════════════════ -->
    <template v-else>
      <!-- Breadcrumb -->
      <nav class="breadcrumb" aria-label="Migas de pan">
        <button class="bc-back" type="button" @click="cancelar">
          <ArrowLeft :size="15" />
        </button>
        <button class="bc-item bc-link" type="button" @click="cancelar">
          Inventario
        </button>
        <ChevronRight :size="14" class="bc-sep" />
        <span class="bc-item bc-current">Registrar entrada</span>
      </nav>

      <!-- Hero -->
      <header class="hero">
        <div class="hero-left">
          <p class="hero-eyebrow">
            <ArrowDownToLine :size="12" />
            Almacén · Entrada de mercancía
          </p>
          <h1>Registrar entrada de mercancía</h1>
          <p class="hero-sub">
            Registra la recepción física de productos del proveedor. El stock se actualizará
            automáticamente al procesar la entrada.
          </p>
        </div>
      </header>

      <!-- Card: Datos de la recepción -->
      <section class="card">
        <header class="card-header">
          <div class="card-header-left">
            <div class="card-icon"><FileText :size="16" /></div>
            <div>
              <h3>Datos de la recepción</h3>
              <p class="card-header-sub">Información de la factura física del proveedor</p>
            </div>
          </div>
        </header>
        <div class="card-body">
          <div class="form-grid-2">
            <div class="form-group">
              <label class="form-label" for="proveedor">
                Proveedor <span class="required">*</span>
              </label>
              <select
                id="proveedor"
                v-model="form.idProveedor"
                class="form-select"
                :class="{ 'is-invalid': errors.idProveedor }"
                :disabled="procesando"
              >
                <option :value="null" disabled>Seleccione un proveedor…</option>
                <option v-for="p in proveedores" :key="p.id" :value="p.id">
                  {{ p.nombreEmpresa }} · {{ p.rif }}
                </option>
              </select>
              <span v-if="errors.idProveedor" class="form-error">
                Selecciona un proveedor.
              </span>
            </div>

            <div class="form-group">
              <label class="form-label" for="factura">
                Número de factura <span class="required">*</span>
              </label>
              <input
                id="factura"
                v-model="form.numeroFactura"
                type="text"
                class="form-input"
                :class="{ 'is-invalid': errors.numeroFactura }"
                placeholder="Ej: FAC-2024-00123"
                :disabled="procesando"
              />
              <span v-if="errors.numeroFactura" class="form-error">
                El número de factura es obligatorio.
              </span>
            </div>

            <div class="form-group">
              <label class="form-label" for="fecha">
                Fecha de recepción <span class="required">*</span>
              </label>
              <input
                id="fecha"
                v-model="form.fechaRecepcion"
                type="date"
                class="form-input"
                :max="hoy"
                :disabled="procesando"
              />
            </div>

            <div class="form-group">
              <label class="form-label" for="obs">
                Observaciones <span class="optional">(opcional)</span>
              </label>
              <input
                id="obs"
                v-model="form.observaciones"
                type="text"
                class="form-input"
                placeholder="Notas sobre la recepción…"
                maxlength="200"
                :disabled="procesando"
              />
            </div>
          </div>
        </div>
      </section>

      <!-- Card: Líneas de productos -->
      <section class="card">
        <header class="card-header">
          <div class="card-header-left">
            <div class="card-icon"><Package :size="16" /></div>
            <div>
              <h3>Productos recibidos</h3>
              <p class="card-header-sub">
                Agrega cada ítem recibido con su cantidad y precio
              </p>
            </div>
          </div>
          <button
            class="btn-secondary"
            type="button"
            :disabled="procesando"
            @click="abrirModalProducto"
          >
            <Plus :size="14" />
            Crear producto nuevo
          </button>
        </header>
        <div class="card-body">
          <!-- Alerta de error general de líneas -->
          <div v-if="errors.lineas" class="alert alert-error" style="margin-top: 0; margin-bottom: 16px;">
            <AlertCircle :size="16" />
            <span>{{ errors.lineas }}</span>
          </div>

          <!-- Estado vacío -->
          <div v-if="!form.lineas.length" class="empty-lineas">
            <div class="empty-lineas-icon">
              <Package :size="28" />
            </div>
            <h4>Sin productos agregados</h4>
            <p>Comienza buscando el primer producto a registrar.</p>
            <button class="btn-primary" type="button" @click="agregarLineaVacia">
              <Plus :size="15" /> Agregar primer producto
            </button>
          </div>

          <!-- Lista de líneas -->
          <div v-else class="lineas-list">
            <article
              v-for="(linea, idx) in form.lineas"
              :key="linea.uid"
              class="linea-item"
              :class="{ 'has-error': linea.errors }"
            >
              <div class="linea-header">
                <span class="linea-num">#{{ idx + 1 }}</span>
                <button
                  class="linea-remove"
                  type="button"
                  :disabled="procesando"
                  aria-label="Eliminar línea"
                  @click="eliminarLinea(idx)"
                >
                  <Trash2 :size="14" />
                </button>
              </div>

              <div class="linea-body">
                <!-- Buscador de producto -->
                <div class="form-group full-width">
                  <label class="form-label">
                    Producto <span class="required">*</span>
                  </label>

                  <!-- Producto seleccionado -->
                  <div v-if="linea.idProducto" class="producto-seleccionado">
                    <div class="producto-mini">
                      <span class="producto-sku">{{ linea.codigoSku }}</span>
                      <p class="producto-nombre">{{ linea.nombre }}</p>
                      <p class="producto-meta">
                        {{ linea.categoria }} · Stock actual: {{ linea.stockActualPrev }}
                      </p>
                    </div>
                    <button
                      class="btn-chip"
                      type="button"
                      :disabled="procesando"
                      @click="quitarProductoDeLinea(idx)"
                    >
                      <X :size="13" /> Cambiar
                    </button>
                  </div>

                  <!-- Búsqueda -->
                  <div v-else class="producto-search">
                    <Search :size="15" class="search-icon" />
                    <input
                      :id="`buscar-${idx}`"
                      v-model="linea.busqueda"
                      type="text"
                      class="form-input search-input"
                      :class="{ 'is-invalid': linea.errors?.idProducto }"
                      placeholder="Busca por nombre o SKU…"
                      autocomplete="off"
                      :disabled="procesando"
                      @input="onBuscarProducto(idx)"
                      @focus="onFocusBusqueda(idx)"
                    />
                    <Loader2 v-if="linea.buscando" :size="14" class="search-spinner spin" />

                    <div
                      v-if="linea.resultados?.length && linea.mostrandoResultados"
                      class="search-results"
                    >
                      <button
                        v-for="p in linea.resultados"
                        :key="p.id"
                        type="button"
                        class="search-result"
                        @click="seleccionarProductoEnLinea(idx, p)"
                      >
                        <div class="sr-info">
                          <span class="sr-sku">{{ p.codigoSku }}</span>
                          <span class="sr-nombre">{{ p.nombre }}</span>
                          <span class="sr-meta">
                            {{ p.categoria }} · Stock: {{ p.stockActual }}
                          </span>
                        </div>
                        <ChevronRight :size="14" class="sr-arrow" />
                      </button>
                    </div>

                    <div
                      v-else-if="linea.busqueda && linea.busqueda.length >= 2 && !linea.buscando && linea.resultados?.length === 0"
                      class="search-empty"
                    >
                      <span>Sin coincidencias.</span>
                      <button
                        class="btn-link"
                        type="button"
                        @click="abrirModalProducto(linea.busqueda)"
                      >
                        Crear "{{ linea.busqueda }}"
                      </button>
                    </div>
                  </div>
                </div>

                <!-- Cantidades y precio -->
                <div class="form-grid-3">
                  <div class="form-group">
                    <label class="form-label">
                      Cantidad recibida <span class="required">*</span>
                    </label>
                    <input
                      v-model.number="linea.cantidadRecibida"
                      type="number"
                      min="1"
                      class="form-input"
                      :class="{ 'is-invalid': linea.errors?.cantidadRecibida }"
                      placeholder="0"
                      :disabled="procesando"
                    />
                    <span v-if="linea.errors?.cantidadRecibida" class="form-error">
                      Debe ser mayor a 0.
                    </span>
                  </div>

                  <div class="form-group">
                    <label class="form-label">
                      Precio unitario (USD) <span class="required">*</span>
                    </label>
                    <input
                      v-model.number="linea.precioUnitario"
                      type="number"
                      step="0.01"
                      min="0"
                      class="form-input"
                      :class="{ 'is-invalid': linea.errors?.precioUnitario }"
                      placeholder="0.00"
                      :disabled="procesando"
                    />
                    <span v-if="linea.errors?.precioUnitario" class="form-error">
                      Precio inválido.
                    </span>
                  </div>

                  <div class="form-group">
                    <label class="form-label">
                      Número de lote <span class="optional">(opcional)</span>
                    </label>
                    <input
                      v-model="linea.numeroLote"
                      type="text"
                      class="form-input"
                      placeholder="Ej: LOT-2024-A"
                      maxlength="50"
                      :disabled="procesando"
                    />
                  </div>

                  <div class="form-group">
                    <label class="form-label">
                      Fecha de vencimiento <span class="optional">(opcional)</span>
                    </label>
                    <input
                      v-model="linea.fechaVencimientoLote"
                      type="date"
                      class="form-input"
                      :disabled="procesando"
                    />
                  </div>

                  <div class="form-group">
                    <label class="form-label">Subtotal</label>
                    <div class="subtotal-display">
                      {{ fmtUsd(subtotalLinea(linea)) }}
                    </div>
                  </div>
                </div>
              </div>
            </article>

            <button
              class="btn-add-linea"
              type="button"
              :disabled="procesando"
              @click="agregarLineaVacia"
            >
              <Plus :size="15" />
              Agregar otro producto
            </button>
          </div>
        </div>
      </section>

      <!-- Card: Resumen y acciones -->
      <section class="card resumen-card">
        <div class="resumen-body">
          <div class="resumen-stats">
            <div class="resumen-stat">
              <span class="rs-label">Productos</span>
              <strong class="rs-value">{{ form.lineas.length }}</strong>
            </div>
            <div class="resumen-stat">
              <span class="rs-label">Unidades</span>
              <strong class="rs-value">{{ totalUnidades }}</strong>
            </div>
            <div class="resumen-stat is-total">
              <span class="rs-label">Monto total</span>
              <strong class="rs-value rs-total">{{ fmtUsd(totalMonto) }}</strong>
            </div>
          </div>

          <div class="resumen-actions">
            <button
              class="btn-cancel"
              type="button"
              :disabled="procesando"
              @click="cancelar"
            >
              <X :size="15" /> Cancelar
            </button>
            <button
              class="btn-next"
              type="button"
              :disabled="procesando || !puedeProcesar"
              @click="procesarEntrada"
            >
              <Loader2 v-if="procesando" :size="15" class="spin" />
              <CheckCircle2 v-else :size="15" />
              {{ procesando ? 'Procesando…' : 'Procesar entrada' }}
            </button>
          </div>
        </div>
      </section>
    </template>

    <!-- Modal de crear/editar producto -->
    <ProductFormModal
      :visible="modalProductoVisible"
      :producto="null"
      :categorias="categoriasDisponibles"
      :proveedores="proveedores"
      :prefill-nombre="prefillNombreProducto"
      @close="cerrarModalProducto"
      @saved="onProductoCreado"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeft, ArrowDownToLine, CheckCircle2, ChevronRight, Package,
  FileText, Layers, DollarSign, Search, X, Plus, Trash2, Loader2,
  AlertCircle
} from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import ProductFormModal from '@/components/almacen/ProductFormModal.vue'
import { getProductos, getProveedores, registrarEntrada } from '@/api/almacen.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { toastError } = useToast()

const hoy = new Date().toISOString().split('T')[0]

// ─── Catálogos ───
const proveedores = ref([])
const catalogoProductos = ref([])

// ─── Form ───
const form = reactive({
  idProveedor: null,
  numeroFactura: '',
  fechaRecepcion: hoy,
  observaciones: '',
  lineas: [],
})

const errors = reactive({
  idProveedor: false,
  numeroFactura: false,
  lineas: '',
})

// ─── Estado UI ───
const procesando = ref(false)
const resultado = ref(null)

// ─── Modal ───
const modalProductoVisible = ref(false)
const prefillNombreProducto = ref('')

// ─── Categorías disponibles (para el modal) ───
const categoriasDisponibles = computed(() => {
  const set = new Set()
  for (const p of catalogoProductos.value) {
    if (p.categoria) set.add(p.categoria)
  }
  return Array.from(set).sort()
})

// ─── Totales ───
const totalUnidades = computed(() =>
  form.lineas.reduce((s, l) => s + (Number(l.cantidadRecibida) || 0), 0)
)
const totalMonto = computed(() =>
  form.lineas.reduce((s, l) => s + subtotalLinea(l), 0)
)
const puedeProcesar = computed(() => form.lineas.length > 0 && form.lineas.every(l => l.idProducto))

function subtotalLinea(l) {
  const c = Number(l.cantidadRecibida) || 0
  const p = Number(l.precioUnitario) || 0
  return c * p
}

function fmtUsd(v) {
  return `$${Number(v || 0).toFixed(2)}`
}

// ─── Gestión de líneas ───
let uidCounter = 0

function agregarLineaVacia() {
  form.lineas.push({
    uid: ++uidCounter,
    idProducto: null,
    codigoSku: '',
    nombre: '',
    categoria: '',
    stockActualPrev: 0,
    cantidadRecibida: 1,
    precioUnitario: 0,
    numeroLote: '',
    fechaVencimientoLote: '',
    busqueda: '',
    buscando: false,
    mostrandoResultados: false,
    resultados: [],
    errors: null,
  })
}

function eliminarLinea(idx) {
  form.lineas.splice(idx, 1)
  if (errors.lineas && form.lineas.length > 0) errors.lineas = ''
}

function quitarProductoDeLinea(idx) {
  const l = form.lineas[idx]
  l.idProducto = null
  l.codigoSku = ''
  l.nombre = ''
  l.categoria = ''
  l.stockActualPrev = 0
  l.busqueda = ''
  l.resultados = []
}

// ─── Búsqueda en línea (con debounce) ───
const debounces = new Map()

function onBuscarProducto(idx) {
  const linea = form.lineas[idx]
  clearTimeout(debounces.get(idx))
  if (!linea.busqueda || linea.busqueda.length < 2) {
    linea.resultados = []
    linea.mostrandoResultados = false
    return
  }
  debounces.set(idx, setTimeout(() => ejecutarBusqueda(idx), 300))
}

async function ejecutarBusqueda(idx) {
  const linea = form.lineas[idx]
  linea.buscando = true
  linea.mostrandoResultados = true
  try {
    // Buscar en el catálogo local + API (para nuevos)
    const q = linea.busqueda.toLowerCase().trim()
    linea.resultados = catalogoProductos.value
      .filter((p) =>
        p.nombre.toLowerCase().includes(q) ||
        p.codigoSku.toLowerCase().includes(q)
      )
      .slice(0, 8)
  } catch {
    linea.resultados = []
  } finally {
    linea.buscando = false
  }
}

function onFocusBusqueda(idx) {
  const linea = form.lineas[idx]
  if (linea.busqueda && linea.resultados.length) {
    linea.mostrandoResultados = true
  }
}

function seleccionarProductoEnLinea(idx, producto) {
  const l = form.lineas[idx]
  l.idProducto = producto.id
  l.codigoSku = producto.codigoSku
  l.nombre = producto.nombre
  l.categoria = producto.categoria
  l.stockActualPrev = producto.stockActual
  l.precioUnitario = producto.costoAdquisicion || 0
  l.busqueda = ''
  l.resultados = []
  l.mostrandoResultados = false
  l.errors = null
}

// ─── Modal producto ───
function abrirModalProducto(prefill = '') {
  prefillNombreProducto.value = typeof prefill === 'string' ? prefill : ''
  modalProductoVisible.value = true
}

function cerrarModalProducto() {
  modalProductoVisible.value = false
  prefillNombreProducto.value = ''
}

async function onProductoCreado(producto) {
  cerrarModalProducto()
  await cargarCatalogo()
  toastError(null) // limpia errores previos
  // Auto-agregar el producto recién creado a la primera línea vacía
  const lineaVacia = form.lineas.find(l => !l.idProducto)
  if (lineaVacia) {
    seleccionarProductoEnLinea(form.lineas.indexOf(lineaVacia), producto)
  } else {
    agregarLineaVacia()
    seleccionarProductoEnLinea(form.lineas.length - 1, producto)
  }
}

// ─── Carga de catálogos ───
async function cargarCatalogo() {
  try {
    const { data } = await getProductos()
    catalogoProductos.value = data || []
  } catch (err) {
    toastError(getApiErrorMessage(err))
  }
}

async function cargarProveedores() {
  try {
    const { data } = await getProveedores()
    proveedores.value = data || []
  } catch (err) {
    toastError(getApiErrorMessage(err))
  }
}

// ─── Validación ───
function validar() {
  errors.idProveedor = false
  errors.numeroFactura = false
  errors.lineas = ''

  let ok = true

  if (!form.idProveedor) {
    errors.idProveedor = true
    ok = false
  }
  if (!form.numeroFactura.trim()) {
    errors.numeroFactura = true
    ok = false
  }
  if (!form.fechaRecepcion) {
    ok = false
  }
  if (form.lineas.length === 0) {
    errors.lineas = 'Debes agregar al menos un producto.'
    ok = false
  }

  // Validar cada línea
  for (const l of form.lineas) {
    l.errors = {}
    if (!l.idProducto) {
      l.errors.idProducto = true
      ok = false
    }
    if (!l.cantidadRecibida || Number(l.cantidadRecibida) <= 0) {
      l.errors.cantidadRecibida = true
      ok = false
    }
    if (l.precioUnitario == null || Number(l.precioUnitario) < 0) {
      l.errors.precioUnitario = true
      ok = false
    }
    if (Object.keys(l.errors).length === 0) l.errors = null
  }

  return ok
}

// ─── Procesar entrada ───
async function procesarEntrada() {
  if (!validar()) {
    toastError('Verifique los datos ingresados. Las cantidades deben ser números mayores a cero y los campos marcados son obligatorios.')
    return
  }

  procesando.value = true
  try {
    const payload = {
      idProveedor: form.idProveedor,
      numeroFactura: form.numeroFactura.trim(),
      fechaRecepcion: form.fechaRecepcion,
      observaciones: form.observaciones.trim() || null,
      lineas: form.lineas.map(l => ({
        idProducto: l.idProducto,
        cantidadRecibida: Number(l.cantidadRecibida),
        precioUnitario: Number(l.precioUnitario),
        numeroLote: l.numeroLote?.trim() || null,
        fechaVencimientoLote: l.fechaVencimientoLote || null,
      })),
    }

    const { data } = await registrarEntrada(payload)
    resultado.value = data
  } catch (err) {
    toastError(getApiErrorMessage(err))
  } finally {
    procesando.value = false
  }
}

// ─── Navegación ───
function cancelar() {
  if (procesando.value) return
  router.push('/almacen/inventario')
}
function irAlInventario() {
  router.push('/almacen/inventario')
}
function nuevaEntrada() {
  resultado.value = null
  form.idProveedor = null
  form.numeroFactura = ''
  form.fechaRecepcion = hoy
  form.observaciones = ''
  form.lineas = []
  errors.idProveedor = false
  errors.numeroFactura = false
  errors.lineas = ''
  agregarLineaVacia()
}

onMounted(async () => {
  await Promise.all([cargarCatalogo(), cargarProveedores()])
  // Arrancar con una línea vacía lista para llenar
  agregarLineaVacia()
})
</script>

<style scoped>
.stock-entry {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 28px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #0F172A;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
button { font-family: inherit; }

/* ═══ BREADCRUMB ═══ */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748B;
}
.bc-back {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: 1px solid #E2E8F0;
  background: #fff;
  color: #475569;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .15s;
  margin-right: 2px;
}
.bc-back:hover { background: #F1F5F9; color: #0F766E; border-color: #CBD5E1; }
.bc-item { font-weight: 500; }
.bc-link {
  background: none;
  border: none;
  color: #64748B;
  cursor: pointer;
  padding: 0;
  font-size: 13px;
}
.bc-link:hover { color: #0F766E; text-decoration: underline; }
.bc-current { color: #1E293B; font-weight: 700; }
.bc-sep { color: #CBD5E1; }

/* ═══ HERO ═══ */
.hero {
  padding: 24px 28px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
}
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
  line-height: 1.55;
}

/* ═══ CARDS ═══ */
.card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 1px 2px rgba(15, 23, 42, .03), 0 4px 12px -2px rgba(15, 23, 42, .04);
  overflow: hidden;
}
.card-header {
  padding: 18px 22px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.card-header-left { display: flex; align-items: center; gap: 12px; min-width: 0; }
.card-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #F0FDFA;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.card-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #0F172A;
}
.card-header-sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748B;
}
.card-body { padding: 20px 22px 22px; }

/* ═══ FORM ═══ */
.form-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}
.form-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}
.form-group.full-width { grid-column: 1 / -1; }
.form-label {
  font-size: 12.5px;
  font-weight: 600;
  color: #374151;
}
.required { color: #EF4444; }
.optional {
  color: #94A3B8;
  font-weight: 500;
  font-size: 11.5px;
  margin-left: 2px;
}
.form-input,
.form-select {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  font-size: 13.5px;
  color: #0F172A;
  background: #fff;
  font-family: inherit;
  transition: border-color .2s, box-shadow .2s;
  box-sizing: border-box;
  appearance: none;
  -webkit-appearance: none;
}
.form-select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px;
  cursor: pointer;
}
.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.form-input:disabled,
.form-select:disabled {
  background: #F8FAFC;
  color: #94A3B8;
  cursor: not-allowed;
}
.is-invalid { border-color: #EF4444 !important; background: #FEF2F2 !important; }
.form-error {
  font-size: 11.5px;
  color: #EF4444;
  font-weight: 600;
}

/* ═══ ALERTA ═══ */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  font-weight: 500;
}
.alert-error { background: #FEF2F2; color: #991B1B; border: 1px solid #FECACA; }

/* ═══ VACÍO ═══ */
.empty-lineas {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 40px 24px;
  background: #F8FAFC;
  border: 1.5px dashed #E2E8F0;
  border-radius: 12px;
  text-align: center;
}
.empty-lineas-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #F0FDFA 0%, #ECFDF5 100%);
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}
.empty-lineas h4 {
  margin: 0;
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
}
.empty-lineas p {
  margin: 0 0 8px;
  font-size: 13px;
  color: #64748B;
}

/* ═══ LISTA DE LÍNEAS ═══ */
.lineas-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.linea-item {
  background: #F8FAFC;
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  overflow: hidden;
  transition: border-color .2s;
}
.linea-item.has-error {
  border-color: #FECACA;
  background: #FEF2F2;
}
.linea-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #F1F5F9;
}
.linea-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 28px;
  height: 22px;
  padding: 0 8px;
  border-radius: 20px;
  background: #F0FDFA;
  color: #0F766E;
  border: 1px solid #CCFBF1;
  font-size: 11px;
  font-weight: 700;
}
.linea-remove {
  width: 28px;
  height: 28px;
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
.linea-remove:hover:not(:disabled) {
  background: #FEF2F2;
  color: #DC2626;
  border-color: #FECACA;
}
.linea-remove:disabled { opacity: .5; cursor: not-allowed; }

.linea-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* ─── Búsqueda de producto ─── */
.producto-search {
  position: relative;
}
.search-icon {
  position: absolute;
  left: 12px;
  top: 14px;
  color: #94A3B8;
  pointer-events: none;
}
.search-input {
  padding-left: 36px;
  padding-right: 36px;
}
.search-spinner {
  position: absolute;
  right: 12px;
  top: 14px;
  color: #0F766E;
}
.search-results {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  box-shadow: 0 10px 24px -8px rgba(15, 23, 42, .15);
  z-index: 20;
  overflow: hidden;
  max-height: 280px;
  overflow-y: auto;
}
.search-result {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 10px 14px;
  background: #fff;
  border: none;
  border-bottom: 1px solid #F1F5F9;
  cursor: pointer;
  text-align: left;
  font-family: inherit;
  transition: background-color .15s;
}
.search-result:last-child { border-bottom: none; }
.search-result:hover { background: #F0FDFA; }
.sr-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.sr-sku {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 10.5px;
  font-weight: 700;
  color: #0F766E;
  background: #F0FDFA;
  padding: 1px 6px;
  border-radius: 4px;
  border: 1px solid #CCFBF1;
  align-self: flex-start;
}
.sr-nombre {
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.sr-meta {
  font-size: 11.5px;
  color: #64748B;
}
.sr-arrow { color: #CBD5E1; flex-shrink: 0; }
.search-result:hover .sr-arrow { color: #0F766E; }

.search-empty {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 14px;
  background: #FFFBEB;
  border: 1px solid #FDE68A;
  border-radius: 8px;
  margin-top: 6px;
  font-size: 12.5px;
  color: #92400E;
}

/* ─── Producto seleccionado ─── */
.producto-seleccionado {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  background: #fff;
  border: 1.5px solid #CCFBF1;
  border-radius: 10px;
}
.producto-mini { min-width: 0; }
.producto-sku {
  display: inline-block;
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 10.5px;
  font-weight: 700;
  color: #0F766E;
  background: #F0FDFA;
  padding: 1px 7px;
  border-radius: 4px;
  border: 1px solid #CCFBF1;
  margin-bottom: 4px;
}
.producto-nombre {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
}
.producto-meta {
  margin: 2px 0 0;
  font-size: 11.5px;
  color: #64748B;
}
.btn-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  color: #64748B;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s;
  flex-shrink: 0;
}
.btn-chip:hover:not(:disabled) {
  background: #FEF2F2;
  border-color: #FECACA;
  color: #DC2626;
}
.btn-chip:disabled { opacity: .5; cursor: not-allowed; }

/* ─── Subtotal display ─── */
.subtotal-display {
  padding: 10px 14px;
  border-radius: 10px;
  background: #F0FDFA;
  border: 1px solid #CCFBF1;
  color: #0F766E;
  font-size: 14px;
  font-weight: 700;
  text-align: right;
}

/* ─── Botón agregar línea ─── */
.btn-add-linea {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 12px 20px;
  background: #fff;
  border: 1.5px dashed #CCFBF1;
  border-radius: 12px;
  color: #0F766E;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s;
  font-family: inherit;
}
.btn-add-linea:hover:not(:disabled) {
  background: #F0FDFA;
  border-style: solid;
}
.btn-add-linea:disabled { opacity: .5; cursor: not-allowed; }

/* ═══ RESUMEN ═══ */
.resumen-card {
  border-color: #CCFBF1;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 60%);
}
.resumen-body {
  padding: 20px 22px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}
.resumen-stats {
  display: flex;
  gap: 28px;
  flex-wrap: wrap;
}
.resumen-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.rs-label {
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .7px;
  color: #64748B;
}
.rs-value {
  font-size: 18px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
}
.resumen-stat.is-total .rs-total {
  font-size: 22px;
  color: #0F766E;
}
.resumen-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* ═══ BOTONES ═══ */
.btn-primary,
.btn-secondary,
.btn-cancel,
.btn-next {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 10px 20px;
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
.btn-primary:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-secondary {
  background: #fff;
  color: #475569;
  border: 1px solid #E2E8F0;
}
.btn-secondary:hover:not(:disabled) {
  background: #F8FAFC;
  border-color: #CBD5E1;
  color: #0F766E;
}
.btn-cancel {
  background: none;
  border: 1px solid #E2E8F0;
  color: #64748B;
}
.btn-cancel:hover:not(:disabled) {
  background: #FEF2F2;
  border-color: #FECACA;
  color: #DC2626;
}
.btn-next {
  background: #0F766E;
  border: none;
  color: #fff;
}
.btn-next:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-next:disabled,
.btn-cancel:disabled,
.btn-primary:disabled,
.btn-secondary:disabled { opacity: .55; cursor: not-allowed; }
.btn-link {
  background: none;
  border: none;
  color: #0F766E;
  font-size: 12.5px;
  font-weight: 700;
  cursor: pointer;
  text-decoration: underline;
}

/* ═══ ÉXITO ═══ */
.success-container {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 12px -2px rgba(15, 23, 42, .04);
  padding: 48px 32px;
  max-width: 640px;
  margin: 40px auto 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
}
.success-icon {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDFA 100%);
  border: 3px solid #10B981;
  color: #059669;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  animation: successPop .5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.08); }
  100% { transform: scale(1); opacity: 1; }
}
.success-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.015em;
}
.success-message {
  margin: 0 0 8px;
  font-size: 13.5px;
  color: #64748B;
  max-width: 460px;
  line-height: 1.6;
}
.success-resumen {
  width: 100%;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 6px 18px;
  margin: 8px 0;
}
.resumen-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #E2E8F0;
  font-size: 13px;
}
.resumen-fila:last-child { border-bottom: none; }
.resumen-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #64748B;
  font-weight: 600;
  font-size: 12.5px;
}
.resumen-label svg { color: #94A3B8; }
.resumen-value {
  color: #0F172A;
  font-weight: 700;
  text-align: right;
}
.resumen-value.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12.5px;
  background: #F1F5F9;
  padding: 2px 8px;
  border-radius: 5px;
}
.resumen-fila.total {
  border-top: 2px solid #E2E8F0;
  padding-top: 14px;
  margin-top: 4px;
}
.total-value {
  color: #0F766E;
  font-size: 16px;
}
.success-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
  justify-content: center;
  width: 100%;
}

.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 768px) {
  .stock-entry { padding: 16px 16px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero h1 { font-size: 22px; }
  .form-grid-2,
  .form-grid-3 { grid-template-columns: 1fr; }
  .card-body { padding: 16px 18px 20px; }
  .card-header { padding: 16px 18px; }
  .resumen-body { padding: 16px 18px; flex-direction: column; align-items: stretch; }
  .resumen-actions { flex-direction: column-reverse; }
  .resumen-actions .btn-next,
  .resumen-actions .btn-cancel { width: 100%; }
  .resumen-stats { gap: 18px; }
  .success-container { padding: 32px 20px; margin-top: 20px; }
  .success-icon { width: 72px; height: 72px; }
  .success-title { font-size: 19px; }
  .success-actions { flex-direction: column; }
  .success-actions .btn-primary,
  .success-actions .btn-secondary { width: 100%; }
}
</style>