<template>
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="visible" class="modal-overlay" @click.self="cerrar">
        <Transition name="slide-up">
          <div v-if="visible" class="modal-card">
            <header class="modal-header">
              <div class="modal-header-left">
                <div class="modal-icon">
                  <PackagePlus :size="18" />
                </div>
                <div>
                  <h3>{{ esEdicion ? 'Editar producto' : 'Nuevo producto' }}</h3>
                  <p class="modal-header-sub">
                    {{ esEdicion ? 'Actualiza los datos del producto' : 'Registra un nuevo ítem en el catálogo' }}
                  </p>
                </div>
              </div>
              <button class="btn-close" type="button" :disabled="guardando" @click="cerrar">
                <X :size="18" />
              </button>
            </header>

            <div class="modal-body">
              <div v-if="error" class="alert alert-error">
                <AlertCircle :size="16" />
                <span>{{ error }}</span>
              </div>

              <!-- Sección: Identificación -->
              <div class="section-block">
                <p class="section-eyebrow"><Tag :size="12" /> Identificación</p>

                <div class="form-group">
                  <label class="form-label" for="nombre">
                    Nombre del producto <span class="required">*</span>
                  </label>
                  <input
                    id="nombre"
                    v-model="form.nombre"
                    type="text"
                    class="form-input"
                    :class="{ 'is-invalid': errors.nombre }"
                    placeholder="Ej: Vacuna Antirrábica"
                    :disabled="guardando"
                  />
                  <span v-if="errors.nombre" class="form-error">El nombre es obligatorio.</span>
                </div>

                <div class="form-grid-2">
                  <div class="form-group">
                    <label class="form-label" for="sku">
                      Código SKU <span class="required">*</span>
                    </label>
                    <input
                      id="sku"
                      v-model="form.codigoSku"
                      type="text"
                      class="form-input"
                      :class="{ 'is-invalid': errors.codigoSku }"
                      placeholder="Ej: VAC-001"
                      :disabled="guardando"
                    />
                    <span v-if="errors.codigoSku" class="form-error">El SKU es obligatorio.</span>
                  </div>

                  <div class="form-group">
                    <label class="form-label" for="categoria">
                      Categoría <span class="required">*</span>
                    </label>
                    <select
                      id="categoria"
                      v-model="form.categoria"
                      class="form-select"
                      :class="{ 'is-invalid': errors.categoria }"
                      :disabled="guardando"
                    >
                      <option value="" disabled>Seleccione…</option>
                      <option v-for="cat in categorias" :key="cat" :value="cat">{{ cat }}</option>
                    </select>
                    <span v-if="errors.categoria" class="form-error">Selecciona una categoría.</span>
                  </div>
                </div>

                <div class="form-group">
                  <label class="form-label" for="descripcion">
                    Descripción <span class="optional">(opcional)</span>
                  </label>
                  <textarea
                    id="descripcion"
                    v-model="form.descripcion"
                    class="form-textarea"
                    rows="2"
                    maxlength="255"
                    placeholder="Breve descripción del producto…"
                    :disabled="guardando"
                  />
                </div>
              </div>

              <!-- Sección: Presentación y precios -->
              <div class="section-block">
                <p class="section-eyebrow"><DollarSign :size="12" /> Presentación y precios</p>

                <div class="form-grid-2">
                  <div class="form-group">
                    <label class="form-label" for="presentacion">
                      Presentación <span class="required">*</span>
                    </label>
                    <select
                      id="presentacion"
                      v-model="form.presentacion"
                      class="form-select"
                      :disabled="guardando"
                    >
                      <option v-for="u in unidadesMedida" :key="u" :value="u">{{ u }}</option>
                    </select>
                  </div>

                  <div class="form-group">
                    <label class="form-label" for="precioVenta">
                      Precio de venta (USD) <span class="required">*</span>
                    </label>
                    <input
                      id="precioVenta"
                      v-model.number="form.precioVenta"
                      type="number"
                      step="0.01"
                      min="0"
                      class="form-input"
                      :class="{ 'is-invalid': errors.precioVenta }"
                      placeholder="0.00"
                      :disabled="guardando"
                    />
                    <span v-if="errors.precioVenta" class="form-error">Precio inválido.</span>
                  </div>

                  <div class="form-group">
                    <label class="form-label" for="costoAdquisicion">
                      Costo de adquisición (USD) <span class="optional">(opcional)</span>
                    </label>
                    <input
                      id="costoAdquisicion"
                      v-model.number="form.costoAdquisicion"
                      type="number"
                      step="0.01"
                      min="0"
                      class="form-input"
                      placeholder="0.00"
                      :disabled="guardando"
                    />
                  </div>

                  <div class="form-group">
                    <label class="form-label" for="requiereReceta">Requiere receta</label>
                    <label class="check-row">
                      <input v-model="form.requiereReceta" type="checkbox" class="checkbox-input" />
                      <span class="checkbox-box">
                        <Check v-if="form.requiereReceta" :size="12" />
                      </span>
                      <span class="checkbox-label">Solo venta con receta médica</span>
                    </label>
                  </div>
                </div>
              </div>

              <!-- Sección: Stock inicial (solo al crear) -->
              <div v-if="!esEdicion" class="section-block">
                <p class="section-eyebrow"><Layers :size="12" /> Stock inicial</p>
                <div class="form-grid-3">
                  <div class="form-group">
                    <label class="form-label" for="stockActual">Stock actual</label>
                    <input
                      id="stockActual"
                      v-model.number="form.stockActual"
                      type="number"
                      min="0"
                      class="form-input"
                      :disabled="guardando"
                    />
                  </div>
                  <div class="form-group">
                    <label class="form-label" for="stockMinimo">
                      Stock mínimo <span class="required">*</span>
                    </label>
                    <input
                      id="stockMinimo"
                      v-model.number="form.stockMinimo"
                      type="number"
                      min="0"
                      class="form-input"
                      :class="{ 'is-invalid': errors.stockMinimo }"
                      :disabled="guardando"
                    />
                    <span v-if="errors.stockMinimo" class="form-error">Requerido.</span>
                  </div>
                  <div class="form-group">
                    <label class="form-label" for="stockMaximo">
                      Stock máximo <span class="optional">(opcional)</span>
                    </label>
                    <input
                      id="stockMaximo"
                      v-model.number="form.stockMaximo"
                      type="number"
                      min="0"
                      class="form-input"
                      :disabled="guardando"
                    />
                  </div>
                </div>
              </div>

              <!-- Sección: Proveedor (solo si hay) -->
              <div v-if="proveedores.length" class="section-block">
                <p class="section-eyebrow"><Truck :size="12" /> Proveedor</p>
                <div class="form-group">
                  <label class="form-label" for="proveedor">
                    Proveedor predeterminado <span class="optional">(opcional)</span>
                  </label>
                  <select
                    id="proveedor"
                    v-model="form.idProveedorPredeterminado"
                    class="form-select"
                    :disabled="guardando"
                  >
                    <option :value="null">Sin proveedor asignado</option>
                    <option v-for="p in proveedores" :key="p.id" :value="p.id">
                      {{ p.nombreEmpresa }}
                    </option>
                  </select>
                </div>
              </div>
            </div>

            <footer class="modal-footer">
              <button class="btn-secondary" type="button" :disabled="guardando" @click="cerrar">
                Cancelar
              </button>
              <button class="btn-primary" type="button" :disabled="guardando" @click="guardar">
                <Loader2 v-if="guardando" :size="15" class="spin" />
                <Save v-else :size="15" />
                {{ guardando ? 'Guardando…' : (esEdicion ? 'Guardar cambios' : 'Crear producto') }}
              </button>
            </footer>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import {
  X, PackagePlus, Tag, DollarSign, Layers, Truck,
  AlertCircle, Loader2, Save, Check
} from 'lucide-vue-next'
import { UNIDADES_MEDIDA, crearProducto, actualizarProducto } from '@/api/almacen.api'
import { getApiErrorMessage } from '@/utils/apiError'

