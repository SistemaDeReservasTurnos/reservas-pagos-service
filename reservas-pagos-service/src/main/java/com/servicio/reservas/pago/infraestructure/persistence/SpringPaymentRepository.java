package com.servicio.reservas.pago.infraestructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringPaymentRepository extends JpaRepository<PaymentModel, Long> {

    Optional<PaymentModel> findByExternalPaymentId(String externalPaymentId);
    Optional<PaymentModel> findByReservationId(Long reservationId);
}