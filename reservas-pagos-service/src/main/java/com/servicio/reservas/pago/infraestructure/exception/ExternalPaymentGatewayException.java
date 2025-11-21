package com.servicio.reservas.pago.infraestructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Map 503 Service Unavailable.
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ExternalPaymentGatewayException extends RuntimeException {

    public ExternalPaymentGatewayException(String message) {
        super(message);
    }

    public ExternalPaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}