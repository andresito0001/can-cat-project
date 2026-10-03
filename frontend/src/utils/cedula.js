/**
 * Utilidades para formatear y validar cédulas/RIF venezolanos.
 * Soporta tipos: V (venezolano), E (extranjero), J (jurídico), P (pasaporte), G (gobierno).
 */

const TIPOS_VALIDOS = ['V', 'E', 'J', 'P', 'G']

/**
 * Formatea un string al patrón T-12345678 mientras el usuario escribe.
 * - Si el usuario escribe solo números, asume "V".
 * - Limita a 9 dígitos.
 * - Preserva el prefijo si ya está presente.
 *
 * @example
 * formatearCedula('12345678')      // "V-12345678"
 * formatearCedula('v12345678')     // "V-12345678"
 * formatearCedula('E-1234')        // "E-1234"
 * formatearCedula('V-')            // "V-"
 * formatearCedula('')              // ""
 */
export function formatearCedula(valor) {
  if (valor === null || valor === undefined) return ''
  const raw = String(valor).toUpperCase().trim()
  if (!raw) return ''

  // Detectar tipo (si viene) y extraer los dígitos
  let tipo = null
  let resto = raw
  for (const t of TIPOS_VALIDOS) {
    if (raw.startsWith(t)) {
      tipo = t
      resto = raw.slice(1)
      break
    }
  }

  // Extraer solo dígitos
  const digitos = resto.replace(/\D/g, '').slice(0, 9)

  // Si no había tipo, asumir V por defecto
  const tipoFinal = tipo ?? 'V'

  if (!digitos) return `${tipoFinal}-`
  return `${tipoFinal}-${digitos}`
}

/**
 * Valida una cédula en formato T-12345678 (6 a 9 dígitos).
 * @returns {string|null} Mensaje de error o null si es válida.
 */
export function validarCedula(valor) {
  if (!valor) return 'La cédula es obligatoria'
  const raw = String(valor).trim()
  const match = raw.match(/^([VEJPG])-(\d{6,9})$/)
  if (!match) return 'Formato inválido. Ej: V-12345678'
  return null
}

/**
 * Extrae tipo y número por separado. Útil para enviar al backend si algún día
 * se separan. Por ahora devuelve el string completo tal cual.
 */
export function parsearCedula(valor) {
  const raw = String(valor || '').trim()
  const match = raw.match(/^([VEJPG])-(\d{6,9})$/)
  if (!match) return { tipo: null, numero: null, formateada: raw }
  return {
    tipo: match[1],
    numero: match[2],
    formateada: `${match[1]}-${match[2]}`,
  }
}