<template>
  <div class="appointment-view">
    <!-- ═══ HERO ═══ -->
    <header class="wizard-hero">
      <p class="hero-eyebrow">
        <Sparkles :size="12" />
        Nueva reserva
      </p>
      <h1>Solicitar Cita</h1>
      <p class="hero-sub">Reserva un turno para tu mascota en 3 simples pasos.</p>
    </header>

    <!-- ═══ STEPPER ═══ -->
    <nav class="stepper" aria-label="Progreso de la reserva">
      <div
        v-for="(step, idx) in steps"
        :key="idx"
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
          <!-- ── STEP 1 ── -->
          <section v-if="currentStep === 1" key="step1" class="step-panel">
            <div v-if="isLoadingInitial" class="card">
              <div class="card-body">
                <div class="loading-box">
                  <div class="spinner" />
                  <p>Cargando datos necesarios…</p>
                </div>
              </div>
            </div>
            <template v-else>
              <div v-if="loadError" class="alert alert-error">
                <AlertTriangle :size="18" />
                <span>{{ loadError }}</span>
                <button type="button" class="alert-action" @click="loadInitialData">
                  Reintentar
                </button>
              </div>
              <template v-else>
                <!-- ══ MASCOTA ══ -->
                <div class="card">
                  <div class="card-header">
                    <div class="card-header-left">
                      <div class="card-icon"><PawPrint :size="16" /></div>
                      <div>
                        <h3>¿Para quién es la cita?</h3>
                        <p class="card-header-sub">Elige la mascota que recibirá la atención</p>
                      </div>
                    </div>
                  </div>
                  <div class="card-body">
                    <div v-if="!mascotas.length" class="empty-inline">
                      <PawPrint :size="28" />
                      <p>No tienes mascotas registradas.</p>
                      <button type="button" class="btn-link" @click="router.push('/cliente/mascotas')">
                        Registrar mascota
                      </button>
                    </div>
                    <template v-else>
                      <!-- Búsqueda (solo si >5) -->
                      <div v-if="mostrarBusquedaMascotas" class="search-bar">
                        <Search :size="15" class="search-icon" />
                        <input
                          v-model="busquedaMascota"
                          type="text"
                          class="search-input"
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

                      <!-- Contador de resultados -->
                      <p v-if="busquedaMascota" class="results-count">
                        {{ mascotasFiltradas.principales.length }} de {{ mascotas.length }} mascotas
                      </p>

                      <!-- Pinned seleccionada -->
                      <div v-if="mascotasFiltradas.seleccionada" class="pinned-section">
                        <span class="pinned-label">
                          <Check :size="12" /> Seleccionada
                        </span>
                        <button
                          type="button"
                          class="pick-card selected"
                          @click="selectedMascota = mascotasFiltradas.seleccionada.idMascota"
                        >
                          <div class="pick-avatar" :style="{ backgroundColor: colorAvatar(mascotasFiltradas.seleccionada.nombre) }">
                            {{ inicialNombre(mascotasFiltradas.seleccionada.nombre) }}
                          </div>
                          <div class="pick-info">
                            <span class="pick-name">{{ mascotasFiltradas.seleccionada.nombre }}</span>
                            <span class="pick-meta">
                              {{ [mascotasFiltradas.seleccionada.nombreRaza, mascotasFiltradas.seleccionada.nombreEspecie].filter(Boolean).join(' · ') || 'Mascota' }}
                            </span>
                          </div>
                          <span class="pick-check"><Check :size="13" /></span>
                        </button>
                      </div>

                      <!-- Grid principal -->
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
                          <div class="pick-avatar" :style="{ backgroundColor: colorAvatar(m.nombre) }">
                            {{ inicialNombre(m.nombre) }}
                          </div>
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

                      <!-- Empty búsqueda -->
                      <div v-if="busquedaMascota && !mascotasFiltradas.principales.length && !mascotasFiltradas.seleccionada" class="empty-inline">
                        <Search :size="28" />
                        <p>No hay mascotas que coincidan con "{{ busquedaMascota }}".</p>
                        <button type="button" class="btn-link" @click="busquedaMascota = ''">Limpiar búsqueda</button>
                      </div>
                    </template>
                    <span v-if="stepErrors.mascota" class="form-error mt-8">{{ stepErrors.mascota }}</span>
                  </div>
                </div>

                <!-- ══ VETERINARIO ══ -->
                <div class="card">
                  <div class="card-header">
                    <div class="card-header-left">
                      <div class="card-icon"><Stethoscope :size="16" /></div>
                      <div>
                        <h3>¿Con quién quieres atenderte?</h3>
                        <p class="card-header-sub">Filtra por tipo de atención o elige directamente</p>
                      </div>
                    </div>
                  </div>
                  <div class="card-body">
                    <div v-if="!veterinarios.length" class="empty-inline">
                      <Stethoscope :size="28" />
                      <p>No hay veterinarios disponibles en este momento.</p>
                    </div>
                    <template v-else>
                      <!-- Búsqueda (solo si >5) -->
                      <div v-if="mostrarBusquedaVets" class="search-bar">
                        <Search :size="15" class="search-icon" />
                        <input
                          v-model="busquedaVeterinario"
                          type="text"
                          class="search-input"
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

                      <!-- Chips de tipo de atención (siempre visibles con el catálogo completo) -->
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

                      <!-- Contador -->
                      <p v-if="veterinariosFiltrados.hayFiltro" class="results-count">
                        {{ veterinariosFiltrados.principales.length }} de {{ veterinarios.length }} veterinarios
                      </p>

                      <!-- Pinned seleccionado -->
                      <div v-if="veterinariosFiltrados.seleccionado" class="pinned-section">
                        <span class="pinned-label">
                          <Check :size="12" /> Seleccionado
                        </span>
                        <button
                          type="button"
                          class="pick-card selected"
                          @click="selectVeterinario(veterinariosFiltrados.seleccionado)"
                        >
                          <div class="pick-avatar pick-avatar-vet" :style="{ backgroundColor: colorAvatar(veterinariosFiltrados.seleccionado.nombre) }">
                            {{ inicialesNombre(veterinariosFiltrados.seleccionado.nombre) }}
                          </div>
                          <div class="pick-info">
                            <span class="pick-name">{{ veterinariosFiltrados.seleccionado.nombre }}</span>
                            <span class="pick-meta">{{ ESPECIALIDAD_LABELS[veterinariosFiltrados.seleccionado.especialidad] || veterinariosFiltrados.seleccionado.especialidad || 'Veterinario' }}</span>
                          </div>
                          <span class="pick-check"><Check :size="13" /></span>
                        </button>
                      </div>

                      <!-- Grid principal -->
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
                          <div class="pick-avatar pick-avatar-vet" :style="{ backgroundColor: colorAvatar(v.nombre) }">
                            {{ inicialesNombre(v.nombre) }}
                          </div>
                          <div class="pick-info">
                            <span class="pick-name">{{ v.nombre }}</span>
                            <span class="pick-meta">{{ ESPECIALIDAD_LABELS[v.especialidad] || v.especialidad || 'Veterinario' }}</span>
                          </div>
                          <span v-if="selectedVeterinario === v.id" class="pick-check">
                            <Check :size="13" />
                          </span>
                        </button>
                      </div>

                      <!-- Empty filtros -->
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
                    <span v-if="stepErrors.veterinario" class="form-error mt-8">{{ stepErrors.veterinario }}</span>
                  </div>
                </div>

                <!-- ══ SERVICIO ══ -->
                <div class="card">
                  <div class="card-header">
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
                    <span v-if="isLoadingServicios" class="label-loading">
                      <div class="spinner spinner-sm" /> Cargando…
                    </span>
                  </div>
                  <div class="card-body">
                    <div v-if="!selectedVeterinario" class="empty-inline">
                      <Stethoscope :size="28" />
                      <p>Selecciona un veterinario para ver sus servicios disponibles.</p>
                    </div>
                    <div v-else-if="isLoadingServicios" class="service-skeleton">
                      <div v-for="i in 3" :key="i" class="skeleton-row" />
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
                    <span v-if="stepErrors.servicio" class="form-error mt-8">{{ stepErrors.servicio }}</span>
                  </div>
                </div>

                <!-- ══ MOTIVO ══ -->
                <div class="card">
                  <div class="card-header">
                    <div class="card-header-left">
                      <div class="card-icon"><MessageSquare :size="16" /></div>
                      <div>
                        <h3>Motivo de la consulta</h3>
                        <p class="card-header-sub">Cuéntanos brevemente qué sucede</p>
                      </div>
                    </div>
                  </div>
                  <div class="card-body">
                    <textarea
                      v-model="motivoConsulta"
                      class="form-textarea"
                      :class="{ 'is-invalid': stepErrors.motivo }"
                      placeholder="Ej: Mi perro lleva 2 días sin comer y presenta decaimiento…"
                      maxlength="1000"
                      rows="4"
                    />
                    <div class="form-footer">
                      <span v-if="stepErrors.motivo" class="form-error">{{ stepErrors.motivo }}</span>
                      <span class="char-count">{{ motivoConsulta.length }}/1000</span>
                    </div>
                  </div>
                </div>
              </template>
            </template>
          </section>

          <!-- ── STEP 2: HORARIO ── -->
          <section v-else-if="currentStep === 2" key="step2" class="step-panel">
            <div class="card">
              <div class="card-header">
                <div class="card-header-left">
                  <div class="card-icon"><CalendarDays :size="16" /></div>
                  <div>
                    <h3>Selecciona el horario</h3>
                    <p class="card-header-sub">Elige el día y la hora que mejor te convenga</p>
                  </div>
                </div>
                <span class="vet-badge">
                  <Stethoscope :size="13" />
                  {{ vetSeleccionado?.nombre || '' }} · {{ servicioSeleccionado?.nombre || '' }}
                </span>
              </div>
              <div class="card-body">
                <div class="calendar">
                  <div class="calendar-nav">
                    <button type="button" class="cal-nav-btn" :disabled="!canGoPrevMonth" @click="prevMonth">
                      <ChevronLeft :size="18" />
                    </button>
                    <div class="cal-month-wrap">
                      <span class="cal-month-year">{{ calendarMonthLabel }}</span>
                      <span class="cal-month-hint">{{ MAX_MONTHS_AHEAD }} meses disponibles</span>
                    </div>
                    <button type="button" class="cal-nav-btn" :disabled="!canGoNextMonth" @click="nextMonth">
                      <ChevronRight :size="18" />
                    </button>
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

                <div v-if="showNoSlotsAlert" class="alert alert-warning">
                  <AlertTriangle :size="18" />
                  <span>No hay horarios disponibles para este día. Prueba con otra fecha.</span>
                </div>
                <div v-if="disponibilidadError" class="alert alert-error">
                  <AlertTriangle :size="18" />
                  <span>{{ disponibilidadError }}</span>
                  <button type="button" class="alert-action" @click="retryDisponibilidad">Reintentar</button>
                </div>
                <div v-if="confirmSelectionError" class="alert alert-error">
                  <AlertTriangle :size="18" />
                  <span>{{ confirmSelectionError }}</span>
                </div>

                <div v-if="selectedDate && !disponibilidadError" class="slots-section">
                  <h4 class="slots-title">
                    <Clock :size="15" />
                    Horarios disponibles — {{ formatDateDisplay(selectedDate) }}
                  </h4>
                  <div v-if="isLoadingSlots" class="slots-loading">
                    <div class="spinner spinner-sm" />
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
              </div>
            </div>
          </section>

          <!-- ── STEP 3: PAGO ── -->
          <section v-else-if="currentStep === 3" key="step3" class="step-panel">
            <div class="card">
              <div class="card-header">
                <div class="card-header-left">
                  <div class="card-icon"><CreditCard :size="16" /></div>
                  <div>
                    <h3>Método de pago</h3>
                    <p class="card-header-sub">Elige cómo deseas realizar el pago</p>
                  </div>
                </div>
                <span class="reserved-badge">
                  <Lock :size="11" /> Horario reservado
                </span>
              </div>
              <div class="card-body">
                <div v-if="pagoError" class="alert alert-error" style="margin-top: 0; margin-bottom: 16px;">
                  <AlertTriangle :size="18" />
                  <span>{{ pagoError }}</span>
                </div>
                <div v-if="isLoadingMetodos" class="loading-box">
                  <div class="spinner" />
                  <p>Cargando métodos de pago…</p>
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
                        <input
                          type="text"
                          class="form-input"
                          :class="{ 'is-invalid': pagoFormErrors.referencia }"
                          placeholder="Ej: 0000123456789"
                          v-model="referenciaTransaccion"
                        />
                        <span v-if="pagoFormErrors.referencia" class="form-error">{{ pagoFormErrors.referencia }}</span>
                      </div>
                      <div v-for="campo in camposDinamicos" :key="campo.key" class="form-group">
                        <label class="form-label">
                          {{ formatFieldLabel(campo.key) }} <span class="required">*</span>
                        </label>
                        <select
                          v-if="campo.key === 'banco'"
                          class="form-select"
                          :class="{ 'is-invalid': pagoFormErrors[campo.key] }"
                          v-model="datosPago[campo.key]"
                        >
                          <option value="" disabled>Seleccione el banco emisor</option>
                          <option v-for="banco in BANCOS_VENEZUELA" :key="banco.codigo" :value="banco.nombre">
                            {{ banco.codigo }} - {{ banco.nombre }}
                          </option>
                        </select>
                        <input
                          v-else
                          type="text"
                          class="form-input"
                          :class="{ 'is-invalid': pagoFormErrors[campo.key] }"
                          :placeholder="formatFieldPlaceholder(campo.key)"
                          v-model="datosPago[campo.key]"
                        />
                        <span v-if="pagoFormErrors[campo.key]" class="form-error">{{ pagoFormErrors[campo.key] }}</span>
                      </div>
                    </div>
                  </Transition>

                  <button
                    type="button"
                    class="btn-pay-main"
                    :disabled="!selectedMetodoPago || isProcessingPayment"
                    @click="procesarPago"
                  >
                    <div v-if="isProcessingPayment" class="spinner spinner-white spinner-sm" />
                    <CreditCard v-else :size="18" />
                    {{ isProcessingPayment ? 'Procesando pago…' : 'Procesar Pago' }}
                  </button>
                  <p class="payment-disclaimer">
                    <Lock :size="11" />
                    El pago quedará pendiente de verificación por nuestro personal.
                  </p>
                </template>
              </div>
            </div>
          </section>

          <!-- ── STEP 4: ÉXITO ── -->
          <section v-else-if="currentStep === 4" key="step4" class="step-panel step-success">
            <div class="success-container">
              <div class="success-icon">
                <Check :size="40" />
              </div>
              <h2 class="success-title">¡Pago procesado!</h2>
              <p class="success-message">
                Tu pago fue registrado exitosamente. La cita queda confirmada y lista para atenderte.
              </p>

              <div v-if="facturaAdvertencia" class="alert alert-warning" style="max-width: 520px;">
                <AlertTriangle :size="18" />
                <span>{{ facturaAdvertencia }}</span>
              </div>

              <div v-if="facturaNumeroControl" class="success-factura-info">
                <div class="factura-info-row">
                  <span class="factura-label">Número de factura</span>
                  <span class="factura-value">{{ facturaNumeroControl }}</span>
                </div>
                <div class="factura-info-row">
                  <span class="factura-label">Estado del pago</span>
                  <span class="factura-status">Pendiente de verificación</span>
                </div>
                <div v-if="citaResumen?.fecha" class="factura-info-row">
                  <span class="factura-label">Fecha de la cita</span>
                  <span class="factura-value">{{ formatDateDisplay(citaResumen.fecha) }}</span>
                </div>
                <div v-if="citaResumen?.horaInicio" class="factura-info-row">
                  <span class="factura-label">Horario</span>
                  <span class="factura-value">
                    {{ formatTime12h(citaResumen.horaInicio) }} — {{ formatTime12h(citaResumen.horaFin) }}
                  </span>
                </div>
              </div>

              <div class="success-actions">
                <button type="button" class="btn-download" @click="descargarComprobante">
                  <Download :size="16" />
                  Descargar comprobante
                </button>
                <router-link to="/cliente/historial-pagos" class="btn-history">
                  <Receipt :size="16" />
                  Ver en historial
                </router-link>
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
          <header class="summary-card-header">
            <h4>Resumen de tu reserva</h4>
            <span class="summary-card-hint">Se actualiza en tiempo real</span>
          </header>
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
              Pago seguro. La reserva queda bloqueada hasta completar el pago.
            </p>
          </footer>
        </div>
      </aside>
    </div>

    <!-- ═══ STEP ACTIONS ═══ -->
    <div v-if="currentStep < 4" class="step-actions">
      <button type="button" class="btn-cancel" @click="cancelProcess">
        <X :size="15" /> Cancelar
      </button>
      <div class="step-actions-right">
        <button v-if="currentStep > 1" type="button" class="btn-back" @click="goBack">
          <ArrowLeft :size="15" /> Atrás
        </button>
        <button
          v-if="currentStep === 1"
          type="button"
          class="btn-next"
          :disabled="!canGoNextFromStep1"
          @click="goToStep2"
        >
          Siguiente <ArrowRight :size="15" />
        </button>
        <button
          v-if="currentStep === 2"
          type="button"
          class="btn-next"
          :disabled="!selectedBloque || isConfirmingSelection"
          @click="confirmarSeleccion"
        >
          <div v-if="isConfirmingSelection" class="spinner spinner-white spinner-sm" />
          <template v-else>Confirmar horario <ArrowRight :size="15" /></template>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Check, ChevronLeft, ChevronRight, Clock, PawPrint, Stethoscope,
  CreditCard, CalendarDays, ClipboardList, MessageSquare, CircleDot,
  DollarSign, Lock, AlertTriangle, X, ArrowLeft, ArrowRight,
  LayoutDashboard, Download, Receipt, Sparkles, Inbox, Search
} from 'lucide-vue-next'
import { getMisMascotas } from '@/api/mascotas.api'
import { getServicios, getVeterinarios, getDisponibilidad, solicitarCita } from '@/api/citas.api'
import { getMetodosOnline, procesarPagoCita, descargarFactura } from '@/api/pagos.api'

