<template>
  <div class="appointment-view">
    <ToastContainer />

    <!-- ═══ HERO ═══ -->
    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <Sparkles :size="12" /> Nueva reserva
        </span>
        <h1>Solicitar Cita</h1>
        <p class="page-header-sub">
          Reserva un turno para tu mascota en 3 simples pasos.
        </p>
      </div>
    </header>

    <!-- ═══ STEPPER ═══ -->
    <nav class="stepper" aria-label="Progreso de la reserva">
      <div
        v-for="(step, idx) in steps"
        :key="step.num"
        class="stepper-step"
        :class="{ active: currentStep === step.num, completed: currentStep > step.num }"
      >
        <div class="stepper-circle">
          <Check v-if="currentStep > step.num" :size="16" />
          <span v-else>{{ step.num }}</span>
        </div>
        <div class="stepper-text">
          <span class="stepper-label">{{ step.label }}</span>
          <span class="stepper-desc">{{ step.desc }}</span>
        </div>
        <div v-if="idx < steps.length - 1" class="stepper-line" />
      </div>
    </nav>

    <!-- ═══ LAYOUT ═══ -->
    <div class="wizard-layout" :class="{ 'is-success': currentStep === 4 }">
      <main class="wizard-main">
        <Transition name="step-fade" mode="out-in">
          <!-- ══════ STEP 1 ══════ -->
          <section v-if="currentStep === 1" key="step1" class="step-panel">
            <div v-if="isLoadingInitial" class="card">
              <div class="card-body">
                <div class="loading-state">
                  <span class="spinner spinner-lg" />
                  <p>Cargando datos necesarios…</p>
                </div>
              </div>
            </div>

            <template v-else>
              <AppAlert v-if="loadError" variant="error" :action="'Reintentar'" @action="loadInitialData">
                {{ loadError }}
              </AppAlert>

              <template v-else>
                <!-- ══ MASCOTA ══ -->
                <AppCard
                  title="¿Para quién es la cita?"
                  subtitle="Elige la mascota que recibirá la atención"
                >
                  <template #header>
                    <div class="card-header-left">
                      <div class="card-icon"><PawPrint :size="16" /></div>
                      <div>
                        <h3>¿Para quién es la cita?</h3>
                        <p class="card-header-sub">Elige la mascota que recibirá la atención</p>
                      </div>
                    </div>
                  </template>

                  <div v-if="!mascotas.length" class="empty-inline">
                    <PawPrint :size="28" />
                    <p>No tienes mascotas registradas.</p>
                    <button type="button" class="btn-link" @click="router.push('/cliente/mascotas')">
                      Registrar mascota
                    </button>
                  </div>

                  <template v-else>
                    <div v-if="mostrarBusquedaMascotas" class="search-bar">
                      <Search :size="15" class="search-icon" />
                      <input
                        v-model="busquedaMascota"
                        type="text"
                        class="form-input search-input"
                        placeholder="Buscar por nombre, raza o especie…"
                        aria-label="Buscar mascota"
                      />
                      <button
                        v-if="busquedaMascota"
                        type="button"
                        class="search-clear"
                        aria-label="Limpiar búsqueda"
                        @click="busquedaMascota = ''"
                      >
                        <X :size="13" />
                      </button>
                    </div>

                    <p v-if="busquedaMascota" class="results-count">
                      {{ mascotasFiltradas.principales.length }} de {{ mascotas.length }} mascotas
                    </p>

                    <div v-if="mascotasFiltradas.seleccionada" class="pinned-section">
                      <span class="pinned-label"><Check :size="12" /> Seleccionada</span>
                      <button
                        type="button"
                        class="pick-card selected"
                        title="Quitar filtros para verla en la lista"
                        @click="busquedaMascota = ''"
                      >
                        <PetAvatar :nombre-especie="mascotasFiltradas.seleccionada.nombreEspecie" size="md" />
                        <div class="pick-info">
                          <span class="pick-name">{{ mascotasFiltradas.seleccionada.nombre }}</span>
                          <span class="pick-meta">
                            {{ [mascotasFiltradas.seleccionada.nombreRaza, mascotasFiltradas.seleccionada.nombreEspecie].filter(Boolean).join(' · ') || 'Mascota' }}
                          </span>
                        </div>
                        <span class="pick-check"><Check :size="13" /></span>
                      </button>
                    </div>

                    <div
                      class="pick-grid"
                      :class="{
                        'with-error': stepErrors.mascota,
                        'scrollable': scrollMascotas && !busquedaMascota,
                      }"
                    >
                      <button
                        v-for="m in mascotasFiltradas.principales"
                        :key="m.idMascota"
                        type="button"
                        class="pick-card"
                        :class="{ selected: selectedMascota === m.idMascota }"
                        @click="selectedMascota = m.idMascota"
                      >
                        <PetAvatar :nombre-especie="m.nombreEspecie" size="md" />
                        <div class="pick-info">
                          <span class="pick-name">{{ m.nombre }}</span>
                          <span class="pick-meta">
                            {{ [m.nombreRaza, m.nombreEspecie].filter(Boolean).join(' · ') || 'Mascota' }}
                          </span>
                        </div>
                        <span v-if="selectedMascota === m.idMascota" class="pick-check">
                          <Check :size="13" />
                        </span>
                      </button>
                    </div>

                    <div v-if="busquedaMascota && !mascotasFiltradas.principales.length && !mascotasFiltradas.seleccionada" class="empty-inline">
                      <Search :size="28" />
                      <p>No hay mascotas que coincidan con "{{ busquedaMascota }}".</p>
                      <button type="button" class="btn-link" @click="busquedaMascota = ''">Limpiar búsqueda</button>
                    </div>
                  </template>

                  <p v-if="stepErrors.mascota" class="form-error mt-2">{{ stepErrors.mascota }}</p>
                </AppCard>

                <!-- ══ VETERINARIO ══ -->
                <AppCard>
                  <template #header>
                    <div class="card-header-left">
                      <div class="card-icon"><Stethoscope :size="16" /></div>
                      <div>
                        <h3>¿Con quién quieres atenderte?</h3>
                        <p class="card-header-sub">Filtra por tipo de atención o elige directamente</p>
                      </div>
                    </div>
                  </template>

                  <div v-if="!veterinarios.length" class="empty-inline">
                    <Stethoscope :size="28" />
                    <p>No hay veterinarios disponibles en este momento.</p>
                  </div>

                  <template v-else>
                    <div v-if="mostrarBusquedaVets" class="search-bar">
                      <Search :size="15" class="search-icon" />
                      <input
                        v-model="busquedaVeterinario"
                        type="text"
                        class="form-input search-input"
                        placeholder="Buscar por nombre…"
                        aria-label="Buscar veterinario"
                      />
                      <button
                        v-if="busquedaVeterinario"
                        type="button"
                        class="search-clear"
                        aria-label="Limpiar búsqueda"
                        @click="busquedaVeterinario = ''"
                      >
                        <X :size="13" />
                      </button>
                    </div>

                    <div v-if="mostrarChipsVets && chipsEspecialidades.length" class="chips-row">
                      <button
                        type="button"
                        class="chip"
                        :class="{ active: !filtroEspecialidad }"
                        @click="filtroEspecialidad = ''"
                      >
                        Todas
                        <span class="chip-count">{{ veterinarios.length }}</span>
                      </button>
                      <button
                        v-for="chip in chipsEspecialidades"
                        :key="chip.value"
                        type="button"
                        class="chip"
                        :class="{
                          active: filtroEspecialidad === chip.value,
                          'is-empty': chip.disabled,
                        }"
                        :disabled="chip.disabled"
                        :title="chip.disabled
                          ? `Sin veterinarios disponibles en ${chip.label}`
                          : `${chip.count} veterinario${chip.count === 1 ? '' : 's'} en ${chip.label}`"
                        @click="filtroEspecialidad = filtroEspecialidad === chip.value ? '' : chip.value"
                      >
                        {{ chip.label }}
                        <span class="chip-count">{{ chip.count }}</span>
                      </button>
                    </div>

                    <p v-if="veterinariosFiltrados.hayFiltro" class="results-count">
                      {{ veterinariosFiltrados.principales.length }} de {{ veterinarios.length }} veterinarios
                    </p>

                    <div v-if="veterinariosFiltrados.seleccionado" class="pinned-section">
                      <span class="pinned-label"><Check :size="12" /> Seleccionado</span>
                      <button
                        type="button"
                        class="pick-card selected"
                        title="Quitar filtros para verlo en la lista"
                        @click="busquedaVeterinario = ''; filtroEspecialidad = ''"
                      >
                        <EntityAvatar :nombre="veterinariosFiltrados.seleccionado.nombre" tipo="veterinario" size="md" />
                        <div class="pick-info">
                          <span class="pick-name">{{ veterinariosFiltrados.seleccionado.nombre }}</span>
                          <span class="pick-meta">{{ ESPECIALIDAD_LABELS[veterinariosFiltrados.seleccionado.especialidad] || veterinariosFiltrados.seleccionado.especialidad || 'Veterinario' }}</span>
                        </div>
                        <span class="pick-check"><Check :size="13" /></span>
                      </button>
                    </div>

                    <div
                      class="pick-grid"
                      :class="{
                        'with-error': stepErrors.veterinario,
                        'scrollable': scrollVets && !veterinariosFiltrados.hayFiltro,
                      }"
                    >
                    <button
                      v-for="v in veterinariosFiltrados.principales"
                      :key="v.id"
                      type="button"
                      class="pick-card"
                      :class="{ selected: selectedVeterinario === v.id }"
                      @click="selectVeterinario(v)"
                    >
                      <EntityAvatar :nombre="v.nombre" tipo="veterinario" size="md" />
                      <div class="pick-info">
                        <span class="pick-name">{{ v.nombre }}</span>
                        <span class="pick-meta">{{ ESPECIALIDAD_LABELS[v.especialidad] || v.especialidad || 'Veterinario' }}</span>
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
                          <Check :size="13" />
                        </span>
                      </span>
                    </button>
                    </div>

                    <div v-if="veterinariosFiltrados.hayFiltro && !veterinariosFiltrados.principales.length && !veterinariosFiltrados.seleccionado" class="empty-inline">
                      <Search :size="28" />
                      <p>
                        No hay veterinarios que coincidan con
                        <template v-if="busquedaVeterinario">"{{ busquedaVeterinario }}"</template>
                        <template v-if="busquedaVeterinario && filtroEspecialidad"> y </template>
                        <template v-if="filtroEspecialidad">el tipo de atención "{{ ESPECIALIDAD_LABELS[filtroEspecialidad] || filtroEspecialidad }}"</template>.
                      </p>
                      <button type="button" class="btn-link" @click="busquedaVeterinario = ''; filtroEspecialidad = ''">
                        Limpiar filtros
                      </button>
                    </div>
                  </template>

                  <p v-if="stepErrors.veterinario" class="form-error mt-2">{{ stepErrors.veterinario }}</p>
                </AppCard>

                <!-- ══ SERVICIO ══ -->
                <AppCard>
                  <template #header>
                    <div class="card-header-left">
                      <div class="card-icon"><ClipboardList :size="16" /></div>
                      <div>
                        <h3>Servicio</h3>
                        <p class="card-header-sub">
                          {{ !selectedVeterinario
                            ? 'Primero elige un veterinario'
                            : (isLoadingServicios
                                ? 'Cargando servicios disponibles…'
                                : 'Elige el tipo de atención que necesitas') }}
                        </p>
                      </div>
                    </div>
                  </template>
                  <template #header-actions>
                    <span v-if="isLoadingServicios" class="label-loading">
                      <span class="spinner spinner-sm" /> Cargando…
                    </span>
                  </template>

                  <div v-if="!selectedVeterinario" class="empty-inline">
                    <Stethoscope :size="28" />
                    <p>Selecciona un veterinario para ver sus servicios disponibles.</p>
                  </div>
                  <div v-else-if="isLoadingServicios" class="service-skeleton">
                    <div v-for="i in 3" :key="i" class="skeleton skeleton-row-svc" />
                  </div>
                  <div v-else-if="!servicios.length" class="empty-inline">
                    <ClipboardList :size="28" />
                    <p>Este veterinario aún no tiene servicios configurados.</p>
                  </div>
                  <div v-else class="service-list" :class="{ 'with-error': stepErrors.servicio }">
                    <button
                      v-for="s in servicios"
                      :key="s.id"
                      type="button"
                      class="service-item"
                      :class="{ selected: selectedServicio === s.id }"
                      @click="selectedServicio = s.id"
                    >
                      <span class="service-item-radio">
                        <span v-if="selectedServicio === s.id" class="service-item-radio-inner" />
                      </span>
                      <div class="service-item-body">
                        <span class="service-item-name">{{ s.nombre }}</span>
                        <span v-if="s.descripcion" class="service-item-desc">{{ s.descripcion }}</span>
                        <div class="service-item-meta">
                          <span class="meta-chip"><Clock :size="12" /> {{ s.duracionMinutos }} min</span>
                          <span class="meta-chip meta-price"><DollarSign :size="12" /> {{ formatCurrency(s.precioUsd) }}</span>
                          <span v-if="s.tipoAtencion" class="meta-badge">{{ s.tipoAtencion }}</span>
                        </div>
                      </div>
                    </button>
                  </div>

                  <p v-if="stepErrors.servicio" class="form-error mt-2">{{ stepErrors.servicio }}</p>
                </AppCard>

                <!-- ══ MOTIVO ══ -->
                <AppCard>
                  <template #header>
                    <div class="card-header-left">
                      <div class="card-icon"><MessageSquare :size="16" /></div>
                      <div>
                        <h3>Motivo de la consulta</h3>
                        <p class="card-header-sub">Cuéntanos brevemente qué sucede</p>
                      </div>
                    </div>
                  </template>

                  <AppTextarea
                    v-model="motivoConsulta"
                    :error="stepErrors.motivo"
                    placeholder="Ej: Mi perro lleva 2 días sin comer y presenta decaimiento…"
                    maxlength="1000"
                    :rows="4"
                  />
                  <div class="form-footer">
                    <span v-if="stepErrors.motivo" class="form-error">{{ stepErrors.motivo }}</span>
                    <span class="char-count">{{ motivoConsulta.length }}/1000</span>
                  </div>
                </AppCard>
              </template>
            </template>
          </section>

          <!-- ══════ STEP 2: HORARIO ══════ -->
          <section v-else-if="currentStep === 2" key="step2" class="step-panel">
            <AppCard>
              <template #header>
                <div class="card-header-left">
                  <div class="card-icon"><CalendarDays :size="16" /></div>
                  <div>
                    <h3>Selecciona el horario</h3>
                    <p class="card-header-sub">Elige el día y la hora que mejor te convenga</p>
                  </div>
                </div>
              </template>
              <template #header-actions>
                <span class="vet-badge">
                  <Stethoscope :size="13" />
                  {{ vetSeleccionado?.nombre || '' }} · {{ servicioSeleccionado?.nombre || '' }}
                </span>
              </template>

              <div class="calendar">
                <div class="calendar-nav">
                  <button type="button" class="cal-nav-btn" :disabled="!canGoPrevMonth" aria-label="Mes anterior" @click="prevMonth">
                    <ChevronLeft :size="18" />
                  </button>
                  <div class="cal-month-wrap">
                    <span class="cal-month-year">{{ calendarMonthLabel }}</span>
                    <span class="cal-month-hint">{{ MAX_MONTHS_AHEAD }} meses disponibles</span>
                  </div>
                  <button type="button" class="cal-nav-btn" :disabled="!canGoNextMonth" aria-label="Mes siguiente" @click="nextMonth">
                    <ChevronRight :size="18" />
                  </button>
                </div>

                <div class="calendar-weekdays">
                  <span v-for="d in DIAS_SEMANA" :key="d" class="cal-weekday">{{ d }}</span>
                </div>

                <div class="calendar-grid">
                  <button
                    v-for="day in calendarDays"
                    :key="formatDateISO(day.date)"
                    type="button"
                    class="cal-day"
                    :class="{
                      'other-month': !day.isCurrentMonth,
                      'is-past': day.isPast,
                      'is-today': day.isToday,
                      'is-selected': isDateSelected(day.date),
                      'has-slots': hasSlots(day.date),
                      'no-slots': hasNoSlots(day.date),
                    }"
                    :disabled="!day.isCurrentMonth || day.isPast"
                    @click="selectDate(day)"
                  >
                    {{ day.date.getDate() }}
                  </button>
                </div>

                <div class="calendar-legend">
                  <span class="legend-item"><span class="legend-dot dot-available" /> Disponible</span>
                  <span class="legend-item"><span class="legend-dot dot-full" /> Sin cupos</span>
                  <span class="legend-item"><span class="legend-dot dot-today" /> Hoy</span>
                </div>
              </div>

              <AppAlert v-if="showNoSlotsAlert" variant="warning" class="mt-4">
                No hay horarios disponibles para este día. Prueba con otra fecha.
              </AppAlert>

              <AppAlert
                v-if="disponibilidadError"
                variant="error"
                :action="'Reintentar'"
                class="mt-4"
                @action="retryDisponibilidad"
              >
                {{ disponibilidadError }}
              </AppAlert>

              <AppAlert v-if="confirmSelectionError" variant="error" class="mt-4">
                {{ confirmSelectionError }}
              </AppAlert>

              <div v-if="selectedDate && !disponibilidadError" class="slots-section">
                <h4 class="slots-title">
                  <Clock :size="15" />
                  Horarios disponibles — {{ formatDateDisplay(selectedDate) }}
                </h4>

                <div v-if="isLoadingSlots" class="slots-loading">
                  <span class="spinner spinner-sm" />
                  <span>Consultando disponibilidad…</span>
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
                    <span class="slot-sep">a</span>
                    <span class="slot-time slot-end">{{ formatTime12h(bloque.horaFin) }}</span>
                    <span v-if="isBloqueSelected(bloque)" class="slot-check">
                      <Check :size="13" />
                    </span>
                  </button>
                </div>

                <div v-else class="slots-empty">
                  <Inbox :size="28" />
                  <p>No hay horarios disponibles para este día.</p>
                </div>
              </div>
            </AppCard>
          </section>

          <!-- ══════ STEP 3: PAGO ══════ -->
          <section v-else-if="currentStep === 3" key="step3" class="step-panel">
            <AppCard>
              <template #header>
                <div class="card-header-left">
                  <div class="card-icon"><CreditCard :size="16" /></div>
                  <div>
                    <h3>Método de pago</h3>
                    <p class="card-header-sub">Elige cómo deseas realizar el pago</p>
                  </div>
                </div>
              </template>
              <template #header-actions>
                <span class="reserved-badge">
                  <Lock :size="11" /> Horario reservado
                </span>
              </template>

              <AppAlert v-if="pagoError" variant="error" class="mb-4">{{ pagoError }}</AppAlert>

              <div v-if="isLoadingMetodos" class="loading-state">
                <span class="spinner spinner-lg" />
                <p>Cargando métodos de pago…</p>
              </div>

              <AppAlert
                v-else-if="metodosError"
                variant="error"
                :action="'Reintentar'"
                @action="loadMetodosPago"
              >
                {{ metodosError }}
              </AppAlert>

              <div v-else-if="!metodosPago.length" class="empty-inline">
                <CreditCard :size="28" />
                <p>No hay métodos de pago online disponibles en este momento.</p>
              </div>

              <template v-else>
                <div class="methods-list">
                  <button
                    v-for="m in metodosPago"
                    :key="m.id"
                    type="button"
                    class="method-option"
                    :class="{ active: selectedMetodoPago === m.id }"
                    @click="selectedMetodoPago = m.id; onMetodoPagoChange()"
                  >
                    <div class="method-radio">
                      <div class="radio-outer" :class="{ checked: selectedMetodoPago === m.id }">
                        <div v-if="selectedMetodoPago === m.id" class="radio-inner" />
                      </div>
                    </div>
                    <div class="method-info">
                      <span class="method-name">
                        {{ m.nombre === 'Pago_Movil' ? 'Pago Móvil' : m.nombre }}
                      </span>
                      <span class="method-desc">{{ m.descripcion }}</span>
                    </div>
                  </button>
                </div>

                <Transition name="expand">
                  <div
                    v-if="selectedMetodoPago && (camposDinamicos.length > 0 || metodoSeleccionado?.camposRequeridos?.referencia)"
                    class="dynamic-fields"
                  >
                    <div v-if="metodoSeleccionado?.camposRequeridos?.referencia" class="form-group">
                      <label class="form-label">
                        Número de referencia <span class="required">*</span>
                      </label>
                      <AppInput
                        v-model="referenciaTransaccion"
                        placeholder="Ej: 0000123456789"
                        :error="pagoFormErrors.referencia"
                        :icon="Hash"
                      />  
                      <span v-if="pagoFormErrors.referencia" class="form-error">{{ pagoFormErrors.referencia }}</span>
                    </div>

                    <div v-for="campo in camposDinamicos" :key="campo.key" class="form-group">
                      <label class="form-label">
                        {{ formatFieldLabel(campo.key) }} <span class="required">*</span>
                      </label>
                      <AppSelect
                        v-if="campo.key === 'banco'"
                        v-model="datosPago[campo.key]"
                        :error="pagoFormErrors[campo.key]"
                      >
                        <option value="" disabled>Seleccione el banco emisor</option>
                        <option v-for="banco in BANCOS_VENEZUELA" :key="banco.codigo" :value="banco.nombre">
                          {{ banco.codigo }} - {{ banco.nombre }}
                        </option>
                      </AppSelect>
                      <AppInput
                        v-else
                        v-model="datosPago[campo.key]"
                        :placeholder="formatFieldPlaceholder(campo.key)"
                        :error="pagoFormErrors[campo.key]"
                        :icon="getFieldIcon(campo.key)"
                      />
                      <span v-if="pagoFormErrors[campo.key]" class="form-error">{{ pagoFormErrors[campo.key] }}</span>
                    </div>
                  </div>
                </Transition>

                <AppButton
                  variant="primary"
                  size="lg"
                  block
                  :loading="isProcessingPayment"
                  :disabled="!selectedMetodoPago"
                  class="mt-4"
                  @click="procesarPago"
                >
                  <template #icon-left><CreditCard :size="18" /></template>
                  {{ isProcessingPayment ? 'Procesando pago…' : `Pagar ${resumenActual.total || ''}`.trim() }}
                </AppButton>

                <p class="payment-disclaimer">
                  <Lock :size="11" />
                  El pago quedará pendiente de verificación por nuestro personal.
                </p>
              </template>
            </AppCard>
          </section>

          <!-- ══════ STEP 4: ÉXITO ══════ -->
          <section v-else-if="currentStep === 4" key="step4" class="step-panel step-success">
            <div class="success-container">
              <div class="success-icon">
                <Check :size="40" />
              </div>
              <h2 class="success-title">¡Pago procesado!</h2>
              <p class="success-message">
                Tu pago fue registrado exitosamente. La cita queda confirmada y lista para atenderte.
              </p>

              <AppAlert v-if="facturaAdvertencia" variant="warning">
                {{ facturaAdvertencia }}
              </AppAlert>

              <div class="success-factura-info">
                <div v-if="facturaNumeroControl" class="factura-info-row">
                  <span class="factura-label">Número de factura</span>
                  <span class="factura-value">{{ facturaNumeroControl }}</span>
                </div>
                <div class="factura-info-row">
                  <span class="factura-label">Estado del pago</span>
                  <span class="badge badge-warning">Pendiente de verificación</span>
                </div>
                <div v-if="resumenActual.mascota" class="factura-info-row">
                  <span class="factura-label">Mascota</span>
                  <span class="factura-value is-text">{{ resumenActual.mascota }}</span>
                </div>
                <div v-if="resumenActual.servicio" class="factura-info-row">
                  <span class="factura-label">Servicio</span>
                  <span class="factura-value is-text">{{ resumenActual.servicio }}</span>
                </div>
                <div v-if="resumenActual.fecha" class="factura-info-row">
                  <span class="factura-label">Fecha de la cita</span>
                  <span class="factura-value is-text">{{ resumenActual.fecha }}</span>
                </div>
                <div v-if="resumenActual.hora" class="factura-info-row">
                  <span class="factura-label">Horario</span>
                  <span class="factura-value is-text">{{ resumenActual.hora }}</span>
                </div>
                <div v-if="resumenActual.total" class="factura-info-row fila-total">
                  <span class="factura-label">Total pagado</span>
                  <span class="factura-value factura-total">{{ resumenActual.total }}</span>
                </div>
              </div>

              <div class="success-actions">
                <AppButton
                  variant="primary"
                  size="lg"
                  :loading="descargandoComprobante"
                  :disabled="!facturaId"
                  @click="descargarComprobante"
                >
                  <template #icon-left><Download :size="16" /></template>
                  {{ descargandoComprobante ? 'Descargando…' : 'Descargar comprobante' }}
                </AppButton>
                <AppButton
                  variant="secondary"
                  size="lg"
                  @click="router.push('/cliente/historial-pagos')"
                >
                  <template #icon-left><Receipt :size="16" /></template>
                  Ver en historial
                </AppButton>
              </div>

              <button type="button" class="btn-back-dashboard" @click="goToDashboard">
                <LayoutDashboard :size="16" />
                Volver al panel
              </button>
            </div>
          </section>
        </Transition>
      </main>

      <!-- ═══ SIDEBAR ═══ -->
      <aside v-if="currentStep < 4" class="wizard-sidebar">
        <div class="summary-card">
          <!-- ═══ HEADER ═══ -->
          <header class="summary-card-header">
            <h4>Resumen de tu reserva</h4>
            <span class="summary-card-hint">Se actualiza en tiempo real</span>
          </header>

          <!-- ═══ CUERPO: datos de la reserva ═══ -->
          <div class="summary-card-body">
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
            <div v-if="resumenActual.duracion" class="summary-line">
              <span class="summary-line-label"><CircleDot :size="13" /> Duración</span>
              <span class="summary-line-value">{{ resumenActual.duracion }} min</span>
            </div>
          </div>

          <!-- ═══ SECCIÓN: datos bancarios (solo paso 3) ═══ -->
          <div v-if="datosBancarios && currentStep === 3" class="banco-section">
            <div class="section-divider">
              <span class="section-eyebrow">
                <Landmark :size="12" /> Datos para pagar
              </span>
            </div>

            <div class="banco-body">
              <!-- Pago Móvil -->
              <div class="banco-bloque">
                <p class="banco-bloque-titulo">
                  <Smartphone :size="13" /> Pago Móvil
                </p>
                <div class="banco-fila">
                  <span class="banco-label">Banco</span>
                  <span class="banco-valor">{{ datosBancarios.pagoMovil.banco }}</span>
                </div>
                <div class="banco-fila">
                  <span class="banco-label">Teléfono</span>
                  <button
                    type="button"
                    class="banco-copiar mono"
                    title="Copiar teléfono"
                    @click="copiarDato(datosBancarios.pagoMovil.telefono, 'Teléfono')"
                  >
                    {{ datosBancarios.pagoMovil.telefono }} <Copy :size="12" />
                  </button>
                </div>
                <div class="banco-fila">
                  <span class="banco-label">Cédula / RIF</span>
                  <button
                    type="button"
                    class="banco-copiar mono"
                    title="Copiar cédula"
                    @click="copiarDato(datosBancarios.pagoMovil.cedula, 'Cédula')"
                  >
                    {{ datosBancarios.pagoMovil.cedula }} <Copy :size="12" />
                  </button>
                </div>
              </div>

              <!-- Transferencia -->
              <div class="banco-bloque">
                <p class="banco-bloque-titulo">
                  <ArrowRightLeft :size="13" /> Transferencia
                </p>
                <div class="banco-fila">
                  <span class="banco-label">Banco</span>
                  <span class="banco-valor">
                    {{ datosBancarios.bancoPrincipal.codigo }} {{ datosBancarios.bancoPrincipal.nombre }}
                  </span>
                </div>
                <div class="banco-fila">
                  <span class="banco-label">Cuenta</span>
                  <button
                    type="button"
                    class="banco-copiar mono"
                    title="Copiar número de cuenta"
                    @click="copiarDato(datosBancarios.bancoPrincipal.cuenta, 'Número de cuenta')"
                  >
                    {{ datosBancarios.bancoPrincipal.cuenta }} <Copy :size="12" />
                  </button>
                </div>
                <div class="banco-fila">
                  <span class="banco-label">RIF</span>
                  <button
                    type="button"
                    class="banco-copiar mono"
                    title="Copiar RIF"
                    @click="copiarDato(datosBancarios.rif, 'RIF')"
                  >
                    {{ datosBancarios.rif }} <Copy :size="12" />
                  </button>
                </div>
              </div>

              <!-- Nota -->
              <div v-if="datosBancarios.nota" class="banco-nota">
                <Info :size="13" />
                <span>{{ datosBancarios.nota }}</span>
              </div>
            </div>
          </div>

          <!-- ═══ FOOTER: monto + nota ═══ -->
          <footer class="summary-card-footer">
            <div class="summary-total">
              <span class="total-label">
                {{ currentStep === 3 ? 'Total a pagar' : 'Total estimado' }}
              </span>
              <span class="total-value">{{ resumenActual.total || '—' }}</span>
            </div>
            <div v-if="resumenActual.totalBs" class="total-bs">
              {{ resumenActual.totalBs }}
            </div>
            <p class="summary-note">
              <Lock :size="11" />
              {{ currentStep === 3
                ? 'Copia los datos y realiza el pago con el monto exacto.'
                : 'Pago seguro. La reserva queda bloqueada hasta completar el pago.' }}
            </p>
          </footer>
        </div>
      </aside>
    </div>

    <!-- ═══ STEP ACTIONS ═══ -->
    <div v-if="currentStep < 4" class="step-actions">
      <AppButton
        :variant="cancelArmed ? 'danger-soft' : 'ghost'"
        @click="cancelProcess"
      >
        <template #icon-left><X :size="15" /></template>
        {{ cancelArmed ? '¿Confirmar cancelación?' : 'Cancelar' }}
      </AppButton>
      <div class="step-actions-right">
        <AppButton
          v-if="currentStep > 1"
          variant="secondary"
          :disabled="isProcessingPayment"
          @click="goBack"
        >
          <template #icon-left><ArrowLeft :size="15" /></template>
          Atrás
        </AppButton>
        <AppButton
          v-if="currentStep === 1"
          variant="primary"
          :class="{ 'is-shake': shakeBtn }"
          :disabled="!canGoNextFromStep1"
          @click="goToStep2"
        >
          Siguiente
          <template #icon-right><ArrowRight :size="15" /></template>
        </AppButton>
        <AppButton
          v-if="currentStep === 2 && !isConfirmingSelection"
          variant="primary"
          :disabled="!selectedBloque"
          @click="confirmarSeleccion"
        >
          Confirmar horario
          <template #icon-right><ArrowRight :size="15" /></template>
        </AppButton>

        <AppButton
          v-else-if="currentStep === 2 && isConfirmingSelection"
          variant="primary"
          loading
          disabled
        />
      </div>
    </div>

    <!-- ═══ MODAL: Horario del veterinario ═══ -->
    <AppModal
      :model-value="horarioModalVisible"
      :title="vetHorario?.nombre || 'Horario de atención'"
      :subtitle="ESPECIALIDAD_LABELS[vetHorario?.especialidad] || vetHorario?.especialidad || 'Veterinario'"
      size="md"
      @update:model-value="cerrarHorarioModal"
    >
      <HorarioSemanalCard :horario="vetHorario?.horarioAtencion" />
    </AppModal>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Check, ChevronLeft, ChevronRight, Clock, PawPrint, Stethoscope,
  CreditCard, CalendarDays, ClipboardList, MessageSquare, CircleDot,
  DollarSign, Lock, X, ArrowLeft, ArrowRight,
  LayoutDashboard, Download, Receipt, Sparkles, Inbox, Search,
  Landmark, Smartphone, ArrowRightLeft, Info, Copy,
  Hash, Phone, CalendarClock,
} from 'lucide-vue-next'
import { getMisMascotas } from '@/api/mascotas.api'
import { getServicios, getVeterinarios, getDisponibilidad, solicitarCita } from '@/api/citas.api'
import { getMetodosOnline, procesarPagoCita, descargarFactura, getDatosBancarios } from '@/api/pagos.api'
import { useToast } from '@/composables/useToast'

