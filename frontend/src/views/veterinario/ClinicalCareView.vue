<template>
  <div class="atencion">
    <ToastContainer />

    <!-- ══════════════ LOADING ══════════════ -->
    <div v-if="cargando" class="state-full">
      <span class="spinner spinner-lg" />
      <p>Cargando el contexto de la cita…</p>
    </div>

    <!-- ══════════════ ERROR FATAL ══════════════ -->
    <div v-else-if="errorFatal" class="state-full state-full-error">
      <div class="state-error-icon"><AlertTriangle :size="36" /></div>
      <h2 class="state-title">No fue posible abrir la atención</h2>
      <p class="state-text">{{ errorFatal }}</p>
      <button class="btn btn-primary" type="button" @click="router.push('/veterinario/agenda')">
        <ChevronLeft :size="15" /> Volver a mi agenda
      </button>
    </div>

    <!-- ══════════════ SELECTOR SIN CITA_ID ══════════════ -->
    <template v-else-if="!citaId">
      <header class="page-top">
        <div class="page-top-info">
          <span class="page-top-eyebrow">
            <Stethoscope :size="12" />
            Atención clínica
          </span>
          <h1 class="page-top-title">Selecciona una cita</h1>
          <p class="page-top-sub">Elige la cita que deseas atender hoy.</p>
        </div>
      </header>

      <section class="panel">
        <header class="panel-header">
          <div>
            <h3 class="panel-title">Citas por atender de hoy</h3>
            <p class="panel-sub">{{ citasPendientes.length }} pendientes</p>
          </div>
          <button class="quick-action" type="button" @click="cargarCitasDelDia">
            <RefreshCw :size="13" /> Actualizar
          </button>
        </header>

        <div class="panel-body">
          <div v-if="cargandoCitas" class="state-inline">
            <span class="spinner spinner-lg" />
          </div>

          <AppEmptyState
            v-else-if="!citasPendientes.length"
            :icon="Inbox"
            title="No hay citas pendientes por atender hoy"
            description="Cuando se agenden citas contigo aparecerán en esta vista."
          >
            <template #action>
              <button class="btn btn-ghost" type="button" @click="router.push('/veterinario/agenda')">
                Ir a mi agenda
              </button>
            </template>
          </AppEmptyState>

          <ul v-else class="selector-list">
            <li v-for="c in citasPendientes" :key="c.idCita" class="selector-item">
              <div class="selector-time">{{ rangoHora(c.horaInicio, c.horaFin) }}</div>
              <div class="selector-info">
                <p class="selector-pet">
                  <PawPrint :size="14" />
                  <strong>{{ c.mascotaNombre }}</strong>
                  <span class="selector-species">{{ c.especie }} · {{ c.raza }}</span>
                </p>
                <p class="selector-meta">
                  <User :size="11" /> {{ c.clienteNombre }} · {{ c.motivoConsulta }}
                </p>
              </div>
              <span class="status-pill" :style="estiloEstado(c.estado)">
                {{ infoEstado(c.estado).etiqueta }}
              </span>
              <button
                class="btn-atender-sm"
                type="button"
                @click="router.push(`/veterinario/atencion/${c.idCita}`)"
              >
                Atender
              </button>
            </li>
          </ul>
        </div>
      </section>
    </template>

    <!-- ══════════════ MODO SOLO LECTURA ══════════════ -->
    <template v-else-if="modoSoloLectura">
      <header class="page-top">
        <div class="page-top-info">
          <span class="page-top-eyebrow">
            <Eye :size="12" />
            Consulta guardada
          </span>
          <h1 class="page-top-title">{{ mascota?.nombre }}</h1>
          <p class="page-top-sub">
            {{ atencionGuardada?.fechaHoraInicio
                ? fechaHoraCorta(atencionGuardada.fechaHoraInicio)
                : fechaCompleta(cita?.fechaCita) }}
          </p>
        </div>
        <div class="page-top-actions">
          <span class="readonly-badge">
            <Eye :size="13" /> Modo solo lectura
          </span>
        </div>
      </header>

      <div class="readonly-banner">
        <AlertTriangle :size="15" />
        <span>Esta cita ya fue atendida. Los datos no pueden modificarse.</span>
      </div>

      <div class="readonly-grid">
        <aside class="side-panel">
          <section class="side-block">
            <h4><PawPrint :size="13" /> Paciente</h4>
            <p class="side-primary">{{ mascota?.nombre }}</p>
            <p class="side-line">{{ mascota?.especie }} · {{ mascota?.raza }}</p>
            <p class="side-line">{{ mascota?.sexo }} · {{ mascota?.edad }}</p>
            <p class="side-line">Peso: {{ mascota?.pesoActualKg }} kg</p>
          </section>

          <section class="side-block">
            <h4><User :size="13" /> Dueño</h4>
            <p class="side-primary">{{ dueno?.nombreCompleto }}</p>
            <p class="side-line">{{ dueno?.documentoIdentidad }}</p>
            <p class="side-line"><Phone :size="11" /> {{ dueno?.telefonoPrincipal }}</p>
          </section>

          <section class="side-block">
            <h4><ClipboardList :size="13" /> Cita</h4>
            <p class="side-line"><strong>Motivo:</strong> {{ cita?.motivoConsulta }}</p>
            <p class="side-line"><strong>Servicio:</strong> {{ cita?.servicio }}</p>
            <p class="side-line"><strong>Horario:</strong> {{ rangoHora(cita?.horaInicio, cita?.horaFin) }}</p>
          </section>
        </aside>

        <main class="readonly-content">
          <header class="readonly-head">
            <p class="readonly-vet">
              <Stethoscope :size="14" /> {{ atencionGuardada?.veterinarioNombre }}
            </p>
            <span class="status-pill" :style="estiloEstado(atencionGuardada?.estadoAtencion)">
              {{ infoEstado(atencionGuardada?.estadoAtencion).etiqueta }}
            </span>
          </header>

          <section class="vitals-row">
            <div class="vital">
              <span class="vital-label">Peso</span>
              <span class="vital-value">{{ atencionGuardada?.pesoKg }} <small>kg</small></span>
            </div>
            <div class="vital">
              <span class="vital-label">Temperatura</span>
              <span class="vital-value">{{ atencionGuardada?.temperaturaC }} <small>°C</small></span>
            </div>
            <div class="vital">
              <span class="vital-label">Frec. cardíaca</span>
              <span class="vital-value">{{ atencionGuardada?.frecCardiaca }} <small>lpm</small></span>
            </div>
            <div v-if="atencionGuardada?.frecRespiratoria" class="vital">
              <span class="vital-label">Frec. respiratoria</span>
              <span class="vital-value">{{ atencionGuardada.frecRespiratoria }} <small>rpm</small></span>
            </div>
          </section>

          <div class="clinical-grid">
            <div class="clinical-block">
              <h5>Anamnesis</h5>
              <p>{{ atencionGuardada?.anamnesis }}</p>
            </div>
            <div v-if="atencionGuardada?.sintomasObservados" class="clinical-block">
              <h5>Hallazgos físicos</h5>
              <p>{{ atencionGuardada.sintomasObservados }}</p>
            </div>
            <div class="clinical-block clinical-block-highlight">
              <h5>Diagnóstico principal</h5>
              <p>{{ atencionGuardada?.diagnosticoPrincipal }}</p>
            </div>
            <div v-if="atencionGuardada?.diagnosticosDiferenciales" class="clinical-block">
              <h5>Diagnósticos diferenciales</h5>
              <p>{{ atencionGuardada.diagnosticosDiferenciales }}</p>
            </div>
            <div v-if="atencionGuardada?.observacionesGenerales" class="clinical-block">
              <h5>Pronóstico / observaciones</h5>
              <p>{{ atencionGuardada.observacionesGenerales }}</p>
            </div>
            <div v-if="atencionGuardada?.indicacionesDueno" class="clinical-block">
              <h5>Indicaciones al dueño</h5>
              <p>{{ atencionGuardada.indicacionesDueno }}</p>
            </div>
            <div v-if="atencionGuardada?.proximaCitaRecomendada" class="clinical-block">
              <h5>Próxima cita recomendada</h5>
              <p>{{ atencionGuardada.proximaCitaRecomendada }}</p>
            </div>
          </div>

          <section v-if="atencionGuardada?.insumos?.length" class="inner-block">
            <h5 class="inner-block-title"><Package :size="13" /> Insumos aplicados</h5>
            <div class="table-wrap">
              <table class="mini-table">
                <thead>
                  <tr>
                    <th>Insumo</th>
                    <th class="der">Cant.</th>
                    <th class="der">P. unitario</th>
                    <th class="der">Subtotal</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="i in atencionGuardada.insumos" :key="i.idProducto">
                    <td>
                      <span class="cell-main">{{ i.nombre }}</span>
                      <span class="cell-sub">{{ i.codigoSku }}</span>
                    </td>
                    <td class="der">{{ i.cantidad }} {{ i.unidadMedida }}</td>
                    <td class="der">{{ formatoUSD(i.precioUnitarioUsd) }}</td>
                    <td class="der amount">{{ formatoUSD(subtotalInsumo(i)) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p class="inner-block-total">
              <span>Total insumos</span>
              <span class="amount">{{ formatoUSD(totalInsumosGuardados) }}</span>
            </p>
          </section>

          <section v-if="atencionGuardada?.receta" class="inner-block inner-block-receta">
            <header class="inner-block-head">
              <h5 class="inner-block-title">
                <Pill :size="13" /> Récipe {{ atencionGuardada.receta.codigoReceta }}
              </h5>
              <button
                class="btn-pdf"
                type="button"
                :disabled="descargandoReceta"
                @click="descargarReceta(atencionGuardada.receta.idReceta, atencionGuardada.receta.codigoReceta)"
              >
                <Loader2 v-if="descargandoReceta" :size="13" class="spin" />
                <Download v-else :size="13" />
                Descargar
              </button>
            </header>
            <p v-if="atencionGuardada.receta.indicacionesGenerales" class="recipe-notes">
              <strong>Indicaciones generales:</strong>
              {{ atencionGuardada.receta.indicacionesGenerales }}
            </p>
            <div class="table-wrap">
              <table class="mini-table">
                <thead>
                  <tr>
                    <th>Medicamento</th>
                    <th>Concentración</th>
                    <th>Dosis</th>
                    <th>Vía</th>
                    <th>Frecuencia</th>
                    <th>Duración</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(item, i) in atencionGuardada.receta.items" :key="i">
                    <td class="cell-main">{{ item.medicamento }}</td>
                    <td>{{ item.concentracion || '—' }}</td>
                    <td>{{ item.dosis }}</td>
                    <td>{{ item.viaAdministracion }}</td>
                    <td>{{ item.frecuencia }}</td>
                    <td>{{ item.duracion }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </main>
      </div>

      <footer class="readonly-actions">
        <button class="btn btn-ghost" type="button" @click="verHistorialCompleto">
          <History :size="15" /> Ver historial completo
        </button>
        <button class="btn btn-secondary" type="button" @click="router.push('/veterinario/agenda')">
          <ChevronLeft :size="15" /> Volver a agenda
        </button>
      </footer>
    </template>

    <!-- ══════════════ ÉXITO ══════════════ -->
    <div v-else-if="guardadoExitoso" class="success-shell">
      <div class="success-body">
        <div class="success-icon">
          <CheckCircle2 :size="38" />
        </div>
        <h2 class="success-title">Atención guardada</h2>
        <p class="success-message">{{ guardadoExitoso.mensaje }}</p>

        <div class="success-summary">
          <div class="summary-row">
            <span class="summary-label">Paciente</span>
            <span class="summary-value">{{ guardadoExitoso.resumen?.mascota }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Diagnóstico</span>
            <span class="summary-value">{{ guardadoExitoso.resumen?.diagnostico }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Insumos aplicados</span>
            <span class="summary-value">{{ textoInsumosAplicados }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Total insumos</span>
            <span class="summary-value amount">{{ formatoUSD(guardadoExitoso.resumen?.totalInsumosUsd) }}</span>
          </div>
          <div class="summary-row">
            <span class="summary-label">Estado de la cita</span>
            <span class="summary-value">{{ guardadoExitoso.resumen?.estadoCita }}</span>
          </div>
          <div v-if="guardadoExitoso.codigoReceta" class="summary-row">
            <span class="summary-label">Código del récipe</span>
            <span class="summary-value mono">{{ guardadoExitoso.codigoReceta }}</span>
          </div>
          <div v-if="guardadoExitoso.idFacturaProductos" class="summary-row">
            <span class="summary-label">Factura de productos</span>
            <span class="summary-value">
              #{{ guardadoExitoso.idFacturaProductos }} ·
              {{ formatoUSD(guardadoExitoso.totalFacturaProductos) }}
              <em class="summary-note">(por cobrar en mostrador)</em>
            </span>
          </div>
        </div>

        <div class="success-actions">
          <button
            v-if="guardadoExitoso.idReceta"
            class="btn btn-primary"
            type="button"
            :disabled="descargandoReceta"
            @click="descargarReceta(guardadoExitoso.idReceta, guardadoExitoso.codigoReceta)"
          >
            <Loader2 v-if="descargandoReceta" :size="15" class="spin" />
            <Download v-else :size="15" />
            {{ descargandoReceta ? 'Descargando…' : 'Descargar récipe' }}
          </button>
          <button class="btn btn-secondary" type="button" @click="router.push('/veterinario/agenda')">
            <CalendarDays :size="15" /> Volver a agenda
          </button>
        </div>
      </div>
    </div>

    <!-- ══════════════ WIZARD ══════════════ -->
    <template v-else>
      <!-- Header compacto -->
      <header class="page-top">
        <div class="page-top-info">
          <span class="page-top-eyebrow">
            <Stethoscope :size="12" />
            Atención clínica
          </span>
          <h1 class="page-top-title">{{ mascota?.nombre }}</h1>
          <p class="page-top-sub">
            {{ cita?.servicio }} · {{ fechaCompleta(cita?.fechaCita) }} ·
            {{ rangoHora(cita?.horaInicio, cita?.horaFin) }}
          </p>
        </div>
        <div class="page-top-actions">
          <button class="quick-action" type="button" @click="modalCancelarAbierto = true">
            <Ban :size="14" /> Cancelar
          </button>
        </div>
      </header>

      <!-- Patient strip -->
      <div class="patient-strip">
        <PetAvatar :nombre-especie="mascota?.especie" size="lg" />
        <div class="patient-strip-info">
          <p class="patient-strip-name">{{ mascota?.nombre }}</p>
          <p class="patient-strip-meta">
            {{ mascota?.especie }} · {{ mascota?.raza }} · {{ dueno?.nombreCompleto }}
          </p>
        </div>
        <span class="status-pill" :style="estiloEstado(cita?.estado)">
          {{ infoEstado(cita?.estado).etiqueta }}
        </span>
      </div>

      <!-- Stepper -->
      <nav class="stepper" aria-label="Progreso">
        <template v-for="(p, idx) in pasos" :key="p.numero">
          <button
            type="button"
            class="step"
            :class="{
              'step-active': pasoActual === p.numero,
              'step-done': pasoActual > p.numero,
              'step-clickable': p.numero < pasoActual,
            }"
            :disabled="p.numero > pasoActual"
            @click="p.numero < pasoActual && (pasoActual = p.numero)"
          >
            <span class="step-num">
              <CheckCircle2 v-if="pasoActual > p.numero" :size="14" />
              <template v-else>{{ p.numero }}</template>
            </span>
            <span class="step-label">{{ p.titulo }}</span>
          </button>
          <div
            v-if="idx < pasos.length - 1"
            class="step-line"
            :class="{ 'step-line-filled': pasoActual > p.numero }"
          />
        </template>
      </nav>

      <!-- Contenido del wizard -->
      <div class="wizard">
        <!-- PASO 1 -->
        <div v-show="pasoActual === 1" class="paso">
          <div class="paso-layout">
            <div class="paso-main">
              <section class="form-section">
                <h3 class="section-title">
                  <ClipboardList :size="15" /> Anamnesis y hallazgos
                </h3>

                <div class="field">
                  <label class="field-label" for="anamnesis">
                    Anamnesis <span class="req">*</span>
                  </label>
                  <textarea
                    id="anamnesis"
                    v-model="form.anamnesis"
                    rows="3"
                    class="field-input field-textarea"
                    :class="{ 'field-error': errores.anamnesis }"
                    placeholder="Relato del dueño: evolución, síntomas, tiempo de enfermedad, apetito…"
                  />
                  <p v-if="errores.anamnesis" class="error-msg">{{ errores.anamnesis }}</p>
                </div>

                <div class="field-grid-2">
                  <div class="field">
                    <label class="field-label" for="hallazgos">Hallazgos físicos</label>
                    <textarea
                      id="hallazgos"
                      v-model="form.sintomasObservados"
                      rows="3"
                      class="field-input field-textarea"
                      placeholder="Ej: mucosas pálidas, abdomen tenso…"
                    />
                  </div>
                  <div class="field">
                    <label class="field-label" for="pronostico">Pronóstico / observaciones</label>
                    <textarea
                      id="pronostico"
                      v-model="form.observacionesGenerales"
                      rows="3"
                      class="field-input field-textarea"
                      placeholder="Ej: pronóstico reservado, se reevalúa en 7 días…"
                    />
                  </div>
                </div>
              </section>

              <section class="form-section">
                <h3 class="section-title">
                  <Activity :size="15" /> Signos vitales
                </h3>

                <div class="vitals-grid">
                  <div class="vital-field">
                    <label class="field-label" for="peso">Peso <small>kg</small> <span class="req">*</span></label>
                    <input
                      id="peso"
                      v-model.number="form.pesoKg"
                      type="number" step="0.01" min="0"
                      class="field-input"
                      :class="{ 'field-error': errores.pesoKg }"
                      placeholder="0.00"
                    />
                    <p v-if="errores.pesoKg" class="error-msg">{{ errores.pesoKg }}</p>
                  </div>
                  <div class="vital-field">
                    <label class="field-label" for="temperatura">Temperatura <small>°C</small> <span class="req">*</span></label>
                    <input
                      id="temperatura"
                      v-model.number="form.temperaturaC"
                      type="number" step="0.1" min="0"
                      class="field-input"
                      :class="{ 'field-error': errores.temperaturaC }"
                      placeholder="0.0"
                    />
                    <p v-if="errores.temperaturaC" class="error-msg">{{ errores.temperaturaC }}</p>
                  </div>
                  <div class="vital-field">
                    <label class="field-label" for="cardiaca">Frec. cardíaca <small>lpm</small> <span class="req">*</span></label>
                    <input
                      id="cardiaca"
                      v-model.number="form.frecCardiaca"
                      type="number" step="1" min="0"
                      class="field-input"
                      :class="{ 'field-error': errores.frecCardiaca }"
                      placeholder="0"
                    />
                    <p v-if="errores.frecCardiaca" class="error-msg">{{ errores.frecCardiaca }}</p>
                  </div>
                  <div class="vital-field">
                    <label class="field-label" for="respiratoria">Frec. respiratoria <small>rpm</small></label>
                    <input
                      id="respiratoria"
                      v-model.number="form.frecRespiratoria"
                      type="number" step="1" min="0"
                      class="field-input"
                      :class="{ 'field-error': errores.frecRespiratoria }"
                      placeholder="Opcional"
                    />
                    <p v-if="errores.frecRespiratoria" class="error-msg">{{ errores.frecRespiratoria }}</p>
                  </div>
                </div>
              </section>

              <section class="form-section">
                <h3 class="section-title">
                  <Stethoscope :size="15" /> Diagnóstico
                </h3>

                <div class="field">
                  <label class="field-label" for="diagnostico">
                    Diagnóstico principal <span class="req">*</span>
                  </label>
                  <textarea
                    id="diagnostico"
                    v-model="form.diagnosticoPrincipal"
                    rows="2"
                    class="field-input field-textarea"
                    :class="{ 'field-error': errores.diagnosticoPrincipal }"
                    placeholder="Diagnóstico presuntivo o definitivo…"
                  />
                  <p v-if="errores.diagnosticoPrincipal" class="error-msg">{{ errores.diagnosticoPrincipal }}</p>
                </div>

                <div class="field-grid-2">
                  <div class="field">
                    <label class="field-label" for="diferenciales">Diagnósticos diferenciales</label>
                    <textarea
                      id="diferenciales"
                      v-model="form.diagnosticosDiferenciales"
                      rows="2"
                      class="field-input field-textarea"
                      placeholder="Opcional…"
                    />
                  </div>
                  <div class="field">
                    <label class="field-label" for="proxima">Próxima cita recomendada</label>
                    <DatePicker
                        id="proxima"
                        v-model="form.proximaCitaRecomendada"
                        placeholder="Seleccionar fecha"
                        :min="hoyFecha"
                        :meses-ahead="24"
                    />
                  </div>
                </div>
              </section>
            </div>

            <aside class="side-panel">
              <section class="side-block">
                <h4><PawPrint :size="13" /> Paciente</h4>
                <p class="side-primary">{{ mascota?.nombre }}</p>
                <p class="side-line">{{ mascota?.especie }} · {{ mascota?.raza }}</p>
                <p class="side-line">{{ mascota?.sexo }} · {{ mascota?.edad }}</p>
                <p class="side-line">Peso actual: {{ mascota?.pesoActualKg }} kg</p>
                <span v-if="contexto?.primeraVez" class="side-badge">Primera consulta</span>
              </section>

              <section class="side-block">
                <h4><User :size="13" /> Dueño</h4>
                <p class="side-primary">{{ dueno?.nombreCompleto }}</p>
                <p class="side-line">{{ dueno?.documentoIdentidad }}</p>
                <p class="side-line"><Phone :size="11" /> {{ dueno?.telefonoPrincipal }}</p>
              </section>

              <section class="side-block">
                <h4><ClipboardList :size="13" /> Cita</h4>
                <p class="side-line"><strong>Motivo:</strong> {{ cita?.motivoConsulta }}</p>
                <p class="side-line"><strong>Servicio:</strong> {{ cita?.servicio }}</p>
                <p class="side-line"><strong>Tipo:</strong> {{ cita?.tipoAtencion }}</p>
              </section>

              <section v-if="ultimaAtencion" class="side-block">
                <h4><History :size="13" /> Última atención</h4>
                <p class="side-line"><strong>Fecha:</strong> {{ fechaHoraCorta(ultimaAtencion.fecha) }}</p>
                <p class="side-line"><strong>Dx:</strong> {{ ultimaAtencion.diagnostico }}</p>
                <p class="side-line">
                  {{ ultimaAtencion.pesoKg }} kg · {{ ultimaAtencion.temperaturaC }} °C ·
                  FC {{ ultimaAtencion.frecCardiaca }}
                </p>
              </section>
            </aside>
          </div>
        </div>

        <!-- PASO 2 -->
        <div v-show="pasoActual === 2" class="paso">
          <div class="paso-layout">
            <div class="paso-main">
              <div class="search-bar">
                <Search :size="16" class="search-icon" />
                <input
                  v-model="busqueda"
                  class="search-input"
                  placeholder="Buscar insumo por nombre, código o categoría…"
                />
                <button v-if="busqueda" class="search-clear" type="button" @click="busqueda = ''">
                  <X :size="14" />
                </button>
              </div>

              <div v-if="cargandoProductos" class="state-inline">
                <span class="spinner spinner-lg" />
              </div>

              <AppEmptyState
                v-else-if="!productos.length"
                :icon="Package"
                title="No fue posible cargar el catálogo"
                description="Verifica tu conexión e intenta nuevamente."
              >
                <template #action>
                  <button class="btn btn-ghost" type="button" @click="cargarProductos()">Reintentar</button>
                </template>
              </AppEmptyState>

              <AppEmptyState
                v-else-if="!productosFiltrados.length"
                :icon="Search"
                title="Sin resultados"
                :description="`No se encontraron insumos para «${busquedaAplicada}».`"
              />

              <ul v-else class="products-list">
                <li
                  v-for="p in productosFiltrados"
                  :key="p.id"
                  class="product-item"
                  :class="{ 'product-out': p.stockActual <= 0 }"
                >
                  <button
                    type="button"
                    class="product-btn"
                    :disabled="p.stockActual <= 0"
                    @click="agregarInsumo(p)"
                  >
                    <div class="product-info">
                      <p class="product-name">
                        {{ p.nombre }}
                        <span v-if="p.requiereReceta" class="pill-recipe">Receta</span>
                      </p>
                      <p class="product-meta">
                        {{ p.codigoSku }} · {{ p.tipoCategoria }} · {{ p.presentacion }}
                      </p>
                    </div>
                    <div class="product-side">
                      <span class="product-price">{{ formatoUSD(p.precioVenta) }}</span>
                      <span class="product-stock" :class="{ 'stock-zero': p.stockActual <= 0 }">
                        {{ p.stockActual > 0 ? `Stock ${p.stockActual}` : 'Agotado' }}
                      </span>
                    </div>
                    <span class="product-add" aria-hidden="true">
                      <Plus :size="14" />
                    </span>
                  </button>
                </li>
              </ul>

              <p class="hint-block">
                Los insumos se descuentan del almacén al guardar y generan una factura
                de productos (insumos + IVA 16%) por cobrar en mostrador.
              </p>
            </div>

            <aside class="side-panel">
              <h4 class="side-title">
                <Syringe :size="13" /> Insumos aplicados
                <span v-if="insumosSeleccionados.length" class="counter">
                  {{ insumosSeleccionados.length }}
                </span>
              </h4>

              <p v-if="errorStock" class="alert-inline alert-error">
                <AlertTriangle :size="14" /> {{ errorStock.mensaje }}
              </p>

              <div v-if="!insumosSeleccionados.length" class="side-empty">
                <Syringe :size="26" />
                <p>Aún no has aplicado insumos.</p>
              </div>

              <div v-else class="applied-list">
                <div
                  v-for="item in insumosSeleccionados"
                  :key="item.idProducto"
                  class="applied-item"
                  :class="{ 'applied-error': errorStock && errorStock.nombre === item.nombre }"
                >
                  <div class="applied-head">
                    <div class="applied-info">
                      <p class="applied-name">{{ item.nombre }}</p>
                      <p class="applied-meta">
                        {{ formatoUSD(item.precioUsd) }} / {{ item.unidadMedida }}
                        · Stock: {{ item.stockActual }}
                      </p>
                    </div>
                    <button class="icon-btn icon-danger" type="button" title="Quitar" @click="quitarInsumo(item)">
                      <Trash2 :size="13" />
                    </button>
                  </div>

                  <div class="applied-controls">
                    <div class="counter-group">
                      <button type="button" :disabled="item.cantidad <= 1" @click="cambiarCantidad(item, -1)">
                        <Minus :size="12" />
                      </button>
                      <input
                        v-model.number="item.cantidad"
                        type="number"
                        min="1"
                        :max="item.stockActual"
                        @change="normalizarCantidad(item)"
                      />
                      <button type="button" :disabled="item.cantidad >= item.stockActual" @click="cambiarCantidad(item, 1)">
                        <Plus :size="12" />
                      </button>
                    </div>
                    <span class="amount">{{ formatoUSD(item.precioUsd * item.cantidad) }}</span>
                  </div>

                  <p v-if="errorStock && errorStock.nombre === item.nombre" class="error-msg">
                    {{ errorStock.mensaje }}
                  </p>
                </div>

                <div class="applied-total">
                  <span>Total insumos</span>
                  <span class="amount">{{ formatoUSD(totalInsumos) }}</span>
                </div>
              </div>
            </aside>
          </div>
        </div>

        <!-- PASO 3 -->
        <div v-show="pasoActual === 3" class="paso">
          <div class="field">
            <label class="field-label" for="indicaciones">Indicaciones para el dueño</label>
            <textarea
              id="indicaciones"
              v-model="form.indicacionesDueno"
              rows="4"
              class="field-input field-textarea"
              placeholder="Recomendaciones, cuidados y próximos pasos para el dueño en casa…"
            />
          </div>

          <section class="recipe-block">
            <header class="recipe-head">
              <div>
                <h4>
                  <Pill :size="14" /> Récipe médico
                  <span class="pill-opt">opcional</span>
                </h4>
                <p class="recipe-sub">Genera el récipe de la consulta; se anexará al guardar.</p>
              </div>
              <div class="recipe-actions">
                <button v-if="!recetaGenerada" class="btn btn-primary" type="button" @click="abrirModalReceta">
                  <Plus :size="14" /> Generar récipe
                </button>
                <template v-else>
                  <button class="btn btn-ghost" type="button" @click="abrirModalReceta">Editar</button>
                  <button class="btn btn-ghost btn-danger-ghost" type="button" @click="descartarReceta">
                    <Trash2 :size="13" /> Descartar
                  </button>
                </template>
              </div>
            </header>

            <div v-if="recetaGenerada" class="recipe-preview">
              <p v-if="recetaIndicaciones" class="recipe-notes">
                <strong>Indicaciones generales:</strong> {{ recetaIndicaciones }}
              </p>
              <div class="table-wrap">
                <table class="mini-table">
                  <thead>
                    <tr>
                      <th>Medicamento</th>
                      <th>Concentración</th>
                      <th>Dosis</th>
                      <th>Vía</th>
                      <th>Frecuencia</th>
                      <th>Duración</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(item, i) in recetaItems" :key="i">
                      <td class="cell-main">{{ item.medicamento }}</td>
                      <td>{{ item.concentracion || '—' }}</td>
                      <td>{{ item.dosis }}</td>
                      <td>{{ item.viaAdministracion }}</td>
                      <td>{{ item.frecuencia }}</td>
                      <td>{{ item.duracion }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <p v-else class="recipe-empty">
              La consulta puede guardarse sin récipe. Si el tratamiento lo requiere, genéralo antes de continuar.
            </p>
          </section>
        </div>

        <!-- PASO 4 -->
        <div v-show="pasoActual === 4" class="paso">
          <div class="summary-grid">
            <section class="summary-card">
              <h4><PawPrint :size="14" /> Paciente y consulta</h4>
              <dl class="summary-dl">
                <div><dt>Paciente</dt><dd>{{ mascota?.nombre }} ({{ mascota?.especie }} · {{ mascota?.raza }})</dd></div>
                <div><dt>Dueño</dt><dd>{{ dueno?.nombreCompleto }}</dd></div>
                <div><dt>Motivo</dt><dd>{{ cita?.motivoConsulta }}</dd></div>
                <div><dt>Servicio</dt><dd>{{ cita?.servicio }}</dd></div>
              </dl>
            </section>

            <section class="summary-card">
              <h4><Activity :size="14" /> Signos vitales</h4>
              <dl class="summary-dl">
                <div><dt>Peso</dt><dd>{{ form.pesoKg }} kg</dd></div>
                <div><dt>Temperatura</dt><dd>{{ form.temperaturaC }} °C</dd></div>
                <div><dt>Frec. cardíaca</dt><dd>{{ form.frecCardiaca }} lpm</dd></div>
                <div v-if="form.frecRespiratoria !== null && form.frecRespiratoria !== ''">
                  <dt>Frec. respiratoria</dt><dd>{{ form.frecRespiratoria }} rpm</dd>
                </div>
              </dl>
            </section>

            <section class="summary-card summary-wide">
              <h4><ClipboardList :size="14" /> Diagnóstico y anamnesis</h4>
              <p class="summary-text"><strong>Diagnóstico:</strong> {{ form.diagnosticoPrincipal }}</p>
              <p class="summary-text"><strong>Anamnesis:</strong> {{ form.anamnesis }}</p>
              <p v-if="form.sintomasObservados" class="summary-text">
                <strong>Hallazgos:</strong> {{ form.sintomasObservados }}
              </p>
              <p v-if="form.diagnosticosDiferenciales" class="summary-text">
                <strong>Dx. diferenciales:</strong> {{ form.diagnosticosDiferenciales }}
              </p>
              <p v-if="form.observacionesGenerales" class="summary-text">
                <strong>Pronóstico:</strong> {{ form.observacionesGenerales }}
              </p>
              <p v-if="form.proximaCitaRecomendada" class="summary-text">
                <strong>Próxima cita:</strong> {{ form.proximaCitaRecomendada }}
              </p>
            </section>

            <section class="summary-card">
              <h4><Package :size="14" /> Insumos y factura estimada</h4>
              <table v-if="insumosSeleccionados.length" class="mini-table">
                <thead>
                  <tr>
                    <th>Insumo</th>
                    <th class="der">Cant.</th>
                    <th class="der">P. unit.</th>
                    <th class="der">Subtotal</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="i in insumosSeleccionados" :key="i.idProducto">
                    <td>{{ i.nombre }}</td>
                    <td class="der">{{ i.cantidad }} {{ i.unidadMedida }}</td>
                    <td class="der">{{ formatoUSD(i.precioUsd) }}</td>
                    <td class="der amount">{{ formatoUSD(i.precioUsd * i.cantidad) }}</td>
                  </tr>
                </tbody>
              </table>
              <p v-else class="summary-empty">Sin insumos aplicados.</p>

              <div class="summary-totals">
                <div class="totals-row"><span>Subtotal</span><span>{{ formatoUSD(totalInsumos) }}</span></div>
                <div class="totals-row"><span>IVA (16%)</span><span>{{ formatoUSD(ivaEstimado) }}</span></div>
                <div class="totals-row totals-final">
                  <span>Total estimado</span>
                  <span class="amount">{{ formatoUSD(totalFacturaEstimado) }}</span>
                </div>
              </div>
            </section>

            <section class="summary-card">
              <h4><Pill :size="14" /> Récipe</h4>
              <template v-if="recetaGenerada">
                <p v-if="recetaIndicaciones" class="summary-text">
                  <strong>Indicaciones:</strong> {{ recetaIndicaciones }}
                </p>
                <ul class="summary-list">
                  <li v-for="(item, i) in recetaItems" :key="i">
                    {{ item.medicamento }}{{ item.concentracion ? ` (${item.concentracion})` : '' }} —
                    {{ item.dosis }}, vía {{ item.viaAdministracion }}, {{ item.frecuencia }}, {{ item.duracion }}
                  </li>
                </ul>
              </template>
              <p v-else class="summary-empty">Sin récipe para esta consulta.</p>
            </section>

            <section class="summary-card summary-wide">
              <h4><Info :size="14" /> Indicaciones para el dueño</h4>
              <p class="summary-text">{{ form.indicacionesDueno || 'Sin indicaciones registradas.' }}</p>
            </section>
          </div>
        </div>

        <!-- Footer del wizard -->
        <footer class="wizard-footer">
          <button
            v-if="pasoActual > 1"
            class="btn btn-ghost"
            type="button"
            @click="retrocederPaso"
          >
            <ChevronLeft :size="15" /> Anterior
          </button>
          <span v-else />

          <div class="footer-right">
            <button
              v-if="pasoActual < 4"
              class="btn btn-primary"
              type="button"
              @click="avanzarPaso"
            >
              Siguiente <ChevronRight :size="15" />
            </button>
            <button
              v-else
              class="btn btn-primary btn-lg"
              type="button"
              :disabled="guardando"
              @click="guardarConsulta"
            >
              <Loader2 v-if="guardando" :size="15" class="spin" />
              <Save v-else :size="15" />
              {{ guardando ? 'Guardando…' : 'Guardar atención' }}
            </button>
          </div>
        </footer>
      </div>

      <!-- Modal récipe -->
      <Teleport to="body">
        <Transition name="fade">
          <div v-if="modalRecetaAbierto" class="modal-overlay" @click.self="cerrarModalReceta">
            <Transition name="slide-up" appear>
              <div v-if="modalRecetaAbierto" class="modal-recipe" role="dialog" aria-modal="true">
                <header class="modal-head">
                  <div>
                    <h3><Pill :size="16" /> Vista previa del récipe</h3>
                    <p class="modal-sub">
                      {{ mascota?.nombre }} · {{ fechaCompleta(cita?.fechaCita) }}
                    </p>
                  </div>
                  <button class="modal-close" type="button" @click="cerrarModalReceta">
                    <X :size="16" />
                  </button>
                </header>

                <div class="modal-body">
                  <div class="field">
                    <label class="field-label" for="receta-ind">Indicaciones generales del récipe</label>
                    <textarea
                      id="receta-ind"
                      v-model="recetaIndicacionesBorrador"
                      rows="2"
                      class="field-input field-textarea"
                      placeholder="Ej: administrar con los alimentos…"
                    />
                  </div>

                  <div v-for="(item, i) in recetaItemsBorrador" :key="i" class="recipe-item">
                    <header class="recipe-item-head">
                      <span class="recipe-item-num">Medicamento {{ i + 1 }}</span>
                      <button
                        class="icon-btn icon-danger"
                        type="button"
                        :disabled="recetaItemsBorrador.length <= 1"
                        @click="quitarItemReceta(i)"
                      >
                        <Trash2 :size="13" />
                      </button>
                    </header>

                    <div class="recipe-grid">
                      <div class="field" :class="{ 'field-has-error': erroresReceta[i]?.medicamento }">
                        <label class="field-label">Medicamento <span class="req">*</span></label>
                        <input
                          v-model="item.medicamento"
                          class="field-input"
                          list="productos-catalogo"
                          placeholder="Texto libre o del catálogo"
                        />
                        <p v-if="erroresReceta[i]?.medicamento" class="error-msg">
                          {{ erroresReceta[i].medicamento }}
                        </p>
                      </div>

                      <div class="field">
                        <label class="field-label">Concentración</label>
                        <input v-model="item.concentracion" class="field-input" placeholder="Ej: 500 mg" />
                      </div>

                      <div class="field" :class="{ 'field-has-error': erroresReceta[i]?.dosis }">
                        <label class="field-label">Dosis <span class="req">*</span></label>
                        <input v-model="item.dosis" class="field-input" placeholder="Ej: 1 tableta" />
                        <p v-if="erroresReceta[i]?.dosis" class="error-msg">{{ erroresReceta[i].dosis }}</p>
                      </div>

                      <div class="field">
                        <label class="field-label">Vía</label>
                        <select v-model="item.viaAdministracion" class="field-input field-select">
                          <option v-for="via in VIAS_ADMINISTRACION" :key="via" :value="via">{{ via }}</option>
                        </select>
                      </div>

                      <div class="field" :class="{ 'field-has-error': erroresReceta[i]?.frecuencia }">
                        <label class="field-label">Frecuencia <span class="req">*</span></label>
                        <input v-model="item.frecuencia" class="field-input" placeholder="Ej: cada 12 horas" />
                        <p v-if="erroresReceta[i]?.frecuencia" class="error-msg">
                          {{ erroresReceta[i].frecuencia }}
                        </p>
                      </div>

                      <div class="field" :class="{ 'field-has-error': erroresReceta[i]?.duracion }">
                        <label class="field-label">Duración <span class="req">*</span></label>
                        <input v-model="item.duracion" class="field-input" placeholder="Ej: 7 días" />
                        <p v-if="erroresReceta[i]?.duracion" class="error-msg">
                          {{ erroresReceta[i].duracion }}
                        </p>
                      </div>
                    </div>
                  </div>

                  <button class="btn btn-ghost btn-block" type="button" @click="agregarItemReceta">
                    <Plus :size="14" /> Agregar medicamento
                  </button>

                  <datalist id="productos-catalogo">
                    <option v-for="p in productos" :key="p.id" :value="p.nombre" />
                  </datalist>
                </div>

                <footer class="modal-foot">
                  <button class="btn btn-ghost" type="button" @click="cerrarModalReceta">Cancelar</button>
                  <button class="btn btn-primary" type="button" @click="confirmarReceta">
                    <CheckCircle2 :size="15" /> Confirmar récipe
                  </button>
                </footer>
              </div>
            </Transition>
          </div>
        </Transition>
      </Teleport>

      <!-- Modal cancelar -->
      <Teleport to="body">
        <Transition name="fade">
          <div v-if="modalCancelarAbierto" class="modal-overlay" @click.self="modalCancelarAbierto = false">
            <Transition name="slide-up" appear>
              <div v-if="modalCancelarAbierto" class="modal-sm" role="dialog" aria-modal="true">
                <header class="modal-head">
                  <h3><AlertTriangle :size="16" class="icon-warn" /> ¿Descartar la consulta?</h3>
                  <button class="modal-close" type="button" @click="modalCancelarAbierto = false">
                    <X :size="16" />
                  </button>
                </header>
                <div class="modal-body">
                  <p class="modal-text">Los datos no guardados se perderán.</p>
                  <p class="modal-note">La cita permanecerá En Atención y podrás retomarla desde tu agenda.</p>
                </div>
                <footer class="modal-foot">
                  <button class="btn btn-ghost" type="button" @click="modalCancelarAbierto = false">
                    Seguir editando
                  </button>
                  <button class="btn btn-danger" type="button" @click="confirmarCancelacion">
                    Descartar y volver
                  </button>
                </footer>
              </div>
            </Transition>
          </div>
        </Transition>
      </Teleport>
    </template>
  </div>
</template>

<script setup>
/* ═══════════════════════════════════════════════════════════════
   LÓGICA SIN CAMBIOS — toda la funcionalidad original se conserva
   ═══════════════════════════════════════════════════════════════ */
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Activity, AlertTriangle, Ban, CalendarDays, CheckCircle2, ChevronLeft, ChevronRight,
  ClipboardList, Download, Eye, History, Inbox, Info, Minus, Package, PawPrint, Phone,
  Pill, Plus, RefreshCw, Save, Search, Stethoscope, Syringe, Trash2, User, X, Loader2,
} from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import ToastContainer from '@/components/ui/ToastContainer.vue'
import PetAvatar from '@/components/ui/PetAvatar.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import { getAgendaVet, getCitaContexto, iniciarAtencion, guardarAtencion, descargarRecetaPdf } from '@/api/atenciones.api'
import { getProductos } from '@/api/almacen.api'
import { getApiErrorMessage, getValidationFieldErrors } from '@/utils/apiError'
import { descargarBlob } from '@/utils/descargas'
import { ESTADO_COLOR } from '@/utils/constants/estadosCita'
import { fechaCompleta, fechaHoraCorta, hoyISO, rangoHora } from '@/utils/fecha'
import DatePicker from '@/components/ui/DatePicker.vue'


const route = useRoute()
const router = useRouter()
const { toastExito, toastError, toastInfo } = useToast()
const hoyFecha = hoyISO()

const formatoUSD = (valor) => `$${Number(valor || 0).toFixed(2)}`

/* ═══════════════════════════════════════════════════════════════
   ESTADO GENERAL
   ═══════════════════════════════════════════════════════════════ */
const citaId = computed(() => route.params.citaId)
const cargando = ref(true)
const errorFatal = ref(null)
const contexto = ref(null)
const modoSoloLectura = ref(false)
const guardadoExitoso = ref(null)
const guardando = ref(false)
const descargandoReceta = ref(false)

const pasos = [
  { numero: 1, titulo: 'Evaluación clínica' },
  { numero: 2, titulo: 'Tratamiento e insumos' },
  { numero: 3, titulo: 'Récipe e indicaciones' },
  { numero: 4, titulo: 'Revisión y guardado' },
]
const pasoActual = ref(1)

/* ═══════════════════════════════════════════════════════════════
   PASO 1 — Form
   ═══════════════════════════════════════════════════════════════ */
const form = reactive({
  anamnesis: '',
  sintomasObservados: '',
  pesoKg: null,
  temperaturaC: null,
  frecCardiaca: null,
  frecRespiratoria: null,
  diagnosticoPrincipal: '',
  diagnosticosDiferenciales: '',
  observacionesGenerales: '',
  indicacionesDueno: '',
  proximaCitaRecomendada: '',
})
const errores = reactive({})

/* ═══════════════════════════════════════════════════════════════
   PASO 2 — Insumos
   ═══════════════════════════════════════════════════════════════ */
const productos = ref([])
const cargandoProductos = ref(false)
const busqueda = ref('')
const busquedaAplicada = ref('')
const insumosSeleccionados = ref([])
const errorStock = ref(null)
let temporizadorBusqueda = null

/* ═══════════════════════════════════════════════════════════════
   PASO 3 — Récipe
   ═══════════════════════════════════════════════════════════════ */
const VIAS_ADMINISTRACION = ['Oral', 'Intramuscular', 'Subcutánea', 'Intravenosa', 'Tópica', 'Ocular']
const modalRecetaAbierto = ref(false)
const recetaGenerada = ref(false)
const recetaItems = ref([])
const recetaIndicaciones = ref('')
const recetaItemsBorrador = ref([])
const recetaIndicacionesBorrador = ref('')
const erroresReceta = ref([])

/* ═══════════════════════════════════════════════════════════════
   Alterno A — Cancelar wizard
   ═══════════════════════════════════════════════════════════════ */
const modalCancelarAbierto = ref(false)

/* ═══════════════════════════════════════════════════════════════
   Selector de citas (cuando no hay citaId en la URL)
   ═══════════════════════════════════════════════════════════════ */
const citasPendientes = ref([])
const cargandoCitas = ref(false)

/* ═══════════════════════════════════════════════════════════════
   COMPUTED — Derivados del contexto
   ═══════════════════════════════════════════════════════════════ */
const cita = computed(() => contexto.value?.cita || null)
const mascota = computed(() => contexto.value?.mascota || null)
const dueno = computed(() => contexto.value?.dueno || null)
const ultimaAtencion = computed(() => contexto.value?.ultimaAtencion || null)
const atencionGuardada = computed(() => contexto.value?.atencionGuardada || null)

const productosFiltrados = computed(() => {
  const q = busquedaAplicada.value.trim().toLowerCase()
  if (!q) return productos.value
  return productos.value.filter((p) =>
    [p.nombre, p.codigoSku, p.tipoCategoria, p.presentacion]
      .filter(Boolean)
      .some((v) => String(v).toLowerCase().includes(q))
  )
})

const totalInsumos = computed(() =>
  insumosSeleccionados.value.reduce((s, i) => s + Number(i.precioUsd) * Number(i.cantidad), 0)
)
const ivaEstimado = computed(() => totalInsumos.value * 0.16)
const totalFacturaEstimado = computed(() => totalInsumos.value * 1.16)

const textoInsumosAplicados = computed(() => {
  const val = guardadoExitoso.value?.resumen?.insumosAplicados
  if (val == null) return 'Ninguno'
  if (Array.isArray(val)) return val.length ? val.join(', ') : 'Ninguno'
  return String(val)
})

const totalInsumosGuardados = computed(() =>
  (atencionGuardada.value?.insumos || []).reduce((s, i) => s + subtotalInsumo(i), 0)
)

/* ═══════════════════════════════════════════════════════════════
   HELPERS — Formato y estado
   ═══════════════════════════════════════════════════════════════ */
function subtotalInsumo(insumo) {
  return insumo.subtotal ?? insumo.subtotalUsd ?? (insumo.cantidad * insumo.precioUnitarioUsd)
}

function infoEstado(estado) {
  return {
    color: ESTADO_COLOR[estado] || 'var(--neutral-500)',
    etiqueta: String(estado || '').replaceAll('_', ' '),
  }
}

function estiloEstado(estado) {
  const { color } = infoEstado(estado)
  return { color, borderColor: color }
}

/* ═══════════════════════════════════════════════════════════════
   CICLO DE VIDA
   ═══════════════════════════════════════════════════════════════ */
function reiniciarFormulario() {
  pasoActual.value = 1
  guardadoExitoso.value = null
  modoSoloLectura.value = false
  errorFatal.value = null
  contexto.value = null
  errorStock.value = null
  Object.assign(form, {
    anamnesis: '',
    sintomasObservados: '',
    pesoKg: null,
    temperaturaC: null,
    frecCardiaca: null,
    frecRespiratoria: null,
    diagnosticoPrincipal: '',
    diagnosticosDiferenciales: '',
    observacionesGenerales: '',
    indicacionesDueno: '',
    proximaCitaRecomendada: '',
  })
  Object.keys(errores).forEach((k) => delete errores[k])
  insumosSeleccionados.value = []
  busqueda.value = ''
  busquedaAplicada.value = ''
  recetaGenerada.value = false
  recetaItems.value = []
  recetaIndicaciones.value = ''
}

async function inicializar() {
  reiniciarFormulario()
  if (!citaId.value) {
    cargando.value = false
    cargarCitasDelDia()
    return
  }
  cargando.value = true
  try {
    contexto.value = await getCitaContexto(citaId.value)
    if (contexto.value.atencionGuardada) {
      modoSoloLectura.value = true
      return
    }
    if (contexto.value.cita.estado === 'Confirmada') {
      try {
        await iniciarAtencion(citaId.value)
        contexto.value.cita.estado = 'En_Atencion'
      } catch (errorInicio) {
        errorFatal.value = getApiErrorMessage(errorInicio)
        return
      }
    } else if (contexto.value.cita.estado !== 'En_Atencion') {
      errorFatal.value = `La cita no puede atenderse en su estado actual (${contexto.value.cita.estado}).`
      return
    }
    form.pesoKg = contexto.value.mascota?.pesoActualKg ?? null
    cargarProductos(true)
  } catch (error) {
    errorFatal.value = getApiErrorMessage(error)
  } finally {
    cargando.value = false
  }
}

onMounted(inicializar)

watch(() => route.params.citaId, () => {
  if (route.path.startsWith('/veterinario/atencion')) inicializar()
})

/* ═══════════════════════════════════════════════════════════════
   SELECTOR — Cargar citas del día
   ═══════════════════════════════════════════════════════════════ */
async function cargarCitasDelDia() {
  cargandoCitas.value = true
  try {
    const agenda = await getAgendaVet(hoyISO())
    citasPendientes.value = agenda.filter((c) => !c.atendida)
  } catch (error) {
    toastError(getApiErrorMessage(error))
    citasPendientes.value = []
  } finally {
    cargandoCitas.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   CATÁLOGO DE PRODUCTOS
   ═══════════════════════════════════════════════════════════════ */
async function cargarProductos(silencioso = false) {
  cargandoProductos.value = true
  try {
    const { data } = await getProductos()
    productos.value = data || []
    insumosSeleccionados.value.forEach((item) => {
      const fresco = productos.value.find((p) => p.idProducto === item.idProducto)
      if (fresco) {
        item.stockActual = fresco.stockActual
        if (fresco.stockActual > 0 && item.cantidad > fresco.stockActual) {
          item.cantidad = fresco.stockActual
        }
      }
    })
  } catch (error) {
    if (!silencioso) toastError(getApiErrorMessage(error))
  } finally {
    cargandoProductos.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   BÚSQUEDA — debounce
   ═══════════════════════════════════════════════════════════════ */
watch(busqueda, (valor) => {
  clearTimeout(temporizadorBusqueda)
  temporizadorBusqueda = setTimeout(() => { busquedaAplicada.value = valor }, 350)
})

/* ═══════════════════════════════════════════════════════════════
   NAVEGACIÓN DEL WIZARD
   ═══════════════════════════════════════════════════════════════ */
function avanzarPaso() {
  if (pasoActual.value === 1 && !validarPaso1()) {
    toastError('Debe completar el diagnóstico y los signos vitales obligatorios')
    return
  }
  pasoActual.value = Math.min(4, pasoActual.value + 1)
}

function retrocederPaso() {
  pasoActual.value = Math.max(1, pasoActual.value - 1)
}

function validarPaso1() {
  Object.keys(errores).forEach((k) => delete errores[k])
  if (!form.anamnesis || !form.anamnesis.trim()) errores.anamnesis = 'La anamnesis es obligatoria'

  const peso = parseFloat(form.pesoKg)
  if (form.pesoKg === null || form.pesoKg === '' || Number.isNaN(peso)) {
    errores.pesoKg = 'Campo obligatorio'
  } else if (peso < 0.01 || peso > 999.99) {
    errores.pesoKg = 'Debe estar entre 0.01 y 999.99 kg'
  }

  const temp = parseFloat(form.temperaturaC)
  if (form.temperaturaC === null || form.temperaturaC === '' || Number.isNaN(temp)) {
    errores.temperaturaC = 'Campo obligatorio'
  } else if (temp < 30.0 || temp > 45.0) {
    errores.temperaturaC = 'Debe estar entre 30.0 y 45.0 °C'
  }

  const fc = parseFloat(form.frecCardiaca)
  if (form.frecCardiaca === null || form.frecCardiaca === '' || Number.isNaN(fc)) {
    errores.frecCardiaca = 'Campo obligatorio'
  } else if (fc < 20 || fc > 400) {
    errores.frecCardiaca = 'Debe estar entre 20 y 400 lpm'
  }

  if (form.frecRespiratoria !== null && form.frecRespiratoria !== '') {
    const fr = parseFloat(form.frecRespiratoria)
    if (Number.isNaN(fr) || fr < 1 || fr > 400) {
      errores.frecRespiratoria = 'Debe estar entre 1 y 400 rpm'
    }
  }

  if (!form.diagnosticoPrincipal || !form.diagnosticoPrincipal.trim()) {
    errores.diagnosticoPrincipal = 'El diagnóstico es obligatorio'
  }

  return Object.keys(errores).length === 0
}

/* ═══════════════════════════════════════════════════════════════
   INSUMOS
   ═══════════════════════════════════════════════════════════════ */
function agregarInsumo(producto) {
  if (producto.stockActual <= 0) {
    toastError('Stock insuficiente para este producto. Verifique con el almacén')
    return
  }
  const existente = insumosSeleccionados.value.find((i) => i.idProducto === producto.id)
  if (existente) {
    if (existente.cantidad < producto.stockActual) existente.cantidad += 1
    else toastInfo('Ya seleccionó todo el stock disponible de este producto')
  } else {
    insumosSeleccionados.value.push({
      idProducto: producto.id,
      nombre: producto.nombre,
      codigoSku: producto.codigoSku,
      unidadMedida: producto.unidadMedida,
       precioUsd: producto.precioVenta,
      cantidad: 1,
      stockActual: producto.stockActual,
      requiereReceta: producto.requiereReceta,
    })
  }
}

function quitarInsumo(item) {
  insumosSeleccionados.value = insumosSeleccionados.value.filter((i) => i.idProducto !== item.idProducto)
  if (errorStock.value && errorStock.value.nombre === item.nombre) errorStock.value = null
}

function cambiarCantidad(item, delta) {
  const nueva = Number(item.cantidad) + delta
  if (nueva < 1) return
  if (nueva > item.stockActual) {
    toastInfo('No hay más stock disponible de este producto')
    return
  }
  item.cantidad = nueva
}

function normalizarCantidad(item) {
  let c = Number(item.cantidad)
  if (!Number.isFinite(c) || c < 1) c = 1
  if (item.stockActual > 0 && c > item.stockActual) c = item.stockActual
  item.cantidad = c
}

/* ═══════════════════════════════════════════════════════════════
   RÉCIPE
   ═══════════════════════════════════════════════════════════════ */
function nuevoItemReceta() {
  return {
    medicamento: '',
    concentracion: '',
    dosis: '',
    viaAdministracion: 'Oral',
    frecuencia: '',
    duracion: '',
  }
}

function abrirModalReceta() {
  recetaItemsBorrador.value = recetaItems.value.length
    ? recetaItems.value.map((i) => ({ ...i }))
    : [nuevoItemReceta()]
  recetaIndicacionesBorrador.value = recetaIndicaciones.value
  erroresReceta.value = []
  modalRecetaAbierto.value = true
}

function cerrarModalReceta() {
  modalRecetaAbierto.value = false
}

function agregarItemReceta() {
  recetaItemsBorrador.value.push(nuevoItemReceta())
}

function quitarItemReceta(indice) {
  if (recetaItemsBorrador.value.length <= 1) return
  recetaItemsBorrador.value.splice(indice, 1)
}

function validarRecetaBorrador() {
  erroresReceta.value = recetaItemsBorrador.value.map((item) => {
    const err = {}
    if (!item.medicamento?.trim()) err.medicamento = 'Obligatorio'
    if (!item.dosis?.trim()) err.dosis = 'Obligatorio'
    if (!item.frecuencia?.trim()) err.frecuencia = 'Obligatorio'
    if (!item.duracion?.trim()) err.duracion = 'Obligatorio'
    return err
  })
  return erroresReceta.value.every((e) => Object.keys(e).length === 0)
}

function confirmarReceta() {
  if (!recetaItemsBorrador.value.length) {
    toastError('El récipe debe incluir al menos un medicamento')
    return
  }
  if (!validarRecetaBorrador()) {
    toastError('Complete los campos obligatorios (dosis, frecuencia y duración)')
    return
  }
  recetaItems.value = recetaItemsBorrador.value.map((i) => ({ ...i }))
  recetaIndicaciones.value = recetaIndicacionesBorrador.value
  recetaGenerada.value = true
  modalRecetaAbierto.value = false
  toastExito('Récipe generado. Se anexará a la atención al guardar.')
}

function descartarReceta() {
  recetaItems.value = []
  recetaIndicaciones.value = ''
  recetaGenerada.value = false
  toastInfo('Récipe descartado')
}

/* ═══════════════════════════════════════════════════════════════
   CANCELAR WIZARD
   ═══════════════════════════════════════════════════════════════ */
function confirmarCancelacion() {
  modalCancelarAbierto.value = false
  router.push('/veterinario/agenda')
}

/* ═══════════════════════════════════════════════════════════════
   GUARDAR CONSULTA
   ═══════════════════════════════════════════════════════════════ */
function construirPayload() {
  const payload = {
    idCita: Number(citaId.value),
    anamnesis: form.anamnesis.trim(),
    diagnosticoPrincipal: form.diagnosticoPrincipal.trim(),
    pesoKg: Number(form.pesoKg),
    temperaturaC: Number(form.temperaturaC),
    frecCardiaca: Number(form.frecCardiaca),
  }

  const opcionales = {
    sintomasObservados: form.sintomasObservados.trim(),
    frecRespiratoria: (form.frecRespiratoria === null || form.frecRespiratoria === '')
      ? null
      : Number(form.frecRespiratoria),
    diagnosticosDiferenciales: form.diagnosticosDiferenciales.trim(),
    observacionesGenerales: form.observacionesGenerales.trim(),
    indicacionesDueno: form.indicacionesDueno.trim(),
    proximaCitaRecomendada: form.proximaCitaRecomendada || null,
  }
  Object.entries(opcionales).forEach(([k, v]) => {
    if (v !== null && v !== '') payload[k] = v
  })

  if (insumosSeleccionados.value.length) {
    payload.insumos = insumosSeleccionados.value.map((i) => ({
      idProducto: i.idProducto,
      cantidad: i.cantidad,
    }))
  }

  if (recetaGenerada.value && recetaItems.value.length) {
    payload.receta = {
      items: recetaItems.value.map((i) => ({
        medicamento: i.medicamento.trim(),
        concentracion: i.concentracion?.trim() || undefined,
        dosis: i.dosis.trim(),
        viaAdministracion: i.viaAdministracion,
        frecuencia: i.frecuencia.trim(),
        duracion: i.duracion.trim(),
      })),
    }
    if (recetaIndicaciones.value.trim()) {
      payload.receta.indicacionesGenerales = recetaIndicaciones.value.trim()
    }
  }

  return payload
}

async function guardarConsulta() {
  if (guardando.value) return
  guardando.value = true
  try {
    const respuesta = await guardarAtencion(construirPayload())
    guardadoExitoso.value = respuesta
    toastExito(respuesta.mensaje || 'Atención registrada correctamente')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } catch (error) {
    const mensaje = getApiErrorMessage(error)
    const status = error?.response?.status

    if (status === 409 && mensaje.includes('Stock insuficiente')) {
      pasoActual.value = 2
      errorStock.value = {
        nombre: mensaje.match(/'([^']+)'/)?.[1] || null,
        mensaje,
      }
      await cargarProductos(true)
      toastError(mensaje)
    } else if (status === 409 && mensaje.includes('ya fue atendida')) {
      toastError(mensaje)
      await inicializar()
    } else if (status === 400) {
      const campos = getValidationFieldErrors(error)
      if (campos) {
        Object.entries(campos).forEach(([campo, msg]) => { errores[campo] = msg })
        const camposPaso1 = [
          'anamnesis', 'sintomasObservados', 'pesoKg', 'temperaturaC', 'frecCardiaca',
          'frecRespiratoria', 'diagnosticoPrincipal', 'diagnosticosDiferenciales',
          'observacionesGenerales', 'proximaCitaRecomendada',
        ]
        if (Object.keys(campos).some((c) => camposPaso1.includes(c))) pasoActual.value = 1
      }
      toastError(mensaje)
    } else {
      toastError(mensaje)
    }
  } finally {
    guardando.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   DESCARGAR RÉCIPE
   ═══════════════════════════════════════════════════════════════ */
async function descargarReceta(idReceta, codigoReceta) {
  if (!idReceta) return
  descargandoReceta.value = true
  try {
    const { blob, filename } = await descargarRecetaPdf(idReceta)
    descargarBlob(blob, filename || `receta-${codigoReceta || idReceta}.pdf`)
  } catch (error) {
    toastError(getApiErrorMessage(error))
  } finally {
    descargandoReceta.value = false
  }
}

/* ═══════════════════════════════════════════════════════════════
   VER HISTORIAL COMPLETO
   ═══════════════════════════════════════════════════════════════ */
function verHistorialCompleto() {
  router.push({
    path: '/veterinario/historiales',
    query: {
      mascota: mascota.value?.idMascota,
      nombre: mascota.value?.nombre,
    },
  })
}
</script>

<style scoped>
/* ═══════════════════════════════════════════════════════════════
   BASE
   ═══════════════════════════════════════════════════════════════ */
.atencion {
  max-width: 1280px;
  margin: 0 auto;
  padding: var(--space-8) var(--space-6) var(--space-12);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.spin { animation: spin 0.9s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ═══════════════════════════════════════════════════════════════
   ESTADOS FULL (loading / error fatal)
   ═══════════════════════════════════════════════════════════════ */
.state-full {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  padding: var(--space-16) var(--space-6);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  text-align: center;
  min-height: 320px;
  color: var(--text-secondary);
}
.state-full-error { min-height: 400px; }
.state-error-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--danger-50);
  color: var(--danger-600);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
}
.state-title {
  margin: 0;
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
}
.state-text {
  margin: 0 0 var(--space-3);
  font-size: var(--text-md);
  color: var(--text-secondary);
  max-width: 460px;
  line-height: var(--leading-relaxed);
}

.state-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-10) var(--space-6);
  color: var(--text-secondary);
}

/* ═══════════════════════════════════════════════════════════════
   HEADER COMPACTO (page-top)
   ═══════════════════════════════════════════════════════════════ */
.page-top {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-6);
  flex-wrap: wrap;
  padding-bottom: var(--space-5);
  border-bottom: 1px solid var(--border-subtle);
}
.page-top-info { min-width: 0; }
.page-top-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin: 0 0 var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.07em;
  color: var(--brand-700);
}
.page-top-title {
  margin: 0 0 var(--space-1);
  font-size: var(--text-5xl);
  font-weight: var(--font-bold);
  letter-spacing: var(--tracking-tight);
  line-height: 1.1;
  color: var(--text-primary);
}
.page-top-sub {
  margin: 0;
  font-size: var(--text-md);
  color: var(--text-secondary);
}
.page-top-actions { display: flex; gap: var(--space-2); align-items: center; }
.quick-action {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--neutral-700);
  border-radius: var(--radius-lg);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  font-family: inherit;
  cursor: pointer;
  transition: all var(--duration-fast) var(--ease-out);
}
.quick-action:hover {
  border-color: var(--brand-200);
  background: var(--brand-50);
  color: var(--brand-700);
}

/* ═══════════════════════════════════════════════════════════════
   PATIENT STRIP (wizard)
   ═══════════════════════════════════════════════════════════════ */
.patient-strip {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: linear-gradient(135deg, var(--brand-50) 0%, var(--bg-surface) 70%);
  border: 1px solid var(--brand-100);
  border-radius: var(--radius-xl);
  flex-wrap: wrap;
}
.patient-strip-info { min-width: 0; flex: 1; }
.patient-strip-name {
  margin: 0 0 2px;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
.patient-strip-meta {
  margin: 0;
  font-size: var(--text-md);
  color: var(--text-secondary);
}

/* ═══════════════════════════════════════════════════════════════
   STEPPER
   ═══════════════════════════════════════════════════════════════ */
.stepper {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow-x: auto;
  scrollbar-width: none;
}
.stepper::-webkit-scrollbar { display: none; }

.step {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border: none;
  background: transparent;
  border-radius: var(--radius-lg);
  font-family: inherit;
  cursor: pointer;
  transition: background-color var(--duration-fast) var(--ease-out);
  flex-shrink: 0;
}
.step:disabled { cursor: default; }
.step-clickable:hover { background: var(--brand-50); }

.step-num {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--text-tertiary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  flex-shrink: 0;
  transition: all var(--duration-base) var(--ease-out);
}
.step-label {
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--text-tertiary);
  white-space: nowrap;
  transition: color var(--duration-base);
}
.step-active .step-num {
  border-color: var(--brand-700);
  color: var(--brand-700);
  box-shadow: 0 0 0 4px var(--brand-100);
}
.step-active .step-label { color: var(--brand-700); }
.step-done .step-num {
  border-color: var(--brand-700);
  background: var(--brand-700);
  color: var(--text-inverse);
}
.step-done .step-label { color: var(--neutral-700); }

.step-line {
  flex: 1;
  min-width: 16px;
  height: 2px;
  background: var(--border-subtle);
  border-radius: 1px;
  transition: background var(--duration-base);
}
.step-line-filled { background: var(--brand-700); }

/* ═══════════════════════════════════════════════════════════════
   WIZARD CARD
   ═══════════════════════════════════════════════════════════════ */
.wizard {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.paso { padding: var(--space-6) var(--space-6) var(--space-8); }
.paso-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: var(--space-6);
  align-items: start;
}
.paso-main {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  min-width: 0;
}

/* ═══════════════════════════════════════════════════════════════
   FORM SECTIONS
   ═══════════════════════════════════════════════════════════════ */
.form-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.section-title {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0;
  padding-bottom: var(--space-2);
  border-bottom: 1px solid var(--border-subtle);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--brand-700);
}
.section-title svg { color: var(--brand-700); }

/* ═══════════════════════════════════════════════════════════════
   FIELDS
   ═══════════════════════════════════════════════════════════════ */
.field { display: flex; flex-direction: column; gap: var(--space-2); min-width: 0; }
.field-label {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
  color: var(--neutral-700);
  line-height: 1.3;
}
.field-label small { font-size: var(--text-xs); color: var(--text-tertiary); font-weight: var(--font-medium); }
.req { color: var(--danger-500); margin-left: 2px; }

.field-input {
  width: 100%;
  padding: var(--space-3) var(--space-4);
  border: 1px solid var(--border-strong);
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  color: var(--text-primary);
  background: var(--bg-surface);
  font-family: inherit;
  box-sizing: border-box;
  line-height: var(--leading-normal);
  transition: all var(--duration-fast) var(--ease-out);
}
.field-input::placeholder { color: var(--text-tertiary); }
.field-input:hover { border-color: var(--neutral-400); }
.field-input:focus {
  outline: none;
  border-color: var(--brand-700);
  box-shadow: 0 0 0 4px var(--brand-100);
}
.field-input:disabled { background: var(--bg-surface-alt); color: var(--text-tertiary); cursor: not-allowed; }

.field-textarea {
  resize: vertical;
  min-height: 72px;
  line-height: var(--leading-relaxed);
}
.field-select {
  appearance: none;
  -webkit-appearance: none;
  cursor: pointer;
  padding-right: var(--space-10);
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right var(--space-4) center;
}

.field-error {
  border-color: var(--danger-500) !important;
  background: var(--danger-50);
}
.field-error:focus {
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.12) !important;
}
.error-msg {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--danger-600);
  font-weight: var(--font-semibold);
  line-height: var(--leading-snug);
}
.field-has-error .field-input { border-color: var(--danger-500); background: var(--danger-50); }

.field-grid-2 {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-4);
}

/* ═══════════════════════════════════════════════════════════════
   VITALS GRID
   ═══════════════════════════════════════════════════════════════ */
.vitals-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: var(--space-3);
}
.vital-field { display: flex; flex-direction: column; gap: var(--space-2); min-width: 0; }

