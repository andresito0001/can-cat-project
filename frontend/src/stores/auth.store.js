import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth.api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || null)
  const isLoading = ref(false)
  const error = ref(null)

  const isAuthenticated = computed(() => !!token.value)
  const userRole = computed(() => user.value?.rol || null)
  const userName = computed(() => user.value?.nombreCompleto || 'Usuario')
  const userPermissions = computed(() => user.value?.permisos || [])

  const dashboardRoute = computed(() => {
    const routes = {
      'Cliente': '/cliente/dashboard',
      'Recepcionista': '/recepcion/dashboard',
      'Veterinario': '/veterinario/dashboard',
      'Encargado_Almacen': '/almacen/dashboard',
      'Administrador': '/admin/dashboard'
    }
    return routes[userRole.value] || '/auth/login'
  })

  // ─── LOGIN REAL ───
  async function login(credentials) {
    isLoading.value = true
    error.value = null

    try {
      const { data } = await authApi.login(credentials)
      
      // DTO exacto del backend:
      // data.accessToken, data.tokenType, data.expiresIn, data.usuario
      token.value = data.accessToken
      user.value = data.usuario
      localStorage.setItem('token', data.accessToken)
      
      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.mensaje
                 || err.response?.data?.message
                 || 'Credenciales incorrectas'
      return { success: false }
    } finally {
      isLoading.value = false
    }
  }

  // ─── REGISTRO REAL ───
  async function register(data) {
    isLoading.value = true
    error.value = null
    try {
      await authApi.register(data)
      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.mensaje || 'Error al registrar'
      return { success: false }
    } finally {
      isLoading.value = false
    }
  }

  // ─── RECUPERAR PASSWORD ───
  async function solicitarRecuperacion(correo) {
    isLoading.value = true
    error.value = null
    try {
      const { data } = await authApi.recuperarPassword({ correoElectronico: correo })
      return { success: true, mensaje: data.mensaje || 'Revisa tu correo.' }
    } catch (err) {
      error.value = err.response?.data?.mensaje || 'Error al procesar'
      return { success: false }
    } finally {
      isLoading.value = false
    }
  }

  // ─── RESTABLECER PASSWORD ───
  async function restablecerContrasena(tokenReset, nuevaPassword) {
    isLoading.value = true
    error.value = null
    try {
      await authApi.nuevaContrasena({
        token: tokenReset,
        nuevaContrasena: nuevaPassword
      })
      return { success: true }
    } catch (err) {
      error.value = err.response?.data?.mensaje || 'No se pudo restablecer'
      return { success: false }
    } finally {
      isLoading.value = false
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    window.location.href = '/auth/login'
  }

  // ─── MOCK para desarrollar dashboards sin backend ───
  function mockLogin(roleName = 'Cliente') {
    const mocks = {
      'Cliente': { 
        id: 1, nombreCompleto: 'María González', correoElectronico: 'maria@email.com', 
        rol: 'Cliente', permisos: ['mascotas:own', 'citas:own', 'facturas:own'] 
      },
      'Recepcionista': { 
        id: 2, nombreCompleto: 'Carlos Ruiz', correoElectronico: 'carlos@email.com', 
        rol: 'Recepcionista', permisos: ['citas:*', 'facturas:*', 'pagos:*', 'clientes:read'] 
      },
      'Veterinario': { 
        id: 3, nombreCompleto: 'Dra. Ana Pérez', correoElectronico: 'ana@email.com', 
        rol: 'Veterinario', permisos: ['atenciones:*', 'historial:*', 'citas:read', 'productos:read'] 
      },
      'Encargado_Almacen': { 
        id: 4, nombreCompleto: 'Luis Torres', correoElectronico: 'luis@email.com', 
        rol: 'Encargado_Almacen', permisos: ['productos:*', 'movimientos:*', 'compras:*', 'proveedores:*'] 
      },
      'Administrador': { 
        id: 5, nombreCompleto: 'Admin Sistema', correoElectronico: 'admin@clinica.com', 
        rol: 'Administrador', permisos: ['*'] 
      }
    }
    user.value = mocks[roleName]
    token.value = 'mock-jwt-token'
    localStorage.setItem('token', token.value)
    return { success: true }
  }

  function hasPermission(permission) {
    if (!user.value?.permisos) return false
    if (user.value.permisos.includes('*')) return true
    return user.value.permisos.includes(permission)
  }

  return {
    user, token, isLoading, error,
    isAuthenticated, userRole, userName, userPermissions, dashboardRoute,
    login, logout, register, mockLogin,
    solicitarRecuperacion, restablecerContrasena,
    hasPermission
  }
})