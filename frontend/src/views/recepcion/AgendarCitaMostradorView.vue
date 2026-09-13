<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Search, X, UserPlus, PawPrint, Stethoscope, CalendarDays, Clock,
  CreditCard, Printer, Mail, CheckCircle2, AlertTriangle, ArrowLeft, ArrowRight, Loader2
} from 'lucide-vue-next'
import * as clientesApi from '@/api/clientes.api'
import { getServicios, getVeterinarios, getDisponibilidad, agendarMostrador } from '@/api/citas.api'
import { getMetodosPresenciales, enviarFactura, descargarFactura } from '@/api/pagos.api'
import { getMascotasPorCliente } from '@/api/mascotas.api'

const router = useRouter()
const route = useRoute()

const BANCOS_VENEZUELA = [
  { codigo: '0102', nombre: 'Banco de Venezuela, S.A.C.A.' }, { codigo: '0104', nombre: 'Venezolano de Crédito, S.A.' },
  { codigo: '0105', nombre: 'Mercantil Banco, C.A.' }, { codigo: '0108', nombre: 'Banco Provincial, S.A.' },
  { codigo: '0114', nombre: 'Banco del Caribe, C.A.' }, { codigo: '0134', nombre: 'Banesco Banco Universal, C.A.' },
  { codigo: '0151', nombre: 'Banco Fondo Común, C.A.' }, { codigo: '0163', nombre: 'Banco del Tesoro, C.A.' },
  { codigo: '0177', nombre: 'BANFANB' }, { codigo: '0190', nombre: 'Banco Nacional de Crédito, C.A.' }
]
const METODO_LABEL = { Efectivo: 'Efectivo', Tarjeta: 'Tarjeta (Punto de Venta)', Pago_Movil: 'Pago Móvil' }

const steps = [
  { num: 1, label: 'Cliente y Mascota' },
  { num: 2, label: 'Horario' },
  { num: 3, label: 'Cobro' },
  { num: 4, label: 'Comprobante' }
]
const currentStep = ref(1)

// ── STEP 1 ──
const clienteFiltro = ref('')
const resultadosClientes = ref([])
const buscandoCliente = ref(false)
const clienteSeleccionado = ref(null)
const mascotas = ref([])
const cargandoMascotas = ref(false)
const selectedMascota = ref(null)
const motivoConsulta = ref('')
const step1Errors = ref({})
let debounce = null

// ── STEP 2 ──
const veterinarios = ref([])
const selectedVeterinario = ref(null)
const servicios = ref([])
const selectedServicio = ref(null)
const fecha = ref('')
const hoy = new Date().toISOString().split('T')[0]
const bloques = ref([])
const cargandoBloques = ref(false)
const selectedBloque = ref(null)
const bloqueConflicto = ref(null) // flujo alterno 2: bloque en rojo
const step2Errors = ref({})

// ── STEP 3 ──
const metodos = ref([])
const selectedMetodo = ref(null)
const datosPago = ref({})
const referencia = ref('')
const fondosConfirmados = ref(false)
const step3Errors = ref({})
const procesando = ref(false)
const pagoError = ref('')

// ── STEP 4 ──
const resultado = ref(null)
const envio = ref(null)
const enviando = ref(false)
const imprimiendo = ref(false)

const servicioSeleccionado = computed(() => servicios.value.find(s => s.id === selectedServodoValue.value) || null)
const selectedServodoValue = computed(() => selectedServicio.value)
const metodoSeleccionado = computed(() => metodos.value.find(m => m.id === selectedMetodo.value) || null)
const camposDinamicos = computed(() => {
  if (!metodoSeleccionado.value?.camposRequeridos) return []
  return Object.entries(metodoSeleccionado.value.camposRequeridos)
    .filter(([key]) => key !== 'referencia')
    .map(([key]) => ({ key }))
})

// ── Búsqueda de cliente (CU paso 3: búsqueda por documento o nombre) ──
watch(clienteFiltro, (val) => {
  if (clienteSeleccionado.value) return
  clearTimeout(debounce)
  if (!val || val.trim().length < 2) { resultadosClientes.value = []; return }
  debounce = setTimeout(buscarClientes, 350)
})

async function buscarClientes() {
  buscandoCliente.value = true
  try {
    const { data } = await clientesApi.getAll(clienteFiltro.value.trim())
    resultadosClientes.value = data
  } catch { resultadosClientes.value = [] }
  finally { buscandoCliente.value = false }
}

