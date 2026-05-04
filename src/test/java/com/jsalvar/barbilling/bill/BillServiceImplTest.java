package com.jsalvar.barbilling.bill;

import com.jsalvar.barbilling.dto.request.BillCancelRequestDto;
import com.jsalvar.barbilling.dto.request.BillCreateRequestDto;
import com.jsalvar.barbilling.entity.*;
import com.jsalvar.barbilling.entity.enums.*;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.BillRepository;
import com.jsalvar.barbilling.service.BarTableService;
import com.jsalvar.barbilling.service.BillService;
import com.jsalvar.barbilling.service.OrderItemService;
import com.jsalvar.barbilling.service.TabService;
import com.jsalvar.barbilling.service.UserService;
import com.jsalvar.barbilling.service.impl.BillServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillServiceImplTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private TabService tabService;

    @Mock
    private UserService userService;

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private BarTableService barTableService;

    @InjectMocks
    private BillServiceImpl billService;

    private Tab testTab;
    private BarTable testTable;
    private UserImpl testCashier;
    private UserImpl testWaiter;
    private Bill testBill;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testTable = BarTable.builder()
                .id("table-id-123")
                .number("1")
                .capacity(4)
                .status(TableStatus.OCCUPIED)
                .build();

        testWaiter = UserImpl.builder()
                .id("waiter-id-123")
                .name("John")
                .lastname("Doe")
                .role(Role.WAITER)
                .build();

        testTab = Tab.builder()
                .id("tab-id-123")
                .table(testTable)
                .waiter(testWaiter)
                .status(TabStatus.CLOSED)
                .build();

        testCashier = UserImpl.builder()
                .id("cashier-id-123")
                .name("Jane")
                .lastname("Smith")
                .role(Role.CASHIER)
                .build();

        testBill = Bill.builder()
                .id("bill-id-123")
                .tab(testTab)
                .cashier(testCashier)
                .subtotal(new BigDecimal("25.00"))
                .tax(new BigDecimal("4.00"))
                .tip(new BigDecimal("3.00"))
                .total(new BigDecimal("32.00"))
                .billStatus(BillStatus.PENDING)
                .build();

        Product testProduct = Product.builder()
                .id("product-id-123")
                .name("Margarita")
                .price(new BigDecimal("12.50"))
                .category(Category.builder().id("category-id-123").name("Drinks").build())
                .build();

        testOrderItem = OrderItem.builder()
                .id("order-item-id-123")
                .product(testProduct)
                .tab(testTab)
                .quantity(2)
                .unitPrice(new BigDecimal("12.50"))
                .build();
    }

    @Test
    void findById_Success() {
        when(billRepository.findById("bill-id-123")).thenReturn(Optional.of(testBill));

        Bill result = billService.findById("bill-id-123");

        assertNotNull(result);
        assertEquals("bill-id-123", result.getId());
    }

    @Test
    void findById_NotFound() {
        when(billRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> billService.findById("unknown-id"));
    }

    @Test
    void findAll_Success() {
        when(billRepository.findAll()).thenReturn(List.of(testBill));

        List<Bill> result = billService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void create_Success() {
        BillCreateRequestDto dto = new BillCreateRequestDto(
                "tab-id-123", "cashier-id-123", new BigDecimal("0.10"));

        when(tabService.findById("tab-id-123")).thenReturn(testTab);
        when(userService.findById("cashier-id-123")).thenReturn(testCashier);
        when(billRepository.existsByTabIdAndBillStatusNot("tab-id-123", BillStatus.CANCELLED))
                .thenReturn(false);
        when(orderItemService.findByTabId("tab-id-123")).thenReturn(List.of(testOrderItem));
        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> {
            Bill bill = invocation.getArgument(0);
            bill.setId("new-bill-id");
            return bill;
        });

        Bill result = billService.create(dto);

        assertNotNull(result);
        assertEquals(BillStatus.PENDING, result.getBillStatus());
        assertEquals(testTab, result.getTab());
        assertEquals(testCashier, result.getCashier());
    }

    @Test
    void create_TabNotClosed() {
        testTab.setStatus(TabStatus.OPEN);
        BillCreateRequestDto dto = new BillCreateRequestDto(
                "tab-id-123", "cashier-id-123", new BigDecimal("0.10"));

        when(tabService.findById("tab-id-123")).thenReturn(testTab);

        assertThrows(UnprocessableEntityException.class, () -> billService.create(dto));
    }

    @Test
    void create_ActiveBillExists() {
        BillCreateRequestDto dto = new BillCreateRequestDto(
                "tab-id-123", "cashier-id-123", new BigDecimal("0.10"));

        when(tabService.findById("tab-id-123")).thenReturn(testTab);
        when(billRepository.existsByTabIdAndBillStatusNot("tab-id-123", BillStatus.CANCELLED))
                .thenReturn(true);

        assertThrows(UnprocessableEntityException.class, () -> billService.create(dto));
    }

    @Test
    void create_CashierNotCashierRole() {
        testCashier.setRole(Role.WAITER);
        BillCreateRequestDto dto = new BillCreateRequestDto(
                "tab-id-123", "cashier-id-123", new BigDecimal("0.10"));

        when(tabService.findById("tab-id-123")).thenReturn(testTab);
        when(userService.findById("cashier-id-123")).thenReturn(testCashier);
        when(billRepository.existsByTabIdAndBillStatusNot(anyString(), any()))
                .thenReturn(false);

        assertThrows(UnprocessableEntityException.class, () -> billService.create(dto));
    }

    @Test
    void cancel_Success() {
        BillCancelRequestDto dto = new BillCancelRequestDto("bill-id-123", "Customer left");

        when(billRepository.findById("bill-id-123")).thenReturn(Optional.of(testBill));
        when(billRepository.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        Bill result = billService.cancel(dto);

        assertEquals(BillStatus.CANCELLED, result.getBillStatus());
        assertEquals("Customer left", result.getCancellationReason());
        assertNotNull(result.getCancelledAt());
    }

    @Test
    void cancel_AlreadyPaid() {
        testBill.setBillStatus(BillStatus.PAID);
        BillCancelRequestDto dto = new BillCancelRequestDto("bill-id-123", "Reason");

        when(billRepository.findById("bill-id-123")).thenReturn(Optional.of(testBill));

        assertThrows(UnprocessableEntityException.class, () -> billService.cancel(dto));
    }

    @Test
    void cancel_AlreadyCancelled() {
        testBill.setBillStatus(BillStatus.CANCELLED);
        BillCancelRequestDto dto = new BillCancelRequestDto("bill-id-123", "Reason");

        when(billRepository.findById("bill-id-123")).thenReturn(Optional.of(testBill));

        assertThrows(UnprocessableEntityException.class, () -> billService.cancel(dto));
    }

    @Test
    void cancel_NotFound() {
        BillCancelRequestDto dto = new BillCancelRequestDto("unknown-id", "Reason");

        when(billRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> billService.cancel(dto));
    }

    @Test
    void confirmPayment_Success() {
        Payment payment = Payment.builder()
                .bill(testBill)
                .paymentStatus(PaymentStatus.APPROVED)
                .build();
        testBill.setBillStatus(BillStatus.PENDING);

        when(billRepository.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));
        when(barTableService.changeStatus(testTable, TableStatus.AVAILABLE)).thenReturn(testTable);

        Bill result = billService.confirmPayment(payment);

        assertEquals(BillStatus.PAID, result.getBillStatus());
        assertNotNull(result.getPaidAt());
        verify(barTableService).changeStatus(testTable, TableStatus.AVAILABLE);
    }

    @Test
    void confirmPayment_NotPending() {
        Payment payment = Payment.builder()
                .bill(testBill)
                .paymentStatus(PaymentStatus.APPROVED)
                .build();
        testBill.setBillStatus(BillStatus.CANCELLED);

        assertThrows(UnprocessableEntityException.class, () -> billService.confirmPayment(payment));
    }

    @Test
    void confirmPayment_PaymentNotApproved() {
        Payment payment = Payment.builder()
                .bill(testBill)
                .paymentStatus(PaymentStatus.DECLINED)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> billService.confirmPayment(payment));
    }
}