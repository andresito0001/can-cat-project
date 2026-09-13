<template>
  <div class="appointment-view">
    <!-- PAGE HEADER -->
    <div class="page-header">
      <div>
        <h1>Solicitar Cita</h1>
        <p class="page-subtitle">Reserva un turno de atención médica para tu mascota</p>
      </div>
    </div>

    <!-- STEPPER -->
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

    <!-- STEP 1: DATOS -->
    <div v-if="currentStep === 1" class="step-panel">
      <div v-if="isLoadingInitial" class="loading-box">
        <div class="spinner" />
        <p>Cargando datos necesarios...</p>
      </div>
      <template v-else>
        <div v-if="loadError" class="alert alert-error">
          <AlertTriangle :size="18" />
          <span>{{ loadError }}</span>
          <button type="button" class="alert-action" @click="loadInitialData">Reintentar</button>
        </div>
        <template v-else>
          <div class="card">
            <div class="card-header">
              <h3><ClipboardList :size="18" /> Datos de la Cita</h3>
            </div>
            <div class="card-body">
              <div class="form-grid">
                <div class="form-group">
                  <label class="form-label" for="mascota">Mascota <span class="required">*</span></label>
                  <select id="mascota" v-model="selectedMascota" class="form-select" :class="{ 'is-invalid': stepErrors.mascota }">
                    <option :value="null" disabled>Selecciona una mascota</option>
                    <option v-for="m in mascotas" :key="m.idMascota" :value="m.idMascota">
                      {{ m.nombre }} — {{ m.nombreRaza }} ({{ m.nombreEspecie }})
                    </option>
                  </select>
                  <span v-if="stepErrors.mascota" class="form-error">{{ stepErrors.mascota }}</span>
                </div>
                <div class="form-group">
                  <label class="form-label" for="veterinario">Veterinario <span class="required">*</span></label>
                  <select id="veterinario" v-model="selectedVeterinario" class="form-select" :class="{ 'is-invalid': stepErrors.veterinario }" @change="onVeterinarioChange">
                    <option :value="null" disabled>Selecciona un veterinario</option>
                    <option v-for="v in veterinarios" :key="v.id" :value="v.id">{{ v.nombre }}</option>
                  </select>
                  <span v-if="stepErrors.veterinario" class="form-error">{{ stepErrors.veterinario }}</span>
                  <div v-if="vetSeleccionado" class="service-detail" style="margin-top: 8px;">
                    <div class="service-meta">
                      <span class="service-badge">{{ vetSeleccionado.especialidad }}</span>
                      <span class="service-meta-item"><Stethoscope :size="14" /> Especialidad</span>
                    </div>
                  </div>
                </div>
                <div class="form-group full-width">
                  <label class="form-label" for="servicio">
                    Servicio <span class="required">*</span>
                    <span v-if="isLoadingServicios" class="label-loading">
                      <div class="spinner spinner-sm" style="border-top-color: #0F766E;" /> Cargando...
                    </span>
                  </label>
                  <select id="servicio" v-model="selectedServicio" class="form-select" :class="{ 'is-invalid': stepErrors.servicio }" :disabled="!selectedVeterinario || isLoadingServicios">
                    <option :value="null" disabled>{{ !selectedVeterinario ? 'Selecciona un veterinario primero' : 'Selecciona un servicio' }}</option>
                    <option v-for="s in servicios" :key="s.id" :value="s.id">{{ s.nombre }} — {{ formatCurrency(s.precioUsd) }}</option>
                  </select>
                  <span v-if="stepErrors.servicio" class="form-error">{{ stepErrors.servicio }}</span>
                  <div v-if="servicioSeleccionado" class="service-detail">
                    <p v-if="servicioSeleccionado.descripcion" class="service-desc">{{ servicioSeleccionado.descripcion }}</p>
                    <div class="service-meta">
                      <span class="service-meta-item"><Clock :size="14" /> {{ servicioSeleccionado.duracionMinutos }} min</span>
                      <span class="service-meta-item"><DollarSign :size="14" /> {{ formatCurrency(servicioSeleccionado.precioUsd) }}</span>
                      <span v-if="servicioSeleccionado.tipoAtencion" class="service-badge">{{ servicioSeleccionado.tipoAtencion }}</span>
                    </div>
                  </div>
                </div>
                <div class="form-group full-width">
                  <label class="form-label" for="motivo">Motivo de la consulta <span class="required">*</span></label>
                  <textarea id="motivo" v-model="motivoConsulta" class="form-textarea" :class="{ 'is-invalid': stepErrors.motivo }" placeholder="Describe brevemente el motivo de la consulta..." maxlength="1000" rows="4" />
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

    <!-- STEP 2: HORARIO -->
    <div v-if="currentStep === 2" class="step-panel">
      <div class="card">
        <div class="card-header">
          <h3><CalendarDays :size="18" /> Seleccionar Horario</h3>
          <span class="vet-badge"><Stethoscope :size="14" /> {{ vetSeleccionado?.nombre || '' }} · {{ servicioSeleccionado?.nombre || '' }}</span>
        </div>
        <div class="card-body">
          <div class="calendar">
            <div class="calendar-nav">
              <button type="button" class="cal-nav-btn" :disabled="!canGoPrevMonth" @click="prevMonth"><ChevronLeft :size="18" /></button>
              <span class="cal-month-year">{{ calendarMonthLabel }}</span>
              <button type="button" class="cal-nav-btn" :disabled="!canGoNextMonth" @click="nextMonth"><ChevronRight :size="18" /></button>
            </div>
            <div class="calendar-weekdays">
              <span v-for="d in DIAS_SEMANA" :key="d" class="cal-weekday">{{ d }}</span>
            </div>
            <div class="calendar-grid">
              <button
                v-for="(day, idx) in calendarDays"
                :key="idx"
                type="button"
                class="cal-day"
                :class="{ 'other-month': !day.isCurrentMonth, 'is-past': day.isPast, 'is-today': day.isToday, 'is-selected': isDateSelected(day.date), 'has-slots': hasSlots(day.date), 'no-slots': hasNoSlots(day.date) }"
                :disabled="!day.isCurrentMonth || day.isPast"
                @click="selectDate(day)"
              >
                {{ day.date.getDate() }}
              </button>
            </div>
          </div>
          <div v-if="showNoSlotsAlert" class="alert alert-warning">
            <AlertTriangle :size="18" />
            <span>No hay horarios disponibles para este día. Selecciona otro día en el calendario.</span>
          </div>
          <div v-if="disponibilidadError" class="alert alert-error">
            <AlertTriangle :size="18" />
            <span>{{ disponibilidadError }}</span>
            <button type="button" class="alert-action" @click="retryDisponibilidad">Reintentar</button>
          </div>
          <div v-if="selectedDate && !disponibilidadError" class="slots-section">
            <h4 class="slots-title"><Clock :size="16" /> Horarios disponibles — {{ formatDateDisplay(selectedDate) }}</h4>
            <div v-if="isLoadingSlots" class="slots-loading">
              <div class="spinner spinner-sm" /><span>Consultando disponibilidad...</span>
            </div>
            <div v-else-if="currentSlots.length > 0" class="slots-grid">
              <button
                v-for="bloque in currentSlots"
                :key="bloque.horaInicio"
                type="button"
                class="slot-btn"
                :class="{ 'is-selected': isBloqueSelected(bloque) }"
                @click="selectBloque(bloque)"
              >
                <span class="slot-time">{{ formatTime12h(bloque.horaInicio) }}</span>
                <span class="slot-separator">a</span>
                <span class="slot-time slot-end">{{ formatTime12h(bloque.horaFin) }}</span>
              </button>
            </div>
          </div>
          <div v-if="selectedBloque" class="mini-summary">
            <div class="mini-summary-row"><span class="mini-label">Fecha</span><span class="mini-value">{{ formatDateDisplay(selectedDate) }}</span></div>
            <div class="mini-summary-row"><span class="mini-label">Horario</span><span class="mini-value">{{ formatTime12h(selectedBloque.horaInicio) }} — {{ formatTime12h(selectedBloque.horaFin) }}</span></div>
            <div class="mini-summary-row"><span class="mini-label">Duración</span><span class="mini-value">{{ servicioSeleccionado?.duracionMinutos }} min</span></div>
          </div>
        </div>
      </div>
    </div>

    <!-- STEP 3: PASARELA DE PAGO -->
    <div v-if="currentStep === 3" class="step-panel">
      <div class="payment-layout">
        <div class="card">
          <div class="card-header"><h3><FileText :size="18" /> Detalles de la Cita</h3></div>
          <div class="card-body">
            <div class="summary-rows">
              <div class="summary-row"><span class="summary-label"><PawPrint :size="15" /> Mascota</span><span class="summary-value">{{ citaResumen?.mascota }}</span></div>
              <div class="summary-row"><span class="summary-label"><Stethoscope :size="15" /> Veterinario</span><span class="summary-value">{{ citaResumen?.veterinario }}</span></div>
              <div class="summary-row"><span class="summary-label"><ClipboardList :size="15" /> Servicio</span><span class="summary-value">{{ citaResumen?.servicio }}</span></div>
              <div class="summary-row"><span class="summary-label"><CalendarDays :size="15" /> Fecha</span><span class="summary-value">{{ formatDateDisplay(citaResumen?.fecha) }}</span></div>
              <div class="summary-row"><span class="summary-label"><Clock :size="15" /> Horario</span><span class="summary-value">{{ formatTime12h(citaResumen?.horaInicio) }} — {{ formatTime12h(citaResumen?.horaFin) }}</span></div>
              <div class="summary-row full"><span class="summary-label"><MessageSquare :size="15" /> Motivo</span><span class="summary-value motivo-text">{{ motivoConsulta }}</span></div>
            </div>
            <div class="payment-divider" style="margin: 16px 0 0 0;" />
            <div class="payment-rows" style="margin-top: 0;">
              <div class="payment-row"><span>Servicio</span><span>{{ formatCurrency(citaResumen?.costoUsd) }}</span></div>
              <div class="payment-row"><span>Tasa de cambio</span><span>{{ citaResumen?.tasaCambio }} Bs/$</span></div>
              <div class="payment-divider" />
              <div class="payment-row payment-total"><span>Total a pagar</span><span>{{ formatCurrencyBs(citaResumen?.costoBs) }}</span></div>
            </div>
          </div>
        </div>
        <div class="card payment-gateway-card">
          <div class="card-header">
            <h3><CreditCard :size="18" /> Método de Pago</h3>
            <span class="reserved-badge"><Lock :size="12" /> Horario reservado</span>
          </div>
          <div class="card-body">
            <div v-if="pagoError" class="alert alert-error" style="margin-top: 0; margin-bottom: 16px;">
              <AlertTriangle :size="18" /><span>{{ pagoError }}</span>
            </div>
            <div v-if="isLoadingMetodos" class="slots-loading" style="padding: 32px 0;">
              <div class="spinner spinner-sm" /><span>Cargando métodos de pago...</span>
            </div>
            <template v-else>
              <div
                v-for="m in metodosPago"
                :key="m.id"
                class="method-option"
                :class="{ active: selectedMetodoPago === m.id }"
                @click="selectedMetodoPago = m.id; onMetodoPagoChange()"
              >
                <div class="method-radio">
                  <div class="radio-outer" :class="{ checked: selectedMetodoPago === m.id }"><div v-if="selectedMetodoPago === m.id" class="radio-inner" /></div>
                </div>
                <div class="method-info">
                  <span class="method-name">{{ m.nombre === 'Pago_Movil' ? 'Pago Móvil' : m.nombre }}</span>
                  <span class="method-desc">{{ m.descripcion }}</span>
                </div>
              </div>
              <div v-if="selectedMetodoPago && (camposDinamicos.length > 0 || metodoSeleccionado?.camposRequeridos?.referencia)" class="dynamic-fields">
                <div v-if="metodoSeleccionado?.camposRequeridos?.referencia" class="form-group">
                  <label class="form-label">Número de referencia <span class="required">*</span></label>
                  <input type="text" class="form-select" :class="{ 'is-invalid': pagoFormErrors.referencia }" placeholder="Ej: 0000123456789" v-model="referenciaTransaccion" />
                  <span v-if="pagoFormErrors.referencia" class="form-error">{{ pagoFormErrors.referencia }}</span>
                </div>
                <div v-for="campo in camposDinamicos" :key="campo.key" class="form-group">
                  <label class="form-label">{{ formatFieldLabel(campo.key) }} <span class="required">*</span></label>
                  <select v-if="campo.key === 'banco'" class="form-select" :class="{ 'is-invalid': pagoFormErrors[campo.key] }" v-model="datosPago[campo.key]">
                    <option value="" disabled>Seleccione el banco emisor</option>
                    <option v-for="banco in BANCOS_VENEZUELA" :key="banco.codigo" :value="banco.nombre">{{ banco.codigo }} - {{ banco.nombre }}</option>
                  </select>
                  <input v-else type="text" class="form-select" :class="{ 'is-invalid': pagoFormErrors[campo.key] }" :placeholder="formatFieldPlaceholder(campo.key)" v-model="datosPago[campo.key]" />
                  <span v-if="pagoFormErrors[campo.key]" class="form-error">{{ pagoFormErrors[campo.key] }}</span>
                </div>
              </div>
              <button type="button" class="btn-pay-main" :disabled="!selectedMetodoPago || isProcessingPayment" @click="procesarPago" style="margin-top: 16px;">
                <div v-if="isProcessingPayment" class="spinner spinner-white spinner-sm" />
                <CreditCard v-else :size="18" />
                {{ isProcessingPayment ? 'Procesando pago...' : 'Procesar Pago' }}
              </button>
              <p class="payment-disclaimer">El pago quedará pendiente de verificación por nuestro personal.</p>
            </template>
          </div>
        </div>
      </div>
    </div>

    <!-- STEP 4: ÉXITO -->
    <div v-if="currentStep === 4" class="step-panel">
      <div class="success-container">
        <div class="success-icon"><Check :size="48" /></div>
        <h2 class="success-title">¡Pago Procesado!</h2>
        <p class="success-message">Su pago ha sido registrado exitosamente. La cita está confirmada.</p>
        <div v-if="facturaAdvertencia" class="alert alert-warning" style="max-width: 480px; margin: 0;">
          <AlertTriangle :size="18" /><span>{{ facturaAdvertencia }}</span>
        </div>
        <div v-if="facturaNumeroControl" class="success-factura-info">
          <div class="factura-info-row"><span class="factura-label">Factura</span><span class="factura-value">{{ facturaNumeroControl }}</span></div>
          <div class="factura-info-row"><span class="factura-label">Estado</span><span class="factura-status">Pendiente de verificación</span></div>
        </div>
        <div class="success-actions">
          <button type="button" class="btn-download" @click="descargarComprobante"><Download :size="18" /> Descargar Comprobante</button>
          <router-link to="/cliente/historial-pagos" class="btn-history"><Receipt :size="18" /> Ver en Historial</router-link>
        </div>
        <button type="button" class="btn-back-dashboard" @click="goToDashboard" style="margin-top: 8px;"><LayoutDashboard :size="18" /> Volver al Panel</button>
      </div>
    </div>

    <!-- ACCIONES DEL STEPPER -->
    <div v-if="currentStep < 4" class="step-actions">
      <button type="button" class="btn-cancel" @click="cancelProcess"><X :size="16" /> Cancelar</button>
      <div class="step-actions-right">
        <button v-if="currentStep > 1" type="button" class="btn-back" @click="goBack"><ArrowLeft :size="16" /> Atrás</button>
        <button v-if="currentStep === 1" type="button" class="btn-next" :disabled="!canGoNextFromStep1" @click="goToStep2">Siguiente <ArrowRight :size="16" /></button>
        <button v-if="currentStep === 2" type="button" class="btn-next" :disabled="!selectedBloque || isConfirmingSelection" @click="confirmarSeleccion">
          <div v-if="isConfirmingSelection" class="spinner spinner-white spinner-sm" />
          <template v-else>Confirmar Selección <ArrowRight :size="16" /></template>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
