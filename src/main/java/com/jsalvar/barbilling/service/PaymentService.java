package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.dto.request.PaymentApproveRequestDto;
import com.jsalvar.barbilling.dto.request.PaymentAttemptRequestDto;
import com.jsalvar.barbilling.entity.Payment;

import java.util.List;

public interface PaymentService {
    Payment attempt(PaymentAttemptRequestDto dto);   // creates PENDING payment
    Payment approve(String id, PaymentApproveRequestDto dto); // PENDING → APPROVED
    Payment decline(String id, String reason);        // PENDING → DECLINED
    Payment findById(String id);
    List<Payment> findAll();
    List<Payment> findByBillId(String billId);
}
