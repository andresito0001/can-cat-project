import api from './axios.config'

// ─── CATÁLOGOS ───
export const getEspecies = () => api.get('/especies')

export const getRazasPorEspecie = (idEspecie) => api.get(`/especies/${idEspecie}/razas`)

// ─── MASCOTAS ───
export const registrarMascota = (payload) => api.post('/mascotas', payload)

export const getMisMascotas = () => api.get('/mascotas/mias')

export const getMascotasPorCliente = (idCliente) => api.get(`/mascotas/por-cliente/${idCliente}`)

export const eliminarMascota = (id) => api.delete(`/mascotas/${id}`)

export async function buscarMascotas(filtro = '') {
  const res = await api
        .get('/mascotas/buscar', { params: { filtro } })
    return res.data
}