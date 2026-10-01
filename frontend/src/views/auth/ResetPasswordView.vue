<template>
  <AuthShell>
    <header class="form-header">
      <div class="form-icon"><ShieldCheck :size="18" /></div>
      <h1 class="form-title">Nueva contraseña</h1>
      <p class="form-subtitle">
        Crea una contraseña segura para proteger tu cuenta.
      </p>
    </header>

    <div v-if="tokenInvalido" class="token-error">
      <div class="token-error-icon"><AlertCircle :size="24" /></div>
      <h2 class="token-error-title">Enlace inválido o expirado</h2>
      <p class="token-error-text">
        El enlace de recuperación que utilizaste ya no es válido.
        Solicita uno nuevo para continuar.
      </p>
      <RouterLink to="/auth/recuperar" class="token-error-cta">
        <AppButton variant="primary" size="lg" block>
          <template #icon-left><RotateCcw :size="15" /></template>
          Solicitar nuevo enlace
        </AppButton>
      </RouterLink>
    </div>

    <template v-else>
      <Transition name="slide-down">
        <AppAlert
          v-if="mostrarError && !exitoso && authStore.error"
          variant="error"
          class="alert-spacing"
        >
          {{ authStore.error }}
        </AppAlert>
      </Transition>

      <form v-if="!exitoso" @submit.prevent="handleReset" novalidate>
        <AppFormField
          label="Nueva contraseña"
          :error="errors.password ? 'La contraseña debe tener al menos 6 caracteres.' : ''"
          required
        >
          <template #default="{ id, invalid }">
            <AppPasswordField
              :id="id"
              v-model="form.nuevaContrasena"
              placeholder="Mínimo 6 caracteres"
              :disabled="authStore.isLoading"
              :error="invalid"
              autocomplete="new-password"
            />
          </template>
        </AppFormField>

        <Transition name="slide-down">
          <div v-if="form.nuevaContrasena" class="strength-block">
            <div class="strength-bar">
              <div
                class="strength-fill"
                :class="`is-${fuerzaPassword.nivel}`"
                :style="{ width: `${fuerzaPassword.pct}%` }"
              />
            </div>
            <span class="strength-text" :class="`is-${fuerzaPassword.nivel}`">
              {{ fuerzaPassword.texto }}
            </span>
          </div>
        </Transition>

        <AppFormField
          label="Confirmar contraseña"
          :error="errors.confirmar ? 'Las contraseñas no coinciden.' : ''"
          required
        >
          <template #default="{ id, invalid }">
            <div class="confirm-wrap">
              <AppPasswordField
                :id="id"
                v-model="form.confirmarContrasena"
                placeholder="Repite la contraseña"
                :disabled="authStore.isLoading"
                :error="invalid"
                autocomplete="new-password"
              />
              <span
                v-if="form.confirmarContrasena && coinciden"
                class="confirm-check"
                aria-label="Las contraseñas coinciden"
              >
                <Check :size="14" />
              </span>
            </div>
          </template>
        </AppFormField>

        <AppButton
          type="submit"
          variant="primary"
          size="lg"
          block
          :loading="authStore.isLoading"
        >
          <template #icon-left><ShieldCheck :size="16" /></template>
          {{ authStore.isLoading ? 'Guardando…' : 'Guardar contraseña' }}
        </AppButton>
      </form>

      <div v-else class="success-block">
        <div class="success-icon"><CheckCircle2 :size="28" /></div>
        <h2 class="success-title">Contraseña actualizada</h2>
        <p class="success-text">
          Tu contraseña se cambió correctamente. Te estamos redirigiendo al
          inicio de sesión…
        </p>

        <div class="redirect-progress">
          <div class="redirect-bar">
            <div class="redirect-fill" :style="{ width: `${progresoRedirect}%` }" />
          </div>
          <span class="redirect-hint">Redirigiendo en {{ segundosRestantes }}s</span>
        </div>

        <AppButton variant="primary" size="lg" block @click="router.push('/auth/login')">
          <template #icon-left><LogIn :size="15" /></template>
          Ir al inicio de sesión ahora
        </AppButton>
      </div>
    </template>

    <div v-if="!exitoso && !tokenInvalido" class="register-block">
      <RouterLink to="/auth/login" class="link-register">
        <ArrowLeft :size="13" />
        Volver al inicio de sesión
      </RouterLink>
    </div>
  </AuthShell>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ShieldCheck, AlertCircle, CheckCircle2, Check,
  ArrowLeft, RotateCcw, LogIn,
} from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth.store'

import AuthShell from './AuthShell.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppFormField from '@/components/ui/AppFormField.vue'
import AppPasswordField from '@/components/ui/AppPasswordField.vue'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const mostrarError = ref(false)
const exitoso = ref(false)
const errors = reactive({ password: false, confirmar: false })

const form = reactive({
  nuevaContrasena: '',
  confirmarContrasena: '',
})

const tokenReset = ref(route.query.token || '')
const tokenInvalido = ref(false)

