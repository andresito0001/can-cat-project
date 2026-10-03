<template>
  <div ref="wrapperRef" class="date-picker" :class="{ 'is-open': abierto }">
    <button
      ref="triggerRef"
      type="button"
      class="dp-trigger"
      :class="{ 'is-invalid': !!error, 'is-disabled': disabled, 'has-value': !!modelValue }"
      :disabled="disabled"
      :aria-expanded="abierto"
      aria-haspopup="dialog"
      @click="toggle"
    >
      <CalendarDays :size="15" class="dp-trigger-icon" />
      <span class="dp-trigger-value" :class="{ 'is-placeholder': !modelValue }">
        {{ modelValue ? formatearVisual(modelValue) : placeholder }}
      </span>
      <ChevronDown
        :size="14"
        class="dp-trigger-chevron"
        :class="{ 'is-open': abierto }"
      />
    </button>

    <Teleport to="body">
      <Transition name="dp-pop">
        <div
          v-if="abierto"
          ref="popoverRef"
          class="dp-popover"
          :style="popoverStyle"
          role="dialog"
          aria-modal="false"
          @click.stop
        >
          <!-- Header: navegación de mes con selectores -->
          <header class="dp-header">
            <button
              type="button"
              class="dp-nav"
              :disabled="!puedeRetroceder"
              aria-label="Mes anterior"
              @click="moverMes(-1)"
            >
              <ChevronLeft :size="16" />
            </button>
          
            <div class="dp-selectors">
              <select v-model.number="mesSeleccionado" class="dp-select" aria-label="Mes">
                <option v-for="(nombre, i) in MESES" :key="i" :value="i">{{ nombre }}</option>
              </select>
              <select v-model.number="anioSeleccionado" class="dp-select" aria-label="Año">
                <option v-for="anio in aniosDisponibles" :key="anio" :value="anio">{{ anio }}</option>
              </select>
            </div>
          
            <button
              type="button"
              class="dp-nav"
              :disabled="!puedeAvanzar"
              aria-label="Mes siguiente"
              @click="moverMes(1)"
            >
              <ChevronRight :size="16" />
            </button>
          </header>

          <!-- Días de la semana -->
          <div class="dp-weekdays">
            <span v-for="d in DIAS_SEMANA" :key="d">{{ d }}</span>
          </div>

          <!-- Grilla de días -->
          <div class="dp-grid">
            <button
              v-for="(day, i) in diasDelMes"
              :key="i"
              type="button"
              class="dp-day"
              :class="{
                'is-other-month': !day.inMonth,
                'is-today': day.esHoy,
                'is-selected': day.iso === modelValue,
                'is-disabled': day.deshabilitado,
              }"
              :disabled="day.deshabilitado || !day.inMonth"
              @click="seleccionar(day)"
            >
              {{ day.dia }}
            </button>
          </div>

          <!-- Footer: atajos -->
          <footer class="dp-footer">
            <button type="button" class="dp-shortcut" :disabled="!hoySeleccionable" @click="irAHoy">
              Hoy
            </button>
            <button type="button" class="dp-shortcut" :disabled="!modelValue" @click="limpiar">
              Limpiar
            </button>
          </footer>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { CalendarDays, ChevronDown, ChevronLeft, ChevronRight } from 'lucide-vue-next'

const props = defineProps({
  modelValue:   { type: String, default: '' },      // ISO YYYY-MM-DD
  placeholder:  { type: String, default: 'Seleccionar fecha' },
  min:          { type: String, default: '' },      // ISO
  max:          { type: String, default: '' },      // ISO
  disabled:     { type: Boolean, default: false },
  error:        { type: [String, Boolean], default: '' },
  mesesAhead:   { type: Number, default: 240 },      // cuántos meses al futuro
  mesesBehind:  { type: Number, default: 1200 },     // cuántos al pasado
})
const emit = defineEmits(['update:modelValue', 'change'])

/* ─── Constantes ─── */
const DIAS_SEMANA = ['Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sá', 'Do']
const MESES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
]
const MESES_CORTOS = ['ene','feb','mar','abr','may','jun','jul','ago','sep','oct','nov','dic']

/* ─── Estado ─── */
const abierto = ref(false)
const wrapperRef = ref(null)
const triggerRef = ref(null)
const popoverRef = ref(null)
const mesVisible = ref(new Date())
const popoverStyle = ref({})