import ToastContainer from '@/components/ui/ToastContainer.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AppAlert from '@/components/ui/AppAlert.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppSelect from '@/components/ui/AppSelect.vue'
import AppTextarea from '@/components/ui/AppTextarea.vue'
import PetAvatar from '@/components/ui/PetAvatar.vue'
import EntityAvatar from '@/components/ui/EntityAvatar.vue'

import { BANCOS_VENEZUELA } from '@/utils/constants/bancos'

import HorarioSemanalCard from '@/components/ui/HorarioSemanalCard.vue'
import AppModal from '@/components/ui/AppModal.vue'

/* ═══════════════════════════════════════════════════════════════
   CONSTANTES
   ═══════════════════════════════════════════════════════════════ */
const router = useRouter()
const { toastSuccess } = useToast()

const DIAS_SEMANA = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']
const MESES = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre']
const MAX_MONTHS_AHEAD = 3

const UMBRAL_BUSQUEDA = 5
const UMBRAL_CHIPS_VETS = 8
const UMBRAL_SCROLL_MASCOTAS = 8
const UMBRAL_SCROLL_VETS = 10

const ESPECIALIDADES_VET = ['Consulta', 'Vacunacion', 'Cirugia', 'Estetica']

const ESPECIALIDAD_LABELS = {
  Consulta: 'Consulta',
  Vacunacion: 'Vacunación',
  Cirugia: 'Cirugía',
  Estetica: 'Estética',
}

