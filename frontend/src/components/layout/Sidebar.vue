<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="logo-box">
        <PawPrint class="logo-icon" :size="28" />
        <span class="logo-text">CanCat</span>
      </div>
    </div>

    <nav class="sidebar-nav">
      <router-link
        v-for="item in items"
        :key="item.route"
        :to="item.route"
        class="nav-item"
        :class="{ active: $route.path === item.route }"
      >
        <component :is="getIcon(item.icon)" :size="20" />
        <span>{{ item.label }}</span>
      </router-link>
    </nav>

    <div class="sidebar-footer">
      <button class="logout-btn" @click="authStore.logout">
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
  Package, ArrowDownToLine, AlertTriangle, LogOut
} from 'lucide-vue-next'

const props = defineProps({
  items: { type: Array, required: true }
})

const route = useRoute()
const authStore = useAuthStore()

const iconMap = {
  LayoutDashboard, PawPrint, CalendarPlus, CreditCard, Stethoscope,
  CalendarDays, UserPlus, Dog, CalendarCheck, Search,
  Package, ArrowDownToLine, AlertTriangle
}

function getIcon(name) {
  return iconMap[name] || LayoutDashboard
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
  z-index: 100;
  /* SOLUCIÓN AL ERROR DE FUENTE */
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.sidebar-header {
  /* El padding derecho e izquierdo (20px) hace que el texto del logo 
     se alinee exactamente con el texto de los items del menú */
  padding: 24px 20px;
  border-bottom: 1px solid #E2E8F0;
}

.logo-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  color: #0F766E;
}

.logo-text {
  font-family: var(--font-brand); /* Usará Poppins */
  font-size: 22px;
  font-weight: 700;
  color: #1E293B;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px; /* Ligeramente reducido para dar más espacio al contenido */
  display: flex;
  flex-direction: column;
  gap: 4px;
  /* Permite scroll interno si el menú es muy largo en pantallas pequeñas */
  overflow-y: auto;
  scrollbar-width: none; /* Oculta scroll en Firefox */
}

/* Oculta scroll en Chrome/Safari */
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
  transition: all 0.2s ease;
  position: relative;
}

.nav-item:hover {
  background-color: #F1F5F9;
  color: #0F766E;
}

/* Diseño mejorado para el item activo */
.nav-item.active {
  background-color: rgba(15, 118, 110, 0.1);
  color: #0F766E;
  font-weight: 600;
  /* Indicador lateral izquierdo */
  border-left: 3px solid #0F766E;
  /* Ajuste de margen para que no se desplace el texto al aparecer el borde */
  margin-left: -12px;
  padding-left: 13px;
  border-radius: 0 8px 8px 0;
}

.nav-item.active :deep(svg) {
  stroke-width: 2.5px;
}

.sidebar-footer {
  padding: 16px 12px;
  border-top: 1px solid #E2E8F0;
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
  transition: all 0.2s;
  font-family: inherit;
}

.logout-btn:hover {
  background-color: #FEF2F2;
  color: #EF4444;
}
</style>