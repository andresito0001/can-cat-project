<template>
  <div class="appointment-view">
    <!-- ═══════════ PAGE HEADER ═══════════ -->
    <div class="page-header">
      <div>
        <h1>Solicitar Cita</h1>
        <p class="page-subtitle">Reserva un turno de atención médica para tu mascota</p>
      </div>
    </div>

    <!-- ═══════════ STEPPER ═══════════ -->
    <div class="stepper">
      <div
        v-for="(step, idx) in steps"
        :key="idx"
        class="stepper-step"
        :class="{
          active: currentStep === step.num,
          completed: currentStep > step.num
        }"
      >
        <div class="stepper-circle">
          <Check v-if="currentStep > step.num" :size="18" />
          <span v-else>{{ step.num }}</span>
        </div>
        <span class="stepper-label">{{ step.label }}</span>
        <div v-if="idx < steps.length - 1" class="stepper-line" />
      </div>
    </div>

    <!-- ═══════════ STEP 1: DATOS ═══════════ -->
    <div v-if="currentStep === 1" class="step-panel">
      <!-- Loading inicial -->
      <div v-if="isLoadingInitial" class="loading-box">
        <div class="spinner" />
        <p>Cargando datos necesarios...</p>
      </div>

      <template v-else>
        <!-- Error de carga -->
        <div v-if="loadError" class="alert alert-error">
          <AlertTriangle :size="18" />
          <span>{{ loadError }}</span>
          <button class="alert-action" @click="loadInitialData">Reintentar</button>
        </div>

        <template v-else>
          <div class="card">
            <div class="card-header">
              <h3><ClipboardList :size="18" /> Datos de la Cita</h3>
            </div>
            <div class="card-body">
              <div class="form-grid">
                <!-- Mascota -->
                <div class="form-group">
                  <label class="form-label" for="mascota">Mascota <span class="required">*</span></label>
                  <select
                    id="mascota"
                    v-model="selectedMascota"
                    class="form-select"
                    :class="{ 'is-invalid': stepErrors.mascota }"
                  >
                    <option :value="null" disabled>Selecciona una mascota</option>
                    <option
                      v-for="m in mascotas"
                      :key="m.idMascota"
                      :value="m.idMascota"
                    >
                      {{ m.nombre }} — {{ m.nombreRaza }} ({{ m.nombreEspecie }})
                    </option>
                  </select>
                  <span v-if="stepErrors.mascota" class="form-error">{{ stepErrors.mascota }}</span>
                </div>
                <!-- Veterinario -->
                <div class="form-group">
                  <label class="form-label" for="veterinario">Veterinario <span class="required">*</span></label>
                  <select
                    id="veterinario"
                    v-model="selectedVeterinario"
                    class="form-select"
                    :class="{ 'is-invalid': stepErrors.veterinario }"
                    @change="onVeterinarioChange"
                  >
                    <option :value="null" disabled>Selecciona un veterinario</option>
                    <option
                      v-for="v in veterinarios"
                      :key="v.id"
                      :value="v.id"
                    >
                      {{ v.nombre }}
                    </option>
                  </select>
                  <span v-if="stepErrors.veterinario" class="form-error">{{ stepErrors.veterinario }}</span>

                  <!-- Especialidad del veterinario seleccionado -->
                  <div v-if="vetSeleccionado" class="service-detail" style="margin-top: 8px;">
                    <div class="service-meta">
                      <span class="service-badge">{{ vetSeleccionado.especialidad }}</span>
                      <span class="service-meta-item">
                        <Stethoscope :size="14" />
                        Especialidad
                      </span>
                    </div>
                  </div>
                </div>
                <!-- Servicio -->
                <div class="form-group full-width">
                  <label class="form-label" for="servicio">
                    Servicio <span class="required">*</span>
                    <span v-if="isLoadingServicios" class="label-loading">
                      <div class="spinner spinner-sm" style="border-top-color: #0F766E;" />
                      Cargando...
                    </span>
                  </label>
                  <select
                    id="servicio"
                    v-model="selectedServicio"
                    class="form-select"
                    :class="{ 'is-invalid': stepErrors.servicio }"
                    :disabled="!selectedVeterinario || isLoadingServicios"
                  >
                    <option :value="null" disabled>
                      {{ !selectedVeterinario ? 'Selecciona un veterinario primero' : 'Selecciona un servicio' }}
                    </option>
                    <option
                      v-for="s in servicios"
                      :key="s.id"
                      :value="s.id"
                    >
                      {{ s.nombre }} — {{ formatCurrency(s.precioUsd) }}
                    </option>
                  </select>
                  <span v-if="stepErrors.servicio" class="form-error">{{ stepErrors.servicio }}</span>

                  <!-- Info del servicio seleccionado -->
                  <div v-if="servicioSeleccionado" class="service-detail">
                    <p v-if="servicioSeleccionado.descripcion" class="service-desc">
                      {{ servicioSeleccionado.descripcion }}
                    </p>
                    <div class="service-meta">
                      <span class="service-meta-item">
                        <Clock :size="14" />
                        {{ servicioSeleccionado.duracionMinutos }} min
                      </span>
                      <span class="service-meta-item">
                        <DollarSign :size="14" />
                        {{ formatCurrency(servicioSeleccionado.precioUsd) }}
                      </span>
                      <span v-if="servicioSeleccionado.tipoAtencion" class="service-badge">
                        {{ servicioSeleccionado.tipoAtencion }}
                      </span>
                    </div>
                  </div>
                </div>

                <!-- Motivo de consulta -->
                <div class="form-group full-width">
                  <label class="form-label" for="motivo">Motivo de la consulta <span class="required">*</span></label>
                  <textarea
                    id="motivo"
                    v-model="motivoConsulta"
                    class="form-textarea"
                    :class="{ 'is-invalid': stepErrors.motivo }"
                    placeholder="Describe brevemente el motivo de la consulta..."
                    maxlength="1000"
                    rows="4"
                  />
                  <div class="form-footer">
                    <span v-if="stepErrors.motivo" class="form-error">{{ stepErrors.motivo }}</span>
                    <span class="char-count">{{ motivoConsulta.length }}/1000</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </template>
    </div>

    <!-- ═══════════ STEP 2: HORARIO ═══════════ -->
    <div v-if="currentStep === 2" class="step-panel">
      <div class="card">
        <div class="card-header">
          <h3><CalendarDays :size="18" /> Seleccionar Horario</h3>
          <span class="vet-badge">
            <Stethoscope :size="14" />
            {{ vetSeleccionado?.nombre || '' }} · {{ servicioSeleccionado?.nombre || '' }}
          </span>
        </div>
        <div class="card-body">
          <!-- Calendario -->
          <div class="calendar">
            <div class="calendar-nav">
              <button
                class="cal-nav-btn"
                :disabled="!canGoPrevMonth"
                @click="prevMonth"
              >
                <ChevronLeft :size="18" />
              </button>
              <span class="cal-month-year">{{ calendarMonthLabel }}</span>
              <button
                class="cal-nav-btn"
                :disabled="!canGoNextMonth"
                @click="nextMonth"
              >
                <ChevronRight :size="18" />
              </button>
            </div>

            <!-- Días de la semana -->
            <div class="calendar-weekdays">
              <span v-for="d in DIAS_SEMANA" :key="d" class="cal-weekday">{{ d }}</span>
            </div>

            <!-- Grid de días -->
            <div class="calendar-grid">
              <button
                v-for="(day, idx) in calendarDays"
                :key="idx"
                class="cal-day"
                :class="{
                  'other-month': !day.isCurrentMonth,
                  'is-past': day.isPast,
                  'is-today': day.isToday,
                  'is-selected': isDateSelected(day.date),
                  'has-slots': hasSlots(day.date),
                  'no-slots': hasNoSlots(day.date)
                }"
                :disabled="!day.isCurrentMonth || day.isPast"
                @click="selectDate(day)"
              >
                {{ day.date.getDate() }}
              </button>
            </div>
          </div>

          <!-- Alerta: sin disponibilidad -->
          <div v-if="showNoSlotsAlert" class="alert alert-warning">
            <AlertTriangle :size="18" />
            <span>No hay horarios disponibles para este día. Selecciona otro día en el calendario.</span>
          </div>

          <!-- Error de disponibilidad -->
          <div v-if="disponibilidadError" class="alert alert-error">
            <AlertTriangle :size="18" />
            <span>{{ disponibilidadError }}</span>
            <button class="alert-action" @click="retryDisponibilidad">Reintentar</button>
          </div>

          <!-- Bloques horarios -->
          <div v-if="selectedDate && !disponibilidadError" class="slots-section">
            <h4 class="slots-title">
              <Clock :size="16" />
              Horarios disponibles — {{ formatDateDisplay(selectedDate) }}
            </h4>

            <!-- Loading bloques -->
            <div v-if="isLoadingSlots" class="slots-loading">
              <div class="spinner spinner-sm" />
              <span>Consultando disponibilidad...</span>
            </div>

            <!-- Grid de bloques -->
            <div v-else-if="currentSlots.length > 0" class="slots-grid">
              <button
                v-for="bloque in currentSlots"
                :key="bloque.horaInicio"
                class="slot-btn"
                :class="{ 'is-selected': isBloqueSelected(bloque) }"
                @click="selectBloque(bloque)"
              >
                <span class="slot-time">{{ formatTime12h(bloque.horaInicio) }}</span>
                <span class="slot-separator">a</span>
                <span class="slot-time slot-end">{{ formatTime12h(bloque.horaFin) }}</span>
              </button>
            </div>

            <!-- Sin bloques (sin alerta ya mostrada arriba) -->
            <div
              v-else-if="currentSlots.length === 0 && fechaFueConsultada"
              class="slots-empty"
            />
          </div>

          <!-- Resumen mini antes de confirmar -->
          <div v-if="selectedBloque" class="mini-summary">
            <div class="mini-summary-row">
              <span class="mini-label">Fecha</span>
              <span class="mini-value">{{ formatDateDisplay(selectedDate) }}</span>
            </div>
            <div class="mini-summary-row">
              <span class="mini-label">Horario</span>
              <span class="mini-value">
                {{ formatTime12h(selectedBloque.horaInicio) }} — {{ formatTime12h(selectedBloque.horaFin) }}
              </span>
            </div>
            <div class="mini-summary-row">
              <span class="mini-label">Duración</span>
              <span class="mini-value">{{ servicioSeleccionado?.duracionMinutos }} min</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ═══════════ STEP 3: PASARELA DE PAGO ═══════════ -->
    <div v-if="currentStep === 3" class="step-panel">
      <div class="payment-layout">
        <!-- Columna izquierda: resumen -->
        <div class="card">
          <div class="card-header">
            <h3><FileText :size="18" /> Detalles de la Cita</h3>
          </div>
          <div class="card-body">
            <div class="summary-rows">
              <div class="summary-row">
                <span class="summary-label"><PawPrint :size="15" /> Mascota</span>
                <span class="summary-value">{{ citaResumen?.mascota }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-label"><Stethoscope :size="15" /> Veterinario</span>
                <span class="summary-value">{{ citaResumen?.veterinario }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-label"><ClipboardList :size="15" /> Servicio</span>
                <span class="summary-value">{{ citaResumen?.servicio }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-label"><CalendarDays :size="15" /> Fecha</span>
                <span class="summary-value">{{ formatDateDisplay(citaResumen?.fecha) }}</span>
              </div>
              <div class="summary-row">
                <span class="summary-label"><Clock :size="15" /> Horario</span>
                <span class="summary-value">
                  {{ formatTime12h(citaResumen?.horaInicio) }} — {{ formatTime12h(citaResumen?.horaFin) }}
                </span>
              </div>
              <div class="summary-row full">
                <span class="summary-label"><MessageSquare :size="15" /> Motivo</span>
                <span class="summary-value motivo-text">{{ motivoConsulta }}</span>
              </div>
            </div>

            <div class="payment-divider" style="margin: 16px 0 0 0;" />

            <div class="payment-rows" style="margin-top: 0;">
              <div class="payment-row">
                <span>Servicio</span>
                <span>{{ formatCurrency(citaResumen?.costoUsd) }}</span>
              </div>
              <div class="payment-row">
                <span>Tasa de cambio</span>
                <span>{{ citaResumen?.tasaCambio }} Bs/$</span>
              </div>
              <div class="payment-divider" />
              <div class="payment-row payment-total">
                <span>Total a pagar</span>
                <span>{{ formatCurrencyBs(citaResumen?.costoBs) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Columna derecha: pasarela -->
        <div class="card payment-gateway-card">
          <div class="card-header">
            <h3><CreditCard :size="18" /> Método de Pago</h3>
            <span class="reserved-badge">
              <Lock :size="12" />
              Horario reservado
            </span>
          </div>
          <div class="card-body">
            <!-- Error de pago -->
            <div v-if="pagoError" class="alert alert-error" style="margin-top: 0; margin-bottom: 16px;">
              <AlertTriangle :size="18" />
              <span>{{ pagoError }}</span>
            </div>

            <!-- Loading métodos -->
            <div v-if="isLoadingMetodos" class="slots-loading" style="padding: 32px 0;">
              <div class="spinner spinner-sm" />
              <span>Cargando métodos de pago...</span>
            </div>

            <template v-else>
              <!-- Selección de método -->
              <div v-for="m in metodosPago" :key="m.id" class="method-option" :class="{ active: selectedMetodoPago === m.id }" @click="selectedMetodoPago = m.id; onMetodoPagoChange()">
                <div class="method-radio">
                  <div class="radio-outer" :class="{ checked: selectedMetodoPago === m.id }">
                    <div v-if="selectedMetodoPago === m.id" class="radio-inner" />
                  </div>
                </div>
                <div class="method-info">
                  <span class="method-name">{{ m.nombre === 'Pago_Movil' ? 'Pago Móvil' : m.nombre }}</span>
                  <span class="method-desc">{{ m.descripcion }}</span>
                </div>
              </div>

              <!-- Campos dinámicos -->
              <div
                v-if="selectedMetodoPago && (camposDinamicos.length > 0 || metodoSeleccionado?.camposRequeridos?.referencia)"
                class="dynamic-fields"
              >
                <!-- Campo referencia solo si el método lo requiere -->
                <div v-if="metodoSeleccionado?.camposRequeridos?.referencia" class="form-group">
                  <label class="form-label">
                    Número de referencia <span class="required">*</span>
                  </label>
                  <input
                    type="text"
                    class="form-select"
                    :class="{ 'is-invalid': pagoFormErrors.referencia }"
                    placeholder="Ej: 0000123456789"
                    v-model="referenciaTransaccion"
                  />
                  <span v-if="pagoFormErrors.referencia" class="form-error">{{ pagoFormErrors.referencia }}</span>
                </div>

                <!-- Resto de campos dinámicos -->
                <div v-for="campo in camposDinamicos" :key="campo.key" class="form-group">
                  <label class="form-label">
                    {{ formatFieldLabel(campo.key) }} <span class="required">*</span>
                  </label>
                  <!-- Si el campo es "banco", mostrar select -->
                  <select
                    v-if="campo.key === 'banco'"
                    class="form-select"
                    :class="{ 'is-invalid': pagoFormErrors[campo.key] }"
                    v-model="datosPago[campo.key]"
                  >
                    <option value="" disabled>Seleccione el banco emisor</option>
                    <option
                      v-for="banco in BANCOS_VENEZUELA"
                      :key="banco.codigo"
                      :value="banco.nombre"
                    >
                      {{ banco.codigo }} - {{ banco.nombre }}
                    </option>
                  </select>
                  <!-- Para cualquier otro campo, input de texto -->
                  <input
                    v-else
                    type="text"
                    class="form-select"
                    :class="{ 'is-invalid': pagoFormErrors[campo.key] }"
                    :placeholder="formatFieldPlaceholder(campo.key)"
                    v-model="datosPago[campo.key]"
                  />
                  <span v-if="pagoFormErrors[campo.key]" class="form-error">{{ pagoFormErrors[campo.key] }}</span>
                </div>
              </div>

              <!-- Botón procesar -->
              <button
                class="btn-pay-main"
                :disabled="!selectedMetodoPago || isProcessingPayment"
                @click="procesarPago"
                style="margin-top: 16px;"
              >
                <div v-if="isProcessingPayment" class="spinner spinner-white spinner-sm" />
                <CreditCard v-else :size="18" />
                {{ isProcessingPayment ? 'Procesando pago...' : 'Procesar Pago' }}
              </button>

              <p class="payment-disclaimer">
                El pago quedará pendiente de verificación por nuestro personal.
              </p>
            </template>
          </div>
        </div>
      </div>
    </div>
    <!-- ═══════════ STEP 4: ÉXITO ═══════════ -->
    <div v-if="currentStep === 4" class="step-panel">
      <div class="success-container">
        <div class="success-icon">
          <Check :size="48" />
        </div>
        <h2 class="success-title">¡Pago Procesado!</h2>
        <p class="success-message">
          Su pago ha sido registrado exitosamente. La cita está confirmada.
        </p>

        <!-- Advertencia de correo -->
        <div v-if="facturaAdvertencia" class="alert alert-warning" style="max-width: 480px; margin: 0;">
          <AlertTriangle :size="18" />
          <span>{{ facturaAdvertencia }}</span>
        </div>

        <!-- Info de la factura -->
        <div v-if="facturaNumeroControl" class="success-factura-info">
          <div class="factura-info-row">
            <span class="factura-label">Factura</span>
            <span class="factura-value">{{ facturaNumeroControl }}</span>
          </div>
          <div class="factura-info-row">
            <span class="factura-label">Estado</span>
            <span class="factura-status">Pendiente de verificación</span>
          </div>
        </div>

        <!-- Botones de acción -->
        <div class="success-actions">
          <button class="btn-download" @click="descargarComprobante">
            <Download :size="18" />
            Descargar Comprobante
          </button>
          <router-link to="/cliente/historial-pagos" class="btn-history">
            <Receipt :size="18" />
            Ver en Historial
          </router-link>
        </div>

        <button class="btn-back-dashboard" @click="goToDashboard" style="margin-top: 8px;">
          <LayoutDashboard :size="18" />
          Volver al Panel
        </button>
      </div>
    </div>

    <!-- ═══════════ ACCIONES DEL STEPPER ═══════════ -->
    <div v-if="currentStep < 4" class="step-actions">
      <button class="btn-cancel" @click="cancelProcess">
        <X :size="16" />
        Cancelar
      </button>

      <div class="step-actions-right">
        <button
          v-if="currentStep > 1"
          class="btn-back"
          @click="goBack"
        >
          <ArrowLeft :size="16" />
          Atrás
        </button>

        <button
          v-if="currentStep === 1"
          class="btn-next"
          :disabled="!canGoNextFromStep1"
          @click="goToStep2"
        >
          Siguiente
          <ArrowRight :size="16" />
        </button>

        <button
          v-if="currentStep === 2"
          class="btn-next"
          :disabled="!selectedBloque || isConfirmingSelection"
          @click="confirmarSeleccion"
        >
          <div v-if="isConfirmingSelection" class="spinner spinner-white spinner-sm" />
          <template v-else>
            Confirmar Selección
            <ArrowRight :size="16" />
          </template>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Check, ChevronLeft, ChevronRight, Clock, PawPrint, Stethoscope,
  CreditCard, CalendarDays, ClipboardList, FileText, MessageSquare,
  DollarSign, Lock, AlertTriangle, X, ArrowLeft, ArrowRight,
  LayoutDashboard, Download, Receipt
} from 'lucide-vue-next'
import { getMisMascotas } from '@/api/mascotas.api'
import {
  getServicios, getVeterinarios, getDisponibilidad,
  solicitarCita, confirmarPago, cancelarCita
} from '@/api/citas.api'
import {
  getMetodosOnline, procesarPagoCita, descargarFactura
} from '@/api/pagos.api'

// ─── ROUTER ───
const router = useRouter()

// ─── CONSTANTES ───
const DIAS_SEMANA = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']
const MESES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
]
const MAX_MONTHS_AHEAD = 3

// ─── LISTA DE BANCOS EN VENEZUELA ───
const BANCOS_VENEZUELA = [
  { codigo: '0102', nombre: 'Banco de Venezuela, S.A.C.A.' },
  { codigo: '0104', nombre: 'Venezolano de Crédito, S.A.' },
  { codigo: '0105', nombre: 'Mercantil Banco, C.A.' },
  { codigo: '0108', nombre: 'Banco Provincial, S.A.' },
  { codigo: '0114', nombre: 'Banco del Caribe, C.A.' },
  { codigo: '0115', nombre: 'Banco Exterior, C.A.' },
  { codigo: '0116', nombre: 'Banco Occidental de Descuento, B.O.D.' },
  { codigo: '0128', nombre: 'Banco Caroní, C.A.' },
  { codigo: '0134', nombre: 'Banesco Banco Universal, C.A.' },
  { codigo: '0137', nombre: 'Banco Sofitasa, C.A.' },
  { codigo: '0138', nombre: 'Banco Plaza, C.A.' },
  { codigo: '0146', nombre: 'Banco de la Gente Emprendedora, C.A. (Bangente)' },
  { codigo: '0151', nombre: 'Banco Fondo Común, C.A.' },
  { codigo: '0156', nombre: '100% Banco, C.A.' },
  { codigo: '0157', nombre: 'Banco del Sur, C.A.' },
  { codigo: '0163', nombre: 'Banco del Tesoro, C.A.' },
  { codigo: '0166', nombre: 'Banco Agrícola de Venezuela, C.A.' },
  { codigo: '0168', nombre: 'Bancrecer, C.A.' },
  { codigo: '0169', nombre: 'Mi Banco, C.A.' },
  { codigo: '0171', nombre: 'Banco Activo, C.A.' },
  { codigo: '0172', nombre: 'Bancamiga, C.A.' },
  { codigo: '0173', nombre: 'Banco Internacional de Desarrollo, C.A.' },
  { codigo: '0174', nombre: 'Banplus, C.A.' },
  { codigo: '0175', nombre: 'Banco Bicentenario del Pueblo, C.A.' },
  { codigo: '0177', nombre: 'Banco de la Fuerza Armada Nacional Bolivariana, B.A.N.F.A.N.B.' },
  { codigo: '0190', nombre: 'Banco Nacional de Crédito, C.A.' },
  { codigo: '0191', nombre: 'Banco del Pueblo Soberano, C.A.' }
]

const steps = [
  { num: 1, label: 'Datos' },
  { num: 2, label: 'Horario' },
  { num: 3, label: 'Pago' }
]

// ─── ESTADO GENERAL ───
const currentStep = ref(1)
const isLoadingInitial = ref(true)
const loadError = ref(null)
const isLoadingServicios = ref(false)

// ─── STEP 1: Datos ───
const mascotas = ref([])
const servicios = ref([])
const veterinarios = ref([])
const selectedMascota = ref(null)
const selectedVeterinario = ref(null)
const selectedServicio = ref(null)
const motivoConsulta = ref('')
const stepErrors = ref({})

// ─── STEP 2: Calendario y horario ───
const calendarYear = ref(new Date().getFullYear())
const calendarMonth = ref(new Date().getMonth())
const selectedDate = ref(null)
const selectedBloque = ref(null)
const disponibilidadCache = ref({})
const isLoadingSlots = ref(false)
const disponibilidadError = ref(null)
const isConfirmingSelection = ref(false)
const confirmSelectionError = ref(null)

// ─── STEP 3: Pago ───
const citaPendienteId = ref(null)
const citaResumen = ref(null)
const isProcessingPayment = ref(false)
const pagoError = ref(null)

// Pasarela de pago
const metodosPago = ref([])
const selectedMetodoPago = ref(null)
const datosPago = ref({})
const isLoadingMetodos = ref(false)
const pagoFormErrors = ref({})
const referenciaTransaccion = ref('')

// Resultado del pago (paso 4)
const facturaId = ref(null)
const facturaNumeroControl = ref(null)
const facturaEmailEnviado = ref(false)
const facturaAdvertencia = ref(null)

// ─── COMPUTED ───
const servicioSeleccionado = computed(() =>
  servicios.value.find(s => s.id === selectedServicio.value) || null
)

const vetSeleccionado = computed(() =>
  veterinarios.value.find(v => v.id === selectedVeterinario.value) || null
)

const calendarMonthLabel = computed(() =>
  `${MESES[calendarMonth.value]} ${calendarYear.value}`
)

const canGoPrevMonth = computed(() => {
  const now = new Date()
  const prev = new Date(calendarYear.value, calendarMonth.value - 1, 1)
  return prev >= new Date(now.getFullYear(), now.getMonth(), 1)
})

const canGoNextMonth = computed(() => {
  const limit = new Date()
  limit.setMonth(limit.getMonth() + MAX_MONTHS_AHEAD)
  const next = new Date(calendarYear.value, calendarMonth.value + 1, 1)
  return next <= new Date(limit.getFullYear(), limit.getMonth(), 1)
})

const canGoNextFromStep1 = computed(() =>
  selectedMascota.value && selectedVeterinario.value &&
  selectedServicio.value && motivoConsulta.value.trim().length > 0
)

const calendarDays = computed(() => {
  const year = calendarYear.value
  const month = calendarMonth.value
  const firstDay = new Date(year, month, 1)
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const offset = firstDay.getDay() === 0 ? 6 : firstDay.getDay() - 1

  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const days = []

  // Días del mes anterior
  const prevLast = new Date(year, month, 0).getDate()
  for (let i = offset - 1; i >= 0; i--) {
    const d = new Date(year, month - 1, prevLast - i)
    days.push({ date: d, isCurrentMonth: false, isPast: true, isToday: false })
  }

  // Días del mes actual
  for (let d = 1; d <= daysInMonth; d++) {
    const date = new Date(year, month, d)
    days.push({
      date,
      isCurrentMonth: true,
      isPast: date < today,
      isToday: date.getTime() === today.getTime()
    })
  }

  // Completar última fila
  const totalRows = Math.ceil(days.length / 7)
  const remaining = totalRows * 7 - days.length
  for (let d = 1; d <= remaining; d++) {
    days.push({ date: new Date(year, month + 1, d), isCurrentMonth: false, isPast: false, isToday: false })
  }

  return days
})

const fechaFueConsultada = computed(() => {
  if (!selectedDate.value) return false
  const key = formatDateISO(selectedDate.value)
  return key in disponibilidadCache.value
})

const currentSlots = computed(() => {
  if (!selectedDate.value) return []
  const key = formatDateISO(selectedDate.value)
  return disponibilidadCache.value[key] || []
})

const showNoSlotsAlert = computed(() =>
  selectedDate.value && fechaFueConsultada.value && currentSlots.value.length === 0 && !disponibilidadError.value
)

const metodoSeleccionado = computed(() =>
  metodosPago.value.find(m => m.id === selectedMetodoPago.value) || null
)

const camposDinamicos = computed(() => {
  if (!metodoSeleccionado.value?.camposRequeridos) return []
  return Object.entries(metodoSeleccionado.value.camposRequeridos)
    .filter(([key]) => key !== 'referencia') // referencia se maneja por separado
    .map(([key, tipo]) => ({ key, tipo }))
})

// ─── LIFECYCLE ───
onMounted(() => {
  loadInitialData()
})

// ─── WATCHERS ───
// Si cambian servicio o veterinario en paso 1, limpiar selección de paso 2
watch([selectedServicio, selectedVeterinario], () => {
  selectedDate.value = null
  selectedBloque.value = null
  disponibilidadCache.value = {}
  disponibilidadError.value = null
})

// ─── CARGA DE DATOS INICIALES ───
async function loadInitialData() {
  isLoadingInitial.value = true
  loadError.value = null

  try {
    const [mascotasRes, vetsRes] = await Promise.all([
      getMisMascotas(),
      getVeterinarios()
    ])
    mascotas.value = mascotasRes.data
    veterinarios.value = vetsRes.data
    // Servicios ya NO se cargan aquí, se cargan al seleccionar veterinario
  } catch (err) {
    console.error('Error cargando datos iniciales:', err)
    loadError.value = err.response?.data?.mensaje || 'No se pudieron cargar los datos necesarios.'
  } finally {
    isLoadingInitial.value = false
  }
}

async function onVeterinarioChange() {
  selectedServicio.value = null
  stepErrors.value = {}
  servicios.value = []

  if (!selectedVeterinario.value) return

  const vet = vetSeleccionado.value
  if (!vet?.especialidad) return

  isLoadingServicios.value = true
  try {
    const { data } = await getServicios({ especialidad: vet.especialidad })
    servicios.value = data
  } catch (err) {
    console.error('Error cargando servicios por especialidad:', err)
  } finally {
    isLoadingServicios.value = false
  }
}

// ─── NAVEGACIÓN DEL STEPPER ───
function validateStep1() {
  const errors = {}
  if (!selectedMascota.value) errors.mascota = 'Selecciona una mascota'
  if (!selectedVeterinario.value) errors.veterinario = 'Selecciona un veterinario'
  if (!selectedServicio.value) errors.servicio = 'Selecciona un servicio'
  if (!motivoConsulta.value.trim()) errors.motivo = 'Escribe el motivo de la consulta'
  stepErrors.value = errors
  return Object.keys(errors).length === 0
}

function goToStep2() {
  if (!validateStep1()) return
  stepErrors.value = {}
  currentStep.value = 2
}

function goBack() {
  pagoError.value = null
  pagoFormErrors.value = {}
  currentStep.value--
}

async function cancelProcess() {
  // La cita queda en PENDIENTE_PAGO — se puede pagar después desde el historial
  goToDashboard()
}

function goToDashboard() {
  router.push('/cliente/dashboard')
}

// ─── CALENDARIO ───
function prevMonth() {
  if (!canGoPrevMonth.value) return
  const d = new Date(calendarYear.value, calendarMonth.value - 1, 1)
  calendarYear.value = d.getFullYear()
  calendarMonth.value = d.getMonth()
}

function nextMonth() {
  if (!canGoNextMonth.value) return
  const d = new Date(calendarYear.value, calendarMonth.value + 1, 1)
  calendarYear.value = d.getFullYear()
  calendarMonth.value = d.getMonth()
}

function isDateSelected(date) {
  if (!selectedDate.value) return false
  return formatDateISO(date) === formatDateISO(selectedDate.value)
}

function hasSlots(date) {
  const key = formatDateISO(date)
  return disponibilidadCache.value[key]?.length > 0
}

function hasNoSlots(date) {
  const key = formatDateISO(date)
  return key in disponibilidadCache.value && disponibilidadCache.value[key].length === 0
}

async function selectDate(day) {
  if (!day.isCurrentMonth || day.isPast) return

  const dateKey = formatDateISO(day.date)

  // Si ya estaba seleccionada, no hacer nada
  if (selectedDate.value && formatDateISO(selectedDate.value) === dateKey) return

  selectedDate.value = day.date
  selectedBloque.value = null
  disponibilidadError.value = null
  confirmSelectionError.value = null

  // Consultar disponibilidad si no está en cache
  if (!(dateKey in disponibilidadCache.value)) {
    await fetchDisponibilidad(dateKey)
  }
}

async function fetchDisponibilidad(dateStr) {
  isLoadingSlots.value = true
  disponibilidadError.value = null

  try {
    const { data } = await getDisponibilidad({
      id_veterinario: selectedVeterinario.value,
      fecha: dateStr,
      id_servicio: selectedServicio.value
    })
    disponibilidadCache.value[dateStr] = data.bloquesDisponibles || []
  } catch (err) {
    console.error('Error consultando disponibilidad:', err)
    disponibilidadError.value = err.response?.data?.mensaje || 'Error al consultar disponibilidad. Intenta de nuevo.'
    disponibilidadCache.value[dateStr] = []
  } finally {
    isLoadingSlots.value = false
  }
}

function retryDisponibilidad() {
  if (!selectedDate.value) return
  const key = formatDateISO(selectedDate.value)
  delete disponibilidadCache.value[key]
  fetchDisponibilidad(key)
}

function isBloqueSelected(bloque) {
  return selectedBloque.value?.horaInicio === bloque.horaInicio
}

function selectBloque(bloque) {
  selectedBloque.value = bloque
  confirmSelectionError.value = null
}

// ─── CONFIRMAR SELECCIÓN (bloqueo temporal) ───
async function confirmarSeleccion() {
  if (!selectedBloque.value || !selectedDate.value) return

  isConfirmingSelection.value = true
  confirmSelectionError.value = null

  try {
    const { data } = await solicitarCita({
      idMascota: selectedMascota.value,
      idVeterinario: selectedVeterinario.value,
      idServicio: selectedServicio.value,
      fechaCita: formatDateISO(selectedDate.value),
      horaInicio: selectedBloque.value.horaInicio,
      motivoConsulta: motivoConsulta.value.trim()
    })

    citaPendienteId.value = data.idCita
    citaResumen.value = data.resumen
    pagoError.value = null
    currentStep.value = 3
    // Cargar métodos de pago para la pasarela
    await loadMetodosPago()
  } catch (err) {
    console.error('Error al solicitar cita:', err)
    const msg = err.response?.data?.mensaje
    if (msg && msg.includes('ya no está disponible')) {
      // El horario fue tomado — refrescar disponibilidad
      confirmSelectionError.value = msg
      const key = formatDateISO(selectedDate.value)
      delete disponibilidadCache.value[key]
      await fetchDisponibilidad(key)
      selectedBloque.value = null
    } else {
      confirmSelectionError.value = msg || 'No se pudo bloquear el horario. Intenta de nuevo.'
    }
  } finally {
    isConfirmingSelection.value = false
  }
}

// ─── PROCESAR PAGO ───
async function procesarPago() {
  console.log('Iniciando procesarPago')
  if (!citaPendienteId.value || !selectedMetodoPago.value) {
    console.warn('Falta citaPendienteId o selectedMetodoPago')
    return
  }

  const camposRequeridos = metodoSeleccionado.value?.camposRequeridos || {}
  const errors = {}

  // Validar referencia solo si el backend la exige
  if ('referencia' in camposRequeridos) {
    if (!referenciaTransaccion.value.trim()) {
      errors.referencia = 'La referencia es obligatoria'
    }
  }

  // Validar el resto de campos dinámicos
  for (const { key } of camposDinamicos.value) {
    if (!datosPago.value[key]?.trim()) {
      errors[key] = 'Este campo es obligatorio'
    }
  }

  pagoFormErrors.value = errors
  if (Object.keys(errors).length > 0) {
    console.warn('Errores de validación:', errors)
    return
  }

  isProcessingPayment.value = true
  pagoError.value = null

  try {
    // Construir datosPagoCompletos incluyendo referencia solo si es necesaria
    const datosPagoCompletos = { ...datosPago.value }
    if ('referencia' in camposRequeridos) {
      datosPagoCompletos.referencia = referenciaTransaccion.value.trim()
    }

    const { data } = await procesarPagoCita({
      idCita: citaPendienteId.value,
      idMetodoPago: selectedMetodoPago.value,
      referenciaTransaccion: referenciaTransaccion.value.trim(),
      datosPago: datosPagoCompletos
    })

    // Éxito
    facturaId.value = data.idFactura
    facturaNumeroControl.value = data.numeroControl
    facturaEmailEnviado.value = data.emailEnviado
    facturaAdvertencia.value = data.advertenciaEmail

    // Limpiar estado pendiente
    const savedResumen = { ...citaResumen.value }
    citaPendienteId.value = null
    citaResumen.value = savedResumen

    currentStep.value = 4
  } catch (err) {
    console.error('Error procesando pago:', err)
    const msg = err.response?.data?.mensaje || err.response?.data?.message
    pagoError.value = msg || 'Transacción rechazada o datos inválidos. Verifique e intente nuevamente.'
  } finally {
    isProcessingPayment.value = false
  }
}

// ─── HELPERS DE FORMATO ───
function formatDateISO(date) {
  if (!date) return ''
  if (typeof date === 'string') return date
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function formatDateDisplay(dateStr) {
  if (!dateStr) return ''
  const d = typeof dateStr === 'string' ? new Date(dateStr + 'T12:00:00') : dateStr
  return `${d.getDate()} de ${MESES[d.getMonth()]} de ${d.getFullYear()}`
}

function formatTime12h(timeStr) {
  if (!timeStr) return ''
  const parts = timeStr.split(':')
  const h = parseInt(parts[0], 10)
  const m = parts[1] || '00'
  const period = h >= 12 ? 'PM' : 'AM'
  const h12 = h % 12 || 12
  return `${h12}:${m} ${period}`
}

function formatCurrency(amount) {
  if (amount == null) return '$0.00'
  return `$${Number(amount).toFixed(2)} USD`
}

function formatCurrencyBs(amount) {
  if (amount == null) return 'Bs. 0.00'
  return `Bs. ${Number(amount).toFixed(2)}`
}

async function loadMetodosPago() {
  isLoadingMetodos.value = true
  try {
    const { data } = await getMetodosOnline()
    metodosPago.value = data
    console.log('Métodos de pago cargados:', data)
  } catch (err) {
    console.error('Error cargando métodos de pago:', err)
  } finally {
    isLoadingMetodos.value = false
  }
}

function onMetodoPagoChange() {
  datosPago.value = {}
  referenciaTransaccion.value = ''
  pagoFormErrors.value = {}
  pagoError.value = null
}

function resetPagoForm() {
  selectedMetodoPago.value = null
  datosPago.value = {}
  referenciaTransaccion.value = ''
  pagoFormErrors.value = {}
  pagoError.value = null
}

function formatFieldLabel(key) {
  const labels = {
    banco: 'Banco emisor',
    numero_cuenta: 'Número de cuenta',
    telefono: 'Número de teléfono asociado',
    lote: 'Número de lote',
    ultimos_digitos: 'Últimos 4 dígitos de la tarjeta'
  }
  return labels[key] || key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}

function formatFieldPlaceholder(key) {
  const ph = {
    banco: 'Ej: Banesco, Mercantil, Provincial...',
    numero_cuenta: 'Ej: 0000-0000-00-0000000000',
    telefono: 'Ej: 04141234567'
  }
  return ph[key] || ''
}

async function descargarComprobante() {
  if (!facturaId.value) return
  try {
    const response = await descargarFactura(facturaId.value)
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `Factura-${facturaNumeroControl.value}.pdf`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch (err) {
    console.error('Error descargando factura:', err)
  }
}
</script>

<style scoped>
/* ═══════════════════════════════════════════
   CONTENEDOR PRINCIPAL
   ═══════════════════════════════════════════ */
.appointment-view {
  max-width: 960px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.label-loading {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 500;
  color: #64748B;
  margin-left: 8px;
}

/* ═══════════════════════════════════════════
   PAGE HEADER
   ═══════════════════════════════════════════ */
.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #64748B;
  margin: 0;
}

/* ═══════════════════════════════════════════
   STEPPER
   ═══════════════════════════════════════════ */
.stepper {
  display: flex;
  align-items: center;
  background: #ffffff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 20px 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.stepper-step {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  flex: 1;
}

.stepper-step:last-child {
  flex: 0;
}

.stepper-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  background: #F1F5F9;
  color: #94A3B8;
  border: 2px solid #E2E8F0;
  flex-shrink: 0;
  transition: all 0.25s ease;
}

.stepper-step.active .stepper-circle {
  background: #0F766E;
  color: #ffffff;
  border-color: #0F766E;
  box-shadow: 0 0 0 4px rgba(15, 118, 110, 0.15);
}

.stepper-step.completed .stepper-circle {
  background: #0F766E;
  color: #ffffff;
  border-color: #0F766E;
}

.stepper-label {
  font-size: 13px;
  font-weight: 600;
  color: #94A3B8;
  white-space: nowrap;
  transition: color 0.25s ease;
}

.stepper-step.active .stepper-label {
  color: #0F766E;
}

.stepper-step.completed .stepper-label {
  color: #1E293B;
}

.stepper-line {
  flex: 1;
  height: 2px;
  background: #E2E8F0;
  margin: 0 16px;
  border-radius: 1px;
  transition: background 0.25s ease;
}

.stepper-step.completed .stepper-line {
  background: #0F766E;
}

/* ═══════════════════════════════════════════
   CARDS (mismo patrón del dashboard)
   ═══════════════════════════════════════════ */
.card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.card-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.card-body {
  padding: 24px;
}

/* ═══════════════════════════════════════════
   LOADING
   ═══════════════════════════════════════════ */
.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 24px;
  gap: 16px;
  color: #64748B;
  font-size: 14px;
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #E2E8F0;
  border-top-color: #0F766E;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.spinner-sm {
  width: 18px;
  height: 18px;
  border-width: 2px;
}

.spinner-white {
  border-color: rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ═══════════════════════════════════════════
   ALERTAS
   ═══════════════════════════════════════════ */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 18px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  margin-top: 20px;
}

.alert-error {
  background: #FEF2F2;
  color: #991B1B;
  border: 1px solid #FECACA;
}

.alert-warning {
  background: #FFFBEB;
  color: #92400E;
  border: 1px solid #FDE68A;
}

.alert-action {
  margin-left: auto;
  background: none;
  border: none;
  color: inherit;
  font-weight: 600;
  font-size: 12px;
  cursor: pointer;
  text-decoration: underline;
  white-space: nowrap;
  font-family: inherit;
  flex-shrink: 0;
}

/* ═══════════════════════════════════════════
   FORMULARIOS (STEP 1)
   ═══════════════════════════════════════════ */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.required {
  color: #EF4444;
}

.form-select,
.form-textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  font-size: 14px;
  color: #1E293B;
  background: #ffffff;
  transition: border-color 0.2s, box-shadow 0.2s;
  font-family: inherit;
  appearance: none;
  -webkit-appearance: none;
}

.form-select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px;
  cursor: pointer;
}

.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.1);
}

