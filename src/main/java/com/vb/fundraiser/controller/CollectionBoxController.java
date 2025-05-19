package com.vb.fundraiser.controller;

import com.vb.fundraiser.model.request.AddMoneyRequest;
import com.vb.fundraiser.model.dto.CollectionBoxDTO;
import com.vb.fundraiser.service.CollectionBoxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Collection Boxes", description = "Endpoints for managing collection boxes")
@RestController
@RequestMapping("/api/v1/boxes")
@RequiredArgsConstructor
public class CollectionBoxController {
    private final CollectionBoxService boxService;

    @Operation(summary = "Register a new collection box")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Box registered successfully")
    })
    @PostMapping
    public ResponseEntity<CollectionBoxDTO> registerNewBox() {
        return ResponseEntity.ok(boxService.registerNewBox());
    }

    @Operation(summary = "List all collection boxes with pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Box list retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<Page<CollectionBoxDTO>> getAllBoxes(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String direction
    ) {
        return ResponseEntity.ok(boxService.getAllBoxes(page, size, direction));
    }

    @Operation(summary = "Unregister (remove) a collection box")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Box unregistered successfully"),
            @ApiResponse(responseCode = "404", description = "Box not found")
    })
    @DeleteMapping("/{boxId}")
    public ResponseEntity<String> unregisterBox(@PathVariable Long boxId) {
        boxService.unregisterBox(boxId);
        return ResponseEntity.ok("Box " + boxId + " successfully unregistered");
    }

    @Operation(summary = "Assign a collection box to a fundraising event")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Box assigned to event successfully"),
            @ApiResponse(responseCode = "400", description = "Box is already assigned or not empty"),
            @ApiResponse(responseCode = "404", description = "Box or event not found")
    })
    @PatchMapping("/{boxId}/assign")
    public ResponseEntity<CollectionBoxDTO> assignBoxToEvent(
            @PathVariable Long boxId,
            @RequestParam Long eventId
    ) {
        return ResponseEntity.ok(boxService.assignBoxToEvent(boxId, eventId));
    }

    @Operation(summary = "Add money to a collection box")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Money added successfully"),
            @ApiResponse(responseCode = "400", description = "Box is not assigned to an event"),
            @ApiResponse(responseCode = "404", description = "Box or currency not found")
    })
    @PutMapping("/{boxId}/add-money")
    public ResponseEntity<String> addMoney(
            @PathVariable Long boxId,
            @RequestBody @Valid AddMoneyRequest request
    ) {
        boxService.addMoney(boxId, request.currencyCode().toUpperCase(), request.amount());
        return ResponseEntity.ok("Money successfully added to the box " + boxId);
    }

    @Operation(summary = "Empty the box (transfer money to assigned event)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Box emptied and funds transferred successfully"),
            @ApiResponse(responseCode = "400", description = "Attempt to transfer money from an empty box or box is not assigned to an event"),
            @ApiResponse(responseCode = "404", description = "Box not found")
    })
    @PostMapping("/{boxId}/empty")
    public ResponseEntity<String> emptyBox(@PathVariable Long boxId) {
        boxService.emptyBox(boxId);
        return ResponseEntity.ok("Box " + boxId + " successfully emptied");
    }
}
