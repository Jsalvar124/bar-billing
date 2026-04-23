package com.jsalvar.barbilling.table;

import com.jsalvar.barbilling.dto.request.BarTableCreateRequestDto;
import com.jsalvar.barbilling.dto.request.BarTableUpdateRequestDto;
import com.jsalvar.barbilling.entity.BarTable;
import com.jsalvar.barbilling.entity.enums.TableStatus;
import com.jsalvar.barbilling.exception.UnprocessableEntityException;
import com.jsalvar.barbilling.repository.BarTableRepository;
import com.jsalvar.barbilling.service.BarTableService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarTableServiceImplTest {

    @Mock
    private BarTableRepository barTableRepository;

    @InjectMocks
    private com.jsalvar.barbilling.service.impl.BarTableImpl barTableService;

    private BarTable testTable;

    @BeforeEach
    void setUp() {
        testTable = new BarTable();
        testTable.setId("table-id-123");
        testTable.setNumber("1");
        testTable.setCapacity(4);
        testTable.setStatus(TableStatus.AVAILABLE);
        testTable.setActive(true);
    }

    @Test
    void findAll_Success() {
        when(barTableRepository.findAll()).thenReturn(List.of(testTable));

        List<BarTable> result = barTableService.findAll();

        assertEquals(1, result.size());
        assertEquals("table-id-123", result.get(0).getId());
    }

    @Test
    void findAll_EmptyList() {
        when(barTableRepository.findAll()).thenReturn(List.of());

        List<BarTable> result = barTableService.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_Success() {
        when(barTableRepository.findById("table-id-123")).thenReturn(Optional.of(testTable));

        BarTable result = barTableService.findById("table-id-123");

        assertNotNull(result);
        assertEquals("table-id-123", result.getId());
    }

    @Test
    void findById_NotFound() {
        when(barTableRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> barTableService.findById("unknown-id"));
    }

    @Test
    void findByNumber_Success() {
        when(barTableRepository.findByNumber("1")).thenReturn(Optional.of(testTable));

        BarTable result = barTableService.findByNumber("1");

        assertNotNull(result);
        assertEquals("1", result.getNumber());
    }

    @Test
    void findByNumber_NotFound() {
        when(barTableRepository.findByNumber("999")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> barTableService.findByNumber("999"));
    }

    @Test
    void create_Success() {
        BarTableCreateRequestDto dto = new BarTableCreateRequestDto("5", 5);
        when(barTableRepository.save(any(BarTable.class))).thenAnswer(invocation -> {
            BarTable table = invocation.getArgument(0);
            table.setId("new-table-id");
            return table;
        });

        BarTable result = barTableService.create(dto);

        assertNotNull(result);
        assertEquals("5", result.getNumber());
        assertEquals(5, result.getCapacity());
        assertEquals(TableStatus.AVAILABLE, result.getStatus());
    }

    @Test
    void update_Success() {
        BarTableUpdateRequestDto dto = new BarTableUpdateRequestDto("table-id-123", "2", 6);
        when(barTableRepository.findById("table-id-123")).thenReturn(Optional.of(testTable));
        when(barTableRepository.save(any(BarTable.class))).thenAnswer(inv -> inv.getArgument(0));

        BarTable result = barTableService.update("table-id-123", dto);

        assertEquals("2", result.getNumber());
        assertEquals(6, result.getCapacity());
    }

    @Test
    void update_NotFound() {
        BarTableUpdateRequestDto dto = new BarTableUpdateRequestDto("unknown-id", "2", 6);
        when(barTableRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> barTableService.update("unknown-id", dto));
    }

    @Test
    void delete_SoftDelete() {
        when(barTableRepository.findById("table-id-123")).thenReturn(Optional.of(testTable));
        when(barTableRepository.save(any(BarTable.class))).thenAnswer(inv -> inv.getArgument(0));

        barTableService.delete("table-id-123");

        assertFalse(testTable.isActive());
        verify(barTableRepository).save(testTable);
    }

    @Test
    void delete_NotAvailable_ThrowsException() {
        testTable.setStatus(TableStatus.RESERVED);
        when(barTableRepository.findById("table-id-123")).thenReturn(Optional.of(testTable));

        assertThrows(UnprocessableEntityException.class, () -> barTableService.delete("table-id-123"));
    }

    @Test
    void reserveTable_Success_Available() {
        when(barTableRepository.findById("table-id-123")).thenReturn(Optional.of(testTable));
        when(barTableRepository.save(any(BarTable.class))).thenAnswer(inv -> inv.getArgument(0));

        BarTable result = barTableService.reserveTable("table-id-123");

        assertEquals(TableStatus.RESERVED, result.getStatus());
    }

    @Test
    void reserveTable_AlreadyReserved() {
        testTable.setStatus(TableStatus.RESERVED);
        when(barTableRepository.findById("table-id-123")).thenReturn(Optional.of(testTable));

        assertThrows(UnprocessableEntityException.class, () -> barTableService.reserveTable("table-id-123"));
    }

    @Test
    void cancelReservation_Success_Reserved() {
        testTable.setStatus(TableStatus.RESERVED);
        when(barTableRepository.findById("table-id-123")).thenReturn(Optional.of(testTable));
        when(barTableRepository.save(any(BarTable.class))).thenAnswer(inv -> inv.getArgument(0));

        BarTable result = barTableService.cancelReservation("table-id-123");

        assertEquals(TableStatus.AVAILABLE, result.getStatus());
    }

    @Test
    void cancelReservation_NotReserved_ThrowsException() {
        when(barTableRepository.findById("table-id-123")).thenReturn(Optional.of(testTable));

        assertThrows(UnprocessableEntityException.class, () -> barTableService.cancelReservation("table-id-123"));
    }
}