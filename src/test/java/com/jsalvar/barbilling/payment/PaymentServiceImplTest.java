package com.jsalvar.barbilling.payment;

import com.jsalvar.barbilling.dto.request.PaymentApproveRequestDto;
import com.jsalvar.barbilling.dto.request.PaymentAttemptRequestDto;
import com.jsalvar.barbilling.entity.Bill;
import com.jsalvar.barbilling.entity.Payment;
import com.jsalvar.barbilling.entity.enums.BillStatus;
import com.jsalvar.barbilling.entity.enums.PaymentMethod;
import com.jsalvar.barbilling.entity.enums.PaymentStatus;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.PaymentRepository;
import com.jsalvar.barbilling.service.BillService;
import com.jsalvar.barbilling.service.PaymentService;
import com.jsalvar.barbilling.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BillService billService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

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

    @Test
    void findById_Success() {
        when(paymentRepository.findById("payment-id-123")).thenReturn(Optional.of(testPayment));

        Payment result = paymentService.findById("payment-id-123");

        assertNotNull(result);
        assertEquals("payment-id-123", result.getId());
    }

    @Test
    void findById_NotFound() {
        when(paymentRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.findById("unknown-id"));
    }

    @Test
    void findAll_Success() {
        when(paymentRepository.findAll()).thenReturn(List.of(testPayment));

        List<Payment> result = paymentService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findByBillId_Success() {
        when(paymentRepository.findByBillId("bill-id-123")).thenReturn(List.of(testPayment));

        List<Payment> result = paymentService.findByBillId("bill-id-123");

        assertEquals(1, result.size());
        assertEquals("bill-id-123", result.get(0).getBill().getId());
    }

    @Test
    void attempt_Success() {
        PaymentAttemptRequestDto dto = new PaymentAttemptRequestDto(
                "bill-id-123", PaymentMethod.CASH, new BigDecimal("32.00"));

        when(billService.findById("bill-id-123")).thenReturn(testBill);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId("new-payment-id");
            return payment;
        });

        Payment result = paymentService.attempt(dto);

        assertNotNull(result);
        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());
        assertEquals(testBill, result.getBill());
    }

    @Test
    void attempt_AmountMismatch() {
        PaymentAttemptRequestDto dto = new PaymentAttemptRequestDto(
                "bill-id-123", PaymentMethod.CASH, new BigDecimal("50.00"));

        when(billService.findById("bill-id-123")).thenReturn(testBill);

        assertThrows(UnprocessableEntityException.class, () -> paymentService.attempt(dto));
    }

    @Test
    void attempt_BillNotPending() {
        testBill.setBillStatus(BillStatus.PAID);
        PaymentAttemptRequestDto dto = new PaymentAttemptRequestDto(
                "bill-id-123", PaymentMethod.CASH, new BigDecimal("32.00"));

        when(billService.findById("bill-id-123")).thenReturn(testBill);

        assertThrows(UnprocessableEntityException.class, () -> paymentService.attempt(dto));
    }

    @Test
    void approve_Success_Cash() {
        testPayment.setPaymentMethod(PaymentMethod.CASH);
        PaymentApproveRequestDto dto = new PaymentApproveRequestDto(null);

        when(paymentRepository.findById("payment-id-123")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(billService.confirmPayment(any(Payment.class))).thenReturn(testBill);

        Payment result = paymentService.approve("payment-id-123", dto);

        assertEquals(PaymentStatus.APPROVED, result.getPaymentStatus());
        assertNotNull(result.getResolvedAt());
        verify(billService).confirmPayment(testPayment);
    }

    @Test
    void approve_Success_Card() {
        testPayment.setPaymentMethod(PaymentMethod.CARD);
        PaymentApproveRequestDto dto = new PaymentApproveRequestDto("tok_test123");

        when(paymentRepository.findById("payment-id-123")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(billService.confirmPayment(any(Payment.class))).thenReturn(testBill);

        Payment result = paymentService.approve("payment-id-123", dto);

        assertEquals(PaymentStatus.APPROVED, result.getPaymentStatus());
        assertEquals("tok_test123", result.getConfirmationToken());
    }

    @Test
    void approve_MissingTokenForCard() {
        testPayment.setPaymentMethod(PaymentMethod.CARD);
        PaymentApproveRequestDto dto = new PaymentApproveRequestDto(null);

        when(paymentRepository.findById("payment-id-123")).thenReturn(Optional.of(testPayment));

        assertThrows(UnprocessableEntityException.class, () -> paymentService.approve("payment-id-123", dto));
    }

    @Test
    void approve_NotPending() {
        testPayment.setPaymentStatus(PaymentStatus.APPROVED);
        PaymentApproveRequestDto dto = new PaymentApproveRequestDto(null);

        when(paymentRepository.findById("payment-id-123")).thenReturn(Optional.of(testPayment));

        assertThrows(UnprocessableEntityException.class, () -> paymentService.approve("payment-id-123", dto));
    }

    @Test
    void approve_NotFound() {
        PaymentApproveRequestDto dto = new PaymentApproveRequestDto(null);

        when(paymentRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.approve("unknown-id", dto));
    }

    @Test
    void decline_Success() {
        when(paymentRepository.findById("payment-id-123")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.decline("payment-id-123", "Customer cancelled");

        assertEquals(PaymentStatus.DECLINED, result.getPaymentStatus());
        assertEquals("Customer cancelled", result.getFailureReason());
        assertNotNull(result.getResolvedAt());
    }

    @Test
    void decline_NotPending() {
        testPayment.setPaymentStatus(PaymentStatus.APPROVED);

        when(paymentRepository.findById("payment-id-123")).thenReturn(Optional.of(testPayment));

        assertThrows(UnprocessableEntityException.class, () -> paymentService.decline("payment-id-123", "Reason"));
    }

    @Test
    void decline_NotFound() {
        when(paymentRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.decline("unknown-id", "Reason"));
    }
}