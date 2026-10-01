<template>
  <textarea
    :id="id"
    :value="modelValue"
    :placeholder="placeholder"
    :disabled="disabled"
    :rows="rows"
    :class="['form-textarea', { 'is-invalid': !!error }]"
    v-bind="$attrs"
    @input="$emit('update:modelValue', $event.target.value)"
  />
</template>

<script setup>
defineProps({
  modelValue:  { type: String, default: '' },
  placeholder: { type: String, default: '' },
  disabled:    { type: Boolean, default: false },
  error:       { type: [String, Boolean], default: '' },
  id:          { type: String, default: '' },
  rows:        { type: Number, default: 4 },
})
defineEmits(['update:modelValue'])
</script>
