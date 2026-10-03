<template>
  <div class="caja-view">
    <ToastContainer />

    <!-- ══════════════ HERO COMPACTO ══════════════ -->
    <header class="caja-hero">
      <div class="hero-info">
        <span class="hero-eyebrow">
          <Sparkles :size="12" /> Recepción · Caja
        </span>
        <h1 class="hero-title">Caja</h1>
        <p class="hero-sub">
          Cobra facturas de productos y verifica los pagos online del día.
        </p>
      </div>
      <div class="hero-actions">
        <button
          class="hero-refresh"
          type="button"
          :disabled="cargandoFacturas || cargandoVerificacion"
          @click="actualizar"
        >
          <RefreshCw
            :size="15"
            :class="{ 'is-spinning': cargandoFacturas || cargandoVerificacion }"
          />
          <span>Actualizar</span>
        </button>
      </div>
    </header>

    <!-- ══════════════ KPIs DEL DÍA ══════════════ -->
    <section class="caja-kpis">
      <KpiCard
        :icon="Receipt"
        tone="brand"
        label="Facturas pendientes"
        :value="facturas.length"
        :hint="facturas.length === 0 ? 'Todo cobrado' : 'Por cobrar en mostrador'"
      />
      <KpiCard
        :icon="ShieldCheck"
        tone="warning"
        label="Pagos por verificar"
        :value="pagosPorVerificar.length"
        :hint="pagosPorVerificar.length === 0 ? 'Todo verificado' : 'Esperando validación'"
        :warn="pagosPorVerificar.length > 0"
      />
      <KpiCard
        :icon="Banknote"
        tone="success"
        label="Cobrado hoy"
        :value="fmtUsd(statsDelDia.cobradoUsd)"
        :hint="`${statsDelDia.pagosHoy} ${statsDelDia.pagosHoy === 1 ? 'pago' : 'pagos'} procesados`"
      />
      <KpiCard
        :icon="Clock3"
        tone="info"
        label="Antigüedad máx."
        :value="statsDelDia.antiguedadMax"
        :hint="statsDelDia.antiguedadMax === '—' ? 'Sin trabajo pendiente' : 'Esperando gestión'"
      />
    </section>

    <!-- ══════════════ TABS STICKY ══════════════ -->
    <nav class="caja-tabs" role="tablist" aria-label="Bandejas de trabajo">
      <button
        type="button"
        role="tab"
        class="caja-tab"
        :class="{ 'is-active': tab === 'facturas' }"
        :aria-selected="tab === 'facturas'"
        @click="tab = 'facturas'"
      >
        <span class="tab-icon"><Receipt :size="15" /></span>
        <span class="tab-label">Facturas de productos</span>
        <span
          v-if="facturas.length"
          class="tab-counter"
          :class="{ 'is-warn': statsDelDia.facturasUrgentes > 0 }"
        >
          {{ facturas.length }}
        </span>
      </button>

      <button
        type="button"
        role="tab"
        class="caja-tab"
        :class="{ 'is-active': tab === 'verificacion' }"
        :aria-selected="tab === 'verificacion'"
        @click="tab = 'verificacion'"
      >
        <span class="tab-icon"><ShieldCheck :size="15" /></span>
        <span class="tab-label">Pagos por verificar</span>
        <span
          v-if="pagosPorVerificar.length"
          class="tab-counter is-warn is-pulsing"
        >
          {{ pagosPorVerificar.length }}
        </span>
      </button>
    </nav>

    <!-- ══════════════ CONTENIDO ══════════════ -->
    <main class="caja-content">
      <!-- ══════════ TAB 1: FACTURAS ══════════ -->
      <section v-if="tab === 'facturas'" class="caja-panel">
        <AppAlert
          v-if="errorFacturas"
          variant="error"
          :action="'Reintentar'"
          @action="cargarFacturas"
        >
          {{ errorFacturas }}
        </AppAlert>

        <!-- Loading -->
        <div v-if="cargandoFacturas" class="skeleton-list">
          <div v-for="i in 3" :key="i" class="skeleton-card">
            <div class="skeleton sk-date" />
            <div class="skeleton-body">
              <div class="skeleton sk-line-lg" />
              <div class="skeleton sk-line-sm" />
            </div>
            <div class="skeleton sk-monto" />
          </div>
        </div>

        <!-- Empty: sin facturas -->
        <AppEmptyState
          v-else-if="facturas.length === 0"
          :icon="CheckCircle2"
          title="No hay facturas pendientes"
          description="Las facturas generadas por insumos aplicados en atenciones clínicas aparecerán aquí para su cobro."
        >
          <template #action>
            <AppButton variant="secondary" @click="actualizar">
              <template #icon-left><RefreshCw :size="15" /></template>
              Actualizar
            </AppButton>
          </template>
        </AppEmptyState>

        <!-- Lista -->
        <ul v-else class="facturas-list">
          <li
            v-for="f in facturas"
            :key="f.idFactura"
            class="factura-row"
            :class="`urgencia-${urgencia(f.fechaEmision)}`"
          >
            <article class="factura-card">
              <!-- Columna izquierda: fecha + antigüedad -->
              <div class="factura-fecha">
                <span class="fecha-dia">{{ partesFecha(f.fechaEmision).dia }}</span>
                <span class="fecha-mes">{{ partesFecha(f.fechaEmision).mes }}</span>
                <span class="fecha-hace">{{ haceTiempo(f.fechaEmision) }}</span>
              </div>

              <!-- Columna central: identificación -->
              <div class="factura-info">
                <div class="factura-head">
                  <span class="factura-control mono">{{ f.numeroControl }}</span>
                  <span
                    v-if="urgencia(f.fechaEmision) === 'alta'"
                    class="factura-alert"
                  >
                    <AlertCircle :size="12" />
                    Vencida
                  </span>
                </div>
                <h4 class="factura-cliente">{{ f.cliente?.nombre || 'Cliente' }}</h4>
                <div class="factura-meta">
                  <span class="meta-item">
                    <CreditCard :size="11" />
                    {{ f.cliente?.documento || '—' }}
                  </span>
                  <span class="meta-item">
                    <Package :size="11" />
                    {{ (f.detalles?.length || 0) }}
                    {{ (f.detalles?.length || 0) === 1 ? 'ítem' : 'ítems' }}
                  </span>
                  <button
                    type="button"
                    class="meta-toggle"
                    :aria-expanded="facturaExpandida === f.idFactura"
                    @click="alternarDetalles(f.idFactura)"
                  >
                    <component
                      :is="facturaExpandida === f.idFactura ? ChevronUp : ChevronDown"
                      :size="12"
                    />
                    {{ facturaExpandida === f.idFactura ? 'Ocultar detalle' : 'Ver detalle' }}
                  </button>
                </div>
              </div>

              <!-- Columna derecha: monto + acción -->
              <div class="factura-action">
                <div class="factura-monto">
                  <span class="monto-label">Total</span>
                  <strong class="monto-valor">{{ fmtUsd(f.totalNeto) }}</strong>
                </div>
                <button
                  class="btn-cobrar"
                  type="button"
                  @click="abrirCobroFactura(f)"
                >
                  <Banknote :size="14" />
                  Cobrar
                </button>
              </div>
            </article>

            <!-- Detalle expandible -->
            <Transition name="expand">
              <div v-if="facturaExpandida === f.idFactura" class="factura-detalle">
                <table class="detalle-table">
                  <thead>
                    <tr>
                      <th>Producto</th>
                      <th class="der">Cant.</th>
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
            </Transition>
          </li>
        </ul>
      </section>

      <!-- ══════════ TAB 2: VERIFICACIÓN ══════════ -->
      <section v-else class="caja-panel">
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
          <div v-for="i in 2" :key="i" class="skeleton-card skeleton-card-tall">
            <div class="skeleton sk-line-lg" />
            <div class="skeleton sk-line-md" />
            <div class="skeleton sk-line-sm" />
          </div>
        </div>

        <!-- Empty -->
        <AppEmptyState
          v-else-if="pagosPorVerificar.length === 0"
          :icon="ShieldCheck"
          title="No hay pagos pendientes de verificación"
          description="Todos los pagos online están verificados. Los nuevos aparecerán aquí automáticamente."
        >
          <template #action>
            <AppButton variant="secondary" @click="actualizar">
              <template #icon-left><RefreshCw :size="15" /></template>
              Actualizar
            </AppButton>
          </template>
        </AppEmptyState>

        <!-- Lista -->
        <div v-else class="verif-list">
          <article
            v-for="p in pagosPorVerificar"
            :key="p.idPago"
            class="verif-card"
            :data-pago-id="p.idPago"
          >
            <!-- Header: método + monto -->
            <header class="verif-header">
              <div class="verif-metodo">
                <div class="verif-metodo-icon">
                  <component :is="iconoMetodoVerif(p.metodoPago)" :size="18" />
                </div>
                <div class="verif-metodo-info">
                  <p class="verif-metodo-name">{{ fmtMetodoNombre(p.metodoPago) }}</p>
                  <p class="verif-metodo-ref mono">{{ p.numeroControl }}</p>
                </div>
              </div>

              <div class="verif-monto-block">
                <span class="verif-monto-label">Monto</span>
                <strong class="verif-monto-valor">{{ fmtUsd(p.monto) }}</strong>
                <span v-if="p.montoBs" class="verif-monto-bs">
                  Bs. {{ fmtBs(p.montoBs) }}
                </span>
              </div>
            </header>

            <!-- Body: cliente + transacción -->
            <div class="verif-body">
              <div class="verif-cliente">
                <p class="verif-label"><User :size="12" /> Cliente</p>
                <p class="verif-valor">{{ p.clienteNombre }}</p>
                <p class="verif-meta">
                  {{ p.clienteDocumento }} · {{ p.clienteTelefono }}
                </p>
              </div>

              <div v-if="p.mascotaNombre" class="verif-cliente">
                <p class="verif-label"><PawPrint :size="12" /> Mascota</p>
                <p class="verif-valor">{{ p.mascotaNombre }}</p>
              </div>

              <div class="verif-cliente">
                <p class="verif-label"><Clock3 :size="12" /> Recibido</p>
                <p class="verif-valor">{{ fmtFechaHora(p.fechaPago) }}</p>
                <p class="verif-meta">{{ haceTiempo(p.fechaPago) }}</p>
              </div>
            </div>

            <!-- Transacción: referencia + metadatos -->
            <div class="verif-transaccion">
              <div class="trans-titulo">
                <CreditCard :size="12" /> Datos de la transacción
              </div>
              <div class="trans-grid">
                <div v-if="p.referenciaTransaccion" class="trans-field">
                  <span class="trans-label">Referencia</span>
                  <span class="trans-valor mono">{{ p.referenciaTransaccion }}</span>
                </div>
                <div
                  v-for="(val, key) in p.metadataPago || {}"
                  :key="key"
                  class="trans-field"
                >
                  <span class="trans-label">{{ fmtFieldLabelVerif(key) }}</span>
                  <span class="trans-valor">{{ val }}</span>
                </div>
              </div>
            </div>

            <!-- Footer: acciones -->
            <footer class="verif-footer">
              <button
                class="btn-rechazar"
                type="button"
                @click="abrirVerificacion(p, false)"
              >
                <X :size="14" />
                Rechazar
              </button>
              <button
                class="btn-confirmar"
                type="button"
                @click="abrirVerificacion(p, true)"
              >
                <CheckCircle2 :size="14" />
                Confirmar pago
              </button>
            </footer>
          </article>
        </div>
      </section>
    </main>

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
              <BancoSelector
                v-if="campo.key === 'banco'"
                v-model="datosPago[campo.key]"
                :error="stepErrors[campo.key]"
                placeholder="Seleccione el banco emisor"
              />
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
  getPagosPendientesVerificacion, verificarPago,  getEstadisticasCajaHoy,
} from '@/api/pagos.api'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppModal from '@/components/ui/AppModal.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppTextarea from '@/components/ui/AppTextarea.vue'
import BancoSelector from '@/components/ui/BancoSelector.vue'
import KpiCard from '@/components/ui/KpiCard.vue'
import {
  Banknote, RefreshCw, PawPrint, CalendarDays, CreditCard,
  Receipt, ChevronDown, ChevronUp, Sparkles, DollarSign, User,
  FileText, Package, Check, ShieldCheck, CheckCircle2,
  AlertCircle, Landmark, Smartphone, X, Clock3, Printer, Mail,
} from 'lucide-vue-next'

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
const estadisticasHoy = ref(null)

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