.form-select.is-invalid,
.form-textarea.is-invalid {
  border-color: #EF4444;
}

.form-select.is-invalid:focus,
.form-textarea.is-invalid:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.form-error {
  font-size: 12px;
  color: #EF4444;
  font-weight: 500;
}

.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 2px;
}

.char-count {
  font-size: 12px;
  color: #94A3B8;
}

/* Info del servicio */
.service-detail {
  margin-top: 8px;
  padding: 12px 16px;
  background: #F0FDFA;
  border: 1px solid #99F6E4;
  border-radius: 8px;
}

.service-desc {
  font-size: 13px;
  color: #115E59;
  margin: 0 0 8px 0;
  line-height: 1.5;
}

.service-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.service-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  color: #0F766E;
}

.service-badge {
  font-size: 11px;
  font-weight: 600;
  color: #0F766E;
  background: #CCFBF1;
  padding: 3px 10px;
  border-radius: 20px;
  border: 1px solid #99F6E4;
}

/* ═══════════════════════════════════════════
   CALENDARIO (STEP 2)
   ═══════════════════════════════════════════ */
.calendar {
  margin-bottom: 8px;
}

.calendar-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.cal-nav-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  background: #ffffff;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.cal-nav-btn:hover:not(:disabled) {
  background: #F8FAFC;
  color: #1E293B;
  border-color: #CBD5E1;
}

