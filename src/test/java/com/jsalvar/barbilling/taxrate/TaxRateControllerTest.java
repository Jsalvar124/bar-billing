package com.jsalvar.barbilling.taxrate;

import com.jsalvar.barbilling.controller.TaxRateController;
import com.jsalvar.barbilling.dto.request.TaxRateCreateRequestDto;
import com.jsalvar.barbilling.dto.request.TaxRateUpdateRequestDto;
import com.jsalvar.barbilling.dto.response.TaxRateResponseDto;
import com.jsalvar.barbilling.entity.TaxRate;
import com.jsalvar.barbilling.service.TaxRateService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxRateControllerTest {

    @Mock
    private TaxRateService taxRateService;

    @InjectMocks
    private TaxRateController taxRateController;

    private TaxRate testTaxRate;

    @BeforeEach
    void setUp() {
        testTaxRate = TaxRate.builder()
                .id("tax-id-123")
                .name("IVA")
                .rate(new BigDecimal("0.16"))
                .build();
    }

    private TaxRateResponseDto toDto(TaxRate taxRate) {
        return new TaxRateResponseDto(
                taxRate.getId(),
                taxRate.getName(),
                taxRate.getRate()
        );
    }

    @Test
    void findAll_AsAdmin_Returns200() {
        when(taxRateService.findAll()).thenReturn(List.of(testTaxRate));

        ResponseEntity<List<TaxRateResponseDto>> response = taxRateController.findAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void findById_AsAdmin_Returns200() {
        when(taxRateService.findById("tax-id-123")).thenReturn(testTaxRate);

        ResponseEntity<TaxRateResponseDto> response = taxRateController.findById("tax-id-123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("tax-id-123", response.getBody().id());
    }

    @Test
    void findById_NotFound_ThrowsException() {
        when(taxRateService.findById("unknown-id")).thenThrow(new EntityNotFoundException("Not found"));

        assertThrows(EntityNotFoundException.class, () -> taxRateController.findById("unknown-id"));
    }

    @Test
    void create_AsAdmin_Returns201() {
        TaxRateCreateRequestDto dto = new TaxRateCreateRequestDto("IVA", new BigDecimal("0.16"));
        when(taxRateService.create(any())).thenReturn(testTaxRate);

        ResponseEntity<TaxRateResponseDto> response = taxRateController.create(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void update_AsAdmin_Returns200() {
        TaxRateUpdateRequestDto dto = new TaxRateUpdateRequestDto("IVA Updated", new BigDecimal("0.20"));
        when(taxRateService.update(eq("tax-id-123"), any())).thenReturn(testTaxRate);

        ResponseEntity<TaxRateResponseDto> response = taxRateController.update("tax-id-123", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void delete_AsAdmin_Returns204() {
        doNothing().when(taxRateService).delete("tax-id-123");

        ResponseEntity<Void> response = taxRateController.delete("tax-id-123");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(taxRateService).delete("tax-id-123");
    }

    @Test
    void delete_NotFound_ThrowsException() {
        doThrow(new EntityNotFoundException("Not found")).when(taxRateService).delete("unknown-id");

        assertThrows(EntityNotFoundException.class, () -> taxRateController.delete("unknown-id"));
    }
}