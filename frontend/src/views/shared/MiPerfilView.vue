<template>
  <div class="perfil-view">
    <ToastContainer />

    <!-- ═══ LOADING ═══ -->
    <div v-if="cargando" class="skeleton-wrap">
      <div class="skeleton-hero">
        <div class="skeleton sk-avatar" />
        <div class="sk-lines">
          <div class="skeleton sk-line-xl" />
          <div class="skeleton sk-line-md" />
          <div class="skeleton sk-line-sm" />
        </div>
      </div>
      <div class="grid-2">
        <div class="card card-skeleton">
          <div class="skeleton sk-card-title" />
          <div v-for="i in 4" :key="i" class="skeleton sk-row" />
        </div>
        <div class="card card-skeleton">
          <div class="skeleton sk-card-title" />
          <div v-for="i in 3" :key="i" class="skeleton sk-row" />
        </div>
      </div>
    </div>

    <!-- ═══ ERROR ═══ -->
    <div v-else-if="error" class="state-full">
      <div class="state-error-icon"><AlertTriangle :size="40" /></div>
      <h2 class="state-title">No pudimos cargar tu perfil</h2>
      <p class="state-text">{{ error }}</p>
      <AppButton variant="primary" @click="cargar">
        <template #icon-left><RefreshCw :size="15" /></template>
        Reintentar
      </AppButton>
    </div>

    <!-- ═══ PERFIL ═══ -->
    <template v-else-if="perfil">
      <!-- ═══ HERO ═══ -->
      <header class="hero">
        <div class="hero-avatar" :class="`tone-${tone}`">
          <span class="hero-avatar-initials">{{ initials }}</span>
        </div>

        <div class="hero-info">
          <span class="hero-rol-pill" :class="`tone-${tone}`">
            {{ formatRol(perfil.rol) }}
          </span>
          <h1 class="hero-name">{{ perfil.nombreCompleto }}</h1>
          <div class="hero-meta">
            <span class="hero-meta-item">
              <Mail :size="13" />
              {{ perfil.correoElectronico }}
            </span>
            <span v-if="perfil.fechaRegistro" class="hero-meta-item">
              <Calendar :size="13" />
              Miembro desde {{ formatearFecha(perfil.fechaRegistro) }}
            </span>
          </div>
        </div>
      </header>

      <!-- ═══ GRID: Datos + Info ═══ -->
      <div class="grid-2">
        <!-- ══════════════ DATOS PERSONALES ══════════════ -->
        <section v-if="esCliente" class="card">
          <header class="card-header">
            <div class="card-header-icon">
              <UserCircle :size="16" />
            </div>
            <div class="card-header-text">
              <h3>Datos personales</h3>
              <p class="card-sub">Puedes actualizar tu información de contacto</p>
            </div>
            <AppButton
              v-if="!editando"
              variant="secondary"
              size="sm"
              @click="entrarEdicion"
            >
              <template #icon-left><Pencil :size="14" /></template>
              Editar
            </AppButton>
          </header>

          <div class="card-body">
            <!-- Modo lectura -->
            <ul v-if="!editando" class="info-list">
              <li class="info-row">
                <span class="info-label">Documento</span>
                <span class="info-value mono">{{ perfil.cliente?.documentoIdentidad || '—' }}</span>
              </li>
              <li class="info-row">
                <span class="info-label">Teléfono principal</span>
                <span class="info-value">{{ perfil.cliente?.telefonoPrincipal || '—' }}</span>
              </li>
              <li class="info-row">
                <span class="info-label">Teléfono secundario</span>
                <span class="info-value">{{ perfil.cliente?.telefonoSecundario || '—' }}</span>
              </li>
              <li class="info-row">
                <span class="info-label">Dirección</span>
                <span class="info-value">{{ perfil.cliente?.direccion || '—' }}</span>
              </li>
              <li class="info-row">
                <span class="info-label">Ciudad</span>
                <span class="info-value">{{ perfil.cliente?.ciudad || '—' }}</span>
              </li>
              <li class="info-row">
                <span class="info-label">Fecha de nacimiento</span>
                <span class="info-value">{{ formatearFechaSolo(perfil.cliente?.fechaNacimiento) }}</span>
              </li>
            </ul>

            <!-- Modo edición -->
            <form v-else class="edit-form" @submit.prevent="guardar">
              <AppFormField
                label="Nombre completo"
                :error="errores.nombreCompleto"
                required
              >
                <template #default="{ id, invalid }">
                  <AppInput
                    :id="id"
                    v-model="form.nombreCompleto"
                    placeholder="Tu nombre completo"
                    :error="invalid"
                    maxlength="150"
                  />
                </template>
              </AppFormField>

              <div class="edit-grid-2">
                <AppFormField
                  label="Teléfono principal"
                  :error="errores.telefonoPrincipal"
                  required
                >
                  <template #default="{ id, invalid }">
                    <AppInput
                      :id="id"
                      v-model="form.telefonoPrincipal"
                      placeholder="0412-1234567"
                      :error="invalid"
                    />
                  </template>
                </AppFormField>

                <AppFormField label="Teléfono secundario" optional>
                  <template #default="{ id }">
                    <AppInput
                      :id="id"
                      v-model="form.telefonoSecundario"
                      placeholder="Opcional"
                    />
                  </template>
                </AppFormField>
              </div>

              <AppFormField label="Dirección" optional>
                <template #default="{ id }">
                  <AppInput
                    :id="id"
                    v-model="form.direccion"
                    placeholder="Av. Principal, Casa 5, Sector Centro"
                  />
                </template>
              </AppFormField>

              <div class="edit-grid-2">
                <AppFormField label="Ciudad" optional>
                  <template #default="{ id }">
                    <AppInput
                      :id="id"
                      v-model="form.ciudad"
                      placeholder="Ej: Barcelona"
                    />
                  </template>
                </AppFormField>

                <AppFormField label="Fecha de nacimiento" optional>
                  <template #default="{ id }">
                    <AppInput
                      :id="id"
                      v-model="form.fechaNacimiento"
                      type="date"
                    />
                  </template>
                </AppFormField>
              </div>

              <div class="edit-actions">
                <AppButton
                  variant="secondary"
                  type="button"
                  :disabled="guardando"
                  @click="cancelarEdicion"
                >
                  Cancelar
                </AppButton>
                <AppButton
                  variant="primary"
                  type="submit"
                  :loading="guardando"
                >
                  <template #icon-left><Save :size="15" /></template>
                  {{ guardando ? 'Guardando…' : 'Guardar cambios' }}
                </AppButton>
              </div>
            </form>
          </div>
        </section>

        <!-- ══════════════ INFO PROFESIONAL (solo personal) ══════════════ -->
        <section v-if="esPersonal" class="card">
          <header class="card-header">
            <div class="card-header-icon card-header-icon--info">
              <Briefcase :size="16" />
            </div>
            <div class="card-header-text">
              <h3>Información profesional</h3>
              <p class="card-sub">Datos laborales asignados por administración</p>
            </div>
          </header>

          <div class="card-body">
            <ul class="info-list">
              <li class="info-row">
                <span class="info-label">Código de empleado</span>
                <span class="info-value mono">{{ perfil.personal?.codigoEmpleado || '—' }}</span>
              </li>
              <li class="info-row">
                <span class="info-label">Cargo</span>
                <span class="info-value">
                  <span class="cargo-pill" :data-cargo="perfil.personal?.cargo">
                    {{ formatCargo(perfil.personal?.cargo) }}
                  </span>
                </span>
              </li>
              <li v-if="perfil.personal?.especialidad" class="info-row">
                <span class="info-label">Especialidad</span>
                <span class="info-value">{{ perfil.personal.especialidad }}</span>
              </li>
              <li v-if="perfil.personal?.licenciaProfesional" class="info-row">
                <span class="info-label">Licencia profesional</span>
                <span class="info-value mono">{{ perfil.personal.licenciaProfesional }}</span>
              </li>
              <li class="info-row">
                <span class="info-label">Fecha de contratación</span>
                <span class="info-value">{{ formatearFechaSolo(perfil.personal?.fechaContratacion) }}</span>
              </li>
              <li class="info-row">
                <span class="info-label">Estado</span>
                <span class="info-value">
                  <span class="badge" :class="perfil.personal?.activo ? 'badge-success' : 'badge-danger'">
                    {{ perfil.personal?.activo ? 'Activo' : 'Inactivo' }}
                  </span>
                </span>
              </li>
            </ul>

            <!-- Horario de atención -->
            <div v-if="horarioResumen" class="horario-block">
              <p class="horario-title">
                <Clock :size="13" />
                Horario de atención
              </p>
              <ul class="horario-list">
                <li v-for="h in horarioResumen" :key="h.dia" class="horario-item">
                  <span class="horario-dia">{{ h.dia }}</span>
                  <span class="horario-bloques">{{ h.bloques }}</span>
                </li>
              </ul>
            </div>
          </div>
        </section>

        <!-- ══════════════ SEGURIDAD ══════════════ -->
        <section class="card" :class="{ 'card-wide': !esPersonal }">
          <header class="card-header">
            <div class="card-header-icon card-header-icon--warning">
              <ShieldCheck :size="16" />
            </div>
            <div class="card-header-text">
              <h3>Seguridad</h3>
              <p class="card-sub">Protege tu cuenta con una contraseña segura</p>
            </div>
          </header>

          <div class="card-body">
            <ul class="info-list">
              <li class="info-row">
                <span class="info-label">Contraseña</span>
                <span class="info-value">••••••••••••</span>
              </li>
              <li class="info-row">
                <span class="info-label">Último acceso</span>
                <span class="info-value">
                  {{ perfil.ultimoAcceso ? formatearFechaHora(perfil.ultimoAcceso) : '—' }}
                </span>
              </li>
            </ul>

            <div class="seguridad-actions">
              <AppButton variant="primary" @click="abrirCambiarContrasena">
                <template #icon-left><KeyRound :size="15" /></template>
                Cambiar contraseña
              </AppButton>
            </div>
          </div>
        </section>
      </div>
    </template>

    <!-- ═══ MODAL CAMBIAR CONTRASEÑA ═══ -->
    <AppModal
      :model-value="modalPasswordVisible"
      title="Cambiar contraseña"
      subtitle="Verifica tu identidad con la contraseña actual"
      size="md"
      :loading="cambiandoPassword"
      @update:model-value="cerrarModalPassword"
    >
      <div class="modal-password">
        <AppAlert v-if="passwordError" variant="error">{{ passwordError }}</AppAlert>

        <AppFormField
          label="Contraseña actual"
          :error="passwordErrores.actual"
          required
        >
          <template #default="{ id, invalid }">
            <AppPasswordField
              :id="id"
              v-model="passwordForm.contrasenaActual"
              placeholder="Tu contraseña actual"
              :error="invalid"
              autocomplete="current-password"
            />
          </template>
        </AppFormField>

        <AppFormField
          label="Nueva contraseña"
          :error="passwordErrores.nueva"
          hint="Mínimo 8 caracteres"
          required
        >
          <template #default="{ id, invalid }">
            <AppPasswordField
              :id="id"
              v-model="passwordForm.contrasenaNueva"
              placeholder="Mínimo 8 caracteres"
              :error="invalid"
              autocomplete="new-password"
            />
          </template>
        </AppFormField>

        <!-- Indicador de fuerza -->
        <Transition name="slide-down">
          <div v-if="passwordForm.contrasenaNueva" class="strength-block">
            <div class="strength-bar">
              <div
                class="strength-fill"
                :class="`is-${fuerzaPassword.nivel}`"
                :style="{ width: fuerzaPassword.pct + '%' }"
              />
            </div>
            <span class="strength-text" :class="`is-${fuerzaPassword.nivel}`">
              {{ fuerzaPassword.texto }}
            </span>
          </div>
        </Transition>

        <AppFormField
          label="Confirmar nueva contraseña"
          :error="passwordErrores.confirmar"
          required
        >
          <template #default="{ id, invalid }">
            <div class="confirm-wrap">
              <AppPasswordField
                :id="id"
                v-model="passwordForm.confirmarContrasena"
                placeholder="Repite la nueva contraseña"
                :error="invalid"
                autocomplete="new-password"
              />
              <span
                v-if="passwordForm.confirmarContrasena && coincidenPassword"
                class="confirm-check"
                aria-label="Las contraseñas coinciden"
              >
                <Check :size="13" />
              </span>
            </div>
          </template>
        </AppFormField>
      </div>

      <template #footer>
        <AppButton
          variant="secondary"
          :disabled="cambiandoPassword"
          @click="cerrarModalPassword"
        >
          Cancelar
        </AppButton>
        <AppButton
          variant="primary"
          :loading="cambiandoPassword"
          :disabled="!puedeCambiarPassword"
          @click="confirmarCambioPassword"
        >
          <template #icon-left><ShieldCheck :size="15" /></template>
          {{ cambiandoPassword ? 'Cambiando…' : 'Cambiar contraseña' }}
        </AppButton>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import {
  Mail, Calendar, UserCircle, Briefcase, ShieldCheck, KeyRound,
  Pencil, Save, AlertTriangle, RefreshCw, Clock, Check,
} from 'lucide-vue-next'
import { getPerfil, actualizarPerfil, cambiarContrasena } from '@/api/perfil.api'
import { getApiErrorMessage, getValidationFieldErrors } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'
import { useAuthStore } from '@/stores/auth.store'

