import api from './axios.config'

// ─── CATÁLOGOS ───
export const getEspecies = () => api.get('/especies')

export const getRazasPorEspecie = (idEspecie) => api.get(`/especies/${idEspecie}/razas`)

// ─── MASCOTAS ───
export const registrarMascota = (payload) => api.post('/mascotas', payload)

export const getMisMascotas = () => api.get('/mascotas/mias')