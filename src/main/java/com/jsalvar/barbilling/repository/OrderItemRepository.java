package com.jsalvar.barbilling.repository;

import com.jsalvar.barbilling.entity.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    // Eager load up to tax rates, avoids N+1 problem, here is even worse, for 20 products, 61 queries, now it's just one.
    @EntityGraph(attributePaths = {
            "product",
            "product.category",
            "product.category.taxRates"
    })
    List<OrderItem> findByTabId(String id);
}
