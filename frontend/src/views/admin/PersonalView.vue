<template>
  <div class="personal-view">
    <ToastContainer />

    <!-- ═══ HERO ═══ -->
    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <Users :size="12" /> Administración · Personal
        </span>
        <h1>Gestión de Personal</h1>
        <p class="page-header-sub">
          Crea, edita y administra las cuentas del equipo de la clínica.
        </p>
      </div>
      <div class="page-header-actions">
        <AppButton variant="primary" @click="abrirCrear">
          <template #icon-left><Plus :size="16" /></template>
          Nuevo Personal
        </AppButton>
      </div>
    </header>

    <!-- ═══ KPIs ═══ -->
    <section v-if="!cargando && personalList.length" class="kpis">
      <article class="kpi">
        <div class="kpi-icon kpi-icon--brand"><Users :size="18" /></div>
        <div>
          <p class="kpi-value">{{ personalList.length }}</p>
          <p class="kpi-label">Total personal</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon kpi-icon--success"><UserCheck :size="18" /></div>
        <div>
          <p class="kpi-value">{{ activos }}</p>
          <p class="kpi-label">Activos</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon kpi-icon--warning"><UserMinus :size="18" /></div>
        <div>
          <p class="kpi-value">{{ inactivos }}</p>
          <p class="kpi-label">Inactivos</p>
        </div>
      </article>
      <article class="kpi">
        <div class="kpi-icon kpi-icon--info"><Stethoscope :size="18" /></div>
        <div>
          <p class="kpi-value">{{ totalVeterinarios }}</p>
          <p class="kpi-label">Veterinarios</p>
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
          placeholder="Buscar por nombre, código o correo…"
          aria-label="Buscar personal"
        />
        <button
          v-if="filtro"
          type="button"
          class="search-clear"
          aria-label="Limpiar"
          @click="filtro = ''"
        >
          <X :size="13" />
        </button>
      </div>

      <div class="chips-row">
        <button
          type="button"
          class="chip"
          :class="{ active: !filtroCargo }"
          @click="filtroCargo = ''"
        >
          Todos
          <span class="chip-count">{{ personalList.length }}</span>
        </button>
        <button
          v-for="c in chipsCargos"
          :key="c.value"
          type="button"
          class="chip"
          :class="{ active: filtroCargo === c.value, 'is-empty': c.count === 0 }"
          :disabled="c.count === 0"
          @click="filtroCargo = filtroCargo === c.value ? '' : c.value"
        >
          {{ c.label }}
          <span class="chip-count">{{ c.count }}</span>
        </button>
      </div>
    </section>

    <!-- ═══ LOADING ═══ -->
    <div v-if="cargando" class="table-card">
      <div class="skeleton-table">
        <div v-for="i in 5" :key="i" class="skeleton-row">
          <div class="skeleton sk-cell" />
          <div class="skeleton sk-cell" />
          <div class="skeleton sk-cell" />
          <div class="skeleton sk-cell" />
          <div class="skeleton sk-cell" />
        </div>
      </div>
    </div>

    <!-- ═══ EMPTY ═══ -->
    <AppEmptyState
      v-else-if="!filtrados.length"
      :icon="filtro || filtroCargo ? Search : Users"
      :title="filtro || filtroCargo ? 'Sin resultados' : 'Sin personal registrado'"
      :description="filtro || filtroCargo
        ? 'No hay miembros del equipo que coincidan con los filtros.'
        : 'Comienza registrando al primer miembro del equipo.'"
    >
      <template #action>
        <AppButton v-if="filtro || filtroCargo" variant="secondary" @click="limpiarFiltros">
          Limpiar filtros
        </AppButton>
        <AppButton v-else variant="primary" @click="abrirCrear">
          <template #icon-left><Plus :size="16" /></template>
          Nuevo Personal
        </AppButton>
      </template>
    </AppEmptyState>

    <!-- ═══ TABLA (desktop) ═══ -->
    <div v-else class="table-card">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>Código</th>
              <th>Nombre</th>
              <th>Cargo</th>
              <th>Correo</th>
              <th>Horario</th>
              <th>Estado</th>
              <th class="col-actions">Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in filtrados" :key="p.personalId" class="data-row">
              <td><span class="sku-pill">{{ p.codigoEmpleado }}</span></td>
              <td>
                <div class="row-with-avatar">
                  <EntityAvatar :nombre="p.nombreCompleto" :tipo="tipoAvatar(p.cargo)" size="sm" />
                  <span class="font-bold">{{ p.nombreCompleto }}</span>
                </div>
              </td>
              <td>
                <span class="cargo-pill" :data-cargo="p.cargo">{{ formatCargo(p.cargo) }}</span>
              </td>
              <td class="cell-secondary">{{ p.correoElectronico || '—' }}</td>
              <td>
                <span class="horario-resumen" :title="resumenHorarioTooltip(p.horarioAtencion)">
                  {{ resumenHorario(p.horarioAtencion) }}
                </span>
              </td>
              <td>
                <span class="badge" :class="p.activo ? 'badge-success' : 'badge-danger'">
                  {{ p.activo ? 'Activo' : 'Inactivo' }}
                </span>
              </td>
              <td class="col-actions">
                <div class="actions">
                  <button class="icon-btn" type="button" title="Editar" @click="abrirEditar(p)">
                    <Pencil :size="14" />
                  </button>
                  <button
                    class="icon-btn"
                    :class="p.activo ? 'icon-btn--danger' : 'icon-btn--success'"
                    type="button"
                    :title="p.activo ? 'Desactivar' : 'Activar'"
                    @click="toggleActivo(p)"
                  >
                    <component :is="p.activo ? UserX : UserCheck" :size="14" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <PersonalFormModal
      v-model="modalVisible"
      :personal="personalSeleccionado"
      @guardado="onGuardado"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Users, Plus, Pencil, UserX, UserCheck, Search, X, Stethoscope, UserMinus } from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import PersonalFormModal from '@/components/admin/PersonalFormModal.vue'
