package com.udo.can_cat.citas.infrastructure.scheduler;

import com.udo.can_cat.citas.domain.entity.Cita;
import com.udo.can_cat.citas.domain.entity.EstadoCita;
import com.udo.can_cat.citas.domain.repository.CitaRepository;
import com.udo.can_cat.citas.domain.repository.EstadoCitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CitasExpiracionJob {

    private static final Logger log = LoggerFactory.getLogger(CitasExpiracionJob.class);

    private final CitaRepository citaRepo;
    private final EstadoCitaRepository estadoRepo;

    public CitasExpiracionJob(CitaRepository citaRepo, EstadoCitaRepository estadoRepo) {
        this.citaRepo = citaRepo;
        this.estadoRepo = estadoRepo;
    }

    /** Corre cada 5 minutos (arranca 1 min después de levantar la app). */
    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT1M")
    @Transactional
    public void cancelarPendientesExpiradas() {
        EstadoCita cancelada = estadoRepo.buscarPorNombre("Cancelada").orElse(null);
        if (cancelada == null) {
            log.warn("Job expiración: estado 'Cancelada' no encontrado");
            return;
        }

        List<Cita> expiradas = citaRepo.buscarPendientesExpiradas(LocalDateTime.now());
        if (expiradas.isEmpty()) return;

        for (Cita c : expiradas) {
            c.setIdEstado(cancelada.getId());
            c.setExpiraEn(null);
            citaRepo.guardar(c);
        }
        log.info("Job expiración: {} cita(s) Pendiente_Pago canceladas por falta de pago",
                expiradas.size());
    }
}