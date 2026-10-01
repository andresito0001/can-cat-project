<template>
  <div class="agendar-mostrador">
    <ToastContainer />

    <!-- Breadcrumb -->
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

    <!-- Hero -->
    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <Sparkles :size="12" /> Recepción · Mostrador
        </span>
        <h1>Nueva Reserva</h1>
        <p class="page-header-sub">
          Agenda y cobra una cita presencial en un solo flujo.
        </p>
      </div>
    </header>

    <!-- Stepper -->
    <nav class="stepper" aria-label="Progreso">
      <div
        v-for="(s, i) in steps"
        :key="s.num"
        class="stepper-step"
        :class="{ active: currentStep === s.num, completed: currentStep > s.num }"
      >
        <div class="stepper-circle">
          <Check v-if="currentStep > s.num" :size="15" />
          <span v-else>{{ s.num }}</span>
        </div>
        <div class="stepper-text">
          <span class="stepper-label">{{ s.label }}</span>
          <span class="stepper-desc">{{ s.desc }}</span>
        </div>
        <div v-if="i < steps.length - 1" class="stepper-line" />
      </div>
    </nav>

    <!-- Layout -->
    <div class="wizard-layout" :class="{ 'is-success': currentStep === 4 }">
      <main class="wizard-main">
        <Transition name="step-fade" mode="out-in">

          <!-- ══════ STEP 1: CLIENTE + MASCOTA + MOTIVO ══════ -->
          <section v-if="currentStep === 1" key="step1" class="step-panel">
            <!-- Cliente -->
            <AppCard>
              <template #header>
                <div class="card-header-left">
                  <div class="card-icon"><UserPlus :size="16" /></div>
                  <div>
                    <h3>Cliente</h3>
                    <p class="card-header-sub">Busca por nombre o documento de identidad</p>
                  </div>
                </div>
              </template>

              <template v-if="!clienteSeleccionado">
                <div class="search-bar">
                  <Search :size="15" class="search-icon" />
                  <input
                    v-model="clienteFiltro"
                    type="text"
                    class="form-input search-input"
                    placeholder="Ej: María González o V-12345678…"
                    autocomplete="off"
                  />
                  <span v-if="buscandoCliente" class="search-spinner">
                    <span class="spinner spinner-sm" />
                  </span>
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
                  v-if="clienteFiltro.length >= 2 && !buscandoCliente && !resultadosClientes.length"
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

                <div v-if="resultadosClientes.length" class="results-list">
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
              <span v-if="step1Errors.cliente" class="form-error mt-2">{{ step1Errors.cliente }}</span>
            </AppCard>

            <!-- Mascota -->
            <AppCard v-if="clienteSeleccionado">
              <template #header>
                <div class="card-header-left">
                  <div class="card-icon"><PawPrint :size="16" /></div>
                  <div>
                    <h3>Mascota</h3>
                    <p class="card-header-sub">¿Qué paciente vas a atender?</p>
                  </div>
                </div>
              </template>

              <div v-if="cargandoMascotas" class="mini-load">
                <span class="spinner spinner-sm" /> Cargando mascotas…
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
                <span v-if="step1Errors.mascota" class="form-error mt-2">{{ step1Errors.mascota }}</span>
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
            </AppCard>

            <!-- Motivo -->
            <AppCard>
              <template #header>
                <div class="card-header-left">
                  <div class="card-icon"><MessageSquare :size="16" /></div>
                  <div>
                    <h3>Motivo de la consulta</h3>
                    <p class="card-header-sub">Descripción indicada por el cliente</p>
                  </div>
                </div>
              </template>

              <AppTextarea
                v-model="motivoConsulta"
                :error="step1Errors.motivo"
                :rows="3"
                maxlength="1000"
                placeholder="Ej: Control de vacunas anuales, decaimiento desde ayer…"
              />
              <div class="form-footer">
                <span v-if="step1Errors.motivo" class="form-error">{{ step1Errors.motivo }}</span>
                <span class="char-count">{{ motivoConsulta.length }}/1000</span>
              </div>
            </AppCard>
          </section>

          <!-- ══════ STEP 2: HORARIO ══════ -->
          <section v-else-if="currentStep === 2" key="step2" class="step-panel">
            <!-- Veterinario -->
            <AppCard>
              <template #header>
                <div class="card-header-left">
                  <div class="card-icon"><Stethoscope :size="16" /></div>
                  <div>
                    <h3>Veterinario</h3>
                    <p class="card-header-sub">Selecciona quién atenderá al paciente</p>
                  </div>
                </div>
              </template>

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
                  <span class="pick-actions">
                    <span
                      class="pick-icon-btn"
                      role="button"
                      tabindex="0"
                      :aria-label="`Ver horario de ${v.nombre}`"
                      title="Ver horario de atención"
                      @click.stop="verHorario(v)"
                      @keydown.enter.stop.prevent="verHorario(v)"
                      @keydown.space.stop.prevent="verHorario(v)"
                    >
                      <CalendarClock :size="14" />
                    </span>
                    <span
                      v-if="selectedVeterinario === v.id"
                      class="pick-check"
                    >
                      <CheckCircle2 :size="14" />
                    </span>
                  </span>
                </button>
              </div>
              <span v-if="step2Errors.veterinario" class="form-error mt-2">{{ step2Errors.veterinario }}</span>
            </AppCard>

            <!-- Servicio -->
            <AppCard>
              <template #header>
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
              </template>

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
                      <span class="meta-chip meta-price">
                        <DollarSign :size="12" /> {{ fmtUsd(s.precioUsd) }}
                      </span>
                    </div>
                  </div>
                </button>
              </div>
              <span v-if="step2Errors.servicio" class="form-error mt-2">{{ step2Errors.servicio }}</span>
            </AppCard>

            <!-- Fecha -->
            <AppCard>
              <template #header>
                <div class="card-header-left">
                  <div class="card-icon"><CalendarDays :size="16" /></div>
                  <div>
                    <h3>Fecha</h3>
                    <p class="card-header-sub">Selecciona el día de la cita</p>
                  </div>
                </div>
              </template>

              <AppInput
                v-model="fecha"
                type="date"
                :min="hoy"
                :error="step2Errors.fecha ? 'Selecciona una fecha' : ''"
              />
            </AppCard>

            <!-- Bloques horarios -->
            <AppCard v-if="fecha && selectedServicio && selectedVeterinario">
              <template #header>
                <div class="card-header-left">
                  <div class="card-icon"><Clock :size="16" /></div>
                  <div>
                    <h3>Horarios disponibles</h3>
                    <p class="card-header-sub">{{ fmtFecha(fecha) }}</p>
                  </div>
                </div>
              </template>

              <div v-if="cargandoBloques" class="mini-load">
                <span class="spinner spinner-sm" /> Consultando disponibilidad…
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

              <AppAlert v-if="step2Errors.bloque" variant="warning" class="mt-4">
                {{ step2Errors.bloque }}
              </AppAlert>

              <p class="hint-teal">
                <DollarSign :size="12" />
                El monto en bolívares se calcula con la tasa oficial al confirmar.
              </p>
            </AppCard>
          </section>

          <!-- ══════ STEP 3: COBRO ══════ -->
          <section v-else-if="currentStep === 3" key="step3" class="step-panel">
            <AppCard>
              <template #header>
                <div class="card-header-left">
                  <div class="card-icon"><CreditCard :size="16" /></div>
                  <div>
                    <h3>Método de pago presencial</h3>
                    <p class="card-header-sub">Selecciona cómo está pagando el cliente</p>
                  </div>
                </div>
              </template>

              <AppAlert v-if="pagoError" variant="error" class="mb-4">{{ pagoError }}</AppAlert>

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
              <span v-if="step3Errors.metodo" class="form-error mt-2">{{ step3Errors.metodo }}</span>

              <Transition name="expand">
                <div v-if="metodoSeleccionado" class="dynamic-fields">
                  <div
                    v-if="'referencia' in (metodoSeleccionado.camposRequeridos || {})"
                    class="form-group"
                  >
                    <label class="form-label">
                      Número de referencia <span class="required">*</span>
                    </label>
                    <AppInput
                      v-model="referencia"
                      placeholder="Ej: 0000123456789"
                      :error="step3Errors.referencia"
                    />
                    <span v-if="step3Errors.referencia" class="form-error">{{ step3Errors.referencia }}</span>
                  </div>

                  <div v-for="campo in camposDinamicos" :key="campo.key" class="form-group">
                    <label class="form-label">
                      {{ fmtFieldLabel(campo.key) }} <span class="required">*</span>
                    </label>
                    <AppSelect
                      v-if="campo.key === 'banco'"
                      v-model="datosPago[campo.key]"
                      :error="step3Errors[campo.key]"
                    >
                      <option value="" disabled>Seleccione el banco</option>
                      <option v-for="b in BANCOS_VENEZUELA" :key="b.codigo" :value="b.nombre">
                        {{ b.codigo }} - {{ b.nombre }}
                      </option>
                    </AppSelect>
                    <AppInput
                      v-else
                      v-model="datosPago[campo.key]"
                      :error="step3Errors[campo.key]"
                    />
                    <span v-if="step3Errors[campo.key]" class="form-error">{{ step3Errors[campo.key] }}</span>
                  </div>
                </div>
              </Transition>

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

              <AppButton
                variant="primary"
                size="lg"
                block
                class="mt-5"
                :loading="procesando"
                @click="confirmarYFacturar"
              >
                <template #icon-left><CreditCard :size="18" /></template>
                {{ procesando ? 'Procesando…' : 'Confirmar y Facturar' }}
              </AppButton>
            </AppCard>
          </section>

          <!-- ══════ STEP 4: ÉXITO ══════ -->
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

              <AppAlert
                v-if="envio"
                :variant="envio.enviado ? 'success' : 'warning'"
              >
                {{ envio.mensaje }}
              </AppAlert>

              <div class="success-actions">
                <AppButton variant="secondary" :loading="imprimiendo" @click="imprimirComprobante">
                  <template #icon-left><Printer :size="15" /></template>
                  {{ imprimiendo ? 'Imprimiendo…' : 'Imprimir' }}
                </AppButton>
                <AppButton variant="secondary" :loading="enviando" @click="enviarPorCorreo">
                  <template #icon-left><Mail :size="15" /></template>
                  {{ enviando ? 'Enviando…' : 'Enviar por correo' }}
                </AppButton>
                <AppButton variant="primary" @click="volverAlCalendario">
                  <template #icon-left><ArrowLeft :size="15" /></template>
                  Volver al calendario
                </AppButton>
              </div>
            </div>
          </section>
        </Transition>
      </main>

      <!-- Sidebar resumen -->
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
            <div v-if="resumenActual.totalBs" class="total-bs">
              {{ resumenActual.totalBs }}
            </div>
            <p class="summary-note">
              <Lock :size="11" />
              {{ resumenActual.totalBs
                ? 'Monto calculado con la tasa oficial actual.'
                : 'El monto en Bs. se calcula con la tasa oficial.' }}
            </p>
          </footer>
        </div>
      </aside>
    </div>

    <!-- Step actions -->
    <div v-if="currentStep < 4" class="step-actions">
      <AppButton variant="ghost" @click="cancelar">
        <template #icon-left><X :size="15" /></template>
        Cancelar
      </AppButton>
      <div class="step-actions-right">
        <AppButton
          v-if="currentStep > 1"
          variant="secondary"
          @click="currentStep--; pagoError = ''"
        >
          <template #icon-left><ArrowLeft :size="15" /></template>
          Atrás
        </AppButton>
        <AppButton
          v-if="currentStep === 1"
          variant="primary"
          @click="validarStep1() && (currentStep = 2)"
        >
          Siguiente
          <template #icon-right><ArrowRight :size="15" /></template>
        </AppButton>
        <AppButton
          v-if="currentStep === 2"
          variant="primary"
          :disabled="!selectedBloque"
          @click="validarStep2() && (currentStep = 3)"
        >
          Ir al cobro
          <template #icon-right><ArrowRight :size="15" /></template>
        </AppButton>
      </div>
    </div>

    <!-- ═══ MODAL: Horario del veterinario ═══ -->
    <AppModal
      :model-value="horarioModalVisible"
      :title="vetHorario?.nombre || 'Horario de atención'"
      :subtitle="vetHorario?.especialidad || 'Veterinario'"
      size="md"
      @update:model-value="cerrarHorarioModal"
    >
      <HorarioSemanalCard :horario="vetHorario?.horarioAtencion" />
    </AppModal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Search, X, UserPlus, PawPrint, Stethoscope, CalendarDays, Clock,
  CreditCard, Printer, Mail, CheckCircle2, AlertTriangle, ArrowLeft,
  ArrowRight, ChevronRight, ClipboardList, MessageSquare,
  DollarSign, Check, Sparkles, Lock, CalendarClock,
} from 'lucide-vue-next'
import * as clientesApi from '@/api/clientes.api'
import { getServicios, getVeterinarios, getDisponibilidad, agendarMostrador } from '@/api/citas.api'
import { getMetodosPresenciales, enviarFactura, descargarFactura, getTasaCambio } from '@/api/pagos.api'
import { getMascotasPorCliente } from '@/api/mascotas.api'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppSelect from '@/components/ui/AppSelect.vue'
import AppTextarea from '@/components/ui/AppTextarea.vue'
import PetAvatar from '@/components/ui/PetAvatar.vue'
import EntityAvatar from '@/components/ui/EntityAvatar.vue'