const steps = [
  { num: 1, label: 'Datos', desc: 'Mascota y servicio' },
  { num: 2, label: 'Horario', desc: 'Fecha y hora' },
  { num: 3, label: 'Pago', desc: 'Confirmación' },
]

/* ═══════════════════════════════════════════════════════════════
   ESTADO
   ═══════════════════════════════════════════════════════════════ */
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
const datosBancarios = ref(null)

const busquedaMascota = ref('')
const busquedaVeterinario = ref('')
const filtroEspecialidad = ref('')

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
const metodosError = ref(null)
const selectedMetodoPago = ref(null)
const datosPago = ref({})
const isLoadingMetodos = ref(false)
const pagoFormErrors = ref({})
const referenciaTransaccion = ref('')
const facturaId = ref(null)
const facturaNumeroControl = ref(null)
const facturaAdvertencia = ref(null)
const descargandoComprobante = ref(false)

const horarioConfirmado = ref(null)

const cancelArmed = ref(false)
const shakeBtn = ref(false)
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

let cancelTimer = null
let shakeTimer = null

/* ═══════════════════════════════════════════════════════════════
   COMPUTED
   ═══════════════════════════════════════════════════════════════ */
const servicioSeleccionado = computed(
  () => servicios.value.find((s) => s.id === selectedServicio.value) || null
)
const vetSeleccionado = computed(
  () => veterinarios.value.find((v) => v.id === selectedVeterinario.value) || null
)
const mascotaSeleccionada = computed(
  () => mascotas.value.find((m) => m.idMascota === selectedMascota.value) || null
)

