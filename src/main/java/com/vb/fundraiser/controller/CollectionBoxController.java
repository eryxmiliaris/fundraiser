package com.vb.fundraiser.controller;

import com.vb.fundraiser.model.request.AddMoneyRequest;
import com.vb.fundraiser.model.dto.CollectionBoxDTO;
import com.vb.fundraiser.service.CollectionBoxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Collection Boxes", description = "Endpoints for managing collection boxes")
@RestController
@RequestMapping("/api/v1/boxes")
@RequiredArgsConstructor
public class CollectionBoxController {
    private final CollectionBoxService boxService;

    @Operation(summary = "Register a new collection box")
    @PostMapping
    public ResponseEntity<CollectionBoxDTO> registerNewBox() {
        return ResponseEntity.ok(boxService.registerNewBox());
    }

    @Operation(summary = "List all collection boxes")
    @GetMapping
    public ResponseEntity<List<CollectionBoxDTO>> getAllBoxes() {
        return ResponseEntity.ok(boxService.getAllBoxes());
    }

    @Operation(summary = "Unregister (remove) a collection box")
    @DeleteMapping("/{boxId}")
    public ResponseEntity<String> unregisterBox(@PathVariable Long boxId) {
        boxService.unregisterBox(boxId);
        return ResponseEntity.ok("Box " + boxId + " successfully unregistered");
    }

    @Operation(summary = "Assign a collection box to a fundraising event")
    @PutMapping("/{boxId}/events/{eventId}")
    public ResponseEntity<CollectionBoxDTO> assignBoxToEvent(
            @PathVariable Long boxId,
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(boxService.assignBoxToEvent(boxId, eventId));
    }

    @Operation(summary = "Add money to a collection box")
    @PutMapping("/{boxId}/add-money")
    public ResponseEntity<String> addMoney(
            @PathVariable Long boxId,
            @RequestBody @Valid AddMoneyRequest request
    ) {
        boxService.addMoney(boxId, request.currencyCode(), request.amount());
        return ResponseEntity.ok("Money successfully added to the box " + boxId);
    }

    @Operation(summary = "Empty the box (transfer money to assigned event)")
    @PostMapping("/{boxId}/empty")
    public ResponseEntity<String> emptyBox(@PathVariable Long boxId) {
        boxService.emptyBox(boxId);
        return ResponseEntity.ok("Box " + boxId + " successfully emptied");
    }
}
