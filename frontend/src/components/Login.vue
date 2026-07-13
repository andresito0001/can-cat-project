<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">Iniciar Sesión</h2>
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="input-group">
          <label for="email">Correo electrónico</label>
          <input type="email" id="email" v-model="email" placeholder="correo@ejemplo.com" required />
        </div>
        <div class="input-group">
          <label for="password">Contraseña</label>
          <input type="password" id="password" v-model="password" placeholder="••••••••" required />
        </div>
        <p v-if="mostrarError" class="error-message">Credenciales inválidas</p>
        <button type="submit" class="login-button" :disabled="isLoading">
          {{ isLoading ? 'Ingresando...' : 'Ingresar' }}
        </button>
        <div class="register-container">
          <span>¿No tienes cuenta?</span>
          <a href="#" class="register-link" @click.prevent="irARegistro">Registrarse</a>
        </div>
      </form>
    </div>
  </div>
</template>


<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../stores/auth' 
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const email = ref('')
const password = ref('')
const isLoading = ref(false)
const mostrarError = ref(false)

const handleLogin = async () => {
  isLoading.value = true
  mostrarError.value = false
  
  const success = await authStore.login(email.value, password.value)
  
  isLoading.value = false

  if (success) {
    router.push('/dashboard')
  } else {
    mostrarError.value = true
  }
}

const irARegistro = () => {
  alert('Pantalla de registro próximamente')
}
</script>


<style scoped>
/* === ESTÁNDARES DE DISEÑO CAN-CAT (VINOTINTO) === */

/* Colores Base (Se podrían exportar como CSS Variables después) */
/* Vinotinto Principal: #AC1A2E | Hover: #8d1525 | Fondo: #f4f4f5 */

* { 
  box-sizing: border-box; 
  margin: 0; 
  padding: 0; 
}

/* Contenedor: Fondo gris muy claro (casi blanco) */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f4f4f5; /* Blanco roto suave, cuida la vista */
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 20px; /* Responsividad: Evita que toque los bordes en móviles */
}

/* Tarjeta: Efecto cristal con acento Vinotinto arriba */
.login-card {
  background-color: #ffffff;
  padding: 40px 35px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04);
  width: 100%;
  max-width: 380px;
  border-top: 5px solid #AC1A2E; /* 👈 El toque de color Vinotinto */
  transition: transform 0.3s ease;
}

/* Título */
.login-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 35px;
  text-align: center;
  letter-spacing: -0.5px; /* Tipografía moderna */
}

/* Espaciado de inputs */
.input-group {
  margin-bottom: 22px;
}

/* Etiquetas */
.input-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #555555;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Cajas de texto */
.input-group input {
  width: 100%;
  padding: 12px 14px;
  border: 1.5px solid #e0e0e0;
  border-radius: 8px;
  font-size: 15px;
  color: #333333;
  background-color: #fafafa;
  transition: all 0.2s ease;
  outline: none;
}

.input-group input::placeholder {
  color: #aaaaaa;
}

/* Interacción al hacer click en el input */
.input-group input:focus {
  border-color: #AC1A2E; /* Borde vinotinto al seleccionar */
  background-color: #ffffff;
  box-shadow: 0 0 0 3px rgba(172, 26, 46, 0.1); /* Resplandor sutil vinotinto */
}

/* Mensaje de error */
.error-message {
  color: #AC1A2E; /* Ahora el error también es vinotinto */
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 15px;
  text-align: center;
  background-color: rgba(172, 26, 46, 0.05);
  padding: 8px;
  border-radius: 6px;
}

.login-button {
  width: 100%;
  padding: 13px;
  background-color: #AC1A2E; /* Vinotinto puro */
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease; /* Transición suave para el efecto hover */
  margin-top: 10px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Efecto al pasar el ratón (Hover) */
.login-button:hover:not(:disabled) {
  background-color: #8d1525; /* Vinotinto más oscuro */
  transform: translateY(-2px); /* 👈 Se levanta ligeramente */
  box-shadow: 0 6px 15px rgba(172, 26, 46, 0.3); /* Sombra vinotinta */
}

/* Efecto al hacer click */
.login-button:active:not(:disabled) {
  transform: translateY(0px);
}

/* Botón bloqueado (mientras carga) */
.login-button:disabled {
  background-color: #d4a0a8; /* Vinotinto desaturado/grisáceo */
  cursor: not-allowed;
  transform: translateY(0px);
  box-shadow: none;
}

/* Estilo para el texto de registrarse */
.register-container {
  text-align: center;
  margin-top: 25px;
  font-size: 14px;
  color: #777777;
}

.register-link {
  color: #AC1A2E;
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
  transition: color 0.2s;
}

.register-link:hover {
  color: #8d1525;
  text-decoration: underline;
}
</style>