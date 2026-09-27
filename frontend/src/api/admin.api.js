import api from './axios.config'

// ─── Dashboard / Stats ───
export const getAdminStats = () => api.get('/personal/stats')

// ─── Personal ───
export const listarPersonal        = ()      => api.get('/personal')
export const obtenerPersonal       = (id)    => api.get(`/personal/${id}`)
export const crearPersonal         = (data)  => api.post('/personal/con-usuario', data)
export const actualizarPersonal    = (id, data) => api.put(`/personal/${id}`, data)
export const cambiarActivoPersonal = (id, activo) => api.patch(`/personal/${id}/activo`, { activo })
export const eliminarPersonal      = (id)    => api.delete(`/personal/${id}`)

// ─── Clientes 
export const obtenerCliente  = (id)       => api.get(`/clientes/${id}`)
export const actualizarCliente = (id, data) => api.put(`/clientes/${id}`, data)

// ─── Usuarios ───
export const listarUsuarios = (filtros = {}) => api.get('/usuarios', { params: filtros })
export const cambiarEstadoUsuario = (id, estado) =>
  api.patch(`/usuarios/${id}/estado`, { estado })

// ─── Roles ───
export const listarRoles = () => api.get('/roles')