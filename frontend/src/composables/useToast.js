import { ref } from 'vue';

const toasts = ref([]);
let contador = 0;

export function useToast() {
  function mostrarToast(mensaje, tipo = 'success', duracion = 4200) {
    const id = ++contador;
    toasts.value.push({ id, mensaje, tipo });
    setTimeout(() => cerrarToast(id), duracion);
  }
  function cerrarToast(id) {
    toasts.value = toasts.value.filter((t) => t.id !== id);
  }
  return {
    toasts,
    cerrarToast,
    toastExito: (m) => mostrarToast(m, 'success'),
    toastError: (m) => mostrarToast(m, 'error'),
    toastInfo: (m) => mostrarToast(m, 'info'),
  };
}