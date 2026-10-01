<template>
  <div class="pay-view">
    <ToastContainer />

    <!-- ═══ LOADING ═══ -->
    <div v-if="cargando" class="card state-full">
      <div class="card-body">
        <div class="loading-state">
          <span class="spinner spinner-lg" />
          <p>Cargando información de la cita…</p>
        </div>
      </div>
    </div>

    <!-- ═══ ERROR DE CARGA ═══ -->
    <div v-else-if="errorCarga" class="state-full">
      <div class="state-error-icon"><AlertTriangle :size="48" /></div>
      <h2 class="state-title">No pudimos cargar la cita</h2>
      <p class="state-text">{{ errorCarga }}</p>
      <AppButton variant="secondary" @click="volver">
        <template #icon-left><ArrowLeft :size="15" /></template>
        Volver a Mis Citas
      </AppButton>
    </div>

    <!-- ═══ ESTADO NO PAGABLE ═══ -->
    <div v-else-if="!esPagable" class="state-full">
      <component :is="estadoIcono" :size="52" :class="estadoIconClass" />
      <h2 class="state-title">{{ estadoTitulo }}</h2>
      <p class="state-text">{{ estadoDescripcion }}</p>
      <div class="state-actions">
        <AppButton variant="secondary" @click="volver">
          <template #icon-left><ArrowLeft :size="15" /></template>
          Volver a Mis Citas
        </AppButton>

        <!-- NUEVO: solo aparece cuando expiró -->
        <AppButton
          v-if="expirado"
          variant="primary"
          @click="irANuevaCita"
        >
          <template #icon-left><CalendarPlus :size="15" /></template>
          Agendar nueva cita
        </AppButton>

        <AppButton
          v-if="!expirado && ['Confirmada', 'Completada'].includes(cita.estado)"
          variant="primary"
          @click="irAlHistorial"
        >
          <template #icon-left><ReceiptText :size="15" /></template>
          Ver en historial
        </AppButton>
      </div>
    </div>

    <!-- ═══ FLUJO DE PAGO ═══ -->
    <template v-else>
      <header class="page-header">
        <div>
          <button class="btn-back" type="button" @click="volver">
            <ArrowLeft :size="16" /> Volver
          </button>
          <span class="page-header-eyebrow">
            <CreditCard :size="12" /> Pago de cita
          </span>
          <h1>Pagar Cita</h1>
          <p class="page-header-sub">
            Completa el pago para confirmar tu reserva.
          </p>
        </div>
      </header>

      <div class="pay-layout">
        <!-- ═══ COLUMNA IZQUIERDA: resumen ═══ -->
        <aside class="summary-col">
          <!-- Countdown -->
          <CitaCountdown
            v-if="cita.expiraEn && !expirado"
            :expira-en="cita.expiraEn"
            :duracion-total-min="15"
            @expirado="onExpirado"
          />

          <!-- Alerta de expirado -->
          <AppAlert v-if="expirado" variant="error">
            <div>
              <strong>La reserva expiró</strong>
              <p style="margin-top: 4px;">
                Este horario ya no está disponible y la cita fue cancelada.
                Vuelve a agendar una nueva cita cuando estés listo.
              </p>
            </div>
          </AppAlert>

          <!-- Card resumen -->
          <div class="summary-card">
            <h3 class="card-title">Resumen de la cita</h3>

            <div class="summary-row">
              <PawPrint :size="16" class="summary-icon" />
              <div class="summary-text">
                <span class="summary-label">Mascota</span>
                <span class="summary-value">{{ cita.nombreMascota }}</span>
              </div>
            </div>

            <div class="summary-row">
              <Stethoscope :size="16" class="summary-icon" />
              <div class="summary-text">
                <span class="summary-label">Veterinario</span>
                <span class="summary-value">{{ cita.nombreVeterinario }}</span>
              </div>
            </div>

            <div class="summary-row">
              <ClipboardList :size="16" class="summary-icon" />
              <div class="summary-text">
                <span class="summary-label">Servicio</span>
                <span class="summary-value">{{ cita.nombreServicio }}</span>
              </div>
            </div>

            <div class="summary-row">
              <CalendarDays :size="16" class="summary-icon" />
              <div class="summary-text">
                <span class="summary-label">Fecha</span>
                <span class="summary-value">{{ formatFecha(cita.fechaCita) }}</span>
              </div>
            </div>

            <div class="summary-row">
              <Clock :size="16" class="summary-icon" />
              <div class="summary-text">
                <span class="summary-label">Hora</span>
                <span class="summary-value">{{ cita.horaInicio }} — {{ cita.horaFin }}</span>
              </div>
            </div>

            <div class="divider" />

            <div class="total-block">
              <div class="total-line">
                <span>Total USD</span>
                <span class="total-usd">$ {{ fmtUsd(cita.costoUsd) }}</span>
              </div>
              <div class="total-line total-bs-line">
                <span>Total Bs</span>
                <span class="total-bs">Bs. {{ fmtBs(cita.costoBs) }}</span>
              </div>
              <p v-if="tasaImplicita" class="tasa-hint">
                Tasa aplicada: {{ fmtTasa(tasaImplicita) }} Bs/USD
              </p>
            </div>
          </div>

          <!-- Datos bancarios -->
          <div v-if="datosBancarios" class="banco-card">
            <header class="banco-head">
              <div class="banco-icon"><Landmark :size="16" /></div>
              <div>
                <h3 class="banco-title">Datos para pagar</h3>
                <p class="banco-sub">{{ datosBancarios.nombreTitular }}</p>
              </div>
            </header>

            <div class="banco-body">
              <div class="banco-bloque">
                <p class="banco-bloque-titulo">
                  <Smartphone :size="13" /> Pago Móvil
                </p>
                <div class="banco-fila">
                  <span class="banco-label">Banco</span>
                  <span class="banco-valor">{{ datosBancarios.pagoMovil.banco }}</span>
                </div>
                <div class="banco-fila">
                  <span class="banco-label">Teléfono</span>
                  <button
                    type="button"
                    class="banco-copiar mono"
                    title="Copiar teléfono"
                    @click="copiarDato(datosBancarios.pagoMovil.telefono, 'Teléfono')"
                  >
                    {{ datosBancarios.pagoMovil.telefono }} <Copy :size="12" />
                  </button>
                </div>
                <div class="banco-fila">
                  <span class="banco-label">Cédula / RIF</span>
                  <button
                    type="button"
                    class="banco-copiar mono"
                    title="Copiar cédula"
                    @click="copiarDato(datosBancarios.pagoMovil.cedula, 'Cédula')"
                  >
                    {{ datosBancarios.pagoMovil.cedula }} <Copy :size="12" />
                  </button>
                </div>
              </div>

              <div class="banco-bloque">
                <p class="banco-bloque-titulo">
                  <ArrowRightLeft :size="13" /> Transferencia
                </p>
                <div class="banco-fila">
                  <span class="banco-label">Banco</span>
                  <span class="banco-valor">
                    {{ datosBancarios.bancoPrincipal.codigo }} {{ datosBancarios.bancoPrincipal.nombre }}
                  </span>
                </div>
                <div class="banco-fila">
                  <span class="banco-label">Cuenta</span>
                  <button
                    type="button"
                    class="banco-copiar mono"
                    title="Copiar número de cuenta"
                    @click="copiarDato(datosBancarios.bancoPrincipal.cuenta, 'Número de cuenta')"
                  >
                    {{ datosBancarios.bancoPrincipal.cuenta }} <Copy :size="12" />
                  </button>
                </div>
                <div class="banco-fila">
                  <span class="banco-label">RIF</span>
                  <button
                    type="button"
                    class="banco-copiar mono"
                    title="Copiar RIF"
                    @click="copiarDato(datosBancarios.rif, 'RIF')"
                  >
                    {{ datosBancarios.rif }} <Copy :size="12" />
                  </button>
                </div>
              </div>

              <div v-if="datosBancarios.nota" class="banco-nota">
                <Info :size="13" />
                <span>{{ datosBancarios.nota }}</span>
              </div>
            </div>
          </div>
        </aside>

        <!-- ═══ COLUMNA DERECHA: formulario ═══ -->
        <main class="form-col">
          <div class="payment-card">
            <h3 class="card-title">Método de pago</h3>
            <p class="card-hint">Selecciona cómo deseas pagar esta cita</p>

            <div v-if="cargandoMetodos" class="mini-loading">
              <span class="spinner spinner-sm" />
              <span>Cargando métodos disponibles…</span>
            </div>

            <div v-else-if="!metodos.length" class="mini-empty">
              <AlertCircle :size="20" />
              <span>No hay métodos de pago disponibles en este momento.</span>
            </div>

            <div v-else class="method-grid">
              <button
                v-for="m in metodos"
                :key="m.id"
                type="button"
                class="method-card"
                :class="{ active: metodoSeleccionado?.id === m.id }"
                @click="seleccionarMetodo(m)"
              >
                <div class="method-icon">
                  <component :is="iconoMetodo(m.nombre)" :size="20" />
                </div>
                <div class="method-info">
                  <span class="method-name">{{ formatMetodo(m.nombre) }}</span>
                  <span class="method-desc">{{ m.descripcion }}</span>
                </div>
                <div class="method-radio">
                  <div class="radio-dot" :class="{ filled: metodoSeleccionado?.id === m.id }" />
                </div>
              </button>
            </div>

            <Transition name="slide-down">
              <div v-if="metodoSeleccionado" class="dynamic-fields">
                <h4 class="fields-title">Datos del pago</h4>

                <div v-for="key in camposDinamicos" :key="key" class="form-field">
                  <label :for="`field-${key}`">
                    {{ labelCampo(key) }} <span class="required">*</span>
                  </label>

                  <select
                    v-if="key === 'banco'"
                    :id="`field-${key}`"
                    v-model="datosPago[key]"
                    class="form-select"
                    :class="{ 'field-invalid': erroresCampos[key] }"
                    @change="erroresCampos[key] = ''"
                  >
                    <option value="" disabled>Selecciona un banco…</option>
                    <optgroup label="Bancos universales">
                      <option
                        v-for="b in BANCOS_VENEZUELA"
                        :key="b.codigo"
                        :value="`${b.codigo} - ${b.nombre}`"
                      >
                        {{ b.codigo }} — {{ b.nombre }}
                      </option>
                    </optgroup>
                  </select>

                  <input
                    v-else
                    :id="`field-${key}`"
                    v-model="datosPago[key]"
                    :type="tipoCampo(key)"
                    :placeholder="placeholderCampo(key)"
                    :inputmode="inputModeCampo(key)"
                    :maxlength="maxlengthCampo(key)"
                    autocomplete="off"
                    :class="{ 'field-invalid': erroresCampos[key] }"
                    @input="erroresCampos[key] = ''"
                  />

                  <small v-if="erroresCampos[key]" class="field-error">{{ erroresCampos[key] }}</small>
                </div>

                <div class="form-field">
                  <label for="referencia">
                    Referencia de transacción <span class="required">*</span>
                  </label>
                  <input
                    id="referencia"
                    v-model="referenciaTransaccion"
                    type="text"
                    maxlength="20"
                    inputmode="numeric"
                    placeholder="Ej: 1234567890"
                    autocomplete="off"
                    :class="{ 'field-invalid': erroresCampos.referencia }"
                    @input="erroresCampos.referencia = ''"
                  />
                  <small v-if="erroresCampos.referencia" class="field-error">{{ erroresCampos.referencia }}</small>
                  <small v-else class="field-hint">Número de confirmación de tu banco o app</small>
                </div>
              </div>
            </Transition>

            <Transition name="slide-down">
              <AppAlert v-if="errorForm" variant="error" class="mt-4">
                {{ errorForm }}
              </AppAlert>
            </Transition>

            <AppButton
              variant="primary"
              size="lg"
              block
              class="mt-5"
              :loading="procesando"
              :disabled="!puedeConfirmar || expirado"
              @click="confirmarPago"
            >
              <template #icon-left><ShieldCheck :size="18" /></template>
              {{ procesando ? 'Procesando pago…' : 'Confirmar pago' }}
            </AppButton>

            <p class="secure-hint">
              <Lock :size="12" />
              Verificaremos tu transacción antes de confirmar la cita.
            </p>
          </div>
        </main>
      </div>
    </template>

    <!-- ═══ MODAL DE ÉXITO ═══ -->
    <AppModal
      :model-value="!!pagoExitoso"
      title="¡Pago registrado!"
      size="md"
      @update:model-value="cerrarModalExito"
    >
      <div v-if="pagoExitoso" class="success-content">
        <div class="success-icon-wrap">
          <CheckCircle2 :size="34" />
        </div>

        <p class="success-message">{{ pagoExitoso.mensaje }}</p>

        <div class="success-details">
          <div class="detail-row">
            <span class="detail-label">Factura</span>
            <span class="detail-value mono">{{ pagoExitoso.numeroControl }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">Estado</span>
            <span class="detail-value">{{ formatEstadoCita(pagoExitoso.estadoCita) }}</span>
          </div>
          <div v-if="pagoExitoso.montoUsd != null" class="detail-row detail-row-total">
            <span class="detail-label">Total USD</span>
            <span class="detail-value amount">{{ fmtUsd(pagoExitoso.montoUsd) }} USD</span>
          </div>
          <div v-if="pagoExitoso.montoBs != null" class="detail-row detail-row-total">
            <span class="detail-label">Total Bs</span>
            <span class="detail-value amount-bs">Bs. {{ fmtBs(pagoExitoso.montoBs) }}</span>
          </div>
          <div v-if="pagoExitoso.tasaCambio != null" class="detail-row detail-row-tasa">
            <span class="detail-label">Tasa aplicada</span>
            <span class="detail-value tasa">{{ fmtTasa(pagoExitoso.tasaCambio) }} Bs/USD</span>
          </div>
        </div>

        <AppAlert v-if="pagoExitoso.advertenciaEmail" variant="info" class="mt-4">
          {{ pagoExitoso.advertenciaEmail }}
        </AppAlert>
      </div>

      <template #footer>
        <AppButton
          variant="secondary"
          :loading="descargandoPdf"
          @click="descargarFacturaActual"
        >
          <template #icon-left><Download :size="15" /></template>
          {{ descargandoPdf ? 'Generando…' : 'Descargar factura' }}
        </AppButton>
        <AppButton variant="primary" @click="irAlHistorial">
          Ver historial
        </AppButton>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, PawPrint, Stethoscope, ClipboardList, CalendarDays, Clock,
  AlertTriangle, AlertCircle, CheckCircle2, Info,
  ShieldCheck, Lock, Download, ReceiptText,
  Landmark, Smartphone, ArrowRightLeft, Copy, CreditCard, CalendarPlus,
} from 'lucide-vue-next'
import api from '@/api/axios.config'
import {
  getMetodosOnline, procesarPagoCita, descargarFactura, getDatosBancarios,
} from '@/api/pagos.api.js'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

