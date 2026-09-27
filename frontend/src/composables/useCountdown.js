import { ref, computed, onBeforeUnmount, watch } from 'vue'

/**
 * Cuenta regresiva reactiva desde una fecha ISO.
 * Uso:
 *   const { restanteMs, texto, expirado, urgente } = useCountdown(fechaIso)
 */
export function useCountdown(fechaObjetivoRef) {
  const ahora = ref(Date.now())
  let intervalId = null

  function tick() {
    ahora.value = Date.now()
  }

  function iniciar() {
    detener()
    tick()
    intervalId = setInterval(tick, 1000)
  }

  function detener() {
    if (intervalId) {
      clearInterval(intervalId)
      intervalId = null
    }
  }

  // Arranca/detiene según si hay fecha válida
  if (typeof fechaObjetivoRef === 'object' && 'value' in fechaObjetivoRef) {
    watch(fechaObjetivoRef, (v) => {
      if (v) iniciar()
      else detener()
    }, { immediate: true })
  } else {
    iniciar()
  }

  onBeforeUnmount(detener)

  const objetivoMs = computed(() => {
    const v = typeof fechaObjetivoRef === 'object' && 'value' in fechaObjetivoRef
      ? fechaObjetivoRef.value
      : fechaObjetivoRef
    if (!v) return null
    const ms = new Date(v).getTime()
    return Number.isNaN(ms) ? null : ms
  })

  const restanteMs = computed(() => {
    if (objetivoMs.value == null) return null
    return Math.max(0, objetivoMs.value - ahora.value)
  })

  const expirado = computed(() => restanteMs.value !== null && restanteMs.value <= 0)

  // Verde > 15 min, ámbar 5-15, rojo < 5 min
  const urgente = computed(() => {
    if (restanteMs.value == null) return 'none'
    const min = restanteMs.value / 60000
    if (min <= 5) return 'critical'
    if (min <= 15) return 'warning'
    return 'ok'
  })

  const texto = computed(() => {
    if (restanteMs.value == null) return ''
    const totalSeg = Math.floor(restanteMs.value / 1000)
    const h = Math.floor(totalSeg / 3600)
    const m = Math.floor((totalSeg % 3600) / 60)
    const s = totalSeg % 60
    const pad = (n) => String(n).padStart(2, '0')
    if (h > 0) return `${h}:${pad(m)}:${pad(s)}`
    return `${pad(m)}:${pad(s)}`
  })

  return { restanteMs, texto, expirado, urgente }
}