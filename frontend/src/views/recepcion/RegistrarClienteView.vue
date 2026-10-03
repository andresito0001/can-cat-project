<template>
  <div class="nuevo-cliente">
    <ToastContainer />

    <!-- Breadcrumb -->
    <nav class="breadcrumb" aria-label="Migas de pan">
      <button class="bc-back" type="button" @click="cancelar">
        <ArrowLeft :size="15" />
      </button>
      <button class="bc-item bc-link" type="button" @click="cancelar">
        Gestión de Clientes
      </button>
      <ChevronRight :size="14" class="bc-sep" />
      <span class="bc-item bc-current">Nuevo Cliente</span>
    </nav>

    <!-- Hero -->
    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <Sparkles :size="12" /> Recepción · Registro asistido
        </span>
        <h1>Añadir Nuevo Cliente</h1>
        <p class="page-header-sub">
          Completa los datos del cliente. Recibirá un enlace por correo para definir su contraseña.
        </p>
      </div>
    </header>

    <!-- Layout -->
    <div class="wizard-layout">
      <main class="wizard-main">
        <form @submit.prevent="handleRegistrar" novalidate>
          <Transition name="slide-down">
            <AppAlert v-if="mostrarError" variant="error" class="alert-spacing">
              {{ errorMessage }}
            </AppAlert>
          </Transition>

          <!-- Identificación -->
          <AppCard>
            <template #header>
              <div class="card-header-left">
                <div class="card-icon"><UserCircle :size="16" /></div>
                <div>
                  <h3>Identificación</h3>
                  <p class="card-header-sub">Datos personales del cliente</p>
                </div>
              </div>
            </template>

            <AppFormField
              label="Nombre completo"
              :error="errors.nombreCompleto ? 'Ingresa el nombre completo (mínimo 3 caracteres).' : ''"
              required
            >
              <template #default="{ id, invalid }">
                <AppInput
                  :id="id"
                  v-model="form.nombreCompleto"
                  placeholder="Ej: María Alejandra González"
                  :disabled="isLoading"
                  :error="invalid"
                  autocomplete="name"
                />
              </template>
            </AppFormField>

            <div class="form-grid-2">
              <AppFormField
                label="Cédula / Documento"
                :error="errors.documentoIdentidad ? 'Documento requerido (mínimo 6 caracteres).' : ''"
                required
              >
                <template #default="{ id, invalid }">
                <AppInput
                  :id="id"
                  :model-value="form.documentoIdentidad"
                  placeholder="V-12345678"
                  :disabled="isLoading"
                  :error="invalid"
                  maxlength="11"
                  @update:model-value="form.documentoIdentidad = formatearCedula($event)"
                />
                </template>
              </AppFormField>

              <AppFormField label="Fecha de nacimiento" optional>
                <template #default="{ id }">
                 <DatePicker
                    :id="id"
                    v-model="form.fechaNacimiento"
                    placeholder="Seleccionar fecha"
                    :max="hoy"
                    :disabled="isLoading"
                 />
                </template>
              </AppFormField>
            </div>
          </AppCard>

          <!-- Contacto -->
          <AppCard>
            <template #header>
              <div class="card-header-left">
                <div class="card-icon"><AtSign :size="16" /></div>
                <div>
                  <h3>Contacto</h3>
                  <p class="card-header-sub">Correo y teléfonos del cliente</p>
                </div>
              </div>
            </template>

            <AppFormField
              label="Correo electrónico"
              :error="errors.correoElectronico ? 'Ingresa un correo válido (se usará para la invitación).' : ''"
              :hint="!errors.correoElectronico ? 'Aquí llegará el enlace para que el cliente defina su contraseña.' : ''"
              required
            >
              <template #default="{ id, invalid }">
                <AppInput
                  :id="id"
                  v-model="form.correoElectronico"
                  type="email"
                  placeholder="usuario@ejemplo.com"
                  :disabled="isLoading"
                  :error="invalid"
                  autocomplete="email"
                />
              </template>
            </AppFormField>

            <div class="form-grid-2">
              <AppFormField
                label="Teléfono principal"
                :error="errors.telefonoPrincipal ? 'El teléfono principal es obligatorio.' : ''"
                required
              >
                <template #default="{ id, invalid }">
                  <AppInput
                    :id="id"
                    v-model="form.telefonoPrincipal"
                    type="tel"
                    placeholder="0412-1234567"
                    :disabled="isLoading"
                    :error="invalid"
                    autocomplete="tel"
                  />
                </template>
              </AppFormField>

              <AppFormField label="Teléfono secundario" optional>
                <template #default="{ id }">
                  <AppInput
                    :id="id"
                    v-model="form.telefonoSecundario"
                    type="tel"
                    placeholder="0212-9876543"
                    :disabled="isLoading"
                  />
                </template>
              </AppFormField>
            </div>
          </AppCard>

          <!-- Ubicación -->
          <AppCard>
            <template #header>
              <div class="card-header-left">
                <div class="card-icon"><MapPin :size="16" /></div>
                <div>
                  <h3>Ubicación</h3>
                  <p class="card-header-sub">Dirección de residencia</p>
                </div>
              </div>
            </template>

            <AppFormField
              label="Dirección"
              :error="errors.direccion ? 'La dirección es obligatoria.' : ''"
              required
            >
              <template #default="{ id, invalid }">
                <AppInput
                  :id="id"
                  v-model="form.direccion"
                  placeholder="Av. Principal, Casa 5, Sector Centro"
                  :disabled="isLoading"
                  :error="invalid"
                  autocomplete="street-address"
                />
              </template>
            </AppFormField>

            <AppFormField label="Ciudad" optional>
              <template #default="{ id }">
                <AppInput
                  :id="id"
                  v-model="form.ciudad"
                  placeholder="Ej: Barcelona"
                  :disabled="isLoading"
                  autocomplete="address-level2"
                />
              </template>
            </AppFormField>
          </AppCard>

          <!-- Actions -->
          <div class="form-actions">
            <AppButton variant="ghost" :disabled="isLoading" @click="cancelar">
              <template #icon-left><X :size="15" /></template>
              Cancelar
            </AppButton>
            <AppButton type="submit" variant="primary" size="lg" :loading="isLoading">
              <template #icon-left><UserPlus :size="15" /></template>
              {{ isLoading ? 'Registrando…' : 'Registrar cliente' }}
            </AppButton>
          </div>
        </form>
      </main>

      <!-- Sidebar -->
      <aside class="wizard-sidebar">
        <div class="preview-card">
          <header class="preview-header">
            <h4>Vista previa</h4>
            <span class="preview-hint">Así se verá la ficha</span>
          </header>

          <div class="preview-body">
            <EntityAvatar :nombre="form.nombreCompleto" tipo="cliente" size="lg" />

            <p class="preview-nombre" :class="{ 'is-empty': !form.nombreCompleto }">
              {{ form.nombreCompleto || 'Nombre del cliente' }}
            </p>

            <div class="preview-meta">
              <div class="preview-line" :class="{ 'is-empty': !form.documentoIdentidad }">
                <CreditCard :size="13" />
                <span>{{ form.documentoIdentidad || 'Documento pendiente' }}</span>
              </div>
              <div class="preview-line" :class="{ 'is-empty': !form.correoElectronico }">
                <Mail :size="13" />
                <span>{{ form.correoElectronico || 'Correo pendiente' }}</span>
              </div>
              <div class="preview-line" :class="{ 'is-empty': !form.telefonoPrincipal }">
                <Phone :size="13" />
                <span>{{ form.telefonoPrincipal || 'Teléfono pendiente' }}</span>
              </div>
              <div
                v-if="form.ciudad || form.direccion"
                class="preview-line"
              >
                <MapPin :size="13" />
                <span>
                  {{ [form.ciudad, form.direccion].filter(Boolean).join(' · ') }}
                </span>
              </div>
            </div>
          </div>

          <footer class="preview-footer">
            <div class="progress-top">
              <span class="progress-label">Datos obligatorios</span>
              <span class="progress-count">{{ completadosObligatorios }} / {{ totalObligatorios }}</span>
            </div>
            <div class="progress-bar">
              <div
                class="progress-fill"
                :class="{ 'is-complete': progresoObligatorios === 100 }"
                :style="{ width: progresoObligatorios + '%' }"
              />
            </div>
            <p class="progress-note">
              <component :is="progresoObligatorios === 100 ? CheckCircle2 : Info" :size="12" />
              <span v-if="progresoObligatorios === 100">Listo para registrar</span>
              <span v-else>
                Faltan {{ totalObligatorios - completadosObligatorios }} campo{{
                  totalObligatorios - completadosObligatorios === 1 ? '' : 's'
                }}
              </span>
            </p>
          </footer>
        </div>

        <div class="process-note">
          <div class="process-icon"><Mail :size="16" /></div>
          <div>
            <p class="process-title">Envío automático de invitación</p>
            <p class="process-desc">
              Al registrar, el sistema enviará un correo al cliente con un enlace único para
              que defina su contraseña de acceso.
            </p>
          </div>
        </div>
      </aside>
    </div>

    <!-- Modal de éxito -->
    <AppModal
      :model-value="!!clienteRegistrado"
      title="Cliente registrado con éxito"
      subtitle="La ficha quedó guardada y lista para agendar citas"
      size="md"
      @update:model-value="cerrarModalExito"
    >
      <div v-if="clienteRegistrado" class="modal-success">
        <div class="success-icon-modal">
          <CheckCircle2 :size="36" />
        </div>

        <div class="modal-resumen">
          <div class="resumen-fila">
            <span class="resumen-label"><UserCircle :size="13" /> Nombre</span>
            <span class="resumen-value">{{ clienteRegistrado.nombreCompleto }}</span>
          </div>
          <div class="resumen-fila">
            <span class="resumen-label"><CreditCard :size="13" /> Documento</span>
            <span class="resumen-value mono">{{ clienteRegistrado.documentoIdentidad }}</span>
          </div>
          <div class="resumen-fila">
            <span class="resumen-label"><Mail :size="13" /> Correo</span>
            <span class="resumen-value">{{ clienteRegistrado.correoElectronico }}</span>
          </div>
        </div>

        <AppAlert v-if="clienteRegistrado.invitacionEnviada" variant="success">
          Se envió un enlace al correo del cliente para que defina su contraseña de acceso.
        </AppAlert>
        <AppAlert v-else variant="warning">
          No se pudo enviar el enlace de contraseña. El cliente puede usar la opción
          "¿Olvidaste tu contraseña?".
        </AppAlert>
      </div>

      <template #footer>
        <AppButton variant="secondary" @click="volverAlListado">
          <template #icon-left><ArrowLeft :size="15" /></template>
          Volver al listado
        </AppButton>
        <AppButton variant="primary" @click="irRegistrarMascota">
          <template #icon-left><PawPrint :size="15" /></template>
          Registrar mascota ahora
        </AppButton>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as clientesApi from '@/api/clientes.api'