import CitaCountdown from '@/components/cliente/CitaCountdown.vue'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppModal from '@/components/ui/AppModal.vue'

import { BANCOS_VENEZUELA } from '@/utils/constants/bancos'

import { cancelarCitaExpirada } from '@/api/citas.api.js'

const route = useRoute()
const router = useRouter()
const { toastError, toastSuccess } = useToast()

const idCita = computed(() => Number(route.params.idCita))

/* ═══════════════════════════════════════════════════════════════
   ESTADO
   ═══════════════════════════════════════════════════════════════ */
const cargando = ref(true)
const errorCarga = ref('')
const cita = ref(null)
const expirado = ref(false)

const cargandoMetodos = ref(true)
const metodos = ref([])
const metodoSeleccionado = ref(null)

const datosPago = ref({})
const referenciaTransaccion = ref('')
const errorForm = ref('')
const procesando = ref(false)
const erroresCampos = ref({})

const pagoExitoso = ref(null)
const descargandoPdf = ref(false)

const datosBancarios = ref(null)

/* ═══════════════════════════════════════════════════════════════
   VALIDADORES
   ═══════════════════════════════════════════════════════════════ */
const SOLO_DIGITOS = /^\d+$/

function validarReferencia(v) {
  const s = String(v || '').trim()
  if (!s) return 'La referencia es obligatoria'
  if (!SOLO_DIGITOS.test(s)) return 'La referencia debe contener solo números'
  if (s.length < 4) return 'Mínimo 4 dígitos'
  if (s.length > 20) return 'Máximo 20 dígitos'
  return null
}

