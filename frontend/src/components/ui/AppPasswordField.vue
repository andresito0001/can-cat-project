<template>
  <div class="password-field">
    <AppInput
      :id="id"
      :model-value="modelValue"
      :type="visible ? 'text' : 'password'"
      :placeholder="placeholder"
      :disabled="disabled"
      :error="error"
      :autocomplete="autocomplete"
      v-bind="$attrs"
      @update:model-value="$emit('update:modelValue', $event)"
    />
    <button
      type="button"
      class="password-field-toggle"
      :disabled="disabled"
      :aria-label="visible ? 'Ocultar contraseña' : 'Mostrar contraseña'"
      tabindex="-1"
      @click="visible = !visible"
    >
      <EyeOff v-if="visible" :size="16" />
      <Eye v-else :size="16" />
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Eye, EyeOff } from 'lucide-vue-next'
import AppInput from './AppInput.vue'

defineProps({
  modelValue:   { type: String, default: '' },
  placeholder:  { type: String, default: 'Ingresa tu contraseña' },
  disabled:     { type: Boolean, default: false },
  error:        { type: [String, Boolean], default: '' },
  id:           { type: String, default: '' },
  autocomplete: { type: String, default: 'current-password' },
})
defineEmits(['update:modelValue'])

const visible = ref(false)
</script>

<style scoped>

.password-field {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;   /* ← FIX: garantiza ancho completo siempre */
}
.password-field :deep(.form-input) { padding-right: 44px; }

.password-field-toggle {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: transparent;
  border: none;
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: color var(--duration-fast) var(--ease-out),
              background-color var(--duration-fast) var(--ease-out);
}
.password-field-toggle:hover:not(:disabled) {
  color: var(--brand-700);
  background: var(--brand-50);
}
.password-field-toggle:disabled { opacity: 0.5; cursor: not-allowed; }
.password-field-toggle:focus-visible {
  outline: none;
  box-shadow: var(--shadow-focus);
}
</style>
