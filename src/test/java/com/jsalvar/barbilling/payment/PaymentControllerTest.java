package com.jsalvar.barbilling.payment;

import com.jsalvar.barbilling.controller.PaymentController;
import com.jsalvar.barbilling.dto.request.PaymentApproveRequestDto;
import com.jsalvar.barbilling.dto.request.PaymentAttemptRequestDto;
import com.jsalvar.barbilling.dto.request.PaymentDeclineRequestDto;
import com.jsalvar.barbilling.dto.response.PaymentResponseDto;
import com.jsalvar.barbilling.entity.Bill;
import com.jsalvar.barbilling.entity.Payment;
import com.jsalvar.barbilling.entity.enums.BillStatus;
import com.jsalvar.barbilling.entity.enums.PaymentMethod;
import com.jsalvar.barbilling.entity.enums.PaymentStatus;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private Bill testBill;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testBill = Bill.builder()
                .id("bill-id-123")
                .total(new BigDecimal("32.00"))
                .billStatus(BillStatus.PENDING)
                .build();

        testPayment = Payment.builder()
                .id("payment-id-123")
                .bill(testBill)
                .amount(new BigDecimal("32.00"))
                .paymentMethod(PaymentMethod.CASH)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }

    private PaymentResponseDto toDto(Payment payment) {
        return new PaymentResponseDto(
                payment.getId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getConfirmationToken(),
                payment.getFailureReason(),
                payment.getAttemptedAt(),
                payment.getResolvedAt(),
                new PaymentResponseDto.BillInfo(
                        payment.getBill().getId(),
                        payment.getBill().getTotal(),
                        payment.getBill().getBillStatus()
                )
        );
    }

    @Test
    void attempt_Success_Returns201() {
        PaymentAttemptRequestDto dto = new PaymentAttemptRequestDto(
                "bill-id-123", PaymentMethod.CASH, new BigDecimal("32.00"));

        when(paymentService.attempt(any())).thenReturn(testPayment);

        ResponseEntity<PaymentResponseDto> response = paymentController.attempt(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void attempt_Unprocessable_ThrowsException() {
        PaymentAttemptRequestDto dto = new PaymentAttemptRequestDto(
                "bill-id-123", PaymentMethod.CASH, new BigDecimal("32.00"));

        when(paymentService.attempt(any()))
                .thenThrow(new UnprocessableEntityException("Payment amount must match bill total"));

        assertThrows(UnprocessableEntityException.class,
                () -> paymentController.attempt(dto));
    }

    @Test
    void findAll_Returns200() {
        when(paymentService.findAll()).thenReturn(List.of(testPayment));

        ResponseEntity<List<PaymentResponseDto>> response = paymentController.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void findById_Success_Returns200() {
        when(paymentService.findById("payment-id-123")).thenReturn(testPayment);

        ResponseEntity<PaymentResponseDto> response = paymentController.findById("payment-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("payment-id-123", response.getBody().id());
    }

    @Test
    void findById_NotFound_ThrowsException() {
        when(paymentService.findById("unknown-id")).thenThrow(
                new ResourceNotFoundException("Payment not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> paymentController.findById("unknown-id"));
    }

    @Test
    void findByBillId_Returns200() {
        when(paymentService.findByBillId("bill-id-123")).thenReturn(List.of(testPayment));

        ResponseEntity<List<PaymentResponseDto>> response = paymentController.findByBillId("bill-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void approve_Success_Returns200() {
        PaymentApproveRequestDto dto = new PaymentApproveRequestDto(null);
        testPayment.setPaymentStatus(PaymentStatus.APPROVED);
        testPayment.setResolvedAt(LocalDateTime.now());

        when(paymentService.approve(any(), any())).thenReturn(testPayment);

        ResponseEntity<PaymentResponseDto> response = paymentController.approve("payment-id-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(PaymentStatus.APPROVED, response.getBody().paymentStatus());
    }

    @Test
    void decline_Success_Returns200() {
        PaymentDeclineRequestDto dto = new PaymentDeclineRequestDto("Customer cancelled");
        testPayment.setPaymentStatus(PaymentStatus.DECLINED);
        testPayment.setResolvedAt(LocalDateTime.now());

        when(paymentService.decline(any(), any())).thenReturn(testPayment);

        ResponseEntity<PaymentResponseDto> response = paymentController.decline("payment-id-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(PaymentStatus.DECLINED, response.getBody().paymentStatus());
    }
}