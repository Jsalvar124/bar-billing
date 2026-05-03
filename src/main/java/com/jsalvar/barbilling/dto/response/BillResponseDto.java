package com.jsalvar.barbilling.dto.response;

import com.jsalvar.barbilling.entity.enums.BillStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BillResponseDto(
        String id,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal tip,
        BigDecimal total,
        BillStatus billStatus,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt,
        String cancellationReason,
        TabInfo tab,
        CashierInfo cashier
) {
    public record TabInfo(
            String id,
            String tableId,
            String tableName,
            String waiterName,
            String tabStatus
    ) {}

    public record CashierInfo(
            String id,
            String name,
            String lastname
    ) {}
}