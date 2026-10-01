<template>
  <div class="caja-view">
    <ToastContainer />

    <!-- ═══ HERO ═══ -->
    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <Sparkles :size="12" /> Recepción · Caja
        </span>
        <h1>Caja</h1>
        <p class="page-header-sub">
          Cobra las facturas de productos generadas por las atenciones clínicas y verifica los pagos online.
        </p>
      </div>
      <div class="page-header-actions">
        <AppButton
          variant="secondary"
          :loading="cargandoFacturas || cargandoVerificacion"
          @click="actualizar"
        >
          <template #icon-left>
            <RefreshCw :size="15" />
          </template>
          Actualizar
        </AppButton>
      </div>
    </header>

    <!-- ═══ KPIs ═══ -->
    <section class="kpis">
      <article class="kpi">
        <div class="kpi-icon kpi-icon-purple"><Receipt :size="18" /></div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ facturas.length }}</p>
          <p class="kpi-label">Facturas por cobrar</p>
        </div>
      </article>
      <article class="kpi" :class="{ 'is-warn': pagosPorVerificar.length > 0 }">
        <div class="kpi-icon" :class="pagosPorVerificar.length ? 'kpi-icon-warning' : 'kpi-icon-neutral'">
          <ShieldCheck :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value" :class="{ 'is-warn': pagosPorVerificar.length > 0 }">
            {{ pagosPorVerificar.length }}
          </p>
          <p class="kpi-label">Pagos por verificar</p>
        </div>
      </article>
    </section>

    <!-- ═══ TABS ═══ -->
    <nav class="tabs" role="tablist">
      <button
        type="button"
        role="tab"
        class="tab"
        :class="{ active: tab === 'facturas' }"
        :aria-selected="tab === 'facturas'"
        @click="tab = 'facturas'"
      >
        <Receipt :size="15" />
        Facturas de productos
        <span v-if="facturas.length" class="tab-count">{{ facturas.length }}</span>
      </button>
      <button
        type="button"
        role="tab"
        class="tab"
        :class="{ active: tab === 'verificacion' }"
        :aria-selected="tab === 'verificacion'"
        @click="tab = 'verificacion'"
      >
        <ShieldCheck :size="15" />
        Pagos por verificar
        <span v-if="pagosPorVerificar.length" class="tab-count is-warn">
          {{ pagosPorVerificar.length }}
        </span>
      </button>
    </nav>

    <!-- ══════════════ TAB 1: FACTURAS ══════════════ -->
    <template v-if="tab === 'facturas'">
      <AppAlert
        v-if="errorFacturas"
        variant="error"
        :action="'Reintentar'"
        @action="cargarFacturas"
      >
        {{ errorFacturas }}
      </AppAlert>

      <!-- Loading -->
      <div v-if="cargandoFacturas" class="table-card">
        <div class="skeleton-table">
          <div v-for="i in 4" :key="i" class="skeleton-row-table">
            <div class="skeleton w-25" />
            <div class="skeleton w-15" />
            <div class="skeleton w-30" />
            <div class="skeleton w-20" />
            <div class="skeleton w-15" />
          </div>
        </div>
      </div>

      <!-- Empty -->
      <AppEmptyState
        v-else-if="facturas.length === 0"
        :icon="CheckCircle2"
        title="No hay facturas de productos pendientes"
        description="Las facturas generadas por insumos aplicados en atenciones clínicas aparecerán aquí para su cobro en mostrador."
      >
        <template #action>
          <AppButton variant="secondary" @click="actualizar">
            <template #icon-left><RefreshCw :size="15" /></template>
            Actualizar
          </AppButton>
        </template>
      </AppEmptyState>

      <!-- Table -->
      <div v-else class="table-card">
        <div class="table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>N.º control</th>
                <th>Fecha</th>
                <th>Cliente</th>
                <th>Documento</th>
                <th class="der">Total</th>
                <th class="centro">Detalles</th>
                <th class="der">Acción</th>
              </tr>
            </thead>
            <tbody>
              <template v-for="f in facturas" :key="f.idFactura">
                <tr class="fila-factura">
                  <td class="cell-control">
                    <span class="mono">{{ f.numeroControl }}</span>
                  </td>
                  <td class="cell-fecha">{{ fmtFecha(f.fechaEmision) }}</td>
                  <td class="cell-cliente">{{ f.cliente?.nombre || '—' }}</td>
                  <td class="cell-doc">
                    <span class="doc-pill">{{ f.cliente?.documento || '—' }}</span>
                  </td>
                  <td class="der cell-monto">
                    <strong>{{fmtUsd(f.totalNeto) }}</strong>
                  </td>
                  <td class="centro">
                    <button
                      type="button"
                      class="btn-expand"
                      :aria-expanded="facturaExpandida === f.idFactura"
                      :title="facturaExpandida === f.idFactura ? 'Ocultar detalles' : 'Ver detalles'"
                      @click="alternarDetalles(f.idFactura)"
                    >
                      <ChevronUp v-if="facturaExpandida === f.idFactura" :size="15" />
                      <ChevronDown v-else :size="15" />
                    </button>
                  </td>
                  <td class="der">
                    <AppButton variant="primary" size="sm" @click="abrirCobroFactura(f)">
                      <template #icon-left><Banknote :size="14" /></template>
                      Cobrar
                    </AppButton>
                  </td>
                </tr>
                <tr v-if="facturaExpandida === f.idFactura" class="detalles-row">
                  <td colspan="7">
                    <div class="detalles-wrap">
                      <p class="detalles-titulo">
                        <Package :size="13" /> Detalle de la factura
                      </p>
                      <table class="data-table inner">
                        <thead>
                          <tr>
                            <th>Producto</th>
                            <th class="der">Cantidad</th>
                            <th class="der">P. unitario</th>
                            <th class="der">Subtotal</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="(d, i) in f.detalles" :key="i">
                            <td>{{ d.descripcion }}</td>
                            <td class="der">{{ d.cantidad }}</td>
                            <td class="der">{{ fmtUsd(d.precioUnitario) }}</td>
                            <td class="der amount">
                              {{ fmtUsd(d.cantidad * d.precioUnitario) }}
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </td>
                </tr>
              </template>
            </tbody>
          </table>
        </div>
      </div>
    </template>

    <!-- ══════════════ TAB 2: PAGOS POR VERIFICAR ══════════════ -->
    <template v-else>
      <AppAlert
        v-if="errorVerificacion"
        variant="error"
        :action="'Reintentar'"
        @action="cargarPagosPorVerificar"
      >
        {{ errorVerificacion }}
      </AppAlert>

      <!-- Loading -->
      <div v-if="cargandoVerificacion" class="skeleton-list">
        <div v-for="i in 3" :key="i" class="skeleton-card">
          <div class="skeleton sk-fecha" />
          <div class="skeleton-info">
            <div class="skeleton w-60" />
            <div class="skeleton w-40" />
          </div>
          <div class="skeleton sk-monto" />
        </div>
      </div>

      <!-- Empty -->
      <AppEmptyState
        v-else-if="pagosPorVerificar.length === 0"
        :icon="ShieldCheck"
        title="No hay pagos pendientes de verificación"
        description="Todos los pagos online están verificados. Los nuevos aparecerán aquí automáticamente tras el pago del cliente."
      >
        <template #action>
          <AppButton variant="secondary" @click="actualizar">
            <template #icon-left><RefreshCw :size="15" /></template>
            Actualizar
          </AppButton>
        </template>
      </AppEmptyState>

      <!-- Verificación cards -->
      <div v-else class="verificacion-list">
        <article
          v-for="p in pagosPorVerificar"
          :key="p.idPago"
          class="verif-card"
          :data-pago-id="p.idPago"
        >
          <header class="verif-header">
            <div class="verif-metodo">
              <div class="verif-icon">
                <component :is="iconoMetodoVerif(p.metodoPago)" :size="18" />
              </div>
              <div>
                <p class="verif-metodo-nombre">{{ fmtMetodoNombre(p.metodoPago) }}</p>
                <p class="verif-metodo-tipo">Pago online · Factura {{ p.numeroControl }}</p>
              </div>
            </div>
            <div class="verif-monto">
              <span class="verif-monto-label">Monto</span>
              <strong class="verif-monto-valor">{{ fmtUsd(p.monto) }} USD</strong>
              <span v-if="fmtBs(p.montoBs)" class="verif-monto-bs">
                {{ fmtBs(p.montoBs) }}
              </span>
            </div>
          </header>

          <div class="verif-body">
            <div class="verif-col">
              <p class="verif-label"><User :size="12" /> Cliente</p>
              <p class="verif-valor">{{ p.clienteNombre }}</p>
              <p class="verif-meta">{{ p.clienteDocumento }} · {{ p.clienteTelefono }}</p>
            </div>
            <div v-if="p.mascotaNombre" class="verif-col">
              <p class="verif-label"><PawPrint :size="12" /> Mascota</p>
              <p class="verif-valor">{{ p.mascotaNombre }}</p>
            </div>
            <div class="verif-col">
              <p class="verif-label"><CalendarDays :size="12" /> Fecha del pago</p>
              <p class="verif-valor">{{ fmtFechaHora(p.fechaPago) }}</p>
            </div>
          </div>

          <div class="verif-transaccion">
            <p class="verif-transaccion-titulo">
              <CreditCard :size="13" /> Datos de la transacción
            </p>
            <div class="verif-grid">
              <div v-if="p.referenciaTransaccion" class="verif-field">
                <span class="verif-field-label">Referencia</span>
                <span class="verif-field-valor mono">{{ p.referenciaTransaccion }}</span>
              </div>
              <div
                v-for="(val, key) in p.metadataPago || {}"
                :key="key"
                class="verif-field"
              >
                <span class="verif-field-label">{{ fmtFieldLabelVerif(key) }}</span>
                <span class="verif-field-valor">{{ val }}</span>
              </div>
            </div>
          </div>

          <footer class="verif-actions">
            <AppButton variant="danger-soft" @click="abrirVerificacion(p, false)">
              <template #icon-left><X :size="14" /></template>
              Rechazar
            </AppButton>
            <AppButton variant="primary" @click="abrirVerificacion(p, true)">
              <template #icon-left><CheckCircle2 :size="14" /></template>
              Confirmar pago
            </AppButton>
          </footer>
        </article>
      </div>
    </template>

    <!-- ══════════════ MODAL COBRO DE FACTURA ══════════════ -->
    <AppModal
      :model-value="!!facturaACobrar"
      :title="`Cobro de ${facturaACobrar?.numeroControl || 'factura'}`"
      :subtitle="facturaACobrar?.cliente?.nombre || ''"
      size="lg"
      :loading="cobrando"
      @update:model-value="cerrarCobro"
    >
      <div v-if="facturaACobrar" class="cobro-content">
        <div class="info-card">
          <div class="info-row">
            <span class="info-label"><CreditCard :size="13" /> Documento</span>
            <span class="info-value mono">{{ facturaACobrar.cliente?.documento || '—' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label"><Package :size="13" /> Productos</span>
            <span class="info-value">
              {{ facturaACobrar.detalles?.length || 0 }}
              {{ (facturaACobrar.detalles?.length || 0) === 1 ? 'producto' : 'productos' }}
            </span>
          </div>
          <div class="info-row">
            <span class="info-label"><CalendarDays :size="13" /> Emisión</span>
            <span class="info-value">{{ fmtFecha(facturaACobrar.fechaEmision) }}</span>
          </div>
        </div>

        <div class="total-card">
          <div class="total-left">
            <span class="total-label">Total a cobrar</span>
            <span class="total-hint">Bs. se calcula con la tasa oficial al confirmar</span>
          </div>
          <div class="total-right">
            <span class="total-monto">{{ fmtUsd(facturaACobrar.totalNeto) }}</span>
            <span class="total-currency">USD</span>
          </div>
        </div>

        <AppAlert v-if="cobroError" variant="error">{{ cobroError }}</AppAlert>

        <p class="section-label">Método de pago <span class="required">*</span></p>
        <div class="methods-list">
          <button
            v-for="m in metodos"
            :key="m.id"
            type="button"
            class="method-option"
            :class="{ active: selectedMetodo === m.id }"
            @click="selectedMetodo = m.id; datosPago = {}; referencia = ''; stepErrors = {}; cobroError = ''"
          >
            <div class="method-radio">
              <div class="radio-outer" :class="{ checked: selectedMetodo === m.id }">
                <div v-if="selectedMetodo === m.id" class="radio-inner" />
              </div>
            </div>
            <div class="method-info">
              <span class="method-name">{{ METODO_LABEL[m.nombre] || m.nombre }}</span>
              <span class="method-desc">{{ m.descripcion }}</span>
            </div>
          </button>
        </div>
        <span v-if="stepErrors.metodo" class="form-error mt-2">{{ stepErrors.metodo }}</span>

        <Transition name="expand">
          <div v-if="metodoSeleccionado" class="dynamic-fields">
            <p class="section-label">Datos del pago</p>

            <div v-if="'referencia' in camposActuales" class="form-group">
              <label class="form-label">
                Número de referencia <span class="required">*</span>
              </label>
              <AppInput
                v-model="referencia"
                placeholder="Ej: 0000123456789"
                :error="stepErrors.referencia"
              />
              <span v-if="stepErrors.referencia" class="form-error">{{ stepErrors.referencia }}</span>
            </div>

            <div v-for="campo in camposDinamicos" :key="campo.key" class="form-group">
              <label class="form-label">
                {{ fmtFieldLabel(campo.key) }} <span class="required">*</span>
              </label>
              <AppSelect
                v-if="campo.key === 'banco'"
                v-model="datosPago[campo.key]"
                :error="stepErrors[campo.key]"
              >
                <option value="" disabled>Seleccione el banco emisor</option>
                <option v-for="b in BANCOS_VENEZUELA" :key="b.codigo" :value="b.nombre">
                  {{ b.codigo }} - {{ b.nombre }}
                </option>
              </AppSelect>
              <AppInput
                v-else
                v-model="datosPago[campo.key]"
                :error="stepErrors[campo.key]"
              />
              <span v-if="stepErrors[campo.key]" class="form-error">{{ stepErrors[campo.key] }}</span>
            </div>
          </div>
        </Transition>

        <label class="check-row" :class="{ 'is-invalid': stepErrors.fondos }">
          <input v-model="fondosConfirmados" type="checkbox" class="checkbox-input" />
          <span class="checkbox-box">
            <Check v-if="fondosConfirmados" :size="12" />
          </span>
          <span class="checkbox-label">
            Confirmo la recepción de los fondos del cliente
          </span>
        </label>
        <span v-if="stepErrors.fondos" class="form-error">{{ stepErrors.fondos }}</span>
      </div>

      <template #footer>
        <AppButton variant="secondary" :disabled="cobrando" @click="cerrarCobro">
          Cancelar
        </AppButton>
        <AppButton
          variant="primary"
          :loading="cobrando"
          :disabled="!selectedMetodo"
          @click="confirmarCobro"
        >
          <template #icon-left><Banknote :size="15" /></template>
          {{ cobrando ? 'Procesando…' : 'Cobrar factura' }}
        </AppButton>
      </template>
    </AppModal>

    <!-- ══════════════ MODAL VERIFICACIÓN ══════════════ -->
    <AppModal
      :model-value="verificacionModal"
      :title="verificacionAprobada ? 'Confirmar pago' : 'Rechazar pago'"
      subtitle="Verificación de pago online"
      size="md"
      :loading="verificandoPago"
      @update:model-value="verificacionModal = false"
    >
      <div class="verif-modal-content">
        <p class="verif-modal-texto">
          <template v-if="verificacionAprobada">
            Confirma que verificaste manualmente la transacción
            <strong>{{ pagoAVerificar?.referenciaTransaccion }}</strong>
            en el {{ fmtMetodoNombre(pagoAVerificar?.metodoPago) }}.
            El pago quedará marcado como <strong>Confirmado</strong>.
          </template>
          <template v-else>
            Si rechazas, el pago se marcará como <strong>Rechazado</strong>
            y la cita asociada se cancelará automáticamente.
          </template>
        </p>

        <div class="form-group">
          <label class="form-label">Observaciones (opcional)</label>
          <AppTextarea
            v-model="observacionesVerif"
            :rows="3"
            placeholder="Ej: Verificado en el banco. Transacción confirmada."
          />
        </div>

        <AppAlert v-if="errorVerifModal" variant="error">{{ errorVerifModal }}</AppAlert>
      </div>

      <template #footer>
        <AppButton variant="secondary" :disabled="verificandoPago" @click="verificacionModal = false">
          Cancelar
        </AppButton>
        <AppButton
          :variant="verificacionAprobada ? 'primary' : 'danger'"
          :loading="verificandoPago"
          @click="confirmarVerificacion"
        >
          <template #icon-left>
            <component :is="verificacionAprobada ? CheckCircle2 : X" :size="15" />
          </template>
          {{ verificandoPago
            ? 'Procesando…'
            : (verificacionAprobada ? 'Sí, confirmar' : 'Sí, rechazar') }}
        </AppButton>
      </template>
    </AppModal>

    <!-- ══════════════ MODAL COMPROBANTE ══════════════ -->
    <AppModal
      :model-value="!!resultado"
      title="Factura cobrada correctamente"
      size="md"
      @update:model-value="cerrarComprobante"
    >
      <div v-if="resultado" class="comprobante-content">
        <div class="success-icon-wrap">
          <CheckCircle2 :size="40" />
        </div>

        <p v-if="resultado.mensaje" class="success-message">{{ resultado.mensaje }}</p>

        <div class="cobro-resumen">
          <div class="resumen-fila">
            <span class="resumen-label"><FileText :size="13" /> Factura</span>
            <span class="resumen-value mono">{{ resultado.numeroControl }}</span>
          </div>
          <div class="resumen-fila">
            <span class="resumen-label"><User :size="13" /> Cliente</span>
            <span class="resumen-value">{{ resultado.resumen.cliente }}</span>
          </div>
          <div class="resumen-fila">
            <span class="resumen-label"><CheckCircle2 :size="13" /> Estado</span>
            <span class="resumen-value">
              <span class="badge badge-success">{{ resultado.estadoPago || 'Confirmado' }}</span>
            </span>
          </div>
          <div class="resumen-fila">
            <span class="resumen-label"><CreditCard :size="13" /> Método</span>
            <span class="resumen-value">
              {{ METODO_LABEL[resultado.resumen.metodoPago] || resultado.resumen.metodoPago }}
            </span>
          </div>
          <div class="resumen-fila total">
            <span class="resumen-label"><DollarSign :size="13" /> Total facturado</span>
            <span class="resumen-value total-value">
              {{ fmtUsd(resultado.resumen.costoUsd) }}
              <small v-if="resultado.resumen.costoBs != null" class="total-bs">
                Bs. {{ Number(resultado.resumen.costoBs).toFixed(2) }}
              </small>
            </span>
          </div>
        </div>

        <AppAlert
          v-if="envio"
          :variant="envio.enviado ? 'success' : 'warning'"
        >
          {{ envio.mensaje }}
        </AppAlert>
      </div>

      <template #footer>
        <AppButton
          variant="secondary"
          :loading="imprimiendo"
          @click="imprimirComprobante"
        >
          <template #icon-left><Printer :size="15" /></template>
          {{ imprimiendo ? 'Imprimiendo…' : 'Imprimir' }}
        </AppButton>
        <AppButton
          variant="secondary"
          :loading="enviando"
          @click="enviarPorCorreo"
        >
          <template #icon-left><Mail :size="15" /></template>
          {{ enviando ? 'Enviando…' : 'Enviar por correo' }}
        </AppButton>
        <AppButton variant="primary" @click="cerrarComprobante">
          Volver a la caja
        </AppButton>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import {
  getMetodosPresenciales, enviarFactura, descargarFactura,
  getFacturasPendientes, cobrarFactura,
  getPagosPendientesVerificacion, verificarPago,
} from '@/api/pagos.api'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppModal from '@/components/ui/AppModal.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppSelect from '@/components/ui/AppSelect.vue'
import AppTextarea from '@/components/ui/AppTextarea.vue'
import {
  Banknote, RefreshCw, PawPrint, CalendarDays, CreditCard,
  Receipt, ChevronDown, ChevronUp, Sparkles, DollarSign, User,
  FileText, Package, Check, ShieldCheck, CheckCircle2,
  AlertCircle, Landmark, Smartphone, X,
} from 'lucide-vue-next'

import { BANCOS_VENEZUELA } from '@/utils/constants/bancos'

const route = useRoute()


const METODO_LABEL = {
  Efectivo: 'Efectivo',
  Tarjeta: 'Tarjeta (Punto de Venta)',
  Pago_Movil: 'Pago Móvil',
  Transferencia: 'Transferencia bancaria',
}

/* ═══════════════════════════════════════════════════════════════
   ESTADO
   ═══════════════════════════════════════════════════════════════ */
const tab = ref('facturas')

/* Facturas */
const facturas = ref([])
const cargandoFacturas = ref(false)
const errorFacturas = ref('')
const facturaExpandida = ref(null)

/* Verificación */
const pagosPorVerificar = ref([])
const cargandoVerificacion = ref(false)
const errorVerificacion = ref('')

/* Modal verificación */
const verificacionModal = ref(false)
const pagoAVerificar = ref(null)
const verificacionAprobada = ref(true)
const observacionesVerif = ref('')
const verificandoPago = ref(false)
const errorVerifModal = ref('')

/* Modal cobro */
const facturaACobrar = ref(null)
const metodos = ref([])
const selectedMetodo = ref(null)
const datosPago = ref({})
const referencia = ref('')
const fondosConfirmados = ref(false)
const cobrando = ref(false)
const cobroError = ref('')
const stepErrors = ref({})

/* Comprobante */
const resultado = ref(null)
const envio = ref(null)
const enviando = ref(false)
const imprimiendo = ref(false)

/* ═══════════════════════════════════════════════════════════════
   COMPUTED
   ═══════════════════════════════════════════════════════════════ */
const metodoSeleccionado = computed(() =>
  metodos.value.find((m) => m.id === selectedMetodo.value) || null
)

const camposActuales = computed(() =>
  metodoSeleccionado.value?.camposRequeridos
  ?? metodoSeleccionado.value?.datosRequeridos
  ?? {}
)

const camposDinamicos = computed(() =>
  Object.entries(camposActuales.value)
    .filter(([key]) => key !== 'referencia')
    .map(([key]) => ({ key }))
)

/* ═══════════════════════════════════════════════════════════════
   CARGA DE DATOS
   ═══════════════════════════════════════════════════════════════ */
async function cargarFacturas() {
  cargandoFacturas.value = true
  errorFacturas.value = ''
  try {
    const data = await getFacturasPendientes()
    facturas.value = data || []
    facturaExpandida.value = null
  } catch (err) {
    const msg = err.response?.data?.message
    errorFacturas.value = typeof msg === 'string' ? msg : 'No se pudo cargar las facturas pendientes.'
  } finally {
    cargandoFacturas.value = false
  }
}

async function cargarPagosPorVerificar() {
  cargandoVerificacion.value = true
  errorVerificacion.value = ''
  try {
    const { data } = await getPagosPendientesVerificacion()
    pagosPorVerificar.value = data || []
  } catch (err) {
    errorVerificacion.value = err.response?.data?.message
      || 'No se pudo cargar los pagos pendientes de verificación.'
  } finally {
    cargandoVerificacion.value = false
  }
}

async function cargarMetodos() {
  try {
    const { data } = await getMetodosPresenciales()
    metodos.value = data || []
  } catch { /* vacío */ }
}

async function cargarTodo() {
  await Promise.all([
    cargarFacturas(),
    cargarPagosPorVerificar(),
    cargarMetodos(),
  ])
}

function actualizar() {
  if (tab.value === 'verificacion') cargarPagosPorVerificar()
  else cargarFacturas()
}

/* ═══════════════════════════════════════════════════════════════
   TABS Y QUERY PARAMS
   ═══════════════════════════════════════════════════════════════ */
function enfocarPagoDesdeQuery() {
  const idPago = Number(route.query.focus)
  if (!idPago) return
  nextTick(() => {
    const el = document.querySelector(`[data-pago-id="${idPago}"]`)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'center' })
      el.classList.add('is-focused')
      setTimeout(() => el.classList.remove('is-focused'), 2400)
    }
  })
}

