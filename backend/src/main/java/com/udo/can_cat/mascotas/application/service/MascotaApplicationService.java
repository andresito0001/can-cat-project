package com.udo.can_cat.mascotas.application.service;

import java.time.LocalDate;
import java.util.List;
import com.udo.can_cat.mascotas.application.dto.MascotaRegistradaResponseDTO;
import com.udo.can_cat.mascotas.application.dto.RegistrarMascotaRequestDTO;
import com.udo.can_cat.mascotas.application.service.MascotaApplicationService.MascotaRegistrationException;
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
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
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

    @Transactional
    public MascotaRegistradaResponseDTO registrarMascota(RegistrarMascotaRequestDTO request,
                                                          UsuarioAutenticado usuarioAutenticado) {
        // 1. Resolver el cliente según el rol
        ClienteId clienteId = resolverClienteId(request, usuarioAutenticado);

        // 2. Validar que la especie existe
        Especie especie = especieRepository.findById(new EspecieId(request.idEspecie()))
                .orElseThrow(() -> new MascotaRegistrationException("La especie especificada no existe"));

        // 3. Validar raza si se proporcionó (y su coherencia con la especie)
        Raza raza = null;
        if (request.idRaza() != null) {
            raza = razaRepository.findById(new RazaId(request.idRaza()))
                    .orElseThrow(() -> new MascotaRegistrationException("La raza especificada no existe"));

            if (!raza.getEspecieId().equals(new EspecieId(request.idEspecie()))) {
                throw new MascotaRegistrationException(
                        "La raza '" + raza.getNombre() + "' no pertenece a la especie seleccionada");
            }
        }

        // 4. Validar fecha de nacimiento
        if (request.fechaNacimiento() != null && request.fechaNacimiento().isAfter(LocalDate.now())) {
            throw new MascotaRegistrationException("La fecha de nacimiento no puede ser futura");
        }

        // 5. Crear entidad de dominio
        Mascota mascota = Mascota.crear(
                clienteId,
                new EspecieId(request.idEspecie()),
                request.idRaza() != null ? new RazaId(request.idRaza()) : null,
                request.nombre(),
                request.fechaNacimiento(),
                Sexo.valueOf(request.sexo()),
                request.color(),
                request.pesoActual(),
                Boolean.TRUE.equals(request.esterilizado())
        );

        // 6. Persistir
        Mascota mascotaGuardada = mascotaRepository.save(mascota);

        // 7. Obtener nombre del cliente para la respuesta
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new MascotaRegistrationException("Cliente no encontrado"));

        // 8. Construir respuesta
        return construirRespuesta(mascotaGuardada, cliente, especie, raza);
    }

    private ClienteId resolverClienteId(RegistrarMascotaRequestDTO request,
                                        UsuarioAutenticado usuarioAutenticado) {
        return switch (usuarioAutenticado.rol()) {
            case "Cliente" -> {
                if (request.documentoIdentidadCliente() != null) {
                    throw new MascotaRegistrationException(
                            "Un cliente no puede especificar un documento de identidad");
                }
                
                Usuario usuario = usuarioRepository.findByCorreoElectronico(usuarioAutenticado.correo())
                        .orElseThrow(() -> new MascotaRegistrationException(
                                "Usuario autenticado no encontrado en el sistema"));
                
                        
                yield clienteRepository.findByUsuarioId(usuario.getId())
                        .map(Cliente::getId)
                        .orElseThrow(() -> new MascotaRegistrationException(
                                "No se encontró un perfil de cliente asociado a su usuario"));
            }
            case "Recepcionista" -> {
                if (request.documentoIdentidadCliente() == null 
                        || request.documentoIdentidadCliente().isBlank()) {
                    throw new MascotaRegistrationException(
                            "Debe ingresar el documento de identidad del cliente dueño de la mascota");
                }
                yield clienteRepository.findByDocumentoIdentidad(
                                request.documentoIdentidadCliente().trim())
                        .map(Cliente::getId)
                        .orElseThrow(() -> new MascotaRegistrationException(
                                "No existe un cliente registrado con el documento: " 
                                        + request.documentoIdentidadCliente()));
            }
            default -> throw new MascotaRegistrationException(
                    "Su rol no tiene permisos para registrar mascotas");
        };
    }

    private MascotaRegistradaResponseDTO construirRespuesta(Mascota mascota, Cliente cliente,
                                                            Especie especie, Raza raza) {
        return new MascotaRegistradaResponseDTO(
                mascota.getId().value(),
                mascota.getClienteId().value(),
                cliente.getNombreCompleto(),
                mascota.getEspecieId().value(),
                especie.getNombre(),
                raza != null ? raza.getId().value() : null,
                raza != null ? raza.getNombre() : null,
                mascota.getNombre(),
                mascota.getFechaNacimiento(),
                mascota.getSexo().name(),
                mascota.getColor(),
                mascota.getPesoActual(),
                mascota.isEsterilizado(),
                mascota.isActivo(),
                mascota.getCreatedAt()
        );
    }

    public List<MascotaRegistradaResponseDTO> listarMascotasDeCliente(UsuarioAutenticado usuarioAutenticado) {
        if (!"Cliente".equals(usuarioAutenticado.rol())) {
                throw new MascotaRegistrationException("Este endpoint es solo para clientes");
        }

        Usuario usuario = usuarioRepository.findByCorreoElectronico(usuarioAutenticado.correo())
                .orElseThrow(() -> new MascotaRegistrationException("Usuario no encontrado"));

        ClienteId clienteId = clienteRepository.findByUsuarioId(usuario.getId())
                .map(Cliente::getId)
                .orElseThrow(() -> new MascotaRegistrationException("Perfil de cliente no encontrado"));

        return mascotaRepository.findByClienteId(clienteId).stream()
                .map(mascota -> {
                        Especie especie = especieRepository.findById(mascota.getEspecieId()).orElse(null);
                        Raza raza = mascota.getRazaId() != null
                                ? razaRepository.findById(mascota.getRazaId()).orElse(null)
                                : null;
                        Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
                        return construirRespuesta(mascota, cliente, especie, raza);
                })
                .toList();
        }

        // En MascotaApplicationService:
        @Transactional(readOnly = true)
        public List<MascotaRegistradaResponseDTO> listarMascotasPorIdCliente(Integer idCliente) {
                Cliente cliente = clienteRepository.findById(new Cliente.ClienteId(idCliente))
                        .orElseThrow(() -> new MascotaRegistrationException("Cliente no encontrado"));

                return mascotaRepository.findByClienteId(cliente.getId()).stream()
                        .map(mascota -> construirRespuesta(
                                mascota, cliente,
                                especieRepository.findById(mascota.getEspecieId()).orElse(null),
                                mascota.getRazaId() != null
                                        ? razaRepository.findById(mascota.getRazaId()).orElse(null)
                                        : null))
                        .toList();
        }


    // Record para encapsular los datos del usuario autenticado que necesita el servicio
    public record UsuarioAutenticado(String correo, String rol) {}

    // Excepción específica del caso de uso
    public static class MascotaRegistrationException extends RuntimeException {
        public MascotaRegistrationException(String message) {
            super(message);
        }
    }
}