// El script se mantiene exactamente igual que en el archivo original.
// (No se modifica para preservar la lógica de negocio)
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Check, ChevronLeft, ChevronRight, Clock, PawPrint, Stethoscope,
  CreditCard, CalendarDays, ClipboardList, FileText, MessageSquare,
  DollarSign, Lock, AlertTriangle, X, ArrowLeft, ArrowRight,
  LayoutDashboard, Download, Receipt
} from 'lucide-vue-next'
import { getMisMascotas } from '@/api/mascotas.api'
import { getServicios, getVeterinarios, getDisponibilidad, solicitarCita } from '@/api/citas.api'
import { getMetodosOnline, procesarPagoCita, descargarFactura } from '@/api/pagos.api'

const router = useRouter()
const DIAS_SEMANA = ['Lun','Mar','Mié','Jue','Vie','Sáb','Dom']
const MESES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']
const MAX_MONTHS_AHEAD = 3
const BANCOS_VENEZUELA = [
  {codigo:'0102',nombre:'Banco de Venezuela, S.A.C.A.'},{codigo:'0104',nombre:'Venezolano de Crédito, S.A.'},
  {codigo:'0105',nombre:'Mercantil Banco, C.A.'},{codigo:'0108',nombre:'Banco Provincial, S.A.'},
  {codigo:'0114',nombre:'Banco del Caribe, C.A.'},{codigo:'0115',nombre:'Banco Exterior, C.A.'},
  {codigo:'0116',nombre:'Banco Occidental de Descuento, B.O.D.'},{codigo:'0128',nombre:'Banco Caroní, C.A.'},
  {codigo:'0134',nombre:'Banesco Banco Universal, C.A.'},{codigo:'0137',nombre:'Banco Sofitasa, C.A.'},
  {codigo:'0138',nombre:'Banco Plaza, C.A.'},{codigo:'0146',nombre:'Banco de la Gente Emprendedora, C.A. (Bangente)'},
  {codigo:'0151',nombre:'Banco Fondo Común, C.A.'},{codigo:'0156',nombre:'100% Banco, C.A.'},
  {codigo:'0157',nombre:'Banco del Sur, C.A.'},{codigo:'0163',nombre:'Banco del Tesoro, C.A.'},
  {codigo:'0166',nombre:'Banco Agrícola de Venezuela, C.A.'},{codigo:'0168',nombre:'Bancrecer, C.A.'},
  {codigo:'0169',nombre:'Mi Banco, C.A.'},{codigo:'0171',nombre:'Banco Activo, C.A.'},
  {codigo:'0172',nombre:'Bancamiga, C.A.'},{codigo:'0173',nombre:'Banco Internacional de Desarrollo, C.A.'},
  {codigo:'0174',nombre:'Banplus, C.A.'},{codigo:'0175',nombre:'Banco Bicentenario del Pueblo, C.A.'},
  {codigo:'0177',nombre:'Banco de la Fuerza Armada Nacional Bolivariana, B.A.N.F.A.N.B.'},
  {codigo:'0190',nombre:'Banco Nacional de Crédito, C.A.'},{codigo:'0191',nombre:'Banco del Pueblo Soberano, C.A.'}
]
const steps = [{num:1,label:'Datos'},{num:2,label:'Horario'},{num:3,label:'Pago'}]

