import api from './axios.config';

/**
 * Lanza un error si el id es inválido (undefined, null o cadena vacía).
 * @param {number|string} value
 * @param {string} name
 */
function assertId(value, name) {
  if (value === undefined || value === null || value === '') {
    throw new Error(`${name} es requerido`);
  }
}

/**
 * Extrae el nombre de archivo del header Content-Disposition.
 * @param {string|undefined} header
 * @returns {string|undefined}
 */
function parseFilenameFromContentDisposition(header) {
  if (!header) return undefined;
  const match = /filename\*?=(?:UTF-8''|")?([^";]+)/i.exec(header);
  return match ? decodeURIComponent(match[1].replace(/"/g, '')) : undefined;
}

/**
 * GET /api/atenciones/agenda-vet?fecha=YYYY-MM-DD
 * (default: hoy del servidor si no se envía fecha)
 * @param {string} [fecha]
 * @returns {Promise<any>}
 */
export async function getAgendaVet(fecha) {
  const { data } = await api.get('/atenciones/agenda-vet', {
    params: fecha ? { fecha } : {},
  });
  return data;
}

/**
 * GET /api/atenciones/cita/{idCita}/contexto
 * @param {number|string} idCita
 * @returns {Promise<any>}
 */
export async function getCitaContexto(idCita) {
  assertId(idCita, 'idCita');
  const { data } = await api.get(`/atenciones/cita/${idCita}/contexto`);
  return data;
}

/**
 * POST /api/atenciones/cita/{idCita}/iniciar → 204
 * Idempotente si la cita ya está En_Atención.
 * @param {number|string} idCita
 * @returns {Promise<{ ok: true }>}
 */
export async function iniciarAtencion(idCita) {
  assertId(idCita, 'idCita');
  await api.post(`/atenciones/cita/${idCita}/iniciar`);
  return { ok: true };
}

/**
 * POST /api/atenciones → 201
 * @param {object} payload
 * @returns {Promise<{
 *   idAtencion: number,
 *   codigoReceta: string,
 *   idReceta: number,
 *   idFacturaProductos: number,
 *   [key: string]: any
 * }>}
 */
export async function guardarAtencion(payload) {
  const { data } = await api.post('/atenciones', payload);
  return data;
}

/**
 * GET /api/atenciones/por-mascota/{idMascota} (cronológico DESC)
 * @param {number|string} idMascota
 * @returns {Promise<any[]>}
 */
export async function getHistorialMascota(idMascota) {
  assertId(idMascota, 'idMascota');
  const { data } = await api.get(`/atenciones/por-mascota/${idMascota}`);
  return data;
}

/**
 * GET /api/recetas/{idReceta}/descargar → PDF binario
 * @param {number|string} idReceta
 * @returns {Promise<{ blob: Blob, filename: string, headers: object }>}
 */
export async function descargarRecetaPdf(idReceta) {
  assertId(idReceta, 'idReceta');
  const res = await api.get(`/recetas/${idReceta}/descargar`, {
    responseType: 'blob',
  });

  const filename =
    parseFilenameFromContentDisposition(res.headers?.['content-disposition']) ??
    `receta-${idReceta}.pdf`;

  return {
    blob: res.data,
    filename,
    headers: res.headers,
  };
}