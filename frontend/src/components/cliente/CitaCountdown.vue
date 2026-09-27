<template>
  <div class="cita-countdown" :class="`nivel-${urgente}`">
    <div class="countdown-header">
      <Hourglass :size="14" class="countdown-icon" />
      <span class="countdown-label">Tiempo para pagar</span>
    </div>

    <div class="countdown-body">
      <span class="countdown-timer">{{ texto }}</span>
      <span class="countdown-hint" v-if="urgente === 'critical'">¡Últimos minutos!</span>
      <span class="countdown-hint" v-else-if="urgente === 'warning'">Queda poco tiempo</span>
      <span class="countdown-hint" v-else>Completa el pago para confirmar</span>
    </div>

    <div class="countdown-bar">
      <div class="countdown-progress" :style="{ width: progreso + '%' }"></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Hourglass } from 'lucide-vue-next'
import { useCountdown } from '@/composables/useCountdown'

const props = defineProps({
  expiraEn: { type: String, required: true },
  duracionTotalMin: { type: Number, default: 60 }, // 1h por defecto
})

const emit = defineEmits(['expirado'])

// Ref reactiva a partir del prop
const fechaRef = computed(() => props.expiraEn)

const { texto, restanteMs, expirado, urgente } = useCountdown(fechaRef)

// Notificar al padre cuando llega a 0 (una sola vez)
let yaEmitido = false
import { watch } from 'vue'
watch(expirado, (v) => {
  if (v && !yaEmitido) {
    yaEmitido = true
    emit('expirado')
  }
})

// Barra de progreso: qué % del tiempo original queda
const progreso = computed(() => {
  if (restanteMs.value == null) return 0
  const totalMs = props.duracionTotalMin * 60 * 1000
  return Math.max(0, Math.min(100, (restanteMs.value / totalMs) * 100))
})
</script>

<style scoped>
.cita-countdown {
  border-radius: 10px; padding: 10px 12px;
  border: 1px solid; display: flex; flex-direction: column; gap: 6px;
  transition: border-color .2s, background-color .2s;
}
.cita-countdown.nivel-ok {
  background: #F0FDFA; border-color: #99F6E4;
}
.cita-countdown.nivel-warning {
  background: #FFFBEB; border-color: #FDE68A;
}
.cita-countdown.nivel-critical {
  background: #FEF2F2; border-color: #FECACA;
  animation: pulse 1.6s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
  50%      { box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.08); }
}

.countdown-header {
  display: flex; align-items: center; gap: 6px;
  font-size: 11px; font-weight: 700; text-transform: uppercase;
  letter-spacing: .5px;
}
.nivel-ok .countdown-header       { color: #0F766E; }
.nivel-warning .countdown-header  { color: #B45309; }
.nivel-critical .countdown-header { color: #B91C1C; }

.countdown-icon { flex-shrink: 0; }

.countdown-body {
  display: flex; align-items: baseline; gap: 10px;
  justify-content: space-between;
}
.countdown-timer {
  font-size: 22px; font-weight: 800; font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em; line-height: 1;
}
.nivel-ok .countdown-timer       { color: #0F766E; }
.nivel-warning .countdown-timer  { color: #B45309; }
.nivel-critical .countdown-timer { color: #B91C1C; }

.countdown-hint {
  font-size: 11.5px; font-weight: 500; text-align: right;
  line-height: 1.3;
}
.nivel-ok .countdown-hint       { color: #64748B; }
.nivel-warning .countdown-hint  { color: #92400E; }
.nivel-critical .countdown-hint { color: #B91C1C; font-weight: 600; }

.countdown-bar {
  height: 3px; background: rgba(15, 23, 42, 0.06);
  border-radius: 3px; overflow: hidden;
}
.countdown-progress {
  height: 100%; transition: width 1s linear;
  border-radius: 3px;
}
.nivel-ok .countdown-progress       { background: #0F766E; }
.nivel-warning .countdown-progress  { background: #F59E0B; }
.nivel-critical .countdown-progress { background: #DC2626; }
</style>