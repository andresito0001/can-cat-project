<template>
  <div class="roles-view">
    <ToastContainer />

    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow"><Shield :size="12" /> Administración · Roles</p>
        <h1>Roles y Permisos</h1>
        <p class="hero-sub">Consulta los roles definidos y los permisos asociados.</p>
      </div>
    </header>

    <div v-if="cargando" class="loading-state"><div class="spin"></div></div>

    <div v-else class="roles-grid">
      <article v-for="r in roles" :key="r.id" class="role-card">
        <header class="role-header">
          <div class="role-icon"><Shield :size="18" /></div>
          <div>
            <h3>{{ r.nombre }}</h3>
            <p class="role-id">Rol #{{ r.id }}</p>
          </div>
        </header>
        <p class="role-desc">{{ r.descripcion || 'Sin descripción' }}</p>
        <div class="permisos">
          <p class="permisos-label">Permisos</p>
          <div class="permisos-list">
            <span v-for="(perm, idx) in parsePermisos(r.permisosJs)" :key="idx" class="permiso-pill">
              {{ perm }}
            </span>
            <span v-if="parsePermisos(r.permisosJs).length === 0" class="sin-permisos">Sin permisos</span>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Shield } from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import { listarRoles } from '@/api/admin.api'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

const { toastError } = useToast()
const cargando = ref(true)
const roles = ref([])

onMounted(async () => {
  try {
    const { data } = await listarRoles()
    roles.value = data || []
  } catch (err) {
    toastError(getApiErrorMessage(err))
  } finally {
    cargando.value = false
  }
})

function parsePermisos(json) {
  if (!json) return []
  try {
    const arr = typeof json === 'string' ? JSON.parse(json) : json
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}
</script>

<style scoped>
.roles-view { max-width: 1500px; margin: 0 auto; padding: 24px 28px 48px; font-family: 'Inter', sans-serif; color: #0F172A; display: flex; flex-direction: column; gap: 20px; }
.hero { padding: 24px 28px; background: linear-gradient(135deg, #F5F3FF 0%, #FFFFFF 55%); border: 1px solid #EDE9FE; border-radius: 16px; }
.hero-eyebrow { display: inline-flex; align-items: center; gap: 6px; margin: 0 0 8px; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .7px; color: #7C3AED; background: #fff; padding: 4px 10px; border-radius: 20px; border: 1px solid #EDE9FE; }
.hero h1 { margin: 0 0 4px; font-size: 26px; font-weight: 700; }
.hero-sub { margin: 0; font-size: 14px; color: #64748B; }
.roles-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }
.role-card { background: #fff; border: 1px solid #E2E8F0; border-radius: 14px; padding: 22px; display: flex; flex-direction: column; gap: 14px; transition: all .2s; }
.role-card:hover { border-color: #C7D2FE; box-shadow: 0 8px 24px -12px rgba(124,58,237,.25); }
.role-header { display: flex; align-items: center; gap: 12px; }
.role-icon { width: 42px; height: 42px; border-radius: 12px; background: #F5F3FF; color: #7C3AED; display: flex; align-items: center; justify-content: center; }
.role-header h3 { margin: 0; font-size: 16px; font-weight: 700; }
.role-id { margin: 2px 0 0; font-size: 11.5px; color: #94A3B8; font-family: monospace; }
.role-desc { margin: 0; font-size: 13px; color: #64748B; }
.permisos-label { margin: 0 0 6px; font-size: 11px; font-weight: 700; text-transform: uppercase; color: #94A3B8; letter-spacing: .5px; }
.permisos-list { display: flex; flex-wrap: wrap; gap: 6px; }
.permiso-pill { font-size: 11px; font-weight: 600; padding: 3px 9px; border-radius: 12px; background: #F0FDFA; color: #0F766E; font-family: monospace; }
.sin-permisos { font-size: 12px; color: #94A3B8; font-style: italic; }
.loading-state { display: flex; justify-content: center; padding: 80px; }
.spin { width: 32px; height: 32px; border: 3px solid #E2E8F0; border-top-color: #7C3AED; border-radius: 50%; animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
</style>