/* ═══════════════════════════════════════════════════════════════
   SEARCH BAR (insumos)
   ═══════════════════════════════════════════════════════════════ */
.search-bar {
  position: relative;
  margin-bottom: var(--space-4);
}
.search-icon {
  position: absolute;
  left: var(--space-4);
  top: 50%;
  transform: translateY(-50%);
  color: var(--text-tertiary);
  pointer-events: none;
}
.search-input {
  width: 100%;
  padding: var(--space-3) var(--space-10) var(--space-3) 42px;
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  border-radius: var(--radius-xl);
  font-size: var(--text-md);
  font-family: inherit;
  color: var(--text-primary);
  outline: none;
  box-sizing: border-box;
  transition: all var(--duration-fast) var(--ease-out);
}
.search-input::placeholder { color: var(--text-tertiary); }
.search-input:focus { border-color: var(--brand-700); box-shadow: 0 0 0 4px var(--brand-100); }
.search-clear {
  position: absolute;
  right: var(--space-3);
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
}
.search-clear:hover { color: var(--neutral-600); background: var(--neutral-100); }

/* ═══════════════════════════════════════════════════════════════
   PRODUCTS LIST
   ═══════════════════════════════════════════════════════════════ */
.products-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  max-height: 480px;
  overflow-y: auto;
  padding-right: var(--space-1);
}
.products-list::-webkit-scrollbar { width: 6px; }
.products-list::-webkit-scrollbar-thumb { background: var(--neutral-300); border-radius: 3px; }