onMounted(async () => {
  if (route.query.tab === 'verificacion') {
    tab.value = 'verificacion'
  }
  await cargarTodo()
  enfocarPagoDesdeQuery()
})

watch(() => route.query, () => {
  if (route.query.tab === 'verificacion') {
    tab.value = 'verificacion'
    nextTick(() => enfocarPagoDesdeQuery())
  }
}, { deep: true })

/* ═══════════════════════════════════════════════════════════════
   FACTURAS: EXPANDIR DETALLES
   ═══════════════════════════════════════════════════════════════ */
function alternarDetalles(idFactura) {
  facturaExpandida.value = facturaExpandida.value === idFactura ? null : idFactura
}

/* ═══════════════════════════════════════════════════════════════
   COBRO DE FACTURA
   ═══════════════════════════════════════════════════════════════ */
function abrirCobroFactura(factura) {
  facturaACobrar.value = factura
  selectedMetodo.value = null
  datosPago.value = {}
  referencia.value = ''
  fondosConfirmados.value = false
  cobroError.value = ''
  stepErrors.value = {}
}

function cerrarCobro() {
  if (cobrando.value) return
  facturaACobrar.value = null
}

async function confirmarCobro() {
  const campos = camposActuales.value
  const errors = {}
  if (!selectedMetodo.value) errors.metodo = 'Seleccione un método de pago'
  if ('referencia' in campos && !referencia.value.trim()) {
    errors.referencia = 'La referencia es obligatoria'
  }
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
    const payload = {
      idMetodoPago: selectedMetodo.value,
      referenciaTransaccion: referencia.value.trim() || undefined,
      datosPago: datosPagoCompletos,
    }

    const data = await cobrarFactura(facturaACobrar.value.idFactura, payload)
    resultado.value = {
      tipo: 'factura',
      idFactura: data.idFactura,
      numeroControl: data.numeroControl,
      estadoPago: data.estadoPago,
      mensaje: data.mensaje,
      resumen: {
        cliente: facturaACobrar.value.cliente?.nombre || '—',
        metodoPago: data.metodoPago || metodoSeleccionado.value?.nombre,
        costoUsd: data.monto,
        costoBs: null,
      },
    }
    facturaACobrar.value = null
    envio.value = null
  } catch (err) {
    const msgRaw = err.response?.data?.message
    const msg = typeof msgRaw === 'string'
      ? msgRaw
      : (msgRaw ? Object.values(msgRaw).join(' · ') : '')

    if (err.response?.status === 409) {
      facturaACobrar.value = null
      cobroError.value = msg || 'Esta factura ya tiene un pago registrado. La lista fue actualizada.'
      await cargarFacturas()
    } else {
      cobroError.value = msg || 'No se pudo confirmar el pago. Verifique la transacción e intente nuevamente.'
    }
  } finally {
    cobrando.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   VERIFICACIÓN DE PAGOS
   ═══════════════════════════════════════════════════════════════ */
function abrirVerificacion(pago, aprobado) {
  pagoAVerificar.value = pago
  verificacionAprobada.value = aprobado
  observacionesVerif.value = ''
  errorVerifModal.value = ''
  verificacionModal.value = true
}

async function confirmarVerificacion() {
  if (!pagoAVerificar.value) return
  verificandoPago.value = true
  errorVerifModal.value = ''
  try {
    await verificarPago(pagoAVerificar.value.idPago, {
      aprobado: verificacionAprobada.value,
      observaciones: observacionesVerif.value.trim() || null,
    })
    verificacionModal.value = false
    pagoAVerificar.value = null
    await cargarPagosPorVerificar()
    await cargarFacturas()
  } catch (err) {
    errorVerifModal.value = err.response?.data?.message
      || 'No se pudo procesar la verificación. Intenta de nuevo.'
  } finally {
    verificandoPago.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   COMPROBANTE
   ═══════════════════════════════════════════════════════════════ */
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
  } finally {
    enviando.value = false
  }
}

function cerrarComprobante() {
  resultado.value = null
  cargarFacturas()
}

/* ═══════════════════════════════════════════════════════════════
   HELPERS
   ═══════════════════════════════════════════════════════════════ */
const MESES = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']

function fmtFecha(iso) {
  if (!iso) return ''
  const [y, m, d] = String(iso).slice(0, 10).split('-')
  return `${d} ${MESES[Number(m) - 1]} ${y}`
}

function fmtFechaHora(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleString('es-VE', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  })
}