import HorarioSemanalCard from '@/components/ui/HorarioSemanalCard.vue'
import AppModal from '@/components/ui/AppModal.vue'

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

/* STEP 1 */
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

/* STEP 2 */
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

/* STEP 3 */
const metodos = ref([])
const selectedMetodo = ref(null)
const datosPago = ref({})
const referencia = ref('')
const fondosConfirmados = ref(false)
const step3Errors = ref({})
const procesando = ref(false)
const pagoError = ref('')
const tasaCambio = ref(null)


/* STEP 4 */
const resultado = ref(null)
const envio = ref(null)

const horarioModalVisible = ref(false)
const vetHorario = ref(null)

function verHorario(v) {
  vetHorario.value = v
  horarioModalVisible.value = true
}

function cerrarHorarioModal() {
  horarioModalVisible.value = false
  vetHorario.value = null
}

const enviando = ref(false)
const imprimiendo = ref(false)

const servicioSeleccionado = computed(() =>
  servicios.value.find((s) => s.id === selectedServicio.value) || null
)
const metodoSeleccionado = computed(() =>
  metodos.value.find((m) => m.id === selectedMetodo.value) || null
)
const camposDinamicos = computed(() => {
  if (!metodoSeleccionado.value?.camposRequeridos) return []
  return Object.entries(metodoSeleccionado.value.camposRequeridos)
    .filter(([key]) => key !== 'referencia')
    .map(([key]) => ({ key }))
})

