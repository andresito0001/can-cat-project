import api from './axios.config'

// ═══════════════════════════════════════════════════════════════
// FLAG DE MOCKS
// Cambiar a `false` cuando el backend tenga los endpoints listos
// ═══════════════════════════════════════════════════════════════
const USE_MOCKS = false

// ─── Persistencia de mocks ───
const MOCK_PRODUCTOS_KEY = 'cancat-almacen-productos'
const MOCK_ENTRADAS_KEY = 'cancat-almacen-entradas'
const MOCK_MOVIMIENTOS_KEY = 'cancat-almacen-movimientos'

// ═══════════════════════════════════════════════════════════════
// NORMALIZADOR — tolera el DTO actual (mínimo) y el extendido
// ═══════════════════════════════════════════════════════════════
function normalizarProducto(raw) {
  if (!raw) return null
  return {
    id: raw.id ?? raw.idProducto ?? null,
    codigoSku: raw.codigoSku ?? '',
    nombre: raw.nombre ?? '',
    descripcion: raw.descripcion ?? '',
    categoria: raw.tipoCategoria ?? raw.categoria ?? raw.nombreCategoria ?? '—',
    presentacion: raw.presentacion ?? raw.unidadMedida ?? 'Unidad',
    idCategoria: raw.idCategoria ?? null,
    idProveedorPredeterminado: raw.idProveedorPredeterminado ?? null,
    nombreProveedor: raw.nombreProveedor ?? null,
    precioVenta: Number(raw.precioUsd ?? raw.precioVenta ?? 0),
    costoAdquisicion: Number(raw.costoAdquisicion ?? 0),
    stockActual: Number(raw.stockActual ?? 0),
    stockMinimo: Number(raw.stockMinimo ?? 5),
    stockMaximo: raw.stockMaximo ?? null,
    requiereReceta: Boolean(raw.requiereReceta),
    activo: raw.activo !== false,
    createdAt: raw.createdAt ?? null,
    updatedAt: raw.updatedAt ?? null,
  }
}

