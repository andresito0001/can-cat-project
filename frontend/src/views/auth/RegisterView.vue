<template>
  <div class="login-container">
    <div class="login-card">
      <div class="header-section">
        <div class="login-title">Crear Cuenta</div>
        <p class="subtitle">Regístrate para gestionar las citas de tu mascota.</p>
      </div>

      <Transition name="slide-fade" mode="out-in">
        <div v-if="mostrarError" key="error" class="error-message">
          <AlertCircle :size="18" />
          <span>{{ authStore.error }}</span>
        </div>
      </Transition>

      <form @submit.prevent="handleRegister">
        <!-- Nombre Completo -->
        <div class="input-group" :class="{ 'has-error': errors.nombreCompleto }">
          <label>Nombre Completo <span class="required">*</span></label>
          <input
            v-model="form.nombreCompleto"
            type="text"
            placeholder="Ej: María Alejandra González"
            :disabled="authStore.isLoading"
          />
        </div>

        <!-- Documento y Correo -->
        <div class="form-grid">
          <div class="input-group" :class="{ 'has-error': errors.documentoIdentidad }">
            <label>Cédula / Documento <span class="required">*</span></label>
            <input
              v-model="form.documentoIdentidad"
              type="text"
              placeholder="V-12345678"
              :disabled="authStore.isLoading"
            />
          </div>
          <div class="input-group" :class="{ 'has-error': errors.correoElectronico }">
            <label>Correo Electrónico <span class="required">*</span></label>
            <input
              v-model="form.correoElectronico"
              type="email"
              placeholder="usuario@ejemplo.com"
              :disabled="authStore.isLoading"
            />
          </div>
        </div>

        <!-- Contraseña -->
        <div class="input-group" :class="{ 'has-error': errors.contrasena }">
          <label>Contraseña <span class="required">*</span></label>
          <div class="password-wrapper">
            <input
              v-model="form.contrasena"
              :type="mostrarPassword ? 'text' : 'password'"
              placeholder="Mínimo 6 caracteres"
              :disabled="authStore.isLoading"
            />
            <button type="button" class="toggle-password" @click="mostrarPassword = !mostrarPassword" tabindex="-1">
              <Eye v-if="!mostrarPassword" :size="18" />
              <EyeOff v-else :size="18" />
            </button>
          </div>
        </div>

        <!-- Teléfonos -->
        <div class="form-grid">
          <div class="input-group" :class="{ 'has-error': errors.telefonoPrincipal }">
            <label>Teléfono Principal <span class="required">*</span></label>
            <input
              v-model="form.telefonoPrincipal"
              type="tel"
              placeholder="0412-1234567"
              :disabled="authStore.isLoading"
            />
          </div>
          <div class="input-group">
            <label>Teléfono Secundario <span class="optional">(opcional)</span></label>
            <input
              v-model="form.telefonoSecundario"
              type="tel"
              placeholder="0212-9876543"
              :disabled="authStore.isLoading"
            />
          </div>
        </div>

        <!-- Dirección y Ciudad -->
        <div class="form-grid">
          <div class="input-group">
            <label>Dirección <span class="optional">(opcional)</span></label>
            <input
              v-model="form.direccion"
              type="text"
              placeholder="Av. Principal, Casa 5"
              :disabled="authStore.isLoading"
            />
          </div>
          <div class="input-group">
            <label>Ciudad <span class="optional">(opcional)</span></label>
            <input
              v-model="form.ciudad"
              type="text"
              placeholder="Barcelona"
              :disabled="authStore.isLoading"
            />
          </div>
        </div>

        <!-- Fecha de Nacimiento -->
        <div class="input-group">
          <label>Fecha de Nacimiento <span class="optional">(opcional)</span></label>
          <input
            v-model="form.fechaNacimiento"
            type="date"
            :disabled="authStore.isLoading"
          />
        </div>

        <button type="submit" class="login-button" :disabled="authStore.isLoading">
          <span v-if="authStore.isLoading" class="spinner"></span>
          <span v-else>Registrarse</span>
        </button>
      </form>

      <div class="register-container">
        ¿Ya tienes cuenta?
        <router-link to="/auth/login" class="register-link">Inicia sesión</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { AlertCircle, Eye, EyeOff } from 'lucide-vue-next'

const authStore = useAuthStore()
const router = useRouter()

const mostrarPassword = ref(false)
const mostrarError = ref(false)
const errors = reactive({
  nombreCompleto: false,
  documentoIdentidad: false,
  correoElectronico: false,
  contrasena: false,
  telefonoPrincipal: false
})

const form = reactive({
  nombreCompleto: '',
  documentoIdentidad: '',
  correoElectronico: '',
  contrasena: '',
  telefonoPrincipal: '',
  telefonoSecundario: '',
  direccion: '',
  ciudad: '',
  fechaNacimiento: ''
})

function resetErrors() {
  Object.keys(errors).forEach(k => errors[k] = false)
  mostrarError.value = false
  authStore.error = null
}

function validarEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

async function handleRegister() {
  resetErrors()

  let hayError = false

  if (!form.nombreCompleto.trim() || form.nombreCompleto.trim().length < 3) {
    errors.nombreCompleto = true
    hayError = true
  }
  if (!form.documentoIdentidad.trim() || form.documentoIdentidad.trim().length < 6) {
    errors.documentoIdentidad = true
    hayError = true
  }
  if (!form.correoElectronico.trim() || !validarEmail(form.correoElectronico)) {
    errors.correoElectronico = true
    hayError = true
  }
  if (!form.contrasena || form.contrasena.length < 6) {
    errors.contrasena = true
    hayError = true
  }
  if (!form.telefonoPrincipal.trim()) {
    errors.telefonoPrincipal = true
    hayError = true
  }

  if (hayError) {
    authStore.error = 'Por favor completa todos los campos obligatorios correctamente.'
    mostrarError.value = true
    return
  }

  // Limpiar campos vacíos opcionales para no enviar strings vacíos
  const payload = { ...form }
  if (!payload.telefonoSecundario) delete payload.telefonoSecundario
  if (!payload.direccion) delete payload.direccion
  if (!payload.ciudad) delete payload.ciudad
  if (!payload.fechaNacimiento) delete payload.fechaNacimiento

  const result = await authStore.register(payload)

  if (result.success) {
    alert('¡Registro exitoso! Ahora puedes iniciar sesión.')
    router.push('/auth/login')
  } else {
    mostrarError.value = true
    // Resaltar campo según error del backend
    const msg = (authStore.error || '').toLowerCase()
    if (msg.includes('correo')) errors.correoElectronico = true
    if (msg.includes('documento') || msg.includes('cédula')) errors.documentoIdentidad = true
    if (msg.includes('nombre')) errors.nombreCompleto = true
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #F1F5F9;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 20px;
}

.login-card {
  background-color: #ffffff;
  padding: 40px 32px; /* Padding simétrico estándar */
  border-radius: 16px;
  /* Sombra unificada con el resto del sistema */
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 20px 40px -4px rgba(15, 118, 110, 0.08);
  width: 100%;
  max-width: 520px; /* Movido desde el HTML inline */
  border-top: 4px solid #0F766E;
}

.header-section {
  margin-bottom: 24px;
}

.login-title {
  font-family: 'Poppins', sans-serif;
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  margin-bottom: 8px;
  text-align: center;
  letter-spacing: -0.03em;
}

.subtitle {
  text-align: center;
  color: #64748B;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 0; /* El margen lo controla el header-section */
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.input-group {
  margin-bottom: 20px; /* Uniformado a 20px (ideal para formularios largos) */
}

.input-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 8px; /* Ajustado de 6px a 8px para coherencia */
}

.required {
  color: #EF4444;
}

.optional {
  color: #94A3B8;
  font-weight: 400;
  font-size: 12px;
}

.input-group input {
  width: 100%;
  padding: 12px 16px; /* Padding calzado con el resto de los formularios */
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  font-size: 15px;
  color: #1E293B;
  background-color: #F8FAFC;
  transition: all 0.2s ease;
  outline: none;
  font-family: inherit;
  box-sizing: border-box;
}

.input-group input::placeholder {
  color: #94A3B8;
}

.input-group input:focus {
  border-color: #0F766E;
  background-color: #ffffff;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.1);
}

.input-group.has-error input {
  border-color: #EF4444;
  background-color: #FEF2F2;
}

.input-group.has-error input:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.password-wrapper {
  position: relative;
}

.password-wrapper input {
  padding-right: 44px;
}

.toggle-password {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #94A3B8;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  transition: color 0.2s;
}

.toggle-password:hover {
  color: #0F766E;
}

.error-message {
  color: #EF4444;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 20px; /* Igual al margin de los inputs */
  text-align: center;
  background-color: #FEF2F2;
  padding: 12px 16px; /* Padding calzado horizontalmente */
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid #FECACA;
}

.login-button {
  width: 100%;
  padding: 14px;
  background-color: #0F766E;
  color: #ffffff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 0; /* Se elimina el margen extra */
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-family: inherit;
}

.login-button:hover:not(:disabled) {
  background-color: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(15, 118, 110, 0.25);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  background-color: #94A3B8;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.register-container {
  text-align: center;
  margin-top: 24px; /* Espacio estándar */
  padding-top: 20px; /* Espacio estándar */
  font-size: 14px;
  color: #64748B;
  border-top: 1px solid #E2E8F0;
}

.register-link {
  color: #0F766E;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: color 0.2s;
  display: inline-block;
}

.register-link:hover {
  color: #115E59;
  text-decoration: underline;
}

/* Transiciones optimizadas */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s ease-in;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(10px); /* Cae al desaparecer */
}

/* Responsive */
@media (max-width: 560px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  .login-card {
    padding: 32px 24px; /* Múltiplos de 8px en mobile también */
  }
}
</style>