const resumenActual = computed(() => {
  const servicio = servicioSeleccionado.value
  const precioUsd = servicio?.precioUsd ?? null
  const totalBs = (precioUsd != null && tasaCambio.value != null)
    ? precioUsd * tasaCambio.value
    : null

  return {
    cliente: clienteSeleccionado.value?.nombreCompleto || null,
    mascota: mascotas.value.find((m) => m.idMascota === selectedMascota.value)?.nombre || null,
    veterinario: veterinarios.value.find((v) => v.id === selectedVeterinario.value)?.nombre || null,
    servicio: servicio?.nombre || null,
    fecha: fecha.value ? fmtFecha(fecha.value) : null,
    hora: selectedBloque.value
      ? `${fmtHora(selectedBloque.value.horaInicio)} – ${fmtHora(selectedBloque.value.horaFin)}`
      : null,
    total: precioUsd != null ? fmtUsd(precioUsd) : null,
    totalBs: totalBs != null ? fmtBs(totalBs) : null,
  }
})

/* Búsqueda cliente */
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

/* Vet → servicios */
async function onVeterinarioChange() {
  selectedServicio.value = null
  servicios.value = []
  bloques.value = []
  selectedBloque.value = null
  const vet = veterinarios.value.find((v) => v.id === selectedVeterinario.value)
  if (!vet?.especialidad) return
  try {
    const { data } = await getServicios({ especialidad: vet.especialidad })
    servicios.value = data
  } catch { /* silencioso */ }
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

/* Validaciones */
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

/* Confirmar y facturar */
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

/* Acciones del comprobante */
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

/* Helpers */
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

function fmtBs(v) {
  if (v == null) return '—'
  return `Bs. ${Number(v).toLocaleString('es-VE', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`
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

/* Init */
onMounted(async () => {
  // Cargar tasa, veterinarios y métodos en paralelo
  const [vetsRes, metodosRes, tasaRes] = await Promise.allSettled([
    getVeterinarios(),
    getMetodosPresenciales(),
    getTasaCambio(),
  ])

  veterinarios.value = vetsRes.status === 'fulfilled' ? vetsRes.value.data : []
  metodos.value = metodosRes.status === 'fulfilled' ? metodosRes.value.data : []
  tasaCambio.value = tasaRes.status === 'fulfilled' ? Number(tasaRes.value.data.tasa) : null

  if (route.query.clienteDocumento) {
    clienteFiltro.value = route.query.clienteDocumento
    await buscarClientes()
    const exacto = resultadosClientes.value.find(
      (c) => c.documentoIdentidad === route.query.clienteDocumento
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
  padding: var(--space-6) var(--space-6) var(--space-12);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* BREADCRUMB */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-md);
  color: var(--text-secondary);
}
.bc-back {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--neutral-600);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  margin-right: 2px;
}
.bc-back:hover { background: var(--neutral-100); color: var(--brand-700); border-color: var(--border-strong); }
.bc-item { font-weight: var(--font-medium); }
.bc-link {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
  font-size: var(--text-md);
  font-family: inherit;
}
.bc-link:hover { color: var(--brand-700); text-decoration: underline; }
.bc-current { color: var(--text-primary); font-weight: var(--font-bold); }
.bc-sep { color: var(--neutral-300); }

/* STEPPER */
.stepper {
  display: flex;
  align-items: center;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  padding: var(--space-4) var(--space-5);
  box-shadow: var(--shadow-sm);
  gap: var(--space-2);
}
.stepper-step {
  display: flex;
  align-items: center;
  gap: var(--space-3);
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
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  background: var(--neutral-100);
  color: var(--text-tertiary);
  border: 2px solid var(--border-subtle);
  flex-shrink: 0;
  transition: all var(--duration-slow) var(--ease-out);
}
.stepper-step.active .stepper-circle {
  background: var(--brand-700);
  color: var(--text-inverse);
  border-color: var(--brand-700);
  box-shadow: 0 0 0 4px var(--brand-100);
}
.stepper-step.completed .stepper-circle {
  background: var(--brand-700);
  color: var(--text-inverse);
  border-color: var(--brand-700);
}
.stepper-text { display: flex; flex-direction: column; gap: 1px; min-width: 0; }
.stepper-label {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-tertiary);
  transition: color var(--duration-slow) var(--ease-out);
  white-space: nowrap;
}
.stepper-step.active .stepper-label { color: var(--brand-700); }
.stepper-step.completed .stepper-label { color: var(--text-primary); }
.stepper-desc {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.stepper-line {
  flex: 1;
  height: 2px;
  background: var(--border-subtle);
  margin: 0 var(--space-3);
  border-radius: 1px;
  transition: background var(--duration-slow) var(--ease-out);
  min-width: 20px;
}
.stepper-step.completed .stepper-line { background: var(--brand-700); }

/* LAYOUT */
.wizard-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: var(--space-5);
  align-items: start;
}
.wizard-layout.is-success { grid-template-columns: 1fr; }
.wizard-main { min-width: 0; display: flex; flex-direction: column; gap: var(--space-4); }

/* SIDEBAR */
.wizard-sidebar {
  position: sticky;
  top: var(--space-6);
  max-height: calc(100vh - var(--space-12));
  overflow-y: auto;
  min-width: 0;
}
.summary-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.summary-card-header {
  padding: var(--space-5) var(--space-5) var(--space-3);
  border-bottom: 1px solid var(--border-subtle);
}
.summary-card-header h4 { margin: 0 0 2px; font-size: var(--text-lg); font-weight: var(--font-bold); color: var(--text-primary); }
.summary-card-hint { font-size: var(--text-sm); color: var(--text-tertiary); }
.summary-card-body { padding: var(--space-3) var(--space-5) var(--space-4); display: flex; flex-direction: column; gap: var(--space-3); }
.summary-line { display: flex; flex-direction: column; gap: 3px; transition: opacity var(--duration-base) var(--ease-out); }
.summary-line.is-empty { opacity: 0.55; }
.summary-line-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}
.summary-line-value { font-size: var(--text-base); font-weight: var(--font-semibold); color: var(--text-primary); line-height: var(--leading-snug); word-break: break-word; }
.summary-line.is-empty .summary-line-value { color: var(--text-tertiary); font-weight: var(--font-medium); font-style: italic; }
.summary-card-footer {
  padding: var(--space-4) var(--space-5) var(--space-5);
  background: linear-gradient(180deg, var(--neutral-50) 0%, var(--brand-50) 100%);
  border-top: 1px solid var(--border-subtle);
}
.summary-total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-3);
  flex-wrap: wrap;
}
.total-label {
  font-size: var(--text-xs);
  font-weight: var(--font-extrabold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--text-secondary);
  white-space: nowrap;
  flex-shrink: 0;
}
.total-value {
  font-size: 18px;
  font-weight: var(--font-bold);
  color: var(--brand-700);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
  line-height: 1;
  white-space: nowrap;
  margin-left: auto;
}
.summary-note {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: var(--space-3) 0 0;
  font-size: var(--text-xs);
  color: var(--text-secondary);
  line-height: var(--leading-snug);
}
.summary-note svg { flex-shrink: 0; }

