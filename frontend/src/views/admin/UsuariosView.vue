<template>
  <div class="usuarios-view">
    <ToastContainer />

    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <Users :size="12" /> Administración · Usuarios
        </span>
        <h1>Directorio de Usuarios</h1>
        <p class="page-header-sub">
          Busca por correo, nombre o cédula, y administra cuentas y clientes.
        </p>
      </div>
    </header>

    <!-- KPIs -->
    <section v-if="!cargando && usuarios.length" class="kpis">
      <article class="kpi">
        <div class="kpi-icon kpi-icon--brand"><Users :size="18" /></div>
        <div>
          <p class="kpi-value">{{ usuarios.length }}</p>
          <p class="kpi-label">Usuarios totales</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon kpi-icon--success"><UserCheck :size="18" /></div>
        <div>
          <p class="kpi-value">{{ conteoActivos }}</p>
          <p class="kpi-label">Activos</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon kpi-icon--danger"><Lock :size="18" /></div>
        <div>
          <p class="kpi-value">{{ conteoBloqueados }}</p>
          <p class="kpi-label">Bloqueados</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon kpi-icon--info"><UserCog :size="18" /></div>
        <div>
          <p class="kpi-value">{{ conteoPersonal }}</p>
          <p class="kpi-label">Personal interno</p>
        </div>
      </article>
    </section>

    <!-- Toolbar -->
    <section class="toolbar">
      <div class="search-box">
        <Search :size="15" class="search-icon" />
        <input
          v-model="filtro"
          type="text"
          class="search-input"
          placeholder="Buscar por correo, nombre o cédula…"
          @input="debouncedCargar"
        />
        <button v-if="filtro" type="button" class="search-clear" @click="filtro = ''" aria-label="Limpiar">
          <X :size="13" />
        </button>
      </div>

      <div class="chips-row">
        <button type="button" class="chip" :class="{ active: !filtroRol }" @click="filtroRol = ''; cargar()">
          Todos
        </button>
        <button
          v-for="r in roles"
          :key="r.id"
          type="button"
          class="chip"
          :class="{ active: String(filtroRol) === String(r.id) }"
          @click="filtroRol = String(filtroRol) === String(r.id) ? '' : r.id; cargar()"
        >
          {{ r.nombre }}
        </button>
      </div>

      <div class="chips-row">
        <button
          v-for="e in estadosFiltro"
          :key="e.value"
          type="button"
          class="chip chip-estado"
          :class="[`is-${e.tone}`, { active: filtroEstado === e.value }]"
          @click="filtroEstado = filtroEstado === e.value ? '' : e.value; cargar()"
        >
          {{ e.label }}
        </button>
      </div>
    </section>

    <!-- Loading -->
    <div v-if="cargando" class="table-card">
      <div class="skeleton-table">
        <div v-for="i in 5" :key="i" class="skeleton-row">
          <div v-for="j in 6" :key="j" class="skeleton sk-cell" />
        </div>
      </div>
    </div>

    <!-- Empty -->
    <AppEmptyState
      v-else-if="!usuarios.length"
      :icon="Search"
      title="Sin resultados"
      description="No hay usuarios que coincidan con los filtros."
    >
      <template #action>
        <AppButton variant="secondary" @click="limpiarFiltros">Limpiar filtros</AppButton>
      </template>
    </AppEmptyState>

    <!-- Table -->
    <div v-else class="table-card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Usuario</th>
              <th>Documento</th>
              <th>Tipo</th>
              <th>Estado</th>
              <th class="col-actions">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in usuarios" :key="u.id" class="data-row">
              <td><span class="id-pill">#{{ u.id }}</span></td>
              <td>
                <div class="user-cell">
                  <EntityAvatar :nombre="u.nombreCompleto || u.correoElectronico" tipo="persona" size="sm" />
                  <div class="user-info">
                    <p class="user-name">{{ u.nombreCompleto || '—' }}</p>
                    <p class="user-email">{{ u.correoElectronico }}</p>
                  </div>
                </div>
              </td>
              <td>
                <span v-if="u.documentoIdentidad" class="mono-sm">{{ u.documentoIdentidad }}</span>
                <span v-else class="cell-empty">—</span>
              </td>
              <td>
                <span class="tipo-pill" :data-tipo="u.tipoUsuario">{{ u.tipoUsuario }}</span>
              </td>
              <td>
                <span class="badge" :class="badgeEstado(u.estado)">{{ u.estado }}</span>
              </td>
              <td class="col-actions">
                <div class="actions">
                  <button
                    v-if="u.tipoUsuario === 'Cliente'"
                    class="icon-btn"
                    type="button"
                    title="Editar cliente"
                    @click="abrirEditarCliente(u)"
                  >
                    <Pencil :size="14" />
                  </button>
                  <select
                    class="estado-select"
                    :value="u.estado"
                    @change="cambiarEstado(u, $event.target.value)"
                    aria-label="Cambiar estado"
                  >
                    <option value="Activo">Activo</option>
                    <option value="Inactivo">Inactivo</option>
                    <option value="Bloqueado">Bloqueado</option>
                  </select>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <ClienteFormModal
      v-if="clienteSeleccionado"
      v-model="modalClienteVisible"
      :cliente="clienteSeleccionado"
      @guardado="onClienteGuardado"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Users, Pencil, Search, X, UserCheck, Lock, UserCog } from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import ClienteFormModal from '@/components/admin/ClienteFormModal.vue'
