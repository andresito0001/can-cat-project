package com.udo.can_cat.citas.application.service;

import com.udo.can_cat.citas.application.dto.*;
import com.udo.can_cat.citas.domain.entity.Servicio;
import com.udo.can_cat.citas.domain.exception.HorarioNoDisponibleException;
import com.udo.can_cat.citas.domain.repository.ServicioRepository;
import com.udo.can_cat.usuarios.domain.entity.Personal;
import com.udo.can_cat.usuarios.domain.entity.Personal.HorarioAtencion;
import com.udo.can_cat.usuarios.domain.entity.Personal.PersonalId;
import com.udo.can_cat.usuarios.domain.repository.PersonalRepository;
import com.udo.can_cat.usuarios.domain.repository.UsuarioRepository;
import com.udo.can_cat.usuarios.application.dto.VeterinarioDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DisponibilidadApplicationService {

    private static final Logger log = LoggerFactory.getLogger(DisponibilidadApplicationService.class);
    private static final int MARGEN_MINUTOS_ANTICIPACION = 15;

    private final PersonalRepository personalRepo;
    private final UsuarioRepository usuarioRepo;
    private final ServicioRepository servicioRepo;
    private final CitaApplicationService citaAppService;

    public DisponibilidadApplicationService(PersonalRepository personalRepo,
                                            UsuarioRepository usuarioRepo,
                                            ServicioRepository servicioRepo,
                                            CitaApplicationService citaAppService) {
        this.personalRepo = personalRepo;
        this.usuarioRepo = usuarioRepo;
        this.servicioRepo = servicioRepo;
        this.citaAppService = citaAppService;
    }

    // ═══════════════════════════════════════════════════════════════
    // VETERINARIOS
    // ═══════════════════════════════════════════════════════════════
    public List<VeterinarioDTO> listarVeterinarios() {
        return personalRepo.findAllByCargoAndActivo(Personal.Cargo.VETERINARIO, true)
                .stream()
                .map(p -> new VeterinarioDTO(
                        p.getPersonalId().value(),
                        p.getNombreCompleto(),
                        p.getEspecialidad() != null ? p.getEspecialidad().value().trim() : "",
                        p.getHorarioAtencion() != null ? p.getHorarioAtencion().value() : Map.of()
                ))
                .toList();
    }

    // ═══════════════════════════════════════════════════════════════
    // DISPONIBILIDAD
    // ═══════════════════════════════════════════════════════════════
    public DisponibilidadResponseDTO consultarDisponibilidad(PersonalId personalId,
                                                             LocalDate fecha,
                                                             Integer idServicio) {
        Personal vet = personalRepo.findById(personalId)
                .filter(p -> p.getCargo() == Personal.Cargo.VETERINARIO)
                .filter(Personal::isActivo)
                .orElseThrow(() -> new HorarioNoDisponibleException(
                        "Veterinario no encontrado o inactivo"));

        Servicio servicio = servicioRepo.buscarPorId(idServicio)
                .filter(Servicio::getActivo)
                .orElseThrow(() -> new HorarioNoDisponibleException(
                        "Servicio no encontrado o inactivo"));

        int diaNumero = fecha.getDayOfWeek().getValue();

        // ─── LOG de debug ────────────────────────────────────
        Map<String, Object> horarioCrudo = vet.getHorarioAtencion() != null
                ? vet.getHorarioAtencion().value()
                : null;
        Object rawDia = (horarioCrudo != null) ? horarioCrudo.get(String.valueOf(diaNumero)) : null;
        log.info("DISPONIBILIDAD: vet={} fecha={} (dow={}) servicio={}",
                personalId.value(), fecha, fecha.getDayOfWeek(), idServicio);
        log.info("DISPONIBILIDAD: horario_crudo={}", horarioCrudo);
        log.info("DISPONIBILIDAD: raw_dia[{}]={} (clase={})",
                diaNumero, rawDia, rawDia != null ? rawDia.getClass().getName() : "null");

        List<VentanaHoraria> ventanas = parsearVentanas(vet.getHorarioAtencion(), diaNumero);
        log.info("DISPONIBILIDAD: ventanas_parseadas={}", ventanas);

        if (ventanas.isEmpty()) {
            log.info("DISPONIBILIDAD: sin ventanas para ese día → devolviendo lista vacía");
            return new DisponibilidadResponseDTO(
                    personalId.value(),
                    vet.getNombreCompleto(),
                    fecha.toString(),
                    servicio.getNombre(),
                    servicio.getDuracionMinutos(),
                    List.of());
        }

        // Generar bloques candidatos
        List<BloqueHorarioDTO> bloquesGenerados = generarBloques(
                ventanas, servicio.getDuracionMinutos());
        log.info("DISPONIBILIDAD: bloques_generados={}", bloquesGenerados.size());

        // Filtrar citas existentes (colisión de horarios)
        List<BloqueHorarioDTO> bloquesLibres = citaAppService.filtrarBloquesLibres(
                personalId.value(), fecha, bloquesGenerados);
        log.info("DISPONIBILIDAD: bloques_tras_filtro_citas={}", bloquesLibres.size());

        // Descartar bloques ya pasados si es hoy
        List<BloqueHorarioDTO> bloquesFinales = filtrarBloquesPasados(fecha, bloquesLibres);
        log.info("DISPONIBILIDAD: bloques_finales={}", bloquesFinales.size());

        return new DisponibilidadResponseDTO(
                personalId.value(),
                vet.getNombreCompleto(),
                fecha.toString(),
                servicio.getNombre(),
                servicio.getDuracionMinutos(),
                bloquesFinales);
    }

    // ═══════════════════════════════════════════════════════════════
    // PARSEO DEL HORARIO — robusto ante variaciones de Hibernate/Jackson
    // ═══════════════════════════════════════════════════════════════
    private List<VentanaHoraria> parsearVentanas(HorarioAtencion horario, int diaNumero) {
        if (horario == null || horario.value() == null) return List.of();

        Object raw = horario.value().get(String.valueOf(diaNumero));
        if (raw == null) return List.of();
        if (!(raw instanceof List<?> lista) || lista.isEmpty()) return List.of();

        List<VentanaHoraria> ventanas = new ArrayList<>();
        for (Object item : lista) {
            if (!(item instanceof Map<?, ?> ventana)) {
                log.warn("DISPONIBILIDAD: item no es Map: {} ({})",
                        item, item != null ? item.getClass() : "null");
                continue;
            }

            // Normalizar claves a String (Hibernate podría dar tipos raros)
            Map<String, Object> normalizado = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : ventana.entrySet()) {
                normalizado.put(String.valueOf(e.getKey()), e.getValue());
            }

            Object inicio = normalizado.get("inicio");
            Object fin = normalizado.get("fin");
            if (inicio == null || fin == null) {
                log.warn("DISPONIBILIDAD: ventana sin inicio/fin: {}", normalizado);
                continue;
            }

            try {
                LocalTime hInicio = parsearHora(inicio);
                LocalTime hFin = parsearHora(fin);
                if (hInicio == null || hFin == null || !hFin.isAfter(hInicio)) {
                    log.warn("DISPONIBILIDAD: ventana inválida inicio={} fin={}", inicio, fin);
                    continue;
                }
                ventanas.add(new VentanaHoraria(hInicio, hFin));
            } catch (Exception e) {
                log.warn("DISPONIBILIDAD: formato de hora inválido inicio={} fin={}: {}",
                        inicio, fin, e.getMessage());
            }
        }
        return ventanas;
    }

    /** Acepta "08:00", "08:00:00", o cualquier cosa que LocalTime.parse entienda. */
    private LocalTime parsearHora(Object valor) {
        if (valor == null) return null;
        String s = valor.toString().trim();
        if (s.isEmpty()) return null;
        // Si viene "08:00:00" recortamos a "08:00"
        if (s.length() > 5 && s.charAt(2) == ':') {
            s = s.substring(0, 5);
        }
        return LocalTime.parse(s);
    }

    private List<BloqueHorarioDTO> generarBloques(List<VentanaHoraria> ventanas,
                                                   int duracionMinutos) {
        List<BloqueHorarioDTO> bloques = new ArrayList<>();
        for (VentanaHoraria v : ventanas) {
            LocalTime actual = v.inicio();
            LocalTime limite = v.fin().minusMinutes(duracionMinutos);
            while (!actual.isAfter(limite)) {
                LocalTime fin = actual.plusMinutes(duracionMinutos);
                bloques.add(new BloqueHorarioDTO(actual.toString(), fin.toString()));
                actual = fin;
            }
        }
        return bloques;
    }

    private List<BloqueHorarioDTO> filtrarBloquesPasados(LocalDate fecha,
                                                          List<BloqueHorarioDTO> bloques) {
        if (!fecha.equals(LocalDate.now())) return bloques;
        LocalTime limite = LocalTime.now().plusMinutes(MARGEN_MINUTOS_ANTICIPACION);
        return bloques.stream()
                .filter(b -> LocalTime.parse(b.horaInicio()).isAfter(limite))
                .toList();
    }

    private record VentanaHoraria(LocalTime inicio, LocalTime fin) {}
}