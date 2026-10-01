<template>
  <AppModal
    :model-value="modelValue"
    :title="cliente?.nombreCompleto || 'Cliente'"
    subtitle="Ficha del cliente"
    size="lg"
    @update:model-value="cerrar"
  >
    <div class="data-grid">
      <div class="data-field">
        <span class="field-label"><CreditCard :size="12" /> Documento</span>
        <span class="field-value mono">{{ cliente?.documentoIdentidad || '—' }}</span>
      </div>
      <div class="data-field">
        <span class="field-label"><Phone :size="12" /> Teléfono principal</span>
        <span class="field-value">{{ cliente?.telefonoPrincipal || '—' }}</span>
      </div>
      <div class="data-field">
        <span class="field-label"><Phone :size="12" /> Teléfono secundario</span>
        <span class="field-value">{{ cliente?.telefonoSecundario || '—' }}</span>
      </div>
      <div class="data-field">
        <span class="field-label"><Mail :size="12" /> Correo</span>
        <span class="field-value">{{ cliente?.correoElectronico || '—' }}</span>
      </div>
      <div class="data-field">
        <span class="field-label"><Calendar :size="12" /> Fecha de nacimiento</span>
        <span class="field-value">{{ fmtFecha(cliente?.fechaNacimiento) }}</span>
      </div>
      <div class="data-field">
        <span class="field-label"><MapPin :size="12" /> Ciudad</span>
        <span class="field-value">{{ cliente?.ciudad || '—' }}</span>
      </div>
      <div class="data-field data-field-full">
        <span class="field-label"><Home :size="12" /> Dirección</span>
        <span class="field-value">{{ cliente?.direccion || '—' }}</span>
      </div>
    </div>

    <template #footer>
      <AppButton variant="secondary" @click="cerrar">Cerrar</AppButton>
    </template>
  </AppModal>
</template>

<script setup>
import { X, CreditCard, Phone, Mail, Calendar, MapPin, Home } from 'lucide-vue-next'
import AppModal from '@/components/ui/AppModal.vue'
import AppButton from '@/components/ui/AppButton.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  cliente: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue'])

function cerrar() {
  emit('update:modelValue', false)
}

function fmtFecha(iso) {
  if (!iso) return '—'
  const d = new Date(iso + 'T12:00:00')
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleDateString('es-VE', { day: '2-digit', month: 'long', year: 'numeric' })
}
</script>

<style scoped>
.data-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}
.data-field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  min-width: 0;
}
.data-field-full { grid-column: 1 / -1; }

.field-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
}
.field-value {
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  word-break: break-word;
}
.field-value.mono {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  background: var(--bg-surface);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
  align-self: flex-start;
}

@media (max-width: 640px) {
  .data-grid { grid-template-columns: 1fr; }
}
</style>