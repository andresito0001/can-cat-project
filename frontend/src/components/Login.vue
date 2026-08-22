<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">Iniciar Sesión</h2>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <!-- Campo: Correo Electrónico -->
        <div class="input-group" :class="{ 'has-error': campoError.correo }">
          <label for="email">Correo electrónico</label>
          <input 
            type="email" 
            id="email" 
            v-model="email" 
            placeholder="correo@ejemplo.com"
            :disabled="isLoading"
            @input="limpiarError('correo')"
          />
        </div>

        <!-- Campo: Contraseña -->
        <div class="input-group" :class="{ 'has-error': campoError.password }">
          <label for="password">Contraseña</label>
          <div class="password-wrapper">
            <input 
              :type="mostrarPassword ? 'text' : 'password'" 
              id="password" 
              v-model="password" 
              placeholder="••••••••"
              :disabled="isLoading"
              @input="limpiarError('password')"
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
        </div>

        <!-- Mensaje de error -->
        <Transition name="slide-fade">
          <p v-if="mostrarError" class="error-message">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" x2="12" y1="8" y2="12"/>
              <line x1="12" x2="12.01" y1="16" y2="16"/>
            </svg>
            {{ authStore.errorMessage || 'Credenciales incorrectas' }}
          </p>
        </Transition>

        <!-- Botón de ingreso -->
        <button type="submit" class="login-button" :disabled="isLoading">
          <span v-if="isLoading" class="spinner"></span>
          {{ isLoading ? 'Ingresando...' : 'Ingresar' }}
        </button>

        <!-- Opciones adicionales -->
        <div class="options-container">
          <a href="#" class="forgot-link" @click.prevent="irARecuperacion">
            ¿Olvidó su contraseña?
          </a>
        </div>

        <div class="register-container">
          <span>¿No tienes cuenta?</span>
          <a href="#" class="register-link" @click.prevent="irARegistro">Registrarse</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter, useRoute } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const email = ref('')
const password = ref('')
const isLoading = ref(false)
const mostrarError = ref(false)
const mostrarPassword = ref(false)

const campoError = ref({
  correo: false,
  password: false
})

onMounted(() => {
  // Verificar si viene de sesión expirada
  if (route.query.session === 'expired') {
    mostrarError.value = true
    authStore.errorMessage = 'Su sesión ha expirado. Inicie sesión nuevamente.'
  }
})

const handleLogin = async () => {
  // Resetear errores
  mostrarError.value = false
  campoError.value = { correo: false, password: false }

  // Validaciones locales
  let hayError = false
  
  if (!email.value.trim()) {
    campoError.value.correo = true
    hayError = true
  }
  
  if (!password.value) {
    campoError.value.password = true
    hayError = true
  }
  
  if (hayError) return

  isLoading.value = true

  const result = await authStore.login(email.value, password.value)
  
  isLoading.value = false

  if (result.success) {
    // Paso 5: Redirigir según rol
    router.push(authStore.getDashboardRoute())
  } else {
    // Flujo alternativo 2: Credenciales incorrectas
    // "El sistema vacía los campos de texto y solicita al actor intentar nuevamente"
    mostrarError.value = true
    email.value = ''
    password.value = ''
  }
}

const limpiarError = (campo) => {
  campoError.value[campo] = false
  mostrarError.value = false
}

const irARecuperacion = () => {
  router.push('/recuperar-password')
}

const irARegistro = () => {
  router.push('/registro')
}
</script>

<style scoped>
/* === ESTÁNDARES DE DISEÑO CAN-CAT (VINOTINTO) === */

* { 
  box-sizing: border-box; 
  margin: 0; 
  padding: 0; 
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f4f4f5;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 20px;
}

.login-card {
  background-color: #ffffff;
  padding: 40px 35px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 380px;
  border-top: 5px solid #AC1A2E;
  transition: transform 0.3s ease;
}

.login-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 35px;
  text-align: center;
  letter-spacing: -0.5px;
}

.input-group {
  margin-bottom: 22px;
}

.input-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #555555;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.input-group input {
  width: 100%;
  padding: 12px 14px;
  border: 1.5px solid #e0e0e0;
  border-radius: 8px;
  font-size: 15px;
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

/* Error en campo individual */
.input-group.has-error input {
  border-color: #AC1A2E;
}

/* Wrapper para contraseña con botón mostrar/ocultar */
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

/* Mensaje de error */
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

.error-message svg {
  flex-shrink: 0;
}

/* Botón principal */
.login-button {
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
  margin-top: 10px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-family: inherit;
}

.login-button:hover:not(:disabled) {
  background-color: #8d1525;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(172, 26, 46, 0.3);
}

.login-button:active:not(:disabled) {
  transform: translateY(0px);
}

.login-button:disabled {
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

/* Opciones adicionales */
.options-container {
  text-align: right;
  margin-top: 15px;
}

.forgot-link {
  color: #AC1A2E;
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #8d1525;
  text-decoration: underline;
}

/* Registro */
.register-container {
  text-align: center;
  margin-top: 25px;
  font-size: 14px;
  color: #777777;
  padding-top: 20px;
  border-top: 1px solid #eeeeee;
}

.register-link {
  color: #AC1A2E;
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
  transition: color 0.2s;
}

.register-link:hover {
  color: #8d1525;
  text-decoration: underline;
}

/* Transición del error */
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
  transform: translateY(0);
}
</style>