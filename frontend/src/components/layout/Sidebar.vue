<template>
  <aside
    class="sidebar"
    :class="[
      `tone-${tone}`,
      {
        'is-open': open,
        'is-mobile': isMobile,
        'is-collapsed': isCollapsed && !isMobile,
      },
    ]"
    :aria-hidden="!open"
  >
    <!-- ═══ HEADER ═══ -->
    <div class="sidebar-header">
      <div class="brand">
        <div class="brand-mark" :title="isCollapsed && !isMobile ? 'CanCat' : ''">
          <PawPrint :size="24" />
        </div>
        <Transition name="fade-slide">
          <div v-if="!isCollapsed || isMobile" class="brand-text">
            <span class="brand-name">CanCat</span>
            <span class="brand-role">{{ rolLabel }}</span>
          </div>
        </Transition>
      </div>

      <!-- Botón colapsar (desktop) -->
      <button
        v-if="!isMobile"
        class="collapse-btn"
        type="button"
        :aria-label="isCollapsed ? 'Expandir menú' : 'Colapsar menú'"
        :title="isCollapsed ? 'Expandir menú (Ctrl/Cmd+B)' : 'Colapsar menú (Ctrl/Cmd+B)'"
        @click="$emit('toggle-collapse')"
      >
        <PanelLeftClose v-if="!isCollapsed" :size="16" />
        <PanelLeftOpen v-else :size="16" />
      </button>

      <!-- Botón cerrar (mobile) -->
      <button
        v-if="isMobile"
        class="close-btn"
        type="button"
        aria-label="Cerrar menú"
        @click="$emit('close')"
      >
        <X :size="18" />
      </button>
    </div>

    <!-- ═══ NAV ═══ -->
    <nav class="sidebar-nav" aria-label="Navegación principal">
      <router-link
        v-for="item in items"
        :key="item.route"
        :to="item.route"
        class="nav-item"
        :class="{ active: isActive(item.route) }"
        :aria-label="item.label"
        :data-tooltip="isCollapsed && !isMobile ? item.label : null"
      >
        <span class="nav-icon-wrap">
          <component :is="getIcon(item.icon)" :size="18" class="nav-icon" />
        </span>
        <Transition name="fade-slide">
          <span v-if="!isCollapsed || isMobile" class="nav-label">{{ item.label }}</span>
        </Transition>
        <span class="nav-active-bar" aria-hidden="true" />
      </router-link>
    </nav>

    <!-- ═══ FOOTER ═══ -->
    <div class="sidebar-footer">
      <button
        class="logout-btn"
        type="button"
        :aria-label="'Cerrar Sesión'"
        :data-tooltip="isCollapsed && !isMobile ? 'Cerrar Sesión' : null"
        @click="authStore.logout"
      >
        <LogOut :size="16" />
        <Transition name="fade-slide">
          <span v-if="!isCollapsed || isMobile">Cerrar Sesión</span>
        </Transition>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import {
  LayoutDashboard, PawPrint, CalendarPlus, CreditCard, Stethoscope,
  CalendarDays, UserPlus, Dog, CalendarCheck, Search,
  Package, ArrowDownToLine, AlertTriangle, LogOut, X,
  Users, Banknote, ClipboardList, List, UserCog, Shield, BarChart3, Settings,
  Truck, PanelLeftClose, PanelLeftOpen,
} from 'lucide-vue-next'

const props = defineProps({
  items:         { type: Array,   required: true },
  open:          { type: Boolean, default: false },
  isMobile:      { type: Boolean, default: false },
  isCollapsed:   { type: Boolean, default: false },
})

defineEmits(['close', 'toggle-collapse'])

const route = useRoute()
const authStore = useAuthStore()

/* ─── Mapa de iconos ─── */
const iconMap = {
  LayoutDashboard, PawPrint, CalendarPlus, CreditCard, Stethoscope,
  CalendarDays, UserPlus, Dog, CalendarCheck, Search,
  Package, ArrowDownToLine, AlertTriangle,
  Users, Banknote, ClipboardList, List, UserCog, Shield, BarChart3, Settings, Truck,
}
function getIcon(name) {
  return iconMap[name] || LayoutDashboard
}