function seleccionarCliente(c) {
  clienteSeleccionado.value = { id: c.id, nombreCompleto: c.nombreCompleto, documentoIdentidad: c.documentoIdentidad }
  clienteFiltro.value = ''
  resultadosClientes.value = []
  selectedMascota.value = null
  step1Errors.value = {}
  cargarMascotas()
}

function cambiarCliente() {
  clienteSeleccionado.value = null
  mascotas.value = []
  selectedMascota.value = null
}

async function cargarMascotas() {
  cargandoMascotas.value = true
  try {
    const { data } = await getMascotasPorCliente(clienteSeleccionado.value.id)
    mascotas.value = data
  } catch { mascotas.value = [] }
  finally { cargandoMascotas.value = false }
}

// ── STEP 2: vet → servicios por especialidad → fecha → bloques ──
async function onVeterinarioChange() {
  selectedServicio.value = null
  servicios.value = []
  bloques.value = []
  selectedBloque.value = null
  const vet = veterinarios.value.find(v => v.id === selectedVeterinario.value)
  if (!vet?.especialidad) return
  try {
    const { data } = await getServicios({ especialidad: vet.especialidad })
    servicios.value = data
  } catch { /* silencioso */ }
}

watch(fecha, () => { if (fecha.value && selectedVeterinario.value && selectedServicio.value) cargarBloques() })
watch(selectedServicio, () => { bloques.value = []; selectedBloque.value = null })

async function cargarBloques() {
  cargandoBloques.value = true
  bloqueConflicto.value = null
  try {
    const { data } = await getDisponibilidad({
      id_veterinario: selectedVeterinario.value, fecha: fecha.value, id_servicio: selectedServicio.value
    })
    bloques.value = data.bloquesDisponibles || []
  } catch { bloques.value = [] }
  finally { cargandoBloques.value = false }
}

// ── Validaciones por paso (alternos 3 y 4) ──
function validarStep1() {
  const errors = {}
  if (!clienteSeleccionado.value) errors.cliente = 'Seleccione un cliente'
  if (!selectedMascota.value) errors.mascota = 'Seleccione una mascota'
  if (!motivoConsulta.value.trim()) errors.motivo = 'Escriba el motivo de la consulta'
  step1Errors.value = errors
  return Object.keys(errors).length === 0
}

function validarStep2() {
  const errors = {}
  if (!selectedVeterinario.value) errors.veterinario = 'Seleccione un veterinario'
  if (!selectedServicio.value) errors.servicio = 'Seleccione un servicio'
  if (!fecha.value) errors.fecha = 'Seleccione una fecha'
  if (!selectedBloque.value) errors.bloque = 'Seleccione un bloque de horario'
  step2Errors.value = errors
  return Object.keys(errors).length === 0
}

// ── STEP 3: "Confirmar y Facturar" (CU pasos 5-7, transacción atómica) ──
async function confirmarYFacturar() {
  const campos = metodoSeleccionado.value?.camposRequeridos || {}
  const errors = {}
  if (!selectedMetodo.value) errors.metodo = 'Seleccione un método de pago'
  if ('referencia' in campos && !referencia.value.trim()) errors.referencia = 'La referencia es obligatoria'
  for (const { key } of camposDinamicos.value) {
    if (!datosPago.value[key]?.trim()) errors[key] = 'Este campo es obligatorio'
  }
  if (!fondosConfirmados.value) errors.fondos = 'Debe confirmar la recepción de los fondos'
  step3Errors.value = errors
  pagoError.value = ''
  if (Object.keys(errors).length > 0) {
    // Flujo alterno 3 (fallo de confirmación de pago)
    pagoError.value = 'No se pudo confirmar el pago. Verifique la transacción e intente nuevamente.'
    return
  }

  procesando.value = true
  try {
    const datosPagoCompletos = { ...datosPago.value }
    if ('referencia' in campos) datosPagoCompletos.referencia = referencia.value.trim()

    const { data } = await agendarMostrador({
      idCliente: clienteSeleccionado.value.id,
      idMascota: selectedMascota.value,
      idVeterinario: selectedVeterinario.value,
      idServicio: selectedServicio.value,
      fechaCita: fecha.value,
      horaInicio: selectedBloque.value.horaInicio,
      motivoConsulta: motivoConsulta.value.trim(),
      idMetodoPago: selectedMetodo.value,
      referenciaTransaccion: referencia.value.trim() || undefined,
      datosPago: datosPagoCompletos
    })
    resultado.value = data
    envio.value = null
    currentStep.value = 4
  } catch (err) {
    const msg = err.response?.data?.message || ''
    if (err.response?.status === 409 && msg.includes('ya no se encuentra disponible')) {
      // Flujo alterno 2: choque de agendas → bloque en rojo + volver al paso de horario
      bloqueConflicto.value = selectedBloque.value?.horaInicio
      selectedBloque.value = null
      currentStep.value = 2
      step2Errors.value = { bloque: msg }
      await cargarBloques()
    } else if (err.response?.status === 403) {
      pagoError.value = 'La mascota no está asociada al cliente seleccionado. Verifique los datos.'
      currentStep.value = 1
    } else {
      pagoError.value = msg || 'No se pudo confirmar el pago. Verifique la transacción e intente nuevamente.'
    }
  } finally {
    procesando.value = false
  }
}

