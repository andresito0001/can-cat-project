<template>
  <div class="horario-editor">
    <div class="horario-header">
      <div>
        <h4>Horario de atención</h4>
        <p class="hint">
          Configura los bloques horarios por día. Deja un día sin bloques si no atiende.
        </p>
      </div>
      <div class="bulk-actions">
        <button type="button" class="btn-ghost-sm"
                :disabled="!tieneLunes" @click="copiarLunesATodos">
          <Copy :size="12" /> Copiar lunes a todos
        </button>
        <button type="button" class="btn-ghost-sm"
                :disabled="estaVacio" @click="limpiarTodo">
          <Eraser :size="12" /> Limpiar
        </button>
      </div>
    </div>

    <div class="dias">
      <div v-for="dia in DIAS" :key="dia.num" class="dia-row"
           :class="{ 'dia-inactivo': estado[dia.num].length === 0 }">
        <div class="dia-label">
          <label class="switch">
            <input
              type="checkbox"
              :checked="estado[dia.num].length > 0"
              @change="toggleDia(dia.num, $event.target.checked)"
            />
            <span class="slider"></span>
          </label>
          <span class="dia-nombre">{{ dia.nombre }}</span>
        </div>

        <div class="dia-body">
          <template v-if="estado[dia.num].length === 0">
            <span class="text-muted">Sin atención</span>
            <button type="button" class="btn-add-mini" @click="agregarBloque(dia.num)">
              <Plus :size="12" /> Añadir bloque
            </button>
          </template>

          <template v-else>
            <div v-for="(bloque, idx) in estado[dia.num]" :key="idx" class="bloque">
              <input
                type="time" v-model="bloque.inicio"
                :class="{ 'input-error': bloque.inicio && bloque.fin && bloque.inicio >= bloque.fin }"
                @change="emitir"
              />
              <span class="sep">a</span>
              <input
                type="time" v-model="bloque.fin"
                :class="{ 'input-error': bloque.inicio && bloque.fin && bloque.inicio >= bloque.fin }"
                @change="emitir"
              />
              <button type="button" class="btn-remove"
                      title="Eliminar bloque" @click="quitarBloque(dia.num, idx)">
                <X :size="14" />
              </button>
            </div>
            <button type="button" class="btn-add-mini" @click="agregarBloque(dia.num)">
              <Plus :size="12" /> Añadir otro
            </button>
          </template>
        </div>
      </div>
    </div>

    <p v-if="error" class="error-msg">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { Plus, X, Copy, Eraser } from 'lucide-vue-next'

const DIAS = [
  { num: 1, nombre: 'Lunes' },
  { num: 2, nombre: 'Martes' },
  { num: 3, nombre: 'Miércoles' },
  { num: 4, nombre: 'Jueves' },
  { num: 5, nombre: 'Viernes' },
  { num: 6, nombre: 'Sábado' },
  { num: 7, nombre: 'Domingo' },
]

const props = defineProps({
  modelValue: { type: [Object, null], default: null },
})
const emit = defineEmits(['update:modelValue'])

const estado = ref(normalizar(props.modelValue))
const error = ref('')

const tieneLunes = computed(() => estado.value[1]?.length > 0)
const estaVacio = computed(() =>
  DIAS.every(d => estado.value[d.num].length === 0)
)

// Cuando el padre cambia el valor (ej. abrir modal en modo edición)
watch(() => props.modelValue, (nuevo) => {
  estado.value = normalizar(nuevo)
}, { deep: true })

function normalizar(horario) {
  const base = { 1: [], 2: [], 3: [], 4: [], 5: [], 6: [], 7: [] }
  if (!horario || typeof horario !== 'object') return base
  for (let d = 1; d <= 7; d++) {
    const val = horario[String(d)]
    if (Array.isArray(val)) {
      base[d] = val
        .filter(b => b && typeof b === 'object')
        .map(b => ({
          inicio: b.inicio || '08:00',
          fin: b.fin || '12:00',
        }))
    }
  }
  return base
}

function toggleDia(num, activo) {
  if (activo) {
    estado.value[num] = [{ inicio: '08:00', fin: '12:00' }]
  } else {
    estado.value[num] = []
  }
  emitir()
}

function agregarBloque(num) {
  const ultimo = estado.value[num][estado.value[num].length - 1]
  const inicio = ultimo?.fin || '14:00'
  const fin = sumarHoras(inicio, 4)
  estado.value[num].push({ inicio, fin })
  emitir()
}

function quitarBloque(num, idx) {
  estado.value[num].splice(idx, 1)
  emitir()
}

function copiarLunesATodos() {
  const lunes = JSON.parse(JSON.stringify(estado.value[1] || []))
  for (let d = 2; d <= 5; d++) {   // L a V
    estado.value[d] = JSON.parse(JSON.stringify(lunes))
  }
  emitir()
}

