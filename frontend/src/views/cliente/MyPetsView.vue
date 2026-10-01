<template>
  <div class="mascotas-view">
    <header class="page-header">
      <div>
        <span class="page-header-eyebrow">
          <PawPrint :size="12" /> Mis Mascotas
        </span>
        <h1>Mis Mascotas</h1>
        <p class="page-header-sub">
          Gestiona y registra las mascotas asociadas a tu cuenta.
        </p>
      </div>
      <div class="page-header-actions">
        <AppButton variant="primary" @click="abrirCrear">
          <template #icon-left><Plus :size="16" /></template>
          Nueva Mascota
        </AppButton>
      </div>
    </header>

    <!-- Tabs -->
    <nav class="tabs" role="tablist">
      <button
        type="button"
        role="tab"
        class="tab"
        :class="{ active: tab === 'activas' }"
        :aria-selected="tab === 'activas'"
        @click="tab = 'activas'"
      >
        <PawPrint :size="15" /> Activas
        <span class="tab-count">{{ activas.length }}</span>
      </button>
      <button
        type="button"
        role="tab"
        class="tab"
        :class="{ active: tab === 'inactivas' }"
        :aria-selected="tab === 'inactivas'"
        @click="tab = 'inactivas'"
      >
        <Archive :size="15" /> Inactivas
        <span v-if="inactivas.length" class="tab-count">{{ inactivas.length }}</span>
      </button>
      <button
        type="button"
        role="tab"
        class="tab"
        :class="{ active: tab === 'fallecidas' }"
        :aria-selected="tab === 'fallecidas'"
        @click="tab = 'fallecidas'"
      >
        <HeartOff :size="15" /> Fallecidas
        <span v-if="fallecidas.length" class="tab-count">{{ fallecidas.length }}</span>
      </button>
    </nav>

    <!-- Loading -->
    <div v-if="cargando" class="card">
      <div class="card-body">
        <div class="loading-state">
          <span class="spinner spinner-lg" />
          <p>Cargando mascotas…</p>
        </div>
      </div>
    </div>

    <template v-else>
      <!-- ACTIVAS -->
      <div v-if="tab === 'activas'">
        <AppEmptyState
          v-if="!activas.length"
          :icon="PawPrint"
          title="No tienes mascotas activas"
          description="Registra tu primera mascota para comenzar."
        >
          <template #action>
            <AppButton variant="primary" @click="abrirCrear">
              <template #icon-left><Plus :size="16" /></template>
              Registrar Mascota
            </AppButton>
          </template>
        </AppEmptyState>

        <div v-else class="mascotas-grid">
          <div
            v-for="m in activas"
            :key="m.idMascota"
            class="mascota-card-wrapper"
          >
            <button
              type="button"
              class="mascota-card"
              @click="verHistorial(m.idMascota)"
            >
              <PetAvatar :nombre-especie="m.nombreEspecie || getEspecieLabel(m.idEspecie)" size="lg" />
              <div class="mascota-info">
                <h4 class="mascota-nombre">{{ m.nombre }}</h4>
                <div class="mascota-meta">
                  <span class="badge badge-brand">
                    {{ m.nombreEspecie || getEspecieLabel(m.idEspecie) }}
                  </span>
                  <span v-if="m.sexo" class="badge badge-info">
                    {{ m.sexo === 'M' ? 'Macho' : 'Hembra' }}
                  </span>
                </div>
                <div class="mascota-datos">
                  <p v-if="m.fechaNacimiento" class="mascota-detail">
                    <Calendar :size="14" /> {{ formatFecha(m.fechaNacimiento) }}
                  </p>
                  <p v-if="m.pesoActual" class="mascota-detail">
                    <Weight :size="14" /> {{ m.pesoActual }} kg
                  </p>
                  <p v-if="m.esterilizado" class="mascota-detail mascota-detail-success">
                    <CheckCircle2 :size="14" /> Esterilizado/a
                  </p>
                </div>
              </div>
              <span class="mascota-cta" aria-hidden="true">
                <FileText :size="16" />
              </span>
            </button>

            <div class="card-actions">
              <button
                class="card-action-btn"
                type="button"
                title="Editar"
                @click.stop="abrirEditar(m)"
              >
                <Pencil :size="14" />
              </button>
              <button
                class="card-action-btn card-action-btn--danger"
                type="button"
                title="Desactivar"
                @click.stop="abrirArchivar(m)"
              >
                <Archive :size="14" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- INACTIVAS -->
      <div v-else-if="tab === 'inactivas'">
        <AppEmptyState
          v-if="!inactivas.length"
          :icon="Archive"
          title="Sin mascotas inactivas"
          description="Cuando desactives una mascota aparecerá aquí. Podrás reactivarla cuando quieras."
        />

        <div v-else class="mascotas-grid">
          <div
            v-for="m in inactivas"
            :key="m.idMascota"
            class="mascota-card-wrapper is-archived"
          >
            <div class="mascota-card mascota-card--static">
              <PetAvatar
                :nombre-especie="m.nombreEspecie || getEspecieLabel(m.idEspecie)"
                size="lg"
                muted
              />
              <div class="mascota-info">
                <h4 class="mascota-nombre">{{ m.nombre }}</h4>
                <div class="mascota-meta">
                  <span class="badge badge-brand">
                    {{ m.nombreEspecie || getEspecieLabel(m.idEspecie) }}
                  </span>
                  <span class="badge badge-neutral">Inactiva</span>
                </div>
                <p class="mascota-detail">
                  <Calendar :size="14" /> {{ formatFecha(m.fechaNacimiento) }}
                </p>
              </div>
            </div>
            <div class="card-actions">
              <button
                class="card-action-btn card-action-btn--success"
                type="button"
                title="Reactivar"
                @click.stop="reactivar(m)"
              >
                <RotateCcw :size="14" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- FALLECIDAS -->
      <div v-else>
        <AppEmptyState
          v-if="!fallecidas.length"
          :icon="HeartOff"
          title="Sin mascotas fallecidas"
          description="Las mascotas que marques como fallecidas aparecerán aquí, como memoria de su historia."
        />

        <div v-else class="mascotas-grid">
          <div
            v-for="m in fallecidas"
            :key="m.idMascota"
            class="mascota-card-wrapper is-archived"
          >
            <div class="mascota-card mascota-card--static">
              <PetAvatar
                :nombre-especie="m.nombreEspecie || getEspecieLabel(m.idEspecie)"
                size="lg"
                muted
              />
              <div class="mascota-info">
                <h4 class="mascota-nombre">{{ m.nombre }}</h4>
                <div class="mascota-meta">
                  <span class="badge badge-brand">
                    {{ m.nombreEspecie || getEspecieLabel(m.idEspecie) }}
                  </span>
                  <span class="badge badge-danger">En memoria</span>
                </div>
                <p class="mascota-detail">
                  <Calendar :size="14" /> {{ formatFecha(m.fechaNacimiento) }}
                </p>
              </div>
            </div>
            <div class="card-actions">
              <button
                class="card-action-btn"
                type="button"
                title="Reactivar (por si fue un error)"
                @click.stop="reactivar(m)"
              >
                <RotateCcw :size="14" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <PetFormModal
      :visible="modalFormVisible"
      :mascota="mascotaEditando"
      :especies="especies"
      @close="cerrarForm"
      @saved="onGuardado"
    />

    <PetArchiveModal
      :visible="modalArchivarVisible"
      :mascota="mascotaAArchivar"
      @close="cerrarArchivar"
      @archived="onArchivado"
    />

    <ToastContainer />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Plus, PawPrint, Calendar, Weight, CheckCircle2, FileText,
  Pencil, Archive, HeartOff, RotateCcw,
} from 'lucide-vue-next'
import { getMisMascotas, getEspecies, cambiarEstadoMascota } from '@/api/mascotas.api.js'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'

