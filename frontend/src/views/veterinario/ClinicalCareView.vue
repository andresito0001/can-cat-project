<template>
  <div class="atencion">
    <ToastContainer />

    <!-- ═══ CARGANDO ═══ -->
    <div v-if="cargando" class="state-full">
      <div class="spin-lg"></div>
      <p>Cargando el contexto de la cita…</p>
    </div>

    <!-- ═══ ERROR FATAL ═══ -->
    <div v-else-if="errorFatal" class="state-full state-error">
      <div class="state-error-icon"><AlertTriangle :size="36" /></div>
      <h3>No fue posible abrir la atención</h3>
      <p>{{ errorFatal }}</p>
      <button class="btn btn-primary" type="button" @click="router.push('/veterinario/agenda')">
        <ChevronLeft :size="15" /> Volver a mi agenda
      </button>
    </div>

    <!-- ═══ SELECTOR SIN CITA_ID ═══ -->
    <template v-else-if="!citaId">
      <header class="view-header">
        <div>
          <p class="breadcrumb">
            <Stethoscope :size="12" /> Atención clínica
          </p>
          <h1>Selecciona una cita</h1>
          <p class="view-sub">Elige la cita que deseas atender hoy.</p>
        </div>
      </header>

      <section class="card">
        <header class="card-header">
          <div>
            <h3>Citas por atender de hoy</h3>
            <p class="card-sub">{{ citasPendientes.length }} pendientes</p>
          </div>
          <button class="btn btn-ghost btn-sm" type="button" @click="cargarCitasDelDia">
            <RefreshCw :size="13" /> Actualizar
          </button>
        </header>

        <div class="card-body">
          <div v-if="cargandoCitas" class="loading-inline"><div class="spin-md"></div></div>

          <div v-else-if="!citasPendientes.length" class="empty-state">
            <Inbox :size="40" />
            <p>No tienes citas pendientes por atender hoy.</p>
            <button class="btn btn-ghost btn-sm" type="button" @click="router.push('/veterinario/agenda')">
              Ir a mi agenda
            </button>
          </div>

          <div v-else class="selector-list">
            <article v-for="c in citasPendientes" :key="c.idCita" class="selector-item">
              <div class="selector-hora">{{ rangoHora(c.horaInicio, c.horaFin) }}</div>
              <div class="selector-info">
                <div class="selector-title">
                  <PawPrint :size="14" /> <strong>{{ c.mascotaNombre }}</strong>
                  <span class="selector-especie">{{ c.especie }} · {{ c.raza }}</span>
                </div>
                <p class="selector-meta">
                  <User :size="11" /> {{ c.clienteNombre }} · {{ c.motivoConsulta }}
                </p>
              </div>
              <span class="status-pill" :style="estiloEstado(c.estado)">
                {{ infoEstado(c.estado).etiqueta }}
              </span>
              <button class="btn btn-primary btn-sm" type="button"
                      @click="router.push(`/veterinario/atencion/${c.idCita}`)">
                Atender
              </button>
            </article>
          </div>
        </div>
      </section>
    </template>

    <!-- ═══ MODO SOLO LECTURA ═══ -->
    <template v-else-if="modoSoloLectura">
      <header class="view-header">
        <div>
          <p class="breadcrumb"><Eye :size="12" /> Consulta guardada</p>
          <h1>{{ mascota?.nombre }}</h1>
          <p class="view-sub">
            {{ atencionGuardada?.fechaHoraInicio
                ? fechaHoraCorta(atencionGuardada.fechaHoraInicio)
                : fechaCompleta(cita?.fechaCita) }}
          </p>
        </div>
        <span class="pill-readonly"><Eye :size="13" /> Modo solo lectura</span>
      </header>

      <div class="readonly-banner">
        <AlertTriangle :size="15" />
        <span>Esta cita ya fue atendida. Los datos no pueden modificarse.</span>
      </div>

      <div class="readonly-grid">
        <aside class="info-panel">
          <section class="info-block">
            <h4><PawPrint :size="13" /> Paciente</h4>
            <p class="info-primary">{{ mascota?.nombre }}</p>
            <p class="info-line">{{ mascota?.especie }} · {{ mascota?.raza }}</p>
            <p class="info-line">{{ mascota?.sexo }} · {{ mascota?.edad }}</p>
            <p class="info-line">Peso: {{ mascota?.pesoActualKg }} kg</p>
          </section>

          <section class="info-block">
            <h4><User :size="13" /> Dueño</h4>
            <p class="info-primary">{{ dueno?.nombreCompleto }}</p>
            <p class="info-line">{{ dueno?.documentoIdentidad }}</p>
            <p class="info-line"><Phone :size="11" /> {{ dueno?.telefonoPrincipal }}</p>
          </section>

          <section class="info-block">
            <h4><ClipboardList :size="13" /> Cita</h4>
            <p class="info-line"><strong>Motivo:</strong> {{ cita?.motivoConsulta }}</p>
            <p class="info-line"><strong>Servicio:</strong> {{ cita?.servicio }}</p>
            <p class="info-line"><strong>Horario:</strong> {{ rangoHora(cita?.horaInicio, cita?.horaFin) }}</p>
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

          <section class="vitales-row">
            <div class="vital-card">
              <span class="vital-label">Peso</span>
              <strong class="vital-value">{{ atencionGuardada?.pesoKg }} <small>kg</small></strong>
            </div>
            <div class="vital-card">
              <span class="vital-label">Temperatura</span>
              <strong class="vital-value">{{ atencionGuardada?.temperaturaC }} <small>°C</small></strong>
            </div>
            <div class="vital-card">
              <span class="vital-label">Frec. cardíaca</span>
              <strong class="vital-value">{{ atencionGuardada?.frecCardiaca }} <small>lpm</small></strong>
            </div>
            <div v-if="atencionGuardada?.frecRespiratoria" class="vital-card">
              <span class="vital-label">Frec. respiratoria</span>
              <strong class="vital-value">{{ atencionGuardada.frecRespiratoria }} <small>rpm</small></strong>
            </div>
          </section>

          <section class="readonly-block">
            <h5>Anamnesis</h5>
            <p>{{ atencionGuardada?.anamnesis }}</p>
          </section>

          <section v-if="atencionGuardada?.sintomasObservados" class="readonly-block">
            <h5>Hallazgos físicos</h5>
            <p>{{ atencionGuardada.sintomasObservados }}</p>
          </section>

          <section class="readonly-block readonly-highlight">
            <h5>Diagnóstico principal</h5>
            <p class="readonly-diagnostico">{{ atencionGuardada?.diagnosticoPrincipal }}</p>
          </section>

          <section v-if="atencionGuardada?.diagnosticosDiferenciales" class="readonly-block">
            <h5>Diagnósticos diferenciales</h5>
            <p>{{ atencionGuardada.diagnosticosDiferenciales }}</p>
          </section>

          <section v-if="atencionGuardada?.observacionesGenerales" class="readonly-block">
            <h5>Pronóstico / observaciones</h5>
            <p>{{ atencionGuardada.observacionesGenerales }}</p>
          </section>

          <section v-if="atencionGuardada?.indicacionesDueno" class="readonly-block">
            <h5>Indicaciones al dueño</h5>
            <p>{{ atencionGuardada.indicacionesDueno }}</p>
          </section>

          <section v-if="atencionGuardada?.proximaCitaRecomendada" class="readonly-block">
            <h5>Próxima cita recomendada</h5>
            <p>{{ atencionGuardada.proximaCitaRecomendada }}</p>
          </section>

          <section v-if="atencionGuardada?.insumos?.length" class="readonly-block">
            <h5><Package :size="13" /> Insumos aplicados</h5>
            <table class="data-table">
              <thead>
                <tr>
                  <th>Insumo</th>
                  <th class="ta-right">Cant.</th>
                  <th class="ta-right">P. unitario</th>
                  <th class="ta-right">Subtotal</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="i in atencionGuardada.insumos" :key="i.idProducto">
                  <td>
                    <span class="cell-main">{{ i.nombre }}</span>
                    <span class="cell-sub">{{ i.codigoSku }}</span>
                  </td>
                  <td class="ta-right">{{ i.cantidad }} {{ i.unidadMedida }}</td>
                  <td class="ta-right">{{ formatoUSD(i.precioUnitarioUsd) }}</td>
                  <td class="ta-right amount">{{ formatoUSD(subtotalInsumo(i)) }}</td>
                </tr>
              </tbody>
            </table>
            <p class="total-inline">
              <span>Total insumos</span>
              <span class="amount">{{ formatoUSD(totalInsumosGuardados) }}</span>
            </p>
          </section>

          <section v-if="atencionGuardada?.receta" class="readonly-block">
            <header class="readonly-receta-head">
              <h5><Pill :size="13" /> Récipe {{ atencionGuardada.receta.codigoReceta }}</h5>
              <button class="btn btn-primary btn-sm" type="button"
                      :disabled="descargandoReceta"
                      @click="descargarReceta(atencionGuardada.receta.idReceta, atencionGuardada.receta.codigoReceta)">
                <Download :size="13" />
                {{ descargandoReceta ? 'Descargando…' : 'Descargar' }}
              </button>
            </header>
            <p v-if="atencionGuardada.receta.indicacionesGenerales" class="readonly-note">
              <strong>Indicaciones generales:</strong>
              {{ atencionGuardada.receta.indicacionesGenerales }}
            </p>
            <table class="data-table">
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

    <!-- ═══ ÉXITO ═══ -->
    <div v-else-if="guardadoExitoso" class="card">
      <div class="success-body">
        <div class="success-icon"><CheckCircle2 :size="38" /></div>
        <h2>Atención guardada</h2>
        <p class="success-msg">{{ guardadoExitoso.mensaje }}</p>

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
          <button v-if="guardadoExitoso.idReceta"
                  class="btn btn-primary" type="button"
                  :disabled="descargandoReceta"
                  @click="descargarReceta(guardadoExitoso.idReceta, guardadoExitoso.codigoReceta)">
            <Download :size="15" />
            {{ descargandoReceta ? 'Descargando…' : 'Descargar récipe' }}
          </button>
          <button class="btn btn-secondary" type="button" @click="router.push('/veterinario/agenda')">
            <CalendarDays :size="15" /> Volver a agenda
          </button>
        </div>
      </div>
    </div>

    <!-- ═══ WIZARD ═══ -->
    <template v-else>
      <header class="view-header">
        <div>
          <p class="breadcrumb"><Stethoscope :size="12" /> Atención clínica</p>
          <h1>{{ mascota?.nombre }}</h1>
          <p class="view-sub">
            {{ cita?.servicio }} · {{ fechaCompleta(cita?.fechaCita) }} ·
            {{ rangoHora(cita?.horaInicio, cita?.horaFin) }}
          </p>
        </div>
        <button class="btn btn-ghost" type="button" @click="modalCancelarAbierto = true">
          <Ban :size="15" /> Cancelar
        </button>
      </header>

      <!-- Patient strip -->
      <div class="patient-strip">
        <div class="patient-avatar" :style="{ backgroundColor: colorAvatar(mascota?.nombre) }">
          {{ inicialNombre(mascota?.nombre) }}
        </div>
        <div class="patient-info">
          <p class="patient-name">{{ mascota?.nombre }}</p>
          <p class="patient-line">
            {{ mascota?.especie }} · {{ mascota?.raza }} · {{ dueno?.nombreCompleto }}
          </p>
        </div>
        <span class="status-pill" :style="estiloEstado(cita?.estado)">
          {{ infoEstado(cita?.estado).etiqueta }}
        </span>
      </div>

      <!-- Stepper -->
      <nav class="stepper">
        <template v-for="(p, idx) in pasos" :key="p.numero">
          <button
            type="button"
            class="step"
            :class="{
              'step--active': pasoActual === p.numero,
              'step--done': pasoActual > p.numero,
              'step--clickable': p.numero < pasoActual,
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
          <div v-if="idx < pasos.length - 1" class="step-line"
               :class="{ 'step-line--filled': pasoActual > p.numero }"></div>
        </template>
      </nav>

      <!-- Contenido -->
      <div class="wizard-card">

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
                  ></textarea>
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
                    ></textarea>
                  </div>
                  <div class="field">
                    <label class="field-label" for="pronostico">Pronóstico / observaciones</label>
                    <textarea
                      id="pronostico"
                      v-model="form.observacionesGenerales"
                      rows="3"
                      class="field-input field-textarea"
                      placeholder="Ej: pronóstico reservado, se reevalúa en 7 días…"
                    ></textarea>
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
                  ></textarea>
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
                    ></textarea>
                  </div>
                  <div class="field">
                    <label class="field-label" for="proxima">Próxima cita recomendada</label>
                    <input
                      id="proxima"
                      v-model="form.proximaCitaRecomendada"
                      type="date"
                      class="field-input"
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

              <div v-if="cargandoProductos" class="loading-inline"><div class="spin-md"></div></div>

              <div v-else-if="!productos.length" class="empty-state empty-sm">
                <Package :size="32" />
                <p>No fue posible cargar el catálogo.</p>
                <button class="btn btn-ghost btn-sm" type="button" @click="cargarProductos()">Reintentar</button>
              </div>

              <div v-else-if="!productosFiltrados.length" class="empty-state empty-sm">
                <Search :size="32" />
                <p>Sin resultados para «{{ busquedaAplicada }}».</p>
              </div>

              <div v-else class="products-list">
                <button
                  v-for="p in productosFiltrados"
                  :key="p.id"
                  type="button"
                  class="product-item"
                  :class="{ 'product-out': p.stockActual <= 0 }"
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
                    <span class="product-price">{{ formatoUSD(p.precioUsd) }}</span>
                    <span class="product-stock" :class="{ 'stock-zero': p.stockActual <= 0 }">
                      {{ p.stockActual > 0 ? `Stock ${p.stockActual}` : 'Agotado' }}
                    </span>
                  </div>
                  <span class="product-add" aria-hidden="true">
                    <Plus :size="14" />
                  </span>
                </button>
              </div>

              <p class="hint-block">
                Los insumos se descuentan del almacén al guardar y generan una factura
                de productos (insumos + IVA 16%) por cobrar en mostrador.
              </p>
            </div>

            <aside class="side-panel">
              <h4 class="side-title">
                <Syringe :size="13" /> Insumos aplicados
                <span v-if="insumosSeleccionados.length" class="counter">{{ insumosSeleccionados.length }}</span>
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
                    <button class="icon-btn icon-danger" type="button" title="Quitar"
                            @click="quitarInsumo(item)">
                      <Trash2 :size="13" />
                    </button>
                  </div>

                  <div class="applied-controls">
                    <div class="counter-group">
                      <button type="button" :disabled="item.cantidad <= 1"
                              @click="cambiarCantidad(item, -1)">
                        <Minus :size="12" />
                      </button>
                      <input v-model.number="item.cantidad" type="number" min="1"
                             :max="item.stockActual" @change="normalizarCantidad(item)" />
                      <button type="button" :disabled="item.cantidad >= item.stockActual"
                              @click="cambiarCantidad(item, 1)">
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
            ></textarea>
          </div>

          <section class="recipe-block">
            <header class="recipe-head">
              <div>
                <h4><Pill :size="14" /> Récipe médico <span class="pill-opt">opcional</span></h4>
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
              <p v-if="recetaIndicaciones" class="recipe-note">
                <strong>Indicaciones generales:</strong> {{ recetaIndicaciones }}
              </p>
              <div class="table-wrap">
                <table class="data-table">
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
              <table v-if="insumosSeleccionados.length" class="data-table">
                <thead>
                  <tr>
                    <th>Insumo</th>
                    <th class="ta-right">Cant.</th>
                    <th class="ta-right">P. unit.</th>
                    <th class="ta-right">Subtotal</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="i in insumosSeleccionados" :key="i.idProducto">
                    <td>{{ i.nombre }}</td>
                    <td class="ta-right">{{ i.cantidad }} {{ i.unidadMedida }}</td>
                    <td class="ta-right">{{ formatoUSD(i.precioUsd) }}</td>
                    <td class="ta-right amount">{{ formatoUSD(i.precioUsd * i.cantidad) }}</td>
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

        <!-- Footer -->
        <footer class="wizard-footer">
          <button
            v-if="pasoActual > 1"
            class="btn btn-ghost"
            type="button"
            @click="retrocederPaso"
          >
            <ChevronLeft :size="15" /> Anterior
          </button>
          <span v-else></span>

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

      <!-- MODAL: récipe -->
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
                    ></textarea>
                  </div>

                  <div
                    v-for="(item, i) in recetaItemsBorrador"
                    :key="i"
                    class="recipe-item"
                  >
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

      <!-- MODAL: cancelar -->
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
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  Activity, AlertTriangle, Ban, CalendarDays, CheckCircle2, ChevronLeft, ChevronRight,
  ClipboardList, Download, Eye, History, Inbox, Info, Minus, Package, PawPrint, Phone,
  Pill, Plus, RefreshCw, Save, Search, Stethoscope, Syringe, Trash2, User, X, Loader2,
} from 'lucide-vue-next';
import { useToast } from '@/composables/useToast';
import ToastContainer from '@/components/ui/ToastContainer.vue';
import { getAgendaVet, getCitaContexto, iniciarAtencion, guardarAtencion, descargarRecetaPdf } from '@/api/atenciones.api';
import { getProductos } from '@/api/almacen.api';
import { getApiErrorMessage, getValidationFieldErrors } from '@/utils/apiError';
import { descargarBlob } from '@/utils/descargas';
import { ESTADO_COLOR } from '@/utils/constants/estadosCita';
import { fechaCompleta, fechaHoraCorta, hoyISO, rangoHora } from '@/utils/fecha';

