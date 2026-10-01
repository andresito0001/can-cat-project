<template>
  <div
    class="app-layout"
    :class="{
      'sidebar-open': sidebarOpen,
      'sidebar-collapsed': sidebarCollapsed && !isMobile,
      'is-mobile': isMobile,
    }"
  >
    <!-- Overlay en mobile -->
    <Transition name="fade">
      <div
        v-if="sidebarOpen && isMobile"
        class="overlay"
        @click="cerrarSidebar"
      />
    </Transition>

    <!-- Sidebar -->
    <Sidebar
      :items="menuItems"
      :open="sidebarOpen"
      :is-mobile="isMobile"
      :is-collapsed="sidebarCollapsed"
      @close="cerrarSidebar"
      @toggle-collapse="toggleCollapse"
    />

    <!-- Main -->
    <div class="main-wrapper">
      <TopNavbar
        :title="tituloActivo"
        :is-mobile="isMobile"
        @toggle-sidebar="toggleSidebar"
      />
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '@/components/layout/Sidebar.vue'
import TopNavbar from '@/components/layout/TopNavbar.vue'
import { useAuthStore } from '@/stores/auth.store'
import { MENU_ITEMS } from '@/utils/constants/menu'

const route = useRoute()
const authStore = useAuthStore()

const SIDEBAR_KEY = 'cancat-sidebar-open'
const COLLAPSE_KEY = 'cancat-sidebar-collapsed'
const MOBILE_BREAKPOINT = 1024

const sidebarOpen = ref(true)
const sidebarCollapsed = ref(false)
const isMobile = ref(false)

const menuItems = computed(() => MENU_ITEMS[authStore.userRole] || [])

const tituloActivo = computed(() => {
  const path = route.path
  const match = menuItems.value.find((item) => {
    if (!item.route) return false
    return path === item.route || path.startsWith(item.route + '/')
  })
  if (match) return match.label
  if (route.meta?.title) return route.meta.title
  return 'Panel'
})

function detectarMobile() {
  const wasMobile = isMobile.value
  isMobile.value = window.innerWidth < MOBILE_BREAKPOINT
  // Al entrar a mobile → colapsar automáticamente el sidebar
  if (!wasMobile && isMobile.value) {
    sidebarOpen.value = false
  }
  // Al volver a desktop → restaurar preferencias
  if (wasMobile && !isMobile.value) {
    restaurarPreferencias()
  }
}

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function cerrarSidebar() {
  sidebarOpen.value = false
}

function toggleCollapse() {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

function guardarPreferencias() {
  if (!isMobile.value) {
    localStorage.setItem(SIDEBAR_KEY, String(sidebarOpen.value))
    localStorage.setItem(COLLAPSE_KEY, String(sidebarCollapsed.value))
  }
}

function restaurarPreferencias() {
  if (isMobile.value) {
    sidebarOpen.value = false
    return
  }
  const savedOpen = localStorage.getItem(SIDEBAR_KEY)
  const savedCollapsed = localStorage.getItem(COLLAPSE_KEY)
  sidebarOpen.value = savedOpen === null ? true : savedOpen === 'true'
  sidebarCollapsed.value = savedCollapsed === 'true'
}

/* Atajo Ctrl/Cmd + B → colapsar/expandir en desktop, abrir/cerrar en mobile */
function handleKeydown(e) {
  const isCmdB = (e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'b'
  if (!isCmdB) return
  e.preventDefault()
  if (isMobile.value) toggleSidebar()
  else toggleCollapse()
}

onMounted(() => {
  detectarMobile()
  restaurarPreferencias()
  window.addEventListener('resize', detectarMobile)
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', detectarMobile)
  window.removeEventListener('keydown', handleKeydown)
})

watch([sidebarOpen, sidebarCollapsed], guardarPreferencias)

watch(() => route.path, () => {
  if (isMobile.value) cerrarSidebar()
})
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-page);
}

/* ═══ Overlay mobile ═══ */
.overlay {
  position: fixed;
  inset: 0;
  background: var(--bg-overlay);
  backdrop-filter: blur(2px);
  z-index: 30;
  animation: fadeIn var(--duration-base) var(--ease-out);
}
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* ═══ Main wrapper ═══ */
.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-width: 0;
  transition: margin-left var(--duration-slow) var(--ease-out);
}

/* Desktop: empuje según estado del sidebar */
.app-layout:not(.is-mobile).sidebar-open .main-wrapper {
  margin-left: 260px;
}
.app-layout:not(.is-mobile).sidebar-open.sidebar-collapsed .main-wrapper {
  margin-left: 76px;
}

.main-content {
  flex: 1;
  padding: var(--space-6) var(--space-7);
}

/* ═══ Transiciones ═══ */
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--duration-base) var(--ease-out);
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ═══ Responsive ═══ */
@media (max-width: 1023px) {
  .main-content {
    padding: var(--space-5) var(--space-4);
  }
}
</style>