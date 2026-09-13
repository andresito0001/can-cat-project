<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPendientesPago, cobrarCitaMostrador } from '@/api/citas.api'
import { getMetodosPresenciales, enviarFactura, descargarFactura } from '@/api/pagos.api'
import { ESTADO_COLOR } from '@/utils/constants/estadosCita'
import {
  Banknote, Loader2, AlertCircle, AlertTriangle, CheckCircle2,
  Printer, Mail, X, RefreshCw, PawPrint, CalendarDays, CreditCard
} from 'lucide-vue-next'

const BANCOS_VENEZUELA = [
  { codigo: '0102', nombre: 'Banco de Venezuela, S.A.C.A.' }, { codigo: '0104', nombre: 'Venezolano de Crédito, S.A.' },
  { codigo: '0105', nombre: 'Mercantil Banco, C.A.' }, { codigo: '0108', nombre: 'Banco Provincial, S.A.' },
  { codigo: '0114', nombre: 'Banco del Caribe, C.A.' }, { codigo: '0134', nombre: 'Banesco Banco Universal, C.A.' },
  { codigo: '0151', nombre: 'Banco Fondo Común, C.A.' }, { codigo: '0163', nombre: 'Banco del Tesoro, C.A.' },
  { codigo: '0177', nombre: 'BANFANB' }, { codigo: '0190', nombre: 'Banco Nacional de Crédito, C.A.' }
]
const METODO_LABEL = { Efectivo: 'Efectivo', Tarjeta: 'Tarjeta (Punto de Venta)', Pago_Movil: 'Pago Móvil' }

// ── Lista de pendientes ──
const pendientes = ref([])
const cargando = ref(false)
const errorCarga = ref('')
const hoy = new Date().toISOString().split('T')[0]

async function cargarPendientes() {
  cargando.value = true
  errorCarga.value = ''
  try {
    const { data } = await getPendientesPago()
    pendientes.value = data
  } catch (err) {
    errorCarga.value = err.response?.data?.message || 'No se pudo cargar las citas pendientes.'
  } finally {
    cargando.value = false
  }
}

// ── Modal de cobro ──
const citaACobrar = ref(null)
const metodos = ref([])
const selectedMetodo = ref(null)
const datosPago = ref({})
const referencia = ref('')
const fondosConfirmados = ref(false)
const cobrando = ref(false)
const cobroError = ref('')
const stepErrors = ref({})

// ── Comprobante ──
const resultado = ref(null)
const envio = ref(null)
const enviando = ref(false)
const imprimiendo = ref(false)

const metodoSeleccionado = computed(() => metodos.value.find(m => m.id === selectedMetodo.value) || null)
const camposDinamicos = computed(() => {
  if (!metodoSeleccionado.value?.camposRequeridos) return []
  return Object.entries(metodoSeleccionado.value.camposRequeridos)
    .filter(([key]) => key !== 'referencia')
    .map(([key]) => ({ key }))
})

function abrirCobro(cita) {
  citaACobrar.value = cita
  selectedMetodo.value = null
  datosPago.value = {}
  referencia.value = ''
  fondosConfirmados.value = false
  cobroError.value = ''
  stepErrors.value = {}
}

async function confirmarCobro() {
  const campos = metodoSeleccionado.value?.camposRequeridos || {}
  const errors = {}
  if (!selectedMetodo.value) errors.metodo = 'Seleccione un método de pago'
  if ('referencia' in campos && !referencia.value.trim()) errors.referencia = 'La referencia es obligatoria'
  for (const { key } of camposDinamicos.value) {
    if (!datosPago.value[key]?.trim()) errors[key] = 'Este campo es obligatorio'
  }
  if (!fondosConfirmados.value) errors.fondos = 'Debe confirmar la recepción de los fondos'
  stepErrors.value = errors
  cobroError.value = ''
  if (Object.keys(errors).length > 0) {
    cobroError.value = 'No se pudo confirmar el pago. Verifique la transacción e intente nuevamente.'
    return
  }

  cobrando.value = true
  try {
    const datosPagoCompletos = { ...datosPago.value }
    if ('referencia' in campos) datosPagoCompletos.referencia = referencia.value.trim()

    const { data } = await cobrarCitaMostrador(citaACobrar.value.idCita, {
      idMetodoPago: selectedMetodo.value,
      referenciaTransaccion: referencia.value.trim() || undefined,
      datosPago: datosPagoCompletos
    })
    resultado.value = data
    citaACobrar.value = null
    envio.value = null
  } catch (err) {
    const msg = err.response?.data?.message || ''
    if (msg.includes('Pendiente_Pago') || msg.includes('Solo se pueden cobrar')) {
      // Otro recepcionista la cobró mientras tanto — cerrar y refrescar
      citaACobrar.value = null
      cobroError.value = 'Esta cita ya no está pendiente de pago. La lista fue actualizada.'
      await cargarPendientes()
    } else {
      cobroError.value = msg || 'No se pudo confirmar el pago. Verifique la transacción e intente nuevamente.'
    }
  } finally {
    cobrando.value = false
  }
}