import {
  AlertCircle, CheckCircle2, PawPrint, ArrowLeft, ChevronRight, X,
  UserPlus, UserCircle, AtSign, Mail, Phone, MapPin, CreditCard,
  Sparkles, Info,
} from 'lucide-vue-next'
import EntityAvatar from '@/components/ui/EntityAvatar.vue'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppModal from '@/components/ui/AppModal.vue'
import AppFormField from '@/components/ui/AppFormField.vue'
import AppInput from '@/components/ui/AppInput.vue'

import DatePicker from '@/components/ui/DatePicker.vue'

 import { formatearCedula, validarCedula } from '@/utils/cedula'

const router = useRouter()
const hoy = new Date().toISOString().split('T')[0]

const isLoading = ref(false)
const mostrarError = ref(false)
const errorMessage = ref('')
const clienteRegistrado = ref(null)

const errors = reactive({
  nombreCompleto: false,
  documentoIdentidad: false,
  correoElectronico: false,
  telefonoPrincipal: false,
  direccion: false,
})

const form = reactive({
  nombreCompleto: '',
  documentoIdentidad: '',
  correoElectronico: '',
  telefonoPrincipal: '',
  telefonoSecundario: '',
  direccion: '',
  ciudad: '',
  fechaNacimiento: '',
})

