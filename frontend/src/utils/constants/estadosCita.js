// Matriz espejo de TRANSICIONES_PERMITIDAS del backend (validación real en server)
export const TRANSICIONES_ESTADOS = {
  Pendiente_Pago: ['Confirmada', 'Cancelada'],
  Confirmada:     ['En_Atencion', 'Cancelada'],
  En_Atencion:    ['Completada'],
  Completada:     [],
  Cancelada:      [],
}

export const TRANSICIONES_MANUALES_POR_ROL = {
  Recepcionista: {
    Pendiente_Pago: ['Cancelada'],
    Confirmada:     ['Cancelada'],
    En_Atencion:    [],
    Completada:     [],
    Cancelada:      [],
  },
  Administrador: {
    Pendiente_Pago: ['Cancelada'],
    Confirmada:     ['Cancelada'],
    En_Atencion:    [],
    Completada:     [],
    Cancelada:      [],
  },
}

export const ESTADO_LABEL = {
  Pendiente_Pago: 'Pendiente de Pago',
  Confirmada:     'Confirmada',
  En_Atencion:    'En Atención',
  Completada:     'Completada',
  Cancelada:      'Cancelada',
}

export const ESTADO_COLOR = {
  Pendiente_Pago: '#FFC107',
  Confirmada:     '#28A745',
  En_Atencion:    '#FD7E14',
  Completada:     '#6C757D',
  Cancelada:      '#DC3545',
}

export function transicionesDisponibles(rol, estadoCita) {
  const mapa = TRANSICIONES_MANUALES_POR_ROL[rol] || {}
  return mapa[estadoCita] || []
}