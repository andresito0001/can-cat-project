<template>
  <AuthShell>
    <header class="form-header">
      <div class="form-icon"><KeyRound :size="18" /></div>
      <h1 class="form-title">Recuperar contraseña</h1>
      <p class="form-subtitle">
        Ingresa tu correo y te enviaremos un enlace para restablecer el acceso.
      </p>
    </header>

    <Transition name="slide-fade" mode="out-in">
      <div v-if="mensajeExito" key="success" class="alert alert-success">
        <CheckCircle2 :size="16" />
        <span>{{ mensajeExito }}</span>
      </div>
      <div v-else-if="mostrarError" key="error" class="alert alert-error">
        <AlertCircle :size="16" />
        <span>{{ authStore.error || 'No se pudo procesar la solicitud.' }}</span>
      </div>
    </Transition>

    <form v-if="!mensajeExito" @submit.prevent="handleRecuperar" novalidate>
      <div class="form-group" :class="{ 'has-error': campoError }">
        <label class="form-label" for="rec-email">Correo electrónico</label>
        <div class="input-wrap">
          <Mail :size="15" class="input-icon" />
          <input
            id="rec-email"
            v-model="email"
            type="email"
            class="form-input"
            placeholder="usuario@ejemplo.com"
            :disabled="authStore.isLoading"
            autocomplete="email"
            @input="limpiarError"
          />
        </div>
        <span v-if="campoError" class="form-error">
          Ingresa un correo electrónico válido.
        </span>
      </div>

      <button
        type="submit"
        class="btn-submit"
        :disabled="authStore.isLoading || !email.trim()"
      >
        <Loader2 v-if="authStore.isLoading" :size="16" class="spin" />
        <Send v-else :size="16" />
        {{ authStore.isLoading ? 'Enviando…' : 'Enviar enlace' }}
      </button>
    </form>

    <div v-else class="success-block">
      <div class="success-icon"><MailCheck :size="28" /></div>
      <h2 class="success-title">Revisa tu bandeja de entrada</h2>
      <p class="success-text">
        Si el correo está registrado, recibirás un enlace de recuperación en los próximos minutos.
        Si no lo ves, revisa la carpeta de spam.
      </p>
      <button type="button" class="btn-secondary" @click="reintentar">
        <RotateCcw :size="14" />
        Usar otro correo
      </button>
    </div>

    <div class="register-block">
      <router-link to="/auth/login" class="link-register">
        <ArrowLeft :size="13" />
        Volver al inicio de sesión
      </router-link>
    </div>
  </AuthShell>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import {
  AlertCircle, CheckCircle2, Mail, Loader2, Send,
  KeyRound, MailCheck, ArrowLeft, RotateCcw,
} from 'lucide-vue-next'

import AuthShell from './AuthShell.vue'

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

function reintentar() {
  mensajeExito.value = ''
  mostrarError.value = false
  campoError.value = false
  authStore.error = null
  email.value = ''
}
</script>