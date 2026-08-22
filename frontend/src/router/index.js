import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Registro from '../components/Registro.vue'
import ForgotPassword from '../components/ForgotPassword.vue'
import ResetPassword from '../components/ResetPassword.vue'
import Dashboard from '../views/Dashboard.vue'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', component: Login },
  { path: '/registro', component: Registro },
  { path: '/recuperar-password', component: ForgotPassword },
  { path: '/reset-password', component: ResetPassword },
  { path: '/dashboard', component: Dashboard },
  {
    path: '/nueva-contrasena',
    component: ResetPassword,
    meta: { publica: true }  // No requiere autenticación
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  const rutasProtegidas = ['/dashboard']
  
  if (rutasProtegidas.includes(to.path) && !authStore.isAuthenticated) {
    next('/')
  } else if (to.path === '/' && authStore.isAuthenticated) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router