/* ═══ Stats del día (para los KPIs) ═══ */
const statsDelDia = computed(() => {
  const stats = estadisticasHoy.value || {}

  const fechas = [
    ...facturas.value.map((f) => f.fechaEmision),
    ...pagosPorVerificar.value.map((p) => p.fechaPago),
  ].filter(Boolean)

  let antiguedadMax = '—'
  if (fechas.length) {
    const msMax = Math.max(...fechas.map((f) => Date.now() - new Date(f).getTime()))
    antiguedadMax = formatearDuracion(msMax)
  }

  return {
    facturasUrgentes: stats.facturasUrgentes ?? 0,
    cobradoUsd: Number(stats.totalCobradoHoyUsd ?? 0),
    pagosHoy: stats.cantidadPagosHoy ?? 0,
    antiguedadMax,
  }
})

/* ═══════════════════════════════════════════════════════════════
   HELPERS DE FECHA Y URGENCIA
   ═══════════════════════════════════════════════════════════════ */
const MESES_CORTOS = ['ENE','FEB','MAR','ABR','MAY','JUN','JUL','AGO','SEP','OCT','NOV','DIC']

function partesFecha(iso) {
  if (!iso) return { dia: '--', mes: '—' }
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return { dia: '--', mes: '—' }
  return {
    dia: String(d.getDate()).padStart(2, '0'),
    mes: MESES_CORTOS[d.getMonth()],
  }
}

