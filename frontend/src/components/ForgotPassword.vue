<template>
  <div class="forgot-container">
    <div class="forgot-card">
      <!-- Botón volver -->
      <button class="back-button" @click="irALogin">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m15 18-6-6 6-6"/>
        </svg>
        Volver al inicio de sesión
      </button>

      <!-- ESTADO: Formulario -->
      <template v-if="!resultado">
        <h2 class="forgot-title">Recuperar Contraseña</h2>
        <p class="forgot-description">
          Ingrese su correo electrónico y le enviaremos un enlace para restablecer su contraseña.
        </p>

        <form @submit.prevent="handleRecuperar" class="forgot-form">
          <div class="input-group" :class="{ 'has-error': campoError }">
            <label for="recovery-email">Correo electrónico</label>
            <input 
              type="email" 
              id="recovery-email" 
              v-model="email" 
              placeholder="correo@ejemplo.com"
              :disabled="isLoading"
              @input="campoError = false"
            />
          </div>

          <button type="submit" class="submit-button" :disabled="isLoading">
            <span v-if="isLoading" class="spinner"></span>
            {{ isLoading ? 'Enviando...' : 'Recuperar' }}
          </button>
        </form>
      </template>

      <!-- ESTADO: Éxito - Cliente -->
      <template v-else-if="resultado.tipo === 'ENLACE_ENVIADO'">
        <div class="result-container success">
          <div class="result-icon success-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <h2 class="result-title">¡Correo enviado!</h2>
          <p class="result-message">
            Se ha enviado un enlace de recuperación a <strong>{{ resultado.correo }}</strong>
          </p>
          <p class="result-hint">El enlace expirará en 24 horas. Revise su bandeja de entrada.</p>
          <button class="submit-button" @click="irALogin">Volver al inicio de sesión</button>
        </div>
      </template>

      <!-- ESTADO: Bloqueado - Personal -->
      <template v-else-if="resultado.tipo === 'BLOQUEADO_POR_POLITICA'">
        <div class="result-container warning">
          <div class="result-icon warning-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/>
              <path d="M12 9v4"/>
              <path d="M12 17h.01"/>
            </svg>
          </div>
          <h2 class="result-title">Acceso restringido</h2>
          <p class="result-message">
            Por políticas de la clínica, el personal interno debe contactar al <strong>Administrador</strong> para restablecer sus credenciales.
          </p>
          <div class="info-box warning-box">
            <p>Comuníquese con el administrador del sistema o diríjase a la oficina de TI.</p>
          </div>
          <button class="submit-button" @click="irALogin">Volver al inicio de sesión</button>
        </div>
      </template>

      <!-- ESTADO: Correo no registrado -->
      <template v-else-if="resultado.tipo === 'CORREO_NO_REGISTRADO'">
        <div class="result-container info">
          <div class="result-icon info-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#3b82f6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <path d="M12 16v-4"/>
              <path d="M12 8h.01"/>
            </svg>
          </div>
          <h2 class="result-title">Solicitud recibida</h2>
          <p class="result-message">
            Si el correo está registrado en nuestro sistema, recibirá instrucciones para recuperar su contraseña.
          </p>
          <button class="submit-button" @click="irALogin">Volver al inicio de sesión</button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const email = ref('')
const isLoading = ref(false)
const campoError = ref(false)
const resultado = ref(null)

const handleRecuperar = async () => {
  campoError.value = false

  if (!email.value.trim()) {
    campoError.value = true
    return
  }

  isLoading.value = true

  const response = await authStore.solicitarRecuperacionPassword(email.value)
  
  isLoading.value = false

  if (response.success) {
    resultado.value = response.data
  } else {
    campoError.value = true
  }
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

.forgot-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f4f4f5;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 20px;
}

.forgot-card {
  background-color: #ffffff;
  padding: 35px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 420px;
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
.forgot-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.forgot-description {
  font-size: 14px;
  color: #666666;
  line-height: 150%;
  margin-bottom: 28px;
}

/* Input */
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

.input-group.has-error input {
  border-color: #AC1A2E;
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
  margin-top: 10px;
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

/* Resultados */
.result-container {
  text-align: center;
  padding: 10px 0;
}

.result-icon {
  margin-bottom: 20px;
}

.result-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  letter-spacing: -0.3px;
}

.result-message {
  font-size: 14px;
  color: #666666;
  line-height: 150%;
  margin-bottom: 8px;
}

.result-message strong {
  color: #333333;
}

.result-hint {
  font-size: 13px;
  color: #999999;
  margin-bottom: 25px;
}

/* Caja informativa */
.info-box {
  background-color: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 25px;
}

.info-box.warning-box {
  background-color: #fffbeb;
  border-color: #fde68a;
}

.info-box p {
  font-size: 13px;
  color: #92400e;
  line-height: 145%;
}
</style>