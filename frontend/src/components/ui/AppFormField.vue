<template>
  <div class="form-group" :class="{ 'has-error': !!error }">
    <label v-if="label" :for="inputId" class="form-label">
      {{ label }}
      <span v-if="required" class="required">*</span>
      <span v-if="optional" class="optional">(opcional)</span>
    </label>

    <slot :id="inputId" :invalid="!!error" :error="error" />

    <span v-if="error" class="form-error">{{ error }}</span>
    <span v-else-if="hint" class="form-hint">{{ hint }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label:    { type: String, default: '' },
  error:    { type: String, default: '' },
  hint:     { type: String, default: '' },
  required: { type: Boolean, default: false },
  optional: { type: Boolean, default: false },
  id:       { type: String, default: '' },
})

const inputId = computed(() => props.id || `field-${Math.random().toString(36).slice(2, 8)}`)
</script>
