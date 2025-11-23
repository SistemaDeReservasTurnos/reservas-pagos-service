package com.servicio.reservas.pago.domain.repository;

import com.servicio.reservas.pago.domain.entities.Payment;

import java.util.List;
import java.util.Optional;

public interface IPaymentRepository {

    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    List<Payment> findAll();
}

