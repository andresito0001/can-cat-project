<template>
  <AuthShell>
    <header class="form-header">
      <h1 class="form-title">Bienvenido de nuevo</h1>
      <p class="form-subtitle">Ingresa tus credenciales para acceder a tu cuenta.</p>
    </header>

    <Transition name="slide-down">
      <AppAlert v-if="mensajeExito" variant="success" class="alert-spacing">
        {{ mensajeExito }}
      </AppAlert>
      <AppAlert v-else-if="authStore.error" variant="error" class="alert-spacing">
        {{ authStore.error }}
      </AppAlert>
    </Transition>

    <form @submit.prevent="handleLogin" novalidate>
      <AppFormField
        label="Correo electrónico"
        :error="campoError.correo ? 'Ingresa un correo electrónico válido.' : ''"
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
            @input="limpiarError('correo')"
          />
        </template>
      </AppFormField>

      <AppFormField
        label="Contraseña"
        :error="campoError.password ? 'Ingresa tu contraseña.' : ''"
        required
      >
        <template #default="{ id, invalid }">
          <AppPasswordField
            :id="id"
            v-model="password"
            :disabled="authStore.isLoading"
            :error="invalid"
            autocomplete="current-password"
            @update:model-value="limpiarError('password')"
          />
        </template>
      </AppFormField>

      <div class="form-actions-row">
        <RouterLink to="/auth/recuperar" class="link-forgot">
          ¿Olvidaste tu contraseña?
        </RouterLink>
      </div>

      <AppButton
        type="submit"
        variant="primary"
        size="lg"
        block
        :loading="authStore.isLoading"
      >
        <template #icon-left><LogIn :size="16" /></template>
        {{ authStore.isLoading ? 'Ingresando…' : 'Ingresar' }}
      </AppButton>
    </form>

    <div class="register-block">
      <span class="register-text">¿Aún no tienes una cuenta?</span>
      <RouterLink to="/auth/registro" class="link-register">Regístrate gratis</RouterLink>
    </div>
  </AuthShell>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LogIn } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth.store'

import AuthShell from './AuthShell.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppFormField from '@/components/ui/AppFormField.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppPasswordField from '@/components/ui/AppPasswordField.vue'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const email = ref('')
const password = ref('')
const mensajeExito = ref('')
const campoError = ref({ correo: false, password: false })

onMounted(() => {
  if (route.query.session === 'expired') {
    authStore.error = 'Su sesión ha expirado. Inicie sesión nuevamente.'
  }
  if (route.query.registered === '1') {
    mensajeExito.value = '¡Cuenta creada correctamente! Ya puedes iniciar sesión.'
  }
})

function limpiarError(campo) {
  campoError.value[campo] = false
  authStore.error = null
  mensajeExito.value = ''
}

async function handleLogin() {
  mensajeExito.value = ''
  authStore.error = null
  campoError.value = { correo: false, password: false }

  if (!email.value.trim()) campoError.value.correo = true
  if (!password.value) campoError.value.password = true
  if (campoError.value.correo || campoError.value.password) return

  const { success } = await authStore.login({
    correoElectronico: email.value,
    contrasena: password.value,
  })

  if (success) router.push(authStore.dashboardRoute)
  else password.value = ''
}
</script>

<style scoped>
.form-header { margin-bottom: var(--space-6); }
.form-title {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  letter-spacing: var(--tracking-tight);
  color: var(--text-primary);
  margin: 0;
}
.form-subtitle {
  margin: var(--space-2) 0 0;
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: var(--leading-normal);
}
.alert-spacing { margin-bottom: var(--space-5); }

.form-actions-row {
  display: flex;
  justify-content: flex-end;
  margin-top: calc(var(--space-2) * -1);
  margin-bottom: var(--space-5);
}
.link-forgot {
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--brand-700);
  text-decoration: none;
}
.link-forgot:hover { color: var(--brand-800); text-decoration: underline; }

.register-block {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
  margin-top: var(--space-6);
  padding-top: var(--space-5);
  border-top: 1px solid var(--border-subtle);
  font-size: var(--text-base);
}
.register-text { color: var(--text-secondary); }
.link-register {
  font-weight: var(--font-bold);
  color: var(--brand-700);
  text-decoration: none;
}
.link-register:hover { color: var(--brand-800); text-decoration: underline; }
</style>