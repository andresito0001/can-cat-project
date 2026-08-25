import api from './axios.config'

export const getAll = () => api.get('/personal')
export const create = (data) => api.post('/personal', data)
export const update = (id, data) => api.put(`/personal/${id}`, data)
export const deletePersonal = (id) => api.delete(`/personal/${id}`)

// así sucesivamente con las demas tablas que tenga 
// mascotas.api.js, citas.api.js, inventario.api.js, pagos.api.js.