const router = useRouter()
const DIAS_SEMANA = ['Lun','Mar','Mié','Jue','Vie','Sáb','Dom']
const MESES = ['Enero','Febrero','Marzo','Abril','Mayo','Junio','Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre']
const MAX_MONTHS_AHEAD = 3
const PALETA_AVATARES = ['#0F766E', '#3B82F6', '#F59E0B', '#F43F5E', '#8B5CF6', '#0EA5E9']

// Umbrales para el comportamiento adaptativo
const UMBRAL_BUSQUEDA = 5
const UMBRAL_CHIPS_VETS = 8
const UMBRAL_SCROLL_MASCOTAS = 8
const UMBRAL_SCROLL_VETS = 10

// ─── Catálogo de tipos de atención (coincide con el CHECK de la BD) ───
// Fuente de verdad: V6__agregar_check_tabla_personal.sql
// El backend restringe a estos 4 valores — los mostramos siempre
// para que el usuario vea las opciones disponibles, aunque no haya
// veterinarios cargados con esa especialidad todavía.
const ESPECIALIDADES_VET = ['Consulta', 'Vacunacion', 'Cirugia', 'Estetica']

// Etiquetas humanizadas (el valor en BD no lleva tildes, la UI sí)
const ESPECIALIDAD_LABELS = {
  Consulta: 'Consulta',
  Vacunacion: 'Vacunación',
  Cirugia: 'Cirugía',
  Estetica: 'Estética',
}

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

