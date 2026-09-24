<template>
  <div class="nuevo-cliente">
    <!-- ═══ BREADCRUMB ═══ -->
    <nav class="breadcrumb" aria-label="Migas de pan">
      <button class="bc-back" type="button" @click="cancelar">
        <ArrowLeft :size="15" />
      </button>
      <button class="bc-item bc-link" type="button" @click="cancelar">
        Gestión de Clientes
      </button>
      <ChevronRight :size="14" class="bc-sep" />
      <span class="bc-item bc-current">Nuevo Cliente</span>
    </nav>

    <!-- ═══ HERO ═══ -->
    <header class="hero">
      <p class="hero-eyebrow">
        <Sparkles :size="12" />
        Recepción · Registro asistido
      </p>
      <h1>Añadir Nuevo Cliente</h1>
      <p class="hero-sub">
        Completa los datos del cliente. Recibirá un enlace por correo para definir su contraseña.
      </p>
    </header>

    <!-- ═══ LAYOUT ═══ -->
    <div class="wizard-layout">
      <!-- ═══ FORMULARIO ═══ -->
      <main class="wizard-main">
        <form @submit.prevent="handleRegistrar" novalidate>
          <!-- Alerta general -->
          <Transition name="slide-fade">
            <div v-if="mostrarError" class="alert alert-error">
              <AlertCircle :size="16" />
              <span>{{ errorMessage }}</span>
            </div>
          </Transition>

          <!-- Card: Identificación -->
          <section class="card">
            <header class="card-header">
              <div class="card-header-left">
                <div class="card-icon"><UserCircle :size="16" /></div>
                <div>
                  <h3>Identificación</h3>
                  <p class="card-header-sub">Datos personales del cliente</p>
                </div>
              </div>
            </header>
            <div class="card-body">
              <div class="form-grid">
                <div class="form-group full-width" :class="{ 'has-error': errors.nombreCompleto }">
                  <label class="form-label" for="nombre">
                    Nombre completo <span class="required">*</span>
                  </label>
                  <input
                    id="nombre"
                    v-model="form.nombreCompleto"
                    type="text"
                    class="form-input"
                    :class="{ 'is-invalid': errors.nombreCompleto }"
                    placeholder="Ej: María Alejandra González"
                    :disabled="isLoading"
                    autocomplete="name"
                  />
                  <span v-if="errors.nombreCompleto" class="form-error">
                    Ingresa el nombre completo (mínimo 3 caracteres).
                  </span>
                </div>

                <div class="form-group">
                  <label class="form-label" for="documento">
                    Cédula / Documento <span class="required">*</span>
                  </label>
                  <input
                    id="documento"
                    v-model="form.documentoIdentidad"
                    type="text"
                    class="form-input"
                    :class="{ 'is-invalid': errors.documentoIdentidad }"
                    placeholder="V-12345678"
                    :disabled="isLoading"
                    autocomplete="off"
                  />
                  <span v-if="errors.documentoIdentidad" class="form-error">
                    Documento requerido (mínimo 6 caracteres).
                  </span>
                </div>

                <div class="form-group">
                  <label class="form-label" for="nacimiento">
                    Fecha de nacimiento <span class="optional">(opcional)</span>
                  </label>
                  <input
                    id="nacimiento"
                    v-model="form.fechaNacimiento"
                    type="date"
                    class="form-input"
                    :disabled="isLoading"
                  />
                </div>
              </div>
            </div>
          </section>

          <!-- Card: Contacto -->
          <section class="card">
            <header class="card-header">
              <div class="card-header-left">
                <div class="card-icon"><AtSign :size="16" /></div>
                <div>
                  <h3>Contacto</h3>
                  <p class="card-header-sub">Correo y teléfonos del cliente</p>
                </div>
              </div>
            </header>
            <div class="card-body">
              <div class="form-grid">
                <div class="form-group full-width" :class="{ 'has-error': errors.correoElectronico }">
                  <label class="form-label" for="correo">
                    Correo electrónico <span class="required">*</span>
                  </label>
                  <input
                    id="correo"
                    v-model="form.correoElectronico"
                    type="email"
                    class="form-input"
                    :class="{ 'is-invalid': errors.correoElectronico }"
                    placeholder="usuario@ejemplo.com"
                    :disabled="isLoading"
                    autocomplete="email"
                  />
                  <span v-if="errors.correoElectronico" class="form-error">
                    Ingresa un correo válido (se usará para enviar la invitación).
                  </span>
                  <span v-else class="form-hint">
                    <Mail :size="11" />
                    Aquí llegará el enlace para que el cliente defina su contraseña.
                  </span>
                </div>

                <div class="form-group" :class="{ 'has-error': errors.telefonoPrincipal }">
                  <label class="form-label" for="tel1">
                    Teléfono principal <span class="required">*</span>
                  </label>
                  <input
                    id="tel1"
                    v-model="form.telefonoPrincipal"
                    type="tel"
                    class="form-input"
                    :class="{ 'is-invalid': errors.telefonoPrincipal }"
                    placeholder="0412-1234567"
                    :disabled="isLoading"
                    autocomplete="tel"
                  />
                  <span v-if="errors.telefonoPrincipal" class="form-error">
                    El teléfono principal es obligatorio.
                  </span>
                </div>

                <div class="form-group">
                  <label class="form-label" for="tel2">
                    Teléfono secundario <span class="optional">(opcional)</span>
                  </label>
                  <input
                    id="tel2"
                    v-model="form.telefonoSecundario"
                    type="tel"
                    class="form-input"
                    placeholder="0212-9876543"
                    :disabled="isLoading"
                  />
                </div>
              </div>
            </div>
          </section>

          <!-- Card: Ubicación -->
          <section class="card">
            <header class="card-header">
              <div class="card-header-left">
                <div class="card-icon"><MapPin :size="16" /></div>
                <div>
                  <h3>Ubicación</h3>
                  <p class="card-header-sub">Dirección de residencia</p>
                </div>
              </div>
            </header>
            <div class="card-body">
              <div class="form-grid">
                <div class="form-group full-width" :class="{ 'has-error': errors.direccion }">
                  <label class="form-label" for="direccion">
                    Dirección <span class="required">*</span>
                  </label>
                  <input
                    id="direccion"
                    v-model="form.direccion"
                    type="text"
                    class="form-input"
                    :class="{ 'is-invalid': errors.direccion }"
                    placeholder="Av. Principal, Casa 5, Sector Centro"
                    :disabled="isLoading"
                    autocomplete="street-address"
                  />
                  <span v-if="errors.direccion" class="form-error">
                    La dirección es obligatoria.
                  </span>
                </div>

                <div class="form-group">
                  <label class="form-label" for="ciudad">
                    Ciudad <span class="optional">(opcional)</span>
                  </label>
                  <input
                    id="ciudad"
                    v-model="form.ciudad"
                    type="text"
                    class="form-input"
                    placeholder="Ej: Barcelona"
                    :disabled="isLoading"
                    autocomplete="address-level2"
                  />
                </div>
              </div>
            </div>
          </section>

          <!-- Acciones (dentro del form para submit) -->
          <div class="form-actions">
            <button type="button" class="btn-cancel" :disabled="isLoading" @click="cancelar">
              <X :size="15" /> Cancelar
            </button>
            <button type="submit" class="btn-next" :disabled="isLoading">
              <Loader2 v-if="isLoading" :size="15" class="spin" />
              <UserPlus v-else :size="15" />
              {{ isLoading ? 'Registrando…' : 'Registrar cliente' }}
            </button>
          </div>
        </form>
      </main>

      <!-- ═══ SIDEBAR: VISTA PREVIA ═══ -->
      <aside class="wizard-sidebar">
        <!-- Preview cliente -->
        <div class="preview-card">
          <header class="preview-header">
            <h4>Vista previa</h4>
            <span class="preview-hint">Así se verá la ficha</span>
          </header>

          <div class="preview-body">
            <div class="preview-avatar-wrap">
              <div
                class="preview-avatar"
                :style="{ backgroundColor: colorAvatar(form.nombreCompleto || '?') }"
              >
                {{ inicialesCliente(form.nombreCompleto) || '?' }}
              </div>
            </div>

            <p class="preview-nombre" :class="{ 'is-empty': !form.nombreCompleto }">
              {{ form.nombreCompleto || 'Nombre del cliente' }}
            </p>

            <div class="preview-meta">
              <div class="preview-line" :class="{ 'is-empty': !form.documentoIdentidad }">
                <CreditCard :size="13" />
                <span>{{ form.documentoIdentidad || 'Documento pendiente' }}</span>
              </div>
              <div class="preview-line" :class="{ 'is-empty': !form.correoElectronico }">
                <Mail :size="13" />
                <span>{{ form.correoElectronico || 'Correo pendiente' }}</span>
              </div>
              <div class="preview-line" :class="{ 'is-empty': !form.telefonoPrincipal }">
                <Phone :size="13" />
                <span>{{ form.telefonoPrincipal || 'Teléfono pendiente' }}</span>
              </div>
              <div
                v-if="form.ciudad || form.direccion"
                class="preview-line"
                :class="{ 'is-empty': !(form.ciudad || form.direccion) }"
              >
                <MapPin :size="13" />
                <span>
                  {{ [form.ciudad, form.direccion].filter(Boolean).join(' · ') || 'Ubicación pendiente' }}
                </span>
              </div>
            </div>
          </div>

          <footer class="preview-footer">
            <div class="progress-top">
              <span class="progress-label">Datos obligatorios</span>
              <span class="progress-count">{{ completadosObligatorios }} / {{ totalObligatorios }}</span>
            </div>
            <div class="progress-bar">
              <div
                class="progress-fill"
                :class="{ 'is-complete': progresoObligatorios === 100 }"
                :style="{ width: progresoObligatorios + '%' }"
              />
            </div>
            <p class="progress-note">
              <component :is="progresoObligatorios === 100 ? CheckCircle2 : Info" :size="12" />
              <span v-if="progresoObligatorios === 100">
                Listo para registrar
              </span>
              <span v-else>
                Faltan {{ totalObligatorios - completadosObligatorios }} campo{{
                  totalObligatorios - completadosObligatorios === 1 ? '' : 's'
                }}
              </span>
            </p>
          </footer>
        </div>

        <!-- Nota de proceso -->
        <div class="process-note">
          <div class="process-icon"><Mail :size="16" /></div>
          <div>
            <p class="process-title">Envío automático de invitación</p>
            <p class="process-desc">
              Al registrar, el sistema enviará un correo al cliente con un enlace único para
              que defina su contraseña de acceso.
            </p>
          </div>
        </div>
      </aside>
    </div>

    <!-- ═══ MODAL DE ÉXITO ═══ -->
    <Teleport to="body">
      <Transition name="modal-fade">
        <div v-if="clienteRegistrado" class="modal-overlay">
          <Transition name="modal-slide" appear>
            <div class="modal-card">
              <div class="modal-success-icon">
                <CheckCircle2 :size="36" />
              </div>

              <h2 class="modal-title">Cliente registrado con éxito</h2>
              <p class="modal-subtitle">
                La ficha quedó guardada y lista para agendar citas.
              </p>

              <div class="modal-resumen">
                <div class="resumen-fila">
                  <span class="resumen-label">
                    <UserCircle :size="13" /> Nombre
                  </span>
                  <span class="resumen-value">{{ clienteRegistrado.nombreCompleto }}</span>
                </div>
                <div class="resumen-fila">
                  <span class="resumen-label">
                    <CreditCard :size="13" /> Documento
                  </span>
                  <span class="resumen-value mono">{{ clienteRegistrado.documentoIdentidad }}</span>
                </div>
                <div class="resumen-fila">
                  <span class="resumen-label">
                    <Mail :size="13" /> Correo
                  </span>
                  <span class="resumen-value">{{ clienteRegistrado.correoElectronico }}</span>
                </div>
              </div>

              <div
                v-if="clienteRegistrado.invitacionEnviada"
                class="alert alert-success"
              >
                <CheckCircle2 :size="16" />
                <span>Se envió un enlace al correo del cliente para que defina su contraseña de acceso.</span>
              </div>
              <div v-else class="alert alert-warning">
                <AlertCircle :size="16" />
                <span>
                  No se pudo enviar el enlace de contraseña. El cliente puede usar la opción
                  "¿Olvidaste tu contraseña?".
                </span>
              </div>

              <div class="modal-actions">
                <button class="btn-secondary" type="button" @click="volverAlListado">
                  <ArrowLeft :size="15" /> Volver al listado
                </button>
                <button class="btn-primary" type="button" @click="irRegistrarMascota">
                  <PawPrint :size="15" /> Registrar mascota ahora
                </button>
              </div>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import * as clientesApi from '@/api/clientes.api'
