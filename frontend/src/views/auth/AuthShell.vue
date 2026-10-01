<template>
  <div class="auth-page">
    <div
      class="page-bg"
      :style="{ backgroundImage: `url(${imagenFondo})` }"
      role="img"
      aria-label="Mascota feliz en la clínica veterinaria Can & Cat"
    />
    <div class="page-overlay" />
    <div class="page-vignette" />
    <div class="page-grain" />

    <div class="page-content" :class="{ 'is-wide': wide }">
      <aside class="hero-side">
        <div class="brand">
          <div class="brand-mark"><PawPrint :size="18" /></div>
          <div class="brand-text">
            <span class="brand-name">Can &amp; Cat</span>
            <span class="brand-sub">Clínica Veterinaria</span>
          </div>
        </div>

        <div class="hero-message">
          <p class="hero-eyebrow">
            <Heart :size="11" />
            Bienestar animal
          </p>
          <h2 class="hero-title">
            Ellos dan todo su amor. Nosotros lo cuidamos.
          </h2>
          <p class="hero-paragraph">
            En Can &amp; Cat tratamos a cada mascota como parte de la familia:
            medicina de vanguardia, respeto por su bienestar y mucho cariño.
            Verlos sanos y felices es lo que más nos mueve.
          </p>

          <ul class="hero-features">
            <li>
              <span class="hf-icon"><Stethoscope :size="14" /></span>
              Medicina preventiva y curativa
            </li>
            <li>
              <span class="hf-icon"><HeartHandshake :size="14" /></span>
              Atención cálida, sin estrés
            </li>
            <li>
              <span class="hf-icon"><ShieldCheck :size="14" /></span>
              Equipo veterinario certificado
            </li>
          </ul>
        </div>

        <p class="hero-credit">
          <PawPrint :size="11" />
          <span>Amor por los animales, ciencia en cada visita</span>
        </p>
      </aside>

      <main class="form-side">
        <div class="form-card">
          <div class="mobile-brand">
            <div class="brand-mark brand-mark-sm"><PawPrint :size="16" /></div>
            <span class="brand-name-sm">Can &amp; Cat</span>
          </div>

          <slot />
        </div>

        <footer class="form-footer">
          <span>© {{ añoActual }} Can &amp; Cat</span>
          <span class="footer-sep">·</span>
          <span>Sistema de gestión veterinaria</span>
        </footer>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { PawPrint, Heart, Stethoscope, HeartHandshake, ShieldCheck } from 'lucide-vue-next'

defineProps({
  wide: { type: Boolean, default: false },
})

const imagenFondo = '/tsunami-login.webp'
const añoActual = computed(() => new Date().getFullYear())
</script>

<style scoped>
/* ═══ LAYOUT ═══ */
.auth-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  font-family: var(--font-sans);
  background: var(--brand-900);
}

.page-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}
.page-overlay {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(
    100deg,
    rgba(4, 47, 46, 0.94) 0%,
    rgba(4, 47, 46, 0.80) 32%,
    rgba(19, 78, 74, 0.48) 66%,
    rgba(19, 78, 74, 0.24) 100%
  );
}
.page-vignette {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: radial-gradient(
    130% 115% at 50% 42%,
    transparent 52%,
    rgba(2, 20, 18, 0.55) 100%
  );
}
.page-grain {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.05;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='180' height='180'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
}

.page-content {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  align-items: center;
  gap: var(--space-12);
  min-height: 100vh;
  padding: var(--space-12);
  max-width: 1440px;
  margin: 0 auto;
}
.page-content.is-wide {
  grid-template-columns: minmax(0, 0.85fr) minmax(0, 1.15fr);
}

