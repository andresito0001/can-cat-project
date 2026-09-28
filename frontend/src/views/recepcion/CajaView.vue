<template>
  <div class="cobrar-mostrador">
    <ToastContainer />

    <!-- ═══ HERO ═══ -->
    <header class="hero">
      <div class="hero-left">
        <p class="hero-eyebrow">
          <Sparkles :size="12" />
          Recepción · Caja
        </p>
        <h1>Caja</h1>
        <p class="hero-sub">
          Cobra las facturas de productos generadas por las atenciones clínicas y verifica los pagos online.
        </p>
      </div>
      <div class="hero-right">
        <button
          class="btn-secondary"
          type="button"
          :disabled="cargandoFacturas || cargandoVerificacion"
          @click="actualizar"
        >
          <RefreshCw
            :size="15"
            :class="{ spin: cargandoFacturas || cargandoVerificacion }"
          />
          Actualizar
        </button>
      </div>
    </header>

    <!-- ═══ KPIs ═══ -->
    <section class="kpis">
      <article class="kpi">
        <div class="kpi-icon" style="--kpi-color: #8B5CF6; --kpi-bg: #F5F3FF;">
          <Receipt :size="18" />
        </div>
        <div class="kpi-texto">
          <p class="kpi-value">{{ facturas.length }}</p>
          <p class="kpi-label">Facturas por cobrar</p>
        </div>
      </article>
      <article class="kpi">
        <div
          class="kpi-icon"
          :style="pagosPorVerificar.length
            ? '--kpi-color: #D97706; --kpi-bg: #FFFBEB;'
            : '--kpi-color: #94A3B8; --kpi-bg: #F1F5F9;'"
        >
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

    <!-- ═══ ALERTA GLOBAL ═══ -->
    <div v-if="cobroError && !citaACobrar && !resultado" class="alert alert-warning">
      <AlertTriangle :size="16" />
      <span>{{ cobroError }}</span>
      <button type="button" class="alert-action" @click="cobroError = ''">Cerrar</button>
    </div>

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
      <div v-if="errorFacturas" class="alert alert-error">
        <AlertCircle :size="16" />
        <span>{{ errorFacturas }}</span>
        <button type="button" class="alert-action" @click="cargarFacturas">Reintentar</button>
      </div>

      <div v-if="cargandoFacturas" class="table-card">
        <div class="skeleton-table">
          <div v-for="i in 4" :key="i" class="skeleton-row-table">
            <div class="skeleton-block w-25" />
            <div class="skeleton-block w-15" />
            <div class="skeleton-block w-30" />
            <div class="skeleton-block w-20" />
            <div class="skeleton-block w-15" />
          </div>
        </div>
      </div>

      <div v-else-if="facturas.length === 0" class="empty-state">
        <div class="empty-icon"><CheckCircle2 :size="32" /></div>
        <h3>No hay facturas de productos pendientes</h3>
        <p>
          Las facturas generadas por insumos aplicados en atenciones clínicas
          aparecerán aquí para su cobro en mostrador.
        </p>
        <button class="btn-secondary" type="button" @click="actualizar">
          <RefreshCw :size="15" /> Actualizar
        </button>
      </div>

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
                  <td class="cell-cliente">{{ f.cliente?.nombre }}</td>
                  <td class="cell-doc">
                    <span class="doc-pill">{{ f.cliente?.documento }}</span>
                  </td>
                  <td class="der cell-monto">
                    <strong>{{ fmtUsd(f.totalNeto) }}</strong>
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
                    <button class="btn-cobrar" type="button" @click="abrirCobroFactura(f)">
                      <Banknote :size="14" />
                      Cobrar
                    </button>
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
      <div v-if="errorVerificacion" class="alert alert-error">
        <AlertCircle :size="16" />
        <span>{{ errorVerificacion }}</span>
        <button type="button" class="alert-action" @click="cargarPagosPorVerificar">
          Reintentar
        </button>
      </div>

      <div v-if="cargandoVerificacion" class="skeleton-list">
        <div v-for="i in 3" :key="i" class="skeleton-card">
          <div class="skeleton-block skeleton-fecha" />
          <div class="skeleton-info">
            <div class="skeleton-block w-60" />
            <div class="skeleton-block w-40" />
          </div>
          <div class="skeleton-block skeleton-monto" />
        </div>
      </div>

      <div v-else-if="pagosPorVerificar.length === 0" class="empty-state">
        <div class="empty-icon" style="background: #FFFBEB; color: #D97706;">
          <ShieldCheck :size="32" />
        </div>
        <h3>No hay pagos pendientes de verificación</h3>
        <p>
          Todos los pagos online están verificados. Los nuevos aparecerán aquí
          automáticamente tras el pago del cliente.
        </p>
        <button class="btn-secondary" type="button" @click="actualizar">
          <RefreshCw :size="15" /> Actualizar
        </button>
      </div>

      <div v-else class="verificacion-list">
        <article v-for="p in pagosPorVerificar" :key="p.idPago" class="verif-card">
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
              <strong class="verif-monto-valor">{{ fmtUsd(p.monto) }}</strong>
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
            <button class="btn-rechazar" type="button" @click="abrirVerificacion(p, false)">
              <X :size="14" /> Rechazar
            </button>
            <button class="btn-aprobar" type="button" @click="abrirVerificacion(p, true)">
              <CheckCircle2 :size="14" /> Confirmar pago
            </button>
          </footer>
        </article>
      </div>
    </template>

    <!-- ══════════════ MODAL DE COBRO ══════════════ -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="citaACobrar"
          class="modal-overlay"
          @click.self="!cobrando && (citaACobrar = null)"
        >
          <Transition name="slide-up">
            <div v-if="citaACobrar" class="cobro-modal">
              <header class="cobro-modal-header">
                <div class="cobro-modal-titles">
                  <span class="cobro-modal-eyebrow">
                    <Banknote :size="12" />
                    Cobro de factura
                  </span>
                  <h3>{{ citaACobrar.nombreCliente }}</h3>
                </div>
                <button
                  class="cobro-modal-close"
                  type="button"
                  :disabled="cobrando"
                  aria-label="Cerrar"
                  @click="citaACobrar = null"
                >
                  <X :size="17" />
                </button>
              </header>

              <div class="cobro-modal-body">
                <div v-if="cobroError" class="alert alert-error" style="margin-top: 0;">
                  <AlertTriangle :size="16" />
                  <span>{{ cobroError }}</span>
                </div>

                <div class="cobro-modal-info">
                  <div>
                    <div class="info-row">
                      <span class="info-label"><CreditCard :size="13" /> Documento</span>
                      <span class="info-value mono">{{ citaACobrar.documentoCliente }}</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label"><Package :size="13" /> Productos</span>
                      <span class="info-value">{{ citaACobrar.productosResumen }}</span>
                    </div>
                    <div class="info-row">
                      <span class="info-label"><CalendarDays :size="13" /> Emisión</span>
                      <span class="info-value">{{ fmtFecha(citaACobrar.fecha) }}</span>
                    </div>
                  </div>
                </div>

                <div class="cobro-modal-total">
                  <div class="total-left">
                    <span class="total-label">Total a cobrar</span>
                    <span class="total-hint">Bs. se calcula con la tasa oficial al confirmar</span>
                  </div>
                  <div class="total-right">
                    <span class="total-monto">{{ fmtUsd(citaACobrar.costoUsd) }}</span>
                    <span class="total-currency">USD</span>
                  </div>
                </div>

                <p class="cobro-modal-section">
                  Método de pago <span class="required">*</span>
                </p>
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
                <span v-if="stepErrors.metodo" class="form-error mt-8">{{ stepErrors.metodo }}</span>

                <Transition name="expand">
                  <div v-if="metodoSeleccionado" class="dynamic-fields">
                    <p class="cobro-modal-section">Datos del pago</p>

                    <div v-if="'referencia' in camposActuales" class="form-group">
                      <label class="form-label">
                        Número de referencia <span class="required">*</span>
                      </label>
                      <input
                        v-model="referencia"
                        type="text"
                        class="form-input"
                        :class="{ 'is-invalid': stepErrors.referencia }"
                        placeholder="Ej: 0000123456789"
                      />
                      <span v-if="stepErrors.referencia" class="form-error">
                        {{ stepErrors.referencia }}
                      </span>
                    </div>

                    <div v-for="campo in camposDinamicos" :key="campo.key" class="form-group">
                      <label class="form-label">
                        {{ fmtFieldLabel(campo.key) }} <span class="required">*</span>
                      </label>
                      <select
                        v-if="campo.key === 'banco'"
                        v-model="datosPago[campo.key]"
                        class="form-select"
                        :class="{ 'is-invalid': stepErrors[campo.key] }"
                      >
                        <option value="" disabled>Seleccione el banco emisor</option>
                        <option v-for="b in BANCOS_VENEZUELA" :key="b.codigo" :value="b.nombre">
                          {{ b.codigo }} - {{ b.nombre }}
                        </option>
                      </select>
                      <input
                        v-else
                        v-model="datosPago[campo.key]"
                        type="text"
                        class="form-input"
                        :class="{ 'is-invalid': stepErrors[campo.key] }"
                      />
                      <span v-if="stepErrors[campo.key]" class="form-error">
                        {{ stepErrors[campo.key] }}
                      </span>
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

              <footer class="cobro-modal-footer">
                <button
                  class="btn-secondary"
                  type="button"
                  :disabled="cobrando"
                  @click="citaACobrar = null"
                >
                  Cancelar
                </button>
                <button
                  class="btn-primary"
                  type="button"
                  :disabled="!selectedMetodo || cobrando"
                  @click="confirmarCobro"
                >
                  <Loader2 v-if="cobrando" :size="15" class="spin" />
                  <Banknote v-else :size="15" />
                  {{ cobrando ? 'Procesando…' : 'Cobrar factura' }}
                </button>
              </footer>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <!-- ══════════════ MODAL VERIFICACIÓN ══════════════ -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="verificacionModal"
          class="modal-overlay"
          @click.self="!verificandoPago && (verificacionModal = false)"
        >
          <Transition name="slide-up">
            <div v-if="verificacionModal" class="cobro-modal" style="max-width: 520px;">
              <header class="cobro-modal-header">
                <div class="cobro-modal-titles">
                  <span class="cobro-modal-eyebrow">
                    <ShieldCheck :size="12" />
                    Verificación de pago
                  </span>
                  <h3>
                    {{ verificacionAprobada ? 'Confirmar pago' : 'Rechazar pago' }}
                  </h3>
                </div>
                <button
                  class="cobro-modal-close"
                  type="button"
                  :disabled="verificandoPago"
                  @click="verificacionModal = false"
                >
                  <X :size="17" />
                </button>
              </header>

              <div class="cobro-modal-body">
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
                  <textarea
                    v-model="observacionesVerif"
                    rows="3"
                    class="form-textarea"
                    placeholder="Ej: Verificado en el banco. Transacción confirmada."
                  ></textarea>
                </div>

                <div v-if="errorVerifModal" class="alert alert-error">
                  <AlertTriangle :size="16" />
                  <span>{{ errorVerifModal }}</span>
                </div>
              </div>

              <footer class="cobro-modal-footer">
                <button
                  class="btn-secondary"
                  type="button"
                  :disabled="verificandoPago"
                  @click="verificacionModal = false"
                >
                  Cancelar
                </button>
                <button
                  class="btn-primary"
                  :style="!verificacionAprobada ? 'background:#DC2626' : ''"
                  type="button"
                  :disabled="verificandoPago"
                  @click="confirmarVerificacion"
                >
                  <Loader2 v-if="verificandoPago" :size="15" class="spin" />
                  <component :is="verificacionAprobada ? CheckCircle2 : X" v-else :size="15" />
                  {{ verificandoPago
                    ? 'Procesando…'
                    : (verificacionAprobada ? 'Sí, confirmar' : 'Sí, rechazar') }}
                </button>
              </footer>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <!-- ══════════════ COMPROBANTE ══════════════ -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="resultado" class="modal-overlay">
          <Transition name="slide-up">
            <div v-if="resultado" class="modal-card modal-success">
              <div class="success-body">
                <div class="success-icon">
                  <CheckCircle2 :size="40" />
                </div>

                <h2 class="success-title">Factura cobrada correctamente</h2>

                <p v-if="resultado.mensaje" class="success-message">
                  {{ resultado.mensaje }}
                </p>

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
                      <span class="pill-success">{{ resultado.estadoPago || 'Confirmado' }}</span>
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

                <div
                  v-if="envio"
                  class="alert"
                  :class="envio.enviado ? 'alert-success' : 'alert-warning'"
                >
                  <CheckCircle2 v-if="envio.enviado" :size="16" />
                  <AlertTriangle v-else :size="16" />
                  <span>{{ envio.mensaje }}</span>
                </div>

                <div class="success-actions">
                  <button
                    class="btn-secondary"
                    type="button"
                    :disabled="imprimiendo"
                    @click="imprimirComprobante"
                  >
                    <Loader2 v-if="imprimiendo" :size="15" class="spin" />
                    <Printer v-else :size="15" />
                    {{ imprimiendo ? 'Imprimiendo…' : 'Imprimir' }}
                  </button>
                  <button
                    class="btn-secondary"
                    type="button"
                    :disabled="enviando"
                    @click="enviarPorCorreo"
                  >
                    <Loader2 v-if="enviando" :size="15" class="spin" />
                    <Mail v-else :size="15" />
                    {{ enviando ? 'Enviando…' : 'Enviar por correo' }}
                  </button>
                  <button class="btn-primary" type="button" @click="cerrarComprobante">
                    Volver a la caja
                  </button>
                </div>
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
import {
  getMetodosPresenciales, enviarFactura, descargarFactura,
  getFacturasPendientes, cobrarFactura,
  getPagosPendientesVerificacion, verificarPago,
} from '@/api/pagos.api'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import {
  Banknote, Loader2, AlertCircle, AlertTriangle, CheckCircle2,
  Printer, Mail, X, RefreshCw, PawPrint, CalendarDays, CreditCard,
  Receipt, ChevronDown, ChevronUp, Sparkles, DollarSign, User,
  FileText, Package, Check,
  ShieldCheck, Landmark, Smartphone,
} from 'lucide-vue-next'