import {
  AlertCircle, CheckCircle2, PawPrint, ArrowLeft, ChevronRight, X,
  UserPlus, UserCircle, AtSign, Mail, Phone, MapPin, CreditCard,
  Loader2, Sparkles, Info
} from 'lucide-vue-next'

const router = useRouter()

const isLoading = ref(false)
const mostrarError = ref(false)
const errorMessage = ref('')
const clienteRegistrado = ref(null)

const errors = reactive({
  nombreCompleto: false,
  documentoIdentidad: false,
  correoElectronico: false,
  telefonoPrincipal: false,
  direccion: false,
})

const form = reactive({
  nombreCompleto: '',
  documentoIdentidad: '',
  correoElectronico: '',
  telefonoPrincipal: '',
  telefonoSecundario: '',
  direccion: '',
  ciudad: '',
  fechaNacimiento: '',
})

const PALETA_AVATARES = ['#0F766E', '#3B82F6', '#F59E0B', '#F43F5E', '#8B5CF6', '#0EA5E9']

// ─── Progreso de campos obligatorios ───
const totalObligatorios = 5

const completadosObligatorios = computed(() => {
  let count = 0
  if (form.nombreCompleto.trim().length >= 3) count++
  if (form.documentoIdentidad.trim().length >= 6) count++
  if (form.correoElectronico.trim() && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.correoElectronico)) count++
  if (form.telefonoPrincipal.trim()) count++
  if (form.direccion.trim()) count++
  return count
})

