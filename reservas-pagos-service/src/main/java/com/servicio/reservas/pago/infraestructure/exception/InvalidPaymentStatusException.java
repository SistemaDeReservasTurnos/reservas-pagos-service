package com.servicio.reservas.pago.infraestructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 400 Bad Request
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPaymentStatusException extends RuntimeException {

    public InvalidPaymentStatusException(String message) {
        super(message);
    }
}