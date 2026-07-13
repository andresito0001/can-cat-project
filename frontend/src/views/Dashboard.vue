<template>
  <div class="dashboard">
    <h1>Bienvenido al Sistema Can-Cat</h1>
    <p>Esta es la pantalla principal (Aquí irá el menú lateral y todo lo demás).</p>
    <button @click="cerrarSesion" class="logout-btn">Cerrar Sesión</button>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const cerrarSesion = () => {
  authStore.logout()
  router.push('/')
}
</script>

<style scoped>
.dashboard {
  padding: 40px;
  font-family: 'Inter', sans-serif;
  color: #333;
}
.logout-btn {
  margin-top: 20px;
  padding: 10px 15px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  color: #555;
}
.logout-btn:hover {
  background-color: #e0e0e0;
}
</style>