import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store.js'

const routes = [
  { path: '/', redirect: '/auth/login' },
  
  {
    path: '/auth',
    component: () => import('@/layouts/AuthLayout.vue'),
    meta: { public: true },
    children: [
      { path: 'login', name: 'Login', component: () => import('@/views/auth/LoginView.vue') },
      { path: 'registro', name: 'Register', component: () => import('@/views/auth/RegisterView.vue') },
      { path: 'recuperar', name: 'ForgotPassword', component: () => import('@/views/auth/ForgotPasswordView.vue') },
      { path: 'nueva-contrasena', name: 'ResetPassword', component: () => import('@/views/auth/ResetPasswordView.vue') },
    ]
  },
  
  // ─── CLIENTE ───
  {
    path: '/cliente',
    component: () => import('@/layouts/ClientLayout.vue'),
    meta: { requiresAuth: true, role: 'Cliente' },
    children: [
      { path: 'dashboard', name: 'ClientDashboard', component: () => import('@/views/cliente/DashboardClientView.vue') },
      { path: 'mascotas', name: 'MyPets', component: () => import('@/views/cliente/MyPetsView.vue') },
      { path: 'solicitar-cita', name: 'RequestAppointment', component: () => import('@/views/cliente/RequestAppointmentView.vue') },
      { path: 'historial-pagos', name: 'PaymentHistory', component: () => import('@/views/cliente/PaymentHistoryView.vue') },
      { path: 'historial-clinico', name: 'PetHistory', component: () => import('@/views/cliente/PetHistoryView.vue') },
      { path: 'mis-citas', name: 'MyAppointments', component: () => import('@/views/cliente/MyAppointmentsView.vue') },
      { path: 'pagar-cita/:idCita', name: 'PayAppointment', component: () => import('@/views/cliente/PayAppointmentView.vue') }
    
    ]
  },
  
  // ─── RECEPCIONISTA ───
  {
    path: '/recepcion',
    component: () => import('@/layouts/ReceptionistLayout.vue'),
    meta: { requiresAuth: true, role: 'Recepcionista' },
    children: [
      { path: 'dashboard', name: 'ReceptionistDashboard', component: () => import('@/views/recepcion/DashboardReceptionistView.vue') },
      { path: 'citas', name: 'ManageAppointments', component: () => import('@/views/recepcion/GestionCitasView.vue') },
      { path: 'citas/nueva', name: 'NuevaReservaMostrador', component: () => import('@/views/recepcion/AgendarCitaMostradorView.vue') },
      { path: 'clientes', name: 'ManageClients', component: () => import('@/views/recepcion/ClientesView.vue') },
      { path: 'clientes/nuevo', name: 'RegisterClient', component: () => import('@/views/recepcion/RegistrarClienteView.vue') },
      { path: 'registrar-mascota', name: 'RegisterPetReception', component: () => import('@/views/recepcion/RegisterPetView.vue') },
      { path: 'caja', name: 'Caja', component: () => import('@/views/recepcion/CajaView.vue') },
    ]
  },
  
  // ─── VETERINARIO ───
  {
    path: '/veterinario',
    component: () => import('@/layouts/VetLayout.vue'),
    meta: { requiresAuth: true, role: 'Veterinario' },
    children: [
      { path: '', redirect: '/veterinario/dashboard' },
      { path: 'dashboard', name: 'VeterinarioDashboard', component: () => import('@/views/veterinario/DashboardVetView.vue') },
      { path: 'agenda', name: 'VeterinarioAgenda', component: () => import('@/views/veterinario/DailyAgendaView.vue') },
      { path: 'atencion/:citaId?', name: 'VeterinarioAtencion', component: () => import('@/views/veterinario/ClinicalCareView.vue') },
      { path: 'pacientes', redirect: '/veterinario/historiales' },
      { path: 'historiales', name: 'VeterinarioHistoriales', component: () => import('@/views/veterinario/PatientHistoryMasterView.vue') },
    ],
  },
  
  // ─── ALMACÉN ───
  {
    path: '/almacen',
    component: () => import('@/layouts/WarehouseLayout.vue'),
    meta: { requiresAuth: true, role: 'Encargado_Almacen' },
    children: [
      { path: '', redirect: '/almacen/dashboard' },
      { path: 'dashboard', name: 'WarehouseDashboard', component: () => import('@/views/almacen/DashboardWarehouseView.vue') },
      { path: 'inventario', name: 'Inventory', component: () => import('@/views/almacen/InventoryView.vue') },
      { path: 'entrada', name: 'StockEntry', component: () => import('@/views/almacen/StockEntryView.vue') },
      { path: 'proveedores', name: 'Proveedores', component: () => import('@/views/almacen/ProveedoresView.vue') },
    ]
  },

  // ADMINISTRADOR
  {
    path: '/admin',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true, role: 'Administrador' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/AdminDashboardView.vue') },
      { path: 'usuarios', name: 'AdminUsuarios', component: () => import('@/views/admin/UsuariosView.vue') },
      { path: 'personal', name: 'AdminPersonal', component: () => import('@/views/admin/PersonalView.vue') },
      { path: 'roles', name: 'AdminRoles', component: () => import('@/views/admin/RolesView.vue') },
      { path: 'reportes', name: 'AdminReportes', component: () => import('@/views/admin/ReportesView.vue') },
      { path: 'configuracion', name: 'AdminConfiguracion', component: () => import('@/views/admin/ConfiguracionView.vue') },
    ]
  },

  // ─── PERFIL (transversal a todos los roles) ───
  {
    path: '/perfil',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'MiPerfil',
        component: () => import('@/views/shared/MiPerfilView.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.public) return next()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }
  
  // Comparación EXACTA con el nombre de rol de la BD
  if (to.meta.role && authStore.userRole !== to.meta.role) {
    if (authStore.isAuthenticated) {
      return next(authStore.dashboardRoute)
    }
    return next({ name: 'Login' })
  }
  
  next()
})

export default router