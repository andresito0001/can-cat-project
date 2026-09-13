<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import * as clientesApi from '@/api/clientes.api'
import { AlertCircle, CheckCircle, PawPrint, ArrowLeft } from 'lucide-vue-next'

const router = useRouter()

const isLoading = ref(false)
const mostrarError = ref(false)
const errorMessage = ref('')
const clienteRegistrado = ref(null) // cuando se llena, se muestra el modal de éxito

const errors = reactive({
  nombreCompleto: false,
  documentoIdentidad: false,
  correoElectronico: false,
  telefonoPrincipal: false,
  direccion: false
})

const form = reactive({
  nombreCompleto: '',
  documentoIdentidad: '',
  correoElectronico: '',
  telefonoPrincipal: '',
  telefonoSecundario: '',
  direccion: '',
  ciudad: '',
  fechaNacimiento: ''
})

function resetErrors() {
  Object.keys(errors).forEach(k => errors[k] = false)
  mostrarError.value = false
  errorMessage.value = ''
}

function validarEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

// Flujo alterno 1: cancelación — descarta datos y vuelve al módulo
function cancelar() {
  router.push('/recepcion/clientes')
}

async function handleRegistrar() {
  resetErrors()

  // Flujo alterne 3: datos obligatorios omitidos
  let hayError = false
  if (!form.nombreCompleto.trim() || form.nombreCompleto.trim().length < 3) { errors.nombreCompleto = true; hayError = true }
  if (!form.documentoIdentidad.trim() || form.documentoIdentidad.trim().length < 6) { errors.documentoIdentidad = true; hayError = true }
  if (!form.correoElectronico.trim() || !validarEmail(form.correoElectronico)) { errors.correoElectronico = true; hayError = true }
  if (!form.telefonoPrincipal.trim()) { errors.telefonoPrincipal = true; hayError = true }
  if (!form.direccion.trim()) { errors.direccion = true; hayError = true }

  if (hayError) {
    errorMessage.value = 'Debe completar todos los campos marcados como obligatorios.'
    mostrarError.value = true
    return
  }

  // Limpiar opcionales vacíos
  const payload = { ...form }
  if (!payload.telefonoSecundario) delete payload.telefonoSecundario
  if (!payload.ciudad) delete payload.ciudad
  if (!payload.fechaNacimiento) delete payload.fechaNacimiento

  isLoading.value = true
  try {
    const { data } = await clientesApi.registrarAsistido(payload)
    clienteRegistrado.value = data // Paso 6: modal de éxito + sugerencia Registrar Mascota
  } catch (err) {
    mostrarError.value = true
    const codigo = err.response?.data?.error
    const mensaje = err.response?.data?.message || 'Ocurrió un error al registrar el cliente.'

    if (codigo === 'DOCUMENTO_DUPLICADO') {
      // Flujo alterno 2: se vacía SOLO el documento, el resto se mantiene
      form.documentoIdentidad = ''
      errors.documentoIdentidad = true
      errorMessage.value = 'El documento de identidad ingresado ya se encuentra registrado. Por favor, utilice el buscador para localizar el perfil del cliente.'
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

// Paso 6: sugerencia de Registrar Mascota (pre-seleccionando el cliente)
function irRegistrarMascota() {
  router.push({
    path: '/recepcion/registrar-mascota',
    query: {
      clienteDocumento: clienteRegistrado.value.documentoIdentidad,
      clienteNombre: clienteRegistrado.value.nombreCompleto
    }
  })
}

// Paso 7: retorno al listado general
function volverAlListado() {
  router.push('/recepcion/clientes')
}
</script>

<template>
  <div class="page">
    <button class="back-btn" @click="cancelar">
      <ArrowLeft :size="16" />
      Volver al listado
    </button>

    <div class="form-card">
      <div class="header-section">
        <div class="form-title">Añadir Nuevo Cliente</div>
        <p class="subtitle">Registro asistido en mostrador. El cliente recibirá un enlace en su correo para definir su contraseña.</p>
      </div>

      <Transition name="slide-fade" mode="out-in">
        <div v-if="mostrarError" key="error" class="error-message">
          <AlertCircle :size="18" />
          <span>{{ errorMessage }}</span>
        </div>
      </Transition>

      <form @submit.prevent="handleRegistrar">
        <!-- Nombre Completo -->
        <div class="input-group" :class="{ 'has-error': errors.nombreCompleto }">
          <label>Nombre Completo <span class="required">*</span></label>
          <input v-model="form.nombreCompleto" type="text" placeholder="Ej: María Alejandra González" :disabled="isLoading" />
        </div>

        <!-- Documento y Correo -->
        <div class="form-grid">
          <div class="input-group" :class="{ 'has-error': errors.documentoIdentidad }">
            <label>Cédula / Documento <span class="required">*</span></label>
            <input v-model="form.documentoIdentidad" type="text" placeholder="V-12345678" :disabled="isLoading" />
          </div>
          <div class="input-group" :class="{ 'has-error': errors.correoElectronico }">
            <label>Correo Electrónico <span class="required">*</span></label>
            <input v-model="form.correoElectronico" type="email" placeholder="usuario@ejemplo.com" :disabled="isLoading" />
          </div>
        </div>

        <!-- Teléfonos -->
        <div class="form-grid">
          <div class="input-group" :class="{ 'has-error': errors.telefonoPrincipal }">
            <label>Teléfono Principal <span class="required">*</span></label>
            <input v-model="form.telefonoPrincipal" type="tel" placeholder="0412-1234567" :disabled="isLoading" />
          </div>
          <div class="input-group">
            <label>Teléfono Secundario <span class="optional">(opcional)</span></label>
            <input v-model="form.telefonoSecundario" type="tel" placeholder="0212-9876543" :disabled="isLoading" />
          </div>
        </div>

        <!-- Dirección y Ciudad -->
        <div class="form-grid">
          <div class="input-group" :class="{ 'has-error': errors.direccion }">
            <label>Dirección <span class="required">*</span></label>
            <input v-model="form.direccion" type="text" placeholder="Av. Principal, Casa 5" :disabled="isLoading" />
          </div>
          <div class="input-group">
            <label>Ciudad <span class="optional">(opcional)</span></label>
            <input v-model="form.ciudad" type="text" placeholder="Barcelona" :disabled="isLoading" />
          </div>
        </div>

        <!-- Fecha de Nacimiento -->
        <div class="input-group">
          <label>Fecha de Nacimiento <span class="optional">(opcional)</span></label>
          <input v-model="form.fechaNacimiento" type="date" :disabled="isLoading" />
        </div>

        <div class="button-row">
          <button type="button" class="btn-secondary" :disabled="isLoading" @click="cancelar">Cancelar</button>
          <button type="submit" class="btn-primary" :disabled="isLoading">
            <span v-if="isLoading" class="spinner"></span>
            <span v-else>Registrar</span>
          </button>
        </div>
      </form>
    </div>

    <!-- PASO 6: Modal de éxito con sugerencia de Registrar Mascota -->
    <Transition name="modal-fade">
      <div v-if="clienteRegistrado" class="modal-overlay">
        <div class="modal-card">
          <div class="modal-icon"><CheckCircle :size="48" /></div>
          <h3 class="modal-title">Cliente registrado satisfactoriamente</h3>

          <div class="cliente-resumen">
            <div class="resumen-row"><strong>{{ clienteRegistrado.nombreCompleto }}</strong></div>
            <div class="resumen-row">{{ clienteRegistrado.documentoIdentidad }} · {{ clienteRegistrado.correoElectronico }}</div>
          </div>

          <p v-if="clienteRegistrado.invitacionEnviada" class="modal-note">
            Se envió un enlace al correo del cliente para que defina su contraseña de acceso.
          </p>
          <p v-else class="modal-note warn">
            No se pudo enviar el enlace de contraseña. El cliente puede usar la opción "¿Olvidaste tu contraseña?".
          </p>

          <div class="modal-actions">
            <button class="btn-secondary" @click="volverAlListado">Volver al listado</button>
            <button class="btn-primary" @click="irRegistrarMascota">
              <PawPrint :size="18" />
              Registrar mascota ahora
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; max-width: 720px; }

.back-btn {
  display: inline-flex; align-items: center; gap: 6px; background: none; border: none;
  color: #64748B; font-size: 13px; font-weight: 600; cursor: pointer; padding: 4px 0;
  font-family: inherit; width: fit-content; transition: color 0.2s;
}
.back-btn:hover { color: #0F766E; }

.form-card {
  background: #fff; padding: 40px 32px; border-radius: 16px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.03), 0 20px 40px -4px rgba(15,118,110,0.08);
  border-top: 4px solid #0F766E;
}
.header-section { margin-bottom: 24px; }
.form-title { font-size: 24px; font-weight: 700; color: #1E293B; text-align: center; letter-spacing: -0.03em; margin-bottom: 8px; }
.subtitle { text-align: center; color: #64748B; font-size: 14px; line-height: 1.5; margin: 0; }

.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.input-group { margin-bottom: 20px; }
.input-group label { display: block; font-size: 13px; font-weight: 600; color: #475569; margin-bottom: 8px; }
.required { color: #EF4444; }
.optional { color: #94A3B8; font-weight: 400; font-size: 12px; }
.input-group input {
  width: 100%; padding: 12px 16px; border: 1.5px solid #E2E8F0; border-radius: 10px;
  font-size: 15px; color: #1E293B; background: #F8FAFC; transition: all 0.2s ease;
  outline: none; font-family: inherit; box-sizing: border-box;
}
.input-group input::placeholder { color: #94A3B8; }
.input-group input:focus { border-color: #0F766E; background: #fff; box-shadow: 0 0 0 3px rgba(15,118,110,0.1); }
.input-group.has-error input { border-color: #EF4444; background: #FEF2F2; }
.input-group.has-error input:focus { box-shadow: 0 0 0 3px rgba(239,68,68,0.1); }

.error-message {
  color: #EF4444; font-size: 13px; font-weight: 500; margin-bottom: 20px; text-align: center;
  background: #FEF2F2; padding: 12px 16px; border-radius: 8px; display: flex;
  align-items: center; justify-content: center; gap: 8px; border: 1px solid #FECACA;
}

.button-row { display: flex; justify-content: flex-end; gap: 12px; }
.btn-primary, .btn-secondary {
  padding: 14px 24px; border-radius: 10px; font-size: 15px; font-weight: 600; cursor: pointer;
  transition: all 0.2s ease; display: inline-flex; align-items: center; justify-content: center; gap: 8px; font-family: inherit;
}
.btn-primary { background: #0F766E; color: #fff; border: none; min-width: 140px; }
.btn-primary:hover:not(:disabled) { background: #115E59; transform: translateY(-1px); box-shadow: 0 6px 20px rgba(15,118,110,0.25); }
.btn-primary:disabled { background: #94A3B8; cursor: not-allowed; }
.btn-secondary { background: #fff; color: #475569; border: 1.5px solid #E2E8F0; min-width: 120px; }
.btn-secondary:hover:not(:disabled) { border-color: #CBD5E1; color: #1E293B; }

.spinner {
  width: 18px; height: 18px; border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff; border-radius: 50%; animation: spin 0.6s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* ─── Modal de éxito ─── */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(15, 23, 42, 0.55); backdrop-filter: blur(2px);
  display: flex; justify-content: center; align-items: center; z-index: 100; padding: 20px;
}
.modal-card {
  background: #fff; border-radius: 16px; padding: 40px 32px; max-width: 480px; width: 100%;
  text-align: center; box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25); border-top: 4px solid #0F766E;
}
.modal-icon { color: #10B981; margin-bottom: 16px; }
.modal-title { font-size: 20px; font-weight: 700; color: #1E293B; margin: 0 0 16px; letter-spacing: -0.02em; }
.cliente-resumen { background: #F8FAFC; border-radius: 10px; padding: 14px; margin-bottom: 16px; }
.resumen-row { font-size: 14px; color: #475569; line-height: 1.6; }
.modal-note { font-size: 13px; color: #64748B; line-height: 1.5; margin: 0 0 24px; }
.modal-note.warn { color: #B45309; background: #FFFBEB; border: 1px solid #FDE68A; border-radius: 8px; padding: 10px 14px; }
.modal-actions { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }

.slide-fade-enter-active { transition: all 0.3s ease-out; }
.slide-fade-leave-active { transition: all 0.2s ease-in; }
.slide-fade-enter-from, .slide-fade-leave-to { opacity: 0; }
.modal-fade-enter-active, .modal-fade-leave-active { transition: opacity 0.25s ease; }
.modal-fade-enter-from, .modal-fade-leave-to { opacity: 0; }

@media (max-width: 560px) {
  .form-grid { grid-template-columns: 1fr; }
  .button-row { flex-direction: column-reverse; }
  .btn-primary, .btn-secondary { width: 100%; }
}
</style>