// ── STEP 4: comprobante (CU paso 8) ──
async function imprimirComprobante() {
  imprimiendo.value = true
  try {
    const res = await descargarFactura(resultado.value.idFactura)
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
    window.open(url, '_blank')
    setTimeout(() => window.URL.revokeObjectURL(url), 60000)
  } catch { /* el PDF puede reintentarse */ }
  finally { imprimiendo.value = false }
}

async function enviarPorCorreo() {
  enviando.value = true
  try {
    const { data } = await enviarFactura(resultado.value.idFactura)
    envio.value = data
  } catch {
    envio.value = { enviado: false, mensaje: 'No se pudo enviar el correo. Intente nuevamente.' }
  } finally { enviando.value = false }
}

function volverAlCalendario() {
  router.push('/recepcion/citas')
}
function cancelar() {
  router.push('/recepcion/citas')
}

// ── Formato ──
function fmtHora(t) {
  if (!t) return ''
  const [h, m] = t.split(':')
  const hh = Number(h) % 12 || 12
  return `${hh}:${m} ${Number(h) >= 12 ? 'PM' : 'AM'}`
}
function fmtFecha(iso) {
  if (!iso) return ''
  const [y, m, d] = iso.split('-')
  const MESES = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']
  return `${d} ${MESES[Number(m) - 1]} ${y}`
}
function fmtFieldLabel(key) {
  const labels = { banco: 'Banco emisor', telefono: 'Teléfono asociado', lote: 'Número de lote', ultimos_digitos: 'Últimos 4 dígitos de la tarjeta' }
  return labels[key] || key.replace(/_/g, ' ')
}

// ── Init: carga catálogos + preselección de cliente por query param ──
onMounted(async () => {
  const { data } = await getVeterinarios()
  veterinarios.value = data
  const { data: m } = await getMetodosPresenciales()
  metodos.value = m

  // Integración con el flujo Registrar Mascota → "Agendar cita"
  if (route.query.clienteDocumento) {
    clienteFiltro.value = route.query.clienteDocumento
    await buscarClientes()
    const exacto = resultadosClientes.value.find(c => c.documentoIdentidad === route.query.clienteDocumento)
    if (exacto) seleccionarCliente(exacto)
    clienteFiltro.value = ''
  }
})
</script>

