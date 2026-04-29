package com.jsalvar.barbilling.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final TabService tabService;
    private final ProductService productService;
    private final StockService stockService;

    public OrderItemServiceImpl(OrderItemRepository orderItemRepository, TabService tabService, ProductService productService, StockService stockService) {
        this.orderItemRepository = orderItemRepository;
        this.tabService = tabService;
        this.productService = productService;
        this.stockService = stockService;
    }


    @Override
    public List<OrderItem> findByTabId(String tabId) {
        tabService.findById(tabId); // throws ResourceNotFoundException if not found
        return orderItemRepository.findByTabId(tabId);
    }

    @Override
    @Transactional
    public OrderItem create(OrderItemCreateRequestDto dto) {
        Tab tab = tabService.findById(dto.tabId());
        // check tab status
        if(!tab.getStatus().equals(TabStatus.OPEN)){
            throw new UnprocessableEntityException("Invalid operation, Cannot add items on a closed or cancelled tab");
        }
        Product product = productService.findById(dto.productId());
        // check product status
        if(!product.isActive() || !product.isAvailable() ){
            throw new UnprocessableEntityException("Invalid operation, product is inactive or unavailable");
        }
        // Check product stock
        Stock productStock = stockService.findByProductId(product.getId());
        if(productStock.getQuantity() < dto.quantity()){
            throw new UnprocessableEntityException("Not enough stock for the order, remaining stock:" + productStock.getQuantity());
        }

        // reduce stock
        stockService.decrementStock(product.getId(), dto.quantity());

        OrderItem orderItem = OrderItem.builder()
                .tab(tab)
                .product(product)
                .unitPrice(product.getPrice()) // price is a snapshot from the product
                .quantity(dto.quantity())
                .notes(dto.notes())
                .build();

        return orderItemRepository.save(orderItem);
    }

    @Override
    @Transactional
    public void delete(String id) {
        OrderItem orderItem = findById(id); //get order item

        Tab tab = orderItem.getTab();
        // check tab status
        if(!tab.getStatus().equals(TabStatus.OPEN)){
            throw new UnprocessableEntityException("Invalid operation, Cannot delete items from a closed or cancelled tab");
        }
        // Refill product stock
        stockService.refillStock(orderItem.getProduct().getId(), orderItem.getQuantity());

        orderItemRepository.delete(orderItem);
    }

    @Override
    public OrderItem findById(String id) {
        return orderItemRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Order item with id "+id+" not found"));
    }
}
