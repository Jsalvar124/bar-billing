package com.jsalvar.barbilling.repository;

import com.jsalvar.barbilling.entity.Bill;
import com.jsalvar.barbilling.entity.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, String> {
    boolean existsByTabIdAndBillStatusNot(String tabId, BillStatus status);
}
