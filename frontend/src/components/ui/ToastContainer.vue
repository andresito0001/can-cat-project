<script setup>
import { AlertCircle, CheckCircle2, Info, X } from 'lucide-vue-next';
import { useToast } from '@/composables/useToast';

const { toasts, cerrarToast } = useToast();

const ICONOS = { success: CheckCircle2, error: AlertCircle, info: Info };
</script>

<template>
  <Teleport to="body">
    <div class="toast-stack" role="status" aria-live="polite">
      <div v-for="t in toasts" :key="t.id" class="toast" :class="`toast--${t.tipo}`">
        <component :is="ICONOS[t.tipo] || Info" class="toast__icono" />
        <p class="toast__msg">{{ t.mensaje }}</p>
        <button class="toast__cerrar" type="button" aria-label="Cerrar notificación" @click="cerrarToast(t.id)">
          <X :size="14" />
        </button>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-stack {
  position: fixed; top: 20px; right: 20px; z-index: 3000;
  display: flex; flex-direction: column; gap: 10px;
  width: min(380px, calc(100vw - 40px));
}
.toast {
  display: flex; align-items: flex-start; gap: 10px; background: #fff;
  border: 1px solid #E2E8F0; border-radius: 10px; padding: 12px 14px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,.06), 0 10px 15px -3px rgba(0,0,0,.08);
  animation: toast-entrada .25s ease;
}
.toast--success { border-left: 4px solid #059669; }
.toast--error   { border-left: 4px solid #EF4444; }
.toast--info    { border-left: 4px solid #0F766E; }
.toast__icono { width: 18px; height: 18px; flex-shrink: 0; margin-top: 1px; }
.toast--success .toast__icono { color: #059669; }
.toast--error   .toast__icono { color: #EF4444; }
.toast--info    .toast__icono { color: #0F766E; }
.toast__msg { margin: 0; font-size: 13px; color: #1E293B; line-height: 1.5; flex: 1; }
.toast__cerrar {
  background: none; border: none; color: #94A3B8; cursor: pointer;
  padding: 2px; display: flex; font-family: inherit;
}
.toast__cerrar:hover { color: #64748B; }
@keyframes toast-entrada {
  from { opacity: 0; transform: translateX(16px); }
  to   { opacity: 1; transform: none; }
}
</style>