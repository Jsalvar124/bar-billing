package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.OrderItemCreateRequestDto;
import com.jsalvar.barbilling.dto.request.ProductCreateRequestDto;
import com.jsalvar.barbilling.dto.response.OrderItemResponseDto;
import com.jsalvar.barbilling.dto.response.ProductResponseDto;
import com.jsalvar.barbilling.entity.OrderItem;
import com.jsalvar.barbilling.entity.Product;
import com.jsalvar.barbilling.service.OrderItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/order-items")
public class OrderItemController {
    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @Loggable
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAITER')")
    public ResponseEntity<OrderItemResponseDto> create(@RequestBody @Valid OrderItemCreateRequestDto dto) {
        OrderItem orderItem = orderItemService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(orderItem));
    }

    @Loggable
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('WAITER')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        orderItemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Loggable
    @GetMapping("/tab/{tabId}")
    public ResponseEntity<List<OrderItemResponseDto>> findByTabId(@PathVariable String tabId){
        List<OrderItemResponseDto> orderItems = orderItemService.findByTabId(tabId).stream().map(this::toDto).toList();
        return ResponseEntity.ok(orderItems);
    }

    private OrderItemResponseDto toDto(OrderItem orderItem){
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

}
