package com.vb.fundraiser.controller;

import com.vb.fundraiser.model.request.CreateEventRequest;
import com.vb.fundraiser.model.dto.FinancialReportEntryDTO;
import com.vb.fundraiser.model.dto.FundraisingEventDTO;
import com.vb.fundraiser.service.FundraisingEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class FundraisingEventController {
    private final FundraisingEventService eventService;

    @PostMapping
    public ResponseEntity<FundraisingEventDTO> createEvent(
            @RequestBody @Valid CreateEventRequest request
    ) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @GetMapping
    public ResponseEntity<List<FinancialReportEntryDTO>> getFinancialReport() {
        return ResponseEntity.ok(eventService.getFinancialReport());
    }
}
