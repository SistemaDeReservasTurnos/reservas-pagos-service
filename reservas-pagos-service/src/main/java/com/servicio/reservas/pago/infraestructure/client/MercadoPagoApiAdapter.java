package com.servicio.reservas.pago.infraestructure.client;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;

import com.servicio.reservas.pago.application.dto.PreferenceRequest;
import com.servicio.reservas.pago.application.dto.PreferenceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@Slf4j
public class MercadoPagoApiAdapter implements IGatewayPaymentPort {

    private final PreferenceClient preferenceClient;
    private final String notificationUrl;

    public MercadoPagoApiAdapter(@Value("${mercado-pago.access-token}") String accessToken,
                                 @Value("${app.notification_url}") String notificationUrl) {

        // Global SDK configuration
        MercadoPagoConfig.setAccessToken(accessToken);

        this.preferenceClient = new PreferenceClient();
        this.notificationUrl = notificationUrl;
    }

    @Override
    public Optional<PreferenceResponse> createPaymentPreference(PreferenceRequest request) {
        log.info("Creating payment preferences for reservations: {}", request.getExternalReference());

        try {
            // 1. Map internal DTO to SDK format, including the webhook URL
            com.mercadopago.client.preference.PreferenceRequest sdkRequest =
                    PreferenceMapper.toSdkRequest(request, notificationUrl);

            // 2. Direct call to the SDK
            Preference mpPreference = preferenceClient.create(sdkRequest);

            // 3. Map SDK response to internal DTO
            PreferenceResponse response = PreferenceMapper.fromSdkResponse(mpPreference);

            return Optional.of(response);

        } catch (MPApiException e) {
            // 💥 MODIFICACIÓN CLAVE: Imprime el contenido REAL del cuerpo (content) 💥
            String responseContent = e.getApiResponse() != null ? e.getApiResponse().getContent() : "No response body found.";

            log.error("Error de API de Mercado Pago: HTTP Status: {}, Response Body Content: {}",
                    e.getStatusCode(), responseContent);

            return Optional.empty();

        } catch (Exception e) {
            // Bloque genérico para otras excepciones (como MPException, si no es ApiException)
            log.error("Error inesperado creando preferencia en Mercado Pago: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}