import PetFormModal from '@/components/cliente/PetFormModal.vue'
import PetArchiveModal from '@/components/cliente/PetArchiveModal.vue'
import PetAvatar from '@/components/ui/PetAvatar.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppEmptyState from '@/components/ui/AppEmptyState.vue'
import ToastContainer from '@/components/ui/ToastContainer.vue'

const router = useRouter()
const { toastSuccess, toastError } = useToast()

const cargando = ref(false)
const mascotas = ref([])
const especies = ref([])
const tab = ref('activas')

const modalFormVisible = ref(false)
const mascotaEditando = ref(null)
const modalArchivarVisible = ref(false)
const mascotaAArchivar = ref(null)

const activas = computed(() => mascotas.value.filter((m) => m.activo && !m.fallecido))
const inactivas = computed(() => mascotas.value.filter((m) => !m.activo && !m.fallecido))
const fallecidas = computed(() => mascotas.value.filter((m) => m.fallecido))

function getEspecieLabel(id) {
  const e = especies.value.find((e) => e.id === id)
  return e ? e.nombre : 'Mascota'
}

function formatFecha(fecha) {
  if (!fecha) return '—'
  const d = new Date(fecha + 'T00:00:00')
  return d.toLocaleDateString('es-VE', { day: '2-digit', month: 'short', year: 'numeric' })
}

