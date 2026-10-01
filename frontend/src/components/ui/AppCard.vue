<template>
  <section class="card">
    <header v-if="$slots.header || title" class="card-header">
      <div>
        <slot name="header">
          <h3>{{ title }}</h3>
          <p v-if="subtitle" class="card-header-sub">{{ subtitle }}</p>
        </slot>
      </div>
      <slot name="header-actions" />
    </header>

    <div class="card-body" :class="{ 'card-body-sm': compact }">
      <slot />
    </div>

    <footer v-if="$slots.footer" class="card-footer">
      <slot name="footer" />
    </footer>
  </section>
</template>

<script setup>
defineProps({
  title:    { type: String, default: '' },
  subtitle: { type: String, default: '' },
  compact:  { type: Boolean, default: false },
})
</script>
