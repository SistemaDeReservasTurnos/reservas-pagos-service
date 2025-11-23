package com.servicio.reservas.pago.infraestructure.exception;

public class VoucherGenerationException extends RuntimeException {
    public VoucherGenerationException(String message) {
        super(message);
    }
}
