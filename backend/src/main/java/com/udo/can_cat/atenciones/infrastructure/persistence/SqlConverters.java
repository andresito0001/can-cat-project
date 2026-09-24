package com.udo.can_cat.atenciones.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Conversores tolerantes para resultados de native queries (Hibernate 6 puede
 * devolver java.time.* o java.sql.* según el dialecto; estos helpers aceptan ambos).
 */
final class SqlConverters {

    private SqlConverters() {}

    static Integer aEntero(Object valor) {
        if (valor == null) return null;
        if (valor instanceof Integer i) return i;
        if (valor instanceof Number n) return n.intValue();
        return Integer.valueOf(valor.toString());
    }

    static BigDecimal aDecimal(Object valor) {
        if (valor == null) return null;
        if (valor instanceof BigDecimal bd) return bd;
        if (valor instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(valor.toString());
    }

    static LocalDate aFecha(Object valor) {
        if (valor == null) return null;
        if (valor instanceof LocalDate d) return d;
        if (valor instanceof java.sql.Date d) return d.toLocalDate();
        if (valor instanceof java.util.Date d) return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return LocalDate.parse(valor.toString());
    }

    static LocalTime aHora(Object valor) {
        if (valor == null) return null;
        if (valor instanceof LocalTime t) return t;
        if (valor instanceof java.sql.Time t) return t.toLocalTime();
        return LocalTime.parse(valor.toString());
    }

    static LocalDateTime aFechaHora(Object valor) {
        if (valor == null) return null;
        if (valor instanceof LocalDateTime dt) return dt;
        if (valor instanceof java.sql.Timestamp ts) return ts.toLocalDateTime();
        return LocalDateTime.parse(valor.toString());
    }

    static Boolean aBooleano(Object valor) {
        if (valor == null) return null;
        if (valor instanceof Boolean b) return b;
        if (valor instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(valor.toString());
    }

    static String aTexto(Object valor) {
        if (valor == null) return null;
        if (valor instanceof String s) return s;
        if (valor instanceof Character c) return c.toString();
        return valor.toString();
    }
}