const route = useRoute();
const router = useRouter();
const { toastExito, toastError, toastInfo } = useToast();

const formatoUSD = (valor) => `$${Number(valor || 0).toFixed(2)}`;

// ══ Estado general ══
const citaId = computed(() => route.params.citaId);
const cargando = ref(true);
const errorFatal = ref(null);
const contexto = ref(null);
const modoSoloLectura = ref(false);
const guardadoExitoso = ref(null);
const guardando = ref(false);
const descargandoReceta = ref(false);

const pasos = [
  { numero: 1, titulo: 'Evaluación clínica' },
  { numero: 2, titulo: 'Tratamiento e insumos' },
  { numero: 3, titulo: 'Récipe e indicaciones' },
  { numero: 4, titulo: 'Revisión y guardado' },
];
const pasoActual = ref(1);

// ══ Paso 1 ══
const form = reactive({
  anamnesis: '', sintomasObservados: '', pesoKg: null, temperaturaC: null,
  frecCardiaca: null, frecRespiratoria: null, diagnosticoPrincipal: '',
  diagnosticosDiferenciales: '', observacionesGenerales: '',
  indicacionesDueno: '', proximaCitaRecomendada: '',
});
const errores = reactive({});

// ══ Paso 2 ══
const productos = ref([]);
const cargandoProductos = ref(false);
const busqueda = ref('');
const busquedaAplicada = ref('');
const insumosSeleccionados = ref([]);
const errorStock = ref(null);
let temporizadorBusqueda = null;

