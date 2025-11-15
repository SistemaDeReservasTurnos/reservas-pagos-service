package com.servicio.reservas.pago.application.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long reservationId;
    private Double amount;
    private String clientEmail; // For MercadoPago and for contacting the client
}