.product-item { margin: 0; }
.product-btn {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: var(--space-3);
  align-items: center;
  width: 100%;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: all var(--duration-fast) var(--ease-out);
}
.product-btn:hover:not(:disabled) {
  border-color: var(--brand-200);
  background: var(--brand-50);
  transform: translateY(-1px);
}
.product-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  background: var(--bg-surface-alt);
}
.product-out .product-btn:hover { transform: none; }

.product-info { min-width: 0; }
.product-name {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 3px;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  flex-wrap: wrap;
}
.product-meta {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.pill-recipe {
  display: inline-flex;
  padding: 1px var(--space-2);
  border-radius: var(--radius-full);
  background: var(--warning-50);
  color: var(--warning-700);
  border: 1px solid var(--warning-200);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.product-side { display: flex; flex-direction: column; gap: 2px; align-items: flex-end; text-align: right; flex-shrink: 0; }
.product-price { font-size: var(--text-base); font-weight: var(--font-bold); color: var(--brand-700); font-variant-numeric: tabular-nums; }
.product-stock { font-size: var(--text-xs); font-weight: var(--font-semibold); color: var(--text-tertiary); }
.stock-zero { color: var(--danger-600); }

.product-add {
  width: 30px;
  height: 30px;
  border-radius: var(--radius-md);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  color: var(--brand-700);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all var(--duration-fast);
}
.product-btn:hover:not(:disabled) .product-add {
  background: var(--brand-700);
  color: var(--text-inverse);
  border-color: var(--brand-700);
}

.hint-block {
  margin: var(--space-4) 0 0;
  padding: var(--space-3) var(--space-4);
  background: var(--brand-50);
  border-left: 3px solid var(--brand-700);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  color: var(--brand-700);
  line-height: var(--leading-normal);
  font-weight: var(--font-medium);
}

/* ═══════════════════════════════════════════════════════════════
   SIDE PANEL
   ═══════════════════════════════════════════════════════════════ */
.side-panel {
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  position: sticky;
  top: var(--space-6);
}
.side-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--brand-700);
}
.side-title .counter {
  margin-left: auto;
  background: var(--brand-700);
  color: var(--text-inverse);
  border-radius: var(--radius-full);
  font-size: var(--text-2xs);
  min-width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 var(--space-2);
}

