<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMisCitas, cancelarCita } from '@/api/citas.api.js'
import { getMetodosOnline, procesarPagoCita, descargarFactura } from '@/api/pagos.api.js'
import {
  CreditCard, Download, CheckCircle2, AlertCircle, AlertTriangle,
  X, Loader2, Calendar, Clock, PawPrint, Stethoscope, CalendarPlus,
  ReceiptText, Banknote
} from 'lucide-vue-next'

const router = useRouter()

const ESTADO_PENDIENTE = 'Pendiente_Pago'
const ESTADOS_CANCELABLES = ['Pendiente_Pago', 'Pagada', 'Confirmada']

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

// ─── ESTADO ───
const tab = ref('pendientes')
const citas = ref([])
const cargando = ref(false)
const cancelando = ref(null)
const toast = ref({ visible: false, message: '', type: 'success' })

// Confirmación de cancelación
const mostrarModalCancelacion = ref(false)
const citaACancelar = ref(null)

// Modal de pago
const mostrarModal = ref(false)
const citaActual = ref(null)
const metodos = ref([])
const cargandoMetodos = ref(false)
const form = ref({ idMetodoPago: '', referenciaTransaccion: '' })
const datosPago = ref({})
const procesando = ref(false)
const errorPago = ref('')
const pagoExitoso = ref(null)
const descargando = ref(false)

watch(() => form.value.idMetodoPago, () => {
  datosPago.value = {}
  form.value.referenciaTransaccion = ''
})

// ─── HELPERS ───

function showToast(message, type = 'success') {
  toast.value = { visible: true, message, type }
  setTimeout(() => { toast.value.visible = false }, 4000)
}

function getAvatarColor(str) {
  const colors = ['#0F766E', '#3B82F6', '#F59E0B', '#EF4444', '#8B5CF6', '#EC4899']
  let hash = 0
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}

const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

function formatearFecha(iso) {
  if (!iso) return ''
  const [y, m, d] = iso.split('-')
  return `${d} ${MESES[Number(m) - 1]} ${y}`
}

function formatearHora(h) {
  return (h || '').split(':').slice(0, 2).join(':')
}

function fmtUsd(v) {
  return `$ ${Number(v).toFixed(2)}`
}

