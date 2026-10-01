package com.udo.can_cat.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Fuerza el timezone del JVM a Venezuela para que LocalDate.now()
 * y LocalTime.now() coincidan con la hora real del usuario.
 *
 * Sin esto, si el servidor corre en UTC (default en muchos entornos),
 * los filtros de "bloques pasados" en DisponibilidadApplicationService
 * aplican el margen incorrecto y bloquean horarios válidos del día actual.
 */
@Configuration
public class TimeZoneConfig {

    private static final Logger log = LoggerFactory.getLogger(TimeZoneConfig.class);
    private static final String ZONA = "America/Caracas";

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZONA));
        log.info("[TZ] Zona horaria del JVM: {}", TimeZone.getDefault().getID());
        log.info("[TZ] Hora local del servidor: {}", java.time.LocalDateTime.now());
    }
}