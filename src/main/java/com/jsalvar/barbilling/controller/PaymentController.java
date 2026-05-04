package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.PaymentApproveRequestDto;
import com.jsalvar.barbilling.dto.request.PaymentAttemptRequestDto;
import com.jsalvar.barbilling.dto.request.PaymentDeclineRequestDto;
import com.jsalvar.barbilling.dto.response.PaymentResponseDto;
import com.jsalvar.barbilling.entity.Payment;
import com.jsalvar.barbilling.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Loggable
    @PostMapping
    @PreAuthorize("hasRole('CASHIER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponseDto> attempt(@RequestBody @Valid PaymentAttemptRequestDto dto) {
        Payment payment = paymentService.attempt(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(payment));
    }

    @Loggable
    @GetMapping
    @PreAuthorize("hasRole('CASHIER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponseDto>> findAll() {
        List<PaymentResponseDto> payments = paymentService.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(payments);
    }

    @Loggable
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CASHIER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponseDto> findById(@PathVariable String id) {
        Payment payment = paymentService.findById(id);
        return ResponseEntity.ok(toDto(payment));
    }

    @Loggable
    @GetMapping("/bill/{billId}")
    @PreAuthorize("hasRole('CASHIER') or hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponseDto>> findByBillId(@PathVariable String billId) {
        List<PaymentResponseDto> payments = paymentService.findByBillId(billId).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(payments);
    }

    @Loggable
    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('CASHIER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponseDto> approve(
            @PathVariable String id,
            @RequestBody @Valid PaymentApproveRequestDto dto) {
        Payment payment = paymentService.approve(id, dto);
        return ResponseEntity.ok(toDto(payment));
    }

    @Loggable
    @PatchMapping("/{id}/decline")
    @PreAuthorize("hasRole('CASHIER') or hasRole('ADMIN')")
    public ResponseEntity<PaymentResponseDto> decline(
            @PathVariable String id,
            @RequestBody PaymentDeclineRequestDto dto) {
        Payment payment = paymentService.decline(id, dto.reason());
        return ResponseEntity.ok(toDto(payment));
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
}