/* ═══ HERO ═══ */
.hero-side {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: var(--space-8);
  max-width: 520px;
  min-height: 70vh;
  color: var(--text-inverse);
}
.brand { display: inline-flex; align-items: center; gap: var(--space-3); }
.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: var(--radius-xl);
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
  flex-shrink: 0;
}
.brand-mark-sm {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-lg);
  background: var(--brand-700);
  border: none;
  backdrop-filter: none;
}
.brand-text { display: flex; flex-direction: column; line-height: 1.15; }
.brand-name {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-inverse);
  letter-spacing: var(--tracking-tight);
}
.brand-name-sm {
  color: var(--text-primary);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
}
.brand-sub {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: rgba(255, 255, 255, 0.78);
}

.hero-message { display: flex; flex-direction: column; gap: var(--space-3); }
.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  align-self: flex-start;
  padding: var(--space-1) var(--space-3);
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.20);
  border-radius: var(--radius-full);
  font-size: var(--text-2xs);
  font-weight: var(--font-bold);
  letter-spacing: 0.07em;
  text-transform: uppercase;
  color: var(--text-inverse);
}
.hero-eyebrow svg { color: var(--brand-300); }

.hero-title {
  margin: 0;
  font-size: 36px;
  font-weight: var(--font-bold);
  line-height: var(--leading-tight);
  letter-spacing: var(--tracking-tight);
  color: var(--text-inverse);
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.35);
  text-wrap: balance;
}
.hero-paragraph {
  margin: 0;
  font-size: var(--text-lg);
  line-height: var(--leading-relaxed);
  color: rgba(255, 255, 255, 0.90);
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.30);
  max-width: 46ch;
}
.hero-features {
  list-style: none;
  margin: var(--space-2) 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.hero-features li {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-md);
  font-weight: var(--font-medium);
  color: rgba(255, 255, 255, 0.94);
}
.hf-icon {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  color: var(--brand-300);
  display: flex;
  align-items: center;
  justify-content: center;
}
.hero-credit {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  margin: 0;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: rgba(255, 255, 255, 0.72);
}

/* ═══ FORM SIDE ═══ */
.form-side {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-5);
  width: 100%;
}
.form-card {
  background: var(--bg-surface);
  border-radius: var(--radius-4xl);
  padding: 40px 36px 34px;
  width: 100%;
  max-width: 440px;
  display: flex;
  flex-direction: column;
  box-shadow:
    0 1px 2px rgba(2, 20, 18, 0.08),
    0 12px 36px -12px rgba(2, 20, 18, 0.25),
    0 40px 90px -32px rgba(2, 20, 18, 0.42);
}
.page-content.is-wide .form-card {
  max-width: 560px;
  padding: var(--space-10);
}

.mobile-brand {
  display: none;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  margin-bottom: var(--space-6);
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: rgba(255, 255, 255, 0.75);
  flex-wrap: wrap;
}
.footer-sep { color: rgba(255, 255, 255, 0.40); }

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .page-content { gap: var(--space-8); padding: var(--space-10) var(--space-8); }
  .hero-title { font-size: 30px; }
  .hero-side { min-height: auto; gap: var(--space-6); }
  .form-card { padding: var(--space-8) var(--space-7) var(--space-7); }
  .page-content.is-wide .form-card { max-width: 520px; }
}

@media (max-width: 768px) {
  .page-content {
    grid-template-columns: 1fr;
    gap: var(--space-6);
    padding: var(--space-8) var(--space-6) var(--space-10);
    align-items: flex-start;
  }
  .hero-side {
    gap: var(--space-5);
    min-height: auto;
    max-width: 100%;
    text-align: center;
    align-items: center;
  }
  .hero-message { display: none; }
  .hero-credit { display: none; }
  .brand { align-self: center; }

  .form-side { gap: var(--space-4); }
  .form-card {
    padding: var(--space-8) var(--space-6) var(--space-7);
    border-radius: var(--radius-3xl);
    max-width: 100%;
  }
  .mobile-brand { display: inline-flex; }
}

@media (max-width: 480px) {
  .page-content { padding: var(--space-6) var(--space-4) var(--space-8); }
  .form-card { padding: var(--space-7) var(--space-5) var(--space-6); }
}
</style>