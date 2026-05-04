package com.jsalvar.barbilling.dto.response;

import com.jsalvar.barbilling.entity.enums.BillStatus;
import com.jsalvar.barbilling.entity.enums.PaymentMethod;
import com.jsalvar.barbilling.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDto(
        String id,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String confirmationToken,
        String failureReason,
        LocalDateTime attemptedAt,
        LocalDateTime resolvedAt,
        BillInfo bill
) {
    public record BillInfo(
            String id,
            BigDecimal total,
            BillStatus billStatus
    ) {}
}