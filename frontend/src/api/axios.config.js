import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api', 
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const url = error.config?.url || ''

    const esEndpointAuth =
      url.includes('/auth/login') ||
      url.includes('/auth/registro') ||
      url.includes('/auth/recuperar-password') ||
      url.includes('/auth/nueva-contrasena')

    const yaEnAuth = window.location.pathname.startsWith('/auth/')

    if (status === 401 && !esEndpointAuth && !yaEnAuth) {
      localStorage.removeItem('token')
      window.location.href = '/auth/login?session=expired'
    }

    return Promise.reject(error)
  }
)

export default api