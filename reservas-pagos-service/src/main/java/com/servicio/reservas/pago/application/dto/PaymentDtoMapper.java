package com.servicio.reservas.pago.application.dto;

import com.servicio.reservas.pago.domain.entities.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentDtoMapper {

    public Payment toDomain(PaymentRequest request){
        Payment newPayment = new Payment();
        newPayment.setReservationId(request.getReservationId());
        newPayment.setAmount(request.getAmount());

        return newPayment;
    }

    // Existente: Convierte Dominio a Response (para devolver el estado del pago)
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