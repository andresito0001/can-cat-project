<template>
  <header class="top-navbar">
    <div class="page-title">
      <h1>{{ pageTitle }}</h1>
      <p class="breadcrumb">{{ currentDate }}</p>
    </div>
    <div class="user-section">
      <div class="notification-btn">
        <Bell :size="20" />
        <span v-if="notificationCount > 0" class="badge">{{ notificationCount }}</span>
      </div>
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
import { useRoute } from 'vue-router'
import { Bell } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth.store'

const route = useRoute()
const authStore = useAuthStore()

const pageTitle = computed(() => route.meta.title || 'Dashboard')

const currentDate = computed(() => {
  return new Date().toLocaleDateString('es-VE', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
  })
})

const initials = computed(() => {
  const name = authStore.userName || 'U'
  return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase()
})

const notificationCount = computed(() => 2) // Mock

function formatRole(rol) {
  const map = {
    'CLIENTE': 'Cliente',
    'RECEPCIONISTA': 'Recepcionista',
    'VETERINARIO': 'Veterinario',
    'ENCARGADO_ALMACEN': 'Encargado de Almacén'
  }
  return map[rol] || rol
}
</script>

<style scoped>
.top-navbar {
  height: 72px;
  background: #ffffff;
  border-bottom: 1px solid #E2E8F0;
  /* Sombra sutil añadida para dar profundidad al flotar sobre el contenido */
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 50;
  /* SOLUCIÓN AL ERROR DE FUENTE */
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.page-title h1 {
  font-family: var(--font-brand); /* Usará Poppins si está definida globalmente */
  font-size: 20px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
  line-height: 1.2;
}

.breadcrumb {
  font-size: 13px;
  color: #64748B;
  margin: 2px 0 0 0;
  text-transform: capitalize;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.notification-btn {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid #E2E8F0;
  /* Sombra sutil al botón */
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748B;
  cursor: pointer;
  transition: all 0.2s;
  background: #ffffff;
}

.notification-btn:hover {
  background: #F1F5F9;
  color: #0F766E;
  border-color: transparent;
}

.badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  background: #F59E0B;
  color: white;
  font-size: 10px;
  font-weight: 700;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
  padding: 0 4px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
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
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  line-height: 1.2;
}

.user-role {
  font-size: 12px;
  color: #64748B;
  margin-top: 1px;
}

/* Responsive para móviles si la navbar se usa sin sidebar */
@media (max-width: 768px) {
  .top-navbar {
    padding: 0 16px;
  }
  .user-info {
    display: none; /* Oculta el texto en móvil, solo muestra el avatar */
  }
}
</style>