async function imprimirComprobante() {
  imprimiendo.value = true
  try {
    const res = await descargarFactura(resultado.value.idFactura)
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
    window.open(url, '_blank')
    setTimeout(() => window.URL.revokeObjectURL(url), 60000)
  } catch { /* reintentable */ }
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

function cerrarComprobante() {
  resultado.value = null
  cargarPendientes()
}

// ── Formato ──
const MESES = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']
function fmtFecha(iso) {
  if (!iso) return ''
  const [y, m, d] = iso.split('-')
  return `${d} ${MESES[Number(m) - 1]} ${y}`
}
function fmtHora(t) {
  if (!t) return ''
  const [h, m] = t.split(':')
  const hh = Number(h) % 12 || 12
  return `${hh}:${m} ${Number(h) >= 12 ? 'PM' : 'AM'}`
}
function fmtFieldLabel(key) {
  const labels = { banco: 'Banco emisor', telefono: 'Teléfono asociado', lote: 'Número de lote', ultimos_digitos: 'Últimos 4 dígitos de la tarjeta' }
  return labels[key] || key.replace(/_/g, ' ')
}

onMounted(async () => {
  cargarPendientes()
  try { const { data } = await getMetodosPresenciales(); metodos.value = data } catch { /* vacío */ }
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Cobrar en Mostrador</h1>
        <p class="page-subtitle">Citas solicitadas online con pago pendiente. Cobre presencialmente y genere la factura al instante.</p>
      </div>
      <button class="btn-secondary" :disabled="cargando" @click="cargarPendientes">
        <RefreshCw :size="16" :class="{ spin: cargando }" /> Actualizar
      </button>
    </div>

    <div v-if="cobroError && !citaACobrar && !resultado" class="alert warn">
      <AlertTriangle :size="18" /> {{ cobroError }}
    </div>
    <div v-if="errorCarga" class="alert error"><AlertCircle :size="18" /> {{ errorCarga }}</div>

    <div v-if="cargando" class="state"><span class="spinner" /> Cargando citas pendientes...</div>

    <div v-else-if="pendientes.length === 0" class="state empty">
      <Banknote :size="48" />
      <h3>No hay citas pendientes de pago</h3>
      <p>Todas las citas solicitadas están al día. Las nuevas solicitudes online sin pagar aparecerán aquí.</p>
    </div>

    <div v-else class="pendientes-list">
      <div v-for="c in pendientes" :key="c.idCita" class="pend-card" :class="{ vencida: c.fecha < hoy }">
        <div class="pend-fecha" :class="{ vencida: c.fecha < hoy }">
          <span class="pf-dia">{{ fmtFecha(c.fecha).split(' ')[0] }}</span>
          <span class="pf-mes">{{ fmtFecha(c.fecha).split(' ')[1] }}</span>
          <span class="pf-hora">{{ fmtHora(c.horaInicio) }}</span>
        </div>
        <div class="pend-info">
          <strong><PawPrint :size="14" /> {{ c.nombreMascota }}</strong> · {{ c.nombreCliente }}
          <div class="pend-meta">
            <CalendarDays :size="12" /> {{ c.nombreServicio }} · {{ c.nombreVeterinario }}
          </div>
          <div class="pend-meta">
            <span class="badge" :style="{ background: ESTADO_COLOR[c.estado] }">{{ c.estado.replaceAll('_', ' ') }}</span>
            <span v-if="c.fecha < hoy" class="vencida-tag">Vencida</span>
          </div>
        </div>
        <div class="pend-lateral">
          <strong class="monto">${{ Number(c.costoUsd).toFixed(2) }}</strong>
          <button class="btn-primary" @click="abrirCobro(c)">
            <Banknote :size="16" /> Cobrar
          </button>
        </div>
      </div>
    </div>

    <!-- ══ Modal de cobro ══ -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="citaACobrar" class="modal-overlay" @click.self="!cobrando && (citaACobrar = null)">
          <div class="modal-card">
            <div class="modal-header">
              <h3><CreditCard :size="18" /> Cobro de Cita</h3>
              <button class="btn-close" :disabled="cobrando" @click="citaACobrar = null"><X :size="18" /></button>
            </div>
            <div class="modal-body">
              <div v-if="cobroError" class="alert error small"><AlertTriangle :size="16" /> {{ cobroError }}</div>

              <div class="resumen">
                <div class="row"><span>Cliente</span><strong>{{ citaACobrar.nombreCliente }}</strong></div>
                <div class="row"><span>Mascota</span><strong>{{ citaACobrar.nombreMascota }}</strong></div>
                <div class="row"><span>Servicio</span><strong>{{ citaACobrar.nombreServicio }}</strong></div>
                <div class="row"><span>Turno</span><strong>{{ fmtFecha(citaACobrar.fecha) }} · {{ fmtHora(citaACobrar.horaInicio) }}</strong></div>
                <div class="row total"><span>Monto</span><strong>${{ Number(citaACobrar.costoUsd).toFixed(2) }} USD</strong></div>
              </div>

              <label class="section-label">Método de pago presencial <span class="req">*</span></label>
              <div v-for="m in metodos" :key="m.id" class="method" :class="{ active: selectedMetodo === m.id }"
                @click="selectedMetodo = m.id; datosPago = {}; referencia = ''; stepErrors = {}; cobroError = ''">
                <div class="radio" :class="{ checked: selectedMetodo === m.id }"><div v-if="selectedMetodo === m.id" class="dot" /></div>
                <div><span class="m-name">{{ METODO_LABEL[m.nombre] || m.nombre }}</span><span class="m-desc">{{ m.descripcion }}</span></div>
              </div>
              <div v-if="stepErrors.metodo" class="ferr">{{ stepErrors.metodo }}</div>

              <template v-if="metodoSeleccionado">
                <div v-if="'referencia' in (metodoSeleccionado.camposRequeridos || {})" class="form-group">
                  <label>Número de referencia <span class="req">*</span></label>
                  <input v-model="referencia" type="text" placeholder="Ej: 0000123456789" :class="{ invalid: stepErrors.referencia }" />
                  <div v-if="stepErrors.referencia" class="ferr">{{ stepErrors.referencia }}</div>
                </div>
                <div v-for="campo in camposDinamicos" :key="campo.key" class="form-group">
                  <label>{{ fmtFieldLabel(campo.key) }} <span class="req">*</span></label>
                  <select v-if="campo.key === 'banco'" v-model="datosPago[campo.key]" :class="{ invalid: stepErrors[campo.key] }">
                    <option value="" disabled>Seleccione el banco</option>
                    <option v-for="b in BANCOS_VENEZUELA" :key="b.codigo" :value="b.nombre">{{ b.codigo }} - {{ b.nombre }}</option>
                  </select>
                  <input v-else v-model="datosPago[campo.key]" type="text" :class="{ invalid: stepErrors[campo.key] }" />
                  <div v-if="stepErrors[campo.key]" class="ferr">{{ stepErrors[campo.key] }}</div>
                </div>
              </template>

              <label class="check-row" :class="{ invalid: stepErrors.fondos }">
                <input v-model="fondosConfirmados" type="checkbox" />
                Confirmo la recepción de los fondos del cliente
              </label>
              <div v-if="stepErrors.fondos" class="ferr">{{ stepErrors.fondos }}</div>
            </div>
            <div class="modal-footer">
              <button class="btn-secondary" :disabled="cobrando" @click="citaACobrar = null">Cancelar</button>
              <button class="btn-primary" :disabled="!selectedMetodo || cobrando" @click="confirmarCobro">
                <Loader2 v-if="cobrando" :size="16" class="spin" />
                <Banknote v-else :size="16" /> Cobrar y Facturar
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- ══ Comprobante ══ -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="resultado" class="modal-overlay">
          <div class="modal-card success">
            <div class="modal-body success-body">
              <div class="ok-icon"><CheckCircle2 :size="44" /></div>
              <h2>Cita cobrada y facturada correctamente</h2>
              <div class="resumen">
                <div class="row"><span>Factura</span><strong class="mono">{{ resultado.numeroControl }}</strong></div>
                <div class="row"><span>Cliente</span><strong>{{ resultado.resumen.cliente }}</strong></div>
                <div class="row"><span>Mascota</span><strong>{{ resultado.resumen.mascota }}</strong></div>
                <div class="row"><span>Servicio</span><strong>{{ resultado.resumen.servicio }}</strong></div>
                <div class="row"><span>Turno</span><strong>{{ fmtFecha(resultado.resumen.fecha) }} · {{ fmtHora(resultado.resumen.horaInicio) }}</strong></div>
                <div class="row"><span>Método</span><strong>{{ METODO_LABEL[resultado.resumen.metodoPago] || resultado.resumen.metodoPago }}</strong></div>
                <div class="row total"><span>Total facturado</span><strong>${{ Number(resultado.resumen.costoUsd).toFixed(2) }} USD · Bs. {{ Number(resultado.resumen.costoBs).toFixed(2) }}</strong></div>
              </div>
              <div v-if="envio" class="alert" :class="envio.enviado ? 'ok' : 'warn'">{{ envio.mensaje }}</div>
              <div class="actions">
                <button class="btn-secondary" :disabled="imprimiendo" @click="imprimirComprobante">
                  <Loader2 v-if="imprimiendo" :size="16" class="spin" /><Printer v-else :size="16" /> Imprimir
                </button>
                <button class="btn-secondary" :disabled="enviando" @click="enviarPorCorreo">
                  <Loader2 v-if="enviando" :size="16" class="spin" /><Mail v-else :size="16" /> Enviar por correo
                </button>
                <button class="btn-primary" @click="cerrarComprobante">Volver</button>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; font-family: 'Inter','Segoe UI',Roboto,sans-serif; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; flex-wrap: wrap; }
.page-title { font-size: 24px; font-weight: 700; color: #1E293B; margin: 0; letter-spacing: -0.03em; }
.page-subtitle { color: #64748B; font-size: 14px; margin: 4px 0 0; }
.btn-primary, .btn-secondary { display: inline-flex; align-items: center; gap: 8px; padding: 10px 20px; border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit; transition: all .2s; }
.btn-primary { background: #0F766E; color: #fff; border: none; }
.btn-primary:hover:not(:disabled) { background: #115E59; }
.btn-primary:disabled { background: #94A3B8; cursor: not-allowed; }
.btn-secondary { background: #fff; color: #475569; border: 1.5px solid #E2E8F0; }
.alert { display: flex; align-items: center; gap: 8px; padding: 12px 16px; border-radius: 10px; font-size: 13px; font-weight: 500; }
.alert.error { color: #DC2626; background: #FEF2F2; border: 1px solid #FECACA; }
.alert.warn { color: #B45309; background: #FFFBEB; border: 1px solid #FDE68A; }
.alert.ok { color: #059669; background: #ECFDF5; border: 1px solid #A7F3D0; }
.alert.error.small { margin-bottom: 14px; }
.state { padding: 64px 24px; text-align: center; color: #64748B; background: #fff; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,.03), 0 20px 40px -4px rgba(15,118,110,.08); }
.state.empty { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.state.empty h3 { font-size: 18px; color: #1E293B; margin: 0; } .state.empty p { font-size: 14px; margin: 0; }
.spinner { width: 18px; height: 18px; border: 2px solid rgba(15,118,110,.2); border-top-color: #0F766E; border-radius: 50%; animation: spin .6s linear infinite; display: inline-block; vertical-align: middle; margin-right: 8px; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.pendientes-list { display: flex; flex-direction: column; gap: 14px; }
.pend-card { background: #fff; border-radius: 14px; box-shadow: 0 4px 6px -1px rgba(0,0,0,.03), 0 20px 40px -4px rgba(15,118,110,.08); padding: 18px 20px; display: flex; align-items: center; gap: 18px; }
.pend-card.vencida { border-left: 4px solid #DC3545; }
.pend-fecha { display: flex; flex-direction: column; align-items: center; min-width: 64px; background: #F0FDFA; border: 1px solid #99F6E4; border-radius: 12px; padding: 8px 10px; }
.pend-fecha.vencida { background: #FEF2F2; border-color: #FECACA; }
.pf-dia { font-size: 18px; font-weight: 700; color: #0F766E; }
.pend-fecha.vencida .pf-dia { color: #DC2626; }
.pf-mes { font-size: 11px; color: #0F766E; text-transform: uppercase; }
.pf-hora { font-size: 11px; color: #64748B; margin-top: 2px; }
.pend-info { flex: 1; font-size: 14px; color: #475569; }
.pend-info strong { display: inline-flex; align-items: center; gap: 4px; color: #1E293B; }
.pend-meta { display: flex; align-items: center; gap: 8px; font-size: 12px; color: #94A3B8; margin-top: 4px; flex-wrap: wrap; }
.badge { font-size: 11px; font-weight: 600; color: #fff; padding: 2px 10px; border-radius: 20px; }
.vencida-tag { font-size: 11px; font-weight: 600; color: #DC2626; background: #FEF2F2; border: 1px solid #FECACA; padding: 2px 8px; border-radius: 20px; }
.pend-lateral { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; }
.monto { font-size: 16px; font-weight: 700; color: #1E293B; }
.modal-overlay { position: fixed; inset: 0; background: rgba(15,23,42,.5); backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center; padding: 24px; z-index: 100; }
.modal-card { background: #fff; border-radius: 16px; max-width: 500px; width: 100%; max-height: 90vh; overflow-y: auto; box-shadow: 0 25px 50px -12px rgba(0,0,0,.25); }
.modal-card.success { border-top: 4px solid #0F766E; }
.modal-header { padding: 18px 24px; border-bottom: 1px solid #E2E8F0; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { font-size: 16px; font-weight: 700; color: #1E293B; display: flex; align-items: center; gap: 8px; margin: 0; }
.btn-close { width: 32px; height: 32px; border-radius: 8px; border: none; background: #F1F5F9; color: #64748B; display: flex; align-items: center; justify-content: center; cursor: pointer; }
.modal-body { padding: 24px; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 16px 24px 20px; border-top: 1px solid #E2E8F0; }
.resumen { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 12px; padding: 6px 16px; margin-bottom: 18px; }
.resumen .row { display: flex; justify-content: space-between; gap: 12px; padding: 9px 0; border-bottom: 1px solid #F1F5F9; font-size: 13px; }
.resumen .row:last-child { border-bottom: none; }
.resumen .row span { color: #64748B; }
.resumen .row strong { color: #1E293B; text-align: right; }
.resumen .row.total strong { color: #0F766E; font-size: 15px; }
.mono { font-family: ui-monospace, Menlo, Consolas, monospace; }
.section-label { display: block; font-size: 13px; font-weight: 600; color: #475569; margin: 4px 0 10px; }
.req { color: #EF4444; }
.method { display: flex; align-items: center; gap: 12px; border: 1.5px solid #E2E8F0; border-radius: 10px; padding: 12px 16px; cursor: pointer; margin-bottom: 10px; transition: all .15s; }
.method:hover { border-color: #99F6E4; }
.method.active { border-color: #0F766E; background: #F0FDFA; }
.radio { width: 18px; height: 18px; border-radius: 50%; border: 2px solid #CBD5E1; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.radio.checked { border-color: #0F766E; }
.radio .dot { width: 10px; height: 10px; border-radius: 50%; background: #0F766E; }
.m-name { display: block; font-size: 14px; font-weight: 600; color: #1E293B; }
.m-desc { font-size: 12px; color: #64748B; }
.form-group { display: flex; flex-direction: column; gap: 6px; margin: 12px 0; }
.form-group label { font-size: 13px; font-weight: 600; color: #475569; }
.form-group input, .form-group select { padding: 10px 14px; border: 1.5px solid #E2E8F0; border-radius: 10px; font-size: 14px; color: #1E293B; background: #F8FAFC; font-family: inherit; outline: none; box-sizing: border-box; }
.form-group input:focus, .form-group select:focus { border-color: #0F766E; background: #fff; box-shadow: 0 0 0 3px rgba(15,118,110,.1); }
.invalid { border-color: #EF4444 !important; background: #FEF2F2 !important; }
.ferr { font-size: 12px; color: #EF4444; font-weight: 500; }
.check-row { display: flex; align-items: center; gap: 10px; font-size: 14px; color: #475569; font-weight: 500; margin: 14px 0 4px; cursor: pointer; }
.check-row.invalid { color: #DC2626; }
.check-row input { width: 18px; height: 18px; accent-color: #0F766E; }
.success-body { text-align: center; }
.ok-icon { color: #10B981; margin-bottom: 10px; }
.success-body h2 { font-size: 19px; font-weight: 700; color: #1E293B; margin: 0 0 18px; }
.success-body .resumen { text-align: left; }
.actions { display: flex; gap: 12px; justify-content: center; flex-wrap: wrap; margin-top: 8px; }
.fade-enter-active, .fade-leave-active { transition: opacity .25s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>