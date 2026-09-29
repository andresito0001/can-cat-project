<template>
  <div class="pay-view">
    <!-- ═══ Loading ═══ -->
    <div v-if="cargando" class="state-full">
      <Loader2 :size="36" class="spin" />
      <p>Cargando información de la cita...</p>
    </div>

    <!-- ═══ Error de carga ═══ -->
    <div v-else-if="errorCarga" class="state-full state-error">
      <AlertTriangle :size="48" />
      <h2>No pudimos cargar la cita</h2>
      <p>{{ errorCarga }}</p>
      <button class="btn-secondary" @click="volver">
        <ArrowLeft :size="15" /> Volver a Mis Citas
      </button>
    </div>

    <!-- ═══ Estado no pagable ═══ -->
    <div v-else-if="!esPagable" class="state-full">
      <component :is="estadoIcono" :size="52" :class="estadoIconClass" />
      <h2>{{ estadoTitulo }}</h2>
      <p>{{ estadoDescripcion }}</p>
      <div class="state-actions">
        <button class="btn-secondary" @click="volver">
          <ArrowLeft :size="15" /> Volver a Mis Citas
        </button>
        <button
          v-if="['Confirmada', 'Completada'].includes(cita.estado)"
          class="btn-primary"
          @click="irAlHistorial"
        >
          <ReceiptText :size="15" /> Ver en historial
        </button>
      </div>
    </div>

    <!-- ═══ Flujo de pago ═══ -->
    <template v-else>
      <header class="page-header">
        <button class="btn-back" @click="volver">
          <ArrowLeft :size="16" /> Volver
        </button>
        <div class="header-title">
          <h1>Pagar Cita</h1>
          <p>Completa el pago para confirmar tu reserva</p>
        </div>
      </header>

      <div class="pay-layout">
        <!-- ─── Columna izquierda: resumen ─── -->
        <aside class="summary-col">
          <!-- Countdown -->
          <CitaCountdown
            v-if="cita.expiraEn && !expirado"
            :expira-en="cita.expiraEn"
            @expirado="onExpirado"
          />

          <!-- Alerta de expirado -->
          <div v-if="expirado" class="alert-expired">
            <AlertTriangle :size="18" />
            <div>
              <strong>La reserva expiró</strong>
              <p>Este horario ya no está disponible. Vuelve a agendar una nueva cita.</p>
            </div>
          </div>

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

            <div class="divider"></div>

            <!-- Totales -->
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

          <!-- ═══ Datos bancarios de la clínica ═══ -->
          <div v-if="datosBancarios" class="banco-card">
            <header class="banco-head">
              <div class="banco-icon">
                <Landmark :size="16" />
              </div>
              <div>
                <h3 class="banco-title">Datos para pagar</h3>
                <p class="banco-sub">{{ datosBancarios.nombreTitular }}</p>
              </div>
            </header>

            <div class="banco-body">
              <!-- Pago Móvil -->
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
                  <span class="banco-valor mono">{{ datosBancarios.pagoMovil.telefono }}</span>
                </div>
                <div class="banco-fila">
                  <span class="banco-label">Cédula / RIF</span>
                  <span class="banco-valor mono">{{ datosBancarios.pagoMovil.cedula }}</span>
                </div>
              </div>

              <!-- Transferencia -->
              <div class="banco-bloque">
                <p class="banco-bloque-titulo">
                  <ArrowRightLeft :size="13" /> Transferencia
                </p>
                <div class="banco-fila">
                  <span class="banco-label">Banco</span>
                  <span class="banco-valor">{{ datosBancarios.bancoPrincipal.codigo }} {{ datosBancarios.bancoPrincipal.nombre }}</span>
                </div>
                <div class="banco-fila">
                  <span class="banco-label">Cuenta</span>
                  <span class="banco-valor mono">{{ datosBancarios.bancoPrincipal.cuenta }}</span>
                </div>
                <div class="banco-fila">
                  <span class="banco-label">RIF</span>
                  <span class="banco-valor mono">{{ datosBancarios.rif }}</span>
                </div>
              </div>

              <div v-if="datosBancarios.nota" class="banco-nota">
                <Info :size="13" />
                <span>{{ datosBancarios.nota }}</span>
              </div>
            </div>
          </div>
        </aside>

        <!-- ─── Columna derecha: formulario ─── -->
        <main class="form-col">
          <div class="payment-card">
            <h3 class="card-title">Método de pago</h3>
            <p class="card-hint">Selecciona cómo deseas pagar esta cita</p>

            <!-- Cargando métodos -->
            <div v-if="cargandoMetodos" class="mini-loading">
              <Loader2 :size="20" class="spin" />
              <span>Cargando métodos disponibles...</span>
            </div>

            <!-- Sin métodos disponibles -->
            <div v-else-if="metodos.length === 0" class="mini-empty">
              <AlertCircle :size="20" />
              <span>No hay métodos de pago disponibles en este momento.</span>
            </div>

            <!-- Grid de métodos -->
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
                  <div class="radio-dot" :class="{ filled: metodoSeleccionado?.id === m.id }"></div>
                </div>
              </button>
            </div>

            <!-- Campos dinámicos -->
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
                    <option value="" disabled>Selecciona un banco...</option>
                    <optgroup label="Bancos universales">
                      <option v-for="b in BANCOS_VE" :key="b.codigo" :value="`${b.codigo} - ${b.nombre}`">
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

                <!-- Referencia de transacción -->
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

            <!-- Error general -->
            <Transition name="slide-down">
              <div v-if="errorForm" class="alert-error">
                <AlertCircle :size="16" />
                <span>{{ errorForm }}</span>
              </div>
            </Transition>

            <!-- Botón confirmar -->
            <button
              class="btn-confirm"
              :disabled="!puedeConfirmar || procesando || expirado"
              @click="confirmarPago"
            >
              <Loader2 v-if="procesando" :size="18" class="spin" />
              <ShieldCheck v-else :size="18" />
              {{ procesando ? 'Procesando pago...' : 'Confirmar pago' }}
            </button>

            <p class="secure-hint">
              <Lock :size="12" />
              Verificaremos tu transacción antes de confirmar la cita.
            </p>
          </div>
        </main>
      </div>
    </template>

    <!-- ═══ Modal de éxito ═══ -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="pagoExitoso" class="modal-overlay">
          <Transition name="slide-up" appear>
            <div class="success-modal" role="dialog" aria-modal="true">
              <div class="success-icon">
                <CheckCircle2 :size="34" />
              </div>

              <h2>¡Pago registrado!</h2>
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
                <div v-if="pagoExitoso.montoUsd != null" class="detail-row detail-row--total">
                  <span class="detail-label">Total USD</span>
                  <span class="detail-value amount">{{ fmtUsd(pagoExitoso.montoUsd) }} USD</span>
                </div>
                <div v-if="pagoExitoso.montoBs != null" class="detail-row detail-row--total">
                  <span class="detail-label">Total Bs</span>
                  <span class="detail-value amount-bs">Bs. {{ fmtBs(pagoExitoso.montoBs) }}</span>
                </div>
                <div v-if="pagoExitoso.tasaCambio != null" class="detail-row detail-row--tasa">
                  <span class="detail-label">Tasa aplicada</span>
                  <span class="detail-value tasa">{{ fmtTasa(pagoExitoso.tasaCambio) }} Bs/USD</span>
                </div>
              </div>

              <div v-if="pagoExitoso.advertenciaEmail" class="alert-info">
                <Info :size="16" />
                <span>{{ pagoExitoso.advertenciaEmail }}</span>
              </div>

              <div class="success-actions">
                <button class="btn-secondary" :disabled="descargandoPdf"
                        @click="descargarFacturaActual">
                  <Loader2 v-if="descargandoPdf" :size="15" class="spin" />
                  <Download v-else :size="15" />
                  {{ descargandoPdf ? 'Generando...' : 'Descargar factura' }}
                </button>
                <button class="btn-primary" @click="irAlHistorial">
                  Ver historial
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
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowLeft, PawPrint, Stethoscope, ClipboardList, CalendarDays, Clock,
  Loader2, AlertTriangle, AlertCircle, CheckCircle2, Info,
  ShieldCheck, Lock, Download, ReceiptText,
  Landmark, Smartphone, ArrowRightLeft
} from 'lucide-vue-next'
import api from '@/api/axios.config'
import { getMetodosOnline, procesarPagoCita, descargarFacturaPdf, getDatosBancarios } from '@/api/pagos.api.js'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'
import CitaCountdown from '@/components/cliente/CitaCountdown.vue'