.side-block {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: var(--space-3) var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}
.side-block h4 {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-1);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--brand-700);
}
.side-primary {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}
.side-line {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-snug);
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex-wrap: wrap;
}
.side-line strong { color: var(--neutral-700); font-weight: var(--font-semibold); }
.side-badge {
  display: inline-flex;
  align-self: flex-start;
  margin-top: var(--space-1);
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  background: var(--success-50);
  color: var(--success-700);
  border: 1px solid var(--success-200);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
}

.side-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-6) var(--space-3);
  color: var(--text-tertiary);
  text-align: center;
  font-size: var(--text-sm);
}
.side-empty p { margin: 0; }

/* ═══════════════════════════════════════════════════════════════
   APPLIED INSUMOS
   ═══════════════════════════════════════════════════════════════ */
.applied-list { display: flex; flex-direction: column; gap: var(--space-3); }
.applied-item {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: var(--space-3);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  transition: border-color var(--duration-fast);
}
.applied-error { border-color: var(--danger-500); box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.12); }

.applied-head { display: flex; align-items: flex-start; justify-content: space-between; gap: var(--space-2); }
.applied-info { min-width: 0; }
.applied-name {
  margin: 0 0 2px;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.3;
}
.applied-meta { margin: 0; font-size: var(--text-2xs); color: var(--text-secondary); }

