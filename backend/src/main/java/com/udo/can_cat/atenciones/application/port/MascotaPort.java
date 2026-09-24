package com.udo.can_cat.atenciones.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Puerto sobre mascotas/clientes: lectura enriquecida + actualización de peso (D10). */
public interface MascotaPort {

    Optional<MascotaInfo> buscarPorId(Integer idMascota);

    /** Búsqueda por nombre de mascota, nombre o documento del dueño (6.2). */
    List<MascotaInfo> buscarPorFiltro(String filtro, int limite);

    void actualizarPeso(Integer idMascota, BigDecimal pesoKg);

    record MascotaInfo(Integer idMascota, String nombre, String especie, String raza, String sexo,
                       LocalDate fechaNacimiento, BigDecimal pesoActual, Boolean esterilizado,
                       Boolean activo, Boolean fallecido,
                       Integer idCliente, String clienteNombre, String clienteDocumento,
                       String clienteTelefono) {}
}