const route = useRoute()
const router = useRouter()
const { toastError, toastSuccess } = useToast()

const idCita = computed(() => Number(route.params.idCita))

// ─── Estado de la cita ───
const cargando = ref(true)
const errorCarga = ref('')
const cita = ref(null)
const expirado = ref(false)

// ─── Métodos de pago ───
const cargandoMetodos = ref(true)
const metodos = ref([])
const metodoSeleccionado = ref(null)

// ─── Formulario ───
const datosPago = ref({})
const referenciaTransaccion = ref('')
const errorForm = ref('')
const procesando = ref(false)
const erroresCampos = ref({})

// ─── Resultado ───
const pagoExitoso = ref(null)
const descargandoPdf = ref(false)

// ─── Datos bancarios ───
const datosBancarios = ref(null)

// ═══════════════════════════════════════════════════════════
// VALIDACIONES DE CAMPOS DE PAGO
// ═══════════════════════════════════════════════════════════
const SOLO_DIGITOS = /^\d+$/

/** Referencia de transacción: 4-20 dígitos */
function validarReferencia(v) {
  const s = String(v || '').trim()
  if (!s) return 'La referencia es obligatoria'
  if (!SOLO_DIGITOS.test(s)) return 'La referencia debe contener solo números'
  if (s.length < 4) return 'Mínimo 4 dígitos'
  if (s.length > 20) return 'Máximo 20 dígitos'
  return null
}