const currentStep = ref(1)
const isLoadingInitial = ref(true)
const loadError = ref(null)
const isLoadingServicios = ref(false)
const mascotas = ref([])
const servicios = ref([])
const veterinarios = ref([])
const selectedMascota = ref(null)
const selectedVeterinario = ref(null)
const selectedServicio = ref(null)
const motivoConsulta = ref('')
const stepErrors = ref({})
const calendarYear = ref(new Date().getFullYear())
const calendarMonth = ref(new Date().getMonth())
const selectedDate = ref(null)
const selectedBloque = ref(null)
const disponibilidadCache = ref({})
const isLoadingSlots = ref(false)
const disponibilidadError = ref(null)
const isConfirmingSelection = ref(false)
const confirmSelectionError = ref(null)
const citaPendienteId = ref(null)
const citaResumen = ref(null)
const isProcessingPayment = ref(false)
const pagoError = ref(null)
const metodosPago = ref([])
const selectedMetodoPago = ref(null)
const datosPago = ref({})
const isLoadingMetodos = ref(false)
const pagoFormErrors = ref({})
const referenciaTransaccion = ref('')
const facturaId = ref(null)
const facturaNumeroControl = ref(null)
const facturaEmailEnviado = ref(false)
const facturaAdvertencia = ref(null)

