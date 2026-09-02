import api from './axios.config'

export const getMetodosOnline = () => api.get('/pagos/metodos-online')

export const procesarPagoCita = (data) => api.post('/pagos/procesar-pago-cita', data)

export const descargarFactura = (id) =>
  api.get(`/pagos/facturas/${id}/descargar`, { responseType: 'blob' })