function fmtUsd(v) {
  if (v == null) return '$0.00'
  return `$${Number(v).toFixed(2)}`
}

function fmtBs(v) {
  if (v == null) return null
  return `Bs. ${Number(v).toLocaleString('es-VE', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`
}

function iconoMetodoVerif(nombre) {
  if (nombre === 'Transferencia') return Landmark
  if (nombre === 'Pago_Movil') return Smartphone
  return CreditCard
}

function fmtMetodoNombre(nombre) {
  if (!nombre) return 'Pago online'
  return METODO_LABEL[nombre] || nombre
}

function fmtFieldLabelVerif(key) {
  const labels = {
    banco: 'Banco',
    telefono: 'Teléfono',
    referencia: 'Referencia',
    lote: 'Lote',
    ultimos_digitos: 'Últimos 4 dígitos',
    numero_cuenta: 'Número de cuenta',
  }
  return labels[key] || String(key).replace(/_/g, ' ')
}

function fmtFieldLabel(key) {
  const labels = {
    banco: 'Banco emisor',
    telefono: 'Teléfono asociado',
    lote: 'Número de lote',
    ultimos_digitos: 'Últimos 4 dígitos de la tarjeta',
  }
  return labels[key] || key.replace(/_/g, ' ')
}
</script>

<style scoped>
.caja-view {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* KPIs */
.kpis {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}
.kpi {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  transition: all var(--duration-base) var(--ease-out);
}
.kpi:hover { border-color: var(--border-strong); transform: translateY(-2px); box-shadow: var(--shadow-md); }
.kpi.is-warn { border-color: var(--warning-200); }
.kpi-icon {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.kpi-icon-purple  { background: var(--purple-50); color: var(--purple-600); }
.kpi-icon-warning { background: var(--warning-50); color: var(--warning-600); }
.kpi-icon-neutral { background: var(--neutral-100); color: var(--text-secondary); }
.kpi-texto { min-width: 0; }
.kpi-value {
  margin: 0;
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.1;
  letter-spacing: var(--tracking-tight);
}
.kpi-value.is-warn { color: var(--warning-700); }
.kpi-label {
  margin: var(--space-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}

/* TABS */
.tabs { margin-bottom: var(--space-1); }
.tab-count.is-warn {
  background: var(--warning-50);
  color: var(--warning-700);
  border: 1px solid var(--warning-200);
}

/* TABLE */
.table-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}
.table-wrap { overflow-x: auto; }
.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 780px;
}
.data-table thead { background: var(--bg-surface-alt); }
.data-table th {
  text-align: left;
  padding: var(--space-3) var(--space-4);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-subtle);
  white-space: nowrap;
}
.data-table td {
  padding: var(--space-4);
  font-size: var(--text-md);
  color: var(--text-primary);
  border-bottom: 1px solid var(--neutral-100);
  vertical-align: middle;
}
.fila-factura:hover td { background: var(--bg-surface-alt); }
.der { text-align: right; }
.centro { text-align: center; }
.amount { font-weight: var(--font-bold); color: var(--brand-700); }
.mono {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  color: var(--neutral-700);
  background: var(--neutral-100);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
  display: inline-block;
}
.cell-fecha {
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.cell-cliente { font-weight: var(--font-semibold); color: var(--text-primary); }
.doc-pill {
  display: inline-block;
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  color: var(--neutral-600);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  padding: 2px var(--space-2);
  border-radius: var(--radius-sm);
}
.cell-monto strong {
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--brand-700);
}
.btn-expand {
  width: 30px;
  height: 30px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.btn-expand:hover { border-color: var(--brand-700); color: var(--brand-700); background: var(--brand-50); }

/* Detalles */
.detalles-row td {
  background: var(--brand-50);
  padding: 0;
  border-bottom: 1px solid var(--brand-200);
}
.detalles-wrap { padding: var(--space-4) var(--space-5) var(--space-5); }
.detalles-titulo {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--brand-700);
}
.data-table.inner {
  background: var(--bg-surface);
  border: 1px solid var(--brand-200);
  border-radius: var(--radius-lg);
  overflow: hidden;
  min-width: 0;
}
.data-table.inner thead { background: var(--bg-surface); }
.data-table.inner th {
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-2xs);
  border-bottom: 1px solid var(--border-subtle);
}
.data-table.inner td {
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  border-bottom: 1px solid var(--neutral-100);
}
.data-table.inner tbody tr:last-child td { border-bottom: none; }