function validarTelefono(v) {
  const s = String(v || '').replace(/[\s\-()]/g, '')
  if (!s) return 'El teléfono es obligatorio'
  if (!SOLO_DIGITOS.test(s)) return 'El teléfono debe contener solo números'
  if (!s.startsWith('04')) return 'Debe comenzar con 04 (Venezuela)'
  if (s.length !== 11) return 'Debe tener 11 dígitos (ej: 04141234567)'
  return null
}

function validarCuenta(v) {
  const s = String(v || '').replace(/[\s\-]/g, '')
  if (!s) return 'El número de cuenta es obligatorio'
  if (!SOLO_DIGITOS.test(s)) return 'Solo números'
  if (s.length !== 20) return 'Debe tener 20 dígitos'
  return null
}

function validarUltimosDigitos(v) {
  const s = String(v || '').trim()
  if (!s) return 'Requerido'
  if (!SOLO_DIGITOS.test(s)) return 'Solo números'
  if (s.length !== 4) return 'Deben ser exactamente 4 dígitos'
  return null
}

function validarLote(v) {
  const s = String(v || '').trim()
  if (!s) return 'El lote es obligatorio'
  if (!SOLO_DIGITOS.test(s)) return 'Solo números'
  if (s.length < 4 || s.length > 10) return 'Entre 4 y 10 dígitos'
  return null
}

