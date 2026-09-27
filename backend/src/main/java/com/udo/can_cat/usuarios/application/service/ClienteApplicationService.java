package com.udo.can_cat.usuarios.application.service;

import com.udo.can_cat.usuarios.application.dto.ActualizarClienteRequestDTO;
import com.udo.can_cat.usuarios.application.dto.ClienteDTO;
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteApplicationService {
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public ClienteApplicationService(ClienteRepository clienteRepository,
                                     UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ClienteDTO crearCliente(ClienteDTO dto) {
        UsuarioId usuarioId = new UsuarioId(dto.usuarioId());
        usuarioRepository.findById(new com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId(usuarioId.value()))
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));

        clienteRepository.findByDocumentoIdentidad(dto.documentoIdentidad())
                .ifPresent(c -> { throw new IllegalStateException("Ya existe un cliente con ese documento"); });

        clienteRepository.findByUsuarioId(usuarioId)
                .ifPresent(c -> { throw new IllegalStateException("El usuario ya tiene un cliente asociado"); });

        Cliente cliente = Cliente.crear (
                usuarioId,
                dto.nombreCompleto(),
                dto.documentoIdentidad(),
                dto.telefonoPrincipal(),
                dto.telefonoSecundario(),
                dto.direccion(),
                dto.ciudad(),
                dto.fechaNacimiento(),
                dto.preferenciasNotificacion()
        );

        Cliente guardado = clienteRepository.save(cliente);
        return toDTO(guardado);
    }

    @Transactional(readOnly = true)
    public ClienteDTO obtenerPorId(Integer id) {
        Cliente cliente = clienteRepository.findById(new Cliente.ClienteId(id))
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return toDTO(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteDTO obtenerPorUsuarioId(Integer usuarioId) {
        Cliente cliente = clienteRepository.findByUsuarioId(new UsuarioId(usuarioId))
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado para ese usuario"));
        return toDTO(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteDTO obtenerPorDocumento(String documento) {
        Cliente cliente = clienteRepository.findByDocumentoIdentidad(documento)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return toDTO(cliente);
    }

    @Transactional
    public ClienteDTO actualizarCliente(Integer id, ActualizarClienteRequestDTO req) {
        Cliente actual = clienteRepository.findById(new Cliente.ClienteId(id))
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));

        // Validar duplicado de documento contra otros clientes
        clienteRepository.findByDocumentoIdentidad(req.documentoIdentidad())
                .filter(c -> !c.getId().value().equals(id))
                .ifPresent(c -> { throw new IllegalStateException("El documento ya está registrado en otro cliente"); });

        Cliente actualizado = new Cliente(
            actual.getId(),
            actual.getUsuarioId(),
            req.nombreCompleto().trim(),
            req.documentoIdentidad().trim(),
            req.telefonoPrincipal().trim(),
            limpiar(req.telefonoSecundario()),
            limpiar(req.direccion()),
            limpiar(req.ciudad()),
            req.fechaNacimiento(),
            actual.getPreferenciasNotificacion(),
            actual.getCreatedAt(),
            LocalDateTime.now()
        );

        return toDTO(clienteRepository.save(actualizado));
    }

    private String limpiar(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
    
    private ClienteDTO toDTO(Cliente cliente) {
        return new ClienteDTO(
                cliente.getId() != null ? cliente.getId().value() : null,
                cliente.getUsuarioId().value(),
                cliente.getNombreCompleto(),
                cliente.getDocumentoIdentidad(),
                cliente.getTelefonoPrincipal(),
                cliente.getTelefonoSecundario(),
                cliente.getDireccion(),
                cliente.getCiudad(),
                cliente.getFechaNacimiento(),
                cliente.getPreferenciasNotificacion(),
                cliente.getCreatedAt(),
                cliente.getUpdatedAt()
        );
    }
}