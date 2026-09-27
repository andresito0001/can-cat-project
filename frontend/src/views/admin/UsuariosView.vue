<template>
  <div class="usuarios-view">
    <ToastContainer />

    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow"><Users :size="12" /> Administración · Usuarios</p>
        <h1>Directorio de Usuarios</h1>
        <p class="hero-sub">Busca por correo, nombre o cédula, y administra cuentas y clientes.</p>
      </div>
    </header>

    <div class="toolbar">
      <input
        v-model="filtro"
        type="text"
        placeholder="Buscar por correo, nombre o cédula…"
        @input="debouncedCargar"
      />
      <select v-model="filtroRol" @change="cargar">
        <option value="">Todos los roles</option>
        <option v-for="r in roles" :key="r.id" :value="r.id">{{ r.nombre }}</option>
      </select>
      <select v-model="filtroEstado" @change="cargar">
        <option value="">Todos los estados</option>
        <option value="Activo">Activo</option>
        <option value="Inactivo">Inactivo</option>
        <option value="Bloqueado">Bloqueado</option>
      </select>
    </div>

    <div v-if="cargando" class="loading-state"><div class="spin"></div></div>

    <div v-else class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Correo</th>
            <th>Nombre</th>
            <th>Cédula</th>
            <th>Tipo</th>
            <th>Rol</th>
            <th>Estado</th>
            <th class="th-actions">Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in usuarios" :key="u.id">
            <td class="mono">#{{ u.id }}</td>
            <td>{{ u.correoElectronico }}</td>
            <td class="font-bold">{{ u.nombreCompleto || '—' }}</td>
            <td class="mono">{{ u.documentoIdentidad || '—' }}</td>
            <td>
              <span class="tipo-pill" :class="tipoClass(u.tipoUsuario)">
                {{ u.tipoUsuario }}
              </span>
            </td>
            <td><span class="rol-pill">{{ u.rolNombre || '—' }}</span></td>
            <td>
              <span class="badge" :class="badgeEstado(u.estado)">{{ u.estado }}</span>
            </td>
            <td class="td-actions">
              <button
                v-if="u.tipoUsuario === 'Cliente'"
                class="icon-action"
                title="Editar cliente"
                @click="abrirEditarCliente(u)"
              >
                <Pencil :size="15" />
              </button>
              <select
                class="estado-select"
                :value="u.estado"
                @change="cambiarEstado(u, $event.target.value)"
              >
                <option value="Activo">Activo</option>
                <option value="Inactivo">Inactivo</option>
                <option value="Bloqueado">Bloqueado</option>
              </select>
            </td>
          </tr>
          <tr v-if="usuarios.length === 0">
            <td colspan="8" class="empty-row">Sin resultados.</td>
          </tr>
        </tbody>
      </table>
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
import { ref, onMounted } from 'vue'
import { Users, Pencil } from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import ClienteFormModal from '@/components/admin/ClienteFormModal.vue'
import {
  listarUsuarios, listarRoles, cambiarEstadoUsuario,
  obtenerCliente,
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
  return 'badge-warn'
}

function tipoClass(tipo) {
  if (tipo === 'Personal') return 'tipo-personal'
  if (tipo === 'Cliente') return 'tipo-cliente'
  return 'tipo-otro'
}
</script>

<style scoped>
.usuarios-view { max-width: 1500px; margin: 0 auto; padding: 24px 28px 48px; font-family: 'Inter', sans-serif; color: #0F172A; display: flex; flex-direction: column; gap: 20px; }
.hero { padding: 24px 28px; background: linear-gradient(135deg, #EFF6FF 0%, #FFFFFF 55%); border: 1px solid #DBEAFE; border-radius: 16px; }
.hero-eyebrow { display: inline-flex; align-items: center; gap: 6px; margin: 0 0 8px; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .7px; color: #2563EB; background: #fff; padding: 4px 10px; border-radius: 20px; border: 1px solid #DBEAFE; }
.hero h1 { margin: 0 0 4px; font-size: 26px; font-weight: 700; }
.hero-sub { margin: 0; font-size: 14px; color: #64748B; }
.toolbar { display: flex; gap: 12px; }
.toolbar input { flex: 1; padding: 10px 14px; border: 1px solid #CBD5E1; border-radius: 10px; font-size: 13.5px; font-family: inherit; }
.toolbar select { padding: 10px 14px; border: 1px solid #CBD5E1; border-radius: 10px; font-size: 13.5px; font-family: inherit; background: #fff; }
.table-card { background: #fff; border: 1px solid #E2E8F0; border-radius: 14px; overflow: hidden; }
.data-table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 14px 18px; font-size: 10.5px; font-weight: 700; text-transform: uppercase; color: #64748B; border-bottom: 1px solid #E2E8F0; background: #F8FAFC; }
.data-table td { padding: 14px 18px; font-size: 13.5px; border-bottom: 1px solid #F1F5F9; }
.th-actions, .td-actions { text-align: right; }
.mono { font-family: monospace; color: #64748B; font-size: 12.5px; }
.font-bold { font-weight: 700; }
.rol-pill { font-size: 11.5px; font-weight: 700; padding: 3px 10px; border-radius: 20px; background: #F1F5F9; color: #334155; }
.tipo-pill { font-size: 11px; font-weight: 700; padding: 3px 9px; border-radius: 20px; }
.tipo-personal { background: #F0FDFA; color: #0F766E; }
.tipo-cliente { background: #EFF6FF; color: #2563EB; }
.tipo-otro { background: #F8FAFC; color: #64748B; }
.badge { padding: 3px 10px; border-radius: 20px; font-size: 11.5px; font-weight: 700; }
.badge-success { background: #ECFDF5; color: #059669; }
.badge-danger { background: #FEF2F2; color: #DC2626; }
.badge-warn { background: #FFFBEB; color: #B45309; }
.estado-select { padding: 6px 10px; border: 1px solid #CBD5E1; border-radius: 8px; font-size: 12.5px; font-family: inherit; background: #fff; cursor: pointer; margin-left: 6px; }
.icon-action { width: 32px; height: 32px; border-radius: 8px; border: 1px solid #E2E8F0; background: #fff; color: #64748B; cursor: pointer; display: inline-flex; align-items: center; justify-content: center; transition: all .15s; vertical-align: middle; }
.icon-action:hover { background: #EFF6FF; color: #2563EB; border-color: #BFDBFE; }
.empty-row { text-align: center; padding: 40px; color: #94A3B8; }
.loading-state { display: flex; justify-content: center; padding: 80px; }
.spin { width: 32px; height: 32px; border: 3px solid #E2E8F0; border-top-color: #0F766E; border-radius: 50%; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>