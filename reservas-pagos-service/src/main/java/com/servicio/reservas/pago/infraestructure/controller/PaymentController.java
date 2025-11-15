package com.servicio.reservas.pago.infraestructure.controller;

import com.servicio.reservas.pago.application.dto.PaymentDtoMapper;
import com.servicio.reservas.pago.application.dto.PaymentResponse;
import com.servicio.reservas.pago.application.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable Long id){
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
