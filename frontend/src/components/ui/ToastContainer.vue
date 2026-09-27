<template>
  <div class="toast-container" aria-live="polite" aria-atomic="true">
    <TransitionGroup name="toast">
      <div
        v-for="t in toasts"
        :key="t.id"
        class="toast"
        :class="`toast-${t.type}`"
        role="alert"
      >
        <div class="toast-icon">
          <component :is="iconFor(t.type)" :size="18" />
        </div>
        <p class="toast-message">{{ t.message }}</p>
        <button
          class="toast-close"
          type="button"
          aria-label="Cerrar"
          @click="remove(t.id)"
        >
          <X :size="14" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { CheckCircle2, AlertCircle, Info, AlertTriangle, X } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'

const { toasts, remove } = useToast()

function iconFor(type) {
  switch (type) {
    case 'success': return CheckCircle2
    case 'error':   return AlertCircle
    case 'warning': return AlertTriangle
    default:        return Info
  }
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
  max-width: 380px;
  width: calc(100% - 40px);
}

.toast {
  pointer-events: auto;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #ffffff;
  border: 1px solid #E2E8F0;
  border-left: 4px solid #94A3B8;
  border-radius: 12px;
  box-shadow: 0 10px 30px -10px rgba(15, 23, 42, 0.25);
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  min-width: 0;
}

.toast-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  margin-top: 1px;
}

.toast-message {
  flex: 1;
  margin: 0;
  font-size: 13.5px;
  font-weight: 500;
  color: #1E293B;
  line-height: 1.45;
  word-break: break-word;
}

.toast-close {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: #94A3B8;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
}

.toast-close:hover {
  background: #F1F5F9;
  color: #475569;
}

/* ─── Variantes por tipo ─── */
.toast-success { border-left-color: #10B981; }
.toast-success .toast-icon { color: #10B981; }

.toast-error { border-left-color: #EF4444; }
.toast-error .toast-icon { color: #EF4444; }

.toast-warning { border-left-color: #F59E0B; }
.toast-warning .toast-icon { color: #F59E0B; }

.toast-info { border-left-color: #3B82F6; }
.toast-info .toast-icon { color: #3B82F6; }

/* ─── Animación ─── */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.28s cubic-bezier(0.16, 1, 0.3, 1);
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-active {
  position: absolute;
  width: 100%;
}

.toast-move {
  transition: transform 0.28s cubic-bezier(0.16, 1, 0.3, 1);
}
</style>