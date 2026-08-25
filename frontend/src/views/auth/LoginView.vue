<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-title">Iniciar Sesión</div>

      <Transition name="slide-fade">
        <div v-if="mostrarError || authStore.error" class="error-message">
          <AlertCircle :size="18" />
          <span>{{ authStore.error || authStore.errorMessage || 'Credenciales incorrectas' }}</span>
        </div>
      </Transition>

      <form @submit.prevent="handleLogin">
        <div class="input-group" :class="{ 'has-error': campoError.correo }">
          <label for="email">Correo Electrónico</label>
          <input
            id="email"
            v-model="email"
            type="email"
            placeholder="usuario@ejemplo.com"
            :disabled="isLoading"
            @input="limpiarError('correo')"
            @blur="validarEmail"
          />
        </div>

        <div class="input-group" :class="{ 'has-error': campoError.password }">
          <label for="password">Contraseña</label>
          <div class="password-wrapper">
            <input
              id="password"
              v-model="password"
              :type="mostrarPassword ? 'text' : 'password'"
              placeholder="Ingrese su contraseña"
              :disabled="isLoading"
              @input="limpiarError('password')"
            />
            <button
              type="button"
              class="toggle-password"
              @click="mostrarPassword = !mostrarPassword"
              tabindex="-1"
            >
              <Eye v-if="!mostrarPassword" :size="18" />
              <EyeOff v-else :size="18" />
            </button>
          </div>
        </div>

        <div class="options-container">
          <router-link to="/auth/recuperar" class="forgot-link">
            ¿Olvidó su contraseña?
          </router-link>
        </div>

        <button type="submit" class="login-button" :disabled="isLoading">
          <span v-if="isLoading" class="spinner"></span>
          <span v-else>Ingresar</span>
        </button>
      </form>

      <div class="register-container">
        ¿No tiene una cuenta?
        <router-link to="/auth/registro" class="register-link">
          Registrarse
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter, useRoute } from 'vue-router'
import { AlertCircle, Eye, EyeOff } from 'lucide-vue-next'

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
  if (route.query.session === 'expired') {
    mostrarError.value = true
    authStore.error = 'Su sesión ha expirado. Inicie sesión nuevamente.'
  }
})

const validarEmail = () => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (email.value.trim() && !regex.test(email.value.trim())) {
    campoError.value.correo = true
  }
}






const handleLogin = async () => {
  mostrarError.value = false
  campoError.value = { correo: false, password: false }
  authStore.error = null

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

  // ─── MODO DESARROLLO: escribe "mock" como correo ───
  if (email.value === 'mock') {
    authStore.mockLogin('Cliente')
    router.push(authStore.dashboardRoute)
    return
  }

  // ─── LOGIN REAL AL BACKEND ───
  const result = await authStore.login({
    correoElectronico: email.value,
    contrasena: password.value
  })

  isLoading.value = false

  if (result.success) {
    router.push(authStore.dashboardRoute)
  } else {
    mostrarError.value = true
    password.value = ''
  }
}



const limpiarError = (campo) => {
  campoError.value[campo] = false
  if (authStore.error) authStore.error = null
  if (authStore.errorMessage) authStore.errorMessage = null
  mostrarError.value = false
}

const irARecuperacion = () => {
  router.push('/auth/recuperar')
}

const irARegistro = () => {
  router.push('/auth/registro')
}
</script>

<style scoped>
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
  background-color: #F1F5F9;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 20px;
}

.login-card {
  background-color: #ffffff;
  padding: 40px 35px;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(15, 118, 110, 0.08);
  width: 100%;
  max-width: 400px;
  border-top: 4px solid #0F766E;
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  margin-bottom: 32px;
  text-align: center;
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
  border-color: #0F766E;
  background-color: #ffffff;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.1);
}

.input-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.input-group.has-error input {
  border-color: #DC2626;
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

.error-message {
  color: #DC2626;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 15px;
  text-align: center;
  background-color: rgba(220, 38, 38, 0.05);
  padding: 10px 12px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid rgba(220, 38, 38, 0.15);
}

.error-message svg {
  flex-shrink: 0;
}

.login-button {
  width: 100%;
  padding: 13px;
  background-color: #0F766E;
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
  background-color: #0D5F58;
  transform: translateY(-2px);
  box-shadow: 0 6px 15px rgba(15, 118, 110, 0.3);
}

.login-button:active:not(:disabled) {
  transform: translateY(0px);
}

.login-button:disabled {
  background-color: #9CA3AF;
  cursor: not-allowed;
  transform: translateY(0px);
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

.options-container {
  text-align: right;
  margin-top: 15px;
  margin-bottom: 10px;
}

.forgot-link {
  color: #0F766E;
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #0D5F58;
  text-decoration: underline;
}

.register-container {
  text-align: center;
  margin-top: 25px;
  font-size: 14px;
  color: #777777;
  padding-top: 20px;
  border-top: 1px solid #eeeeee;
}

.register-link {
  color: #0F766E;
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
  transition: color 0.2s;
}

.register-link:hover {
  color: #0D5F58;
  text-decoration: underline;
}

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