package com.jsalvar.barbilling.dto.response;

import com.jsalvar.barbilling.entity.enums.BillStatus;
import com.jsalvar.barbilling.entity.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BillResponseDto(
        String id,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal tip,
        BigDecimal total,
        Currency currency,
        BillStatus billStatus,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt,
        String cancellationReason,
        TabInfo tab,
        CashierInfo cashier,
        List<Item> items
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

    public record Item(
            String productName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal total,
            Currency currency
    ) {}
}