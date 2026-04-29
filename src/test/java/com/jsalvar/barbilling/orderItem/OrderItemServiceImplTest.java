package com.jsalvar.barbilling.orderItem;

import com.jsalvar.barbilling.dto.request.OrderItemCreateRequestDto;
import com.jsalvar.barbilling.entity.OrderItem;
import com.jsalvar.barbilling.entity.Product;
import com.jsalvar.barbilling.entity.Stock;
import com.jsalvar.barbilling.entity.Tab;
import com.jsalvar.barbilling.entity.enums.TabStatus;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.OrderItemRepository;
import com.jsalvar.barbilling.service.OrderItemService;
import com.jsalvar.barbilling.service.ProductService;
import com.jsalvar.barbilling.service.StockService;
import com.jsalvar.barbilling.service.TabService;
import com.jsalvar.barbilling.service.impl.OrderItemServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private TabService tabService;

    @Mock
    private ProductService productService;

    @Mock
    private StockService stockService;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private Product testProduct;
    private Tab testTab;
    private Stock testStock;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id("product-id-123")
                .name("Margarita")
                .price(new BigDecimal("12.50"))
                .available(true)
                .active(true)
                .build();

        testTab = Tab.builder()
                .id("tab-id-123")
                .status(TabStatus.OPEN)
                .build();

        testStock = Stock.builder()
                .id("stock-id-123")
                .product(testProduct)
                .quantity(10)
                .lowStockThreshold(5)
                .build();

        testOrderItem = OrderItem.builder()
                .id("order-item-id-123")
                .product(testProduct)
                .tab(testTab)
                .quantity(2)
                .unitPrice(new BigDecimal("12.50"))
                .notes("No olives")
                .build();
    }

    @Test
    void findByTabId_Success() {
        when(tabService.findById("tab-id-123")).thenReturn(testTab);
        when(orderItemRepository.findByTabId("tab-id-123")).thenReturn(List.of(testOrderItem));

        List<OrderItem> result = orderItemService.findByTabId("tab-id-123");

        assertEquals(1, result.size());
        assertEquals("order-item-id-123", result.get(0).getId());
    }

    @Test
    void findByTabId_TabNotFound() {
        when(tabService.findById("unknown-tab")).thenThrow(
                new ResourceNotFoundException("Tab not found"));

        assertThrows(ResourceNotFoundException.class, () -> orderItemService.findByTabId("unknown-tab"));
    }

    @Test
    void create_Success() {
        OrderItemCreateRequestDto dto = new OrderItemCreateRequestDto(
                2, "No olives", "product-id-123", "tab-id-123");

        when(tabService.findById("tab-id-123")).thenReturn(testTab);
        when(productService.findById("product-id-123")).thenReturn(testProduct);
        when(stockService.findByProductId("product-id-123")).thenReturn(testStock);
        when(stockService.decrementStock(eq("product-id-123"), anyInt())).thenReturn(testStock);
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> {
            OrderItem item = invocation.getArgument(0);
            item.setId("new-order-item-id");
            return item;
        });

        OrderItem result = orderItemService.create(dto);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
        verify(stockService).decrementStock("product-id-123", 2);
    }

    @Test
    void create_TabClosed() {
        testTab.setStatus(TabStatus.CLOSED);
        OrderItemCreateRequestDto dto = new OrderItemCreateRequestDto(
                2, null, "product-id-123", "tab-id-123");

        when(tabService.findById("tab-id-123")).thenReturn(testTab);

        assertThrows(UnprocessableEntityException.class, () -> orderItemService.create(dto));
    }

    @Test
    void create_ProductInactive() {
        testProduct.setActive(false);
        OrderItemCreateRequestDto dto = new OrderItemCreateRequestDto(
                2, null, "product-id-123", "tab-id-123");

        when(tabService.findById("tab-id-123")).thenReturn(testTab);
        when(productService.findById("product-id-123")).thenReturn(testProduct);

        assertThrows(UnprocessableEntityException.class, () -> orderItemService.create(dto));
    }

    @Test
    void create_InsufficientStock() {
        testStock.setQuantity(1);
        OrderItemCreateRequestDto dto = new OrderItemCreateRequestDto(
                5, null, "product-id-123", "tab-id-123");

        when(tabService.findById("tab-id-123")).thenReturn(testTab);
        when(productService.findById("product-id-123")).thenReturn(testProduct);
        when(stockService.findByProductId("product-id-123")).thenReturn(testStock);

        UnprocessableEntityException exception = assertThrows(UnprocessableEntityException.class,
                () -> orderItemService.create(dto));
        assertTrue(exception.getMessage().contains("Not enough stock"));
    }

    @Test
    void delete_Success() {
        testTab.setStatus(TabStatus.OPEN);
        when(orderItemRepository.findById("order-item-id-123")).thenReturn(Optional.of(testOrderItem));
        when(stockService.refillStock(eq("product-id-123"), anyInt())).thenReturn(testStock);
        doNothing().when(orderItemRepository).delete(testOrderItem);

        orderItemService.delete("order-item-id-123");

        verify(stockService).refillStock("product-id-123", 2);
        verify(orderItemRepository).delete(testOrderItem);
    }

    @Test
    void delete_TabClosed() {
        testTab.setStatus(TabStatus.CLOSED);
        when(orderItemRepository.findById("order-item-id-123")).thenReturn(Optional.of(testOrderItem));

        assertThrows(UnprocessableEntityException.class, () -> orderItemService.delete("order-item-id-123"));
    }

    @Test
    void delete_NotFound() {
        when(orderItemRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderItemService.delete("unknown-id"));
    }

    @Test
    void findById_Success() {
        when(orderItemRepository.findById("order-item-id-123")).thenReturn(Optional.of(testOrderItem));

        OrderItem result = orderItemService.findById("order-item-id-123");

        assertNotNull(result);
        assertEquals("order-item-id-123", result.getId());
    }

    @Test
    void findById_NotFound() {
        when(orderItemRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderItemService.findById("unknown-id"));
    }
}