<template>
  <header class="top-navbar">
    <!-- ═══ IZQUIERDA: hamburguesa + título ═══ -->
    <div class="navbar-left">
      <button
        class="icon-btn"
        type="button"
        :aria-label="isMobile ? 'Abrir menú' : 'Alternar menú'"
        :title="isMobile ? 'Abrir menú' : 'Alternar menú (Ctrl/Cmd+B)'"
        @click="$emit('toggle-sidebar')"
      >
        <Menu :size="18" />
      </button>

      <div class="page-info">
        <h1 class="page-title">{{ title }}</h1>
        <p class="page-date">{{ currentDate }}</p>
      </div>
    </div>

    <!-- ═══ DERECHA: user menu ═══ -->
    <div class="navbar-right">
      <div ref="menuRef" class="user-menu" :class="`tone-${userTone}`">
        <button
          ref="triggerRef"
          class="user-trigger"
          type="button"
          :aria-expanded="dropdownOpen"
          aria-haspopup="menu"
          @click="toggleDropdown"
        >
          <div class="user-avatar">{{ initials }}</div>
          <div class="user-trigger-info">
            <span class="user-trigger-name">{{ authStore.userName }}</span>
            <span class="user-trigger-role">{{ formatRole(authStore.userRole) }}</span>
          </div>
          <ChevronDown
            :size="14"
            class="user-trigger-chevron"
            :class="{ 'is-open': dropdownOpen }"
          />
        </button>

        <Transition name="dropdown">
          <div
            v-if="dropdownOpen"
            class="user-dropdown"
            role="menu"
            aria-orientation="vertical"
          >
            <!-- Header del dropdown -->
            <div class="dropdown-header">
              <div class="dropdown-avatar">{{ initials }}</div>
              <div class="dropdown-info">
                <span class="dropdown-name">{{ authStore.userName }}</span>
                <span class="dropdown-email">{{ authStore.user?.correoElectronico || '' }}</span>
              </div>
              <span class="dropdown-rol-pill">{{ formatRole(authStore.userRole) }}</span>
            </div>

            <div class="dropdown-divider" />

            <!-- Items -->
            <ul class="dropdown-list">
              <li>
                <button class="dropdown-item" type="button" role="menuitem" @click="onMiPerfil">
                  <UserCircle :size="16" />
                  <span>Mi perfil</span>
                </button>
              </li>
              <li v-if="esAdmin">
                <button class="dropdown-item" type="button" role="menuitem" @click="onConfiguracion">
                  <Settings :size="16" />
                  <span>Configuración</span>
                </button>
              </li>
            </ul>

            <div class="dropdown-divider" />

            <ul class="dropdown-list">
              <li>
                <button
                  class="dropdown-item dropdown-item--danger"
                  type="button"
                  role="menuitem"
                  @click="onLogout"
                >
                  <LogOut :size="16" />
                  <span>Cerrar sesión</span>
                </button>
              </li>
            </ul>
          </div>
        </Transition>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Menu, ChevronDown, UserCircle, Settings, LogOut } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth.store'

const props = defineProps({
  title:    { type: String,  default: 'Panel' },
  isMobile: { type: Boolean, default: false },
})

defineEmits(['toggle-sidebar'])

const router = useRouter()
const authStore = useAuthStore()

/* ═══════════════════════════════════════════════════════════════
   FECHA
   ═══════════════════════════════════════════════════════════════ */
const currentDate = computed(() =>
  new Date().toLocaleDateString('es-VE', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
)

/* ═══════════════════════════════════════════════════════════════
   INICIALES
   ═══════════════════════════════════════════════════════════════ */
const initials = computed(() => {
  const name = authStore.userName || 'U'
  return name
    .split(' ')
    .filter(Boolean)
    .map((n) => n[0])
    .join('')
    .substring(0, 2)
    .toUpperCase()
})

function formatRole(rol) {
  const map = {
    Cliente: 'Cliente',
    Recepcionista: 'Recepcionista',
    Veterinario: 'Veterinario',
    Encargado_Almacen: 'Encargado de Almacén',
    Administrador: 'Administrador',
  }
  return map[rol] || rol || ''
}

/* ═══════════════════════════════════════════════════════════════
   TONO POR ROL (mismo que el Sidebar)
   ═══════════════════════════════════════════════════════════════ */
const userTone = computed(() => {
  const map = {
    Cliente: 'brand',
    Recepcionista: 'info',
    Veterinario: 'success',
    Encargado_Almacen: 'warning',
    Administrador: 'purple',
  }
  return map[authStore.userRole] || 'brand'
})

const esAdmin = computed(() => authStore.userRole === 'Administrador')

/* ═══════════════════════════════════════════════════════════════
   DROPDOWN
   ═══════════════════════════════════════════════════════════════ */
const dropdownOpen = ref(false)
const menuRef = ref(null)

function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
}
function closeDropdown() {
  dropdownOpen.value = false
}

