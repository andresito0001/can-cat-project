import api from './axios.config'

export const getServicios = (params) =>
  params ? api.get('/citas/servicios', { params }) : api.get('/citas/servicios')

export const getVeterinarios = () => api.get('/citas/veterinarios')

export const getDisponibilidad = (params) =>
  api.get('/citas/disponibilidad', { params })

export const solicitarCita = (data) => api.post('/citas/solicitar', data)

export const confirmarPago = (id) => api.post(`/citas/${id}/confirmar-pago`)

export const cancelarCita = (id) => api.post(`/citas/${id}/cancelar`)

export const getMisCitas = (estado) =>
  estado ? api.get('/citas/mis-citas', { params: { estado } }) : api.get('/citas/mis-citas')

export const getAgenda = (params) => api.get('/citas/agenda', { params })

export const agendarMostrador = (data) => api.post('/citas/agenda-mostrador', data)

export const getPendientesPago = () => api.get('/citas/pendientes-pago')

export const cobrarCitaMostrador = (id, data) => api.post(`/citas/${id}/cobrar-mostrador`, data)

export const cambiarEstadoCita = (id, estado) => api.post(`/citas/${id}/cambiar-estado`, { estado })