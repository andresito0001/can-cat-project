import api from './axios'

/**
 * Servicio de autenticación - Comunicación con el backend
 * Implementa los endpoints del caso de uso 4.6.1.1
 */
export const authService = {
  
  /**
   * Flujo normal: Iniciar Sesión
   * Pasos 3-5 del caso de uso
   */
  async login(credentials) {
    const response = await api.post('/auth/login', credentials)
    return response.data
  },

  /**
   * Flujo alternativo: Recuperación de contraseña
   * Verifica si es cliente o personal y actúa según política
   */
  async solicitarRecuperacionPassword(correo) {
    const response = await api.post('/auth/recuperar-password', {
      correoElectronico: correo
    })
    return response.data
  },

  /**
   * Validar token de recuperación
   */
  async validarTokenRecuperacion(token) {
    const response = await api.get(`/auth/validar-token-recuperacion?token=${token}`)
    return response.data
  },

  /**
   * Restablecer contraseña con token
   */
  async restablecerPassword(token, nuevaPassword) {
    const response = await api.post('/auth/restablecer-password', {
      token,
      nuevaPassword
    })
    return response.data
  },

  /**
   * Cerrar sesión en el servidor (opcional - invalidar token)
   */
  async logout() {
    try {
      await api.post('/auth/logout')
    } catch {
      // Continuar con logout local aunque falle el servidor
    }
  }
}