const totalObligatorios = 5

const completadosObligatorios = computed(() => {
  let count = 0
  if (form.nombreCompleto.trim().length >= 3) count++
  if (form.documentoIdentidad.trim().length >= 6) count++
  if (form.correoElectronico.trim() && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.correoElectronico)) count++
  if (form.telefonoPrincipal.trim()) count++
  if (form.direccion.trim()) count++
  return count
})

const progresoObligatorios = computed(() =>
  Math.round((completadosObligatorios.value / totalObligatorios) * 100)
)

function resetErrors() {
  Object.keys(errors).forEach((k) => (errors[k] = false))
  mostrarError.value = false
  errorMessage.value = ''
}

function validarEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

function cancelar() {
  router.push('/recepcion/clientes')
}

async function handleRegistrar() {
  resetErrors()
  let hayError = false

  if (!form.nombreCompleto.trim() || form.nombreCompleto.trim().length < 3) {
    errors.nombreCompleto = true
    hayError = true
  }
  
  if (validarCedula(form.documentoIdentidad)) {
    errors.documentoIdentidad = true
    hayError = true
  }

  if (!form.correoElectronico.trim() || !validarEmail(form.correoElectronico)) {
    errors.correoElectronico = true
    hayError = true
  }
  if (!form.telefonoPrincipal.trim()) {
    errors.telefonoPrincipal = true
    hayError = true
  }
  if (!form.direccion.trim()) {
    errors.direccion = true
    hayError = true
  }

  if (hayError) {
    errorMessage.value = 'Debe completar todos los campos marcados como obligatorios.'
    mostrarError.value = true
    return
  }

  const payload = { ...form }
  if (!payload.telefonoSecundario) delete payload.telefonoSecundario
  if (!payload.ciudad) delete payload.ciudad
  if (!payload.fechaNacimiento) delete payload.fechaNacimiento

  isLoading.value = true
  try {
    const { data } = await clientesApi.registrarAsistido(payload)
    clienteRegistrado.value = data
  } catch (err) {
    mostrarError.value = true
    const codigo = err.response?.data?.error
    const mensaje = err.response?.data?.message || 'Ocurrió un error al registrar el cliente.'

    if (codigo === 'DOCUMENTO_DUPLICADO') {
      form.documentoIdentidad = ''
      errors.documentoIdentidad = true
      errorMessage.value =
        'El documento de identidad ingresado ya se encuentra registrado. Por favor, utilice el buscador para localizar el perfil del cliente.'
    } else if (codigo === 'CORREO_DUPLICADO') {
      errors.correoElectronico = true
      errorMessage.value = mensaje
    } else {
      errorMessage.value = mensaje
    }
  } finally {
    isLoading.value = false
  }
}

