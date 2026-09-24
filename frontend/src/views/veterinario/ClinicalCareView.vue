<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  Activity, AlertTriangle, Ban, CalendarDays, CheckCircle2, ChevronLeft, ChevronRight,
  ClipboardList, Download, Eye, History, Inbox, Info, Minus, Package, PawPrint, Phone,
  Pill, Plus, Save, Search, Stethoscope, Syringe, Trash2, User, X,
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

// ══ Paso 1 — Evaluación clínica ══
const form = reactive({
  anamnesis: '', sintomasObservados: '', pesoKg: null, temperaturaC: null,
  frecCardiaca: null, frecRespiratoria: null, diagnosticoPrincipal: '',
  diagnosticosDiferenciales: '', observacionesGenerales: '',
  indicacionesDueno: '', proximaCitaRecomendada: '',
});
const errores = reactive({});

// ══ Paso 2 — Tratamiento e insumos ══
const productos = ref([]);
const cargandoProductos = ref(false);
const busqueda = ref('');
const busquedaAplicada = ref('');
const insumosSeleccionados = ref([]);
const errorStock = ref(null); // { nombre, mensaje } → ALTERNO B (409 de stock)
let temporizadorBusqueda = null;

// ══ Paso 3 — Récipe e indicaciones ══
const VIAS_ADMINISTRACION = ['Oral', 'Intramuscular', 'Subcutánea', 'Intravenosa', 'Tópica', 'Ocular'];
const modalRecetaAbierto = ref(false);
const recetaGenerada = ref(false);
const recetaItems = ref([]);
const recetaIndicaciones = ref('');
const recetaItemsBorrador = ref([]);
const recetaIndicacionesBorrador = ref('');
const erroresReceta = ref([]);

// ══ Alterno A — cancelar ══
const modalCancelarAbierto = ref(false);

// ══ Selector de citas (ruta sin citaId) ══
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
  insumosSeleccionados.value.reduce((suma, i) => suma + Number(i.precioUsd) * Number(i.cantidad), 0));
const ivaEstimado = computed(() => totalInsumos.value * 0.16);
const totalFacturaEstimado = computed(() => totalInsumos.value * 1.16);

const textoInsumosAplicados = computed(() => {
  const val = guardadoExitoso.value?.resumen?.insumosAplicados;
  if (val == null) return 'Ninguno';
  if (Array.isArray(val)) return val.length ? val.join(', ') : 'Ninguno';
  return String(val);
});

const totalInsumosGuardados = computed(() =>
  (atencionGuardada.value?.insumos || []).reduce((suma, i) => suma + subtotalInsumo(i), 0));

function subtotalInsumo(insumo) {
  // El nombre exacto del subtotal se coteja con InsumoDTO; se tolera variante
  // y como última instancia se calcula cantidad × precio unitario.
  return insumo.subtotal ?? insumo.subtotalUsd ?? (insumo.cantidad * insumo.precioUnitarioUsd);
}

const PALETA_AVATARES = ['#0F766E', '#3B82F6', '#F59E0B', '#F43F5E', '#8B5CF6', '#0EA5E9'];
function colorAvatar(nombre) {
  let hash = 0;
  for (const caracter of String(nombre || '')) hash = (hash * 31 + caracter.charCodeAt(0)) % 997;
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

// ══ Ciclo de vida / carga del contexto ══
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
      // La cita ya fue atendida → modo SOLO LECTURA
      modoSoloLectura.value = true;
      return;
    }

    if (contexto.value.cita.estado === 'Confirmada') {
      // Transición Confirmada → En_Atención (idempotente en el backend)
      try {
        await iniciarAtencion(citaId.value);
        contexto.value.cita.estado = 'En_Atencion';
      } catch (errorInicio) {
        errorFatal.value = getApiErrorMessage(errorInicio); // 403 / 409
        return;
      }
    } else if (contexto.value.cita.estado !== 'En_Atencion') {
      errorFatal.value = `La cita no puede atenderse en su estado actual (${contexto.value.cita.estado}).`;
      return;
    }

    // Retomable: la cita En_Atención abre el wizard desde el paso 1
    form.pesoKg = contexto.value.mascota?.pesoActualKg ?? null;
    cargarProductos(true); // catálogo en background para pasos 2 y 3
  } catch (error) {
    errorFatal.value = getApiErrorMessage(error); // 404 "Cita no encontrada: N" | 403
  } finally {
    cargando.value = false;
  }
}

onMounted(inicializar);
// Reutilización del componente al pasar de /atencion → /atencion/:id
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

