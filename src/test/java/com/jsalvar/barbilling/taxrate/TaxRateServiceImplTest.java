package com.jsalvar.barbilling.taxrate;

import com.jsalvar.barbilling.dto.request.TaxRateCreateRequestDto;
import com.jsalvar.barbilling.dto.request.TaxRateUpdateRequestDto;
import com.jsalvar.barbilling.entity.TaxRate;
import com.jsalvar.barbilling.repository.TaxRateRepository;
import com.jsalvar.barbilling.service.impl.TaxRateServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxRateServiceImplTest {

    @Mock
    private TaxRateRepository taxRateRepository;

    @InjectMocks
    private TaxRateServiceImpl taxRateService;

    private TaxRate testTaxRate;

    @BeforeEach
    void setUp() {
        testTaxRate = TaxRate.builder()
                .id("tax-id-123")
                .name("IVA")
                .rate(new BigDecimal("0.16"))
                .build();
    }

    @Test
    void findAll_Success() {
        when(taxRateRepository.findAll()).thenReturn(List.of(testTaxRate));

        List<TaxRate> result = taxRateService.findAll();

        assertEquals(1, result.size());
        assertEquals("IVA", result.get(0).getName());
    }

    @Test
    void findAll_EmptyList() {
        when(taxRateRepository.findAll()).thenReturn(List.of());

        List<TaxRate> result = taxRateService.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_Success() {
        when(taxRateRepository.findById("tax-id-123")).thenReturn(Optional.of(testTaxRate));

        TaxRate result = taxRateService.findById("tax-id-123");

        assertNotNull(result);
        assertEquals("tax-id-123", result.getId());
    }

    @Test
    void findById_NotFound() {
        when(taxRateRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taxRateService.findById("unknown-id"));
    }

    @Test
    void create_Success() {
        TaxRateCreateRequestDto dto = new TaxRateCreateRequestDto("IVA", new BigDecimal("0.16"));
        when(taxRateRepository.existsByName("IVA")).thenReturn(false);
        when(taxRateRepository.save(any(TaxRate.class))).thenAnswer(invocation -> {
            TaxRate taxRate = invocation.getArgument(0);
            taxRate.setId("new-tax-id");
            return taxRate;
        });

        TaxRate result = taxRateService.create(dto);

        assertNotNull(result);
        assertEquals("IVA", result.getName());
        assertEquals(new BigDecimal("0.16"), result.getRate());
    }

    @Test
    void create_DuplicateName() {
        TaxRateCreateRequestDto dto = new TaxRateCreateRequestDto("IVA", new BigDecimal("0.16"));
        when(taxRateRepository.existsByName("IVA")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> taxRateService.create(dto));
    }

    @Test
    void create_InvalidRate_Negative() {
        TaxRateCreateRequestDto dto = new TaxRateCreateRequestDto("IVA", new BigDecimal("-0.1"));

        assertThrows(IllegalArgumentException.class, () -> taxRateService.create(dto));
    }

    @Test
    void create_InvalidRate_Over1() {
        TaxRateCreateRequestDto dto = new TaxRateCreateRequestDto("IVA", new BigDecimal("1.5"));

        assertThrows(IllegalArgumentException.class, () -> taxRateService.create(dto));
    }

    @Test
    void create_RateZero() {
        TaxRateCreateRequestDto dto = new TaxRateCreateRequestDto("Zero Tax", new BigDecimal("0"));
        when(taxRateRepository.existsByName("Zero Tax")).thenReturn(false);
        when(taxRateRepository.save(any(TaxRate.class))).thenAnswer(inv -> inv.getArgument(0));

        TaxRate result = taxRateService.create(dto);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getRate());
    }

    @Test
    void update_Success() {
        TaxRateUpdateRequestDto dto = new TaxRateUpdateRequestDto("IVA Actualizado", new BigDecimal("0.20"));
        when(taxRateRepository.findById("tax-id-123")).thenReturn(Optional.of(testTaxRate));
        when(taxRateRepository.existsByName("IVA Actualizado")).thenReturn(false);
        when(taxRateRepository.save(any(TaxRate.class))).thenAnswer(inv -> inv.getArgument(0));

        TaxRate result = taxRateService.update("tax-id-123", dto);

        assertEquals("IVA Actualizado", result.getName());
        assertEquals(new BigDecimal("0.20"), result.getRate());
    }

    @Test
    void update_NotFound() {
        TaxRateUpdateRequestDto dto = new TaxRateUpdateRequestDto("IVA", new BigDecimal("0.20"));
        when(taxRateRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taxRateService.update("unknown-id", dto));
    }

    @Test
    void update_PartialUpdate() {
        TaxRateUpdateRequestDto dto = new TaxRateUpdateRequestDto(null, new BigDecimal("0.18"));
        when(taxRateRepository.findById("tax-id-123")).thenReturn(Optional.of(testTaxRate));
        when(taxRateRepository.save(any(TaxRate.class))).thenAnswer(inv -> inv.getArgument(0));

        TaxRate result = taxRateService.update("tax-id-123", dto);

        assertEquals("IVA", result.getName());
        assertEquals(new BigDecimal("0.18"), result.getRate());
    }

    @Test
    void delete_Success() {
        when(taxRateRepository.findById("tax-id-123")).thenReturn(Optional.of(testTaxRate));
        doNothing().when(taxRateRepository).delete(testTaxRate);

        taxRateService.delete("tax-id-123");

        verify(taxRateRepository).delete(testTaxRate);
    }

    @Test
    void delete_NotFound() {
        when(taxRateRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taxRateService.delete("unknown-id"));
    }
}