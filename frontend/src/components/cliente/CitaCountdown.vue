<template>
  <div class="countdown" :class="`countdown-${urgente}`">
    <div class="countdown-header">
      <Hourglass :size="14" class="countdown-icon" />
      <span class="countdown-label">Tiempo para pagar</span>
    </div>

    <div class="countdown-body">
      <span class="countdown-timer">{{ texto }}</span>
      <span v-if="urgente === 'critical'" class="countdown-hint">¡Últimos minutos!</span>
      <span v-else-if="urgente === 'warning'" class="countdown-hint">Queda poco tiempo</span>
      <span v-else class="countdown-hint">Completa el pago para confirmar</span>
    </div>

    <div class="countdown-bar">
      <div class="countdown-progress" :style="{ width: progreso + '%' }" />
    </div>
  </div>
</template>

<script setup>
import { computed, watch } from 'vue'
import { Hourglass } from 'lucide-vue-next'
import { useCountdown } from '@/composables/useCountdown'

const props = defineProps({
  expiraEn: { type: String, required: true },
  duracionTotalMin: { type: Number, default: 60 },
})
const emit = defineEmits(['expirado'])

const fechaRef = computed(() => props.expiraEn)
const { texto, restanteMs, expirado, urgente } = useCountdown(fechaRef)

let yaEmitido = false
watch(expirado, (v) => {
  if (v && !yaEmitido) {
    yaEmitido = true
    emit('expirado')
  }
})

const progreso = computed(() => {
  if (restanteMs.value == null) return 0
  const totalMs = props.duracionTotalMin * 60 * 1000
  return Math.max(0, Math.min(100, (restanteMs.value / totalMs) * 100))
})
</script>

<style scoped>
.countdown {
  border-radius: var(--radius-lg);
  padding: var(--space-3) var(--space-3);
  border: 1px solid;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  transition: border-color var(--duration-base) var(--ease-out),
              background-color var(--duration-base) var(--ease-out);
}
.countdown-ok {
  background: var(--brand-50);
  border-color: var(--brand-200);
}
.countdown-warning {
  background: var(--warning-50);
  border-color: var(--warning-200);
}
.countdown-critical {
  background: var(--danger-50);
  border-color: var(--danger-200);
  animation: pulse 1.6s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
  50%      { box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.08); }
}

.countdown-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.countdown-ok .countdown-header       { color: var(--brand-700); }
.countdown-warning .countdown-header  { color: var(--warning-700); }
.countdown-critical .countdown-header { color: var(--danger-700); }
.countdown-icon { flex-shrink: 0; }

.countdown-body {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
  justify-content: space-between;
}
.countdown-timer {
  font-size: var(--text-5xl);
  font-weight: var(--font-extrabold);
  font-variant-numeric: tabular-nums;
  letter-spacing: var(--tracking-tight);
  line-height: 1;
}
.countdown-ok .countdown-timer       { color: var(--brand-700); }
.countdown-warning .countdown-timer  { color: var(--warning-700); }
.countdown-critical .countdown-timer { color: var(--danger-700); }

.countdown-hint {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  text-align: right;
  line-height: var(--leading-snug);
}
.countdown-ok .countdown-hint       { color: var(--text-secondary); }
.countdown-warning .countdown-hint  { color: var(--warning-700); }
.countdown-critical .countdown-hint { color: var(--danger-700); font-weight: var(--font-semibold); }

.countdown-bar {
  height: 3px;
  background: rgba(15, 23, 42, 0.06);
  border-radius: var(--radius-full);
  overflow: hidden;
}
.countdown-progress {
  height: 100%;
  transition: width 1s linear;
  border-radius: var(--radius-full);
}
.countdown-ok .countdown-progress       { background: var(--brand-700); }
.countdown-warning .countdown-progress  { background: var(--warning-500); }
.countdown-critical .countdown-progress { background: var(--danger-600); }
</style>