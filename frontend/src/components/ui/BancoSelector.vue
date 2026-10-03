<template>
  <div class="banco-selector" :class="{ 'is-disabled': disabled }">
    <select
      :id="id"
      :value="modelValue"
      :disabled="disabled"
      :class="['banco-select', { 'is-invalid': !!error }]"
      v-bind="$attrs"
      @change="$emit('update:modelValue', $event.target.value)"
    >
      <option value="" disabled>{{ placeholder }}</option>
      <optgroup label="Bancos universales">
        <option v-for="b in BANCOS_VENEZUELA" :key="b.codigo" :value="b.nombre">
          {{ b.codigo }} — {{ b.nombre }}
        </option>
      </optgroup>
    </select>
    <ChevronDown :size="14" class="banco-chevron" aria-hidden="true" />
  </div>
</template>

<script setup>
import { ChevronDown } from 'lucide-vue-next'
import { BANCOS_VENEZUELA } from '@/utils/constants/bancos'

defineProps({
  modelValue:  { type: String, default: '' },
  disabled:    { type: Boolean, default: false },
  error:       { type: [String, Boolean], default: '' },
  id:          { type: String, default: '' },
  placeholder: { type: String, default: 'Seleccione el banco' },
})
defineEmits(['update:modelValue'])
</script>

<style scoped>
.banco-selector {
  position: relative;
  width: 100%;
}

.banco-select {
  width: 100%;
  height: var(--input-h-md, 40px);
  padding: 0 40px 0 var(--space-4);
  appearance: none;
  -webkit-appearance: none;
  background: var(--bg-surface);
  border: 1px solid var(--border-strong);
  border-radius: var(--radius-lg);
  font-family: inherit;
  font-size: var(--text-base);
  color: var(--text-primary);
  cursor: pointer;
  transition: border-color var(--duration-fast) var(--ease-out),
              box-shadow var(--duration-fast) var(--ease-out),
              background-color var(--duration-fast) var(--ease-out);
}
.banco-select:hover:not(:disabled) { border-color: var(--neutral-400); }
.banco-select:focus {
  outline: none;
  border-color: var(--brand-700);
  box-shadow: var(--shadow-focus);
}
.banco-select.is-invalid {
  border-color: var(--danger-500);
  background: var(--danger-50);
}
.banco-select:disabled {
  background: var(--neutral-50);
  color: var(--text-tertiary);
  cursor: not-allowed;
}

.banco-chevron {
  position: absolute;
  right: var(--space-4);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  pointer-events: none;
}
</style>