import EntityAvatar from '@/components/ui/EntityAvatar.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import {
  listarUsuarios, listarRoles, cambiarEstadoUsuario, obtenerCliente,
} from '@/api/admin.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

const { toastError, toastSuccess } = useToast()

const cargando = ref(true)
const usuarios = ref([])
const roles = ref([])
const filtro = ref('')
const filtroRol = ref('')
const filtroEstado = ref('')

const modalClienteVisible = ref(false)
const clienteSeleccionado = ref(null)

const estadosFiltro = [
  { value: 'Activo', label: 'Activos', tone: 'success' },
  { value: 'Inactivo', label: 'Inactivos', tone: 'neutral' },
  { value: 'Bloqueado', label: 'Bloqueados', tone: 'danger' },
]

const conteoActivos = computed(() => usuarios.value.filter((u) => u.estado === 'Activo').length)
const conteoBloqueados = computed(() => usuarios.value.filter((u) => u.estado === 'Bloqueado').length)
const conteoPersonal = computed(() => usuarios.value.filter((u) => u.tipoUsuario === 'Personal').length)

let timer = null
function debouncedCargar() {
  clearTimeout(timer)
  timer = setTimeout(cargar, 300)
}

onMounted(async () => {
  try {
    const { data } = await listarRoles()
    roles.value = data || []
  } catch (err) {
    toastError(getApiErrorMessage(err))
  }
  cargar()
})

async function cargar() {
  cargando.value = true
  try {
    const params = {}
    if (filtro.value) params.filtro = filtro.value
    if (filtroRol.value) params.rolId = filtroRol.value
    if (filtroEstado.value) params.estado = filtroEstado.value
    const { data } = await listarUsuarios(params)
    usuarios.value = data || []
  } catch (err) {
    toastError(getApiErrorMessage(err))
  } finally {
    cargando.value = false
  }
}

function limpiarFiltros() {
  filtro.value = ''
  filtroRol.value = ''
  filtroEstado.value = ''
  cargar()
}

async function cambiarEstado(usuario, nuevoEstado) {
  if (nuevoEstado === usuario.estado) return
  try {
    await cambiarEstadoUsuario(usuario.id, nuevoEstado)
    usuario.estado = nuevoEstado
    toastSuccess(`Usuario #${usuario.id} → ${nuevoEstado}`)
  } catch (err) {
    toastError(getApiErrorMessage(err))
    await cargar()
  }
}

async function abrirEditarCliente(usuario) {
  try {
    const { data } = await obtenerCliente(usuario.entidadId)
    clienteSeleccionado.value = data
    modalClienteVisible.value = true
  } catch (err) {
    toastError(getApiErrorMessage(err))
  }
}

function onClienteGuardado() {
  toastSuccess('Cliente actualizado correctamente')
  cargar()
}

function badgeEstado(estado) {
  if (estado === 'Activo') return 'badge-success'
  if (estado === 'Bloqueado') return 'badge-danger'
  return 'badge-warning'
}
</script>

<style scoped>
.usuarios-view {
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

/* KPIs */
.kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
.kpi-icon--brand   { background: var(--brand-50);   color: var(--brand-700); }
.kpi-icon--info    { background: var(--info-50);    color: var(--info-600); }
.kpi-icon--success { background: var(--success-50); color: var(--success-600); }
.kpi-icon--danger  { background: var(--danger-50);  color: var(--danger-600); }
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

/* Toolbar */
.toolbar {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
}
.search-box {
  position: relative;
  flex: 1 1 280px;
  max-width: 400px;
}
.search-icon {
  position: absolute;
  left: var(--space-4);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  pointer-events: none;
}
.search-input {
  width: 100%;
  padding: var(--space-3) var(--space-10) var(--space-3) 40px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface-alt);
  border-radius: var(--radius-lg);
  font-size: var(--text-md);
  color: var(--text-primary);
  outline: none;
  box-sizing: border-box;
  transition: all var(--duration-fast) var(--ease-out);
}
.search-input:focus {
  border-color: var(--brand-700);
  background: var(--bg-surface);
  box-shadow: 0 0 0 4px var(--brand-100);
}
.search-clear {
  position: absolute;
  right: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
}
.search-clear:hover { color: var(--text-secondary); background: var(--neutral-100); }

