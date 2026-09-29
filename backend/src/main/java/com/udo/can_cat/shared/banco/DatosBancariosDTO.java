package com.udo.can_cat.shared.banco;

public record DatosBancariosDTO(
        String nombreTitular,
        String rif,
        BancoCuenta bancoPrincipal,
        PagoMovil pagoMovil,
        String horarioAtencion,
        String nota
) {
    public record BancoCuenta(String codigo, String nombre, String cuenta, String tipo) {}
    public record PagoMovil(String banco, String telefono, String cedula) {}
}