<template>
  <div class="registro-container">
    <div class="registro-card">
      <!-- Botón volver -->
      <button class="back-button" @click="irALogin">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m15 18-6-6 6-6"/>
        </svg>
        Volver al inicio de sesión
      </button>

      <!-- ESTADO: Formulario -->
      <template v-if="!registroExitoso">
        <h2 class="registro-title">Crear Cuenta</h2>
        <p class="registro-description">
          Complete sus datos para registrarse en el sistema de Can-Cat Veterinaria.
        </p>

        <form @submit.prevent="handleRegistro" class="registro-form" novalidate>
          <!-- Sección: Datos Personales -->
          <div class="form-section">
            <h3 class="section-title">Datos Personales</h3>
            
            <div class="input-group" :class="{ 'has-error': errores.nombreCompleto }">
              <label for="nombre">Nombre completo *</label>
              <input 
                type="text" 
                id="nombre" 
                v-model="form.nombreCompleto" 
                placeholder="Juan Pérez García"
                :disabled="isLoading"
                @input="errores.nombreCompleto = ''"
              />
              <span v-if="errores.nombreCompleto" class="field-error">{{ errores.nombreCompleto }}</span>
            </div>

            <div class="input-row">
              <div class="input-group" :class="{ 'has-error': errores.documentoIdentidad }">
                <label for="documento">Documento de identidad *</label>
                <input 
                  type="text" 
                  id="documento" 
                  v-model="form.documentoIdentidad" 
                  placeholder="12345678"
                  :disabled="isLoading"
                  @input="errores.documentoIdentidad = ''"
                />
                <span v-if="errores.documentoIdentidad" class="field-error">{{ errores.documentoIdentidad }}</span>
              </div>

              <div class="input-group">
                <label for="fechaNac">Fecha de nacimiento</label>
                <input 
                  type="date" 
                  id="fechaNac" 
                  v-model="form.fechaNacimiento"
                  :disabled="isLoading"
                />
              </div>
            </div>
          </div>

          <!-- Sección: Contacto -->
          <div class="form-section">
            <h3 class="section-title">Datos de Contacto</h3>
            
            <div class="input-group" :class="{ 'has-error': errores.correoElectronico }">
              <label for="correo">Correo electrónico *</label>
              <input 
                type="email" 
                id="correo" 
                v-model="form.correoElectronico" 
                placeholder="correo@ejemplo.com"
                :disabled="isLoading"
                @input="errores.correoElectronico = ''"
              />
              <span v-if="errores.correoElectronico" class="field-error">{{ errores.correoElectronico }}</span>
            </div>

            <div class="input-row">
              <div class="input-group" :class="{ 'has-error': errores.telefonoPrincipal }">
                <label for="telefono">Teléfono principal *</label>
                <input 
                  type="tel" 
                  id="telefono" 
                  v-model="form.telefonoPrincipal" 
                  placeholder="+51 999 888 777"
                  :disabled="isLoading"
                  @input="errores.telefonoPrincipal = ''"
                />
                <span v-if="errores.telefonoPrincipal" class="field-error">{{ errores.telefonoPrincipal }}</span>
              </div>

              <div class="input-group">
                <label for="telefono2">Teléfono secundario</label>
                <input 
                  type="tel" 
                  id="telefono2" 
                  v-model="form.telefonoSecundario" 
                  placeholder="Opcional"
                  :disabled="isLoading"
                />
              </div>
            </div>

            <div class="input-row">
              <div class="input-group">
                <label for="direccion">Dirección</label>
                <input 
                  type="text" 
                  id="direccion" 
                  v-model="form.direccion" 
                  placeholder="Av. Principal 123"
                  :disabled="isLoading"
                />
              </div>

              <div class="input-group">
                <label for="ciudad">Ciudad</label>
                <input 
                  type="text" 
                  id="ciudad" 
                  v-model="form.ciudad" 
                  placeholder="Lima"
                  :disabled="isLoading"
                />
              </div>
            </div>
          </div>

          <!-- Sección: Seguridad -->
          <div class="form-section">
            <h3 class="section-title">Seguridad</h3>
            
            <div class="input-group" :class="{ 'has-error': errores.contrasena }">
              <label for="password">Contraseña *</label>
              <div class="password-wrapper">
                <input 
                  :type="mostrarPassword ? 'text' : 'password'" 
                  id="password" 
                  v-model="form.contrasena" 
                  placeholder="Mínimo 6 caracteres"
                  :disabled="isLoading"
                  @input="errores.contrasena = ''"
                />
                <button 
                  type="button" 
                  class="toggle-password" 
                  @click="mostrarPassword = !mostrarPassword"
                  tabindex="-1"
                >
                  <svg v-if="!mostrarPassword" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24"/>
                    <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68"/>
                    <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61"/>
                    <line x1="2" x2="22" y1="2" y2="22"/>
                  </svg>
                </button>
              </div>
              <span v-if="errores.contrasena" class="field-error">{{ errores.contrasena }}</span>
            </div>

            <div class="input-group" :class="{ 'has-error': errores.confirmarPassword }">
              <label for="confirmPassword">Confirmar contraseña *</label>
              <input 
                type="password" 
                id="confirmPassword" 
                v-model="confirmarPassword" 
                placeholder="Repita la contraseña"
                :disabled="isLoading"
                @input="errores.confirmarPassword = ''"
              />
              <span v-if="errores.confirmarPassword" class="field-error">{{ errores.confirmarPassword }}</span>
            </div>
          </div>

          <!-- Error general -->
          <Transition name="slide-fade">
            <p v-if="errorGeneral" class="error-message">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" x2="12" y1="8" y2="12"/>
                <line x1="12" x2="12.01" y1="16" y2="16"/>
              </svg>
              {{ errorGeneral }}
            </p>
          </Transition>

          <!-- Botón de registro -->
          <button type="submit" class="submit-button" :disabled="isLoading">
            <span v-if="isLoading" class="spinner"></span>
            {{ isLoading ? 'Registrando...' : 'Crear Cuenta' }}
          </button>

          <p class="login-link">
            ¿Ya tiene cuenta? 
            <a href="#" @click.prevent="irALogin">Inicie sesión</a>
          </p>
        </form>
      </template>

      <!-- ESTADO: Registro exitoso -->
      <template v-else>
        <div class="success-container">
          <div class="success-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <h2 class="success-title">¡Registro Exitoso!</h2>
          <p class="success-message">
            Su cuenta ha sido creada correctamente. 
            Ya puede iniciar sesión con sus credenciales.
          </p>
          <button class="submit-button" @click="irALogin">
            Ir a Iniciar Sesión
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const isLoading = ref(false)
const mostrarPassword = ref(false)
const confirmarPassword = ref('')
const errorGeneral = ref('')
const registroExitoso = ref(false)

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