/* SKELETON */
.skeleton-table { padding: var(--space-4); }
.skeleton-row-table {
  display: grid;
  grid-template-columns: 1fr 1fr 2fr 1fr 1fr;
  gap: var(--space-4);
  padding: var(--space-4);
  border-bottom: 1px solid var(--neutral-100);
}
.skeleton-row-table:last-child { border-bottom: none; }
.w-15 { height: 12px; width: 60%; }
.w-20 { height: 12px; width: 80%; }
.w-25 { height: 12px; width: 100%; }
.w-30 { height: 12px; width: 90%; }

.skeleton-list { display: flex; flex-direction: column; gap: var(--space-3); }
.skeleton-card {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
}
.sk-fecha { width: 64px; height: 76px; border-radius: var(--radius-xl); }
.skeleton-info { display: flex; flex-direction: column; gap: var(--space-2); }
.sk-monto { width: 90px; height: 24px; border-radius: var(--radius-sm); }
.w-40 { height: 12px; width: 40%; }
.w-60 { height: 14px; width: 60%; }

/* VERIFICACIÓN */
.verificacion-list { display: flex; flex-direction: column; gap: var(--space-4); }
.verif-card {
  background: var(--bg-surface);
  border: 1px solid var(--warning-200);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: all var(--duration-base) var(--ease-out);
}
.verif-card:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-1px);
}
.verif-card.is-focused {
  animation: highlightPulse 2.4s ease;
}
@keyframes highlightPulse {
  0%, 100% { box-shadow: var(--shadow-sm); border-color: var(--warning-200); }
  20%, 60% { box-shadow: 0 0 0 4px rgba(217, 119, 6, 0.18); border-color: var(--warning-500); }
}