const servicioSeleccionado = computed(() => servicios.value.find(s => s.id === selectedServicio.value) || null)
const vetSeleccionado = computed(() => veterinarios.value.find(v => v.id === selectedVeterinario.value) || null)
const calendarMonthLabel = computed(() => `${MESES[calendarMonth.value]} ${calendarYear.value}`)
const canGoPrevMonth = computed(() => { const now = new Date(); const prev = new Date(calendarYear.value, calendarMonth.value - 1, 1); return prev >= new Date(now.getFullYear(), now.getMonth(), 1) })
const canGoNextMonth = computed(() => { const limit = new Date(); limit.setMonth(limit.getMonth() + MAX_MONTHS_AHEAD); const next = new Date(calendarYear.value, calendarMonth.value + 1, 1); return next <= new Date(limit.getFullYear(), limit.getMonth(), 1) })
const canGoNextFromStep1 = computed(() => selectedMascota.value && selectedVeterinario.value && selectedServicio.value && motivoConsulta.value.trim().length > 0)
const calendarDays = computed(() => {
  const year = calendarYear.value, month = calendarMonth.value
  const firstDay = new Date(year, month, 1)
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const offset = firstDay.getDay() === 0 ? 6 : firstDay.getDay() - 1
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const days = []
  const prevLast = new Date(year, month, 0).getDate()
  for (let i = offset - 1; i >= 0; i--) { const d = new Date(year, month - 1, prevLast - i); days.push({date:d, isCurrentMonth:false, isPast:true, isToday:false}) }
  for (let d = 1; d <= daysInMonth; d++) { const date = new Date(year, month, d); days.push({date, isCurrentMonth:true, isPast:date < today, isToday:date.getTime() === today.getTime()}) }
  const totalRows = Math.ceil(days.length / 7)
  const remaining = totalRows * 7 - days.length
  for (let d = 1; d <= remaining; d++) { days.push({date:new Date(year, month + 1, d), isCurrentMonth:false, isPast:false, isToday:false}) }
  return days
})
const fechaFueConsultada = computed(() => { if (!selectedDate.value) return false; const key = formatDateISO(selectedDate.value); return key in disponibilidadCache.value })
const currentSlots = computed(() => { if (!selectedDate.value) return []; const key = formatDateISO(selectedDate.value); return disponibilidadCache.value[key] || [] })
const showNoSlotsAlert = computed(() => selectedDate.value && fechaFueConsultada.value && currentSlots.value.length === 0 && !disponibilidadError.value)
const metodoSeleccionado = computed(() => metodosPago.value.find(m => m.id === selectedMetodoPago.value) || null)
const camposDinamicos = computed(() => { if (!metodoSeleccionado.value?.camposRequeridos) return []; return Object.entries(metodoSeleccionado.value.camposRequeridos).filter(([key]) => key !== 'referencia').map(([key, tipo]) => ({key, tipo})) })

onMounted(() => { loadInitialData() })
watch([selectedServicio, selectedVeterinario], () => { selectedDate.value = null; selectedBloque.value = null; disponibilidadCache.value = {}; disponibilidadError.value = null })

