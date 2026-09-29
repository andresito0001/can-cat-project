import api from './axios.config'

// ─── CATÁLOGOS ───
export const getEspecies = () => api.get('/especies')
export const getRazasPorEspecie = (idEspecie) => api.get(`/especies/${idEspecie}/razas`)

// ─── MASCOTAS ───
export const registrarMascota = (payload) => api.post('/mascotas', payload)

export const actualizarMascota = (id, payload) => api.put(`/mascotas/${id}`, payload)

export const cambiarEstadoMascota = (id, estado) =>
  api.patch(`/mascotas/${id}/estado`, { estado })

export const getMisMascotas = (incluirArchivadas = false) =>
  api.get('/mascotas/mias', { params: incluirArchivadas ? { incluirArchivadas: true } : {} })

export const getMascotasPorCliente = (idCliente, incluirArchivadas = false) =>
  api.get(`/mascotas/por-cliente/${idCliente}`, {
    params: incluirArchivadas ? { incluirArchivadas: true } : {},
  })

// Mantener por compatibilidad (ahora hace soft delete)
export const eliminarMascota = (id) => api.delete(`/mascotas/${id}`)

export async function buscarMascotas(filtro = '') {
  const res = await api.get('/mascotas/buscar', { params: { filtro } })
  return res.data
}