.cal-nav-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.cal-month-year {
  font-size: 16px;
  font-weight: 700;
  color: #1E293B;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 4px;
}

.cal-weekday {
  text-align: center;
  font-size: 11px;
  font-weight: 700;
  color: #94A3B8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 8px 0;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}

.cal-day {
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
  color: #1E293B;
  border: 2px solid transparent;
  border-radius: 10px;
  background: none;
  cursor: pointer;
  transition: all 0.15s ease;
  position: relative;
  font-family: inherit;
}

.cal-day:hover:not(:disabled):not(.other-month):not(.is-past) {
  background: #F0FDFA;
  border-color: #99F6E4;
}

.cal-day.other-month {
  color: #CBD5E1;
  cursor: default;
}

.cal-day.is-past {
  color: #CBD5E1;
  cursor: not-allowed;
}

.cal-day.is-today {
  font-weight: 700;
  color: #0F766E;
}

.cal-day.is-selected {
  background: #0F766E;
  color: #ffffff;
  border-color: #0F766E;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(15, 118, 110, 0.3);
}

.cal-day.is-selected.is-today {
  color: #ffffff;
}

/* Indicadores de disponibilidad bajo el número */
.cal-day.has-slots:not(.is-selected)::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #10B981;
}

.cal-day.no-slots:not(.is-selected)::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #FCA5A5;
}

