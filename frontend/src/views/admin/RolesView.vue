<template>
  <div class="roles-view">
    <ToastContainer />

    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <Shield :size="12" /> Administración · Roles
        </span>
        <h1>Roles y Permisos</h1>
        <p class="page-header-sub">
          Consulta los roles definidos en el sistema y los permisos asociados a cada uno.
        </p>
      </div>
    </header>

    <!-- Loading -->
    <div v-if="cargando" class="roles-grid">
      <div v-for="i in 4" :key="i" class="skeleton-card">
        <div class="skeleton sk-line-md" />
        <div class="skeleton sk-line-lg" />
        <div class="sk-chips">
          <div class="skeleton sk-chip" />
          <div class="skeleton sk-chip" />
        </div>
      </div>
    </div>

    <!-- Empty -->
    <AppEmptyState
      v-else-if="!roles.length"
      :icon="Shield"
      title="Sin roles registrados"
      description="No se encontraron roles definidos en el sistema."
    />

    <!-- Grid de roles -->
    <div v-else class="roles-grid">
      <article
        v-for="r in roles"
        :key="r.id"
        class="role-card"
        :class="`role-tone-${tonoRol(r.nombre)}`"
      >
        <header class="role-header">
          <div class="role-icon">
            <component :is="iconoRol(r.nombre)" :size="22" />
          </div>
          <div class="role-title">
            <h3>{{ r.nombre }}</h3>
            <p class="role-id">Rol #{{ r.id }}</p>
          </div>
        </header>

        <p class="role-desc">{{ r.descripcion || 'Sin descripción' }}</p>

        <div class="role-permisos">
          <p class="permisos-label">
            <KeyRound :size="12" />
            Permisos ({{ permisosDe(r).length }})
          </p>
          <div class="permisos-list">
            <span
              v-for="(perm, idx) in permisosDe(r)"
              :key="idx"
              class="permiso-pill"
            >
              {{ perm }}
            </span>
            <span v-if="permisosDe(r).length === 0" class="sin-permisos">
              Sin permisos asignados
            </span>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Shield, KeyRound, Crown, Stethoscope, Headset, Package, User } from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
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

function permisosDe(rol) {
  if (!rol.permisosJs) return []
  try {
    const arr = typeof rol.permisosJs === 'string' ? JSON.parse(rol.permisosJs) : rol.permisosJs
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

function iconoRol(nombre) {
  const n = String(nombre || '').toLowerCase()
  if (n.includes('admin')) return Crown
  if (n.includes('veterin')) return Stethoscope
  if (n.includes('recep')) return Headset
  if (n.includes('almac')) return Package
  if (n.includes('client')) return User
  return Shield
}

function tonoRol(nombre) {
  const n = String(nombre || '').toLowerCase()
  if (n.includes('admin')) return 'brand'
  if (n.includes('veterin')) return 'info'
  if (n.includes('recep')) return 'purple'
  if (n.includes('almac')) return 'warning'
  if (n.includes('client')) return 'success'
  return 'neutral'
}
</script>

<style scoped>
.roles-view {
  max-width: 1500px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-7) var(--space-12);
  font-family: var(--font-sans);
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.roles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--space-4);
}

/* ═══ CARD BASE ═══ */
.role-card {
  position: relative;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  padding: var(--space-5) var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  transition: all var(--duration-base) var(--ease-out);
  overflow: hidden;
  box-shadow: var(--shadow-xs);
}
.role-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--tone-color, var(--brand-700));
  transition: height var(--duration-base) var(--ease-out);
}
.role-card:hover {
  border-color: var(--tone-color, var(--brand-700));
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
.role-card:hover::before { height: 4px; }

/* Tonalidades */
.role-tone-brand   { --tone-color: var(--brand-700);    --tone-bg: var(--brand-50);    --tone-fg: var(--brand-700); }
.role-tone-info    { --tone-color: var(--info-600);     --tone-bg: var(--info-50);     --tone-fg: var(--info-600); }
.role-tone-purple  { --tone-color: var(--purple-600);   --tone-bg: var(--purple-50);   --tone-fg: var(--purple-600); }
.role-tone-warning { --tone-color: var(--warning-600);  --tone-bg: var(--warning-50);  --tone-fg: var(--warning-600); }
.role-tone-success { --tone-color: var(--success-600);  --tone-bg: var(--success-50);  --tone-fg: var(--success-600); }
.role-tone-neutral { --tone-color: var(--neutral-600);  --tone-bg: var(--neutral-100); --tone-fg: var(--neutral-600); }

.role-header { display: flex; align-items: center; gap: var(--space-4); }
.role-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-xl);
  background: var(--tone-bg);
  color: var(--tone-fg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.role-title { min-width: 0; }
.role-title h3 {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
.role-id {
  margin: 2px 0 0;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-family: var(--font-mono);
}

.role-desc {
  margin: 0;
  font-size: var(--text-md);
  color: var(--text-secondary);
  line-height: var(--leading-normal);
}

.role-permisos {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-subtle);
}
.permisos-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}
.permisos-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}
.permiso-pill {
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  background: var(--tone-bg);
  color: var(--tone-fg);
  font-family: var(--font-mono);
  border: 1px solid transparent;
}
.sin-permisos {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  font-style: italic;
}

/* Skeleton */
.skeleton-card {
  padding: var(--space-5) var(--space-6);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.skeleton {
  background: linear-gradient(90deg, var(--neutral-100) 25%, var(--neutral-200) 50%, var(--neutral-100) 75%);
  background-size: 200% 100%;
  border-radius: var(--radius-md);
  animation: shimmer 1.4s infinite;
}
.sk-line-md { height: 14px; width: 40%; }
.sk-line-lg { height: 20px; width: 70%; }
.sk-chips { display: flex; gap: var(--space-2); }
.sk-chip { height: 22px; width: 80px; border-radius: var(--radius-full); }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* Responsive */
@media (max-width: 768px) {
  .roles-view { padding: var(--space-5) var(--space-4) var(--space-10); }
  .roles-grid { grid-template-columns: 1fr; }
}
@media (max-width: 480px) {
  .roles-view { padding: var(--space-4) var(--space-3) var(--space-8); }
  .role-card { padding: var(--space-4) var(--space-5); }
}
</style>