function haceTiempo(iso) {
  if (!iso) return ''
  const ms = Date.now() - new Date(iso).getTime()
  return formatearDuracion(ms)
}

function formatearDuracion(ms) {
  if (ms < 60_000) return 'ahora'
  const min = Math.floor(ms / 60_000)
  if (min < 60) return `hace ${min} min`
  const h = Math.floor(min / 60)
  if (h < 24) return `hace ${h}h`
  const d = Math.floor(h / 24)
  return `hace ${d} d`
}

/** Categoriza la urgencia según horas transcurridas. */
function urgencia(iso) {
  if (!iso) return 'baja'
  const h = (Date.now() - new Date(iso).getTime()) / 3_600_000
  if (h < 1) return 'baja'
  if (h < 24) return 'media'
  return 'alta'
}

const MESES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic']

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
  return Number(v).toLocaleString('es-VE', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
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

async function cargarEstadisticas() {
  try {
     estadisticasHoy.value = await getEstadisticasCajaHoy()
   } catch {
     // silencioso: es un KPI secundario
     estadisticasHoy.value = null
   }
}

async function cargarTodo() {
  await Promise.all([
    cargarFacturas(),
    cargarPagosPorVerificar(),
    cargarMetodos(),
    cargarEstadisticas(),
  ])
}



function actualizar() {
  cargarEstadisticas()
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
  if (route.query.tab === 'verificacion') tab.value = 'verificacion'
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
    await cargarFacturas()
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

/* ═══════════════════════════════════════════════════════════════
   HERO COMPACTO
   ═══════════════════════════════════════════════════════════════ */
.caja-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-5);
  flex-wrap: wrap;
  padding: var(--space-6) var(--space-7);
  background: linear-gradient(135deg, var(--brand-50) 0%, var(--bg-surface) 55%);
  border: 1px solid var(--brand-100);
  border-radius: var(--radius-3xl);
}
.hero-info { min-width: 0; }
.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin: 0 0 var(--space-2);
  padding: var(--space-1) var(--space-3);
  background: var(--bg-surface);
  border: 1px solid var(--brand-100);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--brand-700);
}
.hero-title {
  margin: 0 0 var(--space-1);
  font-size: var(--text-5xl);
  font-weight: var(--font-bold);
  letter-spacing: var(--tracking-tight);
  line-height: 1.1;
  color: var(--text-primary);
}
.hero-sub {
  margin: 0;
  font-size: var(--text-base);
  color: var(--text-secondary);
  max-width: 620px;
}
.hero-refresh {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  background: var(--bg-surface);
  color: var(--neutral-700);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  font-family: inherit;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  white-space: nowrap;
}
.hero-refresh:hover:not(:disabled) {
  border-color: var(--brand-200);
  color: var(--brand-700);
  background: var(--brand-50);
}
.hero-refresh:disabled { opacity: 0.6; cursor: not-allowed; }
.hero-refresh .is-spinning { animation: spin 0.9s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══════════════════════════════════════════════════════════════
   KPIs
   ═══════════════════════════════════════════════════════════════ */
.caja-kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}