/** Teléfono Venezuela: 04XX-XXXXXXX (11 dígitos), acepta guiones/espacios */
function validarTelefono(v) {
  const s = String(v || '').replace(/[\s\-()]/g, '')
  if (!s) return 'El teléfono es obligatorio'
  if (!SOLO_DIGITOS.test(s)) return 'El teléfono debe contener solo números'
  if (!s.startsWith('04')) return 'Debe comenzar con 04 (Venezuela)'
  if (s.length !== 11) return 'Debe tener 11 dígitos (ej: 04141234567)'
  return null
}

/** Número de cuenta Venezuela: 20 dígitos */
function validarCuenta(v) {
  const s = String(v || '').replace(/[\s\-]/g, '')
  if (!s) return 'El número de cuenta es obligatorio'
  if (!SOLO_DIGITOS.test(s)) return 'Solo números'
  if (s.length !== 20) return 'Debe tener 20 dígitos'
  return null
}

/** Últimos 4 dígitos de tarjeta */
function validarUltimosDigitos(v) {
  const s = String(v || '').trim()
  if (!s) return 'Requerido'
  if (!SOLO_DIGITOS.test(s)) return 'Solo números'
  if (s.length !== 4) return 'Deben ser exactamente 4 dígitos'
  return null
}

/** Lote: 4-10 dígitos */
function validarLote(v) {
  const s = String(v || '').trim()
  if (!s) return 'El lote es obligatorio'
  if (!SOLO_DIGITOS.test(s)) return 'Solo números'
  if (s.length < 4 || s.length > 10) return 'Entre 4 y 10 dígitos'
  return null
}

/** Mapa de validador por campo */
const VALIDADORES_CAMPO = {
  telefono: validarTelefono,
  numero_cuenta: validarCuenta,
  ultimos_digitos: validarUltimosDigitos,
  lote: validarLote,
}