// ══ Paso 3 ══
const VIAS_ADMINISTRACION = ['Oral', 'Intramuscular', 'Subcutánea', 'Intravenosa', 'Tópica', 'Ocular'];
const modalRecetaAbierto = ref(false);
const recetaGenerada = ref(false);
const recetaItems = ref([]);
const recetaIndicaciones = ref('');
const recetaItemsBorrador = ref([]);
const recetaIndicacionesBorrador = ref('');
const erroresReceta = ref([]);

// ══ Alterno A ══
const modalCancelarAbierto = ref(false);

// ══ Selector de citas ══
const citasPendientes = ref([]);
const cargandoCitas = ref(false);

// ══ Computados ══
const cita = computed(() => contexto.value?.cita || null);
const mascota = computed(() => contexto.value?.mascota || null);
const dueno = computed(() => contexto.value?.dueno || null);
const ultimaAtencion = computed(() => contexto.value?.ultimaAtencion || null);
const atencionGuardada = computed(() => contexto.value?.atencionGuardada || null);

const productosFiltrados = computed(() => {
  const q = busquedaAplicada.value.trim().toLowerCase();
  if (!q) return productos.value;
  return productos.value.filter((p) =>
    [p.nombre, p.codigoSku, p.tipoCategoria, p.presentacion]
      .filter(Boolean)
      .some((v) => String(v).toLowerCase().includes(q)));
});

const totalInsumos = computed(() =>
  insumosSeleccionados.value.reduce((s, i) => s + Number(i.precioUsd) * Number(i.cantidad), 0));
const ivaEstimado = computed(() => totalInsumos.value * 0.16);
const totalFacturaEstimado = computed(() => totalInsumos.value * 1.16);

const textoInsumosAplicados = computed(() => {
  const val = guardadoExitoso.value?.resumen?.insumosAplicados;
  if (val == null) return 'Ninguno';
  if (Array.isArray(val)) return val.length ? val.join(', ') : 'Ninguno';
  return String(val);
});

const totalInsumosGuardados = computed(() =>
  (atencionGuardada.value?.insumos || []).reduce((s, i) => s + subtotalInsumo(i), 0));

function subtotalInsumo(insumo) {
  return insumo.subtotal ?? insumo.subtotalUsd ?? (insumo.cantidad * insumo.precioUnitarioUsd);
}

const PALETA_AVATARES = ['#0F766E', '#3B82F6', '#F59E0B', '#F43F5E', '#8B5CF6', '#0EA5E9'];
function colorAvatar(nombre) {
  let hash = 0;
  for (const c of String(nombre || '')) hash = (hash * 31 + c.charCodeAt(0)) % 997;
  return PALETA_AVATARES[hash % PALETA_AVATARES.length];
}
function inicialNombre(nombre) {
  return String(nombre || '?').trim().charAt(0).toUpperCase() || '?';
}
function infoEstado(estado) {
  return { color: ESTADO_COLOR[estado] || '#64748B', etiqueta: String(estado || '').replaceAll('_', ' ') };
}
function estiloEstado(estado) {
  const { color } = infoEstado(estado);
  return { color, borderColor: color, backgroundColor: `${color}1A` };
}

// ══ Ciclo de vida ══
function reiniciarFormulario() {
  pasoActual.value = 1;
  guardadoExitoso.value = null;
  modoSoloLectura.value = false;
  errorFatal.value = null;
  contexto.value = null;
  errorStock.value = null;
  Object.assign(form, {
    anamnesis: '', sintomasObservados: '', pesoKg: null, temperaturaC: null,
    frecCardiaca: null, frecRespiratoria: null, diagnosticoPrincipal: '',
    diagnosticosDiferenciales: '', observacionesGenerales: '',
    indicacionesDueno: '', proximaCitaRecomendada: '',
  });
  Object.keys(errores).forEach((k) => delete errores[k]);
  insumosSeleccionados.value = [];
  busqueda.value = '';
  busquedaAplicada.value = '';
  recetaGenerada.value = false;
  recetaItems.value = [];
  recetaIndicaciones.value = '';
}

