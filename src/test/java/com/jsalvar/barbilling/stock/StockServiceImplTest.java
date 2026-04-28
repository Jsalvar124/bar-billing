package com.jsalvar.barbilling.stock;

import com.jsalvar.barbilling.entity.Product;
import com.jsalvar.barbilling.entity.Stock;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.StockRepository;
import com.jsalvar.barbilling.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    private Product testProduct;
    private Stock testStock;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stockService, "maxRefillQuantity", 100);
        ReflectionTestUtils.setField(stockService, "maxLowStockThreshold", 100);

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

    @Test
    void findLowStock_Success() {
        when(stockRepository.findLowStock()).thenReturn(List.of(testStock));

        List<Stock> result = stockService.findLowStock();

        assertEquals(1, result.size());
        assertTrue(result.get(0).getQuantity() < result.get(0).getLowStockThreshold());
    }

    @Test
    void findByProductId_Success() {
        when(stockRepository.findByProductId("product-id-123")).thenReturn(Optional.of(testStock));

        Stock result = stockService.findByProductId("product-id-123");

        assertNotNull(result);
        assertEquals("product-id-123", result.getProduct().getId());
    }

    @Test
    void findByProductId_NotFound() {
        when(stockRepository.findByProductId("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> stockService.findByProductId("unknown-id"));
    }

    @Test
    void updateThreshold_Success() {
        when(stockRepository.findByProductId("product-id-123")).thenReturn(Optional.of(testStock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        Stock result = stockService.updateThreshold("product-id-123", 20);

        assertEquals(20, result.getLowStockThreshold());
    }

    @Test
    void updateThreshold_Invalid_Zero() {
        when(stockRepository.findByProductId("product-id-123")).thenReturn(Optional.of(testStock));

        assertThrows(IllegalArgumentException.class, () -> stockService.updateThreshold("product-id-123", 0));
    }

    @Test
    void updateThreshold_Invalid_Negative() {
        when(stockRepository.findByProductId("product-id-123")).thenReturn(Optional.of(testStock));

        assertThrows(IllegalArgumentException.class, () -> stockService.updateThreshold("product-id-123", -5));
    }

    @Test
    void updateThreshold_Invalid_ExceedsMax() {
        when(stockRepository.findByProductId("product-id-123")).thenReturn(Optional.of(testStock));

        assertThrows(IllegalArgumentException.class, () -> stockService.updateThreshold("product-id-123", 150));
    }

    @Test
    void initializeStock_Success() {
        when(stockRepository.save(any(Stock.class))).thenAnswer(invocation -> {
            Stock stock = invocation.getArgument(0);
            stock.setId("new-stock-id");
            return stock;
        });

        Stock result = stockService.initializeStock(testProduct);

        assertNotNull(result);
        assertEquals(0, result.getQuantity());
        assertEquals(10, result.getLowStockThreshold());
    }

    @Test
    void decrementStock_Success() {
        when(stockRepository.findByProductId("product-id-123")).thenReturn(Optional.of(testStock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        Stock result = stockService.decrementStock("product-id-123", 3);

        assertEquals(2, result.getQuantity());
    }

    @Test
    void decrementStock_ExceedsAvailable() {
        testStock.setQuantity(5);
        when(stockRepository.findByProductId("product-id-123")).thenReturn(Optional.of(testStock));

        assertThrows(UnprocessableEntityException.class, () -> stockService.decrementStock("product-id-123", 10));
    }

    @Test
    void decrementStock_NegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> stockService.decrementStock("product-id-123", -1));
    }

    @Test
    void refillStock_Success() {
        when(stockRepository.findByProductId("product-id-123")).thenReturn(Optional.of(testStock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        Stock result = stockService.refillStock("product-id-123", 50);

        assertEquals(55, result.getQuantity());
    }

    @Test
    void refillStock_ExceedsMax() {
        assertThrows(IllegalArgumentException.class, () -> stockService.refillStock("product-id-123", 150));
    }

    @Test
    void refillStock_NegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> stockService.refillStock("product-id-123", -1));
    }
}