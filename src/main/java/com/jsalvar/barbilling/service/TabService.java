package com.jsalvar.barbilling.service;

import com.jsalvar.barbilling.dto.request.TabCreateRequestDto;
import com.jsalvar.barbilling.dto.request.TabSearchRequestDto;
import com.jsalvar.barbilling.entity.Tab;
import com.jsalvar.barbilling.entity.enums.TabStatus;

import java.util.List;

public interface TabService {
    Tab create(TabCreateRequestDto dto);
    Tab closeTab(String id);
    Tab cancelTab(String id);
    Tab findById(String id);
    List<Tab> findByStatus(TabStatus status);
    List<Tab> searchTab(TabSearchRequestDto dto);
}
