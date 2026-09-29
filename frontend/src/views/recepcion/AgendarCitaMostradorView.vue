<template>
  <div class="agendar-mostrador">
    <!-- ═══ BREADCRUMB ═══ -->
    <nav class="breadcrumb" aria-label="Migas de pan">
      <button class="bc-back" type="button" @click="volverAlCalendario">
        <ArrowLeft :size="15" />
      </button>
      <button class="bc-item bc-link" type="button" @click="volverAlCalendario">
        Gestión de Citas
      </button>
      <ChevronRight :size="14" class="bc-sep" />
      <span class="bc-item bc-current">Nueva Reserva</span>
    </nav>

    <!-- ═══ HERO ═══ -->
    <header class="wizard-hero">
      <p class="hero-eyebrow">
        <Sparkles :size="12" />
        Recepción · Mostrador
      </p>
      <h1>Nueva Reserva</h1>
      <p class="hero-sub">Agenda y cobra una cita presencial en un solo flujo.</p>
    </header>

    <!-- ═══ STEPPER ═══ -->
    <nav class="stepper" aria-label="Progreso">
      <div
        v-for="(s, i) in steps"
        :key="s.num"
        class="stepper-step"
        :class="{ active: currentStep === s.num, completed: currentStep > s.num }"
      >
        <div class="stepper-circle">
          <CheckCircle2 v-if="currentStep > s.num" :size="15" />
          <span v-else>{{ s.num }}</span>
        </div>
        <div class="stepper-text">
          <span class="stepper-label">{{ s.label }}</span>
          <span class="stepper-desc">{{ s.desc }}</span>
        </div>
        <div v-if="i < steps.length - 1" class="stepper-line" />
      </div>
    </nav>

    <!-- ═══ LAYOUT ═══ -->
    <div class="wizard-layout" :class="{ 'is-success': currentStep === 4 }">
      <main class="wizard-main">
        <Transition name="step-fade" mode="out-in">
          <!-- ══════ STEP 1: CLIENTE + MASCOTA + MOTIVO ══════ -->
          <section v-if="currentStep === 1" key="step1" class="step-panel">
            <!-- Card: Cliente -->
            <div class="card">
              <div class="card-header">
                <div class="card-header-left">
                  <div class="card-icon"><UserPlus :size="16" /></div>
                  <div>
                    <h3>Cliente</h3>
                    <p class="card-header-sub">Busca por nombre o documento de identidad</p>
                  </div>
                </div>
              </div>
              <div class="card-body">
                <!-- Sin cliente seleccionado -->
                <template v-if="!clienteSeleccionado">
                  <div class="search-box">
                    <Search :size="15" class="search-icon" />
                    <input
                      v-model="clienteFiltro"
                      type="text"
                      class="search-input"
                      placeholder="Ej: María González o V-12345678…"
                      autocomplete="off"
                    />
                    <Loader2 v-if="buscandoCliente" :size="14" class="search-spinner spin" />
                    <button
                      v-else-if="clienteFiltro"
                      type="button"
                      class="search-clear"
                      aria-label="Limpiar"
                      @click="clienteFiltro = ''"
                    >
                      <X :size="13" />
                    </button>
                  </div>

                  <div
                    v-if="clienteFiltro.length >= 2 && !buscandoCliente && resultadosClientes.length === 0"
                    class="empty-inline"
                  >
                    <AlertTriangle :size="24" />
                    <p>
                      No se encontraron resultados. Verifica los datos o
                      <router-link to="/recepcion/clientes/nuevo" class="link-teal">
                        registra previamente al cliente
                      </router-link>.
                    </p>
                  </div>

                  <div v-if="resultadosClientes.length > 0" class="results-list">
                    <button
                      v-for="c in resultadosClientes"
                      :key="c.id"
                      type="button"
                      class="result-row"
                      @click="seleccionarCliente(c)"
                    >
                      <EntityAvatar :nombre="c.nombreCompleto" tipo="cliente" size="sm" />
                      <div class="result-info">
                        <p class="result-name">{{ c.nombreCompleto }}</p>
                        <p class="result-meta">
                          {{ c.documentoIdentidad }} · {{ c.telefonoPrincipal }}
                        </p>
                      </div>
                      <ChevronRight :size="16" class="result-arrow" />
                    </button>
                  </div>
                </template>

                <!-- Cliente seleccionado -->
                <div v-else class="cliente-chip">
                  <EntityAvatar :nombre="clienteSeleccionado.nombreCompleto" tipo="cliente" size="md" />
                  <div class="cliente-chip-info">
                    <p class="cliente-chip-name">{{ clienteSeleccionado.nombreCompleto }}</p>
                    <p class="cliente-chip-doc">{{ clienteSeleccionado.documentoIdentidad }}</p>
                  </div>
                  <button type="button" class="btn-chip" @click="cambiarCliente">
                    <X :size="13" /> Cambiar
                  </button>
                </div>
                <span v-if="step1Errors.cliente" class="form-error">{{ step1Errors.cliente }}</span>
              </div>
            </div>

            <!-- Card: Mascota -->
            <div v-if="clienteSeleccionado" class="card">
              <div class="card-header">
                <div class="card-header-left">
                  <div class="card-icon"><PawPrint :size="16" /></div>
                  <div>
                    <h3>Mascota</h3>
                    <p class="card-header-sub">¿Qué paciente vas a atender?</p>
                  </div>
                </div>
              </div>
              <div class="card-body">
                <div v-if="cargandoMascotas" class="mini-load">
                  <Loader2 :size="14" class="spin" /> Cargando mascotas…
                </div>

                <template v-else-if="mascotas.length > 0">
                  <div class="pick-grid" :class="{ 'with-error': step1Errors.mascota }">
                    <button
                      v-for="m in mascotas"
                      :key="m.idMascota"
                      type="button"
                      class="pick-card"
                      :class="{ selected: selectedMascota === m.idMascota }"
                      @click="selectedMascota = m.idMascota; step1Errors.mascota = null"
                    >
                      <PetAvatar :nombre-especie="m.nombreEspecie" size="md" />
                      <div class="pick-info">
                        <span class="pick-name">{{ m.nombre }}</span>
                        <span class="pick-meta">
                          {{ m.nombreRaza || m.nombreEspecie || 'Mascota' }}
                        </span>
                      </div>
                      <span v-if="selectedMascota === m.idMascota" class="pick-check">
                        <CheckCircle2 :size="14" />
                      </span>
                    </button>
                  </div>
                  <span v-if="step1Errors.mascota" class="form-error mt-8">{{ step1Errors.mascota }}</span>
                </template>

                <div v-else class="empty-inline">
                  <AlertTriangle :size="24" />
                  <p>
                    No se encontraron mascotas para este cliente.
                    <router-link
                      :to="{
                        path: '/recepcion/registrar-mascota',
                        query: {
                          clienteDocumento: clienteSeleccionado.documentoIdentidad,
                          clienteNombre: clienteSeleccionado.nombreCompleto,
                        },
                      }"
                      class="link-teal"
                    >
                      Registra una mascota
                    </router-link>
                    y regresa.
                  </p>
                </div>
              </div>
            </div>

            <!-- Card: Motivo -->
            <div class="card">
              <div class="card-header">
                <div class="card-header-left">
                  <div class="card-icon"><MessageSquare :size="16" /></div>
                  <div>
                    <h3>Motivo de la consulta</h3>
                    <p class="card-header-sub">Descripción indicada por el cliente</p>
                  </div>
                </div>
              </div>
              <div class="card-body">
                <textarea
                  v-model="motivoConsulta"
                  rows="3"
                  maxlength="1000"
                  class="form-textarea"
                  :class="{ 'is-invalid': step1Errors.motivo }"
                  placeholder="Ej: Control de vacunas anuales, decaimiento desde ayer…"
                />
                <div class="form-footer">
                  <span v-if="step1Errors.motivo" class="form-error">{{ step1Errors.motivo }}</span>
                  <span class="char-count">{{ motivoConsulta.length }}/1000</span>
                </div>
              </div>
            </div>
          </section>

          <!-- ══════ STEP 2: HORARIO ══════ -->
          <section v-else-if="currentStep === 2" key="step2" class="step-panel">
            <!-- Veterinario -->
            <div class="card">
              <div class="card-header">
                <div class="card-header-left">
                  <div class="card-icon"><Stethoscope :size="16" /></div>
                  <div>
                    <h3>Veterinario</h3>
                    <p class="card-header-sub">Selecciona quién atenderá al paciente</p>
                  </div>
                </div>
              </div>
              <div class="card-body">
                <div class="pick-grid" :class="{ 'with-error': step2Errors.veterinario }">
                  <button
                    v-for="v in veterinarios"
                    :key="v.id"
                    type="button"
                    class="pick-card"
                    :class="{ selected: selectedVeterinario === v.id }"
                    @click="selectedVeterinario = v.id; onVeterinarioChange(); step2Errors.veterinario = null"
                  >
                    <EntityAvatar :nombre="v.nombre" tipo="veterinario" size="md" />
                    <div class="pick-info">
                      <span class="pick-name">{{ v.nombre }}</span>
                      <span class="pick-meta">{{ v.especialidad || 'Veterinario' }}</span>
                    </div>
                    <span v-if="selectedVeterinario === v.id" class="pick-check">
                      <CheckCircle2 :size="14" />
                    </span>
                  </button>
                </div>
                <span v-if="step2Errors.veterinario" class="form-error mt-8">{{ step2Errors.veterinario }}</span>
              </div>
            </div>

            <!-- Servicio -->
            <div class="card">
              <div class="card-header">
                <div class="card-header-left">
                  <div class="card-icon"><ClipboardList :size="16" /></div>
                  <div>
                    <h3>Servicio</h3>
                    <p class="card-header-sub">
                      {{ !selectedVeterinario
                        ? 'Primero selecciona un veterinario'
                        : 'Elige el tipo de atención' }}
                    </p>
                  </div>
                </div>
              </div>
              <div class="card-body">
                <div v-if="!selectedVeterinario" class="empty-inline">
                  <Stethoscope :size="24" />
                  <p>Selecciona un veterinario para ver los servicios disponibles.</p>
                </div>
                <div v-else-if="!servicios.length" class="empty-inline">
                  <ClipboardList :size="24" />
                  <p>Este veterinario aún no tiene servicios configurados.</p>
                </div>
                <div v-else class="service-list" :class="{ 'with-error': step2Errors.servicio }">
                  <button
                    v-for="s in servicios"
                    :key="s.id"
                    type="button"
                    class="service-item"
                    :class="{ selected: selectedServicio === s.id }"
                    @click="selectedServicio = s.id; step2Errors.servicio = null"
                  >
                    <span class="service-item-radio">
                      <span v-if="selectedServicio === s.id" class="service-item-radio-inner" />
                    </span>
                    <div class="service-item-body">
                      <span class="service-item-name">{{ s.nombre }}</span>
                      <div class="service-item-meta">
                        <span class="meta-chip"><Clock :size="12" /> {{ s.duracionMinutos }} min</span>
                        <span class="meta-chip meta-price"><DollarSign :size="12" /> {{ fmtUsd(s.precioUsd) }}</span>
                      </div>
                    </div>
                  </button>
                </div>
                <span v-if="step2Errors.servicio" class="form-error mt-8">{{ step2Errors.servicio }}</span>
              </div>
            </div>

            <!-- Fecha -->
            <div class="card">
              <div class="card-header">
                <div class="card-header-left">
                  <div class="card-icon"><CalendarDays :size="16" /></div>
                  <div>
                    <h3>Fecha</h3>
                    <p class="card-header-sub">Selecciona el día de la cita</p>
                  </div>
                </div>
              </div>
              <div class="card-body">
                <input
                  v-model="fecha"
                  type="date"
                  :min="hoy"
                  class="form-input"
                  :class="{ 'is-invalid': step2Errors.fecha }"
                />
                <span v-if="step2Errors.fecha" class="form-error mt-8">{{ step2Errors.fecha }}</span>
              </div>
            </div>

            <!-- Bloques horarios -->
            <div
              v-if="fecha && selectedServicio && selectedVeterinario"
              class="card"
            >
              <div class="card-header">
                <div class="card-header-left">
                  <div class="card-icon"><Clock :size="16" /></div>
                  <div>
                    <h3>Horarios disponibles</h3>
                    <p class="card-header-sub">{{ fmtFecha(fecha) }}</p>
                  </div>
                </div>
              </div>
              <div class="card-body">
                <div v-if="cargandoBloques" class="mini-load">
                  <Loader2 :size="14" class="spin" /> Consultando disponibilidad…
                </div>

                <div v-else-if="bloques.length === 0" class="empty-inline">
                  <AlertTriangle :size="24" />
                  <p>No hay bloques libres para esta fecha. Prueba con otro día.</p>
                </div>

                <div v-else class="slots-grid">
                  <button
                    v-for="b in bloques"
                    :key="b.horaInicio"
                    type="button"
                    class="slot-btn"
                    :class="{
                      'is-selected': selectedBloque?.horaInicio === b.horaInicio,
                      'is-conflicto': bloqueConflicto === b.horaInicio,
                    }"
                    @click="selectedBloque = b; step2Errors.bloque = null; bloqueConflicto = null"
                  >
                    <span class="slot-time">{{ fmtHora(b.horaInicio) }}</span>
                    <span class="slot-sep">a</span>
                    <span class="slot-time slot-end">{{ fmtHora(b.horaFin) }}</span>
                    <span v-if="selectedBloque?.horaInicio === b.horaInicio" class="slot-check">
                      <CheckCircle2 :size="13" />
                    </span>
                  </button>
                </div>

                <div v-if="step2Errors.bloque" class="alert alert-warning mt-12">
                  <AlertTriangle :size="16" />
                  <span>{{ step2Errors.bloque }}</span>
                </div>
                <p class="hint-teal">
                  <DollarSign :size="12" />
                  El monto en bolívares se calcula con la tasa oficial al confirmar.
                </p>
              </div>
            </div>
          </section>

          <!-- ══════ STEP 3: COBRO ══════ -->
          <section v-else-if="currentStep === 3" key="step3" class="step-panel">
            <div class="card">
              <div class="card-header">
                <div class="card-header-left">
                  <div class="card-icon"><CreditCard :size="16" /></div>
                  <div>
                    <h3>Método de pago presencial</h3>
                    <p class="card-header-sub">Selecciona cómo está pagando el cliente</p>
                  </div>
                </div>
              </div>
              <div class="card-body">
                <div v-if="pagoError" class="alert alert-error" style="margin-top: 0; margin-bottom: 16px;">
                  <AlertTriangle :size="16" />
                  <span>{{ pagoError }}</span>
                </div>

                <!-- Métodos -->
                <div class="methods-list">
                  <button
                    v-for="m in metodos"
                    :key="m.id"
                    type="button"
                    class="method-option"
                    :class="{ active: selectedMetodo === m.id }"
                    @click="selectedMetodo = m.id; datosPago = {}; referencia = ''; step3Errors = {}; pagoError = ''"
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
                <span v-if="step3Errors.metodo" class="form-error mt-8">{{ step3Errors.metodo }}</span>

                <!-- Campos dinámicos -->
                <Transition name="expand">
                  <div v-if="metodoSeleccionado" class="dynamic-fields">
                    <div v-if="'referencia' in (metodoSeleccionado.camposRequeridos || {})" class="form-group">
                      <label class="form-label">
                        Número de referencia <span class="required">*</span>
                      </label>
                      <input
                        v-model="referencia"
                        type="text"
                        class="form-input"
                        :class="{ 'is-invalid': step3Errors.referencia }"
                        placeholder="Ej: 0000123456789"
                      />
                      <span v-if="step3Errors.referencia" class="form-error">{{ step3Errors.referencia }}</span>
                    </div>

                    <div v-for="campo in camposDinamicos" :key="campo.key" class="form-group">
                      <label class="form-label">
                        {{ fmtFieldLabel(campo.key) }} <span class="required">*</span>
                      </label>
                      <select
                        v-if="campo.key === 'banco'"
                        v-model="datosPago[campo.key]"
                        class="form-select"
                        :class="{ 'is-invalid': step3Errors[campo.key] }"
                      >
                        <option value="" disabled>Seleccione el banco</option>
                        <option v-for="b in BANCOS_VENEZUELA" :key="b.codigo" :value="b.nombre">
                          {{ b.codigo }} - {{ b.nombre }}
                        </option>
                      </select>
                      <input
                        v-else
                        v-model="datosPago[campo.key]"
                        type="text"
                        class="form-input"
                        :class="{ 'is-invalid': step3Errors[campo.key] }"
                      />
                      <span v-if="step3Errors[campo.key]" class="form-error">{{ step3Errors[campo.key] }}</span>
                    </div>
                  </div>
                </Transition>

                <!-- Confirmación de fondos -->
                <label class="check-row" :class="{ 'is-invalid': step3Errors.fondos }">
                  <input v-model="fondosConfirmados" type="checkbox" class="checkbox-input" />
                  <span class="checkbox-box">
                    <Check v-if="fondosConfirmados" :size="12" />
                  </span>
                  <span class="checkbox-label">
                    Confirmo la recepción de los fondos del cliente
                  </span>
                </label>
                <span v-if="step3Errors.fondos" class="form-error">{{ step3Errors.fondos }}</span>

                <!-- Botón cobrar -->
                <button class="btn-pay-main" :disabled="procesando" @click="confirmarYFacturar">
                  <Loader2 v-if="procesando" :size="18" class="spin" />
                  <CreditCard v-else :size="18" />
                  {{ procesando ? 'Procesando…' : 'Confirmar y Facturar' }}
                </button>
              </div>
            </div>
          </section>

          <!-- ══════ STEP 4: COMPROBANTE ══════ -->
          <section v-else-if="currentStep === 4 && resultado" key="step4" class="step-panel step-success">
            <div class="success-container">
              <div class="success-icon">
                <CheckCircle2 :size="40" />
              </div>
              <h2 class="success-title">Cita agendada y facturada</h2>
              <p class="success-message">
                La cita quedó registrada y el pago fue confirmado. El comprobante está listo.
              </p>

              <div class="factura-box">
                <div class="factura-row">
                  <span class="factura-label">Factura</span>
                  <span class="factura-value mono">{{ resultado.numeroControl }}</span>
                </div>
                <div class="factura-row">
                  <span class="factura-label">Cliente</span>
                  <span class="factura-value">
                    {{ resultado.resumen.cliente }} ({{ resultado.resumen.documentoCliente }})
                  </span>
                </div>
                <div class="factura-row">
                  <span class="factura-label">Mascota</span>
                  <span class="factura-value">{{ resultado.resumen.mascota }}</span>
                </div>
                <div class="factura-row">
                  <span class="factura-label">Veterinario</span>
                  <span class="factura-value">{{ resultado.resumen.veterinario }}</span>
                </div>
                <div class="factura-row">
                  <span class="factura-label">Servicio</span>
                  <span class="factura-value">{{ resultado.resumen.servicio }}</span>
                </div>
                <div class="factura-row">
                  <span class="factura-label">Turno</span>
                  <span class="factura-value">
                    {{ fmtFecha(resultado.resumen.fecha) }} · {{ fmtHora(resultado.resumen.horaInicio) }} – {{ fmtHora(resultado.resumen.horaFin) }}
                  </span>
                </div>
                <div class="factura-row">
                  <span class="factura-label">Método de pago</span>
                  <span class="factura-value">
                    {{ METODO_LABEL[resultado.resumen.metodoPago] || resultado.resumen.metodoPago }}
                  </span>
                </div>
                <div class="factura-row total">
                  <span class="factura-label">Total facturado</span>
                  <span class="factura-value total-value">
                    {{ fmtUsd(resultado.resumen.costoUsd) }}
                    <small class="total-bs">Bs. {{ Number(resultado.resumen.costoBs).toFixed(2) }}</small>
                  </span>
                </div>
              </div>

              <div v-if="envio" class="alert" :class="envio.enviado ? 'alert-success' : 'alert-warning'">
                <CheckCircle2 v-if="envio.enviado" :size="16" />
                <AlertTriangle v-else :size="16" />
                <span>{{ envio.mensaje }}</span>
              </div>

              <div class="success-actions">
                <button class="btn-secondary" :disabled="imprimiendo" @click="imprimirComprobante">
                  <Loader2 v-if="imprimiendo" :size="15" class="spin" />
                  <Printer v-else :size="15" />
                  Imprimir
                </button>
                <button class="btn-secondary" :disabled="enviando" @click="enviarPorCorreo">
                  <Loader2 v-if="enviando" :size="15" class="spin" />
                  <Mail v-else :size="15" />
                  Enviar por correo
                </button>
                <button class="btn-primary" @click="volverAlCalendario">
                  <ArrowLeft :size="15" />
                  Volver al calendario
                </button>
              </div>
            </div>
          </section>
        </Transition>
      </main>

      <!-- ═══ SIDEBAR RESUMEN ═══ -->
      <aside v-if="currentStep < 4" class="wizard-sidebar">
        <div class="summary-card">
          <header class="summary-card-header">
            <h4>Resumen de la reserva</h4>
            <span class="summary-card-hint">Se actualiza en tiempo real</span>
          </header>
          <div class="summary-card-body">
            <div class="summary-line" :class="{ 'is-empty': !resumenActual.cliente }">
              <span class="summary-line-label"><UserPlus :size="13" /> Cliente</span>
              <span class="summary-line-value">{{ resumenActual.cliente || 'Por seleccionar' }}</span>
            </div>
            <div class="summary-line" :class="{ 'is-empty': !resumenActual.mascota }">
              <span class="summary-line-label"><PawPrint :size="13" /> Mascota</span>
              <span class="summary-line-value">{{ resumenActual.mascota || 'Por seleccionar' }}</span>
            </div>
            <div class="summary-line" :class="{ 'is-empty': !resumenActual.veterinario }">
              <span class="summary-line-label"><Stethoscope :size="13" /> Veterinario</span>
              <span class="summary-line-value">{{ resumenActual.veterinario || 'Por seleccionar' }}</span>
            </div>
            <div class="summary-line" :class="{ 'is-empty': !resumenActual.servicio }">
              <span class="summary-line-label"><ClipboardList :size="13" /> Servicio</span>
              <span class="summary-line-value">{{ resumenActual.servicio || 'Por seleccionar' }}</span>
            </div>
            <div class="summary-line" :class="{ 'is-empty': !resumenActual.fecha }">
              <span class="summary-line-label"><CalendarDays :size="13" /> Fecha</span>
              <span class="summary-line-value">{{ resumenActual.fecha || 'Por seleccionar' }}</span>
            </div>
            <div class="summary-line" :class="{ 'is-empty': !resumenActual.hora }">
              <span class="summary-line-label"><Clock :size="13" /> Horario</span>
              <span class="summary-line-value">{{ resumenActual.hora || 'Por seleccionar' }}</span>
            </div>
          </div>
          <footer class="summary-card-footer">
            <div class="summary-total">
              <span class="total-label">Total estimado</span>
              <span class="total-value">{{ resumenActual.total || '—' }}</span>
            </div>
            <p class="summary-note">
              <Lock :size="11" />
              El monto en Bs. se calcula con la tasa oficial al confirmar.
            </p>
          </footer>
        </div>
      </aside>
    </div>

    <!-- ═══ STEP ACTIONS ═══ -->
    <div v-if="currentStep < 4" class="step-actions">
      <button class="btn-cancel" type="button" @click="cancelar">
        <X :size="15" /> Cancelar
      </button>
      <div class="step-actions-right">
        <button
          v-if="currentStep > 1"
          class="btn-back"
          type="button"
          @click="currentStep--; pagoError = ''"
        >
          <ArrowLeft :size="15" /> Atrás
        </button>
        <button
          v-if="currentStep === 1"
          class="btn-next"
          type="button"
          @click="validarStep1() && (currentStep = 2)"
        >
          Siguiente <ArrowRight :size="15" />
        </button>
        <button
          v-if="currentStep === 2"
          class="btn-next"
          type="button"
          :disabled="!selectedBloque"
          @click="validarStep2() && (currentStep = 3)"
        >
          Ir al cobro <ArrowRight :size="15" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Search, X, UserPlus, PawPrint, Stethoscope, CalendarDays, Clock,
  CreditCard, Printer, Mail, CheckCircle2, AlertTriangle, ArrowLeft,
  ArrowRight, Loader2, ChevronRight, ClipboardList, MessageSquare,
  DollarSign, Check, Sparkles, Lock
} from 'lucide-vue-next'
import * as clientesApi from '@/api/clientes.api'
import { getServicios, getVeterinarios, getDisponibilidad, agendarMostrador } from '@/api/citas.api'
import { getMetodosPresenciales, enviarFactura, descargarFactura } from '@/api/pagos.api'
import { getMascotasPorCliente } from '@/api/mascotas.api'
import PetAvatar from '@/components/ui/PetAvatar.vue'
import EntityAvatar from '@/components/ui/EntityAvatar.vue'