/* ─── Helpers ─── */
function isoHoy() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
function parseISO(iso) {
  const [y, m, d] = String(iso).split('-').map(Number)
  return new Date(y, m - 1, d)
}
function toISO(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function formatearVisual(iso) {
  if (!iso) return ''
  const d = parseISO(iso)
  return `${d.getDate()} ${MESES_CORTOS[d.getMonth()]} ${d.getFullYear()}`
}

/* ─── Navegación de mes (MEJORADO: Selectores) ─── */
const mesSeleccionado = computed({
  get: () => mesVisible.value.getMonth(),
  set: (m) => {
    const d = new Date(mesVisible.value)
    d.setMonth(m)
    d.setDate(1) // UX FIX: Evita que cambiar a febrero desde el 31 de marzo salte a marzo/abril
    mesVisible.value = d
  },
})

const anioSeleccionado = computed({
  get: () => mesVisible.value.getFullYear(),
  set: (a) => {
    const d = new Date(mesVisible.value)
    d.setFullYear(a)
    d.setDate(1) // UX FIX: Evita bugs de desbordamiento de fechas
    mesVisible.value = d
  },
})

const aniosDisponibles = computed(() => {
  const anioBase = new Date().getFullYear()
  const ANIOS_ATRAS = 100
  const ANIOS_ADELANTE = 20
  const arr = []
  for (let a = anioBase + ANIOS_ADELANTE; a >= anioBase - ANIOS_ATRAS; a--) {
    arr.push(a)
  }
  return arr
})

const puedeRetroceder = computed(() => {
  if (!props.mesesBehind) return true
  const limit = new Date()
  limit.setMonth(limit.getMonth() - props.mesesBehind)
  return mesVisible.value > new Date(limit.getFullYear(), limit.getMonth(), 1)
})

const puedeAvanzar = computed(() => {
  if (!props.mesesAhead) return true
  const limit = new Date()
  limit.setMonth(limit.getMonth() + props.mesesAhead)
  return mesVisible.value < new Date(limit.getFullYear(), limit.getMonth(), 1)
})

function moverMes(delta) {
  const d = new Date(mesVisible.value)
  d.setMonth(d.getMonth() + delta)
  mesVisible.value = d
}

/* ─── Construcción de la grilla ─── */
const diasDelMes = computed(() => {
  const y = mesVisible.value.getFullYear()
  const m = mesVisible.value.getMonth()
  const primerDia = new Date(y, m, 1)
  const ultimoDia = new Date(y, m + 1, 0).getDate()
  const offset = (primerDia.getDay() + 6) % 7
  const hoyIso = isoHoy()
  const minIso = props.min || null
  const maxIso = props.max || null

  const dias = []
  for (let i = offset - 1; i >= 0; i--) {
    const d = new Date(y, m, -i)
    dias.push({ dia: d.getDate(), iso: toISO(d), inMonth: false, esHoy: false, deshabilitado: false })
  }
  // Días del mes
  for (let d = 1; d <= ultimoDia; d++) {
    const date = new Date(y, m, d)
    const iso = toISO(date)
    let deshabilitado = false
    if (minIso && iso < minIso) deshabilitado = true
    if (maxIso && iso > maxIso) deshabilitado = true
    dias.push({
      dia: d,
      iso,
      inMonth: true,
      esHoy: iso === hoyIso,
      deshabilitado,
    })
  }
  // Relleno final
  const restante = (7 - (dias.length % 7)) % 7
  for (let i = 1; i <= restante; i++) {
    const d = new Date(y, m + 1, i)
    dias.push({ dia: d.getDate(), iso: toISO(d), inMonth: false, esHoy: false, deshabilitado: true })
  }
  return dias
})

/* ─── Selección ─── */
function seleccionar(day) {
  if (day.deshabilitado) return
  // Si el día es de otro mes, navegamos y seleccionamos
  
  if (!day.inMonth) {
    const nuevaFecha = parseISO(day.iso)
    mesVisible.value = new Date(nuevaFecha.getFullYear(), nuevaFecha.getMonth(), 1)
  }

  emit('update:modelValue', day.iso)
  emit('change', day.iso)
  cerrar()
}

const hoySeleccionable = computed(() => {
  const iso = isoHoy()
  if (props.min && iso < props.min) return false
  if (props.max && iso > props.max) return false
  return true
})

function irAHoy() {
  if (!hoySeleccionable.value) return
  emit('update:modelValue', isoHoy())
  emit('change', isoHoy())
  cerrar()
}

function limpiar() {
  emit('update:modelValue', '')
  emit('change', '')
  cerrar()
}

/* ─── Abrir / cerrar ─── */
async function toggle() {
  if (props.disabled) return
  abierto.value = !abierto.value
  if (abierto.value) {
    const base = props.modelValue ? parseISO(props.modelValue) : new Date()
    mesVisible.value = new Date(base.getFullYear(), base.getMonth(), 1)
    await nextTick()
    posicionarPopover()
  }
}

function cerrar() {
  abierto.value = false
}

/* ─── Posicionamiento del popover ─── */
function posicionarPopover() {
  const trigger = triggerRef.value
  if (!trigger) return
  const rect = trigger.getBoundingClientRect()
  const POP_W = 300
  const POP_H = 360
  const MARGEN = 8

  let top = rect.bottom + MARGEN
  let left = rect.left

  if (left + POP_W > window.innerWidth - MARGEN) {
    left = window.innerWidth - POP_W - MARGEN
  }
  if (left < MARGEN) left = MARGEN

  if (top + POP_H > window.innerHeight - MARGEN && rect.top > POP_H + MARGEN) {
    top = rect.top - POP_H - MARGEN
  }

  popoverStyle.value = {
    position: 'fixed',
    top: `${top}px`,
    left: `${left}px`,
    width: `${POP_W}px`,
    zIndex: 9999,
  }
}

/* ─── Click fuera / Escape / scroll ─── */
function onClickFuera(e) {
  if (!abierto.value) return
  if (wrapperRef.value?.contains(e.target)) return
  if (popoverRef.value?.contains(e.target)) return
  cerrar()
}

function onKeydown(e) {
  if (e.key === 'Escape' && abierto.value) cerrar()
}

function onScrollReposition() {
  if (abierto.value) posicionarPopover()
}

watch(() => props.disabled, (v) => { if (v) cerrar() })

onMounted(() => {
  document.addEventListener('mousedown', onClickFuera)
  document.addEventListener('keydown', onKeydown)
  window.addEventListener('resize', onScrollReposition)
  window.addEventListener('scroll', onScrollReposition, true)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', onClickFuera)
  document.removeEventListener('keydown', onKeydown)
  window.removeEventListener('resize', onScrollReposition)
  window.removeEventListener('scroll', onScrollReposition, true)
})
</script>

<style scoped>
.date-picker { position: relative; width: 100%; }

/* ═══ Trigger ═══ */
.dp-trigger {
  display: flex;
  align-items: center;
  gap: var(--space-3, 0.75rem);
  width: 100%;
  height: var(--input-h-md, 40px);
  padding: 0 var(--space-4, 1rem);
  background: var(--bg-surface, #ffffff);
  border: 1px solid var(--border-strong, #e2e8f0);
  border-radius: var(--radius-lg, 10px);
  font-family: inherit;
  font-size: var(--text-base, 0.875rem);
  color: var(--text-primary, #0f172a);
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
  text-align: left;
}
.dp-trigger:hover:not(.is-disabled) {
  border-color: var(--neutral-400, #cbd5e1);
}
.dp-trigger:focus-visible,
.date-picker.is-open .dp-trigger {
  outline: none;
  border-color: var(--brand-700, #0f766e);
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.1);
}
.dp-trigger.is-invalid {
  border-color: var(--danger-500, #ef4444);
  background: var(--danger-50, #fef2f2);
}
.dp-trigger.is-disabled {
  background: var(--neutral-50, #f8fafc);
  color: var(--text-tertiary, #94a3b8);
  cursor: not-allowed;
}

.dp-trigger-icon { color: var(--text-tertiary, #94a3b8); flex-shrink: 0; }
.dp-trigger.has-value .dp-trigger-icon { color: var(--brand-700, #0f766e); }

.dp-trigger-value {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.dp-trigger-value.is-placeholder { color: var(--text-tertiary, #94a3b8); }

.dp-trigger-chevron {
  color: var(--text-tertiary, #94a3b8);
  flex-shrink: 0;
  transition: transform 0.2s ease;
}
.dp-trigger-chevron.is-open { transform: rotate(180deg); color: var(--brand-700, #0f766e); }

/* ═══ Popover (Teleport) ═══ */
.dp-popover {
  background: var(--bg-surface, #ffffff);
  border: 1px solid var(--border-subtle, #f1f5f9);
  border-radius: var(--radius-2xl, 16px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  padding: var(--space-3, 0.75rem);
  font-family: var(--font-sans, inherit);
  color: var(--text-primary, #0f172a);
  user-select: none;
}

/* Header con Selectores */
.dp-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2, 0.5rem);
  margin-bottom: var(--space-3, 0.75rem);
}

.dp-selectors {
  display: flex;
  gap: var(--space-1, 0.25rem);
  flex: 1;
  min-width: 0;
  justify-content: center;
}

.dp-select {
  flex: 1;
  min-width: 0;
  height: 32px;
  padding: 0 1.5rem 0 var(--space-2, 0.5rem);
  border: 1px solid transparent;
  background: transparent;
  border-radius: var(--radius-md, 8px);
  font-family: inherit;
  font-size: var(--text-sm, 0.875rem);
  font-weight: var(--font-bold, 700);
  color: var(--text-primary, #0f172a);
  cursor: pointer;
  text-align: center;
  text-align-last: center;
  transition: all 0.2s ease;
  appearance: none;
  -webkit-appearance: none;
  /* Flecha personalizada para consistencia cross-browser */
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%2364748b' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 0.25rem center;
  background-repeat: no-repeat;
  background-size: 1.25em 1.25em;
}

.dp-select:hover {
  background: var(--brand-50, #f0fdfa);
  border-color: var(--brand-100, #ccfbf1);
}

.dp-select:focus {
  outline: none;
  border-color: var(--brand-700, #0f766e);
  background: var(--bg-surface, #ffffff);
  box-shadow: 0 0 0 3px rgba(15, 118, 110, 0.1);
}

.dp-nav {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md, 8px);
  border: 1px solid transparent;
  background: transparent;
  color: var(--text-secondary, #64748b);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}
.dp-nav:hover:not(:disabled) {
  background: var(--brand-50, #f0fdfa);
  color: var(--brand-700, #0f766e);
  border-color: var(--brand-100, #ccfbf1);
}
.dp-nav:disabled { opacity: 0.35; cursor: not-allowed; }

/* Weekdays */
.dp-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
  margin-bottom: var(--space-1, 0.25rem);
}
.dp-weekdays span {
  text-align: center;
  font-size: var(--text-2xs, 0.65rem);
  font-weight: var(--font-bold, 700);
  color: var(--text-tertiary, #94a3b8);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: var(--space-1, 0.25rem) 0;
}

/* Grid */
.dp-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}
.dp-day {
  aspect-ratio: 1 / 1;
  border: none;
  background: transparent;
  border-radius: var(--radius-md, 8px);
  font-family: inherit;
  font-size: var(--text-sm, 0.875rem);
  font-weight: var(--font-semibold, 600);
  color: var(--text-primary, #0f172a);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  padding: 0;
}
.dp-day:hover:not(:disabled) {
  background: var(--brand-50, #f0fdfa);
  color: var(--brand-700, #0f766e);
}
.dp-day.is-today {
  color: var(--brand-700, #0f766e);
  font-weight: var(--font-bold, 700);
  box-shadow: inset 0 0 0 1.5px var(--brand-200, #99f6e4);
}
.dp-day.is-selected {
  background: var(--brand-700, #0f766e);
  color: var(--text-inverse, #ffffff);
  font-weight: var(--font-bold, 700);
  box-shadow: 0 4px 10px -3px rgba(15, 118, 110, 0.45);
}
.dp-day.is-other-month {
  color: var(--neutral-300, #cbd5e1);
  cursor: default;
}
.dp-day.is-disabled {
  color: var(--neutral-300, #cbd5e1);
  cursor: not-allowed;
  text-decoration: line-through;
  text-decoration-thickness: 1px;
}

/* Footer */
.dp-footer {
  display: flex;
  gap: var(--space-2, 0.5rem);
  justify-content: space-between;
  margin-top: var(--space-3, 0.75rem);
  padding-top: var(--space-2, 0.5rem);
  border-top: 1px solid var(--border-subtle, #f1f5f9);
}
.dp-shortcut {
  background: none;
  border: none;
  padding: var(--space-1, 0.25rem) var(--space-3, 0.75rem);
  border-radius: var(--radius-md, 8px);
  font-family: inherit;
  font-size: var(--text-sm, 0.875rem);
  font-weight: var(--font-bold, 700);
  color: var(--brand-700, #0f766e);
  cursor: pointer;
  transition: background-color 0.2s ease;
}
.dp-shortcut:hover:not(:disabled) { background: var(--brand-50, #f0fdfa); }
.dp-shortcut:disabled { color: var(--text-tertiary, #94a3b8); cursor: not-allowed; }

/* Pop transition */
.dp-pop-enter-active,
.dp-pop-leave-active {
  transition: opacity 0.2s ease, transform 0.2s cubic-bezier(0.16, 1, 0.3, 1);
}
.dp-pop-enter-from,
.dp-pop-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.98);
}
</style>