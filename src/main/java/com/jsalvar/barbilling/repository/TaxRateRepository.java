package com.jsalvar.barbilling.repository;

import com.jsalvar.barbilling.entity.TaxRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaxRateRepository extends JpaRepository<TaxRate, String> {
    Optional<TaxRate> findByName(String name);
    boolean existsByName(String name);
}