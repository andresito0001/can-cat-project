<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="modelValue" class="modal-overlay" @click.self="cerrar">
        <Transition name="slide-up" appear>
          <div v-if="modelValue" class="detail-modal">
            <header class="modal-head">
              <div class="modal-head-left">
                <div class="modal-icon"><Info :size="18" /></div>
                <div>
                  <span class="modal-eyebrow">Detalle de la cita</span>
                  <h3>{{ cita?.nombreMascota }}</h3>
                </div>
              </div>
              <button class="modal-close" type="button" @click="cerrar">
                <X :size="17" />
              </button>
            </header>

            <div class="modal-body">
              <div class="info-card">
                <div class="info-row">
                  <span class="info-label"><PawPrint :size="13" /> Mascota</span>
                  <span class="info-value">{{ cita?.nombreMascota }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label"><User :size="13" /> Cliente</span>
                  <span class="info-value">{{ cita?.nombreCliente }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label"><CreditCard :size="13" /> Documento</span>
                  <span class="info-value mono">{{ cita?.documentoCliente || '—' }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label"><Stethoscope :size="13" /> Veterinario</span>
                  <span class="info-value">{{ cita?.nombreVeterinario }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label"><ClipboardList :size="13" /> Servicio</span>
                  <span class="info-value">{{ cita?.nombreServicio }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label"><CalendarDays :size="13" /> Fecha</span>
                  <span class="info-value">{{ fmtFecha(cita?.fecha) }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label"><Clock :size="13" /> Horario</span>
                  <span class="info-value">{{ fmtHora(cita?.horaInicio) }} — {{ fmtHora(cita?.horaFin) }}</span>
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
            </div>

            <footer class="modal-foot">
              <button class="btn btn-ghost" type="button" @click="cerrar">Cerrar</button>
            </footer>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import {
  Info, X, PawPrint, User, CreditCard, Stethoscope, ClipboardList,
  CalendarDays, Clock, DollarSign, FileText,
} from 'lucide-vue-next'
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
  ESTADO_LABEL[props.cita?.estado] || props.cita?.estado
)

const estadoStyle = computed(() => {
  const color = ESTADO_COLOR[props.cita?.estado] || '#64748B'
  return { color, borderColor: color, backgroundColor: `${color}1A` }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, .55);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  padding: 24px; z-index: 1100;
  font-family: 'Inter', 'Segoe UI', Roboto, sans-serif;
}
.detail-modal {
  background: #fff; border-radius: 18px;
  width: 100%; max-width: 560px;
  max-height: 90vh; display: flex; flex-direction: column;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .3);
}
.modal-head {
  display: flex; justify-content: space-between; align-items: flex-start;
  gap: 16px; padding: 20px 24px 18px;
  border-bottom: 1px solid #E2E8F0;
}
.modal-head-left { display: flex; gap: 14px; align-items: center; }
.modal-icon {
  width: 40px; height: 40px; border-radius: 11px;
  background: #F0FDFA; color: #0F766E;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.modal-eyebrow {
  display: inline-block;
  font-size: 10.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .6px;
  color: #0F766E; margin-bottom: 4px;
}
.modal-head h3 { margin: 0; font-size: 16px; font-weight: 700; color: #0F172A; }
.modal-close {
  width: 34px; height: 34px; border-radius: 9px;
  border: 1px solid #E2E8F0; background: #fff; color: #64748B;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; flex-shrink: 0;
}
.modal-close:hover { background: #F8FAFC; color: #1E293B; }
.modal-body { padding: 20px 24px; overflow-y: auto; }
.info-card {
  background: #F8FAFC; border: 1px solid #E2E8F0;
  border-radius: 12px; padding: 4px 16px;
}
.info-row {
  display: flex; justify-content: space-between; align-items: center;
  gap: 14px; padding: 9px 0;
  border-bottom: 1px solid #E2E8F0; font-size: 13px;
}
.info-row:last-child { border-bottom: none; }
.info-row-col {
  flex-direction: column; align-items: flex-start; gap: 4px;
}
.info-label {
  display: inline-flex; align-items: center; gap: 6px;
  color: #64748B; font-weight: 600; font-size: 12.5px;
  white-space: nowrap; flex-shrink: 0;
}
.info-value {
  color: #0F172A; font-weight: 700; text-align: right;
  word-break: break-word; min-width: 0;
}
.info-row-col .info-value { text-align: left; }
.info-value.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12.5px; background: #F1F5F9;
  padding: 2px 8px; border-radius: 5px;
}
.info-value.amount { color: #0F766E; font-size: 14px; }
.info-value .monto-bs {
  color: #334155;
  font-weight: 700;
  font-size: 13px;
}
.estado-pill {
  display: inline-flex; align-items: center;
  padding: 4px 12px; border-radius: 20px;
  font-size: 11.5px; font-weight: 700; border: 1px solid;
  white-space: nowrap;
}
.modal-foot {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 16px 24px; border-top: 1px solid #E2E8F0;
  background: #FAFBFC;
}
.btn {
  display: inline-flex; align-items: center; justify-content: center;
  gap: 7px; padding: 10px 20px; border-radius: 10px;
  font-size: 13.5px; font-weight: 700; font-family: inherit;
  cursor: pointer; transition: all .2s; border: 1px solid #E2E8F0;
  background: #fff; color: #475569;
}
.btn:hover { background: #F8FAFC; color: #0F766E; }

.fade-enter-active, .fade-leave-active { transition: opacity .2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-up-enter-active, .slide-up-leave-active {
  transition: all .3s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from, .slide-up-leave-to {
  opacity: 0; transform: translateY(20px) scale(.98);
}
</style>