// ═══════════════════════════════════════════════════════════════
// SEED INICIAL
// ═══════════════════════════════════════════════════════════════
function seedProductos() {
  const now = new Date().toISOString()
  return [
    { id: 1, codigoSku: 'VAC-001', nombre: 'Vacuna Antirrábica', descripcion: 'Vacuna antirrábica canina/felina inactivada', categoria: 'Medicamento', presentacion: 'Unidad', idCategoria: 1, precioVenta: 12.00, costoAdquisicion: 6.50, stockActual: 25, stockMinimo: 5, stockMaximo: 50, requiereReceta: true, activo: true, createdAt: now, updatedAt: now },
    { id: 2, codigoSku: 'VAC-002', nombre: 'Vacuna Trivalente Felina', descripcion: 'Panleucopenia, rinotraqueítis y calicivirus', categoria: 'Medicamento', presentacion: 'Unidad', idCategoria: 1, precioVenta: 15.00, costoAdquisicion: 8.00, stockActual: 10, stockMinimo: 3, stockMaximo: 30, requiereReceta: true, activo: true, createdAt: now, updatedAt: now },
    { id: 3, codigoSku: 'MED-001', nombre: 'Amoxicilina 250mg', descripcion: 'Antibiótico de amplio espectro', categoria: 'Medicamento', presentacion: 'Unidad', idCategoria: 1, precioVenta: 0.80, costoAdquisicion: 0.35, stockActual: 100, stockMinimo: 10, stockMaximo: 200, requiereReceta: true, activo: true, createdAt: now, updatedAt: now },
    { id: 4, codigoSku: 'MED-002', nombre: 'Metronidazol 500mg', descripcion: 'Antimicrobiano/antiprotozoario', categoria: 'Medicamento', presentacion: 'Unidad', idCategoria: 1, precioVenta: 0.60, costoAdquisicion: 0.25, stockActual: 50, stockMinimo: 10, stockMaximo: 100, requiereReceta: true, activo: true, createdAt: now, updatedAt: now },
    { id: 5, codigoSku: 'MED-003', nombre: 'Meloxicam 1.5mg/ml', descripcion: 'Antiinflamatorio no esteroideo', categoria: 'Medicamento', presentacion: 'ml', idCategoria: 1, precioVenta: 2.50, costoAdquisicion: 1.10, stockActual: 30, stockMinimo: 5, stockMaximo: 60, requiereReceta: true, activo: true, createdAt: now, updatedAt: now },
    { id: 6, codigoSku: 'MED-004', nombre: 'Ivermectina 1%', descripcion: 'Antiparasitario', categoria: 'Medicamento', presentacion: 'ml', idCategoria: 1, precioVenta: 3.00, costoAdquisicion: 1.40, stockActual: 15, stockMinimo: 3, stockMaximo: 40, requiereReceta: true, activo: true, createdAt: now, updatedAt: now },
    { id: 7, codigoSku: 'MED-005', nombre: 'Suero Lactato Ringer 500ml', descripcion: 'Fluidoterapia IV', categoria: 'Medicamento', presentacion: 'Unidad', idCategoria: 1, precioVenta: 6.00, costoAdquisicion: 2.80, stockActual: 8, stockMinimo: 4, stockMaximo: 20, requiereReceta: false, activo: true, createdAt: now, updatedAt: now },
    { id: 8, codigoSku: 'INS-001', nombre: 'Gasas Estériles 10x10', descripcion: 'Gasas esterilizadas individuales', categoria: 'Accesorio', presentacion: 'Unidad', idCategoria: 3, precioVenta: 1.50, costoAdquisicion: 0.60, stockActual: 40, stockMinimo: 10, stockMaximo: 100, requiereReceta: false, activo: true, createdAt: now, updatedAt: now },
    { id: 9, codigoSku: 'INS-002', nombre: 'Jeringas 5ml', descripcion: 'Jeringas desechables con aguja', categoria: 'Accesorio', presentacion: 'Unidad', idCategoria: 3, precioVenta: 0.30, costoAdquisicion: 0.10, stockActual: 60, stockMinimo: 20, stockMaximo: 150, requiereReceta: false, activo: true, createdAt: now, updatedAt: now },
    { id: 10, codigoSku: 'INS-003', nombre: 'Guantes Nitrilo T/M', descripcion: 'Guantes de examen', categoria: 'Accesorio', presentacion: 'Unidad', idCategoria: 3, precioVenta: 0.50, costoAdquisicion: 0.20, stockActual: 0, stockMinimo: 20, stockMaximo: 100, requiereReceta: false, activo: true, createdAt: now, updatedAt: now },
    { id: 11, codigoSku: 'ALI-001', nombre: 'Royal Canin Adult 15kg', descripcion: 'Alimento balanceado para perros adultos', categoria: 'Alimento', presentacion: 'caja', idCategoria: 2, precioVenta: 45.00, costoAdquisicion: 28.00, stockActual: 12, stockMinimo: 5, stockMaximo: 25, requiereReceta: false, activo: true, createdAt: now, updatedAt: now },
    { id: 12, codigoSku: 'ALI-002', nombre: 'Whiskas Gatito 1kg', descripcion: 'Alimento para gatos jóvenes', categoria: 'Alimento', presentacion: 'Unidad', idCategoria: 2, precioVenta: 8.50, costoAdquisicion: 4.50, stockActual: 3, stockMinimo: 10, stockMaximo: 40, requiereReceta: false, activo: true, createdAt: now, updatedAt: now },
    { id: 13, codigoSku: 'MED-006', nombre: 'Desparasitante Oral Canino', descripcion: 'Antiparasitario de amplio espectro', categoria: 'Medicamento', presentacion: 'Unidad', idCategoria: 1, precioVenta: 5.50, costoAdquisicion: 2.20, stockActual: 22, stockMinimo: 8, stockMaximo: 50, requiereReceta: true, activo: true, createdAt: now, updatedAt: now },
    { id: 14, codigoSku: 'ACC-001', nombre: 'Collar Antipulgas', descripcion: 'Collar repelente 6 meses', categoria: 'Accesorio', presentacion: 'Unidad', idCategoria: 3, precioVenta: 4.00, costoAdquisicion: 1.80, stockActual: 2, stockMinimo: 5, stockMaximo: 20, requiereReceta: false, activo: true, createdAt: now, updatedAt: now },
    { id: 15, codigoSku: 'INS-004', nombre: 'Alcohol 70% 500ml', descripcion: 'Solución antiséptica', categoria: 'Accesorio', presentacion: 'ml', idCategoria: 3, precioVenta: 3.00, costoAdquisicion: 1.20, stockActual: 18, stockMinimo: 6, stockMaximo: 40, requiereReceta: false, activo: true, createdAt: now, updatedAt: now },
  ]
}

// ─── Acceso a mocks ───
function leerProductosMock() {
  const stored = localStorage.getItem(MOCK_PRODUCTOS_KEY)
  if (stored) {
    try { return JSON.parse(stored) } catch { /* reset */ }
  }
  const seeded = seedProductos()
  localStorage.setItem(MOCK_PRODUCTOS_KEY, JSON.stringify(seeded))
  return seeded
}

function guardarProductosMock(items) {
  localStorage.setItem(MOCK_PRODUCTOS_KEY, JSON.stringify(items))
}

function leerMovimientosMock() {
  const stored = localStorage.getItem(MOCK_MOVIMIENTOS_KEY)
  if (stored) {
    try { return JSON.parse(stored) } catch { /* reset */ }
  }
  return []
}

