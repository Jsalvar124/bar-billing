package com.jsalvar.barbilling.service.impl;

import com.jsalvar.barbilling.dto.request.BillCancelRequestDto;
import com.jsalvar.barbilling.dto.request.BillCreateRequestDto;
import com.jsalvar.barbilling.entity.*;
import com.jsalvar.barbilling.entity.enums.*;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.BillRepository;
import com.jsalvar.barbilling.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class BillServiceImpl implements BillService {
    private final BillRepository billRepository;
    private final TabService tabService;
    private final UserService userService;
    private final OrderItemService orderItemService;
    private final BarTableService barTableService;

    public BillServiceImpl(BillRepository billRepository, TabService tabService, UserService userService, OrderItemService orderItemService, BarTableService barTableService) {
        this.billRepository = billRepository;
        this.tabService = tabService;
        this.userService = userService;
        this.orderItemService = orderItemService;
        this.barTableService = barTableService;
    }

    @Override
    @Transactional
    public Bill create(BillCreateRequestDto dto) {
        // Get tab and check status is CLOSED
        Tab tab = tabService.findById(dto.tabId());
        if(!tab.getStatus().equals(TabStatus.CLOSED)){
            throw new UnprocessableEntityException("Tab must be closed before billing");
        }
        // check no active bill exist for a given tab, multiple cancelled can have the same tab
        boolean existActiveBill = billRepository.existsByTabIdAndBillStatusNot(tab.getId(), BillStatus.CANCELLED);
        if (existActiveBill) {
            throw new UnprocessableEntityException("An active bill already exists for this tab");
        }
        // Check cashier role
        UserImpl cashier = userService.findById(dto.cashierId());
        if(!cashier.getRole().equals(Role.CASHIER) && !cashier.getRole().equals(Role.ADMIN)){
            throw new UnprocessableEntityException("Selected user is not a cashier");
        }
        // Calculate subtotal
        List<OrderItem> orderItems = orderItemService.findByTabId(tab.getId());
        BigDecimal subtotal = calculateSubtotal(orderItems);
        BigDecimal tax = calculateTax(orderItems);
        BigDecimal total = calculateTotal(subtotal, tax, dto.tip());


        // timestamp is created by hibernate using @CreationTimestamp
        Bill bill = Bill.builder()
                .tab(tab)
                .cashier(cashier)
                .tip(dto.tip())
                .subtotal(subtotal)
                .tax(tax)
                .total(total)
                .build();

        return billRepository.save(bill);
    }

    @Override
    @Transactional
    public Bill cancel(BillCancelRequestDto dto) {
        // Get bill and check it was not already payed or cancelled.
        Bill bill = findById(dto.id());
        if(bill.getBillStatus().equals(BillStatus.PAID)){
            throw new UnprocessableEntityException("Bill was already payed, cannot cancel a payed bill");
        }
        if(bill.getBillStatus().equals(BillStatus.CANCELLED)){
            throw new UnprocessableEntityException("Bill is already cancelled");
        }

        bill.setCancellationReason(dto.cancellationReason());
        bill.setBillStatus(BillStatus.CANCELLED);
        bill.setCancelledAt(LocalDateTime.now());

        return billRepository.save(bill);
    }

    @Override
    public Bill findById(String id) {
        return billRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Bill with id "+id+" not found"));
    }

    @Override
    public List<Bill> findAll() {
        return billRepository.findAll();
    }

    @Override
    @Transactional
    public Bill confirmPayment(Payment payment) {
        Bill bill = payment.getBill();
        if (!bill.getBillStatus().equals(BillStatus.PENDING)) {
            throw new UnprocessableEntityException("Bill is not pending payment");
        }
        if (!payment.getPaymentStatus().equals(PaymentStatus.APPROVED)) {
            throw new UnprocessableEntityException("Payment has not been approved");
        }
        bill.setBillStatus(BillStatus.PAID);
        bill.setPaidAt(LocalDateTime.now());
        barTableService.changeStatus(bill.getTab().getTable(), TableStatus.AVAILABLE);
        return billRepository.save(bill);
    }

    private BigDecimal calculateSubtotal(List<OrderItem> orderItems){
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            subtotal = subtotal.add(
                    item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
            );
        }
        return subtotal;
    }

    private BigDecimal calculateTax(List<OrderItem> orderItems){
        BigDecimal tax = BigDecimal.ZERO;
        for (OrderItem item : orderItems) {
            // Each item may have multiple taxes
            Set<TaxRate> taxRateList = item.getProduct().getCategory().getTaxRates(); // Here you may have N+1 problem, fixed with @EntityGraph

            tax = tax.add (taxRateList.stream().map(taxRate -> taxRate.getRate()
                    .multiply(item.getUnitPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
            );
        }
        return tax;
    }

    private BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal tax, BigDecimal tip){
        BigDecimal tipAmount = subtotal.multiply(tip);
        return subtotal.add(tax).add(tipAmount);
    }
}