/* CARD HEADER */
.card-header-left { display: flex; align-items: center; gap: var(--space-3); min-width: 0; }
.card-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-lg);
  background: var(--brand-50);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* SEARCH */
.search-bar { position: relative; }
.search-icon {
  position: absolute;
  left: var(--space-4);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  pointer-events: none;
}
.search-input { padding-left: 40px; padding-right: 40px; }
.search-spinner {
  position: absolute;
  right: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  color: var(--brand-700);
}
.search-clear {
  position: absolute;
  right: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
}
.search-clear:hover { color: var(--neutral-600); background: var(--neutral-100); }

.results-list {
  margin-top: var(--space-3);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  max-height: 300px;
  overflow-y: auto;
}
.result-row {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: var(--space-3);
  align-items: center;
  width: 100%;
  text-align: left;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface);
  border: none;
  border-bottom: 1px solid var(--neutral-100);
  cursor: pointer;
  font-family: inherit;
  transition: background-color var(--duration-fast) var(--ease-out);
}
.result-row:last-child { border-bottom: none; }
.result-row:hover { background: var(--brand-50); }
.result-info { min-width: 0; }
.result-name { margin: 0; font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.result-meta { margin: 2px 0 0; font-size: var(--text-sm); color: var(--text-secondary); }
.result-arrow { color: var(--neutral-300); flex-shrink: 0; }
.result-row:hover .result-arrow { color: var(--brand-700); }

/* CLIENTE CHIP */
.cliente-chip {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--brand-50);
  border: 1px solid var(--brand-200);
  border-radius: var(--radius-xl);
}
.cliente-chip-info { flex: 1; min-width: 0; }
.cliente-chip-name { margin: 0; font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.cliente-chip-doc { margin: 2px 0 0; font-size: var(--text-sm); color: var(--brand-700); font-weight: var(--font-semibold); }
.btn-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  background: var(--bg-surface);
  border: 1px solid var(--brand-200);
  color: var(--brand-700);
  border-radius: var(--radius-md);
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--duration-fast) var(--ease-out);
  flex-shrink: 0;
}
.btn-chip:hover { background: var(--brand-100); border-color: var(--brand-700); }

