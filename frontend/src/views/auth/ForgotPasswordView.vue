<template>
  <AuthShell>
    <header class="form-header">
      <div class="form-icon"><KeyRound :size="18" /></div>
      <h1 class="form-title">Recuperar contraseña</h1>
      <p class="form-subtitle">
        Ingresa tu correo y te enviaremos un enlace para restablecer el acceso.
      </p>
    </header>

    <Transition name="slide-down">
      <AppAlert v-if="mensajeExito" variant="success" class="alert-spacing">
        {{ mensajeExito }}
      </AppAlert>
      <AppAlert v-else-if="mostrarError && authStore.error" variant="error" class="alert-spacing">
        {{ authStore.error }}
      </AppAlert>
    </Transition>

    <form v-if="!mensajeExito" @submit.prevent="handleRecuperar" novalidate>
      <AppFormField
        label="Correo electrónico"
        :error="campoError ? 'Ingresa un correo electrónico válido.' : ''"
        required
      >
        <template #default="{ id, invalid }">
          <AppInput
            :id="id"
            v-model="email"
            type="email"
            placeholder="usuario@ejemplo.com"
            :disabled="authStore.isLoading"
            :error="invalid"
            autocomplete="email"
            @input="limpiarError"
          />
        </template>
      </AppFormField>

      <AppButton
        type="submit"
        variant="primary"
        size="lg"
        block
        :loading="authStore.isLoading"
        :disabled="!email.trim()"
      >
        <template #icon-left><Send :size="16" /></template>
        {{ authStore.isLoading ? 'Enviando…' : 'Enviar enlace' }}
      </AppButton>
    </form>

    <div v-else class="success-block">
      <div class="success-icon"><MailCheck :size="28" /></div>
      <h2 class="success-title">Revisa tu bandeja de entrada</h2>
      <p class="success-text">
        Si el correo está registrado, recibirás un enlace de recuperación en los
        próximos minutos. Si no lo ves, revisa la carpeta de spam.
      </p>
      <AppButton variant="secondary" block @click="reintentar">
        <template #icon-left><RotateCcw :size="14" /></template>
        Usar otro correo
      </AppButton>
    </div>

    <div class="register-block">
      <RouterLink to="/auth/login" class="link-register">
        <ArrowLeft :size="13" />
        Volver al inicio de sesión
      </RouterLink>
    </div>
  </AuthShell>
</template>

<script setup>
import { ref } from 'vue'
import { KeyRound, Send, MailCheck, ArrowLeft, RotateCcw } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth.store'

import AuthShell from './AuthShell.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppFormField from '@/components/ui/AppFormField.vue'
import AppInput from '@/components/ui/AppInput.vue'

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

<style scoped>
.form-header {
  margin-bottom: var(--space-6);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-2);
}
.form-icon {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-xl);
  background: var(--brand-50);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
}
.form-title {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  letter-spacing: var(--tracking-tight);
  color: var(--text-primary);
  margin: 0;
}
.form-subtitle {
  margin: 0;
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: var(--leading-normal);
}
.alert-spacing { margin-bottom: var(--space-5); }

.success-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
  padding: var(--space-2) 0 var(--space-1);
}
.success-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-full);
  background: var(--success-50);
  border: 2px solid var(--success-200);
  color: var(--success-600);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
  animation: successPop 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.08); }
  100% { transform: scale(1); opacity: 1; }
}
.success-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
  margin: 0;
}
.success-text {
  font-size: var(--text-md);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  max-width: 340px;
  margin: 0 0 var(--space-3);
}

.register-block {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--space-2);
  margin-top: var(--space-6);
  padding-top: var(--space-5);
  border-top: 1px solid var(--border-subtle);
  font-size: var(--text-base);
}
.link-register {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  text-decoration: none;
}
.link-register:hover { color: var(--brand-800); text-decoration: underline; }
</style>