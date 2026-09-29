<template>
  <div
    class="avatar"
    :class="[`avatar--${size}`, { 'avatar--muted': muted }]"
    :title="titulo"
  >
    <component v-if="icon" :is="icon" :size="iconSize" :stroke-width="1.75" />
    <span v-else class="avatar-initials">{{ iniciales }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Truck, Package, Pill, ShoppingBasket, User, Stethoscope, Headset, Shield } from 'lucide-vue-next'

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
  background: #F1F5F9;
  color: #0F766E;
  border: 1px solid rgba(15, 23, 42, 0.04);
  transition: background-color .15s ease, color .15s ease;
}
.avatar--sm { width: 36px; height: 36px; border-radius: 10px; }
.avatar--md { width: 44px; height: 44px; border-radius: 12px; }
.avatar--lg { width: 52px; height: 52px; border-radius: 14px; }
.avatar--muted { background: #F8FAFC; color: #94A3B8; opacity: .85; }
.avatar-initials { font-weight: 700; letter-spacing: -0.02em; line-height: 1; }
.avatar--sm .avatar-initials { font-size: 12px; }
.avatar--md .avatar-initials { font-size: 14px; }
.avatar--lg .avatar-initials { font-size: 17px; }
</style>