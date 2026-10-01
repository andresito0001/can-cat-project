<template>
  <div :class="['alert', `alert-${variant}`]" role="alert">
    <component :is="icon" :size="16" />
    <div class="alert-content">
      <slot />
    </div>
    <button v-if="action" class="alert-action" type="button" @click="$emit('action')">
      {{ action }}
    </button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { AlertCircle, CheckCircle2, AlertTriangle, Info } from 'lucide-vue-next'

const props = defineProps({
  variant: { type: String, default: 'info' },
  action:  { type: String, default: '' },
})
defineEmits(['action'])

const icon = computed(() => ({
  success: CheckCircle2,
  warning: AlertTriangle,
  error:   AlertCircle,
  info:    Info,
}[props.variant]))
</script>

<style scoped>
.alert-content { flex: 1; min-width: 0; }
</style>