import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppFormField from '@/components/ui/AppFormField.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppPasswordField from '@/components/ui/AppPasswordField.vue'
import AppModal from '@/components/ui/AppModal.vue'
import AppAlert from '@/components/ui/AppAlert.vue'

const authStore = useAuthStore()
const { toastSuccess, toastError } = useToast()

/* ═══════════════════════════════════════════════════════════════
   ESTADO
   ═══════════════════════════════════════════════════════════════ */
const cargando = ref(true)
const error = ref('')
const perfil = ref(null)

const editando = ref(false)
const guardando = ref(false)
const errores = reactive({
  nombreCompleto: '',
  telefonoPrincipal: '',
})

const form = reactive({
  nombreCompleto: '',
  telefonoPrincipal: '',
  telefonoSecundario: '',
  direccion: '',
  ciudad: '',
  fechaNacimiento: '',
})

const modalPasswordVisible = ref(false)
const cambiandoPassword = ref(false)
const passwordError = ref('')
const passwordErrores = reactive({ actual: '', nueva: '', confirmar: '' })
const passwordForm = reactive({
  contrasenaActual: '',
  contrasenaNueva: '',
  confirmarContrasena: '',
})

/* ═══════════════════════════════════════════════════════════════
   COMPUTED
   ═══════════════════════════════════════════════════════════════ */
