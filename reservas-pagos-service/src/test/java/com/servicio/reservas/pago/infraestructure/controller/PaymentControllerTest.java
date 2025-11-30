package com.servicio.reservas.pago.infraestructure.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicio.reservas.pago.application.dto.PaymentRequest;
import com.servicio.reservas.pago.application.dto.PaymentResponse;
import com.servicio.reservas.pago.application.services.IPaymentService;
import com.servicio.reservas.pago.domain.entities.PaymentStatus;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock the Application Service layer dependency
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

        // Simulate service throwing the controlled exception
        doThrow(new VoucherGenerationException("Error generating PDF")).when(ipaymentservice)
                .generatePaymentVoucher(paymentId);

        // Assumes a GlobalExceptionHandler maps VoucherGenerationException to 422
        mockMvc.perform(get(BASE_URL + "/{id}/voucher", paymentId))
                .andExpect(status().isUnprocessableEntity()) // 422 UNPROCESSABLE ENTITY
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }
}