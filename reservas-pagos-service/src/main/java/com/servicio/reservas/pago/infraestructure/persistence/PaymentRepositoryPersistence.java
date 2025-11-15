package com.servicio.reservas.pago.infraestructure.persistence;


import com.servicio.reservas.pago.domain.entities.Payment;
import com.servicio.reservas.pago.domain.repository.IPaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentRepositoryPersistence implements IPaymentRepository {

    private final SpringPaymentRepository springRepository;

    public PaymentRepositoryPersistence(SpringPaymentRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Override
    public Payment save(Payment payment) {
        PaymentModel model = PaymentMapper.toModel(payment);
        PaymentModel savedModel = springRepository.save(model);
        return PaymentMapper.toDomain(savedModel);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return springRepository.findById(id).map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByReservationId(Long reservationId) {
        return springRepository.findByReservationId(reservationId).map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByExternalPaymentId(String externalPaymentId) {
        return springRepository.findByExternalPaymentId(externalPaymentId).map(PaymentMapper::toDomain);
    }
}