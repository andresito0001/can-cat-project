<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import * as clientesApi from '@/api/clientes.api'
import { UserPlus, Search, X, AlertCircle, PawPrint } from 'lucide-vue-next'

const router = useRouter()

const clientes = ref([])
const filtro = ref('')
const isLoading = ref(false)
const errorMessage = ref(null)

let debounceTimer = null

async function cargarClientes() {
  isLoading.value = true
  errorMessage.value = null
  try {
    const { data } = await clientesApi.getAll(filtro.value)
    clientes.value = data
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'No se pudo cargar el listado de clientes.'
  } finally {
    isLoading.value = false
  }
}

function limpiarFiltro() {
  filtro.value = ''
}

function formatearFecha(fecha) {
  if (!fecha) return '—'
  return new Date(fecha).toLocaleDateString('es-VE', { day: '2-digit', month: 'short', year: 'numeric' })
}

onMounted(cargarClientes)

watch(filtro, () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(cargarClientes, 350)
})

onBeforeUnmount(() => clearTimeout(debounceTimer))
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Gestión de Clientes</h1>
        <p class="page-subtitle">Registro, búsqueda y administración de clientes de la clínica.</p>
      </div>
      <button class="btn-primary" @click="router.push('/recepcion/clientes/nuevo')">
        <UserPlus :size="18" />
        Añadir nuevo cliente
      </button>
    </div>

    <div class="toolbar">
      <div class="search-box">
        <Search :size="18" class="search-icon" />
        <input
          v-model="filtro"
          type="text"
          placeholder="Buscar por nombre o documento de identidad..."
        />
        <button v-if="filtro" class="clear-btn" @click="limpiarFiltro" tabindex="-1">
          <X :size="16" />
        </button>
      </div>
      <span class="counter">{{ clientes.length }} cliente(s)</span>
    </div>

    <div v-if="errorMessage" class="alert error">
      <AlertCircle :size="18" />
      <span>{{ errorMessage }}</span>
    </div>

    <div class="table-card">
      <div v-if="isLoading" class="table-state">
        <span class="spinner"></span> Cargando clientes...
      </div>

      <div v-else-if="clientes.length === 0" class="table-state empty">
        <template v-if="filtro">
          No se encontraron clientes que coincidan con "{{ filtro }}".
        </template>
        <template v-else>
          Aún no hay clientes registrados. Usa el botón <strong>"Añadir nuevo cliente"</strong>.
        </template>
      </div>

      <table v-else class="data-table">
        <thead>
          <tr>
            <th>Cliente</th>
            <th>Documento</th>
            <th>Teléfono</th>
            <th>Ciudad</th>
            <th>Registrado</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="cliente in clientes" :key="cliente.id">
            <td class="cell-name">{{ cliente.nombreCompleto }}</td>
            <td>{{ cliente.documentoIdentidad }}</td>
            <td>{{ cliente.telefonoPrincipal }}</td>
            <td>{{ cliente.ciudad || '—' }}</td>
            <td>{{ formatearFecha(cliente.createdAt) }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }

.page-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; flex-wrap: wrap; }
.page-title { font-size: 24px; font-weight: 700; color: #1E293B; margin: 0; letter-spacing: -0.03em; }
.page-subtitle { color: #64748B; font-size: 14px; margin: 4px 0 0; }

.btn-primary {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 12px 20px; background: #0F766E; color: #fff; border: none;
  border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer;
  transition: all 0.2s ease; font-family: inherit;
}
.btn-primary:hover { background: #115E59; transform: translateY(-1px); }

.toolbar { display: flex; align-items: center; gap: 16px; }
.search-box { position: relative; flex: 1; max-width: 420px; }
.search-box input {
  width: 100%; padding: 12px 40px 12px 42px; border: 1.5px solid #E2E8F0;
  border-radius: 10px; font-size: 14px; color: #1E293B; background: #F8FAFC;
  outline: none; transition: all 0.2s ease; box-sizing: border-box; font-family: inherit;
}
.search-box input:focus { border-color: #0F766E; background: #fff; box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.1); }
.search-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: #94A3B8; }
.clear-btn {
  position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
  background: none; border: none; color: #94A3B8; cursor: pointer; padding: 4px; display: flex;
}
.clear-btn:hover { color: #EF4444; }
.counter { color: #64748B; font-size: 13px; font-weight: 500; }

.alert {
  display: flex; align-items: center; gap: 8px; padding: 12px 16px;
  border-radius: 8px; font-size: 13px; font-weight: 500;
}
.alert.error { color: #EF4444; background: #FEF2F2; border: 1px solid #FECACA; }

.table-card { background: #fff; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.03), 0 20px 40px -4px rgba(15,118,110,0.08); overflow: hidden; }

.table-state { padding: 48px 24px; text-align: center; color: #64748B; font-size: 14px; }
.spinner {
  display: inline-block; width: 16px; height: 16px; border: 2px solid rgba(15,118,110,0.2);
  border-top-color: #0F766E; border-radius: 50%; animation: spin 0.6s linear infinite;
  margin-right: 8px; vertical-align: middle;
}
@keyframes spin { to { transform: rotate(360deg); } }

.data-table { width: 100%; border-collapse: collapse; }
.data-table th {
  text-align: left; padding: 14px 20px; font-size: 12px; font-weight: 600;
  color: #64748B; text-transform: uppercase; letter-spacing: 0.05em;
  border-bottom: 1px solid #E2E8F0; background: #F8FAFC;
}
.data-table td { padding: 14px 20px; font-size: 14px; color: #1E293B; border-bottom: 1px solid #F1F5F9; }
.data-table tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover { background: #F8FAFC; }
.cell-name { font-weight: 600; }
</style>