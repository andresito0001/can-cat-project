<template>
  <div
    class="app-layout"
    :class="{
      'sidebar-open': sidebarOpen,
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
    @close="cerrarSidebar"
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
const MOBILE_BREAKPOINT = 1024

// ─── Estado reactivo ───
const sidebarOpen = ref(true)
const isMobile = ref(false)

// ─── Items del menú según rol ───
const menuItems = computed(() => MENU_ITEMS[authStore.userRole] || [])

// ─── Título dinámico: matchea el item del menú activo ───
const tituloActivo = computed(() => {
  const path = route.path
  // Match exacto o por prefijo (para rutas con :id)
  const match = menuItems.value.find((item) => {
    if (!item.route) return false
    return path === item.route || path.startsWith(item.route + '/')
  })
  if (match) return match.label
  if (route.meta?.title) return route.meta.title
  return 'Panel'
})

// ─── Detectar mobile ───
function detectarMobile() {
  isMobile.value = window.innerWidth < MOBILE_BREAKPOINT
}

// ─── Toggle y cierre ───
function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function cerrarSidebar() {
  sidebarOpen.value = false
}

// ─── Persistencia ───
function guardarPreferencia() {
  // Solo guardamos en desktop; en mobile el estado no se persiste
  if (!isMobile.value) {
    localStorage.setItem(SIDEBAR_KEY, String(sidebarOpen.value))
  }
}

function restaurarPreferencia() {
  if (isMobile.value) {
    // En mobile siempre empieza cerrado
    sidebarOpen.value = false
    return
  }
  const saved = localStorage.getItem(SIDEBAR_KEY)
  sidebarOpen.value = saved === null ? true : saved === 'true'
}

// ─── Atajo Cmd/Ctrl + B ───
function handleKeydown(e) {
  const isCmdB =
    (e.metaKey || e.ctrlKey) &&
    e.key.toLowerCase() === 'b'
  if (isCmdB) {
    e.preventDefault()
    toggleSidebar()
  }
}

// ─── Lifecycle ───
onMounted(() => {
  detectarMobile()
  restaurarPreferencia()

  window.addEventListener('resize', detectarMobile)
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', detectarMobile)
  window.removeEventListener('keydown', handleKeydown)
})

// ─── Watchers ───
// Guardar preferencia cada vez que cambia (solo desktop)
watch(sidebarOpen, guardarPreferencia)

// Al cambiar de ruta: cerrar en mobile, ajustar si entra/sale del breakpoint
watch(
  () => route.path,
  () => {
    if (isMobile.value) cerrarSidebar()
  }
)

watch(isMobile, (nuevoEsMobile) => {
  if (nuevoEsMobile) {
    // Pasamos a mobile: cerrar
    sidebarOpen.value = false
  } else {
    // Volvemos a desktop: restaurar preferencia
    restaurarPreferencia()
  }
})
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: #F1F5F9;
}

/* ═══ Overlay mobile ═══ */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  backdrop-filter: blur(2px);
  z-index: 30;
  animation: fadeIn 0.2s ease;
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
  transition: margin-left 300ms cubic-bezier(0.16, 1, 0.3, 1);
}

/* Empuje lateral SOLO en desktop cuando el sidebar está abierto */
.app-layout:not(.is-mobile).sidebar-open .main-wrapper {
  margin-left: 260px;
}

.main-content {
  flex: 1;
  padding: 24px 32px;
}

/* ═══ Transiciones ═══ */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ═══ Responsive ═══ */
@media (max-width: 1023px) {
  .main-content {
    padding: 20px 16px;
  }
}
</style>