<template>
  <div
    class="avatar"
    :class="[`avatar--${size}`, { 'avatar--muted': muted }]"
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
  background: #F1F5F9;
  color: #0F766E;
  border: 1px solid rgba(15, 23, 42, 0.04);
  transition: background-color .15s ease, color .15s ease;
}
.avatar--sm { width: 36px; height: 36px; border-radius: 10px; }
.avatar--md { width: 44px; height: 44px; border-radius: 12px; }
.avatar--lg { width: 52px; height: 52px; border-radius: 14px; }
.avatar--muted { background: #F8FAFC; color: #94A3B8; opacity: .85; }
</style>