/* PICK GRID */
.pick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--space-3);
}
.pick-grid.with-error {
  padding: var(--space-1);
  border-radius: var(--radius-xl);
  background: var(--danger-50);
  border: 1px solid var(--danger-200);
}
.pick-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface);
  border: 1.5px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: all var(--duration-base) var(--ease-out);
  overflow: hidden;
}
.pick-card:hover {
  border-color: var(--brand-200);
  background: var(--brand-50);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -8px rgba(15, 118, 110, 0.2);
}
.pick-card.selected {
  border-color: var(--brand-700);
  background: var(--brand-50);
  box-shadow: 0 0 0 3px var(--brand-100);
}
.pick-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
.pick-name { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.pick-meta { font-size: var(--text-sm); color: var(--text-secondary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.pick-check {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--brand-700);
  color: var(--text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  animation: checkPop 0.25s var(--ease-spring);
}
@keyframes checkPop { 0% { transform: scale(0); } 100% { transform: scale(1); } }

/* EMPTY */
.empty-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-7) var(--space-5);
  color: var(--text-tertiary);
  text-align: center;
  font-size: var(--text-md);
}
.empty-inline p { margin: 0; }
.link-teal { color: var(--brand-700); font-weight: var(--font-bold); text-decoration: none; }
.link-teal:hover { text-decoration: underline; }

/* SERVICE */
.service-list { display: flex; flex-direction: column; gap: var(--space-2); }
.service-list.with-error {
  padding: var(--space-1);
  border-radius: var(--radius-xl);
  background: var(--danger-50);
  border: 1px solid var(--danger-200);
}
.service-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface);
  border: 1.5px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  width: 100%;
  transition: all var(--duration-base) var(--ease-out);
}
.service-item:hover { border-color: var(--brand-200); background: var(--brand-50); }
.service-item.selected { border-color: var(--brand-700); background: var(--brand-50); box-shadow: 0 0 0 3px var(--brand-100); }
.service-item-radio {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid var(--neutral-300);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
  transition: border-color var(--duration-base);
}
.service-item.selected .service-item-radio { border-color: var(--brand-700); }
.service-item-radio-inner {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--brand-700);
  animation: radioPop 0.2s var(--ease-out);
}
@keyframes radioPop { 0% { transform: scale(0); } 100% { transform: scale(1); } }
.service-item-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: var(--space-1); }
.service-item-name { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); line-height: 1.3; }
.service-item-meta { display: flex; align-items: center; gap: var(--space-2); flex-wrap: wrap; }
.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--brand-700);
  background: var(--bg-surface);
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid var(--brand-200);
}
.meta-price { color: var(--text-primary); border-color: var(--border-subtle); }

