<template>
  <div class="login-container">
    <div class="login-card">
      <!-- Agrupar el título y subtítulo ayuda a controlar el espacio superior -->
      <div class="header-section">
        <div class="login-title">Recuperar Contraseña</div>
        <p class="subtitle">Ingresa tu correo y te enviaremos un enlace de recuperación.</p>
      </div>

      <!-- mode="out-in" asegura que el mensaje salga antes de que entre el nuevo, evitando saltos bruscos -->
      <Transition name="slide-fade" mode="out-in">
        <div v-if="mensajeExito" key="success" class="message success-message">
          <CheckCircle :size="18" />
          <span>{{ mensajeExito }}</span>
        </div>
        <div v-else-if="mostrarError" key="error" class="message error-message">
          <AlertCircle :size="18" />
          <span>{{ authStore.error }}</span>
        </div>
      </Transition>

      <form v-if="!mensajeExito" @submit.prevent="handleRecuperar" class="form-section">
        <div class="input-group" :class="{ 'has-error': campoError }">
          <label for="rec-email">Correo Electrónico</label>
          <input
            id="rec-email"
            v-model="email"
            type="email"
            placeholder="usuario@ejemplo.com"
            :disabled="authStore.isLoading"
            @input="limpiarError"
          />
        </div>

        <button type="submit" class="login-button" :disabled="authStore.isLoading || !email.trim()">
          <span v-if="authStore.isLoading" class="spinner"></span>
          <span v-else>Enviar Enlace</span>
        </button>
      </form>

      <div class="register-container">
        <router-link to="/auth/login" class="register-link">← Volver al inicio de sesión</router-link>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { AlertCircle, CheckCircle } from 'lucide-vue-next'

const authStore = useAuthStore()

const email = ref('')
const mostrarError = ref(false)
const campoError = ref(false)
const mensajeExito = ref('')

function limpiarError() {
  campoError.value = false
  mostrarError.value = false
  authStore.error = null
}

async function handleRecuperar() {
  limpiarError()

  if (!email.value.trim()) {
    campoError.value = true
    return
  }

  const result = await authStore.solicitarRecuperacion(email.value)

  if (result.success) {
    mensajeExito.value = result.mensaje
    email.value = ''
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
  /* Padding simétrico (arriba/abajo vs izquierda/derecha) para mejor equilibrio visual */
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
  line-height: 1.5; /* Mejora la legibilidad del texto */
  margin-bottom: 0;
}

/* Clase base para los mensajes para evitar repetir código */
.message {
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 24px; /* Espacio consistente antes del formulario */
  padding: 12px 16px; /* Padding horizontal aumentado ligeramente */
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
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

.form-section {
  /* El form-section se encarga de su propio margen si no hay mensajes */
  margin-top: 0;
}

.input-group {
  margin-bottom: 24px; /* Espacio uniforme entre el input y el botón */
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
  padding: 12px 16px; /* Padding horizontal calzado con los mensajes */
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
  margin-top: 0; /* Se elimina el margin-top extra para confiar en el margin-bottom del input */
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
  transform: translateY(0); /* Efecto de clic */
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
  display: inline-block; /* Mejora el área clickeable */
}

.register-link:hover {
  color: #115E59;
  text-decoration: underline;
}

/* Transiciones mejoradas con mode="out-in" */
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
  transform: translateY(10px); /* Hace que el mensaje caiga al desaparecer en lugar de subir */
}
</style>