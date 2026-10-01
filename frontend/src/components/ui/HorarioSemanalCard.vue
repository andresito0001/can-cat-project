<template>
  <div class="horario-card">
    <!-- Días -->
    <ul class="horario-dias">
      <li
        v-for="dia in dias"
        :key="dia.num"
        class="dia-row"
        :class="{ 'is-inactive': !dia.bloques.length }"
      >
        <span class="dia-nombre">{{ dia.nombre }}</span>
        <div class="dia-bloques">
          <template v-if="dia.bloques.length">
            <span
              v-for="(b, i) in dia.bloques"
              :key="i"
              class="bloque-pill"
            >
              {{ b.inicio }} <span class="bloque-sep">–</span> {{ b.fin }}
            </span>
          </template>
          <span v-else class="sin-atencion">Sin atención</span>
        </div>
      </li>
    </ul>

    <!-- Footer con métricas -->
    <footer v-if="diasActivos > 0" class="horario-footer">
      <div class="footer-stat">
        <span class="stat-label">Días activos</span>
        <span class="stat-value">
          {{ diasActivos }} <small>de 7</small>
        </span>
      </div>
      <div class="footer-divider" aria-hidden="true" />
      <div class="footer-stat">
        <span class="stat-label">Total semanal</span>
        <span class="stat-value">{{ totalHorasTexto }}</span>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  horario: { type: [Object, null], default: null },
})

const DIAS = [
  { num: 1, nombre: 'Lunes' },
  { num: 2, nombre: 'Martes' },
  { num: 3, nombre: 'Miércoles' },
  { num: 4, nombre: 'Jueves' },
  { num: 5, nombre: 'Viernes' },
  { num: 6, nombre: 'Sábado' },
  { num: 7, nombre: 'Domingo' },
]

const dias = computed(() => {
  const h = props.horario || {}
  return DIAS.map((d) => {
    const raw = h[String(d.num)]
    const bloques = Array.isArray(raw) ? raw : []
    const limpios = bloques
      .filter((b) => b && b.inicio && b.fin)
      .map((b) => ({
        inicio: String(b.inicio).slice(0, 5),
        fin: String(b.fin).slice(0, 5),
      }))
    return { num: d.num, nombre: d.nombre, bloques: limpios }
  })
})

const diasActivos = computed(
  () => dias.value.filter((d) => d.bloques.length > 0).length
)

const totalMinutos = computed(() => {
  let total = 0
  for (const d of dias.value) {
    for (const b of d.bloques) {
      const [hi, mi] = b.inicio.split(':').map(Number)
      const [hf, mf] = b.fin.split(':').map(Number)
      const diff = hf * 60 + mf - (hi * 60 + mi)
      if (diff > 0) total += diff
    }
  }
  return total
})

const totalHorasTexto = computed(() => {
  const min = totalMinutos.value
  if (min === 0) return '0 h'
  const h = Math.floor(min / 60)
  const m = min % 60
  if (h === 0) return `${m} min`
  if (m === 0) return `${h} h`
  return `${h} h ${m} min`
})
</script>

<style scoped>
.horario-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

/* ═══ DÍAS ═══ */
.horario-dias {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dia-row {
  display: grid;
  grid-template-columns: 110px 1fr;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-3) var(--space-4);
  background: var(--bg-surface-alt);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  transition: background-color var(--duration-fast) var(--ease-out);
}
.dia-row:hover { background: var(--bg-surface); }

.dia-row.is-inactive {
  background: var(--neutral-50);
  border-style: dashed;
  opacity: 0.7;
}

.dia-nombre {
  font-size: var(--text-md);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  letter-spacing: -0.01em;
}
.dia-row.is-inactive .dia-nombre { color: var(--text-tertiary); }

.dia-bloques {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  justify-content: flex-end;
}

.bloque-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px var(--space-3);
  border-radius: var(--radius-full);
  background: var(--brand-50);
  border: 1px solid var(--brand-200);
  color: var(--brand-700);
  font-size: var(--text-sm);
  font-weight: var(--font-bold);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.bloque-sep {
  color: var(--brand-500);
  font-weight: var(--font-medium);
}

.sin-atencion {
  font-size: var(--text-sm);
  font-style: italic;
  color: var(--text-tertiary);
  font-weight: var(--font-medium);
}

/* ═══ FOOTER ═══ */
.horario-footer {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: var(--space-4);
  align-items: center;
  padding: var(--space-4) var(--space-5);
  background: linear-gradient(135deg, var(--brand-50) 0%, var(--bg-surface) 70%);
  border: 1px solid var(--brand-100);
  border-radius: var(--radius-xl);
}

.footer-stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: center;
  text-align: center;
}
.stat-label {
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--text-secondary);
}
.stat-value {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--brand-700);
  font-variant-numeric: tabular-nums;
  line-height: 1;
}
.stat-value small {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  margin-left: 2px;
}

.footer-divider {
  width: 1px;
  height: 32px;
  background: var(--border-subtle);
}

/* ═══ RESPONSIVE ═══ */
@media (max-width: 480px) {
  .dia-row {
    grid-template-columns: 1fr;
    gap: var(--space-2);
    padding: var(--space-3);
  }
  .dia-bloques { justify-content: flex-start; }
  .horario-footer { padding: var(--space-3) var(--space-4); }
}
</style>