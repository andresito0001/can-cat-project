package com.udo.can_cat.usuarios.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.udo.can_cat.usuarios.application.dto.ActualizarPersonalRequestDTO;
import com.udo.can_cat.usuarios.application.dto.PersonalDTO;
import com.udo.can_cat.usuarios.application.dto.RegistrarPersonalRequestDTO;
import com.udo.can_cat.usuarios.application.dto.UsuarioDTO;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Personal.*;
import com.udo.can_cat.usuarios.domain.entity.Rol;
import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;
import com.udo.can_cat.usuarios.domain.repository.RolRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;

@Service
public class PersonalApplicationService {

    private final PersonalRepository personalRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioApplicationService usuarioApplicationService;
    private final RolRepository rolRepository;

    public PersonalApplicationService(PersonalRepository personalRepository,
                                      UsuarioRepository usuarioRepository,
                                      UsuarioApplicationService usuarioApplicationService,
                                      RolRepository rolRepository) {
        this.personalRepository = personalRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioApplicationService = usuarioApplicationService;
        this.rolRepository = rolRepository;
    }

    // ────────────────────────────────────────────────────────
    // CRUD
    // ────────────────────────────────────────────────────────

    @Transactional
    public PersonalDTO crearPersonal(PersonalDTO dto) {
        UsuarioId usuarioId = new UsuarioId(dto.usuarioId());
        usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        personalRepository.findByCodigoEmpleado(new CodigoEmpleado(dto.codigoEmpleado()))
                .ifPresent(c -> { throw new IllegalStateException("El código de empleado ya existe"); });

        personalRepository.findByUsuarioId(usuarioId)
                .ifPresent(c -> { throw new IllegalStateException("El usuario ya tiene un personal asociado"); });

        return toDTO(personalRepository.save(
            new Personal(null, usuarioId, dto.nombreCompleto(),
                new CodigoEmpleado(dto.codigoEmpleado()),
                Cargo.fromDbValue(dto.cargo()),
                dto.especialidad() != null ? new Especialidad(dto.especialidad()) : null,
                new FechaContratacion(dto.fechaContratacion()),
                dto.activo() != null ? dto.activo() : true,
                new HorarioAtencion(dto.horarioAtencion()),
                dto.licenciaProfesional() != null ? new LicenciaProfesional(dto.licenciaProfesional()) : null,
                null, null)
        ));
    }

