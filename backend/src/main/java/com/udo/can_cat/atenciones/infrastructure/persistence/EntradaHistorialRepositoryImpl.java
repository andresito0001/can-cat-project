package com.udo.can_cat.atenciones.infrastructure.persistence;

import com.udo.can_cat.atenciones.domain.entity.EntradaHistorial;
import com.udo.can_cat.atenciones.domain.repository.EntradaHistorialRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public class EntradaHistorialRepositoryImpl implements EntradaHistorialRepository {

    private final EntradaHistorialJpaRepository jpa;

    public EntradaHistorialRepositoryImpl(EntradaHistorialJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public EntradaHistorial guardar(EntradaHistorial entrada) {
        EntradaHistorialJpaEntity e = new EntradaHistorialJpaEntity();
        e.setId(entrada.getId() != null ? entrada.getId().value() : null);
        e.setIdMascota(entrada.getIdMascota());
        e.setIdAtencion(entrada.getIdAtencion());
        e.setTipoRegistro(entrada.getTipoRegistro());
        e.setFechaRegistro(entrada.getFechaRegistro() != null ? entrada.getFechaRegistro() : LocalDateTime.now());
        e.setResumenEjecutivo(entrada.getResumenEjecutivo());
        EntradaHistorialJpaEntity guardado = jpa.saveAndFlush(e);
        EntradaHistorial dominio = new EntradaHistorial();
        dominio.setId(guardado.getId() != null ? new EntradaHistorial.EntradaHistorialId(guardado.getId()) : null);
        dominio.setIdMascota(guardado.getIdMascota());
        dominio.setIdAtencion(guardado.getIdAtencion());
        dominio.setTipoRegistro(guardado.getTipoRegistro());
        dominio.setFechaRegistro(guardado.getFechaRegistro());
        dominio.setResumenEjecutivo(guardado.getResumenEjecutivo());
        return dominio;
    }
}