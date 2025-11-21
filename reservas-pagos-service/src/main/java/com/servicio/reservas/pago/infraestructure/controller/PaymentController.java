package com.servicio.reservas.pago.infraestructure.controller;

import com.servicio.reservas.pago.application.dto.PaymentResponse;
import com.servicio.reservas.pago.application.services.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@RequestParam Long reservationId) {
        PaymentResponse response = paymentService.createPayment(reservationId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhookNotification(
            @RequestParam(name = "id") Long paymentId,
            @RequestParam(name = "status") String newStatus,
            @RequestParam(name = "topic", required = false) String topic
    ) {
        final String updatedBySource = "MERCADOPAGO_WEBHOOK";
        try {
            paymentService.updatePaymentStatus(paymentId, newStatus, updatedBySource);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}