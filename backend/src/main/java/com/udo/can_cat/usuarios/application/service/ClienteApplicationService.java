package com.udo.can_cat.usuarios.application.service;

import com.udo.can_cat.usuarios.application.dto.ClienteDTO;
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
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