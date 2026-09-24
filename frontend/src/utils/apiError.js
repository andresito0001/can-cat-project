export function getApiErrorMessage(error) {
  const data = error?.response?.data;
  if (!data) {
    return error?.message || 'No fue posible conectar con el servidor. Intente nuevamente.';
  }
  const msg = data.message;
  if (typeof msg === 'string' && msg.trim()) return msg;
  if (msg && typeof msg === 'object') {
    return Object.entries(msg).map(([campo, texto]) => `${campo}: ${texto}`).join(' · ');
  }
  return data.error || 'Ocurrió un error inesperado.';
}

// Devuelve el mapa {campo: mensaje} cuando el 400 es de validación, o null.
export function getValidationFieldErrors(error) {
  const msg = error?.response?.data?.message;
  if (msg && typeof msg === 'object' && !Array.isArray(msg)) return msg;
  return null;
}