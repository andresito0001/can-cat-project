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
      { path: 'mis-citas', name: 'MyAppointments', component: () => import('@/views/cliente/MyAppointmentsView.vue') }
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
      { path: 'cobrar', name: 'CounterPayment', component: () => import('@/views/recepcion/CounterPaymentView.vue') },
    ]
  },
  
//   // ─── VETERINARIO ───
//   {
//     path: '/veterinario',
//     component: () => import('@/layouts/VetLayout.vue'),
//     meta: { requiresAuth: true, role: 'Veterinario' },
//     children: [
//       { path: 'dashboard', name: 'VetDashboard', component: () => import('@/views/veterinario/DashboardVetView.vue') },
//       { path: 'agenda', name: 'VetAgenda', component: () => import('@/views/veterinario/DailyAgendaView.vue') },
//       { path: 'atencion/:citaId?', name: 'ClinicalCare', component: () => import('@/views/veterinario/ClinicalCareView.vue') },
//       { path: 'pacientes', name: 'PatientSearch', component: () => import('@/views/veterinario/PatientSearchView.vue') },
//       { path: 'historiales', name: 'MedicalRecords', component: () => import('@/views/veterinario/MedicalRecordsView.vue') },
//     ]
//   },
  
//   // ─── ALMACÉN ───
//   {
//     path: '/almacen',
//     component: () => import('@/layouts/WarehouseLayout.vue'),
//     meta: { requiresAuth: true, role: 'Encargado_Almacen' },
//     children: [
//       { path: 'dashboard', name: 'WarehouseDashboard', component: () => import('@/views/almacen/DashboardWarehouseView.vue') },
//       { path: 'inventario', name: 'Inventory', component: () => import('@/views/almacen/InventoryView.vue') },
//       { path: 'entrada', name: 'StockEntry', component: () => import('@/views/almacen/StockEntryView.vue') },
//       { path: 'alertas', name: 'StockAlerts', component: () => import('@/views/almacen/StockAlertsView.vue') },
//       { path: 'catalogo', name: 'Catalog', component: () => import('@/views/almacen/CatalogView.vue') },
//     ]
//   },
  
//   // ─── ADMINISTRADOR ───
//   {
//     path: '/admin',
//     component: () => import('@/layouts/AdminLayout.vue'),
//     meta: { requiresAuth: true, role: 'Administrador' },
//     children: [
//       { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/DashboardAdminView.vue') },
//       { path: 'usuarios', name: 'UserManagement', component: () => import('@/views/admin/UserManagementView.vue') },
//       { path: 'personal', name: 'StaffManagement', component: () => import('@/views/admin/StaffManagementView.vue') },
//       { path: 'roles', name: 'RoleManagement', component: () => import('@/views/admin/RoleManagementView.vue') },
//       { path: 'reportes', name: 'Reports', component: () => import('@/views/admin/ReportsView.vue') },
//       { path: 'configuracion', name: 'Settings', component: () => import('@/views/admin/SettingsView.vue') },
//     ]
//   },
  
//   { path: '/:pathMatch(.*)*', redirect: '/auth/login' }
// ]
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