import EntityAvatar from '@/components/ui/EntityAvatar.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import { listarPersonal, cambiarActivoPersonal } from '@/api/admin.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

const { toastError, toastSuccess } = useToast()
const cargando = ref(true)
const personalList = ref([])
const modalVisible = ref(false)
const personalSeleccionado = ref(null)
const filtro = ref('')
const filtroCargo = ref('')

const CARGOS = [
  { value: 'Veterinario', label: 'Veterinarios' },
  { value: 'Recepcionista', label: 'Recepcionistas' },
  { value: 'Encargado_Almacen', label: 'Almacén' },
  { value: 'Administrador', label: 'Administradores' },
]

const activos = computed(() => personalList.value.filter((p) => p.activo).length)
const inactivos = computed(() => personalList.value.length - activos.value)
const totalVeterinarios = computed(() =>
  personalList.value.filter((p) => p.cargo === 'Veterinario' && p.activo).length
)

const chipsCargos = computed(() =>
  CARGOS.map((c) => ({
    ...c,
    count: personalList.value.filter((p) => p.cargo === c.value).length,
  }))
)

const filtrados = computed(() => {
  const f = filtro.value.trim().toLowerCase()
  return personalList.value.filter((p) => {
    const matchTexto = !f ||
      p.nombreCompleto?.toLowerCase().includes(f) ||
      p.codigoEmpleado?.toLowerCase().includes(f) ||
      p.correoElectronico?.toLowerCase().includes(f)
    const matchCargo = !filtroCargo.value || p.cargo === filtroCargo.value
    return matchTexto && matchCargo
  })
})

onMounted(cargar)

async function cargar() {
  cargando.value = true
  try {
    const { data } = await listarPersonal()
    personalList.value = data || []
  } catch (err) {
    toastError(getApiErrorMessage(err))
  } finally {
    cargando.value = false
  }
}

function abrirCrear() {
  personalSeleccionado.value = null
  modalVisible.value = true
}

function abrirEditar(p) {
  personalSeleccionado.value = { ...p }
  modalVisible.value = true
}

function limpiarFiltros() {
  filtro.value = ''
  filtroCargo.value = ''
}

async function toggleActivo(p) {
  try {
    await cambiarActivoPersonal(p.personalId, !p.activo)
    toastSuccess(`Personal ${!p.activo ? 'activado' : 'desactivado'} correctamente`)
    await cargar()
  } catch (err) {
    toastError(getApiErrorMessage(err))
  }
}

function onGuardado() {
  toastSuccess('Personal guardado correctamente')
  cargar()
}

function formatCargo(cargo) {
  const map = {
    Veterinario: 'Veterinario',
    Recepcionista: 'Recepcionista',
    Encargado_Almacen: 'Almacén',
    Administrador: 'Administrador',
  }
  return map[cargo] || cargo
}

function tipoAvatar(cargo) {
  const map = {
    Veterinario: 'veterinario',
    Recepcionista: 'recepcionista',
    Administrador: 'admin',
  }
  return map[cargo] || 'persona'
}

function resumenHorario(horario) {
  if (!horario) return '—'
  const activos = []
  for (let d = 1; d <= 7; d++) {
    if (Array.isArray(horario[String(d)]) && horario[String(d)].length > 0) activos.push(d)
  }
  if (activos.length === 0) return '—'
  const nombres = ['', 'L', 'M', 'M', 'J', 'V', 'S', 'D']
  return activos.map((d) => nombres[d]).join(' · ')
}