async function inicializar() {
  reiniciarFormulario();
  if (!citaId.value) {
    cargando.value = false;
    cargarCitasDelDia();
    return;
  }
  cargando.value = true;
  try {
    contexto.value = await getCitaContexto(citaId.value);
    if (contexto.value.atencionGuardada) {
      modoSoloLectura.value = true;
      return;
    }
    if (contexto.value.cita.estado === 'Confirmada') {
      try {
        await iniciarAtencion(citaId.value);
        contexto.value.cita.estado = 'En_Atencion';
      } catch (errorInicio) {
        errorFatal.value = getApiErrorMessage(errorInicio);
        return;
      }
    } else if (contexto.value.cita.estado !== 'En_Atencion') {
      errorFatal.value = `La cita no puede atenderse en su estado actual (${contexto.value.cita.estado}).`;
      return;
    }
    form.pesoKg = contexto.value.mascota?.pesoActualKg ?? null;
    cargarProductos(true);
  } catch (error) {
    errorFatal.value = getApiErrorMessage(error);
  } finally {
    cargando.value = false;
  }
}

onMounted(inicializar);
watch(() => route.params.citaId, () => {
  if (route.path.startsWith('/veterinario/atencion')) inicializar();
});

async function cargarCitasDelDia() {
  cargandoCitas.value = true;
  try {
    const agenda = await getAgendaVet(hoyISO());
    citasPendientes.value = agenda.filter((c) => !c.atendida);
  } catch (error) {
    toastError(getApiErrorMessage(error));
    citasPendientes.value = [];
  } finally {
    cargandoCitas.value = false;
  }
}

async function cargarProductos(silencioso = false) {
  cargandoProductos.value = true;
  try {
    productos.value = await getProductos();
    insumosSeleccionados.value.forEach((item) => {
      const fresco = productos.value.find((p) => p.idProducto === item.idProducto);
      if (fresco) {
        item.stockActual = fresco.stockActual;
        if (fresco.stockActual > 0 && item.cantidad > fresco.stockActual) item.cantidad = fresco.stockActual;
      }
    });
  } catch (error) {
    if (!silencioso) toastError(getApiErrorMessage(error));
  } finally {
    cargandoProductos.value = false;
  }
}

watch(busqueda, (valor) => {
  clearTimeout(temporizadorBusqueda);
  temporizadorBusqueda = setTimeout(() => { busquedaAplicada.value = valor; }, 350);
});

function avanzarPaso() {
  if (pasoActual.value === 1 && !validarPaso1()) {
    toastError('Debe completar el diagnóstico y los signos vitales obligatorios');
    return;
  }
  pasoActual.value = Math.min(4, pasoActual.value + 1);
}
function retrocederPaso() {
  pasoActual.value = Math.max(1, pasoActual.value - 1);
}

function validarPaso1() {
  Object.keys(errores).forEach((k) => delete errores[k]);
  if (!form.anamnesis || !form.anamnesis.trim()) errores.anamnesis = 'La anamnesis es obligatoria';
  const peso = parseFloat(form.pesoKg);
  if (form.pesoKg === null || form.pesoKg === '' || Number.isNaN(peso)) errores.pesoKg = 'Campo obligatorio';
  else if (peso < 0.01 || peso > 999.99) errores.pesoKg = 'Debe estar entre 0.01 y 999.99 kg';
  const temp = parseFloat(form.temperaturaC);
  if (form.temperaturaC === null || form.temperaturaC === '' || Number.isNaN(temp)) errores.temperaturaC = 'Campo obligatorio';
  else if (temp < 30.0 || temp > 45.0) errores.temperaturaC = 'Debe estar entre 30.0 y 45.0 °C';
  const fc = parseFloat(form.frecCardiaca);
  if (form.frecCardiaca === null || form.frecCardiaca === '' || Number.isNaN(fc)) errores.frecCardiaca = 'Campo obligatorio';
  else if (fc < 20 || fc > 400) errores.frecCardiaca = 'Debe estar entre 20 y 400 lpm';
  if (form.frecRespiratoria !== null && form.frecRespiratoria !== '') {
    const fr = parseFloat(form.frecRespiratoria);
    if (Number.isNaN(fr) || fr < 1 || fr > 400) errores.frecRespiratoria = 'Debe estar entre 1 y 400 rpm';
  }
  if (!form.diagnosticoPrincipal || !form.diagnosticoPrincipal.trim()) errores.diagnosticoPrincipal = 'El diagnóstico es obligatorio';
  return Object.keys(errores).length === 0;
}

function agregarInsumo(producto) {
  if (producto.stockActual <= 0) {
    toastError('Stock insuficiente para este producto. Verifique con el almacén');
    return;
  }
  const existente = insumosSeleccionados.value.find((i) => i.idProducto === producto.id);
  if (existente) {
    if (existente.cantidad < producto.stockActual) existente.cantidad += 1;
    else toastInfo('Ya seleccionó todo el stock disponible de este producto');
  } else {
    insumosSeleccionados.value.push({
      idProducto: producto.id, nombre: producto.nombre, codigoSku: producto.codigoSku,
      unidadMedida: producto.unidadMedida, precioUsd: producto.precioUsd,
      cantidad: 1, stockActual: producto.stockActual, requiereReceta: producto.requiereReceta,
    });
  }
}

function quitarInsumo(item) {
  insumosSeleccionados.value = insumosSeleccionados.value.filter((i) => i.idProducto !== item.idProducto);
  if (errorStock.value && errorStock.value.nombre === item.nombre) errorStock.value = null;
}

function cambiarCantidad(item, delta) {
  const nueva = Number(item.cantidad) + delta;
  if (nueva < 1) return;
  if (nueva > item.stockActual) { toastInfo('No hay más stock disponible de este producto'); return; }
  item.cantidad = nueva;
}

function normalizarCantidad(item) {
  let c = Number(item.cantidad);
  if (!Number.isFinite(c) || c < 1) c = 1;
  if (item.stockActual > 0 && c > item.stockActual) c = item.stockActual;
  item.cantidad = c;
}

function nuevoItemReceta() {
  return { medicamento: '', concentracion: '', dosis: '', viaAdministracion: 'Oral', frecuencia: '', duracion: '' };
}

function abrirModalReceta() {
  recetaItemsBorrador.value = recetaItems.value.length
    ? recetaItems.value.map((i) => ({ ...i }))
    : [nuevoItemReceta()];
  recetaIndicacionesBorrador.value = recetaIndicaciones.value;
  erroresReceta.value = [];
  modalRecetaAbierto.value = true;
}
function cerrarModalReceta() { modalRecetaAbierto.value = false; }
function agregarItemReceta() { recetaItemsBorrador.value.push(nuevoItemReceta()); }
function quitarItemReceta(indice) {
  if (recetaItemsBorrador.value.length <= 1) return;
  recetaItemsBorrador.value.splice(indice, 1);
}

function validarRecetaBorrador() {
  erroresReceta.value = recetaItemsBorrador.value.map((item) => {
    const err = {};
    if (!item.medicamento?.trim()) err.medicamento = 'Obligatorio';
    if (!item.dosis?.trim()) err.dosis = 'Obligatorio';
    if (!item.frecuencia?.trim()) err.frecuencia = 'Obligatorio';
    if (!item.duracion?.trim()) err.duracion = 'Obligatorio';
    return err;
  });
  return erroresReceta.value.every((e) => Object.keys(e).length === 0);
}

