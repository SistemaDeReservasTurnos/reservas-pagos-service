package com.servicio.reservas.pago.infraestructure.persistence;

import com.servicio.reservas.pago.domain.entities.Payment;
import com.servicio.reservas.pago.domain.entities.PaymentStatus;
public class PaymentMapper {

    public static PaymentModel toModel(Payment domain) {
        PaymentModel model = new PaymentModel();
        model.setId(domain.getId());
        model.setReservationId(domain.getReservationId());
        model.setExternalPaymentId(domain.getExternalPaymentId());
        model.setAmount(domain.getAmount());
        model.setStatus(domain.getStatus());
        model.setPaymentLink(domain.getPaymentLink());
        model.setCreatedAt(domain.getCreatedAt());
        model.setUpdatedAt(domain.getUpdatedAt());
        model.setCreatedBy(domain.getCreatedBy());
        model.setUpdatedBy(domain.getUpdatedBy());

        return model;
    }

    public static Payment toDomain(PaymentModel model) {
        return new Payment(
                model.getId(),
                model.getReservationId(),
                model.getExternalPaymentId(),
                model.getAmount(),
                model.getStatus(),
                model.getPaymentLink(),
                model.getCreatedAt(),
                model.getUpdatedAt(),
                model.getCreatedBy(),
                model.getUpdatedBy()
        );
    }
}
