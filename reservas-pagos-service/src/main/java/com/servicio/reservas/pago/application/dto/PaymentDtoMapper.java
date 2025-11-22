package com.servicio.reservas.pago.application.dto;

import com.servicio.reservas.pago.domain.entities.Payment;
import org.springframework.stereotype.Component;
@Component
public class PaymentDtoMapper {

    public PaymentResponse toResponse(Payment payment){
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setReservationId(payment.getReservationId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setPaymentLink(payment.getPaymentLink());
        response.setCreatedAt(payment.getCreatedAt());

        return response;
    }
}