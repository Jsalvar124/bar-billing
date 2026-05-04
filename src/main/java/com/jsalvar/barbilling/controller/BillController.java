package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.BillCancelRequestDto;
import com.jsalvar.barbilling.dto.request.BillCreateRequestDto;
import com.jsalvar.barbilling.dto.response.BillResponseDto;
import com.jsalvar.barbilling.entity.Bill;
import com.jsalvar.barbilling.entity.Tab;
import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.entity.BarTable;
import com.jsalvar.barbilling.entity.enums.BillStatus;
import com.jsalvar.barbilling.entity.enums.TabStatus;
import com.jsalvar.barbilling.service.BillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bills")
public class BillController {
    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @Loggable
    @PostMapping
    @PreAuthorize("hasRole('CASHIER') or hasRole('ADMIN')")
    public ResponseEntity<BillResponseDto> create(@RequestBody @Valid BillCreateRequestDto dto) {
        Bill bill = billService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(bill));
    }

    @Loggable
    @GetMapping
    public ResponseEntity<List<BillResponseDto>> findAll() {
        List<BillResponseDto> bills = billService.findAll().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(bills);
    }

    @Loggable
    @GetMapping("/{id}")
    public ResponseEntity<BillResponseDto> findById(@PathVariable String id) {
        Bill bill = billService.findById(id);
        return ResponseEntity.ok(toDto(bill));
    }

    @Loggable
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CASHIER') or hasRole('ADMIN')")
    public ResponseEntity<BillResponseDto> cancel(
            @PathVariable String id,
            @RequestBody @Valid BillCancelRequestDto dto) {
        Bill bill = billService.cancel(new BillCancelRequestDto(id, dto.cancellationReason()));
        return ResponseEntity.ok(toDto(bill));
    }

    private BillResponseDto toDto(Bill bill) {
        Tab tab = bill.getTab();
        BarTable table = tab.getTable();
        UserImpl waiter = tab.getWaiter();
        UserImpl cashier = bill.getCashier();

        List<BillResponseDto.Item> items = bill.getItems().stream()
                .map(item -> new BillResponseDto.Item(
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal(),
                        item.getTax(),
                        item.getTotal(),
                        bill.getCurrency()
                ))
                .toList();

        return new BillResponseDto(
                bill.getId(),
                bill.getSubtotal(),
                bill.getTax(),
                bill.getTip(),
                bill.getTotal(),
                bill.getCurrency(),
                bill.getBillStatus(),
                bill.getPaidAt(),
                bill.getCancelledAt(),
                bill.getCancellationReason(),
                new BillResponseDto.TabInfo(
                        tab.getId(),
                        table.getId(),
                        table.getNumber(),
                        waiter.getName() + " " + waiter.getLastname(),
                        tab.getStatus().name()
                ),
                new BillResponseDto.CashierInfo(
                        cashier.getId(),
                        cashier.getName(),
                        cashier.getLastname()
                ),
                items
        );
    }
}