const mostrarBusquedaMascotas = computed(() => mascotas.value.length > UMBRAL_BUSQUEDA)
const mostrarBusquedaVets = computed(() => veterinarios.value.length > UMBRAL_BUSQUEDA)
const mostrarChipsVets = computed(() => veterinarios.value.length > UMBRAL_CHIPS_VETS)
const scrollMascotas = computed(() => mascotas.value.length > UMBRAL_SCROLL_MASCOTAS)
const scrollVets = computed(() => veterinarios.value.length > UMBRAL_SCROLL_VETS)

const chipsEspecialidades = computed(() => {
  const conteos = {}
  for (const v of veterinarios.value) {
    if (v.especialidad) conteos[v.especialidad] = (conteos[v.especialidad] || 0) + 1
  }
  return ESPECIALIDADES_VET.map((esp) => ({
    value: esp,
    label: ESPECIALIDAD_LABELS[esp] || esp,
    count: conteos[esp] || 0,
    disabled: (conteos[esp] || 0) === 0,
  }))
})

const mascotasFiltradas = computed(() => {
  const q = busquedaMascota.value.trim().toLowerCase()
  const lista = mascotas.value
  if (!q) return { principales: lista, seleccionada: null }
  const filtradas = lista.filter((m) =>
    [m.nombre, m.nombreRaza, m.nombreEspecie]
      .filter(Boolean)
      .some((v) => String(v).toLowerCase().includes(q))
  )
  const sel = lista.find((m) => m.idMascota === selectedMascota.value)
  const enResultados = sel && filtradas.some((m) => m.idMascota === sel.idMascota)
  return {
    principales: filtradas,
    seleccionada: sel && !enResultados ? sel : null,
  }
})

