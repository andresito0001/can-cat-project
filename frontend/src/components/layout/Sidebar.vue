<template>
  <aside
    class="sidebar"
    :class="{
      'is-open': open,
      'is-mobile': isMobile,
    }"
    :aria-hidden="!open"
  >
    <!-- Header con logo + botón cerrar (mobile) -->
    <div class="sidebar-header">
      <div class="logo-box">
        <PawPrint class="logo-icon" :size="28" />
        <span class="logo-text">CanCat</span>
      </div>
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

    <!-- Navegación -->
    <nav class="sidebar-nav">
      <router-link
        v-for="item in items"
        :key="item.route"
        :to="item.route"
        class="nav-item"
        :class="{ active: isActive(item.route) }"
      >
        <component :is="getIcon(item.icon)" :size="20" class="nav-icon" />
        <span class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>

    <!-- Footer: logout -->
    <div class="sidebar-footer">
      <button class="logout-btn" type="button" @click="authStore.logout">
        <LogOut :size="18" />
        <span>Cerrar Sesión</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import {
  LayoutDashboard, PawPrint, CalendarPlus, CreditCard, Stethoscope,
  CalendarDays, UserPlus, Dog, CalendarCheck, Search,
  Package, ArrowDownToLine, AlertTriangle, LogOut, X,
  Users, Banknote, ClipboardList, List, UserCog, Shield, BarChart3, Settings,
  Truck,
} from 'lucide-vue-next'


const props = defineProps({
  items: { type: Array, required: true },
  open: { type: Boolean, default: false },
  isMobile: { type: Boolean, default: false },
})

defineEmits(['close'])

const route = useRoute()
const authStore = useAuthStore()

const iconMap = {
  LayoutDashboard, PawPrint, CalendarPlus, CreditCard, Stethoscope,
  CalendarDays, UserPlus, Dog, CalendarCheck, Search,
  Package, ArrowDownToLine, AlertTriangle,
  Users, Banknote, ClipboardList, List, UserCog, Shield, BarChart3, Settings, Truck,
}

function getIcon(name) {
  return iconMap[name] || LayoutDashboard
}

// Marcamos activo por prefijo también, para rutas con :id (ej: /veterinario/atencion/5)
function isActive(itemRoute) {
  if (!itemRoute) return false
  return route.path === itemRoute || route.path.startsWith(itemRoute + '/')
}
</script>

<style scoped>
.sidebar {
  width: 260px;
  height: 100vh;
  background: #ffffff;
  border-right: 1px solid #E2E8F0;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 40;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  transform: translateX(-100%);
  transition: transform 300ms cubic-bezier(0.16, 1, 0.3, 1);
}

/* Sidebar visible */
.sidebar.is-open {
  transform: translateX(0);
}

/* Sombra solo cuando es overlay (mobile) */
.sidebar.is-mobile.is-open {
  box-shadow: 0 8px 32px rgba(15, 23, 42, 0.25);
}

/* ─── Header ─── */
.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-shrink: 0;
}

.logo-box {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.logo-icon {
  color: #0F766E;
  flex-shrink: 0;
}

.logo-text {
  font-family: var(--font-brand, inherit);
  font-size: 22px;
  font-weight: 700;
  color: #1E293B;
  white-space: nowrap;
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: 1px solid #E2E8F0;
  background: #ffffff;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
  flex-shrink: 0;
}
.close-btn:hover {
  background: #F1F5F9;
  color: #0F172A;
  border-color: #CBD5E1;
}

/* ─── Navegación ─── */
.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
  scrollbar-width: none;
}
.sidebar-nav::-webkit-scrollbar {
  display: none;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  color: #64748B;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s ease, color 0.2s ease;
  position: relative;
  white-space: nowrap;
}

.nav-icon {
  flex-shrink: 0;
}

.nav-label {
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

.nav-item:hover {
  background-color: #F1F5F9;
  color: #0F766E;
}

.nav-item.active {
  background-color: rgba(15, 118, 110, 0.1);
  color: #0F766E;
  font-weight: 600;
  border-left: 3px solid #0F766E;
  margin-left: -12px;
  padding-left: 13px;
  border-radius: 0 8px 8px 0;
}

.nav-item.active .nav-icon {
  stroke-width: 2.5;
}

/* ─── Footer ─── */
.sidebar-footer {
  padding: 16px 12px;
  border-top: 1px solid #E2E8F0;
  flex-shrink: 0;
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #64748B;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.logout-btn:hover {
  background-color: #FEF2F2;
  color: #EF4444;
}
</style>