const progresoObligatorios = computed(() =>
  Math.round((completadosObligatorios.value / totalObligatorios) * 100)
)

// ─── Helpers visuales ───
function inicialesCliente(nombre) {
  const words = String(nombre || '').trim().split(/\s+/).filter(Boolean)
  if (!words.length) return ''
  if (words.length === 1) return words[0].charAt(0).toUpperCase()
  return (words[0].charAt(0) + words[words.length - 1].charAt(0)).toUpperCase()
}
function colorAvatar(nombre) {
  let hash = 0
  for (const ch of String(nombre || '')) hash = (hash * 31 + ch.charCodeAt(0)) % 997
  return PALETA_AVATARES[hash % PALETA_AVATARES.length]
}

// ─── Validación ───
function resetErrors() {
  Object.keys(errors).forEach(k => (errors[k] = false))
  mostrarError.value = false
  errorMessage.value = ''
}

function validarEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

// ─── Acciones ───
function cancelar() {
  router.push('/recepcion/clientes')
}

async function handleRegistrar() {
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
  if (!form.telefonoPrincipal.trim()) {
    errors.telefonoPrincipal = true
    hayError = true
  }
  if (!form.direccion.trim()) {
    errors.direccion = true
    hayError = true
  }

  if (hayError) {
    errorMessage.value = 'Debe completar todos los campos marcados como obligatorios.'
    mostrarError.value = true
    return
  }

  const payload = { ...form }
  if (!payload.telefonoSecundario) delete payload.telefonoSecundario
  if (!payload.ciudad) delete payload.ciudad
  if (!payload.fechaNacimiento) delete payload.fechaNacimiento

  isLoading.value = true
  try {
    const { data } = await clientesApi.registrarAsistido(payload)
    clienteRegistrado.value = data
  } catch (err) {
    mostrarError.value = true
    const codigo = err.response?.data?.error
    const mensaje = err.response?.data?.message || 'Ocurrió un error al registrar el cliente.'

    if (codigo === 'DOCUMENTO_DUPLICADO') {
      form.documentoIdentidad = ''
      errors.documentoIdentidad = true
      errorMessage.value =
        'El documento de identidad ingresado ya se encuentra registrado. Por favor, utilice el buscador para localizar el perfil del cliente.'
    } else if (codigo === 'CORREO_DUPLICADO') {
      errors.correoElectronico = true
      errorMessage.value = mensaje
    } else {
      errorMessage.value = mensaje
    }
  } finally {
    isLoading.value = false
  }
}

