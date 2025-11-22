package com.servicio.reservas.pago.infraestructure.client;

import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import com.servicio.reservas.pago.application.dto.PreferenceResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

public class PreferenceMapper {

    // Convert the internal DTO to the SDK request format
    public static PreferenceRequest toSdkRequest(com.servicio.reservas.pago.application.dto.PreferenceRequest internalRequest, String notificationUrl) {

        // Maps internal items to SDK items
        java.util.List<PreferenceItemRequest> mpItems = internalRequest.getItems().stream()
                .map(item -> {

                    // Safe conversion to BigDecimal and rounding
                    BigDecimal originalPrice = new BigDecimal(String.valueOf(item.getUnitPrice()));
                    BigDecimal priceAsInteger = originalPrice.setScale(0, RoundingMode.HALF_UP);

                    return PreferenceItemRequest.builder()
                            .title(item.getTitle())
                            .unitPrice(priceAsInteger)
                            .quantity(item.getQuantity())
                            .currencyId(item.getCurrencyId() != null ? item.getCurrencyId() : "COP")
                            .build();
                })
                .collect(Collectors.toList());

        PreferenceBackUrlsRequest mpBackUrls = null;
        if (internalRequest.getBackUrls() != null) {
            mpBackUrls = PreferenceBackUrlsRequest.builder()
                    .success(internalRequest.getBackUrls().getSuccess())
                    .pending(internalRequest.getBackUrls().getPending())
                    .failure(internalRequest.getBackUrls().getFailure())
                    .build();
        }

        return PreferenceRequest.builder()
                .items(mpItems)
                .backUrls(mpBackUrls)
                .notificationUrl(notificationUrl) // URL Webhook (SRT-48)
                .externalReference(internalRequest.getExternalReference())
                .build();
    }

    // Convert the SDK response to your internal DTO
    public static PreferenceResponse fromSdkResponse(Preference mpPreference) {
        return new PreferenceResponse(
                mpPreference.getId(),
                mpPreference.getInitPoint() //The payment link
        );
    }
}