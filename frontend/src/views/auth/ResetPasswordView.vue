<template>
  <div class="login-page">
    <!-- ═══════════════════════════════════════════════════════
         FONDO FULLSCREEN
         ═══════════════════════════════════════════════════════ -->
    <div
      class="page-bg"
      :style="{ backgroundImage: `url(${imagenLogin})` }"
      role="img"
      aria-label="Tsunami, el perrito rescatista"
    />
    <div class="page-overlay" />

    <!-- ═══════════════════════════════════════════════════════
         CONTENIDO FLOTANTE
         ═══════════════════════════════════════════════════════ -->
    <div class="page-content">
      <!-- ─── Columna izquierda: marca + homenaje ─── -->
      <aside class="hero-side">
        <div class="brand">
          <div class="brand-mark">
            <PawPrint :size="18" />
          </div>
          <div class="brand-text">
            <span class="brand-name">Can &amp; Cat</span>
            <span class="brand-sub">Clínica Veterinaria</span>
          </div>
        </div>

        <div class="hero-message">
          <p class="hero-eyebrow">
            <Heart :size="11" />
            En memoria de Tsunami
          </p>
          <h2 class="hero-title">
            El héroe que nos enseñó que el amor no entiende de razas.
          </h2>
          <p class="hero-paragraph">
            En medio de la tragedia, Tsunami eligió ayudar. Su valentía nos recordó
            que el verdadero coraje no se mide en tamaño, sino en corazón.
            Cada paciente que cuidamos lleva un poco de su espíritu.
          </p>
        </div>

        <div class="hero-credit">
          <PawPrint :size="11" />
          <span>Su legado vive en cada rescate</span>
        </div>
      </aside>

      <!-- ─── Columna derecha: card flotante ─── -->
      <main class="form-side">
        <div class="form-card">
          <!-- Marca mobile (oculta en desktop) -->
          <div class="mobile-brand">
            <div class="brand-mark brand-mark-sm">
              <PawPrint :size="16" />
            </div>
            <span class="brand-name brand-name-sm">Can &amp; Cat</span>
          </div>

          <!-- Header -->
          <header class="form-header">
            <div class="form-icon">
              <ShieldCheck :size="18" />
            </div>
            <h1 class="form-title">Nueva contraseña</h1>
            <p class="form-subtitle">
              Crea una contraseña segura para proteger tu cuenta.
            </p>
          </header>

          <!-- Token inválido: bloque de error específico -->
          <div v-if="tokenInvalido" class="token-error">
            <div class="token-error-icon">
              <AlertCircle :size="24" />
            </div>
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

          <!-- Formulario principal -->
          <template v-else>
            <!-- Alerta de error -->
            <Transition name="slide-fade" mode="out-in">
              <div v-if="mostrarError && !exitoso" key="error" class="alert alert-error">
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
                    type="button"
                    class="toggle-password"
                    :aria-label="mostrarPassword ? 'Ocultar contraseñas' : 'Mostrar contraseñas'"
                    tabindex="-1"
                    @click="mostrarPassword = !mostrarPassword"
                  >
                    <Eye v-if="!mostrarPassword" :size="16" />
                    <EyeOff v-else :size="16" />
                  </button>
                </div>

                <!-- Indicador de fuerza (solo si hay contenido) -->
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

              <!-- Confirmar contraseña -->
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
                  <!-- Checkmark si las contraseñas coinciden -->
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

              <!-- Botón -->
              <button type="submit" class="btn-submit" :disabled="authStore.isLoading">
                <Loader2 v-if="authStore.isLoading" :size="16" class="spin" />
                <ShieldCheck v-else :size="16" />
                {{ authStore.isLoading ? 'Guardando…' : 'Guardar contraseña' }}
              </button>
            </form>

            <!-- Estado de éxito -->
            <div v-else class="success-block">
              <div class="success-icon">
                <CheckCircle2 :size="28" />
              </div>
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

          <!-- Volver al login -->
          <div v-if="!exitoso && !tokenInvalido" class="register-block">
            <router-link to="/auth/login" class="link-register">
              <ArrowLeft :size="13" />
              Volver al inicio de sesión
            </router-link>
          </div>
        </div>

        <!-- Pie fuera de la card -->
        <footer class="form-footer">
          <span>© {{ añoActual }} Can &amp; Cat</span>
          <span class="footer-sep">·</span>
          <span>Sistema de gestión veterinaria</span>
        </footer>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import {
  AlertCircle, Eye, EyeOff, Loader2, CheckCircle2, PawPrint, Heart,
  ShieldCheck, Lock, Check, ArrowLeft, RotateCcw, LogIn
} from 'lucide-vue-next'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

// Coloca la imagen en frontend/public/tsunami-login.webp
const imagenLogin = '/tsunami-login.webp'

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

const añoActual = computed(() => new Date().getFullYear())