async function cargarTodo() {
  cargando.value = true
  try {
    const [{ data: mascotasData }, { data: especiesData }] = await Promise.all([
      getMisMascotas(true),
      getEspecies(),
    ])
    mascotas.value = mascotasData || []
    especies.value = especiesData || []
  } catch (err) {
    toastError(getApiErrorMessage(err) || 'Error al cargar las mascotas')
  } finally {
    cargando.value = false
  }
}

function abrirCrear() {
  mascotaEditando.value = null
  modalFormVisible.value = true
}
function abrirEditar(m) {
  mascotaEditando.value = m
  modalFormVisible.value = true
}
function cerrarForm() {
  modalFormVisible.value = false
  mascotaEditando.value = null
}
function abrirArchivar(m) {
  mascotaAArchivar.value = m
  modalArchivarVisible.value = true
}
function cerrarArchivar() {
  modalArchivarVisible.value = false
  mascotaAArchivar.value = null
}

async function onGuardado() {
  cerrarForm()
  await cargarTodo()
}
async function onArchivado() {
  cerrarArchivar()
  await cargarTodo()
}

async function reactivar(m) {
  try {
    await cambiarEstadoMascota(m.idMascota, 'Activa')
    toastSuccess(`"${m.nombre}" fue reactivada.`)
    await cargarTodo()
  } catch (err) {
    toastError(getApiErrorMessage(err) || 'No se pudo reactivar')
  }
}

function verHistorial(mascotaId) {
  if (!mascotaId) return
  router.push({ path: '/cliente/historial-clinico', query: { mascota: mascotaId } })
}

onMounted(cargarTodo)
</script>

<style scoped>
.mascotas-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.tabs { margin-bottom: var(--space-2); }

/* ═══ GRID ═══ */
.mascotas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-4);
}

.mascota-card-wrapper { position: relative; }
.mascota-card-wrapper.is-archived { opacity: 0.85; }

/* ═══ CARD ═══ */
.mascota-card {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  text-align: left;
  cursor: pointer;
  overflow: hidden;
  width: 100%;
  font-family: inherit;
  color: inherit;
  transition: border-color var(--duration-base) var(--ease-out),
              box-shadow var(--duration-base) var(--ease-out),
              transform var(--duration-base) var(--ease-out);
  box-shadow: var(--shadow-xs);
}
.mascota-card:hover {
  border-color: var(--brand-200);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}
.mascota-card--static { cursor: default; }
.mascota-card--static:hover { transform: none; }

.mascota-info { flex: 1; min-width: 0; }
.mascota-nombre {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2);
  letter-spacing: var(--tracking-tight);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding-right: 22px;
}
.mascota-meta {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
  flex-wrap: wrap;
}
.mascota-datos { display: flex; flex-direction: column; gap: var(--space-1); }
.mascota-detail {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-md);
  color: var(--text-secondary);
  margin: 0;
}
.mascota-detail svg { color: var(--text-tertiary); flex-shrink: 0; }
.mascota-detail-success { color: var(--success-600); font-weight: var(--font-semibold); }
.mascota-detail-success svg { color: var(--success-500); }

.mascota-cta {
  width: 30px;
  height: 30px;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-tertiary);
  background: var(--neutral-100);
  flex-shrink: 0;
  align-self: center;
  transition: all var(--duration-fast) var(--ease-out);
}
.mascota-card:hover .mascota-cta {
  background: var(--brand-700);
  color: var(--text-inverse);
}

/* ═══ BOTONES FLOTANTES ═══ */
.card-actions {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  display: flex;
  gap: var(--space-1);
  opacity: 0;
  transform: translateY(-4px);
  transition: all var(--duration-base) var(--ease-out);
  z-index: 2;
}
.mascota-card-wrapper:hover .card-actions,
.mascota-card-wrapper:focus-within .card-actions {
  opacity: 1;
  transform: translateY(0);
}
.card-action-btn {
  width: 30px;
  height: 30px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(4px);
  color: var(--text-secondary);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all var(--duration-fast) var(--ease-out);
}
.card-action-btn:hover {
  background: var(--brand-50);
  border-color: var(--brand-200);
  color: var(--brand-700);
}
.card-action-btn--danger:hover {
  background: var(--danger-50);
  border-color: var(--danger-200);
  color: var(--danger-600);
}
.card-action-btn--success:hover {
  background: var(--success-50);
  border-color: var(--success-200);
  color: var(--success-600);
}

@media (hover: none) {
  .card-actions { opacity: 1; transform: translateY(0); }
}

/* ═══ LOADING ═══ */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12) var(--space-6);
  color: var(--text-secondary);
  font-size: var(--text-base);
}

/* ═══ RESPONSIVE ═══ */
@media (max-width: 640px) {
  .mascotas-view { padding: var(--space-4); }
  .mascotas-grid { grid-template-columns: 1fr; }
}
</style>