<template>
  <div class="page">
    <button class="back-btn" @click="volverAlCalendario"><ArrowLeft :size="16" /> Volver al calendario</button>

    <!-- Stepper -->
    <div class="stepper">
      <div v-for="(s, i) in steps" :key="s.num" class="step" :class="{ active: currentStep === s.num, done: currentStep > s.num }">
        <div class="circle"><CheckCircle2 v-if="currentStep > s.num" :size="16" /><span v-else>{{ s.num }}</span></div>
        <span class="label">{{ s.label }}</span>
        <div v-if="i < steps.length - 1" class="line" />
      </div>
    </div>

    <!-- ══ STEP 1: Cliente + Mascota + Motivo ══ -->
    <div v-if="currentStep === 1" class="card">
      <div class="card-header"><h3><PawPrint :size="18" /> Datos de la Reserva</h3></div>
      <div class="card-body">
        <!-- Cliente: búsqueda (CU paso 3) -->
        <div v-if="!clienteSeleccionado" class="form-group">
          <label>Cliente <span class="req">*</span> <span class="hint-inline">búsqueda por nombre o documento</span></label>
          <div class="search-box">
            <Search :size="16" />
            <input v-model="clienteFiltro" type="text" placeholder="Ej: María González o V-12345678..." />
            <Loader2 v-if="buscandoCliente" :size="16" class="spin" />
          </div>
          <div v-if="clienteFiltro.length >= 2 && !buscandoCliente && resultadosClientes.length === 0" class="no-results">
            <AlertTriangle :size="14" />
            No se encontraron resultados. Verifique los datos o
            <router-link to="/recepcion/clientes/nuevo">registre previamente al cliente</router-link>.
          </div>
          <div v-if="resultadosClientes.length > 0" class="results">
            <button v-for="c in resultadosClientes" :key="c.id" type="button" class="result-row" @click="seleccionarCliente(c)">
              <strong>{{ c.nombreCompleto }}</strong>
              <span>{{ c.documentoIdentidad }} · {{ c.telefonoPrincipal }}</span>
            </button>
          </div>
        </div>
        <!-- Cliente seleccionado -->
        <div v-else class="cliente-chip">
          <div>
            <strong>{{ clienteSeleccionado.nombreCompleto }}</strong>
            <span>{{ clienteSeleccionado.documentoIdentidad }}</span>
          </div>
          <button type="button" class="chip-btn" @click="cambiarCliente"><X :size="14" /> Cambiar</button>
        </div>
        <div v-if="step1Errors.cliente" class="ferr">{{ step1Errors.cliente }}</div>

        <!-- Mascota -->
        <div v-if="clienteSeleccionado" class="form-group">
          <label>Mascota <span class="req">*</span></label>
          <div v-if="cargandoMascotas" class="mini-load"><Loader2 :size="14" class="spin" /> Cargando mascotas...</div>
          <template v-else-if="mascotas.length > 0">
            <select v-model="selectedMascota" :class="{ invalid: step1Errors.mascota }">
              <option :value="null" disabled>Seleccione una mascota</option>
              <option v-for="m in mascotas" :key="m.idMascota" :value="m.idMascota">
                {{ m.nombre }} — {{ m.nombreRaza || m.nombreEspecie }}
              </option>
            </select>
            <div v-if="step1Errors.mascota" class="ferr">{{ step1Errors.mascota }}</div>
          </template>
          <div v-else class="no-results">
            <AlertTriangle :size="14" />
            No se encontraron mascotas para este cliente.
            <router-link :to="{ path: '/recepcion/registrar-mascota', query: { clienteDocumento: clienteSeleccionado.documentoIdentidad, clienteNombre: clienteSeleccionado.nombreCompleto } }">
              Registre una mascota</router-link> y regrese.
          </div>
        </div>

        <!-- Motivo -->
        <div class="form-group">
          <label>Motivo de la consulta <span class="req">*</span></label>
          <textarea v-model="motivoConsulta" rows="3" maxlength="1000" placeholder="Motivo indicado por el cliente..." :class="{ invalid: step1Errors.motivo }" />
          <div v-if="step1Errors.motivo" class="ferr">{{ step1Errors.motivo }}</div>
        </div>
      </div>
    </div>

    <!-- ══ STEP 2: Horario ══ -->
    <div v-if="currentStep === 2" class="card">
      <div class="card-header"><h3><CalendarDays :size="18" /> Horario</h3></div>
      <div class="card-body">
        <div class="grid2">
          <div class="form-group">
            <label>Veterinario <span class="req">*</span></label>
            <select v-model="selectedVeterinario" :class="{ invalid: step2Errors.veterinario }" @change="onVeterinarioChange">
              <option :value="null" disabled>Seleccione un veterinario</option>
              <option v-for="v in veterinarios" :key="v.id" :value="v.id">{{ v.nombre }} · {{ v.especialidad }}</option>
            </select>
            <div v-if="step2Errors.veterinario" class="ferr">{{ step2Errors.veterinario }}</div>
          </div>
          <div class="form-group">
            <label>Servicio <span class="req">*</span></label>
            <select v-model="selectedServicio" :disabled="!selectedVeterinario" :class="{ invalid: step2Errors.servicio }">
              <option :value="null" disabled>{{ selectedVeterinario ? 'Seleccione un servicio' : 'Seleccione veterinario primero' }}</option>
              <option v-for="s in servicios" :key="s.id" :value="s.id">{{ s.nombre }} — ${{ Number(s.precioUsd).toFixed(2) }} ({{ s.duracionMinutos }} min)</option>
            </select>
            <div v-if="step2Errors.servicio" class="ferr">{{ step2Errors.servicio }}</div>
          </div>
          <div class="form-group">
            <label>Fecha <span class="req">*</span></label>
            <input v-model="fecha" type="date" :min="hoy" :class="{ invalid: step2Errors.fecha }" />
            <div v-if="step2Errors.fecha" class="ferr">{{ step2Errors.fecha }}</div>
          </div>
        </div>

        <div v-if="step2Errors.bloque" class="alert warn"><AlertTriangle :size="16" /> {{ step2Errors.bloque }}</div>

        <div v-if="fecha && selectedServicio && selectedVeterinario" class="slots">
          <h4><Clock :size="14" /> Bloques disponibles — {{ fmtFecha(fecha) }}</h4>
          <div v-if="cargandoBloques" class="mini-load"><Loader2 :size="14" class="spin" /> Consultando disponibilidad...</div>
          <div v-else-if="bloques.length === 0" class="no-results">No hay bloques libres para esta fecha/servicio.</div>
          <div v-else class="slots-grid">
            <button v-for="b in bloques" :key="b.horaInicio" type="button" class="slot"
              :class="{ selected: selectedBloque?.horaInicio === b.horaInicio, conflicto: bloqueConflicto === b.horaInicio }"
              @click="selectedBloque = b; step2Errors.bloque = null">
              {{ fmtHora(b.horaInicio) }} – {{ fmtHora(b.horaFin) }}
            </button>
          </div>
          <p class="hint">El monto en bolívares se calcula con la tasa oficial al confirmar.</p>
        </div>
      </div>
    </div>

    <!-- ══ STEP 3: Cobro ══ -->
    <div v-if="currentStep === 3" class="card">
      <div class="card-header"><h3><CreditCard :size="18" /> Cobro en Mostrador</h3></div>
      <div class="card-body">
        <div v-if="pagoError" class="alert error"><AlertTriangle :size="16" /> {{ pagoError }}</div>

        <div class="resumen-mini">
          <div><span>Cliente</span><strong>{{ clienteSeleccionado?.nombreCompleto }}</strong></div>
          <div><span>Mascota</span><strong>{{ mascotas.find(m => m.idMascota === selectedMascota)?.nombre }}</strong></div>
          <div><span>Servicio</span><strong>{{ servicios.find(s => s.id === selectedServicio)?.nombre }}</strong></div>
          <div><span>Turno</span><strong>{{ fmtFecha(fecha) }} · {{ fmtHora(selectedBloque?.horaInicio) }}</strong></div>
          <div><span>Monto</span><strong>${{ Number(servicios.find(s => s.id === selectedServicio)?.precioUsd).toFixed(2) }} USD</strong></div>
        </div>

        <label class="section-label">Método de pago presencial <span class="req">*</span></label>
        <div v-for="m in metodos" :key="m.id" class="method" :class="{ active: selectedMetodo === m.id }"
          @click="selectedMetodo = m.id; datosPago = {}; referencia = ''; step3Errors = {}; pagoError = ''">
          <div class="radio" :class="{ checked: selectedMetodo === m.id }"><div v-if="selectedMetodo === m.id" class="dot" /></div>
          <div><span class="m-name">{{ METODO_LABEL[m.nombre] || m.nombre }}</span><span class="m-desc">{{ m.descripcion }}</span></div>
        </div>
        <div v-if="step3Errors.metodo" class="ferr">{{ step3Errors.metodo }}</div>

        <template v-if="metodoSeleccionado">
          <div v-if="'referencia' in (metodoSeleccionado.camposRequeridos || {})" class="form-group">
            <label>Número de referencia <span class="req">*</span></label>
            <input v-model="referencia" type="text" placeholder="Ej: 0000123456789" :class="{ invalid: step3Errors.referencia }" />
            <div v-if="step3Errors.referencia" class="ferr">{{ step3Errors.referencia }}</div>
          </div>
          <div v-for="campo in camposDinamicos" :key="campo.key" class="form-group">
            <label>{{ fmtFieldLabel(campo.key) }} <span class="req">*</span></label>
            <select v-if="campo.key === 'banco'" v-model="datosPago[campo.key]" :class="{ invalid: step3Errors[campo.key] }">
              <option value="" disabled>Seleccione el banco</option>
              <option v-for="b in BANCOS_VENEZUELA" :key="b.codigo" :value="b.nombre">{{ b.codigo }} - {{ b.nombre }}</option>
            </select>
            <input v-else v-model="datosPago[campo.key]" type="text" :class="{ invalid: step3Errors[campo.key] }" />
            <div v-if="step3Errors[campo.key]" class="ferr">{{ step3Errors[campo.key] }}</div>
          </div>
        </template>

        <label class="check-row" :class="{ invalid: step3Errors.fondos }">
          <input v-model="fondosConfirmados" type="checkbox" />
          Confirmo la recepción de los fondos del cliente
        </label>
        <div v-if="step3Errors.fondos" class="ferr">{{ step3Errors.fondos }}</div>

        <button class="btn-pay" :disabled="procesando" @click="confirmarYFacturar">
          <Loader2 v-if="procesando" :size="18" class="spin" />
          <CreditCard v-else :size="18" />
          {{ procesando ? 'Procesando...' : 'Confirmar y Facturar' }}
        </button>
      </div>
    </div>

    <!-- ══ STEP 4: Comprobante (CU paso 8) ══ -->
    <div v-if="currentStep === 4 && resultado" class="card success">
      <div class="card-body">
        <div class="success-icon"><CheckCircle2 :size="48" /></div>
        <h2>Cita agendada y facturada correctamente</h2>
        <div class="factura-box">
          <div class="row"><span>Factura</span><strong class="mono">{{ resultado.numeroControl }}</strong></div>
          <div class="row"><span>Cliente</span><strong>{{ resultado.resumen.cliente }} ({{ resultado.resumen.documentoCliente }})</strong></div>
          <div class="row"><span>Mascota</span><strong>{{ resultado.resumen.mascota }}</strong></div>
          <div class="row"><span>Veterinario</span><strong>{{ resultado.resumen.veterinario }}</strong></div>
          <div class="row"><span>Servicio</span><strong>{{ resultado.resumen.servicio }}</strong></div>
          <div class="row"><span>Turno</span><strong>{{ fmtFecha(resultado.resumen.fecha) }} · {{ fmtHora(resultado.resumen.horaInicio) }} – {{ fmtHora(resultado.resumen.horaFin) }}</strong></div>
          <div class="row"><span>Método de pago</span><strong>{{ METODO_LABEL[resultado.resumen.metodoPago] || resultado.resumen.metodoPago }}</strong></div>
          <div class="row total"><span>Total facturado</span><strong>${{ Number(resultado.resumen.costoUsd).toFixed(2) }} USD · Bs. {{ Number(resultado.resumen.costoBs).toFixed(2) }}</strong></div>
        </div>
        <div v-if="envio" class="alert" :class="envio.enviado ? 'ok' : 'warn'">
          {{ envio.mensaje }}
        </div>
        <div class="actions">
          <button class="btn-secondary" :disabled="imprimiendo" @click="imprimirComprobante">
            <Loader2 v-if="imprimiendo" :size="16" class="spin" /><Printer v-else :size="16" /> Imprimir
          </button>
          <button class="btn-secondary" :disabled="enviando" @click="enviarPorCorreo">
            <Loader2 v-if="enviando" :size="16" class="spin" /><Mail v-else :size="16" /> Enviar por correo
          </button>
          <button class="btn-primary" @click="volverAlCalendario">
            <ArrowLeft :size="16" /> Volver al calendario
          </button>
        </div>
      </div>
    </div>

    <!-- Acciones -->
    <div v-if="currentStep < 4" class="step-actions">
      <button class="btn-secondary" @click="cancelar"><X :size="16" /> Cancelar</button>
      <div class="right">
        <button v-if="currentStep > 1" class="btn-secondary" @click="currentStep--; pagoError = ''">
          <ArrowLeft :size="16" /> Atrás
        </button>
        <button v-if="currentStep === 1" class="btn-primary" @click="validarStep1() && (currentStep = 2)">
          Siguiente <ArrowRight :size="16" />
        </button>
        <button v-if="currentStep === 2" class="btn-primary" :disabled="!selectedBloque" @click="validarStep2() && (currentStep = 3)">
          Ir al cobro <ArrowRight :size="16" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; max-width: 780px; font-family: 'Inter','Segoe UI',Roboto,sans-serif; }
