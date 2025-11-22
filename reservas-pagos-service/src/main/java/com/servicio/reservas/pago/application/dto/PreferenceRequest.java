package com.servicio.reservas.pago.application.dto;

import lombok.*;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
public class PreferenceRequest {

    private List<Item> items;
    private BackUrls backUrls;
    private String externalReference;

    @Data
    @AllArgsConstructor
    public static class Item {
        private String title;
        private Double unitPrice;
        private int quantity;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class BackUrls {
        private String success;
        private String pending;
        private String failure;
    }

    public PreferenceRequest(List<Item> items, BackUrls backUrls, String externalReference) {
        this.items = items;
        this.backUrls = backUrls;
        this.externalReference = externalReference;
    }
}