<template>
  <div class="layout">
    <Sidebar :items="menuItems" />
    <div class="main-wrapper">
      <TopNavbar />
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import Sidebar from '@/components/layout/Sidebar.vue'
import TopNavbar from '@/components/layout/TopNavbar.vue'
import { MENU_ITEMS } from '@/utils/constants/menu.js'

// ← Esta variable DEBE coincidir con :items="menuItems" en el template
const menuItems = MENU_ITEMS['Cliente']
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  background: #F1F5F9;
}

.main-wrapper {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  padding: 24px 32px;
}
</style>