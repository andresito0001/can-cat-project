<template>
  <div class="empty-state">
    <div v-if="icon" class="empty-state-icon">
      <component :is="icon" :size="32" />
    </div>
    <h3 v-if="title">{{ title }}</h3>
    <p v-if="description">{{ description }}</p>
    <div v-if="$slots.action" class="empty-state-action">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  icon:        { type: [Object, Function], default: null },
  title:       { type: String, default: '' },
  description: { type: String, default: '' },
})
</script>

<style scoped>
.empty-state-action { margin-top: var(--space-2); display: flex; gap: var(--space-3); flex-wrap: wrap; justify-content: center; }
</style>
