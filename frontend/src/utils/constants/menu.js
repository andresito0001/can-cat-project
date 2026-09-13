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
    { label: 'Gestionar Citas', icon: 'CalendarDays', route: '/recepcion/citas' },
    { label: 'Gestión de Clientes', icon: 'Users', route: '/recepcion/clientes' },
    { label: 'Registrar Mascota', icon: 'Dog', route: '/recepcion/registrar-mascota' },
    { label: 'Cobrar en Mostrador', icon: 'Banknote', route: '/recepcion/cobrar' },
  ],
  [ROLES.VET]: [
    { label: 'Dashboard', icon: 'LayoutDashboard', route: '/veterinario/dashboard' },
    { label: 'Mi Agenda Hoy', icon: 'CalendarCheck', route: '/veterinario/agenda' },
    { label: 'Atención Clínica', icon: 'Stethoscope', route: '/veterinario/atencion' },
    { label: 'Buscar Pacientes', icon: 'Search', route: '/veterinario/pacientes' },
    { label: 'Historiales', icon: 'ClipboardList', route: '/veterinario/historiales' },
  ],
  [ROLES.WAREHOUSE]: [
    { label: 'Dashboard', icon: 'LayoutDashboard', route: '/almacen/dashboard' },
    { label: 'Inventario', icon: 'Package', route: '/almacen/inventario' },
    { label: 'Entrada Mercancía', icon: 'ArrowDownToLine', route: '/almacen/entrada' },
    { label: 'Alertas Stock', icon: 'AlertTriangle', route: '/almacen/alertas' },
    { label: 'Catálogo', icon: 'List', route: '/almacen/catalogo' },
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