.verif-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: linear-gradient(90deg, var(--warning-50) 0%, var(--bg-surface) 60%);
  border-bottom: 1px solid var(--warning-200);
  flex-wrap: wrap;
}
.verif-metodo { display: flex; align-items: center; gap: var(--space-3); min-width: 0; }
.verif-icon {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-xl);
  background: var(--bg-surface);
  border: 1px solid var(--warning-200);
  color: var(--warning-700);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.verif-metodo-nombre { margin: 0; font-size: var(--text-lg); font-weight: var(--font-bold); color: var(--text-primary); }
.verif-metodo-tipo { margin: 2px 0 0; font-size: var(--text-sm); color: var(--text-secondary); }
.verif-monto { display: flex; flex-direction: column; align-items: flex-end; gap: 2px; }
.verif-monto-label {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
}
.verif-monto-valor { font-size: var(--text-4xl); font-weight: var(--font-bold); color: var(--brand-700); letter-spacing: -0.02em; }

.verif-monto-bs {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
  letter-spacing: -0.01em;
  font-variant-numeric: tabular-nums;
  margin-top: var(--space-1);
}

.verif-body {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--neutral-100);
}
.verif-col { min-width: 0; }
.verif-label {
  margin: 0 0 var(--space-1);
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
}
.verif-valor { margin: 0; font-size: var(--text-md); font-weight: var(--font-bold); color: var(--text-primary); word-break: break-word; }
.verif-meta { margin: 3px 0 0; font-size: var(--text-sm); color: var(--text-secondary); }

