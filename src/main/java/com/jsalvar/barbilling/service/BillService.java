package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.dto.request.BillCancelRequestDto;
import com.jsalvar.barbilling.dto.request.BillCreateRequestDto;
import com.jsalvar.barbilling.entity.Bill;
import com.jsalvar.barbilling.entity.Payment;

import java.util.List;

public interface BillService {
    Bill create(BillCreateRequestDto dto);
    Bill cancel(BillCancelRequestDto dto);
    Bill findById(String id);
    List<Bill> findAll();
    Bill confirmPayment(Payment payment);
}
