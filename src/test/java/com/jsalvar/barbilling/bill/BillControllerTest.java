package com.jsalvar.barbilling.bill;

import com.jsalvar.barbilling.controller.BillController;
import com.jsalvar.barbilling.dto.request.BillCancelRequestDto;
import com.jsalvar.barbilling.dto.request.BillCreateRequestDto;
import com.jsalvar.barbilling.dto.response.BillResponseDto;
import com.jsalvar.barbilling.entity.BarTable;
import com.jsalvar.barbilling.entity.Bill;
import com.jsalvar.barbilling.entity.Tab;
import com.jsalvar.barbilling.entity.UserImpl;
import com.jsalvar.barbilling.entity.enums.BillStatus;
import com.jsalvar.barbilling.entity.enums.TableStatus;
import com.jsalvar.barbilling.entity.enums.TabStatus;
import com.jsalvar.barbilling.exception.ResourceNotFoundException;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.service.BillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillControllerTest {

    @Mock
    private BillService billService;

    @InjectMocks
    private BillController billController;

    private Tab testTab;
    private BarTable testTable;
    private UserImpl testWaiter;
    private UserImpl testCashier;
    private Bill testBill;

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
    }

    private BillResponseDto toDto(Bill bill) {
        return new BillResponseDto(
                bill.getId(),
                bill.getSubtotal(),
                bill.getTax(),
                bill.getTip(),
                bill.getTotal(),
                bill.getBillStatus(),
                bill.getPaidAt(),
                bill.getCancelledAt(),
                bill.getCancellationReason(),
                new BillResponseDto.TabInfo(
                        bill.getTab().getId(),
                        bill.getTab().getTable().getId(),
                        bill.getTab().getTable().getNumber(),
                        bill.getTab().getWaiter().getName() + " " + bill.getTab().getWaiter().getLastname(),
                        bill.getTab().getStatus().name()
                ),
                new BillResponseDto.CashierInfo(
                        bill.getCashier().getId(),
                        bill.getCashier().getName(),
                        bill.getCashier().getLastname()
                )
        );
    }

    @Test
    void create_Success_Returns201() {
        BillCreateRequestDto dto = new BillCreateRequestDto(
                "tab-id-123", "cashier-id-123", new BigDecimal("0.10"));

        when(billService.create(any())).thenReturn(testBill);

        ResponseEntity<BillResponseDto> response = billController.create(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void create_Unprocessable_ThrowsException() {
        BillCreateRequestDto dto = new BillCreateRequestDto(
                "tab-id-123", "cashier-id-123", new BigDecimal("0.10"));

        when(billService.create(any()))
                .thenThrow(new UnprocessableEntityException("Tab must be closed"));

        assertThrows(UnprocessableEntityException.class,
                () -> billController.create(dto));
    }

    @Test
    void findAll_Returns200() {
        when(billService.findAll()).thenReturn(List.of(testBill));

        ResponseEntity<List<BillResponseDto>> response = billController.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void findById_Success_Returns200() {
        when(billService.findById("bill-id-123")).thenReturn(testBill);

        ResponseEntity<BillResponseDto> response = billController.findById("bill-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("bill-id-123", response.getBody().id());
    }

    @Test
    void findById_NotFound_ThrowsException() {
        when(billService.findById("unknown-id")).thenThrow(
                new ResourceNotFoundException("Bill not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> billController.findById("unknown-id"));
    }

    @Test
    void cancel_Success_Returns200() {
        BillCancelRequestDto dto = new BillCancelRequestDto("bill-id-123", "Customer left");
        when(billService.cancel(any())).thenReturn(testBill);

        ResponseEntity<BillResponseDto> response = billController.cancel("bill-id-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void cancel_NotFound_ThrowsException() {
        BillCancelRequestDto dto = new BillCancelRequestDto("unknown-id", "Reason");
        when(billService.cancel(any())).thenThrow(
                new ResourceNotFoundException("Bill not found"));

        assertThrows(ResourceNotFoundException.class,
                () -> billController.cancel("unknown-id", dto));
    }
}