.verif-transaccion {
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface-alt);
  border-bottom: 1px solid var(--border-subtle);
}
.verif-transaccion-titulo {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--brand-700);
}
.verif-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--space-3);
}
.verif-field {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.verif-field-label {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--text-tertiary);
}
.verif-field-valor { font-size: var(--text-md); font-weight: var(--font-bold); color: var(--text-primary); word-break: break-word; }
.verif-field-valor.mono { font-family: var(--font-mono); font-size: var(--text-sm); }

.verif-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-5) var(--space-4);
  background: var(--bg-surface-alt);
}

/* MODAL COBRO */
.cobro-content { display: flex; flex-direction: column; gap: var(--space-4); }

.info-card {
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-1) var(--space-4);
}
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-subtle);
  font-size: var(--text-md);
}
.info-row:last-child { border-bottom: none; }
.info-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  font-weight: var(--font-semibold);
  font-size: var(--text-sm);
  white-space: nowrap;
}
.info-label svg { color: var(--text-tertiary); }
.info-value { color: var(--text-primary); font-weight: var(--font-bold); text-align: right; word-break: break-word; min-width: 0; }
.info-value.mono { font-family: var(--font-mono); font-size: var(--text-sm); background: var(--neutral-100); padding: 2px var(--space-2); border-radius: var(--radius-sm); }

