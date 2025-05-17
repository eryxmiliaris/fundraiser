package com.vb.fundraiser.controller;

import com.vb.fundraiser.model.request.CreateEventRequest;
import com.vb.fundraiser.model.dto.FundraisingEventDTO;
import com.vb.fundraiser.service.FundraisingEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Fundraising Events", description = "Endpoints for managing fundraising events")
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class FundraisingEventController {
    private final FundraisingEventService eventService;

    @Operation(summary = "Create a new fundraising event")
    @PostMapping
    public ResponseEntity<FundraisingEventDTO> createEvent(
            @RequestBody @Valid CreateEventRequest request
    ) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @Operation(summary = "Get financial report for all fundraising events")
    @GetMapping
    public ResponseEntity<List<FundraisingEventDTO>> getFinancialReport() {
        return ResponseEntity.ok(eventService.getFinancialReport());
    }
}
