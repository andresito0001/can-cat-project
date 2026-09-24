package com.udo.can_cat.almacen.domain.exception;

/** No figuraba en la lista de archivos del contrato, pero el endpoint
 *  POST /productos exige 409 con el mensaje "El SKU {codigoSku} ya existe." */
public class SkuDuplicadoException extends RuntimeException {
    public SkuDuplicadoException(String codigoSku) {
        super("El SKU " + codigoSku + " ya existe.");
    }
}
