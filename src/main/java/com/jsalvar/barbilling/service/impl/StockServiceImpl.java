package com.jsalvar.barbilling.service.impl;

import com.jsalvar.barbilling.entity.Product;
import com.jsalvar.barbilling.entity.Stock;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.StockRepository;
import com.jsalvar.barbilling.service.StockService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StockServiceImpl implements StockService {
    @Value("${barbilling.stock.max-refill-quantity}")
    private int maxRefillQuantity;

    @Value("${barbilling.stock.max-threshold}")
    private int maxLowStockThreshold;

    private final StockRepository stockRepository;

    public StockServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }


    @Override
    public Stock findByProductId(String productId) {
        return stockRepository.findByProductId(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product with id " + productId + " not found"));
    }

    @Override
    public List<Stock> findLowStock() {
        return stockRepository.findLowStock();
    }

    @Override
    @Transactional
    public Stock updateThreshold(String productId, int threshold) {
        Stock stock = findByProductId(productId);
        if(threshold <= 0 || threshold > maxLowStockThreshold){
            throw new IllegalArgumentException("Threshold must be between 1 and " + maxLowStockThreshold);
        }
        stock.setLowStockThreshold(threshold);
        return stockRepository.save(stock);
    }

    @Override
    @Transactional
    public Stock initializeStock(Product product) {
        Stock stock = Stock.builder()
                .product(product)
                .quantity(0) // initialize in zero
                .lowStockThreshold(10)  // initial low stock threshold
                .build();
        return stockRepository.save(stock);
    }

    @Override
    @Transactional
    public Stock decrementStock(String productId, int quantity) {
        if(quantity < 0){
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Stock stock = findByProductId(productId);
        int currentStock = stock.getQuantity();

        if(quantity > currentStock){
            throw new UnprocessableEntityException("Not enough stock for purchase");
        }
        stock.setQuantity(currentStock - quantity);
        return stockRepository.save(stock);
    }

    @Override
    @Transactional
    public Stock refillStock(String productId, int quantity) {
        if(quantity < 0 || quantity > maxRefillQuantity){
            throw new IllegalArgumentException("Quantity must be between 1 and " + maxRefillQuantity);
        }
        Stock stock = findByProductId(productId);
        int currentStock = stock.getQuantity();
        stock.setQuantity(currentStock + quantity);
        return stockRepository.save(stock);
    }
}
