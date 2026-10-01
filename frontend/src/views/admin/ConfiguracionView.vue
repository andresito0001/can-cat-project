<template>
  <div class="configuracion-view">
    <ToastContainer />

    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <Settings :size="12" /> Administración · Configuración
        </span>
        <h1>Configuración</h1>
        <p class="page-header-sub">
          Información general del sistema y parámetros de la clínica.
        </p>
      </div>
    </header>

    <div class="config-grid">
      <!-- Info del sistema -->
      <section class="card">
        <header class="card-header">
          <div class="card-header-icon"><Info :size="16" /></div>
          <div>
            <h3>Información general</h3>
            <p class="card-sub">Detalles técnicos del sistema</p>
          </div>
        </header>
        <div class="card-body">
          <ul class="info-list">
            <li v-for="item in infoSistema" :key="item.label" class="info-row">
              <span class="info-label">{{ item.label }}</span>
              <span class="info-value" :class="{ 'is-mono': item.mono }">
                {{ item.value }}
              </span>
            </li>
          </ul>
        </div>
      </section>

      <!-- Sesión actual -->
      <section class="card">
        <header class="card-header">
          <div class="card-header-icon"><UserCircle :size="16" /></div>
          <div>
            <h3>Sesión actual</h3>
            <p class="card-sub">Datos del usuario autenticado</p>
          </div>
        </header>
        <div class="card-body">
          <ul class="info-list">
            <li class="info-row">
              <span class="info-label">Nombre</span>
              <span class="info-value">{{ authStore.userName }}</span>
            </li>
            <li class="info-row">
              <span class="info-label">Correo</span>
              <span class="info-value">{{ authStore.user?.correoElectronico || '—' }}</span>
            </li>
            <li class="info-row">
              <span class="info-label">Rol</span>
              <span class="info-value">
                <span class="rol-pill">{{ authStore.userRole }}</span>
              </span>
            </li>
          </ul>
        </div>
      </section>

      <!-- Próximamente -->
      <section class="card card-wide">
        <header class="card-header">
          <div class="card-header-icon card-header-icon--warning"><Sparkles :size="16" /></div>
          <div>
            <h3>En desarrollo</h3>
            <p class="card-sub">Funcionalidades futuras de configuración</p>
          </div>
        </header>
        <div class="card-body">
          <div class="proximos-grid">
            <div v-for="p in proximos" :key="p.titulo" class="proximo-item">
              <div class="proximo-icon">
                <component :is="p.icon" :size="18" />
              </div>
              <div class="proximo-texto">
                <p class="proximo-titulo">{{ p.titulo }}</p>
                <p class="proximo-desc">{{ p.desc }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Settings, Info, UserCircle, Sparkles, Mail, Bell, Database, Shield } from 'lucide-vue-next'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import { useAuthStore } from '@/stores/auth.store'

const authStore = useAuthStore()

const infoSistema = computed(() => [
  { label: 'Sistema',         value: 'CanCat · Gestión Veterinaria', mono: false },
  { label: 'Versión backend', value: '0.0.1-SNAPSHOT',              mono: true },
  { label: 'Frontend',        value: 'Vue 3 + Vite',                mono: false },
  { label: 'Backend',         value: 'Spring Boot + PostgreSQL',    mono: false },
])

const proximos = [
  { icon: Mail,      titulo: 'Correo electrónico', desc: 'Configuración de plantillas y servidor SMTP' },
  { icon: Bell,      titulo: 'Notificaciones',     desc: 'Reglas de alertas internas y correos automáticos' },
  { icon: Database,  titulo: 'Respaldos',           desc: 'Programación de backups automáticos de la BD' },
  { icon: Shield,    titulo: 'Seguridad',           desc: 'Políticas de contraseña y sesiones activas' },
]
</script>

<style scoped>
.configuracion-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-7) var(--space-12);
  font-family: var(--font-sans);
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.config-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-5);
  align-items: start;
}
.card-wide { grid-column: 1 / -1; }

.card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
  overflow: hidden;
}
.card-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-subtle);
}
.card-header-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  background: var(--brand-50);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.card-header-icon--warning {
  background: var(--warning-50);
  color: var(--warning-600);
}
.card-header h3 {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
.card-sub {
  margin: 2px 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.card-body { padding: var(--space-4) var(--space-6) var(--space-5); }

/* Info list */
.info-list { list-style: none; margin: 0; padding: 0; }
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-subtle);
  font-size: var(--text-md);
}
.info-row:last-child { border-bottom: none; }
.info-label {
  font-size: var(--text-md);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
  flex-shrink: 0;
}
.info-value {
  color: var(--text-primary);
  font-weight: var(--font-bold);
  text-align: right;
  word-break: break-word;
  min-width: 0;
}
.info-value.is-mono {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  background: var(--neutral-100);
  padding: 2px var(--space-3);
  border-radius: var(--radius-sm);
  color: var(--neutral-700);
}
.rol-pill {
  display: inline-block;
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  background: var(--brand-50);
  color: var(--brand-700);
  border: 1px solid var(--brand-200);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
}

/* Próximos */
.proximos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--space-4);
}
.proximo-item {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
  padding: var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  transition: all var(--duration-base) var(--ease-out);
}
.proximo-item:hover {
  border-color: var(--brand-200);
  background: var(--bg-surface);
}
.proximo-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  background: var(--warning-50);
  color: var(--warning-600);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.proximo-titulo {
  margin: 0;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
.proximo-desc {
  margin: 3px 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-snug);
}

/* Responsive */
@media (max-width: 1024px) {
  .config-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .configuracion-view { padding: var(--space-5) var(--space-4) var(--space-10); }
  .card-header { padding: var(--space-4) var(--space-5); }
  .card-body { padding: var(--space-3) var(--space-5) var(--space-4); }
}
@media (max-width: 480px) {
  .configuracion-view { padding: var(--space-4) var(--space-3) var(--space-8); }
  .proximos-grid { grid-template-columns: 1fr; }
  .info-row { flex-direction: column; align-items: flex-start; gap: var(--space-1); }
  .info-value { text-align: left; }
}
</style>