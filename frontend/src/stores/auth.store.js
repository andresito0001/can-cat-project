import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth.api'
import { getApiErrorMessage } from '@/utils/apiError'

const TOKEN_KEY = 'token'
const USER_KEY  = 'cancat_user'

function leerToken() {
  return sessionStorage.getItem(TOKEN_KEY) || null
}

function leerUsuario() {
  try {
    const raw = sessionStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const user  = ref(leerUsuario())
  const token = ref(leerToken())
  const isLoading = ref(false)
  const error = ref(null)

  const isAuthenticated = computed(() => !!token.value)
  const userRole        = computed(() => user.value?.rol || null)
  const userName        = computed(() => user.value?.nombreCompleto || 'Usuario')
  const userPermissions = computed(() => user.value?.permisos || [])

  const dashboardRoute = computed(() => {
    const routes = {
      'Cliente': '/cliente/dashboard',
      'Recepcionista': '/recepcion/dashboard',
      'Veterinario': '/veterinario/dashboard',
      'Encargado_Almacen': '/almacen/dashboard',
      'Administrador': '/admin/dashboard',
    }
    return routes[userRole.value] || '/auth/login'
  })

  async function login(credentials) {
    isLoading.value = true
    error.value = null
    try {
      const { data } = await authApi.login(credentials)
      token.value = data.accessToken
      user.value  = data.usuario
      // ⚠️ sessionStorage → cada pestaña tiene su propia sesión
      sessionStorage.setItem(TOKEN_KEY, data.accessToken)
      sessionStorage.setItem(USER_KEY, JSON.stringify(data.usuario))
      return { success: true }
    } catch (err) {
      error.value = getApiErrorMessage(err) || 'Credenciales incorrectas'
      return { success: false }
    } finally {
      isLoading.value = false
    }
  }

  async function register(data) { /* sin cambios */ }
  async function solicitarRecuperacion(correo) { /* sin cambios */ }
  async function restablecerContrasena(tokenReset, nuevaPassword) { /* sin cambios */ }

  function logout() {
    token.value = null
    user.value = null
    sessionStorage.removeItem(TOKEN_KEY)
    sessionStorage.removeItem(USER_KEY)
    window.location.href = '/auth/login'
  }

  function mockLogin(roleName = 'Cliente') {
    
  }

  function hasPermission(permission) { /* sin cambios */ }

  return {
    user, token, isLoading, error,
    isAuthenticated, userRole, userName, userPermissions, dashboardRoute,
    login, logout, register, mockLogin,
    solicitarRecuperacion, restablecerContrasena,
    hasPermission,
  }
})