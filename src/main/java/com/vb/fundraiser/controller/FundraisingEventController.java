package com.vb.fundraiser.controller;

import com.vb.fundraiser.model.request.CreateEventRequest;
import com.vb.fundraiser.model.dto.FundraisingEventDTO;
import com.vb.fundraiser.service.FundraisingEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Fundraising Events", description = "Endpoints for managing fundraising events")
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class FundraisingEventController {
    private final FundraisingEventService eventService;

    @Operation(summary = "Create a new fundraising event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or duplicate event name"),
            @ApiResponse(responseCode = "404", description = "Currency not found")
    })
    @PostMapping
    public ResponseEntity<FundraisingEventDTO> createEvent(
            @Parameter(
                    description = "Payload containing the event name and currency code",
                    required = true
            )
            @RequestBody @Valid CreateEventRequest request
    ) {
        return ResponseEntity.ok(eventService.createEvent(request.name(), request.currencyCode().toUpperCase()));
    }

    @Operation(summary = "Get paginated financial report for fundraising events")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated financial report returned successfully")
    })
    @GetMapping
    public ResponseEntity<Page<FundraisingEventDTO>> getFinancialReport(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort by field (e.g. name)", example = "name")
            @RequestParam(defaultValue = "name") String sort,

            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(eventService.getFinancialReport(page, size, sort, direction));
    }

    @Operation(summary = "Get financial report in HTML format for all events")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Financial report in HTML format returned successfully")
    })
    @GetMapping(value = "/table", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getReportAsHtml(
            @Parameter(description = "Field to sort by (e.g. name, currency, accountBalance)", example = "name")
            @RequestParam(defaultValue = "name") String sort,

            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(eventService.generateHtmlReport(sort, direction));
    }
}
