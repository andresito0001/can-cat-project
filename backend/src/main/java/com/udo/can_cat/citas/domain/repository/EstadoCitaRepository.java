package com.udo.can_cat.citas.domain.repository;

import com.udo.can_cat.citas.domain.entity.EstadoCita;
import java.util.List;
import java.util.Optional;

public interface EstadoCitaRepository {

    Optional<EstadoCita> buscarPorNombre(String nombre);

    List<EstadoCita> buscarTodos();

    /**
     * Retorna los IDs de los estados que NO son finales.
     * Es decir: Pendiente_Pago, Pagada, Confirmada, En_Atencion.
     * Estos son los estados que "ocupan" un espacio en la agenda.
     */
    List<Integer> buscarIdsEstadosActivos();

    /**
     * Retorna los IDs de los estados que SÍ son finales.
     * Es decir: Completada, Cancelada.
     */
    List<Integer> buscarIdsEstadosFinales();
}