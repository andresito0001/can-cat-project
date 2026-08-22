import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Interceptor: Añadir token a las peticiones
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Interceptor: Manejar errores de respuesta
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      
      // Token expirado o inválido
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/?session=expired'
      }
      
      // Retornar error estructurado
      return Promise.reject({
        status,
        message: data?.message || 'Error del servidor',
        data
      })
    }
    
    return Promise.reject({
      status: 0,
      message: 'No se pudo conectar con el servidor'
    })
  }
)

export default api