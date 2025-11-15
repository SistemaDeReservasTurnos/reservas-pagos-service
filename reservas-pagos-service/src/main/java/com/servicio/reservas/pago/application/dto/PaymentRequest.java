package com.servicio.reservas.pago.application.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull
    private Long reservationId;
    @NotNull
    @Positive
    private Double amount;
    private String clientEmail; // For MercadoPago and for contacting the client
}