const errores = reactive({
  nombreCompleto: '',
  documentoIdentidad: '',
  correoElectronico: '',
  contrasena: '',
  telefonoPrincipal: '',
  confirmarPassword: ''
})

const handleRegistro = async () => {
  // Limpiar errores
  Object.keys(errores).forEach(key => errores[key] = '')
  errorGeneral.value = ''

  // Validar
  if (!validarFormulario()) return

  isLoading.value = true

  const result = await authStore.registrarCliente(form)
  
  isLoading.value = false

  if (result.success) {
    registroExitoso.value = true
  } else {
    // Determinar tipo de error
    const msg = result.message.toLowerCase()
    
    if (msg.includes('correo')) {
      errores.correoElectronico = result.message
    } else if (msg.includes('documento')) {
      errores.documentoIdentidad = result.message
    } else {
      errorGeneral.value = result.message
    }
  }
}

const validarFormulario = () => {
  let valido = true

  // Nombre completo
  if (!form.nombreCompleto.trim()) {
    errores.nombreCompleto = 'El nombre completo es obligatorio'
    valido = false
  } else if (form.nombreCompleto.trim().length < 3) {
    errores.nombreCompleto = 'El nombre debe tener al menos 3 caracteres'
    valido = false
  }

  // Documento
  if (!form.documentoIdentidad.trim()) {
    errores.documentoIdentidad = 'El documento de identidad es obligatorio'
    valido = false
  } else if (form.documentoIdentidad.trim().length < 6) {
    errores.documentoIdentidad = 'El documento debe tener al menos 6 caracteres'
    valido = false
  }

  // Correo
  if (!form.correoElectronico.trim()) {
    errores.correoElectronico = 'El correo electrónico es obligatorio'
    valido = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.correoElectronico)) {
    errores.correoElectronico = 'El formato del correo no es válido'
    valido = false
  }

  // Teléfono
  if (!form.telefonoPrincipal.trim()) {
    errores.telefonoPrincipal = 'El teléfono principal es obligatorio'
    valido = false
  }

  // Contraseña
  if (!form.contrasena) {
    errores.contrasena = 'La contraseña es obligatoria'
    valido = false
  } else if (form.contrasena.length < 6) {
    errores.contrasena = 'La contraseña debe tener al menos 6 caracteres'
    valido = false
  }

  // Confirmar contraseña
  if (!confirmarPassword.value) {
    errores.confirmarPassword = 'Debe confirmar la contraseña'
    valido = false
  } else if (form.contrasena !== confirmarPassword.value) {
    errores.confirmarPassword = 'Las contraseñas no coinciden'
    valido = false
  }

  return valido
}