const router = useRouter()
const route = useRoute()

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
}

const steps = [
  { num: 1, label: 'Cliente y Mascota', desc: 'Datos del paciente' },
  { num: 2, label: 'Horario', desc: 'Vet, servicio y turno' },
  { num: 3, label: 'Cobro', desc: 'Método de pago' },
  { num: 4, label: 'Comprobante', desc: 'Factura emitida' },
]
const currentStep = ref(1)

// ─── STEP 1 ───
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

// ─── STEP 2 ───
const veterinarios = ref([])
const selectedVeterinario = ref(null)
const servicios = ref([])
const selectedServicio = ref(null)
const fecha = ref('')
const hoy = new Date().toISOString().split('T')[0]
const bloques = ref([])
const cargandoBloques = ref(false)
const selectedBloque = ref(null)
const bloqueConflicto = ref(null)
const step2Errors = ref({})

// ─── STEP 3 ───
const metodos = ref([])
const selectedMetodo = ref(null)
const datosPago = ref({})
const referencia = ref('')
const fondosConfirmados = ref(false)
const step3Errors = ref({})
const procesando = ref(false)
const pagoError = ref('')

// ─── STEP 4 ───
const resultado = ref(null)
const envio = ref(null)
const enviando = ref(false)
const imprimiendo = ref(false)