function guardarMovimientosMock(items) {
  localStorage.setItem(MOCK_MOVIMIENTOS_KEY, JSON.stringify(items))
}

function simularLatencia(ms = 300) {
  return new Promise((r) => setTimeout(r, ms))
}

// ═══════════════════════════════════════════════════════════════
// CATÁLOGOS AUXILIARES
// ═══════════════════════════════════════════════════════════════
export const CATEGORIAS_ALMACEN = [
  { id: 1, nombre: 'Medicamento', requierePrescripcion: true },
  { id: 2, nombre: 'Alimento', requierePrescripcion: false },
  { id: 3, nombre: 'Accesorio', requierePrescripcion: false },
  { id: 4, nombre: 'Servicio', requierePrescripcion: false },
]

export const UNIDADES_MEDIDA = ['Unidad', 'Kg', 'ml', 'caja', 'lt']

const PROVEEDORES_MOCK = [
  { id: 1, rif: 'J-12345678-9', nombreEmpresa: 'Distribuidora VetMed C.A.', nombreContacto: 'María López', telefono: '0212-5551234', tipoSuministro: 'Medicamentos', activo: true },
  { id: 2, rif: 'J-98765432-1', nombreEmpresa: 'Alimentos Premium Animal', nombreContacto: 'Pedro Ramírez', telefono: '0212-5559876', tipoSuministro: 'Alimentos', activo: true },
  { id: 3, rif: 'J-55555555-5', nombreEmpresa: 'Insumos Clínicos del Centro', nombreContacto: 'Ana Torres', telefono: '0241-8887766', tipoSuministro: 'Mixto', activo: true },
]

// ═══════════════════════════════════════════════════════════════
// ENDPOINTS
// ═══════════════════════════════════════════════════════════════

/**
 * GET /api/almacen/productos?filtro=&categoria=&soloAlertas=
 */
export async function getProductos({ filtro = '', categoria = '', soloAlertas = false } = {}) {
  if (USE_MOCKS) {
    await simularLatencia()
    let items = leerProductosMock()

    if (filtro.trim()) {
      const q = filtro.trim().toLowerCase()
      items = items.filter((p) =>
        p.nombre.toLowerCase().includes(q) ||
        p.codigoSku.toLowerCase().includes(q) ||
        (p.descripcion || '').toLowerCase().includes(q)
      )
    }

    if (categoria) {
      items = items.filter((p) => p.categoria === categoria)
    }

    if (soloAlertas) {
      items = items.filter((p) => p.stockActual <= p.stockMinimo)
    }

    return { data: items.map(normalizarProducto) }
  }

  // Real: el backend actual solo soporta ?filtro=; el resto se filtra acá
  const { data } = await api.get('/almacen/productos', {
    params: filtro ? { filtro } : {},
  })
  let items = data.map(normalizarProducto)
  if (categoria) items = items.filter((p) => p.categoria === categoria)
  if (soloAlertas) items = items.filter((p) => p.stockActual <= p.stockMinimo)
  return { data: items }
}

/**
 * GET /api/almacen/productos/{id}
 * ⚠️ Endpoint no existe aún en backend
 */
export async function getProducto(id) {
  if (USE_MOCKS) {
    await simularLatencia(150)
    const items = leerProductosMock()
    const found = items.find((p) => p.id === Number(id))
    if (!found) throw { response: { status: 404, data: { message: 'Producto no encontrado' } } }
    return { data: normalizarProducto(found) }
  }
  const { data } = await api.get(`/almacen/productos/${id}`)
  return { data: normalizarProducto(data) }
}

/**
 * POST /api/almacen/productos
 * ⚠️ Endpoint no existe aún en backend
 */
export async function crearProducto(payload) {
  if (USE_MOCKS) {
    await simularLatencia(400)
    const items = leerProductosMock()
    if (items.some((p) => p.codigoSku.toLowerCase() === payload.codigoSku.toLowerCase())) {
      throw { response: { status: 409, data: { message: `El SKU ${payload.codigoSku} ya existe.` } } }
    }
    const now = new Date().toISOString()
    const nuevo = {
      id: Math.max(0, ...items.map((p) => p.id)) + 1,
      codigoSku: payload.codigoSku,
      nombre: payload.nombre,
      descripcion: payload.descripcion || '',
      categoria: payload.categoria,
      presentacion: payload.presentacion || 'Unidad',
      idCategoria: payload.idCategoria,
      idProveedorPredeterminado: payload.idProveedorPredeterminado || null,
      nombreProveedor: payload.nombreProveedor || null,
      precioVenta: Number(payload.precioVenta || 0),
      costoAdquisicion: Number(payload.costoAdquisicion || 0),
      stockActual: Number(payload.stockActual || 0),
      stockMinimo: Number(payload.stockMinimo || 5),
      stockMaximo: payload.stockMaximo ? Number(payload.stockMaximo) : null,
      requiereReceta: Boolean(payload.requiereReceta),
      activo: true,
      createdAt: now,
      updatedAt: now,
    }
    items.push(nuevo)
    guardarProductosMock(items)
    return { data: normalizarProducto(nuevo) }
  }
  const { data } = await api.post('/almacen/productos', payload)
  return { data: normalizarProducto(data) }
}

