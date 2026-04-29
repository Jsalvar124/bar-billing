package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.dto.request.OrderItemCreateRequestDto;
import com.jsalvar.barbilling.entity.OrderItem;

import java.util.List;

public interface OrderItemService {
    List<OrderItem> findByTabId(String id);
    OrderItem create(OrderItemCreateRequestDto dto);
    void delete(String id);
    OrderItem findById(String id);
}
