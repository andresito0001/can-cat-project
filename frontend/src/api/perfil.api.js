import api from './axios.config'

// GET /api/perfil
export const getPerfil = () => api.get('/perfil')

// PUT /api/perfil — solo Cliente
export const actualizarPerfil = (payload) => api.put('/perfil', payload)

// POST /api/perfil/cambiar-contrasena
export const cambiarContrasena = (payload) =>
  api.post('/perfil/cambiar-contrasena', payload)