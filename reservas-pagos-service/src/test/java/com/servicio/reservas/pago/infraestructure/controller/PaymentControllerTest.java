package com.servicio.reservas.pago.infraestructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicio.reservas.pago.application.dto.PaymentRequest;
import com.servicio.reservas.pago.application.dto.PaymentResponse;
import com.servicio.reservas.pago.application.services.IPaymentService;
import com.servicio.reservas.pago.domain.entities.PaymentStatus;
import com.servicio.reservas.pago.infraestructure.exception.InvalidPaymentStatusException;
import com.servicio.reservas.pago.infraestructure.exception.PaymentNotFoundException;
import com.servicio.reservas.pago.infraestructure.exception.VoucherGenerationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de Integración para PaymentController.
 * Se enfoca en las interacciones HTTP, validación de DTOs y manejo de excepciones.
 */
@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Simulación del servicio de la capa de aplicación
    @MockBean
    private IPaymentService ipaymentservice;

    private final String BASE_URL = "/api/payments";

    // --- UTILS ---

    private PaymentResponse createMockPaymentResponse(Long id, PaymentStatus status) {
        return PaymentResponse.builder()
                .id(id)
                .reservationId(100L)
                .amount(99.99)
                .status(status)
                .paymentLink("http://mercadopago.com/link-test/" + id)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // =========================================================================
    //                            1. GET /
    // =========================================================================

    @Test
    void listAllPayments_shouldReturn200OkWithPaymentsList() throws Exception {
        PaymentResponse p1 = createMockPaymentResponse(1L, PaymentStatus.APPROVED);
        PaymentResponse p2 = createMockPaymentResponse(2L, PaymentStatus.PENDING);

        when(ipaymentservice.findAllPayments()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(ipaymentservice, times(1)).findAllPayments();
    }

    // =========================================================================
    //                            2. POST /create
    // =========================================================================

    @Test
    void createPayment_withValidRequest_shouldReturn201Created() throws Exception {
        Long reservationId = 1L;
        PaymentRequest validRequest = new PaymentRequest();
        validRequest.setReservationId(reservationId);
        validRequest.setAmount(100.50);
        validRequest.setClientEmail("test@example.com");

        PaymentResponse mockResponse = createMockPaymentResponse(1L, PaymentStatus.PENDING);

        when(ipaymentservice.createPayment(any(PaymentRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post(BASE_URL + "/create")
                        .content(objectMapper.writeValueAsString(validRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated()) // 201 CREATED
                .andExpect(jsonPath("$.id").value(1L));

        verify(ipaymentservice, times(1)).createPayment(any(PaymentRequest.class));
    }

    // NOTA: Para probar las anotaciones @NotNull y @Positive en el @RequestParam (que es ahora un @RequestBody PaymentRequest),
    // se requeriría añadir @Valid al argumento del controller y el handler de MethodArgumentNotValidException,
    // pero simularemos el flujo de error de negocio si la validación falla internamente o en la capa de servicio.

    // =========================================================================
    //                            3. GET /{id}
    // =========================================================================

    @Test
    void getPaymentStatus_withExistingId_shouldReturn200Ok() throws Exception {
        Long existingId = 1L;
        PaymentResponse mockResponse = createMockPaymentResponse(existingId, PaymentStatus.APPROVED);

        when(ipaymentservice.getPaymentById(existingId)).thenReturn(Optional.of(mockResponse));

        mockMvc.perform(get(BASE_URL + "/{id}", existingId))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(jsonPath("$.id").value(existingId));
    }

    @Test
    void getPaymentStatus_withNonExistingId_shouldReturn404NotFound() throws Exception {
        Long nonExistingId = 99L;

        when(ipaymentservice.getPaymentById(nonExistingId)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL + "/{id}", nonExistingId))
                .andExpect(status().isNotFound()); // 404 NOT FOUND
    }

    // =========================================================================
    //                            4. GET /{id}/voucher
    // =========================================================================

    @Test
    void generateVoucher_withValidId_shouldReturn200OkAndPdf() throws Exception {
        Long paymentId = 1L;
        byte[] mockPdfContent = "PDF TEST CONTENT".getBytes();

        when(ipaymentservice.generatePaymentVoucher(paymentId)).thenReturn(mockPdfContent);

        mockMvc.perform(get(BASE_URL + "/{id}/voucher", paymentId))
                .andExpect(status().isOk()) // 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"payment_voucher_1.pdf\""))
                .andExpect(content().bytes(mockPdfContent));

        verify(ipaymentservice, times(1)).generatePaymentVoucher(paymentId);
    }

    @Test
    void generateVoucher_whenGenerationFails_shouldReturn422UnprocessableEntity() throws Exception {
        Long paymentId = 1L;

        // Simular que el servicio lanza la excepción controlada para la generación
        doThrow(new VoucherGenerationException("Error al generar PDF")).when(ipaymentservice)
                .generatePaymentVoucher(paymentId);

        // El GlobalExceptionHandler debe capturar VoucherGenerationException y devolver 422
        mockMvc.perform(get(BASE_URL + "/{id}/voucher", paymentId))
                .andExpect(status().isUnprocessableEntity()) // 422 UNPROCESSABLE ENTITY
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }
}