const steps = [
  { num: 1, label: 'Datos', desc: 'Mascota y servicio' },
  { num: 2, label: 'Horario', desc: 'Fecha y hora' },
  { num: 3, label: 'Pago', desc: 'Confirmación' },
]

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

// Búsqueda y filtros adaptativos
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
const mascotaSeleccionada = computed(() => mascotas.value.find(m => m.idMascota === selectedMascota.value) || null)

// ─── Umbrales reactivos ───
const mostrarBusquedaMascotas = computed(() => mascotas.value.length > UMBRAL_BUSQUEDA)
const mostrarBusquedaVets = computed(() => veterinarios.value.length > UMBRAL_BUSQUEDA)
const mostrarChipsVets = computed(() => veterinarios.value.length > UMBRAL_CHIPS_VETS)
const scrollMascotas = computed(() => mascotas.value.length > UMBRAL_SCROLL_MASCOTAS)
const scrollVets = computed(() => veterinarios.value.length > UMBRAL_SCROLL_VETS)

// ─── Chips de tipo de atención ───
// Se muestran SIEMPRE los 4 valores del catálogo del backend, con conteo.
// Las que tienen 0 vets quedan deshabilitadas visualmente (dashed + dimmed).
const chipsEspecialidades = computed(() => {
  const conteos = {}
  for (const v of veterinarios.value) {
    if (v.especialidad) conteos[v.especialidad] = (conteos[v.especialidad] || 0) + 1
  }
  return ESPECIALIDADES_VET.map(esp => ({
    value: esp,
    label: ESPECIALIDAD_LABELS[esp] || esp,
    count: conteos[esp] || 0,
    disabled: (conteos[esp] || 0) === 0,
  }))
})

