<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { PawPrint, Search, AlertTriangle, CheckCircle2, Loader2, CalendarPlus, ArrowLeft } from 'lucide-vue-next'
import * as clientesApi from '@/api/clientes.api'
import { registrarMascota, getEspecies, getRazasPorEspecie } from '@/api/mascotas.api'

const router = useRouter()
const route = useRoute()

// Cliente: viene pre-cargado por query (desde Registrar Cliente o Gestionar Cita) o se busca
const clienteDocumento = ref(route.query.clienteDocumento || '')
const clienteNombre = ref(route.query.clienteNombre || '')
const clienteFiltro = ref('')
const resultados = ref([])
const buscando = ref(false)
let debounce = null

watch(clienteFiltro, (val) => {
  if (clienteDocumento.value) return
  clearTimeout(debounce)
  if (!val || val.trim().length < 2) { resultados.value = []; return }
  debounce = setTimeout(async () => {
    buscando.value = true
    try {
      const { data } = await clientesApi.getAll(val.trim())
      resultados.value = data
    } catch { resultados.value = [] }
    finally { buscando.value = false }
  }, 350)
})

function seleccionarCliente(c) {
  clienteDocumento.value = c.documentoIdentidad
  clienteNombre.value = c.nombreCompleto
  clienteFiltro.value = ''
  resultados.value = []
}

function cambiarCliente() {
  clienteDocumento.value = ''
  clienteNombre.value = ''
}

// Formulario de mascota (misma estructura del modal de MyPetsView)
const especies = ref([])
const razas = ref([])
const cargandoRazas = ref(false)
const guardando = ref(false)
const error = ref('')
const exito = ref(null) // { nombreMascota, ... }

const form = ref({ nombre: '', idEspecie: '', idRaza: '', sexo: '', fechaNacimiento: '', color: '', pesoActual: '', esterilizado: false })
const hoy = new Date().toISOString().split('T')[0]

watch(() => form.value.idEspecie, async (id) => {
  form.value.idRaza = ''
  razas.value = []
  if (!id) return
  cargandoRazas.value = true
  try { const { data } = await getRazasPorEspecie(id); razas.value = data }
  catch { /* vacío */ }
  finally { cargandoRazas.value = false }
})

async function guardar() {
  error.value = ''
  if (!clienteDocumento.value) { error.value = 'Debe seleccionar el cliente dueño de la mascota.'; return }
  if (!form.value.nombre.trim() || !form.value.idEspecie || !form.value.sexo) {
    error.value = 'Debe completar todos los campos obligatorios.'; return
  }
  guardando.value = true
  try {
    const { data } = await registrarMascota({
      documentoIdentidadCliente: clienteDocumento.value,
      idEspecie: parseInt(form.value.idEspecie),
      idRaza: form.value.idRaza ? parseInt(form.value.idRaza) : null,
      nombre: form.value.nombre.trim(),
      fechaNacimiento: form.value.fechaNacimiento || null,
      sexo: form.value.sexo,
      color: form.value.color.trim() || null,
      pesoActual: form.value.pesoActual ? parseFloat(form.value.pesoActual) : null,
      esterilizado: form.value.esterilizado
    })
    exito.value = data
  } catch (err) {
    error.value = err.response?.data?.message || 'Error al registrar la mascota.'
  } finally { guardando.value = false }
}

onMounted(async () => {
  try { const { data } = await getEspecies(); especies.value = data } catch { /* vacío */ }
})
</script>

