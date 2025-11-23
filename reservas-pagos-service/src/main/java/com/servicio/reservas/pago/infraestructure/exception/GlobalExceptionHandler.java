package com.servicio.reservas.pago.infraestructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import lombok.extern.slf4j.Slf4j; // Logger object import

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handler for 404 Not Found (Payments and Reservations)
    @ExceptionHandler({PaymentNotFoundException.class, ReservationNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFoundExceptions(RuntimeException ex) {
        return new ResponseEntity<>(
                Map.of("error", "Not Found", "message", ex.getMessage()),
                HttpStatus.NOT_FOUND);
    }

    // Handler for 400 Bad Request (Status validation)
    @ExceptionHandler(InvalidPaymentStatusException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPaymentStatus(InvalidPaymentStatusException ex) {
        return new ResponseEntity<>(
                Map.of("error", "Bad Request", "message", ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    // Handler for 503 Service Unavailable (Mercado Pago failure - SRT-45)
    @ExceptionHandler(ExternalPaymentGatewayException.class)
    public ResponseEntity<Map<String, String>> handleExternalPaymentGateway(ExternalPaymentGatewayException ex) {
        // Log the error for visibility in production
        log.error("External Payment Gateway Error (503): {}", ex.getMessage());

        return new ResponseEntity<>(
                Map.of("error", "Service Unavailable", "message", ex.getMessage()),
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    // Generic handler for any other 500 error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {

        // **IMPORTANT**: Log the full stack trace for diagnosis without exposing details to the client.
        log.error("Unhandled internal server error occurred.", ex);

        return new ResponseEntity<>(
                Map.of("error", "Internal Server Error", "message", "An unexpected error occurred. Please contact support."),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(VoucherGenerationException.class)
    public ResponseEntity<Map<String, String>> handleVoucherGenerationException(VoucherGenerationException ex) {
        return new ResponseEntity<>(
                Map.of("error", "Unprocessable Entity", "message", ex.getMessage()),
                HttpStatus.UNPROCESSABLE_ENTITY);
    }
}