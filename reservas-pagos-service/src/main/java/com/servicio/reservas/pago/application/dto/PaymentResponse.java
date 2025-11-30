package com.servicio.reservas.pago.application.dto;

import com.servicio.reservas.pago.domain.entities.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
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
