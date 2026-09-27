<template>
  <AuthShell>
    <header class="form-header">
      <div class="form-icon"><ShieldCheck :size="18" /></div>
      <h1 class="form-title">Nueva contraseña</h1>
      <p class="form-subtitle">
        Crea una contraseña segura para proteger tu cuenta.
      </p>
    </header>

    <!-- Token inválido -->
    <div v-if="tokenInvalido" class="token-error">
      <div class="token-error-icon"><AlertCircle :size="24" /></div>
      <h2 class="token-error-title">Enlace inválido o expirado</h2>
      <p class="token-error-text">
        El enlace de recuperación que utilizaste ya no es válido.
        Solicita uno nuevo para continuar.
      </p>
      <router-link to="/auth/recuperar" class="btn-submit">
        <RotateCcw :size="15" />
        Solicitar nuevo enlace
      </router-link>
    </div>

    <template v-else>
      <Transition name="slide-fade">
        <div v-if="mostrarError && !exitoso" class="alert alert-error">
          <AlertCircle :size="16" />
          <span>{{ authStore.error || 'Revisa los campos marcados.' }}</span>
        </div>
      </Transition>

      <form v-if="!exitoso" @submit.prevent="handleReset" novalidate>
        <!-- Nueva contraseña -->
        <div class="form-group">
          <label class="form-label" for="password">
            Nueva contraseña <span class="required">*</span>
          </label>
          <div class="input-wrap">
            <Lock :size="15" class="input-icon" />
            <input
              id="password"
              v-model="form.nuevaContrasena"
              :type="mostrarPassword ? 'text' : 'password'"
              class="form-input form-input-password"
              :class="{ 'is-invalid': errors.password }"
              placeholder="Mínimo 6 caracteres"
              :disabled="authStore.isLoading"
              autocomplete="new-password"
            />
            <button
              type="button" class="toggle-password" tabindex="-1"
              :aria-label="mostrarPassword ? 'Ocultar contraseñas' : 'Mostrar contraseñas'"
              @click="mostrarPassword = !mostrarPassword"
            >
              <Eye v-if="!mostrarPassword" :size="16" />
              <EyeOff v-else :size="16" />
            </button>
          </div>

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

          <span v-if="errors.password" class="form-error">
            La contraseña debe tener al menos 6 caracteres.
          </span>
        </div>

        <!-- Confirmar -->
        <div class="form-group">
          <label class="form-label" for="confirm">
            Confirmar contraseña <span class="required">*</span>
          </label>
          <div class="input-wrap">
            <Lock :size="15" class="input-icon" />
            <input
              id="confirm"
              v-model="form.confirmarContrasena"
              :type="mostrarPassword ? 'text' : 'password'"
              class="form-input"
              :class="{ 'is-invalid': errors.confirmar }"
              placeholder="Repite la contraseña"
              :disabled="authStore.isLoading"
              autocomplete="new-password"
            />
            <span
              v-if="form.confirmarContrasena && coinciden"
              class="input-status is-ok"
              aria-label="Coinciden"
            >
              <Check :size="14" />
            </span>
          </div>
          <span v-if="errors.confirmar" class="form-error">
            Las contraseñas no coinciden.
          </span>
        </div>

        <button type="submit" class="btn-submit" :disabled="authStore.isLoading">
          <Loader2 v-if="authStore.isLoading" :size="16" class="spin" />
          <ShieldCheck v-else :size="16" />
          {{ authStore.isLoading ? 'Guardando…' : 'Guardar contraseña' }}
        </button>
      </form>

      <!-- Éxito -->
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
        <router-link to="/auth/login" class="btn-submit">
          <LogIn :size="15" />
          Ir al inicio de sesión ahora
        </router-link>
      </div>
    </template>

    <div v-if="!exitoso && !tokenInvalido" class="register-block">
      <router-link to="/auth/login" class="link-register">
        <ArrowLeft :size="13" />
        Volver al inicio de sesión
      </router-link>
    </div>
  </AuthShell>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import {
  AlertCircle, Eye, EyeOff, Loader2, CheckCircle2,
  ShieldCheck, Lock, Check, ArrowLeft, RotateCcw, LogIn,
} from 'lucide-vue-next'

import AuthShell from './AuthShell.vue'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const mostrarPassword = ref(false)
const mostrarError = ref(false)
const exitoso = ref(false)
const errors = reactive({ password: false, confirmar: false })

const form = reactive({
  nuevaContrasena: '',
  confirmarContrasena: '',
})

const tokenReset = ref(route.query.token || '')
const tokenInvalido = ref(false)

const coinciden = computed(() =>
  form.nuevaContrasena && form.nuevaContrasena === form.confirmarContrasena
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
    authStore.error = 'Token de recuperación inválido.'
    mostrarError.value = true
    tokenInvalido.value = true
    return
  }

  const result = await authStore.restablecerContrasena(tokenReset.value, form.nuevaContrasena)

  if (result.success) {
    exitoso.value = true
    iniciarRedireccion()
  } else {
    mostrarError.value = true
  }
}
</script>

<style scoped>
/* ── Token inválido ── */
.token-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
  padding: 8px 0 4px;
}
.token-error-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #FEF2F2;
  border: 2px solid #FECACA;
  color: #DC2626;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}
.token-error-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
}
.token-error-text {
  margin: 0 0 8px;
  font-size: 13.5px;
  line-height: 1.6;
  color: #64748B;
  max-width: 340px;
}

/* ── Medidor de fuerza ── */
.strength-block {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
}
.strength-bar {
  flex: 1;
  height: 5px;
  border-radius: 999px;
  background: #E2E8F0;
  overflow: hidden;
}
.strength-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.35s cubic-bezier(0.16, 1, 0.3, 1), background-color 0.25s ease;
}
.strength-fill.is-debil { background: #EF4444; }
.strength-fill.is-regular { background: #F59E0B; }
.strength-fill.is-buena { background: #3B82F6; }
.strength-fill.is-fuerte { background: #10B981; }

.strength-text {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  white-space: nowrap;
}
.strength-text.is-debil { color: #EF4444; }
.strength-text.is-regular { color: #D97706; }
.strength-text.is-buena { color: #2563EB; }
.strength-text.is-fuerte { color: #059669; }

/* ── Check de coincidencia ── */
.input-status {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  background: #10B981;
  flex-shrink: 0;
  animation: popIn 0.25s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes popIn {
  0% { transform: translateY(-50%) scale(0); }
  100% { transform: translateY(-50%) scale(1); }
}

/* ── Barra de redirección ── */
.redirect-progress {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 12px 0 6px;
}
.redirect-bar {
  height: 4px;
  background: #E2E8F0;
  border-radius: 999px;
  overflow: hidden;
}
.redirect-fill {
  height: 100%;
  background: linear-gradient(90deg, #0F766E, #14B8A6);
  border-radius: 999px;
  transition: width 0.1s linear;
}
.redirect-hint {
  font-size: 11.5px;
  color: #94A3B8;
  font-weight: 600;
  text-align: center;
}

@media (prefers-reduced-motion: reduce) {
  .input-status { animation: none; }
}
</style>