// ─── Computados ───
const esPagable = computed(() => cita.value?.estado === 'Pendiente_Pago')

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

// ─── Estado "no pagable" ───
const estadoIcono = computed(() => {
  const map = {
    Confirmada: CheckCircle2,
    Completada: CheckCircle2,
    Cancelada: AlertCircle,
    En_Atencion: AlertTriangle,
  }
  return map[cita.value?.estado] || AlertCircle
})

const estadoIconClass = computed(() => {
  const ok = ['Confirmada', 'Completada']
  return ok.includes(cita.value?.estado) ? 'icon-ok' : 'icon-warn'
})

const estadoTitulo = computed(() => {
  const map = {
    Confirmada: 'Esta cita ya está confirmada',
    Completada: 'Esta cita ya fue completada',
    Cancelada: 'Esta cita fue cancelada',
    En_Atencion: 'Esta cita está en atención',
  }
  return map[cita.value?.estado] || 'Esta cita no se puede pagar'
})

const estadoDescripcion = computed(() => {
  const map = {
    Confirmada: 'La cita fue confirmada. Revisa los detalles en tu historial.',
    Completada: 'La consulta fue atendida. Revisa el historial de pagos si necesitas el comprobante.',
    Cancelada: 'No es posible pagar una cita cancelada. Agenda una nueva desde "Solicitar Cita".',
    En_Atencion: 'La cita ya está siendo atendida por el veterinario.',
  }
  return map[cita.value?.estado] || 'El estado actual no permite pagar esta cita.'
})

// ═══════════════════════════════════════════════════════════
// BANCOS DE VENEZUELA — códigos oficiales SUDEBAN
// ═══════════════════════════════════════════════════════════
const BANCOS_VE = [
  { codigo: '0102', nombre: 'Banco de Venezuela' },
  { codigo: '0104', nombre: 'Venezolano de Crédito' },
  { codigo: '0105', nombre: 'Mercantil' },
  { codigo: '0108', nombre: 'BBVA Provincial' },
  { codigo: '0114', nombre: 'Bancaribe' },
  { codigo: '0115', nombre: 'Banco Exterior' },
  { codigo: '0128', nombre: 'Banco Caroní' },
  { codigo: '0134', nombre: 'Banesco' },
  { codigo: '0137', nombre: 'Sofitasa' },
  { codigo: '0138', nombre: 'Banco Plaza' },
  { codigo: '0146', nombre: 'Bangente' },
  { codigo: '0151', nombre: 'BFC Banco Fondo Común' },
  { codigo: '0156', nombre: '100% Banco' },
  { codigo: '0157', nombre: 'DelSur' },
  { codigo: '0163', nombre: 'Banco del Tesoro' },
  { codigo: '0166', nombre: 'Banco Agrícola de Venezuela' },
  { codigo: '0168', nombre: 'Bancrecer' },
  { codigo: '0169', nombre: 'Mi Banco' },
  { codigo: '0171', nombre: 'Banco Activo' },
  { codigo: '0172', nombre: 'Bancamiga' },
  { codigo: '0174', nombre: 'Banplus' },
  { codigo: '0175', nombre: 'Banco Bicentenario' },
  { codigo: '0177', nombre: 'Banfanb' },
  { codigo: '0191', nombre: 'BNC (Banco Nacional de Crédito)' },
]

const FIELD_META = {
  banco:           { label: 'Banco',              placeholder: 'Selecciona un banco', type: 'text' },
  numero_cuenta:   { label: 'Número de cuenta',   placeholder: '0102-0123-45-6789012345', type: 'text', inputmode: 'numeric', maxlength: 30 },
  telefono:        { label: 'Teléfono asociado',  placeholder: '0414-1234567', type: 'tel', inputmode: 'tel', maxlength: 15 },
  ultimos_digitos: { label: 'Últimos 4 dígitos',  placeholder: '1234', type: 'text', inputmode: 'numeric', maxlength: 4 },
  lote:            { label: 'Lote / aprobación',  placeholder: '987654', type: 'text', inputmode: 'numeric', maxlength: 10 },
}

