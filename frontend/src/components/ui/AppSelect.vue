<template>
  <select
    :id="id"
    :value="modelValue"
    :disabled="disabled"
    :class="['form-select', { 'is-invalid': !!error }]"
    v-bind="$attrs"
    @change="$emit('update:modelValue', $event.target.value)"
  >
    <slot />
  </select>
</template>

<script setup>
defineProps({
  modelValue: { type: [String, Number, null], default: '' },
  disabled:   { type: Boolean, default: false },
  error:      { type: [String, Boolean], default: '' },
  id:         { type: String, default: '' },
})
defineEmits(['update:modelValue'])
</script>