/* ─── Tono por rol ─── */
const tone = computed(() => {
  const map = {
    Cliente: 'brand',
    Recepcionista: 'info',
    Veterinario: 'success',
    Encargado_Almacen: 'warning',
    Administrador: 'purple',
  }
  return map[authStore.userRole] || 'brand'
})

const rolLabel = computed(() => {
  const map = {
    Cliente: 'Área de cliente',
    Recepcionista: 'Recepción',
    Veterinario: 'Consultorio',
    Encargado_Almacen: 'Almacén',
    Administrador: 'Administración',
  }
  return map[authStore.userRole] || 'Panel'
})

/* ─── Item activo ─── */
function isActive(itemRoute) {
  if (!itemRoute) return false
  return route.path === itemRoute || route.path.startsWith(itemRoute + '/')
}
</script>

<style scoped>
/* ═══════════════════════════════════════════════════════════════
   BASE
   ═══════════════════════════════════════════════════════════════ */
.sidebar {
  --accent:       var(--brand-700);
  --accent-soft:  var(--brand-50);
  --accent-hover: var(--brand-100);
  --accent-ring:  rgba(15, 118, 110, 0.15);

  width: 260px;
  height: 100vh;
  background: var(--bg-surface);
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  z-index: var(--z-sidebar);
  font-family: var(--font-sans);
  transform: translateX(-100%);
  transition: transform var(--duration-slow) var(--ease-out),
              width var(--duration-slow) var(--ease-out);
}

.sidebar.is-open { transform: translateX(0); }
.sidebar.is-mobile.is-open { box-shadow: 0 8px 32px rgba(15, 23, 42, 0.25); }

/* Modo colapsado (solo desktop) */
.sidebar.is-collapsed {
  width: 76px;
}

/* ═══════════════════════════════════════════════════════════════
   TONOS POR ROL
   ═══════════════════════════════════════════════════════════════ */
.tone-brand   { --accent: var(--brand-700);    --accent-soft: var(--brand-50);    --accent-hover: var(--brand-100);    --accent-ring: rgba(15, 118, 110, 0.15); }
.tone-info    { --accent: var(--info-600);     --accent-soft: var(--info-50);     --accent-hover: var(--info-100);     --accent-ring: rgba(37, 99, 235, 0.15); }
.tone-success { --accent: var(--success-600);  --accent-soft: var(--success-50);  --accent-hover: var(--success-100);  --accent-ring: rgba(5, 150, 105, 0.15); }
.tone-warning { --accent: var(--warning-600);  --accent-soft: var(--warning-50);  --accent-hover: var(--warning-100);  --accent-ring: rgba(217, 119, 6, 0.15); }
.tone-purple  { --accent: var(--purple-600);   --accent-soft: var(--purple-50);   --accent-hover: var(--purple-100);   --accent-ring: rgba(124, 58, 237, 0.15); }

/* ═══════════════════════════════════════════════════════════════
   HEADER / BRAND
   ═══════════════════════════════════════════════════════════════ */
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
  padding: var(--space-5) var(--space-4) var(--space-4);
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
  min-height: 76px;
}

.sidebar.is-collapsed .sidebar-header {
  justify-content: center;
  padding: var(--space-5) var(--space-2) var(--space-4);
}

.brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
  flex: 1;
}

.sidebar.is-collapsed .brand { flex: 0 0 auto; }

.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-xl);
  background: var(--accent-soft);
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background-color var(--duration-base) var(--ease-out),
              color var(--duration-base) var(--ease-out);
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
  white-space: nowrap;
}

.brand-name {
  font-family: var(--font-brand);
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
  line-height: 1.15;
}

.brand-role {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--accent);
  line-height: 1.15;
  transition: color var(--duration-base) var(--ease-out);
}

/* ─── Botón colapsar ─── */
.collapse-btn {
  width: 30px;
  height: 30px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: all var(--duration-fast) var(--ease-out);
}
.collapse-btn:hover {
  background: var(--accent-soft);
  border-color: var(--accent);
  color: var(--accent);
}
.collapse-btn:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--accent-ring);
}

.sidebar.is-collapsed .collapse-btn { display: none; }