/* ═══════════════════════════════════════════════════════════════
   TABS STICKY
   ═══════════════════════════════════════════════════════════════ */
.caja-tabs {
  position: sticky;
  top: var(--space-4);
  z-index: var(--z-sticky, 40);
  display: flex;
  gap: var(--space-1);
  padding: var(--space-1);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
  overflow-x: auto;
  scrollbar-width: none;
}
.caja-tabs::-webkit-scrollbar { display: none; }

.caja-tab {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  background: transparent;
  border: none;
  border-radius: var(--radius-xl);
  font-family: inherit;
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--duration-base) var(--ease-out);
  white-space: nowrap;
  flex: 1;
  justify-content: center;
}
.caja-tab:hover { color: var(--brand-700); background: var(--brand-50); }
.caja-tab.is-active {
  background: var(--brand-700);
  color: var(--text-inverse);
  box-shadow: 0 4px 12px -4px rgba(15, 118, 110, 0.35);
}
.caja-tab.is-active .tab-counter {
  background: rgba(255, 255, 255, 0.22);
  color: var(--text-inverse);
}

.tab-icon { display: inline-flex; }
.tab-label { font-weight: var(--font-bold); }
.tab-counter {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 22px;
  padding: 0 var(--space-2);
  border-radius: var(--radius-full);
  background: var(--neutral-100);
  color: var(--text-secondary);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  font-variant-numeric: tabular-nums;
}
.tab-counter.is-warn {
  background: var(--warning-50);
  color: var(--warning-700);
  border: 1px solid var(--warning-200);
}
.tab-counter.is-pulsing {
  animation: tabPulse 2s ease-in-out infinite;
}
@keyframes tabPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(217, 119, 6, 0.35); }
  50%      { box-shadow: 0 0 0 5px rgba(217, 119, 6, 0); }
}

