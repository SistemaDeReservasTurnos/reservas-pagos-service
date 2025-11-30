package com.servicio.reservas.pago.application.services;

import com.servicio.reservas.pago.application.dto.*;
import com.servicio.reservas.pago.domain.entities.Payment;
import com.servicio.reservas.pago.domain.entities.PaymentStatus;
import com.servicio.reservas.pago.domain.repository.IPaymentRepository;
import com.servicio.reservas.pago.domain.services.VoucherPdfGeneratorService;
import com.servicio.reservas.pago.infraestructure.client.IGatewayPaymentPort;
import com.servicio.reservas.pago.infraestructure.client.IReservationClient;
import com.servicio.reservas.pago.infraestructure.client.ReservationDTO;
import com.servicio.reservas.pago.infraestructure.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentServiceTest {

    @Mock
    private IPaymentRepository paymentRepository;
    @Mock
    private PaymentDtoMapper paymentDtoMapper;
    @Mock
    private IReservationClient reservationClient;
    @Mock
    private IGatewayPaymentPort gatewayPaymentPort;
    @Mock
    private VoucherPdfGeneratorService voucherPdfGeneratorService;

    @InjectMocks
    private PaymentService paymentService;

    // Test Data
    private final Long RESERVATION_ID = 101L;
    private final Long PAYMENT_ID = 1L;
    private final Double AMOUNT = 250.00;
    private final String AUDIT_USER = "TEST_USER";
    private final Long SERVICE_ID = 50L;
    private final Long USER_ID = 20L;

    private PaymentRequest validRequest;
    private ReservationDTO reservationDTO;
    private Payment savedPayment;
    private PreferenceResponse preferenceResponse;
    private PaymentResponse expectedResponse;

    @BeforeEach
    void setUp() {
        // Mocking @Value injection
        ReflectionTestUtils.setField(paymentService, "successUrl", "http://success");
        ReflectionTestUtils.setField(paymentService, "pendingUrl", "http://pending");
        ReflectionTestUtils.setField(paymentService, "failureUrl", "http://failure");

        validRequest = new PaymentRequest();
        validRequest.setReservationId(RESERVATION_ID);
        validRequest.setAmount(AMOUNT);

        // Correct ReservationDTO constructor signature: (id, serviceId, userId, amount)
        reservationDTO = new ReservationDTO(RESERVATION_ID, SERVICE_ID, USER_ID, AMOUNT);

        // Payment Entity setup
        savedPayment = Payment.builder()
                .id(PAYMENT_ID)
                .reservationId(RESERVATION_ID)
                .amount(AMOUNT)
                .status(PaymentStatus.PENDING)
                .externalPaymentId("MP_ID_123")
                .paymentLink("http://link_mp")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("RESERVAS_SERVICE")
                .build();

        preferenceResponse = new PreferenceResponse("MP_ID_123", "http://link_mp");

        // PaymentResponse setup using Builder to match the DTO fields.
        expectedResponse = PaymentResponse.builder()
                .id(PAYMENT_ID) // Uses 'id' field as defined in PaymentResponse DTO
                .reservationId(RESERVATION_ID)
                .externalPaymentId("MP_ID_123")
                .amount(AMOUNT)
                .status(PaymentStatus.PENDING)
                .paymentLink("http://link_mp")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Stubbing the mapper to return the expected DTO
        when(paymentDtoMapper.toResponse(any(Payment.class))).thenReturn(expectedResponse);
    }

    // --- 1. TESTS FOR createPayment ---

    @Test
    void createPayment_SuccessPath_ReturnsPaymentResponseWithLink() {
        // ARRANGE
        when(reservationClient.findReservationById(RESERVATION_ID)).thenReturn(Optional.of(reservationDTO));

        // First save (no ID): Added null check to prevent NullPointerException with argThat
        when(paymentRepository.save(argThat(p -> p != null && p.getId() == null))).thenReturn(savedPayment);

        when(gatewayPaymentPort.createPaymentPreference(any(PreferenceRequest.class))).thenReturn(Optional.of(preferenceResponse));

        // Second save (with external ID): Added null check to prevent NullPointerException with argThat
        when(paymentRepository.save(argThat(p -> p != null && p.getExternalPaymentId() != null))).thenReturn(savedPayment);


        // ACT
        PaymentResponse result = paymentService.createPayment(validRequest);

        // ASSERT
        assertNotNull(result, "Response must not be null.");
        assertEquals("http://link_mp", result.getPaymentLink(), "Should return the generated payment link.");

        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(gatewayPaymentPort, times(1)).createPaymentPreference(any(PreferenceRequest.class));
    }

    @Test
    void createPayment_ReservationNotFound_ThrowsReservationNotFoundException() {
        // ARRANGE
        when(reservationClient.findReservationById(RESERVATION_ID)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ReservationNotFoundException.class, () ->
                paymentService.createPayment(validRequest)
        );

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(gatewayPaymentPort, never()).createPaymentPreference(any(PreferenceRequest.class));
    }

    @Test
    void createPayment_GatewayFails_ThrowsExternalPaymentGatewayException() {
        // ARRANGE
        when(reservationClient.findReservationById(RESERVATION_ID)).thenReturn(Optional.of(reservationDTO));

        // First save successful
        when(paymentRepository.save(argThat(p -> p != null && p.getId() == null))).thenReturn(savedPayment);

        // Simulate gateway returning empty
        when(gatewayPaymentPort.createPaymentPreference(any(PreferenceRequest.class))).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(ExternalPaymentGatewayException.class, () ->
                paymentService.createPayment(validRequest)
        );

        // Should call save only once (before the gateway call)
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(gatewayPaymentPort, times(1)).createPaymentPreference(any(PreferenceRequest.class));
    }

    // --- 2. TESTS FOR getPaymentById ---

    @Test
    void getPaymentById_PaymentFound_ReturnsOptionalPaymentResponse() {
        // ARRANGE
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(savedPayment));

        // ACT
        Optional<PaymentResponse> result = paymentService.getPaymentById(PAYMENT_ID);

        // ASSERT
        assertTrue(result.isPresent());
        // Uses getId() as the DTO field is named 'id'
        assertEquals(PAYMENT_ID, result.get().getId());
        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test
    void getPaymentById_PaymentNotFound_ReturnsOptionalEmpty() {
        // ARRANGE
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT
        Optional<PaymentResponse> result = paymentService.getPaymentById(99L);

        // ASSERT
        assertTrue(result.isEmpty());
        verify(paymentRepository, times(1)).findById(99L);
    }

    // --- 3. TESTS FOR findAllPayments ---

    @Test
    void findAllPayments_ReturnsListOfPaymentResponses() {
        // ARRANGE
        List<Payment> paymentList = Collections.singletonList(savedPayment);
        when(paymentRepository.findAll()).thenReturn(paymentList);

        // ACT
        List<PaymentResponse> result = paymentService.findAllPayments();

        // ASSERT
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(paymentRepository, times(1)).findAll();
        verify(paymentDtoMapper, times(1)).toResponse(any(Payment.class));
    }

    @Test
    void findAllPayments_NoPayments_ReturnsEmptyList() {
        // ARRANGE
        when(paymentRepository.findAll()).thenReturn(Collections.emptyList());

        // ACT
        List<PaymentResponse> result = paymentService.findAllPayments();

        // ASSERT
        assertTrue(result.isEmpty());
        verify(paymentRepository, times(1)).findAll();
        verify(paymentDtoMapper, never()).toResponse(any(Payment.class));
    }

    // --- 4. TESTS FOR updatePaymentStatus ---

    @Test
    void updatePaymentStatus_ValidStatus_UpdatesAndSavesPayment() {
        // ARRANGE
        String newStatus = "APPROVED";
        Payment paymentToUpdate = Payment.builder()
                .id(PAYMENT_ID)
                .reservationId(RESERVATION_ID)
                .amount(AMOUNT)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(paymentToUpdate));
        when(paymentRepository.save(any(Payment.class))).thenReturn(paymentToUpdate);

        // ACT
        Payment result = paymentService.updatePaymentStatus(PAYMENT_ID, newStatus, AUDIT_USER);

        // ASSERT
        assertEquals(PaymentStatus.APPROVED, result.getStatus());
        assertEquals(AUDIT_USER, result.getUpdatedBy());
        verify(paymentRepository, times(1)).save(result);
    }

    @Test
    void updatePaymentStatus_PaymentNotFound_ThrowsPaymentNotFoundException() {
        // ARRANGE
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(PaymentNotFoundException.class, () ->
                paymentService.updatePaymentStatus(PAYMENT_ID, "APPROVED", AUDIT_USER)
        );
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void updatePaymentStatus_InvalidStatus_ThrowsInvalidPaymentStatusException() {
        // ARRANGE
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(savedPayment));

        // ACT & ASSERT
        assertThrows(InvalidPaymentStatusException.class, () ->
                paymentService.updatePaymentStatus(PAYMENT_ID, "INVALID_STATUS", AUDIT_USER)
        );
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    // --- 5. TESTS FOR generatePaymentVoucher ---

    @Test
    void generatePaymentVoucher_PaymentApproved_ReturnsPdfBytes() {
        // ARRANGE
        Payment approvedPayment = Payment.builder()
                .id(PAYMENT_ID)
                .status(PaymentStatus.APPROVED)
                .build();
        byte[] pdfBytes = "PDF_CONTENT".getBytes();

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(approvedPayment));
        when(voucherPdfGeneratorService.generatePdf(approvedPayment)).thenReturn(pdfBytes);

        // ACT
        byte[] result = paymentService.generatePaymentVoucher(PAYMENT_ID);

        // ASSERT
        assertNotNull(result);
        assertArrayEquals(pdfBytes, result);
        verify(voucherPdfGeneratorService, times(1)).generatePdf(approvedPayment);
    }

    @Test
    void generatePaymentVoucher_PaymentNotFound_ThrowsPaymentNotFoundException() {
        // ARRANGE
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThrows(PaymentNotFoundException.class, () ->
                paymentService.generatePaymentVoucher(PAYMENT_ID)
        );
        verify(voucherPdfGeneratorService, never()).generatePdf(any(Payment.class));
    }

    @Test
    void generatePaymentVoucher_PaymentPending_ThrowsVoucherGenerationException() {
        // ARRANGE
        Payment pendingPayment = Payment.builder()
                .id(PAYMENT_ID)
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(pendingPayment));

        // ACT & ASSERT
        assertThrows(VoucherGenerationException.class, () ->
                paymentService.generatePaymentVoucher(PAYMENT_ID)
        );
        verify(voucherPdfGeneratorService, never()).generatePdf(any(Payment.class));
    }
}