function irRegistrarMascota() {
  router.push({
    path: '/recepcion/registrar-mascota',
    query: {
      clienteDocumento: clienteRegistrado.value.documentoIdentidad,
      clienteNombre: clienteRegistrado.value.nombreCompleto,
    },
  })
}

function volverAlListado() {
  router.push('/recepcion/clientes')
}
</script>

<style scoped>
.nuevo-cliente {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 24px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
}
button { font-family: inherit; }

/* ═══ BREADCRUMB ═══ */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 13px;
  color: #64748B;
}
.bc-back {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: 1px solid #E2E8F0;
  background: #fff;
  color: #475569;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .15s ease;
  margin-right: 2px;
}
.bc-back:hover { background: #F1F5F9; color: #0F766E; border-color: #CBD5E1; }
.bc-item { font-weight: 500; }
.bc-link {
  background: none;
  border: none;
  color: #64748B;
  cursor: pointer;
  padding: 0;
  font-size: 13px;
}
.bc-link:hover { color: #0F766E; text-decoration: underline; }
.bc-current { color: #1E293B; font-weight: 700; }
.bc-sep { color: #CBD5E1; }

/* ═══ HERO ═══ */
.hero {
  padding: 24px 28px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
}
.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 8px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .7px;
  color: #0F766E;
  background: #fff;
  padding: 4px 10px;
  border-radius: 20px;
  border: 1px solid #CCFBF1;
}
.hero h1 {
  margin: 0 0 4px;
  font-size: 26px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.02em;
  line-height: 1.15;
}
.hero-sub {
  margin: 0;
  font-size: 14px;
  color: #64748B;
  max-width: 620px;
  line-height: 1.5;
}

/* ═══ LAYOUT ═══ */
.wizard-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 20px;
  align-items: start;
}
.wizard-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.wizard-sidebar {
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: sticky;
  top: 24px;
}

/* ═══ CARDS ═══ */
.card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  overflow: hidden;
}
.card-header {
  padding: 16px 22px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.card-header-left { display: flex; align-items: center; gap: 12px; min-width: 0; }
.card-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: #F0FDFA;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.card-header h3 {
  margin: 0;
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.2;
}
.card-header-sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748B;
}
.card-body { padding: 20px 22px 22px; }

