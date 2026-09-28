<template>
  <div class="proveedores-view">
    <ToastContainer />

    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow"><Truck :size="12" /> Almacén · Proveedores</p>
        <h1>Proveedores</h1>
        <p class="hero-sub">Administra los proveedores que suministran productos a la clínica.</p>
      </div>
      <div class="hero-right">
        <button class="btn-primary" type="button" @click="abrirModal(null)">
          <Plus :size="15" /> Nuevo proveedor
        </button>
      </div>
    </header>

    <section class="kpis">
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #0F766E; --kpi-bg: #F0FDFA;">
          <Truck :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ stats.activos }}</p>
          <p class="kpi-label">Proveedores activos</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #3B82F6; --kpi-bg: #EFF6FF;">
          <Pill :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ stats.medicamentos }}</p>
          <p class="kpi-label">De medicamentos</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #F59E0B; --kpi-bg: #FFFBEB;">
          <Package :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ stats.alimentos }}</p>
          <p class="kpi-label">De alimentos</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #8B5CF6; --kpi-bg: #F5F3FF;">
          <Boxes :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ stats.mixtos }}</p>
          <p class="kpi-label">Mixtos</p>
        </div>
      </article>
    </section>

    <section class="toolbar">
      <div class="search-box">
        <Search :size="15" class="search-icon" />
        <input
          v-model="filtro"
          type="text"
          class="search-input"
          placeholder="Buscar por empresa, RIF o contacto…"
        />
        <button v-if="filtro" type="button" class="search-clear" @click="filtro = ''">
          <X :size="13" />
        </button>
      </div>

      <div class="tabs-filter">
        <button
          type="button"
          class="chip" :class="{ active: filtroEstado === 'activos' }"
          @click="filtroEstado = 'activos'"
        >Activos</button>
        <button
          type="button"
          class="chip" :class="{ active: filtroEstado === 'inactivos' }"
          @click="filtroEstado = 'inactivos'"
        >Inactivos</button>
        <button
          type="button"
          class="chip" :class="{ active: filtroEstado === 'todos' }"
          @click="filtroEstado = 'todos'"
        >Todos</button>
      </div>
    </section>

    <div v-if="error" class="alert alert-error">
      <AlertCircle :size="16" /><span>{{ error }}</span>
      <button type="button" class="alert-action" @click="cargar">Reintentar</button>
    </div>

    <div v-if="cargando" class="skeleton-table">
      <div v-for="i in 5" :key="i" class="skeleton-row">
        <div class="skeleton-cell w-15" />
        <div class="skeleton-cell w-30" />
        <div class="skeleton-cell w-20" />
        <div class="skeleton-cell w-15" />
      </div>
    </div>

    <div v-else-if="!proveedoresFiltrados.length" class="empty-state">
      <div class="empty-icon"><Truck :size="32" /></div>
      <h3>{{ tieneFiltros ? 'Sin resultados' : 'Sin proveedores registrados' }}</h3>
      <p>{{ tieneFiltros ? 'Prueba con otros criterios.' : 'Registra tu primer proveedor.' }}</p>
      <button v-if="tieneFiltros" class="btn-link" type="button" @click="limpiarFiltros">
        Limpiar filtros
      </button>
      <button v-else class="btn-primary" type="button" @click="abrirModal(null)">
        <Plus :size="15" /> Nuevo proveedor
      </button>
    </div>

    <div v-else class="table-card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>RIF</th>
              <th>Empresa</th>
              <th>Contacto</th>
              <th>Tipo</th>
              <th>Estado</th>
              <th class="der">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in proveedoresFiltrados" :key="p.id" class="table-row" :class="{ 'is-inactivo': !p.activo }">
              <td><span class="rif-pill">{{ p.rif }}</span></td>
              <td>
                <div class="empresa-cell">
                  <p class="empresa-nombre">{{ p.nombreEmpresa }}</p>
                  <p v-if="p.direccion" class="empresa-dir">{{ p.direccion }}</p>
                </div>
              </td>
              <td>
                <div class="contacto-cell">
                  <p v-if="p.nombreContacto" class="contacto-nombre">{{ p.nombreContacto }}</p>
                  <p v-if="p.telefono" class="contacto-line"><Phone :size="11" /> {{ p.telefono }}</p>
                  <p v-if="p.correo" class="contacto-line"><Mail :size="11" /> {{ p.correo }}</p>
                </div>
              </td>
              <td>
                <span class="tipo-pill" :data-tipo="p.tipoSuministro">{{ p.tipoSuministro }}</span>
              </td>
              <td>
                <span class="status-pill" :class="p.activo ? 'is-active' : 'is-inactive'">
                  {{ p.activo ? 'Activo' : 'Inactivo' }}
                </span>
              </td>
              <td class="der">
                <div class="acciones">
                  <button class="icon-action" type="button" title="Editar" @click="abrirModal(p)">
                    <Pencil :size="14" />
                  </button>
                  <button
                    class="icon-action"
                    :class="p.activo ? 'icon-danger' : 'icon-success'"
                    type="button"
                    :title="p.activo ? 'Desactivar' : 'Activar'"
                    @click="toggleActivo(p)"
                  >
                    <component :is="p.activo ? Archive : RotateCcw" :size="14" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <footer class="table-footer">
        <span>{{ proveedoresFiltrados.length }} proveedor{{ proveedoresFiltrados.length === 1 ? '' : 'es' }}</span>
      </footer>
    </div>

    <ProveedorFormModal
      :visible="modalVisible"
      :proveedor="proveedorEditando"
      @close="cerrarModal"
      @saved="onSaved"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Truck, Plus, Search, X, AlertCircle, Phone, Mail,
  Pencil, Archive, RotateCcw, Pill, Package, Boxes,
} from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import ProveedorFormModal from '@/components/almacen/ProveedorFormModal.vue'
import { getProveedoresTodos, cambiarEstadoProveedor } from '@/api/almacen.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