function cerrarModalExito() {
  clienteRegistrado.value = null
}

function irRegistrarMascota() {
  router.push({
    path: '/recepcion/registrar-mascota',
    query: {
      clienteDocumento: clienteRegistrado.value.documentoIdentidad,
      clienteNombre: clienteRegistrado.value.nombreCompleto,
    },
  })
}

function volverAlListado() {
  router.push('/recepcion/clientes')
}
</script>

<style scoped>
.nuevo-cliente {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* BREADCRUMB */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-md);
  color: var(--text-secondary);
}
.bc-back {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--neutral-600);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  margin-right: 2px;
}
.bc-back:hover { background: var(--neutral-100); color: var(--brand-700); border-color: var(--border-strong); }
.bc-item { font-weight: var(--font-medium); }
.bc-link {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
  font-size: var(--text-md);
  font-family: inherit;
}
.bc-link:hover { color: var(--brand-700); text-decoration: underline; }
.bc-current { color: var(--text-primary); font-weight: var(--font-bold); }
.bc-sep { color: var(--neutral-300); }

/* LAYOUT */
.wizard-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: var(--space-5);
  align-items: start;
}
.wizard-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.wizard-sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  position: sticky;
  top: var(--space-6);
}

/* CARD HEADER */
.card-header-left { display: flex; align-items: center; gap: var(--space-3); min-width: 0; }
.card-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  background: var(--brand-50);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* FORM GRID */
.form-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}
.form-grid-2 :deep(.form-group) { margin-bottom: 0; }