/* ═══ FORM ═══ */
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}
.form-group { display: flex; flex-direction: column; gap: 6px; min-width: 0; }
.form-group.full-width { grid-column: 1 / -1; }
.form-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
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
.form-input {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  font-size: 14px;
  color: #1E293B;
  background: #fff;
  font-family: inherit;
  transition: border-color .2s, box-shadow .2s, background-color .2s;
  box-sizing: border-box;
}
.form-input::placeholder { color: #94A3B8; }
.form-input:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
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
  box-shadow: 0 0 0 3px rgba(239, 68, 68, .1);
}
.form-error {
  font-size: 12px;
  color: #EF4444;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 5px;
}
.form-hint {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  color: #64748B;
  line-height: 1.4;
}

/* ═══ ALERTA ═══ */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  font-weight: 500;
}
.alert-error { background: #FEF2F2; color: #991B1B; border: 1px solid #FECACA; }
.alert-warning { background: #FFFBEB; color: #92400E; border: 1px solid #FDE68A; }
.alert-success { background: #ECFDF5; color: #059669; border: 1px solid #A7F3D0; }

/* ═══ FORM ACTIONS ═══ */
.form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 22px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03);
}
.btn-cancel,
.btn-next {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 11px 22px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
}
.btn-cancel {
  background: none;
  border: 1px solid #E2E8F0;
  color: #64748B;
}
.btn-cancel:hover:not(:disabled) {
  background: #FEF2F2;
  border-color: #FECACA;
  color: #EF4444;
}
.btn-next {
  background: #0F766E;
  border: none;
  color: #fff;
}
.btn-next:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-next:disabled,
.btn-cancel:disabled { opacity: .55; cursor: not-allowed; }

/* ═══ PREVIEW CARD ═══ */
.preview-card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  overflow: hidden;
}
.preview-header {
  padding: 14px 18px 10px;
  border-bottom: 1px solid #F1F5F9;
}
.preview-header h4 {
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: #0F172A;
  text-transform: uppercase;
  letter-spacing: .5px;
}
.preview-hint {
  display: block;
  margin-top: 2px;
  font-size: 11px;
  color: #94A3B8;
}

