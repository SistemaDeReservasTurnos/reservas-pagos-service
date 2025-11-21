package com.servicio.reservas.pago.infraestructure.client;

import com.servicio.reservas.pago.application.dto.PreferenceRequest;
import com.servicio.reservas.pago.application.dto.PreferenceResponse;
import java.util.Optional;

public interface IGatewayPaymentPort {
    /**
     * Creates a payment preference in the external gateway.
     * @param request Preference data (items, notification URL).
     * @return Optional with the response (ID and payment link).
     */
    Optional<PreferenceResponse> createPaymentPreference(PreferenceRequest request);
}