const VALIDADORES_CAMPO = {
  telefono: validarTelefono,
  numero_cuenta: validarCuenta,
  ultimos_digitos: validarUltimosDigitos,
  lote: validarLote,
}

/* ═══════════════════════════════════════════════════════════════
   COMPUTED
   ═══════════════════════════════════════════════════════════════ */
const esPagable = computed(() =>
  cita.value?.estado === 'Pendiente_Pago' && !expirado.value
)

const camposDinamicos = computed(() => {
  if (!metodoSeleccionado.value) return []
  const req = metodoSeleccionado.value.camposRequeridos || {}
  return Object.keys(req).filter((k) => k !== 'referencia')
})

const puedeConfirmar = computed(() => {
  if (!metodoSeleccionado.value) return false
  if (!referenciaTransaccion.value.trim()) return false
  if (validarReferencia(referenciaTransaccion.value) !== null) return false

  const req = metodoSeleccionado.value.camposRequeridos || {}
  for (const key of Object.keys(req)) {
    if (key === 'referencia') continue
    const v = datosPago.value[key]
    if (v == null || String(v).trim() === '') return false
    const validador = VALIDADORES_CAMPO[key]
    if (validador && validador(v) !== null) return false
  }
  return true
})

const tasaImplicita = computed(() => {
  const usd = Number(cita.value?.costoUsd)
  const bs = Number(cita.value?.costoBs)
  if (!usd || !bs) return null
  return bs / usd
})

/* ─── Estado no pagable ─── */
const estadoIcono = computed(() => {
  if (expirado.value) return Clock            // ← reloj para expiradas
  const map = {
    Confirmada: CheckCircle2,
    Completada: CheckCircle2,
    Cancelada: AlertCircle,
    En_Atencion: AlertTriangle,
  }
  return map[cita.value?.estado] || AlertCircle
})

const estadoIconClass = computed(() => {
  if (expirado.value) return 'icon-warn'
  const ok = ['Confirmada', 'Completada']
  return ok.includes(cita.value?.estado) ? 'icon-ok' : 'icon-warn'
})