// ─── Coincidencia de contraseñas ───
const coinciden = computed(() =>
  form.nuevaContrasena && form.nuevaContrasena === form.confirmarContrasena
)

// ─── Fuerza de contraseña ───
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

// ─── Redirección con progreso visual ───
const segundosRestantes = ref(3)
const progresoRedirect = ref(0)
let redirectTimer = null
let countdownTimer = null

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
      clearInterval(countdownTimer)
      router.push('/auth/login')
    }
  }, intervalo)
}

function limpiarTimers() {
  if (redirectTimer) clearInterval(redirectTimer)
  if (countdownTimer) clearInterval(countdownTimer)
}

onMounted(() => {
  if (!tokenReset.value) {
    tokenInvalido.value = true
    authStore.error = 'El enlace de recuperación es inválido o ha expirado.'
  }
})

onBeforeUnmount(limpiarTimers)

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
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

button { font-family: inherit; }
a { font-family: inherit; }

/* ═══════════════════════════════════════════════════════════
   PÁGINA
   ═══════════════════════════════════════════════════════════ */
.login-page {
  position: relative;
  min-height: 100vh;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  overflow: hidden;
  background: linear-gradient(135deg, #0F766E 0%, #115E59 60%, #134E4A 100%);
}

.page-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-color: #0F766E;
  transform: scale(1.03);
  transition: transform 10s ease-out;
}

.login-page:hover .page-bg {
  transform: scale(1.07);
}

.page-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(
      105deg,
      rgba(15, 23, 42, 0.72) 0%,
      rgba(15, 118, 110, 0.55) 45%,
      rgba(15, 23, 42, 0.35) 100%
    );
  pointer-events: none;
}

.page-content {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  align-items: center;
  gap: 48px;
  min-height: 100vh;
  padding: 48px 56px;
  max-width: 1440px;
  margin: 0 auto;
}

/* ═══════════════════════════════════════════════════════════
   COLUMNA IZQUIERDA
   ═══════════════════════════════════════════════════════════ */
.hero-side {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 32px;
  max-width: 520px;
  min-height: 70vh;
  color: #FFFFFF;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}
.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  flex-shrink: 0;
}
.brand-mark-sm {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: #0F766E;
  border: none;
  color: #FFFFFF;
  backdrop-filter: none;
}
.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}
.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: -0.01em;
}
.brand-name-sm {
  color: #0F172A;
  font-size: 15px;
}
.brand-sub {
  font-size: 11.5px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.78);
  letter-spacing: .3px;
}

.hero-message {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  align-self: flex-start;
  padding: 5px 12px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 20px;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: .7px;
  text-transform: uppercase;
  color: #FFFFFF;
}
.hero-eyebrow svg { color: #FCA5A5; }
.hero-title {
  font-size: 36px;
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -0.025em;
  color: #FFFFFF;
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.35);
}
.hero-paragraph {
  font-size: 14.5px;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.3);
}

.hero-credit {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.72);
  letter-spacing: .2px;
}

/* ═══════════════════════════════════════════════════════════
   COLUMNA DERECHA
   ═══════════════════════════════════════════════════════════ */
.form-side {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 100%;
}

.form-card {
  background: #FFFFFF;
  border-radius: 18px;
  padding: 40px 36px 36px;
  width: 100%;
  max-width: 440px;
  box-shadow:
    0 24px 60px -18px rgba(15, 23, 42, 0.35),
    0 8px 20px -8px rgba(15, 23, 42, 0.2);
  display: flex;
  flex-direction: column;
}

.mobile-brand {
  display: none;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
}

.form-header {
  margin-bottom: 26px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}
.form-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: #F0FDFA;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}
.form-title {
  font-size: 24px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.025em;
  line-height: 1.2;
}
.form-subtitle {
  font-size: 13.5px;
  color: #64748B;
  line-height: 1.55;
  margin-top: 2px;
}

/* ─── Token inválido ─── */
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
.token-error .btn-submit {
  margin-top: 4px;
}

/* Alertas */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  font-weight: 500;
  margin-bottom: 20px;
}
.alert-error {
  background: #FEF2F2;
  color: #991B1B;
  border: 1px solid #FECACA;
}
.alert svg { flex-shrink: 0; margin-top: 1px; }