/* Badge de info en header */
.vet-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #0F766E;
  background: #F0FDFA;
  padding: 5px 12px;
  border-radius: 20px;
  border: 1px solid #99F6E4;
}

/* ═══════════════════════════════════════════
   BLOQUES HORARIOS
   ═══════════════════════════════════════════ */
.slots-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #E2E8F0;
}

.slots-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 16px 0;
}

.slots-loading {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 24px;
  color: #64748B;
  font-size: 13px;
}

.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 10px;
}

.slot-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 14px 12px;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  background: #F8FAFC;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.slot-btn:hover {
  border-color: #0F766E;
  background: #F0FDFA;
  box-shadow: 0 2px 8px rgba(15, 118, 110, 0.08);
}

.slot-btn.is-selected {
  background: #0F766E;
  border-color: #0F766E;
  box-shadow: 0 2px 12px rgba(15, 118, 110, 0.3);
}

.slot-time {
  font-size: 15px;
  font-weight: 700;
  color: #1E293B;
  transition: color 0.2s;
}

.slot-btn.is-selected .slot-time {
  color: #ffffff;
}

.slot-separator {
  font-size: 10px;
  color: #94A3B8;
  transition: color 0.2s;
}

.slot-btn.is-selected .slot-separator {
  color: rgba(255, 255, 255, 0.7);
}

