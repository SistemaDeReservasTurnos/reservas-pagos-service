package com.servicio.reservas.pago.application.dto;

import lombok.*;
import org.dom4j.tree.BackedList;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.DoubleStream;

@Builder
@Data
@NoArgsConstructor
// Archivo: PreferenceRequest.java (o el nombre que uses para el request a Mercado Pago)

// ... imports (incluyendo Lombok, si lo usas)

public class PreferenceRequest {

    private List<Item> items;
    private BackUrls backUrls; // <--- Nuevo campo para las URLs de retorno
    private String externalReference;

    // Asumiendo que usas Lombok y Records (o clases simples)
    @Data
    @Builder
    @AllArgsConstructor
    public static class Item {
        private String title;
        private Double unitPrice;
        private int quantity;
    }

    /**
     * Clase para manejar las URLs de redireccionamiento de Mercado Pago.
     */
    @Data // O usar Records
    @Builder // Para poder usar .builder()
    public static class BackUrls { // <--- CLASE AGREGADA
        private String success;
        private String pending;
        private String failure;
    }

    // Constructor que usa tu PaymentService (si no usas @Builder para todo el DTO)
    public PreferenceRequest(List<Item> items, BackUrls backUrls, String externalReference) {
        this.items = items;
        this.backUrls = backUrls;
        this.externalReference = externalReference;
    }
}