const esCliente = computed(() => perfil.value?.tipoUsuario === 'Cliente')
const esPersonal = computed(() => perfil.value?.tipoUsuario === 'Personal')

const initials = computed(() => {
  const name = perfil.value?.nombreCompleto || 'U'
  return name.split(' ').filter(Boolean).map((n) => n[0]).join('').substring(0, 2).toUpperCase()
})

const tone = computed(() => {
  const map = {
    Cliente: 'brand',
    Recepcionista: 'info',
    Veterinario: 'success',
    Encargado_Almacen: 'warning',
    Administrador: 'purple',
  }
  return map[perfil.value?.rol] || 'brand'
})

const horarioResumen = computed(() => {
  const h = perfil.value?.personal?.horarioAtencion
  if (!h || typeof h !== 'object') return null

  const DIAS = ['', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo']
  const out = []
  for (let d = 1; d <= 7; d++) {
    const bloques = h[String(d)]
    if (Array.isArray(bloques) && bloques.length > 0) {
      out.push({
        dia: DIAS[d],
        bloques: bloques.map((b) => `${b.inicio}–${b.fin}`).join(', '),
      })
    }
  }
  return out.length ? out : null
})

const coincidenPassword = computed(
  () => passwordForm.contrasenaNueva && passwordForm.contrasenaNueva === passwordForm.confirmarContrasena
)

const fuerzaPassword = computed(() => {
  const p = passwordForm.contrasenaNueva
  if (!p) return { nivel: 'vacio', texto: '', pct: 0 }
  let score = 0
  if (p.length >= 8) score++
  if (p.length >= 12) score++
  if (/[A-Z]/.test(p)) score++
  if (/[0-9]/.test(p)) score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  if (score <= 1) return { nivel: 'debil', texto: 'Débil', pct: 25 }
  if (score === 2) return { nivel: 'regular', texto: 'Regular', pct: 50 }
  if (score === 3) return { nivel: 'buena', texto: 'Buena', pct: 75 }
  return { nivel: 'fuerte', texto: 'Fuerte', pct: 100 }
})

const puedeCambiarPassword = computed(() =>
  passwordForm.contrasenaActual.trim().length > 0
  && passwordForm.contrasenaNueva.length >= 8
  && coincidenPassword.value
)

/* ═══════════════════════════════════════════════════════════════
   CARGA
   ═══════════════════════════════════════════════════════════════ */
async function cargar() {
  cargando.value = true
  error.value = ''
  try {
    const { data } = await getPerfil()
    perfil.value = data
  } catch (err) {
    error.value = getApiErrorMessage(err) || 'Error al cargar tu perfil.'
  } finally {
    cargando.value = false
  }
}

onMounted(cargar)

/* ═══════════════════════════════════════════════════════════════
   EDICIÓN
   ═══════════════════════════════════════════════════════════════ */
function entrarEdicion() {
  const c = perfil.value?.cliente
  form.nombreCompleto = perfil.value?.nombreCompleto || ''
  form.telefonoPrincipal = c?.telefonoPrincipal || ''
  form.telefonoSecundario = c?.telefonoSecundario || ''
  form.direccion = c?.direccion || ''
  form.ciudad = c?.ciudad || ''
  form.fechaNacimiento = c?.fechaNacimiento || ''
  errores.nombreCompleto = ''
  errores.telefonoPrincipal = ''
  editando.value = true
}

function cancelarEdicion() {
  editando.value = false
}

async function guardar() {
  errores.nombreCompleto = ''
  errores.telefonoPrincipal = ''

  if (!form.nombreCompleto.trim() || form.nombreCompleto.trim().length < 3) {
    errores.nombreCompleto = 'El nombre debe tener al menos 3 caracteres.'
    return
  }
  if (!form.telefonoPrincipal.trim()) {
    errores.telefonoPrincipal = 'El teléfono principal es obligatorio.'
    return
  }

  guardando.value = true
  try {
    const { data } = await actualizarPerfil({
      nombreCompleto: form.nombreCompleto.trim(),
      telefonoPrincipal: form.telefonoPrincipal.trim(),
      telefonoSecundario: form.telefonoSecundario.trim() || null,
      direccion: form.direccion.trim() || null,
      ciudad: form.ciudad.trim() || null,
      fechaNacimiento: form.fechaNacimiento || null,
    })
    perfil.value = data
    editando.value = false
    toastSuccess('Perfil actualizado correctamente')

    // Refrescar el nombre en authStore para que el Sidebar y TopNavbar lo reflejen
    if (authStore.user) {
      authStore.user.nombreCompleto = data.nombreCompleto
    }
  } catch (err) {
    const fieldErrors = getValidationFieldErrors(err)
    if (fieldErrors) {
      errores.nombreCompleto = fieldErrors.nombreCompleto || ''
      errores.telefonoPrincipal = fieldErrors.telefonoPrincipal || ''
    } else {
      toastError(getApiErrorMessage(err))
    }
  } finally {
    guardando.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   CAMBIO DE CONTRASEÑA
   ═══════════════════════════════════════════════════════════════ */
function abrirCambiarContrasena() {
  passwordForm.contrasenaActual = ''
  passwordForm.contrasenaNueva = ''
  passwordForm.confirmarContrasena = ''
  passwordErrores.actual = ''
  passwordErrores.nueva = ''
  passwordErrores.confirmar = ''
  passwordError.value = ''
  modalPasswordVisible.value = true
}

function cerrarModalPassword() {
  if (cambiandoPassword.value) return
  modalPasswordVisible.value = false
}

async function confirmarCambioPassword() {
  passwordErrores.actual = ''
  passwordErrores.nueva = ''
  passwordErrores.confirmar = ''
  passwordError.value = ''

  if (!passwordForm.contrasenaActual) {
    passwordErrores.actual = 'Ingresa tu contraseña actual.'
    return
  }
  if (passwordForm.contrasenaNueva.length < 8) {
    passwordErrores.nueva = 'La nueva contraseña debe tener al menos 8 caracteres.'
    return
  }
  if (!coincidenPassword.value) {
    passwordErrores.confirmar = 'Las contraseñas no coinciden.'
    return
  }

  cambiandoPassword.value = true
  try {
    await cambiarContrasena({
      contrasenaActual: passwordForm.contrasenaActual,
      contrasenaNueva: passwordForm.contrasenaNueva,
    })
    modalPasswordVisible.value = false
    toastSuccess('Contraseña cambiada correctamente')
  } catch (err) {
    const msg = getApiErrorMessage(err) || 'No se pudo cambiar la contraseña.'
    if (msg.toLowerCase().includes('actual')) {
      passwordErrores.actual = msg
    } else {
      passwordError.value = msg
    }
  } finally {
    cambiandoPassword.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   HELPERS
   ═══════════════════════════════════════════════════════════════ */
const MESES = ['enero','febrero','marzo','abril','mayo','junio','julio','agosto','septiembre','octubre','noviembre','diciembre']

function formatearFecha(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  return `${d.getDate()} de ${MESES[d.getMonth()]} de ${d.getFullYear()}`
}

function formatearFechaSolo(iso) {
  if (!iso) return '—'
  const [y, m, d] = String(iso).slice(0, 10).split('-')
  return `${d} ${MESES[Number(m) - 1]?.slice(0, 3)} ${y}`
}

function formatearFechaHora(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleString('es-VE', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

function formatRol(rol) {
  const map = {
    Cliente: 'Cliente',
    Recepcionista: 'Recepcionista',
    Veterinario: 'Veterinario',
    Encargado_Almacen: 'Encargado de Almacén',
    Administrador: 'Administrador',
  }
  return map[rol] || rol
}

function formatCargo(cargo) {
  const map = {
    Veterinario: 'Veterinario',
    Recepcionista: 'Recepcionista',
    Encargado_Almacen: 'Encargado de Almacén',
    Administrador: 'Administrador',
  }
  return map[cargo] || cargo || '—'
}
</script>

<style scoped>
.perfil-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-7) var(--space-12);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* ═══════════════════════════════════════════════════════════════
   HERO
   ═══════════════════════════════════════════════════════════════ */
.hero {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: var(--space-6);
  align-items: center;
  padding: var(--space-6) var(--space-7);
  background: linear-gradient(135deg, var(--brand-50) 0%, var(--bg-surface) 65%);
  border: 1px solid var(--brand-100);
  border-radius: var(--radius-3xl);
}

.hero-avatar {
  --tone-fg: var(--text-inverse);
  --tone-bg: var(--brand-700);
  width: 96px;
  height: 96px;
  border-radius: var(--radius-3xl);
  background: var(--tone-bg);
  color: var(--tone-fg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 8px 20px -8px rgba(15, 23, 42, 0.30);
}
.hero-avatar.tone-brand   { --tone-bg: var(--brand-700); }
.hero-avatar.tone-info    { --tone-bg: var(--info-600); }
.hero-avatar.tone-success { --tone-bg: var(--success-600); }
.hero-avatar.tone-warning { --tone-bg: var(--warning-600); }
.hero-avatar.tone-purple  { --tone-bg: var(--purple-600); }

.hero-avatar-initials {
  font-size: 36px;
  font-weight: var(--font-extrabold);
  letter-spacing: -0.02em;
}

.hero-info { min-width: 0; }

.hero-rol-pill {
  display: inline-block;
  padding: 3px var(--space-3);
  margin-bottom: var(--space-3);
  border-radius: var(--radius-full);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
}
.hero-rol-pill.tone-brand   { color: var(--brand-700);   border-color: var(--brand-200); }
.hero-rol-pill.tone-info    { color: var(--info-600);    border-color: var(--info-200); }
.hero-rol-pill.tone-success { color: var(--success-600); border-color: var(--success-200); }
.hero-rol-pill.tone-warning { color: var(--warning-600); border-color: var(--warning-200); }
.hero-rol-pill.tone-purple  { color: var(--purple-600);  border-color: var(--purple-100); }

.hero-name {
  margin: 0 0 var(--space-3);
  font-size: var(--text-5xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
  line-height: 1.15;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  font-size: var(--text-md);
  color: var(--text-secondary);
}
.hero-meta-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
}
.hero-meta-item svg { color: var(--text-tertiary); }

/* ═══════════════════════════════════════════════════════════════
   GRID
   ═══════════════════════════════════════════════════════════════ */
.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-5);
  align-items: start;
}
.card-wide { grid-column: 1 / -1; }

/* ═══════════════════════════════════════════════════════════════
   CARD
   ═══════════════════════════════════════════════════════════════ */
.card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
  overflow: hidden;
}
.card-header {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-3);
  align-items: center;
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-subtle);
}
.card-header-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  background: var(--brand-50);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.card-header-icon--info    { background: var(--info-50);    color: var(--info-600); }
.card-header-icon--warning { background: var(--warning-50); color: var(--warning-600); }

.card-header-text { min-width: 0; }
.card-header h3 {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
.card-sub {
  margin: 2px 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-snug);
}
.card-body { padding: var(--space-2) var(--space-6) var(--space-5); }

/* ═══════════════════════════════════════════════════════════════
   INFO LIST
   ═══════════════════════════════════════════════════════════════ */
.info-list { list-style: none; margin: 0; padding: 0; }
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-subtle);
  font-size: var(--text-md);
}
.info-row:last-child { border-bottom: none; }
.info-label {
  color: var(--text-secondary);
  font-weight: var(--font-medium);
  flex-shrink: 0;
}
.info-value {
  color: var(--text-primary);
  font-weight: var(--font-bold);
  text-align: right;
  word-break: break-word;
  min-width: 0;
}
.info-value.mono {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  background: var(--neutral-100);
  padding: 2px var(--space-3);
  border-radius: var(--radius-sm);
  color: var(--neutral-700);
}