/**
 * PUT /api/almacen/productos/{id}
 * ⚠️ Endpoint no existe aún en backend
 */
export async function actualizarProducto(id, payload) {
  if (USE_MOCKS) {
    await simularLatencia(400)
    const items = leerProductosMock()
    const idx = items.findIndex((p) => p.id === Number(id))
    if (idx === -1) throw { response: { status: 404, data: { message: 'Producto no encontrado' } } }
    items[idx] = { ...items[idx], ...payload, updatedAt: new Date().toISOString() }
    guardarProductosMock(items)
    return { data: normalizarProducto(items[idx]) }
  }
  const { data } = await api.put(`/almacen/productos/${id}`, payload)
  return { data: normalizarProducto(data) }
}

/**
 * GET /api/almacen/proveedores
 * ⚠️ Endpoint no existe aún en backend
 */
export async function getProveedores() {
  if (USE_MOCKS) {
    await simularLatencia(200)
    return { data: PROVEEDORES_MOCK }
  }
  const { data } = await api.get('/almacen/proveedores')
  return { data }
}

/**
 * POST /api/almacen/entradas
 * ⚠️ Endpoint no existe aún en backend
 */
export async function registrarEntrada(payload) {
  if (USE_MOCKS) {
    await simularLatencia(700)
    const items = leerProductosMock()

    // Validar productos y cantidades
    for (const linea of payload.lineas) {
      const prod = items.find((p) => p.id === Number(linea.idProducto))
      if (!prod) throw { response: { status: 400, data: { message: `Producto #${linea.idProducto} no existe.` } } }
      if (Number(linea.cantidadRecibida) <= 0) {
        throw { response: { status: 400, data: { message: `Cantidad inválida para ${prod.nombre}.` } } }
      }
    }

    // Sumar stock y crear movimientos
    const movimientos = leerMovimientosMock()
    let totalUnidades = 0
    let montoTotal = 0

    for (const linea of payload.lineas) {
      const idx = items.findIndex((p) => p.id === Number(linea.idProducto))
      items[idx].stockActual += Number(linea.cantidadRecibida)
      items[idx].updatedAt = new Date().toISOString()

      totalUnidades += Number(linea.cantidadRecibida)
      montoTotal += Number(linea.cantidadRecibida) * Number(linea.precioUnitario || 0)

      movimientos.push({
        id: movimientos.length + 1,
        idProducto: Number(linea.idProducto),
        nombreProducto: items[idx].nombre,
        tipoMovimiento: 'Entrada',
        motivo: 'Compra',
        cantidad: Number(linea.cantidadRecibida),
        fechaMovimiento: new Date().toISOString(),
        documentoReferencia: payload.numeroFactura,
        observaciones: payload.observaciones || '',
      })
    }

    guardarProductosMock(items)
    guardarMovimientosMock(movimientos)

    const numeroOrden = `ENT-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${String(movimientos.length).padStart(4, '0')}`

    return {
      data: {
        idCompra: movimientos.length,
        numeroOrden,
        totalProductos: payload.lineas.length,
        totalUnidades,
        montoTotal,
        mensaje: 'Inventario actualizado correctamente.',
      },
    }
  }
  const { data } = await api.post('/almacen/entradas', payload)
  return { data }
}

/**
 * GET /api/almacen/movimientos?tipo=&desde=&hasta=
 * ⚠️ Endpoint no existe aún en backend
 */
export async function getMovimientos({ tipo = '', desde = '', hasta = '' } = {}) {
  if (USE_MOCKS) {
    await simularLatencia(250)
    let items = leerMovimientosMock()
    if (tipo) items = items.filter((m) => m.tipoMovimiento === tipo)
    if (desde) items = items.filter((m) => m.fechaMovimiento >= desde)
    if (hasta) items = items.filter((m) => m.fechaMovimiento <= hasta)
    return { data: items.sort((a, b) => b.fechaMovimiento.localeCompare(a.fechaMovimiento)) }
  }
  const { data } = await api.get('/almacen/movimientos', { params: { tipo, desde, hasta } })
  return { data }
}

// ─── Utilidad de desarrollo: resetear mocks ───
export function resetMocks() {
  localStorage.removeItem(MOCK_PRODUCTOS_KEY)
  localStorage.removeItem(MOCK_MOVIMIENTOS_KEY)
  localStorage.removeItem(MOCK_ENTRADAS_KEY)
}