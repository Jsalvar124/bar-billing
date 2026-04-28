package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.response.StockResponseDto;
import com.jsalvar.barbilling.entity.Stock;
import com.jsalvar.barbilling.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("stocks")
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

    private StockResponseDto toDto(Stock stock){
        return new StockResponseDto(
                stock.getProduct().getId(),
                stock.getProduct().getName(),
                stock.getQuantity(),
                stock.getLowStockThreshold()
        );
    }


}