/* Form */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 20px;
}
.form-label {
  font-size: 12.5px;
  font-weight: 600;
  color: #374151;
}
.required { color: #EF4444; }

.input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}
.input-icon {
  position: absolute;
  left: 14px;
  color: #94A3B8;
  pointer-events: none;
  transition: color .2s ease;
}
.form-input {
  width: 100%;
  padding: 12px 14px 12px 40px;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  font-size: 14px;
  color: #1E293B;
  background: #FFFFFF;
  font-family: inherit;
  transition: border-color .2s, box-shadow .2s, background-color .2s;
  box-sizing: border-box;
}
.form-input::placeholder { color: #94A3B8; }
.form-input:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .12);
}
.input-wrap:focus-within .input-icon { color: #0F766E; }
.form-input:disabled {
  background: #F8FAFC;
  color: #94A3B8;
  cursor: not-allowed;
}
.form-input.is-invalid {
  border-color: #EF4444;
  background: #FEF2F2;
}
.form-input.is-invalid:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, .12);
}
.input-wrap:has(.form-input.is-invalid) .input-icon { color: #EF4444; }

.form-input-password { padding-right: 44px; }

.toggle-password {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #94A3B8;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  transition: color .15s ease, background-color .15s ease;
}
.toggle-password:hover {
  color: #0F766E;
  background: #F0FDFA;
}

/* Checkmark de coincidencia */
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
  flex-shrink: 0;
  animation: popIn .25s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.input-status.is-ok { background: #10B981; }
@keyframes popIn {
  0% { transform: translateY(-50%) scale(0); }
  100% { transform: translateY(-50%) scale(1); }
}

/* Indicador de fuerza */
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
  transition: width .35s cubic-bezier(0.16, 1, 0.3, 1), background-color .25s ease;
}
.strength-fill.is-debil { background: #EF4444; }
.strength-fill.is-regular { background: #F59E0B; }
.strength-fill.is-buena { background: #3B82F6; }
.strength-fill.is-fuerte { background: #10B981; }

.strength-text {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .3px;
  white-space: nowrap;
}
.strength-text.is-debil { color: #EF4444; }
.strength-text.is-regular { color: #D97706; }
.strength-text.is-buena { color: #2563EB; }
.strength-text.is-fuerte { color: #059669; }

.form-error {
  font-size: 12px;
  color: #EF4444;
  font-weight: 600;
}

/* Botón principal */
.btn-submit {
  width: 100%;
  padding: 13px 20px;
  background: #0F766E;
  color: #FFFFFF;
  border: none;
  border-radius: 10px;
  font-size: 14.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  letter-spacing: .1px;
  text-decoration: none;
}
.btn-submit:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 8px 20px -6px rgba(15, 118, 110, .5);
}
.btn-submit:active:not(:disabled) {
  transform: translateY(0);
}
.btn-submit:disabled {
  background: #94A3B8;
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
}

/* Estado de éxito */
.success-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
  padding: 8px 0 4px;
}
.success-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDFA 100%);
  border: 2px solid #A7F3D0;
  color: #059669;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  animation: successPop .5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.08); }
  100% { transform: scale(1); opacity: 1; }
}
.success-title {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
}
.success-text {
  margin: 0;
  font-size: 13.5px;
  line-height: 1.6;
  color: #64748B;
  max-width: 340px;
}

/* Barra de redirección */
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
  transition: width .1s linear;
}
.redirect-hint {
  font-size: 11.5px;
  color: #94A3B8;
  font-weight: 600;
  text-align: center;
}
.success-block .btn-submit {
  margin-top: 4px;
}

/* Volver al login */
.register-block {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #F1F5F9;
  display: flex;
  align-items: center;
  justify-content: center;
}
.link-register {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #0F766E;
  font-weight: 700;
  font-size: 13.5px;
  text-decoration: none;
  transition: color .2s;
}
.link-register:hover {
  color: #115E59;
  text-decoration: underline;
}

/* Pie */
.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.7);
  flex-wrap: wrap;
  text-shadow: 0 1px 6px rgba(0, 0, 0, 0.3);
}
.footer-sep { color: rgba(255, 255, 255, 0.45); }

/* Spinner */
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Transiciones */
.slide-fade-enter-active { transition: all .3s ease-out; }
.slide-fade-leave-active { transition: all .2s ease-in; }
.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

/* ═══════════════════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════════════════ */
@media (max-width: 1024px) {
  .page-content { gap: 32px; padding: 40px 32px; }
  .hero-title { font-size: 30px; }
  .hero-side { min-height: auto; gap: 24px; }
  .form-card { padding: 32px 28px 28px; }
}

@media (max-width: 768px) {
  .page-content {
    grid-template-columns: 1fr;
    gap: 24px;
    padding: 32px 24px 40px;
    align-items: flex-start;
  }
  .hero-side {
    gap: 18px;
    min-height: auto;
    max-width: 100%;
    text-align: center;
    align-items: center;
  }
  .hero-message { display: none; }
  .hero-credit { display: none; }
  .brand { align-self: center; }

  .form-side { gap: 16px; }
  .form-card {
    padding: 32px 24px 28px;
    border-radius: 16px;
  }
  .mobile-brand { display: inline-flex; }
  .form-footer {
    color: #64748B;
    text-shadow: none;
  }
  .footer-sep { color: #CBD5E1; }
}

@media (max-width: 480px) {
  .page-content { padding: 24px 16px 32px; }
  .form-card { padding: 28px 20px 24px; }
  .form-title { font-size: 22px; }
  .form-subtitle { font-size: 13px; }
  .hero-title { font-size: 24px; }
  .success-text { font-size: 13px; }
}
</style>