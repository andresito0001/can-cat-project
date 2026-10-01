<template>
  <component
    :is="tag"
    :type="tag === 'button' ? type : undefined"
    :disabled="tag === 'button' ? disabled || loading : undefined"
    :class="classes"
    v-bind="$attrs"
  >
    <span v-if="loading" class="spinner spinner-sm" :class="{ 'spinner-inverse': isDark }" />
    <slot v-else name="icon-left" />
    <slot />
    <slot name="icon-right" />
  </component>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps({
  variant:  { type: String, default: 'primary' },
  size:     { type: String, default: 'md' },
  loading:  { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  block:    { type: Boolean, default: false },
  icon:     { type: Boolean, default: false },
  to:       { type: [String, Object], default: null },
  href:     { type: String, default: null },
  type:     { type: String, default: 'button' },
})

const tag = computed(() => {
  if (props.to) return RouterLink
  if (props.href) return 'a'
  return 'button'
})

const isDark = computed(() =>
  ['primary', 'danger', 'success'].includes(props.variant)
)

const classes = computed(() => [
  'btn',
  `btn-${props.variant}`,
  props.size !== 'md' && `btn-${props.size}`,
  props.block && 'btn-block',
  props.icon && 'btn-icon',
  props.loading && 'is-loading',
])
</script>

<style scoped>
.is-loading { pointer-events: none; }
</style>