const estadoTitulo = computed(() => {
  if (expirado.value) return 'La reserva expiró'
  const map = {
    Confirmada: 'Esta cita ya está confirmada',
    Completada: 'Esta cita ya fue completada',
    Cancelada: 'Esta cita fue cancelada',
    En_Atencion: 'Esta cita está en atención',
  }
  return map[cita.value?.estado] || 'Esta cita no se puede pagar'
})

const estadoDescripcion = computed(() => {
  if (expirado.value) {
    return 'El tiempo para pagar esta reserva terminó y el horario fue liberado automáticamente. ' +
           'Si aún quieres esta cita, puedes agendar una nueva.'
  }
  const map = {
    Confirmada: 'La cita fue confirmada. Revisa los detalles en tu historial.',
    Completada: 'La consulta fue atendida. Revisa el historial de pagos si necesitas el comprobante.',
    Cancelada: 'No es posible pagar una cita cancelada. Agenda una nueva desde "Solicitar Cita".',
    En_Atencion: 'La cita ya está siendo atendida por el veterinario.',
  }
  return map[cita.value?.estado] || 'El estado actual no permite pagar esta cita.'
})

const FIELD_META = {
  banco:           { label: 'Banco',             placeholder: 'Selecciona un banco', type: 'text' },
  numero_cuenta:   { label: 'Número de cuenta',  placeholder: '0102-0123-45-6789012345', type: 'text', inputmode: 'numeric', maxlength: 30 },
  telefono:        { label: 'Teléfono asociado', placeholder: '0414-1234567', type: 'tel', inputmode: 'tel', maxlength: 15 },
  ultimos_digitos: { label: 'Últimos 4 dígitos', placeholder: '1234', type: 'text', inputmode: 'numeric', maxlength: 4 },
  lote:            { label: 'Lote / aprobación', placeholder: '987654', type: 'text', inputmode: 'numeric', maxlength: 10 },
}

function labelCampo(k) { return FIELD_META[k]?.label || k.replaceAll('_', ' ') }
function placeholderCampo(k) { return FIELD_META[k]?.placeholder || '' }
function tipoCampo(k) { return FIELD_META[k]?.type || 'text' }
function inputModeCampo(k) { return FIELD_META[k]?.inputmode || 'text' }
function maxlengthCampo(k) { return FIELD_META[k]?.maxlength || undefined }

/* ═══════════════════════════════════════════════════════════════
   CARGA INICIAL
   ═══════════════════════════════════════════════════════════════ */
onMounted(async () => {
  await Promise.all([cargarCita(), cargarMetodos(), cargarDatosBancarios()])
})

async function cargarCita() {
  cargando.value = true
  errorCarga.value = ''
  try {
    const { data } = await api.get('/citas/mis-citas')
    const found = (data || []).find((c) => c.idCita === idCita.value)
    if (!found) {
      errorCarga.value = 'La cita no existe o no te pertenece.'
      return
    }
    cita.value = found
    
    if (found.expiraEn && new Date(found.expiraEn) <= new Date()) {
      expirado.value = true
      // Actualizamos local + pedimos al backend que la cancele
      if (found.estado === 'Pendiente_Pago') {
        cita.value = { ...cita.value, estado: 'Cancelada', expiraEn: null }
        cancelarCitaExpirada(idCita.value).catch((err) => {
          console.warn('[cargarCita] Backend no confirmó:', err?.response?.data || err?.message)
        })
      }
    }
  } catch (err) {
    errorCarga.value = getApiErrorMessage(err) || 'Error al cargar la cita.'
  } finally {
    cargando.value = false
  }
}

async function cargarMetodos() {
  cargandoMetodos.value = true
  try {
    const { data } = await getMetodosOnline()
    metodos.value = data || []
  } catch (err) {
    toastError(getApiErrorMessage(err) || 'No se pudieron cargar los métodos de pago')
  } finally {
    cargandoMetodos.value = false
  }
}

async function cargarDatosBancarios() {
  try {
    const { data } = await getDatosBancarios()
    datosBancarios.value = data
  } catch {
    datosBancarios.value = null
  }
}

/* ═══════════════════════════════════════════════════════════════
   SELECCIÓN DE MÉTODO
   ═══════════════════════════════════════════════════════════════ */
function seleccionarMetodo(m) {
  metodoSeleccionado.value = m
  datosPago.value = {}
  referenciaTransaccion.value = ''
  errorForm.value = ''
  erroresCampos.value = {}
  const req = m.camposRequeridos || {}
  for (const k of Object.keys(req)) {
    if (k !== 'referencia') datosPago.value[k] = ''
  }
}

function iconoMetodo(nombre) {
  if (nombre === 'Transferencia') return Landmark
  if (nombre === 'Pago_Movil') return Smartphone
  return AlertCircle
}

function formatMetodo(nombre) {
  const map = {
    Transferencia: 'Transferencia bancaria',
    Pago_Movil: 'Pago Móvil',
  }
  return map[nombre] || nombre
}