async function loadInitialData() {
  isLoadingInitial.value = true; loadError.value = null
  try {
    const [mascotasRes, vetsRes] = await Promise.all([getMisMascotas(), getVeterinarios()])
    mascotas.value = mascotasRes.data; veterinarios.value = vetsRes.data
  } catch (err) { console.error('Error cargando datos iniciales:', err); loadError.value = err.response?.data?.mensaje || 'No se pudieron cargar los datos necesarios.' }
  finally { isLoadingInitial.value = false }
}

async function onVeterinarioChange() {
  selectedServicio.value = null; stepErrors.value = {}; servicios.value = []
  if (!selectedVeterinario.value) return
  const vet = vetSeleccionado.value; if (!vet?.especialidad) return
  isLoadingServicios.value = true
  try { const { data } = await getServicios({especialidad: vet.especialidad}); servicios.value = data }
  catch (err) { console.error('Error cargando servicios:', err) }
  finally { isLoadingServicios.value = false }
}

function validateStep1() {
  const errors = {}
  if (!selectedMascota.value) errors.mascota = 'Selecciona una mascota'
  if (!selectedVeterinario.value) errors.veterinario = 'Selecciona un veterinario'
  if (!selectedServicio.value) errors.servicio = 'Selecciona un servicio'
  if (!motivoConsulta.value.trim()) errors.motivo = 'Escribe el motivo de la consulta'
  stepErrors.value = errors; return Object.keys(errors).length === 0
}
function goToStep2() { if (!validateStep1()) return; stepErrors.value = {}; currentStep.value = 2 }
function goBack() { pagoError.value = null; pagoFormErrors.value = {}; currentStep.value-- }
async function cancelProcess() { goToDashboard() }
function goToDashboard() { router.push('/cliente/dashboard') }

function prevMonth() { if (!canGoPrevMonth.value) return; const d = new Date(calendarYear.value, calendarMonth.value - 1, 1); calendarYear.value = d.getFullYear(); calendarMonth.value = d.getMonth() }
function nextMonth() { if (!canGoNextMonth.value) return; const d = new Date(calendarYear.value, calendarMonth.value + 1, 1); calendarYear.value = d.getFullYear(); calendarMonth.value = d.getMonth() }
function isDateSelected(date) { if (!selectedDate.value) return false; return formatDateISO(date) === formatDateISO(selectedDate.value) }
function hasSlots(date) { const key = formatDateISO(date); return disponibilidadCache.value[key]?.length > 0 }
function hasNoSlots(date) { const key = formatDateISO(date); return key in disponibilidadCache.value && disponibilidadCache.value[key].length === 0 }

async function selectDate(day) {
  if (!day.isCurrentMonth || day.isPast) return
  const dateKey = formatDateISO(day.date)
  if (selectedDate.value && formatDateISO(selectedDate.value) === dateKey) return
  selectedDate.value = day.date; selectedBloque.value = null; disponibilidadError.value = null; confirmSelectionError.value = null
  if (!(dateKey in disponibilidadCache.value)) { await fetchDisponibilidad(dateKey) }
}

async function fetchDisponibilidad(dateStr) {
  isLoadingSlots.value = true; disponibilidadError.value = null
  try {
    const { data } = await getDisponibilidad({id_veterinario: selectedVeterinario.value, fecha: dateStr, id_servicio: selectedServicio.value})
    disponibilidadCache.value[dateStr] = data.bloquesDisponibles || []
  } catch (err) { console.error('Error consultando disponibilidad:', err); disponibilidadError.value = err.response?.data?.mensaje || 'Error al consultar disponibilidad. Intenta de nuevo.'; disponibilidadCache.value[dateStr] = [] }
  finally { isLoadingSlots.value = false }
}
function retryDisponibilidad() { if (!selectedDate.value) return; const key = formatDateISO(selectedDate.value); delete disponibilidadCache.value[key]; fetchDisponibilidad(key) }
function isBloqueSelected(bloque) { return selectedBloque.value?.horaInicio === bloque.horaInicio }
function selectBloque(bloque) { selectedBloque.value = bloque; confirmSelectionError.value = null }

async function confirmarSeleccion() {
  if (!selectedBloque.value || !selectedDate.value) return
  isConfirmingSelection.value = true; confirmSelectionError.value = null
  try {
    const { data } = await solicitarCita({idMascota: selectedMascota.value, idVeterinario: selectedVeterinario.value, idServicio: selectedServicio.value, fechaCita: formatDateISO(selectedDate.value), horaInicio: selectedBloque.value.horaInicio, motivoConsulta: motivoConsulta.value.trim()})
    citaPendienteId.value = data.idCita; citaResumen.value = data.resumen; pagoError.value = null; currentStep.value = 3; await loadMetodosPago()
  } catch (err) {
    console.error('Error al solicitar cita:', err)
    const msg = err.response?.data?.mensaje
    if (msg && msg.includes('ya no está disponible')) { confirmSelectionError.value = msg; const key = formatDateISO(selectedDate.value); delete disponibilidadCache.value[key]; await fetchDisponibilidad(key); selectedBloque.value = null }
    else { confirmSelectionError.value = msg || 'No se pudo bloquear el horario. Intenta de nuevo.' }
  } finally { isConfirmingSelection.value = false }
}

async function procesarPago() {
  if (!citaPendienteId.value || !selectedMetodoPago.value) return
  const camposRequeridos = metodoSeleccionado.value?.camposRequeridos || {}
  const errors = {}
  if ('referencia' in camposRequeridos) { if (!referenciaTransaccion.value.trim()) errors.referencia = 'La referencia es obligatoria' }
  for (const { key } of camposDinamicos.value) { if (!datosPago.value[key]?.trim()) errors[key] = 'Este campo es obligatorio' }
  pagoFormErrors.value = errors; if (Object.keys(errors).length > 0) return
  isProcessingPayment.value = true; pagoError.value = null
  try {
    const datosPagoCompletos = { ...datosPago.value }
    if ('referencia' in camposRequeridos) datosPagoCompletos.referencia = referenciaTransaccion.value.trim()
    const { data } = await procesarPagoCita({idCita: citaPendienteId.value, idMetodoPago: selectedMetodoPago.value, referenciaTransaccion: referenciaTransaccion.value.trim(), datosPago: datosPagoCompletos})
    facturaId.value = data.idFactura; facturaNumeroControl.value = data.numeroControl; facturaEmailEnviado.value = data.emailEnviado; facturaAdvertencia.value = data.advertenciaEmail
    const savedResumen = { ...citaResumen.value }; citaPendienteId.value = null; citaResumen.value = savedResumen; currentStep.value = 4
  } catch (err) { console.error('Error procesando pago:', err); const msg = err.response?.data?.mensaje || err.response?.data?.message; pagoError.value = msg || 'Transacción rechazada o datos inválidos. Verifique e intente nuevamente.' }
  finally { isProcessingPayment.value = false }
}

