package com.jsalvar.barbilling.service.impl;

import com.jsalvar.barbilling.dto.request.PaymentApproveRequestDto;
import com.jsalvar.barbilling.dto.request.PaymentAttemptRequestDto;
import com.jsalvar.barbilling.entity.Bill;
import com.jsalvar.barbilling.entity.Payment;
import com.jsalvar.barbilling.entity.enums.BillStatus;
import com.jsalvar.barbilling.entity.enums.PaymentMethod;
import com.jsalvar.barbilling.entity.enums.PaymentStatus;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.PaymentRepository;
import com.jsalvar.barbilling.service.BillService;
import com.jsalvar.barbilling.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final BillService billService;

    public PaymentServiceImpl(PaymentRepository paymentRepository, BillService billService) {
        this.paymentRepository = paymentRepository;
        this.billService = billService;
    }

    @Override
    @Transactional
    public Payment attempt(PaymentAttemptRequestDto dto) {
        Bill bill = billService.findById(dto.billId()); // bill throws if not found
        if (dto.amount().compareTo(bill.getTotal()) != 0) {
            throw new UnprocessableEntityException("Payment amount must match bill total of " + bill.getTotal());
        }
        validateBillStatus(bill);

        Payment payment = Payment.builder()
                .bill(bill)
                .paymentMethod(dto.paymentMethod())
                .amount(dto.amount())
                .build();

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment approve(String id, PaymentApproveRequestDto dto) {
        Payment payment = findById(id);

        Bill bill = payment.getBill();
        validateBillStatus(bill);
        validatePaymentStatus(payment);

        PaymentMethod paymentMethod = payment.getPaymentMethod();
        if(paymentMethod.equals(PaymentMethod.CARD) && dto.confirmationToken()==null){
            throw new UnprocessableEntityException("Confirmation token is required for Card payment");
        }
        if(paymentMethod.equals(PaymentMethod.CARD)){
            payment.setConfirmationToken(dto.confirmationToken());
        }

        payment.setPaymentStatus(PaymentStatus.APPROVED);
        payment.setResolvedAt(LocalDateTime.now());

        Payment savedPayment = paymentRepository.save(payment);
        billService.confirmPayment(savedPayment);
        return savedPayment;
    }

    @Override
    @Transactional
    public Payment decline(String id, String reason) {
        Payment payment = findById(id);
        Bill bill = payment.getBill();
        validateBillStatus(bill);
        validatePaymentStatus(payment);

        payment.setPaymentStatus(PaymentStatus.DECLINED);
        payment.setResolvedAt(LocalDateTime.now());
        payment.setFailureReason(reason);
        return paymentRepository.save(payment);
    }

    @Override
    public Payment findById(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Payment with id "+id+" not found"));
    }

    @Override
    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    @Override
    public List<Payment> findByBillId(String billId) {
        return paymentRepository.findByBillId(billId);
    }

    private void validatePaymentStatus(Payment payment){
        if(!payment.getPaymentStatus().equals(PaymentStatus.PENDING)){
            throw new UnprocessableEntityException("Payment status must be pending, current state: "+ payment.getPaymentStatus());
        }
    }

    private void validateBillStatus(Bill bill){
        if(!bill.getBillStatus().equals(BillStatus.PENDING)){
            throw new UnprocessableEntityException("Associated bill payment status must be pending, current state: "+ bill.getBillStatus());
        }
    }


}
