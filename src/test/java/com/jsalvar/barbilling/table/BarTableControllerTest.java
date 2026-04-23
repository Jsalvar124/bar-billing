package com.jsalvar.barbilling.table;

import com.jsalvar.barbilling.controller.BarTableController;
import com.jsalvar.barbilling.dto.request.BarTableCreateRequestDto;
import com.jsalvar.barbilling.dto.request.BarTableUpdateRequestDto;
import com.jsalvar.barbilling.dto.response.BarTableResponseDto;
import com.jsalvar.barbilling.entity.BarTable;
import com.jsalvar.barbilling.entity.enums.TableStatus;
import com.jsalvar.barbilling.service.BarTableService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BarTableControllerTest {

    @Mock
    private BarTableService barTableService;

    @InjectMocks
    private BarTableController barTableController;

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

    private BarTableResponseDto toDto(BarTable barTable) {
        return new BarTableResponseDto(
                barTable.getId(),
                barTable.getNumber(),
                barTable.getCapacity(),
                barTable.getStatus()
        );
    }

    @Test
    void findAll_AsAdmin_Returns200() {
        when(barTableService.findAll()).thenReturn(List.of(testTable));

        ResponseEntity<List<BarTableResponseDto>> response = barTableController.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void findById_AsAdmin_Returns200() {
        when(barTableService.findById("table-id-123")).thenReturn(testTable);

        ResponseEntity<BarTableResponseDto> response = barTableController.findById("table-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("table-id-123", response.getBody().id());
    }

    @Test
    void findById_NotFound_Returns404() {
        when(barTableService.findById("unknown-id")).thenThrow(new EntityNotFoundException("Not found"));

        assertThrows(EntityNotFoundException.class, () -> barTableController.findById("unknown-id"));
    }

    @Test
    void findByNumber_AsAdmin_Returns200() {
        when(barTableService.findByNumber("1")).thenReturn(testTable);

        ResponseEntity<BarTableResponseDto> response = barTableController.findByNumber("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("1", response.getBody().number());
    }

    @Test
    void reserveTable_AsWaiter_Returns200() {
        testTable.setStatus(TableStatus.RESERVED);
        when(barTableService.reserveTable("table-id-123")).thenReturn(testTable);

        ResponseEntity<BarTableResponseDto> response = barTableController.reserveTable("table-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TableStatus.RESERVED, response.getBody().status());
    }

    @Test
    void reserveTable_AsAdmin_Returns200() {
        testTable.setStatus(TableStatus.RESERVED);
        when(barTableService.reserveTable("table-id-123")).thenReturn(testTable);

        ResponseEntity<BarTableResponseDto> response = barTableController.reserveTable("table-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void cancelReservation_AsWaiter_Returns200() {
        when(barTableService.cancelReservation("table-id-123")).thenReturn(testTable);

        ResponseEntity<BarTableResponseDto> response = barTableController.cancelTableReservation("table-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void create_AsAdmin_Returns201() {
        BarTableCreateRequestDto dto = new BarTableCreateRequestDto("5", 5);
        when(barTableService.create(any())).thenReturn(testTable);

        ResponseEntity<BarTableResponseDto> response = barTableController.create(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void update_AsAdmin_Returns200() {
        BarTableUpdateRequestDto dto = new BarTableUpdateRequestDto("table-id-123", "2", 6);
        when(barTableService.update(eq("table-id-123"), any())).thenReturn(testTable);

        ResponseEntity<BarTableResponseDto> response = barTableController.update("table-id-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void delete_AsAdmin_Returns204() {
        doNothing().when(barTableService).delete("table-id-123");

        ResponseEntity<Void> response = barTableController.delete("table-id-123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(barTableService).delete("table-id-123");
    }
}