/* Botón re-expandir en modo colapsado: hover sobre el brand-mark */
.sidebar.is-collapsed .brand-mark { cursor: pointer; }
.sidebar.is-collapsed .brand-mark:hover {
  background: var(--accent);
  color: var(--text-inverse);
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
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
.close-btn:hover {
  background: var(--neutral-100);
  color: var(--text-primary);
  border-color: var(--border-strong);
}

/* ═══════════════════════════════════════════════════════════════
   NAV
   ═══════════════════════════════════════════════════════════════ */
.sidebar-nav {
  flex: 1;
  padding: var(--space-3) var(--space-3);
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: none;
}
.sidebar-nav::-webkit-scrollbar { display: none; }

.sidebar.is-collapsed .sidebar-nav {
  padding: var(--space-3) var(--space-2);
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-3) var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  text-decoration: none;
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  transition: background-color var(--duration-fast) var(--ease-out),
              color var(--duration-fast) var(--ease-out);
  overflow: visible;
}

.sidebar.is-collapsed .nav-item {
  justify-content: center;
  padding: var(--space-3);
  overflow: visible;
}

.nav-item:hover {
  background: var(--accent-soft);
  color: var(--accent);
}

.nav-item.active {
  background: var(--accent-soft);
  color: var(--accent);
  font-weight: var(--font-bold);
}

.nav-icon-wrap {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform var(--duration-base) var(--ease-out);
}
.nav-item.active .nav-icon-wrap {
  transform: scale(1.05);
}

.nav-icon { transition: color var(--duration-fast) var(--ease-out); }

.nav-label {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-active-bar {
  position: absolute;
  left: 0;
  top: 50%;
  width: 3px;
  height: 0;
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  background: var(--accent);
  transform: translateY(-50%);
  transition: height var(--duration-base) var(--ease-out);
}
.nav-item.active .nav-active-bar { height: 22px; }

/* ─── Tooltip en modo colapsado ─── */
.sidebar.is-collapsed .nav-item::after,
.sidebar.is-collapsed .logout-btn::after {
  content: attr(data-tooltip);
  position: absolute;
  left: calc(100% + var(--space-2));
  top: 50%;
  transform: translateY(-50%) translateX(-6px);
  padding: var(--space-2) var(--space-3);
  background: var(--neutral-900);
  color: var(--text-inverse);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  border-radius: var(--radius-md);
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity var(--duration-fast) var(--ease-out),
              transform var(--duration-fast) var(--ease-out);
  z-index: 100;
  box-shadow: var(--shadow-lg);
}
.sidebar.is-collapsed .nav-item:hover::after,
.sidebar.is-collapsed .logout-btn:hover::after {
  opacity: 1;
  transform: translateY(-50%) translateX(0);
}

/* ═══════════════════════════════════════════════════════════════
   FOOTER
   ═══════════════════════════════════════════════════════════════ */
.sidebar-footer {
  padding: var(--space-3);
  border-top: 1px solid var(--border-subtle);
  flex-shrink: 0;
}
.sidebar.is-collapsed .sidebar-footer {
  padding: var(--space-3) var(--space-2);
}

.logout-btn {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  font-family: inherit;
  cursor: pointer;
  width: 100%;
  text-align: left;
  transition: all var(--duration-fast) var(--ease-out);
  overflow: visible;
}
.sidebar.is-collapsed .logout-btn {
  justify-content: center;
  padding: var(--space-3);
}
.logout-btn:hover {
  background: var(--danger-50);
  color: var(--danger-600);
}

/* ═══════════════════════════════════════════════════════════════
   FOCUS / A11Y
   ═══════════════════════════════════════════════════════════════ */
.nav-item:focus-visible,
.close-btn:focus-visible,
.logout-btn:focus-visible,
.collapse-btn:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--accent-ring);
}

/* ═══════════════════════════════════════════════════════════════
   TRANSICIONES DE TEXTO
   ═══════════════════════════════════════════════════════════════ */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity var(--duration-fast) var(--ease-out),
              transform var(--duration-fast) var(--ease-out);
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(-6px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-6px);
}

@media (prefers-reduced-motion: reduce) {
  .sidebar,
  .nav-item,
  .nav-active-bar,
  .nav-icon-wrap,
  .brand-mark,
  .fade-slide-enter-active,
  .fade-slide-leave-active {
    transition: none;
  }
}
</style>