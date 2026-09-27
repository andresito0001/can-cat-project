<template>
  <AuthShell variant="app">
    <header class="form-header">
      <p class="form-eyebrow">Panel de clientes</p>
      <h1 class="form-title">Bienvenido de nuevo</h1>
      <p class="form-subtitle">
        Inicia sesión para gestionar citas y la salud de tus mascotas.
      </p>
    </header>

    <!-- Alertas: éxito de registro o error de login -->
    <Transition name="slide-fade">
      <div v-if="mensajeExito" key="ok" class="alert alert-success">
        <CheckCircle2 :size="16" />
        <span>{{ mensajeExito }}</span>
      </div>
      <div v-else-if="mostrarError || authStore.error" key="err" class="alert alert-error">
        <AlertCircle :size="16" />
        <span>
          {{ authStore.error || authStore.errorMessage || 'Credenciales incorrectas' }}
        </span>
      </div>
    </Transition>

    <form @submit.prevent="handleLogin" novalidate>
      <!-- Correo -->
      <div class="form-group" :class="{ 'has-error': campoError.correo }">
        <label class="form-label" for="email">Correo electrónico</label>
        <div class="input-wrap">
          <Mail :size="15" class="input-icon" />
          <input
            id="email"
            v-model="email"
            type="email"
            class="form-input"
            placeholder="usuario@ejemplo.com"
            :disabled="isLoading"
            autocomplete="email"
            @input="limpiarError('correo')"
            @blur="validarEmail"
          />
        </div>
        <span v-if="campoError.correo" class="form-error">
          Ingresa un correo electrónico válido.
        </span>
      </div>

      <!-- Contraseña -->
      <div class="form-group" :class="{ 'has-error': campoError.password }">
        <label class="form-label" for="password">Contraseña</label>
        <div class="input-wrap">
          <Lock :size="15" class="input-icon" />
          <input
            id="password"
            v-model="password"
            :type="mostrarPassword ? 'text' : 'password'"
            class="form-input form-input-password"
            placeholder="Ingresa tu contraseña"
            :disabled="isLoading"
            autocomplete="current-password"
            @input="limpiarError('password')"
            @keydown="actualizarCapsLock"
            @keyup="actualizarCapsLock"
            @blur="capsLockActivo = false"
          />
          <button
            type="button"
            class="toggle-password"
            :aria-label="mostrarPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'"
            tabindex="-1"
            @click="mostrarPassword = !mostrarPassword"
          >
            <Eye v-if="!mostrarPassword" :size="16" />
            <EyeOff v-else :size="16" />
          </button>
        </div>
        <span v-if="capsLockActivo" class="caps-warning">
          <AlertTriangle :size="12" /> Bloq Mayús está activado
        </span>
        <span v-if="campoError.password" class="form-error">
          Ingresa tu contraseña.
        </span>
      </div>

      <!-- Opciones: recordar + olvidé mi contraseña -->
      <div class="login-options">
        <label class="remember">
          <input v-model="recordarme" type="checkbox" />
          <span>Mantener sesión activa</span>
        </label>
        <router-link to="/auth/recuperar" class="link-forgot">
          ¿Olvidaste tu contraseña?
        </router-link>
      </div>

      <button type="submit" class="btn-submit" :disabled="isLoading">
        <Loader2 v-if="isLoading" :size="16" class="spin" />
        <ArrowRight v-else :size="16" />
        {{ isLoading ? 'Ingresando…' : 'Ingresar' }}
      </button>

      <p class="security-note">
        <ShieldCheck :size="13" />
        Conexión protegida · Tus datos viajan cifrados
      </p>
    </form>

    <div class="register-block">
      <span class="register-text">¿Aún no tienes una cuenta?</span>
      <router-link to="/auth/registro" class="link-register">
        Regístrate gratis
      </router-link>
    </div>
  </AuthShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter, useRoute } from 'vue-router'
import {
  AlertCircle, CheckCircle2, Eye, EyeOff, Loader2, ArrowRight,
  Mail, Lock, ShieldCheck, AlertTriangle,
} from 'lucide-vue-next'
import AuthShell from './AuthShell.vue'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const email = ref('')
const password = ref('')
const isLoading = ref(false)
const mostrarError = ref(false)
const mostrarPassword = ref(false)
const mensajeExito = ref('')
const recordarme = ref(false)
const capsLockActivo = ref(false)

const LLAVE_EMAIL = 'cc_email_recordado'

const campoError = ref({
  correo: false,
  password: false,
})

onMounted(() => {
  // Restaurar correo si el usuario marcó "mantener sesión activa"
  const guardado = localStorage.getItem(LLAVE_EMAIL)
  if (guardado) {
    email.value = guardado
    recordarme.value = true
  }

  if (route.query.session === 'expired') {
    mostrarError.value = true
    authStore.error = 'Su sesión ha expirado. Inicie sesión nuevamente.'
  }
  if (route.query.registered === '1') {
    mensajeExito.value = '¡Cuenta creada correctamente! Ya puedes iniciar sesión.'
  }
})

const validarEmail = () => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (email.value.trim() && !regex.test(email.value.trim())) {
    campoError.value.correo = true
  }
}

function actualizarCapsLock(e) {
  if (e.getModifierState) {
    capsLockActivo.value = e.getModifierState('CapsLock')
  }
}

const handleLogin = async () => {
  mensajeExito.value = ''
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

  // Recordar / olvidar el correo
  if (recordarme.value) {
    localStorage.setItem(LLAVE_EMAIL, email.value.trim())
  } else {
    localStorage.removeItem(LLAVE_EMAIL)
  }

  isLoading.value = true

  // MODO DESARROLLO
  if (email.value === 'mock') {
    authStore.mockLogin('Cliente')
    router.push(authStore.dashboardRoute)
    return
  }

  const result = await authStore.login({
    correoElectronico: email.value,
    contrasena: password.value,
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
</script>

<style scoped>
/* Eyebrow sobre el título, muy SaaS */
.form-eyebrow {
  margin: 0 0 4px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  color: #0F766E;
}

/* Fila de opciones: remember + forgot */
.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
  margin-top: -4px;
  flex-wrap: wrap;
}

.remember {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
  cursor: pointer;
  user-select: none;
}
.remember input {
  width: 15px;
  height: 15px;
  accent-color: #0F766E;
  cursor: pointer;
}

/* Aviso de Bloq Mayús */
.caps-warning {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  font-weight: 600;
  color: #D97706;
  margin-top: 2px;
}

/* Nota de seguridad bajo el botón */
.security-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 14px 0 0;
  font-size: 11.5px;
  font-weight: 500;
  color: #94A3B8;
}
.security-note svg { color: #14B8A6; }
</style>