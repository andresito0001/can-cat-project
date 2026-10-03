<template>
  <component
    :is="clickable ? 'button' : 'article'"
    :type="clickable ? 'button' : undefined"
    class="kpi-card"
    :class="[`tone-${tone}`, { 'is-clickable': clickable, 'is-warn': warn }]"
    @click="clickable ? $emit('click') : undefined"
  >
    <div class="kpi-icon">
      <component :is="icon" :size="20" />
    </div>

    <div class="kpi-texto">
      <p class="kpi-value">{{ value }}</p>
      <p class="kpi-label">
        {{ label }}
        <span v-if="hint" class="kpi-hint">{{ hint }}</span>
      </p>
    </div>
  </component>
</template>

<script setup>
defineProps({
  icon:      { type: [Object, Function], required: true },
  value:     { type: [String, Number], default: '—' },
  label:     { type: String, required: true },
  hint:      { type: String, default: '' },
  tone:      { type: String, default: 'brand' }, // brand | info | warning | success | purple | neutral
  clickable: { type: Boolean, default: false },
  warn:      { type: Boolean, default: false },
})
defineEmits(['click'])
</script>

<style scoped>
.kpi-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  width: 100%;
  padding: var(--space-4) var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xs);
  text-align: left;
  font-family: inherit;
  color: inherit;
  transition: border-color var(--duration-base) var(--ease-out),
              box-shadow var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
}
.kpi-card:hover {
  border-color: var(--border-strong);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.kpi-card.is-clickable { cursor: pointer; }
.kpi-card.is-clickable:focus-visible {
  outline: none;
  border-color: var(--brand-700);
  box-shadow: var(--shadow-focus);
}
.kpi-card.is-warn {
  border-color: var(--warning-200);
  background: linear-gradient(135deg, var(--warning-50) 0%, var(--bg-surface) 65%);
}

.kpi-icon {
  width: 46px;
  height: 46px;
  border-radius: var(--radius-xl);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.kpi-texto { min-width: 0; }

.kpi-value {
  margin: 0;
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  line-height: 1.1;
  letter-spacing: var(--tracking-tight);
  font-variant-numeric: tabular-nums;
}
.kpi-card.is-warn .kpi-value { color: var(--warning-700); }

.kpi-label {
  margin: var(--space-1) 0 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  font-weight: var(--font-semibold);
  line-height: var(--leading-snug);
}
.kpi-hint {
  display: block;
  margin-top: 2px;
  font-weight: var(--font-medium);
  color: var(--text-tertiary);
  font-size: var(--text-xs);
}

/* ─── Tonos ─── */
.tone-brand   .kpi-icon { background: var(--brand-50);   color: var(--brand-700); }
.tone-info    .kpi-icon { background: var(--info-50);    color: var(--info-600); }
.tone-warning .kpi-icon { background: var(--warning-50); color: var(--warning-600); }
.tone-success .kpi-icon { background: var(--success-50); color: var(--success-600); }
.tone-purple  .kpi-icon { background: var(--purple-50);  color: var(--purple-600); }
.tone-neutral .kpi-icon { background: var(--neutral-100); color: var(--text-secondary); }
</style>