.back-btn { display: inline-flex; align-items: center; gap: 6px; background: none; border: none; color: #64748B; font-size: 13px; font-weight: 600; cursor: pointer; width: fit-content; font-family: inherit; }
.back-btn:hover { color: #0F766E; }
.stepper { display: flex; align-items: center; background: #fff; border: 1px solid #E2E8F0; border-radius: 12px; padding: 18px 28px; }
.step { display: flex; align-items: center; gap: 10px; flex: 1; position: relative; }
.step:last-child { flex: 0; }
.circle { width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; background: #F1F5F9; color: #94A3B8; border: 2px solid #E2E8F0; }
.step.active .circle { background: #0F766E; color: #fff; border-color: #0F766E; box-shadow: 0 0 0 4px rgba(15,118,110,.15); }
.step.done .circle { background: #0F766E; color: #fff; border-color: #0F766E; }
.label { font-size: 12px; font-weight: 600; color: #94A3B8; white-space: nowrap; }
.step.active .label { color: #0F766E; }
.line { flex: 1; height: 2px; background: #E2E8F0; margin: 0 12px; }
.step.done .line { background: #0F766E; }
.card { background: #fff; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,.03), 0 20px 40px -4px rgba(15,118,110,.08); border-top: 4px solid #0F766E; }
.card-header { padding: 18px 24px; border-bottom: 1px solid #E2E8F0; }
.card-header h3 { font-size: 16px; font-weight: 700; color: #1E293B; display: flex; align-items: center; gap: 8px; margin: 0; }
.card-body { padding: 24px; }
.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 16px; }
.form-group label { font-size: 13px; font-weight: 600; color: #475569; }
.req { color: #EF4444; }
.hint-inline { color: #94A3B8; font-weight: 400; font-size: 11px; margin-left: 6px; }
.form-group input, .form-group select, .form-group textarea, .search-box input { width: 100%; padding: 10px 14px; border: 1.5px solid #E2E8F0; border-radius: 10px; font-size: 14px; color: #1E293B; background: #F8FAFC; font-family: inherit; box-sizing: border-box; outline: none; transition: all .2s; }
.form-group input:focus, .form-group select:focus, .form-group textarea:focus, .search-box input:focus { border-color: #0F766E; background: #fff; box-shadow: 0 0 0 3px rgba(15,118,110,.1); }
.invalid { border-color: #EF4444 !important; background: #FEF2F2 !important; }
.ferr { font-size: 12px; color: #EF4444; font-weight: 500; }
.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.search-box { position: relative; display: flex; align-items: center; }
.search-box svg:first-child { position: absolute; left: 12px; color: #94A3B8; pointer-events: none; }
.search-box input { padding-left: 36px; padding-right: 36px; }
.search-box .spin { position: absolute; right: 12px; color: #0F766E; }
.results { border: 1px solid #E2E8F0; border-radius: 10px; overflow: hidden; max-height: 220px; overflow-y: auto; }
.result-row { display: flex; flex-direction: column; gap: 2px; width: 100%; text-align: left; padding: 10px 14px; background: #fff; border: none; border-bottom: 1px solid #F1F5F9; cursor: pointer; font-family: inherit; }
.result-row:hover { background: #F0FDFA; }
.result-row strong { font-size: 14px; color: #1E293B; }
.result-row span { font-size: 12px; color: #64748B; }
.no-results { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; font-size: 13px; color: #B45309; background: #FFFBEB; border: 1px solid #FDE68A; border-radius: 8px; padding: 10px 14px; }
.no-results a { color: #0F766E; font-weight: 600; }
.cliente-chip { display: flex; justify-content: space-between; align-items: center; background: #F0FDFA; border: 1px solid #99F6E4; border-radius: 10px; padding: 12px 16px; margin-bottom: 16px; }
.cliente-chip strong { display: block; color: #115E59; }
.cliente-chip span { font-size: 12px; color: #0F766E; }
.chip-btn { display: inline-flex; align-items: center; gap: 4px; background: #fff; border: 1px solid #99F6E4; color: #0F766E; border-radius: 8px; padding: 6px 12px; font-size: 12px; font-weight: 600; cursor: pointer; font-family: inherit; }
.mini-load { display: flex; align-items: center; gap: 8px; color: #64748B; font-size: 13px; padding: 8px 0; }
.alert { display: flex; align-items: center; gap: 8px; padding: 12px 16px; border-radius: 10px; font-size: 13px; font-weight: 500; margin-bottom: 16px; }
.alert.error { color: #DC2626; background: #FEF2F2; border: 1px solid #FECACA; }
.alert.warn { color: #B45309; background: #FFFBEB; border: 1px solid #FDE68A; }
.alert.ok { color: #059669; background: #ECFDF5; border: 1px solid #A7F3D0; }
.slots h4 { font-size: 13px; font-weight: 700; color: #1E293B; display: flex; align-items: center; gap: 6px; margin: 8px 0 12px; }
.slots-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 10px; }
.slot { padding: 10px; border: 1.5px solid #E2E8F0; border-radius: 10px; background: #fff; font-size: 13px; font-weight: 600; color: #1E293B; cursor: pointer; font-family: inherit; transition: all .15s; }
.slot:hover { border-color: #0F766E; background: #F0FDFA; }
.slot.selected { background: #0F766E; color: #fff; border-color: #0F766E; }
.slot.conflicto { border-color: #DC3545; background: #FEF2F2; color: #DC2626; }
.hint { font-size: 12px; color: #94A3B8; margin-top: 8px; }
.resumen-mini { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 12px; padding: 6px 16px; margin-bottom: 20px; }
.resumen-mini div { display: flex; justify-content: space-between; padding: 9px 0; border-bottom: 1px solid #F1F5F9; }
.resumen-mini div:last-child { border-bottom: none; }
.resumen-mini span { font-size: 13px; color: #64748B; }
.resumen-mini strong { font-size: 13px; color: #1E293B; }
.section-label { display: block; font-size: 13px; font-weight: 600; color: #475569; margin: 8px 0 10px; }
.method { display: flex; align-items: center; gap: 12px; border: 1.5px solid #E2E8F0; border-radius: 10px; padding: 12px 16px; cursor: pointer; margin-bottom: 10px; transition: all .15s; }
.method:hover { border-color: #99F6E4; }
.method.active { border-color: #0F766E; background: #F0FDFA; }
.radio { width: 18px; height: 18px; border-radius: 50%; border: 2px solid #CBD5E1; display: flex; align-items: center; justify-content: center; }
.radio.checked { border-color: #0F766E; }
.radio .dot { width: 10px; height: 10px; border-radius: 50%; background: #0F766E; }
.m-name { display: block; font-size: 14px; font-weight: 600; color: #1E293B; }
.m-desc { font-size: 12px; color: #64748B; }
.check-row { display: flex; align-items: center; gap: 10px; font-size: 14px; color: #475569; font-weight: 500; margin: 16px 0 8px; cursor: pointer; }
.check-row.invalid { color: #DC2626; }
.btn-pay, .btn-primary, .btn-secondary { display: inline-flex; align-items: center; justify-content: center; gap: 8px; padding: 12px 22px; border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit; transition: all .2s; }
.btn-pay { width: 100%; background: #0F766E; color: #fff; border: none; margin-top: 12px; }
.btn-pay:hover:not(:disabled) { background: #115E59; }
.btn-pay:disabled { background: #94A3B8; cursor: not-allowed; }
.btn-primary { background: #0F766E; color: #fff; border: none; }
.btn-primary:hover:not(:disabled) { background: #115E59; }
.btn-secondary { background: #fff; color: #475569; border: 1.5px solid #E2E8F0; }
.success .card-body { text-align: center; }
.success-icon { color: #10B981; margin-bottom: 12px; }
.success h2 { font-size: 20px; font-weight: 700; color: #1E293B; margin: 0 0 20px; }
.factura-box { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 12px; padding: 6px 18px; text-align: left; margin-bottom: 20px; }
.factura-box .row { display: flex; justify-content: space-between; gap: 12px; padding: 10px 0; border-bottom: 1px solid #F1F5F9; font-size: 13px; }
.factura-box .row:last-child { border-bottom: none; }
.factura-box .row span { color: #64748B; }
.factura-box .row strong { color: #1E293B; text-align: right; }
.factura-box .row.total strong { color: #0F766E; font-size: 15px; }
.mono { font-family: ui-monospace, Menlo, Consolas, monospace; }
.actions { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; }
.step-actions { display: flex; justify-content: space-between; gap: 12px; }
.step-actions .right { display: flex; gap: 12px; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
@media (max-width: 640px) { .grid2 { grid-template-columns: 1fr; } .step .label { display: none; } }
</style>