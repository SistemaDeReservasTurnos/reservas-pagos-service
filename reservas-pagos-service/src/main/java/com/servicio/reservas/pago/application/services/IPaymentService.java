package com.servicio.reservas.pago.application.services;

import com.servicio.reservas.pago.application.dto.PaymentResponse;
import com.servicio.reservas.pago.domain.entities.Payment;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.Optional;

public interface IPaymentService {

    PaymentResponse createPayment(Long reservationId);
    Optional<PaymentResponse> getPaymentById(Long paymentId);
    Payment updatePaymentStatus(Long paymentId, String newStatus, String updatedBy);
    byte[] generatePaymentVoucher(Long paymentId);
    List<PaymentResponse> findAllPayments();
}