function resumenHorarioTooltip(horario) {
  if (!horario) return 'Sin horario'
  const DIAS = ['', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo']
  const lineas = []
  for (let d = 1; d <= 7; d++) {
    const bloques = horario[String(d)] || []
    if (bloques.length > 0) {
      const rangos = bloques.map((b) => `${b.inicio}–${b.fin}`).join(', ')
      lineas.push(`${DIAS[d]}: ${rangos}`)
    }
  }
  return lineas.length ? lineas.join('\n') : 'Sin horario'
}
</script>

<style scoped>
.personal-view {
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
.kpi-icon--warning { background: var(--warning-50); color: var(--warning-600); }
.kpi-icon--success { background: var(--success-50); color: var(--success-600); }
.kpi-value {
  margin: 0;
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
  letter-spacing: var(--tracking-tight);
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
  gap: var(--space-4);
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
  max-width: 420px;
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
  display: flex;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
}
.search-clear:hover { color: var(--text-secondary); background: var(--neutral-100); }

.chips-row {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
}
.chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-3) var(--space-1) var(--space-4);
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
.chip:hover:not(:disabled) {
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
.chip.is-empty {
  opacity: 0.45;
  cursor: not-allowed;
  border-style: dashed;
}
.chip-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 18px;
  padding: 0 var(--space-2);
  border-radius: var(--radius-full);
  background: var(--neutral-100);
  color: var(--text-secondary);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
}
.chip.active .chip-count {
  background: rgba(255, 255, 255, 0.22);
  color: var(--text-inverse);
}

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

.sku-pill {
  font-family: var(--font-mono);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  color: var(--neutral-700);
  background: var(--neutral-100);
  padding: 3px var(--space-3);
  border-radius: var(--radius-sm);
  display: inline-block;
}
.row-with-avatar { display: flex; align-items: center; gap: var(--space-3); }
.font-bold { font-weight: var(--font-bold); }
.cell-secondary { color: var(--text-secondary); font-size: var(--text-md); }

.cargo-pill {
  display: inline-block;
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  background: var(--neutral-100);
  color: var(--neutral-600);
  border: 1px solid var(--border-subtle);
  white-space: nowrap;
}
.cargo-pill[data-cargo="Veterinario"]         { background: var(--info-50);    color: var(--info-700);    border-color: var(--info-200); }
.cargo-pill[data-cargo="Recepcionista"]       { background: var(--purple-50);  color: var(--purple-600);  border-color: var(--purple-100); }
.cargo-pill[data-cargo="Encargado_Almacen"]   { background: var(--warning-50); color: var(--warning-700); border-color: var(--warning-200); }
.cargo-pill[data-cargo="Administrador"]       { background: var(--brand-50);   color: var(--brand-700);   border-color: var(--brand-200); }

.horario-resumen {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  background: var(--brand-50);
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  cursor: help;
  white-space: nowrap;
  border: 1px solid var(--brand-200);
}

.col-actions { text-align: right; width: 100px; }
.actions { display: inline-flex; gap: var(--space-1); }
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
  background: var(--brand-50);
  border-color: var(--brand-200);
  color: var(--brand-700);
}
.icon-btn--danger:hover {
  background: var(--danger-50);
  border-color: var(--danger-200);
  color: var(--danger-600);
}
.icon-btn--success:hover {
  background: var(--success-50);
  border-color: var(--success-200);
  color: var(--success-600);
}

/* Skeleton */
.skeleton-table { padding: var(--space-2) 0; }
.skeleton-row {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr 2fr 1fr 1fr;
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
  .personal-view { padding: var(--space-5) var(--space-4) var(--space-10); }
  .kpis { grid-template-columns: 1fr; gap: var(--space-3); }
  .toolbar { padding: var(--space-3); }
  .search-box { max-width: 100%; flex: 1 1 100%; }
}
@media (max-width: 640px) {
  /* Tabla → tarjetas */
  .data-table thead { display: none; }
  .data-table, .data-table tbody, .data-table tr, .data-table td {
    display: block;
    width: 100%;
  }
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
  .row-with-avatar { padding-right: 80px; }
}
@media (max-width: 480px) {
  .kpis { gap: var(--space-2); }
  .kpi { padding: var(--space-3) var(--space-4); }
  .chips-row { gap: var(--space-1); }
  .chip { padding: var(--space-1) var(--space-2) var(--space-1) var(--space-3); font-size: var(--text-xs); }
}
</style>