function formatDateISO(date) { if (!date) return ''; if (typeof date === 'string') return date; const y = date.getFullYear(); const m = String(date.getMonth() + 1).padStart(2, '0'); const d = String(date.getDate()).padStart(2, '0'); return `${y}-${m}-${d}` }
function formatDateDisplay(dateStr) { if (!dateStr) return ''; const d = typeof dateStr === 'string' ? new Date(dateStr + 'T12:00:00') : dateStr; return `${d.getDate()} de ${MESES[d.getMonth()]} de ${d.getFullYear()}` }
function formatTime12h(timeStr) { if (!timeStr) return ''; const parts = timeStr.split(':'); const h = parseInt(parts[0], 10); const m = parts[1] || '00'; const period = h >= 12 ? 'PM' : 'AM'; const h12 = h % 12 || 12; return `${h12}:${m} ${period}` }
function formatCurrency(amount) { if (amount == null) return '$0.00'; return `$${Number(amount).toFixed(2)} USD` }
function formatCurrencyBs(amount) { if (amount == null) return 'Bs. 0.00'; return `Bs. ${Number(amount).toFixed(2)}` }
async function loadMetodosPago() { isLoadingMetodos.value = true; try { const { data } = await getMetodosOnline(); metodosPago.value = data } catch (err) { console.error('Error cargando métodos:', err) } finally { isLoadingMetodos.value = false } }
function onMetodoPagoChange() { datosPago.value = {}; referenciaTransaccion.value = ''; pagoFormErrors.value = {}; pagoError.value = null }
function resetPagoForm() { selectedMetodoPago.value = null; datosPago.value = {}; referenciaTransaccion.value = ''; pagoFormErrors.value = {}; pagoError.value = null }
function formatFieldLabel(key) { const labels = {banco:'Banco emisor', numero_cuenta:'Número de cuenta', telefono:'Número de teléfono asociado', lote:'Número de lote', ultimos_digitos:'Últimos 4 dígitos de la tarjeta'}; return labels[key] || key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase()) }
function formatFieldPlaceholder(key) { const ph = {banco:'Ej: Banesco, Mercantil, Provincial...', numero_cuenta:'Ej: 0000-0000-00-0000000000', telefono:'Ej: 04141234567'}; return ph[key] || '' }
async function descargarComprobante() { if (!facturaId.value) return; try { const response = await descargarFactura(facturaId.value); const url = window.URL.createObjectURL(new Blob([response.data])); const link = document.createElement('a'); link.href = url; link.setAttribute('download', `Factura-${facturaNumeroControl.value}.pdf`); document.body.appendChild(link); link.click(); link.remove(); window.URL.revokeObjectURL(url) } catch (err) { console.error('Error descargando factura:', err) } }
</script>

<style scoped>
/* ═══════════════════════════════════════════
   CONTENEDOR PRINCIPAL - ALINEADO CON DASHBOARD
   ═══════════════════════════════════════════ */
.appointment-view {
  max-width: 1400px;
  margin: 0 auto;
  padding: clamp(12px, 3vw, 24px);
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  box-sizing: border-box;
}

.label-loading {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  font-weight: 500;
  color: #64748B;
  margin-left: 8px;
}

/* PAGE HEADER */
.page-header {
  margin-bottom: clamp(16px, 3vw, 24px);
}

.page-header h1 {
  font-size: clamp(1.25rem, 3vw, 1.75rem);
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 4px 0;
  line-height: 1.2;
}

.page-subtitle {
  font-size: clamp(0.8rem, 2vw, 0.95rem);
  color: #64748B;
  margin: 0;
}

/* STEPPER */
/* STEPPER CONTENEDOR */
.stepper {
  display: flex;
  flex-direction: row;
  align-items: center;
  background: #ffffff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: clamp(12px, 2vw, 20px) clamp(16px, 3vw, 32px);
  margin-bottom: clamp(16px, 3vw, 24px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 10px 15px -3px rgba(0, 0, 0, 0.05);
}

.stepper-step {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  flex: 1;
  min-width: 0;
}

.stepper-step:last-child {
  flex: 0 0 auto;
}

.stepper-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
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
  font-size: 0.875rem;
  font-weight: 600;
  color: #94A3B8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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
  margin: 0 12px;
  border-radius: 1px;
  transition: background 0.25s ease;
  min-width: 12px;
}

.stepper-step.completed .stepper-line {
  background: #0F766E;
}

/* RESPONSIVE: Cambio de layout a vertical en pantallas pequeñas */
@media (max-width: 640px) {
  .stepper {
    flex-direction: column;
    align-items: flex-start;
    padding: 16px;
    gap: 12px;
  }

  .stepper-step {
    width: 100%;
    flex: none;
    flex-direction: row;
  }

  .stepper-step:last-child {
    flex: none;
  }

  .stepper-line {
    /* La línea pasa a ser vertical bajo el círculo */
    position: absolute;
    top: 36px;
    left: 17px;
    width: 2px;
    height: 12px;
    margin: 0;
    min-width: 0;
  }

  .stepper-step:last-child .stepper-line {
    display: none;
  }

  .stepper-label {
    white-space: normal;
  }
}

/* CARDS */
.card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 20px 25px -5px rgba(0, 0, 0, 0.05);
}

