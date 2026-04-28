package com.jsalvar.barbilling.repository;

import com.jsalvar.barbilling.entity.Category;
import com.jsalvar.barbilling.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    long countByCategory(Category category);
}