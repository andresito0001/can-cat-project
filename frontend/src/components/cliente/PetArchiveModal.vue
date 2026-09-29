<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="modal-overlay" @click.self="!procesando && cerrar()">
        <Transition name="slide-up" appear>
          <div v-if="visible" class="modal-container" role="dialog" aria-modal="true">
            <div class="archive-icon-wrap">
              <div class="archive-icon">
                <Archive :size="22" />
              </div>
            </div>

            <h3 class="archive-title">¿Por qué desactivas a {{ mascota?.nombre }}?</h3>
            <p class="archive-text">
              Su historial clínico se conservará. Podrás reactivarla en cualquier momento
              desde la pestaña <strong>Inactivas</strong> o <strong>Fallecidas</strong>.
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
              <div v-if="error" class="alert-error">
                <AlertCircle :size="16" /><span>{{ error }}</span>
              </div>
            </Transition>

            <div class="archive-actions">
              <button type="button" class="btn-secondary" :disabled="procesando" @click="cerrar">
                Cancelar
              </button>
            </div>

            <div v-if="procesando" class="processing-overlay">
              <Loader2 :size="24" class="spin" />
              <span>Procesando…</span>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Archive, UserMinus, HeartOff, ChevronRight, AlertCircle, Loader2 } from 'lucide-vue-next'
import { cambiarEstadoMascota } from '@/api/mascotas.api.js'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

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

function cerrar() {
  if (procesando.value) return
  emit('close')
}

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
    cerrar()
  } catch (err) {
    error.value = getApiErrorMessage(err) || 'No se pudo actualizar la mascota.'
  } finally {
    procesando.value = false
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  padding: 24px; z-index: 110;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}
.modal-container {
  background: #fff; border-radius: 18px; width: 100%; max-width: 480px;
  padding: 28px 26px 22px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  position: relative;
  text-align: center;
}
.archive-icon-wrap { display: flex; justify-content: center; margin-bottom: 14px; }
.archive-icon {
  width: 56px; height: 56px; border-radius: 16px;
  background: #FEF3C7; color: #B45309;
  display: flex; align-items: center; justify-content: center;
}
.archive-title {
  font-size: 17px; font-weight: 700; color: #1E293B;
  margin: 0 0 8px;
}
.archive-text {
  font-size: 13.5px; color: #64748B; margin: 0 0 20px; line-height: 1.55;
}
.archive-text strong { color: #334155; }

.opciones { display: flex; flex-direction: column; gap: 10px; margin-bottom: 16px; }
.opcion {
  display: grid; grid-template-columns: auto 1fr auto;
  gap: 14px; align-items: center;
  padding: 14px 16px; background: #fff;
  border: 1.5px solid #E2E8F0; border-radius: 12px;
  cursor: pointer; font-family: inherit;
  text-align: left; transition: all .15s;
}
.opcion:hover:not(:disabled) {
  border-color: #0F766E; background: #F8FAFC;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -8px rgba(15, 118, 110, 0.25);
}
.opcion:disabled { opacity: 0.5; cursor: not-allowed; }

.opcion-icon {
  width: 40px; height: 40px; border-radius: 11px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.opcion-icon--inactiva { background: #EFF6FF; color: #2563EB; }
.opcion-icon--fallecida { background: #FEF2F2; color: #DC2626; }

.opcion-texto { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.opcion-texto strong { font-size: 14px; font-weight: 700; color: #0F172A; }
.opcion-texto span { font-size: 12px; color: #64748B; line-height: 1.35; }
.opcion-arrow { color: #CBD5E1; flex-shrink: 0; }
.opcion:hover .opcion-arrow { color: #0F766E; }

.alert-error {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 12px 14px; background: #FEF2F2; color: #B91C1C;
  border-radius: 10px; font-size: 13px; font-weight: 500;
  border: 1px solid #FECACA; text-align: left;
  margin-bottom: 12px;
}

.archive-actions { display: flex; justify-content: center; }
.btn-secondary {
  padding: 9px 22px; background: #F1F5F9; color: #475569;
  border: 1px solid #E2E8F0; border-radius: 10px; font-size: 13.5px;
  font-weight: 600; cursor: pointer; font-family: inherit;
}
.btn-secondary:hover:not(:disabled) { background: #E2E8F0; }
.btn-secondary:disabled { opacity: 0.55; cursor: not-allowed; }

.processing-overlay {
  position: absolute; inset: 0;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(2px);
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 10px; border-radius: 18px;
  font-size: 13px; color: #0F766E; font-weight: 600;
}
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.fade-enter-active, .fade-leave-active { transition: opacity .25s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-up-enter-active, .slide-up-leave-active { transition: all .3s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateY(24px) scale(.97); }
</style>