.card-header {
  padding: clamp(14px, 3vw, 20px) clamp(16px, 3vw, 24px);
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.card-header h3 {
  font-size: clamp(0.9rem, 2vw, 1rem);
  font-weight: 600;
  color: #1E293B;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.card-body {
  padding: clamp(16px, 3vw, 24px);
}

/* LOADING */
.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: clamp(32px, 6vw, 64px) clamp(16px, 3vw, 24px);
  gap: 16px;
  color: #64748B;
  font-size: 0.9rem;
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

/* ALERTAS */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: clamp(10px, 2vw, 14px) clamp(14px, 3vw, 18px);
  border-radius: 10px;
  font-size: 0.85rem;
  line-height: 1.5;
  margin-top: 16px;
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
  font-size: 0.8rem;
  cursor: pointer;
  text-decoration: underline;
  white-space: nowrap;
  font-family: inherit;
  flex-shrink: 0;
}

/* FORMULARIOS */
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: clamp(12px, 3vw, 20px);
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
  font-size: clamp(0.8rem, 2vw, 0.85rem);
  font-weight: 600;
  color: #374151;
}

.required {
  color: #EF4444;
}

.form-select,
.form-textarea,
input.form-select {
  width: 100%;
  padding: clamp(10px, 2vw, 12px) clamp(12px, 2.5vw, 16px);
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  font-size: clamp(0.85rem, 2vw, 1rem);
  color: #1E293B;
  background: #ffffff;
  transition: border-color 0.2s, box-shadow 0.2s;
  font-family: inherit;
  appearance: none;
  -webkit-appearance: none;
  box-sizing: border-box;
}

.form-select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  padding-right: 40px;
  cursor: pointer;
}

.form-select:focus,
.form-textarea:focus,
input.form-select:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.12);
}

.form-select.is-invalid,
.form-textarea.is-invalid {
  border-color: #EF4444;
}

.form-select.is-invalid:focus,
.form-textarea.is-invalid:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.form-error {
  font-size: 0.8rem;
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
  font-size: 0.75rem;
  color: #94A3B8;
}

/* Info del servicio */
.service-detail {
  margin-top: 8px;
  padding: clamp(10px, 2vw, 14px) clamp(14px, 3vw, 18px);
  background: #F0FDFA;
  border: 1px solid #99F6E4;
  border-radius: 10px;
}

.service-desc {
  font-size: 0.85rem;
  color: #115E59;
  margin: 0 0 10px 0;
  line-height: 1.5;
}

.service-meta {
  display: flex;
  align-items: center;
  gap: clamp(8px, 2vw, 16px);
  flex-wrap: wrap;
}

.service-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #0F766E;
}

.service-badge {
  font-size: 0.75rem;
  font-weight: 600;
  color: #0F766E;
  background: #CCFBF1;
  padding: 4px 12px;
  border-radius: 20px;
  border: 1px solid #99F6E4;
}

/* CALENDARIO */
.calendar {
  margin-bottom: 8px;
}

.calendar-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: clamp(12px, 3vw, 20px);
}

.cal-nav-btn {
  width: clamp(36px, 8vw, 40px);
  height: clamp(36px, 8vw, 40px);
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  background: #ffffff;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
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
  font-size: clamp(0.9rem, 3vw, 1.05rem);
  font-weight: 700;
  color: #1E293B;
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  margin-bottom: 6px;
}

.cal-weekday {
  text-align: center;
  font-size: clamp(0.65rem, 2vw, 0.7rem);
  font-weight: 700;
  color: #94A3B8;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 8px 0;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: clamp(2px, 1vw, 4px);
}

.cal-day {
  width: 100%;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: clamp(0.8rem, 2.5vw, 0.95rem);
  font-weight: 500;
  color: #1E293B;
  border: 2px solid transparent;
  border-radius: 12px;
  background: none;
  cursor: pointer;
  transition: all 0.15s ease;
  position: relative;
  font-family: inherit;
  padding: 0;
  min-width: 0;
  min-height: 0;
  box-sizing: border-box;
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

.vet-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #0F766E;
  background: #F0FDFA;
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid #99F6E4;
}

/* BLOQUES HORARIOS */
.slots-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #E2E8F0;
}

.slots-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: clamp(0.85rem, 2vw, 0.9rem);
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
  font-size: 0.85rem;
}

.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(140px, 100%), 1fr));
  gap: 10px;
}

.slot-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 16px 12px;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  background: #F8FAFC;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
  min-height: 60px;
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
  font-size: clamp(0.85rem, 2.5vw, 0.95rem);
  font-weight: 700;
  color: #1E293B;
  transition: color 0.2s;
}

.slot-btn.is-selected .slot-time {
  color: #ffffff;
}

.slot-separator {
  font-size: 0.65rem;
  color: #94A3B8;
  transition: color 0.2s;
}

.slot-btn.is-selected .slot-separator {
  color: rgba(255, 255, 255, 0.7);
}

.slot-end {
  font-size: 0.8rem;
  font-weight: 500;
  color: #64748B;
  transition: color 0.2s;
}

.slot-btn.is-selected .slot-end {
  color: rgba(255, 255, 255, 0.85);
}

/* MINI RESUMEN */
.mini-summary {
  margin-top: 24px;
  padding: clamp(14px, 3vw, 18px) clamp(16px, 3vw, 24px);
  background: #F0FDFA;
  border: 1px solid #99F6E4;
  border-radius: 12px;
  display: flex;
  gap: clamp(12px, 3vw, 32px);
  flex-wrap: wrap;
}

.mini-summary-row {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.mini-label {
  font-size: 0.7rem;
  font-weight: 600;
  color: #64748B;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.mini-value {
  font-size: clamp(0.85rem, 2vw, 0.9rem);
  font-weight: 600;
  color: #115E59;
}

/* STEP 3: RESUMEN Y PAGO */
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
  padding: clamp(10px, 2vw, 14px) 0;
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
  font-size: 0.85rem;
  color: #64748B;
  white-space: nowrap;
  flex-shrink: 0;
}