.applied-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
}
.counter-group {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-surface);
}
.counter-group button {
  width: 26px;
  height: 28px;
  border: none;
  background: var(--bg-surface-alt);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--duration-fast);
}
.counter-group button:hover:not(:disabled) { background: var(--brand-700); color: var(--text-inverse); }
.counter-group button:disabled { color: var(--neutral-300); cursor: not-allowed; }
.counter-group input {
  width: 40px;
  height: 28px;
  border: none;
  border-left: 1px solid var(--border-subtle);
  border-right: 1px solid var(--border-subtle);
  text-align: center;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  font-family: inherit;
  background: var(--bg-surface);
  box-sizing: border-box;
}
.counter-group input:focus { outline: none; }
.counter-group input::-webkit-outer-spin-button,
.counter-group input::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }

.applied-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--space-3);
  margin-top: var(--space-1);
  border-top: 1px dashed var(--border-subtle);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--neutral-700);
}

.alert-inline {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
  padding: var(--space-3);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  line-height: 1.4;
}
.alert-error { background: var(--danger-50); color: var(--danger-700); border: 1px solid var(--danger-200); }

/* ═══════════════════════════════════════════════════════════════
   ICON BUTTONS
   ═══════════════════════════════════════════════════════════════ */
.icon-btn {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--duration-fast);
  flex-shrink: 0;
}
.icon-btn:hover:not(:disabled) { border-color: var(--border-strong); color: var(--neutral-700); }
.icon-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.icon-danger:hover:not(:disabled) { border-color: var(--danger-200); color: var(--danger-600); background: var(--danger-50); }

