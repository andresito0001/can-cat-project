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
              <UserPlus :size="18" />
            </div>
            <h1 class="form-title">Crear cuenta</h1>
            <p class="form-subtitle">
              Regístrate para gestionar las citas y la salud de tu mascota.
            </p>
          </header>

          <!-- Alerta general -->
          <Transition name="slide-fade" mode="out-in">
            <div v-if="mostrarError" key="error" class="alert alert-error">
              <AlertCircle :size="16" />
              <span>{{ authStore.error || 'Revisa los campos marcados.' }}</span>
            </div>
          </Transition>

          <form @submit.prevent="handleRegister" novalidate>
            <!-- ═══ Sección: Tu cuenta ═══ -->
            <div class="section-block">
              <p class="section-eyebrow">
                <User :size="12" />
                Tu cuenta
              </p>

              <div class="form-group">
                <label class="form-label" for="nombre">
                  Nombre completo <span class="required">*</span>
                </label>
                <div class="input-wrap">
                  <UserCircle :size="15" class="input-icon" />
                  <input
                    id="nombre"
                    v-model="form.nombreCompleto"
                    type="text"
                    class="form-input"
                    :class="{ 'is-invalid': errors.nombreCompleto }"
                    placeholder="Ej: María Alejandra González"
                    :disabled="authStore.isLoading"
                    autocomplete="name"
                  />
                </div>
                <span v-if="errors.nombreCompleto" class="form-error">
                  Ingresa tu nombre completo (mínimo 3 caracteres).
                </span>
              </div>

              <div class="form-group">
                <label class="form-label" for="correo">
                  Correo electrónico <span class="required">*</span>
                </label>
                <div class="input-wrap">
                  <Mail :size="15" class="input-icon" />
                  <input
                    id="correo"
                    v-model="form.correoElectronico"
                    type="email"
                    class="form-input"
                    :class="{ 'is-invalid': errors.correoElectronico }"
                    placeholder="usuario@ejemplo.com"
                    :disabled="authStore.isLoading"
                    autocomplete="email"
                  />
                </div>
                <span v-if="errors.correoElectronico" class="form-error">
                  Ingresa un correo electrónico válido.
                </span>
              </div>

              <div class="form-group">
                <label class="form-label" for="password">
                  Contraseña <span class="required">*</span>
                </label>
                <div class="input-wrap">
                  <Lock :size="15" class="input-icon" />
                  <input
                    id="password"
                    v-model="form.contrasena"
                    :type="mostrarPassword ? 'text' : 'password'"
                    class="form-input form-input-password"
                    :class="{ 'is-invalid': errors.contrasena }"
                    placeholder="Mínimo 6 caracteres"
                    :disabled="authStore.isLoading"
                    autocomplete="new-password"
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
                <span v-if="errors.contrasena" class="form-error">
                  La contraseña debe tener al menos 6 caracteres.
                </span>
              </div>
            </div>

            <!-- ═══ Sección: Identificación ═══ -->
            <div class="section-block">
              <p class="section-eyebrow">
                <CreditCard :size="12" />
                Identificación
              </p>

              <div class="form-grid-2">
                <div class="form-group">
                  <label class="form-label" for="doc">
                    Cédula / Documento <span class="required">*</span>
                  </label>
                  <div class="input-wrap">
                    <IdCard :size="15" class="input-icon" />
                    <input
                      id="doc"
                      v-model="form.documentoIdentidad"
                      type="text"
                      class="form-input"
                      :class="{ 'is-invalid': errors.documentoIdentidad }"
                      placeholder="V-12345678"
                      :disabled="authStore.isLoading"
                    />
                  </div>
                  <span v-if="errors.documentoIdentidad" class="form-error">
                    Documento requerido (mínimo 6 caracteres).
                  </span>
                </div>

                <div class="form-group">
                  <label class="form-label" for="nac">
                    Fecha de nacimiento <span class="optional">(opcional)</span>
                  </label>
                  <div class="input-wrap">
                    <CalendarDays :size="15" class="input-icon" />
                    <input
                      id="nac"
                      v-model="form.fechaNacimiento"
                      type="date"
                      class="form-input"
                      :disabled="authStore.isLoading"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- ═══ Sección: Contacto ═══ -->
            <div class="section-block">
              <p class="section-eyebrow">
                <Phone :size="12" />
                Contacto
              </p>

              <div class="form-grid-2">
                <div class="form-group">
                  <label class="form-label" for="tel1">
                    Teléfono principal <span class="required">*</span>
                  </label>
                  <div class="input-wrap">
                    <Phone :size="15" class="input-icon" />
                    <input
                      id="tel1"
                      v-model="form.telefonoPrincipal"
                      type="tel"
                      class="form-input"
                      :class="{ 'is-invalid': errors.telefonoPrincipal }"
                      placeholder="0412-1234567"
                      :disabled="authStore.isLoading"
                      autocomplete="tel"
                    />
                  </div>
                  <span v-if="errors.telefonoPrincipal" class="form-error">
                    El teléfono principal es obligatorio.
                  </span>
                </div>

                <div class="form-group">
                  <label class="form-label" for="tel2">
                    Teléfono secundario <span class="optional">(opcional)</span>
                  </label>
                  <div class="input-wrap">
                    <Phone :size="15" class="input-icon" />
                    <input
                      id="tel2"
                      v-model="form.telefonoSecundario"
                      type="tel"
                      class="form-input"
                      placeholder="0212-9876543"
                      :disabled="authStore.isLoading"
                    />
                  </div>
                </div>

                <div class="form-group full-width">
                  <label class="form-label" for="direccion">
                    Dirección <span class="optional">(opcional)</span>
                  </label>
                  <div class="input-wrap">
                    <MapPin :size="15" class="input-icon" />
                    <input
                      id="direccion"
                      v-model="form.direccion"
                      type="text"
                      class="form-input"
                      placeholder="Av. Principal, Casa 5, Sector Centro"
                      :disabled="authStore.isLoading"
                      autocomplete="street-address"
                    />
                  </div>
                </div>

                <div class="form-group full-width">
                  <label class="form-label" for="ciudad">
                    Ciudad <span class="optional">(opcional)</span>
                  </label>
                  <div class="input-wrap">
                    <Building2 :size="15" class="input-icon" />
                    <input
                      id="ciudad"
                      v-model="form.ciudad"
                      type="text"
                      class="form-input"
                      placeholder="Ej: Barcelona"
                      :disabled="authStore.isLoading"
                      autocomplete="address-level2"
                    />
                  </div>
                </div>
              </div>
            </div>

            <!-- Botón -->
            <button type="submit" class="btn-submit" :disabled="authStore.isLoading">
              <Loader2 v-if="authStore.isLoading" :size="16" class="spin" />
              <UserPlus v-else :size="16" />
              {{ authStore.isLoading ? 'Creando cuenta…' : 'Crear cuenta' }}
            </button>

            <p class="legal-note">
              Al crear una cuenta aceptas nuestros términos de servicio y política de privacidad.
            </p>
          </form>

          <!-- Volver al login -->
          <div class="register-block">
            <span class="register-text">¿Ya tienes una cuenta?</span>
            <router-link to="/auth/login" class="link-register">
              Inicia sesión
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
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import {
  AlertCircle, Eye, EyeOff, Loader2, UserPlus, User, UserCircle,
  Mail, Lock, CreditCard, IdCard, CalendarDays, Phone, MapPin,
  Building2, PawPrint, Heart
} from 'lucide-vue-next'