.cargo-pill {
  display: inline-block;
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  background: var(--neutral-100);
  color: var(--neutral-600);
  border: 1px solid var(--border-subtle);
  white-space: nowrap;
}
.cargo-pill[data-cargo="Veterinario"]       { background: var(--info-50);    color: var(--info-700);    border-color: var(--info-200); }
.cargo-pill[data-cargo="Recepcionista"]     { background: var(--purple-50);  color: var(--purple-600);  border-color: var(--purple-100); }
.cargo-pill[data-cargo="Encargado_Almacen"] { background: var(--warning-50); color: var(--warning-700); border-color: var(--warning-200); }
.cargo-pill[data-cargo="Administrador"]     { background: var(--brand-50);   color: var(--brand-700);   border-color: var(--brand-200); }

/* ═══════════════════════════════════════════════════════════════
   HORARIO
   ═══════════════════════════════════════════════════════════════ */
.horario-block {
  margin-top: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}
.horario-title {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}
.horario-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}
.horario-item {
  display: grid;
  grid-template-columns: 90px 1fr;
  gap: var(--space-3);
  font-size: var(--text-sm);
}
.horario-dia {
  font-weight: var(--font-bold);
  color: var(--neutral-700);
}
.horario-bloques {
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
}

/* ═══════════════════════════════════════════════════════════════
   SEGURIDAD
   ═══════════════════════════════════════════════════════════════ */
