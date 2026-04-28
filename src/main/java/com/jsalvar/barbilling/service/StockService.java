package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.entity.Product;
import com.jsalvar.barbilling.entity.Stock;

import java.util.List;

public interface StockService {
    Stock findByProductId(String productId);
    List<Stock> findLowStock();
    Stock updateThreshold(String productId, int threshold);
    Stock initializeStock(Product product);      // called internally only
    Stock decrementStock(String productId, int quantity);  // called by OrderItem service
    Stock refillStock(String productId, int quantity);
}