/* ═══════════════════════════════════════════════════════════════
   CONTENIDO
   ═══════════════════════════════════════════════════════════════ */
.caja-content { min-height: 300px; }
.caja-panel { display: flex; flex-direction: column; gap: var(--space-3); }

/* ═══════════════════════════════════════════════════════════════
   SKELETONS
   ═══════════════════════════════════════════════════════════════ */
.skeleton-list { display: flex; flex-direction: column; gap: var(--space-3); }
.skeleton-card {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
}
.skeleton-card-tall {
  grid-template-columns: 1fr;
  padding: var(--space-6);
  gap: var(--space-3);
}
.skeleton {
  background: linear-gradient(90deg, var(--neutral-100) 25%, var(--neutral-200) 50%, var(--neutral-100) 75%);
  background-size: 200% 100%;
  border-radius: var(--radius-md);
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
.sk-date { width: 56px; height: 68px; border-radius: var(--radius-xl); }
.skeleton-body { display: flex; flex-direction: column; gap: var(--space-2); }
.sk-line-lg { height: 16px; width: 55%; }
.sk-line-md { height: 14px; width: 70%; }
.sk-line-sm { height: 12px; width: 40%; }
.sk-monto { width: 100px; height: 24px; border-radius: var(--radius-md); }

/* ═══════════════════════════════════════════════════════════════
   FACTURAS — LISTA
   ═══════════════════════════════════════════════════════════════ */
.facturas-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.factura-row {
  position: relative;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
  overflow: hidden;
  transition: box-shadow var(--duration-base) var(--ease-out),
              border-color var(--duration-base) var(--ease-out);
}
.factura-row::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: var(--brand-500);
  transition: background var(--duration-base) var(--ease-out);
}
.factura-row.urgencia-media::before { background: var(--warning-500); }
.factura-row.urgencia-alta::before  { background: var(--danger-500); }

.factura-row:hover {
  border-color: var(--border-strong);
  box-shadow: var(--shadow-md);
}

.factura-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: var(--space-5);
  align-items: center;
  padding: var(--space-4) var(--space-5) var(--space-4) var(--space-6);
}

