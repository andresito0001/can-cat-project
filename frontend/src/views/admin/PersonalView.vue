<template>
  <div class="personal-view">
    <ToastContainer />

    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow"><Users :size="12" /> Administración · Personal</p>
        <h1>Gestión de Personal</h1>
        <p class="hero-sub">Crea, edita y administra las cuentas del equipo de la clínica.</p>
      </div>
      <div class="hero-right">
        <button class="btn-primary" @click="abrirCrear"><Plus :size="15" /> Nuevo Personal</button>
      </div>
    </header>

    <div class="toolbar">
      <input v-model="filtro" type="text" placeholder="Buscar por nombre, código o correo…" />
      <select v-model="filtroCargo">
        <option value="">Todos los cargos</option>
        <option value="Veterinario">Veterinarios</option>
        <option value="Recepcionista">Recepcionistas</option>
        <option value="Encargado_Almacen">Encargados de Almacén</option>
        <option value="Administrador">Administradores</option>
      </select>
    </div>

    <div v-if="cargando" class="loading-state"><div class="spin"></div></div>

    <div v-else class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>Código</th>
            <th>Nombre Completo</th>
            <th>Cargo</th>
            <th>Correo Electrónico</th>
            <th>Horario</th>
            <th>Estado</th>
            <th class="th-actions">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in filtrados" :key="p.personalId">
            <td><span class="sku-pill">{{ p.codigoEmpleado }}</span></td>
            <td class="font-bold">{{ p.nombreCompleto }}</td>
            <td>{{ formatCargo(p.cargo) }}</td>
            <td>{{ p.correoElectronico || '—' }}</td>
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
            <td class="td-actions">
              <button class="icon-action" title="Editar" @click="abrirEditar(p)"><Pencil :size="15" /></button>
              <button class="icon-action" :title="p.activo ? 'Desactivar' : 'Activar'"
                      @click="toggleActivo(p)">
                <component :is="p.activo ? UserX : UserCheck" :size="15" />
              </button>
            </td>
          </tr>
          <tr v-if="filtrados.length === 0">
            <td colspan="7" class="empty-row">No hay personal que coincida con el filtro.</td>
          </tr>
        </tbody>
      </table>
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
import { Users, Plus, Pencil, UserX, UserCheck } from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import PersonalFormModal from '@/components/admin/PersonalFormModal.vue'
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
    Encargado_Almacen: 'Encargado de Almacén',
    Administrador: 'Administrador',
  }
  return map[cargo] || cargo
}

function resumenHorario(horario) {
  if (!horario) return '—'
  const activos = []
  for (let d = 1; d <= 7; d++) {
    if (Array.isArray(horario[String(d)]) && horario[String(d)].length > 0) {
      activos.push(d)
    }
  }
  if (activos.length === 0) return '—'
  const nombres = ['', 'L', 'M', 'M', 'J', 'V', 'S', 'D']
  return activos.map(d => nombres[d]).join(' · ')
}

function resumenHorarioTooltip(horario) {
  if (!horario) return 'Sin horario'
  const DIAS = ['', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo']
  const lineas = []
  for (let d = 1; d <= 7; d++) {
    const bloques = horario[String(d)] || []
    if (bloques.length > 0) {
      const rangos = bloques.map(b => `${b.inicio}–${b.fin}`).join(', ')
      lineas.push(`${DIAS[d]}: ${rangos}`)
    }
  }
  return lineas.length ? lineas.join('\n') : 'Sin horario'
}

</script>

<style scoped>
.personal-view { max-width: 1500px; margin: 0 auto; padding: 24px 28px 48px; font-family: 'Inter', sans-serif; color: #0F172A; display: flex; flex-direction: column; gap: 20px; }
.hero { display: flex; justify-content: space-between; align-items: center; padding: 24px 28px; background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%); border: 1px solid #CCFBF1; border-radius: 16px; }
.hero-eyebrow { display: inline-flex; align-items: center; gap: 6px; margin: 0 0 8px; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .7px; color: #0F766E; background: #fff; padding: 4px 10px; border-radius: 20px; border: 1px solid #CCFBF1; }
.hero h1 { margin: 0 0 4px; font-size: 26px; font-weight: 700; }
.hero-sub { margin: 0; font-size: 14px; color: #64748B; }
.btn-primary { display: inline-flex; align-items: center; gap: 7px; padding: 10px 18px; background: #0F766E; color: #fff; border: none; border-radius: 10px; font-size: 13.5px; font-weight: 700; cursor: pointer; font-family: inherit; }
.btn-primary:hover { background: #0E6862; }
.toolbar { display: flex; gap: 12px; }
.toolbar input, .toolbar select { padding: 10px 14px; border: 1px solid #CBD5E1; border-radius: 10px; font-size: 13.5px; font-family: inherit; background: #fff; }
.toolbar input { flex: 1; }
.table-card { background: #fff; border: 1px solid #E2E8F0; border-radius: 14px; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 14px 18px; font-size: 10.5px; font-weight: 700; text-transform: uppercase; color: #64748B; border-bottom: 1px solid #E2E8F0; background: #F8FAFC; }
.data-table td { padding: 16px 18px; font-size: 13.5px; border-bottom: 1px solid #F1F5F9; }
.th-actions, .td-actions { text-align: right; }
.sku-pill { font-family: monospace; font-size: 11.5px; font-weight: 700; color: #334155; background: #F1F5F9; padding: 3px 10px; border-radius: 6px; }
.font-bold { font-weight: 700; }
.badge { padding: 3px 10px; border-radius: 20px; font-size: 11.5px; font-weight: 700; }
.badge-success { background: #ECFDF5; color: #059669; }
.badge-danger { background: #FEF2F2; color: #DC2626; }
.icon-action { width: 32px; height: 32px; border-radius: 8px; border: 1px solid #E2E8F0; background: #fff; color: #64748B; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; margin-left: 6px; transition: all .15s; }
.icon-action:hover { background: #F0FDFA; color: #0F766E; border-color: #99F6E4; }
.empty-row { text-align: center; padding: 40px; color: #94A3B8; }
.loading-state { display: flex; justify-content: center; padding: 80px; }
.spin { width: 32px; height: 32px; border: 3px solid #E2E8F0; border-top-color: #0F766E; border-radius: 50%; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.horario-resumen {
  font-size: 12px;
  font-weight: 700;
  color: #0F766E;
  background: #F0FDFA;
  padding: 3px 9px;
  border-radius: 12px;
  cursor: help;
  white-space: nowrap;
}
</style>