<template>
  <AuthShell>
    <header class="form-header">
      <h1 class="form-title">Bienvenido de nuevo</h1>
      <p class="form-subtitle">
        Ingresa tus credenciales para acceder a tu cuenta.
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
        <span v-if="campoError.password" class="form-error">
          Ingresa tu contraseña.
        </span>
      </div>

      <div class="form-actions-row">
        <router-link to="/auth/recuperar" class="link-forgot">
          ¿Olvidaste tu contraseña?
        </router-link>
      </div>

      <button type="submit" class="btn-submit" :disabled="isLoading">
        <Loader2 v-if="isLoading" :size="16" class="spin" />
        <LogIn v-else :size="16" />
        {{ isLoading ? 'Ingresando…' : 'Ingresar' }}
      </button>
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
  AlertCircle, CheckCircle2, Eye, EyeOff, Loader2, LogIn, Mail, Lock,
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

const campoError = ref({
  correo: false,
  password: false,
})

onMounted(() => {
  if (route.query.session === 'expired') {
    mostrarError.value = true
    authStore.error = 'Su sesión ha expirado. Inicie sesión nuevamente.'
  }
  // Feedback al llegar desde el registro
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