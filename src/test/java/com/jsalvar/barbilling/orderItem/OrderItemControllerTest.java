package com.jsalvar.barbilling.orderItem;

import com.jsalvar.barbilling.controller.OrderItemController;
import com.jsalvar.barbilling.dto.request.OrderItemCreateRequestDto;
import com.jsalvar.barbilling.dto.response.OrderItemResponseDto;
import com.jsalvar.barbilling.entity.OrderItem;
import com.jsalvar.barbilling.entity.Product;
import com.jsalvar.barbilling.entity.Tab;
import com.jsalvar.barbilling.entity.enums.TabStatus;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.service.OrderItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemControllerTest {

    @Mock
    private OrderItemService orderItemService;

    @InjectMocks
    private OrderItemController orderItemController;

    private Product testProduct;
    private Tab testTab;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id("product-id-123")
                .name("Margarita")
                .price(new BigDecimal("12.50"))
                .build();

        testTab = Tab.builder()
                .id("tab-id-123")
                .status(TabStatus.OPEN)
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

    private OrderItemResponseDto toDto(OrderItem orderItem) {
        BigDecimal subtotal = orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
        return new OrderItemResponseDto(
                orderItem.getId(),
                orderItem.getQuantity(),
                orderItem.getNotes(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                orderItem.getProduct().getPrice(),
                orderItem.getTab().getId(),
                subtotal
        );
    }

    @Test
    void create_Success_Returns201() {
        OrderItemCreateRequestDto dto = new OrderItemCreateRequestDto(
                2, "No olives", "product-id-123", "tab-id-123");

        when(orderItemService.create(any())).thenReturn(testOrderItem);

        ResponseEntity<OrderItemResponseDto> response = orderItemController.create(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void findByTabId_Returns200() {
        when(orderItemService.findByTabId("tab-id-123")).thenReturn(List.of(testOrderItem));

        ResponseEntity<List<OrderItemResponseDto>> response = orderItemController.findByTabId("tab-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void delete_Success_Returns204() {
        doNothing().when(orderItemService).delete("order-item-id-123");

        ResponseEntity<Void> response = orderItemController.delete("order-item-id-123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(orderItemService).delete("order-item-id-123");
    }

    @Test
    void delete_NotFound_ThrowsException() {
        doThrow(new ResourceNotFoundException("Order item not found"))
                .when(orderItemService).delete("unknown-id");

        assertThrows(ResourceNotFoundException.class, () -> orderItemController.delete("unknown-id"));
    }
}