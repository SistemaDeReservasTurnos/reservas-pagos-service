package com.servicio.reservas.pago.infraestructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VoucherGenerationException extends RuntimeException {
    public VoucherGenerationException(String message) {
        super(message);
    }
}