.total-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: linear-gradient(135deg, var(--brand-50) 0%, var(--bg-surface) 100%);
  border: 1px solid var(--brand-200);
  border-radius: var(--radius-xl);
}
.total-left { display: flex; flex-direction: column; gap: 2px; }
.total-label {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--brand-700);
}
.total-hint { font-size: var(--text-xs); color: var(--text-secondary); }
.total-right { display: flex; align-items: baseline; gap: var(--space-1); }
.total-monto {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  letter-spacing: var(--tracking-tight);
  line-height: 1;
}
.total-currency { font-size: var(--text-sm); font-weight: var(--font-bold); color: var(--brand-700); letter-spacing: 0.05em; }

.section-label {
  margin: var(--space-2) 0 0;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}
.required { color: var(--danger-500); }

.methods-list { display: flex; flex-direction: column; gap: var(--space-3); }
.method-option {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  border: 1.5px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--duration-base) var(--ease-out);
  background: var(--bg-surface);
  font-family: inherit;
  text-align: left;
  width: 100%;
}
.method-option:hover { border-color: var(--brand-200); background: var(--brand-50); }
.method-option.active { border-color: var(--brand-700); background: var(--brand-50); box-shadow: 0 0 0 3px var(--brand-100); }
.method-radio { flex-shrink: 0; }
.radio-outer {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid var(--neutral-300);
  display: flex;
  align-items: center;
  justify-content: center;
}
.radio-outer.checked { border-color: var(--brand-700); }
.radio-inner {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--brand-700);
  animation: radioPop 0.2s var(--ease-out);
}
@keyframes radioPop { 0% { transform: scale(0); } 100% { transform: scale(1); } }
.method-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.method-name { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); }
.method-desc { font-size: var(--text-sm); color: var(--text-secondary); }

