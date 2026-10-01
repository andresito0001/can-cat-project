<template>
  <AuthShell wide>
    <header class="form-header">
      <div class="form-icon"><UserPlus :size="18" /></div>
      <h1 class="form-title">Crear cuenta</h1>
      <p class="form-subtitle">
        Regístrate para gestionar las citas y la salud de tu mascota.
      </p>
    </header>

    <Transition name="slide-down">
      <AppAlert v-if="mostrarError && authStore.error" variant="error" class="alert-spacing">
        {{ authStore.error }}
      </AppAlert>
    </Transition>

    <form @submit.prevent="handleRegister" novalidate>
      <section class="form-section">
        <p class="section-eyebrow"><User :size="12" /> Tu cuenta</p>

        <AppFormField
          label="Nombre completo"
          :error="errors.nombreCompleto ? 'Ingresa tu nombre completo (mínimo 3 caracteres).' : ''"
          required
        >
          <template #default="{ id, invalid }">
            <AppInput
              :id="id"
              v-model="form.nombreCompleto"
              placeholder="Ej: María Alejandra González"
              :disabled="authStore.isLoading"
              :error="invalid"
              autocomplete="name"
            />
          </template>
        </AppFormField>

        <AppFormField
          label="Correo electrónico"
          :error="errors.correoElectronico ? 'Ingresa un correo electrónico válido.' : ''"
          required
        >
          <template #default="{ id, invalid }">
            <AppInput
              :id="id"
              v-model="form.correoElectronico"
              type="email"
              placeholder="usuario@ejemplo.com"
              :disabled="authStore.isLoading"
              :error="invalid"
              autocomplete="email"
            />
          </template>
        </AppFormField>

        <AppFormField
          label="Contraseña"
          :error="errors.contrasena ? 'La contraseña debe tener al menos 6 caracteres.' : ''"
          hint="Mínimo 6 caracteres"
          required
        >
          <template #default="{ id, invalid }">
            <AppPasswordField
              :id="id"
              v-model="form.contrasena"
              placeholder="Mínimo 6 caracteres"
              :disabled="authStore.isLoading"
              :error="invalid"
              autocomplete="new-password"
            />
          </template>
        </AppFormField>
      </section>

      <section class="form-section">
        <p class="section-eyebrow"><CreditCard :size="12" /> Identificación</p>

        <div class="form-grid-2">
          <AppFormField
            label="Cédula / Documento"
            :error="errors.documentoIdentidad ? 'Documento requerido (mínimo 6 caracteres).' : ''"
            required
          >
            <template #default="{ id, invalid }">
              <AppInput
                :id="id"
                v-model="form.documentoIdentidad"
                placeholder="V-12345678"
                :disabled="authStore.isLoading"
                :error="invalid"
              />
            </template>
          </AppFormField>

          <AppFormField label="Fecha de nacimiento" optional>
            <template #default="{ id }">
              <AppInput
                :id="id"
                v-model="form.fechaNacimiento"
                type="date"
                :disabled="authStore.isLoading"
              />
            </template>
          </AppFormField>
        </div>
      </section>

      <section class="form-section">
        <p class="section-eyebrow"><Phone :size="12" /> Contacto</p>

        <div class="form-grid-2">
          <AppFormField
            label="Teléfono principal"
            :error="errors.telefonoPrincipal ? 'El teléfono principal es obligatorio.' : ''"
            required
          >
            <template #default="{ id, invalid }">
              <AppInput
                :id="id"
                v-model="form.telefonoPrincipal"
                type="tel"
                placeholder="0412-1234567"
                :disabled="authStore.isLoading"
                :error="invalid"
                autocomplete="tel"
              />
            </template>
          </AppFormField>

          <AppFormField label="Teléfono secundario" optional>
            <template #default="{ id }">
              <AppInput
                :id="id"
                v-model="form.telefonoSecundario"
                type="tel"
                placeholder="0212-9876543"
                :disabled="authStore.isLoading"
              />
            </template>
          </AppFormField>

          <div class="form-grid-full">
            <AppFormField label="Dirección" optional>
              <template #default="{ id }">
                <AppInput
                  :id="id"
                  v-model="form.direccion"
                  placeholder="Av. Principal, Casa 5, Sector Centro"
                  :disabled="authStore.isLoading"
                  autocomplete="street-address"
                />
              </template>
            </AppFormField>
          </div>

          <div class="form-grid-full">
            <AppFormField label="Ciudad" optional>
              <template #default="{ id }">
                <AppInput
                  :id="id"
                  v-model="form.ciudad"
                  placeholder="Ej: Barcelona"
                  :disabled="authStore.isLoading"
                  autocomplete="address-level2"
                />
              </template>
            </AppFormField>
          </div>
        </div>
      </section>

      <AppButton
        type="submit"
        variant="primary"
        size="lg"
        block
        :loading="authStore.isLoading"
      >
        <template #icon-left><UserPlus :size="16" /></template>
        {{ authStore.isLoading ? 'Creando cuenta…' : 'Crear cuenta' }}
      </AppButton>

      <p class="legal-note">
        Al crear una cuenta aceptas nuestros términos de servicio y política de privacidad.
      </p>
    </form>

    <div class="register-block">
      <span class="register-text">¿Ya tienes una cuenta?</span>
      <RouterLink to="/auth/login" class="link-register">Inicia sesión</RouterLink>
    </div>
  </AuthShell>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { UserPlus, User, CreditCard, Phone } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth.store'

import AuthShell from './AuthShell.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppFormField from '@/components/ui/AppFormField.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppPasswordField from '@/components/ui/AppPasswordField.vue'

const authStore = useAuthStore()
const router = useRouter()

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
  Object.keys(errors).forEach((k) => (errors[k] = false))
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
    if (msg.includes('documento') || msg.includes('cédula') || msg.includes('cedula'))
      errors.documentoIdentidad = true
    if (msg.includes('nombre')) errors.nombreCompleto = true
  }
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

.form-section {
  padding-bottom: var(--space-5);
  margin-bottom: var(--space-5);
  border-bottom: 1px solid var(--border-subtle);
}
.form-section:last-of-type {
  border-bottom: none;
  padding-bottom: 0;
  margin-bottom: var(--space-2);
}
.section-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin: 0 0 var(--space-4);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--brand-700);
}
.form-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}
.form-grid-full { grid-column: 1 / -1; }
.form-grid-2 :deep(.form-group),
.form-grid-full :deep(.form-group) { margin-bottom: 0; }

.legal-note {
  margin: var(--space-4) 0 0;
  font-size: var(--text-xs);
  line-height: var(--leading-normal);
  color: var(--text-tertiary);
  text-align: center;
}

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

@media (max-width: 560px) {
  .form-grid-2 { grid-template-columns: 1fr; gap: var(--space-3); }
  .form-section { padding-bottom: var(--space-4); margin-bottom: var(--space-4); }
}
</style>