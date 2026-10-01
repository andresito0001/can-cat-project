import api from './axios.config'

export const getMetodosOnline = () => api.get('/pagos/metodos-online')

export const procesarPagoCita = (data) => api.post('/pagos/procesar-pago-cita', data)

export const descargarFactura = (id) =>
  api.get(`/pagos/facturas/${id}/descargar`, { responseType: 'blob' })

export const getHistorialPagos = () => api.get('/pagos/historial')

export const getMetodosPresenciales = () => api.get('/pagos/metodos-presenciales')
export const enviarFactura = (id) => api.post(`/pagos/facturas/${id}/enviar`)

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
 * GET /api/pagos/facturas-pendientes → facturas de productos sin pago
 * @returns {Promise<any[]>}
 */
export async function getFacturasPendientes() {
  const { data } = await api.get('/pagos/facturas-pendientes');
  return data;
}

/**
 * POST /api/pagos/facturas/{idFactura}/cobrar
 * @param {number|string} idFactura
 * @param {{ idMetodoPago: number, referenciaTransaccion?: string, datosPago?: object }} payload
 * @returns {Promise<any>}
 */
export async function cobrarFactura(idFactura, payload) {
  assertId(idFactura, 'idFactura');
  const { data } = await api.post(
    `/pagos/facturas/${idFactura}/cobrar`,
    payload
  );
  return data;
}

/**
 * GET /api/pagos/facturas/{idFactura}/descargar → PDF binario
 * @param {number|string} idFactura
 * @returns {Promise<{ blob: Blob, filename: string, headers: object }>}
 */
export async function descargarFacturaPdf(idFactura) {
  assertId(idFactura, 'idFactura');
  const res = await api.get(`/pagos/facturas/${idFactura}/descargar`, {
    responseType: 'blob',
  });

  const filename =
    parseFilenameFromContentDisposition(res.headers?.['content-disposition']) ??
    `factura-${idFactura}.pdf`;

  return {
    blob: res.data,
    filename,
    headers: res.headers,
  };
}

/**
 * POST /api/pagos/facturas/{idFactura}/enviar
 * @param {number|string} idFactura
 * @returns {Promise<{ enviado: boolean, mensaje: string }>}
 */
export async function enviarFacturaEmail(idFactura) {
  assertId(idFactura, 'idFactura');
  const { data } = await api.post(`/pagos/facturas/${idFactura}/enviar`);
  return data;
}

export const getPagosPendientesVerificacion = () =>
  api.get('/pagos/pendientes-verificacion')

export const verificarPago = (idPago, payload) =>
  api.post(`/pagos/${idPago}/verificar`, payload)

export const getDatosBancarios = () => api.get('/public/datos-bancarios')

export const getTasaCambio = () => api.get('/public/tasa-cambio')