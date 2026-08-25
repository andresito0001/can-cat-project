import api from './axios.config'

export const getAll = () => api.get('/clientes')
export const getById = (id) => api.get(`/clientes/${id}`)
export const create = (data) => api.post('/clientes', data)
export const update = (id, data) => api.put(`/clientes/${id}`, data)