// ─── Computed ───
const servicioSeleccionado = computed(() =>
  servicios.value.find(s => s.id === selectedServicio.value) || null
)
const metodoSeleccionado = computed(() =>
  metodos.value.find(m => m.id === selectedMetodo.value) || null
)
const camposDinamicos = computed(() => {
  if (!metodoSeleccionado.value?.camposRequeridos) return []
  return Object.entries(metodoSeleccionado.value.camposRequeridos)
    .filter(([key]) => key !== 'referencia')
    .map(([key]) => ({ key }))
})

const resumenActual = computed(() => {
  const servicio = servicioSeleccionado.value
  return {
    cliente: clienteSeleccionado.value?.nombreCompleto || null,
    mascota: mascotas.value.find(m => m.idMascota === selectedMascota.value)?.nombre || null,
    veterinario: veterinarios.value.find(v => v.id === selectedVeterinario.value)?.nombre || null,
    servicio: servicio?.nombre || null,
    fecha: fecha.value ? fmtFecha(fecha.value) : null,
    hora: selectedBloque.value
      ? `${fmtHora(selectedBloque.value.horaInicio)} – ${fmtHora(selectedBloque.value.horaFin)}`
      : null,
    total: servicio?.precioUsd != null ? fmtUsd(servicio.precioUsd) : null,
  }
})

