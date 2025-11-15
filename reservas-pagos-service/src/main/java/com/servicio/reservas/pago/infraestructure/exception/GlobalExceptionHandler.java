package com.servicio.reservas.pago.infraestructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.Map;
import lombok.extern.slf4j.Slf4j; //objet log import

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePaymentNotFound(PaymentNotFoundException ex) {
        return new ResponseEntity<>(
                Map.of("error", "Not Found", "message", ex.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPaymentStatus(InvalidPaymentStatusException ex) {
        return new ResponseEntity<>(
                Map.of("error", "Bad Request", "message", ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }
        @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        log.error("Unhandled internal server error occurred.", ex);
        return new ResponseEntity<>(
                Map.of("error", "Internal Server Error", "message", "An unexpected error occurred. Contact support."),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}