<template>
  <AuthShell wide>
    <header class="form-header">
      <div class="form-icon"><UserPlus :size="18" /></div>
      <h1 class="form-title">Crear cuenta</h1>
      <p class="form-subtitle">
        Regístrate para gestionar las citas y la salud de tu mascota.
      </p>
    </header>

    <Transition name="slide-fade">
      <div v-if="mostrarError" class="alert alert-error">
        <AlertCircle :size="16" />
        <span>{{ authStore.error || 'Revisa los campos marcados.' }}</span>
      </div>
    </Transition>

    <form @submit.prevent="handleRegister" novalidate>
      <!-- ═══ Sección: Tu cuenta ═══ -->
      <div class="section-block">
        <p class="section-eyebrow"><User :size="12" /> Tu cuenta</p>

        <div class="form-group">
          <label class="form-label" for="nombre">
            Nombre completo <span class="required">*</span>
          </label>
          <div class="input-wrap">
            <UserCircle :size="15" class="input-icon" />
            <input
              id="nombre" v-model="form.nombreCompleto" type="text"
              class="form-input" :class="{ 'is-invalid': errors.nombreCompleto }"
              placeholder="Ej: María Alejandra González" :disabled="authStore.isLoading"
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
              id="correo" v-model="form.correoElectronico" type="email"
              class="form-input" :class="{ 'is-invalid': errors.correoElectronico }"
              placeholder="usuario@ejemplo.com" :disabled="authStore.isLoading"
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
              id="password" v-model="form.contrasena"
              :type="mostrarPassword ? 'text' : 'password'"
              class="form-input form-input-password" :class="{ 'is-invalid': errors.contrasena }"
              placeholder="Mínimo 6 caracteres" :disabled="authStore.isLoading"
              autocomplete="new-password"
            />
            <button
              type="button" class="toggle-password" tabindex="-1"
              :aria-label="mostrarPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'"
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
        <p class="section-eyebrow"><CreditCard :size="12" /> Identificación</p>

        <div class="form-grid-2">
          <div class="form-group">
            <label class="form-label" for="doc">
              Cédula / Documento <span class="required">*</span>
            </label>
            <div class="input-wrap">
              <IdCard :size="15" class="input-icon" />
              <input
                id="doc" v-model="form.documentoIdentidad" type="text"
                class="form-input" :class="{ 'is-invalid': errors.documentoIdentidad }"
                placeholder="V-12345678" :disabled="authStore.isLoading"
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
                id="nac" v-model="form.fechaNacimiento" type="date"
                class="form-input" :disabled="authStore.isLoading"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- ═══ Sección: Contacto ═══ -->
      <div class="section-block">
        <p class="section-eyebrow"><Phone :size="12" /> Contacto</p>

        <div class="form-grid-2">
          <div class="form-group">
            <label class="form-label" for="tel1">
              Teléfono principal <span class="required">*</span>
            </label>
            <div class="input-wrap">
              <Phone :size="15" class="input-icon" />
              <input
                id="tel1" v-model="form.telefonoPrincipal" type="tel"
                class="form-input" :class="{ 'is-invalid': errors.telefonoPrincipal }"
                placeholder="0412-1234567" :disabled="authStore.isLoading"
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
                id="tel2" v-model="form.telefonoSecundario" type="tel"
                class="form-input" placeholder="0212-9876543"
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
                id="direccion" v-model="form.direccion" type="text"
                class="form-input" placeholder="Av. Principal, Casa 5, Sector Centro"
                :disabled="authStore.isLoading" autocomplete="street-address"
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
                id="ciudad" v-model="form.ciudad" type="text"
                class="form-input" placeholder="Ej: Barcelona"
                :disabled="authStore.isLoading" autocomplete="address-level2"
              />
            </div>
          </div>
        </div>
      </div>

      <button type="submit" class="btn-submit" :disabled="authStore.isLoading">
        <Loader2 v-if="authStore.isLoading" :size="16" class="spin" />
        <UserPlus v-else :size="16" />
        {{ authStore.isLoading ? 'Creando cuenta…' : 'Crear cuenta' }}
      </button>

      <p class="legal-note">
        Al crear una cuenta aceptas nuestros términos de servicio y política de privacidad.
      </p>
    </form>

    <div class="register-block">
      <span class="register-text">¿Ya tienes una cuenta?</span>
      <router-link to="/auth/login" class="link-register">Inicia sesión</router-link>
    </div>
  </AuthShell>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import {
  AlertCircle, Eye, EyeOff, Loader2, UserPlus, User, UserCircle,
  Mail, Lock, CreditCard, IdCard, CalendarDays, Phone, MapPin, Building2,
} from 'lucide-vue-next'

import AuthShell from './AuthShell.vue'

const authStore = useAuthStore()
const router = useRouter()

const mostrarPassword = ref(false)
const mostrarError = ref(false)

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

  const payload = { ...form }
  if (!payload.telefonoSecundario) delete payload.telefonoSecundario
  if (!payload.direccion) delete payload.direccion
  if (!payload.ciudad) delete payload.ciudad
  if (!payload.fechaNacimiento) delete payload.fechaNacimiento

  const result = await authStore.register(payload)

  if (result.success) {
    router.push({ path: '/auth/login', query: { registered: '1' } })
  } else {
    mostrarError.value = true
    const msg = (authStore.error || '').toLowerCase()
    if (msg.includes('correo')) errors.correoElectronico = true
    if (msg.includes('documento') || msg.includes('cédula') || msg.includes('cedula')) errors.documentoIdentidad = true
    if (msg.includes('nombre')) errors.nombreCompleto = true
  }
}
</script>