function labelCampo(k) {
  return FIELD_META[k]?.label || k.replaceAll('_', ' ')
}
function placeholderCampo(k) {
  return FIELD_META[k]?.placeholder || ''
}
function tipoCampo(k) {
  return FIELD_META[k]?.type || 'text'
}
function inputModeCampo(k) {
  return FIELD_META[k]?.inputmode || 'text'
}
function maxlengthCampo(k) {
  return FIELD_META[k]?.maxlength || undefined
}

// ─── Carga inicial ───
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
    console.log('[banco] llamando...')
    const { data } = await getDatosBancarios()
    console.log('[banco] OK:', data)
    datosBancarios.value = data
  } catch (err) {
    console.error('[banco] ERROR:', err?.response?.status, err?.response?.data, err?.message)
    datosBancarios.value = null
  }
}

// ─── Selección de método ───
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

// ─── Validación global del formulario ───
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

// ─── Confirmar pago ───
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

// ─── Expirado ───
function onExpirado() {
  expirado.value = true
  if (!errorForm.value) {
    errorForm.value = 'La reserva expiró. Este horario ya no está disponible.'
  }
}

// ─── Descargar factura ───
async function descargarFacturaActual() {
  if (!pagoExitoso.value?.idFactura) return
  descargandoPdf.value = true
  try {
    const { blob, filename } = await descargarFacturaPdf(pagoExitoso.value.idFactura)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
    URL.revokeObjectURL(url)
    toastSuccess('Factura descargada')
  } catch (err) {
    toastError('No se pudo descargar la factura. Intenta más tarde.')
  } finally {
    descargandoPdf.value = false
  }
}

// ─── Navegación ───
function volver() {
  router.push('/cliente/mis-citas')
}
function irAlHistorial() {
  router.push('/cliente/historial-pagos')
}