.summary-value {
  font-size: clamp(0.85rem, 2vw, 0.9rem);
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

.reserved-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 0.7rem;
  font-weight: 600;
  color: #D97706;
  background: #FFFBEB;
  padding: 4px 12px;
  border-radius: 20px;
  border: 1px solid #FDE68A;
  flex-shrink: 0;
}

.payment-rows {
  flex: 1;
  margin-bottom: 24px;
}

.payment-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  font-size: clamp(0.85rem, 2vw, 0.9rem);
  color: #475569;
}

.payment-total {
  font-size: clamp(1rem, 3vw, 1.1rem);
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
  padding: clamp(12px, 3vw, 16px) clamp(16px, 4vw, 24px);
  background: #0F766E;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: clamp(0.9rem, 2.5vw, 1rem);
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
  margin-top: 14px;
  font-size: 0.75rem;
  color: #94A3B8;
  text-align: center;
  margin-bottom: 0;
  line-height: 1.5;
}

/* PASARELA DE PAGO */
.payment-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: clamp(16px, 4vw, 24px);
  align-items: start;
}

.payment-gateway-card {
  position: sticky;
  top: 24px;
}

.method-option {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: clamp(12px, 3vw, 16px) clamp(14px, 3vw, 18px);
  border: 1px solid #E2E8F0;
  border-radius: 12px;
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
  width: 22px;
  height: 22px;
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
  gap: 3px;
}

.method-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: #1E293B;
}

.method-desc {
  font-size: 0.8rem;
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

/* STEP 4: ÉXITO */
.success-container {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  padding: clamp(32px, 8vw, 64px) clamp(16px, 5vw, 32px);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 14px;
}

.success-icon {
  width: clamp(64px, 15vw, 96px);
  height: clamp(64px, 15vw, 96px);
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
  font-size: clamp(1.25rem, 4vw, 1.6rem);
  font-weight: 700;
  color: #1E293B;
  margin: 0;
}

.success-message {
  font-size: clamp(0.85rem, 2.5vw, 0.95rem);
  color: #64748B;
  margin: 0;
  max-width: 420px;
  line-height: 1.5;
}

.success-factura-info {
  margin-top: 8px;
  padding: clamp(12px, 3vw, 16px) clamp(16px, 4vw, 24px);
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  max-width: 420px;
}

.factura-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.factura-label {
  font-size: 0.85rem;
  color: #64748B;
}

.factura-value {
  font-size: 0.9rem;
  font-weight: 700;
  color: #1E293B;
  font-family: 'Courier New', monospace;
}

.factura-status {
  font-size: 0.8rem;
  font-weight: 600;
  color: #D97706;
  background: #FFFBEB;
  padding: 4px 12px;
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
  padding: clamp(12px, 3vw, 14px) clamp(20px, 5vw, 28px);
  background: #0F766E;
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 0.9rem;
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
  padding: clamp(12px, 3vw, 14px) clamp(20px, 5vw, 28px);
  background: #ffffff;
  color: #475569;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  font-size: 0.9rem;
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

.btn-back-dashboard {
  margin-top: 16px;
  padding: clamp(10px, 2.5vw, 12px) clamp(20px, 5vw, 28px);
  background: #0F766E;
  color: #ffffff;
  border: none;
  border-radius: 10px;
  font-size: 0.9rem;
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

/* STEP ACTIONS */
.step-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: clamp(16px, 3vw, 24px);
  padding-top: clamp(16px, 3vw, 24px);
  border-top: 1px solid #E2E8F0;
}

.step-actions-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn-cancel {
  padding: clamp(8px, 2vw, 10px) clamp(16px, 4vw, 20px);
  background: none;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  color: #64748B;
  font-size: 0.85rem;
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
  padding: clamp(8px, 2vw, 10px) clamp(16px, 4vw, 20px);
  background: #ffffff;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  color: #475569;
  font-size: 0.85rem;
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
  padding: clamp(8px, 2vw, 10px) clamp(20px, 5vw, 24px);
  background: #0F766E;
  border: none;
  border-radius: 8px;
  color: #ffffff;
  font-size: 0.85rem;
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
   RESPONSIVE - MEJORADO COMPLETAMENTE
   ═══════════════════════════════════════════ */
@media (max-width: 1024px) {
  .payment-layout {
    grid-template-columns: 1fr;
  }
  .payment-gateway-card {
    position: static;
  }
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .stepper-label {
    font-size: 0.7rem;
  }
  .stepper-line {
    margin: 0 8px;
  }
  .step-actions {
    flex-direction: column-reverse;
    gap: 12px;
    align-items: stretch;
  }
  .step-actions-right {
    width: 100%;
    justify-content: stretch;
    flex-wrap: wrap;
  }
  .btn-cancel,
  .btn-back,
  .btn-next {
    width: 100%;
    justify-content: center;
    padding: 14px 20px;
    font-size: 0.9rem;
  }
  .btn-next {
    order: -1;
  }
  .success-actions {
    flex-direction: column;
    width: 100%;
    align-items: stretch;
  }
  .btn-download,
  .btn-history {
    width: 100%;
    justify-content: center;
  }
  .method-option {
    padding: 12px 14px;
  }
}

@media (max-width: 480px) {
  .stepper-label {
    display: none;
  }
  .stepper-step {
    justify-content: center;
  }
  .stepper {
    padding: 12px 16px;
  }
  .slots-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }
  .cal-day {
    font-size: 0.75rem;
    border-radius: 8px;
  }
  .summary-row {
    flex-direction: column;
    gap: 4px;
    padding: 10px 0;
  }
  .summary-value {
    text-align: left;
  }
  .payment-total {
    font-size: 1rem;
  }
}

@media (max-width: 360px) {
  .slots-grid {
    grid-template-columns: 1fr;
  }
  .cal-day {
    font-size: 0.7rem;
    border-radius: 6px;
  }
  .calendar-nav {
    margin-bottom: 8px;
  }
  .cal-nav-btn {
    width: 36px;
    height: 36px;
  }
}
</style>