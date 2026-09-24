<template>
  <div class="login-page">
    <!-- ═══════════════════════════════════════════════════════
         FONDO FULLSCREEN: imagen + overlay
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

          <header class="form-header">
            <h1 class="form-title">Bienvenido de nuevo</h1>
            <p class="form-subtitle">
              Ingresa tus credenciales para acceder a tu cuenta.
            </p>
          </header>

          <!-- Alerta de error -->
          <Transition name="slide-fade">
            <div v-if="mostrarError || authStore.error" class="alert alert-error">
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

            <!-- Recuperar -->
            <div class="form-actions-row">
              <router-link to="/auth/recuperar" class="link-forgot">
                ¿Olvidaste tu contraseña?
              </router-link>
            </div>

            <!-- Botón -->
            <button type="submit" class="btn-submit" :disabled="isLoading">
              <Loader2 v-if="isLoading" :size="16" class="spin" />
              <LogIn v-else :size="16" />
              {{ isLoading ? 'Ingresando…' : 'Ingresar' }}
            </button>
          </form>

          <!-- Registro -->
          <div class="register-block">
            <span class="register-text">¿Aún no tienes una cuenta?</span>
            <router-link to="/auth/registro" class="link-register">
              Regístrate gratis
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
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter, useRoute } from 'vue-router'
import {
  AlertCircle, Eye, EyeOff, Loader2, LogIn, Mail, Lock,
  PawPrint, Heart
} from 'lucide-vue-next'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

// Coloca la imagen en frontend/public/tsunami-login.webp
const imagenLogin = '/tsunami-login.webp'

const email = ref('')
const password = ref('')
const isLoading = ref(false)
const mostrarError = ref(false)
const mostrarPassword = ref(false)

const campoError = ref({
  correo: false,
  password: false,
})

const añoActual = computed(() => new Date().getFullYear())

onMounted(() => {
  if (route.query.session === 'expired') {
    mostrarError.value = true
    authStore.error = 'Su sesión ha expirado. Inicie sesión nuevamente.'
  }
})

const validarEmail = () => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (email.value.trim() && !regex.test(email.value.trim())) {
    campoError.value.correo = true
  }
}

const handleLogin = async () => {
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

<style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

button { font-family: inherit; }
a { font-family: inherit; }

/* ═══════════════════════════════════════════════════════════
   PÁGINA: fondo fullscreen + contenido flotante
   ═══════════════════════════════════════════════════════════ */
.login-page {
  position: relative;
  min-height: 100vh;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  overflow: hidden;
  background: linear-gradient(135deg, #0F766E 0%, #115E59 60%, #134E4A 100%);
}

/* Fondo con imagen */
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

/* Overlay con gradiente teal */
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

/* Contenido en grid 2 columnas sobre el fondo */
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
   COLUMNA IZQUIERDA: marca + homenaje
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

/* Marca */
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

/* Homenaje */
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
.hero-eyebrow svg {
  color: #FCA5A5;
}
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

/* Crédito */
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
   COLUMNA DERECHA: card flotante
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

/* Marca mobile (oculta en desktop) */
.mobile-brand {
  display: none;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;
}

/* Header del formulario */
.form-header {
  margin-bottom: 26px;
}
.form-title {
  font-size: 24px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.025em;
  margin-bottom: 6px;
  line-height: 1.2;
}
.form-subtitle {
  font-size: 13.5px;
  color: #64748B;
  line-height: 1.5;
}

/* Alerta */
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
  margin-bottom: 18px;
}
.form-label {
  font-size: 12.5px;
  font-weight: 600;
  color: #374151;
}
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
.has-error .form-input {
  border-color: #EF4444;
  background: #FEF2F2;
}
.has-error .form-input:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, .12);
}
.has-error .input-icon { color: #EF4444; }

.form-input-password { padding-right: 44px; }

/* Toggle password */
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

/* Error por campo */
.form-error {
  font-size: 12px;
  color: #EF4444;
  font-weight: 600;
}

/* Recuperar */
.form-actions-row {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
  margin-top: -4px;
}
.link-forgot {
  color: #0F766E;
  text-decoration: none;
  font-size: 13px;
  font-weight: 600;
  transition: color .2s;
}
.link-forgot:hover {
  color: #115E59;
  text-decoration: underline;
}

/* Botón submit */
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

/* Registro */
.register-block {
  margin-top: 22px;
  padding-top: 20px;
  border-top: 1px solid #F1F5F9;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13.5px;
  flex-wrap: wrap;
}
.register-text { color: #64748B; }
.link-register {
  color: #0F766E;
  font-weight: 700;
  text-decoration: none;
  transition: color .2s;
}
.link-register:hover {
  color: #115E59;
  text-decoration: underline;
}

/* Pie FUERA de la card */
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

/* Transición */
.slide-fade-enter-active { transition: all .3s ease-out; }
.slide-fade-leave-active { transition: all .2s ease-in; }
.slide-fade-enter-from {
  opacity: 0;
  transform: translateY(-8px);
}
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(0);
}

/* ═══════════════════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════════════════ */
@media (max-width: 1024px) {
  .page-content {
    gap: 32px;
    padding: 40px 32px;
  }
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

  /* El hero se reduce a marca + homenaje compacto */
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
}
</style>