<script setup>
import { useRouter } from 'vue-router'
import { UserPlus, Users, Dog, Banknote } from 'lucide-vue-next'

const router = useRouter()

const accesos = [
  { titulo: 'Gestión de Clientes', desc: 'Registrar y buscar clientes', icon: Users, ruta: '/recepcion/clientes' },
  { titulo: 'Registrar Mascota', desc: 'Asociar pacientes a clientes', icon: Dog, ruta: '/recepcion/registrar-mascota' },
  { titulo: 'Gestionar Citas', desc: 'Agenda de citas (próximamente)', icon: UserPlus, ruta: '/recepcion/citas' },
  { titulo: 'Cobrar en Mostrador', desc: 'Pagos presenciales (próximamente)', icon: Banknote, ruta: '/recepcion/cobrar' },
]
</script>

<template>
  <div class="page">
    <h1 class="page-title">Panel de Recepción</h1>
    <p class="page-subtitle">Bienvenido. Accesos rápidos a los módulos de recepción.</p>

    <div class="cards">
      <div v-for="acceso in accesos" :key="acceso.ruta" class="card" @click="router.push(acceso.ruta)">
        <div class="card-icon"><component :is="acceso.icon" :size="22" /></div>
        <div class="card-titulo">{{ acceso.titulo }}</div>
        <div class="card-desc">{{ acceso.desc }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: 8px; }
.page-title { font-size: 24px; font-weight: 700; color: #1E293B; margin: 0; letter-spacing: -0.03em; }
.page-subtitle { color: #64748B; font-size: 14px; margin: 0 0 16px; }
.cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(230px, 1fr)); gap: 16px; }
.card {
  background: #fff; border-radius: 16px; padding: 24px; cursor: pointer;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.03), 0 20px 40px -4px rgba(15,118,110,0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(15,118,110,0.15); }
.card-icon {
  width: 44px; height: 44px; border-radius: 12px; background: rgba(15,118,110,0.1);
  color: #0F766E; display: flex; align-items: center; justify-content: center; margin-bottom: 14px;
}
.card-titulo { font-size: 15px; font-weight: 700; color: #1E293B; margin-bottom: 4px; }
.card-desc { font-size: 13px; color: #64748B; }
</style>