const coinciden = computed(
  () => form.nuevaContrasena && form.nuevaContrasena === form.confirmarContrasena
)

const fuerzaPassword = computed(() => {
  const p = form.nuevaContrasena
  if (!p) return { nivel: 'vacio', texto: '', pct: 0 }
  let score = 0
  if (p.length >= 6) score++
  if (p.length >= 10) score++
  if (/[A-Z]/.test(p)) score++
  if (/[0-9]/.test(p)) score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  if (score <= 1) return { nivel: 'debil', texto: 'Contraseña débil', pct: 25 }
  if (score === 2) return { nivel: 'regular', texto: 'Contraseña regular', pct: 50 }
  if (score === 3) return { nivel: 'buena', texto: 'Contraseña buena', pct: 75 }
  return { nivel: 'fuerte', texto: 'Contraseña fuerte', pct: 100 }
})

const segundosRestantes = ref(3)
const progresoRedirect = ref(0)
let redirectTimer = null

function iniciarRedireccion() {
  const duracion = 3000
  const intervalo = 50
  let transcurrido = 0
  redirectTimer = setInterval(() => {
    transcurrido += intervalo
    progresoRedirect.value = Math.min((transcurrido / duracion) * 100, 100)
    segundosRestantes.value = Math.max(Math.ceil((duracion - transcurrido) / 1000), 0)
    if (transcurrido >= duracion) {
      clearInterval(redirectTimer)
      router.push('/auth/login')
    }
  }, intervalo)
}

onMounted(() => {
  if (!tokenReset.value) {
    tokenInvalido.value = true
    authStore.error = 'El enlace de recuperación es inválido o ha expirado.'
  }
})

onBeforeUnmount(() => {
  if (redirectTimer) clearInterval(redirectTimer)
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
    authStore.error = 'El enlace de recuperación es inválido.'
    mostrarError.value = true
    tokenInvalido.value = true
    return
  }

  const result = await authStore.restablecerContrasena(tokenReset.value, form.nuevaContrasena)

  if (result.success) {
    exitoso.value = true
    iniciarRedireccion()
    return
  }

  // El backend indica que el token está muerto → mostrar la vista completa de enlace inválido
  const codigo = result.codigo || ''
  if (
    codigo === 'TOKEN_NO_EXISTE' ||
    codigo === 'TOKEN_YA_USADO' ||
    codigo === 'TOKEN_EXPIRADO' ||
    codigo === 'TOKEN_CORRUPTO'
  ) {
    authStore.error = result.mensaje
    tokenInvalido.value = true
    return
  }

  // Error genérico (red, 500, etc.) → mostrar alert en el formulario
  mostrarError.value = true
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

.token-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-2);
  padding: var(--space-2) 0 var(--space-1);
}
.token-error-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-full);
  background: var(--danger-50);
  border: 2px solid var(--danger-200);
  color: var(--danger-600);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
}
.token-error-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
  margin: 0;
}
.token-error-text {
  font-size: var(--text-md);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  max-width: 340px;
  margin: 0 0 var(--space-3);
}
.token-error-cta { width: 100%; }

.strength-block {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-top: calc(var(--space-4) * -1);
  margin-bottom: var(--space-4);
}
.strength-bar {
  flex: 1;
  height: 5px;
  border-radius: var(--radius-full);
  background: var(--neutral-200);
  overflow: hidden;
}
.strength-fill {
  height: 100%;
  border-radius: var(--radius-full);
  transition: width var(--duration-slow) var(--ease-out),
              background-color var(--duration-base) var(--ease-out);
}
.strength-fill.is-debil   { background: var(--danger-500); }
.strength-fill.is-regular { background: var(--warning-500); }
.strength-fill.is-buena   { background: var(--info-500); }
.strength-fill.is-fuerte  { background: var(--success-500); }
.strength-text {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.03em;
  white-space: nowrap;
}
.strength-text.is-debil   { color: var(--danger-600); }
.strength-text.is-regular { color: var(--warning-600); }
.strength-text.is-buena   { color: var(--info-600); }
.strength-text.is-fuerte  { color: var(--success-600); }

.confirm-wrap { position: relative; display: flex; align-items: center; }
.confirm-check {
  position: absolute;
  right: 44px;
  top: 50%;
  transform: translateY(-50%);
  width: 22px;
  height: 22px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  background: var(--success-500);
  flex-shrink: 0;
  pointer-events: none;
  animation: popIn 0.25s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes popIn {
  0% { transform: translateY(-50%) scale(0); }
  100% { transform: translateY(-50%) scale(1); }
}

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
  margin: 0 0 var(--space-2);
}

.redirect-progress {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  margin: var(--space-3) 0 var(--space-2);
}
.redirect-bar {
  height: 4px;
  background: var(--neutral-200);
  border-radius: var(--radius-full);
  overflow: hidden;
}
.redirect-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--brand-700), var(--brand-500));
  border-radius: var(--radius-full);
  transition: width 0.1s linear;
}
.redirect-hint {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: var(--font-semibold);
  text-align: center;
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