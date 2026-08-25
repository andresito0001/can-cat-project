<template>
  <div class="login-container">
    <div class="login-card">
      <!-- Agrupación para controlar el espacio superior -->
      <div class="header-section">
        <div class="login-title">Nueva Contraseña</div>
        <p class="subtitle">Crea una contraseña segura para tu cuenta.</p>
      </div>

      <!-- Se añade mode="out-in" para evitar saltos bruscos en el layout -->
      <Transition name="slide-fade" mode="out-in">
        <div v-if="mostrarError" key="error" class="message error-message">
          <AlertCircle :size="18" />
          <span>{{ authStore.error }}</span>
        </div>
      </Transition>

      <!-- Se elimina el estilo inline y se usa la clase base .message -->
      <div v-if="exitoso" class="message success-message">
        <CheckCircle :size="18" />
        <span>Contraseña actualizada correctamente. Redirigiendo...</span>
      </div>

      <form v-else @submit.prevent="handleReset">
        <div class="input-group" :class="{ 'has-error': errors.password }">
          <label>Nueva Contraseña</label>
          <div class="password-wrapper">
            <input
              v-model="form.nuevaContrasena"
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

        <div class="input-group" :class="{ 'has-error': errors.confirmar }">
          <label>Confirmar Contraseña</label>
          <input
            v-model="form.confirmarContrasena"
            :type="mostrarPassword ? 'text' : 'password'"
            placeholder="Repite la contraseña"
            :disabled="authStore.isLoading"
          />
        </div>

        <button type="submit" class="login-button" :disabled="authStore.isLoading">
          <span v-if="authStore.isLoading" class="spinner"></span>
          <span v-else>Guardar Contraseña</span>
        </button>
      </form>

      <div class="register-container">
        <router-link to="/auth/login" class="register-link">← Ir al inicio de sesión</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { AlertCircle, Eye, EyeOff, CheckCircle } from 'lucide-vue-next'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const mostrarPassword = ref(false)
const mostrarError = ref(false)
const exitoso = ref(false)
const errors = reactive({ password: false, confirmar: false })

const form = reactive({
  nuevaContrasena: '',
  confirmarContrasena: ''
})

const tokenReset = ref(route.query.token || '')

onMounted(() => {
  if (!tokenReset.value) {
    authStore.error = 'El enlace de recuperación es inválido o ha expirado.'
    mostrarError.value = true
  }
})

async function handleReset() {
  mostrarError.value = false
  errors.password = false
  errors.confirmar = false
  authStore.error = null

  if (!form.nuevaContrasena || form.nuevaContrasena.length < 6) {
    authStore.error = 'La contraseña debe tener al menos 6 caracteres.'
    errors.password = true
    mostrarError.value = true
    return
  }

  if (form.nuevaContrasena !== form.confirmarContrasena) {
    authStore.error = 'Las contraseñas no coinciden.'
    errors.confirmar = true
    mostrarError.value = true
    return
  }

  if (!tokenReset.value) {
    authStore.error = 'Token de recuperación inválido.'
    mostrarError.value = true
    return
  }

  const result = await authStore.restablecerContrasena(tokenReset.value, form.nuevaContrasena)

  if (result.success) {
    exitoso.value = true
    setTimeout(() => router.push('/auth/login'), 2500)
  } else {
    mostrarError.value = true
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
  /* Padding simétrico de 32px a los lados para mejor equilibrio */
  padding: 40px 32px;
  border-radius: 16px;
  /* Sombra ligeramente más definida pero sutil */
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 20px 40px -4px rgba(15, 118, 110, 0.08);
  width: 100%;
  max-width: 400px;
  border-top: 4px solid #0F766E;
}

.header-section {
  margin-bottom: 28px;
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
  margin-bottom: 0; /* El margen lo controla el header-section ahora */
}

/* Clase base para mensajes de estado (evita repetir código y estandariza el espacio) */
.message {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 24px; /* Espacio exacto y consistente antes del formulario */
  padding: 12px 16px;  /* Padding horizontal calzado con los inputs */
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-align: center;
}

.error-message {
  color: #EF4444;
  background-color: #FEF2F2;
  border: 1px solid #FECACA;
}

.success-message {
  color: #059669;
  background-color: #ECFDF5;
  border: 1px solid #A7F3D0;
}

.input-group {
  margin-bottom: 24px; /* Espacio uniforme entre inputs y botón */
}

.input-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 8px;
}

.input-group input {
  width: 100%;
  padding: 12px 16px; /* Padding horizontal actualizado */
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

.password-wrapper {
  position: relative;
}

/* Este padding-right específico sobrescribe el general para dejar espacio al ojo */
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
  margin-top: 0; /* Se elimina el margen extra para confiar en el margin-bottom del input */
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
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #E2E8F0;
}

.register-link {
  color: #0F766E;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  transition: color 0.2s;
  display: inline-block;
}

.register-link:hover {
  color: #115E59;
  text-decoration: underline;
}

/* Transiciones mejoradas */
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
  transform: translateY(10px); /* Cae suavemente al desaparecer en lugar de subir */
}
</style>