/* Columna 1: fecha */
.factura-fecha {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  width: 60px;
  padding: var(--space-2) 0;
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  flex-shrink: 0;
}
.fecha-dia {
  font-size: var(--text-3xl);
  font-weight: var(--font-extrabold);
  color: var(--text-primary);
  line-height: 1;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
}
.fecha-mes {
  font-size: var(--text-2xs);
  font-weight: var(--font-extrabold);
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  line-height: 1;
}
.fecha-hace {
  margin-top: 3px;
  font-size: var(--text-2xs);
  font-weight: var(--font-semibold);
  color: var(--brand-700);
  line-height: 1;
}
.factura-row.urgencia-media .fecha-hace { color: var(--warning-600); }
.factura-row.urgencia-alta .fecha-hace  { color: var(--danger-600); }

/* Columna 2: información */
.factura-info { min-width: 0; }
.factura-head {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-1);
}
.factura-control {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  color: var(--text-tertiary);
  letter-spacing: 0.03em;
}
.factura-alert {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px var(--space-2);
  border-radius: var(--radius-full);
  background: var(--danger-50);
  color: var(--danger-700);
  border: 1px solid var(--danger-200);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}
.factura-cliente {
  margin: 0 0 var(--space-2);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.25;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.factura-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}
.meta-item svg { color: var(--text-tertiary); }
.meta-toggle {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px var(--space-2);
  background: none;
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  color: var(--brand-700);
  font-family: inherit;
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.meta-toggle:hover {
  background: var(--brand-50);
  border-color: var(--brand-100);
}

/* Columna 3: monto + acción */
.factura-action {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  flex-shrink: 0;
}
.factura-monto {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  min-width: 90px;
}
.monto-label {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.monto-valor {
  font-size: var(--text-3xl);
  font-weight: var(--font-extrabold);
  color: var(--brand-700);
  letter-spacing: -0.03em;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.btn-cobrar {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  background: var(--brand-700);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-lg);
  font-family: inherit;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  white-space: nowrap;
}
.btn-cobrar:hover {
  background: var(--brand-800);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, 0.4);
}
.btn-cobrar:active { transform: translateY(0); }

/* Detalle expandible */
.factura-detalle {
  padding: var(--space-4) var(--space-6);
  background: var(--brand-50);
  border-top: 1px solid var(--brand-100);
}
.detalle-table {
  width: 100%;
  border-collapse: collapse;
}
.detalle-table th {
  text-align: left;
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--brand-200);
}
.detalle-table td {
  padding: var(--space-3);
  font-size: var(--text-md);
  color: var(--text-primary);
  border-bottom: 1px solid var(--brand-100);
}
.detalle-table tbody tr:last-child td { border-bottom: none; }
.der { text-align: right; }
.amount { font-weight: var(--font-bold); color: var(--brand-700); font-variant-numeric: tabular-nums; }

/* ═══════════════════════════════════════════════════════════════
   VERIFICACIÓN — LISTA
   ═══════════════════════════════════════════════════════════════ */
.verif-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.verif-card {
  background: var(--bg-surface);
  border: 1px solid var(--warning-200);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-xs);
  transition: box-shadow var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
}
.verif-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-1px);
}
.verif-card.is-focused {
  animation: highlightPulse 2.4s ease;
}
@keyframes highlightPulse {
  0%, 100% { box-shadow: var(--shadow-xs); border-color: var(--warning-200); }
  20%, 60% { box-shadow: 0 0 0 4px rgba(217, 119, 6, 0.18); border-color: var(--warning-500); }
}

.verif-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: linear-gradient(90deg, var(--warning-50) 0%, var(--bg-surface) 65%);
  border-bottom: 1px solid var(--warning-200);
  flex-wrap: wrap;
}
.verif-metodo { display: flex; align-items: center; gap: var(--space-3); min-width: 0; }
.verif-metodo-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-xl);
  background: var(--bg-surface);
  border: 1px solid var(--warning-200);
  color: var(--warning-700);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.verif-metodo-info { min-width: 0; }
.verif-metodo-name {
  margin: 0 0 2px;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.2;
}
.verif-metodo-ref {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--text-secondary);
  font-weight: var(--font-semibold);
}