/* ═══════════════════════════════════════════════════════════════
   RÉCIPE (paso 3)
   ═══════════════════════════════════════════════════════════════ */
.recipe-block {
  margin-top: var(--space-5);
  padding: var(--space-5);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
}
.recipe-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  flex-wrap: wrap;
  margin-bottom: var(--space-4);
}
.recipe-head h4 {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-1);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}
.recipe-sub { margin: 0; font-size: var(--text-sm); color: var(--text-secondary); }
.pill-opt {
  display: inline-flex;
  padding: 2px var(--space-3);
  margin-left: var(--space-2);
  border-radius: var(--radius-full);
  background: var(--neutral-100);
  color: var(--text-secondary);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.recipe-actions { display: flex; gap: var(--space-2); flex-wrap: wrap; }
.recipe-preview {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  overflow: hidden;
}
.recipe-notes {
  margin: 0;
  padding: var(--space-3) var(--space-4);
  background: var(--warning-50);
  border-bottom: 1px solid var(--warning-200);
  font-size: var(--text-md);
  color: var(--warning-700);
  line-height: var(--leading-relaxed);
}
.recipe-notes strong { font-weight: var(--font-bold); }
.recipe-empty {
  margin: 0;
  padding: var(--space-6);
  text-align: center;
  font-size: var(--text-md);
  color: var(--text-tertiary);
  background: var(--bg-surface);
  border: 1px dashed var(--border-subtle);
  border-radius: var(--radius-lg);
  line-height: var(--leading-relaxed);
}

/* ═══════════════════════════════════════════════════════════════
   SUMMARY GRID (paso 4)
   ═══════════════════════════════════════════════════════════════ */
.summary-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
}
.summary-card {
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.summary-wide { grid-column: 1 / -1; }
.summary-card h4 {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--brand-700);
}
.summary-dl { margin: 0; display: flex; flex-direction: column; gap: var(--space-2); }
.summary-dl > div {
  display: grid;
  grid-template-columns: 110px 1fr;
  gap: var(--space-3);
  font-size: var(--text-md);
}
.summary-dl dt { color: var(--text-secondary); font-weight: var(--font-semibold); }
.summary-dl dd { margin: 0; color: var(--text-primary); font-weight: var(--font-semibold); }
.summary-text { margin: 0; font-size: var(--text-md); color: var(--neutral-700); line-height: var(--leading-relaxed); }
.summary-text strong { color: var(--text-primary); font-weight: var(--font-bold); }
.summary-list {
  margin: 0;
  padding-left: var(--space-5);
  font-size: var(--text-md);
  color: var(--neutral-700);
  line-height: var(--leading-relaxed);
}
.summary-list li { margin-bottom: var(--space-1); }
.summary-empty { margin: 0; font-size: var(--text-md); color: var(--text-tertiary); font-style: italic; }
.summary-totals {
  margin-top: var(--space-2);
  padding-top: var(--space-3);
  border-top: 1px dashed var(--border-subtle);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}
