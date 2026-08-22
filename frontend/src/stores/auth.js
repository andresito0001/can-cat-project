import { defineStore } from 'pinia'
import api from '../api/axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    errorMessage: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    userRole: (state) => state.user?.rol || null,
    userName: (state) => state.user?.nombreCompleto || ''
  },

  actions: {
    // =============================================
    // LOGIN
    // =============================================
    async login(email, password) {
      try {
        this.errorMessage = null
        
        const response = await api.post('/auth/login', {
          correoElectronico: email,
          contrasena: password
        })

        const { accessToken, usuario } = response.data

        this.token = accessToken
        this.user = usuario
        localStorage.setItem('token', accessToken)
        localStorage.setItem('user', JSON.stringify(usuario))

        return { success: true }
      } catch (error) {
        if (error.response?.status === 401) {
          this.errorMessage = 'Credenciales incorrectas'
        } else if (error.response?.status === 403) {
          this.errorMessage = error.response?.data?.message || 'Cuenta inactiva'
        } else {
          this.errorMessage = 'Error de conexión con el servidor'
        }
        return { success: false, message: this.errorMessage }
      }
    },

    // =============================================
    // RECUPERAR CONTRASEÑA
    // =============================================
    async solicitarRecuperacionPassword(correo) {
      try {
        this.errorMessage = null
        
        const response = await api.post('/auth/recuperar-password', {
          correoElectronico: correo
        })

        return { success: true, data: response.data }
      } catch (error) {
        this.errorMessage = error.response?.data?.message || 'Error al procesar la solicitud'
        return { success: false, message: this.errorMessage }
      }
    },

    // =============================================
    // NUEVA CONTRASEÑA (RESET)
    // =============================================
    async nuevaContrasena(token, contrasena) {
      try {
        this.errorMessage = null
        
        await api.post('/auth/reset-password', {
          token: token,
          nuevaContrasena: contrasena
        })

        return { success: true }
      } catch (error) {
        this.errorMessage = error.response?.data?.message || 'Error al cambiar la contraseña'
        return { success: false, message: this.errorMessage }
      }
    },

    // =============================================
    // REGISTRAR CLIENTE
    // =============================================
    async registrarCliente(datos) {
      try {
        this.errorMessage = null

        const response = await api.post('/registro/cliente', {
          nombreCompleto: datos.nombreCompleto,
          documentoIdentidad: datos.documentoIdentidad,
          correoElectronico: datos.correoElectronico,
          contrasena: datos.contrasena,
          telefonoPrincipal: datos.telefonoPrincipal,
          telefonoSecundario: datos.telefonoSecundario || null,
          direccion: datos.direccion || null,
          ciudad: datos.ciudad || null,
          fechaNacimiento: datos.fechaNacimiento || null
        })

        return { success: true, data: response.data }
      } catch (error) {
        let mensaje = 'Error al procesar el registro'
        
        if (error.response?.status === 409) {
          mensaje = 'El correo ya está registrado'
        } else if (error.response?.data?.message) {
          mensaje = error.response.data.message
        }
        
        this.errorMessage = mensaje
        return { success: false, message: mensaje }
      }
    },

    // =============================================
    // LOGOUT
    // =============================================
    logout() {
      this.token = null
      this.user = null
      this.errorMessage = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },

    // =============================================
    // OBTENER RUTA DASHBOARD
    // =============================================
    getDashboardRoute() {
      const rutas = {
        'Cliente': '/dashboard',
        'Recepcionista': '/dashboard',
        'Veterinario': '/dashboard',
        'Encargado_Almacen': '/dashboard',
        'Administrador': '/dashboard'
      }
      return rutas[this.userRole] || '/dashboard'
    }
  }
})