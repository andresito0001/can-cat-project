<template>
  <div class="mascotas-view">
    <header class="page-header">
      <div>
        <h2>Mis Mascotas</h2>
        <p class="subtitle">Gestiona y registra las mascotas asociadas a tu cuenta</p>
      </div>
      <button class="btn-primary" @click="abrirCrear">
        <Plus :size="18" />
        Nueva Mascota
      </button>
    </header>

    <!-- Tabs -->
    <nav class="tabs" role="tablist">
      <button
        type="button"
        role="tab"
        class="tab"
        :class="{ active: tab === 'activas' }"
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
        @click="tab = 'fallecidas'"
      >
        <HeartOff :size="15" /> Fallecidas
        <span v-if="fallecidas.length" class="tab-count">{{ fallecidas.length }}</span>
      </button>
    </nav>

    <div v-if="cargando" class="loading-state">
      <Loader2 :size="32" class="spin" />
      <p>Cargando mascotas...</p>
    </div>

    <!-- Contenido por tab -->
    <template v-else>
      <!-- ACTIVAS -->
      <div v-if="tab === 'activas'">
        <div v-if="activas.length === 0" class="empty-state">
          <PawPrint :size="48" />
          <h3>No tienes mascotas activas</h3>
          <p>Registra tu primera mascota para comenzar.</p>
          <button class="btn-primary" @click="abrirCrear">
            <Plus :size="18" /> Registrar Mascota
          </button>
        </div>

        <div v-else class="mascotas-grid">
          <div
            v-for="m in activas"
            :key="m.idMascota"
            class="mascota-card-wrapper"
          >
            <button type="button" class="mascota-card" @click="verHistorial(m.idMascota)">
              <PetAvatar :nombre-especie="m.nombreEspecie || getEspecieLabel(m.idEspecie)" size="lg" />
              <div class="mascota-info">
                <h4 class="mascota-nombre">{{ m.nombre }}</h4>
                <p class="mascota-meta">
                  <span class="badge-especie">{{ m.nombreEspecie || getEspecieLabel(m.idEspecie) }}</span>
                  <span v-if="m.sexo" class="badge-sexo">{{ m.sexo === 'M' ? 'Macho' : 'Hembra' }}</span>
                </p>
                <div class="mascota-datos">
                  <p v-if="m.fechaNacimiento" class="mascota-detail">
                    <Calendar :size="14" /> {{ formatFecha(m.fechaNacimiento) }}
                  </p>
                  <p v-if="m.pesoActual" class="mascota-detail">
                    <Weight :size="14" /> {{ m.pesoActual }} kg
                  </p>
                  <p v-if="m.esterilizado" class="mascota-detail esterilizado">
                    <CheckCircle2 :size="14" /> Esterilizado/a
                  </p>
                </div>
              </div>
              <span class="mascota-cta" aria-hidden="true"><FileText :size="16" /></span>
            </button>

            <div class="card-actions">
              <button
                class="card-action-btn"
                title="Editar"
                @click.stop="abrirEditar(m)"
              >
                <Pencil :size="14" />
              </button>
              <button
                class="card-action-btn card-action-btn--danger"
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
        <div v-if="inactivas.length === 0" class="empty-state">
          <Archive :size="48" />
          <h3>Sin mascotas inactivas</h3>
          <p>Cuando desactives una mascota aparecerá aquí. Podrás reactivarla cuando quieras.</p>
        </div>

        <div v-else class="mascotas-grid">
          <div
            v-for="m in inactivas"
            :key="m.idMascota"
            class="mascota-card-wrapper is-archived"
          >
            <div class="mascota-card mascota-card--static">
              <PetAvatar :nombre-especie="m.nombreEspecie || getEspecieLabel(m.idEspecie)" size="lg" muted />
              <div class="mascota-info">
                <h4 class="mascota-nombre">{{ m.nombre }}</h4>
                <p class="mascota-meta">
                  <span class="badge-especie">{{ m.nombreEspecie || getEspecieLabel(m.idEspecie) }}</span>
                  <span class="badge-archivada">Inactiva</span>
                </p>
                <p class="mascota-detail"><Calendar :size="14" /> {{ formatFecha(m.fechaNacimiento) }}</p>
              </div>
            </div>
            <div class="card-actions">
              <button
                class="card-action-btn card-action-btn--success"
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
        <div v-if="fallecidas.length === 0" class="empty-state">
          <HeartOff :size="48" />
          <h3>Sin mascotas fallecidas</h3>
          <p>Las mascotas que marques como fallecidas aparecerán aquí, como memoria de su historia.</p>
        </div>

        <div v-else class="mascotas-grid">
          <div
            v-for="m in fallecidas"
            :key="m.idMascota"
            class="mascota-card-wrapper is-archived"
          >
            <div class="mascota-card mascota-card--static">
              <PetAvatar :nombre-especie="m.nombreEspecie || getEspecieLabel(m.idEspecie)" size="lg" muted />
              <div class="mascota-info">
                <h4 class="mascota-nombre">{{ m.nombre }}</h4>
                <p class="mascota-meta">
                  <span class="badge-especie">{{ m.nombreEspecie || getEspecieLabel(m.idEspecie) }}</span>
                  <span class="badge-fallecida">En memoria</span>
                </p>
                <p class="mascota-detail"><Calendar :size="14" /> {{ formatFecha(m.fechaNacimiento) }}</p>
              </div>
            </div>
            <div class="card-actions">
              <button
                class="card-action-btn"
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

    <!-- Modales -->
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Plus, PawPrint, Calendar, Weight, CheckCircle2, FileText,
  Loader2, Pencil, Archive, HeartOff, RotateCcw,
} from 'lucide-vue-next'
import { getMisMascotas, getEspecies, cambiarEstadoMascota } from '@/api/mascotas.api.js'
import { getApiErrorMessage } from '@/utils/apiError'
import { useToast } from '@/composables/useToast'
import PetFormModal from '@/components/cliente/PetFormModal.vue'
import PetArchiveModal from '@/components/cliente/PetArchiveModal.vue'
import PetAvatar from '@/components/ui/PetAvatar.vue'

