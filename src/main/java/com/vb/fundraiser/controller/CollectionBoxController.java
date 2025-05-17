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
    @DeleteMapping("/{id}")
    public ResponseEntity<String> unregisterBox(@PathVariable Long id) {
        boxService.unregisterBox(id);
        return ResponseEntity.ok("Box " + id + " successfully unregistered");
    }

    @Operation(summary = "Assign a collection box to a fundraising event")
    @PutMapping("/{id_box}/events/{id_event}")
    public ResponseEntity<CollectionBoxDTO> assignBoxToEvent(
            @PathVariable Long id_box,
            @PathVariable Long id_event
    ) {
        return ResponseEntity.ok(boxService.assignBoxToEvent(id_box, id_event));
    }

    @Operation(summary = "Add money to a collection box")
    @PutMapping("/{id}/add-money")
    public ResponseEntity<String> addMoney(
            @PathVariable Long id,
            @RequestBody @Valid AddMoneyRequest request
    ) {
        boxService.addMoney(id, request.currencyCode(), request.amount());
        return ResponseEntity.ok("Money successfully added to the box " + id);
    }

    @Operation(summary = "Empty the box (transfer money to assigned event)")
    @PostMapping("/{id}/empty")
    public ResponseEntity<String> emptyBox(@PathVariable Long id) {
        boxService.emptyBox(id);
        return ResponseEntity.ok("Box " + id + " successfully emptied");
    }
}
