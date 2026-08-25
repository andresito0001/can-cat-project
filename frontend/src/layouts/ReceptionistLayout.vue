<script setup>
import Sidebar from '@/components/layout/Sidebar.vue'
import TopNavbar from '@/components/layout/TopNavbar.vue'
import { MENU_ITEMS } from '@/utils/constants/menu'

const menu = MENU_ITEMS['Recepcionista'] // ← Exacto como en la BD
</script>

<template>
  <div class="layout">
    <Sidebar :items="menu" />
    <div class="main-wrapper">
      <TopNavbar />
      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout { display: flex; min-height: 100vh; background: #F1F5F9; }
.main-wrapper { flex: 1; margin-left: 260px; display: flex; flex-direction: column; }
.main-content { flex: 1; padding: 24px 32px; }
</style>