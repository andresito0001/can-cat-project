package com.udo.can_cat.shared.banco;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.banco")
public class DatosBancariosProperties {

    private String nombreTitular;
    private String rif;
    private BancoCuenta bancoPrincipal = new BancoCuenta();
    private PagoMovil pagoMovil = new PagoMovil();
    private String horarioAtencion;
    private String nota;

    public static class BancoCuenta {
        private String codigo;
        private String nombre;
        private String cuenta;
        private String tipo;
        // getters/setters
        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getCuenta() { return cuenta; }
        public void setCuenta(String cuenta) { this.cuenta = cuenta; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
    }

    public static class PagoMovil {
        private String banco;
        private String telefono;
        private String cedula;
        // getters/setters
        public String getBanco() { return banco; }
        public void setBanco(String banco) { this.banco = banco; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getCedula() { return cedula; }
        public void setCedula(String cedula) { this.cedula = cedula; }
    }

    // getters/setters principales
    public String getNombreTitular() { return nombreTitular; }
    public void setNombreTitular(String nombreTitular) { this.nombreTitular = nombreTitular; }
    public String getRif() { return rif; }
    public void setRif(String rif) { this.rif = rif; }
    public BancoCuenta getBancoPrincipal() { return bancoPrincipal; }
    public void setBancoPrincipal(BancoCuenta bancoPrincipal) { this.bancoPrincipal = bancoPrincipal; }
    public PagoMovil getPagoMovil() { return pagoMovil; }
    public void setPagoMovil(PagoMovil pagoMovil) { this.pagoMovil = pagoMovil; }
    public String getHorarioAtencion() { return horarioAtencion; }
    public void setHorarioAtencion(String horarioAtencion) { this.horarioAtencion = horarioAtencion; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }
}