.preview-body {
  padding: 20px 18px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
}
.preview-avatar-wrap {
  position: relative;
  margin-bottom: 4px;
}
.preview-avatar {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  box-shadow: 0 8px 20px -6px rgba(15, 23, 42, .2);
  transition: background-color .3s ease;
}
.preview-nombre {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
  line-height: 1.3;
  word-break: break-word;
}
.preview-nombre.is-empty {
  color: #94A3B8;
  font-weight: 500;
  font-style: italic;
}

.preview-meta {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 6px;
  padding-top: 14px;
  border-top: 1px solid #F1F5F9;
}
.preview-line {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: #334155;
  text-align: left;
  line-height: 1.4;
}
.preview-line svg { color: #94A3B8; flex-shrink: 0; }
.preview-line span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.preview-line.is-empty { color: #94A3B8; font-style: italic; }

.preview-footer {
  padding: 14px 18px 18px;
  background: linear-gradient(180deg, #F8FAFC 0%, #F0FDFA 100%);
  border-top: 1px solid #E2E8F0;
}
.progress-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.progress-label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
}
.progress-count {
  font-size: 12px;
  font-weight: 700;
  color: #0F766E;
  font-variant-numeric: tabular-nums;
}
.progress-bar {
  height: 6px;
  background: #E2E8F0;
  border-radius: 999px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #0F766E, #14B8A6);
  border-radius: 999px;
  transition: width .35s cubic-bezier(0.16, 1, 0.3, 1);
}
.progress-fill.is-complete {
  background: linear-gradient(90deg, #059669, #10B981);
}
.progress-note {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 10px 0 0;
  font-size: 11.5px;
  color: #64748B;
  line-height: 1.4;
}
.progress-note svg { color: #0F766E; flex-shrink: 0; }

/* ═══ NOTA DE PROCESO ═══ */
.process-note {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  background: #F0FDFA;
  border: 1px solid #CCFBF1;
  border-radius: 12px;
}
.process-icon {
  width: 32px;
  height: 32px;
  border-radius: 9px;
  background: #fff;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.process-title {
  margin: 0 0 2px;
  font-size: 12.5px;
  font-weight: 700;
  color: #0F766E;
}
.process-desc {
  margin: 0;
  font-size: 11.5px;
  color: #334155;
  line-height: 1.5;
}

/* ═══ MODAL ═══ */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, .55);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  z-index: 100;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}
.modal-card {
  background: #fff;
  border-radius: 18px;
  padding: 32px 28px 28px;
  max-width: 520px;
  width: 100%;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .3);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
}
.modal-success-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDFA 100%);
  border: 3px solid #10B981;
  color: #059669;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
  animation: successPop .5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}