function confirmarReceta() {
  if (!recetaItemsBorrador.value.length) {
    toastError('El récipe debe incluir al menos un medicamento');
    return;
  }
  if (!validarRecetaBorrador()) {
    toastError('Complete los campos obligatorios (dosis, frecuencia y duración)');
    return;
  }
  recetaItems.value = recetaItemsBorrador.value.map((i) => ({ ...i }));
  recetaIndicaciones.value = recetaIndicacionesBorrador.value;
  recetaGenerada.value = true;
  modalRecetaAbierto.value = false;
  toastExito('Récipe generado. Se anexará a la atención al guardar.');
}

function descartarReceta() {
  recetaItems.value = [];
  recetaIndicaciones.value = '';
  recetaGenerada.value = false;
  toastInfo('Récipe descartado');
}

function confirmarCancelacion() {
  modalCancelarAbierto.value = false;
  router.push('/veterinario/agenda');
}

function construirPayload() {
  const payload = {
    idCita: Number(citaId.value),
    anamnesis: form.anamnesis.trim(),
    diagnosticoPrincipal: form.diagnosticoPrincipal.trim(),
    pesoKg: Number(form.pesoKg),
    temperaturaC: Number(form.temperaturaC),
    frecCardiaca: Number(form.frecCardiaca),
  };
  const opcionales = {
    sintomasObservados: form.sintomasObservados.trim(),
    frecRespiratoria: (form.frecRespiratoria === null || form.frecRespiratoria === '') ? null : Number(form.frecRespiratoria),
    diagnosticosDiferenciales: form.diagnosticosDiferenciales.trim(),
    observacionesGenerales: form.observacionesGenerales.trim(),
    indicacionesDueno: form.indicacionesDueno.trim(),
    proximaCitaRecomendada: form.proximaCitaRecomendada || null,
  };
  Object.entries(opcionales).forEach(([k, v]) => {
    if (v !== null && v !== '') payload[k] = v;
  });
  if (insumosSeleccionados.value.length) {
    payload.insumos = insumosSeleccionados.value.map((i) => ({ idProducto: i.idProducto, cantidad: i.cantidad }));
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
    };
    if (recetaIndicaciones.value.trim()) payload.receta.indicacionesGenerales = recetaIndicaciones.value.trim();
  }
  return payload;
}