// ─── Búsqueda de cliente ───
watch(clienteFiltro, (val) => {
  if (clienteSeleccionado.value) return
  clearTimeout(debounce)
  if (!val || val.trim().length < 2) {
    resultadosClientes.value = []
    return
  }
  debounce = setTimeout(buscarClientes, 350)
})

async function buscarClientes() {
  buscandoCliente.value = true
  try {
    const { data } = await clientesApi.getAll(clienteFiltro.value.trim())
    resultadosClientes.value = data
  } catch {
    resultadosClientes.value = []
  } finally {
    buscandoCliente.value = false
  }
}

function seleccionarCliente(c) {
  clienteSeleccionado.value = {
    id: c.id,
    nombreCompleto: c.nombreCompleto,
    documentoIdentidad: c.documentoIdentidad,
  }
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
  } catch {
    mascotas.value = []
  } finally {
    cargandoMascotas.value = false
  }
}

// ─── Vet → servicios ───
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
  } catch {
    /* silencioso */
  }
}

watch(fecha, () => {
  if (fecha.value && selectedVeterinario.value && selectedServicio.value) cargarBloques()
})
watch(selectedServicio, () => {
  bloques.value = []
  selectedBloque.value = null
})

async function cargarBloques() {
  cargandoBloques.value = true
  bloqueConflicto.value = null
  try {
    const { data } = await getDisponibilidad({
      id_veterinario: selectedVeterinario.value,
      fecha: fecha.value,
      id_servicio: selectedServicio.value,
    })
    bloques.value = data.bloquesDisponibles || []
  } catch {
    bloques.value = []
  } finally {
    cargandoBloques.value = false
  }
}