.slot-end {
  font-size: 12px;
  font-weight: 500;
  color: #64748B;
  transition: color 0.2s;
}

.slot-btn.is-selected .slot-end {
  color: rgba(255, 255, 255, 0.85);
}

.slots-empty {
  display: none;
}

/* ═══════════════════════════════════════════
   MINI RESUMEN (STEP 2)
   ═══════════════════════════════════════════ */
.mini-summary {
  margin-top: 24px;
  padding: 16px 20px;
  background: #F0FDFA;
  border: 1px solid #99F6E4;
  border-radius: 10px;
  display: flex;
  gap: 32px;
  flex-wrap: wrap;
}

.mini-summary-row {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.mini-label {
  font-size: 11px;
  font-weight: 600;
  color: #64748B;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.mini-value {
  font-size: 14px;
  font-weight: 600;
  color: #115E59;
}

/* Error de confirmación en step 2 */
.alert {
  margin-top: 16px;
}

/* ═══════════════════════════════════════════
   STEP 3: RESUMEN Y PAGO
   ═══════════════════════════════════════════ */
.summary-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.summary-rows {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.summary-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid #F1F5F9;
}

.summary-row.full {
  flex-direction: column;
  gap: 6px;
}

.summary-row:last-child {
  border-bottom: none;
}

.summary-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748B;
  white-space: nowrap;
  flex-shrink: 0;
}

