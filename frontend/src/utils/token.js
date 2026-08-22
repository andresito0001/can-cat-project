const TOKEN_KEY = 'cancat_token'
const USER_KEY = 'cancat_user'
const EXPIRY_KEY = 'cancat_token_expiry'

/**
 * Utilidades para gestión segura de tokens JWT
 */
export const tokenManager = {
  
  /**
   * Guardar token y datos del usuario
   */
  saveAuthData(token, user) {
    // Calcular fecha de expiración (por si necesitamos verificar localmente)
    const expiry = new Date()
    expiry.setSeconds(expiry.getSeconds() + user.expiresIn || 86400)

    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
    localStorage.setItem(EXPIRY_KEY, expiry.toISOString())
  },

  /**
   * Obtener token
   */
  getToken() {
    return localStorage.getItem(TOKEN_KEY)
  },

  /**
   * Obtener datos del usuario
   */
  getUser() {
    const userStr = localStorage.getItem(USER_KEY)
    try {
      return userStr ? JSON.parse(userStr) : null
    } catch {
      return null
    }
  },

  /**
   * Verificar si el token está expirado
   */
  isTokenExpired() {
    const expiryStr = localStorage.getItem(EXPIRY_KEY)
    if (!expiryStr) return true
    
    const expiry = new Date(expiryStr)
    return new Date() >= expiry
  },

  /**
   * Verificar si hay una sesión válida
   */
  hasValidSession() {
    const token = this.getToken()
    return token && !this.isTokenExpired()
  },

  /**
   * Limpiar todos los datos de autenticación
   */
  clearAuthData() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    localStorage.removeItem(EXPIRY_KEY)
  }
}