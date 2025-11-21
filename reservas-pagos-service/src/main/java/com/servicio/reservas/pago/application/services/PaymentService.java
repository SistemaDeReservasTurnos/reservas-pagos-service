package com.servicio.reservas.pago.application.services;

import com.servicio.reservas.pago.application.dto.PaymentDtoMapper;
import com.servicio.reservas.pago.application.dto.PaymentResponse;
import com.servicio.reservas.pago.application.dto.PreferenceRequest;
import com.servicio.reservas.pago.application.dto.PreferenceResponse;
import com.servicio.reservas.pago.domain.entities.Payment;
import com.servicio.reservas.pago.domain.entities.PaymentStatus;
import com.servicio.reservas.pago.domain.repository.IPaymentRepository;
import com.servicio.reservas.pago.infraestructure.client.IGatewayPaymentPort;
import com.servicio.reservas.pago.infraestructure.client.IReservationClient;
import com.servicio.reservas.pago.infraestructure.client.ReservationDTO;
import com.servicio.reservas.pago.infraestructure.exception.*;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final IPaymentRepository paymentRepository;
    private final PaymentDtoMapper paymentDtoMapper;
    private final IReservationClient reservationClient;
    private final IGatewayPaymentPort gatewayPaymentPort;
    private static final String AUDIT_USER_ID = "RESERVAS_SERVICE";

    @Builder
    @Override
    @Transactional
    public PaymentResponse createPayment(Long reservationId) {
        ReservationDTO reservation = reservationClient.findReservationById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with ID: " + reservationId));

        Payment initialPayment = Payment.builder()
                .reservationId(reservationId)
                .amount(reservation.getAmount())
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(AUDIT_USER_ID)
                .build();

        Payment savedPayment = paymentRepository.save(initialPayment);

        PreferenceRequest preferenceRequest = new PreferenceRequest(
                Collections.singletonList(
                        new PreferenceRequest.Item(
                                "Service Reservation No. " + reservationId,
                                reservation.getAmount(),
                                1
                        )
                ),
                PreferenceRequest.BackUrls.builder()
                        .success("http://localhost:8085/payments/sucess")   // Debe ser una URL válida (ej. http://localhost:8080/success)
                        .pending("http://localhost:8086/payments/pending") // URL válida
                        .failure("http://localhost:8087/payments/failure")  // URL válida
                        .build(),
                savedPayment.getId().toString()
        );
        // 4. Llamar al Adaptador de Mercado Pago
        PreferenceResponse mpResponse = gatewayPaymentPort.createPaymentPreference(preferenceRequest)
                .orElseThrow(() -> new ExternalPaymentGatewayException("Could not create payment preference with Mercado Pago."));

        // 5. Actualizar la entidad con datos externos
        savedPayment.setExternalPaymentId(mpResponse.getId());
        savedPayment.setPaymentLink(mpResponse.getInit_point());

        Payment finalPayment = paymentRepository.save(savedPayment);

        // 6. Devolver la respuesta al cliente con el link de pago
        return paymentDtoMapper.toResponse(finalPayment);
    }

//    @Override
//    @Transactional
//    public Payment createInitialPayment(Payment payment) {
//        payment.setStatus(PaymentStatus.PENDING);
//        payment.setCreatedAt(LocalDateTime.now());
//        payment.setCreatedBy(AUDIT_USER_ID);
//
//        return paymentRepository.save(payment);
//    }

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
