<template>
  <div class="reset-container">
    <div class="reset-card">
      <!-- Botón volver -->
      <button class="back-button" @click="irALogin">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="m15 18-6-6 6-6"/>
        </svg>
        Volver al inicio de sesión
      </button>

      <!-- Estado: Cargando -->
      <div v-if="isValidando" class="state-container">
        <div class="spinner-large"></div>
        <p>Verificando enlace...</p>
      </div>

      <!-- Estado: Token inválido -->
      <div v-else-if="!tokenValido" class="state-container error-state">
        <div class="state-icon error">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#AC1A2E" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="15" x2="9" y1="9" y2="15"/>
            <line x1="9" x2="15" y1="9" y2="15"/>
          </svg>
        </div>
        <h2 class="state-title">Enlace inválido o expirado</h2>
        <p class="state-message">El enlace de recuperación ha expirado o no es válido. Solicite uno nuevo.</p>
        <button class="submit-button" @click="irALogin">Volver al inicio de sesión</button>
      </div>

      <!-- Estado: Formulario -->
      <template v-else-if="!exito">
        <h2 class="reset-title">Restablecer Contraseña</h2>
        <p class="reset-description">Ingrese su nueva contraseña.</p>

        <form @submit.prevent="handleReset" class="reset-form">
          <div class="input-group" :class="{ 'has-error': errores.password }">
            <label for="new-password">Nueva Contraseña</label>
            <input 
              type="password" 
              id="new-password" 
              v-model="nuevaPassword" 
              placeholder="Mínimo 6 caracteres"
              @input="errores.password = ''"
            />
            <span v-if="errores.password" class="field-error">{{ errores.password }}</span>
          </div>

          <div class="input-group" :class="{ 'has-error': errores.confirm }">
            <label for="confirm-password">Confirmar Contraseña</label>
            <input 
              type="password" 
              id="confirm-password" 
              v-model="confirmarPassword" 
              placeholder="Repita la contraseña"
              @input="errores.confirm = ''"
            />
            <span v-if="errores.confirm" class="field-error">{{ errores.confirm }}</span>
          </div>

          <p v-if="errorGeneral" class="error-message">{{ errorGeneral }}</p>

          <button type="submit" class="submit-button" :disabled="isLoading">
            <span v-if="isLoading" class="spinner"></span>
            {{ isLoading ? 'Guardando...' : 'Restablecer Contraseña' }}
          </button>
        </form>
      </template>

      <!-- Estado: Éxito -->
      <div v-else class="state-container success-state">
        <div class="state-icon success">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#10b981" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
        </div>
        <h2 class="state-title">¡Contraseña actualizada!</h2>
        <p class="state-message">Su contraseña ha sido restablecida exitosamente.</p>
        <button class="submit-button" @click="irALogin">Ir al inicio de sesión</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api/axios'

const route = useRoute()
const router = useRouter()

const token = ref('')
const isValidando = ref(true)
const tokenValido = ref(false)
const exito = ref(false)
const isLoading = ref(false)
const errorGeneral = ref('')

const nuevaPassword = ref('')
const confirmarPassword = ref('')

const errores = reactive({
  password: '',
  confirm: ''
})

onMounted(() => {
  // Extraer token de la URL
  const t = route.query.token
  
  if (!t) {
    tokenValido.value = false
  } else {
    token.value = t
    tokenValido.value = true
  }
  
  // Quitar el spinner de "validando"
  isValidando.value = false
})

const handleReset = async () => {
  errores.password = ''
  errores.confirm = ''
  errorGeneral.value = ''

  if (!nuevaPassword.value || nuevaPassword.value.length < 6) {
    errores.password = 'La contraseña debe tener al menos 6 caracteres'
    return
  }

  if (nuevaPassword.value !== confirmarPassword.value) {
    errores.confirm = 'Las contraseñas no coinciden'
    return
  }

  isLoading.value = true

  try {
    // ← CORREGIDO: el backend espera 'nuevaContrasena', no 'nuevaPassword'
    await api.post('/auth/nueva-contrasena', {
      token: token.value,
      nuevaContrasena: nuevaPassword.value
    })
    exito.value = true
  } catch (error) {
    // ← CORREGIDO: axios guarda el mensaje del backend en error.response
    errorGeneral.value = error.response?.data?.message 
      || 'Error al restablecer la contraseña. El enlace puede haber expirado.'
  } finally {
    isLoading.value = false
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

.reset-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f4f4f5;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 20px;
}

.reset-card {
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
.reset-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.reset-description {
  font-size: 14px;
  color: #666666;
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

.input-group.has-error input {
  border-color: #AC1A2E;
}

.field-error {
  display: block;
  font-size: 12px;
  color: #AC1A2E;
  margin-top: 6px;
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

.spinner-large {
  width: 36px;
  height: 36px;
  border: 3px solid #e0e0e0;
  border-top-color: #AC1A2E;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Estados */
.state-container {
  text-align: center;
  padding: 20px 0;
}

.state-container p {
  color: #666666;
  font-size: 14px;
}

.state-icon {
  margin-bottom: 16px;
}

.state-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 10px;
  letter-spacing: -0.3px;
}

.state-message {
  font-size: 14px;
  color: #666666;
  line-height: 150%;
  margin-bottom: 25px;
}
</style>