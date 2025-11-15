package com.servicio.reservas.pago.domain.repository;

import com.servicio.reservas.pago.domain.entities.Payment;
import java.util.Optional;

public interface IPaymentRepository {

    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    Optional<Payment> findByReservationId(Long reservationId);
    Optional<Payment> findByExternalPaymentId(String externalPaymentId);
}

