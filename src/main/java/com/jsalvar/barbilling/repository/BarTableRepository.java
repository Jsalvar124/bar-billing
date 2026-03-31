package com.jsalvar.barbilling.repository;

import com.jsalvar.barbilling.entity.BarTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BarTableRepository extends JpaRepository<BarTable, String> {
    Optional<BarTable> findByNumber(String number);
}