async function guardarConsulta() {
  if (guardando.value) return;
  guardando.value = true;
  try {
    const respuesta = await guardarAtencion(construirPayload());
    guardadoExitoso.value = respuesta;
    toastExito(respuesta.mensaje || 'Atención registrada correctamente');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (error) {
    const mensaje = getApiErrorMessage(error);
    const status = error?.response?.status;

    if (status === 409 && mensaje.includes('Stock insuficiente')) {
      pasoActual.value = 2;
      errorStock.value = { nombre: mensaje.match(/'([^']+)'/)?.[1] || null, mensaje };
      await cargarProductos(true);
      toastError(mensaje);
    } else if (status === 409 && mensaje.includes('ya fue atendida')) {
      toastError(mensaje);
      await inicializar();
    } else if (status === 400) {
      const campos = getValidationFieldErrors(error);
      if (campos) {
        Object.entries(campos).forEach(([campo, msg]) => { errores[campo] = msg; });
        const camposPaso1 = ['anamnesis', 'sintomasObservados', 'pesoKg', 'temperaturaC', 'frecCardiaca',
          'frecRespiratoria', 'diagnosticoPrincipal', 'diagnosticosDiferenciales', 'observacionesGenerales', 'proximaCitaRecomendada'];
        if (Object.keys(campos).some((c) => camposPaso1.includes(c))) pasoActual.value = 1;
      }
      toastError(mensaje);
    } else {
      toastError(mensaje);
    }
  } finally {
    guardando.value = false;
  }
}

async function descargarReceta(idReceta, codigoReceta) {
  if (!idReceta) return;
  descargandoReceta.value = true;
  try {
    const { blob, filename } = await descargarRecetaPdf(idReceta);
    descargarBlob(blob, filename || `receta-${codigoReceta || idReceta}.pdf`);
  } catch (error) {
    toastError(getApiErrorMessage(error));
  } finally {
    descargandoReceta.value = false;
  }
}

function verHistorialCompleto() {
  router.push({
    path: '/veterinario/historiales',
    query: { mascota: mascota.value?.idMascota, nombre: mascota.value?.nombre },
  });
}
</script>

<style scoped>
/* ═══════════════════════════════════════════════════════════════
   RESET / BASE
   ═══════════════════════════════════════════════════════════════ */
.atencion {
  max-width: 1320px;
  margin: 0 auto;
  padding: 24px 24px 56px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  color: #1E293B;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
button { font-family: inherit; }

/* ═══════════════════════════════════════════════════════════════
   HEADER
   ═══════════════════════════════════════════════════════════════ */
.view-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}
.breadcrumb {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 6px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .7px;
  color: #0F766E;
}
.view-header h1 {
  margin: 0 0 4px;
  font-size: 24px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.02em;
  line-height: 1.15;
}
.view-sub {
  margin: 0;
  font-size: 13.5px;
  color: #64748B;
}

/* ═══════════════════════════════════════════════════════════════
   STATES (loading / error / empty)
   ═══════════════════════════════════════════════════════════════ */
.state-full {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 80px 24px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  color: #64748B;
  text-align: center;
  min-height: 320px;
}
.state-full h3 {
  margin: 4px 0 0;
  font-size: 17px;
  font-weight: 700;
  color: #1E293B;
}
.state-full p {
  margin: 0 0 8px;
  font-size: 13.5px;
  max-width: 460px;
  line-height: 1.55;
}
.state-error-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #FEF2F2;
  color: #DC2626;
  display: flex;
  align-items: center;
  justify-content: center;
}
.spin { animation: spin 1s linear infinite; }
.spin-lg {
  width: 34px; height: 34px;
  border: 3px solid #E2E8F0;
  border-top-color: #0F766E;
  border-radius: 50%;
  animation: spin .8s linear infinite;
}
.spin-md {
  width: 24px; height: 24px;
  border: 2.5px solid #E2E8F0;
  border-top-color: #0F766E;
  border-radius: 50%;
  animation: spin .8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.loading-inline {
  display: flex;
  justify-content: center;
  padding: 40px 24px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px 24px;
  color: #94A3B8;
  text-align: center;
  font-size: 13.5px;
}
.empty-state p { margin: 0; }
.empty-sm { padding: 32px 16px; }

/* ═══════════════════════════════════════════════════════════════
   CARD
   ═══════════════════════════════════════════════════════════════ */
.card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,.03), 0 10px 15px -3px rgba(0,0,0,.04);
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 24px;
  border-bottom: 1px solid #E2E8F0;
}
.card-header h3 { margin: 0; font-size: 15.5px; font-weight: 700; }
.card-sub { margin: 3px 0 0; font-size: 12.5px; color: #64748B; }
.card-body { padding: 20px 24px; }

/* ═══════════════════════════════════════════════════════════════
   SELECTOR DE CITAS
   ═══════════════════════════════════════════════════════════════ */
.selector-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.selector-item {
  display: grid;
  grid-template-columns: 110px 1fr auto auto;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  transition: border-color .2s, background .2s;
}
.selector-item:hover { border-color: #CBD5E1; background: #fff; }
.selector-hora {
  font-size: 13px;
  font-weight: 700;
  color: #0F766E;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.selector-info { min-width: 0; }
.selector-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #0F172A;
  margin-bottom: 3px;
}
.selector-title svg { color: #0F766E; flex-shrink: 0; }
.selector-especie {
  font-size: 11.5px;
  color: #64748B;
  font-weight: 500;
}
.selector-meta {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12.5px;
  color: #64748B;
}

/* ═══════════════════════════════════════════════════════════════
   STATUS PILL
   ═══════════════════════════════════════════════════════════════ */
.status-pill {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 11.5px;
  font-weight: 700;
  border: 1px solid;
  white-space: nowrap;
}

/* ═══════════════════════════════════════════════════════════════
   BOTONES
   ═══════════════════════════════════════════════════════════════ */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: all .18s ease;
  border: 1px solid transparent;
  white-space: nowrap;
  line-height: 1.2;
}
.btn:disabled { opacity: .55; cursor: not-allowed; }
.btn-sm { padding: 7px 13px; font-size: 12.5px; }
.btn-lg { padding: 12px 24px; font-size: 14px; }
.btn-block { width: 100%; }

.btn-primary {
  background: #0F766E;
  color: #fff;
  border-color: #0F766E;
}
.btn-primary:hover:not(:disabled) {
  background: #115E59;
  border-color: #115E59;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px -4px rgba(15,118,110,.4);
}

.btn-secondary {
  background: #fff;
  color: #475569;
  border-color: #E2E8F0;
}
.btn-secondary:hover:not(:disabled) {
  background: #F8FAFC;
  border-color: #CBD5E1;
  color: #0F766E;
}

.btn-ghost {
  background: #fff;
  color: #0F766E;
  border-color: #99F6E4;
}
.btn-ghost:hover:not(:disabled) { background: #F0FDFA; }
.btn-danger-ghost { color: #DC2626; border-color: #FECACA; }
.btn-danger-ghost:hover:not(:disabled) { background: #FEF2F2; }

.btn-danger {
  background: #DC2626;
  color: #fff;
  border-color: #DC2626;
}
.btn-danger:hover:not(:disabled) { background: #B91C1C; border-color: #B91C1C; }

.icon-btn {
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
  transition: all .15s;
  flex-shrink: 0;
}
.icon-btn:hover:not(:disabled) { border-color: #CBD5E1; color: #334155; }
.icon-btn:disabled { opacity: .4; cursor: not-allowed; }
.icon-danger:hover:not(:disabled) {
  border-color: #FECACA; color: #DC2626; background: #FEF2F2;
}

/* ═══════════════════════════════════════════════════════════════
   READONLY
   ═══════════════════════════════════════════════════════════════ */
.pill-readonly {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  background: #F1F5F9;
  color: #475569;
  border: 1px solid #E2E8F0;
  font-size: 12px;
  font-weight: 700;
}
.readonly-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 18px;
  background: #FFFBEB;
  color: #B45309;
  border: 1px solid #FDE68A;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
}
.readonly-grid {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  align-items: start;
}
.readonly-content {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.readonly-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F1F5F9;
}
.readonly-vet {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: #0F766E;
}
.readonly-block {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.readonly-block h5 {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
}
.readonly-block p {
  margin: 0;
  font-size: 13.5px;
  color: #334155;
  line-height: 1.6;
  white-space: pre-wrap;
}
.readonly-highlight {
  background: #F0FDFA;
  border-left: 3px solid #0F766E;
  padding: 12px 16px;
  border-radius: 8px;
}
.readonly-diagnostico {
  font-size: 14px !important;
  font-weight: 600;
  color: #0F172A !important;
}
.readonly-note {
  margin: 0 0 8px;
  font-size: 13px;
  color: #334155;
}
.readonly-receta-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.readonly-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

/* ═══════════════════════════════════════════════════════════════
   VITALS ROW
   ═══════════════════════════════════════════════════════════════ */
.vitales-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 10px;
}
.vital-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px 14px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
}
.vital-label {
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #94A3B8;
}
.vital-value {
  font-size: 18px;
  font-weight: 700;
  color: #0F172A;
  letter-spacing: -0.01em;
  line-height: 1.1;
}
.vital-value small {
  font-size: 11.5px;
  font-weight: 600;
  color: #64748B;
  margin-left: 3px;
}

/* ═══════════════════════════════════════════════════════════════
   TABLAS
   ═══════════════════════════════════════════════════════════════ */
.table-wrap { overflow-x: auto; }
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-family: inherit;
}
.data-table thead { background: #F8FAFC; }
.data-table th {
  text-align: left;
  padding: 10px 14px;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #64748B;
  border-bottom: 1px solid #E2E8F0;
  white-space: nowrap;
}
.data-table td {
  padding: 11px 14px;
  font-size: 13px;
  color: #334155;
  border-bottom: 1px solid #F1F5F9;
  vertical-align: middle;
}
.data-table tbody tr:last-child td { border-bottom: none; }
.cell-main { font-weight: 600; color: #0F172A; display: block; }
.cell-sub {
  display: block;
  font-size: 11px;
  color: #94A3B8;
  font-family: ui-monospace, monospace;
  margin-top: 1px;
}
.ta-right { text-align: right; }
.amount { font-weight: 700; color: #0F766E; }
.total-inline {
  display: flex;
  justify-content: space-between;
  margin: 12px 0 0;
  padding-top: 10px;
  border-top: 1px dashed #E2E8F0;
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}

/* ═══════════════════════════════════════════════════════════════
   SIDE PANEL
   ═══════════════════════════════════════════════════════════════ */
.side-panel {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  position: sticky;
  top: 24px;
  align-self: start;
}
.side-title {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #0F766E;
}
.side-title .counter {
  margin-left: auto;
  background: #0F766E;
  color: #fff;
  border-radius: 20px;
  font-size: 10.5px;
  min-width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
}
.side-block {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.side-block h4 {
  margin: 0 0 2px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .4px;
  color: #0F766E;
}
.side-primary {
  margin: 0;
  font-size: 14.5px;
  font-weight: 700;
  color: #0F172A;
}
.side-line {
  margin: 0;
  font-size: 12.5px;
  color: #64748B;
  line-height: 1.5;
  display: flex;
  align-items: center;
  gap: 5px;
  flex-wrap: wrap;
}
.side-line strong { color: #334155; }
.side-badge {
  display: inline-flex;
  margin-top: 4px;
  padding: 3px 10px;
  border-radius: 20px;
  background: #ECFDF5;
  color: #059669;
  border: 1px solid #A7F3D0;
  font-size: 11px;
  font-weight: 700;
  align-self: flex-start;
}
.side-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px 12px;
  color: #94A3B8;
  text-align: center;
  font-size: 12.5px;
}
.side-empty p { margin: 0; }

/* ═══════════════════════════════════════════════════════════════
   PATIENT STRIP (wizard)
   ═══════════════════════════════════════════════════════════════ */
.patient-strip {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #F0FDFA 0%, #FFFFFF 70%);
  border: 1px solid #CCFBF1;
  border-radius: 14px;
  flex-wrap: wrap;
}
.patient-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}
.patient-info { min-width: 0; flex: 1; }
.patient-name {
  margin: 0 0 2px;
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
}
.patient-line {
  margin: 0;
  font-size: 13px;
  color: #64748B;
}

/* ═══════════════════════════════════════════════════════════════
   STEPPER
   ═══════════════════════════════════════════════════════════════ */
.stepper {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  overflow-x: auto;
  scrollbar-width: none;
}
.stepper::-webkit-scrollbar { display: none; }
.step {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border: none;
  background: transparent;
  font-family: inherit;
  cursor: pointer;
  border-radius: 8px;
  transition: background .15s;
  flex-shrink: 0;
}
.step:disabled { cursor: default; }
.step--clickable:hover { background: #F0FDFA; }
.step-num {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  border: 2px solid #E2E8F0;
  background: #fff;
  color: #94A3B8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  transition: all .2s;
}
.step-label {
  font-size: 12.5px;
  font-weight: 600;
  color: #94A3B8;
  white-space: nowrap;
  transition: color .2s;
}
.step--active .step-num {
  border-color: #0F766E;
  color: #0F766E;
  box-shadow: 0 0 0 4px rgba(15,118,110,.1);
}
.step--active .step-label { color: #0F766E; }
.step--done .step-num {
  border-color: #0F766E;
  background: #0F766E;
  color: #fff;
}
.step--done .step-label { color: #334155; }
.step-line {
  flex: 1;
  min-width: 20px;
  height: 2px;
  background: #E2E8F0;
  transition: background .2s;
}
.step-line--filled { background: #0F766E; }

/* ═══════════════════════════════════════════════════════════════
   WIZARD CARD
   ═══════════════════════════════════════════════════════════════ */
.wizard-card {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 14px;
  border-top: 4px solid #0F766E;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,.03), 0 10px 15px -3px rgba(0,0,0,.04);
  overflow: hidden;
}
.paso { padding: 24px; }
.paso-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 24px;
  align-items: start;
}
.paso-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 0;
}

/* ═══════════════════════════════════════════════════════════════
   FORM SECTIONS
   ═══════════════════════════════════════════════════════════════ */
.form-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.section-title {
  margin: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #F1F5F9;
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .6px;
  color: #0F766E;
}
.section-title svg { color: #0F766E; }

/* ═══════════════════════════════════════════════════════════════
   FIELDS
   ═══════════════════════════════════════════════════════════════ */
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}
.field-label {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12.5px;
  font-weight: 600;
  color: #374151;
  line-height: 1.3;
}
.field-label small {
  font-size: 10.5px;
  color: #94A3B8;
  font-weight: 500;
}
.req { color: #EF4444; margin-left: 1px; }
.field-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  font-size: 13.5px;
  color: #1E293B;
  background: #fff;
  font-family: inherit;
  transition: border-color .18s, box-shadow .18s, background .18s;
  box-sizing: border-box;
  line-height: 1.45;
}
.field-input::placeholder { color: #94A3B8; }
.field-input:hover:not(:disabled) { border-color: #9CA3AF; }
.field-input:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15,118,110,.12);
}
.field-input:disabled { background: #F8FAFC; color: #94A3B8; cursor: not-allowed; }
.field-textarea {
  resize: vertical;
  min-height: 68px;
  font-family: inherit;
  line-height: 1.55;
}
.field-select {
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%2364748B' stroke-width='2.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='m6 9 6 6 6-6'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  padding-right: 40px;
  cursor: pointer;
}
.field-error,
.field-has-error .field-input {
  border-color: #EF4444 !important;
  background: #FEF2F2;
}
.field-error:focus,
.field-has-error .field-input:focus {
  box-shadow: 0 0 0 3px rgba(239,68,68,.12);
}
.error-msg {
  margin: 0;
  font-size: 12px;
  color: #EF4444;
  font-weight: 600;
  line-height: 1.4;
}

.field-grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

/* ═══════════════════════════════════════════════════════════════
   VITALS GRID
   ═══════════════════════════════════════════════════════════════ */
.vitals-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.vital-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

/* ═══════════════════════════════════════════════════════════════
   BÚSQUEDA Y CATÁLOGO (paso 2)
   ═══════════════════════════════════════════════════════════════ */
.search-bar {
  position: relative;
  margin-bottom: 14px;
}
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
  padding: 11px 44px;
  border: 1.5px solid #E2E8F0;
  border-radius: 11px;
  font-size: 13.5px;
  color: #1E293B;
  background: #fff;
  font-family: inherit;
  transition: border-color .15s, box-shadow .15s;
  box-sizing: border-box;
}
.search-input::placeholder { color: #94A3B8; }
.search-input:focus {
  outline: none;
  border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15,118,110,.1);
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
  padding: 6px;
  border-radius: 6px;
  display: flex;
  align-items: center;
}
.search-clear:hover { color: #475569; background: #F1F5F9; }

.products-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 520px;
  overflow-y: auto;
  padding-right: 4px;
  scrollbar-width: thin;
  scrollbar-color: #CBD5E1 transparent;
}
.products-list::-webkit-scrollbar { width: 6px; }
.products-list::-webkit-scrollbar-track { background: transparent; }
.products-list::-webkit-scrollbar-thumb { background: #CBD5E1; border-radius: 3px; }

.product-item {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 14px;
  align-items: center;
  padding: 12px 14px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 11px;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: border-color .15s, background .15s, transform .15s;
}
.product-item:hover { border-color: #99F6E4; background: #F0FDFA; }
.product-item:active { transform: scale(.995); }
.product-out { opacity: .6; cursor: not-allowed; }
.product-out:hover { border-color: #E2E8F0; background: #F8FAFC; }

.product-info { min-width: 0; }
.product-name {
  margin: 0 0 3px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.3;
}
.product-meta {
  margin: 0;
  font-size: 11.5px;
  color: #64748B;
}
.pill-recipe {
  display: inline-flex;
  padding: 2px 8px;
  border-radius: 20px;
  background: #FFFBEB;
  color: #B45309;
  border: 1px solid #FDE68A;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .3px;
}
.product-side {
  display: flex;
  flex-direction: column;
  gap: 3px;
  align-items: flex-end;
  text-align: right;
  flex-shrink: 0;
}
.product-price {
  font-size: 13.5px;
  font-weight: 700;
  color: #0F766E;
  font-variant-numeric: tabular-nums;
}
.product-stock {
  font-size: 11px;
  font-weight: 600;
  color: #64748B;
}
.stock-zero { color: #DC2626; }
.product-add {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #E2E8F0;
  color: #0F766E;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.product-item:hover .product-add { background: #0F766E; color: #fff; border-color: #0F766E; }

.hint-block {
  margin: 14px 0 0;
  padding: 10px 14px;
  background: #F0FDFA;
  border-left: 3px solid #0F766E;
  border-radius: 8px;
  font-size: 12px;
  color: #0F766E;
  line-height: 1.5;
}

/* ═══════════════════════════════════════════════════════════════
   INSUMOS APLICADOS
   ═══════════════════════════════════════════════════════════════ */
.applied-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.applied-item {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  transition: border-color .2s, box-shadow .2s;
}
.applied-error {
  border-color: #EF4444;
  box-shadow: 0 0 0 3px rgba(239,68,68,.1);
}
.applied-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}
.applied-info { min-width: 0; }
.applied-name {
  margin: 0 0 2px;
  font-size: 13px;
  font-weight: 700;
  color: #0F172A;
  line-height: 1.3;
}
.applied-meta {
  margin: 0;
  font-size: 11px;
  color: #64748B;
}
.applied-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.counter-group {
  display: inline-flex;
  align-items: center;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}
.counter-group button {
  width: 28px;
  height: 30px;
  border: none;
  background: #F8FAFC;
  color: #64748B;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background .15s, color .15s;
}
.counter-group button:hover:not(:disabled) { background: #0F766E; color: #fff; }
.counter-group button:disabled { color: #CBD5E1; cursor: not-allowed; }
.counter-group input {
  width: 44px;
  height: 30px;
  border: none;
  border-left: 1px solid #E2E8F0;
  border-right: 1px solid #E2E8F0;
  text-align: center;
  font-size: 13px;
  font-weight: 700;
  color: #0F172A;
  font-family: inherit;
  background: #fff;
  box-sizing: border-box;
}
.counter-group input:focus { outline: none; }
.counter-group input::-webkit-outer-spin-button,
.counter-group input::-webkit-inner-spin-button { -webkit-appearance: none; margin: 0; }
.counter-group input { -moz-appearance: textfield; }

.applied-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  margin-top: 4px;
  border-top: 1px dashed #E2E8F0;
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}

.alert-inline {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.4;
}
.alert-error {
  background: #FEF2F2;
  color: #B91C1C;
  border: 1px solid #FECACA;
}

/* ═══════════════════════════════════════════════════════════════
   PASO 3 — RECIPE
   ═══════════════════════════════════════════════════════════════ */
.recipe-block {
  margin-top: 18px;
  padding: 18px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
}
.recipe-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.recipe-head h4 {
  margin: 0 0 4px;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
}
.recipe-sub {
  margin: 0;
  font-size: 12.5px;
  color: #64748B;
}
.pill-opt {
  display: inline-flex;
  padding: 2px 9px;
  margin-left: 4px;
  border-radius: 20px;
  background: #F1F5F9;
  color: #64748B;
  font-size: 10.5px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .3px;
}
.recipe-actions { display: flex; gap: 8px; flex-wrap: wrap; }
.recipe-preview {
  background: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  overflow: hidden;
}
.recipe-note {
  margin: 0;
  padding: 12px 14px;
  background: #F0FDFA;
  border-bottom: 1px solid #CCFBF1;
  font-size: 12.5px;
  color: #0F766E;
  line-height: 1.5;
}
.recipe-empty {
  margin: 0;
  padding: 20px;
  text-align: center;
  font-size: 13px;
  color: #94A3B8;
  background: #fff;
  border: 1px dashed #E2E8F0;
  border-radius: 10px;
  line-height: 1.5;
}

/* ═══════════════════════════════════════════════════════════════
   PASO 4 — RESUMEN
   ═══════════════════════════════════════════════════════════════ */
.summary-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.summary-card {
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.summary-wide { grid-column: 1 / -1; }
.summary-card h4 {
  margin: 0 0 4px;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #0F766E;
}
.summary-dl {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.summary-dl > div {
  display: grid;
  grid-template-columns: 110px 1fr;
  gap: 10px;
  font-size: 13px;
}
.summary-dl dt {
  color: #64748B;
  font-weight: 600;
}
.summary-dl dd {
  margin: 0;
  color: #0F172A;
  font-weight: 600;
}
.summary-text {
  margin: 0;
  font-size: 13px;
  color: #334155;
  line-height: 1.55;
}
.summary-text strong { color: #0F172A; }
.summary-list {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: #334155;
  line-height: 1.6;
}
.summary-list li { margin-bottom: 4px; }
.summary-empty {
  margin: 0;
  font-size: 12.5px;
  color: #94A3B8;
  font-style: italic;
}
.summary-totals {
  margin-top: 10px;
  padding-top: 12px;
  border-top: 1px dashed #E2E8F0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.totals-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #334155;
}
.totals-final {
  padding-top: 8px;
  border-top: 1px solid #E2E8F0;
  font-weight: 700;
  font-size: 14.5px;
  color: #0F172A;
}

/* ═══════════════════════════════════════════════════════════════
   WIZARD FOOTER
   ═══════════════════════════════════════════════════════════════ */
.wizard-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #E2E8F0;
  background: #FAFBFC;
  flex-wrap: wrap;
}
.footer-right {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

/* ═══════════════════════════════════════════════════════════════
   SUCCESS
   ═══════════════════════════════════════════════════════════════ */
.success-body {
  padding: 40px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
}
.success-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ECFDF5 0%, #F0FDFA 100%);
  border: 3px solid #10B981;
  color: #059669;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: successPop .5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes successPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.1); }
  100% { transform: scale(1); opacity: 1; }
}
.success-body h2 {
  margin: 6px 0 0;
  font-size: 22px;
  font-weight: 700;
  color: #0F172A;
}
.success-msg {
  margin: 0 0 12px;
  font-size: 13.5px;
  color: #64748B;
  max-width: 520px;
  line-height: 1.55;
}
.success-summary {
  width: 100%;
  max-width: 620px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  text-align: left;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  font-size: 13.5px;
  flex-wrap: wrap;
}
.summary-label { color: #64748B; font-weight: 600; }
.summary-value { color: #0F172A; font-weight: 700; text-align: right; }
.summary-value.mono { font-family: ui-monospace, monospace; font-size: 12.5px; }
.summary-note { font-size: 11px; color: #94A3B8; font-weight: 500; font-style: italic; }
.success-actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

/* ═══════════════════════════════════════════════════════════════
   MODALES
   ═══════════════════════════════════════════════════════════════ */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15,23,42,.55);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 1200;
}
.modal-recipe,
.modal-sm {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0,0,0,.3);
}
.modal-recipe { max-width: 820px; }
.modal-sm { max-width: 460px; }
.modal-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
}
.modal-head h3 {
  margin: 0 0 2px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
}
.modal-sub {
  margin: 0;
  font-size: 12.5px;
  color: #64748B;
}
.modal-close {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  border: 1px solid #E2E8F0;
  background: #fff;
  color: #64748B;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all .15s;
  flex-shrink: 0;
}
.modal-close:hover { background: #F8FAFC; color: #1E293B; }
.modal-body {
  padding: 20px 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.modal-foot {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid #E2E8F0;
  background: #FAFBFC;
}
.modal-text {
  margin: 0;
  font-size: 14px;
  color: #0F172A;
  font-weight: 600;
}
.modal-note {
  margin: 6px 0 0;
  font-size: 12.5px;
  color: #64748B;
  line-height: 1.5;
}
.icon-warn { color: #D97706; }

.recipe-item {
  padding: 14px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 11px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.recipe-item-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.recipe-item-num {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .5px;
  color: #0F766E;
}
.recipe-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

/* ═══════════════════════════════════════════════════════════════
   TRANSICIONES
   ═══════════════════════════════════════════════════════════════ */
.fade-enter-active, .fade-leave-active { transition: opacity .2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-up-enter-active, .slide-up-leave-active {
  transition: all .3s cubic-bezier(0.16, 1, 0.3, 1);
}
.slide-up-enter-from, .slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(.98);
}

/* ═══════════════════════════════════════════════════════════════
   RESPONSIVE
   ═══════════════════════════════════════════════════════════════ */
@media (max-width: 1100px) {
  .paso-layout { grid-template-columns: 1fr; }
  .side-panel { position: static; }
  .readonly-grid { grid-template-columns: 1fr; }
  .vitals-grid { grid-template-columns: repeat(2, 1fr); }
  .summary-grid { grid-template-columns: 1fr; }
  .summary-wide { grid-column: 1; }
}

@media (max-width: 720px) {
  .atencion { padding: 16px 14px 40px; }
  .paso { padding: 18px; }
  .view-header h1 { font-size: 20px; }
  .field-grid-2 { grid-template-columns: 1fr; }
  .vitals-grid { grid-template-columns: 1fr 1fr; }
  .recipe-grid { grid-template-columns: 1fr; }
  .wizard-footer { flex-direction: column-reverse; }
  .wizard-footer .btn, .footer-right { width: 100%; }
  .footer-right { flex-direction: column-reverse; }
  .footer-right .btn { width: 100%; }
  .success-actions { flex-direction: column; }
  .success-actions .btn { width: 100%; }
  .selector-item {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  .selector-item .status-pill { justify-self: start; }
  .selector-item .btn { justify-self: stretch; }
}

@media (max-width: 480px) {
  .vitals-grid { grid-template-columns: 1fr; }
  .summary-dl > div { grid-template-columns: 1fr; gap: 2px; }
}
</style>