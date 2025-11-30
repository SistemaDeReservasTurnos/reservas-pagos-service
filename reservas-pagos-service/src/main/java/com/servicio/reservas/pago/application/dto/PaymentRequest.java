package com.servicio.reservas.pago.application.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotNull
    private Long reservationId;
    @NotNull
    @Positive
    private Double amount;
    @Email
    private String clientEmail; // For MercadoPago and for contacting the client
}