<template>
  <div class="app-input-wrap">
    <component
      v-if="icon"
      :is="icon"
      :size="16"
      class="app-input-icon"
      aria-hidden="true"
    />
    <input
      :id="id"
      :value="modelValue"
      :type="type"
      :placeholder="placeholder"
      :disabled="disabled"
      :class="['form-input', { 'is-invalid': !!error, 'with-icon': !!icon }]"
      v-bind="$attrs"
      @input="$emit('update:modelValue', $event.target.value)"
    />
  </div>
</template>

<script setup>
defineProps({
  modelValue:  { type: [String, Number], default: '' },
  type:        { type: String, default: 'text' },
  placeholder: { type: String, default: '' },
  disabled:    { type: Boolean, default: false },
  error:       { type: [String, Boolean], default: '' },
  id:          { type: String, default: '' },
  icon:        { type: [Object, Function], default: null },
})
defineEmits(['update:modelValue'])
</script>

<style scoped>
.app-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.app-input-icon {
  position: absolute;
  left: 14px;
  color: var(--text-tertiary);
  pointer-events: none;
  z-index: 1;
  transition: color var(--duration-fast) var(--ease-out);
}

.app-input-wrap:focus-within .app-input-icon {
  color: var(--brand-700);
}

/* Cuando hay ícono, el input necesita padding-left extra */
.form-input.with-icon {
  padding-left: 40px;
}
</style>