    @Transactional
    public PersonalDTO registrarPersonalConUsuario(RegistrarPersonalRequestDTO request) {
        String nombreRol = switch (request.cargo()) {
            case VETERINARIO -> "Veterinario";
            case RECEPCIONISTA -> "Recepcionista";
            case ENCARGADO_ALMACEN -> "Encargado_Almacen";
            case ADMINISTRADOR -> "Administrador";
        };

        Rol rol = rolRepository.findByNombreRol(nombreRol)
                .orElseThrow(() -> new IllegalArgumentException("El rol '" + nombreRol + "' no existe"));

        if (usuarioRepository.existsByCorreoElectronico(request.correoElectronico())) {
            throw new IllegalStateException("El correo electrónico ya está registrado");
        }

        // ─── NUEVO: autogenerar código si no vino ───
        String codigo = (request.codigoEmpleado() == null || request.codigoEmpleado().isBlank())
                ? generarCodigoEmpleado(request.cargo())
                : request.codigoEmpleado().trim();

        // Validar que no choque (por si el usuario lo pasó manual)
        personalRepository.findByCodigoEmpleado(new Personal.CodigoEmpleado(codigo))
                .ifPresent(p -> { throw new IllegalStateException("El código de empleado ya existe"); });

        UsuarioDTO usuarioDTO = UsuarioDTO.paraCrear(
            rol.getId().value(),
            request.correoElectronico(),
            request.contrasena(),
            rol.getNombre(),
            request.nombreCompleto(),
            "Personal"
        );

        UsuarioDTO usuarioCreado = usuarioApplicationService.crearUsuario(usuarioDTO);

        Personal personal = new Personal(
            null,
            new Usuario.UsuarioId(usuarioCreado.id()),
            request.nombreCompleto(),
            new Personal.CodigoEmpleado(codigo),   // ← usa el código resuelto
            request.cargo(),
            request.especialidad() != null ? new Personal.Especialidad(request.especialidad()) : null,
            new Personal.FechaContratacion(request.fechaContratacion()),
            true,
            new Personal.HorarioAtencion(request.horarioAtencion()),
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        return toDTO(personalRepository.save(personal));
    }

    // ────────────────────────────────────────────────────────
    // Generador de códigos: PREFIX-NNN
    // ────────────────────────────────────────────────────────
    private String generarCodigoEmpleado(Cargo cargo) {
        String prefix = switch (cargo) {
            case VETERINARIO -> "VET";
            case RECEPCIONISTA -> "REC";
            case ENCARGADO_ALMACEN -> "ALM";
            case ADMINISTRADOR -> "ADM";
        };

        int maxActual = personalRepository.findAll().stream()
                .map(p -> p.getCodigoEmpleado().value())
                .filter(c -> c != null && c.startsWith(prefix + "-"))
                .mapToInt(c -> {
                    try {
                        return Integer.parseInt(c.substring(prefix.length() + 1));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        return String.format("%s-%03d", prefix, maxActual + 1);
    }

    @Transactional
    public PersonalDTO actualizarPersonal(Integer id, ActualizarPersonalRequestDTO req) {
        Personal actual = personalRepository.findById(new PersonalId(id))
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));

        // Validar código de empleado no choque con otro
        personalRepository.findByCodigoEmpleado(new CodigoEmpleado(req.codigoEmpleado()))
                .filter(p -> !p.getPersonalId().value().equals(id))
                .ifPresent(p -> { throw new IllegalStateException("El código de empleado ya está en uso"); });

        Personal actualizado = new Personal(
            actual.getPersonalId(),
            actual.getUsuarioId(),
            req.nombreCompleto(),
            new CodigoEmpleado(req.codigoEmpleado()),
            req.cargo(),
            req.especialidad() != null && !req.especialidad().isBlank()
                    ? new Especialidad(req.especialidad()) : null,
            new FechaContratacion(req.fechaContratacion()),
            actual.isActivo(),
            req.horarioAtencion() != null
            ? new Personal.HorarioAtencion(req.horarioAtencion())
            : actual.getHorarioAtencion(),
            req.licenciaProfesional() != null && !req.licenciaProfesional().isBlank()
                    ? new LicenciaProfesional(req.licenciaProfesional()) : null,
            actual.getCreatedAt(),
            LocalDateTime.now()
        );

        return toDTO(personalRepository.save(actualizado));
    }
    
    @Transactional
    public PersonalDTO cambiarActivo(Integer id, boolean activo) {
        Personal actual = personalRepository.findById(new PersonalId(id))
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con id: " + id));

        // 1. Actualizar personal.activo
        Personal actualizado = new Personal(
            actual.getPersonalId(),
            actual.getUsuarioId(),
            actual.getNombreCompleto(),
            actual.getCodigoEmpleado(),
            actual.getCargo(),
            actual.getEspecialidad(),
            actual.getFechaContratacion(),
            activo,                                     // ← nuevo valor
            actual.getHorarioAtencion(),
            actual.getLicenciaProfesional(),
            actual.getCreatedAt(),
            LocalDateTime.now()
        );
        Personal guardado = personalRepository.save(actualizado);

        // 2. Sincronizar usuario.estado para que el login respete el cambio
        usuarioRepository.findById(actual.getUsuarioId()).ifPresent(u -> {
            u.setEstado(activo
                    ? com.udo.can_cat.usuarios.domain.entity.Usuario.EstadoUsuario.Activo
                    : com.udo.can_cat.usuarios.domain.entity.Usuario.EstadoUsuario.Inactivo);
            usuarioRepository.save(u);
        });

        return toDTO(guardado);
    }

    @Transactional
    public void eliminarPorId(Integer id) {
        PersonalId pid = new Personal.PersonalId(id);
        if (!personalRepository.existsById(pid)) {
            throw new RuntimeException("Personal no encontrado con el id: " + id);
        }
        personalRepository.deleteById(pid);
    }

    // ────────────────────────────────────────────────────────
    // Consultas
    // ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PersonalDTO obtenerPorId(Integer id) {
        Personal personal = personalRepository.findById(new Personal.PersonalId(id))
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con el id: " + id));
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
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado con el código"));
        return toDTO(personal);
    }

    @Transactional(readOnly = true)
    public List<PersonalDTO> listarPersonal() {
        return personalRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<PersonalDTO> listarPorCargo(Cargo cargo) {
        return personalRepository.findAllByCargo(cargo).stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<PersonalDTO> filtrarActivos(boolean activo) {
        return personalRepository.findAllByActivo(activo).stream().map(this::toDTO).toList();
    }

    public Map<String, Object> obtenerStatsAdmin() {
        List<Personal> todos = personalRepository.findAll();
        return Map.of(
            "totalPersonal", todos.size(),
            "veterinariosActivos", todos.stream()
                .filter(p -> p.getCargo() == Cargo.VETERINARIO && p.isActivo()).count(),
            "recepcionistasActivos", todos.stream()
                .filter(p -> p.getCargo() == Cargo.RECEPCIONISTA && p.isActivo()).count(),
            "almacenActivos", todos.stream()
                .filter(p -> p.getCargo() == Cargo.ENCARGADO_ALMACEN && p.isActivo()).count(),
            "administradoresActivos", todos.stream()
                .filter(p -> p.getCargo() == Cargo.ADMINISTRADOR && p.isActivo()).count()
        );
    }

    // ────────────────────────────────────────────────────────
    // Mapper
    // ────────────────────────────────────────────────────────

    private PersonalDTO toDTO(Personal personal) {
        return new PersonalDTO(
            personal.getPersonalId().value(),
            personal.getUsuarioId().value(),
            personal.getNombreCompleto(),
            personal.getCodigoEmpleado().value(),
            personal.getCargo().getDbValue(),
            personal.getEspecialidad() != null ? personal.getEspecialidad().value() : null,
            personal.getFechaContratacion().value(),
            personal.isActivo(),
            personal.getHorarioAtencion() != null ? personal.getHorarioAtencion().value() : null,
            personal.getLicenciaProfesional() != null ? personal.getLicenciaProfesional().value() : null,
            personal.getCreatedAt(),
            personal.getUpdatedAt(),
            personal.getCorreoElectronico()
        );
    }
}