package com.servicio.reservas.pago.application.services;

import com.servicio.reservas.pago.application.dto.PaymentDtoMapper;
import com.servicio.reservas.pago.application.dto.PaymentResponse;
import com.servicio.reservas.pago.application.dto.PreferenceRequest;
import com.servicio.reservas.pago.application.dto.PreferenceResponse;
import com.servicio.reservas.pago.domain.entities.Payment;
import com.servicio.reservas.pago.domain.entities.PaymentStatus;
import com.servicio.reservas.pago.domain.repository.IPaymentRepository;
import com.servicio.reservas.pago.domain.services.VoucherPdfGeneratorService;
import com.servicio.reservas.pago.infraestructure.client.IGatewayPaymentPort;
import com.servicio.reservas.pago.infraestructure.client.IReservationClient;
import com.servicio.reservas.pago.infraestructure.client.ReservationDTO;
import com.servicio.reservas.pago.infraestructure.exception.*;
import feign.Param;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final VoucherPdfGeneratorService voucherPdfGeneratorService;

    @Value("${app.payment.success-url}")
    private String successUrl;

    @Value("${app.payment.pending-url}")
    private String pendingUrl;

    @Value("${app.payment.failure-url}")
    private String failureUrl;

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
                        .success(this.successUrl)
                        .pending(this.pendingUrl)
                        .failure(this.failureUrl)
                        .build(),
                savedPayment.getId().toString()
        );

        PreferenceResponse mpResponse = gatewayPaymentPort.createPaymentPreference(preferenceRequest)
                .orElseThrow(() -> new ExternalPaymentGatewayException("Could not create payment preference with Mercado Pago."));

        savedPayment.setExternalPaymentId(mpResponse.getId());
        savedPayment.setPaymentLink(mpResponse.getInit_point());

        Payment finalPayment = paymentRepository.save(savedPayment);

        return paymentDtoMapper.toResponse(finalPayment);
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

    public byte[] generatePaymentVoucher(Long paymentId) {
        Optional<Payment> paymnetOpt = paymentRepository.findById(paymentId);

        if (paymnetOpt.isEmpty()) {
            throw new PaymentNotFoundException("Payment not found with ID: " + paymentId);
        }

        Payment payment = paymnetOpt.get();

        if (!payment.getStatus().equals(PaymentStatus.APPROVED)) {
            throw new VoucherGenerationException("Cannot generate voucher for payment status: " + payment.getStatus());
        }
        return voucherPdfGeneratorService.generatePdf(payment);
    }
}
