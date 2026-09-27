<template>
  <div class="config-view">
    <ToastContainer />

    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow"><Settings :size="12" /> Administración · Configuración</p>
        <h1>Configuración</h1>
        <p class="hero-sub">Preferencias generales del sistema.</p>
      </div>
    </header>

    <section class="card">
      <header class="card-header"><h3>Información general</h3></header>
      <div class="card-body">
        <div class="info-row">
          <span class="info-label">Sistema</span>
          <span class="info-value">CanCat · Gestión Veterinaria</span>
        </div>
        <div class="info-row">
          <span class="info-label">Versión backend</span>
          <span class="info-value mono">0.0.1-SNAPSHOT</span>
        </div>
        <div class="info-row">
          <span class="info-label">Rol actual</span>
          <span class="info-value">{{ authStore.userRole }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">Usuario</span>
          <span class="info-value">{{ authStore.userName }}</span>
        </div>
      </div>
    </section>

    <section class="card">
      <header class="card-header"><h3>Próximamente</h3></header>
      <div class="card-body">
        <p class="placeholder">Aquí podrás configurar parámetros del sistema: correo, tasas, notificaciones, respaldos y más.</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { Settings } from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import { useAuthStore } from '@/stores/auth.store'

const authStore = useAuthStore()
</script>

<style scoped>
.config-view { max-width: 1100px; margin: 0 auto; padding: 24px 28px 48px; font-family: 'Inter', sans-serif; color: #0F172A; display: flex; flex-direction: column; gap: 20px; }
.hero { padding: 24px 28px; background: linear-gradient(135deg, #F8FAFC 0%, #FFFFFF 55%); border: 1px solid #E2E8F0; border-radius: 16px; }
.hero-eyebrow { display: inline-flex; align-items: center; gap: 6px; margin: 0 0 8px; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .7px; color: #475569; background: #fff; padding: 4px 10px; border-radius: 20px; border: 1px solid #E2E8F0; }
.hero h1 { margin: 0 0 4px; font-size: 26px; font-weight: 700; }
.hero-sub { margin: 0; font-size: 14px; color: #64748B; }
.card { background: #fff; border: 1px solid #E2E8F0; border-radius: 14px; }
.card-header { padding: 18px 24px; border-bottom: 1px solid #E2E8F0; }
.card-header h3 { margin: 0; font-size: 15px; font-weight: 700; }
.card-body { padding: 20px 24px; display: flex; flex-direction: column; gap: 14px; }
.info-row { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid #F1F5F9; }
.info-row:last-child { border-bottom: none; }
.info-label { font-size: 13px; color: #64748B; font-weight: 600; }
.info-value { font-size: 13.5px; color: #0F172A; font-weight: 500; }
.mono { font-family: monospace; font-size: 12.5px; }
.placeholder { margin: 0; font-size: 13.5px; color: #64748B; line-height: 1.6; }
</style>