const veterinariosFiltrados = computed(() => {
  const q = busquedaVeterinario.value.trim().toLowerCase()
  const esp = filtroEspecialidad.value
  const lista = veterinarios.value
  let filtradas = lista
  if (q) filtradas = filtradas.filter((v) => String(v.nombre || '').toLowerCase().includes(q))
  if (esp) filtradas = filtradas.filter((v) => v.especialidad === esp)
  const hayFiltro = Boolean(q || esp)
  const sel = lista.find((v) => v.id === selectedVeterinario.value)
  const enResultados = sel && filtradas.some((v) => v.id === sel.id)
  return {
    principales: filtradas,
    seleccionado: sel && hayFiltro && !enResultados ? sel : null,
    hayFiltro,
  }
})

const calendarMonthLabel = computed(() => `${MESES[calendarMonth.value]} ${calendarYear.value}`)
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
const canGoNextFromStep1 = computed(
  () => selectedMascota.value && selectedVeterinario.value && selectedServicio.value
    && motivoConsulta.value.trim().length > 0
)

const resumenActual = computed(() => {
  const r = citaResumen.value || {}
  return {
    mascota: r.mascota || mascotaSeleccionada.value?.nombre || null,
    veterinario: r.veterinario || vetSeleccionado.value?.nombre || null,
    servicio: r.servicio || servicioSeleccionado.value?.nombre || null,
    fecha: r.fecha ? formatDateDisplay(r.fecha) : (selectedDate.value ? formatDateDisplay(selectedDate.value) : null),
    hora: r.horaInicio
      ? `${formatTime12h(r.horaInicio)} — ${formatTime12h(r.horaFin)}`
      : (selectedBloque.value
          ? `${formatTime12h(selectedBloque.value.horaInicio)} — ${formatTime12h(selectedBloque.value.horaFin)}`
          : null),
    duracion: servicioSeleccionado.value?.duracionMinutos || null,
    total: r.costoUsd != null
      ? formatCurrency(r.costoUsd)
      : (servicioSeleccionado.value ? formatCurrency(servicioSeleccionado.value.precioUsd) : null),
    totalBs: r.costoBs != null ? formatCurrencyBs(r.costoBs) : null,
  }
})

