/**
 * Bancos universales de Venezuela con código SUDEBAN.
 * Formato de código: 4 dígitos (0102, 0134, etc.)
 * Formato usado en los <select>: "CODIGO - Nombre" cuando se muestran,
 * o solo el nombre cuando se envía al backend (ver datosPago.banco).
 */
export const BANCOS_VENEZUELA = [
  { codigo: '0102', nombre: 'Banco de Venezuela' },
  { codigo: '0104', nombre: 'Venezolano de Crédito' },
  { codigo: '0105', nombre: 'Mercantil Banco' },
  { codigo: '0108', nombre: 'Banco Provincial' },
  { codigo: '0114', nombre: 'Banco del Caribe' },
  { codigo: '0115', nombre: 'Banco Exterior' },
  { codigo: '0116', nombre: 'Banco Occidental de Descuento' },
  { codigo: '0128', nombre: 'Banco Caroní' },
  { codigo: '0134', nombre: 'Banesco Banco Universal' },
  { codigo: '0137', nombre: 'Banco Sofitasa' },
  { codigo: '0138', nombre: 'Banco Plaza' },
  { codigo: '0146', nombre: 'Banco de la Gente Emprendedora' },
  { codigo: '0151', nombre: 'Banco Fondo Común' },
  { codigo: '0156', nombre: '100% Banco' },
  { codigo: '0157', nombre: 'Banco del Sur' },
  { codigo: '0163', nombre: 'Banco del Tesoro' },
  { codigo: '0166', nombre: 'Banco Agrícola de Venezuela' },
  { codigo: '0168', nombre: 'Bancrecer' },
  { codigo: '0169', nombre: 'Mi Banco' },
  { codigo: '0171', nombre: 'Banco Activo' },
  { codigo: '0172', nombre: 'Bancamiga' },
  { codigo: '0173', nombre: 'Banco Internacional de Desarrollo' },
  { codigo: '0174', nombre: 'Banplus' },
  { codigo: '0175', nombre: 'Banco Bicentenario del Pueblo' },
  { codigo: '0177', nombre: 'Banco de la Fuerza Armada Nacional Bolivariana' },
  { codigo: '0190', nombre: 'Banco Nacional de Crédito' },
  { codigo: '0191', nombre: 'Banco del Pueblo Soberano' }
];

/** Helper: devuelve "0102 - Banco de Venezuela..." */
export function formatoBancoSelect(banco) {
  return `${banco.codigo} - ${banco.nombre}`
}