.totals-row {
  display: flex;
  justify-content: space-between;
  font-size: var(--text-md);
  color: var(--neutral-700);
}
.totals-final {
  padding-top: var(--space-2);
  margin-top: var(--space-1);
  border-top: 1px solid var(--border-subtle);
  font-weight: var(--font-bold);
  font-size: var(--text-lg);
  color: var(--text-primary);
}

/* ═══════════════════════════════════════════════════════════════
   WIZARD FOOTER
   ═══════════════════════════════════════════════════════════════ */
.wizard-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--border-subtle);
  background: var(--bg-surface-alt);
  flex-wrap: wrap;
}
.footer-right { display: flex; gap: var(--space-2); margin-left: auto; }

/* ═══════════════════════════════════════════════════════════════
   BUTTONS
   ═══════════════════════════════════════════════════════════════ */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-5);
  border-radius: var(--radius-lg);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all var(--duration-fast) var(--ease-out);
  white-space: nowrap;
  line-height: 1;
  height: 40px;
}
.btn:disabled { opacity: 0.55; cursor: not-allowed; }
.btn-primary { background: var(--brand-700); color: var(--text-inverse); border-color: var(--brand-700); }
.btn-primary:hover:not(:disabled) {
  background: var(--brand-800);
  border-color: var(--brand-800);
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15, 118, 110, 0.4);
}
.btn-secondary { background: var(--bg-surface); color: var(--neutral-600); border-color: var(--border-subtle); }
.btn-secondary:hover:not(:disabled) { background: var(--bg-surface-alt); border-color: var(--border-strong); color: var(--brand-700); }
.btn-ghost { background: var(--bg-surface); color: var(--brand-700); border-color: var(--brand-200); }
.btn-ghost:hover:not(:disabled) { background: var(--brand-50); border-color: var(--brand-700); }
.btn-danger { background: var(--danger-600); color: var(--text-inverse); border-color: var(--danger-600); }
.btn-danger:hover:not(:disabled) { background: var(--danger-700); border-color: var(--danger-700); }
.btn-danger-ghost { color: var(--danger-600); border-color: var(--danger-200); }
.btn-danger-ghost:hover:not(:disabled) { background: var(--danger-50); border-color: var(--danger-500); }
.btn-lg { padding: var(--space-4) var(--space-7); font-size: var(--text-base); height: 46px; }
.btn-block { width: 100%; }

/* ═══════════════════════════════════════════════════════════════
   TABLES
   ═══════════════════════════════════════════════════════════════ */
