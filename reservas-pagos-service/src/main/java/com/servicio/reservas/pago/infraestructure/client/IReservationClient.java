package com.servicio.reservas.pago.infraestructure.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "reservas-agenda-service")
public interface IReservationClient {

    @GetMapping("/api/reservations/{id}")
    Optional<ReservationDTO> findReservationById(@PathVariable("id") Long id);
}