const BANCOS_VENEZUELA = [
  { codigo: '0102', nombre: 'Banco de Venezuela, S.A.C.A.' },
  { codigo: '0104', nombre: 'Venezolano de Crédito, S.A.' },
  { codigo: '0105', nombre: 'Mercantil Banco, C.A.' },
  { codigo: '0108', nombre: 'Banco Provincial, S.A.' },
  { codigo: '0114', nombre: 'Banco del Caribe, C.A.' },
  { codigo: '0134', nombre: 'Banesco Banco Universal, C.A.' },
  { codigo: '0151', nombre: 'Banco Fondo Común, C.A.' },
  { codigo: '0163', nombre: 'Banco del Tesoro, C.A.' },
  { codigo: '0177', nombre: 'BANFANB' },
  { codigo: '0190', nombre: 'Banco Nacional de Crédito, C.A.' },
]
const METODO_LABEL = {
  Efectivo: 'Efectivo',
  Tarjeta: 'Tarjeta (Punto de Venta)',
  Pago_Movil: 'Pago Móvil',
  Transferencia: 'Transferencia bancaria',
}

const MESES = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']

// ─── Pestañas ───
const tab = ref('facturas')

// ─── Lista facturas ───
const facturas = ref([])
const cargandoFacturas = ref(false)
const errorFacturas = ref('')
const facturaExpandida = ref(null)