// ─── Mascotas filtradas (con pin de la seleccionada si queda fuera) ───
const mascotasFiltradas = computed(() => {
  const q = busquedaMascota.value.trim().toLowerCase()
  const lista = mascotas.value
  if (!q) return { principales: lista, seleccionada: null }
  const filtradas = lista.filter(m =>
    [m.nombre, m.nombreRaza, m.nombreEspecie]
      .filter(Boolean)
      .some(v => String(v).toLowerCase().includes(q))
  )
  const sel = lista.find(m => m.idMascota === selectedMascota.value)
  const enResultados = sel && filtradas.some(m => m.idMascota === sel.idMascota)
  return {
    principales: filtradas,
    seleccionada: sel && !enResultados ? sel : null,
  }
})

// ─── Veterinarios filtrados (búsqueda + especialidad + pin) ───
const veterinariosFiltrados = computed(() => {
  const q = busquedaVeterinario.value.trim().toLowerCase()
  const esp = filtroEspecialidad.value
  const lista = veterinarios.value
  let filtradas = lista
  if (q) filtradas = filtradas.filter(v => String(v.nombre || '').toLowerCase().includes(q))
  if (esp) filtradas = filtradas.filter(v => v.especialidad === esp)
  const hayFiltro = Boolean(q || esp)
  const sel = lista.find(v => v.id === selectedVeterinario.value)
  const enResultados = sel && filtradas.some(v => v.id === sel.id)
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
  () => selectedMascota.value && selectedVeterinario.value && selectedServicio.value && motivoConsulta.value.trim().length > 0
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
    total: r.costoUsd != null ? formatCurrency(r.costoUsd) : (servicioSeleccionado.value ? formatCurrency(servicioSeleccionado.value.precioUsd) : null),
    totalBs: r.costoBs != null ? formatCurrencyBs(r.costoBs) : null,
  }
})

