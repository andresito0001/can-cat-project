package com.udo.can_cat.citas.infrastructure.persistence;

import com.udo.can_cat.citas.domain.entity.Cita;
import com.udo.can_cat.citas.domain.repository.CitaRepository;
import com.udo.can_cat.usuarios.domain.entity.Cliente.ClienteId;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class CitaRepositoryImpl implements CitaRepository {

    private final CitaJpaEntityMapper mapper;
    private final CitaJpaRepository jpaRepository;

    public CitaRepositoryImpl(CitaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.mapper = new CitaJpaEntityMapper();
    }

    @Override
    public Cita guardar(Cita cita) {
        CitaJpaEntity entity = mapper.toEntity(cita);
        CitaJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Cita> buscarPorId(Integer id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Cita> buscarActivasPorVeterinarioYFecha(Integer idVeterinario,
                                                         LocalDate fecha,
                                                         List<Integer> idsEstadosActivos) {
        return jpaRepository.findByVetFechaAndEstadosActivos(idVeterinario, fecha, idsEstadosActivos)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Cita> buscarPorIdsMascotas(List<Integer> idsMascotas) {
        if (idsMascotas.isEmpty()) return List.of();
        return jpaRepository.findByIdsMascotas(idsMascotas)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existeSolapamiento(Integer idVeterinario,
                                      LocalDate fecha,
                                      LocalTime horaInicio,
                                      LocalTime horaFin,
                                      List<Integer> idsEstadosActivos) {
        return jpaRepository.existeSolapamiento(idVeterinario, fecha, horaInicio, horaFin, idsEstadosActivos);
    }

    @Override
    public List<Object[]> findCitasWithMascotaNombreByClienteId(ClienteId clienteId) {
        return jpaRepository.findCitasWithMascotaNombreByClienteId(clienteId.value());
    }
    
    public Cita toDomain(CitaJpaEntity entity) {
    return mapper.toDomain(entity);
}
    // --- Mapper interno ---

    private static class CitaJpaEntityMapper {

        Cita toDomain(CitaJpaEntity e) {
            Cita c = new Cita();
            c.setId(e.getId());
            c.setIdMascota(e.getIdMascota());
            c.setIdVeterinario(e.getIdVeterinario());
            c.setIdServicio(e.getIdServicio());
            c.setIdEstado(e.getIdEstado());
            c.setFechaCita(e.getFechaCita());
            c.setHoraInicio(e.getHoraInicio());
            c.setHoraFin(e.getHoraFin());
            c.setMotivoConsulta(e.getMotivoConsulta());
            c.setTipoAtencion(e.getTipoAtencion());
            c.setCostoUsd(e.getCostoUsd());
            c.setCostoBs(e.getCostoBs());
            c.setCostoEstimado(e.getCostoEstimado());
            c.setTasaCambioAplicada(e.getTasaCambioAplicada());
            c.setObservacionesRecepcion(e.getObservacionesRecepcion());
            c.setFechaSolicitud(e.getFechaSolicitud());
            c.setCreatedAt(e.getCreatedAt());
            c.setUpdatedAt(e.getUpdatedAt());
            return c;
        }
        CitaJpaEntity toEntity(Cita c) {
            CitaJpaEntity e = new CitaJpaEntity();
            e.setId(c.getId());
            e.setIdMascota(c.getIdMascota());
            e.setIdVeterinario(c.getIdVeterinario());
            e.setIdServicio(c.getIdServicio());
            e.setIdEstado(c.getIdEstado());
            e.setFechaCita(c.getFechaCita());
            e.setHoraInicio(c.getHoraInicio());
            e.setHoraFin(c.getHoraFin());
            e.setMotivoConsulta(c.getMotivoConsulta());
            e.setTipoAtencion(c.getTipoAtencion());
            e.setCostoUsd(c.getCostoUsd());
            e.setCostoBs(c.getCostoBs());
            e.setCostoEstimado(c.getCostoEstimado());
            e.setTasaCambioAplicada(c.getTasaCambioAplicada());
            e.setObservacionesRecepcion(c.getObservacionesRecepcion());
            e.setFechaSolicitud(c.getFechaSolicitud());
            return e;
        }
        

    }



}