const props = defineProps({
  visible: { type: Boolean, default: false },
  producto: { type: Object, default: null },
  categorias: { type: Array, default: () => [] },
  proveedores: { type: Array, default: () => [] },
  prefillNombre: { type: String, default: '' },
})

const emit = defineEmits(['close', 'saved'])

const unidadesMedida = UNIDADES_MEDIDA

const guardando = ref(false)
const error = ref('')

const form = reactive({
  nombre: '',
  codigoSku: '',
  descripcion: '',
  categoria: '',
  presentacion: 'Unidad',
  precioVenta: 0,
  costoAdquisicion: 0,
  stockActual: 0,
  stockMinimo: 5,
  stockMaximo: null,
  requiereReceta: false,
  idProveedorPredeterminado: null,
})

const errors = reactive({
  nombre: false,
  codigoSku: false,
  categoria: false,
  precioVenta: false,
  stockMinimo: false,
})

const esEdicion = computed(() => !!props.producto?.id)

// Reset del form cuando cambia el producto
watch(() => props.visible, (v) => {
  if (v) {
    resetForm()
  }
})

function resetForm() {
  Object.keys(errors).forEach((k) => (errors[k] = false))
  error.value = ''

  if (props.producto) {
    form.nombre = props.producto.nombre || ''
    form.codigoSku = props.producto.codigoSku || ''
    form.descripcion = props.producto.descripcion || ''
    form.categoria = props.producto.categoria || ''
    form.presentacion = props.producto.presentacion || 'Unidad'
    form.precioVenta = props.producto.precioVenta || 0
    form.costoAdquisicion = props.producto.costoAdquisicion || 0
    form.stockActual = props.producto.stockActual || 0
    form.stockMinimo = props.producto.stockMinimo || 5
    form.stockMaximo = props.producto.stockMaximo || null
    form.requiereReceta = !!props.producto.requiereReceta
    form.idProveedorPredeterminado = props.producto.idProveedorPredeterminado || null
  } else {
    form.nombre = props.prefillNombre || ''
    form.codigoSku = ''
    form.descripcion = ''
    form.categoria = props.categorias[0] || ''
    form.presentacion = 'Unidad'
    form.precioVenta = 0
    form.costoAdquisicion = 0
    form.stockActual = 0
    form.stockMinimo = 5
    form.stockMaximo = null
    form.requiereReceta = false
    form.idProveedorPredeterminado = null
  }
}

