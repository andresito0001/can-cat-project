<template>
  <header class="top-navbar">
    <!-- ═══ Zona izquierda: hamburguesa + título ═══ -->
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

    <!-- ═══ Zona derecha: búsqueda + user ═══ -->
    <div class="navbar-right">
      <button
        class="search-trigger"
        type="button"
        title="Búsqueda global (Ctrl/Cmd+K)"
        aria-label="Abrir búsqueda global"
        @click="$emit('open-search')"
      >
        <Search :size="15" />
        <span class="search-label">Buscar…</span>
        <kbd class="search-kbd">⌘K</kbd>
      </button>

      <div class="user-profile">
        <div class="avatar">{{ initials }}</div>
        <div class="user-info">
          <span class="user-name">{{ authStore.userName }}</span>
          <span class="user-role">{{ formatRole(authStore.userRole) }}</span>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { Menu, Search } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth.store'

const props = defineProps({
  title: { type: String, default: 'Panel' },
  isMobile: { type: Boolean, default: false },
})

defineEmits(['toggle-sidebar', 'open-search'])

const authStore = useAuthStore()

const currentDate = computed(() => {
  return new Date().toLocaleDateString('es-VE', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
})

const initials = computed(() => {
  const name = authStore.userName || 'U'
  return name
    .split(' ')
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
</script>

<style scoped>
.top-navbar {
  height: 72px;
  background: #ffffff;
  border-bottom: 1px solid #E2E8F0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 0 24px 0 20px;
  position: sticky;
  top: 0;
  z-index: 20;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

/* ═══ Zona izquierda ═══ */
.navbar-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.icon-btn {
  width: 38px;
  height: 38px;
  border-radius: 10px;
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
.icon-btn:hover {
  background: #F1F5F9;
  color: #0F766E;
  border-color: #CBD5E1;
}

.page-info {
  min-width: 0;
}

.page-title {
  font-family: var(--font-brand, inherit);
  font-size: 18px;
  font-weight: 700;
  color: #1E293B;
  margin: 0;
  line-height: 1.2;
  letter-spacing: -0.01em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.page-date {
  font-size: 12px;
  color: #64748B;
  margin: 2px 0 0 0;
  text-transform: capitalize;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ═══ Zona derecha ═══ */
.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.search-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px 8px 14px;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  background: #F8FAFC;
  color: #64748B;
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s ease;
  min-width: 220px;
}
.search-trigger:hover {
  background: #ffffff;
  border-color: #CBD5E1;
  color: #0F766E;
}

.search-label {
  flex: 1;
  text-align: left;
}

.search-kbd {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 10.5px;
  padding: 2px 6px;
  border-radius: 5px;
  background: #ffffff;
  border: 1px solid #E2E8F0;
  color: #94A3B8;
  font-weight: 600;
  line-height: 1;
}

/* Perfil */
.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-left: 12px;
  border-left: 1px solid #E2E8F0;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #0F766E, #14B8A6);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: 0 2px 6px -2px rgba(15, 118, 110, 0.35);
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: #64748B;
  margin-top: 1px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ═══ Responsive ═══ */
@media (max-width: 1023px) {
  .top-navbar {
    padding: 0 16px;
    gap: 12px;
  }
  .search-trigger {
    min-width: 0;
    width: 40px;
    height: 40px;
    padding: 0;
    justify-content: center;
  }
  .search-label,
  .search-kbd {
    display: none;
  }
}

@media (max-width: 768px) {
  .user-info {
    display: none;
  }
  .user-profile {
    padding-left: 0;
    border-left: none;
  }
  .page-date {
    display: none;
  }
}
</style>