<template>
  <div
    class="avatar"
    :class="[`avatar-${size}`, { 'avatar-muted': muted }]"
    :title="nombreEspecie || 'Mascota'"
  >
    <component :is="icon" :size="iconSize" :stroke-width="1.75" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Dog, Cat, Bird, Rabbit, Turtle, PawPrint } from 'lucide-vue-next'

const props = defineProps({
  nombreEspecie: { type: String, default: '' },
  size: { type: String, default: 'md' },
  muted: { type: Boolean, default: false },
})

const ICONOS = {
  canino: Dog, perro: Dog,
  felino: Cat, gato: Cat,
  ave: Bird, pajaro: Bird, 'pájaro': Bird,
  roedor: Rabbit, conejo: Rabbit,
  reptil: Turtle, tortuga: Turtle,
}

const icon = computed(() => {
  const key = String(props.nombreEspecie || '').trim().toLowerCase()
  return ICONOS[key] || PawPrint
})

const iconSize = computed(() => ({ sm: 16, md: 22, lg: 28 }[props.size] || 22))
</script>

<style scoped>
.avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--brand-50);
  color: var(--brand-700);
  border: 1px solid var(--brand-100);
  transition: background-color var(--duration-fast) var(--ease-out),
              color var(--duration-fast) var(--ease-out);
}
.avatar-sm { width: 36px; height: 36px; border-radius: var(--radius-lg); }
.avatar-md { width: 44px; height: 44px; border-radius: var(--radius-xl); }
.avatar-lg { width: 52px; height: 52px; border-radius: var(--radius-2xl); }
.avatar-muted {
  background: var(--neutral-100);
  color: var(--text-tertiary);
  border-color: var(--border-subtle);
  opacity: 0.85;
}
</style>