async function cargarFacturas() {
  cargandoFacturas.value = true
  errorFacturas.value = ''
  try {
    facturas.value = await getFacturasPendientes()
    facturaExpandida.value = null
  } catch (err) {
    const msg = err.response?.data?.message
    errorFacturas.value = typeof msg === 'string' ? msg : 'No se pudo cargar las facturas pendientes.'
  } finally {
    cargandoFacturas.value = false
  }
}

function alternarDetalles(idFactura) {
  facturaExpandida.value = facturaExpandida.value === idFactura ? null : idFactura
}

// ─── Pagos por verificar ───
const pagosPorVerificar = ref([])
const cargandoVerificacion = ref(false)
const errorVerificacion = ref('')

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

// ─── Modal verificación ───
const verificacionModal = ref(false)
const pagoAVerificar = ref(null)
const verificacionAprobada = ref(true)
const observacionesVerif = ref('')
const verificandoPago = ref(false)
const errorVerifModal = ref('')

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

function iconoMetodoVerif(nombre) {
  if (nombre === 'Transferencia') return Landmark
  if (nombre === 'Pago_Movil') return Smartphone
  return CreditCard
}

function fmtMetodoNombre(nombre) {
  if (!nombre) return 'Pago online'
  const map = {
    Transferencia: 'Transferencia bancaria',
    Pago_Movil: 'Pago Móvil',
    Efectivo: 'Efectivo',
    Tarjeta: 'Tarjeta',
  }
  return map[nombre] || nombre
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

function fmtFechaHora(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return '—'
  return d.toLocaleString('es-VE', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function actualizar() {
  if (tab.value === 'verificacion') cargarPagosPorVerificar()
  else cargarFacturas()
}

// ─── KPIs ───
const totalPorCobrar = computed(() => {
  return facturas.value.reduce((s, f) => s + Number(f.totalNeto || 0), 0)
})

// ─── Modal de cobro ───
const citaACobrar = ref(null)
const tipoCobro = ref('factura')
const metodos = ref([])
const selectedMetodo = ref(null)
const datosPago = ref({})
const referencia = ref('')
const fondosConfirmados = ref(false)
const cobrando = ref(false)
const cobroError = ref('')
const stepErrors = ref({})

// ─── Comprobante ───
const resultado = ref(null)
const envio = ref(null)
const enviando = ref(false)
const imprimiendo = ref(false)

const metodoSeleccionado = computed(() =>
  metodos.value.find(m => m.id === selectedMetodo.value) || null
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

function abrirCobroFactura(factura) {
  tipoCobro.value = 'factura'
  citaACobrar.value = {
    idFactura: factura.idFactura,
    nombreCliente: factura.cliente?.nombre || '—',
    documentoCliente: factura.cliente?.documento || '—',
    productosResumen: `${factura.detalles?.length || 0} producto(s)`,
    fecha: factura.fechaEmision,
    costoUsd: factura.totalNeto,
  }
  selectedMetodo.value = null
  datosPago.value = {}
  referencia.value = ''
  fondosConfirmados.value = false
  cobroError.value = ''
  stepErrors.value = {}
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

    const data = await cobrarFactura(citaACobrar.value.idFactura, payload)
    resultado.value = {
      tipo: 'factura',
      idFactura: data.idFactura,
      numeroControl: data.numeroControl,
      estadoPago: data.estadoPago,
      mensaje: data.mensaje,
      resumen: {
        cliente: citaACobrar.value.nombreCliente,
        metodoPago: data.metodoPago || metodoSeleccionado.value?.nombre,
        costoUsd: data.monto,
        costoBs: null,
      },
    }
    citaACobrar.value = null
    envio.value = null
  } catch (err) {
    const msgRaw = err.response?.data?.message
    const msg = typeof msgRaw === 'string'
      ? msgRaw
      : (msgRaw ? Object.values(msgRaw).join(' · ') : '')

    if (err.response?.status === 409) {
      citaACobrar.value = null
      cobroError.value = msg || 'Esta factura ya tiene un pago registrado. La lista fue actualizada.'
      await cargarFacturas()
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
  } catch {
    /* reintentable */
  } finally {
    imprimiendo.value = false
  }
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

// ─── Helpers de formato ───
function fmtFecha(iso) {
  if (!iso) return ''
  const [y, m, d] = String(iso).slice(0, 10).split('-')
  return `${d} ${MESES[Number(m) - 1]} ${y}`
}

function fmtUsd(v) {
  if (v == null) return '$0.00'
  return `$${Number(v).toFixed(2)}`
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

onMounted(async () => {
  cargarFacturas()
  cargarPagosPorVerificar()
  try {
    const { data } = await getMetodosPresenciales()
    metodos.value = data
  } catch {
    /* vacío */
  }
})
</script>

<style scoped>
.cobrar-mostrador {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 24px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
button { font-family: inherit; }

/* ═══ HERO ═══ */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  padding: 24px 28px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
}
.hero-left { min-width: 0; }
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
.hero-right { display: flex; align-items: center; }

/* ═══ KPIs ═══ */
.kpis {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
.kpi {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  transition: border-color .2s ease, box-shadow .2s ease, transform .2s ease;
}
.kpi:hover {
  border-color: #CBD5E1;
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -10px rgba(15, 23, 42, .08);
}
.kpi-icon {
  width: 42px;
  height: 42px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--kpi-bg);
  color: var(--kpi-color);
}
.kpi-texto { min-width: 0; }
.kpi-value {
  margin: 0;
  font-size: 21px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.1;
  letter-spacing: -0.01em;
}
.kpi-value.is-warn { color: #D97706; }
.kpi-label {
  margin: 3px 0 0;
  font-size: 12px;
  color: #64748B;
  font-weight: 500;
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
.alert-action {
  margin-left: auto;
  background: none;
  border: none;
  color: inherit;
  font-weight: 700;
  font-size: 12px;
  cursor: pointer;
  text-decoration: underline;
  white-space: nowrap;
}

/* ═══ TABS ═══ */
.tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.tab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 16px;
  border-radius: 20px;
  border: 1.5px solid #E2E8F0;
  background: #fff;
  color: #64748B;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all .2s;
}
.tab:hover { border-color: #99F6E4; color: #0F766E; }
.tab.active {
  background: #0F766E;
  border-color: #0F766E;
  color: #fff;
  box-shadow: 0 4px 12px rgba(15, 118, 110, .25);
}
.tab-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 20px;
  background: #F1F5F9;
  color: #64748B;
  font-size: 11px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}
.tab.active .tab-count {
  background: rgba(255, 255, 255, .22);
  color: #fff;
}
.tab-count.is-warn {
  background: #FEF2F2;
  color: #DC2626;
  border: 1px solid #FECACA;
}
.tab.active .tab-count.is-warn {
  background: rgba(255, 255, 255, .22);
  color: #fff;
  border-color: rgba(255, 255, 255, .4);
}
.skeleton-card {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 18px;
  align-items: center;
  padding: 18px 20px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
}
.skeleton-block {
  border-radius: 8px;
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
.skeleton-fecha { width: 64px; height: 76px; border-radius: 12px; }
.skeleton-info { display: flex; flex-direction: column; gap: 8px; }
.skeleton-monto { width: 90px; height: 24px; border-radius: 6px; }
.w-60 { height: 14px; width: 60%; }
.w-40 { height: 12px; width: 40%; }
.w-30 { height: 11px; width: 30%; }
.w-25 { height: 12px; width: 25%; }
.w-15 { height: 12px; width: 15%; }
.w-20 { height: 12px; width: 20%; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ═══ EMPTY STATE ═══ */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 64px 24px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03);
  text-align: center;
}
.empty-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #F0FDFA;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}
.empty-state h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0F172A;
}
.empty-state p {
  margin: 0 0 10px;
  font-size: 13.5px;
  color: #64748B;
  max-width: 460px;
  line-height: 1.5;
}

/* ═══ TABLA DE FACTURAS ═══ */
.table-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  overflow: hidden;
}
.table-wrap { overflow-x: auto; }
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-family: inherit;
  min-width: 780px;
}
.data-table thead { background: #F8FAFC; }
.data-table th {
  text-align: left;
  padding: 12px 16px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #64748B;
  border-bottom: 1px solid #E2E8F0;
  white-space: nowrap;
}
.data-table td {
  padding: 14px 16px;
  font-size: 13.5px;
  color: #1E293B;
  border-bottom: 1px solid #F1F5F9;
  vertical-align: middle;
}
.fila-factura:hover td { background: #FAFBFC; }
.der { text-align: right; }
.centro { text-align: center; }
.amount { font-weight: 700; color: #0F766E; }
.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12.5px;
  font-weight: 700;
  color: #334155;
  background: #F1F5F9;
  padding: 3px 10px;
  border-radius: 6px;
  display: inline-block;
}
.cell-fecha {
  color: #64748B;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.cell-cliente {
  font-weight: 600;
  color: #0F172A;
}
.doc-pill {
  display: inline-block;
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12px;
  color: #475569;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  padding: 2px 8px;
  border-radius: 5px;
}
.cell-monto strong {
  font-size: 14px;
  font-weight: 700;
  color: #0F766E;
  letter-spacing: -0.01px;
}
.btn-expand {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  border: 1px solid #E2E8F0;
  background: #fff;
  color: #64748B;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-family: inherit;
  transition: all .2s;
}
.btn-expand:hover {
  border-color: #0F766E;
  color: #0F766E;
  background: #F0FDFA;
}

/* Detalles expandidos */
.detalles-row td {
  background: #F0FDFA;
  padding: 0;
  border-bottom: 1px solid #99F6E4;
}
.detalles-wrap { padding: 16px 20px 20px; }
.detalles-titulo {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 12px;
  font-size: 11.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #0F766E;
}
.data-table.inner {
  background: #fff;
  border: 1px solid #CCFBF1;
  border-radius: 10px;
  overflow: hidden;
  min-width: 0;
}
.data-table.inner thead { background: #fff; }
.data-table.inner th {
  padding: 8px 14px;
  font-size: 10.5px;
  border-bottom: 1px solid #E2E8F0;
}
.data-table.inner td {
  padding: 9px 14px;
  font-size: 12.5px;
  border-bottom: 1px solid #F1F5F9;
}
.data-table.inner tbody tr:last-child td { border-bottom: none; }

/* ═══ VERIFICACIÓN DE PAGOS ═══ */
.verificacion-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.verif-card {
  background: #fff;
  border: 1px solid #FDE68A;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .04);
  transition: box-shadow .2s, transform .2s;
}
.verif-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px -12px rgba(245, 158, 11, .25);
}

.verif-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  background: linear-gradient(90deg, #FFFBEB 0%, #FFFFFF 60%);
  border-bottom: 1px solid #FEF3C7;
  flex-wrap: wrap;
}
.verif-metodo { display: flex; align-items: center; gap: 12px; min-width: 0; }
.verif-icon {
  width: 42px;
  height: 42px;
  border-radius: 11px;
  background: #fff;
  border: 1px solid #FDE68A;
  color: #B45309;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.verif-metodo-nombre {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #1E293B;
}
.verif-metodo-tipo {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748B;
}
.verif-monto {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}
.verif-monto-label {
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #94A3B8;
}
.verif-monto-valor {
  font-size: 20px;
  font-weight: 700;
  color: #0F766E;
  letter-spacing: -0.02em;
}

.verif-body {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #F1F5F9;
}
.verif-col { min-width: 0; }
.verif-label {
  margin: 0 0 4px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #94A3B8;
}
.verif-valor {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: #1E293B;
  word-break: break-word;
}
.verif-meta {
  margin: 3px 0 0;
  font-size: 12px;
  color: #64748B;
}

.verif-transaccion {
  padding: 16px 20px;
  background: #F8FAFC;
  border-bottom: 1px solid #E2E8F0;
}
.verif-transaccion-titulo {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 10px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #0F766E;
}
.verif-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
}
.verif-field {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.verif-field-label {
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .4px;
  color: #94A3B8;
}
.verif-field-valor {
  font-size: 13px;
  font-weight: 700;
  color: #1E293B;
  word-break: break-word;
}
.verif-field-valor.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12.5px;
  color: #334155;
}

.verif-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 20px;
  background: #FAFBFC;
}

.btn-aprobar,
.btn-rechazar {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  border-radius: 9px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: all .15s;
  border: none;
}
.btn-aprobar {
  background: #0F766E;
  color: #fff;
}
.btn-aprobar:hover {
  background: #0E6862;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-rechazar {
  background: #fff;
  color: #DC2626;
  border: 1px solid #FECACA;
}
.btn-rechazar:hover {
  background: #FEF2F2;
  border-color: #FCA5A5;
}

/* ═══ MODAL COBRO ═══ */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, .55);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 1000;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

.cobro-modal {
  background: #fff;
  border-radius: 18px;
  width: 100%;
  max-width: 620px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .3);
}

.cobro-modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px 18px;
  border-bottom: 1px solid #E2E8F0;
  flex-shrink: 0;
}
.cobro-modal-titles { min-width: 0; }
.cobro-modal-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .7px;
  color: #0F766E;
  margin-bottom: 6px;
}
.cobro-modal-titles h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cobro-modal-close {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  border: 1px solid #E2E8F0;
  background: #fff;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .15s ease;
  flex-shrink: 0;
}
.cobro-modal-close:hover:not(:disabled) {
  background: #F8FAFC;
  border-color: #CBD5E1;
  color: #1E293B;
}
.cobro-modal-close:disabled { opacity: .5; cursor: not-allowed; }