/* SLOTS */
.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: var(--space-3);
}
.slot-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: var(--space-3);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  background: var(--bg-surface);
  cursor: pointer;
  transition: all var(--duration-base) var(--ease-out);
  position: relative;
  min-height: 62px;
  font-family: inherit;
}
.slot-btn:hover { border-color: var(--brand-700); background: var(--brand-50); transform: translateY(-1px); box-shadow: 0 6px 16px -8px rgba(15, 118, 110, 0.25); }
.slot-btn.is-selected { background: var(--brand-700); border-color: var(--brand-700); color: var(--text-inverse); box-shadow: 0 6px 16px -6px rgba(15, 118, 110, 0.5); }
.slot-btn.is-conflicto { border-color: var(--danger-500); background: var(--danger-50); animation: pulseWarn 1.2s ease-in-out; }
@keyframes pulseWarn {
  0%, 100% { box-shadow: 0 0 0 0 rgba(220, 38, 38, 0.2); }
  50% { box-shadow: 0 0 0 6px rgba(220, 38, 38, 0.15); }
}
.slot-time { font-size: var(--text-md); font-weight: var(--font-bold); color: var(--text-primary); transition: color var(--duration-base); }
.slot-btn.is-selected .slot-time { color: var(--text-inverse); }
.slot-sep { font-size: var(--text-2xs); color: var(--text-tertiary); }
.slot-btn.is-selected .slot-sep { color: rgba(255, 255, 255, 0.7); }
.slot-end { font-size: var(--text-sm); font-weight: var(--font-medium); color: var(--text-secondary); }
.slot-btn.is-selected .slot-end { color: rgba(255, 255, 255, 0.85); }
.slot-check {
  position: absolute;
  top: var(--space-1);
  right: var(--space-1);
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
}
.hint-teal {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: var(--space-4) 0 0;
  padding: var(--space-3) var(--space-4);
  font-size: var(--text-sm);
  color: var(--brand-700);
  background: var(--brand-50);
  border: 1px solid var(--brand-200);
  border-radius: var(--radius-md);
}

