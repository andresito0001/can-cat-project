import { defineStore } from 'pinia';
import api from '../api/axios';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    errorMessage: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token
  },

  actions: {
    async login(email, password) {
      try {
        this.errorMessage = null;
        
        const response = await api.post('/auth/login', {
          email: email,
          contrasena: password
        });

        const { token } = response.data;

        this.token = token;
        localStorage.setItem('token', token);

        return true; 
      } catch (error) {
        if (error.response && error.response.status === 401) {
          this.errorMessage = 'Credenciales inválidas';
        } else {
          this.errorMessage = 'Error de conexión con el servidor';
        }
        return false;
      }
    },

    logout() {
      this.token = null;
      this.errorMessage = null;
      localStorage.removeItem('token');
    }
  }
});