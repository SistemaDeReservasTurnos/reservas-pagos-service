package com.servicio.reservas.pago.application.services;

import com.servicio.reservas.pago.application.dto.PaymentRequest;
import com.servicio.reservas.pago.application.dto.PaymentResponse;
import com.servicio.reservas.pago.domain.entities.Payment;

import java.util.List;
import java.util.Optional;

public interface IPaymentService {

    PaymentResponse createPayment(PaymentRequest request);
    Optional<PaymentResponse> getPaymentById(Long paymentId);
    Payment updatePaymentStatus(Long paymentId, String newStatus, String updatedBy);
    byte[] generatePaymentVoucher(Long paymentId);
    List<PaymentResponse> findAllPayments();
}