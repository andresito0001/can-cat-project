<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="modelValue" class="modal-overlay" @click.self="handleOverlayClick">
        <Transition name="slide-up" appear>
          <div
            v-if="modelValue"
            class="modal-container"
            :class="sizeClass"
            role="dialog"
            aria-modal="true"
            :aria-labelledby="titleId"
          >
            <header class="modal-header">
              <div class="modal-header-titles">
                <h3 :id="titleId">{{ title }}</h3>
                <p v-if="subtitle" class="modal-sub">{{ subtitle }}</p>
              </div>
              <button
                class="modal-close"
                type="button"
                :disabled="loading"
                aria-label="Cerrar"
                @click="close"
              >
                <X :size="18" style="flex-shrink: 0;" />
              </button>
            </header>

            <div class="modal-body">
              <slot />
            </div>

            <footer v-if="$slots.footer" class="modal-footer">
              <slot name="footer" />
            </footer>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed, watch, onBeforeUnmount } from 'vue'
import { X } from 'lucide-vue-next'

const props = defineProps({
  modelValue:     { type: Boolean, default: false },
  title:          { type: String, default: '' },
  subtitle:       { type: String, default: '' },
  size:           { type: String, default: 'md' },
  loading:        { type: Boolean, default: false },
  closeOnOverlay: { type: Boolean, default: true },
  closeOnEscape:  { type: Boolean, default: true },
})
const emit = defineEmits(['update:modelValue', 'close'])

const sizeClass = computed(() => `modal-${props.size}`)
const titleId = `modal-title-${Math.random().toString(36).slice(2, 8)}`

function close() {
  if (props.loading) return
  emit('update:modelValue', false)
  emit('close')
}

function handleOverlayClick() {
  if (props.closeOnOverlay) close()
}

function onKeydown(e) {
  if (e.key === 'Escape' && props.closeOnEscape) close()
}

watch(() => props.modelValue, (open) => {
  if (open) {
    window.addEventListener('keydown', onKeydown)
    document.body.style.overflow = 'hidden'
  } else {
    window.removeEventListener('keydown', onKeydown)
    document.body.style.overflow = ''
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})
</script>

<style scoped>
/* El botón de cerrar usa inline style a propósito:
   garantiza el tamaño 34x34 sin depender del cascade de CSS global. */
.modal-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  padding: 0;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--text-secondary);
  cursor: pointer;
  flex: 0 0 34px;
  min-width: 34px;
  min-height: 34px;
  max-width: 34px;
  max-height: 34px;
  box-sizing: border-box;
  transition: background-color var(--duration-fast) var(--ease-out),
              color var(--duration-fast) var(--ease-out);
}
.modal-close:hover:not(:disabled) {
  background: var(--neutral-100);
  color: var(--text-primary);
}
.modal-close:disabled { opacity: 0.55; cursor: not-allowed; }

/* El contenedor de títulos maneja el overflow sin romper el botón */
.modal-header-titles {
  flex: 1 1 auto;
  min-width: 0;
  overflow: hidden;
}
.modal-header-titles h3 {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>