.table-wrap { overflow-x: auto; border-radius: var(--radius-md); }
.mini-table { width: 100%; border-collapse: collapse; min-width: 420px; }
.mini-table th {
  text-align: left;
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
  border-bottom: 2px solid var(--border-subtle);
  background: transparent;
}
.mini-table td {
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-md);
  color: var(--text-primary);
  border-bottom: 1px solid var(--neutral-100);
  vertical-align: middle;
}
.mini-table tbody tr:last-child td { border-bottom: none; }
.mini-table tbody tr:hover td { background: rgba(15, 118, 110, 0.04); }
.der { text-align: right; }
.amount { font-weight: var(--font-bold); color: var(--brand-700); }
.cell-main { font-weight: var(--font-semibold); color: var(--text-primary); display: block; }
.cell-sub {
  display: block;
  font-size: var(--text-2xs);
  color: var(--text-tertiary);
  font-family: var(--font-mono);
  margin-top: 1px;
}

/* ═══════════════════════════════════════════════════════════════
   MODALES
   ═══════════════════════════════════════════════════════════════ */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: var(--bg-overlay);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
  z-index: var(--z-modal);
  font-family: var(--font-sans);
}
.modal-recipe,
.modal-sm {
  background: var(--bg-surface);
  border-radius: var(--radius-3xl);
  width: 100%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: var(--shadow-2xl);
}
.modal-recipe { max-width: 820px; }
.modal-sm { max-width: 460px; }
.modal-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-subtle);
}
.modal-head h3 {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-1);
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}
.modal-sub { margin: 0; font-size: var(--text-sm); color: var(--text-secondary); }
.modal-close {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: var(--bg-surface);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--duration-fast);
  flex-shrink: 0;
}
.modal-close:hover { background: var(--neutral-100); color: var(--text-primary); }
.modal-body {
  padding: var(--space-5) var(--space-6);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}
.modal-foot {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--border-subtle);
  background: var(--bg-surface-alt);
  flex-wrap: wrap;
}
.modal-text { margin: 0; font-size: var(--text-md); color: var(--text-primary); font-weight: var(--font-semibold); }
.modal-note { margin: var(--space-2) 0 0; font-size: var(--text-sm); color: var(--text-secondary); line-height: var(--leading-normal); }
.icon-warn { color: var(--warning-600); }

/* Recipe items in modal */
.recipe-item {
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.recipe-item-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
}
.recipe-item-num {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--brand-700);
}
.recipe-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-3);
}

/* ═══════════════════════════════════════════════════════════════
   READONLY MODE
   ═══════════════════════════════════════════════════════════════ */
.readonly-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: var(--neutral-100);
  color: var(--neutral-600);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
}
.readonly-banner {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-5);
  background: var(--warning-50);
  color: var(--warning-700);
  border: 1px solid var(--warning-200);
  border-radius: var(--radius-xl);
  font-size: var(--text-md);
  font-weight: var(--font-semibold);
}
.readonly-grid {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: var(--space-5);
  align-items: start;
}
.readonly-content {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  padding: var(--space-5) var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  box-shadow: var(--shadow-xs);
}
.readonly-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-subtle);
}
.readonly-vet {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0;
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--brand-700);
}
.readonly-actions {
  display: flex;
  gap: var(--space-3);
  justify-content: flex-end;
  flex-wrap: wrap;
}

/* ═══════════════════════════════════════════════════════════════
   VITALS & CLINICAL BLOCKS (readonly + paso 4)
   ═══════════════════════════════════════════════════════════════ */
.vitals-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: var(--space-3);
}
.vital {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}
.vital-label {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-tertiary);
}
.vital-value {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
  line-height: 1.1;
}
.vital-value small { font-size: var(--text-sm); font-weight: var(--font-semibold); color: var(--text-secondary); margin-left: 3px; }

.clinical-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-3);
}
.clinical-block {
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}
.clinical-block-highlight {
  background: var(--brand-50);
  border-color: var(--brand-200);
}
.clinical-block h5 {
  margin: 0 0 var(--space-2);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}
.clinical-block-highlight h5 { color: var(--brand-700); }
.clinical-block p {
  margin: 0;
  font-size: var(--text-md);
  color: var(--neutral-700);
  line-height: var(--leading-relaxed);
  white-space: pre-wrap;
}

.inner-block {
  margin-top: var(--space-3);
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
}
.inner-block-receta {
  background: var(--warning-50);
  border-color: var(--warning-200);
}
.inner-block-title {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--text-secondary);
}
.inner-block-receta .inner-block-title { color: var(--warning-700); }
.inner-block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  flex-wrap: wrap;
  margin-bottom: var(--space-3);
}
.inner-block-head .inner-block-title { margin: 0; }
.inner-block-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: var(--space-3) 0 0;
  padding-top: var(--space-3);
  border-top: 1px dashed var(--neutral-300);
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--neutral-700);
}
.btn-pdf {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-3);
  background: var(--bg-surface);
  border: 1px solid var(--warning-200);
  border-radius: var(--radius-md);
  color: var(--warning-700);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
  transition: all var(--duration-fast);
}
.btn-pdf:hover:not(:disabled) { background: var(--warning-100); border-color: var(--warning-500); }
.btn-pdf:disabled { opacity: 0.55; cursor: not-allowed; }

/* ═══════════════════════════════════════════════════════════════
   SUCCESS STATE
   ═══════════════════════════════════════════════════════════════ */
.success-shell {
  max-width: 720px;
  margin: 0 auto;
  width: 100%;
}
.success-body {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-3xl);
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
.success-title {
  margin: 0;
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
}
.success-message {
  margin: 0 0 var(--space-3);
  font-size: var(--text-md);
  color: var(--text-secondary);
  max-width: 520px;
  line-height: var(--leading-relaxed);
}
.success-summary {
  width: 100%;
  max-width: 620px;
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  text-align: left;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  gap: var(--space-4);
  font-size: var(--text-md);
  flex-wrap: wrap;
}
.summary-label { color: var(--text-secondary); font-weight: var(--font-semibold); }
.summary-value { color: var(--text-primary); font-weight: var(--font-bold); text-align: right; }
.summary-value.mono { font-family: var(--font-mono); font-size: var(--text-sm); background: var(--neutral-100); padding: 2px var(--space-2); border-radius: var(--radius-sm); }
.summary-note { font-size: var(--text-xs); color: var(--text-tertiary); font-weight: var(--font-medium); font-style: italic; }
.success-actions {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-4);
  flex-wrap: wrap;
  justify-content: center;
}

/* ═══════════════════════════════════════════════════════════════
   SELECTOR (citas pendientes)
   ═══════════════════════════════════════════════════════════════ */
.panel {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--border-subtle);
  flex-wrap: wrap;
}
.panel-title { margin: 0; font-size: var(--text-lg); font-weight: var(--font-bold); color: var(--text-primary); }
.panel-sub { margin: 3px 0 0; font-size: var(--text-sm); color: var(--text-secondary); }
.panel-body { padding: var(--space-4) var(--space-5) var(--space-5); }

.selector-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: var(--space-2); }
.selector-item {
  display: grid;
  grid-template-columns: 100px 1fr auto auto;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  transition: all var(--duration-fast);
}
.selector-item:hover { border-color: var(--brand-200); background: var(--bg-surface); }
.selector-time {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.selector-info { min-width: 0; }
.selector-pet {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0 0 2px;
  font-size: var(--text-base);
  color: var(--text-primary);
  flex-wrap: wrap;
}
.selector-pet svg { color: var(--brand-700); }
.selector-pet strong { font-weight: var(--font-bold); }
.selector-species { font-size: var(--text-sm); color: var(--text-secondary); font-weight: var(--font-medium); }
.selector-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
.btn-atender-sm {
  padding: var(--space-2) var(--space-4);
  background: var(--brand-700);
  color: var(--text-inverse);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  font-family: inherit;
  cursor: pointer;
  transition: all var(--duration-fast);
}
.btn-atender-sm:hover { background: var(--brand-800); }

/* ═══════════════════════════════════════════════════════════════
   STATUS PILL
   ═══════════════════════════════════════════════════════════════ */
.status-pill {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
  border: 1px solid;
  white-space: nowrap;
  background: transparent;
}

/* ═══════════════════════════════════════════════════════════════
   TRANSICIONES
   ═══════════════════════════════════════════════════════════════ */
.fade-enter-active, .fade-leave-active { transition: opacity var(--duration-base) var(--ease-out); }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.slide-up-enter-active, .slide-up-leave-active {
  transition: all var(--duration-slow) var(--ease-out);
}
.slide-up-enter-from, .slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}

/* ═══════════════════════════════════════════════════════════════
   UTIL
   ═══════════════════════════════════════════════════════════════ */
.mt-2 { margin-top: var(--space-2); }
.mt-4 { margin-top: var(--space-4); }
.mb-2 { margin-bottom: var(--space-2); }
.mb-3 { margin-bottom: var(--space-3); }

/* ═══════════════════════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════════════════════ */
@media (max-width: 1024px) {
  .paso-layout { grid-template-columns: 1fr; }
  .side-panel { position: static; }
  .readonly-grid { grid-template-columns: 1fr; }
  .vitals-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .summary-grid { grid-template-columns: 1fr; }
  .summary-wide { grid-column: 1; }
}

@media (max-width: 768px) {
  .atencion { padding: var(--space-5) var(--space-4) var(--space-10); gap: var(--space-4); }
  .page-top { padding-bottom: var(--space-4); }
  .page-top-title { font-size: var(--text-4xl); }
  .page-top-actions { width: 100%; }
  .quick-action { flex: 1; justify-content: center; }

  .stepper { padding: var(--space-2); gap: 0; }
  .step { padding: var(--space-2); }
  .step-label { display: none; }
  .step-active .step-label { display: inline; }

  .paso { padding: var(--space-5) var(--space-4) var(--space-6); }
  .field-grid-2 { grid-template-columns: 1fr; }
  .vitals-grid { grid-template-columns: 1fr; }
  .clinical-grid { grid-template-columns: 1fr; }
  .recipe-grid { grid-template-columns: 1fr; }

  .selector-item { grid-template-columns: 1fr auto; gap: var(--space-3); }
  .selector-time { grid-row: 1; grid-column: 1; }
  .selector-info { grid-column: 1 / -1; }
  .selector-item > .status-pill { grid-column: 1; grid-row: 3; }
  .selector-item > .btn-atender-sm { grid-column: 2; grid-row: 1 / span 3; align-self: center; }

  .wizard-footer { flex-direction: column-reverse; padding: var(--space-4); }
  .wizard-footer .btn { width: 100%; }
  .footer-right { width: 100%; flex-direction: column-reverse; }

  .success-body { padding: var(--space-8) var(--space-5); }
  .success-icon { width: 72px; height: 72px; }
  .success-title { font-size: var(--text-3xl); }
  .success-actions { flex-direction: column; }
  .success-actions .btn { width: 100%; }

  .readonly-content { padding: var(--space-4) var(--space-5); }
  .readonly-actions { flex-direction: column-reverse; }
  .readonly-actions .btn { width: 100%; }
}

@media (max-width: 480px) {
  .atencion { padding: var(--space-4) var(--space-3) var(--space-8); }
  .page-top-title { font-size: var(--text-3xl); }
  .patient-strip-name { font-size: var(--text-lg); }
  .section-title { font-size: var(--text-2xs); }

  .step-num { width: 24px; height: 24px; font-size: var(--text-xs); }

  .vital-value { font-size: var(--text-2xl); }
  .success-summary { padding: var(--space-3) var(--space-4); }
  .summary-row { flex-direction: column; gap: var(--space-1); }
  .summary-value { text-align: left; }

  .modal-foot { flex-direction: column-reverse; }
  .modal-foot .btn { width: 100%; }
}
</style>