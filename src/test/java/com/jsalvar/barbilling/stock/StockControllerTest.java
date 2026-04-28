package com.jsalvar.barbilling.stock;

import com.jsalvar.barbilling.controller.StockController;
import com.jsalvar.barbilling.dto.request.StockRefillRequestDto;
import com.jsalvar.barbilling.dto.request.StockThresholdRequestDto;
import com.jsalvar.barbilling.dto.response.StockResponseDto;
import com.jsalvar.barbilling.entity.Product;
import com.jsalvar.barbilling.entity.Stock;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockControllerTest {

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockController stockController;

    private Product testProduct;
    private Stock testStock;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id("product-id-123")
                .name("Margarita")
                .build();

        testStock = Stock.builder()
                .id("stock-id-123")
                .product(testProduct)
                .quantity(5)
                .lowStockThreshold(10)
                .build();
    }

    private StockResponseDto toDto(Stock stock) {
        return new StockResponseDto(
                stock.getProduct().getId(),
                stock.getProduct().getName(),
                stock.getQuantity(),
                stock.getLowStockThreshold()
        );
    }

    @Test
    void findByProductId_Success_Returns200() {
        when(stockService.findByProductId("product-id-123")).thenReturn(testStock);

        ResponseEntity<StockResponseDto> response = stockController.findByProductId("product-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("product-id-123", response.getBody().productId());
    }

    @Test
    void findByProductId_NotFound_ThrowsException() {
        when(stockService.findByProductId("unknown-id")).thenThrow(
                new ResourceNotFoundException("Product not found"));

        assertThrows(ResourceNotFoundException.class, () -> stockController.findByProductId("unknown-id"));
    }

    @Test
    void findLowStock_Returns200() {
        when(stockService.findLowStock()).thenReturn(List.of(testStock));

        ResponseEntity<List<StockResponseDto>> response = stockController.findLowStock();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void updateLowStockThreshold_Success_Returns200() {
        StockThresholdRequestDto dto = new StockThresholdRequestDto(20);
        when(stockService.updateThreshold(eq("product-id-123"), anyInt())).thenReturn(testStock);

        ResponseEntity<StockResponseDto> response = stockController.updateLowStockThreshold("product-id-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateLowStockThreshold_Invalid_ThrowsException() {
        StockThresholdRequestDto dto = new StockThresholdRequestDto(0);
        when(stockService.updateThreshold(eq("product-id-123"), anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid threshold"));

        assertThrows(IllegalArgumentException.class,
                () -> stockController.updateLowStockThreshold("product-id-123", dto));
    }

    @Test
    void refillStock_Success_Returns200() {
        StockRefillRequestDto dto = new StockRefillRequestDto(50);
        when(stockService.refillStock(eq("product-id-123"), anyInt())).thenReturn(testStock);

        ResponseEntity<StockResponseDto> response = stockController.refillStock("product-id-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void refillStock_ExceedsMax_ThrowsException() {
        StockRefillRequestDto dto = new StockRefillRequestDto(150);
        when(stockService.refillStock(eq("product-id-123"), anyInt()))
                .thenThrow(new IllegalArgumentException("Exceeds max"));

        assertThrows(IllegalArgumentException.class,
                () -> stockController.refillStock("product-id-123", dto));
    }
}