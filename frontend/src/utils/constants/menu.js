import { ROLES } from './roles'

export const MENU_ITEMS = {
  [ROLES.CLIENT]: [
    { label: 'Dashboard', icon: 'LayoutDashboard', route: '/cliente/dashboard' },
    { label: 'Mis Mascotas', icon: 'PawPrint', route: '/cliente/mascotas' },
    { label: 'Solicitar Cita', icon: 'CalendarPlus', route: '/cliente/solicitar-cita' },
    { label: 'Historial de Pagos', icon: 'CreditCard', route: '/cliente/historial-pagos' },
    { label: 'Historial Clínico', icon: 'Stethoscope', route: '/cliente/historial-clinico' },
    { label: 'Mis Citas', icon: 'CalendarDays', route: '/cliente/mis-citas' },
  ],
  [ROLES.RECEPTIONIST]: [
    { label: 'Dashboard', icon: 'LayoutDashboard', route: '/recepcion/dashboard' },
    { label: 'Agenda', icon: 'CalendarDays', route: '/recepcion/citas' },
    { label: 'Gestión de Clientes', icon: 'Users', route: '/recepcion/clientes' },
    { label: 'Registrar Mascota', icon: 'Dog', route: '/recepcion/registrar-mascota' },
    { label: 'Caja', icon: 'Banknote', route: '/recepcion/caja' },
  ],
  [ROLES.VET]: [
    { label: 'Dashboard', icon: 'LayoutDashboard', route: '/veterinario/dashboard' },
    { label: 'Mi Agenda Hoy', icon: 'CalendarCheck', route: '/veterinario/agenda' },
    { label: 'Atención Clínica', icon: 'Stethoscope', route: '/veterinario/atencion' },
    { label: 'Pacientes e historial', icon: 'ClipboardList', route: '/veterinario/historiales' },
  ],
  [ROLES.WAREHOUSE]: [
    { label: 'Dashboard', icon: 'LayoutDashboard', route: '/almacen/dashboard' },
    { label: 'Inventario', icon: 'Package', route: '/almacen/inventario' },
    { label: 'Registrar Entrada', icon: 'ArrowDownToLine', route: '/almacen/entrada' },
    { label: 'Proveedores', icon: 'Truck', route: '/almacen/proveedores' },   // ← NUEVO
  ],
  [ROLES.ADMIN]: [
    { label: 'Dashboard', icon: 'LayoutDashboard', route: '/admin/dashboard' },
    { label: 'Usuarios', icon: 'Users', route: '/admin/usuarios' },
    { label: 'Personal', icon: 'UserCog', route: '/admin/personal' },
    { label: 'Roles y Permisos', icon: 'Shield', route: '/admin/roles' },
    { label: 'Reportes', icon: 'BarChart3', route: '/admin/reportes' },
    { label: 'Configuración', icon: 'Settings', route: '/admin/configuracion' },
  ]
}