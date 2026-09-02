import api from './axios.config'

export const getServicios = (params) =>
  params ? api.get('/citas/servicios', { params }) : api.get('/citas/servicios')

export const getVeterinarios = () => api.get('/citas/veterinarios')

export const getDisponibilidad = (params) =>
  api.get('/citas/disponibilidad', { params })

export const solicitarCita = (data) => api.post('/citas/solicitar', data)

export const confirmarPago = (id) => api.post(`/citas/${id}/confirmar-pago`)

export const cancelarCita = (id) => api.post(`/citas/${id}/cancelar`)

export const getMisCitas = () => api.get('/citas/mis-citas')