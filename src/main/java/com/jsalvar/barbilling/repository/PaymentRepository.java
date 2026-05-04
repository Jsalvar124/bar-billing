package com.jsalvar.barbilling.repository;

import com.jsalvar.barbilling.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByBillId(String billId);
}