function cerrar() {
  if (guardando.value) return
  emit('close')
}

function validar() {
  const errs = {}
  if (!form.nombre.trim()) { errors.nombre = true; errs.nombre = true }
  if (!form.codigoSku.trim()) { errors.codigoSku = true; errs.codigoSku = true }
  if (!form.categoria) { errors.categoria = true; errs.categoria = true }
  if (form.precioVenta == null || form.precioVenta < 0) { errors.precioVenta = true; errs.precioVenta = true }
  if (form.stockMinimo == null || form.stockMinimo < 0) { errors.stockMinimo = true; errs.stockMinimo = true }
  return Object.keys(errs).length === 0
}

async function guardar() {
  error.value = ''
  Object.keys(errors).forEach((k) => (errors[k] = false))

  if (!validar()) {
    error.value = 'Verifique los campos marcados.'
    return
  }

  guardando.value = true
  try {
    const payload = {
      nombre: form.nombre.trim(),
      codigoSku: form.codigoSku.trim().toUpperCase(),
      descripcion: form.descripcion.trim() || null,
      categoria: form.categoria,
      presentacion: form.presentacion,
      precioVenta: Number(form.precioVenta),
      costoAdquisicion: form.costoAdquisicion ? Number(form.costoAdquisicion) : null,
      stockActual: Number(form.stockActual || 0),
      stockMinimo: Number(form.stockMinimo),
      stockMaximo: form.stockMaximo ? Number(form.stockMaximo) : null,
      requiereReceta: form.requiereReceta,
      idProveedorPredeterminado: form.idProveedorPredeterminado,
    }

    const { data } = esEdicion.value
      ? await actualizarProducto(props.producto.id, payload)
      : await crearProducto(payload)

    emit('saved', data)
  } catch (err) {
    error.value = getApiErrorMessage(err)
  } finally {
    guardando.value = false
  }
}
</script>

<style scoped>
* { box-sizing: border-box; }

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 100;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.modal-card {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-width: 620px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .3);
}

/* ─── Header ─── */
.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 24px 18px;
  border-bottom: 1px solid #E2E8F0;
}
.modal-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.modal-icon {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  background: #F0FDFA;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.modal-header h3 {
  margin: 0 0 2px;
  font-size: 15.5px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
}
.modal-header-sub {
  margin: 0;
  font-size: 12.5px;
  color: #64748B;
}
.btn-close {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  border: 1px solid #E2E8F0;
  background: #fff;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .15s;
  flex-shrink: 0;
}
.btn-close:hover:not(:disabled) {
  background: #F1F5F9;
  color: #0F172A;
}
.btn-close:disabled { opacity: .5; cursor: not-allowed; }

