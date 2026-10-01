<template>
  <div
    class="avatar"
    :class="[`avatar-${size}`, { 'avatar-muted': muted }]"
    :title="titulo"
  >
    <component v-if="icon" :is="icon" :size="iconSize" :stroke-width="1.75" />
    <span v-else class="avatar-initials">{{ iniciales }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  Truck, Package, Pill, ShoppingBasket, User, Stethoscope,
  Headset, Shield,
} from 'lucide-vue-next'

const props = defineProps({
  nombre: { type: String, default: '' },
  tipo: { type: String, default: 'persona' },
  size: { type: String, default: 'md' },
  muted: { type: Boolean, default: false },
})

const TIPO_ICONO = {
  proveedor: Truck,
  'producto-medicamento': Pill,
  'producto-alimento': ShoppingBasket,
  'producto-accesorio': Package,
  veterinario: Stethoscope,
  recepcionista: Headset,
  admin: Shield,
}

const icon = computed(() => TIPO_ICONO[props.tipo] || null)

const iniciales = computed(() => {
  const n = String(props.nombre || '').trim()
  if (!n) return '?'
  const partes = n.split(/\s+/).filter(Boolean)
  if (partes.length === 1) return partes[0].charAt(0).toUpperCase()
  return (partes[0].charAt(0) + partes[partes.length - 1].charAt(0)).toUpperCase()
})

const iconSize = computed(() => ({ sm: 16, md: 22, lg: 28 }[props.size] || 22))
const titulo = computed(() => props.nombre || props.tipo)
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
.avatar-initials {
  font-weight: var(--font-bold);
  letter-spacing: -0.02em;
  line-height: 1;
}
.avatar-sm .avatar-initials { font-size: var(--text-sm); }
.avatar-md .avatar-initials { font-size: var(--text-base); }
.avatar-lg .avatar-initials { font-size: var(--text-xl); }
</style>