/* ═══════════════════════════════════════════════════════════════
   VALIDACIÓN GLOBAL
   ═══════════════════════════════════════════════════════════════ */
function validarTodo() {
  const errores = {}

  const errRef = validarReferencia(referenciaTransaccion.value)
  if (errRef) errores.referencia = errRef

  const req = metodoSeleccionado.value?.camposRequeridos || {}
  for (const key of Object.keys(req)) {
    if (key === 'referencia') continue
    const validador = VALIDADORES_CAMPO[key]
    if (validador) {
      const err = validador(datosPago.value[key])
      if (err) errores[key] = err
    }
  }

  erroresCampos.value = errores
  return Object.keys(errores).length === 0
}

/* ═══════════════════════════════════════════════════════════════
   CONFIRMAR PAGO
   ═══════════════════════════════════════════════════════════════ */
async function confirmarPago() {
  if (procesando.value) return
  errorForm.value = ''

  if (!validarTodo()) {
    errorForm.value = 'Corrige los campos marcados antes de continuar.'
    return
  }

  procesando.value = true
  const req = metodoSeleccionado.value.camposRequeridos || {}
  const datosFinales = { ...datosPago.value }
  if (Object.prototype.hasOwnProperty.call(req, 'referencia')) {
    datosFinales.referencia = referenciaTransaccion.value.trim()
  }

  const payload = {
    idCita: idCita.value,
    idMetodoPago: metodoSeleccionado.value.id,
    referenciaTransaccion: referenciaTransaccion.value.trim(),
    datosPago: datosFinales,
  }

  try {
    const { data } = await procesarPagoCita(payload)
    pagoExitoso.value = data
  } catch (err) {
    const status = err?.response?.status
    if (status === 409) {
      errorForm.value = 'La reserva expiró mientras completabas el pago. Vuelve a agendar una nueva cita.'
      expirado.value = true
    } else {
      errorForm.value = getApiErrorMessage(err) || 'No se pudo procesar el pago. Intenta de nuevo.'
    }
  } finally {
    procesando.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   EXPIRADO
   ═══════════════════════════════════════════════════════════════ */
async function onExpirado() {
  if (expirado.value) return
  expirado.value = true

  // ⚡ Cambio local INMEDIATO — la UI ya se actualiza sin esperar al backend
  if (cita.value) {
    cita.value = { ...cita.value, estado: 'Cancelada', expiraEn: null }
  }

  // En paralelo, sincronizamos con el backend (es idempotente)
  try {
    await cancelarCitaExpirada(idCita.value)
  } catch (err) {
    // No es crítico: el CitasExpiracionJob del backend la cancelará igual
    console.warn('[onExpirado] El backend no confirmó la cancelación:',
      err?.response?.data || err?.message)
  }
}

/* ═══════════════════════════════════════════════════════════════
   MODAL DE ÉXITO
   ═══════════════════════════════════════════════════════════════ */
function cerrarModalExito() {
  if (!pagoExitoso.value) return

  pagoExitoso.value = null

  router.replace('/cliente/mis-citas')
}


async function descargarFacturaActual() {
  if (!pagoExitoso.value?.idFactura) return
  descargandoPdf.value = true
  try {
    const { blob, filename } = await descargarFactura(pagoExitoso.value.idFactura)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
    toastSuccess('Factura descargada')
  } catch {
    toastError('No se pudo descargar la factura. Intenta más tarde.')
  } finally {
    descargandoPdf.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   NAVEGACIÓN
   ═══════════════════════════════════════════════════════════════ */
function volver() {
  router.push('/cliente/mis-citas')
}

function irAlHistorial() {
  pagoExitoso.value = null
  router.replace('/cliente/historial-pagos')
}

function irANuevaCita() {
  router.push('/cliente/solicitar-cita')
}

/* ═══════════════════════════════════════════════════════════════
   HELPERS
   ═══════════════════════════════════════════════════════════════ */
function fmtUsd(v) {
  if (v == null) return '0.00'
  return Number(v).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

function fmtBs(v) {
  if (v == null) return '0,00'
  return Number(v).toLocaleString('es-VE', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

function fmtTasa(v) {
  if (v == null) return '—'
  return Number(v).toLocaleString('es-VE', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

function formatFecha(iso) {
  if (!iso) return ''
  const d = new Date(iso + 'T00:00:00')
  return d.toLocaleDateString('es-VE', {
    weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
  })
}

function formatEstadoCita(estado) {
  const map = {
    'Pendiente_Pago': 'Pendiente de Pago',
    Confirmada: 'Confirmada',
    En_Atencion: 'En Atención',
    Completada: 'Completada',
    Cancelada: 'Cancelada',
  }
  return map[estado] || estado
}

async function copiarDato(texto, etiqueta) {
  const valor = String(texto || '').trim()
  if (!valor) return
  try {
    await navigator.clipboard.writeText(valor)
    toastSuccess(`${etiqueta} copiado al portapapeles`)
  } catch {
    const tmp = document.createElement('textarea')
    tmp.value = valor
    tmp.style.cssText = 'position:fixed;opacity:0;pointer-events:none'
    document.body.appendChild(tmp)
    tmp.select()
    document.execCommand('copy')
    tmp.remove()
    toastSuccess(`${etiqueta} copiado al portapapeles`)
  }
}
</script>

<style scoped>
.pay-view {
  max-width: 1100px;
  margin: 0 auto;
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* ═══ ESTADOS FULL ═══ */
.state-full {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-12) var(--space-6);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-3xl);
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
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0;
}
.state-text {
  font-size: var(--text-base);
  color: var(--text-secondary);
  max-width: 460px;
  line-height: var(--leading-relaxed);
  margin: 0 0 var(--space-2);
}
.state-actions {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
  justify-content: center;
}
.icon-ok { color: var(--success-600); }
.icon-warn { color: var(--danger-600); }

/* ═══ HEADER ═══ */
.btn-back {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-3);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--neutral-600);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  font-family: inherit;
  margin-bottom: var(--space-3);
}
.btn-back:hover {
  background: var(--brand-50);
  border-color: var(--brand-200);
  color: var(--brand-700);
}

/* ═══ LAYOUT ═══ */
.pay-layout {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: var(--space-5);
  align-items: start;
}

/* ═══ COLUMNA IZQUIERDA ═══ */
.summary-col {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  position: sticky;
  top: var(--space-6);
}

.summary-card,
.payment-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  padding: var(--space-5) var(--space-6);
}

.card-title {
  margin: 0 0 var(--space-4);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}
.card-hint {
  margin: calc(var(--space-3) * -1) 0 var(--space-5);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

/* ── Filas de resumen ── */
.summary-row {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-subtle);
}
.summary-row:last-of-type { border-bottom: none; }
.summary-icon { color: var(--text-tertiary); margin-top: 3px; flex-shrink: 0; }
.summary-text { display: flex; flex-direction: column; min-width: 0; }
.summary-label {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
}
.summary-value {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-top: 2px;
}

.divider {
  height: 1px;
  background: var(--border-subtle);
  margin: var(--space-3) 0 var(--space-4);
}

.total-block { display: flex; flex-direction: column; gap: var(--space-2); }
.total-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--text-md);
}
.total-line > span:first-child { color: var(--text-secondary); font-weight: var(--font-medium); }
.total-usd {
  font-size: var(--text-3xl);
  font-weight: var(--font-extrabold);
  color: var(--brand-700);
}
.total-bs-line {
  padding-top: var(--space-2);
  border-top: 1px dashed var(--border-subtle);
}
.total-bs {
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--neutral-700);
}
.tasa-hint {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  margin: var(--space-1) 0 0;
  text-align: right;
}

/* ── Datos bancarios ── */
.banco-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
}
.banco-head {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-5);
  background: var(--neutral-50);
  border-bottom: 1px solid var(--border-subtle);
}
.banco-icon {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-md);
  background: var(--info-50);
  color: var(--info-600);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.banco-title { margin: 0; font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); }
