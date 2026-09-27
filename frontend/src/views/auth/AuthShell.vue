<template>
  <div class="auth-page">
    <!-- ═══ Fondo: imagen + tratamiento editorial ═══ -->
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
      <!-- ═══ Columna izquierda: marca + mensaje ═══ -->
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

      <!-- ═══ Columna derecha: card ═══ -->
      <main class="form-side">
        <div class="form-card">
          <!-- Marca mobile (oculta en desktop) -->
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
import {
  PawPrint, Heart, Stethoscope, HeartHandshake, ShieldCheck,
} from 'lucide-vue-next'


import './styles/auth-form.css'
defineProps({
  wide: { type: Boolean, default: false },
})

// Coloca la imagen en frontend/public/tsunami-login.webp
const imagenFondo = '/tsunami-login.webp'
const añoActual = computed(() => new Date().getFullYear())
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  background: #042F2E; /* respaldo armonioso mientras carga la imagen */
}

/* ── Fondo ── */
.page-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

/* Scrim lateral en teal profundo: oscuro donde va el texto,
   más abierto a la derecha para que la foto respire */
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

/* Viñeta suave que enfoca el centro y oscurece bordes */
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

/* Grano sutil: elimina el look de gradiente plano */
.page-grain {
  position: absolute;
  inset: 0;
  pointer-events: none;
  opacity: 0.05;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='180' height='180'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.8' numOctaves='2' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
}

/* ── Layout ── */
.page-content {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  align-items: center;
  gap: 48px;
  min-height: 100vh;
  padding: 48px 56px;
  max-width: 1440px;
  margin: 0 auto;
}
.page-content.is-wide {
  grid-template-columns: minmax(0, 0.85fr) minmax(0, 1.15fr);
}

/* ═══ Columna izquierda ═══ */
.hero-side {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 32px;
  max-width: 520px;
  min-height: 70vh;
  color: #FFFFFF;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}
.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.24);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  flex-shrink: 0;
}
.brand-mark-sm {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: #0F766E;
  border: none;
  color: #FFFFFF;
  backdrop-filter: none;
}
.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}
.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: #FFFFFF;
  letter-spacing: -0.01em;
}
.brand-name-sm {
  color: #0F172A;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: -0.01em;
}
.brand-sub {
  font-size: 11.5px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.78);
  letter-spacing: 0.3px;
}

.hero-message {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  align-self: flex-start;
  padding: 5px 12px;
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: 0.7px;
  text-transform: uppercase;
  color: #FFFFFF;
}
.hero-eyebrow svg { color: #5EEAD4; }

.hero-title {
  margin: 0;
  font-size: 36px;
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -0.025em;
  color: #FFFFFF;
  text-shadow: 0 2px 16px rgba(0, 0, 0, 0.35);
  text-wrap: balance;
}
.hero-paragraph {
  margin: 0;
  font-size: 14.5px;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.3);
  max-width: 46ch;
}

.hero-features {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.hero-features li {
  display: flex;
  align-items: center;
  gap: 11px;
  font-size: 13.5px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.94);
}
.hf-icon {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
  border-radius: 9px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(8px);
  color: #5EEAD4;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-credit {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  font-size: 11.5px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.72);
  letter-spacing: 0.2px;
}

/* ═══ Columna derecha ═══ */
.form-side {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  width: 100%;
}

.form-card {
  background: #FFFFFF;
  border-radius: 20px;
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
.is-wide .form-card {
  max-width: 560px;
  padding: 36px 36px 32px;
}

.mobile-brand {
  display: none;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 24px;
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 11.5px;
  color: rgba(255, 255, 255, 0.75);
  flex-wrap: wrap;
}
.footer-sep { color: rgba(255, 255, 255, 0.4); }

/* ═══ Responsive ═══ */
@media (max-width: 1024px) {
  .page-content { gap: 32px; padding: 40px 32px; }
  .hero-title { font-size: 30px; }
  .hero-side { min-height: auto; gap: 24px; }
  .form-card { padding: 32px 28px 28px; }
  .is-wide .form-card { max-width: 520px; }
}

@media (max-width: 768px) {
  .page-content {
    grid-template-columns: 1fr;
    gap: 24px;
    padding: 32px 24px 40px;
    align-items: flex-start;
  }
  .hero-side {
    gap: 18px;
    min-height: auto;
    max-width: 100%;
    text-align: center;
    align-items: center;
  }
  .hero-message { display: none; }
  .hero-credit { display: none; }
  .brand { align-self: center; }

  .form-side { gap: 16px; }
  .form-card {
    padding: 32px 24px 28px;
    border-radius: 18px;
    max-width: 100%;
  }
  .mobile-brand { display: inline-flex; }
}

@media (max-width: 480px) {
  .page-content { padding: 24px 16px 32px; }
  .form-card { padding: 28px 20px 24px; }
}
</style>