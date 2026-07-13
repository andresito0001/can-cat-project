import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Dashboard from '../views/Dashboard.vue'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', component: Login },
  { 
    path: '/dashboard', 
    component: Dashboard,
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.path === '/dashboard' && !authStore.isAuthenticated) {
    next('/')
  } else if (to.path === '/' && authStore.isAuthenticated) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router