function onClickOutside(e) {
  if (!dropdownOpen.value) return
  if (menuRef.value && !menuRef.value.contains(e.target)) closeDropdown()
}
function onKeydown(e) {
  if (e.key === 'Escape' && dropdownOpen.value) closeDropdown()
}

onMounted(() => {
  document.addEventListener('click', onClickOutside)
  document.addEventListener('keydown', onKeydown)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', onClickOutside)
  document.removeEventListener('keydown', onKeydown)
})

/* ═══════════════════════════════════════════════════════════════
   ACCIONES
   ═══════════════════════════════════════════════════════════════ */
function onMiPerfil() {
  closeDropdown()
  router.push('/perfil')
}

function onConfiguracion() {
  closeDropdown()
  router.push('/admin/configuracion')
}

function onLogout() {
  closeDropdown()
  authStore.logout()
}
</script>

<style scoped>
/* ═══════════════════════════════════════════════════════════════
   BASE
   ═══════════════════════════════════════════════════════════════ */
.top-navbar {
  height: 72px;
  background: var(--bg-surface);
  border-bottom: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-xs);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-5);
  padding: 0 var(--space-6) 0 var(--space-5);
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  font-family: var(--font-sans);
}

/* ═══════════════════════════════════════════════════════════════
   IZQUIERDA
   ═══════════════════════════════════════════════════════════════ */
.navbar-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.icon-btn {
  width: 38px;
  height: 38px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: all var(--duration-fast) var(--ease-out);
}
.icon-btn:hover {
  background: var(--neutral-100);
  border-color: var(--border-strong);
  color: var(--brand-700);
}
.icon-btn:focus-visible {
  outline: none;
  box-shadow: var(--shadow-focus);
}

.page-info { min-width: 0; }

.page-title {
  font-family: var(--font-brand);
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0;
  line-height: 1.2;
  letter-spacing: var(--tracking-tight);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.page-date {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 2px 0 0 0;
  text-transform: capitalize;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: var(--font-medium);
}

/* ═══════════════════════════════════════════════════════════════
   USER MENU
   ═══════════════════════════════════════════════════════════════ */
.user-menu {
  --accent:       var(--brand-700);
  --accent-soft:  var(--brand-50);
  --accent-ring:  rgba(15, 118, 110, 0.15);

  position: relative;
  flex-shrink: 0;
}

.tone-brand   { --accent: var(--brand-700);   --accent-soft: var(--brand-50);   --accent-ring: rgba(15, 118, 110, 0.15); }
.tone-info    { --accent: var(--info-600);    --accent-soft: var(--info-50);    --accent-ring: rgba(37, 99, 235, 0.15); }
.tone-success { --accent: var(--success-600); --accent-soft: var(--success-50); --accent-ring: rgba(5, 150, 105, 0.15); }
.tone-warning { --accent: var(--warning-600); --accent-soft: var(--warning-50); --accent-ring: rgba(217, 119, 6, 0.15); }
.tone-purple  { --accent: var(--purple-600);  --accent-soft: var(--purple-50);  --accent-ring: rgba(124, 58, 237, 0.15); }

.user-trigger {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-1) var(--space-3) var(--space-1) var(--space-2);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--duration-fast) var(--ease-out);
}
.user-trigger:hover {
  background: var(--bg-surface-alt);
  border-color: var(--border-strong);
}
.user-trigger:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--accent-ring);
  border-color: var(--accent);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  background: var(--accent);
  color: var(--text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  flex-shrink: 0;
  transition: background-color var(--duration-base) var(--ease-out);
}

.user-trigger-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
  text-align: left;
}

.user-trigger-name {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.15;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 160px;
}

.user-trigger-role {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  line-height: 1.15;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 160px;
}