const authStore = useAuthStore()
const router = useRouter()

// Coloca la imagen en frontend/public/tsunami-login.webp
const imagenLogin = '/tsunami-login.webp'

const mostrarPassword = ref(false)
const mostrarError = ref(false)
const añoActual = computed(() => new Date().getFullYear())

const errors = reactive({
  nombreCompleto: false,
  documentoIdentidad: false,
  correoElectronico: false,
  contrasena: false,
  telefonoPrincipal: false,
})

const form = reactive({
  nombreCompleto: '',
  documentoIdentidad: '',
  correoElectronico: '',
  contrasena: '',
  telefonoPrincipal: '',
  telefonoSecundario: '',
  direccion: '',
  ciudad: '',
  fechaNacimiento: '',
})

function resetErrors() {
  Object.keys(errors).forEach(k => (errors[k] = false))
  mostrarError.value = false
  authStore.error = null
}

function validarEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

async function handleRegister() {
  resetErrors()

  let hayError = false

  if (!form.nombreCompleto.trim() || form.nombreCompleto.trim().length < 3) {
    errors.nombreCompleto = true
    hayError = true
  }
  if (!form.documentoIdentidad.trim() || form.documentoIdentidad.trim().length < 6) {
    errors.documentoIdentidad = true
    hayError = true
  }
  if (!form.correoElectronico.trim() || !validarEmail(form.correoElectronico)) {
    errors.correoElectronico = true
    hayError = true
  }
  if (!form.contrasena || form.contrasena.length < 6) {
    errors.contrasena = true
    hayError = true
  }
  if (!form.telefonoPrincipal.trim()) {
    errors.telefonoPrincipal = true
    hayError = true
  }

  if (hayError) {
    authStore.error = 'Por favor completa todos los campos obligatorios correctamente.'
    mostrarError.value = true
    return
  }

  // Limpiar campos vacíos opcionales
  const payload = { ...form }
  if (!payload.telefonoSecundario) delete payload.telefonoSecundario
  if (!payload.direccion) delete payload.direccion
  if (!payload.ciudad) delete payload.ciudad
  if (!payload.fechaNacimiento) delete payload.fechaNacimiento

  const result = await authStore.register(payload)

  if (result.success) {
    // Redirección limpia al login (el mensaje de éxito se maneja allá con el flujo normal)
    router.push({ path: '/auth/login', query: { registered: '1' } })
  } else {
    mostrarError.value = true
    // Resaltar campo según error del backend
    const msg = (authStore.error || '').toLowerCase()
    if (msg.includes('correo')) errors.correoElectronico = true
    if (msg.includes('documento') || msg.includes('cédula') || msg.includes('cedula')) errors.documentoIdentidad = true
    if (msg.includes('nombre')) errors.nombreCompleto = true
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
      rgba(15, 23, 42, 0.75) 0%,
      rgba(15, 118, 110, 0.58) 45%,
      rgba(15, 23, 42, 0.4) 100%
    );
  pointer-events: none;
}