// ══ Catálogo de insumos (paso 2) ══
async function cargarProductos(silencioso = false) {
  cargandoProductos.value = true;
  try {
    productos.value = await getProductos();
    // Sincroniza stock de lo ya seleccionado (p. ej. tras un 409 de concurrencia)
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

// ══ Navegación del wizard + ALTERNO C ══
function avanzarPaso() {
  if (pasoActual.value === 1 && !validarPaso1()) {
    toastError('Debe completar el diagnóstico y los signos vitales obligatorios para cerrar la historia médica');
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

// ══ Paso 2 — insumos ══
function agregarInsumo(producto) {
  if (producto.stockActual <= 0) {
    // Mensaje EXACTO del CU para el catálogo agotado
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
  let cantidad = Number(item.cantidad);
  if (!Number.isFinite(cantidad) || cantidad < 1) cantidad = 1;
  if (item.stockActual > 0 && cantidad > item.stockActual) cantidad = item.stockActual;
  item.cantidad = cantidad;
}

// ══ Paso 3 — modal de récipe ══
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

function cerrarModalReceta() {
  modalRecetaAbierto.value = false;
}

function agregarItemReceta() {
  recetaItemsBorrador.value.push(nuevoItemReceta());
}

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
    toastError('Complete los campos obligatorios de los medicamentos (dosis, frecuencia y duración)');
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

// ══ ALTERNO A — cancelar ══
function confirmarCancelacion() {
  modalCancelarAbierto.value = false;
  router.push('/veterinario/agenda');
}

// ══ Paso 4 — guardado ══
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
  Object.entries(opcionales).forEach(([clave, valor]) => {
    if (valor !== null && valor !== '') payload[clave] = valor;
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
    toastExito(respuesta.mensaje || 'Atención registrada correctamente'); // mensaje EXACTO del backend
    window.scrollTo({ top: 0, behavior: 'smooth' });
  } catch (error) {
    const mensaje = getApiErrorMessage(error);
    const status = error?.response?.status;

    if (status === 409 && mensaje.includes('Stock insuficiente')) {
      // ALTERNO B — concurrencia de stock: volver al paso 2, marcar el ítem
      // en rojo con el mensaje del server y refrescar el catálogo.
      pasoActual.value = 2;
      errorStock.value = { nombre: mensaje.match(/'([^']+)'/)?.[1] || null, mensaje };
      await cargarProductos(true);
      toastError(mensaje);
    } else if (status === 409 && mensaje.includes('ya fue atendida')) {
      toastError(mensaje);
      await inicializar(); // recarga → modo solo lectura
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

// ══ Descarga de récipe (éxito y solo lectura) ══
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
  router.push({ path: '/veterinario/historiales', query: { mascota: mascota.value?.idMascota, nombre: mascota.value?.nombre } });
}
</script>

<template>
  <div class="atencion">
    <ToastContainer />

    <!-- Carga inicial del contexto -->
    <div v-if="cargando" class="card estado-inicial">
      <div class="spin"></div>
      <p>Cargando el contexto de la cita…</p>
    </div>

    <!-- Error fatal: 404 / 403 / estado inválido -->
    <div v-else-if="errorFatal" class="card estado-inicial estado-error">
      <AlertTriangle :size="40" class="icono-error" />
      <h3>No fue posible abrir la atención</h3>
      <p>{{ errorFatal }}</p>
      <button class="btn-pay" type="button" @click="router.push('/veterinario/agenda')">Volver a mi agenda</button>
    </div>

    <!-- Ruta sin citaId: selector de citas por atender de hoy -->
    <template v-else-if="!citaId">
      <header class="pagina-header">
        <div>
          <h1>Atención clínica</h1>
          <p>Selecciona la cita que deseas atender.</p>
        </div>
      </header>
      <div class="card">
        <div class="card-header">
          <h3>Citas por atender de hoy</h3>
          <button class="btn-link" type="button" @click="cargarCitasDelDia()">Actualizar</button>
        </div>
        <div class="card-body">
          <div v-if="cargandoCitas" class="loading-inline"><div class="spin"></div></div>
          <div v-else-if="!citasPendientes.length" class="empty-state">
            <Inbox :size="40" />
            <p>No tienes citas pendientes por atender hoy.</p>
            <button class="btn-link" type="button" @click="router.push('/veterinario/agenda')">Ir a mi agenda</button>
          </div>
          <div v-else class="appointment-list">
            <article v-for="c in citasPendientes" :key="c.idCita" class="appointment-item">
              <div class="appointment-info">
                <p class="appointment-title">{{ c.mascotaNombre }}</p>
                <p class="appointment-sub">{{ c.especie }} · {{ c.raza }}</p>
                <p class="appointment-sub">Dueño: {{ c.clienteNombre }} — {{ c.motivoConsulta }}</p>
              </div>
              <div class="appointment-side">
                <span class="appointment-time">{{ rangoHora(c.horaInicio, c.horaFin) }}</span>
                <span class="status-pill" :style="estiloEstado(c.estado)">{{ infoEstado(c.estado).etiqueta }}</span>
              </div>
              <button class="btn-pay" type="button" @click="router.push(`/veterinario/atencion/${c.idCita}`)">Atender</button>
            </article>
          </div>
        </div>
      </div>
    </template>

    <!-- MODO SOLO LECTURA: la cita ya fue atendida -->
    <template v-else-if="modoSoloLectura">
      <header class="pagina-header">
        <div>
          <h1>Atención clínica</h1>
          <p>{{ atencionGuardada?.fechaHoraInicio ? fechaHoraCorta(atencionGuardada.fechaHoraInicio) : fechaCompleta(cita?.fechaCita) }} · {{ mascota?.nombre }}</p>
        </div>
        <span class="pill-lectura"><Eye :size="13" /> Consulta en modo solo lectura</span>
      </header>

      <div class="card wizard-card">
        <div class="banner-lectura">
          <AlertTriangle :size="15" />
          <p>Esta cita ya fue atendida. Los datos se muestran en modo consulta y no pueden modificarse.</p>
        </div>

        <div class="lectura-grid">
          <aside class="panel-lateral">
            <div class="panel-bloque">
              <h4><PawPrint :size="14" /> Paciente</h4>
              <p class="panel-principal">{{ mascota?.nombre }}</p>
              <p class="panel-detalle">{{ mascota?.especie }} · {{ mascota?.raza }}</p>
              <p class="panel-detalle">{{ mascota?.sexo }} · {{ mascota?.edad }}</p>
              <p class="panel-detalle">Peso actual: {{ mascota?.pesoActualKg }} kg</p>
            </div>
            <div class="panel-bloque">
              <h4><User :size="14" /> Dueño</h4>
              <p class="panel-principal">{{ dueno?.nombreCompleto }}</p>
              <p class="panel-detalle">{{ dueno?.documentoIdentidad }}</p>
              <p class="panel-detalle"><Phone :size="12" /> {{ dueno?.telefonoPrincipal }}</p>
            </div>
            <div class="panel-bloque">
              <h4><ClipboardList :size="14" /> Cita</h4>
              <p class="panel-detalle"><strong>Motivo:</strong> {{ cita?.motivoConsulta }}</p>
              <p class="panel-detalle"><strong>Servicio:</strong> {{ cita?.servicio }}</p>
              <p class="panel-detalle"><strong>Horario:</strong> {{ rangoHora(cita?.horaInicio, cita?.horaFin) }}</p>
            </div>
          </aside>

          <div class="lectura-contenido">
            <div class="lectura-cabecera">
              <p class="lectura-vet"><Stethoscope :size="14" /> {{ atencionGuardada?.veterinarioNombre }}</p>
              <span class="status-pill" :style="estiloEstado(atencionGuardada?.estadoAtencion)">
                {{ infoEstado(atencionGuardada?.estadoAtencion).etiqueta }}
              </span>
            </div>

            <div class="vitales-grid">
              <div><span>Peso</span><strong>{{ atencionGuardada?.pesoKg }} kg</strong></div>
              <div><span>Temperatura</span><strong>{{ atencionGuardada?.temperaturaC }} °C</strong></div>
              <div><span>Frec. cardíaca</span><strong>{{ atencionGuardada?.frecCardiaca }} lpm</strong></div>
              <div v-if="atencionGuardada?.frecRespiratoria"><span>Frec. respiratoria</span><strong>{{ atencionGuardada.frecRespiratoria }} rpm</strong></div>
            </div>

            <div class="detalle-textos">
              <div class="detalle-bloque"><h5>Anamnesis</h5><p>{{ atencionGuardada?.anamnesis }}</p></div>
              <div v-if="atencionGuardada?.sintomasObservados" class="detalle-bloque"><h5>Hallazgos físicos</h5><p>{{ atencionGuardada.sintomasObservados }}</p></div>
              <div class="detalle-bloque"><h5>Diagnóstico principal</h5><p>{{ atencionGuardada?.diagnosticoPrincipal }}</p></div>
              <div v-if="atencionGuardada?.diagnosticosDiferenciales" class="detalle-bloque"><h5>Diagnósticos diferenciales</h5><p>{{ atencionGuardada.diagnosticosDiferenciales }}</p></div>
              <div v-if="atencionGuardada?.observacionesGenerales" class="detalle-bloque"><h5>Pronóstico / observaciones</h5><p>{{ atencionGuardada.observacionesGenerales }}</p></div>
              <div v-if="atencionGuardada?.indicacionesDueno" class="detalle-bloque"><h5>Indicaciones al dueño</h5><p>{{ atencionGuardada.indicacionesDueno }}</p></div>
              <div v-if="atencionGuardada?.proximaCitaRecomendada" class="detalle-bloque"><h5>Próxima cita recomendada</h5><p>{{ atencionGuardada.proximaCitaRecomendada }}</p></div>
            </div>

            <div v-if="atencionGuardada?.insumos?.length" class="detalle-insumos">
              <h5><Package :size="13" /> Insumos aplicados</h5>
              <table class="data-table tabla-interna">
                <thead>
                  <tr><th>Insumo</th><th class="der">Cant.</th><th class="der">P. unitario</th><th class="der">Subtotal</th></tr>
                </thead>
                <tbody>
                  <tr v-for="i in atencionGuardada.insumos" :key="i.idProducto">
                    <td>{{ i.nombre }} <span class="sku">({{ i.codigoSku }})</span></td>
                    <td class="der">{{ i.cantidad }} {{ i.unidadMedida }}</td>
                    <td class="der">{{ formatoUSD(i.precioUnitarioUsd) }}</td>
                    <td class="der amount">{{ formatoUSD(subtotalInsumo(i)) }}</td>
                  </tr>
                </tbody>
              </table>
              <p class="total-linea"><span>Total insumos</span><span class="amount">{{ formatoUSD(totalInsumosGuardados) }}</span></p>
            </div>

            <div v-if="atencionGuardada?.receta" class="detalle-receta">
              <div class="receta-cabecera-lectura">
                <h5><Pill :size="13" /> Récipe {{ atencionGuardada.receta.codigoReceta }}</h5>
                <button class="btn-pay" type="button" :disabled="descargandoReceta" @click="descargarReceta(atencionGuardada.receta.idReceta, atencionGuardada.receta.codigoReceta)">
                  <Download :size="14" /> {{ descargandoReceta ? 'Descargando…' : 'Descargar Récipe' }}
                </button>
              </div>
              <p v-if="atencionGuardada.receta.indicacionesGenerales" class="receta-indicaciones">
                <strong>Indicaciones generales:</strong> {{ atencionGuardada.receta.indicacionesGenerales }}
              </p>
              <table class="data-table tabla-interna">
                <thead>
                  <tr><th>Medicamento</th><th>Concentración</th><th>Dosis</th><th>Vía</th><th>Frecuencia</th><th>Duración</th></tr>
                </thead>
                <tbody>
                  <tr v-for="(item, i) in atencionGuardada.receta.items" :key="i">
                    <td>{{ item.medicamento }}</td>
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
        </div>

        <div class="lectura-acciones">
          <button class="btn-outline" type="button" @click="verHistorialCompleto()">Ver historial completo</button>
          <button class="btn-outline" type="button" @click="router.push('/veterinario/agenda')">Volver a agenda</button>
        </div>
      </div>
    </template>

    <!-- ÉXITO tras guardar -->
    <div v-else-if="guardadoExitoso" class="card wizard-card">
      <div class="exito-cuerpo">
        <div class="exito-icono"><CheckCircle2 :size="40" /></div>
        <h2>Atención guardada</h2>
        <p class="exito-mensaje">{{ guardadoExitoso.mensaje }}</p>

        <div class="exito-resumen">
          <div><span>Paciente</span><strong>{{ guardadoExitoso.resumen?.mascota }}</strong></div>
          <div><span>Diagnóstico</span><strong>{{ guardadoExitoso.resumen?.diagnostico }}</strong></div>
          <div><span>Insumos aplicados</span><strong>{{ textoInsumosAplicados }}</strong></div>
          <div><span>Total insumos</span><strong class="amount">{{ formatoUSD(guardadoExitoso.resumen?.totalInsumosUsd) }}</strong></div>
          <div><span>Estado de la cita</span><strong>{{ guardadoExitoso.resumen?.estadoCita }}</strong></div>
          <div v-if="guardadoExitoso.codigoReceta"><span>Código del récipe</span><strong>{{ guardadoExitoso.codigoReceta }}</strong></div>
          <div v-if="guardadoExitoso.idFacturaProductos">
            <span>Factura de productos</span>
            <strong>#{{ guardadoExitoso.idFacturaProductos }} · {{ formatoUSD(guardadoExitoso.totalFacturaProductos) }} (por cobrar en mostrador)</strong>
          </div>
        </div>

        <div class="exito-acciones">
          <button v-if="guardadoExitoso.idReceta" class="btn-pay" type="button" :disabled="descargandoReceta"
            @click="descargarReceta(guardadoExitoso.idReceta, guardadoExitoso.codigoReceta)">
            <Download :size="15" /> {{ descargandoReceta ? 'Descargando…' : 'Descargar Récipe' }}
          </button>
          <button class="btn-outline" type="button" @click="router.push('/veterinario/agenda')">
            <CalendarDays :size="15" /> Volver a agenda
          </button>
        </div>
      </div>
    </div>

    <!-- WIZARD -->
    <template v-else>
      <header class="pagina-header">
        <div>
          <h1>Atención clínica</h1>
          <p>{{ mascota?.nombre }} · {{ cita?.servicio }} · {{ fechaCompleta(cita?.fechaCita) }}</p>
        </div>
        <button class="btn-outline" type="button" @click="modalCancelarAbierto = true"><Ban :size="15" /> Cancelar</button>
      </header>

      <div class="card wizard-card">
        <div class="paciente-strip">
          <div class="pet-avatar" :style="{ backgroundColor: colorAvatar(mascota?.nombre) }">{{ inicialNombre(mascota?.nombre) }}</div>
          <div class="paciente-datos">
            <p class="paciente-nombre">{{ mascota?.nombre }}</p>
            <p class="paciente-sub">{{ mascota?.especie }} · {{ mascota?.raza }} · {{ dueno?.nombreCompleto }}</p>
          </div>
          <span class="status-pill" :style="estiloEstado(cita?.estado)">{{ infoEstado(cita?.estado).etiqueta }}</span>
        </div>

        <nav class="stepper" aria-label="Progreso de la atención">
          <template v-for="(p, idx) in pasos" :key="p.numero">
            <button type="button" class="step"
              :class="{ 'step--activo': pasoActual === p.numero, 'step--hecho': pasoActual > p.numero }"
              :disabled="p.numero >= pasoActual" @click="pasoActual = p.numero">
              <span class="step-circulo">
                <CheckCircle2 v-if="pasoActual > p.numero" :size="15" />
                <template v-else>{{ p.numero }}</template>
              </span>
              <span class="step-titulo">{{ p.titulo }}</span>
            </button>
            <div v-if="idx < pasos.length - 1" class="step-conector" :class="{ 'step-conector--lleno': pasoActual > p.numero }"></div>
          </template>
        </nav>

        <!-- PASO 1 — Evaluación clínica -->
        <div v-show="pasoActual === 1" class="paso paso-dos-columnas">
          <div class="paso-form">
            <div class="campo">
              <label class="form-label" for="anamnesis">Anamnesis <span class="req">*</span></label>
              <textarea id="anamnesis" v-model="form.anamnesis" rows="3" class="form-textarea"
                :class="{ 'input-error': errores.anamnesis }"
                placeholder="Relato del dueño: evolución, síntomas, tiempo de enfermedad, apetito…"></textarea>
              <p v-if="errores.anamnesis" class="mensaje-error">{{ errores.anamnesis }}</p>
            </div>

            <div class="campos-par">
              <div class="campo">
                <label class="form-label" for="hallazgos">Hallazgos físicos (síntomas observados)</label>
                <textarea id="hallazgos" v-model="form.sintomasObservados" rows="3" class="form-textarea"
                  placeholder="Ej.: mucosas pálidas, abdomen tenso…"></textarea>
              </div>
              <div class="campo">
                <label class="form-label" for="pronostico">Pronóstico / observaciones generales</label>
                <textarea id="pronostico" v-model="form.observacionesGenerales" rows="3" class="form-textarea"
                  placeholder="Ej.: pronóstico reservado, se reevalúa en 7 días…"></textarea>
              </div>
            </div>

            <div class="vitales-form">
              <div class="campo">
                <label class="form-label" for="peso">Peso (kg) <span class="req">*</span></label>
                <input id="peso" v-model.number="form.pesoKg" type="number" step="0.01" min="0"
                  class="form-input" :class="{ 'input-error': errores.pesoKg }" placeholder="0.00" />
                <p v-if="errores.pesoKg" class="mensaje-error">{{ errores.pesoKg }}</p>
              </div>
              <div class="campo">
                <label class="form-label" for="temperatura">Temperatura (°C) <span class="req">*</span></label>
                <input id="temperatura" v-model.number="form.temperaturaC" type="number" step="0.1" min="0"
                  class="form-input" :class="{ 'input-error': errores.temperaturaC }" placeholder="0.0" />
                <p v-if="errores.temperaturaC" class="mensaje-error">{{ errores.temperaturaC }}</p>
              </div>
              <div class="campo">
                <label class="form-label" for="cardiaca">Frec. cardíaca (lpm) <span class="req">*</span></label>
                <input id="cardiaca" v-model.number="form.frecCardiaca" type="number" step="1" min="0"
                  class="form-input" :class="{ 'input-error': errores.frecCardiaca }" placeholder="0" />
                <p v-if="errores.frecCardiaca" class="mensaje-error">{{ errores.frecCardiaca }}</p>
              </div>
              <div class="campo">
                <label class="form-label" for="respiratoria">Frec. respiratoria (rpm)</label>
                <input id="respiratoria" v-model.number="form.frecRespiratoria" type="number" step="1" min="0"
                  class="form-input" :class="{ 'input-error': errores.frecRespiratoria }" placeholder="Opcional" />
                <p v-if="errores.frecRespiratoria" class="mensaje-error">{{ errores.frecRespiratoria }}</p>
              </div>
            </div>

            <div class="campo">
              <label class="form-label" for="diagnostico">Diagnóstico principal <span class="req">*</span></label>
              <textarea id="diagnostico" v-model="form.diagnosticoPrincipal" rows="2" class="form-textarea"
                :class="{ 'input-error': errores.diagnosticoPrincipal }"
                placeholder="Diagnóstico presuntivo o definitivo…"></textarea>
              <p v-if="errores.diagnosticoPrincipal" class="mensaje-error">{{ errores.diagnosticoPrincipal }}</p>
            </div>

            <div class="campos-par">
              <div class="campo">
                <label class="form-label" for="diferenciales">Diagnósticos diferenciales</label>
                <textarea id="diferenciales" v-model="form.diagnosticosDiferenciales" rows="2" class="form-textarea" placeholder="Opcional…"></textarea>
              </div>
              <div class="campo">
                <label class="form-label" for="proxima">Próxima cita recomendada</label>
                <input id="proxima" v-model="form.proximaCitaRecomendada" type="date" class="form-input" />
              </div>
            </div>
          </div>

          <aside class="panel-lateral">
            <div class="panel-bloque">
              <h4><PawPrint :size="14" /> Paciente</h4>
              <p class="panel-principal">{{ mascota?.nombre }}</p>
              <p class="panel-detalle">{{ mascota?.especie }} · {{ mascota?.raza }}</p>
              <p class="panel-detalle">{{ mascota?.sexo }} · {{ mascota?.edad }} · Peso actual: {{ mascota?.pesoActualKg }} kg</p>
              <span v-if="contexto?.primeraVez" class="pill-nueva">Primera consulta</span>
            </div>
            <div class="panel-bloque">
              <h4><User :size="14" /> Dueño</h4>
              <p class="panel-principal">{{ dueno?.nombreCompleto }}</p>
              <p class="panel-detalle">{{ dueno?.documentoIdentidad }}</p>
              <p class="panel-detalle"><Phone :size="12" /> {{ dueno?.telefonoPrincipal }}</p>
            </div>
            <div class="panel-bloque">
              <h4><ClipboardList :size="14" /> Cita</h4>
              <p class="panel-detalle"><strong>Motivo:</strong> {{ cita?.motivoConsulta }}</p>
              <p class="panel-detalle"><strong>Servicio:</strong> {{ cita?.servicio }}</p>
              <p class="panel-detalle"><strong>Tipo:</strong> {{ cita?.tipoAtencion }}</p>
              <p class="panel-detalle"><strong>Horario:</strong> {{ rangoHora(cita?.horaInicio, cita?.horaFin) }}</p>
            </div>
            <div v-if="ultimaAtencion" class="panel-bloque">
              <h4><History :size="14" /> Última atención</h4>
              <p class="panel-detalle"><strong>Fecha:</strong> {{ fechaHoraCorta(ultimaAtencion.fecha) }}</p>
              <p class="panel-detalle"><strong>Dx:</strong> {{ ultimaAtencion.diagnostico }}</p>
              <p class="panel-detalle">Peso {{ ultimaAtencion.pesoKg }} kg · {{ ultimaAtencion.temperaturaC }} °C · FC {{ ultimaAtencion.frecCardiaca }}</p>
            </div>
          </aside>
        </div>

        <!-- PASO 2 — Tratamiento e insumos -->
        <div v-show="pasoActual === 2" class="paso paso-dos-columnas">
          <div>
            <div class="buscador">
              <Search :size="16" class="buscador-icono" />
              <input v-model="busqueda" class="form-input buscador-input"
                placeholder="Buscar insumo por nombre, código o categoría…" />
              <button v-if="busqueda" class="buscador-limpiar" type="button" @click="busqueda = ''"><X :size="14" /></button>
            </div>

            <div v-if="cargandoProductos" class="loading-inline"><div class="spin"></div></div>
            <div v-else-if="!productos.length" class="empty-state">
              <Package :size="40" />
              <p>No fue posible cargar el catálogo de insumos.</p>
              <button class="btn-link" type="button" @click="cargarProductos()">Reintentar</button>
            </div>
            <div v-else-if="!productosFiltrados.length" class="empty-state">
              <Search :size="40" />
              <p>Sin resultados para «{{ busquedaAplicada }}».</p>
            </div>
            <div v-else class="productos-lista">
              <article v-for="p in productosFiltrados" :key="p.id" class="producto-item"
                :class="{ 'producto-agotado': p.stockActual <= 0 }" @click="agregarInsumo(p)">
                <div class="producto-info">
                  <p class="producto-nombre">
                    {{ p.nombre }}
                    <span v-if="p.requiereReceta" class="pill-receta">Requiere receta</span>
                  </p>
                  <p class="producto-meta">{{ p.codigoSku }} · {{ p.tipoCategoria }} · {{ p.presentacion }}</p>
                </div>
                <div class="producto-lado">
                  <span class="amount">{{ formatoUSD(p.precioUsd) }}</span>
                  <span class="producto-stock" :class="{ 'stock-cero': p.stockActual <= 0 }">
                    {{ p.stockActual > 0 ? `Stock: ${p.stockActual}` : 'Agotado' }}
                  </span>
                </div>
                <button class="btn-agregar" type="button" :disabled="p.stockActual <= 0"
                  :title="p.stockActual <= 0 ? 'Producto agotado' : 'Agregar a la atención'"
                  @click.stop="agregarInsumo(p)">
                  <Plus :size="16" />
                </button>
              </article>
            </div>
            <p class="nota-insumos">Los insumos se descuentan del almacén al guardar la atención y generan una factura de productos (insumos + IVA 16%) por cobrar en el mostrador.</p>
          </div>

          <aside class="panel-lateral">
            <h4 class="panel-titulo">
              <Syringe :size="14" /> Insumos aplicados
              <span v-if="insumosSeleccionados.length" class="contador">{{ insumosSeleccionados.length }}</span>
            </h4>

            <p v-if="errorStock" class="alerta-stock"><AlertTriangle :size="14" /> {{ errorStock.mensaje }}</p>

            <div v-if="!insumosSeleccionados.length" class="panel-vacio">
              <Syringe :size="28" />
              <p>Aún no ha aplicado insumos. Selecciónelos del catálogo.</p>
            </div>

            <div v-else class="insumos-aplicados">
              <div v-for="item in insumosSeleccionados" :key="item.idProducto" class="insumo-item"
                :class="{ 'insumo-error': errorStock && errorStock.nombre === item.nombre }">
                <div class="insumo-cabecera">
                  <div>
                    <p class="insumo-nombre">{{ item.nombre }}</p>
                    <p class="insumo-meta">{{ formatoUSD(item.precioUsd) }} / {{ item.unidadMedida }} · Stock: {{ item.stockActual }}</p>
                  </div>
                  <button class="btn-icono-peligro" type="button" title="Quitar insumo" @click="quitarInsumo(item)">
                    <Trash2 :size="14" />
                  </button>
                </div>
                <div class="insumo-controles">
                  <div class="contador-cantidad">
                    <button type="button" :disabled="item.cantidad <= 1" @click="cambiarCantidad(item, -1)"><Minus :size="13" /></button>
                    <input v-model.number="item.cantidad" type="number" min="1" :max="item.stockActual" @change="normalizarCantidad(item)" />
                    <button type="button" :disabled="item.cantidad >= item.stockActual" @click="cambiarCantidad(item, 1)"><Plus :size="13" /></button>
                  </div>
                  <span class="amount">{{ formatoUSD(item.precioUsd * item.cantidad) }}</span>
                </div>
                <p v-if="errorStock && errorStock.nombre === item.nombre" class="mensaje-error">{{ errorStock.mensaje }}</p>
              </div>
              <div class="insumos-total">
                <span>Total insumos (consumo temporal)</span>
                <span class="amount">{{ formatoUSD(totalInsumos) }}</span>
              </div>
            </div>
          </aside>
        </div>

        <!-- PASO 3 — Récipe e indicaciones -->
        <div v-show="pasoActual === 3" class="paso">
          <div class="campo">
            <label class="form-label" for="indicaciones">Indicaciones para el dueño</label>
            <textarea id="indicaciones" v-model="form.indicacionesDueno" rows="4" class="form-textarea"
              placeholder="Recomendaciones, cuidados y próximos pasos para el dueño en casa…"></textarea>
          </div>

          <div class="receta-bloque">
            <div class="receta-cabecera">
              <div>
                <h4><Pill :size="15" /> Récipe médico <span class="opcional">opcional</span></h4>
                <p>Genere el récipe de la consulta; se anexará a la atención al guardar.</p>
              </div>
              <button v-if="!recetaGenerada" class="btn-pay" type="button" @click="abrirModalReceta">
                <Plus :size="15" /> Generar Récipe
              </button>
              <div v-else class="receta-acciones">
                <button class="btn-outline" type="button" @click="abrirModalReceta">Editar</button>
                <button class="btn-outline btn-peligro" type="button" @click="descartarReceta"><Trash2 :size="14" /> Descartar</button>
              </div>
            </div>

            <div v-if="recetaGenerada" class="receta-vista">
              <p v-if="recetaIndicaciones" class="receta-indicaciones"><strong>Indicaciones generales:</strong> {{ recetaIndicaciones }}</p>
              <div class="table-wrap">
                <table class="data-table tabla-interna">
                  <thead>
                    <tr><th>Medicamento</th><th>Concentración</th><th>Dosis</th><th>Vía</th><th>Frecuencia</th><th>Duración</th></tr>
                  </thead>
                  <tbody>
                    <tr v-for="(item, i) in recetaItems" :key="i">
                      <td>{{ item.medicamento }}</td>
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
            <p v-else class="receta-vacia">La consulta puede guardarse sin récipe. Si el tratamiento lo requiere, genérelo antes de guardar.</p>
          </div>
        </div>

        <!-- PASO 4 — Revisión y guardado -->
        <div v-show="pasoActual === 4" class="paso">
          <div class="resumen-grid">
            <section class="resumen-bloque">
              <h4><PawPrint :size="14" /> Paciente y consulta</h4>
              <dl class="resumen-datos">
                <div><dt>Paciente</dt><dd>{{ mascota?.nombre }} ({{ mascota?.especie }} · {{ mascota?.raza }})</dd></div>
                <div><dt>Dueño</dt><dd>{{ dueno?.nombreCompleto }}</dd></div>
                <div><dt>Motivo</dt><dd>{{ cita?.motivoConsulta }}</dd></div>
                <div><dt>Servicio</dt><dd>{{ cita?.servicio }}</dd></div>
              </dl>
            </section>

            <section class="resumen-bloque">
              <h4><Activity :size="14" /> Signos vitales</h4>
              <dl class="resumen-datos">
                <div><dt>Peso</dt><dd>{{ form.pesoKg }} kg</dd></div>
                <div><dt>Temperatura</dt><dd>{{ form.temperaturaC }} °C</dd></div>
                <div><dt>Frec. cardíaca</dt><dd>{{ form.frecCardiaca }} lpm</dd></div>
                <div v-if="form.frecRespiratoria !== null && form.frecRespiratoria !== ''"><dt>Frec. respiratoria</dt><dd>{{ form.frecRespiratoria }} rpm</dd></div>
              </dl>
            </section>

            <section class="resumen-bloque resumen-diagnostico">
              <h4><ClipboardList :size="14" /> Diagnóstico y anamnesis</h4>
              <p class="resumen-texto"><strong>Diagnóstico principal:</strong> {{ form.diagnosticoPrincipal }}</p>
              <p class="resumen-texto"><strong>Anamnesis:</strong> {{ form.anamnesis }}</p>
              <p v-if="form.sintomasObservados" class="resumen-texto"><strong>Hallazgos:</strong> {{ form.sintomasObservados }}</p>
              <p v-if="form.diagnosticosDiferenciales" class="resumen-texto"><strong>Dx. diferenciales:</strong> {{ form.diagnosticosDiferenciales }}</p>
              <p v-if="form.observacionesGenerales" class="resumen-texto"><strong>Pronóstico:</strong> {{ form.observacionesGenerales }}</p>
              <p v-if="form.proximaCitaRecomendada" class="resumen-texto"><strong>Próxima cita recomendada:</strong> {{ form.proximaCitaRecomendada }}</p>
            </section>

            <section class="resumen-bloque">
              <h4><Package :size="14" /> Insumos y factura estimada</h4>
              <table v-if="insumosSeleccionados.length" class="data-table tabla-interna">
                <thead>
                  <tr><th>Insumo</th><th class="der">Cant.</th><th class="der">P. unitario</th><th class="der">Subtotal</th></tr>
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
              <p v-else class="resumen-vacio">Sin insumos aplicados.</p>
              <div class="totales-factura">
                <div><span>Subtotal</span><span>{{ formatoUSD(totalInsumos) }}</span></div>
                <div><span>IVA (16%)</span><span>{{ formatoUSD(ivaEstimado) }}</span></div>
                <div class="total-final"><span>Total estimado de la factura</span><span class="amount">{{ formatoUSD(totalFacturaEstimado) }}</span></div>
              </div>
              <p class="nota-insumos">Monto estimado; la factura definitiva la emite el backend al guardar la atención.</p>
            </section>

            <section class="resumen-bloque">
              <h4><Pill :size="14" /> Récipe</h4>
              <template v-if="recetaGenerada">
                <p v-if="recetaIndicaciones" class="resumen-texto"><strong>Indicaciones generales:</strong> {{ recetaIndicaciones }}</p>
                <ul class="resumen-receta">
                  <li v-for="(item, i) in recetaItems" :key="i">
                    {{ item.medicamento }}{{ item.concentracion ? ` (${item.concentracion})` : '' }} —
                    {{ item.dosis }}, vía {{ item.viaAdministracion }}, {{ item.frecuencia }}, {{ item.duracion }}
                  </li>
                </ul>
              </template>
              <p v-else class="resumen-vacio">Sin récipe para esta consulta.</p>
            </section>

            <section class="resumen-bloque">
              <h4><Info :size="14" /> Indicaciones para el dueño</h4>
              <p class="resumen-texto">{{ form.indicacionesDueno || 'Sin indicaciones registradas.' }}</p>
            </section>
          </div>
        </div>

        <footer class="wizard-footer">
          <button v-if="pasoActual > 1" class="btn-outline" type="button" @click="retrocederPaso">
            <ChevronLeft :size="15" /> Anterior
          </button>
          <span v-else></span>
          <div class="footer-derecha">
            <button v-if="pasoActual < 4" class="btn-pay" type="button" @click="avanzarPaso">
              Siguiente <ChevronRight :size="15" />
            </button>
            <button v-else class="btn-pay" type="button" :disabled="guardando" @click="guardarConsulta">
              <div v-if="guardando" class="spin spin-chica"></div>
              <Save v-else :size="15" />
              {{ guardando ? 'Guardando…' : 'Guardar atención' }}
            </button>
          </div>
        </footer>
      </div>

      <!-- MODAL: vista previa del récipe -->
      <Teleport to="body">
        <div v-if="modalRecetaAbierto" class="modal-overlay" @click.self="cerrarModalReceta">
          <div class="modal modal-receta" role="dialog" aria-modal="true">
            <div class="modal-header">
              <div>
                <h3><Pill :size="17" /> Vista previa del récipe</h3>
                <p class="modal-sub">{{ mascota?.nombre }} · {{ fechaCompleta(cita?.fechaCita) }}</p>
              </div>
              <button class="btn-cerrar" type="button" @click="cerrarModalReceta"><X :size="16" /></button>
            </div>
            <div class="modal-body">
              <div class="campo">
                <label class="form-label" for="receta-indicaciones">Indicaciones generales del récipe</label>
                <textarea id="receta-indicaciones" v-model="recetaIndicacionesBorrador" rows="2" class="form-textarea"
                  placeholder="Ej.: administrar con los alimentos…"></textarea>
              </div>

              <div v-for="(item, i) in recetaItemsBorrador" :key="i" class="receta-item-modal">
                <div class="receta-item-cabecera">
                  <span class="receta-item-numero">Medicamento {{ i + 1 }}</span>
                  <button class="btn-icono-peligro" type="button" :disabled="recetaItemsBorrador.length <= 1"
                    title="Quitar medicamento" @click="quitarItemReceta(i)"><Trash2 :size="14" /></button>
                </div>
                <div class="receta-item-grid">
                  <div class="campo" :class="{ 'campo-error': erroresReceta[i]?.medicamento }">
                    <label class="form-label">Medicamento <span class="req">*</span></label>
                    <input v-model="item.medicamento" class="form-input" list="productos-catalogo"
                      placeholder="Texto libre o del catálogo" />
                    <p v-if="erroresReceta[i]?.medicamento" class="mensaje-error">{{ erroresReceta[i].medicamento }}</p>
                  </div>
                  <div class="campo">
                    <label class="form-label">Concentración</label>
                    <input v-model="item.concentracion" class="form-input" placeholder="Ej.: 500 mg" />
                  </div>
                  <div class="campo" :class="{ 'campo-error': erroresReceta[i]?.dosis }">
                    <label class="form-label">Dosis <span class="req">*</span></label>
                    <input v-model="item.dosis" class="form-input" placeholder="Ej.: 1 tableta" />
                    <p v-if="erroresReceta[i]?.dosis" class="mensaje-error">{{ erroresReceta[i].dosis }}</p>
                  </div>
                  <div class="campo">
                    <label class="form-label">Vía de administración</label>
                    <select v-model="item.viaAdministracion" class="form-select">
                      <option v-for="via in VIAS_ADMINISTRACION" :key="via" :value="via">{{ via }}</option>
                    </select>
                  </div>
                  <div class="campo" :class="{ 'campo-error': erroresReceta[i]?.frecuencia }">
                    <label class="form-label">Frecuencia <span class="req">*</span></label>
                    <input v-model="item.frecuencia" class="form-input" placeholder="Ej.: cada 12 horas" />
                    <p v-if="erroresReceta[i]?.frecuencia" class="mensaje-error">{{ erroresReceta[i].frecuencia }}</p>
                  </div>
                  <div class="campo" :class="{ 'campo-error': erroresReceta[i]?.duracion }">
                    <label class="form-label">Duración <span class="req">*</span></label>
                    <input v-model="item.duracion" class="form-input" placeholder="Ej.: 7 días" />
                    <p v-if="erroresReceta[i]?.duracion" class="mensaje-error">{{ erroresReceta[i].duracion }}</p>
                  </div>
                </div>
              </div>

              <button class="btn-outline btn-agregar-item" type="button" @click="agregarItemReceta">
                <Plus :size="14" /> Agregar medicamento
              </button>

              <datalist id="productos-catalogo">
                <option v-for="p in productos" :key="p.id" :value="p.nombre" />
              </datalist>
            </div>
            <div class="modal-footer">
              <button class="btn-outline" type="button" @click="cerrarModalReceta">Cancelar</button>
              <button class="btn-pay" type="button" @click="confirmarReceta"><CheckCircle2 :size="15" /> Confirmar récipe</button>
            </div>
          </div>
        </div>
      </Teleport>

      <!-- MODAL: cancelar consulta (ALTERNO A) -->
      <Teleport to="body">
        <div v-if="modalCancelarAbierto" class="modal-overlay" @click.self="modalCancelarAbierto = false">
          <div class="modal modal-chica" role="dialog" aria-modal="true">
            <div class="modal-header">
              <h3><AlertTriangle :size="17" class="icono-alerta" /> ¿Descartar la consulta?</h3>
              <button class="btn-cerrar" type="button" @click="modalCancelarAbierto = false"><X :size="16" /></button>
            </div>
            <div class="modal-body">
              <p class="modal-texto">Los datos no guardados se perderán</p>
              <p class="modal-nota">La cita permanecerá En Atención y podrá retomarla desde su agenda.</p>
            </div>
            <div class="modal-footer">
              <button class="btn-outline" type="button" @click="modalCancelarAbierto = false">Seguir editando</button>
              <button class="btn-peligro-solido" type="button" @click="confirmarCancelacion">Descartar y volver</button>
            </div>
          </div>
        </div>
      </Teleport>
    </template>
  </div>
</template>

<style scoped>
.atencion {
  max-width: 1400px; margin: 0 auto; padding: 28px 24px 48px;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; color: #1E293B;
}
button { font-family: inherit; }

.pagina-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; flex-wrap: wrap; margin-bottom: 20px; }
.pagina-header h1 { margin: 0 0 4px; font-size: 22px; font-weight: 700; }
.pagina-header p { margin: 0; font-size: 14px; color: #64748B; }

.card { background: #fff; border: 1px solid #E2E8F0; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,.03), 0 10px 15px -3px rgba(0,0,0,.05); }
.card-header { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 20px 24px; border-bottom: 1px solid #E2E8F0; }
.card-header h3 { margin: 0; font-size: 16px; font-weight: 600; }
.card-body { padding: 24px; }

/* Formulario CLAVE: card con border-top teal */
.wizard-card { border-top: 4px solid #0F766E; }

.estado-inicial { padding: 24px; display: flex; flex-direction: column; align-items: center; gap: 12px; color: #64748B; font-size: 14px; }
.estado-error { text-align: center; padding: 48px 24px; }
.estado-error h3 { margin: 12px 0 4px; font-size: 16px; color: #1E293B; }
.estado-error p { margin: 0 0 18px; color: #64748B; font-size: 14px; }
.icono-error { color: #F43F5E; }
.icono-alerta { color: #D97706; }

/* Paciente strip */
.paciente-strip { display: flex; align-items: center; gap: 14px; padding: 16px 24px; border-bottom: 1px solid #E2E8F0; flex-wrap: wrap; }
.pet-avatar { width: 44px; height: 44px; border-radius: 10px; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 18px; font-weight: 700; flex-shrink: 0; }
.paciente-nombre { margin: 0; font-size: 15px; font-weight: 700; }
.paciente-sub { margin: 2px 0 0; font-size: 12.5px; color: #64748B; }
.paciente-strip .status-pill { margin-left: auto; }

/* Stepper */
.stepper { display: flex; align-items: center; gap: 8px; padding: 20px 24px; border-bottom: 1px solid #E2E8F0; overflow-x: auto; }
.step { display: flex; align-items: center; gap: 10px; background: none; border: none; padding: 4px; cursor: pointer; font-family: inherit; }
.step:disabled { cursor: default; }
.step-circulo {
  width: 30px; height: 30px; border-radius: 50%; border: 2px solid #E2E8F0; background: #fff;
  color: #94A3B8; display: flex; align-items: center; justify-content: center;
  font-size: 13px; font-weight: 700; flex-shrink: 0; transition: all .2s ease;
}
.step-titulo { font-size: 13px; font-weight: 600; color: #94A3B8; white-space: nowrap; transition: color .2s ease; }
.step--activo .step-circulo { border-color: #0F766E; color: #0F766E; box-shadow: 0 0 0 4px rgba(15,118,110,.12); }
.step--activo .step-titulo { color: #0F766E; }
.step--hecho .step-circulo { border-color: #0F766E; background: #0F766E; color: #fff; }
.step--hecho .step-titulo { color: #334155; }
.step-conector { flex: 1; min-width: 24px; height: 2px; background: #E2E8F0; }
.step-conector--lleno { background: #0F766E; }

/* Pasos */
.paso { padding: 24px; }
.paso-dos-columnas { display: grid; grid-template-columns: minmax(0, 1fr) 340px; gap: 24px; align-items: start; }
.campo { margin-bottom: 16px; }
.campos-par { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.vitales-form { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 4px; }

/* Formularios */
.form-label { display: block; margin-bottom: 6px; font-size: 13px; font-weight: 600; color: #334155; }
.req { color: #F43F5E; }
.form-input, .form-select, .form-textarea {
  width: 100%; background: #F8FAFC; border: 1.5px solid #E2E8F0; border-radius: 8px;
  padding: 9px 12px; font-size: 14px; color: #1E293B; font-family: inherit; transition: all .2s ease;
}
.form-textarea { resize: vertical; }
.form-input:focus, .form-select:focus, .form-textarea:focus { outline: none; border-color: #0F766E; background: #fff; box-shadow: 0 0 0 3px rgba(15,118,110,.1); }
.input-error { border-color: #F43F5E !important; }
.mensaje-error { margin: 4px 0 0; font-size: 12px; color: #EF4444; }

/* Panel lateral */
.panel-lateral { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 18px; display: flex; flex-direction: column; gap: 18px; }
.panel-bloque h4, .panel-titulo { margin: 0 0 10px; font-size: 14px; font-weight: 600; display: flex; align-items: center; gap: 7px; color: #0F766E; }
.panel-principal { margin: 0; font-size: 15px; font-weight: 700; }
.panel-detalle { margin: 3px 0 0; font-size: 13px; color: #64748B; display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.pill-nueva { display: inline-flex; margin-top: 8px; padding: 3px 10px; border-radius: 20px; font-size: 11px; font-weight: 700; background: #ECFDF5; color: #059669; border: 1px solid #A7F3D0; }
.contador { background: #0F766E; color: #fff; border-radius: 20px; font-size: 11px; min-width: 20px; height: 20px; display: inline-flex; align-items: center; justify-content: center; padding: 0 6px; margin-left: auto; }
.panel-vacio { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #94A3B8; text-align: center; padding: 24px 12px; font-size: 13px; }
.alerta-stock { display: flex; align-items: flex-start; gap: 8px; background: #FEF2F2; border: 1px solid #FECACA; color: #EF4444; border-radius: 8px; padding: 10px 12px; font-size: 12.5px; font-weight: 600; margin: 0 0 4px; }

/* Catálogo paso 2 */
.buscador { position: relative; margin-bottom: 16px; }
.buscador-icono { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94A3B8; }
.buscador-input { padding-left: 38px; padding-right: 38px; }
.buscador-limpiar { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: none; border: none; color: #94A3B8; cursor: pointer; display: flex; padding: 4px; }
.buscador-limpiar:hover { color: #64748B; }
.productos-lista { display: flex; flex-direction: column; gap: 10px; max-height: 480px; overflow-y: auto; padding-right: 4px; }
.producto-item { display: flex; align-items: center; gap: 12px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 12px 14px; cursor: pointer; transition: all .2s ease; }
.producto-item:hover { border-color: #CBD5E1; background: #fff; }
.producto-agotado { opacity: .65; }
.producto-info { flex: 1; min-width: 0; }
.producto-nombre { margin: 0; font-size: 14px; font-weight: 600; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.producto-meta { margin: 3px 0 0; font-size: 12px; color: #64748B; }
.producto-lado { display: flex; flex-direction: column; gap: 4px; align-items: flex-end; text-align: right; }
.producto-stock { font-size: 12px; color: #64748B; font-weight: 600; }
.stock-cero { color: #EF4444; }
.btn-agregar { width: 32px; height: 32px; border-radius: 8px; border: 1px solid #E2E8F0; background: #fff; color: #0F766E; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all .2s ease; flex-shrink: 0; }
.btn-agregar:hover:not(:disabled) { background: #0F766E; color: #fff; border-color: #0F766E; }
.btn-agregar:disabled { color: #CBD5E1; cursor: not-allowed; }
.pill-receta { display: inline-flex; padding: 2px 8px; border-radius: 20px; font-size: 10.5px; font-weight: 700; background: #FFFBEB; color: #D97706; border: 1px solid #FDE68A; text-transform: uppercase; letter-spacing: .3px; }
.nota-insumos { margin: 12px 0 0; font-size: 12px; color: #94A3B8; }

/* Insumos aplicados */
.insumos-aplicados { display: flex; flex-direction: column; gap: 12px; }
.insumo-item { background: #fff; border: 1px solid #E2E8F0; border-radius: 10px; padding: 12px; transition: all .2s ease; }
.insumo-error { border-color: #F43F5E; box-shadow: 0 0 0 3px rgba(244,63,94,.12); }
.insumo-cabecera { display: flex; align-items: flex-start; justify-content: space-between; gap: 8px; }
.insumo-nombre { margin: 0; font-size: 13.5px; font-weight: 600; }
.insumo-meta { margin: 2px 0 0; font-size: 12px; color: #64748B; }
.insumo-controles { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; }
.contador-cantidad { display: flex; align-items: center; border: 1px solid #E2E8F0; border-radius: 8px; overflow: hidden; background: #fff; }
.contador-cantidad button { width: 28px; height: 30px; background: #F8FAFC; border: none; color: #64748B; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all .2s ease; }
.contador-cantidad button:hover:not(:disabled) { background: #0F766E; color: #fff; }
.contador-cantidad button:disabled { color: #CBD5E1; cursor: not-allowed; }
.contador-cantidad input { width: 46px; height: 30px; border: none; text-align: center; font-size: 13px; font-weight: 600; color: #1E293B; font-family: inherit; background: #fff; }
.contador-cantidad input:focus { outline: none; }
.btn-icono-peligro { background: none; border: none; color: #94A3B8; cursor: pointer; padding: 4px; border-radius: 6px; display: flex; transition: all .2s ease; }
.btn-icono-peligro:hover:not(:disabled) { color: #EF4444; background: #FEF2F2; }
.btn-icono-peligro:disabled { opacity: .4; cursor: not-allowed; }
.insumos-total { display: flex; align-items: center; justify-content: space-between; padding-top: 12px; border-top: 1px dashed #E2E8F0; font-size: 13px; font-weight: 600; color: #334155; }

/* Paso 3 — récipe */
.receta-bloque { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 18px; }
.receta-cabecera { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.receta-cabecera h4 { margin: 0 0 4px; font-size: 14px; font-weight: 600; display: flex; align-items: center; gap: 7px; }
.receta-cabecera p { margin: 0; font-size: 13px; color: #64748B; }
.opcional { font-size: 11px; font-weight: 600; color: #94A3B8; background: #F1F5F9; padding: 2px 8px; border-radius: 20px; }
.receta-acciones { display: flex; gap: 10px; }
.btn-peligro { color: #EF4444; }
.btn-peligro:hover:not(:disabled) { border-color: #EF4444; color: #EF4444; background: #FEF2F2; }
.receta-vista { margin-top: 16px; }
.receta-indicaciones { margin: 0 0 12px; font-size: 13.5px; color: #334155; }
.receta-vacia { margin: 14px 0 0; font-size: 13px; color: #94A3B8; }
.receta-item-modal { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 14px; margin-bottom: 14px; }
.receta-item-cabecera { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.receta-item-numero { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: .5px; color: #0F766E; }
.receta-item-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.btn-agregar-item { width: 100%; border-style: dashed; }

/* Paso 4 — resumen */
.resumen-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.resumen-bloque { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 18px; }
.resumen-bloque h4 { margin: 0 0 12px; font-size: 14px; font-weight: 600; display: flex; align-items: center; gap: 7px; color: #0F766E; }
.resumen-datos { margin: 0; display: flex; flex-direction: column; gap: 8px; }
.resumen-datos div { display: flex; gap: 12px; font-size: 13.5px; }
.resumen-datos dt { min-width: 130px; color: #64748B; font-weight: 600; }
.resumen-datos dd { margin: 0; color: #1E293B; font-weight: 600; }
.resumen-texto { margin: 0 0 8px; font-size: 13.5px; color: #334155; line-height: 1.55; }
.resumen-vacio { margin: 0; font-size: 13px; color: #94A3B8; }
.resumen-receta { margin: 0; padding-left: 18px; font-size: 13.5px; color: #334155; }
.resumen-receta li { margin-bottom: 6px; }
.totales-factura { margin-top: 14px; padding-top: 12px; border-top: 1px dashed #E2E8F0; display: flex; flex-direction: column; gap: 6px; }
.totales-factura div { display: flex; justify-content: space-between; font-size: 13.5px; color: #334155; }
.total-final { font-weight: 700; font-size: 15px; color: #1E293B; }

/* Tablas */
.data-table { width: 100%; border-collapse: collapse; font-family: inherit; }
.data-table th { text-align: left; padding: 10px 12px; font-size: 12px; font-weight: 600; text-transform: uppercase; letter-spacing: .5px; color: #64748B; border-bottom: 2px solid #E2E8F0; background: #F8FAFC; }
.data-table td { padding: 12px; font-size: 14px; color: #1E293B; border-bottom: 1px solid #F1F5F9; vertical-align: middle; }
.data-table tbody tr:hover td { background: #F8FAFC; }
.tabla-interna th { padding: 8px 10px; }
.tabla-interna td { padding: 8px 10px; font-size: 13px; }
.amount { font-weight: 700; color: #0F766E; }
.der { text-align: right; }
.sku { color: #94A3B8; font-size: 12px; }
.total-linea { display: flex; justify-content: space-between; margin: 10px 0 0; font-size: 13px; font-weight: 600; color: #334155; }

/* Footer del wizard */
.wizard-footer { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 16px 24px; border-top: 1px solid #E2E8F0; background: #F8FAFC; border-radius: 0 0 12px 12px; }
.footer-derecha { display: flex; gap: 12px; }

/* Botones */
.btn-pay { display: inline-flex; align-items: center; justify-content: center; gap: 8px; background: #0F766E; color: #fff; border: none; border-radius: 8px; padding: 8px 16px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all .2s ease; }
.btn-pay:hover:not(:disabled) { background: #115E59; transform: translateY(-1px); box-shadow: 0 4px 12px rgba(15,118,110,.25); }
.btn-pay:disabled { opacity: .6; cursor: not-allowed; }
.btn-outline { display: inline-flex; align-items: center; justify-content: center; gap: 8px; background: #fff; color: #334155; border: 1px solid #E2E8F0; border-radius: 8px; padding: 8px 14px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all .2s ease; }
.btn-outline:hover:not(:disabled) { border-color: #0F766E; color: #0F766E; }
.btn-outline:disabled { opacity: .6; cursor: not-allowed; }
.btn-link { background: none; border: none; padding: 0; color: #0F766E; font-size: 13px; font-weight: 600; cursor: pointer; font-family: inherit; }
.btn-link:hover { text-decoration: underline; }
.btn-peligro-solido { display: inline-flex; align-items: center; gap: 8px; background: #EF4444; color: #fff; border: none; border-radius: 8px; padding: 8px 14px; font-size: 13px; font-weight: 600; cursor: pointer; transition: all .2s ease; }
.btn-peligro-solido:hover { background: #DC2626; }

/* Pills */
.status-pill { display: inline-flex; align-items: center; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; border: 1px solid; white-space: nowrap; }
.pill-ok { display: inline-flex; align-items: center; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; background: #ECFDF5; color: #059669; border: 1px solid #A7F3D0; white-space: nowrap; }

/* Modales (el scoped aplica también al contenido teleported) */
.modal-overlay { position: fixed; inset: 0; background: rgba(15,23,42,.5); display: flex; align-items: center; justify-content: center; padding: 24px; z-index: 1200; }
.modal { background: #fff; border-radius: 12px; width: 100%; max-width: 640px; max-height: 90vh; display: flex; flex-direction: column; box-shadow: 0 20px 25px -5px rgba(0,0,0,.15), 0 8px 10px -6px rgba(0,0,0,.1); }
.modal-receta { max-width: 720px; }
.modal-chica { max-width: 440px; }
.modal-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; padding: 20px 24px; border-bottom: 1px solid #E2E8F0; }
.modal-header h3 { margin: 0 0 2px; font-size: 16px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
.modal-sub { margin: 0; font-size: 13px; color: #64748B; }
.modal-texto { margin: 0; font-size: 14px; color: #1E293B; font-weight: 600; }
.modal-nota { margin: 8px 0 0; font-size: 12.5px; color: #64748B; }
.modal-body { padding: 24px; overflow-y: auto; }
.modal-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 16px 24px; border-top: 1px solid #E2E8F0; }
.btn-cerrar { background: none; border: none; color: #64748B; cursor: pointer; padding: 4px; border-radius: 6px; display: flex; transition: all .2s ease; }
.btn-cerrar:hover { color: #1E293B; background: #F1F5F9; }

/* Éxito */
.exito-cuerpo { padding: 40px 32px; display: flex; flex-direction: column; align-items: center; text-align: center; }
.exito-icono { width: 72px; height: 72px; border-radius: 50%; background: #ECFDF5; color: #059669; display: flex; align-items: center; justify-content: center; margin-bottom: 12px; }
.exito-cuerpo h2 { margin: 0; font-size: 20px; font-weight: 700; }
.exito-mensaje { margin: 6px 0 20px; font-size: 14px; color: #64748B; max-width: 520px; }
.exito-resumen { width: 100%; max-width: 560px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 18px; display: flex; flex-direction: column; gap: 10px; text-align: left; }
.exito-resumen div { display: flex; justify-content: space-between; gap: 16px; font-size: 13.5px; flex-wrap: wrap; }
.exito-resumen span { color: #64748B; font-weight: 600; }
.exito-resumen strong { color: #1E293B; }
.exito-acciones { display: flex; gap: 12px; margin-top: 20px; flex-wrap: wrap; justify-content: center; }

/* Solo lectura */
.banner-lectura { display: flex; align-items: center; gap: 10px; background: #FFFBEB; border-bottom: 1px solid #FDE68A; color: #D97706; padding: 12px 24px; font-size: 13px; font-weight: 600; }
.banner-lectura p { margin: 0; }
.pill-lectura { display: inline-flex; align-items: center; gap: 6px; padding: 5px 12px; border-radius: 20px; background: #F1F5F9; color: #475569; border: 1px solid #E2E8F0; font-size: 12px; font-weight: 600; }
.lectura-grid { padding: 24px; display: grid; grid-template-columns: 320px 1fr; gap: 24px; align-items: start; }
.lectura-cabecera { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; margin-bottom: 6px; }
.lectura-vet { margin: 0; font-size: 14px; font-weight: 700; color: #0F766E; display: flex; align-items: center; gap: 7px; }
.vitales-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px; margin: 14px 0; }
.vitales-grid div { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 8px; padding: 10px; text-align: center; }
.vitales-grid span { display: block; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: .4px; color: #64748B; }
.vitales-grid strong { font-size: 15px; color: #1E293B; }
.detalle-textos { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.detalle-bloque { background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 8px; padding: 12px; }
.detalle-bloque h5, .detalle-insumos h5, .detalle-receta h5 { margin: 0 0 6px; font-size: 11.5px; font-weight: 600; text-transform: uppercase; letter-spacing: .5px; color: #64748B; display: flex; align-items: center; gap: 6px; }
.detalle-bloque p { margin: 0; font-size: 13.5px; color: #334155; line-height: 1.55; }
.detalle-insumos, .detalle-receta { margin-top: 16px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 8px; padding: 14px; }
.receta-cabecera-lectura { display: flex; align-items: center; justify-content: space-between; gap: 12px; flex-wrap: wrap; margin-bottom: 8px; }
.receta-cabecera-lectura h5 { margin: 0; color: #0F766E; font-size: 12.5px; }
.lectura-acciones { display: flex; gap: 12px; padding: 16px 24px; border-top: 1px solid #E2E8F0; flex-wrap: wrap; }

/* Listas del selector */
.appointment-list { display: flex; flex-direction: column; gap: 12px; }
.appointment-item { display: flex; align-items: center; gap: 16px; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 10px; padding: 12px 14px; transition: all .2s ease; flex-wrap: wrap; }
.appointment-item:hover { border-color: #CBD5E1; }
.appointment-info { flex: 1; min-width: 200px; }
.appointment-title { margin: 0; font-size: 14.5px; font-weight: 700; }
.appointment-sub { margin: 2px 0 0; font-size: 12.5px; color: #64748B; }
.appointment-side { display: flex; flex-direction: column; align-items: flex-end; gap: 8px; }
.appointment-time { font-size: 13px; font-weight: 700; color: #0F766E; white-space: nowrap; }

/* Carga / vacío */
.loading-state, .loading-inline { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 12px; padding: 48px 24px; color: #64748B; font-size: 14px; }
.loading-inline { padding: 24px; }
.spin { width: 32px; height: 32px; border: 3px solid #E2E8F0; border-top-color: #0F766E; border-radius: 50%; animation: girar .8s linear infinite; }
.spin-chica { width: 14px; height: 14px; border-width: 2px; }
@keyframes girar { to { transform: rotate(360deg); } }
.empty-state { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 48px 24px; color: #94A3B8; text-align: center; font-size: 14px; }
.empty-state p { margin: 0; }

/* Responsive */
@media (max-width: 1024px) {
  .paso-dos-columnas { grid-template-columns: 1fr; }
  .vitales-form { grid-template-columns: repeat(2, 1fr); }
  .resumen-grid { grid-template-columns: 1fr; }
  .receta-item-grid { grid-template-columns: 1fr 1fr; }
  .lectura-grid { grid-template-columns: 1fr; }
  .detalle-textos { grid-template-columns: 1fr; }
  .vitales-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .campos-par { grid-template-columns: 1fr; }
  .vitales-form { grid-template-columns: 1fr; }
  .receta-item-grid { grid-template-columns: 1fr; }
  .wizard-footer { flex-direction: column-reverse; }
  .wizard-footer .btn-pay, .wizard-footer .btn-outline, .footer-derecha { width: 100%; }
  .paciente-strip .status-pill { margin-left: 0; }
}
</style>