const calendarDays = computed(() => {
  const year = calendarYear.value
  const month = calendarMonth.value
  const firstDay = new Date(year, month, 1)
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const offset = firstDay.getDay() === 0 ? 6 : firstDay.getDay() - 1
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const days = []
  const prevLast = new Date(year, month, 0).getDate()
  for (let i = offset - 1; i >= 0; i--) {
    const d = new Date(year, month - 1, prevLast - i)
    days.push({ date: d, isCurrentMonth: false, isPast: true, isToday: false })
  }
  for (let d = 1; d <= daysInMonth; d++) {
    const date = new Date(year, month, d)
    days.push({
      date,
      isCurrentMonth: true,
      isPast: date < today,
      isToday: date.getTime() === today.getTime(),
    })
  }
  const totalRows = Math.ceil(days.length / 7)
  const remaining = totalRows * 7 - days.length
  for (let d = 1; d <= remaining; d++) {
    days.push({
      date: new Date(year, month + 1, d),
      isCurrentMonth: false,
      isPast: false,
      isToday: false,
    })
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

const showNoSlotsAlert = computed(
  () => selectedDate.value && fechaFueConsultada.value
    && currentSlots.value.length === 0
    && !disponibilidadError.value
)

const metodoSeleccionado = computed(
  () => metodosPago.value.find((m) => m.id === selectedMetodoPago.value) || null
)

const camposDinamicos = computed(() => {
  if (!metodoSeleccionado.value?.camposRequeridos) return []
  return Object.entries(metodoSeleccionado.value.camposRequeridos)
    .filter(([key]) => key !== 'referencia')
    .map(([key, tipo]) => ({ key, tipo }))
})

/* ═══════════════════════════════════════════════════════════════
   WATCHERS
   ═══════════════════════════════════════════════════════════════ */
watch([selectedServicio, selectedVeterinario], () => {
  selectedDate.value = null
  selectedBloque.value = null
  disponibilidadCache.value = {}
  disponibilidadError.value = null
})

watch([selectedMascota, selectedVeterinario, selectedServicio, motivoConsulta], () => {
  if (!Object.keys(stepErrors.value).length) return
  const e = { ...stepErrors.value }
  if (selectedMascota.value) delete e.mascota
  if (selectedVeterinario.value) delete e.veterinario
  if (selectedServicio.value) delete e.servicio
  if (motivoConsulta.value.trim()) delete e.motivo
  stepErrors.value = e
})

watch(currentStep, () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
})

/* ═══════════════════════════════════════════════════════════════
   HELPERS DE ERROR
   ═══════════════════════════════════════════════════════════════ */
function mensajeError(err, fallback) {
  const d = err?.response?.data
  return d?.mensaje || (typeof d?.message === 'string' ? d.message : '') || fallback
}

/* ═══════════════════════════════════════════════════════════════
   CARGA DE DATOS
   ═══════════════════════════════════════════════════════════════ */
function selectVeterinario(v) {
  if (selectedVeterinario.value === v.id) return
  selectedVeterinario.value = v.id
  onVeterinarioChange()
}

async function loadInitialData() {
  isLoadingInitial.value = true
  loadError.value = null
  try {
    const [mascotasRes, vetsRes] = await Promise.all([getMisMascotas(), getVeterinarios()])
    mascotas.value = mascotasRes.data || []
    veterinarios.value = vetsRes.data || []
  } catch (err) {
    console.error('Error cargando datos iniciales:', err)
    loadError.value = mensajeError(err, 'No se pudieron cargar los datos necesarios.')
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
    servicios.value = data || []
  } catch (err) {
    console.error('Error cargando servicios:', err)
  } finally {
    isLoadingServicios.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   VALIDACIONES + NAVEGACIÓN ENTRE PASOS
   ═══════════════════════════════════════════════════════════════ */
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
  if (!validateStep1()) {
    shakeBtn.value = true
    clearTimeout(shakeTimer)
    shakeTimer = setTimeout(() => { shakeBtn.value = false }, 500)
    return
  }
  stepErrors.value = {}
  currentStep.value = 2
}

function goBack() {
  if (isProcessingPayment.value) return
  pagoError.value = null
  pagoFormErrors.value = {}
  currentStep.value--
}

function cancelProcess() {
  if (currentStep.value === 1 && !citaPendienteId.value) {
    goToDashboard()
    return
  }
  if (!cancelArmed.value) {
    cancelArmed.value = true
    clearTimeout(cancelTimer)
    cancelTimer = setTimeout(() => { cancelArmed.value = false }, 3000)
    return
  }
  clearTimeout(cancelTimer)
  cancelArmed.value = false
  goToDashboard()
}

function goToDashboard() {
  router.push('/cliente/dashboard')
}

/* ═══════════════════════════════════════════════════════════════
   CALENDARIO
   ═══════════════════════════════════════════════════════════════ */
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
  const bloques = disponibilidadCache.value[key]
  return Array.isArray(bloques) && bloques.length > 0
}

function hasNoSlots(date) {
  const key = formatDateISO(date)
  const bloques = disponibilidadCache.value[key]
  return Array.isArray(bloques) && bloques.length === 0
}

async function selectDate(day) {
  if (!day.isCurrentMonth || day.isPast) return
  const dateKey = formatDateISO(day.date)
  if (selectedDate.value && formatDateISO(selectedDate.value) === dateKey) return
  selectedDate.value = day.date
  selectedBloque.value = null
  disponibilidadError.value = null
  confirmSelectionError.value = null
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
      id_servicio: selectedServicio.value,
    })
    disponibilidadCache.value[dateStr] = data.bloquesDisponibles || []
  } catch (err) {
    console.error('Error consultando disponibilidad:', err)
    disponibilidadError.value = mensajeError(
      err,
      'Error al consultar disponibilidad. Intenta de nuevo.'
    )
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

/* ═══════════════════════════════════════════════════════════════
   CONFIRMAR SELECCIÓN (paso 2 → 3)
   ═══════════════════════════════════════════════════════════════ */
async function confirmarSeleccion() {
  if (!selectedBloque.value || !selectedDate.value || isConfirmingSelection.value) return

  if (
    citaPendienteId.value
    && horarioConfirmado.value
    && horarioConfirmado.value.fecha === formatDateISO(selectedDate.value)
    && horarioConfirmado.value.horaInicio === selectedBloque.value.horaInicio
  ) {
    currentStep.value = 3
    return
  }

  isConfirmingSelection.value = true
  confirmSelectionError.value = null
  try {
    const { data } = await solicitarCita({
      idMascota: selectedMascota.value,
      idVeterinario: selectedVeterinario.value,
      idServicio: selectedServicio.value,
      fechaCita: formatDateISO(selectedDate.value),
      horaInicio: selectedBloque.value.horaInicio,
      motivoConsulta: motivoConsulta.value.trim(),
    })
    citaPendienteId.value = data.idCita
    citaResumen.value = data.resumen
    horarioConfirmado.value = {
      fecha: formatDateISO(selectedDate.value),
      horaInicio: selectedBloque.value.horaInicio,
    }
    pagoError.value = null
    currentStep.value = 3
    await Promise.all([loadMetodosPago(), cargarDatosBancarios()])
  } catch (err) {
    console.error('Error al solicitar cita:', err)
    const msg = mensajeError(err, '')
    if (msg.includes('ya no está disponible')) {
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

/* ═══════════════════════════════════════════════════════════════
   MÉTODOS DE PAGO
   ═══════════════════════════════════════════════════════════════ */
async function loadMetodosPago() {
  isLoadingMetodos.value = true
  metodosError.value = null
  try {
    const { data } = await getMetodosOnline()
    metodosPago.value = data || []
  } catch (err) {
    console.error('Error cargando métodos:', err)
    metodosError.value = mensajeError(err, 'No se pudieron cargar los métodos de pago.')
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

async function procesarPago() {
  if (!citaPendienteId.value || !selectedMetodoPago.value || isProcessingPayment.value) return

  const camposRequeridos = metodoSeleccionado.value?.camposRequeridos || {}
  const errors = {}
  if ('referencia' in camposRequeridos && !referenciaTransaccion.value.trim()) {
    errors.referencia = 'La referencia es obligatoria'
  }
  for (const { key } of camposDinamicos.value) {
    if (!datosPago.value[key]?.trim()) errors[key] = 'Este campo es obligatorio'
  }
  pagoFormErrors.value = errors
  if (Object.keys(errors).length > 0) {
    pagoError.value = 'Revisa los campos marcados antes de continuar.'
    return
  }

  isProcessingPayment.value = true
  pagoError.value = null
  try {
    const datosPagoCompletos = { ...datosPago.value }
    if ('referencia' in camposRequeridos) {
      datosPagoCompletos.referencia = referenciaTransaccion.value.trim()
    }
    const { data } = await procesarPagoCita({
      idCita: citaPendienteId.value,
      idMetodoPago: selectedMetodoPago.value,
      referenciaTransaccion: referenciaTransaccion.value.trim(),
      datosPago: datosPagoCompletos,
    })
    facturaId.value = data.idFactura
    facturaNumeroControl.value = data.numeroControl
    facturaAdvertencia.value = data.advertenciaEmail
    citaPendienteId.value = null
    horarioConfirmado.value = null
    currentStep.value = 4
  } catch (err) {
    console.error('Error procesando pago:', err)
    pagoError.value = mensajeError(
      err,
      'Transacción rechazada o datos inválidos. Verifique e intente nuevamente.'
    )
  } finally {
    isProcessingPayment.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   FORMATEADORES
   ═══════════════════════════════════════════════════════════════ */
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

function formatFieldLabel(key) {
  const labels = {
    banco: 'Banco emisor',
    numero_cuenta: 'Número de cuenta',
    telefono: 'Número de teléfono asociado',
    lote: 'Número de lote',
    ultimos_digitos: 'Últimos 4 dígitos de la tarjeta',
  }
  return labels[key] || key.replace(/_/g, ' ').replace(/\b\w/g, (l) => l.toUpperCase())
}

function getFieldIcon(key) {
  const icons = {
    banco: Landmark,
    numero_cuenta: CreditCard,
    telefono: Phone,
    lote: Hash,
    ultimos_digitos: CreditCard,
  }
  return icons[key] || null
}

function formatFieldPlaceholder(key) {
  const ph = {
    banco: 'Ej: Banesco, Mercantil, Provincial...',
    numero_cuenta: 'Ej: 0000-0000-00-0000000000',
    telefono: 'Ej: 04141234567',
  }
  return ph[key] || ''
}

/* ═══════════════════════════════════════════════════════════════
   DESCARGA DE COMPROBANTE
   ═══════════════════════════════════════════════════════════════ */
async function descargarComprobante() {
  if (!facturaId.value || descargandoComprobante.value) return
  descargandoComprobante.value = true
  try {
    const response = await descargarFactura(facturaId.value)
    const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }))
    const link = document.createElement('a')
    link.href = url
    link.download = `Factura-${facturaNumeroControl.value || facturaId.value}.pdf`
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    toastSuccess('Comprobante descargado exitosamente')
  } catch (err) {
    console.error('Error descargando factura:', err)
  } finally {
    descargandoComprobante.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   PROTECCIÓN CONTRA CIERRE ACCIDENTAL
   ═══════════════════════════════════════════════════════════════ */
function onBeforeUnload(e) {
  if (citaPendienteId.value && currentStep.value < 4) {
    e.preventDefault()
    e.returnValue = ''
  }
}

/* ═══════════════════════════════════════════════════════════════
   DATOS BANCARIOS
   ═══════════════════════════════════════════════════════════════ */
async function cargarDatosBancarios() {
  if (datosBancarios.value) return
  try {
    const { data } = await getDatosBancarios()
    datosBancarios.value = data
  } catch {
    datosBancarios.value = null
  }
}

/* ═══════════════════════════════════════════════════════════════
   COPIAR AL PORTAPAPELES
   ═══════════════════════════════════════════════════════════════ */
async function copiarDato(texto, etiqueta) {
  const valor = String(texto || '').trim()
  if (!valor) return
  try {
    await navigator.clipboard.writeText(valor)
    toastSuccess(`${etiqueta} copiado al portapapeles`)
  } catch {
    const tmp = document.createElement('textarea')
    tmp.value = valor
    tmp.style.cssText = 'position:fixed;opacity:0;pointer-events:none'
    document.body.appendChild(tmp)
    tmp.select()
    document.execCommand('copy')
    tmp.remove()
    toastSuccess(`${etiqueta} copiado al portapapeles`)
  }
}

/* ═══════════════════════════════════════════════════════════════
   LIFECYCLE
   ═══════════════════════════════════════════════════════════════ */
onMounted(() => {
  loadInitialData()
  cargarDatosBancarios()
  window.addEventListener('beforeunload', onBeforeUnload)
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', onBeforeUnload)
  if (cancelTimer) clearTimeout(cancelTimer)
  if (shakeTimer) clearTimeout(shakeTimer)
})
</script>

<style scoped>
.appointment-view {
  max-width: 1400px;
  margin: 0 auto;
  padding: var(--space-6) var(--space-6) var(--space-12);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* ═══ STEPPER ═══ */
.stepper {
  display: flex;
  align-items: center;
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  padding: var(--space-4) var(--space-6);
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
  width: 34px;
  height: 34px;
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

/* ═══ LAYOUT ═══ */
.wizard-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: var(--space-5);
  align-items: start;
}
.wizard-layout.is-success { grid-template-columns: 1fr; }
.wizard-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

/* ═══ SIDEBAR (un solo card) ═══ */
.wizard-sidebar {
  position: sticky;
  top: var(--space-6);
  max-height: calc(100vh - var(--space-12));
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  padding-right: var(--space-1);
  scrollbar-width: thin;
  scrollbar-color: var(--neutral-300) transparent;
}
.wizard-sidebar::-webkit-scrollbar { width: 6px; }
.wizard-sidebar::-webkit-scrollbar-thumb {
  background: var(--neutral-300);
  border-radius: 3px;
}

.summary-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.summary-card-header {
  padding: var(--space-5) var(--space-5) var(--space-3);
  border-bottom: 1px solid var(--border-subtle);
}
.summary-card-header h4 {
  margin: 0 0 2px;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}
.summary-card-hint {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

.summary-card-body {
  padding: var(--space-4) var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.summary-line {
  display: flex;
  flex-direction: column;
  gap: 3px;
  transition: opacity var(--duration-base) var(--ease-out);
}
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
.summary-line-value {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  line-height: var(--leading-snug);
  word-break: break-word;
}
.summary-line.is-empty .summary-line-value {
  color: var(--text-tertiary);
  font-weight: var(--font-medium);
  font-style: italic;
}

/* ═══ SECCIÓN BANCARIA (dentro del mismo card) ═══ */
.banco-section {
  border-top: 1px solid var(--border-subtle);
}

.section-divider {
  padding: var(--space-3) var(--space-5);
  background: var(--neutral-50);
  border-bottom: 1px solid var(--border-subtle);
}
.section-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-extrabold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--info-600);
}

.banco-body {
  padding: var(--space-4) var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.banco-bloque { display: flex; flex-direction: column; gap: var(--space-2); }
.banco-bloque-titulo {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin: 0 0 var(--space-1);
  font-size: var(--text-xs);
  font-weight: var(--font-extrabold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--info-600);
}
.banco-fila {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-sm);
  padding: var(--space-1) 0;
  border-bottom: 1px dashed var(--neutral-100);
}
.banco-fila:last-child { border-bottom: none; }
.banco-label {
  color: var(--text-secondary);
  font-weight: var(--font-medium);
  flex-shrink: 0;
}
.banco-valor {
  color: var(--text-primary);
  font-weight: var(--font-bold);
  text-align: right;
  word-break: break-word;
  min-width: 0;
}
.banco-nota {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
  padding: var(--space-3);
  background: var(--warning-50);
  color: var(--warning-700);
  border: 1px solid var(--warning-200);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  line-height: var(--leading-snug);
}
.banco-nota svg { flex-shrink: 0; margin-top: 1px; }

.banco-copiar {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  background: none;
  border: 1px dashed transparent;
  border-radius: var(--radius-sm);
  padding: 2px var(--space-2);
  color: var(--text-primary);
  font-weight: var(--font-bold);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.banco-copiar svg {
  color: var(--text-tertiary);
  transition: color var(--duration-fast) var(--ease-out);
}
.banco-copiar:hover {
  background: var(--info-50);
  border-color: var(--info-200);
  color: var(--info-600);
}
.banco-copiar:hover svg { color: var(--info-600); }
.banco-copiar:active { transform: scale(0.96); }
.banco-copiar:focus-visible {
  outline: none;
  border-color: var(--info-600);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
}

/* ═══ FOOTER: monto ═══ */
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
}
.total-label {
  font-size: var(--text-xs);
  font-weight: var(--font-extrabold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--text-secondary);
  white-space: nowrap;
}
.total-value {
  font-size: 22px;
  font-weight: var(--font-bold);
  color: var(--brand-700);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
  line-height: 1;
}
.total-bs {
  margin-top: var(--space-1);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-secondary);
  text-align: right;
  letter-spacing: -0.01em;
  font-variant-numeric: tabular-nums;
}
.summary-note {
  display: flex;
  align-items: flex-start;
  gap: var(--space-1);
  margin: var(--space-3) 0 0;
  font-size: var(--text-xs);
  color: var(--text-secondary);
  line-height: var(--leading-snug);
}
.summary-note svg { flex-shrink: 0; margin-top: 2px; }

/* ═══ CARD HEADER ═══ */
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
.card-header h3 { font-size: var(--text-lg); font-weight: var(--font-bold); margin: 0; line-height: 1.2; }
.card-header-sub { margin: 2px 0 0; font-size: var(--text-sm); color: var(--text-secondary); }

/* ═══ BÚSQUEDA ═══ */
.search-bar { position: relative; margin-bottom: var(--space-3); }
.search-icon {
  position: absolute;
  left: var(--space-4);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  pointer-events: none;
}
.search-input { padding-left: 40px; padding-right: 40px; }
.search-clear {
  position: absolute;
  right: var(--space-2);
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
  transition: all var(--duration-fast) var(--ease-out);
}
.search-clear:hover { color: var(--text-secondary); background: var(--neutral-100); }

.results-count {
  margin: 0 0 var(--space-3);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
}

/* ═══ CHIPS ═══ */
.chips-row {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  margin-bottom: var(--space-3);
}
.chip {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-3) var(--space-1) var(--space-4);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--neutral-600);
  cursor: pointer;
  transition: all var(--duration-base) var(--ease-out);
  font-family: inherit;
  white-space: nowrap;
}
.chip:hover:not(:disabled) {
  background: var(--brand-50);
  border-color: var(--brand-200);
  color: var(--brand-700);
}
.chip.active {
  background: var(--brand-700);
  color: var(--text-inverse);
  border-color: var(--brand-700);
  box-shadow: 0 2px 8px rgba(15, 118, 110, 0.25);
}
.chip-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 18px;
  padding: 0 var(--space-2);
  border-radius: var(--radius-full);
  background: var(--neutral-100);
  color: var(--text-secondary);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
}
.chip.active .chip-count {
  background: rgba(255, 255, 255, 0.22);
  color: var(--text-inverse);
}
.chip.is-empty {
  opacity: 0.45;
  cursor: not-allowed;
  border-style: dashed;
}

/* ═══ PINNED ═══ */
.pinned-section {
  margin-bottom: var(--space-4);
  padding: var(--space-3) var(--space-4);
  background: var(--brand-50);
  border: 1px solid var(--brand-200);
  border-radius: var(--radius-xl);
}
.pinned-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--brand-700);
  margin-bottom: var(--space-2);
}
.pinned-section .pick-card { margin-bottom: 0; }

/* ═══ PICK GRID ═══ */
.pick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--space-3);
}
.pick-grid.scrollable {
  max-height: 420px;
  overflow-y: auto;
  padding-right: var(--space-2);
  scrollbar-width: thin;
  scrollbar-color: var(--neutral-300) transparent;
}
.pick-grid.scrollable::-webkit-scrollbar { width: 8px; }
.pick-grid.scrollable::-webkit-scrollbar-thumb {
  background: var(--neutral-300);
  border-radius: 4px;
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
  box-shadow: 0 6px 16px -8px rgba(15, 118, 110, 0.20);
}
.pick-card.selected {
  border-color: var(--brand-700);
  background: var(--brand-50);
  box-shadow: 0 0 0 3px var(--brand-100);
}
.pick-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
.pick-name {
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.25;
}
.pick-meta {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
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

@keyframes checkPop {
  0% { transform: scale(0); }
  100% { transform: scale(1); }
}

/* ═══ EMPTY INLINE ═══ */
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
.btn-link {
  background: none;
  border: none;
  padding: 0;
  color: var(--brand-700);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  cursor: pointer;
  font-family: inherit;
}
.btn-link:hover { text-decoration: underline; }

/* ═══ SERVICE LIST ═══ */
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
.service-item.selected {
  border-color: var(--brand-700);
  background: var(--brand-50);
  box-shadow: 0 0 0 3px var(--brand-100);
}
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
  transition: border-color var(--duration-base) var(--ease-out);
}
.service-item.selected .service-item-radio { border-color: var(--brand-700); }
.service-item-radio-inner {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--brand-700);
  animation: radioPop 0.2s var(--ease-out);
}
@keyframes radioPop {
  0% { transform: scale(0); }
  100% { transform: scale(1); }
}
.service-item-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: var(--space-1); }
.service-item-name { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--text-primary); line-height: 1.3; }
.service-item-desc { font-size: var(--text-sm); color: var(--text-secondary); line-height: var(--leading-normal); }
.service-item-meta { display: flex; align-items: center; gap: var(--space-2); flex-wrap: wrap; margin-top: var(--space-1); }
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
.meta-badge {
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  background: var(--brand-100);
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid var(--brand-200);
  text-transform: capitalize;
}

