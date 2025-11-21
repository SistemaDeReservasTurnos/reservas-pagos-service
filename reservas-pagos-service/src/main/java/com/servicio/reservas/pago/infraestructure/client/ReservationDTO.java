package com.servicio.reservas.pago.infraestructure.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private Long id;
    private Long serviceId;
    private Long userId;
    private Double amount;
    //Other necessary fields such as amount if calculated in Reserves
}