function fmtBs(v) {
  return `Bs. ${Number(v).toLocaleString('es-VE', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function badgeEstadoCita(estado) {
  switch (estado) {
    case 'Pagada':
    case 'Confirmada':
      return 'verificado'
    case 'Pendiente_Pago':
      return 'pendiente'
    case 'Cancelada':
      return 'rechazado'
    default:
      return 'otro'
  }
}

function extraerError(e) {
  return e?.response?.data?.message || e?.response?.data?.error || ''
}

// ─── LISTADO ───

async function cargarCitas() {
  cargando.value = true
  try {
    const estado = tab.value === 'pendientes' ? ESTADO_PENDIENTE : undefined
    const { data } = await getMisCitas(estado)
    citas.value = data
  } catch (err) {
    showToast('Error al cargar tus citas', 'error')
  } finally {
    cargando.value = false
  }
}

function cambiarTab(t) {
  if (tab.value === t) return
  tab.value = t
  cargarCitas()
}

// ─── MODAL DE PAGO ───

async function abrirModalPago(cita) {
  citaActual.value = cita
  pagoExitoso.value = null
  errorPago.value = ''
  form.value = { idMetodoPago: '', referenciaTransaccion: '' }
  datosPago.value = {}
  metodos.value = []
  mostrarModal.value = true
  cargandoMetodos.value = true
  try {
    const { data } = await getMetodosOnline()
    metodos.value = data
  } catch {
    errorPago.value = 'No se pudieron cargar los métodos de pago.'
  } finally {
    cargandoMetodos.value = false
  }
}

function cerrarModalPago() {
  if (procesando.value) return
  if (pagoExitoso.value) {
    finalizar()
    return
  }
  mostrarModal.value = false
}

const metodoSeleccionado = computed(() =>
  metodos.value.find((m) => m.id === Number(form.value.idMetodoPago))
)

// Excluir 'referencia' de los campos dinámicos (se maneja aparte)
const camposDinamicos = computed(() => {
  const campos = metodoSeleccionado.value?.camposRequeridos
  if (!campos) return []
  return Object.entries(campos).filter(([key]) => key !== 'referencia')
})

function onMetodoPagoChange() {
  datosPago.value = {}
  form.value.referenciaTransaccion = ''
  errorPago.value = ''
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
    banco: 'Seleccione el banco emisor',
    numero_cuenta: 'Ej: 0000-0000-00-0000000000',
    telefono: 'Ej: 04141234567'
  }
  return ph[key] || ''
}

async function pagar() {
  errorPago.value = ''
  if (!form.value.idMetodoPago) {
    errorPago.value = 'Selecciona un método de pago.'
    return
  }

  const camposRequeridos = metodoSeleccionado.value?.camposRequeridos || {}

  // Validar referencia solo si el método la requiere
  if ('referencia' in camposRequeridos && !form.value.referenciaTransaccion.trim()) {
    errorPago.value = 'Ingresa la referencia de la transacción.'
    return
  }

  // Validar campos dinámicos
  for (const [campo, etiqueta] of camposDinamicos.value) {
    if (!String(datosPago.value[campo] ?? '').trim()) {
      errorPago.value = `Completa el campo "${etiqueta}".`
      return
    }
  }

  // Construir datosPagoCompletos incluyendo referencia si es necesaria
  const datosPagoCompletos = { ...datosPago.value }
  if ('referencia' in camposRequeridos) {
    datosPagoCompletos.referencia = form.value.referenciaTransaccion.trim()
  }

  procesando.value = true
  try {
    const { data } = await procesarPagoCita({
      idCita: citaActual.value.idCita,
      idMetodoPago: form.value.idMetodoPago,
      referenciaTransaccion: form.value.referenciaTransaccion.trim(),
      datosPago: datosPagoCompletos,
    })
    pagoExitoso.value = data
  } catch (e) {
    errorPago.value =
      extraerError(e) ||
      'No se pudo procesar el pago. Verifica los datos e intenta nuevamente.'
  } finally {
    procesando.value = false
  }
}

async function descargar() {
  const idFactura = pagoExitoso.value?.idFactura
  if (!idFactura) return
  descargando.value = true
  try {
    const res = await descargarFactura(idFactura)
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.download = `Factura-${pagoExitoso.value.numeroControl}.pdf`
    link.click()
    window.URL.revokeObjectURL(url)
    showToast('Factura descargada exitosamente')
  } catch {
    showToast('No se pudo descargar la factura', 'error')
  } finally {
    descargando.value = false
  }
}

function finalizar() {
  mostrarModal.value = false
  citaActual.value = null
  cargarCitas()
}

// ─── CANCELAR CITA ───

function abrirConfirmacionCancelacion(cita) {
  citaACancelar.value = cita
  mostrarModalCancelacion.value = true
}

function cerrarConfirmacionCancelacion() {
  mostrarModalCancelacion.value = false
  citaACancelar.value = null
}

async function confirmarCancelacionDefinitiva() {
  if (!citaACancelar.value) return
  cancelando.value = citaACancelar.value.idCita
  try {
    await cancelarCita(citaACancelar.value.idCita)
    showToast('Cita cancelada exitosamente')
    await cargarCitas()
  } catch (e) {
    showToast(extraerError(e) || 'No se pudo cancelar la cita', 'error')
  } finally {
    cancelando.value = null
    cerrarConfirmacionCancelacion()
  }
}

onMounted(cargarCitas)
</script>

<template>
  <div class="citas-view">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2>Mis Citas</h2>
        <p class="subtitle">Gestiona tus citas y completa los pagos pendientes</p>
      </div>
    </div>

    <!-- Tabs -->
    <div class="tabs">
      <button :class="{ active: tab === 'pendientes' }" @click="cambiarTab('pendientes')">
        Pendientes de pago
      </button>
      <button :class="{ active: tab === 'todas' }" @click="cambiarTab('todas')">
        Todas
      </button>
    </div>

    <!-- Loading -->
    <div v-if="cargando" class="loading-state">
      <Loader2 :size="32" class="spin" />
      <p>Cargando citas...</p>
    </div>

    <!-- Empty states -->
    <div v-else-if="citas.length === 0" class="empty-state">
      <CheckCircle2 v-if="tab === 'pendientes'" :size="48" />
      <CalendarPlus v-else :size="48" />
      <h3>{{ tab === 'pendientes' ? 'No tienes citas pendientes de pago' : 'Aún no tienes citas agendadas' }}</h3>
      <p v-if="tab === 'pendientes'">Todas tus citas están al día. Puedes agendar una nueva consulta cuando lo necesites.</p>
      <p v-else>Agenda una consulta para tu mascota seleccionando servicio, veterinario y horario.</p>
      <button v-if="tab === 'todas'" class="btn-primary" @click="router.push('/cliente/solicitar-cita')">
        <CalendarPlus :size="18" />
        Solicitar Cita
      </button>
      <button v-else class="btn-secondary" @click="cambiarTab('todas')">
        Ver todas mis citas
      </button>
    </div>

    <!-- Lista de citas -->
    <div v-else class="citas-lista">
      <div v-for="cita in citas" :key="cita.idCita" class="cita-card">
        <div class="cita-avatar" :style="{ backgroundColor: getAvatarColor(cita.mascota) }">
          {{ cita.mascota[0].toUpperCase() }}
        </div>

        <div class="cita-info">
          <p class="cita-meta-top">
            <span class="badge" :style="{ backgroundColor: cita.colorUi }">
              {{ cita.estado.replaceAll('_', ' ') }}
            </span>
            <span class="cita-fecha">
              <Calendar :size="14" />
              {{ formatearFecha(cita.fechaCita) }} · {{ formatearHora(cita.horaInicio) }}
            </span>
          </p>
          <h4>{{ cita.servicio }}</h4>
          <p class="cita-detail"><PawPrint :size="14" /> {{ cita.mascota }}</p>
          <p class="cita-detail"><Stethoscope :size="14" /> {{ cita.veterinario }}</p>
          <p class="cita-detail"><Clock :size="14" /> {{ formatearHora(cita.horaInicio) }} – {{ formatearHora(cita.horaFin) }}</p>
        </div>

        <div class="cita-lateral">
          <strong class="cita-monto">{{ fmtUsd(cita.costoUsd) }}</strong>
          <span class="cita-bs">{{ fmtBs(cita.costoBs) }}</span>
          <div class="acciones">
            <button
              v-if="ESTADOS_CANCELABLES.includes(cita.estado)"
              class="btn-secondary"
              :disabled="cancelando === cita.idCita"
              @click="abrirConfirmacionCancelacion(cita)"
            >
              Cancelar
            </button>
            <button
              v-if="cita.estado === ESTADO_PENDIENTE"
              class="btn-primary"
              @click="abrirModalPago(cita)"
            >
              <CreditCard :size="16" />
              Pagar
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de pago -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="mostrarModal" class="modal-overlay" @click.self="cerrarModalPago">
          <Transition name="slide-up">
            <div v-if="mostrarModal" class="modal-container">

              <!-- Formulario de pago -->
              <template v-if="!pagoExitoso">
                <div class="modal-header">
                  <h3>Pagar Cita</h3>
                  <button class="btn-close" @click="cerrarModalPago">
                    <X :size="20" />
                  </button>
                </div>

                <div class="modal-body">
                  <!-- Resumen de la cita -->
                  <div v-if="citaActual" class="detalle-resumen">
                    <div class="detalle-fila">
                      <span class="detalle-label"><ReceiptText :size="14" /> Servicio</span>
                      <span class="detalle-valor">{{ citaActual.servicio }}</span>
                    </div>
                    <div class="detalle-fila">
                      <span class="detalle-label"><PawPrint :size="14" /> Mascota</span>
                      <span class="detalle-valor">{{ citaActual.mascota }}</span>
                    </div>
                    <div class="detalle-fila">
                      <span class="detalle-label"><Stethoscope :size="14" /> Veterinario</span>
                      <span class="detalle-valor">{{ citaActual.veterinario }}</span>
                    </div>
                    <div class="detalle-fila">
                      <span class="detalle-label"><Calendar :size="14" /> Fecha</span>
                      <span class="detalle-valor">
                        {{ formatearFecha(citaActual.fechaCita) }} · {{ formatearHora(citaActual.horaInicio) }}
                      </span>
                    </div>
                    <div class="detalle-fila total">
                      <span class="detalle-label"><Banknote :size="14" /> Total a pagar</span>
                      <span class="detalle-valor monto-total">
                        {{ fmtUsd(citaActual.costoUsd) }}
                        <small class="monto-bs">{{ fmtBs(citaActual.costoBs) }}</small>
                      </span>
                    </div>
                  </div>

                  <!-- Métodos de pago -->
                  <p class="seccion-titulo">Método de pago <span class="required">*</span></p>

                  <div v-if="cargandoMetodos" class="metodos-loading">
                    <Loader2 :size="18" class="spin" />
                    Cargando métodos de pago...
                  </div>

                  <div v-for="m in metodos" :key="m.id" class="method-option" :class="{ active: form.idMetodoPago == m.id }" @click="form.idMetodoPago = m.id; onMetodoPagoChange()">
                    <div class="method-radio">
                      <div class="radio-outer" :class="{ checked: form.idMetodoPago == m.id }">
                        <div v-if="form.idMetodoPago == m.id" class="radio-inner" />
                      </div>
                    </div>
                    <div class="method-info">
                      <span class="method-name">{{ m.nombre === 'Pago_Movil' ? 'Pago Móvil' : m.nombre }}</span>
                      <span class="method-desc">{{ m.descripcion }}</span>
                    </div>
                  </div>

                  <!-- Campos dinámicos -->
                  <div v-if="form.idMetodoPago && (camposDinamicos.length > 0 || metodoSeleccionado?.camposRequeridos?.referencia)" class="dynamic-fields">
                    <div v-if="metodoSeleccionado?.camposRequeridos?.referencia" class="campo">
                      <label class="form-label">Número de referencia <span class="required">*</span></label>
                      <input v-model="form.referenciaTransaccion" type="text" class="form-input" placeholder="Ej: 0000123456789" />
                    </div>

                    <div v-for="[campo, etiqueta] in camposDinamicos" :key="campo" class="campo">
                      <label class="form-label">{{ formatFieldLabel(campo) || etiqueta }} <span class="required">*</span></label>
                      <select v-if="campo === 'banco'" v-model="datosPago[campo]" class="form-input" required>
                        <option value="" disabled>Seleccione un banco</option>
                        <option v-for="banco in BANCOS_VENEZUELA" :key="banco.codigo" :value="banco.nombre">
                          {{ banco.codigo }} - {{ banco.nombre }}
                        </option>
                      </select>
                      <input v-else-if="campo === 'telefono'" v-model="datosPago[campo]" type="tel" class="form-input" :placeholder="formatFieldPlaceholder(campo) || etiqueta" />
                      <input v-else v-model="datosPago[campo]" type="text" class="form-input" :placeholder="formatFieldPlaceholder(campo) || etiqueta" />
                    </div>
                  </div>

                  <div v-if="errorPago" class="alert-error">
                    <AlertCircle :size="16" />
                    {{ errorPago }}
                  </div>
                </div>

                <div class="modal-footer">
                  <button class="btn-secondary" :disabled="procesando" @click="cerrarModalPago">
                    Cancelar
                  </button>
                  <button class="btn-primary" :disabled="procesando" @click="pagar">
                    <Loader2 v-if="procesando" :size="18" class="spin" />
                    <CreditCard v-else :size="18" />
                    {{ procesando ? 'Procesando...' : 'Procesar Pago' }}
                  </button>
                </div>
              </template>

              <!-- Pantalla de éxito -->
              <template v-else>
                <div class="modal-header">
                  <h3>Recibo de pago</h3>
                  <button class="btn-close" @click="finalizar">
                    <X :size="20" />
                  </button>
                </div>

                <div class="modal-body exito-body">
                  <div class="exito-icono">
                    <CheckCircle2 :size="32" />
                  </div>

                  <h4 class="exito-titulo">¡Pago procesado exitosamente!</h4>
                  <p class="exito-subtitulo">
                    {{ pagoExitoso.mensaje || 'Su cita ha sido agendada y pagada correctamente.' }}
                  </p>

                  <div class="detalle-resumen ancho-completo">
                    <div class="detalle-fila">
                      <span class="detalle-label"><ReceiptText :size="14" /> Comprobante</span>
                      <span class="detalle-valor mono">{{ pagoExitoso.numeroControl }}</span>
                    </div>
                    <div class="detalle-fila">
                      <span class="detalle-label"><Calendar :size="14" /> Fecha de la cita</span>
                      <span class="detalle-valor">
                        {{ formatearFecha(citaActual?.fechaCita) }} · {{ formatearHora(citaActual?.horaInicio) }}
                      </span>
                    </div>
                    <div class="detalle-fila">
                      <span class="detalle-label"><CheckCircle2 :size="14" /> Estado de la cita</span>
                      <span class="estado-pill" :class="badgeEstadoCita(pagoExitoso.estadoCita)">
                        {{ pagoExitoso.estadoCita?.replaceAll('_', ' ') }}
                      </span>
                    </div>
                    <div class="detalle-fila total">
                      <span class="detalle-label"><Banknote :size="14" /> Monto pagado</span>
                      <span class="detalle-valor monto-total">
                        {{ fmtUsd(citaActual?.costoUsd) }}
                        <small class="monto-bs">{{ fmtBs(citaActual?.costoBs) }}</small>
                      </span>
                    </div>
                  </div>

                  <div v-if="pagoExitoso.emailEnviado === false" class="alert-warn">
                    <AlertTriangle :size="16" />
                    {{ pagoExitoso.advertenciaEmail ||
                      'Su pago fue procesado con éxito, pero no pudimos enviar la factura a su correo. Descárguela directamente desde aquí.' }}
                  </div>
                </div>

                <div class="modal-footer exito-footer">
                  <button
                    v-if="pagoExitoso.idFactura"
                    class="btn-primary"
                    :disabled="descargando"
                    @click="descargar"
                  >
                    <Loader2 v-if="descargando" :size="18" class="spin" />
                    <Download v-else :size="18" />
                    {{ descargando ? 'Descargando...' : 'Descargar Factura' }}
                  </button>
                  <RouterLink to="/cliente/historial-pagos" class="btn-secondary">
                    Ver historial de pagos
                  </RouterLink>
                  <button class="btn-secondary" @click="finalizar">
                    Volver al panel principal
                  </button>
                </div>
              </template>

            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <!-- Modal de confirmación de cancelación -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="mostrarModalCancelacion" class="modal-overlay" @click.self="cerrarConfirmacionCancelacion">
          <Transition name="slide-up">
            <div v-if="mostrarModalCancelacion" class="modal-container modal-confirmacion">
              <div class="modal-header">
                <h3>Cancelar Cita</h3>
                <button class="btn-close" @click="cerrarConfirmacionCancelacion">
                  <X :size="20" />
                </button>
              </div>

              <div class="modal-body confirmacion-body">
                <div class="confirm-icono">
                  <AlertTriangle :size="28" />
                </div>
                <p>
                  ¿Estás seguro de que deseas cancelar la cita de
                  <strong>{{ citaACancelar?.mascota }}</strong> del
                  <strong>{{ formatearFecha(citaACancelar?.fechaCita) }}</strong>?
                </p>
              </div>

              <div class="modal-footer confirmacion-footer">
                <button class="btn-secondary" @click="cerrarConfirmacionCancelacion">
                  No, mantener
                </button>
                <button
                  class="btn-danger"
                  :disabled="cancelando === citaACancelar?.idCita"
                  @click="confirmarCancelacionDefinitiva"
                >
                  <Loader2 v-if="cancelando === citaACancelar?.idCita" :size="18" class="spin" />
                  {{ cancelando === citaACancelar?.idCita ? 'Cancelando...' : 'Sí, cancelar cita' }}
                </button>
              </div>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <!-- Toast -->
    <Transition name="slide-down">
      <div v-if="toast.visible" class="toast" :class="toast.type">
        <CheckCircle2 v-if="toast.type === 'success'" :size="18" />
        <AlertCircle v-else :size="18" />
        {{ toast.message }}
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.citas-view {
  max-width: 1200px;
  margin: 0 auto;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 24px;
}

/* Header */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1E293B;
  margin: 0;
}

.subtitle {
  font-size: 14px;
  color: #64748B;
  margin: 4px 0 0 0;
}

/* Tabs */
.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}

.tabs button {
  padding: 8px 20px;
  border: 1px solid #E2E8F0;
  background: #ffffff;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  transition: all 0.2s ease;
  font-family: inherit;
}

.tabs button:hover {
  background: #F8FAFC;
  border-color: #CBD5E1;
}

.tabs button.active {
  background: #0F766E;
  color: #ffffff;
  border-color: #0F766E;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.25);
}

/* Loading & Empty */
.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
  color: #94A3B8;
  gap: 16px;
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03);
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.empty-state p {
  font-size: 14px;
  color: #64748B;
  margin: 0 0 8px 0;
  text-align: center;
  max-width: 400px;
}

.spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Lista de citas */
.citas-lista {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cita-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.03), 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  transition: all 0.2s;
}

.cita-card:hover {
  border-color: #0F766E;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05), 0 20px 25px -5px rgba(0, 0, 0, 0.05);
  transform: translateY(-2px);
}

.cita-avatar {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 20px;
  flex-shrink: 0;
}

.cita-info {
  flex: 1;
  min-width: 0;
}

.cita-meta-top {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin: 0;
}

.cita-fecha {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748B;
  font-weight: 500;
}

.cita-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  margin: 8px 0 6px 0;
}

.cita-detail {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748B;
  margin: 4px 0 0 0;
}

.cita-lateral {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.cita-monto {
  font-size: 16px;
  font-weight: 700;
  color: #1E293B;
}

.cita-bs {
  font-size: 13px;
  color: #64748B;
}

.acciones {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

/* Badge de estado (color dinámico desde BD) */
.badge {
  color: #ffffff;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
}

/* Botones */
.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  background: #0F766E;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
  text-decoration: none;
}

.btn-primary:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.25);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  background: #F1F5F9;
  color: #475569;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
  text-decoration: none;
}

.btn-secondary:hover:not(:disabled) {
  background: #E2E8F0;
}

.btn-secondary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-danger {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  background: #EF4444;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.btn-danger:hover:not(:disabled) {
  background: #DC2626;
  transform: translateY(-1px);
}

.btn-danger:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* Alertas */
.alert-error {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  background: #FEF2F2;
  color: #DC2626;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #FECACA;
  margin-top: 16px;
  line-height: 1.5;
}

.alert-warn {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  background: #FFFBEB;
  color: #92400E;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #FDE68A;
  margin-top: 16px;
  line-height: 1.5;
  text-align: left;
}

/* ─── MODAL ─── */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 100;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.modal-container {
  background: #ffffff;
  border-radius: 16px;
  width: 100%;
  max-width: 520px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  font-family: inherit;
}

.modal-container::-webkit-scrollbar {
  width: 8px;
}

.modal-container::-webkit-scrollbar-track {
  background: transparent;
}

.modal-container::-webkit-scrollbar-thumb {
  background: #CBD5E1;
  border-radius: 4px;
}

.modal-container::-webkit-scrollbar-thumb:hover {
  background: #94A3B8;
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.btn-close {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: #F1F5F9;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #E2E8F0;
  color: #1E293B;
}

.modal-body {
  padding: 24px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding: 20px 24px 24px;
  border-top: 1px solid #E2E8F0;
}

/* Resumen / detalle */
.detalle-resumen {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 8px 16px;
  margin-bottom: 20px;
}

.detalle-resumen.ancho-completo {
  width: 100%;
  box-sizing: border-box;
}

.detalle-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 11px 0;
}

.detalle-fila + .detalle-fila {
  border-top: 1px solid #E2E8F0;
}

.detalle-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
  color: #64748B;
}

.detalle-valor {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  text-align: right;
  word-break: break-word;
}

.detalle-valor.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-weight: 500;
}

.detalle-fila.total {
  border-top: 2px solid #E2E8F0;
}

.detalle-valor.monto-total {
  font-size: 16px;
  font-weight: 700;
  color: #0F766E;
}

.monto-bs {
  display: block;
  font-size: 12px;
  color: #64748B;
  font-weight: 500;
}

/* Secciones del formulario */
.seccion-titulo {
  font-size: 13px;
  font-weight: 600;
  color: #64748B;
  margin: 0 0 12px 0;
}

.metodos-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #94A3B8;
  padding: 12px 0;
}

/* Métodos de pago como tarjetas seleccionables */
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
  font-family: inherit;
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
  font-family: inherit;
}

.method-desc {
  font-size: 12px;
  color: #64748B;
  font-family: inherit;
}

/* Campos dinámicos */
.dynamic-fields {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #E2E8F0;
}

.campo {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #64748B;
  font-family: inherit;
}

.required {
  color: #EF4444;
}

.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 400;
  color: #1E293B;
  background: #ffffff;
  transition: all 0.2s;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  box-sizing: border-box;
  line-height: 1.5;
}

.form-input::placeholder {
  color: #94A3B8;
  font-family: inherit;
}

.form-input:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.08);
}

select.form-input {
  appearance: none;
  -webkit-appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 12px center;
  padding-right: 36px;
  cursor: pointer;
}

select.form-input:invalid {
  color: #64748B;
}

/* Pantalla de éxito */
.exito-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.exito-body .detalle-resumen {
  margin-bottom: 0; /* el gap lo controla alert-warn / footer */
}

.exito-icono {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #ECFDF5;
  border: 1px solid #A7F3D0;
  color: #059669;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  animation: successPop 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

.exito-titulo {
  font-size: 18px;
  font-weight: 700;
  color: #1E293B;
  margin: 0 0 6px 0;
}

.exito-subtitulo {
  font-size: 14px;
  color: #64748B;
  margin: 0 0 20px 0;
  max-width: 340px;
  line-height: 1.5;
}

.estado-pill {
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
  white-space: nowrap;
  border: 1px solid transparent;
}

.estado-pill.verificado { background: #ECFDF5; color: #059669; border-color: #A7F3D0; }
.estado-pill.pendiente  { background: #FFFBEB; color: #D97706; border-color: #FDE68A; }
.estado-pill.rechazado  { background: #FEF2F2; color: #DC2626; border-color: #FECACA; }
.estado-pill.otro       { background: #F1F5F9; color: #64748B; border-color: #E2E8F0; }

.modal-footer.exito-footer {
  flex-direction: column;
  gap: 10px;
}

.modal-footer.exito-footer > * {
  width: 100%;
}

.exito-acciones > * {
  width: 100%;
}

/* Modal de confirmación */
.modal-confirmacion {
  max-width: 420px;
}

.confirmacion-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.confirm-icono {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #FFFBEB;
  border: 1px solid #FDE68A;
  color: #D97706;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.confirmacion-body p {
  font-size: 14px;
  color: #475569;
  margin: 0;
  line-height: 1.6;
}

.confirmacion-body strong {
  color: #1E293B;
  font-weight: 600;
}

/* Toast */
.toast {
  position: fixed;
  top: 24px;
  right: 24px;
  padding: 14px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
  z-index: 200;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.toast.success {
  background: #ECFDF5;
  color: #059669;
  border: 1px solid #A7F3D0;
}

.toast.error {
  background: #FEF2F2;
  color: #DC2626;
  border: 1px solid #FECACA;
}

/* Transiciones */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

/* Responsive */
@media (max-width: 640px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .cita-card {
    flex-direction: column;
  }

  .cita-lateral {
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    border-top: 1px solid #F1F5F9;
    padding-top: 12px;
  }

  .modal-footer {
    flex-direction: column-reverse;
  }

  .modal-footer .btn-primary,
  .modal-footer .btn-secondary,
  .modal-footer .btn-danger {
    width: 100%;
  }
}
</style>