function limpiarTodo() {
  for (let d = 1; d <= 7; d++) estado.value[d] = []
  emitir()
}

function sumarHoras(hhmm, horas) {
  const [h, m] = hhmm.split(':').map(Number)
  const total = (h + horas) * 60 + m
  const nh = Math.floor(total / 60) % 24
  const nm = total % 60
  return `${String(nh).padStart(2, '0')}:${String(nm).padStart(2, '0')}`
}

function emitir() {
  // Validación: fin > inicio en cada bloque
  for (let d = 1; d <= 7; d++) {
    for (const b of estado.value[d]) {
      if (b.inicio && b.fin && b.inicio >= b.fin) {
        error.value = `En ${DIAS.find(x => x.num === d).nombre}: la hora de fin debe ser mayor a la de inicio`
        return
      }
    }
  }
  error.value = ''

  const out = {}
  for (let d = 1; d <= 7; d++) {
    out[String(d)] = estado.value[d].map(b => ({
      inicio: b.inicio,
      fin: b.fin,
    }))
  }
  emit('update:modelValue', out)
}
</script>

<style scoped>
.horario-editor {
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 18px;
  background: #F8FAFC;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.horario-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}
.horario-header h4 { margin: 0; font-size: 14px; font-weight: 700; color: #0F172A; }
.hint { margin: 4px 0 0; font-size: 12px; color: #64748B; }
.bulk-actions { display: flex; gap: 6px; flex-wrap: wrap; }
.btn-ghost-sm {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 6px 10px; font-size: 12px; font-weight: 600;
  background: #fff; border: 1px solid #CBD5E1; border-radius: 8px;
  color: #475569; cursor: pointer; font-family: inherit;
  transition: all .15s;
}
.btn-ghost-sm:hover:not(:disabled) { background: #F1F5F9; border-color: #94A3B8; }
.btn-ghost-sm:disabled { opacity: .45; cursor: not-allowed; }

.dias { display: flex; flex-direction: column; gap: 6px; }
.dia-row {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 14px;
  align-items: flex-start;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #E2E8F0;
  transition: all .15s;
}
.dia-row.dia-inactivo { background: #F8FAFC; border-color: #E2E8F0; }
.dia-label { display: flex; align-items: center; gap: 10px; padding-top: 6px; }
.dia-nombre { font-size: 13px; font-weight: 600; color: #334155; }

/* Switch */
.switch { position: relative; display: inline-block; width: 34px; height: 20px; flex-shrink: 0; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider {
  position: absolute; cursor: pointer; inset: 0;
  background-color: #CBD5E1; border-radius: 20px; transition: .2s;
}
.slider:before {
  position: absolute; content: "";
  height: 14px; width: 14px; left: 3px; bottom: 3px;
  background-color: #fff; border-radius: 50%; transition: .2s;
}
.switch input:checked + .slider { background-color: #0F766E; }
.switch input:checked + .slider:before { transform: translateX(14px); }

.dia-body {
  display: flex; flex-wrap: wrap; align-items: center; gap: 8px;
  min-height: 32px;
}
.text-muted { font-size: 12.5px; color: #94A3B8; font-style: italic; }
.bloque { display: inline-flex; align-items: center; gap: 6px; }
.bloque input[type="time"] {
  padding: 6px 8px; font-size: 12.5px; font-family: inherit;
  border: 1px solid #CBD5E1; border-radius: 7px; background: #fff;
  color: #0F172A; width: 100px;
}
.bloque input[type="time"]:focus {
  outline: none; border-color: #0F766E;
  box-shadow: 0 0 0 3px rgba(15,118,110,.12);
}
.bloque input.input-error { border-color: #EF4444; background: #FEF2F2; }
.sep { font-size: 12px; color: #94A3B8; }
.btn-remove {
  width: 26px; height: 26px; border-radius: 6px;
  border: 1px solid #E2E8F0; background: #fff; color: #94A3B8;
  cursor: pointer; display: inline-flex; align-items: center; justify-content: center;
}
.btn-remove:hover { background: #FEF2F2; color: #EF4444; border-color: #FECACA; }

.btn-add-mini {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 5px 10px; font-size: 11.5px; font-weight: 600;
  background: #F0FDFA; border: 1px dashed #99F6E4; border-radius: 7px;
  color: #0F766E; cursor: pointer; font-family: inherit;
}
.btn-add-mini:hover { background: #CCFBF1; }

.error-msg {
  margin: 0; padding: 10px 14px; background: #FEF2F2; color: #B91C1C;
  border-radius: 8px; font-size: 12.5px; border: 1px solid #FECACA;
}

@media (max-width: 560px) {
  .dia-row { grid-template-columns: 1fr; }
  .dia-label { padding-top: 0; }
}
</style>