const router = useRouter()
const { toastSuccess, toastError } = useToast()

const cargando = ref(false)
const mascotas = ref([])
const especies = ref([])
const tab = ref('activas')

// Modales
const modalFormVisible = ref(false)
const mascotaEditando = ref(null)
const modalArchivarVisible = ref(false)
const mascotaAArchivar = ref(null)

// Computeds por tab
const activas = computed(() => mascotas.value.filter(m => m.activo && !m.fallecido))
const inactivas = computed(() => mascotas.value.filter(m => !m.activo && !m.fallecido))
const fallecidas = computed(() => mascotas.value.filter(m => m.fallecido))

// ─── Helpers ───
function getEspecieLabel(id) {
  const e = especies.value.find(e => e.id === id)
  return e ? e.nombre : 'Mascota'
}

function formatFecha(fecha) {
  if (!fecha) return '—'
  const d = new Date(fecha + 'T00:00:00')
  return d.toLocaleDateString('es-VE', { day: '2-digit', month: 'short', year: 'numeric' })
}

// ─── Carga ───
async function cargarTodo() {
  cargando.value = true
  try {
    const [{ data: mascotasData }, { data: especiesData }] = await Promise.all([
      getMisMascotas(true),  // incluir archivadas
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

// ─── Modales ───
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

// ─── Reactivar ───
async function reactivar(m) {
  try {
    await cambiarEstadoMascota(m.idMascota, 'Activa')
    toastSuccess(`"${m.nombre}" fue reactivada.`)
    await cargarTodo()
  } catch (err) {
    toastError(getApiErrorMessage(err) || 'No se pudo reactivar')
  }
}

// ─── Navegación ───
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
  font-family: 'Inter', 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  padding: 24px;
}
button { font-family: inherit; }

.page-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px; gap: 16px;
}
.page-header h2 { font-size: 24px; font-weight: 700; color: #1E293B; margin: 0; }
.subtitle { font-size: 14px; color: #64748B; margin: 4px 0 0 0; }

/* Tabs */
.tabs {
  display: flex; gap: 6px; margin-bottom: 20px;
  border-bottom: 1px solid #E2E8F0;
}
.tab {
  display: inline-flex; align-items: center; gap: 7px;
  padding: 10px 16px; border: none; background: transparent;
  color: #64748B; font-size: 13.5px; font-weight: 600;
  cursor: pointer; transition: all .2s;
  border-bottom: 2px solid transparent; margin-bottom: -1px;
}
.tab:hover { color: #0F766E; }
.tab.active { color: #0F766E; border-bottom-color: #0F766E; }
.tab-count {
  display: inline-flex; align-items: center; justify-content: center;
  min-width: 20px; height: 18px; padding: 0 6px;
  border-radius: 10px; background: #F1F5F9; color: #64748B;
  font-size: 10.5px; font-weight: 700;
}
.tab.active .tab-count { background: #F0FDFA; color: #0F766E; }

/* Botones */
.btn-primary {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 10px 20px; background: #0F766E; color: white;
  border: none; border-radius: 10px; font-size: 14px; font-weight: 600;
  cursor: pointer; transition: all 0.2s; white-space: nowrap;
}
.btn-primary:hover { background: #115E59; transform: translateY(-1px); }

/* Estados */
.loading-state, .empty-state {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  padding: 60px 24px; color: #94A3B8; gap: 16px;
  background: #fff; border-radius: 16px; border: 1px solid #E2E8F0;
}
.empty-state h3 { font-size: 18px; font-weight: 600; color: #1E293B; margin: 0; }
.empty-state p { font-size: 14px; color: #64748B; margin: 0 0 8px 0; text-align: center; max-width: 400px; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Grid */
.mascotas-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.mascota-card-wrapper { position: relative; }
.mascota-card-wrapper.is-archived { opacity: .85; }

.mascota-card {
  position: relative; display: flex; align-items: flex-start; gap: 16px;
  padding: 18px; background: #fff; border: 1px solid #E2E8F0;
  border-radius: 12px; text-align: left; cursor: pointer; overflow: hidden;
  font-family: inherit; color: inherit; appearance: none; width: 100%;
  transition: border-color .2s, box-shadow .2s, transform .2s;
}
.mascota-card::before {
  content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 3px;
  background: var(--pet-color, #0F766E);
  transform: scaleY(.35); opacity: 0;
  transition: opacity .25s, transform .25s;
}
.mascota-card:hover {
  border-color: rgba(15, 118, 110, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 10px 24px -8px rgba(15, 118, 110, 0.18);
}
.mascota-card:hover::before { opacity: 1; transform: scaleY(1); }
.mascota-card--static { cursor: default; }
.mascota-card--static:hover { transform: none; }

.mascota-info { flex: 1; min-width: 0; }
.mascota-nombre {
  font-size: 16px; font-weight: 700; color: #1E293B; margin: 0 0 6px 0;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
  padding-right: 22px;
}
.mascota-meta { display: flex; gap: 6px; margin: 0 0 10px 0; flex-wrap: wrap; }
.badge-especie {
  background: #ECFDF5; color: #059669; padding: 2px 10px; border-radius: 20px;
  font-size: 11px; font-weight: 600; border: 1px solid #A7F3D0;
}
.badge-sexo {
  background: #EFF6FF; color: #3B82F6; padding: 2px 10px; border-radius: 20px;
  font-size: 11px; font-weight: 600; border: 1px solid #BFDBFE;
}
.badge-archivada {
  background: #F1F5F9; color: #64748B; padding: 2px 10px; border-radius: 20px;
  font-size: 11px; font-weight: 600; border: 1px solid #E2E8F0;
}
.badge-fallecida {
  background: #FEF2F2; color: #B91C1C; padding: 2px 10px; border-radius: 20px;
  font-size: 11px; font-weight: 600; border: 1px solid #FECACA;
}
.mascota-datos { display: flex; flex-direction: column; gap: 4px; }
.mascota-detail {
  display: flex; align-items: center; gap: 6px; font-size: 13px;
  color: #64748B; margin: 0;
}
.mascota-detail.esterilizado { color: #059669; font-weight: 600; }

.mascota-cta {
  width: 30px; height: 30px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #94A3B8; background: #F1F5F9; flex-shrink: 0; align-self: center;
  transition: all .2s;
}
.mascota-card:hover .mascota-cta { background: #0F766E; color: #fff; }

/* Botones flotantes del card */
.card-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 6px;
  opacity: 0;
  transform: translateY(-4px);
  transition: all 0.18s ease;
  z-index: 2;
}
.mascota-card-wrapper:hover .card-actions {
  opacity: 1;
  transform: translateY(0);
}
.card-action-btn {
  width: 30px; height: 30px; border-radius: 8px;
  border: 1px solid #E2E8F0; background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(4px);
  color: #64748B; cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center;
  transition: all .15s;
}
.card-action-btn:hover {
  background: #F0FDFA;
  border-color: #99F6E4;
  color: #0F766E;
}
.card-action-btn--danger:hover {
  background: #FEF2F2;
  border-color: #FECACA;
  color: #DC2626;
}
.card-action-btn--success:hover {
  background: #ECFDF5;
  border-color: #A7F3D0;
  color: #059669;
}
@media (hover: none) {
  .card-actions { opacity: 1; transform: translateY(0); }
}

@media (max-width: 640px) {
  .page-header { flex-direction: column; align-items: flex-start; gap: 16px; }
  .mascotas-grid { grid-template-columns: 1fr; }
  .tabs { overflow-x: auto; }
}
</style>