.dynamic-fields {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border-subtle);
}
.form-group { display: flex; flex-direction: column; gap: var(--space-2); }
.form-label {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--neutral-700);
}
.form-error {
  font-size: var(--text-sm);
  color: var(--danger-600);
  font-weight: var(--font-semibold);
}
.form-error.mt-2 { display: block; margin-top: var(--space-2); }

.check-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1.5px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--duration-fast) var(--ease-out);
  margin: 0;
}
.check-row:hover { border-color: var(--brand-200); background: var(--brand-50); }
.check-row.is-invalid { border-color: var(--danger-500); background: var(--danger-50); }
.checkbox-input { display: none; }
.checkbox-box {
  width: 20px;
  height: 20px;
  border-radius: var(--radius-sm);
  border: 2px solid var(--neutral-300);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-surface);
  flex-shrink: 0;
  color: var(--text-inverse);
}
.check-row .checkbox-input:checked + .checkbox-box {
  background: var(--brand-700);
  border-color: var(--brand-700);
}
.checkbox-label { font-size: var(--text-md); font-weight: var(--font-semibold); color: var(--text-primary); }

/* MODAL VERIFICACIÓN */
.verif-modal-content { display: flex; flex-direction: column; gap: var(--space-4); }
.verif-modal-texto {
  margin: 0;
  font-size: var(--text-md);
  color: var(--neutral-700);
  line-height: var(--leading-relaxed);
}
.verif-modal-texto strong { color: var(--text-primary); font-weight: var(--font-bold); }

/* COMPROBANTE */
.comprobante-content { display: flex; flex-direction: column; align-items: center; text-align: center; gap: var(--space-4); }
.success-icon-wrap {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--success-50) 0%, var(--brand-50) 100%);
  border: 3px solid var(--success-500);
  color: var(--success-600);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: successPop 0.5s var(--ease-spring);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}
.success-message {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0;
  line-height: var(--leading-relaxed);
  max-width: 420px;
}
.cobro-resumen {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  text-align: left;
}
.resumen-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-md);
}
.resumen-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  font-weight: var(--font-semibold);
  white-space: nowrap;
}
.resumen-label svg { color: var(--text-tertiary); }
.resumen-value { color: var(--text-primary); font-weight: var(--font-bold); text-align: right; word-break: break-word; min-width: 0; }
.resumen-value.mono { font-family: var(--font-mono); font-size: var(--text-sm); background: var(--neutral-100); padding: 2px var(--space-2); border-radius: var(--radius-sm); }
.resumen-fila.total {
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-subtle);
  margin-top: var(--space-1);
}
.total-value {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  color: var(--brand-700);
  font-size: var(--text-lg);
}
.total-bs { font-size: var(--text-sm); color: var(--text-secondary); font-weight: var(--font-medium); }

/* TRANSITIONS */
.expand-enter-active, .expand-leave-active {
  transition: opacity var(--duration-slow) var(--ease-out),
              transform var(--duration-slow) var(--ease-out);
  overflow: hidden;
}
.expand-enter-from, .expand-leave-to { opacity: 0; transform: translateY(-6px); }

.mt-2 { margin-top: var(--space-2); }

/* RESPONSIVE */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: 1fr 1fr; }
  .verif-body { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .caja-view { padding: var(--space-4); }
  .kpis { grid-template-columns: 1fr; gap: var(--space-3); }
  .verif-body { grid-template-columns: 1fr; gap: var(--space-3); }
  .verif-actions { flex-direction: column-reverse; }
  .verif-actions :deep(.btn) { width: 100%; }
  .verif-header { flex-direction: column; align-items: stretch; gap: var(--space-3); }
  .verif-monto { align-items: flex-start; }
}
@media (max-width: 480px) {
  .verif-monto-valor { font-size: var(--text-3xl); }
  .cobro-resumen :deep(.resumen-fila) { flex-direction: column; align-items: flex-start; }
  .resumen-value { text-align: left; }
  .total-value { align-items: flex-start; }
}
</style>