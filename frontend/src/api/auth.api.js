import api from './axios.config'

// POST /api/auth/login
export const login = (credentials) => api.post('/auth/login', credentials)

// POST /api/auth/recuperar-password
export const recuperarPassword = (data) => api.post('/auth/recuperar-password', data)

// POST /api/auth/nueva-contrasena
export const nuevaContrasena = (data) => api.post('/auth/nueva-contrasena', data)

export const register = (data) => api.post('/auth/registro', data)