/* ═══ SKELETON ═══ */
.service-skeleton { display: flex; flex-direction: column; gap: var(--space-2); }
.skeleton-row-svc { height: 76px; border-radius: var(--radius-xl); }

/* ═══ FORM FOOTER ═══ */
.form-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--space-2);
}
.char-count { font-size: var(--text-xs); color: var(--text-tertiary); margin-left: auto; }

/* ═══ LOADING ═══ */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-10) var(--space-6);
  color: var(--text-secondary);
  font-size: var(--text-md);
}
.label-loading {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
}

/* ═══ CALENDARIO ═══ */
.calendar { margin-bottom: var(--space-2); }
.calendar-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
}
.cal-nav-btn {
  width: 38px;
  height: 38px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  background: var(--bg-surface);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--duration-base) var(--ease-out);
  flex-shrink: 0;
}
.cal-nav-btn:hover:not(:disabled) {
  background: var(--brand-50);
  color: var(--brand-700);
  border-color: var(--brand-200);
}
.cal-nav-btn:disabled { opacity: 0.35; cursor: not-allowed; }
.cal-month-wrap { text-align: center; }
.cal-month-year {
  display: block;
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
}
.cal-month-hint {
  display: block;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  margin-top: 2px;
}
.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  margin-bottom: var(--space-2);
}
.cal-weekday {
  text-align: center;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: var(--space-2) 0;
}
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: var(--space-1);
}
.cal-day {
  width: 100%;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  border: 2px solid transparent;
  border-radius: var(--radius-lg);
  background: none;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
  position: relative;
  padding: 0;
  min-width: 0;
  min-height: 0;
  box-sizing: border-box;
}
.cal-day:hover:not(:disabled):not(.other-month):not(.is-past) {
  background: var(--brand-50);
  border-color: var(--brand-200);
}
.cal-day.other-month { color: var(--neutral-300); cursor: default; }
.cal-day.is-past { color: var(--neutral-300); cursor: not-allowed; }
.cal-day.is-today { font-weight: var(--font-bold); color: var(--brand-700); }
.cal-day.is-selected {
  background: var(--brand-700);
  color: var(--text-inverse);
  border-color: var(--brand-700);
  box-shadow: 0 4px 12px rgba(15, 118, 110, 0.35);
}
.cal-day.is-selected.is-today { color: var(--text-inverse); }
.cal-day.has-slots:not(.is-selected)::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--success-500);
}
.cal-day.no-slots:not(.is-selected)::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--danger-200);
}
.calendar-legend {
  display: flex;
  gap: var(--space-4);
  flex-wrap: wrap;
  margin-top: var(--space-4);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-subtle);
}
.legend-item {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-medium);
}
.legend-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dot-available { background: var(--success-500); }
.dot-full { background: var(--danger-200); }
.dot-today { background: var(--brand-700); }

