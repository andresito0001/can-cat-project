package com.udo.can_cat.atenciones.infrastructure.persistence;

import com.udo.can_cat.atenciones.domain.entity.AtencionClinica;
import com.udo.can_cat.atenciones.domain.repository.AtencionClinicaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class AtencionClinicaRepositoryImpl implements AtencionClinicaRepository {

    private final AtencionClinicaJpaRepository jpa;

    public AtencionClinicaRepositoryImpl(AtencionClinicaJpaRepository jpa) {
        this.jpa = jpa;
    }

    /** saveAndFlush: garantiza que atencion_insumo/receta (FK id_atencion)
     *  se inserten después de la atención dentro de la transacción de guardado (F7). */
    @Override
    public AtencionClinica guardar(AtencionClinica atencion) {
        AtencionClinicaJpaEntity guardada = jpa.saveAndFlush(aJpa(atencion));
        return aDominio(guardada);
    }

    @Override
    public Optional<AtencionClinica> buscarPorId(AtencionClinica.AtencionId id) {
        return jpa.findById(id.value()).map(AtencionClinicaRepositoryImpl::aDominio);
    }

    @Override
    public Optional<AtencionClinica> buscarPorCitaId(Integer idCita) {
        return jpa.findByIdCita(idCita).map(AtencionClinicaRepositoryImpl::aDominio);
    }

    @Override
    public boolean existePorCitaId(Integer idCita) {
        return jpa.existsByIdCita(idCita);
    }

    @Override
    public List<AtencionClinica> buscarPorCitaIds(List<Integer> idsCita) {
        if (idsCita == null || idsCita.isEmpty()) return List.of();
        return jpa.findByIdCitaIn(idsCita).stream().map(AtencionClinicaRepositoryImpl::aDominio).toList();
    }

    @Override
    public List<AtencionClinica> buscarPorMascotaId(Integer idMascota) {
        return jpa.findByIdMascotaOrderByFechaHoraInicioDesc(idMascota)
                .stream().map(AtencionClinicaRepositoryImpl::aDominio).toList();
    }

    static AtencionClinicaJpaEntity aJpa(AtencionClinica a) {
        AtencionClinicaJpaEntity e = new AtencionClinicaJpaEntity();
        e.setId(a.getId() != null ? a.getId().value() : null);
        e.setIdCita(a.getIdCita());
        e.setIdVeterinario(a.getIdVeterinario());
        e.setIdMascota(a.getIdMascota());
        e.setFechaHoraInicio(a.getFechaHoraInicio());
        e.setFechaHoraFin(a.getFechaHoraFin());
        e.setMotivoDetallado(a.getMotivoDetallado());
        e.setAnamnesis(a.getAnamnesis());
        e.setSintomasObservados(a.getSintomasObservados());
        e.setPesoKg(a.getPesoKg());
        e.setTemperaturaC(a.getTemperaturaC());
        e.setFrecCardiaca(a.getFrecCardiaca());
        e.setFrecRespiratoria(a.getFrecRespiratoria());
        e.setDiagnosticoPrincipal(a.getDiagnosticoPrincipal());
        e.setDiagnosticosDiferenciales(a.getDiagnosticosDiferenciales());
        e.setTratamientoPrescrito(a.getTratamientoPrescrito());
        e.setObservacionesGenerales(a.getObservacionesGenerales());
        e.setIndicacionesDueno(a.getIndicacionesDueno());
        e.setProximaCitaRecomendada(a.getProximaCitaRecomendada());
        e.setEstadoAtencion(a.getEstadoAtencion());
        LocalDateTime ahora = LocalDateTime.now();
        e.setCreatedAt(a.getCreatedAt() != null ? a.getCreatedAt() : ahora);
        e.setUpdatedAt(ahora);
        return e;
    }

    static AtencionClinica aDominio(AtencionClinicaJpaEntity e) {
        AtencionClinica a = new AtencionClinica();
        a.setId(e.getId() != null ? new AtencionClinica.AtencionId(e.getId()) : null);
        a.setIdCita(e.getIdCita());
        a.setIdVeterinario(e.getIdVeterinario());
        a.setIdMascota(e.getIdMascota());
        a.setFechaHoraInicio(e.getFechaHoraInicio());
        a.setFechaHoraFin(e.getFechaHoraFin());
        a.setMotivoDetallado(e.getMotivoDetallado());
        a.setAnamnesis(e.getAnamnesis());
        a.setSintomasObservados(e.getSintomasObservados());
        a.setPesoKg(e.getPesoKg());
        a.setTemperaturaC(e.getTemperaturaC());
        a.setFrecCardiaca(e.getFrecCardiaca());
        a.setFrecRespiratoria(e.getFrecRespiratoria());
        a.setDiagnosticoPrincipal(e.getDiagnosticoPrincipal());
        a.setDiagnosticosDiferenciales(e.getDiagnosticosDiferenciales());
        a.setTratamientoPrescrito(e.getTratamientoPrescrito());
        a.setObservacionesGenerales(e.getObservacionesGenerales());
        a.setIndicacionesDueno(e.getIndicacionesDueno());
        a.setProximaCitaRecomendada(e.getProximaCitaRecomendada());
        a.setEstadoAtencion(e.getEstadoAtencion());
        a.setCreatedAt(e.getCreatedAt());
        a.setUpdatedAt(e.getUpdatedAt());
        return a;
    }
}