/* ─── Body ─── */
.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  scrollbar-width: thin;
  scrollbar-color: #CBD5E1 transparent;
}
.modal-body::-webkit-scrollbar { width: 6px; }
.modal-body::-webkit-scrollbar-thumb {
  background: #CBD5E1;
  border-radius: 3px;
}

/* ─── Secciones ─── */
.section-block {
  padding-bottom: 18px;
  border-bottom: 1px solid #F1F5F9;
}
.section-block:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.section-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 14px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .7px;
  color: #0F766E;
}

/* ─── Form ─── */
.form-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
.form-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
  margin-bottom: 14px;
}
.form-group:last-child { margin-bottom: 0; }
.form-label {
  font-size: 12.5px;
  font-weight: 600;
  color: #374151;
}
.required { color: #EF4444; }
.optional {
  color: #94A3B8;
  font-weight: 500;
  font-size: 11.5px;
  margin-left: 2px;
}
.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  font-size: 13.5px;
  color: #0F172A;
  background: #fff;
  font-family: inherit;
  transition: border-color .2s, box-shadow .2s;
  box-sizing: border-box;
  appearance: none;
  -webkit-appearance: none;
}
.form-select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px;
  cursor: pointer;
}
.form-textarea { resize: vertical; min-height: 60px; line-height: 1.5; }
.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.form-input:disabled,
.form-select:disabled,
.form-textarea:disabled {
  background: #F8FAFC;
  color: #94A3B8;
  cursor: not-allowed;
}
.is-invalid { border-color: #EF4444 !important; background: #FEF2F2 !important; }
.form-error {
  font-size: 11.5px;
  color: #EF4444;
  font-weight: 600;
}

/* ─── Checkbox ─── */
.check-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #F8FAFC;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  cursor: pointer;
  transition: all .2s;
  height: 40px;
}
.check-row:hover { border-color: #99F6E4; background: #F0FDFA; }
.checkbox-input { display: none; }
.checkbox-box {
  width: 18px;
  height: 18px;
  border-radius: 5px;
  border: 2px solid #CBD5E1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  flex-shrink: 0;
  transition: all .2s;
  color: #fff;
}
.check-row .checkbox-input:checked + .checkbox-box {
  background: #0F766E;
  border-color: #0F766E;
}
.checkbox-label {
  font-size: 12.5px;
  font-weight: 600;
  color: #1E293B;
}

/* ─── Alert ─── */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  font-weight: 500;
}
.alert-error { background: #FEF2F2; color: #991B1B; border: 1px solid #FECACA; }

/* ─── Footer ─── */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid #E2E8F0;
  background: #FAFBFC;
}

/* ─── Botones ─── */
.btn-primary,
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s;
  font-family: inherit;
  white-space: nowrap;
}
.btn-primary {
  background: #0F766E;
  color: #fff;
  border: none;
}
.btn-primary:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-primary:disabled { opacity: .55; cursor: not-allowed; }
.btn-secondary {
  background: #fff;
  color: #475569;
  border: 1px solid #E2E8F0;
}
.btn-secondary:hover:not(:disabled) {
  background: #F8FAFC;
  border-color: #CBD5E1;
}
.btn-secondary:disabled { opacity: .55; cursor: not-allowed; }

.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ─── Transiciones ─── */
.fade-enter-active,
.fade-leave-active { transition: opacity .25s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

.slide-up-enter-active,
.slide-up-leave-active {
  transition: opacity .3s cubic-bezier(0.16, 1, 0.3, 1),
              transform .3s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(.98);
}

/* ─── Responsive ─── */
@media (max-width: 640px) {
  .modal-overlay { padding: 12px; }
  .modal-card { max-width: 100%; max-height: 95vh; }
  .form-grid-2,
  .form-grid-3 { grid-template-columns: 1fr; gap: 12px; }
  .modal-footer { flex-direction: column-reverse; }
  .modal-footer .btn-primary,
  .modal-footer .btn-secondary { width: 100%; }
}
</style>