// ─── Helpers ───
function fmtUsd(v) {
  if (v == null) return '0.00'
  return Number(v).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function fmtBs(v) {
  if (v == null) return '0,00'
  return Number(v).toLocaleString('es-VE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}
function fmtTasa(v) {
  if (v == null) return '—'
  return Number(v).toLocaleString('es-VE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
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
</script>

<style scoped>
.pay-view {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #0F172A;
}
button { font-family: inherit; }

/* ── Estados de pantalla completa ── */
.state-full {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 80px 24px; gap: 16px; color: #94A3B8; text-align: center;
  background: #fff; border: 1px solid #E2E8F0; border-radius: 16px; min-height: 320px;
}
.state-full h2 { font-size: 20px; font-weight: 700; color: #1E293B; margin: 0; }
.state-full p { font-size: 14px; color: #64748B; max-width: 460px; margin: 0; line-height: 1.55; }
.state-error { color: #DC2626; }
.state-error svg { color: #DC2626; }
.state-actions { display: flex; gap: 10px; margin-top: 8px; flex-wrap: wrap; justify-content: center; }
.icon-ok { color: #059669; }
.icon-warn { color: #DC2626; }

.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ── Header ── */
.page-header {
  display: flex; align-items: center; gap: 16px; margin-bottom: 24px;
}
.btn-back {
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 12px; background: #fff; border: 1px solid #E2E8F0;
  border-radius: 9px; font-size: 13px; font-weight: 600; color: #475569;
  cursor: pointer; transition: all .15s;
}
.btn-back:hover { background: #F8FAFC; border-color: #CBD5E1; color: #0F766E; }
.header-title h1 { margin: 0; font-size: 22px; font-weight: 700; color: #0F172A; }
.header-title p { margin: 2px 0 0; font-size: 13.5px; color: #64748B; }

/* ── Layout 2 columnas ── */
.pay-layout {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 20px;
  align-items: start;
}
@media (max-width: 900px) { .pay-layout { grid-template-columns: 1fr; } }

/* ── Card base ── */
.summary-card, .payment-card {
  background: #fff; border: 1px solid #E2E8F0; border-radius: 14px;
  padding: 22px 24px;
}
.card-title { margin: 0 0 4px; font-size: 15px; font-weight: 700; color: #0F172A; }
.card-hint { margin: 0 0 18px; font-size: 12.5px; color: #64748B; }

/* ── Columna izquierda: resumen ── */
.summary-col { display: flex; flex-direction: column; gap: 14px; position: sticky; top: 24px; }
@media (max-width: 900px) { .summary-col { position: static; } }

.summary-row {
  display: flex; align-items: flex-start; gap: 12px; padding: 10px 0;
  border-bottom: 1px solid #F1F5F9;
}
.summary-row:last-of-type { border-bottom: none; }
.summary-icon { color: #94A3B8; margin-top: 3px; flex-shrink: 0; }
.summary-text { display: flex; flex-direction: column; min-width: 0; }
.summary-label { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: .5px; color: #94A3B8; }
.summary-value { font-size: 14px; font-weight: 600; color: #1E293B; margin-top: 2px; }

.divider { height: 1px; background: #E2E8F0; margin: 12px 0 14px; }

.total-block { display: flex; flex-direction: column; gap: 8px; }
.total-line {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 13.5px;
}
.total-line > span:first-child { color: #64748B; font-weight: 500; }
.total-usd { font-size: 20px; font-weight: 800; color: #0F766E; }
.total-bs-line { padding-top: 6px; border-top: 1px dashed #E2E8F0; }
.total-bs { font-size: 15px; font-weight: 700; color: #334155; }
.tasa-hint { font-size: 11.5px; color: #94A3B8; margin: 4px 0 0; text-align: right; }

/* ── Alerta expirado ── */
.alert-expired {
  display: flex; align-items: flex-start; gap: 10px; padding: 12px 14px;
  background: #FEF2F2; border: 1px solid #FECACA; color: #B91C1C;
  border-radius: 10px; font-size: 13px;
}
.alert-expired svg { flex-shrink: 0; margin-top: 1px; }
.alert-expired strong { display: block; margin-bottom: 2px; }
.alert-expired p { margin: 0; font-weight: 500; font-size: 12.5px; line-height: 1.45; }

/* ── Métodos ── */
.mini-loading, .mini-empty {
  display: flex; align-items: center; gap: 10px;
  padding: 20px; color: #94A3B8; font-size: 13px;
  background: #F8FAFC; border-radius: 10px; border: 1px dashed #E2E8F0;
}
.method-grid { display: flex; flex-direction: column; gap: 10px; }

.method-card {
  display: grid;
  grid-template-columns: 44px 1fr 22px;
  gap: 14px; align-items: center;
  padding: 14px 16px; background: #fff; border: 1.5px solid #E2E8F0;
  border-radius: 12px; cursor: pointer; text-align: left;
  transition: all .15s; font-family: inherit;
}
.method-card:hover { border-color: #CBD5E1; background: #F8FAFC; }
.method-card.active {
  border-color: #0F766E; background: #F0FDFA;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.method-icon {
  width: 44px; height: 44px; border-radius: 11px;
  background: #F1F5F9; color: #64748B;
  display: flex; align-items: center; justify-content: center;
}
.method-card.active .method-icon { background: #CCFBF1; color: #0F766E; }
.method-info { display: flex; flex-direction: column; gap: 3px; min-width: 0; }
.method-name { font-size: 14px; font-weight: 700; color: #1E293B; }
.method-desc { font-size: 12px; color: #64748B; line-height: 1.35; }
.method-radio { display: flex; justify-content: flex-end; }
.radio-dot {
  width: 18px; height: 18px; border-radius: 50%;
  border: 2px solid #CBD5E1; background: #fff; transition: all .15s;
}
.radio-dot.filled {
  border-color: #0F766E; background: #0F766E;
  box-shadow: inset 0 0 0 3px #fff;
}

/* ── Campos dinámicos ── */
.dynamic-fields { margin-top: 20px; display: flex; flex-direction: column; gap: 14px; }
.fields-title {
  margin: 0 0 4px; font-size: 11px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .6px; color: #64748B;
  padding-bottom: 6px; border-bottom: 1px dashed #E2E8F0;
}

.form-field { display: flex; flex-direction: column; gap: 6px; }
.form-field label { font-size: 12.5px; font-weight: 600; color: #475569; }
.required { color: #EF4444; margin-left: 2px; }
.form-field input {
  width: 100%; padding: 10px 14px;
  border: 1px solid #E2E8F0; border-radius: 10px;
  font-size: 14px; color: #1E293B; background: #fff;
  transition: border-color .15s, box-shadow .15s;
  font-family: inherit; box-sizing: border-box;
}
.form-field input::placeholder { color: #94A3B8; }
.form-field input:focus {
  outline: none; border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.form-field select,
.form-select {
  width: 100%; padding: 10px 36px 10px 14px;
  border: 1px solid #E2E8F0; border-radius: 10px;
  font-size: 14px; color: #1E293B; background-color: #fff;
  font-family: inherit; box-sizing: border-box;
  cursor: pointer; appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  transition: border-color .15s, box-shadow .15s;
}
.form-field select:focus,
.form-select:focus {
  outline: none; border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.form-field select option { padding: 8px; }
.form-field select option:disabled { color: #94A3B8; }
.form-field select optgroup {
  font-weight: 700; color: #0F766E; font-size: 11.5px;
  text-transform: uppercase; letter-spacing: .5px; padding: 6px 0;
}

.field-hint { font-size: 11px; color: #94A3B8; margin-top: 2px; }

/* ── Validación de errores en campos ── */
.form-field input.field-invalid,
.form-field select.field-invalid {
  border-color: #EF4444;
  background: #FEF2F2;
}
.form-field input.field-invalid:focus,
.form-field select.field-invalid:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12);
}
.field-error {
  font-size: 11.5px;
  color: #EF4444;
  font-weight: 600;
  margin-top: 2px;
}

/* ── Alertas ── */
.alert-error, .alert-info {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 12px 14px; border-radius: 10px; font-size: 13px;
  line-height: 1.45; margin-top: 16px;
}
.alert-error { background: #FEF2F2; color: #B91C1C; border: 1px solid #FECACA; font-weight: 500; }
.alert-info { background: #EFF6FF; color: #1D4ED8; border: 1px solid #BFDBFE; }
.alert-error svg, .alert-info svg { flex-shrink: 0; margin-top: 1px; }

/* ── Botón confirmar ── */
.btn-confirm {
  width: 100%; margin-top: 20px;
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  padding: 13px 20px; background: #0F766E; color: #fff;
  border: none; border-radius: 11px; font-size: 14.5px; font-weight: 700;
  cursor: pointer; transition: all .15s; font-family: inherit;
}
.btn-confirm:hover:not(:disabled) {
  background: #0E6862;
  box-shadow: 0 6px 16px -6px rgba(15, 118, 110, .4);
}
.btn-confirm:disabled { opacity: .5; cursor: not-allowed; }
.secure-hint {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  font-size: 11.5px; color: #94A3B8; margin: 10px 0 0; text-align: center;
}

/* ── Botones secundarios ── */
.btn-secondary, .btn-primary {
  display: inline-flex; align-items: center; justify-content: center; gap: 7px;
  padding: 10px 18px; border-radius: 10px; font-size: 13.5px;
  font-weight: 700; cursor: pointer; font-family: inherit;
  transition: all .15s; border: 1px solid #E2E8F0;
}
.btn-secondary { background: #fff; color: #475569; }
.btn-secondary:hover:not(:disabled) { background: #F1F5F9; border-color: #CBD5E1; }
.btn-secondary:disabled { opacity: .55; cursor: not-allowed; }
.btn-primary {
  background: #0F766E; color: #fff; border-color: #0F766E;
}
.btn-primary:hover { background: #0E6862; border-color: #0E6862; }

/* ── Modal éxito ── */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(15, 23, 42, .55);
  backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center;
  z-index: 100; padding: 24px;
}
.success-modal {
  background: #fff; border-radius: 20px; max-width: 460px; width: 100%;
  padding: 32px 28px 26px; text-align: center;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .3);
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}
.success-icon {
  width: 64px; height: 64px; border-radius: 18px;
  background: #ECFDF5; color: #059669; margin: 0 auto 16px;
  display: flex; align-items: center; justify-content: center;
}
.success-modal h2 { font-size: 20px; font-weight: 700; color: #1E293B; margin: 0 0 8px; }
.success-message {
  font-size: 13.5px; color: #64748B; margin: 0 0 20px; line-height: 1.5;
}
.success-details {
  display: flex; flex-direction: column; gap: 8px;
  padding: 14px 16px; background: #F8FAFC; border-radius: 12px;
  margin-bottom: 18px; text-align: left;
}
.detail-row { display: flex; justify-content: space-between; align-items: center; font-size: 13px; }
.detail-label { color: #64748B; font-weight: 500; }
.detail-value { color: #1E293B; font-weight: 700; }
.mono { font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace; font-size: 12.5px; }

.success-actions { display: flex; gap: 10px; }
.success-actions button { flex: 1; justify-content: center; }

/* ── Transiciones ── */
.fade-enter-active, .fade-leave-active { transition: opacity .2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-up-enter-active, .slide-up-leave-active {
  transition: all .3s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateY(24px) scale(.97); }
.slide-down-enter-active, .slide-down-leave-active { transition: all .2s ease; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-6px); max-height: 0; }

@media (max-width: 640px) {
  .success-actions { flex-direction: column; }
  .summary-card, .payment-card { padding: 18px 18px; }
}

.detail-row--total .amount { color: #0F766E; font-size: 14px; }
.detail-row--total .amount-bs { color: #334155; font-size: 14px; font-weight: 700; }
.detail-row--tasa .tasa { font-size: 11.5px; color: #94A3B8; font-weight: 600; }

/* ═══ Datos bancarios de la clínica ═══ */
.banco-card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  overflow: hidden;
}
.banco-head {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 18px;
  background: #F8FAFC;
  border-bottom: 1px solid #E2E8F0;
}
.banco-icon {
  width: 34px; height: 34px; border-radius: 9px;
  background: #EFF6FF; color: #2563EB;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.banco-title { margin: 0; font-size: 14px; font-weight: 700; color: #0F172A; }
.banco-sub { margin: 2px 0 0; font-size: 11.5px; color: #64748B; }
.banco-body { padding: 14px 18px 16px; display: flex; flex-direction: column; gap: 14px; }

.banco-bloque { display: flex; flex-direction: column; gap: 6px; }
.banco-bloque-titulo {
  display: inline-flex; align-items: center; gap: 5px;
  margin: 0 0 4px;
  font-size: 10.5px; font-weight: 700;
  text-transform: uppercase; letter-spacing: .5px;
  color: #2563EB;
}
.banco-fila {
  display: flex; justify-content: space-between; align-items: center;
  gap: 12px; font-size: 12.5px;
  padding: 6px 0;
  border-bottom: 1px dashed #F1F5F9;
}
.banco-fila:last-child { border-bottom: none; }
.banco-label { color: #64748B; font-weight: 500; }
.banco-valor { color: #0F172A; font-weight: 700; text-align: right; word-break: break-word; }
.banco-valor.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12px;
}
.banco-nota {
  display: flex; align-items: flex-start; gap: 8px;
  padding: 10px 12px;
  background: #FFFBEB; color: #92400E;
  border: 1px solid #FDE68A; border-radius: 8px;
  font-size: 11.5px; line-height: 1.45;
}
.banco-nota svg { flex-shrink: 0; margin-top: 1px; }
</style>