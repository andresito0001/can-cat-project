import api from './axios.config'

// ─── Consultas ───
export const getAll = (filtro) => api.get('/clientes', { params: filtro ? { filtro } : {} })
export const getById = (id) => api.get(`/clientes/${id}`)
export const getByDocumento = (documento) => api.get(`/clientes/documento/${documento}`)
export const getByUsuarioId = (usuarioId) => api.get(`/clientes/usuario/${usuarioId}`)

// ─── Caso de uso 4.6.1.10: Registro asistido (recepcionista) ───
export const registrarAsistido = (data) => api.post('/clientes/registro-asistido', data)

// ─── CRUD genérico ───
export const create = (data) => api.post('/clientes', data)
export const update = (id, data) => api.put(`/clientes/${id}`, data)