.alert-spacing { margin-bottom: var(--space-5); }

/* ACTIONS */
.form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border-radius: var(--radius-2xl);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
}

/* PREVIEW */
.preview-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.preview-header {
  padding: var(--space-4) var(--space-5) var(--space-3);
  border-bottom: 1px solid var(--border-subtle);
}
.preview-header h4 {
  margin: 0;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.preview-hint { display: block; margin-top: 2px; font-size: var(--text-xs); color: var(--text-tertiary); }
.preview-body {
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
}
.preview-nombre {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: var(--leading-snug);
  word-break: break-word;
}
.preview-nombre.is-empty { color: var(--text-tertiary); font-weight: var(--font-medium); font-style: italic; }
.preview-meta {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  margin-top: var(--space-1);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-subtle);
}
.preview-line {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--neutral-700);
  text-align: left;
  line-height: var(--leading-snug);
}
.preview-line svg { color: var(--text-tertiary); flex-shrink: 0; }
.preview-line span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.preview-line.is-empty { color: var(--text-tertiary); font-style: italic; }

.preview-footer {
  padding: var(--space-4) var(--space-5) var(--space-5);
  background: linear-gradient(180deg, var(--neutral-50) 0%, var(--brand-50) 100%);
  border-top: 1px solid var(--border-subtle);
}
.progress-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}
.progress-label {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}
.progress-count {
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  font-variant-numeric: tabular-nums;
}
.progress-bar {
  height: 6px;
  background: var(--neutral-200);
  border-radius: var(--radius-full);
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--brand-700), var(--brand-500));
  border-radius: var(--radius-full);
  transition: width var(--duration-slow) var(--ease-out);
}
.progress-fill.is-complete { background: linear-gradient(90deg, var(--success-600), var(--success-500)); }
.progress-note {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: var(--space-3) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-snug);
}
.progress-note svg { color: var(--brand-700); flex-shrink: 0; }

/* PROCESS NOTE */
.process-note {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--brand-50);
  border: 1px solid var(--brand-200);
  border-radius: var(--radius-xl);
}
.process-icon {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: var(--bg-surface);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.process-title { margin: 0 0 2px; font-size: var(--text-sm); font-weight: var(--font-bold); color: var(--brand-700); }
.process-desc { margin: 0; font-size: var(--text-sm); color: var(--neutral-700); line-height: var(--leading-snug); }

/* MODAL SUCCESS */
.modal-success {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-4);
}
.success-icon-modal {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--success-50) 0%, var(--brand-50) 100%);
  border: 3px solid var(--success-500);
  color: var(--success-600);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: successPop 0.5s var(--ease-spring);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}
.modal-resumen {
  width: 100%;
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-1) var(--space-4);
  text-align: left;
}
.resumen-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-subtle);
  font-size: var(--text-md);
}
.resumen-fila:last-child { border-bottom: none; }
.resumen-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  font-weight: var(--font-semibold);
  white-space: nowrap;
  font-size: var(--text-sm);
}
.resumen-value { color: var(--text-primary); font-weight: var(--font-bold); text-align: right; word-break: break-word; }
.mono {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  background: var(--neutral-100);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
}

/* RESPONSIVE */
@media (max-width: 1024px) {
  .wizard-layout { grid-template-columns: 1fr; }
  .wizard-sidebar { position: static; }
}
@media (max-width: 640px) {
  .nuevo-cliente { padding: var(--space-4); }
  .form-grid-2 { grid-template-columns: 1fr; }
  .form-actions { flex-direction: column-reverse; }
  .form-actions :deep(.btn) { width: 100%; }
  .resumen-fila { flex-direction: column; align-items: flex-start; gap: var(--space-1); }
  .resumen-value { text-align: left; }
}
</style>