.banco-sub { margin: 2px 0 0; font-size: var(--text-sm); color: var(--text-secondary); }
.banco-body { padding: var(--space-3) var(--space-5) var(--space-4); display: flex; flex-direction: column; gap: var(--space-3); }
.banco-bloque { display: flex; flex-direction: column; gap: var(--space-2); }
.banco-bloque-titulo {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin: 0 0 var(--space-1);
  font-size: var(--text-xs);
  font-weight: var(--font-extrabold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--info-600);
}
.banco-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-sm);
  padding: var(--space-1) 0;
  border-bottom: 1px dashed var(--neutral-100);
}
.banco-fila:last-child { border-bottom: none; }
.banco-label { color: var(--text-secondary); font-weight: var(--font-medium); flex-shrink: 0; }
.banco-valor {
  color: var(--text-primary);
  font-weight: var(--font-bold);
  text-align: right;
  word-break: break-word;
  min-width: 0;
}
.banco-nota {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
  padding: var(--space-3);
  background: var(--warning-50);
  color: var(--warning-700);
  border: 1px solid var(--warning-200);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  line-height: var(--leading-snug);
}
.banco-nota svg { flex-shrink: 0; margin-top: 1px; }

.banco-copiar {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  background: none;
  border: 1px dashed transparent;
  border-radius: var(--radius-sm);
  padding: 2px var(--space-2);
  color: var(--text-primary);
  font-weight: var(--font-bold);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.banco-copiar svg { color: var(--text-tertiary); transition: color var(--duration-fast) var(--ease-out); }
.banco-copiar:hover { background: var(--info-50); border-color: var(--info-200); color: var(--info-600); }
.banco-copiar:hover svg { color: var(--info-600); }
.banco-copiar:active { transform: scale(0.96); }

/* ═══ COLUMNA DERECHA ═══ */
.form-col { min-width: 0; }

/* ── Métodos de pago ── */
.mini-loading,
.mini-empty {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-5);
  color: var(--text-tertiary);
  font-size: var(--text-md);
  background: var(--bg-surface-alt);
  border-radius: var(--radius-lg);
  border: 1px dashed var(--border-subtle);
}

