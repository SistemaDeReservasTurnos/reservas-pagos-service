package com.servicio.reservas.pago.infraestructure.controller;

import com.servicio.reservas.pago.application.dto.PaymentDtoMapper;
import com.servicio.reservas.pago.application.dto.PaymentResponse;
import com.servicio.reservas.pago.application.services.IPaymentService;
import com.servicio.reservas.pago.domain.entities.Payment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService ipaymentservice;

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> listAllPayments() {
        List<PaymentResponse> payments = ipaymentservice.findAllPayments();

        return ResponseEntity.ok().body(payments);
    }

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestParam @NotNull(message = "Reservation ID cannot be null")
            @Positive(message = "Reservation ID must be a positive number")
            Long reservationId) {

        PaymentResponse response = ipaymentservice.createPayment(reservationId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long id) {
        return ipaymentservice.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/voucher")
    public ResponseEntity<byte[]> generateVoucher(@PathVariable("id") Long paymentId) {

        byte[] pdfContents = ipaymentservice.generatePaymentVoucher(paymentId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String filename = "payment_voucher_" + paymentId + ".pdf";
        headers.setContentDispositionFormData(filename, filename);
        headers.setContentLength(pdfContents.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContents);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhookNotification(
            @RequestParam(name = "id") Long paymentId,
            @RequestParam(name = "status") String newStatus,
            @RequestParam(name = "topic", required = false) String topic
    ) {
        final String updatedBySource = "MERCADOPAGO_WEBHOOK";
        try {
            ipaymentservice.updatePaymentStatus(paymentId, newStatus, updatedBySource);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}