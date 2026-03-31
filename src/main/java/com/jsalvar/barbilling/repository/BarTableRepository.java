package com.jsalvar.barbilling.repository;

import com.jsalvar.barbilling.entity.BarTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarTableRepository extends JpaRepository<BarTable, String> {
}