.summary-value {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  text-align: right;
}

.motivo-text {
  font-weight: 400;
  color: #475569;
  line-height: 1.6;
  text-align: left;
  white-space: pre-line;
}

/* Payment card */
.payment-card {
  display: flex;
  flex-direction: column;
}

.payment-card .card-body {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.reserved-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 600;
  color: #D97706;
  background: #FFFBEB;
  padding: 4px 10px;
  border-radius: 20px;
  border: 1px solid #FDE68A;
}

.payment-rows {
  flex: 1;
  margin-bottom: 24px;
}

.payment-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  font-size: 14px;
  color: #475569;
}

.payment-total {
  font-size: 18px;
  font-weight: 700;
  color: #1E293B;
}

.payment-divider {
  height: 2px;
  background: #E2E8F0;
  margin: 4px 0 8px 0;
  border-radius: 1px;
}

.btn-pay-main {
  width: 100%;
  padding: 14px 24px;
  background: #0F766E;
  color: #ffffff;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.2s ease;
  font-family: inherit;
}

.btn-pay-main:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(15, 118, 110, 0.3);
}

.btn-pay-main:active:not(:disabled) {
  transform: translateY(0);
}

.btn-pay-main:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.payment-disclaimer {
  margin-top: 12px;
  font-size: 11px;
  color: #94A3B8;
  text-align: center;
  margin-bottom: 0;
}

