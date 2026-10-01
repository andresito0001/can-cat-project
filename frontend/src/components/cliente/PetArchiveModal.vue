<template>
  <AppModal
    :model-value="visible"
    title=""
    size="sm"
    :loading="procesando"
    @update:model-value="$emit('close')"
  >
    <div class="archive-content">
      <div class="archive-icon-wrap">
        <div class="archive-icon">
          <Archive :size="22" />
        </div>
      </div>

      <h3 class="archive-title">¿Por qué desactivas a {{ mascota?.nombre }}?</h3>
      <p class="archive-text">
        Su historial clínico se conservará. Podrás reactivarla en cualquier
        momento desde la pestaña <strong>Inactivas</strong> o
        <strong>Fallecidas</strong>.
      </p>

      <div class="opciones">
        <button
          type="button"
          class="opcion"
          :disabled="procesando"
          @click="archivar('Inactiva')"
        >
          <div class="opcion-icon opcion-icon--inactiva">
            <UserMinus :size="20" />
          </div>
          <div class="opcion-texto">
            <strong>Ya no la tengo</strong>
            <span>La mascota sigue viva pero ya no está bajo mi cuidado</span>
          </div>
          <ChevronRight :size="16" class="opcion-arrow" />
        </button>

        <button
          type="button"
          class="opcion"
          :disabled="procesando"
          @click="archivar('Fallecida')"
        >
          <div class="opcion-icon opcion-icon--fallecida">
            <HeartOff :size="20" />
          </div>
          <div class="opcion-texto">
            <strong>Falleció</strong>
            <span>La mascota ya no está con nosotros</span>
          </div>
          <ChevronRight :size="16" class="opcion-arrow" />
        </button>
      </div>

      <Transition name="slide-down">
        <AppAlert v-if="error" variant="error">{{ error }}</AppAlert>
      </Transition>

      <div class="archive-actions">
        <AppButton variant="secondary" :disabled="procesando" @click="$emit('close')">
          Cancelar
        </AppButton>
      </div>
    </div>
  </AppModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Archive, UserMinus, HeartOff, ChevronRight } from 'lucide-vue-next'
import { cambiarEstadoMascota } from '@/api/mascotas.api.js'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

import AppModal from '@/components/ui/AppModal.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppAlert from '@/components/ui/AppAlert.vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  mascota: { type: Object, default: null },
})
const emit = defineEmits(['close', 'archived'])

const { toastSuccess } = useToast()
const procesando = ref(false)
const error = ref('')

watch(() => props.visible, (v) => {
  if (v) {
    procesando.value = false
    error.value = ''
  }
})

async function archivar(estado) {
  if (!props.mascota || procesando.value) return
  procesando.value = true
  error.value = ''
  try {
    const { data } = await cambiarEstadoMascota(props.mascota.idMascota, estado)
    toastSuccess(
      estado === 'Fallecida'
        ? `Lo sentimos. "${props.mascota.nombre}" fue registrada como fallecida.`
        : `"${props.mascota.nombre}" fue desactivada.`
    )
    emit('archived', data)
    emit('close')
  } catch (err) {
    error.value = getApiErrorMessage(err) || 'No se pudo actualizar la mascota.'
  } finally {
    procesando.value = false
  }
}
</script>

<style scoped>
.archive-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-2);
  padding-top: var(--space-2);
}

.archive-icon-wrap { display: flex; justify-content: center; margin-bottom: var(--space-2); }
.archive-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-2xl);
  background: var(--warning-50);
  color: var(--warning-700);
  display: flex;
  align-items: center;
  justify-content: center;
}

.archive-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
  letter-spacing: var(--tracking-tight);
}
.archive-text {
  font-size: var(--text-md);
  color: var(--text-secondary);
  margin: 0 0 var(--space-4);
  line-height: var(--leading-relaxed);
  max-width: 400px;
}
.archive-text strong { color: var(--neutral-700); font-weight: var(--font-bold); }

.opciones {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  width: 100%;
  margin-bottom: var(--space-4);
}
.opcion {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-4);
  background: var(--bg-surface);
  border: 1.5px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: all var(--duration-fast) var(--ease-out);
}
.opcion:hover:not(:disabled) {
  border-color: var(--brand-700);
  background: var(--brand-50);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -8px rgba(15, 118, 110, 0.25);
}
.opcion:disabled { opacity: 0.5; cursor: not-allowed; }

.opcion-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.opcion-icon--inactiva  { background: var(--info-50);    color: var(--info-600); }
.opcion-icon--fallecida { background: var(--danger-50);  color: var(--danger-600); }

.opcion-texto { display: flex; flex-direction: column; gap: var(--space-1); min-width: 0; }
.opcion-texto strong {
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}
.opcion-texto span {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-snug);
}
.opcion-arrow {
  color: var(--neutral-300);
  flex-shrink: 0;
  transition: color var(--duration-fast) var(--ease-out);
}
.opcion:hover .opcion-arrow { color: var(--brand-700); }

.archive-actions {
  display: flex;
  justify-content: center;
  width: 100%;
}
</style>