.cobro-modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  scrollbar-width: thin;
  scrollbar-color: #CBD5E1 transparent;
}
.cobro-modal-body::-webkit-scrollbar { width: 6px; }
.cobro-modal-body::-webkit-scrollbar-track { background: transparent; }
.cobro-modal-body::-webkit-scrollbar-thumb {
  background: #CBD5E1;
  border-radius: 3px;
}
.cobro-modal-body::-webkit-scrollbar-thumb:hover { background: #94A3B8; }

.cobro-modal-info {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 6px 16px;
}
.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 10px 0;
  border-bottom: 1px solid #E2E8F0;
  font-size: 13px;
}
.info-row:last-child { border-bottom: none; }
.info-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #64748B;
  font-weight: 600;
  font-size: 12.5px;
  white-space: nowrap;
  flex-shrink: 0;
}
.info-label svg { color: #94A3B8; }
.info-value {
  color: #0F172A;
  font-weight: 700;
  text-align: right;
  word-break: break-word;
  min-width: 0;
}
.info-value.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12.5px;
  background: #F1F5F9;
  padding: 2px 8px;
  border-radius: 5px;
}

.cobro-modal-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 100%);
  border: 1px solid #99F6E4;
  border-radius: 12px;
}
.total-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.total-label {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #0F766E;
}
.total-hint {
  font-size: 11px;
  color: #64748B;
  line-height: 1.4;
}
.total-right {
  display: flex;
  align-items: baseline;
  gap: 5px;
  flex-shrink: 0;
}
.total-monto {
  font-size: 24px;
  font-weight: 700;
  color: #0F766E;
  letter-spacing: -0.02em;
  line-height: 1;
}
.total-currency {
  font-size: 12px;
  font-weight: 700;
  color: #0F766E;
  letter-spacing: .5px;
}

