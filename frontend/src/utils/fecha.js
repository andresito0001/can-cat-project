const MESES_CORTOS = ['ENE', 'FEB', 'MAR', 'ABR', 'MAY', 'JUN', 'JUL', 'AGO', 'SEP', 'OCT', 'NOV', 'DIC'];
const MESES_LARGOS = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];
const DIAS_SEMANA = ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado'];

// Fecha local de hoy en ISO corto (YYYY-MM-DD), sin desfases de UTC.
export function hoyISO() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

// '2026-09-19' → { dia: 19, mes: 'SEP' } para el badge de fecha de 52px.
export function partesBadgeFecha(fechaISO) {
  if (!fechaISO) return { dia: '--', mes: '—' };
  const mes = Number(String(fechaISO).slice(5, 7));
  return { dia: Number(String(fechaISO).slice(8, 10)), mes: MESES_CORTOS[mes - 1] || '' };
}

// '2026-09-19' → 'sábado 19 de septiembre de 2026'
export function fechaCompleta(fechaISO) {
  if (!fechaISO) return '';
  const [a, m, d] = String(fechaISO).slice(0, 10).split('-').map(Number);
  const fecha = new Date(a, (m || 1) - 1, d || 1);
  return `${DIAS_SEMANA[fecha.getDay()]} ${d} de ${MESES_LARGOS[(m || 1) - 1]} de ${a}`;
}

// '2026-09-19T09:30:00' → '19/09/2026 · 09:30'
export function fechaHoraCorta(fechaHoraISO) {
  if (!fechaHoraISO) return '';
  const [a, m, d] = String(fechaHoraISO).slice(0, 10).split('-');
  const hora = String(fechaHoraISO).slice(11, 16);
  return `${d}/${m}/${a} · ${hora}`;
}

// '09:00:00' → '09:00'
export function horaCorta(hora) {
  return hora ? String(hora).slice(0, 5) : '';
}

// '09:00:00' + '09:30:00' → '09:00 – 09:30' (tolera horaFin ausente)
export function rangoHora(inicio, fin) {
  const i = horaCorta(inicio);
  const f = horaCorta(fin);
  return f ? `${i} – ${f}` : i;
}