const irALogin = () => {
  router.push('/')
}
</script>

<style scoped>
/* === ESTÁNDARES DE DISEÑO CAN-CAT (VINOTINTO) === */

* { 
  box-sizing: border-box; 
  margin: 0; 
  padding: 0; 
}

.registro-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  min-height: 100vh;
  background-color: #f4f4f5;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 30px 20px;
}

.registro-card {
  background-color: #ffffff;
  padding: 35px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 520px;
  border-top: 5px solid #AC1A2E;
}

/* Botón volver */
.back-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: #777777;
  font-size: 13px;
  font-family: inherit;
  cursor: pointer;
  padding: 0;
  margin-bottom: 25px;
  transition: color 0.2s;
}

.back-button:hover {
  color: #AC1A2E;
}

/* Título */
.registro-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.registro-description {
  font-size: 14px;
  color: #666666;
  line-height: 150%;
  margin-bottom: 28px;
}

/* Secciones del formulario */
.form-section {
  margin-bottom: 25px;
  padding-bottom: 25px;
  border-bottom: 1px solid #eeeeee;
}

.form-section:last-of-type {
  border-bottom: none;
  margin-bottom: 20px;
  padding-bottom: 0;
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: #AC1A2E;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  margin-bottom: 18px;
}

/* Inputs */
.input-group {
  margin-bottom: 18px;
}

.input-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #555555;
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.input-group input {
  width: 100%;
  padding: 11px 14px;
  border: 1.5px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  color: #333333;
  background-color: #fafafa;
  transition: all 0.2s ease;
  outline: none;
  font-family: inherit;
}

.input-group input::placeholder {
  color: #aaaaaa;
}

.input-group input:focus {
  border-color: #AC1A2E;
  background-color: #ffffff;
  box-shadow: 0 0 0 3px rgba(172, 26, 46, 0.1);
}

.input-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.input-group.has-error input {
  border-color: #AC1A2E;
}

/* Fila de inputs */
.input-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

/* Error de campo */
.field-error {
  display: block;
  font-size: 12px;
  color: #AC1A2E;
  margin-top: 4px;
}

/* Password wrapper */
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
  color: #999999;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  transition: color 0.2s;
}

.toggle-password:hover {
  color: #666666;
}

/* Error general */
.error-message {
  color: #AC1A2E;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 15px;
  text-align: center;
  background-color: rgba(172, 26, 46, 0.05);
  padding: 10px 12px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid rgba(172, 26, 46, 0.15);
}

/* Botón */
.submit-button {
  width: 100%;
  padding: 13px;
  background-color: #AC1A2E;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 5px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-family: inherit;
}

.submit-button:hover:not(:disabled) {
  background-color: #8d1525;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(172, 26, 46, 0.3);
}

.submit-button:active:not(:disabled) {
  transform: translateY(0px);
}

.submit-button:disabled {
  background-color: #d4a0a8;
  cursor: not-allowed;
  transform: translateY(0px);
  box-shadow: none;
}

/* Spinner */
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

/* Link a login */
.login-link {
  text-align: center;
  font-size: 14px;
  color: #777777;
  margin-top: 20px;
}

.login-link a {
  color: #AC1A2E;
  text-decoration: none;
  font-weight: 600;
}

.login-link a:hover {
  text-decoration: underline;
}

/* Estado éxito */
.success-container {
  text-align: center;
  padding: 30px 0;
}

.success-icon {
  margin-bottom: 20px;
}

.success-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.success-message {
  font-size: 14px;
  color: #666666;
  line-height: 150%;
  margin-bottom: 25px;
}

/* Transición error */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s ease-in;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}

.slide-fade-leave-to {
  opacity: 0;
}

/* Responsive */
@media (max-width: 500px) {
  .input-row {
    grid-template-columns: 1fr;
    gap: 0;
  }
  
  .registro-card {
    padding: 25px 20px;
  }
}
</style>