package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.StockRefillRequestDto;
import com.jsalvar.barbilling.dto.request.StockThresholdRequestDto;
import com.jsalvar.barbilling.dto.response.StockResponseDto;
import com.jsalvar.barbilling.entity.Stock;
import com.jsalvar.barbilling.service.StockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stocks")
public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @Loggable
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockResponseDto> findByProductId(@PathVariable String productId) {
        return ResponseEntity.ok(toDto(stockService.findByProductId(productId)));
    }

    @Loggable
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StockResponseDto>> findLowStock() {
        List<StockResponseDto> lowStocks = stockService.findLowStock().stream().map(this::toDto).toList();
        return ResponseEntity.ok(lowStocks);
    }

    @Loggable
    @PatchMapping("/product/{productId}/low-threshold")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockResponseDto> updateLowStockThreshold(
            @PathVariable String productId,
            @RequestBody @Valid StockThresholdRequestDto dto) {
        Stock stock = stockService.updateThreshold(productId, dto.threshold());
        return ResponseEntity.ok(toDto(stock));
    }

    @Loggable
    @PatchMapping("/product/{productId}/refill")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockResponseDto> refillStock(
            @PathVariable String productId,
            @RequestBody @Valid StockRefillRequestDto dto) {
        Stock stock = stockService.refillStock(productId, dto.quantity());
        return ResponseEntity.ok(toDto(stock));
    }


    private StockResponseDto toDto(Stock stock){
        return new StockResponseDto(
                stock.getProduct().getId(),
                stock.getProduct().getName(),
                stock.getQuantity(),
                stock.getLowStockThreshold()
        );
    }
}
