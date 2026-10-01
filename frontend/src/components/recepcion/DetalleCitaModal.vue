<template>
  <AppModal
    :model-value="modelValue"
    :title="cita?.nombreMascota || 'Detalle de la cita'"
    subtitle="Información completa del turno"
    size="lg"
    @update:model-value="cerrar"
  >
    <div class="info-card">
      <div class="info-row">
        <span class="info-label"><PawPrint :size="13" /> Mascota</span>
        <span class="info-value">{{ cita?.nombreMascota || '—' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label"><User :size="13" /> Cliente</span>
        <span class="info-value">{{ cita?.nombreCliente || '—' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label"><CreditCard :size="13" /> Documento</span>
        <span class="info-value mono">{{ cita?.documentoCliente || '—' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label"><Stethoscope :size="13" /> Veterinario</span>
        <span class="info-value">{{ cita?.nombreVeterinario || '—' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label"><ClipboardList :size="13" /> Servicio</span>
        <span class="info-value">{{ cita?.nombreServicio || '—' }}</span>
      </div>
      <div class="info-row">
        <span class="info-label"><CalendarDays :size="13" /> Fecha</span>
        <span class="info-value">{{ fmtFecha(cita?.fecha) }}</span>
      </div>
      <div class="info-row">
        <span class="info-label"><Clock :size="13" /> Horario</span>
        <span class="info-value">
          {{ fmtHora(cita?.horaInicio) }} — {{ fmtHora(cita?.horaFin) }}
        </span>
      </div>
      <div class="info-row">
        <span class="info-label"><DollarSign :size="13" /> Costo</span>
        <span class="info-value amount">
          {{ fmtUsd(cita?.costoUsd) }} USD
          <span v-if="cita?.costoBs != null" class="monto-bs">
            · Bs. {{ fmtBs(cita.costoBs) }}
          </span>
        </span>
      </div>
      <div v-if="cita?.motivoConsulta" class="info-row info-row-col">
        <span class="info-label"><FileText :size="13" /> Motivo</span>
        <span class="info-value">{{ cita.motivoConsulta }}</span>
      </div>
      <div class="info-row">
        <span class="info-label"><Info :size="13" /> Estado</span>
        <span class="estado-pill" :style="estadoStyle">{{ estadoLabel }}</span>
      </div>
    </div>

    <template #footer>
      <AppButton variant="secondary" @click="cerrar">Cerrar</AppButton>
    </template>
  </AppModal>
</template>

<script setup>
import { computed } from 'vue'
import {
  PawPrint, User, CreditCard, Stethoscope, ClipboardList,
  CalendarDays, Clock, DollarSign, FileText, Info,
} from 'lucide-vue-next'
import AppModal from '@/components/ui/AppModal.vue'
import AppButton from '@/components/ui/AppButton.vue'
import { ESTADO_LABEL, ESTADO_COLOR } from '@/utils/constants/estadosCita'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  cita: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue'])

function cerrar() {
  emit('update:modelValue', false)
}

function fmtUsd(v) {
  if (v == null) return '$0.00'
  return `$${Number(v).toFixed(2)}`
}

function fmtBs(v) {
  const n = Number(v)
  if (v == null || Number.isNaN(n)) return '—'
  return n.toLocaleString('es-VE', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

function fmtHora(t) {
  if (!t) return ''
  const [h, m] = t.split(':')
  const hh = Number(h) % 12 || 12
  return `${hh}:${m} ${Number(h) >= 12 ? 'PM' : 'AM'}`
}

function fmtFecha(iso) {
  if (!iso) return ''
  const [y, m, d] = String(iso).slice(0, 10).split('-')
  const MESES = ['enero','febrero','marzo','abril','mayo','junio','julio','agosto','septiembre','octubre','noviembre','diciembre']
  return `${d} de ${MESES[Number(m)-1]} de ${y}`
}

const estadoLabel = computed(() =>
  ESTADO_LABEL[props.cita?.estado] || props.cita?.estado || '—'
)

const estadoStyle = computed(() => {
  const color = ESTADO_COLOR[props.cita?.estado] || 'var(--neutral-500)'
  return {
    color,
    borderColor: color,
    backgroundColor: 'transparent',
  }
})
</script>

<style scoped>
.info-card {
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-1) var(--space-4);
}
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
.info-row-col { flex-direction: column; align-items: flex-start; gap: var(--space-1); }
.info-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  font-weight: var(--font-semibold);
  font-size: var(--text-sm);
  white-space: nowrap;
  flex-shrink: 0;
}
.info-label svg { color: var(--text-tertiary); }
.info-value {
  color: var(--text-primary);
  font-weight: var(--font-bold);
  text-align: right;
  word-break: break-word;
  min-width: 0;
}
.info-row-col .info-value { text-align: left; }
.info-value.mono {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  background: var(--neutral-100);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
}
.info-value.amount { color: var(--brand-700); font-size: var(--text-lg); }
.info-value .monto-bs {
  color: var(--neutral-700);
  font-weight: var(--font-bold);
  font-size: var(--text-md);
}
.estado-pill {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  border: 1px solid;
  white-space: nowrap;
}
</style>