.modal-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
}
.modal-subtitle {
  margin: 0 0 8px;
  font-size: 13.5px;
  color: #64748B;
  line-height: 1.5;
  max-width: 400px;
}
.modal-resumen {
  width: 100%;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 6px 16px;
}
.resumen-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #E2E8F0;
  font-size: 13px;
}
.resumen-fila:last-child { border-bottom: none; }
.resumen-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #64748B;
  font-weight: 600;
  white-space: nowrap;
  font-size: 12px;
}
.resumen-value {
  color: #0F172A;
  font-weight: 700;
  text-align: right;
  word-break: break-word;
}
.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12.5px;
  background: #F1F5F9;
  padding: 2px 8px;
  border-radius: 5px;
}
.modal-card .alert {
  width: 100%;
  text-align: left;
}
.modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
  flex-wrap: wrap;
  justify-content: center;
  width: 100%;
}

/* ═══ BOTONES MODAL ═══ */
.btn-primary,
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 11px 20px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
  flex: 1;
  min-width: 160px;
}
.btn-primary {
  background: #0F766E;
  color: #fff;
  border: none;
}
.btn-primary:hover {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-secondary {
  background: #fff;
  color: #475569;
  border: 1px solid #E2E8F0;
}
.btn-secondary:hover {
  background: #F8FAFC;
  border-color: #CBD5E1;
  color: #0F766E;
}

/* ═══ SPIN ═══ */
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══ TRANSICIONES ═══ */
.slide-fade-enter-active,
.slide-fade-leave-active { transition: all .3s ease; }
.slide-fade-enter-from,
.slide-fade-leave-to { opacity: 0; transform: translateY(-6px); }

.modal-fade-enter-active,
.modal-fade-leave-active { transition: opacity .25s ease; }
.modal-fade-enter-from,
.modal-fade-leave-to { opacity: 0; }

.modal-slide-enter-active {
  transition: opacity .3s ease, transform .3s cubic-bezier(0.16, 1, 0.3, 1);
}
.modal-slide-leave-active {
  transition: opacity .2s ease, transform .2s ease;
}
.modal-slide-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(0.97);
}
.modal-slide-leave-to {
  opacity: 0;
  transform: translateY(8px) scale(0.98);
}

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .wizard-layout { grid-template-columns: 1fr; }
  .wizard-sidebar { position: static; }
  .preview-card { position: static; }
}

@media (max-width: 640px) {
  .nuevo-cliente { padding: 16px 16px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero h1 { font-size: 22px; }
  .card-header { padding: 14px 18px; }
  .card-body { padding: 16px 18px 20px; }

  .form-grid { grid-template-columns: 1fr; gap: 14px; }

  .form-actions {
    flex-direction: column-reverse;
    padding: 14px 16px;
  }
  .btn-cancel,
  .btn-next { width: 100%; }

  .modal-card { padding: 24px 20px 20px; }
  .modal-actions { flex-direction: column; }
  .btn-primary,
  .btn-secondary { width: 100%; }

  .resumen-fila {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  .resumen-value { text-align: left; }
}
</style>