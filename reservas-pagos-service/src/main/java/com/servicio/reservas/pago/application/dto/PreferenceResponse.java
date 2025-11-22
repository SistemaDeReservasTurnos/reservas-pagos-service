package com.servicio.reservas.pago.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceResponse {
    private String id;
    private String init_point; //Payment Link
}