const calendarDays = computed(() => {
  const year = calendarYear.value, month = calendarMonth.value
  const firstDay = new Date(year, month, 1)
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const offset = firstDay.getDay() === 0 ? 6 : firstDay.getDay() - 1
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const days = []
  const prevLast = new Date(year, month, 0).getDate()
  for (let i = offset - 1; i >= 0; i--) {
    const d = new Date(year, month - 1, prevLast - i)
    days.push({ date: d, isCurrentMonth: false, isPast: true, isToday: false })
  }
  for (let d = 1; d <= daysInMonth; d++) {
    const date = new Date(year, month, d)
    days.push({ date, isCurrentMonth: true, isPast: date < today, isToday: date.getTime() === today.getTime() })
  }
  const totalRows = Math.ceil(days.length / 7)
  const remaining = totalRows * 7 - days.length
  for (let d = 1; d <= remaining; d++) {
    days.push({ date: new Date(year, month + 1, d), isCurrentMonth: false, isPast: false, isToday: false })
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
  () => selectedDate.value && fechaFueConsultada.value && currentSlots.value.length === 0 && !disponibilidadError.value
)
const metodoSeleccionado = computed(() => metodosPago.value.find(m => m.id === selectedMetodoPago.value) || null)
const camposDinamicos = computed(() => {
  if (!metodoSeleccionado.value?.camposRequeridos) return []
  return Object.entries(metodoSeleccionado.value.camposRequeridos)
    .filter(([key]) => key !== 'referencia')
    .map(([key, tipo]) => ({ key, tipo }))
})

onMounted(() => { loadInitialData() })
watch([selectedServicio, selectedVeterinario], () => {
  selectedDate.value = null
  selectedBloque.value = null
  disponibilidadCache.value = {}
  disponibilidadError.value = null
})

// ─── Helpers de avatar ───
function colorAvatar(nombre) {
  let hash = 0
  for (const caracter of String(nombre || '')) hash = (hash * 31 + caracter.charCodeAt(0)) % 997
  return PALETA_AVATARES[hash % PALETA_AVATARES.length]
}
function inicialNombre(nombre) {
  return String(nombre || '?').trim().charAt(0).toUpperCase() || '?'
}
function inicialesNombre(nombre) {
  const cleaned = String(nombre || '').replace(/^(Dra?\.?|Dr\.?)\s*/i, '').trim()
  if (!cleaned) return '?'
  const words = cleaned.split(/\s+/).filter(Boolean)
  if (words.length === 1) return words[0].charAt(0).toUpperCase()
  return (words[0].charAt(0) + words[words.length - 1].charAt(0)).toUpperCase()
}

function selectVeterinario(v) {
  selectedVeterinario.value = v.id
  onVeterinarioChange()
}

async function loadInitialData() {
  isLoadingInitial.value = true; loadError.value = null
  try {
    const [mascotasRes, vetsRes] = await Promise.all([getMisMascotas(), getVeterinarios()])
    mascotas.value = mascotasRes.data; veterinarios.value = vetsRes.data
  } catch (err) {
    console.error('Error cargando datos iniciales:', err)
    loadError.value = err.response?.data?.mensaje || 'No se pudieron cargar los datos necesarios.'
  } finally {
    isLoadingInitial.value = false
  }
}

async function onVeterinarioChange() {
  selectedServicio.value = null; stepErrors.value = {}; servicios.value = []
  if (!selectedVeterinario.value) return
  const vet = vetSeleccionado.value; if (!vet?.especialidad) return
  isLoadingServicios.value = true
  try {
    const { data } = await getServicios({ especialidad: vet.especialidad })
    servicios.value = data
  } catch (err) {
    console.error('Error cargando servicios:', err)
  } finally {
    isLoadingServicios.value = false
  }
}

function validateStep1() {
  const errors = {}
  if (!selectedMascota.value) errors.mascota = 'Selecciona una mascota'
  if (!selectedVeterinario.value) errors.veterinario = 'Selecciona un veterinario'
  if (!selectedServicio.value) errors.servicio = 'Selecciona un servicio'
  if (!motivoConsulta.value.trim()) errors.motivo = 'Escribe el motivo de la consulta'
  stepErrors.value = errors
  return Object.keys(errors).length === 0
}
function goToStep2() { if (!validateStep1()) return; stepErrors.value = {}; currentStep.value = 2 }
function goBack() { pagoError.value = null; pagoFormErrors.value = {}; currentStep.value-- }
async function cancelProcess() { goToDashboard() }
function goToDashboard() { router.push('/cliente/dashboard') }

function prevMonth() {
  if (!canGoPrevMonth.value) return
  const d = new Date(calendarYear.value, calendarMonth.value - 1, 1)
  calendarYear.value = d.getFullYear(); calendarMonth.value = d.getMonth()
}
function nextMonth() {
  if (!canGoNextMonth.value) return
  const d = new Date(calendarYear.value, calendarMonth.value + 1, 1)
  calendarYear.value = d.getFullYear(); calendarMonth.value = d.getMonth()
}
function isDateSelected(date) {
  if (!selectedDate.value) return false
  return formatDateISO(date) === formatDateISO(selectedDate.value)
}
function hasSlots(date) {
  const key = formatDateISO(date)
  return disponibilidadCache.value[key]?.length > 0
}
function hasNoSlots(date) {
  const key = formatDateISO(date)
  return key in disponibilidadCache.value && disponibilidadCache.value[key].length === 0
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
  isLoadingSlots.value = true; disponibilidadError.value = null
  try {
    const { data } = await getDisponibilidad({
      id_veterinario: selectedVeterinario.value,
      fecha: dateStr,
      id_servicio: selectedServicio.value,
    })
    disponibilidadCache.value[dateStr] = data.bloquesDisponibles || []
  } catch (err) {
    console.error('Error consultando disponibilidad:', err)
    disponibilidadError.value = err.response?.data?.mensaje || 'Error al consultar disponibilidad. Intenta de nuevo.'
    disponibilidadCache.value[dateStr] = []
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
function isBloqueSelected(bloque) { return selectedBloque.value?.horaInicio === bloque.horaInicio }
function selectBloque(bloque) { selectedBloque.value = bloque; confirmSelectionError.value = null }

async function confirmarSeleccion() {
  if (!selectedBloque.value || !selectedDate.value) return
  isConfirmingSelection.value = true; confirmSelectionError.value = null
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
    pagoError.value = null
    currentStep.value = 3
    await loadMetodosPago()
  } catch (err) {
    console.error('Error al solicitar cita:', err)
    const msg = err.response?.data?.mensaje
    if (msg && msg.includes('ya no está disponible')) {
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

async function procesarPago() {
  if (!citaPendienteId.value || !selectedMetodoPago.value) return
  const camposRequeridos = metodoSeleccionado.value?.camposRequeridos || {}
  const errors = {}
  if ('referencia' in camposRequeridos) {
    if (!referenciaTransaccion.value.trim()) errors.referencia = 'La referencia es obligatoria'
  }
  for (const { key } of camposDinamicos.value) {
    if (!datosPago.value[key]?.trim()) errors[key] = 'Este campo es obligatorio'
  }
  pagoFormErrors.value = errors
  if (Object.keys(errors).length > 0) return
  isProcessingPayment.value = true; pagoError.value = null
  try {
    const datosPagoCompletos = { ...datosPago.value }
    if ('referencia' in camposRequeridos) datosPagoCompletos.referencia = referenciaTransaccion.value.trim()
    const { data } = await procesarPagoCita({
      idCita: citaPendienteId.value,
      idMetodoPago: selectedMetodoPago.value,
      referenciaTransaccion: referenciaTransaccion.value.trim(),
      datosPago: datosPagoCompletos,
    })
    facturaId.value = data.idFactura
    facturaNumeroControl.value = data.numeroControl
    facturaEmailEnviado.value = data.emailEnviado
    facturaAdvertencia.value = data.advertenciaEmail
    const savedResumen = { ...citaResumen.value }
    citaPendienteId.value = null
    citaResumen.value = savedResumen
    currentStep.value = 4
  } catch (err) {
    console.error('Error procesando pago:', err)
    const msg = err.response?.data?.mensaje || err.response?.data?.message
    pagoError.value = msg || 'Transacción rechazada o datos inválidos. Verifique e intente nuevamente.'
  } finally {
    isProcessingPayment.value = false
  }
}

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
async function loadMetodosPago() {
  isLoadingMetodos.value = true
  try {
    const { data } = await getMetodosOnline()
    metodosPago.value = data
  } catch (err) {
    console.error('Error cargando métodos:', err)
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
function formatFieldLabel(key) {
  const labels = {
    banco: 'Banco emisor',
    numero_cuenta: 'Número de cuenta',
    telefono: 'Número de teléfono asociado',
    lote: 'Número de lote',
    ultimos_digitos: 'Últimos 4 dígitos de la tarjeta',
  }
  return labels[key] || key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}
function formatFieldPlaceholder(key) {
  const ph = {
    banco: 'Ej: Banesco, Mercantil, Provincial...',
    numero_cuenta: 'Ej: 0000-0000-00-0000000000',
    telefono: 'Ej: 04141234567',
  }
  return ph[key] || ''
}
async function descargarComprobante() {
  if (!facturaId.value) return
  try {
    const response = await descargarFactura(facturaId.value)
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `Factura-${facturaNumeroControl.value}.pdf`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch (err) {
    console.error('Error descargando factura:', err)
  }
}
</script>

<style scoped>
/* ═══ CONTENEDOR ═══ */
.appointment-view {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 24px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
}
button { font-family: inherit; }

/* ═══ HERO ═══ */
.wizard-hero {
  padding: 26px 28px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 55%);
  border: 1px solid #CCFBF1;
  border-radius: 16px;
}
.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 10px;
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
  padding: 16px 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  gap: 8px;
}
.stepper-step {
  display: flex;
  align-items: center;
  gap: 12px;
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
  font-size: 13px;
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
  font-size: 13.5px;
  font-weight: 700;
  color: #94A3B8;
  transition: color .25s ease;
  white-space: nowrap;
}
.stepper-step.active .stepper-label { color: #0F766E; }
.stepper-step.completed .stepper-label { color: #1E293B; }
.stepper-desc {
  font-size: 11.5px;
  color: #94A3B8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.stepper-line {
  flex: 1;
  height: 2px;
  background: #E2E8F0;
  margin: 0 12px;
  border-radius: 1px;
  transition: background .3s ease;
  min-width: 20px;
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
.wizard-main { min-width: 0; display: flex; flex-direction: column; gap: 16px; }

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
.summary-card-header { padding: 18px 20px 14px; border-bottom: 1px solid #F1F5F9; }
.summary-card-header h4 { margin: 0 0 2px; font-size: 14.5px; font-weight: 700; color: #0F172A; }
.summary-card-hint { font-size: 11.5px; color: #94A3B8; }
.summary-card-body { padding: 14px 20px 18px; display: flex; flex-direction: column; gap: 12px; }
.summary-line { display: flex; flex-direction: column; gap: 3px; transition: opacity .2s ease; }
.summary-line.is-empty { opacity: .55; }
.summary-line-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
}
.summary-line-value {
  font-size: 13.5px;
  font-weight: 600;
  color: #0F172A;
  line-height: 1.35;
  word-break: break-word;
}
.summary-line.is-empty .summary-line-value { color: #94A3B8; font-weight: 500; font-style: italic; }
.summary-card-footer {
  padding: 16px 20px 20px;
  background: linear-gradient(180deg, #F8FAFC 0%, #F0FDFA 100%);
  border-top: 1px solid #E2E8F0;
}
.summary-total { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; }
.total-label {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
}
.total-value { font-size: 20px; font-weight: 700; color: #0F766E; letter-spacing: -0.01em; }
.total-bs { margin-top: 2px; font-size: 12px; font-weight: 500; color: #64748B; text-align: right; }
.summary-note {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 14px 0 0;
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
  padding: 18px 24px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.card-header-left { display: flex; align-items: center; gap: 12px; min-width: 0; }
.card-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #F0FDFA;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.card-header h3 { font-size: 15px; font-weight: 700; color: #0F172A; margin: 0; line-height: 1.2; }
.card-header-sub { margin: 2px 0 0; font-size: 12px; color: #64748B; }
.card-body { padding: 20px 24px 24px; }

/* ═══ BÚSQUEDA ═══ */
.search-bar { position: relative; margin-bottom: 12px; }
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

/* ═══ CHIPS DE TIPO DE ATENCIÓN ═══ */
.chips-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}
.chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 12px 6px 14px;
  border: 1px solid #E2E8F0;
  background: #fff;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  white-space: nowrap;
}
.chip:hover:not(:disabled) {
  background: #F0FDFA;
  border-color: #99F6E4;
  color: #0F766E;
}
.chip.active {
  background: #0F766E;
  color: #fff;
  border-color: #0F766E;
  box-shadow: 0 2px 8px rgba(15, 118, 110, .25);
}
.chip-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 18px;
  padding: 0 6px;
  border-radius: 10px;
  background: #F1F5F9;
  color: #64748B;
  font-size: 10.5px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  transition: background-color .2s ease, color .2s ease;
}
.chip.active .chip-count {
  background: rgba(255, 255, 255, .22);
  color: #fff;
}
.chip.is-empty {
  opacity: .45;
  cursor: not-allowed;
  border-style: dashed;
}
.chip.is-empty .chip-count {
  background: transparent;
  color: #94A3B8;
}

/* ═══ CONTADOR DE RESULTADOS ═══ */
.results-count {
  margin: 0 0 12px;
  font-size: 12px;
  font-weight: 600;
  color: #64748B;
  letter-spacing: .1px;
}

/* ═══ PINNED SECTION ═══ */
.pinned-section {
  margin-bottom: 14px;
  padding: 12px 14px;
  background: #F0FDFA;
  border: 1px solid #99F6E4;
  border-radius: 12px;
}
.pinned-label {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #0F766E;
  margin-bottom: 8px;
}
.pinned-section .pick-card { margin-bottom: 0; }

/* ═══ PICK GRID ═══ */
.pick-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 10px;
}
.pick-grid.scrollable {
  max-height: 420px;
  overflow-y: auto;
  padding-right: 6px;
  scrollbar-width: thin;
  scrollbar-color: #CBD5E1 transparent;
}
.pick-grid.scrollable::-webkit-scrollbar { width: 8px; }
.pick-grid.scrollable::-webkit-scrollbar-track { background: transparent; }
.pick-grid.scrollable::-webkit-scrollbar-thumb {
  background: #CBD5E1;
  border-radius: 4px;
}
.pick-grid.scrollable::-webkit-scrollbar-thumb:hover { background: #94A3B8; }

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
.pick-avatar {
  width: 44px;
  height: 44px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 17px;
  flex-shrink: 0;
  box-shadow: 0 4px 10px -3px rgba(15, 23, 42, .12);
  letter-spacing: -0.02em;
}
.pick-avatar-vet { font-size: 14px; letter-spacing: .3px; }
.pick-info { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
.pick-name {
  font-size: 14px;
  font-weight: 700;
  color: #0F172A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.25;
}
.pick-meta {
  font-size: 12px;
  color: #64748B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pick-check {
  width: 22px;
  height: 22px;
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

/* ═══ EMPTY INLINE ═══ */
.empty-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 28px 20px;
  color: #94A3B8;
  text-align: center;
  font-size: 13.5px;
}
.empty-inline p { margin: 0; }
.btn-link {
  background: none;
  border: none;
  padding: 0;
  color: #0F766E;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
}
.btn-link:hover { text-decoration: underline; }

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
@keyframes radioPop { 0% { transform: scale(0); } 100% { transform: scale(1); } }
.service-item-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.service-item-name { font-size: 14px; font-weight: 700; color: #0F172A; line-height: 1.3; }
.service-item-desc { font-size: 12.5px; color: #64748B; line-height: 1.5; }
.service-item-meta { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-top: 4px; }
.meta-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #0F766E;
  background: #fff;
  padding: 3px 10px;
  border-radius: 20px;
  border: 1px solid #CCFBF1;
}
.meta-price { color: #0F172A; border-color: #E2E8F0; }
.meta-badge {
  font-size: 11px;
  font-weight: 700;
  color: #0F766E;
  background: #CCFBF1;
  padding: 3px 10px;
  border-radius: 20px;
  border: 1px solid #99F6E4;
  text-transform: capitalize;
}

/* ═══ SKELETON ═══ */
.service-skeleton { display: flex; flex-direction: column; gap: 8px; }
.skeleton-row {
  height: 76px;
  border-radius: 12px;
  background: linear-gradient(90deg, #F1F5F9 25%, #E2E8F0 50%, #F1F5F9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ═══ FORM ═══ */
.form-group { display: flex; flex-direction: column; gap: 6px; min-width: 0; }
.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  font-weight: 600;
  color: #374151;
}
.required { color: #EF4444; }
.label-loading { display: inline-flex; align-items: center; gap: 6px; font-size: 11px; font-weight: 500; color: #64748B; }
.form-select,
.form-textarea,
.form-input {
  width: 100%;
  padding: 11px 14px;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  font-size: 14px;
  color: #1E293B;
  background: #fff;
  font-family: inherit;
  transition: border-color .2s, box-shadow .2s;
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
.form-input:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15, 118, 110, .12);
}
.form-select.is-invalid,
.form-textarea.is-invalid,
.form-input.is-invalid { border-color: #EF4444; }
.form-select.is-invalid:focus,
.form-textarea.is-invalid:focus,
.form-input.is-invalid:focus { box-shadow: 0 0 0 3px rgba(239, 68, 68, .12); }
.form-textarea { resize: vertical; min-height: 96px; line-height: 1.55; }
.form-error { font-size: 12px; color: #EF4444; font-weight: 600; }
.form-error.mt-8 { display: block; margin-top: 10px; }
.form-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 4px; }
.char-count { font-size: 11px; color: #94A3B8; margin-left: auto; }

/* ═══ LOADING ═══ */
.loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  gap: 14px;
  color: #64748B;
  font-size: 13.5px;
}
.spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #E2E8F0;
  border-top-color: #0F766E;
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
.spinner-sm { width: 16px; height: 16px; border-width: 2px; }
.spinner-white { border-color: rgba(255,255,255,.3); border-top-color: #fff; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══ ALERTAS ═══ */
.alert {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  margin-top: 16px;
}
.alert-error { background: #FEF2F2; color: #991B1B; border: 1px solid #FECACA; }
.alert-warning { background: #FFFBEB; color: #92400E; border: 1px solid #FDE68A; }
.alert-action {
  margin-left: auto;
  background: none;
  border: none;
  color: inherit;
  font-weight: 600;
  font-size: 12px;
  cursor: pointer;
  text-decoration: underline;
  white-space: nowrap;
  flex-shrink: 0;
}

/* ═══ CALENDARIO ═══ */
.calendar { margin-bottom: 8px; }
.calendar-nav { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px; }
.cal-nav-btn {
  width: 38px;
  height: 38px;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  background: #fff;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .2s;
  flex-shrink: 0;
}
.cal-nav-btn:hover:not(:disabled) { background: #F0FDFA; color: #0F766E; border-color: #99F6E4; }
.cal-nav-btn:disabled { opacity: .35; cursor: not-allowed; }
.cal-month-wrap { text-align: center; }
.cal-month-year { display: block; font-size: 15px; font-weight: 700; color: #0F172A; letter-spacing: -0.01em; }
.cal-month-hint { display: block; font-size: 11px; color: #94A3B8; margin-top: 2px; }
.calendar-weekdays { display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); margin-bottom: 6px; }
.cal-weekday {
  text-align: center;
  font-size: 11px;
  font-weight: 700;
  color: #94A3B8;
  text-transform: uppercase;
  letter-spacing: .5px;
  padding: 6px 0;
}
.calendar-grid { display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: 4px; }
.cal-day {
  width: 100%;
  aspect-ratio: 1 / 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13.5px;
  font-weight: 600;
  color: #1E293B;
  border: 2px solid transparent;
  border-radius: 10px;
  background: none;
  cursor: pointer;
  transition: all .15s ease;
  position: relative;
  padding: 0;
  min-width: 0;
  min-height: 0;
  box-sizing: border-box;
}
.cal-day:hover:not(:disabled):not(.other-month):not(.is-past) {
  background: #F0FDFA;
  border-color: #99F6E4;
}
.cal-day.other-month { color: #CBD5E1; cursor: default; }
.cal-day.is-past { color: #CBD5E1; cursor: not-allowed; }
.cal-day.is-today { font-weight: 700; color: #0F766E; }
.cal-day.is-selected {
  background: #0F766E;
  color: #fff;
  border-color: #0F766E;
  box-shadow: 0 4px 12px rgba(15, 118, 110, .35);
}
.cal-day.is-selected.is-today { color: #fff; }
.cal-day.has-slots:not(.is-selected)::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #10B981;
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
  background: #FCA5A5;
}
.calendar-legend {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #F1F5F9;
}
.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  color: #64748B;
  font-weight: 500;
}
.legend-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.dot-available { background: #10B981; }
.dot-full { background: #FCA5A5; }
.dot-today { background: #0F766E; }

.vet-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #0F766E;
  background: #F0FDFA;
  padding: 6px 14px;
  border-radius: 20px;
  border: 1px solid #99F6E4;
}

/* ═══ SLOTS ═══ */
.slots-section { margin-top: 22px; padding-top: 22px; border-top: 1px solid #E2E8F0; }
.slots-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
  margin: 0 0 14px;
}
.slots-loading {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 24px;
  color: #64748B;
  font-size: 13px;
}
.slots-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 10px;
}
.slot-btn {
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
  position: relative;
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
.slot-time { font-size: 13.5px; font-weight: 700; color: #0F172A; transition: color .2s; }
.slot-btn.is-selected .slot-time { color: #fff; }
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
.slots-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px 20px;
  color: #94A3B8;
  text-align: center;
  font-size: 13px;
}
.slots-empty p { margin: 0; }

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

.dynamic-fields {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #E2E8F0;
}

/* ═══ BOTÓN PAGAR ═══ */
.btn-pay-main {
  width: 100%;
  padding: 15px 24px;
  background: #0F766E;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all .2s ease;
  margin-top: 20px;
  font-family: inherit;
}
.btn-pay-main:hover:not(:disabled) {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 8px 20px -6px rgba(15, 118, 110, .4);
}
.btn-pay-main:disabled { opacity: .55; cursor: not-allowed; }
.payment-disclaimer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  margin-top: 12px;
  font-size: 11.5px;
  color: #94A3B8;
  line-height: 1.5;
  text-align: center;
}
.reserved-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 11.5px;
  font-weight: 700;
  color: #D97706;
  background: #FFFBEB;
  padding: 5px 12px;
  border-radius: 20px;
  border: 1px solid #FDE68A;
  white-space: nowrap;
}

/* ═══ ÉXITO ═══ */
.step-success { max-width: 680px; margin: 0 auto; }
.success-container {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #E2E8F0;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, .03), 0 10px 15px -3px rgba(0, 0, 0, .04);
  padding: 48px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 14px;
}
.success-icon {
  width: 88px;
  height: 88px;
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
  font-size: 24px;
  font-weight: 700;
  color: #0F172A;
  margin: 0;
  letter-spacing: -0.01em;
}
.success-message {
  font-size: 14px;
  color: #64748B;
  margin: 0;
  max-width: 460px;
  line-height: 1.6;
}
.success-factura-info {
  margin-top: 8px;
  padding: 18px 22px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 460px;
  text-align: left;
}
.factura-info-row { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
.factura-label { font-size: 12.5px; color: #64748B; }
.factura-value {
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
  font-family: ui-monospace, 'SF Mono', Menlo, Consolas, monospace;
}
.factura-status {
  font-size: 11.5px;
  font-weight: 700;
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
  padding: 13px 24px;
  background: #0F766E;
  color: #fff;
  border: none;
  border-radius: 11px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
}
.btn-download:hover {
  background: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px -6px rgba(15, 118, 110, .4);
}
.btn-history {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 13px 24px;
  background: #fff;
  color: #475569;
  border: 1px solid #E2E8F0;
  border-radius: 11px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all .2s ease;
  font-family: inherit;
  text-decoration: none;
}
.btn-history:hover { background: #F8FAFC; border-color: #CBD5E1; color: #1E293B; }
.btn-back-dashboard {
  margin-top: 14px;
  padding: 11px 24px;
  background: transparent;
  color: #64748B;
  border: none;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 7px;
  transition: all .2s ease;
  font-family: inherit;
  border-radius: 10px;
}
.btn-back-dashboard:hover { background: #F1F5F9; color: #0F766E; }

/* ═══ STEP ACTIONS ═══ */
.step-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 20px;
  padding: 18px 24px;
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
.btn-next {
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
  .appointment-view { padding: 16px; }
  .wizard-hero { padding: 20px; }
  .wizard-hero h1 { font-size: 22px; }
  .stepper {
    flex-direction: column;
    align-items: flex-start;
    padding: 16px;
    gap: 14px;
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
  .step-actions { flex-direction: column-reverse; padding: 16px; }
  .step-actions-right { width: 100%; flex-direction: column-reverse; }
  .btn-cancel,
  .btn-back,
  .btn-next { width: 100%; }
  .success-actions { flex-direction: column; width: 100%; }
  .btn-download,
  .btn-history { width: 100%; justify-content: center; }
  .pick-grid { grid-template-columns: 1fr; }
  .pick-grid.scrollable { max-height: 320px; }
}
@media (max-width: 480px) {
  .card-body { padding: 16px 18px 20px; }
  .calendar-grid { gap: 2px; }
  .cal-day { font-size: 12px; border-radius: 8px; }
  .slots-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .success-container { padding: 32px 20px; }
  .success-icon { width: 72px; height: 72px; }
  .success-title { font-size: 20px; }
  .service-item { padding: 12px 14px; gap: 12px; }
  .chips-row { gap: 6px; }
  .chip { padding: 5px 10px 5px 12px; font-size: 11.5px; }
  .chip-count { min-width: 18px; height: 16px; font-size: 10px; }
}
</style>