/* ═══════════════════════════════════════════
   STEP 4: ÉXITO
   ═══════════════════════════════════════════ */
.success-container {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  padding: 64px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
}

.success-icon {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: #ECFDF5;
  border: 3px solid #10B981;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #059669;
  margin-bottom: 8px;
  animation: successPop 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

.success-title {
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  margin: 0;
}

.success-message {
  font-size: 15px;
  color: #64748B;
  margin: 0;
  max-width: 400px;
}

.success-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 8px;
  padding: 12px 20px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  font-size: 13px;
  color: #475569;
  font-weight: 500;
}

.btn-back-dashboard {
  margin-top: 16px;
  padding: 12px 28px;
  background: #0F766E;
  color: #ffffff;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s ease;
  font-family: inherit;
}

.btn-back-dashboard:hover {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.25);
}

/* ═══════════════════════════════════════════
   STEP ACTIONS (botones inferiores)
   ═══════════════════════════════════════════ */
.step-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #E2E8F0;
}

.step-actions-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn-cancel {
  padding: 10px 20px;
  background: none;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  color: #64748B;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  font-family: inherit;
}

.btn-cancel:hover {
  background: #FEF2F2;
  border-color: #FECACA;
  color: #EF4444;
}

.btn-back {
  padding: 10px 20px;
  background: #ffffff;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s;
  font-family: inherit;
}