.seguridad-actions {
  margin-top: var(--space-4);
  display: flex;
  justify-content: flex-end;
}

/* ═══════════════════════════════════════════════════════════════
   FORMULARIO DE EDICIÓN
   ═══════════════════════════════════════════════════════════════ */
.edit-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding-top: var(--space-2);
}
.edit-form :deep(.form-group) { margin-bottom: var(--space-2); }

.edit-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}
.edit-grid-2 :deep(.form-group) { margin-bottom: 0; }

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-subtle);
}

/* ═══════════════════════════════════════════════════════════════
   MODAL PASSWORD
   ═══════════════════════════════════════════════════════════════ */
.modal-password {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.strength-block {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-top: calc(var(--space-4) * -1);
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
  pointer-events: none;
  animation: popIn 0.25s var(--ease-spring);
}
@keyframes popIn {
  0%   { transform: translateY(-50%) scale(0); }
  100% { transform: translateY(-50%) scale(1); }
}

/* ═══════════════════════════════════════════════════════════════
   ESTADOS
   ═══════════════════════════════════════════════════════════════ */
.state-full {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-16) var(--space-6);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  text-align: center;
  min-height: 320px;
  color: var(--text-secondary);
}
.state-error-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--danger-50);
  color: var(--danger-600);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
}
.state-title {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}
.state-text {
  margin: 0 0 var(--space-3);
  font-size: var(--text-md);
  color: var(--text-secondary);
  max-width: 460px;
  line-height: var(--leading-relaxed);
}