const { toastSuccess, toastError } = useToast()

const cargando = ref(false)
const error = ref('')
const proveedores = ref([])
const filtro = ref('')
const filtroEstado = ref('activos')

const modalVisible = ref(false)
const proveedorEditando = ref(null)

const proveedoresFiltrados = computed(() => {
  let items = proveedores.value
  const q = filtro.value.trim().toLowerCase()

  if (filtroEstado.value === 'activos') items = items.filter((p) => p.activo)
  else if (filtroEstado.value === 'inactivos') items = items.filter((p) => !p.activo)

  if (q) {
    items = items.filter((p) =>
      (p.nombreEmpresa || '').toLowerCase().includes(q) ||
      (p.rif || '').toLowerCase().includes(q) ||
      (p.nombreContacto || '').toLowerCase().includes(q)
    )
  }

  return items.sort((a, b) => (a.nombreEmpresa || '').localeCompare(b.nombreEmpresa || ''))
})

const stats = computed(() => {
  const activos = proveedores.value.filter((p) => p.activo)
  return {
    activos: activos.length,
    medicamentos: activos.filter((p) => p.tipoSuministro === 'Medicamentos').length,
    alimentos: activos.filter((p) => p.tipoSuministro === 'Alimentos').length,
    mixtos: activos.filter((p) => p.tipoSuministro === 'Mixto').length,
  }
})

const tieneFiltros = computed(() => !!filtro.value || filtroEstado.value !== 'activos')

async function cargar() {
  cargando.value = true
  error.value = ''
  try {
    const { data } = await getProveedoresTodos()
    proveedores.value = data || []
  } catch (err) {
    error.value = getApiErrorMessage(err)
    toastError(error.value)
  } finally {
    cargando.value = false
  }
}

function limpiarFiltros() {
  filtro.value = ''
  filtroEstado.value = 'activos'
}

function abrirModal(prov) {
  proveedorEditando.value = prov
  modalVisible.value = true
}

function cerrarModal() {
  modalVisible.value = false
  proveedorEditando.value = null
}

async function onSaved() {
  toastSuccess(proveedorEditando.value ? 'Proveedor actualizado' : 'Proveedor creado')
  cerrarModal()
  await cargar()
}

async function toggleActivo(p) {
  try {
    await cambiarEstadoProveedor(p.id, !p.activo)
    toastSuccess(p.activo ? `"${p.nombreEmpresa}" desactivado` : `"${p.nombreEmpresa}" activado`)
    await cargar()
  } catch (err) {
    toastError(getApiErrorMessage(err))
  }
}

onMounted(cargar)
</script>

<style scoped>
.proveedores-view {
  max-width: 1500px; margin: 0 auto; padding: 24px 28px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, sans-serif;
  color: #0F172A; display: flex; flex-direction: column; gap: 18px;
}
button { font-family: inherit; }

