package com.udo.can_cat.mascotas.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import com.udo.can_cat.atenciones.domain.exception.MascotaNoEncontradaException;
import com.udo.can_cat.mascotas.application.dto.ActualizarMascotaRequestDTO;
import com.udo.can_cat.mascotas.application.dto.MascotaRegistradaResponseDTO;
import com.udo.can_cat.mascotas.application.dto.RegistrarMascotaRequestDTO;
import com.udo.can_cat.mascotas.domain.entity.Especie;
import com.udo.can_cat.mascotas.domain.entity.Especie.EspecieId;
import com.udo.can_cat.mascotas.domain.entity.Mascota;
import com.udo.can_cat.mascotas.domain.entity.Mascota.Sexo;
import com.udo.can_cat.mascotas.domain.entity.Raza;
import com.udo.can_cat.mascotas.domain.entity.Raza.RazaId;
import com.udo.can_cat.mascotas.domain.repository.EspecieRepository;
import com.udo.can_cat.mascotas.domain.repository.MascotaRepository;
import com.udo.can_cat.mascotas.domain.repository.RazaRepository;
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Cliente.ClienteId;
import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MascotaApplicationService {

    private final MascotaRepository mascotaRepository;
    private final EspecieRepository especieRepository;
    private final RazaRepository razaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public MascotaApplicationService(MascotaRepository mascotaRepository,
                                     EspecieRepository especieRepository,
                                     RazaRepository razaRepository,
                                     ClienteRepository clienteRepository,
                                     UsuarioRepository usuarioRepository) {
        this.mascotaRepository = mascotaRepository;
        this.especieRepository = especieRepository;
        this.razaRepository = razaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ═══════════════════════════════════════════════════════
    // CREAR
    // ═══════════════════════════════════════════════════════

    @Transactional
    public MascotaRegistradaResponseDTO registrarMascota(RegistrarMascotaRequestDTO request,
                                                          UsuarioAutenticado usuarioAutenticado) {
        ClienteId clienteId = resolverClienteId(request.documentoIdentidadCliente(), usuarioAutenticado);

        Especie especie = especieRepository.findById(new EspecieId(request.idEspecie()))
                .orElseThrow(() -> new MascotaRegistrationException("La especie especificada no existe"));

        Raza raza = validarRaza(request.idRaza(), request.idEspecie());

        if (request.fechaNacimiento() != null && request.fechaNacimiento().isAfter(LocalDate.now())) {
            throw new MascotaRegistrationException("La fecha de nacimiento no puede ser futura");
        }

        boolean duplicada = mascotaRepository.findAllByClienteId(clienteId).stream()
                .filter(Mascota::isActivo)
                .anyMatch(m -> normalizar(m.getNombre()).equals(normalizar(request.nombre())));

        if (duplicada) {
            throw new MascotaRegistrationException(
                    "Ya tienes una mascota llamada \"" + request.nombre().trim()
                    + "\". Usa un nombre distinto o revisa si ya está registrada.");
        }

        Mascota mascota = Mascota.crear(
                clienteId,
                new EspecieId(request.idEspecie()),
                request.idRaza() != null ? new RazaId(request.idRaza()) : null,
                request.nombre().trim(),
                request.fechaNacimiento(),
                Sexo.valueOf(request.sexo()),
                request.color(),
                request.pesoActual(),
                Boolean.TRUE.equals(request.esterilizado())
        );

        Mascota guardada = mascotaRepository.save(mascota);
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new MascotaRegistrationException("Cliente no encontrado"));

        return construirRespuesta(guardada, cliente, especie, raza);
    }

    // ═══════════════════════════════════════════════════════
    // ACTUALIZAR
    // ═══════════════════════════════════════════════════════

    @Transactional
    public MascotaRegistradaResponseDTO actualizarMascota(Integer mascotaId,
                                                          ActualizarMascotaRequestDTO request,
                                                          UsuarioAutenticado usuarioAutenticado) {
        Mascota mascota = mascotaRepository.findById(new Mascota.MascotaId(mascotaId))
                .orElseThrow(() -> new MascotaNoEncontradaException(mascotaId));

        validarPropiedad(mascota, usuarioAutenticado);

        Especie especie = especieRepository.findById(new EspecieId(request.idEspecie()))
                .orElseThrow(() -> new MascotaRegistrationException("La especie especificada no existe"));

        Raza raza = validarRaza(request.idRaza(), request.idEspecie());

        if (request.fechaNacimiento() != null && request.fechaNacimiento().isAfter(LocalDate.now())) {
            throw new MascotaRegistrationException("La fecha de nacimiento no puede ser futura");
        }

        // Duplicado: misma regla, pero excluyendo la propia mascota
        boolean duplicada = mascotaRepository.findAllByClienteId(mascota.getClienteId()).stream()
                .filter(Mascota::isActivo)
                .filter(m -> !m.getId().value().equals(mascotaId))
                .anyMatch(m -> normalizar(m.getNombre()).equals(normalizar(request.nombre())));

        if (duplicada) {
            throw new MascotaRegistrationException(
                    "Ya tienes otra mascota llamada \"" + request.nombre().trim() + "\".");
        }

        mascota.setNombre(request.nombre().trim());
        mascota.setEspecieId(new EspecieId(request.idEspecie()));
        mascota.setRazaId(request.idRaza() != null ? new RazaId(request.idRaza()) : null);
        mascota.setFechaNacimiento(request.fechaNacimiento());
        mascota.setSexo(Sexo.valueOf(request.sexo()));
        mascota.setColor(request.color());
        mascota.setPesoActual(request.pesoActual());
        mascota.setEsterilizado(Boolean.TRUE.equals(request.esterilizado()));

        Mascota guardada = mascotaRepository.save(mascota);
        Cliente cliente = clienteRepository.findById(mascota.getClienteId())
                .orElseThrow(() -> new MascotaRegistrationException("Cliente no encontrado"));

        return construirRespuesta(guardada, cliente, especie, raza);
    }

    // ═══════════════════════════════════════════════════════
    // CAMBIAR ESTADO (Activa / Inactiva / Fallecida)
    // ═══════════════════════════════════════════════════════

    @Transactional
    public MascotaRegistradaResponseDTO cambiarEstado(Integer mascotaId,
                                                       String nuevoEstado,
                                                       UsuarioAutenticado usuarioAutenticado) {
        Mascota mascota = mascotaRepository.findById(new Mascota.MascotaId(mascotaId))
                .orElseThrow(() -> new MascotaNoEncontradaException(mascotaId));

        validarPropiedad(mascota, usuarioAutenticado);

        switch (nuevoEstado) {
            case "Activa" -> mascota.reactivar();
            case "Inactiva" -> mascota.desactivar();
            case "Fallecida" -> mascota.marcarComoFallecida();
            default -> throw new MascotaRegistrationException(
                    "Estado inválido: " + nuevoEstado);
        }

        Mascota guardada = mascotaRepository.save(mascota);
        Cliente cliente = clienteRepository.findById(mascota.getClienteId())
                .orElseThrow(() -> new MascotaRegistrationException("Cliente no encontrado"));
        Especie especie = especieRepository.findById(mascota.getEspecieId()).orElse(null);
        Raza raza = mascota.getRazaId() != null
                ? razaRepository.findById(mascota.getRazaId()).orElse(null)
                : null;

        return construirRespuesta(guardada, cliente, especie, raza);
    }

    // ═══════════════════════════════════════════════════════
    // LISTADOS
    // ═══════════════════════════════════════════════════════

    public List<MascotaRegistradaResponseDTO> listarMascotasDeCliente(
            UsuarioAutenticado usuarioAutenticado,
            boolean incluirArchivadas) {

        if (!"Cliente".equals(usuarioAutenticado.rol())) {
            throw new MascotaRegistrationException("Este endpoint es solo para clientes");
        }

        Usuario usuario = usuarioRepository.findByCorreoElectronico(usuarioAutenticado.correo())
                .orElseThrow(() -> new MascotaRegistrationException("Usuario no encontrado"));

        ClienteId clienteId = clienteRepository.findByUsuarioId(usuario.getId())
                .map(Cliente::getId)
                .orElseThrow(() -> new MascotaRegistrationException("Perfil de cliente no encontrado"));

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new MascotaRegistrationException("Cliente no encontrado"));

        List<Mascota> mascotas = incluirArchivadas
                ? mascotaRepository.findAllByClienteId(clienteId)
                : mascotaRepository.findByClienteId(clienteId);

        return mascotas.stream()
                .map(mascota -> {
                    Especie especie = especieRepository.findById(mascota.getEspecieId()).orElse(null);
                    Raza raza = mascota.getRazaId() != null
                            ? razaRepository.findById(mascota.getRazaId()).orElse(null)
                            : null;
                    return construirRespuesta(mascota, cliente, especie, raza);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MascotaRegistradaResponseDTO> listarMascotasPorIdCliente(Integer idCliente,
                                                                         boolean incluirArchivadas) {
        Cliente cliente = clienteRepository.findById(new Cliente.ClienteId(idCliente))
                .orElseThrow(() -> new MascotaRegistrationException("Cliente no encontrado"));

        List<Mascota> mascotas = incluirArchivadas
                ? mascotaRepository.findAllByClienteId(cliente.getId())
                : mascotaRepository.findByClienteId(cliente.getId());

        return mascotas.stream()
                .map(mascota -> construirRespuesta(
                        mascota, cliente,
                        especieRepository.findById(mascota.getEspecieId()).orElse(null),
                        mascota.getRazaId() != null
                                ? razaRepository.findById(mascota.getRazaId()).orElse(null)
                                : null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MascotaRegistradaResponseDTO> getMisMascotas(Integer usuarioAutenticadoId,
                                                             boolean incluirArchivadas) {
        Cliente cliente = clienteRepository.findByUsuarioId(new UsuarioId(usuarioAutenticadoId))
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        List<Mascota> mascotas = incluirArchivadas
                ? mascotaRepository.findAllByClienteId(cliente.getId())
                : mascotaRepository.findByClienteId(cliente.getId());

        return mascotas.stream()
                .map(mascota -> {
                    Especie especie = especieRepository.findById(mascota.getEspecieId()).orElse(null);
                    Raza raza = mascota.getRazaId() != null
                            ? razaRepository.findById(mascota.getRazaId()).orElse(null)
                            : null;
                    return construirRespuesta(mascota, cliente, especie, raza);
                })
                .toList();
    }

    // ═══════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════

    private Raza validarRaza(Integer idRaza, Integer idEspecie) {
        if (idRaza == null) return null;
        Raza raza = razaRepository.findById(new RazaId(idRaza))
                .orElseThrow(() -> new MascotaRegistrationException("La raza especificada no existe"));
        if (!raza.getEspecieId().equals(new EspecieId(idEspecie))) {
            throw new MascotaRegistrationException(
                    "La raza '" + raza.getNombre() + "' no pertenece a la especie seleccionada");
        }
        return raza;
    }

    private void validarPropiedad(Mascota mascota, UsuarioAutenticado usuarioAutenticado) {
        if ("Cliente".equals(usuarioAutenticado.rol())) {
            Usuario usuario = usuarioRepository.findByCorreoElectronico(usuarioAutenticado.correo())
                    .orElseThrow(() -> new MascotaRegistrationException("Usuario no encontrado"));
            ClienteId clienteId = clienteRepository.findByUsuarioId(usuario.getId())
                    .map(Cliente::getId)
                    .orElseThrow(() -> new MascotaRegistrationException("Cliente no encontrado"));
            if (!Objects.equals(mascota.getClienteId().value(), clienteId.value())) {
                throw new AccessDeniedException("Esta mascota no te pertenece");
            }
        }
        // Recepcionista/Vet/Admin: sin restricción
    }

    private ClienteId resolverClienteId(String documentoIdentidadCliente,
                                        UsuarioAutenticado usuarioAutenticado) {
        return switch (usuarioAutenticado.rol()) {
            case "Cliente" -> {
                if (documentoIdentidadCliente != null) {
                    throw new MascotaRegistrationException(
                            "Un cliente no puede especificar un documento de identidad");
                }
                Usuario usuario = usuarioRepository.findByCorreoElectronico(usuarioAutenticado.correo())
                        .orElseThrow(() -> new MascotaRegistrationException("Usuario no encontrado"));
                yield clienteRepository.findByUsuarioId(usuario.getId())
                        .map(Cliente::getId)
                        .orElseThrow(() -> new MascotaRegistrationException(
                                "No se encontró un perfil de cliente asociado a su usuario"));
            }
            case "Recepcionista" -> {
                if (documentoIdentidadCliente == null || documentoIdentidadCliente.isBlank()) {
                    throw new MascotaRegistrationException(
                            "Debe ingresar el documento de identidad del cliente");
                }
                yield clienteRepository.findByDocumentoIdentidad(documentoIdentidadCliente.trim())
                        .map(Cliente::getId)
                        .orElseThrow(() -> new MascotaRegistrationException(
                                "No existe un cliente con ese documento"));
            }
            default -> throw new MascotaRegistrationException(
                    "Su rol no tiene permisos para registrar mascotas");
        };
    }

    private MascotaRegistradaResponseDTO construirRespuesta(Mascota mascota, Cliente cliente,
                                                             Especie especie, Raza raza) {
        return new MascotaRegistradaResponseDTO(
                mascota.getId() != null ? mascota.getId().value() : null,
                mascota.getClienteId().value(),
                cliente != null ? cliente.getNombreCompleto() : "—",
                mascota.getEspecieId().value(),
                especie != null ? especie.getNombre() : "—",
                mascota.getRazaId() != null ? mascota.getRazaId().value() : null,
                raza != null ? raza.getNombre() : null,
                mascota.getNombre(),
                mascota.getFechaNacimiento(),
                mascota.getSexo().name(),
                mascota.getColor(),
                mascota.getPesoActual(),
                mascota.isEsterilizado(),
                mascota.isActivo(),
                mascota.isFallecido(),
                mascota.getCreatedAt()
        );
    }

    public record UsuarioAutenticado(String correo, String rol) {}

    public static class MascotaRegistrationException extends RuntimeException {
        public MascotaRegistrationException(String message) { super(message); }
    }

    private String normalizar(String str) {
        if (str == null) return "";
        return java.text.Normalizer.normalize(str.trim().toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}