// ─── Validaciones ───
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

// ─── Confirmar y facturar ───
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
      datosPago: datosPagoCompletos,
    })
    resultado.value = data
    envio.value = null
    currentStep.value = 4
  } catch (err) {
    const msg = err.response?.data?.message || ''
    if (err.response?.status === 409 && msg.includes('ya no se encuentra disponible')) {
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

// ─── Acciones del comprobante ───
async function imprimirComprobante() {
  imprimiendo.value = true
  try {
    const res = await descargarFactura(resultado.value.idFactura)
    const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
    window.open(url, '_blank')
    setTimeout(() => window.URL.revokeObjectURL(url), 60000)
  } catch {
    /* el PDF puede reintentarse */
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
    envio.value = {
      enviado: false,
      mensaje: 'No se pudo enviar el correo. Intente nuevamente.',
    }
  } finally {
    enviando.value = false
  }
}

function volverAlCalendario() {
  router.push('/recepcion/citas')
}
function cancelar() {
  router.push('/recepcion/citas')
}

// ─── Helpers de formato ───
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
function fmtUsd(v) {
  if (v == null) return '—'
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

// ─── Init ───
onMounted(async () => {
  const { data } = await getVeterinarios()
  veterinarios.value = data
  const { data: m } = await getMetodosPresenciales()
  metodos.value = m

  if (route.query.clienteDocumento) {
    clienteFiltro.value = route.query.clienteDocumento
    await buscarClientes()
    const exacto = resultadosClientes.value.find(
      c => c.documentoIdentidad === route.query.clienteDocumento
    )
    if (exacto) seleccionarCliente(exacto)
    clienteFiltro.value = ''
  }
})
</script>

<style scoped>
.agendar-mostrador {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 24px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
}
button { font-family: inherit; }

/* ═══ BREADCRUMB ═══ */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 13px;
  color: #64748B;
}
.bc-back {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: 1px solid #E2E8F0;
  background: #fff;
  color: #475569;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .15s ease;
  margin-right: 2px;
}
.bc-back:hover { background: #F1F5F9; color: #0F766E; border-color: #CBD5E1; }
.bc-item { font-weight: 500; }
.bc-link {
  background: none;
  border: none;
  color: #64748B;
  cursor: pointer;
  padding: 0;
  font-size: 13px;
}
.bc-link:hover { color: #0F766E; text-decoration: underline; }
.bc-current { color: #1E293B; font-weight: 700; }
.bc-sep { color: #CBD5E1; }

/* ═══ HERO ═══ */
.wizard-hero {
  padding: 24px 28px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
}
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
.wizard-hero h1 {
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
  max-width: 520px;
}

/* ═══ STEPPER ═══ */
.stepper {
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  padding: 14px 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  gap: 8px;
}
.stepper-step {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
  position: relative;
}
.stepper-step:last-child { flex: 0 0 auto; }
.stepper-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12.5px;
  font-weight: 700;
  background: #F1F5F9;
  color: #94A3B8;
  border: 2px solid #E2E8F0;
  flex-shrink: 0;
  transition: all .3s ease;
}
.stepper-step.active .stepper-circle {
  background: #0F766E;
  color: #fff;
  border-color: #0F766E;
  box-shadow: 0 0 0 4px rgba(15, 118, 110, .15);
}
.stepper-step.completed .stepper-circle {
  background: #0F766E;
  color: #fff;
  border-color: #0F766E;
}
.stepper-text { display: flex; flex-direction: column; gap: 1px; min-width: 0; }
.stepper-label {
  font-size: 13px;
  font-weight: 700;
  color: #94A3B8;
  transition: color .25s ease;
  white-space: nowrap;
}
.stepper-step.active .stepper-label { color: #0F766E; }
.stepper-step.completed .stepper-label { color: #1E293B; }
.stepper-desc {
  font-size: 11px;
  color: #94A3B8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.stepper-line {
  flex: 1;
  height: 2px;
  background: #E2E8F0;
  margin: 0 10px;
  border-radius: 1px;
  transition: background .3s ease;
  min-width: 16px;
}
.stepper-step.completed .stepper-line { background: #0F766E; }

/* ═══ LAYOUT ═══ */
.wizard-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
  align-items: start;
}
.wizard-layout.is-success { grid-template-columns: 1fr; }
.wizard-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ═══ SIDEBAR ═══ */
.summary-card {
  position: sticky;
  top: 24px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
}
.summary-card-header { padding: 16px 20px 12px; border-bottom: 1px solid #F1F5F9; }
.summary-card-header h4 { margin: 0 0 2px; font-size: 14.5px; font-weight: 700; color: #0F172A; }
.summary-card-hint { font-size: 11.5px; color: #94A3B8; }
.summary-card-body {
  padding: 12px 20px 16px;
  display: flex;
  flex-direction: column;
  gap: 11px;
}
.summary-line { display: flex; flex-direction: column; gap: 3px; transition: opacity .2s ease; }
.summary-line.is-empty { opacity: .55; }
.summary-line-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
}
.summary-line-value {
  font-size: 13px;
  font-weight: 600;
  color: #0F172A;
  line-height: 1.35;
  word-break: break-word;
}
.summary-line.is-empty .summary-line-value {
  color: #94A3B8;
  font-weight: 500;
  font-style: italic;
}
.summary-card-footer {
  padding: 14px 20px 18px;
  background: linear-gradient(180deg, #F8FAFC 0%, #F0FDFA 100%);
  border-top: 1px solid #E2E8F0;
}
.summary-total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}
.total-label {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
}
.total-value { font-size: 20px; font-weight: 700; color: #0F766E; letter-spacing: -0.01em; }
.summary-note {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 12px 0 0;
  font-size: 11px;
  color: #64748B;
  line-height: 1.4;
}

/* ═══ CARDS ═══ */
.card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  overflow: hidden;
}
.card-header {
  padding: 16px 22px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.card-header-left { display: flex; align-items: center; gap: 12px; min-width: 0; }
.card-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: #F0FDFA;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.card-header h3 { font-size: 14.5px; font-weight: 700; color: #0F172A; margin: 0; line-height: 1.2; }
.card-header-sub { margin: 2px 0 0; font-size: 12px; color: #64748B; }
.card-body { padding: 18px 22px 22px; }

/* ═══ SEARCH BOX ═══ */
.search-box { position: relative; }
.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #94A3B8;
  pointer-events: none;
}
.search-input {
  width: 100%;
  padding: 11px 40px 11px 40px;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  font-size: 13.5px;
  color: #1E293B;
  background: #fff;
  font-family: inherit;
  outline: none;
  box-sizing: border-box;
  transition: all .2s;
}
.search-input::placeholder { color: #94A3B8; }
.search-input:focus {
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.search-spinner {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #0F766E;
}
.search-clear {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #94A3B8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 5px;
  border-radius: 6px;
  transition: all .15s;
}
.search-clear:hover { color: #475569; background: #F1F5F9; }

/* ═══ RESULTS DROPDOWN ═══ */
.results-list {
  margin-top: 10px;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  overflow: hidden;
  max-height: 300px;
  overflow-y: auto;
}
.result-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 12px;
  align-items: center;
  width: 100%;
  text-align: left;
  padding: 12px 14px;
  background: #fff;
  border: none;
  border-bottom: 1px solid #F1F5F9;
  cursor: pointer;
  font-family: inherit;
  transition: background-color .15s;
}
.result-row:last-child { border-bottom: none; }
.result-row:hover { background: #F0FDFA; }
.result-info { min-width: 0; }
.result-name {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.result-meta {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.result-arrow { color: #CBD5E1; flex-shrink: 0; transition: color .2s ease; }
.result-row:hover .result-arrow { color: #0F766E; }

/* ═══ CLIENTE CHIP ═══ */
.cliente-chip {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: #F0FDFA;
  border: 1px solid #99F6E4;
  border-radius: 12px;
}
.cliente-chip-info { flex: 1; min-width: 0; }
.cliente-chip-name {
  margin: 0;
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.cliente-chip-doc {
  margin: 2px 0 0;
  font-size: 12.5px;
  color: #0F766E;
  font-weight: 600;
}
.btn-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  background: #fff;
  border: 1px solid #99F6E4;
  color: #0F766E;
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: all .2s ease;
  flex-shrink: 0;
}
.btn-chip:hover {
  background: #CCFBF1;
  border-color: #0F766E;
}

/* ═══ PICK GRID ═══ */
.pick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 10px;
}
.pick-grid.with-error {
  padding: 4px;
  border-radius: 12px;
  background: #FEF2F2;
  border: 1px solid #FECACA;
}
.pick-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #fff;
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: all .2s ease;
  overflow: hidden;
}
.pick-card:hover {
  border-color: #99F6E4;
  background: #F0FDFA;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -8px rgba(15, 118, 110, .2);
}
.pick-card.selected {
  border-color: #0F766E;
  background: #F0FDFA;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.pick-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
.pick-name {
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.25;
}
.pick-meta {
  font-size: 11.5px;
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pick-check {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #0F766E;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  animation: checkPop .25s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes checkPop {
  0% { transform: scale(0); }
  100% { transform: scale(1); }
}

/* ═══ SERVICE LIST ═══ */
.service-list { display: flex; flex-direction: column; gap: 8px; }
.service-list.with-error {
  padding: 4px;
  border-radius: 12px;
  background: #FEF2F2;
  border: 1px solid #FECACA;
}
.service-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px 16px;
  background: #fff;
  border: 1.5px solid #E2E8F0;
  border-radius: 12px;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  width: 100%;
  transition: all .2s ease;
}
.service-item:hover { border-color: #99F6E4; background: #F0FDFA; }
.service-item.selected {
  border-color: #0F766E;
  background: #F0FDFA;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .1);
}
.service-item-radio {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #CBD5E1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
  transition: border-color .2s ease;
}
.service-item.selected .service-item-radio { border-color: #0F766E; }
.service-item-radio-inner {
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
.service-item-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.service-item-name { font-size: 14px; font-weight: 700; color: #0F172A; line-height: 1.3; }
.service-item-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-top: 2px; }
.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11.5px;
  font-weight: 600;
  color: #0F766E;
  background: #fff;
  padding: 3px 10px;
  border-radius: 20px;
  border: 1px solid #CCFBF1;
}
.meta-price { color: #0F172A; border-color: #E2E8F0; }

/* ═══ FORM INPUTS ═══ */
.form-input,
.form-select,
.form-textarea {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  font-size: 14px;
  color: #1E293B;
  background: #fff;
  font-family: inherit;
  transition: border-color .2s, box-shadow .2s;
  box-sizing: border-box;
  appearance: none;
  -webkit-appearance: none;
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
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .12);
}
.form-textarea { resize: vertical; min-height: 84px; line-height: 1.55; }
.is-invalid { border-color: #EF4444 !important; }
.is-invalid:focus { box-shadow: 0 0 0 3px rgba(239, 68, 68, .12) !important; }
.form-group { display: flex; flex-direction: column; gap: 6px; }
.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 600;
  color: #374151;
}
.required { color: #EF4444; }
.form-error { font-size: 12px; color: #EF4444; font-weight: 600; }
.form-error.mt-8 { display: block; margin-top: 10px; }
.form-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 4px; }
.char-count { font-size: 11px; color: #94A3B8; margin-left: auto; }

/* ═══ SLOTS ═══ */
.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 10px;
}
.slot-btn {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 14px 12px;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  background: #fff;
  cursor: pointer;
  transition: all .2s ease;
  min-height: 62px;
  font-family: inherit;
}
.slot-btn:hover {
  border-color: #0F766E;
  background: #F0FDFA;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -8px rgba(15, 118, 110, .25);
}
.slot-btn.is-selected {
  background: #0F766E;
  border-color: #0F766E;
  color: #fff;
  box-shadow: 0 6px 16px -6px rgba(15, 118, 110, .5);
}
.slot-btn.is-conflicto {
  border-color: #DC2626;
  background: #FEF2F2;
  color: #DC2626;
  animation: pulseWarn 1.2s ease-in-out;
}
@keyframes pulseWarn {
  0%, 100% { box-shadow: 0 0 0 0 rgba(220, 38, 38, .2); }
  50% { box-shadow: 0 0 0 6px rgba(220, 38, 38, .15); }
}
.slot-time { font-size: 13px; font-weight: 700; color: #0F172A; transition: color .2s; }
.slot-btn.is-selected .slot-time { color: #fff; }
.slot-btn.is-conflicto .slot-time { color: #DC2626; }
.slot-sep { font-size: 10px; color: #94A3B8; transition: color .2s; }
.slot-btn.is-selected .slot-sep { color: rgba(255, 255, 255, .7); }
.slot-end { font-size: 12px; font-weight: 500; color: #64748B; transition: color .2s; }
.slot-btn.is-selected .slot-end { color: rgba(255, 255, 255, .85); }
.slot-check {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(255, 255, 255, .25);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.hint-teal {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 12px 0 0;
  padding: 8px 12px;
  font-size: 12px;
  color: #0F766E;
  background: #F0FDFA;
  border: 1px solid #CCFBF1;
  border-radius: 8px;
}

/* ═══ MÉTODOS DE PAGO ═══ */
.methods-list { display: flex; flex-direction: column; gap: 10px; }
.method-option {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid #E2E8F0;
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
.method-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.method-name { font-size: 14px; font-weight: 700; color: #0F172A; }
.method-desc { font-size: 12.5px; color: #64748B; }

/* ═══ DYNAMIC FIELDS ═══ */
.dynamic-fields {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #E2E8F0;
}

/* ═══ CHECKBOX ═══ */
.check-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  margin-top: 18px;
  background: #F8FAFC;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  cursor: pointer;
  font-family: inherit;
  transition: all .2s ease;
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
  font-size: 13.5px;
  font-weight: 600;
  color: #1E293B;
}

/* ═══ BOTÓN PAGAR ═══ */
.btn-pay-main {
  width: 100%;
  padding: 14px 24px;
  background: #0F766E;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 14.5px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all .2s ease;
  margin-top: 18px;
  font-family: inherit;
}
.btn-pay-main:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 8px 20px -6px rgba(15, 118, 110, .4);
}
.btn-pay-main:disabled { opacity: .55; cursor: not-allowed; }

/* ═══ ALERTAS ═══ */
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
.mt-12 { margin-top: 12px; }

/* ═══ MINI LOAD ═══ */
.mini-load {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748B;
  font-size: 13px;
  padding: 12px 0;
}

/* ═══ EMPTY INLINE ═══ */
.empty-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px 20px;
  color: #94A3B8;
  text-align: center;
  font-size: 13px;
  background: #F8FAFC;
  border: 1px dashed #E2E8F0;
  border-radius: 10px;
}
.empty-inline p { margin: 0; max-width: 380px; line-height: 1.5; }
.link-teal {
  color: #0F766E;
  font-weight: 700;
  text-decoration: none;
}
.link-teal:hover { text-decoration: underline; }

/* ═══ ÉXITO ═══ */
.step-success { max-width: 720px; margin: 0 auto; }
.success-container {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  padding: 44px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 14px;
}
.success-icon {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDFA 100%);
  border: 3px solid #10B981;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #059669;
  margin-bottom: 8px;
  animation: successPop .5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}
.success-title {
  font-size: 22px;
  font-weight: 700;
  color: #0F172A;
  margin: 0;
  letter-spacing: -0.01em;
}
.success-message {
  font-size: 13.5px;
  color: #64748B;
  margin: 0;
  max-width: 480px;
  line-height: 1.6;
}
.factura-box {
  margin-top: 8px;
  padding: 6px 20px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  width: 100%;
  max-width: 520px;
  text-align: left;
}
.factura-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 11px 0;
  border-bottom: 1px solid #E2E8F0;
}
.factura-row:last-child { border-bottom: none; }
.factura-label {
  font-size: 12.5px;
  font-weight: 600;
  color: #64748B;
  white-space: nowrap;
}
.factura-value {
  font-size: 13px;
  font-weight: 700;
  color: #1E293B;
  text-align: right;
  word-break: break-word;
}
.factura-row.total {
  border-top: 2px solid #E2E8F0;
  padding-top: 14px;
  margin-top: 4px;
}
.total-value { color: #0F766E; font-size: 15px; display: flex; flex-direction: column; align-items: flex-end; }
.total-bs { font-size: 11.5px; color: #64748B; font-weight: 500; margin-top: 2px; }
.mono { font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace; }

.success-actions {
  display: flex;
  gap: 10px;
  margin-top: 18px;
  flex-wrap: wrap;
  justify-content: center;
}

/* ═══ STEP ACTIONS ═══ */
.step-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
  padding: 16px 22px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03);
  gap: 12px;
  flex-wrap: wrap;
}
.step-actions-right { display: flex; align-items: center; gap: 10px; margin-left: auto; }
.btn-cancel,
.btn-back,
.btn-next,
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
.btn-cancel {
  background: none;
  border: 1px solid #E2E8F0;
  color: #64748B;
}
.btn-cancel:hover {
  background: #FEF2F2;
  border-color: #FECACA;
  color: #EF4444;
}
.btn-back {
  background: #fff;
  border: 1px solid #E2E8F0;
  color: #475569;
}
.btn-back:hover { background: #F8FAFC; border-color: #CBD5E1; }
.btn-next {
  background: #0F766E;
  border: none;
  color: #fff;
}
.btn-next:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, .4);
}
.btn-next:disabled { opacity: .5; cursor: not-allowed; }
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
.btn-primary:disabled { opacity: .5; cursor: not-allowed; }
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

/* ═══ SPIN ═══ */
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══ TRANSICIONES ═══ */
.step-fade-enter-active,
.step-fade-leave-active {
  transition: opacity .25s ease, transform .25s ease;
}
.step-fade-enter-from { opacity: 0; transform: translateY(8px); }
.step-fade-leave-to { opacity: 0; transform: translateY(-8px); }
.expand-enter-active,
.expand-leave-active {
  transition: opacity .25s ease, transform .25s ease;
  overflow: hidden;
}
.expand-enter-from,
.expand-leave-to { opacity: 0; transform: translateY(-6px); }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .wizard-layout { grid-template-columns: 1fr; }
  .summary-card { position: static; }
}
@media (max-width: 768px) {
  .agendar-mostrador { padding: 16px; }
  .wizard-hero { padding: 20px; }
  .wizard-hero h1 { font-size: 22px; }
  .stepper {
    flex-direction: column;
    align-items: flex-start;
    padding: 14px;
    gap: 12px;
  }
  .stepper-step { width: 100%; flex: none; }
  .stepper-step:last-child { flex: none; }
  .stepper-line {
    position: absolute;
    top: 32px;
    left: 16px;
    width: 2px;
    height: 12px;
    margin: 0;
    min-width: 0;
  }
  .stepper-step:last-child .stepper-line { display: none; }
  .step-actions { flex-direction: column-reverse; padding: 14px; }
  .step-actions-right { width: 100%; flex-direction: column-reverse; }
  .btn-cancel,
  .btn-back,
  .btn-next { width: 100%; }
  .success-actions { flex-direction: column; width: 100%; }
  .btn-primary,
  .btn-secondary { width: 100%; justify-content: center; }
  .pick-grid { grid-template-columns: 1fr; }
}
@media (max-width: 480px) {
  .card-body { padding: 16px 18px 20px; }
  .card-header { padding: 14px 18px; }
  .slots-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .success-container { padding: 32px 20px; }
  .success-icon { width: 72px; height: 72px; }
  .success-title { font-size: 19px; }
  .factura-row { flex-direction: column; align-items: flex-start; gap: 4px; }
  .factura-value { text-align: left; }
  .total-value { align-items: flex-start; }
}
</style>