.vet-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--brand-700);
  background: var(--brand-50);
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  border: 1px solid var(--brand-200);
}

/* ═══ SLOTS ═══ */
.slots-section {
  margin-top: var(--space-6);
  padding-top: var(--space-6);
  border-top: 1px solid var(--border-subtle);
}
.slots-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-4);
}
.slots-loading {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-6);
  color: var(--text-secondary);
  font-size: var(--text-md);
}
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
.slot-btn:hover {
  border-color: var(--brand-700);
  background: var(--brand-50);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -8px rgba(15, 118, 110, 0.25);
}
.slot-btn.is-selected {
  background: var(--brand-700);
  border-color: var(--brand-700);
  color: var(--text-inverse);
  box-shadow: 0 6px 16px -6px rgba(15, 118, 110, 0.5);
}
.slot-time { font-size: var(--text-md); font-weight: var(--font-bold); color: var(--text-primary); transition: color var(--duration-base); }
.slot-btn.is-selected .slot-time { color: var(--text-inverse); }
.slot-sep { font-size: var(--text-2xs); color: var(--text-tertiary); transition: color var(--duration-base); }
.slot-btn.is-selected .slot-sep { color: rgba(255, 255, 255, 0.7); }
.slot-end { font-size: var(--text-sm); font-weight: var(--font-medium); color: var(--text-secondary); transition: color var(--duration-base); }
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
.slots-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-8) var(--space-5);
  color: var(--text-tertiary);
  text-align: center;
  font-size: var(--text-md);
}
.slots-empty p { margin: 0; }

/* ═══ MÉTODOS DE PAGO ═══ */
.methods-list { display: flex; flex-direction: column; gap: var(--space-3); }
.method-option {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--duration-base) var(--ease-out);
  background: var(--bg-surface);
  font-family: inherit;
  text-align: left;
  width: 100%;
}
.method-option:hover { border-color: var(--brand-200); background: var(--brand-50); }
.method-option.active {
  border-color: var(--brand-700);
  background: var(--brand-50);
  box-shadow: 0 0 0 3px var(--brand-100);
}
.method-radio { flex-shrink: 0; }
.radio-outer {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid var(--neutral-300);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--duration-base);
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

.payment-disclaimer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  margin-top: var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  line-height: var(--leading-normal);
  text-align: center;
}

.reserved-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  color: var(--warning-700);
  background: var(--warning-50);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  border: 1px solid var(--warning-200);
  white-space: nowrap;
}

/* ═══ ÉXITO ═══ */
.step-success { max-width: 680px; margin: 0 auto; }
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
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--success-600);
  margin-bottom: var(--space-2);
  animation: successPop 0.5s var(--ease-spring);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}
.success-title {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0;
  letter-spacing: var(--tracking-tight);
}
.success-message {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin: 0;
  max-width: 460px;
  line-height: var(--leading-relaxed);
}
.success-factura-info {
  margin-top: var(--space-2);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  width: 100%;
  max-width: 460px;
  text-align: left;
}
.factura-info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-3);
}
.factura-label { font-size: var(--text-sm); color: var(--text-secondary); }
.factura-value {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  font-family: var(--font-mono);
}
.factura-value.is-text { font-family: inherit; font-weight: var(--font-semibold); }
.factura-info-row.fila-total {
  border-top: 1px solid var(--border-subtle);
  padding-top: var(--space-3);
}
.factura-total { color: var(--brand-700); font-size: var(--text-lg); }

.success-actions {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-5);
  justify-content: center;
  flex-wrap: wrap;
  width: 100%;
}

.btn-back-dashboard {
  margin-top: var(--space-3);
  padding: var(--space-3) var(--space-6);
  background: transparent;
  color: var(--text-secondary);
  border: none;
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  transition: all var(--duration-base) var(--ease-out);
  font-family: inherit;
  border-radius: var(--radius-lg);
}
.btn-back-dashboard:hover { background: var(--neutral-100); color: var(--brand-700); }

/* ═══ STEP ACTIONS ═══ */
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
.step-actions-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-left: auto;
}

.is-shake { animation: shake 0.45s ease; }
@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-5px); }
  40% { transform: translateX(5px); }
  60% { transform: translateX(-3px); }
  80% { transform: translateX(3px); }
}

/* ═══ FOCUS ═══ */
.pick-card:focus-visible,
.service-item:focus-visible,
.slot-btn:focus-visible,
.method-option:focus-visible,
.chip:focus-visible,
.btn-link:focus-visible,
.cal-day:focus-visible {
  outline: none;
  border-color: var(--brand-700);
  box-shadow: 0 0 0 3px var(--brand-100);
}

/* ═══ TRANSICIONES ═══ */
.step-fade-enter-active,
.step-fade-leave-active {
  transition: opacity var(--duration-slow) var(--ease-out),
              transform var(--duration-slow) var(--ease-out);
}
.step-fade-enter-from { opacity: 0; transform: translateY(8px); }
.step-fade-leave-to { opacity: 0; transform: translateY(-8px); }

.expand-enter-active,
.expand-leave-active {
  transition: opacity var(--duration-slow) var(--ease-out),
              transform var(--duration-slow) var(--ease-out);
  overflow: hidden;
}
.expand-enter-from,
.expand-leave-to { opacity: 0; transform: translateY(-6px); }

/* ═══ UTIL ═══ */
.mt-2 { margin-top: var(--space-2); }
.mt-4 { margin-top: var(--space-4); }
.mb-4 { margin-bottom: var(--space-4); }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .wizard-layout { grid-template-columns: 1fr; }
  .wizard-sidebar {
    position: static;
    max-height: none;
    overflow: visible;
    padding-right: 0;
  }
}

@media (max-width: 768px) {
  .appointment-view { padding: var(--space-4); }
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
  .step-actions-right :deep(.btn),
  .step-actions :deep(.btn) { width: 100%; }
  .pick-grid { grid-template-columns: 1fr; }
  .pick-grid.scrollable { max-height: 320px; }
}

/* ═══ Solo en móvil real: éxito con botones apilados ═══ */
@media (max-width: 480px) {
  .success-actions { flex-direction: column; width: 100%; }
  .success-actions :deep(.btn) { width: 100%; }
}

@media (max-width: 480px) {
  .calendar-grid { gap: 2px; }
  .cal-day { font-size: var(--text-sm); border-radius: var(--radius-md); }
  .slots-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .success-container { padding: var(--space-8) var(--space-5); }
  .success-icon { width: 72px; height: 72px; }
  .success-title { font-size: var(--text-3xl); }
  .service-item { padding: var(--space-3); gap: var(--space-3); }
  .chips-row { gap: var(--space-1); }
  .chip { padding: var(--space-1) var(--space-3); font-size: var(--text-xs); }
  .chip-count { min-width: 18px; height: 16px; font-size: var(--text-2xs); }
}
</style>