<template>
  <div class="page">
    <button class="back-btn" @click="router.push('/recepcion/dashboard')"><ArrowLeft :size="16" /> Volver</button>

    <div class="card">
      <div class="card-header"><h3><PawPrint :size="18" /> Registrar Mascota</h3></div>
      <div class="card-body">
        <!-- Cliente dueño -->
        <div v-if="!exito" class="form-group">
          <label>Cliente dueño <span class="req">*</span></label>
          <div v-if="clienteDocumento" class="chip">
            <div><strong>{{ clienteNombre }}</strong><span>{{ clienteDocumento }}</span></div>
            <button type="button" class="chip-btn" @click="cambiarCliente">Cambiar</button>
          </div>
          <template v-else>
            <div class="search-box">
              <Search :size="16" />
              <input v-model="clienteFiltro" type="text" placeholder="Buscar cliente por nombre o documento..." />
              <Loader2 v-if="buscando" :size="16" class="spin" />
            </div>
            <div v-if="clienteFiltro.length >= 2 && !buscando && resultados.length === 0" class="warn-box">
              No se encontraron resultados. Verifique los datos o
              <router-link to="/recepcion/clientes/nuevo">registre previamente al cliente</router-link>.
            </div>
            <div v-if="resultados.length > 0" class="results">
              <button v-for="c in resultados" :key="c.id" type="button" class="result-row" @click="seleccionarCliente(c)">
                <strong>{{ c.nombreCompleto }}</strong><span>{{ c.documentoIdentidad }}</span>
              </button>
            </div>
          </template>
        </div>

        <div v-if="error" class="alert"><AlertTriangle :size="16" /> {{ error }}</div>

        <form v-if="!exito" @submit.prevent="guardar">
          <div class="grid2">
            <div class="form-group">
              <label>Nombre <span class="req">*</span></label>
              <input v-model="form.nombre" type="text" maxlength="50" placeholder="Ej: Rocky" />
            </div>
            <div class="form-group">
              <label>Sexo <span class="req">*</span></label>
              <select v-model="form.sexo">
                <option value="" disabled>Seleccione...</option>
                <option value="M">Macho</option><option value="H">Hembra</option>
              </select>
            </div>
            <div class="form-group">
              <label>Especie <span class="req">*</span></label>
              <select v-model="form.idEspecie">
                <option value="" disabled>Seleccione...</option>
                <option v-for="e in especies" :key="e.id" :value="e.id">{{ e.nombre }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>Raza <span class="opt">(opcional)</span></label>
              <select v-model="form.idRaza" :disabled="!form.idEspecie || cargandoRazas">
                <option value="">Seleccione...</option>
                <option v-for="r in razas" :key="r.id" :value="r.id">{{ r.nombre }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>Fecha de nacimiento <span class="opt">(opcional)</span></label>
              <input v-model="form.fechaNacimiento" type="date" :max="hoy" />
            </div>
            <div class="form-group">
              <label>Color <span class="opt">(opcional)</span></label>
              <input v-model="form.color" type="text" maxlength="30" placeholder="Ej: Marrón" />
            </div>
            <div class="form-group">
              <label>Peso actual kg <span class="opt">(opcional)</span></label>
              <input v-model="form.pesoActual" type="number" step="0.01" min="0.01" max="999.99" />
            </div>
            <div class="form-group">
              <label>¿Esterilizado/a?</label>
              <input v-model="form.esterilizado" type="checkbox" class="big-check" />
            </div>
          </div>
          <div class="actions">
            <button type="button" class="btn-secondary" :disabled="guardando" @click="router.push('/recepcion/dashboard')">Cancelar</button>
            <button type="submit" class="btn-primary" :disabled="guardando">
              <Loader2 v-if="guardando" :size="16" class="spin" /> Guardar Mascota
            </button>
          </div>
        </form>

        <!-- Éxito -->
        <div v-if="exito" class="exito">
          <CheckCircle2 :size="44" class="ok-icon" />
          <h3>Mascota registrada satisfactoriamente</h3>
          <p><strong>{{ exito.nombre }}</strong> ({{ exito.nombreEspecie }}) ahora está asociada a {{ exito.nombreCliente }}.</p>
          <div class="actions">
            <button class="btn-primary" @click="router.push({ path: '/recepcion/citas/nueva', query: { clienteDocumento: exito.documentoIdentidadCliente || clienteDocumento, clienteNombre: exito.nombreCliente } })">
              <CalendarPlus :size="16" /> Agendar cita ahora
            </button>
            <button class="btn-secondary" @click="router.push('/recepcion/dashboard')">Volver</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { max-width: 680px; display: flex; flex-direction: column; gap: 16px; font-family: 'Inter','Segoe UI',Roboto,sans-serif; }
.back-btn { display: inline-flex; align-items: center; gap: 6px; background: none; border: none; color: #64748B; font-size: 13px; font-weight: 600; cursor: pointer; width: fit-content; font-family: inherit; }
.card { background: #fff; border-radius: 16px; box-shadow: 0 4px 6px -1px rgba(0,0,0,.03), 0 20px 40px -4px rgba(15,118,110,.08); border-top: 4px solid #0F766E; }
.card-header { padding: 18px 24px; border-bottom: 1px solid #E2E8F0; }
.card-header h3 { font-size: 16px; font-weight: 700; color: #1E293B; display: flex; gap: 8px; align-items: center; margin: 0; }
.card-body { padding: 24px; }
.form-group { display: flex; flex-direction: column; gap: 6px; margin-bottom: 14px; }
.form-group label { font-size: 13px; font-weight: 600; color: #475569; }
.req { color: #EF4444; } .opt { color: #94A3B8; font-weight: 400; font-size: 11px; }
input, select { padding: 10px 14px; border: 1.5px solid #E2E8F0; border-radius: 10px; font-size: 14px; color: #1E293B; background: #F8FAFC; font-family: inherit; outline: none; }
input:focus, select:focus { border-color: #0F766E; background: #fff; box-shadow: 0 0 0 3px rgba(15,118,110,.1); }
.big-check { width: 18px; height: 18px; accent-color: #0F766E; }
.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.search-box { position: relative; display: flex; align-items: center; }
.search-box svg:first-child { position: absolute; left: 12px; color: #94A3B8; }
.search-box input { width: 100%; padding-left: 36px; padding-right: 36px; box-sizing: border-box; }
.search-box .spin { position: absolute; right: 12px; color: #0F766E; }
.results { border: 1px solid #E2E8F0; border-radius: 10px; max-height: 200px; overflow-y: auto; }
.result-row { display: flex; flex-direction: column; width: 100%; text-align: left; padding: 10px 14px; background: #fff; border: none; border-bottom: 1px solid #F1F5F9; cursor: pointer; font-family: inherit; }
.result-row:hover { background: #F0FDFA; }
.result-row strong { font-size: 14px; color: #1E293B; } .result-row span { font-size: 12px; color: #64748B; }
.chip { display: flex; justify-content: space-between; align-items: center; background: #F0FDFA; border: 1px solid #99F6E4; border-radius: 10px; padding: 10px 14px; }
.chip strong { display: block; color: #115E59; } .chip span { font-size: 12px; color: #0F766E; }
.chip-btn { background: #fff; border: 1px solid #99F6E4; color: #0F766E; border-radius: 8px; padding: 5px 10px; font-size: 12px; font-weight: 600; cursor: pointer; font-family: inherit; }
.warn-box { font-size: 13px; color: #B45309; background: #FFFBEB; border: 1px solid #FDE68A; border-radius: 8px; padding: 10px 14px; }
.warn-box a { color: #0F766E; font-weight: 600; }
.alert { display: flex; align-items: center; gap: 8px; padding: 12px 16px; border-radius: 10px; font-size: 13px; font-weight: 500; color: #DC2626; background: #FEF2F2; border: 1px solid #FECACA; margin-bottom: 16px; }
.actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 12px; }
.btn-primary, .btn-secondary { display: inline-flex; align-items: center; gap: 8px; padding: 12px 22px; border-radius: 10px; font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit; }
.btn-primary { background: #0F766E; color: #fff; border: none; }
.btn-primary:hover:not(:disabled) { background: #115E59; }
.btn-secondary { background: #fff; color: #475569; border: 1.5px solid #E2E8F0; }
.exito { text-align: center; padding: 16px 0; }
.ok-icon { color: #10B981; }
.exito h3 { font-size: 18px; color: #1E293B; margin: 12px 0 6px; }
.exito p { color: #64748B; font-size: 14px; margin: 0 0 16px; }
.exito .actions { justify-content: center; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
@media (max-width: 640px) { .grid2 { grid-template-columns: 1fr; } }
</style>