.method-grid { display: flex; flex-direction: column; gap: var(--space-3); }

.method-card {
  display: grid;
  grid-template-columns: 44px 1fr 22px;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-4);
  background: var(--bg-surface);
  border: 1.5px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  cursor: pointer;
  text-align: left;
  transition: all var(--duration-base) var(--ease-out);
  font-family: inherit;
}
.method-card:hover {
  border-color: var(--border-strong);
  background: var(--bg-surface-alt);
}
.method-card.active {
  border-color: var(--brand-700);
  background: var(--brand-50);
  box-shadow: 0 0 0 3px var(--brand-100);
}
.method-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-lg);
  background: var(--neutral-100);
  color: var(--neutral-600);
  display: flex;
  align-items: center;
  justify-content: center;
}
.method-card.active .method-icon { background: var(--brand-100); color: var(--brand-700); }
.method-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.method-name { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); }
.method-desc { font-size: var(--text-sm); color: var(--text-secondary); line-height: var(--leading-snug); }
.method-radio { display: flex; justify-content: flex-end; }
.radio-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid var(--neutral-300);
  background: var(--bg-surface);
  transition: all var(--duration-fast) var(--ease-out);
}
.radio-dot.filled {
  border-color: var(--brand-700);
  background: var(--brand-700);
  box-shadow: inset 0 0 0 3px var(--bg-surface);
}

/* ── Campos dinámicos ── */
.dynamic-fields {
  margin-top: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.fields-title {
  margin: 0 0 var(--space-1);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-secondary);
  padding-bottom: var(--space-2);
  border-bottom: 1px dashed var(--border-subtle);
}

.form-field { display: flex; flex-direction: column; gap: var(--space-2); }
.form-field label {
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--neutral-700);
}
.required { color: var(--danger-500); margin-left: 2px; }

.form-field input,
.form-select {
  width: 100%;
  padding: 0 var(--space-4);
  height: var(--input-h-md);
  border: 1px solid var(--border-strong);
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  color: var(--text-primary);
  background: var(--bg-surface);
  font-family: inherit;
  box-sizing: border-box;
  transition: all var(--duration-fast) var(--ease-out);
}
.form-field input::placeholder { color: var(--text-tertiary); }
.form-field input:focus,
.form-select:focus {
  outline: none;
  border-color: var(--brand-700);
  box-shadow: var(--shadow-focus);
}
.form-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right var(--space-4) center;
  padding-right: var(--space-10);
  cursor: pointer;
}

.field-invalid { border-color: var(--danger-500) !important; background: var(--danger-50) !important; }
.field-error { font-size: var(--text-sm); color: var(--danger-600); font-weight: var(--font-semibold); margin-top: 2px; }
.field-hint { font-size: var(--text-xs); color: var(--text-tertiary); margin-top: 2px; }

.secure-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: var(--space-4) 0 0;
  text-align: center;
}

/* ── Utilidades ── */
.mt-4 { margin-top: var(--space-4); }
.mt-5 { margin-top: var(--space-5); }

/* ═══ MODAL DE ÉXITO ═══ */
.success-content { display: flex; flex-direction: column; align-items: center; text-align: center; gap: var(--space-4); }
.success-icon-wrap {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--success-50);
  border: 2px solid var(--success-200);
  color: var(--success-600);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
  animation: successPop 0.5s var(--ease-spring);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.08); }
  100% { transform: scale(1); opacity: 1; }
}
.success-message {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0;
  line-height: var(--leading-relaxed);
  max-width: 380px;
}
.success-details {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  text-align: left;
}
.detail-row { display: flex; justify-content: space-between; align-items: center; gap: var(--space-3); font-size: var(--text-md); }
.detail-label { color: var(--text-secondary); font-weight: var(--font-medium); }
.detail-value { color: var(--text-primary); font-weight: var(--font-bold); text-align: right; }
.mono { font-family: var(--font-mono); font-size: var(--text-sm); }
.detail-row-total .amount { color: var(--brand-700); font-size: var(--text-lg); }
.detail-row-total .amount-bs { color: var(--neutral-700); font-size: var(--text-base); }
.detail-row-tasa .tasa { font-size: var(--text-sm); color: var(--text-tertiary); font-weight: var(--font-semibold); }

/* ═══ LOADING ═══ */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12);
  color: var(--text-secondary);
  font-size: var(--text-base);
}

/* ═══ TRANSICIONES ═══ */
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

/* ═══ RESPONSIVE ═══ */
@media (max-width: 900px) {
  .pay-layout { grid-template-columns: 1fr; }
  .summary-col { position: static; }
}
@media (max-width: 640px) {
  .pay-view { padding: var(--space-4); }
  .summary-card, .payment-card { padding: var(--space-4) var(--space-5); }
  .state-actions { flex-direction: column; width: 100%; }
  .state-actions :deep(.btn) { width: 100%; }
}
</style>