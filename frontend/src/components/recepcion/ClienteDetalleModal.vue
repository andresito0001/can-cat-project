<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="modelValue" class="modal-overlay" @click.self="cerrar">
        <Transition name="slide-up" appear>
          <div v-if="modelValue" class="cliente-modal">
            <header class="modal-head">
              <div class="modal-head-left">
                <EntityAvatar :nombre="cliente?.nombreCompleto" tipo="cliente" size="md" />
                <div>
                  <span class="modal-eyebrow">Cliente</span>
                  <h3>{{ cliente?.nombreCompleto || '—' }}</h3>
                </div>
              </div>
              <button class="modal-close" type="button" @click="cerrar">
                <X :size="17" />
              </button>
            </header>

            <div class="modal-body">
              <section class="data-grid">
                <div class="data-field">
                  <span class="field-label"><CreditCard :size="12" /> Documento</span>
                  <span class="field-value mono">{{ cliente?.documentoIdentidad || '—' }}</span>
                </div>
                <div class="data-field">
                  <span class="field-label"><Phone :size="12" /> Teléfono principal</span>
                  <span class="field-value">{{ cliente?.telefonoPrincipal || '—' }}</span>
                </div>
                <div class="data-field">
                  <span class="field-label"><Phone :size="12" /> Teléfono secundario</span>
                  <span class="field-value">{{ cliente?.telefonoSecundario || '—' }}</span>
                </div>
                <div class="data-field">
                  <span class="field-label"><Mail :size="12" /> Correo</span>
                  <span class="field-value">{{ cliente?.correoElectronico || '—' }}</span>
                </div>
                <div class="data-field">
                  <span class="field-label"><Calendar :size="12" /> Fecha de nacimiento</span>
                  <span class="field-value">{{ fmtFecha(cliente?.fechaNacimiento) }}</span>
                </div>
                <div class="data-field">
                  <span class="field-label"><MapPin :size="12" /> Ciudad</span>
                  <span class="field-value">{{ cliente?.ciudad || '—' }}</span>
                </div>
                <div class="data-field data-field--full">
                  <span class="field-label"><Home :size="12" /> Dirección</span>
                  <span class="field-value">{{ cliente?.direccion || '—' }}</span>
                </div>
              </section>
            </div>

            <footer class="modal-foot">
              <button class="btn btn-ghost" type="button" @click="cerrar">Cerrar</button>
            </footer>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { X, CreditCard, Phone, Mail, Calendar, MapPin, Home } from 'lucide-vue-next'
import EntityAvatar from '@/components/ui/EntityAvatar.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  cliente: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue'])

function cerrar() {
  emit('update:modelValue', false)
}

function fmtFecha(iso) {
  if (!iso) return '—'
  const d = new Date(iso + 'T12:00:00')
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleDateString('es-VE', { day: '2-digit', month: 'long', year: 'numeric' })
}
</script>

<style scoped>
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(15, 23, 42, .55);
  backdrop-filter: blur(4px);
  display: flex; align-items: center; justify-content: center;
  padding: 24px; z-index: 1100;
  font-family: 'Inter', 'Segoe UI', Roboto, sans-serif;
}
.cliente-modal {
  background: #fff; border-radius: 18px;
  width: 100%; max-width: 560px;
  max-height: 90vh; display: flex; flex-direction: column;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .3);
}
.modal-head {
  display: flex; justify-content: space-between; align-items: center;
  gap: 16px; padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
}
.modal-head-left { display: flex; align-items: center; gap: 14px; min-width: 0; }
.modal-eyebrow {
  display: inline-block;
  font-size: 10.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .6px;
  color: #0F766E; margin-bottom: 4px;
}
.modal-head h3 {
  margin: 0; font-size: 16px; font-weight: 700; color: #0F172A;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.modal-close {
  width: 34px; height: 34px; border-radius: 9px;
  border: 1px solid #E2E8F0; background: #fff; color: #64748B;
  display: flex; align-items: center; justify-content: center;
  cursor: pointer; flex-shrink: 0;
}
.modal-close:hover { background: #F8FAFC; color: #1E293B; }

.modal-body { padding: 20px 24px; overflow-y: auto; }

.data-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.data-field {
  display: flex; flex-direction: column; gap: 4px;
  padding: 12px 14px;
  background: #F8FAFC; border: 1px solid #E2E8F0;
  border-radius: 10px;
  min-width: 0;
}
.data-field--full { grid-column: 1 / -1; }
.field-label {
  display: inline-flex; align-items: center; gap: 5px;
  font-size: 10.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .5px;
  color: #94A3B8;
}
.field-value {
  font-size: 13.5px; font-weight: 600; color: #0F172A;
  word-break: break-word;
}
.field-value.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12.5px;
  background: #fff;
  padding: 2px 8px;
  border-radius: 5px;
  align-self: flex-start;
}

.modal-foot {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 16px 24px; border-top: 1px solid #E2E8F0;
  background: #FAFBFC;
}
.btn {
  display: inline-flex; align-items: center; justify-content: center;
  gap: 7px; padding: 10px 20px; border-radius: 10px;
  font-size: 13.5px; font-weight: 700; font-family: inherit;
  cursor: pointer; border: 1px solid #E2E8F0;
  background: #fff; color: #475569;
}
.btn:hover { background: #F8FAFC; color: #0F766E; }

.fade-enter-active, .fade-leave-active { transition: opacity .2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-up-enter-active, .slide-up-leave-active { transition: all .3s cubic-bezier(0.16, 1, 0.3, 1); }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateY(20px) scale(.98); }

@media (max-width: 640px) {
  .data-grid { grid-template-columns: 1fr; }
}

</style>