/* METHODS */
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
.method-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.method-name { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); }
.method-desc { font-size: var(--text-sm); color: var(--text-secondary); }

.dynamic-fields {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  margin-top: var(--space-5);
  padding-top: var(--space-5);
  border-top: 1px solid var(--border-subtle);
}
.form-group { display: flex; flex-direction: column; gap: var(--space-2); }
.form-label { display: flex; align-items: center; gap: var(--space-1); font-size: var(--text-md); font-weight: var(--font-semibold); color: var(--neutral-700); }
.required { color: var(--danger-500); }
.form-error { font-size: var(--text-sm); color: var(--danger-600); font-weight: var(--font-semibold); }
.form-error.mt-2 { display: block; margin-top: var(--space-2); }

/* CHECK */
.check-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  margin-top: var(--space-5);
  background: var(--bg-surface-alt);
  border: 1.5px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--duration-fast) var(--ease-out);
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
  transition: all var(--duration-fast);
  color: var(--text-inverse);
}
.check-row .checkbox-input:checked + .checkbox-box {
  background: var(--brand-700);
  border-color: var(--brand-700);
}
.checkbox-label { font-size: var(--text-md); font-weight: var(--font-semibold); color: var(--text-primary); }

/* MINI LOAD */
.mini-load {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  font-size: var(--text-md);
  padding: var(--space-3) 0;
}
.form-footer { display: flex; justify-content: space-between; align-items: center; margin-top: var(--space-2); }
.char-count { font-size: var(--text-xs); color: var(--text-tertiary); margin-left: auto; }

/* SUCCESS */
.step-success { max-width: 720px; margin: 0 auto; }
.success-container {
  background: var(--bg-surface);
  border-radius: var(--radius-3xl);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-sm);
  padding: var(--space-12) var(--space-8);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: var(--space-3);
}
.success-icon {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--success-50) 0%, var(--brand-50) 100%);
  border: 3px solid var(--success-500);
  color: var(--success-600);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
  animation: successPop 0.5s var(--ease-spring);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}
.success-title { font-size: var(--text-4xl); font-weight: var(--font-bold); color: var(--text-primary); margin: 0; letter-spacing: var(--tracking-tight); }
.success-message { font-size: var(--text-base); color: var(--text-secondary); margin: 0; max-width: 460px; line-height: var(--leading-relaxed); }

