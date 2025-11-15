package com.servicio.reservas.pago.application.services;

import com.servicio.reservas.pago.application.dto.PaymentDtoMapper;
import com.servicio.reservas.pago.application.dto.PaymentResponse;
import com.servicio.reservas.pago.domain.entities.Payment;
import com.servicio.reservas.pago.domain.entities.PaymentStatus;
import com.servicio.reservas.pago.domain.repository.IPaymentRepository;
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

    @Override
    @Transactional
    public Payment createInitialPayment(Payment payment) {
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setCreatedBy("RESERVAS_SERVICE");

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

        PaymentStatus paymentStatus = PaymentStatus.valueOf(newStatus);
        payment.updateStatus(paymentStatus, updatedBy);

        return paymentRepository.save(payment);
    }
}
