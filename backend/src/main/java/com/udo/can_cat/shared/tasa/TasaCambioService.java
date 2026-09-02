package com.udo.can_cat.shared.tasa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TasaCambioService {

    private static final Logger log = LoggerFactory.getLogger(TasaCambioService.class);
    private static final String DOLAR_API_URL = "https://ve.dolarapi.com/v1/dolares/oficial";
    private static final long CACHE_MINUTOS = 60;
    private static final BigDecimal TASA_FALLBACK = new BigDecimal("65.00");

    private final RestClient restClient;
    private volatile BigDecimal tasaCache;
    private volatile LocalDateTime cacheExpiracion;

    public TasaCambioService() {
        this.restClient = RestClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "can-cat-system/1.0")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public BigDecimal obtenerTasaOficial() {
        if (tasaCache != null && cacheExpiracion != null
                && LocalDateTime.now().isBefore(cacheExpiracion)) {
            return tasaCache;
        }
        return consultarApi();
    }

    private synchronized BigDecimal consultarApi() {
        if (tasaCache != null && cacheExpiracion != null
                && LocalDateTime.now().isBefore(cacheExpiracion)) {
            return tasaCache;
        }

        log.info("Consultando tasa de cambio oficial a {}", DOLAR_API_URL);
        try {
            DolarApiResponse response = restClient.get()
                    .uri(DOLAR_API_URL)
                    .retrieve()
                    .body(DolarApiResponse.class);

            if (response == null) {
                return usarFallback("Respuesta vacía");
            }

            BigDecimal tasa = response.venta();
            if (tasa == null || tasa.compareTo(BigDecimal.ZERO) == 0) {
                tasa = response.promedio();
            }

            if (tasa == null || tasa.compareTo(BigDecimal.ZERO) == 0) {
                return usarFallback("Valores nulos o cero");
            }

            this.tasaCache = tasa;
            this.cacheExpiracion = LocalDateTime.now().plusMinutes(CACHE_MINUTOS);
            log.info("Tasa actualizada: {} VES/USD", tasa);
            return tasa;

        } catch (RestClientResponseException e) {
            log.error("Error HTTP {} de DolarApi", e.getStatusCode());
            return usarFallback("HTTP " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Error al consultar DolarApi: {}", e.getMessage());
            return usarFallback(e.getMessage());
        }
    }

    private BigDecimal usarFallback(String razon) {
        if (tasaCache != null) {
            log.warn("Fallback: usando caché anterior {}. Razón: {}", tasaCache, razon);
            cacheExpiracion = LocalDateTime.now().plusMinutes(15);
            return tasaCache;
        }
        log.warn("Fallback: usando tasa hardcodeada {}. Razón: {}", TASA_FALLBACK, razon);
        return TASA_FALLBACK;
    }

    public void invalidarCache() {
        this.tasaCache = null;
        this.cacheExpiracion = null;
    }

    // ✅ Record DTO para Jackson 3.x
    public record DolarApiResponse(
            String fuente,
            String nombre,
            BigDecimal compra,
            BigDecimal venta,
            BigDecimal promedio,
            String fechaActualizacion
    ) {}
}