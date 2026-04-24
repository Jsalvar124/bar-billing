package com.jsalvar.barbilling.controller;

import com.jsalvar.barbilling.aspect.Loggable;
import com.jsalvar.barbilling.dto.request.TabCreateRequestDto;
import com.jsalvar.barbilling.dto.request.TabSearchRequestDto;
import com.jsalvar.barbilling.dto.response.TabResponseDto;
import com.jsalvar.barbilling.entity.Tab;
import com.jsalvar.barbilling.entity.enums.TabStatus;
import com.jsalvar.barbilling.service.TabService;
import jakarta.websocket.server.PathParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/tabs")
public class TabController {

    private final TabService tabService;

    public TabController(TabService tabService) {
        this.tabService = tabService;
    }

    @Loggable
    @PostMapping
    @PreAuthorize("hasRole('WAITER') or hasRole('ADMIN')")
    public ResponseEntity<TabResponseDto> create(@RequestBody TabCreateRequestDto dto) {
        Tab tab = tabService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toDto(tab));
    }

    @Loggable
    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('WAITER') or hasRole('ADMIN')")
    public ResponseEntity<TabResponseDto> closeTab(@PathVariable String id){
        Tab tab = tabService.closeTab(id);
        return ResponseEntity.ok().body(toDto(tab));
    }

    @Loggable
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('WAITER') or hasRole('ADMIN')")
    public ResponseEntity<TabResponseDto> cancelTab(@PathVariable String id){
        Tab tab = tabService.cancelTab(id);
        return ResponseEntity.ok().body(toDto(tab));
    }

    @GetMapping("/id")
    public ResponseEntity<TabResponseDto> findById(@PathVariable String id){
        Tab tab = tabService.findById(id);
        return ResponseEntity.ok().body(toDto(tab));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TabResponseDto>> findByStatus(@PathVariable TabStatus status){
        List<TabResponseDto> tabs = tabService.findByStatus(status).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(tabs);
    }

    @GetMapping
    public ResponseEntity<List<TabResponseDto>> searchTab(@ModelAttribute TabSearchRequestDto dto){
        List<TabResponseDto> tabs = tabService.searchTab(dto).stream().map(this::toDto).toList();
        return ResponseEntity.ok().body(tabs);
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
