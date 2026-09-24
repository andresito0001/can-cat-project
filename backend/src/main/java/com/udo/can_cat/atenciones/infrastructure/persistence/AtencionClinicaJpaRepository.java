package com.udo.can_cat.atenciones.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AtencionClinicaJpaRepository extends JpaRepository<AtencionClinicaJpaEntity, Integer> {

    Optional<AtencionClinicaJpaEntity> findByIdCita(Integer idCita);
    boolean existsByIdCita(Integer idCita);
    List<AtencionClinicaJpaEntity> findByIdCitaIn(List<Integer> idsCita);
    List<AtencionClinicaJpaEntity> findByIdMascotaOrderByFechaHoraInicioDesc(Integer idMascota);
}