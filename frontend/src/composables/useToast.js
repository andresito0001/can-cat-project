import { ref } from 'vue'

const toasts = ref([])
let seq = 0

function push(message, type = 'info', duration = 3500) {
  const id = ++seq
  toasts.value.push({ id, message, type })
  if (duration > 0) {
    setTimeout(() => remove(id), duration)
  }
  return id
}

function remove(id) {
  toasts.value = toasts.value.filter(t => t.id !== id)
}

function clear() {
  toasts.value = []
}

function toastSuccess(message, duration) { return push(message, 'success', duration) }
function toastError(message, duration)   { return push(message, 'error',   duration) }
function toastInfo(message, duration)    { return push(message, 'info',    duration) }
function toastWarning(message, duration) { return push(message, 'warning', duration) }

export function useToast() {
  return {
    toasts,
    push,
    toast: push,
    remove,
    clear,
    toastSuccess,
    toastError,
    toastInfo,
    toastWarning,
  }
}