.hero {
  display: flex; align-items: center; justify-content: space-between;
  gap: 20px; flex-wrap: wrap; padding: 24px 28px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1; border-radius: 16px;
}
.hero-eyebrow {
  display: inline-flex; align-items: center; gap: 6px;
  margin: 0 0 8px; font-size: 11px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .7px;
  color: #0F766E; background: #fff; padding: 4px 10px;
  border-radius: 20px; border: 1px solid #CCFBF1;
}
.hero h1 { margin: 0 0 4px; font-size: 26px; font-weight: 700; letter-spacing: -0.02em; }
.hero-sub { margin: 0; font-size: 14px; color: #64748B; }

.kpis { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.kpi {
  display: flex; align-items: center; gap: 14px;
  padding: 16px 18px; background: #fff;
  border: 1px solid #E2E8F0; border-radius: 14px;
}
.kpi-icon {
  width: 42px; height: 42px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; background: var(--kpi-bg); color: var(--kpi-color);
}
.kpi-value { margin: 0; font-size: 20px; font-weight: 700; }
.kpi-label { margin: 3px 0 0; font-size: 11.5px; color: #64748B; font-weight: 600; }

.toolbar {
  display: flex; align-items: center; gap: 12px;
  flex-wrap: wrap; padding: 12px 16px;
  background: #fff; border: 1px solid #E2E8F0; border-radius: 12px;
}
.search-box { position: relative; flex: 1 1 280px; max-width: 420px; }
.search-icon {
  position: absolute; left: 14px; top: 50%;
  transform: translateY(-50%); color: #94A3B8; pointer-events: none;
}
.search-input {
  width: 100%; padding: 10px 40px;
  border: 1.5px solid #E2E8F0; border-radius: 10px;
  font-size: 13.5px; font-family: inherit;
  outline: none; box-sizing: border-box; transition: all .2s;
}
.search-input:focus { border-color: #0F766E; box-shadow: 0 0 0 3px rgba(15,118,110,.1); }
.search-clear {
  position: absolute; right: 10px; top: 50%;
  transform: translateY(-50%); background: none; border: none;
  color: #94A3B8; cursor: pointer; padding: 5px; border-radius: 6px;
}
.tabs-filter { display: flex; gap: 6px; }
.chip {
  padding: 6px 14px; border: 1px solid #E2E8F0; background: #fff;
  border-radius: 20px; font-size: 12px; font-weight: 600;
  color: #475569; cursor: pointer; transition: all .2s;
}
.chip:hover { background: #F0FDFA; border-color: #99F6E4; color: #0F766E; }
.chip.active { background: #0F766E; color: #fff; border-color: #0F766E; }

.btn-primary {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 10px 20px; background: #0F766E; color: #fff;
  border: none; border-radius: 10px; font-size: 13.5px;
  font-weight: 700; cursor: pointer; font-family: inherit;
}
.btn-primary:hover { background: #115E59; transform: translateY(-1px); box-shadow: 0 6px 16px -4px rgba(15,118,110,.4); }
.btn-link {
  background: none; border: none; color: #0F766E;
  font-size: 13px; font-weight: 700; cursor: pointer;
  padding: 8px 16px; border-radius: 8px;
}
.btn-link:hover { background: #F0FDFA; }

.alert { display: flex; align-items: flex-start; gap: 10px; padding: 12px 16px; border-radius: 10px; font-size: 13px; }
.alert-error { background: #FEF2F2; color: #991B1B; border: 1px solid #FECACA; }
.alert-action {
  margin-left: auto; background: none; border: none;
  color: inherit; font-weight: 700; font-size: 12px;
  cursor: pointer; text-decoration: underline;
}

.skeleton-table { background: #fff; border: 1px solid #E2E8F0; border-radius: 14px; overflow: hidden; }
.skeleton-row {
  display: grid; grid-template-columns: 1fr 2fr 2fr 1fr;
  gap: 20px; padding: 18px 20px; border-bottom: 1px solid #F1F5F9;
}
.skeleton-row:last-child { border-bottom: none; }
.skeleton-cell {
  height: 14px; border-radius: 6px;
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%; animation: shimmer 1.4s infinite;
}
.w-15 { width: 60%; }
.w-20 { width: 80%; }
.w-30 { width: 90%; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

.empty-state {
  display: flex; flex-direction: column; align-items: center;
  gap: 12px; padding: 72px 24px; background: #fff;
  border: 1px solid #E2E8F0; border-radius: 14px; text-align: center;
}
.empty-icon {
  width: 72px; height: 72px; border-radius: 50%;
  background: linear-gradient(135deg, #F0FDFA 0%, #ECFDF5 100%);
  color: #0F766E; display: flex; align-items: center;
  justify-content: center; margin-bottom: 8px;
}
.empty-state h3 { margin: 0; font-size: 17px; font-weight: 700; }
.empty-state p { margin: 0 0 8px; font-size: 13.5px; color: #64748B; }

.table-card { background: #fff; border: 1px solid #E2E8F0; border-radius: 14px; overflow: hidden; }
.table-wrap { overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; min-width: 900px; }
.data-table thead { background: #F8FAFC; }
.data-table th {
  text-align: left; padding: 14px 18px;
  font-size: 10.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .6px;
  color: #64748B; border-bottom: 1px solid #E2E8F0;
}
.data-table td {
  padding: 16px 18px; font-size: 13.5px; color: #0F172A;
  border-bottom: 1px solid #F1F5F9; vertical-align: middle;
}
.table-row:hover { background: #FAFBFC; }
.table-row.is-inactivo { opacity: .6; }
.der { text-align: right; }

.rif-pill {
  display: inline-block; font-family: ui-monospace, 'SF Mono', Menlo, monospace;
  font-size: 11.5px; font-weight: 700; color: #334155;
  background: #F1F5F9; padding: 3px 10px; border-radius: 6px;
  border: 1px solid #E2E8F0;
}
.empresa-cell { min-width: 200px; }
.empresa-nombre { margin: 0; font-size: 13.5px; font-weight: 700; }
.empresa-dir {
  margin: 3px 0 0; font-size: 11.5px; color: #94A3B8;
  max-width: 260px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.contacto-cell { min-width: 160px; }
.contacto-nombre { margin: 0 0 2px; font-size: 12.5px; font-weight: 600; }
.contacto-line {
  margin: 1px 0 0; font-size: 11.5px; color: #64748B;
  display: flex; align-items: center; gap: 4px;
}

.tipo-pill {
  display: inline-block; padding: 3px 10px;
  border-radius: 20px; font-size: 11.5px; font-weight: 700;
  background: #F1F5F9; color: #475569; border: 1px solid #E2E8F0;
}
.tipo-pill[data-tipo="Medicamentos"] { background: #EFF6FF; color: #2563EB; border-color: #BFDBFE; }
.tipo-pill[data-tipo="Alimentos"]    { background: #FFFBEB; color: #D97706; border-color: #FDE68A; }
.tipo-pill[data-tipo="Mixto"]        { background: #F5F3FF; color: #7C3AED; border-color: #DDD6FE; }

.status-pill {
  display: inline-block; padding: 3px 10px; border-radius: 20px;
  font-size: 11.5px; font-weight: 700; border: 1px solid;
}
.status-pill.is-active { background: #ECFDF5; color: #059669; border-color: #A7F3D0; }
.status-pill.is-inactive { background: #FEF2F2; color: #DC2626; border-color: #FECACA; }

.acciones { display: flex; gap: 6px; justify-content: flex-end; }
.icon-action {
  width: 32px; height: 32px; border-radius: 8px;
  border: 1px solid #E2E8F0; background: #fff; color: #64748B;
  display: inline-flex; align-items: center; justify-content: center;
  cursor: pointer; transition: all .2s;
}
.icon-action:hover { background: #F0FDFA; border-color: #99F6E4; color: #0F766E; }
.icon-action.icon-danger:hover { background: #FEF2F2; border-color: #FECACA; color: #DC2626; }
.icon-action.icon-success:hover { background: #ECFDF5; border-color: #A7F3D0; color: #059669; }

.table-footer {
  padding: 12px 20px; border-top: 1px solid #E2E8F0;
  background: #FAFBFC; font-size: 12px; font-weight: 600; color: #64748B;
}

@media (max-width: 1024px) { .kpis { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 768px) {
  .proveedores-view { padding: 16px 14px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero h1 { font-size: 22px; }
  .hero-right { width: 100%; }
  .hero-right .btn-primary { width: 100%; justify-content: center; }
  .kpis { grid-template-columns: 1fr; gap: 10px; }
}
</style>