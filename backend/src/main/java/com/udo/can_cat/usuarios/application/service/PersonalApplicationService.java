package com.udo.can_cat.usuarios.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.udo.can_cat.usuarios.application.dto.PersonalDTO;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Personal.*;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonalApplicationService {
    private final PersonalRepository personalRepository;
    private final UsuarioRepository usuarioRepository;

    public PersonalApplicationService(PersonalRepository personalRepository,
                                         UsuarioRepository  usuarioRepository) {
        this.personalRepository = personalRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public PersonalDTO crearPersonal(PersonalDTO dto) {
        UsuarioId usuarioId = new UsuarioId(dto.usuarioId());
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        personalRepository.findByCodigoEmpleado(new CodigoEmpleado(dto.codigoEmpleado()))
                .ifPresent( c -> { throw new IllegalStateException("El id empleado ya existe"); });

        personalRepository.findByUsuarioId(usuarioId)
                .ifPresent(c -> { throw new IllegalStateException("El usuario ya tiene un personal asociado"); });
           
        return toDTO(personalRepository.save (
            new Personal(null, usuarioId, dto.nombreCompleto(),
                new CodigoEmpleado(dto.codigoEmpleado()),
                Cargo.fromDbValue(dto.cargo()),
                new Especialidad(dto.especialidad()),
                new FechaContratacion(dto.fechaContratacion()),
                dto.activo(),
                new HorarioAtencion(dto.horarioAtencion()),
                new LicenciaProfesional(dto.licenciaProfesional()),
                null, null)
        ));
    }

    @Transactional(readOnly = true)
    public void eliminarPorId(Integer id) {
        if (personalRepository.existsById(new Personal.PersonalId(id)))
            new RuntimeException("Personal no encontrado con el id");
        
        personalRepository.deleteById(new Personal.PersonalId(id));
    }

    @Transactional(readOnly = true)
    public PersonalDTO obtenerPorId(Integer id) {
        Personal personal = personalRepository.findById(new Personal.PersonalId(id))
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado con el id"));

        return toDTO(personal);
    }

    @Transactional(readOnly = true)
    public PersonalDTO buscarPorUsuarioId(Integer usuarioId) {
        Personal personal = personalRepository.findByUsuarioId(new UsuarioId(usuarioId))
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado con el id usuario"));

        return toDTO(personal);
    }

    @Transactional(readOnly = true)
    public PersonalDTO buscarPorCodigoEmpleado(String codigoEmpleado) {
        Personal personal = personalRepository.findByCodigoEmpleado(new CodigoEmpleado(codigoEmpleado))
            .orElseThrow(() -> new RuntimeException("Empleado no encontrado con el codigo"));

        return toDTO(personal);
    }

    @Transactional(readOnly = true)
    public List<PersonalDTO> listarPersonal() {
        return personalRepository.findAll()
            .stream()
            .map(this::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PersonalDTO> listarPorCargo(Cargo cargo) {
        return personalRepository.findAllByCargo(cargo)
            .stream()
            .map(this::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PersonalDTO> filtrarActivos(boolean activo) {
        return personalRepository.findAllByActivo(activo)
            .stream()
            .map(this::toDTO)
            .toList();
    }
    
    private PersonalDTO toDTO(Personal personal) {
        return new PersonalDTO (
            personal.getPersonalId() != null ? personal.getPersonalId().value() : null,
            personal.getUsuarioId().value(),
            personal.getNombreCompleto(),
            personal.getCodigoEmpleado().value(),
            personal.getCargo().getDbValue(),
            personal.getEspecialidad().value(),
            personal.getFechaContratacion().value(),
            personal.isActivo(),
            personal.getHorarioAtencion().value(),
            personal.getLicenciaProfesional().value(),
            personal.getCreatedAt(),
            personal.getUpdatedAt()
        );
    } 
}