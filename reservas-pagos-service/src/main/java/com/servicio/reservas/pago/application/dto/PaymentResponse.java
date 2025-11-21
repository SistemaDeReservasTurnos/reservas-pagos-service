package com.servicio.reservas.pago.application.dto;

import com.servicio.reservas.pago.domain.entities.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private Long id;
    private Long reservationId;
    private String externalPaymentId;
    private Double amount;
    private PaymentStatus status;
    private String paymentLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
