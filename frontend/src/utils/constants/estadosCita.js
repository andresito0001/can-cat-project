// Matriz espejo de TRANSICIONES_PERMITIDAS del backend (validación real en server)
export const TRANSICIONES_ESTADOS = {
  Pendiente_Pago: ['Pagada', 'Confirmada', 'Cancelada'],
  Pagada: ['Confirmada', 'Cancelada'],
  Confirmada: ['En_Atencion', 'Completada', 'Cancelada'],
  En_Atencion: ['Completada', 'Cancelada'],
  Completada: [],
  Cancelada: []
}

export const ESTADO_LABEL = {
  Pendiente_Pago: 'Pendiente de Pago',
  Pagada: 'Pagada',
  Confirmada: 'Confirmada',
  En_Atencion: 'En Atención',
  Completada: 'Completada',
  Cancelada: 'Cancelada'
}

export const ESTADO_COLOR = {
  Pendiente_Pago: '#FFC107',
  Pagada: '#17A2B8',
  Confirmada: '#28A745',
  En_Atencion: '#FD7E14',
  Completada: '#6C757D',
  Cancelada: '#DC3545'
}