.verif-monto-block {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}
.verif-monto-label {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.verif-monto-valor {
  font-size: var(--text-4xl);
  font-weight: var(--font-extrabold);
  color: var(--brand-700);
  letter-spacing: -0.03em;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.verif-monto-bs {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
}

.verif-body {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-subtle);
}
.verif-cliente { min-width: 0; }
.verif-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin: 0 0 var(--space-1);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
}
.verif-valor {
  margin: 0;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  word-break: break-word;
}
.verif-meta {
  margin: 3px 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.verif-transaccion {
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface-alt);
  border-bottom: 1px solid var(--border-subtle);
}
.trans-titulo {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--brand-700);
}
.trans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--space-3);
}
.trans-field {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: var(--space-2) var(--space-3);
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.trans-label {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--text-tertiary);
}
.trans-valor {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  word-break: break-word;
}
.trans-valor.mono {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
}

.verif-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-5) var(--space-4);
  background: var(--bg-surface-alt);
}
.btn-rechazar,
.btn-confirmar {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  border-radius: var(--radius-lg);
  font-family: inherit;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  white-space: nowrap;
}
.btn-rechazar {
  background: var(--bg-surface);
  color: var(--danger-600);
  border: 1px solid var(--danger-200);
}
.btn-rechazar:hover {
  background: var(--danger-50);
  border-color: var(--danger-500);
}
.btn-confirmar {
  background: var(--brand-700);
  color: var(--text-inverse);
  border: none;
}
.btn-confirmar:hover {
  background: var(--brand-800);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, 0.4);
}

/* ═══════════════════════════════════════════════════════════════
   MODALES (mismos estilos que antes)
   ═══════════════════════════════════════════════════════════════ */
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

.verif-modal-content { display: flex; flex-direction: column; gap: var(--space-4); }
.verif-modal-texto {
  margin: 0;
  font-size: var(--text-md);
  color: var(--neutral-700);
  line-height: var(--leading-relaxed);
}
.verif-modal-texto strong { color: var(--text-primary); font-weight: var(--font-bold); }

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

/* ═══════════════════════════════════════════════════════════════
   TRANSICIONES
   ═══════════════════════════════════════════════════════════════ */
.expand-enter-active, .expand-leave-active {
  transition: opacity var(--duration-slow) var(--ease-out),
              transform var(--duration-slow) var(--ease-out);
  overflow: hidden;
}
.expand-enter-from, .expand-leave-to { opacity: 0; transform: translateY(-6px); }

.mt-2 { margin-top: var(--space-2); }

/* ═══════════════════════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════════════════════ */
@media (max-width: 1100px) {
  .caja-kpis { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .caja-view { padding: var(--space-4); gap: var(--space-4); }
  .caja-hero { padding: var(--space-5); border-radius: var(--radius-2xl); flex-direction: column; align-items: stretch; }
  .hero-title { font-size: var(--text-4xl); }
  .hero-actions { width: 100%; }
  .hero-refresh { width: 100%; justify-content: center; }

  .caja-kpis { grid-template-columns: 1fr; gap: var(--space-3); }

  .caja-tab { flex: 1 1 auto; padding: var(--space-2) var(--space-3); font-size: var(--text-sm); }
  .tab-label { display: none; }
  .caja-tab.is-active .tab-label { display: inline; }

  .factura-card {
    grid-template-columns: auto 1fr;
    gap: var(--space-3);
    padding: var(--space-4);
    padding-left: var(--space-5);
  }
  .factura-action {
    grid-column: 1 / -1;
    justify-content: space-between;
    width: 100%;
    padding-top: var(--space-3);
    border-top: 1px dashed var(--border-subtle);
    margin-top: var(--space-1);
  }

  .verif-header { flex-direction: column; align-items: stretch; gap: var(--space-3); }
  .verif-monto-block { align-items: flex-start; }
  .verif-body { grid-template-columns: 1fr; gap: var(--space-3); }
  .verif-footer { flex-direction: column-reverse; }
  .verif-footer button { width: 100%; justify-content: center; }
}
@media (max-width: 480px) {
  .caja-hero { padding: var(--space-4); }
  .hero-title { font-size: var(--text-3xl); }
  .monto-valor { font-size: var(--text-2xl); }
  .verif-monto-valor { font-size: var(--text-3xl); }
  .cobro-resumen .resumen-fila { flex-direction: column; align-items: flex-start; }
  .resumen-value { text-align: left; }
  .total-value { align-items: flex-start; }
}
</style>