.user-trigger-chevron {
  color: var(--text-tertiary);
  flex-shrink: 0;
  transition: transform var(--duration-base) var(--ease-out),
              color var(--duration-fast) var(--ease-out);
}
.user-trigger-chevron.is-open {
  transform: rotate(180deg);
  color: var(--accent);
}

/* ═══════════════════════════════════════════════════════════════
   DROPDOWN
   ═══════════════════════════════════════════════════════════════ */
.user-dropdown {
  position: absolute;
  top: calc(100% + var(--space-2));
  right: 0;
  min-width: 300px;
  max-width: 340px;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xl);
  overflow: hidden;
  z-index: var(--z-dropdown);
}

.dropdown-header {
  position: relative;
  display: grid;
  grid-template-columns: auto 1fr;
  gap: var(--space-3);
  align-items: center;
  padding: var(--space-5) var(--space-5) var(--space-4);
  background: linear-gradient(135deg, var(--accent-soft) 0%, var(--bg-surface) 80%);
  min-width: 0;
}

.dropdown-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-xl);
  background: var(--accent);
  color: var(--text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  flex-shrink: 0;
  box-shadow: 0 4px 12px -4px rgba(15, 23, 42, 0.25);
}

.dropdown-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.dropdown-name {
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-email {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dropdown-rol-pill {
  grid-column: 1 / -1;
  justify-self: start;
  display: inline-block;
  padding: 3px var(--space-3);
  margin-top: var(--space-1);
  border-radius: var(--radius-full);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  color: var(--accent);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.03em;
}

.dropdown-divider {
  height: 1px;
  background: var(--border-subtle);
}

.dropdown-list {
  list-style: none;
  margin: 0;
  padding: var(--space-2);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  width: 100%;
  padding: var(--space-3) var(--space-3);
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  font-family: inherit;
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--neutral-700);
  text-align: left;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.dropdown-item:hover {
  background: var(--accent-soft);
  color: var(--accent);
}
.dropdown-item:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--accent-ring);
}

.dropdown-item svg {
  color: var(--text-tertiary);
  flex-shrink: 0;
  transition: color var(--duration-fast) var(--ease-out);
}
.dropdown-item:hover svg { color: var(--accent); }

.dropdown-item--danger { color: var(--danger-600); }
.dropdown-item--danger svg { color: var(--danger-500); }
.dropdown-item--danger:hover {
  background: var(--danger-50);
  color: var(--danger-700);
}
.dropdown-item--danger:hover svg { color: var(--danger-600); }

/* ═══════════════════════════════════════════════════════════════
   TRANSICIÓN DROPDOWN
   ═══════════════════════════════════════════════════════════════ */
.dropdown-enter-active {
  transition: opacity var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
}
.dropdown-leave-active {
  transition: opacity var(--duration-fast) var(--ease-out),
              transform var(--duration-fast) var(--ease-out);
}
.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-6px) scale(0.98);
}
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.98);
}

/* ═══════════════════════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════════════════════ */
@media (max-width: 1023px) {
  .top-navbar { padding: 0 var(--space-4); gap: var(--space-3); }
}

@media (max-width: 768px) {
  .top-navbar { padding: 0 var(--space-3); gap: var(--space-2); }
  .page-date { display: none; }
  .user-trigger-info { display: none; }
  .user-trigger {
    padding: var(--space-1);
    border: none;
    background: transparent;
  }
  .user-trigger:hover { background: var(--bg-surface-alt); }
  .user-avatar { width: 38px; height: 38px; }
  .user-trigger-chevron { display: none; }

  .user-dropdown {
    min-width: 0;
    width: calc(100vw - var(--space-6));
    max-width: 340px;
    right: 0;
  }
  .dropdown-header { padding: var(--space-4); }
}

@media (max-width: 480px) {
  .top-navbar { height: 64px; }
  .page-title { font-size: var(--text-lg); }
  .user-dropdown {
    right: calc(var(--space-3) * -1);
    width: calc(100vw - var(--space-5));
  }
}

/* ═══════════════════════════════════════════════════════════════
   A11Y
   ═══════════════════════════════════════════════════════════════ */
@media (prefers-reduced-motion: reduce) {
  .user-trigger-chevron,
  .user-avatar,
  .dropdown-item,
  .icon-btn {
    transition: none;
  }
  .dropdown-enter-active,
  .dropdown-leave-active { transition: opacity 0.01ms; }
}
</style>