.cobro-modal-section {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
}
.cobro-modal-section .required { color: #EF4444; }

/* Métodos de pago */
.methods-list { display: flex; flex-direction: column; gap: 8px; }
.method-option {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  cursor: pointer;
  transition: all .2s ease;
  background: #fff;
  font-family: inherit;
  text-align: left;
  width: 100%;
}
.method-option:hover { border-color: #99F6E4; background: #F0FDFA; }
.method-option.active {
  border-color: #0F766E;
  background: #F0FDFA;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.method-radio { flex-shrink: 0; }
.radio-outer {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #CBD5E1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all .2s;
}
.radio-outer.checked { border-color: #0F766E; }
.radio-inner {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #0F766E;
  animation: radioPop .2s ease;
}
@keyframes radioPop {
  0% { transform: scale(0); }
  100% { transform: scale(1); }
}
.method-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.method-name { font-size: 13.5px; font-weight: 700; color: #0F172A; }
.method-desc { font-size: 12px; color: #64748B; }

/* Campos dinámicos */
.dynamic-fields {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding-top: 16px;
  border-top: 1px solid #F1F5F9;
}
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 600;
  color: #374151;
}
.required { color: #EF4444; }
.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  font-size: 13.5px;
  color: #1E293B;
  background: #fff;
  font-family: inherit;
  transition: border-color .2s, box-shadow .2s;
  box-sizing: border-box;
  appearance: none;
  -webkit-appearance: none;
}
.form-textarea {
  resize: vertical;
  min-height: 72px;
  line-height: 1.5;
}
.form-select {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  padding-right: 40px;
  cursor: pointer;
}
.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.is-invalid { border-color: #EF4444 !important; background: #FEF2F2 !important; }
.form-error { font-size: 12px; color: #EF4444; font-weight: 600; }
.form-error.mt-8 { display: block; margin-top: 10px; }

/* Checkbox */
.check-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #F8FAFC;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  cursor: pointer;
  font-family: inherit;
  transition: all .2s ease;
  margin: 0;
}
.check-row:hover { border-color: #99F6E4; background: #F0FDFA; }
.check-row.is-invalid { border-color: #EF4444; background: #FEF2F2; }
.checkbox-input { display: none; }
.checkbox-box {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  border: 2px solid #CBD5E1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  flex-shrink: 0;
  transition: all .2s ease;
  color: #fff;
}
.check-row .checkbox-input:checked + .checkbox-box {
  background: #0F766E;
  border-color: #0F766E;
}
.checkbox-label {
  font-size: 13px;
  font-weight: 600;
  color: #1E293B;
}

.cobro-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid #E2E8F0;
  background: #FAFBFC;
  flex-shrink: 0;
}

/* ─── Verif modal texto ─── */
.verif-modal-texto {
  margin: 0 0 16px;
  font-size: 13.5px;
  color: #334155;
  line-height: 1.55;
}
.verif-modal-texto strong { color: #0F172A; }

/* ═══ BOTONES GENERALES ═══ */
.btn-primary,
.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
}
.btn-primary {
  background: #0F766E;
  color: #fff;
  border: none;
}
.btn-primary:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-primary:disabled { opacity: .55; cursor: not-allowed; }
.btn-secondary {
  background: #fff;
  color: #475569;
  border: 1px solid #E2E8F0;
}
.btn-secondary:hover:not(:disabled) {
  background: #F8FAFC;
  border-color: #CBD5E1;
  color: #0F766E;
}
.btn-secondary:disabled { opacity: .55; cursor: not-allowed; }

/* ═══ COMPROBANTE ═══ */
.modal-card {
  background: #fff;
  border-radius: 18px;
  width: 100%;
  max-width: 520px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, .3);
}
.modal-success { max-width: 520px; }
.success-body {
  padding: 32px 28px 26px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
}
.success-icon {
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDFA 100%);
  border: 3px solid #10B981;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #059669;
  margin-bottom: 4px;
  animation: successPop .5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}
.success-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
}
.success-message {
  margin: 0;
  font-size: 13.5px;
  color: #64748B;
  line-height: 1.55;
  max-width: 420px;
}
.cobro-resumen {
  width: 100%;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.resumen-fila {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  font-size: 13px;
  padding: 4px 0;
  border-bottom: 1px dashed #F1F5F9;
}
.resumen-fila:last-child { border-bottom: none; }
.resumen-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #64748B;
  font-weight: 600;
}
.resumen-label svg { color: #94A3B8; }
.resumen-value {
  color: #0F172A;
  font-weight: 700;
  text-align: right;
  word-break: break-word;
}
.resumen-value.mono {
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
  font-size: 12.5px;
  background: #F1F5F9;
  padding: 2px 8px;
  border-radius: 5px;
}
.resumen-fila.total {
  padding-top: 10px;
  border-top: 1px solid #E2E8F0;
  margin-top: 6px;
}
.total-value {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  color: #0F766E;
  font-size: 15px;
}
.total-bs {
  font-size: 11.5px;
  color: #64748B;
  font-weight: 600;
}
.pill-success {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 20px;
  background: #ECFDF5;
  color: #059669;
  border: 1px solid #A7F3D0;
  font-size: 11.5px;
  font-weight: 700;
}
.success-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  width: 100%;
  flex-wrap: wrap;
  justify-content: center;
}

/* ═══ SPIN ═══ */
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══ TRANSICIONES ═══ */
.fade-enter-active,
.fade-leave-active { transition: opacity .25s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }

.slide-up-enter-active,
.slide-up-leave-active {
  transition: opacity .3s cubic-bezier(0.16, 1, 0.3, 1),
              transform .3s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(.98);
}

.expand-enter-active,
.expand-leave-active {
  transition: opacity .25s ease, transform .25s ease;
  overflow: hidden;
}
.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .kpis { grid-template-columns: repeat(2, 1fr); }
  .verif-body { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 768px) {
  .cobrar-mostrador { padding: 16px 16px 40px; }
  .hero { padding: 20px; border-radius: 14px; }
  .hero h1 { font-size: 22px; }
  .kpis { grid-template-columns: 1fr; gap: 10px; }
  .kpi { padding: 14px 16px; }

  .verif-body { grid-template-columns: 1fr; gap: 12px; }
  .verif-actions { flex-direction: column-reverse; }
  .verif-actions button { width: 100%; justify-content: center; }
  .verif-header { flex-direction: column; align-items: stretch; gap: 12px; }
  .verif-monto { align-items: flex-start; }

  .modal-card { max-width: 100%; }
  .cobro-modal-footer { flex-direction: column-reverse; }
  .cobro-modal-footer .btn-primary,
  .cobro-modal-footer .btn-secondary { width: 100%; }
  .total-monto { font-size: 21px; }
  .info-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 3px;
  }
  .info-value { text-align: left; }

  .success-actions { flex-direction: column; }
  .success-actions .btn-primary,
  .success-actions .btn-secondary { width: 100%; }
}
@media (max-width: 480px) {
  .success-body { padding: 24px 20px 20px; }
  .success-icon { width: 64px; height: 64px; }
  .success-title { font-size: 18px; }
  .resumen-fila {
    flex-direction: column;
    align-items: flex-start;
    gap: 3px;
  }
  .resumen-value { text-align: left; }
  .total-value { align-items: flex-start; }
}

</style>