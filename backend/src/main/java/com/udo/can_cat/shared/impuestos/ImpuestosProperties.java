package com.udo.can_cat.shared.impuestos;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@ConfigurationProperties(prefix = "app.impuestos")
public class ImpuestosProperties {

    /** Porcentaje de IVA vigente. Ej: 16.00 = 16%. Configurable por env var IVA_PORCENTAJE. */
    private BigDecimal ivaPorcentaje = new BigDecimal("16.00");

    public BigDecimal getIvaPorcentaje() {
        return ivaPorcentaje;
    }

    public void setIvaPorcentaje(BigDecimal ivaPorcentaje) {
        this.ivaPorcentaje = ivaPorcentaje;
    }

    /**
     * Factor multiplicador para aplicar IVA sobre un monto base.
     * Ej: 16% → 1.16
     */
    public BigDecimal getFactorIva() {
        return BigDecimal.ONE.add(
            ivaPorcentaje.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
        );
    }

    /**
     * Reverse: dado un total con IVA incluido, devuelve el subtotal base.
     * Ej: total = 11.60, IVA = 16% → subtotal = 10.00
     */
    public BigDecimal extraerSubtotalDeTotal(BigDecimal totalConIva) {
        if (totalConIva == null) return BigDecimal.ZERO;
        return totalConIva.divide(getFactorIva(), 2, RoundingMode.HALF_UP);
    }

    @Bean
    public BigDecimal ivaFactor(ImpuestosProperties props) {
        return props.getFactorIva();
    }
}