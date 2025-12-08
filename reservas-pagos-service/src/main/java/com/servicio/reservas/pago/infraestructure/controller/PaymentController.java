package com.servicio.reservas.pago.infraestructure.controller;

import com.servicio.reservas.pago.application.dto.PaymentRequest;
import com.servicio.reservas.pago.application.dto.PaymentResponse;
import com.servicio.reservas.pago.application.services.IPaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService ipaymentservice;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<List<PaymentResponse>> listAllPayments() {
        List<PaymentResponse> payments = ipaymentservice.findAllPayments();

        return ResponseEntity.ok().body(payments);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or hasAuthority('ROLE_CLIENTE')")
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody @Valid @NotNull(message = "Reservation ID cannot be null")
            PaymentRequest request) {

        PaymentResponse response = ipaymentservice.createPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long id) {
        return ipaymentservice.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/voucher")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR') or hasAuthority('ROLE_CLIENTE')")
    public ResponseEntity<byte[]> generateVoucher(@PathVariable("id") Long paymentId) {

        byte[] pdfContents = ipaymentservice.generatePaymentVoucher(paymentId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String filename = "payment_voucher_" + paymentId + ".pdf";
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename(filename).build());
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