package com.servicio.reservas.pago.application.services;

import com.servicio.reservas.pago.application.dto.PaymentDtoMapper;
import com.servicio.reservas.pago.application.dto.PaymentResponse;
import com.servicio.reservas.pago.domain.entities.Payment;
import com.servicio.reservas.pago.domain.entities.PaymentStatus;
import com.servicio.reservas.pago.domain.repository.IPaymentRepository;
import com.servicio.reservas.pago.infraestructure.exception.InvalidPaymentStatusException;
import com.servicio.reservas.pago.infraestructure.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final IPaymentRepository paymentRepository;
    private final PaymentDtoMapper paymentDtoMapper;
    private static final String AUDIT_USER_ID = "RESERVAS_SERVICE";

    @Override
    @Transactional
    public Payment createInitialPayment(Payment payment) {
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setCreatedBy(AUDIT_USER_ID);

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(paymentDtoMapper::toResponse);
    }


    @Override
    @Transactional
    public Payment updatePaymentStatus(Long paymentId, String newStatus, String updatedBy){
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with ID: "  + paymentId));

        PaymentStatus paymentStatus;
        try {
            paymentStatus = PaymentStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new InvalidPaymentStatusException("Invalid payment status provided: " + newStatus + ". Valid statuses are: PENDING, APPROVED, REJECTED, REFUNDED.");
        }

        payment.updateStatus(paymentStatus, updatedBy);

        return paymentRepository.save(payment);
    }
}
