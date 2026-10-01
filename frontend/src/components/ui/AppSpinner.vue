<template>
  <span :class="['spinner', sizeClass, { 'spinner-inverse': inverse }]" />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  size:    { type: String, default: 'md' },
  inverse: { type: Boolean, default: false },
})
const sizeClass = computed(() => props.size !== 'md' ? `spinner-${props.size}` : '')
</script>
