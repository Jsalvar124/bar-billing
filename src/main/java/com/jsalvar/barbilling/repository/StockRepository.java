package com.jsalvar.barbilling.repository;

import com.jsalvar.barbilling.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {
    Optional<Stock> findByProductId(String productId);

    @Query("SELECT s FROM Stock s WHERE s.quantity <= s.lowStockThreshold")
    List<Stock> findLowStock();
}