.page-content {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 0.85fr) minmax(0, 1.15fr);
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
  max-width: 480px;
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
  font-size: 34px;
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -0.025em;
  color: #FFFFFF;
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.35);
}
.hero-paragraph {
  font-size: 14px;
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
  padding: 36px 36px 32px;
  width: 100%;
  max-width: 560px;
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

/* Header */
.form-header {
  margin-bottom: 24px;
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
  margin-bottom: 18px;
}
.alert-error {
  background: #FEF2F2;
  color: #991B1B;
  border: 1px solid #FECACA;
}
.alert svg { flex-shrink: 0; margin-top: 1px; }

/* Secciones del form */
.section-block {
  padding-bottom: 18px;
  margin-bottom: 18px;
  border-bottom: 1px solid #F1F5F9;
}
.section-block:last-of-type {
  border-bottom: none;
  padding-bottom: 0;
  margin-bottom: 8px;
}
.section-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 14px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .7px;
  color: #0F766E;
}

/* Form */
.form-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
  margin-bottom: 14px;
}
.form-group:last-child { margin-bottom: 0; }
.form-group.full-width { grid-column: 1 / -1; }

.form-label {
  font-size: 12.5px;
  font-weight: 600;
  color: #374151;
}
.required { color: #EF4444; }
.optional {
  color: #94A3B8;
  font-weight: 500;
  font-size: 11.5px;
  margin-left: 2px;
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
  padding: 11px 14px 11px 40px;
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
.form-input.is-invalid + .input-icon,
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
  margin-top: 6px;
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

/* Nota legal */
.legal-note {
  margin: 12px 0 0;
  font-size: 11px;
  line-height: 1.5;
  color: #94A3B8;
  text-align: center;
}

/* Volver al login */
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
  display: inline-flex;
  align-items: center;
  color: #0F766E;
  font-weight: 700;
  text-decoration: none;
  transition: color .2s;
}
.link-register:hover {
  color: #115E59;
  text-decoration: underline;
}

/* Pie fuera de la card */
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
  .page-content {
    grid-template-columns: minmax(0, 0.7fr) minmax(0, 1.3fr);
    gap: 32px;
    padding: 40px 32px;
  }
  .hero-title { font-size: 28px; }
  .hero-side { min-height: auto; gap: 24px; }
  .form-card { padding: 32px 28px 28px; max-width: 520px; }
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
    padding: 28px 22px 24px;
    border-radius: 16px;
    max-width: 100%;
  }
  .mobile-brand { display: inline-flex; }
  .form-footer {
    color: #64748B;
    text-shadow: none;
  }
  .footer-sep { color: #CBD5E1; }
}

@media (max-width: 560px) {
  .page-content { padding: 24px 16px 32px; }
  .form-card { padding: 24px 18px 22px; }
  .form-title { font-size: 22px; }
  .form-subtitle { font-size: 13px; }
  .hero-title { font-size: 22px; }
  .form-grid-2 { grid-template-columns: 1fr; gap: 0; }
  .section-block { padding-bottom: 14px; margin-bottom: 14px; }
  .form-group { margin-bottom: 12px; }
}
</style>