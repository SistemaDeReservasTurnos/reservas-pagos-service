package com.servicio.reservas.pago.domain.entities;


import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Long id;
    private Long reservationId;
    private String externalPaymentId;
    private Double amount;
    private PaymentStatus status;
    private String paymentLink;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public boolean isApproved() {
        return status == PaymentStatus.APPROVED;
    }

    public void updateStatus(PaymentStatus newStatus, String updatedByUser) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedByUser;
    }
}

