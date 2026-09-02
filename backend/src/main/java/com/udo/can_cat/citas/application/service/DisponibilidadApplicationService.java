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

    public List<VeterinarioDTO> listarVeterinarios() {
        return personalRepo.findAllByCargoAndActivo(Personal.Cargo.VETERINARIO, true)
                .stream()
                .map(p -> new VeterinarioDTO(
                        p.getPersonalId().value(),
                        p.getNombreCompleto(),
                        p.getEspecialidad().value().trim()
                ))
                .toList();
        }

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
        List<VentanaHoraria> ventanas = parsearVentanas(vet.getHorarioAtencion(), diaNumero);

        if (ventanas.isEmpty()) {
                log.info("Veterinario {} sin horario para {} ({})",
                        personalId.value(), fecha.getDayOfWeek(), fecha);

                return new DisponibilidadResponseDTO (
                        personalId.value(),
                        vet.getNombreCompleto(),
                        fecha.toString(),
                        servicio.getNombre(),
                        servicio.getDuracionMinutos(),
                        List.of());
        }

        // 4. Generar bloques candidatos
        List<BloqueHorarioDTO> bloquesGenerados = generarBloques(
                ventanas, servicio.getDuracionMinutos());

        // 5. Filtrar citas existentes (colisión de horarios)
        List<BloqueHorarioDTO> bloquesLibres = citaAppService.filtrarBloquesLibres(
                personalId.value(), fecha, bloquesGenerados);

        // 6. Descartar bloques ya pasados si es hoy
        bloquesLibres = filtrarBloquesPasados(fecha, bloquesLibres);

        log.info("Vet={}, fecha={}, servicio={}: {}/{} bloques libres",
                personalId.value(), fecha, servicio.getNombre(),
                bloquesLibres.size(), bloquesGenerados.size());

        return new DisponibilidadResponseDTO(
                personalId.value(),
                vet.getNombreCompleto(),
                fecha.toString(),
                servicio.getNombre(),
                servicio.getDuracionMinutos(),
                bloquesLibres);
        }

        // --- Privados ---
        private List<VentanaHoraria> parsearVentanas(HorarioAtencion horario, int diaNumero) {
                if (horario == null) return List.of();
                
                Object raw = horario.value().get(String.valueOf(diaNumero));
                if (raw == null) return List.of();
                
                if (!(raw instanceof List<?> lista)) return List.of();
                if (lista.isEmpty()) return List.of();
                
                List<VentanaHoraria> ventanas = new ArrayList<>();
                for (Object item : lista) {
                        if (!(item instanceof Map<?, ?> ventana)) continue;
                        
                        Object inicio = ventana.get("inicio");
                        Object fin = ventana.get("fin");
                        if (inicio == null || fin == null) continue;
                        
                        try {
                        ventanas.add(new VentanaHoraria(
                                LocalTime.parse(inicio.toString()),
                                LocalTime.parse(fin.toString())
                        ));
                        } catch (Exception e) {
                        log.warn("Formato de hora inválido: inicio={}, fin={}", inicio, fin);
                        }
                }
        return ventanas;
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