.factura-box {
  margin-top: var(--space-2);
  padding: var(--space-2) var(--space-5);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  width: 100%;
  max-width: 520px;
  text-align: left;
}
.factura-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-subtle);
  font-size: var(--text-md);
}
.factura-row:last-child { border-bottom: none; }
.factura-label {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
  white-space: nowrap;
  flex-shrink: 0;
}
.factura-value { font-size: var(--text-md); font-weight: var(--font-bold); color: var(--text-primary); text-align: right; word-break: break-word; }
.mono { font-family: var(--font-mono); font-size: var(--text-sm); background: var(--neutral-100); padding: 2px var(--space-2); border-radius: var(--radius-sm); }
.factura-row.total { border-top: 2px solid var(--border-subtle); padding-top: var(--space-4); margin-top: var(--space-1); }
.total-value { color: var(--brand-700); display: flex; flex-direction: column; align-items: flex-end; }

.total-bs {
  margin-top: var(--space-1);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
  text-align: right;
  letter-spacing: -0.01em;
  font-variant-numeric: tabular-nums;
}

.success-actions { display: flex; gap: var(--space-3); margin-top: var(--space-5); flex-wrap: wrap; justify-content: center; }

/* STEP ACTIONS */
.step-actions {
  position: sticky;
  bottom: var(--space-4);
  z-index: var(--z-sticky);
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--space-5);
  padding: var(--space-3) var(--space-5);
  border-radius: var(--radius-2xl);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid var(--border-subtle);
  box-shadow: var(--shadow-lg);
  gap: var(--space-3);
  flex-wrap: wrap;
}
.step-actions-right { display: flex; align-items: center; gap: var(--space-3); margin-left: auto; }

/* TRANSITIONS */
.step-fade-enter-active, .step-fade-leave-active {
  transition: opacity var(--duration-slow) var(--ease-out),
              transform var(--duration-slow) var(--ease-out);
}
.step-fade-enter-from { opacity: 0; transform: translateY(8px); }
.step-fade-leave-to { opacity: 0; transform: translateY(-8px); }
.expand-enter-active, .expand-leave-active {
  transition: opacity var(--duration-slow) var(--ease-out),
              transform var(--duration-slow) var(--ease-out);
  overflow: hidden;
}
.expand-enter-from, .expand-leave-to { opacity: 0; transform: translateY(-6px); }

/* UTIL */
.mt-2 { margin-top: var(--space-2); }
.mt-4 { margin-top: var(--space-4); }
.mt-5 { margin-top: var(--space-5); }
.mb-4 { margin-bottom: var(--space-4); }

/* RESPONSIVE */
@media (max-width: 1024px) {
  .wizard-layout { grid-template-columns: 1fr; }
  .wizard-sidebar { position: static; max-height: none; overflow: visible; }
}
@media (max-width: 768px) {
  .agendar-mostrador { padding: var(--space-4); }
  .stepper {
    flex-direction: column;
    align-items: flex-start;
    padding: var(--space-4);
    gap: var(--space-3);
  }
  .stepper-step { width: 100%; flex: none; }
  .stepper-step:last-child { flex: none; }
  .stepper-line {
    position: absolute;
    top: 34px;
    left: 17px;
    width: 2px;
    height: 14px;
    margin: 0;
    min-width: 0;
  }
  .stepper-step:last-child .stepper-line { display: none; }
  .step-actions { flex-direction: column-reverse; padding: var(--space-3); bottom: var(--space-2); }
  .step-actions-right { width: 100%; flex-direction: column-reverse; }
  .step-actions :deep(.btn) { width: 100%; }
  .pick-grid { grid-template-columns: 1fr; }
  .success-actions { flex-direction: column; width: 100%; }
  .success-actions :deep(.btn) { width: 100%; }
}
@media (max-width: 480px) {
  .slots-grid { grid-template-columns: repeat(2, 1fr); }
  .success-container { padding: var(--space-8) var(--space-5); }
  .success-icon { width: 72px; height: 72px; }
  .success-title { font-size: var(--text-3xl); }
  .factura-row { flex-direction: column; align-items: flex-start; gap: var(--space-1); }
  .factura-value { text-align: left; }
  .total-value { align-items: flex-start; }
}

.pick-actions {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  flex-shrink: 0;
}

.pick-icon-btn {
  width: 26px;
  height: 26px;
  border-radius: var(--radius-md);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  color: var(--text-tertiary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.pick-icon-btn:hover {
  background: var(--brand-50);
  border-color: var(--brand-200);
  color: var(--brand-700);
}
.pick-icon-btn:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px var(--brand-100);
}
.pick-card.selected .pick-icon-btn {
  background: var(--bg-surface);
  border-color: var(--brand-200);
  color: var(--brand-700);
}
</style>