/* ═══════════════════════════════════════════════════════════════
   SKELETON
   ═══════════════════════════════════════════════════════════════ */
.skeleton-wrap { display: flex; flex-direction: column; gap: var(--space-5); }

.skeleton {
  background: linear-gradient(90deg, var(--neutral-100) 25%, var(--neutral-200) 50%, var(--neutral-100) 75%);
  background-size: 200% 100%;
  border-radius: var(--radius-md);
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.skeleton-hero {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: var(--space-6);
  align-items: center;
  padding: var(--space-6) var(--space-7);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-3xl);
}
.sk-avatar { width: 96px; height: 96px; border-radius: var(--radius-3xl); }
.sk-lines { display: flex; flex-direction: column; gap: var(--space-3); }
.sk-line-xl { height: 24px; width: 55%; }
.sk-line-md { height: 14px; width: 70%; }
.sk-line-sm { height: 12px; width: 45%; }
.card-skeleton {
  padding: var(--space-5) var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.sk-card-title { height: 18px; width: 50%; margin-bottom: var(--space-2); }
.sk-row { height: 40px; width: 100%; border-radius: var(--radius-lg); }

/* ═══════════════════════════════════════════════════════════════
   TRANSICIONES
   ═══════════════════════════════════════════════════════════════ */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: opacity var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
}
.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ═══════════════════════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════════════════════ */
@media (max-width: 1024px) {
  .grid-2 { grid-template-columns: 1fr; }
  .card-wide { grid-column: 1; }
  .hero { grid-template-columns: 1fr; text-align: center; gap: var(--space-4); }
  .hero-avatar { justify-self: center; width: 84px; height: 84px; }
  .hero-avatar-initials { font-size: 30px; }
  .hero-meta { justify-content: center; }
}

@media (max-width: 768px) {
  .perfil-view { padding: var(--space-5) var(--space-4) var(--space-10); }
  .hero { padding: var(--space-5); border-radius: var(--radius-2xl); }
  .hero-name { font-size: var(--text-4xl); }

  .card-header { padding: var(--space-4) var(--space-5); }
  .card-body { padding: var(--space-2) var(--space-5) var(--space-4); }

  .edit-grid-2 { grid-template-columns: 1fr; }
  .edit-actions { flex-direction: column-reverse; }
  .edit-actions :deep(.btn) { width: 100%; }
}

@media (max-width: 480px) {
  .perfil-view { padding: var(--space-4) var(--space-3) var(--space-8); }
  .hero-name { font-size: var(--text-3xl); }
  .info-row { flex-direction: column; align-items: flex-start; gap: var(--space-1); }
  .info-value { text-align: left; }
  .seguridad-actions { justify-content: stretch; }
  .seguridad-actions :deep(.btn) { width: 100%; }
}
</style>