package com.udo.can_cat.config;

import com.udo.can_cat.mascotas.domain.entity.Especie.EspecieId;
import com.udo.can_cat.mascotas.domain.entity.Mascota;
import com.udo.can_cat.mascotas.domain.entity.Mascota.Sexo;
import com.udo.can_cat.mascotas.domain.entity.Raza.RazaId;
import com.udo.can_cat.mascotas.domain.repository.MascotaRepository;
import com.udo.can_cat.usuarios.domain.entity.Cliente;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Personal.Cargo;
import com.udo.can_cat.usuarios.domain.entity.Rol;
import com.udo.can_cat.usuarios.domain.entity.Usuario;
import com.udo.can_cat.usuarios.domain.entity.Usuario.UsuarioId;
import com.udo.can_cat.usuarios.domain.repository.ClienteRepository;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;
import com.udo.can_cat.usuarios.domain.repository.RolRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeder de datos de prueba para el perfil 'dev'.
 * Se ejecuta UNA sola vez al arrancar la app, después de Flyway.
 * Es idempotente: si los correos ya existen, no vuelve a crear nada.
 *
 * Para desactivarlo: cambiar el perfil activo o comentar @Profile("dev").
 */
@Component
@Profile("dev")
public class DevDataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);
    private static final String PASSWORD_PLANA = "secreto123";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PersonalRepository personalRepository;
    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(UsuarioRepository usuarioRepository,
                         RolRepository rolRepository,
                         PersonalRepository personalRepository,
                         ClienteRepository clienteRepository,
                         MascotaRepository mascotaRepository,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.personalRepository = personalRepository;
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        try {
            Rol rolVet = rolRepository.findByNombreRol("Veterinario")
                    .orElseThrow(() -> new IllegalStateException("Rol 'Veterinario' no encontrado"));
            Rol rolCliente = rolRepository.findByNombreRol("Cliente")
                    .orElseThrow(() -> new IllegalStateException("Rol 'Cliente' no encontrado"));

            List<Usuario> vetsCreados = seedVeterinarios(rolVet);
            List<Cliente> clientesCreados = seedClientes(rolCliente);
            int mascotasCreadas = seedMascotas(clientesCreados);

            if (vetsCreados.isEmpty() && clientesCreados.isEmpty() && mascotasCreadas == 0) {
                log.info("[DevDataSeeder] Datos de prueba ya existentes. Sin cambios.");
            } else {
                imprimirResumen(vetsCreados, clientesCreados, mascotasCreadas);
            }
        } catch (Exception e) {
            log.error("[DevDataSeeder] Error poblando datos: {}", e.getMessage(), e);
        }
    }

    /* ═══════════════════════════════════════════════════════════════
       VETERINARIOS
       ═══════════════════════════════════════════════════════════════ */
    private List<Usuario> seedVeterinarios(Rol rolVet) {
        List<Usuario> creados = new ArrayList<>();

        // Vet 1: Consulta — L-V mañana + tarde
        Usuario v1 = crearVeterinario(
                "ana.perez@cancat.com", "Dra. Ana Pérez", "VET-000",
                Cargo.VETERINARIO, "Consulta", "MPPS-12345",
                horarioSemanal(
                        new String[]{"1", "2", "3", "4", "5"},
                        "08:00", "12:00", "14:00", "18:00"
                ),
                rolVet
        );
        if (v1 != null) creados.add(v1);

        // Vet 2: Cirugía — L-V mañana + tarde, turno corrido
        Usuario v2 = crearVeterinario(
                "carlos.mendez@cancat.com", "Dr. Carlos Méndez", "VET-002",
                Cargo.VETERINARIO, "Cirugia", "MPPS-23456",
                horarioSemanal(
                        new String[]{"1", "2", "3", "4", "5"},
                        "09:00", "13:00", "15:00", "19:00"
                ),
                rolVet
        );
        if (v2 != null) creados.add(v2);

        // Vet 3: Vacunación — Martes a Sábado
        Usuario v3 = crearVeterinario(
                "lucia.fernandez@cancat.com", "Dra. Lucía Fernández", "VET-003",
                Cargo.VETERINARIO, "Vacunacion", "MPPS-34567",
                horarioSemanal(
                        new String[]{"2", "3", "4", "5", "6"},
                        "10:00", "14:00", "16:00", "20:00"
                ),
                rolVet
        );
        if (v3 != null) creados.add(v3);

        // Vet 4: Estética — L-V turno continuo, mañana y tarde sin partido
        Usuario v4 = crearVeterinario(
                "roberto.silva@cancat.com", "Dr. Roberto Silva", "VET-004",
                Cargo.VETERINARIO, "Estetica", "MPVS-45678",
                horarioSemanal(
                        new String[]{"1", "2", "3", "4", "5"},
                        "08:00", "12:00", "13:00", "17:00"
                ),
                rolVet
        );
        if (v4 != null) creados.add(v4);

        return creados;
    }

    private Usuario crearVeterinario(String correo, String nombre, String codigo,
                                     Cargo cargo, String especialidad, String licencia,
                                     Map<String, Object> horario, Rol rol) {

        if (usuarioRepository.existsByCorreoElectronico(correo)) {
            log.debug("[DevDataSeeder] Vet ya existe: {}", correo);
            return null;
        }

        Usuario usuario = Usuario.crear(
                rol.getId(),
                correo,
                passwordEncoder.encode(PASSWORD_PLANA)
        );
        usuario = usuarioRepository.save(usuario);

        Personal personal = new Personal(
                null,
                usuario.getId(),
                nombre,
                new Personal.CodigoEmpleado(codigo),
                cargo,
                new Personal.Especialidad(especialidad),
                new Personal.FechaContratacion(LocalDate.now().minusYears(1)),
                true,
                new Personal.HorarioAtencion(horario),
                new Personal.LicenciaProfesional(licencia),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        personalRepository.save(personal);

        log.info("[DevDataSeeder] ✓ Vet creado: {} ({}, {})", correo, nombre, especialidad);
        return usuario;
    }

    /* ═══════════════════════════════════════════════════════════════
       CLIENTES
       ═══════════════════════════════════════════════════════════════ */
    private List<Cliente> seedClientes(Rol rolCliente) {
        List<Cliente> creados = new ArrayList<>();

        creados.add(crearCliente(
                "maria.gonzalez@email.com", "María González",
                "V-12345678", "0414-1111111", "0424-1111111",
                "Av. Principal, Casa 5, Sector Centro", "Barcelona",
                LocalDate.of(1990, 5, 20), rolCliente
        ));

        creados.add(crearCliente(
                "pedro.ramirez@email.com", "Pedro Ramírez",
                "V-23456789", "0424-2222222", null,
                "Calle Bolívar, Galpón 12", "Puerto La Cruz",
                LocalDate.of(1985, 11, 3), rolCliente
        ));

        creados.add(crearCliente(
                "carmen.silva@email.com", "Carmen Silva",
                "V-34567890", "0412-3333333", "0212-3333333",
                "Urb. Los Chaguaramos, Torre A, Apto 4B", "Lechería",
                LocalDate.of(1992, 8, 14), rolCliente
        ));

        creados.add(crearCliente(
                "jose.martinez@email.com", "José Martínez",
                "V-45678901", "0416-4444444", null,
                "Calle Sucre, Casa 22", "Anaco",
                LocalDate.of(1978, 2, 28), rolCliente
        ));

        creados.add(crearCliente(
                "luisa.torres@email.com", "Luisa Torres",
                "V-56789012", "0426-5555555", null,
                "Av. Intercomunal, Res. Los Pinos, Apto 8", "Barcelona",
                LocalDate.of(1995, 10, 7), rolCliente
        ));

        return creados;
    }

    private Cliente crearCliente(String correo, String nombre, String documento,
                                 String telPrincipal, String telSecundario,
                                 String direccion, String ciudad,
                                 LocalDate fechaNacimiento, Rol rol) {

        if (usuarioRepository.existsByCorreoElectronico(correo)) {
            log.debug("[DevDataSeeder] Cliente ya existe: {}", correo);
            // Devolvemos el cliente existente para poder crear sus mascotas
            return usuarioRepository.findByCorreoElectronico(correo)
                    .flatMap(u -> clienteRepository.findByUsuarioId(u.getId()))
                    .orElse(null);
        }

        Usuario usuario = Usuario.crear(
                rol.getId(),
                correo,
                passwordEncoder.encode(PASSWORD_PLANA)
        );
        usuario = usuarioRepository.save(usuario);

        Cliente cliente = Cliente.crear(
                usuario.getId(),
                nombre,
                documento,
                telPrincipal,
                telSecundario,
                direccion,
                ciudad,
                fechaNacimiento,
                null
        );
        cliente = clienteRepository.save(cliente);

        log.info("[DevDataSeeder] ✓ Cliente creado: {} ({})", correo, nombre);
        return cliente;
    }

    /* ═══════════════════════════════════════════════════════════════
       MASCOTAS
       ═══════════════════════════════════════════════════════════════ */
    private int seedMascotas(List<Cliente> clientes) {
        if (clientes == null || clientes.isEmpty()) return 0;

        // Mapa por documento para ubicar fácilmente cada cliente
        Map<String, Cliente> porDocumento = new LinkedHashMap<>();
        for (Cliente c : clientes) {
            if (c != null) porDocumento.put(c.getDocumentoIdentidad(), c);
        }

        int creadas = 0;

        // María → Rocky + Luna
        creadas += crearMascotaSegura(porDocumento.get("V-12345678"),
                "Rocky", 1, 3, Sexo.M, LocalDate.of(2020, 3, 15),
                "Marrón con manchas blancas", new BigDecimal("25.50"), true);

        creadas += crearMascotaSegura(porDocumento.get("V-12345678"),
                "Luna", 2, 5, Sexo.H, LocalDate.of(2021, 6, 10),
                "Gris", new BigDecimal("4.20"), true);

        // Pedro → Max
        creadas += crearMascotaSegura(porDocumento.get("V-23456789"),
                "Max", 1, 2, Sexo.M, LocalDate.of(2019, 11, 20),
                "Negro y fuego", new BigDecimal("32.00"), false);

        // Carmen → Bella
        creadas += crearMascotaSegura(porDocumento.get("V-34567890"),
                "Bella", 1, 1, Sexo.H, LocalDate.of(2022, 1, 5),
                "Blanco", new BigDecimal("12.80"), false);

        // José → Toby
        creadas += crearMascotaSegura(porDocumento.get("V-45678901"),
                "Toby", 1, 3, Sexo.M, LocalDate.of(2018, 9, 12),
                "Amarillo", new BigDecimal("28.00"), true);

        // Luisa → Michi
        creadas += crearMascotaSegura(porDocumento.get("V-56789012"),
                "Michi", 2, 6, Sexo.H, LocalDate.of(2023, 2, 28),
                "Blanco y gris", new BigDecimal("3.50"), false);

        return creadas;
    }

    private int crearMascotaSegura(Cliente cliente, String nombre,
                                   int idEspecie, int idRaza, Sexo sexo,
                                   LocalDate fechaNac, String color,
                                   BigDecimal peso, boolean esterilizado) {
        if (cliente == null) return 0;

        // Evitar duplicado si el seeder corre dos veces
        boolean yaExiste = mascotaRepository.findAllByClienteId(cliente.getId()).stream()
                .anyMatch(m -> m.getNombre().equalsIgnoreCase(nombre));
        if (yaExiste) return 0;

        Mascota mascota = Mascota.crear(
                cliente.getId(),
                new EspecieId(idEspecie),
                new RazaId(idRaza),
                nombre,
                fechaNac,
                sexo,
                color,
                peso,
                esterilizado
        );
        mascotaRepository.save(mascota);
        log.info("[DevDataSeeder] ✓ Mascota creada: {} ({})", nombre, cliente.getNombreCompleto());
        return 1;
    }

    /* ═══════════════════════════════════════════════════════════════
       HELPERS DE HORARIO
       ═══════════════════════════════════════════════════════════════

       Formato JSONB esperado por el frontend:
       {
         "1": [{"inicio":"08:00","fin":"12:00"},{"inicio":"14:00","fin":"18:00"}],
         "2": [...],
         ...
       }
       Días: 1=Lunes ... 7=Domingo.
     */
    private Map<String, Object> horarioSemanal(String[] dias,
                                                String mananaInicio, String mananaFin,
                                                String tardeInicio, String tardeFin) {
        Map<String, Object> horario = new LinkedHashMap<>();
        for (String dia : dias) {
            List<Map<String, String>> bloques = new ArrayList<>();
            bloques.add(bloque(mananaInicio, mananaFin));
            bloques.add(bloque(tardeInicio, tardeFin));
            horario.put(dia, bloques);
        }
        return horario;
    }

    private Map<String, String> bloque(String inicio, String fin) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("inicio", inicio);
        m.put("fin", fin);
        return m;
    }

    /* ═══════════════════════════════════════════════════════════════
       RESUMEN FINAL
       ═══════════════════════════════════════════════════════════════ */
    private void imprimirResumen(List<Usuario> vets, List<Cliente> clientes, int mascotas) {
        log.info("");
        log.info("╔═══════════════════════════════════════════════════════════════════╗");
        log.info("║          SEED DE DATOS DE PRUEBA COMPLETADO                       ║");
        log.info("╠═══════════════════════════════════════════════════════════════════╣");
        log.info("║  Contraseña común para TODOS los usuarios: {}             ║", PASSWORD_PLANA);
        log.info("╠═══════════════════════════════════════════════════════════════════╣");
        log.info("║  VETERINARIOS                                                     ║");
        log.info("║  ─────────────────────────────────────────────────────────────    ║");
        log.info("║  ana.perez@cancat.com         → Consulta      (L-V 08-12, 14-18)  ║");
        log.info("║  carlos.mendez@cancat.com     → Cirugía       (L-V 09-13, 15-19)  ║");
        log.info("║  lucia.fernandez@cancat.com   → Vacunación    (M-S 10-14, 16-20)  ║");
        log.info("║  roberto.silva@cancat.com     → Estética      (L-V 08-12, 13-17)  ║");
        log.info("╠═══════════════════════════════════════════════════════════════════╣");
        log.info("║  CLIENTES                                                         ║");
        log.info("║  ─────────────────────────────────────────────────────────────    ║");
        log.info("║  maria.gonzalez@email.com     → Rocky, Luna                       ║");
        log.info("║  pedro.ramirez@email.com      → Max                               ║");
        log.info("║  carmen.silva@email.com       → Bella                             ║");
        log.info("║  jose.martinez@email.com      → Toby                              ║");
        log.info("║  luisa.torres@email.com       → Michi                             ║");
        log.info("╠═══════════════════════════════════════════════════════════════════╣");
        log.info("║  ADMINISTRADOR (del seed V8)                                      ║");
        log.info("║  ─────────────────────────────────────────────────────────────    ║");
        log.info("║  admin@cancat.com             → Contraseña: (la de V8)            ║");
        log.info("╚═══════════════════════════════════════════════════════════════════╝");
        log.info("");
    }
}