.btn-back:hover {
  background: #F8FAFC;
  border-color: #CBD5E1;
}

.btn-next {
  padding: 10px 24px;
  background: #0F766E;
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
  font-family: inherit;
}

.btn-next:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.25);
}

.btn-next:active:not(:disabled) {
  transform: translateY(0);
}

.btn-next:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ═══════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════ */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .stepper {
    padding: 16px 20px;
  }

  .stepper-label {
    font-size: 11px;
  }

  .stepper-line {
    margin: 0 8px;
  }

  .mini-summary {
    flex-direction: column;
    gap: 12px;
  }

  .slots-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }

  .step-actions {
    flex-direction: column-reverse;
    gap: 12px;
  }

  .step-actions-right {
    width: 100%;
    justify-content: flex-end;
  }

  .btn-cancel {
    width: 100%;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .stepper-label {
    display: none;
  }

  .stepper-step {
    justify-content: center;
  }

  .slots-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

/* ═══════════════════════════════════════════
   PASARELA DE PAGO
   ═══════════════════════════════════════════ */
.payment-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: start;
}

.payment-gateway-card {
  position: sticky;
  top: 0;
}

.method-option {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 10px;
}

.method-option:hover {
  border-color: #99F6E4;
  background: #F0FDFA;
}

.method-option.active {
  border-color: #0F766E;
  background: #F0FDFA;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.1);
}

.method-radio {
  flex-shrink: 0;
}

.radio-outer {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #CBD5E1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.radio-outer.checked {
  border-color: #0F766E;
}

.radio-inner {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #0F766E;
  animation: radioPop 0.2s ease;
}

@keyframes radioPop {
  0% { transform: scale(0); }
  100% { transform: scale(1); }
}

.method-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.method-name {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
}

.method-desc {
  font-size: 12px;
  color: #64748B;
}

.dynamic-fields {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #E2E8F0;
}

/* ═══════════════════════════════════════════
   PANTALLA DE ÉXITO CON FACTURA
   ═══════════════════════════════════════════ */
.success-factura-info {
  margin-top: 8px;
  padding: 14px 20px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  max-width: 400px;
}

.factura-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.factura-label {
  font-size: 13px;
  color: #64748B;
}

.factura-value {
  font-size: 14px;
  font-weight: 700;
  color: #1E293B;
  font-family: 'Courier New', monospace;
}

.factura-status {
  font-size: 13px;
  font-weight: 600;
  color: #D97706;
  background: #FFFBEB;
  padding: 3px 10px;
  border-radius: 20px;
  border: 1px solid #FDE68A;
}

.success-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  flex-wrap: wrap;
  justify-content: center;
}

.btn-download {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #0F766E;
  color: #ffffff;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
  text-decoration: none;
}

.btn-download:hover {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.25);
}

.btn-history {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: #ffffff;
  color: #475569;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
  text-decoration: none;
}

.btn-history:hover {
  background: #F8FAFC;
  border-color: #CBD5E1;
  color: #1E293B;
}

/* ═══════════════════════════════════════════
   RESPONSIVE PASARELA
   ═══════════════════════════════════════════ */
@media (max-width: 900px) {
  .payment-layout {
    grid-template-columns: 1fr;
  }

  .payment-gateway-card {
    position: static;
  }
}
</style>