.chips-row { display: flex; gap: var(--space-2); flex-wrap: wrap; }
.chip {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-4);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--neutral-600);
  cursor: pointer;
  transition: all var(--duration-base) var(--ease-out);
  font-family: inherit;
  white-space: nowrap;
}
.chip:hover {
  background: var(--brand-50);
  border-color: var(--brand-200);
  color: var(--brand-700);
}
.chip.active {
  background: var(--brand-700);
  color: var(--text-inverse);
  border-color: var(--brand-700);
  box-shadow: 0 2px 8px rgba(15, 118, 110, 0.25);
}
.chip.is-success.active { background: var(--success-600); border-color: var(--success-600); box-shadow: 0 2px 8px rgba(5, 150, 105, 0.25); }
.chip.is-danger.active  { background: var(--danger-600);  border-color: var(--danger-600);  box-shadow: 0 2px 8px rgba(220, 38, 38, 0.25); }
.chip.is-neutral.active { background: var(--neutral-600); border-color: var(--neutral-600); box-shadow: 0 2px 8px rgba(71, 85, 105, 0.25); }

/* Table */
.table-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
  overflow: hidden;
}
.table-wrap { overflow-x: auto; }
.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 900px;
}
.data-table thead { background: var(--bg-surface-alt); }
.data-table th {
  text-align: left;
  padding: var(--space-3) var(--space-5);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-subtle);
  white-space: nowrap;
}
.data-table td {
  padding: var(--space-4) var(--space-5);
  font-size: var(--text-md);
  color: var(--text-primary);
  border-bottom: 1px solid var(--neutral-100);
  vertical-align: middle;
}
.data-row:hover td { background: var(--bg-surface-alt); }
.data-row:last-child td { border-bottom: none; }

.id-pill {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  color: var(--neutral-700);
  background: var(--neutral-100);
  padding: 3px var(--space-3);
  border-radius: var(--radius-sm);
  display: inline-block;
}
.user-cell { display: flex; align-items: center; gap: var(--space-3); min-width: 0; }
.user-info { min-width: 0; }
.user-name {
  margin: 0;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.user-email {
  margin: 2px 0 0;
  font-size: var(--text-xs);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.mono-sm {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  background: var(--neutral-100);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
  color: var(--neutral-700);
}
.cell-empty { color: var(--neutral-300); }

.tipo-pill {
  display: inline-block;
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  white-space: nowrap;
  border: 1px solid;
}
.tipo-pill[data-tipo="Personal"] { background: var(--brand-50);   color: var(--brand-700);   border-color: var(--brand-200); }
.tipo-pill[data-tipo="Cliente"]  { background: var(--info-50);    color: var(--info-700);    border-color: var(--info-200); }
.tipo-pill[data-tipo="Usuario"]  { background: var(--neutral-100); color: var(--neutral-600); border-color: var(--border-subtle); }

.col-actions { text-align: right; width: 160px; }
.actions { display: inline-flex; gap: var(--space-2); align-items: center; }
.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--text-secondary);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all var(--duration-fast) var(--ease-out);
}
.icon-btn:hover {
  background: var(--info-50);
  border-color: var(--info-200);
  color: var(--info-600);
}
.estado-select {
  padding: var(--space-1) var(--space-3);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  background: var(--bg-surface);
  font-size: var(--text-sm);
  font-family: inherit;
  color: var(--text-primary);
  cursor: pointer;
  outline: none;
  transition: all var(--duration-fast) var(--ease-out);
}
.estado-select:focus { border-color: var(--brand-700); box-shadow: 0 0 0 3px var(--brand-100); }

/* Skeleton */
.skeleton-table { padding: var(--space-2) 0; }
.skeleton-row {
  display: grid;
  grid-template-columns: 0.6fr 2fr 1.2fr 1fr 1fr 1.4fr;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--neutral-100);
}
.skeleton-row:last-child { border-bottom: none; }
.skeleton {
  background: linear-gradient(90deg, var(--neutral-100) 25%, var(--neutral-200) 50%, var(--neutral-100) 75%);
  background-size: 200% 100%;
  border-radius: var(--radius-md);
  animation: shimmer 1.4s infinite;
}
.sk-cell { height: 14px; width: 80%; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Responsive */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .usuarios-view { padding: var(--space-5) var(--space-4) var(--space-10); }
  .kpis { grid-template-columns: 1fr; gap: var(--space-3); }
  .toolbar { padding: var(--space-3); }
  .search-box { max-width: 100%; flex: 1 1 100%; }
}
@media (max-width: 640px) {
  .data-table thead { display: none; }
  .data-table, .data-table tbody, .data-table tr, .data-table td { display: block; width: 100%; }
  .data-table tr {
    padding: var(--space-4);
    border-bottom: 1px solid var(--neutral-100);
    position: relative;
  }
  .data-table td {
    padding: 0;
    border: none;
    margin-top: var(--space-2);
    display: flex;
    align-items: center;
    gap: var(--space-2);
    font-size: var(--text-md);
  }
  .data-table td:first-child { margin-top: 0; }
  .col-actions {
    position: absolute;
    top: var(--space-4);
    right: var(--space-4);
    width: auto;
    margin: 0 !important;
  }
  .user-cell { padding-right: 160px; }
}
@media (max-width: 480px) {
  .kpi { padding: var(--space-3) var(--space-4); }
  .actions { flex-direction: column; align-items: flex-end; gap: var(--space-1); }
}
</style>