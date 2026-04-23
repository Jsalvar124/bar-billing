package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.TabCreateRequestDto;
import com.jsalvar.barbilling.dto.response.TabResponseDto;
import com.jsalvar.barbilling.entity.Tab;
import com.jsalvar.barbilling.service.TabService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tabs")
public class TabController {

    private final TabService tabService;

    public TabController(TabService tabService) {
        this.tabService = tabService;
    }

    @Loggable
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TabResponseDto> create(@RequestBody TabCreateRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDto(tabService.create(dto)));
    }

    private TabResponseDto toDto(Tab tab) {
        return new TabResponseDto(
                tab.getId(),
                tab.getTable().getId(),
                tab.getTable().getNumber(),
                tab.getWaiter().getId(),
                tab.getWaiter().getName(),
                tab.getStatus(),
                tab.getOpenedAt(),
                tab.getClosedAt()
        );
    }
}
